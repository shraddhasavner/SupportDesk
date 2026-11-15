import glob
import re

for filepath in glob.glob("documentation/chapters_*.py"):
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()
    
    pb_matches = [m.start() for m in re.finditer(r'<div class="page-break"></div>', content)]
    h1_matches = [m.start() for m in re.finditer(r'class="chapter-title"', content)]
    print(f"{filepath}:")
    print(f"  Total page-break divs: {len(pb_matches)}")
    print(f"  Total chapter-title h1s: {len(h1_matches)}")
    
    # Check what immediately follows each page-break div
    for m in re.finditer(r'<div class="page-break"></div>\s*([^\n<]+|<[^>]+>)', content):
        print(f"    Followed by: {repr(m.group(1)[:60])}")
