"""Generate Android launcher mipmaps/adaptive assets from a source icon photo."""
from __future__ import annotations

from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
SRC = Path(
  r"C:\Users\junai\.cursor\projects\c-Users-junai-Desktop-MusicPlayer\assets"
  r"\c__Users_junai_AppData_Roaming_Cursor_User_workspaceStorage_"
  r"747b5dce946f50a5ab7e5de1cf973014_images_Gemini_Generated_Image_"
  r"dlmfxedlmfxedlmf-eaa37ebe-a3e3-4c2a-875c-21eea99040ec.jpg"
)
RES = ROOT / "app" / "src" / "main" / "res"

MIPMAP_SIZES = {
  "mipmap-mdpi": 48,
  "mipmap-hdpi": 72,
  "mipmap-xhdpi": 96,
  "mipmap-xxhdpi": 144,
  "mipmap-xxxhdpi": 192,
}


def crop_icon_face(img: Image.Image) -> Image.Image:
  """Crop to the colored icon body and fill a square (no outer gray canvas)."""
  rgb = img.convert("RGB")
  arr = np.asarray(rgb).astype(np.float32)
  r, g, b = arr[:, :, 0], arr[:, :, 1], arr[:, :, 2]
  mx = np.maximum(np.maximum(r, g), b)
  mn = np.minimum(np.minimum(r, g), b)
  sat = np.where(mx == 0, 0, (mx - mn) / (mx + 1e-6))
  # Warm saturated face of the icon (excludes gray backdrop).
  is_face = sat > 0.22
  mask = Image.fromarray((is_face * 255).astype(np.uint8), mode="L")
  mask = mask.filter(ImageFilter.MaxFilter(21)).filter(ImageFilter.MinFilter(9))
  ys = np.where(np.any(np.asarray(mask) > 128, axis=1))[0]
  xs = np.where(np.any(np.asarray(mask) > 128, axis=0))[0]
  if len(ys) == 0 or len(xs) == 0:
    raise RuntimeError("Could not locate icon face in source image")

  # Inset to clear soft edge / shadow fringe.
  inset = max(6, int(min(img.width, img.height) * 0.012))
  y0, y1 = int(ys[0]) + inset, int(ys[-1]) - inset
  x0, x1 = int(xs[0]) + inset, int(xs[-1]) - inset
  cy, cx = (y0 + y1) // 2, (x0 + x1) // 2
  side = max(y1 - y0, x1 - x0)
  half = side // 2
  box = (cx - half, cy - half, cx - half + side, cy - half + side)
  return rgb.crop(box).convert("RGBA")


def make_legacy_rounded(icon: Image.Image, size: int, radius_ratio: float = 0.22) -> Image.Image:
  """Legacy launcher PNG with iOS/Android-style rounded corners on transparent bg."""
  base = icon.resize((size, size), Image.Resampling.LANCZOS).convert("RGBA")
  mask = Image.new("L", (size, size), 0)
  draw = ImageDraw.Draw(mask)
  radius = int(size * radius_ratio)
  draw.rounded_rectangle((0, 0, size - 1, size - 1), radius=radius, fill=255)
  out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
  out.paste(base, (0, 0), mask)
  return out


def main() -> None:
  raw = Image.open(SRC)
  face = crop_icon_face(raw)
  print(f"cropped face {face.size}")

  # Adaptive: full-bleed square; system applies the mask.
  adaptive = face.resize((432, 432), Image.Resampling.LANCZOS)
  bg_path = RES / "drawable" / "ic_launcher_background.png"
  adaptive.save(bg_path, optimize=True)
  print(f"wrote {bg_path}")

  fg = Image.new("RGBA", (432, 432), (0, 0, 0, 0))
  fg_path = RES / "drawable" / "ic_launcher_foreground.png"
  fg.save(fg_path, optimize=True)
  print(f"wrote {fg_path}")

  for folder, size in MIPMAP_SIZES.items():
    out = RES / folder / "ic_launcher.png"
    make_legacy_rounded(face, size).save(out, optimize=True)
    print(f"wrote {out} ({size}x{size})")


if __name__ == "__main__":
  main()
