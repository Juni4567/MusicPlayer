from pathlib import Path

from PIL import Image

ASSETS = Path(r"C:\Users\junai\.cursor\projects\c-Users-junai-Desktop-MusicPlayer\assets")
RES = Path(r"c:\Users\junai\Desktop\MusicPlayer\app\src\main\res")

REF = ASSETS / (
    "c__Users_junai_AppData_Roaming_Cursor_User_workspaceStorage_"
    "747b5dce946f50a5ab7e5de1cf973014_images_Gemini_Generated_Image_"
    "4t7kwu4t7kwu4t7k-1238dd4a-6db6-4408-a71c-9bbc75b5d738.jpg"
)
GEN = ASSETS / "a_music_launcher_full.jpg"
BG = ASSETS / "a_music_launcher_background.jpg"


def crop_icon(path: Path) -> Image.Image:
  im = Image.open(path).convert("RGB")
  w, h = im.size
  px = im.load()

  def is_bg(x: int, y: int) -> bool:
    r, g, b = px[x, y]
    return abs(r - g) < 12 and abs(g - b) < 12 and r > 180

  minx, miny, maxx, maxy = w, h, 0, 0
  step = max(1, w // 400)
  for y in range(0, h, step):
    for x in range(0, w, step):
      if not is_bg(x, y):
        minx = min(minx, x)
        miny = min(miny, y)
        maxx = max(maxx, x)
        maxy = max(maxy, y)

  pad = int(0.01 * w)
  minx = max(0, minx - pad)
  miny = max(0, miny - pad)
  maxx = min(w - 1, maxx + pad)
  maxy = min(h - 1, maxy + pad)

  cw, ch = maxx - minx + 1, maxy - miny + 1
  # Icon + label: keep top square of the icon width.
  if ch > cw * 1.15:
    side = cw
    maxy = miny + side - 1
  else:
    side = max(cw, ch)
    cx = (minx + maxx) // 2
    cy = (miny + maxy) // 2
    half = side // 2
    minx = max(0, cx - half)
    miny = max(0, cy - half)
    maxx = min(w - 1, minx + side - 1)
    maxy = min(h - 1, miny + side - 1)
    minx = max(0, maxx - side + 1)
    miny = max(0, maxy - side + 1)

  return im.crop((minx, miny, maxx + 1, maxy + 1)).resize(
    (1024, 1024), Image.Resampling.LANCZOS
  )


def main() -> None:
  src = GEN if GEN.exists() else REF
  icon = crop_icon(src)
  icon_path = ASSETS / "a_music_icon_1024.png"
  icon.save(icon_path, "PNG")
  print("saved", icon_path)

  if BG.exists():
    crop_icon(BG).save(ASSETS / "a_music_bg_1024.png", "PNG")

  sizes = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
  }
  for folder, size in sizes.items():
    dest = RES / folder / "ic_launcher.png"
    icon.resize((size, size), Image.Resampling.LANCZOS).save(dest, "PNG")
    print("wrote", dest)

  # Adaptive layers (API 26+)
  (RES / "drawable").mkdir(exist_ok=True)
  bg_src = ASSETS / "a_music_bg_1024.png"
  bg_img = Image.open(bg_src if bg_src.exists() else icon_path).convert("RGB")
  bg_img.resize((1080, 1080), Image.Resampling.LANCZOS).save(
    RES / "drawable" / "ic_launcher_background.png", "PNG"
  )
  # Full art as foreground; launcher mask applies squircle.
  icon.convert("RGBA").resize((1080, 1080), Image.Resampling.LANCZOS).save(
    RES / "drawable" / "ic_launcher_foreground.png", "PNG"
  )
  print("adaptive layers written")


if __name__ == "__main__":
  main()
