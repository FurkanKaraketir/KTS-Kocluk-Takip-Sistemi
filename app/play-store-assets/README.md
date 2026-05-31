# Play Store and launcher PNG assets

Generated from Android vector drawables in `app/src/main/res/drawable/`:

| Source drawable | Role |
|-----------------|------|
| `ic_launcher_background.xml` + `ic_launcher_foreground.xml` | Adaptive launcher (via `mipmap-anydpi-v26/ic_launcher.xml`) |
| `ic_app_logo.xml` | In-app / marketing logo (circular badge) |

Brand colors resolved from `app/src/main/res/values/colors.xml`.

## PNG outputs

| File | Size | Use |
|------|------|-----|
| `play-store-icon-512.png` | 512×512 | **Google Play Console → Store listing → App icon** (high-res icon) |
| `launcher-adaptive-1024.png` | 1024×1024 | Master adaptive icon (square, full bleed); marketing or manual downscale |
| `marketing-app-logo-1024.png` | 1024×1024 | Marketing / social (circular `ic_app_logo`) |
| `marketing-app-logo-512.png` | 512×512 | Smaller marketing asset |
| `play-store-feature-graphic-1024x500.png` | 1024×500 | **Google Play Console → Store listing → Feature graphic** |
| `mipmap-export/mipmap-*/ic_launcher.png` | 48–192 px | Legacy launcher densities (see below) |

### Mipmap export densities

| Folder | `ic_launcher.png` size |
|--------|-------------------------|
| `mipmap-mdpi` | 48×48 |
| `mipmap-hdpi` | 72×72 |
| `mipmap-xhdpi` | 96×96 |
| `mipmap-xxhdpi` | 144×144 |
| `mipmap-xxxhdpi` | 192×192 |

These are **not** copied into `app/src/main/res/mipmap-*` automatically. To refresh in-app legacy icons, copy each `mipmap-export/mipmap-*/ic_launcher.png` to the matching `app/src/main/res/mipmap-*/` folder (Android Studio **File → New → Image Asset** is an alternative).

## Regenerate

Feature graphic source: `source/play-store-feature-graphic.svg` (brand gradient, logo, title).

Intermediate SVGs (color-resolved from vectors) live in `source/`. From repo root:

```powershell
cd app/play-store-assets/scripts
npm install
node render.mjs
```

Requires Node.js and `@resvg/resvg-js` (see `scripts/package.json`).

### Method

1. Android vector XML was converted manually to standard SVG (`source/*.svg`), replacing `@color/*` with hex from `colors.xml`.
2. PNGs are rasterized with [resvg](https://github.com/RazrFalcon/resvg) via `@resvg/resvg-js` (Node).

Other tools tried on this machine: **cairosvg** (needs libcairo DLL, not installed); **ImageMagick / Inkscape / rsvg-convert** (not on PATH). **aapt2** compiles vectors for APKs but does not export arbitrary-size PNGs for store assets.

### Play Store upload

- **App icon (512×512)**: upload `play-store-icon-512.png`.
- **Feature graphic (1024×500)**: upload `play-store-feature-graphic-1024x500.png`. PNG or JPEG, max 15 MB; use exact dimensions (no rounded corners).
- Use PNG, 32-bit, max 1024 KB; no rounded corners (Play applies masking).
- Feature graphic, screenshots, and privacy policy are separate Play Console assets.

### Store listing copy (Turkish)

| File | Play Console field | Limit |
|------|-------------------|-------|
| `play-store-short-description-tr.txt` | Short description | 80 characters |
| `play-store-full-description-tr.txt` | Full description | 4000 characters |

Paste plain text as-is into **Store listing → Main store listing**.

### Screenshots (phone / tablet)

Upload **2–8** screenshots per device type in Play Console (JPEG or 24-bit PNG; portrait phone assets are usually 9:16). Store finished files under `screenshots/` (e.g. `screenshots/phone/`) when ready for upload.


