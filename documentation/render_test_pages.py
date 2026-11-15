import pypdfium2 as pdfium
import os

pdf_path = r"D:\Kamlesh_September_Oct_Pro_Java\SupportDesk\documentation\SupportDesk_Complete_Interview_Guide.pdf"
out_dir = r"D:\Kamlesh_September_Oct_Pro_Java\SupportDesk\documentation\rendered_pages"
os.makedirs(out_dir, exist_ok=True)

doc = pdfium.PdfDocument(pdf_path)

pages_to_render = [1, 2, 3, 9, 11, 13, 24, 36, 40, 48]

for p_num in pages_to_render:
    page = doc[p_num - 1]
    # Render at 150 DPI (scale=2.08)
    bitmap = page.render(scale=2.08)
    pil_image = bitmap.to_pil()
    img_path = os.path.join(out_dir, f"page_{p_num:02d}.png")
    pil_image.save(img_path)
    print(f"Rendered Page {p_num:02d} -> {img_path} ({pil_image.size[0]}x{pil_image.size[1]} px, mode={pil_image.mode})")

print("Rendering complete!")
