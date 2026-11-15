import pypdfium2 as pdfium

pdf = pdfium.PdfDocument(r'D:\Kamlesh_September_Oct_Pro_Java\SupportDesk\documentation\SupportDesk_Complete_Interview_Guide.pdf')
for i, page in enumerate(pdf):
    # Check if page has errors
    pass
print("PDFium loaded all pages without crashing.")
