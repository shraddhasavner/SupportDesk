from PIL import Image
import os

render_dir = r"D:\Kamlesh_September_Oct_Pro_Java\SupportDesk\documentation\rendered_pages"
for fname in sorted(os.listdir(render_dir)):
    if fname.endswith(".png"):
        im = Image.open(os.path.join(render_dir, fname))
        colors = im.getcolors(maxcolors=256)
        print(f"{fname}: size={im.size}, mode={im.mode}, unique_colors={'<=256' if colors else '>256'}")
