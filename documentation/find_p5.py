import re

pdf_path = r"D:\Kamlesh_September_Oct_Pro_Java\SupportDesk\documentation\SupportDesk_Complete_Interview_Guide.pdf"
with open(pdf_path, 'rb') as f:
    content = f.read()

# Find occurrences of P5
matches = [m.start() for m in re.finditer(b'/P5', content)]
print(f"Found {len(matches)} occurrences of /P5")

for idx in matches:
    start = max(0, idx - 80)
    end = min(len(content), idx + 80)
    print(f"Context at {idx}:")
    print(content[start:end])
    print("-" * 50)
