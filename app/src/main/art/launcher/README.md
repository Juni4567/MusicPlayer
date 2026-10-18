# A-Music launcher icon

Vector source of truth for the app icon.

| File | Purpose |
|------|---------|
| `ic_launcher.svg` | Full icon (gradient + note) |
| `ic_launcher_background.svg` | Adaptive background (gradient) |
| `ic_launcher_foreground.svg` | Adaptive foreground (note) |

Regenerate Android PNGs (mipmaps + adaptive drawables):

```bash
python tools/gen_launcher_icon.py
```

Outputs:

- `res/drawable/ic_launcher_background.png`
- `res/drawable/ic_launcher_foreground.png`
- `res/mipmap-*/ic_launcher.png`
- `tools/out/ic_launcher_512.png` / `ic_launcher_1024.png` (previews)
