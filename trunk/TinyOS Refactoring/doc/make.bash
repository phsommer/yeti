#!/bin/bash
dia --export=menuStructure.png -t png menuStructure.dia
pdflatex doc.tex
makeindex doc.nlo -s /usr/share/texmf-texlive/makeindex/nomencl/nomencl.ist -o doc.nls
pdflatex doc.tex