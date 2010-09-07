#!/bin/bash

# Converting DIA-Files to PNG-Files
for i in *.dia; do 
    dia --export=${i%.dia}.png -t png "${i}"
done

pdflatex doc.tex
makeindex doc.nlo -s /usr/share/texmf-texlive/makeindex/nomencl/nomencl.ist -o doc.nls
pdflatex doc.tex