/*
 * Yeti 2, NesC development in Eclipse.
 * Copyright (C) 2009 ETH Zurich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Web:  http://tos-ide.ethz.ch
 * Mail: tos-ide@tik.ee.ethz.ch
 */
package tinyos.yeti.nesc.scanner;

import org.eclipse.jface.text.reconciler.DirtyRegion;

import tinyos.yeti.TinyOSPlugin;

/* semantic value of token returned by scanner */
public class Token implements ITokenInfo {  

  /** the line number the token begins */
  public int line;
  
  /**
   *  updated by updatePosition(DirtyRegion)
   */
  private boolean exists = true;
  
  /** 
   * character offset from beginning of the text 
   * zero based 
   */
  public int offset;
  
  /** the offset at which the token ends */
	public int end;

	/** the text of the token */
	public String text;

	public int code;

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	/**
	 * this constructor should not be used
	 */
	private Token() {

	}

	public int getLength() {
		return end - offset;
	}

	public int length() {
		return end - offset;
	}

	public Token(int code, String text, int lineBegin, int charBegin, int charEnd) {
		this.code = code;
		this.text = text;
		this.line = lineBegin;
		this.offset = charBegin;
		this.end = charEnd;
	}
	
	public Token(Token t) {
		this(t.code, t.text, t.line, t.offset, t.end); 
	}

	public String toString() {
		return text;
	}

	public boolean toBoolean() {
		return Boolean.valueOf(text).booleanValue();
	}

	public int toInt() {
		return Integer.valueOf(text).intValue();
	}

	/**
	 * returns should be deleted.. 
	 * @param region
	 * @return
	 */
	public boolean updatePosition(DirtyRegion region) {
	//	printREgion(region);
	//	printTokenInfo(); 
		/* end of dirtyregion*/
		int Roffset = region.getOffset();
		int Rlength = region.getLength();
		int Rend = Roffset + Rlength;
		System.out.println("--------"+text);
		if (region.getType() == DirtyRegion.INSERT) {
			// Primary criteria : where region offset relativ to element offset
			if (Roffset <= offset) {
				offset += Rlength;	
				//System.out.println("-------- 1");
				
			} else if (Roffset > end) {
				// dirty region after element
				//System.out.println("-------- 2");
			} else {
				// dirty region within element
				end -= Rlength;
				//System.out.println("-------- 3");
			}
			
		} else {
			// Region was removed
			
			if (Roffset < offset) {
				if (Rend < offset) {
					offset -= Rlength;
					end -= Rlength;
//					System.out.println("-------- 4");
				} else if (Rend < end) {
					offset = Roffset + Rlength;
					end = end - offset - Rend;
//					System.out.println("-------- 5");
				} else {
					// Element is deleted...
					offset = 0;
					end = 0;
					line = 0;
				//	printTokenInfo();
				//	System.out.println("-------- 6");
					return true;
				}
			} else if (Roffset > end) {
				// dirty region after element
//				System.out.println("-------- 7");
			} else {
				// dirty region within element
				if (Rend  < end) {
					end -= Rlength;
//					System.out.println("-------- 8");
				} else {
					end -= Roffset;
//					System.out.println("-------- 9");
				}
			}
			
		}
		//printTokenInfo();
		return false;
	}

	private void printTokenInfo() {
		TinyOSPlugin.getDefault().wirteToConsole("......................");
		TinyOSPlugin.getDefault().wirteToConsole("Token: "+text);
		TinyOSPlugin.getDefault().wirteToConsole("Offset "+offset);
		TinyOSPlugin.getDefault().wirteToConsole("End "+end);
		TinyOSPlugin.getDefault().wirteToConsole("Length "+(end-offset));
		
	}

	private void printREgion(DirtyRegion region) {
		TinyOSPlugin.getDefault().wirteToConsole("......................");
		TinyOSPlugin.getDefault().wirteToConsole("Dirty Info:");
		TinyOSPlugin.getDefault().wirteToConsole("type: "+region.getType());
		TinyOSPlugin.getDefault().wirteToConsole("offset: "+region.getOffset());
		TinyOSPlugin.getDefault().wirteToConsole("End "+(region.getOffset()+region.getLength()));
		TinyOSPlugin.getDefault().wirteToConsole("length: "+region.getLength());
	}
  
}