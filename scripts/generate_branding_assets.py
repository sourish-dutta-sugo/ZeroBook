"""Generate in-app and launcher branding assets from the approved source files."""
from __future__ import annotations

from pathlib import Path

from PIL import Image

REPO = Path(__file__).resolve().parents[1]
DRAWABLE = REPO / "app" / "src" / "main" / "res" / "drawable"
RES = REPO / "app" / "src" / "main" / "res"
STORES = REPO / "stores"

TRANSPARENT_SRC = Path(r"C:\Users\SOURISH DUTTA\Downloads\2nd logo.png")
LAUNCHER_SRC = Path(r"C:\Users\SOURISH DUTTA\Downloads\3rd logo.jpg")

CREAM = (250, 248, 245, 255)
TRANSPARENT_CANVAS = 512
TRANSPARENT_CONTENT_RATIO = 0.86
ICON_CANVAS = 512
ICON_IMAGE_RATIO = 0.82


def is_filled(pixel: tuple[int, int, int, int]) -> bool:
    r, g, b, a = pixel
    return a > 128 and (r + g + b) < 384


def crop_visible_rgba(image: Image.Image) -> Image.Image:
    bbox = image.getbbox()
    if not bbox:
        raise RuntimeError("Image has no visible pixels.")
    return image.crop(bbox)


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


def fit_on_canvas(
    image: Image.Image,
    canvas_size: int,
    content_ratio: float,
    background: tuple[int, int, int, int],
) -> Image.Image:
    target_side = max(1, round(canvas_size * content_ratio))
    scale = min(target_side / image.width, target_side / image.height)
    resized = image.resize(
        (max(1, round(image.width * scale)), max(1, round(image.height * scale))),
        Image.Resampling.LANCZOS,
    )
    canvas = Image.new("RGBA", (canvas_size, canvas_size), background)
    offset = (
        (canvas_size - resized.width) // 2,
        (canvas_size - resized.height) // 2,
    )
    canvas.paste(resized, offset, resized)
    return canvas


def save_transparent_png(cropped: Image.Image) -> None:
    final = fit_on_canvas(
        cropped,
        canvas_size=TRANSPARENT_CANVAS,
        content_ratio=TRANSPARENT_CONTENT_RATIO,
        background=(0, 0, 0, 0),
    )
    out = DRAWABLE / "logo_transparent.png"
    final.save(out, optimize=True)
    print(f"Wrote {out} ({out.stat().st_size} bytes)")


def save_launcher_png(source: Image.Image) -> Path:
    final = fit_on_canvas(
        source.convert("RGBA"),
        canvas_size=ICON_CANVAS,
        content_ratio=ICON_IMAGE_RATIO,
        background=CREAM,
    ).convert("RGB")
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


def generate_store_icon(launcher_path: Path) -> None:
    STORES.mkdir(parents=True, exist_ok=True)
    out = STORES / "icon_512x512.png"
    Image.open(launcher_path).convert("RGB").save(out, optimize=True, quality=95)
    print(f"Wrote {out} ({out.stat().st_size} bytes)")


def main() -> None:
    transparent = crop_visible_rgba(Image.open(TRANSPARENT_SRC).convert("RGBA"))
    generate_vector_drawable(transparent)
    save_transparent_png(transparent)

    launcher_path = save_launcher_png(Image.open(LAUNCHER_SRC))
    generate_mipmaps(launcher_path)
    generate_store_icon(launcher_path)


if __name__ == "__main__":
    main()
