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
 * Standard formatting strategy for nesc.
 * 
 * @author Jianyuan Li
 */
public class NesCFormatterStrategy extends ContextBasedFormattingStrategy {
	private LinkedList<IFormattingContext> formattingContexts = new LinkedList<IFormattingContext>();
	private IFormattingContext currentContext;
	private Document doc;
	
	private IFormattingSettings settings = new DefaultSetting();

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
	
	private IFormattingSettings getSettings(){
		return settings;
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

		format (document, region);
	}

	private void format(IDocument document, IRegion region) {
		doc = new Document(document.get());
		Position position = new Position(region.getOffset(), region.getLength());
		
		try {
			doc.addPosition(position);
			// replace comments
			ArrayList<String> commentsAndStrings = getCommentsAndStrings(doc);
			// replace strings
			ArrayList<String> preprocessorList = preprocessor(doc, position);
			// replace any set of whitespaces by a single whitespace and delete redundant whitespaces
			replaceWhitespaces(doc, position);
			// correct the newlines
			LineFeed lineFeed = new LineFeed(doc, position);
			lineFeed.lineFeed();
			// add and remove spaces around symbols like +, -, * ...
	 		Symbols symbols = new Symbols(doc, position);
			symbols.format();
			// update linefeeds around blocks
			block(doc, position);
			// remove line breaks if they are not necessary (e.g. in an if-statement before the '{')
			noLineBreak (doc, position);
			// add blank lines at locations where blank lines were before code formatting
			blankLine(doc, position);
			// reset preprocessor directives and comments
			putPreprocessorBack(doc, preprocessorList, position);
			putCommentsAndStringsBack(doc, commentsAndStrings);
			
			// update indentation
			NesCIndenterStrategy indenter = new NesCIndenterStrategy();
			Region r = new Region(position.offset, position.length);
			indenter.indent(doc, r);
			int[] indentList = indenter.getIndentList();
			ArrayList<Position> positionList = indenter.getPositionList(doc);

			// wrap lines
			lineWrapping(doc, position, indentList, positionList);
			document.replace(0, document.getLength(), doc.get());	
			doc.removePosition(position);
		} catch (BadLocationException e) {
			TinyOSPlugin.log(e);
		}		
	}
	
	/**
	 * Wraps a line if it has more than {@link IFormattingSettings#getLineWrappingLength()} characters.
	 * 
	 * @param document
	 * @param position
	 */
	private void lineWrapping(Document document, Position position,
			int[] indentArray, ArrayList<Position> positionList) {
		final int lineLength = getSettings().getLineWrappingLength();
		if( lineLength < 0 )
			return;
		
		final String indent = "\t\t";
	
		final String regSymbol = "&&|\\|\\||\\?|:|,|=|;|\\+|-|\\*|/|%|\\||&|\\.|==|<|>|<=|>=";

		ArrayList<Integer> indentList = new ArrayList<Integer>();
		for(int i = 0; i < indentArray.length; i++) {
			indentList.add(indentArray[i]);
		}
		
		FindReplaceDocumentAdapter findReplaceDocumentAdapter = 
			new FindReplaceDocumentAdapter(document);
 		             
 		try {
			int firstLine = document.getLineOfOffset(position.offset);
			int lastLine = document.getLineOfOffset(position.offset + position.length - 1);

			for(int i = firstLine; i <= lastLine; i++) {
				int lineIndent = indentList.get(i) + indent.length();
				int offset = -1;
				
				while(document.getLineLength(i) > lineLength) {
					int lineOffset = document.getLineOffset(i);
					int p = offset == -1 ? lineOffset + lineLength - 1 : offset;
					
					IRegion symbol = findReplaceDocumentAdapter.find(p, regSymbol, false, false, false, true);

					if(symbol != null && symbol.getOffset() > lineOffset) {
						int control = -1;
						for(int j = 0; j < positionList.size(); j++) {
							if(positionList.get(j).includes(symbol.getOffset())) {	
							char c = document.getChar(positionList.get(j).offset + 1);
								if(c == '*' || c == '/') {
									control = 0;
								}
								else {
									offset = symbol.getOffset() - 1;
									control = 1;
								}
								break;
							}								
						}
						
						if(control == 0)
							break;
						if(control == 1)
							continue;
						
						String symbolStr = document.get(symbol.getOffset(), symbol.getLength());
						if(symbolStr.equals(";") || symbolStr.equals(",")) {
							document.replace(symbol.getOffset() + 1, 0, "\n");
						}
						else {
							document.replace(symbol.getOffset(), 0, "\n");
						}
						i++;
						indentList.add(i, lineIndent);						
						lineOffset = document.getLineOffset(i);
						IRegion ir = findReplaceDocumentAdapter.find(lineOffset,"\\S", true, false, false, true);
						String str = "";			
					
						for(int j = 0; j < indentList.get(i); j++) {
							str += "\t";
						}
						document.replace(lineOffset, ir.getOffset() - lineOffset, str);
						
						lastLine++;
						offset = -1;
					}
					else
						break;
				}
			}
			
		} catch (BadLocationException e) {
			TinyOSPlugin.log(e);
		}
	}
	
	private void noLineBreak (Document document, Position position) {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter = 
			new FindReplaceDocumentAdapter(document);
		String[] keywords = {"enum", "do", "struct", "typedef", "union"};
		for (int i = 0; i < keywords.length; i++) {
			try {
				IRegion keyword = findReplaceDocumentAdapter.find(0,
						keywords[i], true, true, true, false);
				while(keyword != null) {
					int offset = keyword.getOffset();
				//	int length = keyword.getLength();
				//	String str = document.get(offset, length);
					
				//	if(str.equals("enum") || str.equals("struct") || str.equals("typedef") || str.equals("union")) {
					int stack = 0;
					IRegion ir = findReplaceDocumentAdapter.find(offset + 1,
							"\\{|\\}", true, false, false, true);
					while(ir != null) {
						offset = ir.getOffset();
						if(document.getChar(offset) == '{') {
							stack++;
						}
						else {
							stack--;
							if(stack == 0)
								break;
						}
						ir = findReplaceDocumentAdapter.find(offset + 1,
								"\\{|\\}", true, false, false, true);
					}
						
					offset++;
					ir = findReplaceDocumentAdapter.find(offset,
							";", true, false, false, false);
					if(ir != null) {
						IRegion t = findReplaceDocumentAdapter.find(offset,
								"[^A-Za-z_0-9 \t\n]", true, false, false, true);
						if(t != null && t.getOffset() < ir.getOffset()) {
							keyword = findReplaceDocumentAdapter.find(ir.getOffset() + 1,
									keywords[i], true, true, true, false);
							continue;
						}
						
						char c = '\t';
						for(int j = offset; j < ir.getOffset(); j++) {
							c = document.getChar(j);
							if(c == '\n')
								document.replace(j, 1, "");
						}
						if(c == ' ')
							document.replace(ir.getOffset() - 1, 1, "");
						
						if(document.getChar(offset) != ' ' && document.getChar(offset) != ';') {
							document.replace(offset, 0, " ");
							offset++;
						}
					}
					
					keyword = findReplaceDocumentAdapter.find(ir.getOffset() + 1,
							keywords[i], true, true, true, false);
				//	}
				}
				
			} catch (BadLocationException e) {
				TinyOSPlugin.log(e);
			}
		}
	}
	
	private void putPreprocessorBack(Document document, ArrayList<String> preprocessorList, Position position) {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter = 
			new FindReplaceDocumentAdapter(document);

		try {
			IRegion preprocessor = findReplaceDocumentAdapter.find(0,
					"//Preprocessor//", true, false, false, false);
		//	int i = 0;
		//	while(preprocessor != null) {
			for(int i = 0; i < preprocessorList.size(); i++) {
				if(preprocessor == null) {
					System.out.println("error");
					return;
				}
				int offset = preprocessor.getOffset();
				int length = preprocessor.getLength(); 
				String str = preprocessorList.get(i);
				if(offset + length <= document.getLength() - 1 && document.getChar(offset + length) == '\n')
					document.replace(offset, length + 1, str);
				else
					document.replace(offset, length, str);
				
				if(offset + preprocessorList.get(i).length() > document.getLength() - 1) {
					if(i != preprocessorList.size() - 1) {
						System.out.println("error");
					}
					position.setLength(offset + preprocessorList.get(i).length() - position.offset - 1);
					return;
				}
				preprocessor = findReplaceDocumentAdapter.find(offset
						+ preprocessorList.get(i).length(),
						"//Preprocessor//", true, false, false, false);
			}
		} catch (BadLocationException e) {
			TinyOSPlugin.log(e);
		}
	}
	
	private ArrayList<String> preprocessor(Document document, Position position) {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter = 
			new FindReplaceDocumentAdapter(document);
		ArrayList<String> list = new ArrayList<String>();
		
		try {
			IRegion pound = findReplaceDocumentAdapter.find(position.getOffset(),
					"#", true, false, false, false);
			
			while(pound != null) {
				int offset = pound.getOffset();
			//	if(offset > position.getOffset() + position.getLength() - 1)
				//	return list;
				
				if(offset + 1 > document.getLength() - 1) {
					while (document.getChar(offset + 1) == ' '
						|| document.getChar(offset + 1) == '\t') {
						if (offset + 1 > position.getOffset()
								+ position.getLength() - 1)
							// return list;
							break;
						document.replace(offset + 1, 1, "");
					}
				}
				
				if(offset > position.getOffset() + position.getLength() - 1) {
					int line = document.getLineOfOffset(offset);
					int lineStart = document.getLineOffset(line);
					int lineEnd = lineStart + document.getLineLength(line) - 1;
					String str = document.get(lineStart, document.getLineLength(line));
					while(document.getChar(lineEnd - 1) == '\\') {
						line++;
						lineStart = document.getLineOffset(line);
						str += document.get(lineStart, document.getLineLength(line));
						lineEnd = lineStart + document.getLineLength(line) - 1;
					}
					
					list.add(str);
					document.replace(offset, lineEnd - offset + 1, "//Preprocessor//\n");
					
					if(offset + "//Preprocessor//\n".length() > document.getLength() - 1) {
						position.setLength(offset + "//Preprocessor//\n".length() - position.offset);
						return list;
					}

					pound = findReplaceDocumentAdapter.find(offset
							+ "//Preprocessor//\n".length(), "#", true, false, false, false);
					
					continue;
				}
				int line = document.getLineOfOffset(offset);
				int lineStart = document.getLineOffset(line);
				offset = lineStart;
				int lineEnd = lineStart + document.getLineLength(line) - 1;
				int end = lineEnd > position.getOffset() + position.getLength()	- 1
						? position.getOffset() + position.getLength() - 1
						: lineEnd - 1;
				
				IRegion whitespace = findReplaceDocumentAdapter.find(lineStart,
						"\\s", true, false, false, true);
				while(whitespace != null) {
					int whitespaceOffset = whitespace.getOffset();
					if(whitespaceOffset > end)
						break;
					if(document.getChar(whitespaceOffset) == '\t') {
						document.replace(whitespaceOffset, 1, " ");
					}
					whitespaceOffset++;
					while(document.getChar(whitespaceOffset) == ' '
						|| document.getChar(whitespaceOffset) == '\t') {
						document.replace(whitespaceOffset, 1, "");
						end--;
						lineEnd--;
					}
					whitespace = findReplaceDocumentAdapter.find(whitespaceOffset,
							"\\s", true, false, false, true);
				}
				
				String str = document.get(lineStart, document.getLineLength(line));
				end = lineEnd - 1; 
		
				while (document.getChar(end) == '\\') {
					line++;
					lineStart = document.getLineOffset(line);
					if(lineStart > position.getOffset() + position.getLength() - 1) {
						lineEnd = lineStart + document.getLineLength(line) - 1;
						str += document.get(lineStart, document.getLineLength(line));
						end = lineEnd - 1;
						continue;
					}
					lineEnd = lineStart + document.getLineLength(line) - 1;
					end = lineEnd > position.getOffset() + position.getLength()
							- 1 ? position.getOffset() + position.getLength()
							- 1 : lineEnd - 1;
							
					whitespace = findReplaceDocumentAdapter.find(lineStart,
							"\\s", true, false, false, true);
					while(whitespace != null) {
						int whitespaceOffset = whitespace.getOffset();
						if(whitespaceOffset > end)
							break;
						if(document.getChar(whitespaceOffset) == '\t') {
							document.replace(whitespaceOffset, 1, " ");
						}
						whitespaceOffset++;
						while(document.getChar(whitespaceOffset) == ' '
							|| document.getChar(whitespaceOffset) == '\t') {
							document.replace(whitespaceOffset, 1, "");
							lineEnd--;
						}
						whitespace = findReplaceDocumentAdapter.find(whitespaceOffset,
								"\\s", true, false, false, true);
					}
				/*	p.setOffset(lineStart);
					p.setLength(end - lineStart + 1);
					replaceWhitespaces(document, p);
					str += document.get(p.offset, p.length + 1);
					end = p.offset + p.length - 1; */
					
					str += document.get(lineStart, document.getLineLength(line));
					end = lineEnd - 1;
				}
				
				list.add(str);
				document.replace(offset, lineEnd - offset + 1, "//Preprocessor//\n");
				
				if(offset + "//Preprocessor//\n".length() > document.getLength() - 1) {
					position.setLength(offset + "//Preprocessor//\n".length() - position.offset);
					return list;
				}

				pound = findReplaceDocumentAdapter.find(offset
						+ "//Preprocessor//\n".length(), "#", true, false, false, false);
				
			}
		} catch (BadLocationException e) {
			TinyOSPlugin.log(e);
		}
		
		return list;
	}
	
	private void blankLine(Document document, Position position) {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter = 
			new FindReplaceDocumentAdapter(document);
		
		try {
			IRegion blankLine = findReplaceDocumentAdapter.find(position.getOffset(),
					"//BLANK_LINE//", true, false, false, false);
			
			while (blankLine != null) {
				int enter = 0;
				int offset = blankLine.getOffset();
				
				IRegion before = findReplaceDocumentAdapter.find(offset - 1,
						"\\S", false, false, false, true);
				IRegion after = findReplaceDocumentAdapter.find(offset + blankLine.getLength(),
						"\\S", true, false, false, true);
				
				
				for(int i = offset - 1; i > before.getOffset(); i--) {
					char c = document.getChar(i);
					if (c == '\n')
						enter++;						
				}
				
				for(int i = offset + blankLine.getLength(); i < after.getOffset(); i++) {
					char c = document.getChar(i);
					if (c == '\n')
						enter++;
				}
				
				if (enter == 0) {
					document.replace(offset, "//BLANK_LINE//".length(), "\n\n");
				}
				else if(enter == 1) {
					document.replace(offset, "//BLANK_LINE//".length(), "\n");
				}
				else
					document.replace(offset, "//BLANK_LINE//".length(), "");
				
				blankLine = findReplaceDocumentAdapter.find(offset,
						"//BLANK_LINE//", true, false, false, false);
			}
		} catch (BadLocationException e) {
			TinyOSPlugin.log(e);
		}
		
	}
	
	private void block(Document document, Position position) {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter = 
			new FindReplaceDocumentAdapter(document);
		
		try {
			IRegion brace = findReplaceDocumentAdapter.find(position.getOffset(), "\\{|\\}", true, false, false, true);
			while (brace != null) {
				int offset = brace.getOffset();
				
				if (offset > position.getOffset() + position.getLength() - 1) {
					return;
				}
				
				if(document.getChar(offset) == '{') {
					IRegion ir = findReplaceDocumentAdapter.find(offset - 1, "\\S", false, false, false, true);
					if (document.getChar(ir.getOffset()) == '=') {
						if(document.getChar(offset + 1) == ' ')
							document.replace(offset + 1, 1, "");
						ir = findReplaceDocumentAdapter.find(offset + 1, ";", true, false, false, false);
						offset = ir.getOffset() - 2;
						if(document.getChar(offset) == ' ') {
							document.replace(offset, 1, "");
							offset--;
						}
						brace = findReplaceDocumentAdapter.find(offset + 3, "\\{|\\}", true, false, false, true);
						continue;
					}
				}
				
				if ((offset + 1 + "//SINGLE_LINE_NOLF//".length() <= document.getLength()
							&& document.get(offset + 1, "//SINGLE_LINE_NOLF//".length()).equals("//SINGLE_LINE_NOLF//"))
						|| (offset + 2 + "//SINGLE_LINE_NOLF//".length() <= document.getLength()
							&& document.get(offset + 2, "//SINGLE_LINE_NOLF//".length()).equals("//SINGLE_LINE_NOLF//"))) {
				}
				else {
					if(offset + 1 >= document.getLength())
						return;
					IRegion after = findReplaceDocumentAdapter.find(offset + 1, "\\S", true, false, false, true);
					int end = position.getOffset() + position.getLength() - 1;
					if (after != null && after.getOffset() > end) {
						document.replace(offset + 1, end - offset, "");
						
						if (document.getChar(offset) == '{' && document.getChar(offset - 1) != ' ') {
							document.replace(offset, 0, " ");
							offset++;
						}
						
						if (document.getLineOfOffset(offset) == document.getLineOfOffset(after.getOffset())) 
							document.replace(offset + 1, 0, "\n");
						return;
					}
					
					if (document.getChar(offset) == '{') {
						if (document.getChar(offset - 1) != ' ') {
							document.replace(offset, 0, " ");
							offset++;
						}
						document.replace(offset + 1, 0, "\n");
						offset++;
					}
					else {
						int braceLineNum = document.getLineOfOffset(offset);
						IRegion before = findReplaceDocumentAdapter.find(offset - 1, "\\S", false, false, false, true);
						if(before != null) {
							int lineNum = document.getLineOfOffset(before.getOffset());
							if(lineNum == braceLineNum) {
								document.replace(before.getOffset() + 1, offset - before.getOffset() - 1, "\n");
								offset = offset - (offset - before.getOffset() - 2);
							}
						}
						
						document.replace(offset + 1, 0, "\n");
						offset++;
					}
				}
				
				if(offset + 1 > position.getOffset() + position.getLength() - 1)
					return;
				brace = findReplaceDocumentAdapter.find(offset + 1, "\\{|\\}", true, false, false, true);
			}
		} catch (BadLocationException e) {
			TinyOSPlugin.log(e);
		}
		
	}
	
	/**
	 * Put all comments and strings back to document
	 * 
	 * @param document
	 * @param commentsAndStrings
	 */
	
	private void putCommentsAndStringsBack(Document document,
			ArrayList<String> commentsAndStrings) {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter = 
			new FindReplaceDocumentAdapter(document);
		
		try {
			int i = 0;
			IRegion ir = findReplaceDocumentAdapter.find(0, "//SINGLE_LINE//|//SINGLE_LINE_NOLF//|//MULTILINE//|//STRING//", true, false, false, true);
			while (ir != null) {
				int offset = ir.getOffset();
				String str = commentsAndStrings.get(i);		

				if (!document.get(offset, ir.getLength()).equals("//STRING//")) {
					if(offset + ir.getLength() <= document.getLength() - 1) {
						IRegion c = findReplaceDocumentAdapter.find(offset + ir.getLength(), "\\S", true, false, false, true);

						if(c != null) {
							int enter = 0;
							for(int j = offset + ir.getLength(); j < c.getOffset(); j++) {
								if(document.getChar(j) == '\n') {
									enter++;
								}
							}

							if(enter < 1)
								str += "\n";
						}
					}
				}

				document.replace(offset, ir.getLength(), str);
				offset += str.length();
				i++;
				
				if(offset > document.getLength() - 1)
					return;
				ir = findReplaceDocumentAdapter.find(offset, "//SINGLE_LINE//|//SINGLE_LINE_NOLF//|//MULTILINE//|//STRING//", true, false, false, true);
			}
		} catch (BadLocationException e) {
			TinyOSPlugin.log(e);
		}
	}
	
	/**
	 * Replaces each white space (tab, enter) with a single space, and delete the redundant ones
	 * 
	 * @param document
	 * @param region
	 */
	private void replaceWhitespaces(Document document, Position position) {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter = 
			new FindReplaceDocumentAdapter(document);
		int offset = position.getOffset();

		try {
			IRegion firstNonWhitespace = findReplaceDocumentAdapter.find(
					offset, "\\S", true, false, false, true);
			
			if (offset < firstNonWhitespace.getOffset()) {
				document.replace(offset, firstNonWhitespace.getOffset() - offset, "");
			}
			
			IRegion lastNonWhitespace = findReplaceDocumentAdapter.find(
					document.getLength() - 1, "\\S", false, false, false, true);

			if (lastNonWhitespace != null
					&& position.offset + position.length - 1 > lastNonWhitespace
							.getOffset())
				document.replace(lastNonWhitespace.getOffset() + 1, position.offset
						+ position.length - 1 - lastNonWhitespace.getOffset(), "");
				
			IRegion whiteSpace = findReplaceDocumentAdapter.find(
					offset, "\\s", true, true, false, true);

			while (whiteSpace != null) {
				offset = whiteSpace.getOffset();
				int enter = 0;
				
				if (offset > position.getOffset() + position.getLength() - 1)
					return;
				
				char c = document.getChar(offset);
				if (c == '\n') { // c == '\r\n'?
					enter++;
					
					if (offset > "//Preprocessor//".length()
							&& !document.get(
									offset - "//Preprocessor//".length(),
									"//Preprocessor//".length()).equals(
									"//Preprocessor//")) {
						document.replace(offset, 1, " ");
					}
				}

			
				offset++;
				if (offset > position.getOffset() + position.getLength() - 1)
					return;

				c = document.getChar(offset);
				while (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
					if (c == '\n')
						enter++;
					document.replace(offset, 1, "");
					if (offset > document.getLength() - 1)
						return;
					c = document.getChar(offset);
				}

				if (enter > 1) {
					document.replace(offset, 0, "//BLANK_LINE//");
					offset += "//BLANK_LINE//".length();
				}
				whiteSpace = findReplaceDocumentAdapter.find(
						offset, "\\s", true, false, false, true);
			}
		} catch (BadLocationException e) {
			TinyOSPlugin.log(e);
		}
	}
		
	/**
	 * Saves all comments and strings in a list ordered by their appearance, and
	 * replaces each of them with "//"
	 * 
	 * @param document
	 * @return the list
	 */

	private ArrayList<String> getCommentsAndStrings(Document document) {
		ArrayList<String> list = new ArrayList<String>();
		FindReplaceDocumentAdapter findReplaceDocumentAdapter = new FindReplaceDocumentAdapter(document);
		
		try {
			IRegion commentOrString = findReplaceDocumentAdapter.find(0, "//|/\\*|\"|\'", true, false, false, true);

			while (commentOrString != null) {
				int offset = commentOrString.getOffset();
				String str = "";
				boolean toList = true;
				
				if(document.getChar(offset) == '\'') {		// char c1 = '"', c2 = '\"';
					if(document.getChar(offset + 1) == '\"') {
						offset += 3;	// c1.length();
						toList = false;
					}
					else if(document.getChar(offset + 1) == '\\' && document.getChar(offset + 2) == '\"'){
						offset += 4;	// c2.length();
						toList = false;
					}
				}
				else if (document.getChar(offset + 1) == '/') {		// single-line comment
					int len = 0;
					if(document.getLineOfOffset(offset) == document.getNumberOfLines() - 1)
						len = document.getLength() - offset;
					else						
						len = document.getLineOffset(document.getLineOfOffset(offset) + 1) - offset - 1;

					str = document.get(offset, len);
					
					int lineOffset = document.getLineOffset(document.getLineOfOffset(offset));
					char c = document.getChar(lineOffset);
					while (lineOffset < offset) {
						if (c != ' ' && c != '\t') {
							document.replace(offset, len, "//SINGLE_LINE_NOLF//");
							break;
						}
						else {
							lineOffset++;
						}
						c = document.getChar(lineOffset);
					}
					if (lineOffset == offset) {
						document.replace(offset, len, "//SINGLE_LINE//");
						offset += "//SINGLE_LINE//".length();
					}
					else {
						offset += "//SINGLE_LINE_NOLF//".length();
					}
				}
				else if (document.getChar(offset + 1) == '*') {		// multiline comment
					commentOrString = findReplaceDocumentAdapter.find(offset + 2, "*/", true, false, false, false);
					str = document.get(offset, commentOrString.getOffset() + 1 - offset + 1);
					document.replace(offset, commentOrString.getOffset() + 1 - offset + 1, "//MULTILINE//");
					offset += "//MULTILINE//".length();
				}
				else {		// string
					commentOrString = findReplaceDocumentAdapter.find(offset + 1, "\"", true, false, false, false);
					str = document.get(offset, commentOrString.getOffset() - offset + 1);
					document.replace(offset, commentOrString.getOffset() - offset + 1, "//STRING//");
					offset += "//STRING//".length();
				}
				
				if (toList)
					list.add(str);
				if(offset > document.getLength() - 1)
					return list;
				commentOrString = findReplaceDocumentAdapter.find(offset, "//|/\\*|\"|\'", true, false, false, true);				
			}
			
			return list;
		
		} catch (BadLocationException e) {
			TinyOSPlugin.log(e);
		}
		
		return list;
	}
}
