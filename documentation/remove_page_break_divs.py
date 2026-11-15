import glob
import re

for filepath in sorted(glob.glob("documentation/chapters_*.py")):
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()
    
    # Replace `<div class="page-break"></div>\n` with empty string
    new_content = re.sub(r'<div class="page-break"></div>\s*', '', content)
    
    if new_content != content:
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(new_content)
        print(f"Cleaned page-break divs from {filepath}")
    else:
        print(f"No page-break divs found in {filepath}")
