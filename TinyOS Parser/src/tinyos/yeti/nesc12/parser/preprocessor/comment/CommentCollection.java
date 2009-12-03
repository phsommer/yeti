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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tinyos.yeti.nesc12.Parser;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.Range;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.visitors.ConvergingASTVisitor;
import tinyos.yeti.preprocessor.CommentCallback;
import tinyos.yeti.preprocessor.FileInfo;
import tinyos.yeti.preprocessor.RangeDescription;

/**
 * Collects comments and redistributes them to {@link ASTNode}s
 * @author Benjamin Sigg
 */
public class CommentCollection implements CommentCallback{
	private List<NesCDocComment> comments = new ArrayList<NesCDocComment>();
	
	public void multiLineComment( int offsetInFile, FileInfo file,
			String comment, boolean topLevel ){
		if( topLevel && comment.startsWith( "/**" )){
			comments.add( new NesCDocComment( offsetInFile, file, comment, topLevel ) );
		}
	}

	public void singleLineComment( int offsetInFile, FileInfo file,
			String comment, boolean topLevel ){
		// ignore
	}
	
	public void redistribute( ASTNode root, AnalyzeStack stack ){
		NesCDocComment[] comments = this.comments.toArray( new NesCDocComment[ this.comments.size() ] );
		Visitor visitor = new Visitor( comments, stack.getParser() );
		root.accept( visitor );
		ASTNode[] nodes = visitor.getNodes();
		
		Map<ASTNode, Integer> counting = new HashMap<ASTNode, Integer>();
		for( ASTNode node : nodes ){
			if( node != null ){
				Integer value = counting.get( node );
				if( value == null )
					value = 1;
				else
					value += 1;
				counting.put( node, value );
			}
		}
		
		Map<ASTNode, NesCDocComment[]> commenting = new HashMap<ASTNode, NesCDocComment[]>();
		for( Map.Entry<ASTNode, Integer> count : counting.entrySet() ){ 
			commenting.put( count.getKey(), new NesCDocComment[ count.getValue() ] );
		}
		
		for( int i = 0; i < comments.length; i++ ){
			if( nodes[i] != null ){
				NesCDocComment[] array = commenting.get( nodes[i] );
				for( int j = 0; j < array.length; j++ ){
					if( array[j] == null ){
						array[j] = comments[i];
						break;
					}
				}
			}
		}
		
		for( Map.Entry<ASTNode, NesCDocComment[]> comment : commenting.entrySet() ){
			comment.getKey().setComments( comment.getValue() );
		}
	}
	
	private static class Visitor extends ConvergingASTVisitor{
		private NesCDocComment[] comments;
		private RangeDescription.Range[] bestRanges;
		private ASTNode[] bestNodes;
		
		private Parser parser;
		
		public Visitor( NesCDocComment[] comments, Parser parser ){
			this.comments = comments;
			this.parser = parser;
			
			bestRanges = new RangeDescription.Range[ comments.length ];
			bestNodes = new ASTNode[ comments.length ];
		}
		
		public ASTNode[] getNodes(){
			return bestNodes;
		}
		
		@Override
		public boolean convergedVisit( ASTNode node ){
			Range anchor = node.getCommentAnchor();
			if( anchor != null ){
				RangeDescription declaration = parser.resolveLocation( false, anchor.getLeft(), anchor.getRight() );
				
				RangeDescription.Range leftMost = null;
				for( RangeDescription.Range check : declaration.getRoots() ){
					if( leftMost == null || check.left() < leftMost.left() ){
						leftMost = check;
					}
				}
				
				if( leftMost != null ){
					for( int i = 0; i < comments.length; i++ ){
						NesCDocComment comment = comments[i];
						if( comment.getOffsetInFile() < leftMost.left() ){
							if( bestRanges[i] == null || bestRanges[i].left() > leftMost.left() ){
								if( comment.getFile().equals( leftMost.file() )){
									bestRanges[i] = leftMost;
									bestNodes[i] = node;
								}	
							}
						}
					}
				}
			}
			
			return true;
		}
		
		@Override
		public void convergedEndVisit( ASTNode node ){
			// ignore	
		}		
	}
}
