package tinyos.yeti.editors.formatter;

import java.util.ArrayList;
import java.util.LinkedList;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;

import tinyos.yeti.TinyOSPlugin;

/**
 * 
 * @author Jianyuan Li
 *
 */

public class NesCIndenterStrategy extends ContextBasedFormattingStrategy {
	private LinkedList<IFormattingContext> formattingContexts = new LinkedList<IFormattingContext>();
	private IFormattingContext currentContext;
	
	private Document doc;
	private static final String INDENT = "\t";
	private int[] indentList = {};
	private ArrayList<Position> positionList = new ArrayList<Position>();

	@Override
	public void formatterStarts(IFormattingContext context) {
		super.formatterStarts(context);
		formattingContexts.add(context);
	}

	@Override
	public void formatterStops() {
		super.formatterStops();
		formattingContexts.removeLast();
	}

	@Override
	public void format() {
		super.format();
		currentContext = formattingContexts.getLast();

		IDocument document = (IDocument) currentContext
				.getProperty(FormattingContextProperties.CONTEXT_MEDIUM);
		boolean wholeDocument = Boolean.TRUE.equals(currentContext
				.getProperty(FormattingContextProperties.CONTEXT_DOCUMENT));
		IRegion region = null;
		if (wholeDocument)
			region = new Region(0, document.getLength());
		else
			region = (IRegion) currentContext
					.getProperty(FormattingContextProperties.CONTEXT_REGION);

		indent(document, region);
	}

	public void indent(IDocument document, IRegion region) {
		doc = new Document(document.get());
		Position position = new Position(region.getOffset(), region.getLength());
		int[] indentList = {};
		try {
			doc.addPosition(position);
			ArrayList<Position> positionList = getCommentsAndStrings(doc);
			indentList = indentScanner(doc, positionList);
			indentation(doc, position, indentList);
			commentIndentation(doc, position, indentList, positionList);
			document.replace(0, document.getLength(), doc.get());
			
			this.indentList = indentList;
			this.positionList = positionList;
		} catch (BadLocationException e) {
			TinyOSPlugin.log(e);
		}
	}
	
	private int[] indentScanner(IDocument document, ArrayList<Position> positionList) {
		int lineTotal = document.getNumberOfLines();
		int[] indentList = new int[lineTotal];
		for (int i = 0; i < lineTotal; i++) {
			indentList[i] = 0;
		}

		indentList = braceScanner(document, indentList, positionList);
		indentList = keywordScanner(document, indentList, positionList);
		
		return indentList;
	}	
	
	private ArrayList<Position> getCommentsAndStrings(IDocument document) {
		ArrayList<Position> list = new ArrayList<Position>();
		FindReplaceDocumentAdapter findReplaceDocumentAdapter = new FindReplaceDocumentAdapter(document);
		
		try {
			IRegion commentOrString = findReplaceDocumentAdapter.find(0, "//|/\\*|\"|\'", true, false, false, true);

			while (commentOrString != null) {
				int offset = commentOrString.getOffset();
				Position position = new Position(0);
				
				boolean toList = true;
				
				if(document.getChar(offset) == '\'') {		// char c1 = '"', c2 = '\"';
					if(document.getChar(offset + 1) == '\"') {
						offset += 2;	// c1.length();
						toList = false;
					}
					else if(document.getChar(offset + 1) == '\\' && document.getChar(offset + 2) == '\"'){
						offset += 3;	// c2.length();
						toList = false;
					}
				}
				else if (document.getChar(offset + 1) == '/') {		// single-line comment
					int len = 0;
					if(document.getLineOfOffset(offset) == document.getNumberOfLines() - 1)
						len = document.getLength() - offset;
					else						
						len = document.getLineOffset(document.getLineOfOffset(offset) + 1) - offset - 1;
					position.setOffset(offset);
					position.setLength(len);
					
					offset += len - 1;
				}
				else if (document.getChar(offset + 1) == '*') {		// multiline comment
					commentOrString = findReplaceDocumentAdapter.find(offset + 2, "*/", true, false, false, false);
					int len = commentOrString.getOffset() + 1 - offset + 1;
					position.setOffset(offset);
					position.setLength(len);
					offset += len - 1;
				}
				else {		// string
					commentOrString = findReplaceDocumentAdapter.find(offset + 1, "\"", true, false, false, false);
					int len = commentOrString.getOffset() - offset + 1;
					position.setOffset(offset);
					position.setLength(len);
					offset += len - 1;
				}
				
				if (toList) {
					document.addPosition(position);
					list.add(position);
				}
				
				if(offset + 1 > document.getLength() - 1)
					return list;
				commentOrString = findReplaceDocumentAdapter.find(offset + 1, "//|/\\*|\"|\'", true, false, false, true);				
			}
			
			return list;
		
		} catch (BadLocationException e) {
			TinyOSPlugin.log(e);
		}
		
		return list;
	
	}	
	
	private void commentIndentation(IDocument document, Position position,
			int[] indentList, ArrayList<Position> positionList) {
		try {
			int selectionFirstLine = document.getLineOfOffset(position.offset);
			int selectionLastLine = document.getLineOfOffset(position.offset
					+ position.length - 1);

			for (int i = 0; i < positionList.size(); i++) {
				Position commentPosition = positionList.get(i);

				if (commentPosition.overlapsWith(position.offset,
						position.length)) {
					if (document.getChar(commentPosition.offset + 1) == '*') {
						int commentFirstLine = document
								.getLineOfOffset(commentPosition.offset);
						int commentLastLine = document
								.getLineOfOffset(commentPosition.offset
										+ commentPosition.length - 1);

						int firstLine = selectionFirstLine > commentFirstLine ? selectionFirstLine
								: commentFirstLine;
						int lastLine = selectionLastLine > commentLastLine ? commentLastLine
								: selectionLastLine;
						
						if(firstLine == commentFirstLine) {
							firstLine++;
						}
						
						for(int j = firstLine; j <= lastLine; j++) {
							document.replace(document.getLineOffset(j) + indentList[j], 0, " ");
						}
					}
				}
			}
		} catch (BadLocationException e) {
			TinyOSPlugin.log(e);
		}
	}
	
	private int[] keywordScanner(IDocument document, int[] indentList, ArrayList<Position> positionList) {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter = 
			new FindReplaceDocumentAdapter(document);
		String[] keywords = {"if", "else", "while", "do", "for"};
		
		try {
			for(int i = 0; i < keywords.length; i++) {
				IRegion keyword = findReplaceDocumentAdapter.find(0,
						keywords[i], true, true, true, false);

				while(keyword != null) {
					int offset = keyword.getOffset();
					boolean con = false;
					
					// checks if the keyword belongs to a comment, or a string, or not
					for(int j = 0; j < positionList.size(); j++) {
						if(positionList.get(j).includes(offset)) {
							keyword = findReplaceDocumentAdapter.find(offset + 1, keywords[i], true, true, true, false);
							con = true;
							break;
						}						
					}
					
					if(con)
						continue;
					
					int stack = 0;
					int line = document.getLineOfOffset(offset);
					
					String keywordStr = document.get(offset, keyword.getLength());
					if(keywordStr.equals("if") || keywordStr.equals("while") || keywordStr.equals("for")) {
						if(keywordStr.equals("if")) {
							IRegion ir = findReplaceDocumentAdapter.find(offset - 1,
									"\\S", false, false, false, true);
							if(ir != null && document.getChar(ir.getOffset()) == '#') {
								keyword = findReplaceDocumentAdapter.find(offset + 1,
										keywords[i], true, true, true, false);
								continue;
							}
						}
						IRegion ir = findReplaceDocumentAdapter.find(offset + 1,
								"[\\(\\)]", true, false, false, true);
						
						while(ir != null) {
							offset = ir.getOffset();
							if(document.getChar(offset) == '(') {
								stack++;
							}
							else { // if(document.getChar(offset) == ')')
								stack--;
								if(stack == 0) {
									break;
								}
							}
							ir = findReplaceDocumentAdapter.find(offset + 1,
									"[\\(\\)]", true, false, false, true);
						}

						ir = findReplaceDocumentAdapter.find(offset + 1, "\\S", true, false, false, true);
						offset = ir.getOffset(); 

						if(document.getChar(offset) != '{') {
 							int nextLine = document.getLineOfOffset(offset);
							if(line != nextLine) {
								indentList[nextLine]++;
							}
						}
					}
					else {
						if(keywordStr.equals("else")) {
							IRegion ir = findReplaceDocumentAdapter.find(offset - 1,
									"\\S", false, false, false, true);
							if(ir != null && document.getChar(ir.getOffset()) == '#') {
								keyword = findReplaceDocumentAdapter.find(offset + 1,
										keywords[i], true, true, true, false);
								continue;
							}
						}
						
						offset = offset + keyword.getLength() - 1;
						IRegion ir = findReplaceDocumentAdapter.find(offset + 1,
								"\\S", true, false, false, true);
						offset = ir.getOffset();
						if(document.getChar(offset) != '{') {
							int nextLine = document.getLineOfOffset(offset);
							if(line != nextLine) {
								indentList[nextLine]++;
							}
						}
					}
					
					keyword = findReplaceDocumentAdapter.find(offset + 1,
							keywords[i], true, true, true, false);
				}
			}
		} catch (BadLocationException e) {
			TinyOSPlugin.log(e);
		}
		
		return indentList;
	}
	
	private int[] braceScanner(IDocument document, int[] indentList, ArrayList<Position> positionList) {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter = 
			new FindReplaceDocumentAdapter(document);

		try {
			IRegion brace = findReplaceDocumentAdapter.find(0, "\\{|\\}", true, false, false, true);
			int line = -1;		
			while(brace != null) {
				int offset = brace.getOffset();
				boolean con = false;
				
				// checks if the brace belongs to a comment, a string or not
				for(int i = 0; i < positionList.size(); i++) {
					if(positionList.get(i).includes(offset)) {
						if(offset + 1 > document.getLength() - 1)
							return indentList;
						brace = findReplaceDocumentAdapter.find(offset + 1, "\\{|\\}", true, false, false, true);
						con = true;
						break;
					}						
				}
				
				if(con)
					continue;
				int lineNum = document.getLineOfOffset(offset);

				if (document.getChar(offset) == '{') {
					line = lineNum;
					for (int i = lineNum + 1; i < indentList.length; i++) {
						indentList[i]++;
					}
				}
				else {	// if (document.getChar(offset) == '}')
					if(lineNum == line) {
						lineNum++;
					}
					for (int i = lineNum; i < indentList.length; i++) {
						if(indentList[i] != 0) {
							indentList[i]--;
						}
						else
							System.out.println("error");
					}
				}
				
				if(offset + 1 > document.getLength() - 1)
					return indentList;
				brace = findReplaceDocumentAdapter.find(offset + 1, "\\{|\\}", true, false, false, true);		
			}
		} catch (BadLocationException e) {
			TinyOSPlugin.log(e);
		}
		return indentList;
	}
	
	private void indentation(IDocument document, Position position, int[] indentList) {
		try {
			int firstLine = document.getLineOfOffset(position.offset);
			int lastLine = document.getLineOfOffset(position.offset + position.length - 1);

			for (int i = firstLine; i <= lastLine; i++) {
				int offset = document.getLineOffset(i);
				char c = document.getChar(offset);
				
				while (c == '\t' || c == ' ') {
					document.replace(offset, 1, "");
					if(offset < document.getLength())
						c = document.getChar(offset);
				}
				
				if (c != '\n') {
					for (int j = 0; j < indentList[i]; j++)
						document.replace(offset, 0, INDENT);
				}
			}
		} catch (BadLocationException e) {
			TinyOSPlugin.log(e);
		}		
	}
	
	public int[] getIndentList() {
		return this.indentList;
	}
	
	public ArrayList<Position> getPositionList(IDocument document) {
		try {
			for (int i = 0; i < this.positionList.size(); i++) {
				document.addPosition(positionList.get(i));
			}
		} catch (BadLocationException e) {
			TinyOSPlugin.log(e);
		}
		
		return this.positionList;
	}
}
