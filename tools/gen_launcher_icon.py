"""
Render A-Music launcher assets from the SVG geometry (source SVGs live in drawable/).

Outputs:
  - drawable/ic_launcher_background.png
  - drawable/ic_launcher_foreground.png
  - mipmap-*/ic_launcher.png
  - tools/out previews (512 / 1024)
"""
from __future__ import annotations

from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app" / "src" / "main" / "res"
DRAWABLE = RES / "drawable"
ART = ROOT / "app" / "src" / "main" / "art" / "launcher"

MIPMAP_SIZES = {
  "mipmap-mdpi": 48,
  "mipmap-hdpi": 72,
  "mipmap-xhdpi": 96,
  "mipmap-xxhdpi": 144,
  "mipmap-xxxhdpi": 192,
}
ADAPTIVE_PX = 432


def _cubic(p0, p1, p2, p3, n: int = 14) -> list[tuple[float, float]]:
  pts = []
  for i in range(n + 1):
    t = i / n
    u = 1 - t
    x = u**3 * p0[0] + 3 * u**2 * t * p1[0] + 3 * u * t**2 * p2[0] + t**3 * p3[0]
    y = u**3 * p0[1] + 3 * u**2 * t * p1[1] + 3 * u * t**2 * p2[1] + t**3 * p3[1]
    pts.append((x, y))
  return pts


def flag_points(s: float) -> list[tuple[float, float]]:
  pts: list[tuple[float, float]] = []
  segs = [
    ((609, 236), (700, 234), (790, 270), (832, 340)),
    ((832, 340), (862, 390), (868, 450), (848, 505)),
    ((848, 505), (836, 536), (804, 548), (778, 528)),
    ((778, 528), (758, 512), (762, 480), (786, 458)),
    ((786, 458), (820, 424), (820, 372), (778, 336)),
    ((778, 336), (742, 304), (680, 280), (609, 278)),
  ]
  for i, (a, b, c, d) in enumerate(segs):
    chunk = _cubic(a, b, c, d, 16)
    pts += chunk if i == 0 else chunk[1:]
  return [(x * s, y * s) for x, y in pts]


def draw_note(size: int) -> Image.Image:
  """Match drawable/ic_launcher.svg note geometry."""
  s = size / 1024.0
  note = Image.new("RGBA", (size, size), (0, 0, 0, 0))
  d = ImageDraw.Draw(note)

  # Circle: M 448 792 A 176 → center (448, 616)
  cx, cy = 448 * s, 616 * s
  r_out, r_in = 176 * s, 84 * s
  d.ellipse([cx - r_out, cy - r_out, cx + r_out, cy + r_out], fill=(255, 255, 255, 255))

  # Punch hole
  hole = Image.new("L", (size, size), 0)
  ImageDraw.Draw(hole).ellipse(
    [cx - r_in, cy - r_in, cx + r_in, cy + r_in], fill=255
  )
  r, g, b, a = note.split()
  a_arr = np.array(a)
  a_arr[np.array(hole) > 0] = 0
  note = Image.merge("RGBA", (r, g, b, Image.fromarray(a_arr)))

  d = ImageDraw.Draw(note)
  x, y, w, h = 578 * s, 236 * s, 62 * s, 420 * s
  d.rounded_rectangle([x, y, x + w, y + h], radius=max(1, int(31 * s)), fill=(255, 255, 255, 255))
  d.polygon(flag_points(s), fill=(255, 255, 255, 255))

  # Soft drop shadow
  sa = note.split()[3]
  shadow = Image.new("RGBA", (size, size), (90, 14, 44, 0))
  shadow.putalpha(sa.point(lambda p: int(p * 0.30)))
  shadow = shadow.filter(ImageFilter.GaussianBlur(radius=max(1, int(9 * s))))

  out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
  out.alpha_composite(shadow, (int(6 * s), int(10 * s)))
  out.alpha_composite(note)
  return out


def draw_background(size: int) -> Image.Image:
  img = Image.new("RGB", (size, size))
  px = img.load()
  c0 = (0xD4, 0x14, 0x5A)
  c1 = (0xEF, 0x4E, 0x78)
  c2 = (0xFF, 0x9B, 0x6A)
  last = max(1, size - 1)
  for y in range(size):
    for x in range(size):
      t = (x + (last - y)) / (2 * last)
      if t < 0.42:
        u = t / 0.42
        col = tuple(int(c0[i] + (c1[i] - c0[i]) * u) for i in range(3))
      else:
        u = (t - 0.42) / 0.58
        col = tuple(int(c1[i] + (c2[i] - c1[i]) * u) for i in range(3))
      px[x, y] = col  # type: ignore[index]

  noise = np.random.default_rng(7).integers(0, 255, (size, size), dtype=np.uint8)
  grain = Image.fromarray(noise, mode="L").convert("RGBA")
  grain.putalpha(Image.new("L", (size, size), 14))
  out = img.convert("RGBA")
  return Image.alpha_composite(out, grain)


def compose_full(size: int) -> Image.Image:
  bg = draw_background(size)
  bg.alpha_composite(draw_note(size))
  return bg


def make_legacy(full: Image.Image, size: int) -> Image.Image:
  base = full.resize((size, size), Image.Resampling.LANCZOS)
  mask = Image.new("L", (size, size), 0)
  ImageDraw.Draw(mask).rounded_rectangle(
    (0, 0, size - 1, size - 1), radius=int(size * 0.22), fill=255
  )
  out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
  out.paste(base, (0, 0), mask)
  return out


def sync_layer_svgs() -> None:
  """Keep bg/fg SVG siblings aligned with the master icon SVG note + gradient."""
  bg = '''<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" width="1024" height="1024">
  <defs>
    <linearGradient id="bgGrad" gradientUnits="userSpaceOnUse" x1="60" y1="980" x2="980" y2="60">
      <stop offset="0%" stop-color="#D4145A"/>
      <stop offset="42%" stop-color="#EF4E78"/>
      <stop offset="100%" stop-color="#FF9B6A"/>
    </linearGradient>
    <filter id="grain" x="0%" y="0%" width="100%" height="100%">
      <feTurbulence type="fractalNoise" baseFrequency="0.9" numOctaves="2" stitchTiles="stitch" result="n"/>
      <feColorMatrix in="n" type="matrix"
        values="0 0 0 0 1  0 0 0 0 1  0 0 0 0 1  0 0 0 0.055 0" result="g"/>
      <feBlend in="SourceGraphic" in2="g" mode="soft-light"/>
    </filter>
  </defs>
  <rect width="1024" height="1024" fill="url(#bgGrad)" filter="url(#grain)"/>
</svg>
'''
  fg = '''<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" width="1024" height="1024">
  <defs>
    <filter id="noteShadow" x="-35%" y="-35%" width="170%" height="170%">
      <feDropShadow dx="6" dy="10" stdDeviation="8" flood-color="#5A0E2C" flood-opacity="0.30"/>
    </filter>
  </defs>
  <g filter="url(#noteShadow)" fill="#FFFFFF" fill-rule="evenodd">
    <path d="
      M 448 792
      A 176 176 0 1 1 448.01 792
      Z
      M 448 700
      A 84 84 0 1 0 448.01 700
      Z
    "/>
    <rect x="578" y="236" width="62" height="420" rx="31"/>
    <path d="
      M 609 236
      C 700 234 790 270 832 340
      C 862 390 868 450 848 505
      C 836 536 804 548 778 528
      C 758 512 762 480 786 458
      C 820 424 820 372 778 336
      C 742 304 680 280 609 278
      Z
    "/>
  </g>
</svg>
'''
  ART.mkdir(parents=True, exist_ok=True)
  (ART / "ic_launcher_background.svg").write_text(bg, encoding="utf-8")
  (ART / "ic_launcher_foreground.svg").write_text(fg, encoding="utf-8")


def main() -> None:
  sync_layer_svgs()

  bg = draw_background(ADAPTIVE_PX)
  fg = draw_note(ADAPTIVE_PX)
  bg.save(DRAWABLE / "ic_launcher_background.png", optimize=True)
  fg.save(DRAWABLE / "ic_launcher_foreground.png", optimize=True)
  print(f"wrote adaptive bg/fg @ {ADAPTIVE_PX}px")

  full = compose_full(1024)
  for folder, size in MIPMAP_SIZES.items():
    out = RES / folder / "ic_launcher.png"
    make_legacy(full, size).save(out, optimize=True)
    print(f"wrote {out}")

  out_dir = ROOT / "tools" / "out"
  out_dir.mkdir(exist_ok=True)
  full.save(out_dir / "ic_launcher_1024.png", optimize=True)
  full.resize((512, 512), Image.Resampling.LANCZOS).save(
    out_dir / "ic_launcher_512.png", optimize=True
  )
  print(f"wrote previews under {out_dir}")


if __name__ == "__main__":
  main()
