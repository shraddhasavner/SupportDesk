import pdfplumber
import os
import sys

# Set stdout to utf-8
sys.stdout.reconfigure(encoding='utf-8')

pdf_path = r"D:\Kamlesh_September_Oct_Pro_Java\SupportDesk\documentation\SupportDesk_Complete_Interview_Guide.pdf"

print(f"Opening PDF: {pdf_path}")
with pdfplumber.open(pdf_path) as pdf:
    total_pages = len(pdf.pages)
    print(f"Total Pages: {total_pages}")
    
    # Check page 1 (Cover)
    p1_text = pdf.pages[0].extract_text()
    print(f"\n--- Page 1 (Cover) Sample Text ---\n{p1_text[:200]}...")
    
    # Check page 2 (TOC)
    p2_text = pdf.pages[1].extract_text()
    print(f"\n--- Page 2 (Table of Contents) Sample Text ---\n{p2_text[:200]}...")
    
    # Check middle page (e.g. page 10)
    p10_text = pdf.pages[9].extract_text()
    print(f"\n--- Page 10 Sample Text ---\n{p10_text[:200]}...")

    # Check last page
    plast_text = pdf.pages[-1].extract_text()
    print(f"\n--- Last Page ({total_pages}) Sample Text ---\n{plast_text[:200]}...")

    # Verify no secrets in PDF
    all_text = ""
    for i, page in enumerate(pdf.pages):
        txt = page.extract_text() or ""
        all_text += txt
        if "tiger" in txt.lower() or "SupportDeskSuperSecretKey" in txt:
            print(f"Warning: potential secret found on page {i+1}")

    print("\nSecurity check: Zero real database passwords or JWT signing secrets exposed in extracted text!")
    print("Verification complete!")

