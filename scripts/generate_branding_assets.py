"""Generate branding assets for the final logo migration."""
from __future__ import annotations

import os
from pathlib import Path

from PIL import Image

REPO = Path(__file__).resolve().parents[1]
ASSETS = Path(
    r"C:\Users\SOURISH DUTTA\.cursor\projects"
    r"\c-Users-SOURISH-DUTTA-AndroidStudioProjects-ZeroBook-Only-Android\assets"
)
DRAWABLE = REPO / "app" / "src" / "main" / "res" / "drawable"
RES = REPO / "app" / "src" / "main" / "res"

TRANSPARENT_SRC = next(
    p for p in ASSETS.iterdir() if p.name.endswith("770f2822-88a8-4fae-b0d9-48805371c081.png")
)
LAUNCHER_SRC = next(
    p for p in ASSETS.iterdir() if p.name.endswith("82ae6670-1d96-42b3-b6ee-0e19f1a4d6ea.png")
)

CREAM = (250, 248, 245, 255)


def is_filled(pixel: tuple[int, int, int, int]) -> bool:
    r, g, b, a = pixel
    return a > 128 and (r + g + b) < 384


def generate_vector_drawable(cropped: Image.Image) -> None:
    grid_size = 24
    small = cropped.resize((grid_size, grid_size), Image.Resampling.NEAREST)
    filled = {
        (x, y)
        for y in range(grid_size)
        for x in range(grid_size)
        if is_filled(small.getpixel((x, y)))
    }

    rects: list[tuple[int, int, int, int]] = []
    for y in range(grid_size):
        x = 0
        while x < grid_size:
            if (x, y) in filled:
                start = x
                while x < grid_size and (x, y) in filled:
                    x += 1
                rects.append((start, y, x, y + 1))
            else:
                x += 1

    path_parts = [
        f"M{x0},{y0}h{x1 - x0}v{y1 - y0}h{x0 - x1}z" for x0, y0, x1, y1 in rects
    ]
    path_data = " ".join(path_parts)
    vector_xml = f"""<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#000000"
        android:pathData="{path_data}" />
</vector>
"""
    out = DRAWABLE / "logo_mark.xml"
    out.write_text(vector_xml, encoding="utf-8")
    print(f"Wrote {out} ({len(rects)} rects)")


def square_rgba(image: Image.Image) -> Image.Image:
    side = max(image.size)
    square = Image.new("RGBA", (side, side), (0, 0, 0, 0))
    ox = (side - image.width) // 2
    oy = (side - image.height) // 2
    square.paste(image, (ox, oy), image)
    return square


def save_transparent_png(cropped: Image.Image) -> None:
    final = square_rgba(cropped).resize((512, 512), Image.Resampling.LANCZOS)
    out = DRAWABLE / "logo_transparent.png"
    final.save(out, optimize=True)
    print(f"Wrote {out} ({out.stat().st_size} bytes)")


def save_launcher_png(launcher: Image.Image) -> Path:
    bbox = launcher.getbbox()
    if bbox:
        launcher = launcher.crop(bbox)
    square = Image.new("RGBA", (max(launcher.size), max(launcher.size)), CREAM)
    ox = (square.width - launcher.width) // 2
    oy = (square.height - launcher.height) // 2
    square.paste(launcher, (ox, oy), launcher)
    final = square.resize((512, 512), Image.Resampling.LANCZOS).convert("RGB")
    out = DRAWABLE / "logo_icon.png"
    final.save(out, optimize=True, quality=95)
    print(f"Wrote {out} ({out.stat().st_size} bytes)")
    return out


def generate_mipmaps(launcher_path: Path) -> None:
    sizes = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }
    source = Image.open(launcher_path).convert("RGBA")
    for folder, size in sizes.items():
        out_dir = RES / folder
        out_dir.mkdir(parents=True, exist_ok=True)
        resized = source.resize((size, size), Image.Resampling.LANCZOS)
        for name in ("ic_launcher.png", "ic_launcher_round.png"):
            out = out_dir / name
            resized.save(out, optimize=True)
            print(f"Wrote {out}")


def generate_play_store_icon(launcher_path: Path) -> None:
    play_dir = REPO / "playstore"
    play_dir.mkdir(parents=True, exist_ok=True)
    out = play_dir / "icon_512x512.png"
    Image.open(launcher_path).convert("RGB").save(out, optimize=True, quality=95)
    print(f"Wrote {out} ({out.stat().st_size} bytes)")


def main() -> None:
    transparent = Image.open(TRANSPARENT_SRC).convert("RGBA")
    bbox = transparent.getbbox()
    if not bbox:
        raise RuntimeError("Transparent logo has no visible content.")
    cropped = transparent.crop(bbox)

    generate_vector_drawable(cropped)
    save_transparent_png(cropped)

    launcher = Image.open(LAUNCHER_SRC).convert("RGBA")
    launcher_path = save_launcher_png(launcher)
    generate_mipmaps(launcher_path)
    generate_play_store_icon(launcher_path)


if __name__ == "__main__":
    main()
