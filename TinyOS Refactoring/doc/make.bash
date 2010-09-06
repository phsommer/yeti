#!/bin/bash
dia --export=menuStructure.png -t png menuStructure.dia
dia --export=AstAnalyzerTypesHierarchy.png -t png AstAnalyzerTypesHierarchy.dia
dia --export=interactionDiagramRefactoring.png -t png interactionDiagramRefactoring.dia 
dia --export=classDiagrammProcessorBasedRefactoring.png -t png classDiagrammProcessorBasedRefactoring.dia
pdflatex doc.tex
makeindex doc.nlo -s /usr/share/texmf-texlive/makeindex/nomencl/nomencl.ist -o doc.nls
pdflatex doc.tex