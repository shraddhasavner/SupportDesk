import pypdfium2 as pdfium
import os
import sys
from PIL import Image

sys.stdout.reconfigure(encoding='utf-8')

pdf_path = r"D:\Kamlesh_September_Oct_Pro_Java\SupportDesk\documentation\SupportDesk_Complete_Interview_Guide.pdf"
if not os.path.exists(pdf_path):
    print("PDF does not exist!")
    sys.exit(1)

size_bytes = os.path.getsize(pdf_path)
print(f"File exists: {pdf_path}")
print(f"File size: {size_bytes} bytes ({size_bytes / (1024*1024):.2f} MB)")

pdf = pdfium.PdfDocument(pdf_path)
total_pages = len(pdf)
print(f"Total Pages in PDF: {total_pages}")

render_dir = r"D:\Kamlesh_September_Oct_Pro_Java\SupportDesk\documentation\rendered_pages"
os.makedirs(render_dir, exist_ok=True)

test_pages = [1, 2, 3, 4, 10, 20, 40, total_pages]
for page_num in test_pages:
    if page_num <= total_pages:
        page = pdf[page_num - 1]
        pil_img = page.render(scale=1.5).to_pil()
        out_img = os.path.join(render_dir, f"page_{page_num}.png")
        pil_img.save(out_img)
        
        # Check pixel content
        # Convert to grayscale to check if all pixels are white (255)
        gray = pil_img.convert("L")
        min_v, max_v = gray.getextrema()
        is_all_white = (min_v == 255 and max_v == 255)
        
        # Also extract text via pypdfium2 textpage
        textpage = page.get_textpage()
        text = textpage.get_text_range()
        
        print(f"Page {page_num:02d}: Extrema(min={min_v}, max={max_v}), AllWhite={is_all_white}, TextLen={len(text.strip())}, TextPreview={repr(text[:60].strip())}")
