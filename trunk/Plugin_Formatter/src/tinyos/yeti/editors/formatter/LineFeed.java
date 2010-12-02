package tinyos.yeti.editors.formatter;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;

import tinyos.yeti.TinyOSPlugin;

public class LineFeed {
	
	private Document document;
	private Position position;
	
	public LineFeed(Document document, Position position) {
		this.document = document;
		this.position = position;
	}
	
	public LineFeed(Document document) {
		this.document = document;
		this.position = new Position(0, document.getLength());
	}
	
	public void lineFeed() {
		newline();
		noLineBreak();
		keyword();
	}
	
	/**
	 * In the case of keyword 'for', no new line is needed between its initialization,
	 * conditional and step.
	 * 
	 * i.e.: for(int i = 0; i < N; i++){}
	 */
	private void noLineBreak() {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter = 
			new FindReplaceDocumentAdapter(document);
		
		try {
			IRegion keyword = findReplaceDocumentAdapter.find(position.getOffset(),
					"for", true, true, true, false);
			while(keyword != null) {
				int offset = keyword.getOffset();
				
				if (offset > position.getOffset() + position.getLength() - 1) {
					return;
				}
				
				for(int i = 0; i < 2; i++) {
					IRegion semicolon = findReplaceDocumentAdapter.find(offset + 1, ";", true, false, false, false);
					offset = semicolon.getOffset();
					if (offset + 1 > position.getOffset() + position.getLength() - 1)
						return;
					
					if(document.getChar(offset + 1) == '\n') {
						if(document.getChar(offset + 2) != ' ')
							document.replace(offset + 1, 1, " ");
						else
							document.replace(offset + 1, 1, "");
					}
				}
				
				keyword = findReplaceDocumentAdapter.find(offset + 1, "for", true, true, true, false);
			}
		} catch (BadLocationException e) {
			TinyOSPlugin.log(e);
		}		
	}
	
	/**
	 * Gives a new line to each statement (that ends with a semicolon)
	 * 
	 * @param document
	 * @param region
	 */
	private void newline() {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter = 
			new FindReplaceDocumentAdapter(document);
		
		try {
			IRegion semicolon = findReplaceDocumentAdapter.find(position.getOffset(), ";", true, false, false, false);
			
			while (semicolon != null) {
				int offset = semicolon.getOffset();
				
				if (offset > position.getOffset() + position.getLength() - 1)
					return;
				
				if (document.getChar(offset - 1) == ' ') {
					document.replace(offset - 1, 1, "");
					offset--;
				}
			
				if ((offset + 1 + "//SINGLE_LINE_NOLF//".length() <= document.getLength())
						&& (document.get(offset + 1, "//SINGLE_LINE_NOLF//".length()).equals("//SINGLE_LINE_NOLF//")
						|| document.get(offset + 2, "//SINGLE_LINE_NOLF//".length()).equals("//SINGLE_LINE_NOLF//"))) {
					semicolon = findReplaceDocumentAdapter.find(offset + 1, ";", true, false, false, false);
					continue;
				}
				
				if(offset + 1 > document.getLength() - 1)
					return;
				IRegion ir = findReplaceDocumentAdapter.find(offset + 1, "\\S", true, false, false, true);
				int end = position.getOffset() + position.getLength() - 1;
				if (ir != null && ir.getOffset() > end) {
					document.replace(offset + 1, end - offset, "");
					
					if (document.getLineOfOffset(offset) == document.getLineOfOffset(ir.getOffset())) 
						document.replace(offset + 1, 0, "\n");
					return;
				}
				document.replace(offset + 1, 0, "\n");	
				semicolon = findReplaceDocumentAdapter.find(offset + 2, ";", true, false, false, false);
			}
		} catch (BadLocationException e) {
			TinyOSPlugin.log(e);
		}
	}
	
	/**
	 * Handles the control-flow constructions that just have one statement and are without braces.
	 * 
	 * e.g.: if(conditions)
	 *			 statement;
	 */
	private void keyword() {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter = 
			new FindReplaceDocumentAdapter(document);
		String[] keywords = {"if", "else", "while", "do", "for", "enum"};

		try {
			for(int i = 0; i < keywords.length; i++) {
				IRegion keyword = findReplaceDocumentAdapter.find(position.getOffset(),
						keywords[i], true, true, true, false);

				while(keyword != null) {
					int offset = keyword.getOffset();
					if (offset > position.getOffset() + position.getLength() - 1)
						return;
					int stack = 0;

					String keywordStr = document.get(offset, keyword.getLength());
					if(keywordStr.equals("if") || keywordStr.equals("while") || keywordStr.equals("for")) {
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

						if(document.getChar(offset) != '{' && document.getChar(offset) != ';') {
						if(offset > position.getOffset() + position.getLength() - 1)
							return;
						document.replace(offset, 0, "\n");						
						}
					}
					else if(keywordStr.equals("do") || keywordStr.equals("else")) {
						offset = offset + keyword.getLength() - 1;
						IRegion ir = findReplaceDocumentAdapter.find(offset + 1,
								"\\S", true, false, false, true);
						offset = ir.getOffset();
						if(document.getChar(offset) != '{') {
							if (offset > position.getOffset() + position.getLength() - 1)
								return;
							document.replace(offset, 0, "\n");						
						}
					}
					else {
						IRegion ir = findReplaceDocumentAdapter.find(offset + 1,
								"{", true, false, false, false);
						if(ir == null) {
							System.out.println("error");
							return;
						}
						int opening = ir.getOffset();
						ir = findReplaceDocumentAdapter.find(opening + 1,
								"}", true, false, false, false);
						if(ir == null) {
							System.out.println("error");
							return;
						}
						int closing = ir.getOffset();

						ir = findReplaceDocumentAdapter.find(opening + 1,
								",", true, false, false, false);

						while(ir != null) {
							int commaOffset = ir.getOffset();
							if(commaOffset > closing)
								break;
							document.replace(commaOffset + 1, 0, "\n");
							ir = findReplaceDocumentAdapter.find(commaOffset + 1,
									",", true, false, false, false);
						}
						offset = closing + 1;
					}
					keyword = findReplaceDocumentAdapter.find(offset + 1,
							keywords[i], true, true, true, false);
				}
			}
		} catch (BadLocationException e) {
			TinyOSPlugin.log(e);
		}
	}	
}
