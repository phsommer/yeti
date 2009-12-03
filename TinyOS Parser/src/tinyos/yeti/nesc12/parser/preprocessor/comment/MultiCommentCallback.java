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

import tinyos.yeti.preprocessor.CommentCallback;
import tinyos.yeti.preprocessor.FileInfo;

/**
 * A comment observer that forwards calls to its methods to one or many
 * other observers.
 * @author Benjamin Sigg
 */
public class MultiCommentCallback implements CommentCallback{
	private CommentCallback[] delegates;
	
	public MultiCommentCallback( CommentCallback... delegates ){
		this.delegates = delegates;
	}
	
	public void add( CommentCallback delegate ){
		if( delegates == null ){
			delegates = new CommentCallback[]{ delegate };
		}
		else{
			CommentCallback[] temp = new CommentCallback[ delegates.length+1 ];
			System.arraycopy( delegates, 0, temp, 0, delegates.length );
			temp[ delegates.length ] = delegate;
			delegates = temp;
		}
	}
	
	public void singleLineComment( int offsetInFile, FileInfo file,
			String comment, boolean topLevel ){
		for( CommentCallback delegate : delegates ){	
			delegate.singleLineComment( offsetInFile, file, comment, topLevel );
		}
	}
	
	public void multiLineComment( int offsetInFile, FileInfo file,
			String comment, boolean topLevel ){
		for( CommentCallback delegate : delegates ){
			delegate.multiLineComment( offsetInFile, file, comment, topLevel );
		}
	}
}
