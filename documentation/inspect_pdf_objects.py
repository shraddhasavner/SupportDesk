import pdfplumber

pdf_path = r"D:\Kamlesh_September_Oct_Pro_Java\SupportDesk\documentation\SupportDesk_Complete_Interview_Guide.pdf"
with pdfplumber.open(pdf_path) as pdf:
    for i in range(min(5, len(pdf.pages))):
        page = pdf.pages[i]
        print(f"--- Page {i+1} Objects ---")
        print(f"  Chars: {len(page.chars)}")
        print(f"  Lines: {len(page.lines)}")
        print(f"  Rects: {len(page.rects)}")
        print(f"  Curves: {len(page.curves)}")
        print(f"  Images: {len(page.images)}")
        if page.chars:
            font_names = set(c.get('fontname') for c in page.chars)
            print(f"  Fonts: {font_names}")
            sample = "".join(c['text'] for c in page.chars[:50])
            print(f"  Text: {sample}")
