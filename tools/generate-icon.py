"""Rebuild the project's original block icon (PNG/ICO/SVG); Python standard library only."""
from pathlib import Path
import struct
import zlib

TARGET = Path(__file__).resolve().parents[1] / "app/src/main/resources/com/team10/tetris/icons"
TILES = [(40, 48, "#A45ADD"), (100, 48, "#A45ADD"), (160, 48, "#A45ADD"),
         (100, 108, "#22C7E8"), (100, 168, "#22C7E8")]


def inside(x, y, left, top, width, radius):
    if not (left <= x < left + width and top <= y < top + width):
        return False
    cx = max(left + radius, min(x, left + width - radius))
    cy = max(top + radius, min(y, top + width - radius))
    return (x - cx) ** 2 + (y - cy) ** 2 <= radius ** 2


def rgba(x, y):
    color = "#0B1220" if inside(x, y, 8, 8, 240, 44) else None
    for left, top, tile in TILES:
        if inside(x, y, left, top, 56, 7):
            color = tile
    return tuple(bytes.fromhex(color[1:])) + (255,) if color else (0, 0, 0, 0)


def chunk(kind, data):
    return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data))


def png(size):
    rows = bytearray()
    for y in range(size):
        rows.append(0)
        for x in range(size):
            # Four samples per pixel keep the small icon's curved edges readable.
            samples = [rgba((x + dx) * 256 / size, (y + dy) * 256 / size)
                       for dx, dy in ((.25, .25), (.75, .25), (.25, .75), (.75, .75))]
            rows.extend(round(sum(sample[c] for sample in samples) / 4) for c in range(4))
    return (b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", size, size, 8, 6, 0, 0, 0))
            + chunk(b"IDAT", zlib.compress(bytes(rows), 9)) + chunk(b"IEND", b""))


if __name__ == "__main__":
    TARGET.mkdir(parents=True, exist_ok=True)
    sizes = (16, 32, 48, 64, 128, 256)
    images = [png(size) for size in sizes]
    offset = 6 + 16 * len(sizes)
    entries = bytearray()
    for size, data in zip(sizes, images):
        entries.extend(struct.pack("<BBBBHHII", size % 256, size % 256, 0, 0, 1, 32, len(data), offset))
        offset += len(data)
    (TARGET / "tetris.ico").write_bytes(struct.pack("<HHH", 0, 1, len(sizes)) + entries + b"".join(images))
    (TARGET / "tetris.png").write_bytes(images[-1])
    svg = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 256 256">\n'
    svg += '<rect x="8" y="8" width="240" height="240" rx="44" fill="#0B1220"/>\n'
    svg += ''.join(f'<rect x="{x}" y="{y}" width="56" height="56" rx="7" fill="{color}"/>\n'
                   for x, y, color in TILES)
    (TARGET / "tetris.svg").write_text(svg + '</svg>\n', encoding="utf-8")
    print(f"Generated PNG, SVG and six-size ICO in {TARGET}")
