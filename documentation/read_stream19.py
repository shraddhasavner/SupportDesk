import zlib

with open(r'D:\Kamlesh_September_Oct_Pro_Java\SupportDesk\documentation\SupportDesk_Complete_Interview_Guide.pdf', 'rb') as f:
    data = f.read()

idx = data.find(b'19 0 obj')
stream_idx = data.find(b'stream', idx)
# Find newline after stream
newline_idx = data.find(b'\n', stream_idx)
stream_start = newline_idx + 1
stream_end = data.find(b'endstream', stream_start)
compressed = data[stream_start:stream_end].strip()

try:
    decompressed = zlib.decompress(compressed)
    print("Stream 19 decompressed:")
    print(decompressed[:400].decode('latin1', errors='ignore'))
except Exception as e:
    # Try raw deflate
    decompressed = zlib.decompress(compressed, -15)
    print(decompressed[:400].decode('latin1', errors='ignore'))
