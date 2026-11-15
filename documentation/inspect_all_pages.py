import pypdfium2 as pdfium
import sys

sys.stdout.reconfigure(encoding='utf-8')

pdf_path = r"D:\Kamlesh_September_Oct_Pro_Java\SupportDesk\documentation\SupportDesk_Complete_Interview_Guide.pdf"
doc = pdfium.PdfDocument(pdf_path)

print(f"Total Pages: {len(doc)}")
chapter_pages = {}

for i, page in enumerate(doc):
    textpage = page.get_textpage()
    text = textpage.get_text_range()
    
    # Check for chapter title
    lines = [l.strip() for l in text.splitlines() if l.strip()]
    first_line = lines[0] if lines else "EMPTY"
    
    for line in lines[:5]:
        if "Chapter " in line or "Table of Contents" in line or "Appendix" in line:
            chapter_pages[line] = i + 1
            break
            
    print(f"Page {i+1:02d}: {len(text):4d} chars | Start: {first_line[:50]}")

print("\n--- Detected Chapter Start Pages ---")
for ch, p in chapter_pages.items():
    print(f"{ch} -> Page {p}")
