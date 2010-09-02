#!/bin/bash
pdflatex doc.tex
makeindex doc.nlo -s /usr/share/texmf-texlive/makeindex/nomencl/nomencl.ist -o doc.nls
pdflatex doc.tex