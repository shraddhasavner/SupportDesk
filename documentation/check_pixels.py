from PIL import Image
import os

render_dir = r"D:\Kamlesh_September_Oct_Pro_Java\SupportDesk\documentation\rendered_pages"
for p in [1, 2, 3, 4, 10]:
    img_path = os.path.join(render_dir, f"page_{p}.png")
    if os.path.exists(img_path):
        im = Image.open(img_path)
        # Check non-white pixels
        gray = im.convert("L")
        non_white = sum(1 for pixel in gray.getdata() if pixel < 250)
        total = gray.size[0] * gray.size[1]
        print(f"Page {p:02d}: {non_white} / {total} non-white pixels ({non_white/total*100:.2f}%)")
