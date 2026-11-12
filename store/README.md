# Google Play store assets

| File | Use | Specification |
|---|---|---|
| `icon-512.png` | App icon | 512×512 PNG, no transparency, < 1 MB |
| `feature-graphic-1024x500.png` | Feature graphic | 1024×500 PNG |
| `screenshots/phone-01-setup.png` | Phone screenshot | 1080×1920 |
| `screenshots/phone-02-timer.png` | Phone screenshot | 1080×1920 |
| `screenshots/phone-03-paused.png` | Phone screenshot | 1080×1920 |
| `listing-en-US.md` | Store listing copy | Title, short description and full description |

The icon and feature graphic are generated from the SVG sources with:

```bash
rsvg-convert -w 512 -h 512 store/icon.svg -o store/icon-512.png
rsvg-convert -w 1024 -h 500 store/feature-graphic.svg -o store/feature-graphic-1024x500.png
```

The screenshots are copied from `docs/` and show only the app interface, as required by Google Play.
