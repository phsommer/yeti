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
package tinyos.yeti.nesc12.parser.preprocessor.comment;

import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.preprocessor.CommentCallback;
import tinyos.yeti.preprocessor.FileInfo;
import tinyos.yeti.utility.IntList;

/**
 * A {@link FoldingCommentCallback} collects all the multi-line comments of
 * the top level file.
 * @author Benjamin Sigg
 */
public class FoldingCommentCallback implements CommentCallback{
	private IntList list = new IntList();
	
	public void transfer( AnalyzeStack stack ){
		for( int i = 0, n = list.length(); i<n; i += 2 ){
			stack.folding( list.get( i ), list.get( i+1 ) );
		}
	}
	
	public void singleLineComment( int offsetInFile, FileInfo file, String comment, boolean topLevel ){
		// ignore
	}
	
	public void multiLineComment( int offsetInFile, FileInfo file, String comment, boolean topLevel ){
		if( topLevel ){
			list.add( offsetInFile );
			list.add( comment.length() );
		}
	}
}
