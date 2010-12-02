package tinyos.yeti.editors.formatter;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;

import tinyos.yeti.TinyOSPlugin;

/**
 * Symbols formatting
 * 
 * @author Jianyuan Li
 */
public class Symbols {
	private Document document;
	private Position position;
	
	public Symbols(Document document, Position position) {
		this.document = document;
		this.position = position;
	}
	
	public Symbols(Document document) {
		this.document = document;
		this.position = new Position(0, document.getLength());
	}
	
	public void format() {
		bracket();
		plusMinus();
		generalSymbols();
		comma();
	}
	
	/**
	 * Handles plus and minus, which are in one of the following form:
	 * a + b; i++; a + ++i; a++ + i; ++i + a; a + -b;
	 */
	private void plusMinus() {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter = new FindReplaceDocumentAdapter(
				document);
		try {
			IRegion symbol = findReplaceDocumentAdapter.find(position
					.getOffset(), "[\\+-]", true, false, false, true);
			
			while(symbol != null) {
				int offset = symbol.getOffset();
				if(offset > position.offset + position.length - 1)
					return;

				if(document.getChar(offset + 1) == '=') {	// +=
					if(document.getChar(offset - 1) != ' ') {
						document.replace(offset, 0, " ");
						offset++;
					}
					if(document.getChar(offset + 2) != ' ') {
						if (offset + 2 > position.offset + position.length - 1)
							return;
						document.replace(offset + 2, 0, " ");
						offset += 2;
					}
				}
				else if(document.getChar(offset + 1) == '>') { // ->
					if(document.getChar(offset - 1) == ' ') {
						document.replace(offset - 1, 1, "");
						offset--;
					}
					if(document.getChar(offset + 2) == ' ') {
						document.replace(offset + 2, 1, "");
					}
					offset++;
				}
				else if(document.getChar(offset - 1) == '<') { // <-
					if(document.getChar(offset - 2) == ' ') {
						document.replace(offset - 2, 1, "");
						offset--;
					}
					if(document.getChar(offset + 1) == ' ') {
						document.replace(offset + 1, 1, "");
					}
					offset++;
				}
				else if(document.getChar(offset + 1) == document.getChar(offset)) { // ++
					if(document.getChar(offset - 1) == ' ') {
						char c = document.getChar(offset - 2);
						if((c >= 48 && c <= 57) || (c >= 65 && c <= 90)
								|| (c >= 97 && c <= 122) || c == '_') { // if c is a digit, or a letter, or an underscore
							document.replace(offset - 1, 1, "");
							offset--;
						}
					}
					offset++;
				}
				else {
					if(document.getChar(offset - 1) == ' ') { // a + b; i++ +1; 
						char c = document.getChar(offset - 2);
						if ((c >= 48 && c <= 57)
								|| (c >= 65 && c <= 90)
								|| (c >= 97 && c <= 122)
								|| c == '_'
								|| ((c == '+' || c == '-') && document.getChar(offset - 3) == c)
								|| c == ')'
								|| c == '/'
								) {
							if(document.getChar(offset + 1) != ' ') {
								if(offset + 1 > position.offset + position.length - 1)
									return;
								document.replace(offset + 1, 0, " ");
							}
							offset++;
						}
						else {
							if(document.getChar(offset + 1) == ' ') {
								if(offset + 1 > position.offset + position.length - 1)
									return;
								document.replace(offset + 1, 1, "");
							}
						}
					}
					else {	
						char c = document.getChar(offset - 1);
						if ((c >= 48 && c <= 57)
								|| (c >= 65 && c <= 90)
								|| (c >= 97 && c <= 122)
								|| c == '_'
								|| ((c == '+' || c == '-') && document.getChar(offset - 2) == c)
								|| c == ')'
								|| c == '/'
								) { // a+-b
			  				document.replace(offset, 0, " "); 
							offset++;
							
							if(document.getChar(offset + 1) != ' ') {
								if(offset + 1 > position.offset + position.length - 1)
									return;
								document.replace(offset + 1, 0, " ");
							}
							offset++;
						}
						else {
							if(document.getChar(offset + 1) == ' ') {
								if(offset + 1 > position.offset + position.length - 1)
									return;
								document.replace(offset + 1, 1, "");
							}
						}
					}
				}
				
				symbol = findReplaceDocumentAdapter.find(offset + 1,
						"[\\+-]", true, false, false, true);
			}
		} catch (BadLocationException e) {
			TinyOSPlugin.log(e);
		}
	}
	
	/**
	 * Handles parentheses, which need no space before and after
	 */
	private void bracket() {
		char[] symbols = {'(', ')', '[', ']', '.'};
		FindReplaceDocumentAdapter findReplaceDocumentAdapter = new FindReplaceDocumentAdapter(
				document);
		String str = getRegularExpression(symbols);
		
		try {
			IRegion symbol = findReplaceDocumentAdapter.find(position
					.getOffset(), str, true, false, false, true);
			
			while(symbol != null) {
				int offset = symbol.getOffset();
				if (offset > position.offset + position.length - 1)
					return;

				if (document.getChar(offset - 1) == ' ') {
					document.replace(offset - 1, 1, "");
					offset--;
				}
				if (document.getChar(offset + 1) == ' ') {
					if(offset + 1 > position.offset + position.length - 1) 
						return;
					document.replace(offset + 1, 1, "");
				}
				
				if(document.getChar(offset) == ')') {
					IRegion ir = findReplaceDocumentAdapter.find(offset + 1,
							"\\S", true, false, false, true);
					if(ir != null) {
						char c = document.getChar(ir.getOffset());
						if((c >= 48 && c <= 57)
								|| (c >= 65 && c <= 90)
								|| (c >= 97 && c <= 122)
								|| c == '_'
								|| c == '@') {
							document.replace(offset + 1, 0, " ");
						}
					}
				}

				symbol = findReplaceDocumentAdapter.find(offset + 1,
						str, true, false, false, true);
			}
		} catch (BadLocationException e) {
			TinyOSPlugin.log(e);
		}
	}
	
	/**
	 * General symbols formatting.
	 * One space before and one after a symbol sequence,
	 * but no space between two adjacent symbols
	 */
	private void generalSymbols() {
		char[] symbols = {'>', '<', '|', '^', '!', '?', ':', '%', '=', '&', '/', '*'};
		FindReplaceDocumentAdapter findReplaceDocumentAdapter = new FindReplaceDocumentAdapter(
				document);
		String str = getRegularExpression(symbols);
		
		try {
			IRegion symbol = findReplaceDocumentAdapter.find(position
					.getOffset(), str, true, false, false, true);
			while (symbol != null) {
				int offset = symbol.getOffset();
				if (offset > position.offset + position.length - 1)
					return;
				
				if(document.getChar(offset) == '<') {
					IRegion ir = findReplaceDocumentAdapter.find(offset - 1,
							";", false, false, false, false);
					int semicolonOffset = ir == null ? 0: ir.getOffset();
					ir = findReplaceDocumentAdapter.find(offset - 1,
							"interface", false, true, true, false);
					
					if(ir != null && ir.getOffset() > semicolonOffset) {
						ir = findReplaceDocumentAdapter.find(offset + 1,
								">", true, false, false, false);
						if(ir != null) {
							int o = ir.getOffset();
							if(document.getChar(offset - 1) == ' ') {
								document.replace(offset - 1, 1, "");
								offset--;
								o--;
							}
							if(document.getChar(offset + 1) == ' ') {
								document.replace(offset + 1, 1, "");
								o--;
							}
							if(document.getChar(o - 1) == ' ') {
								document.replace(o - 1, 1, "");
								o--;
							}
							if(document.getChar(o + 1) != ' ' && document.getChar(o + 1) != ';')
								document.replace(o + 1, 0, " ");
							
							ir = findReplaceDocumentAdapter.find(ir.getOffset() + 1,
									";", true, false, false, false);
							if(ir != null && document.getChar(ir.getOffset() - 1) == ' ')
								document.replace(ir.getOffset() - 1, 1, "");
								
							symbol = findReplaceDocumentAdapter.find(o + 1, str, true, false, false, true);
							continue;
						}
					}
				}
				
				if(document.getChar(offset) == '*') {	// **p
					if(document.getChar(offset + 1) == '*') {
						offset++;
						while(document.getChar(offset + 1) == '*') {
							offset++;
						}
						if(document.getChar(offset + 1) == ' ') {
							if (offset + 1 > position.offset + position.length - 1)
								return;
							document.replace(offset + 1, 1, "");
						}
						symbol = findReplaceDocumentAdapter.find(offset + 1, str, true, false, false, true);
						continue;							
					}
					else {
						char c = 0;
						if(document.getChar(offset - 1) == ' ')
							c = document.getChar(offset - 2);
						else
							c = document.getChar(offset - 1);
						
						if(!((c >= 48 && c <= 57)
								|| (c >= 65 && c <= 90)
								|| (c >= 97 && c <= 122)
								|| c == '_'
								|| c == ')')) {
							
							if(document.getChar(offset + 1) == ' ') {
								if (offset + 1 > position.offset + position.length - 1)
									return;
								document.replace(offset + 1, 1, "");
							}
							symbol = findReplaceDocumentAdapter.find(offset + 1, str, true, false, false, true);
							continue;	
						}				
					}									
				}
				
				if(document.getChar(offset) == '/' && document.getChar(offset + 1) == '/') {
					if(offset + 2 > document.getLength() - 1)
						return;
					symbol = findReplaceDocumentAdapter.find(offset + 2, str, true, false, false, true);
					continue;
				}
				
				if(document.getChar(offset) == '&') { // sp = &m;
					char c = 0;
					if(document.getChar(offset - 1) == ' ')
						c = document.getChar(offset - 2);
					else
						c = document.getChar(offset - 1);
					
					if(!((c >= 48 && c <= 57)
							|| (c >= 65 && c <= 90)
							|| (c >= 97 && c <= 122)
							|| c == '_'
							)) {
						
						while(document.getChar(offset + 1) == '&') {
							offset++;
						}
						symbol = findReplaceDocumentAdapter.find(offset + 1, str, true, false, false, true);
						continue;
					}				
				}
				
				if(document.getChar(offset) == '=' &&  document.getChar(offset - 1) == '+') { // +=
					symbol = findReplaceDocumentAdapter.find(offset + 1, str, true, false, false, true);
					continue;
				}
				
				if(document.getChar(offset) == '>'
					&& document.getChar(offset - 1) == '-'
					&& document.getChar(offset - 2) != '-'
					&& document.getChar(offset - 2) != '<') {	// ->
					
					if(document.getChar(offset - 2) == ' ' && offset - 2 >= position.offset) {
						document.replace(offset - 2, 1, "");
						offset--;
					}
					if(document.getChar(offset + 1) == ' ') {
						if (offset + 1 > position.offset + position.length - 1)
							return;
						document.replace(offset + 1, 1, "");
					}
					symbol = findReplaceDocumentAdapter.find(offset + 1, str, true, false, false, true);
					continue;
				}
				
				if(document.getChar(offset) == '<'
					&& document.getChar(offset + 1) == '-'
					&& document.getChar(offset + 2) != '-'
					&& document.getChar(offset + 2) != '>') {	// <-
					
					if(document.getChar(offset - 1) == ' ' && offset - 1 >= position.offset) {
						document.replace(offset - 1, 1, "");
						offset--;
					}
					if(document.getChar(offset + 2) == ' ') {
						if (offset + 1 > position.offset + position.length - 1)
							return;
						document.replace(offset + 2, 1, "");
					} 
					symbol = findReplaceDocumentAdapter.find(offset + 3, str, true, false, false, true);
					continue;
				}
				
				if (document.getChar(offset - 1) != ' ' && document.getChar(offset - 1) != document.getChar(offset)) {
					document.replace(offset, 0, " ");
					offset++;
				}
				
				if (offset + 1 > position.offset + position.length - 1)
					return;
				char c = document.getChar(offset + 1);
				while (c == '=' || c == document.getChar(offset)) {
					offset++;
					if (offset > position.offset + position.length - 1)
						return;
					c = document.getChar(offset + 1);
				}
				
				if (document.getChar(offset + 1) != ' ')
					document.replace(offset + 1, 0, " ");	
				
				symbol = findReplaceDocumentAdapter.find(offset + 2, str, true, false, false, true);
			}
			
		} catch (BadLocationException e) {
			TinyOSPlugin.log(e);
		}
	}
	
	/**
	 * Handles comma, which needs no space before and one space after.
	 * E.g.: a, b
	 */
	private void comma() {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter = new FindReplaceDocumentAdapter(
				document);

		try {
			IRegion symbol = findReplaceDocumentAdapter.find(position
					.getOffset(), ",", true, false, false, false);
			while (symbol != null) {
				int offset = symbol.getOffset();
				if (offset > position.offset + position.length - 1)
					return;
				if (document.getChar(offset - 1) == ' ') {
					document.replace(offset - 1, 1, "");
					offset--;
				}
				if (document.getChar(offset + 1) != ' ' && document.getChar(offset + 1) != '\n') {
					if(offset + 1 > position.offset + position.length - 1) 
						return;
					document.replace(offset + 1, 0, " ");
				}
	
				symbol = findReplaceDocumentAdapter.find(offset + 1, ",", true,
						false, false, false);
			}
		} catch (BadLocationException e) {
			TinyOSPlugin.log(e);
		}
	}
	
	private String getRegularExpression(char[] symbols) {
		char[] special = { '\\', '|', '(', ')', '[', ']', '{', '}', '^', '$',
				'*', '+', '?', '.' };
		String str = "[";
		
		for (int i = 0; i < symbols.length; i++) {
			for (int j = 0; j < special.length; j++) {
				if (symbols[i] == special[j]) {
					str += "\\";
					break;
				}
			}		
			str += symbols[i];
		}
		str += "]";
		return str;
	}

}
