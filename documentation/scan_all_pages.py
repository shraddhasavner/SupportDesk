import pypdfium2 as pdfium
import os
import sys

sys.stdout.reconfigure(encoding='utf-8')

pdf_path = r"D:\Kamlesh_September_Oct_Pro_Java\SupportDesk\documentation\SupportDesk_Complete_Interview_Guide.pdf"
pdf = pdfium.PdfDocument(pdf_path)

print(f"Scanning all {len(pdf)} pages for content length...")
almost_empty_pages = []
for i, page in enumerate(pdf):
    text = page.get_textpage().get_text_range().strip()
    # Header and footer alone is ~107 characters
    if len(text) < 150:
        almost_empty_pages.append((i + 1, len(text), text[:80].replace('\r', ' ').replace('\n', ' ')))

print(f"Total pages: {len(pdf)}")
print(f"Pages with < 150 characters (basically blank or header/footer only): {len(almost_empty_pages)}")
for p, length, sample in almost_empty_pages:
    print(f"  Page {p:02d} (len={length:3d}): {sample}")
