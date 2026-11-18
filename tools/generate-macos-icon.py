"""Wrap the existing application PNG in an ICNS container for jpackage."""
from pathlib import Path
import struct
import sys

source, destination = map(Path, sys.argv[1:])
image = source.read_bytes()
if image[:8] != b'\x89PNG\r\n\x1a\n' or struct.unpack('>II', image[16:24]) != (256, 256):
    raise ValueError('Expected the existing 256 x 256 PNG application icon')
payload = b'ic08' + struct.pack('>I', len(image) + 8) + image
destination.parent.mkdir(parents=True, exist_ok=True)
destination.write_bytes(b'icns' + struct.pack('>I', len(payload) + 8) + payload)
