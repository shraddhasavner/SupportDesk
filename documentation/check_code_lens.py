import glob
import re

for f in glob.glob('documentation/chapters_*.py'):
    txt = open(f, encoding='utf-8').read()
    codes = re.findall(r'<pre class="code-block">(.*?)</pre>', txt, re.DOTALL)
    for i, c in enumerate(codes):
        lines = c.count('\n')
        if lines > 40:
            print(f'{f} Code block {i+1} has {lines} lines!')
