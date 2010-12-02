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
package tinyos.yeti.preprocessor.output;

import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.preprocessor.RangeDescription;
import tinyos.yeti.preprocessor.lexer.InclusionPath;
import tinyos.yeti.preprocessor.lexer.PreprocessorToken;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;
import tinyos.yeti.preprocessor.parser.stream.ElementStream;

/**
 * Allows to create new {@link RangeDescription}s.
 * @author Benjamin Sigg
 */
public final class RangeDescriptonBuilder{
	private int left;
	private int right;
	private boolean inclusion;

	/**
	 * Builds a new {@link RangeDescription} for the tokens between the character
	 * <code>left</code> and <code>right</code>.
	 * @param left the first character
	 * @param right the last character 
	 * @param tokens the list of available tokens
	 * @param inclusion whether to follow inclusions and macros
	 * @return the range
	 */
	public static RangeDescription build( int left, int right, TokenList tokens, boolean inclusion ){
		RangeDescriptonBuilder builder = new RangeDescriptonBuilder( left, right, inclusion );
		return builder.build( tokens );
	}

	public static RangeDescription build( ElementStream stream, boolean inclusion ){
		RangeDescriptonBuilder builder = new RangeDescriptonBuilder( -1, -1, inclusion );
		return builder.buildStream( stream );
	}

	private RangeDescriptonBuilder( int left, int right, boolean inclusion ){
		this.left = left;
		this.right = right;
		this.inclusion = inclusion;
	}

	private RangeDescription buildStream( ElementStream stream ){
		SingleNode root = new SingleNode();

		while( stream.hasNext() ){
			PreprocessorElement element = stream.next();
			PreprocessorToken token = element.getToken();
			if( token != null && token.hasLocation() ){
				root.insert( token, -1 );
			}
		}

		return build( root );
	}

	private RangeDescription build( TokenList tokens ){
		SingleNode root = new SingleNode();

		int index = tokens.getIndexOf( left );
		
        int offset = tokens.getOffset( index );
        int length = tokens.getLength( index );
        
        if( offset + length <= left ){
            // end of file
        	PreprocessorToken token = tokens.getToken( index );
        	root.insert( token, 0 );
        }
        else{
        	int size = tokens.size();
        	while( index < size ){
        		PreprocessorToken token = tokens.getToken( index );
        		if( token.hasLocation() ){
        			offset = tokens.getOffset( index );
        			length = tokens.getLength( index );

        			root.insert( token, index );

        			if( offset + length >= right )
        				break;
        		}
        		index++;
        	}
        }

		return build( root );
	}

	private RangeDescription build( SingleNode root ){
		MultiNode multi = new MultiNode();
		root.appendTo( multi );

		RangeDescription range = new RangeDescription( left, right );
		multi.toRange( range, null, 0, true );
		multi.toRoot( range );

		return range;
	}

	private class TokenEntry{
		public PreprocessorToken token;
		public int index;
		public InclusionPath path;
		
		public TokenEntry( PreprocessorToken token, int index, InclusionPath path ){
			this.token = token;
			this.index = index;
			this.path = path;
		}
		
		@Override
		public String toString() {
			return String.valueOf( token );
		}
	}
	
	/**
	 * A multi node represents a list of {@link SingleNode}s. The single nodes
	 * build a continuous range.
	 */
	private class MultiNode{
		private List<MultiNode> children = new ArrayList<MultiNode>();
		private List<TokenEntry> tokens = new ArrayList<TokenEntry>();
		// private List<SingleNode> singles = new ArrayList<SingleNode>();

		private RangeDescription.Range range;
		private boolean rangeCreated = false;

		public void add( MultiNode child ){
			children.add( child );
		}

		/*public void add( SingleNode node ){
			singles.add( node );
		}*/

		public void add( TokenEntry token ){
			tokens.add( token );
		}

		public RangeDescription.Range getRange() {
			return range;
		}

		private void toRoot( RangeDescription description ){
			for( MultiNode child : children ){
				description.addRoot( child.getRange() );
			}
		}

		/**
		 * Creates the range for this node.
		 * @param description where to store the ranges
		 * @param tokens the list of used tokens
		 * @param rangeFlags whether this node is part of a macro or inclusion
		 * @param fineLevel if <code>true</code>, then only the fine level is created. If <code>false</code>
		 * then the rough level is created as well.
		 */
		public void toRange( RangeDescription description, TokenList tokens, int rangeFlags, boolean fineLevel ){
			if( rangeCreated )
				return;
			rangeCreated = true;

			RangeDescription.Range[] roughRanges = null;
			RangeDescription.Range[] fineRanges = null;

			int childFlags = rangeFlags;
			int currentFlags = rangeFlags;

			for( TokenEntry entry : this.tokens ){
				InclusionPath path = entry.path;
				if( path != null ){
					if( path.include() ){
						childFlags |= RangeDescription.IN_INCLUDE;
						currentFlags |= RangeDescription.CONTAINS_INCLUDE;
					}
					if( path.macro() ){
						childFlags |= RangeDescription.IN_MACRO;
						currentFlags |= RangeDescription.CONTAINS_MACRO;
					}
				}
			}
			
			// build up the children
			if( children.size() > 0 ){
				// find flags of children
				
				if( fineLevel ){
					roughRanges = new RangeDescription.Range[ children.size() ];	
					for( int i = 0; i < roughRanges.length; i++ ){
						MultiNode child = children.get( i );
						child.toRange( description, tokens, childFlags, false );
						roughRanges[ i ] = child.getRange();
					}
				}
				else{
					fineRanges = new RangeDescription.Range[ children.size() ];	
					for( int i = 0; i < fineRanges.length; i++ ){
						MultiNode child = children.get( i );
						child.toRange( description, tokens, rangeFlags, true );
						fineRanges[ i ] = child.getRange();
					}

					int count = 0;
					for( MultiNode child : children ){
						count += child.children.size();
					}
					roughRanges = new RangeDescription.Range[ count ];
					int index = 0;
					for( MultiNode child : children ){
						for( MultiNode grandchild : child.children ){
							roughRanges[ index++ ] = grandchild.getRange();
						}
					}
				}
			}

			// position
			if( this.tokens.size() > 0 ){ // true for all except the root
				TokenEntry leftNode = this.tokens.get( 0 );
				TokenEntry rightNode = this.tokens.get( this.tokens.size()-1 );

				PreprocessorToken leftToken = leftNode.token;
				PreprocessorToken rightToken = rightNode.token;

				int leftIndex = leftNode.index;
				int rightIndex = rightNode.index;

				int begin;
				if( leftIndex == -1 || tokens == null ){
					begin = leftToken.getBeginLocation();
				}
				else{
					begin = leftToken.getBegin()[ left - tokens.getOffset( leftIndex ) ];
				}

				int end;
				if( rightIndex == -1 || tokens == null ){
					end = rightToken.getEndLocation();
				}
				else{
					int array = right - tokens.getOffset( rightIndex ) - 1;
					if( array >= 0 )
						end = rightToken.getEnd()[ array ];
					else
						end = rightToken.getBeginLocation();
				}

				// special flags
				if( fineRanges != null ){
					for( RangeDescription.Range range : fineRanges ){
						int flags = range.sourceFlags();
						currentFlags |= (flags & (RangeDescription.CONTAINS_INCLUDE | RangeDescription.CONTAINS_MACRO));
					}
				}

				if( fineLevel ){
					this.range = description.addFine( begin, end, leftToken.getLine(), currentFlags, leftToken.getFile(), roughRanges );
				}
				else{
					this.range = description.addRough( begin, end, leftToken.getLine(), currentFlags, leftToken.getFile(), roughRanges, fineRanges );
				}
			}
		}
	}

	/**
	 * Represents one token or an element that is composed of tokens.
	 */
	private class SingleNode{
		private List<SingleNode> children = new ArrayList<SingleNode>();

		private PreprocessorToken token;
		private int tokenIndex = -1;
		private PreprocessorElement element;
		private InclusionPath pathElement;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			toString( builder, "  " );
			return builder.toString();
		}

		private void toString( StringBuilder builder, String prefix ){
			builder.append( prefix );
			builder.append( "token=" );
			builder.append( token );
			builder.append( ", element=" );
			builder.append( element );
			for( SingleNode child : children ){
				builder.append( "\n" );
				child.toString( builder, prefix + "  " );
			}
		}

		/**
		 * Appends the children of this node as new {@link MultiNode}s to
		 * <code>node</code>.
		 * @param nodes the nodes of the upper level to fill
		 */
		public void appendTo( MultiNode... nodes ){
			MultiNode currentNode = null;
			MultiNode currentFineNode = null;

			SingleNode lastNode = null;
			PreprocessorToken lastToken = null;

			for( SingleNode check : children ){
				PreprocessorToken token = check.getToken();

				if( token != null ){
					// a simple node: consists only of one token
					if( token.hasLocation() && token.getFile() != null ){
						if( lastToken == null || !align( lastToken, token ) ){
							currentNode = new MultiNode();
							for( MultiNode node : nodes ){
								node.add( currentNode );
							}

							currentFineNode = new MultiNode();
							currentNode.add( currentFineNode );
						}
						else if( !lastNode.alignFine( check )){
							currentFineNode = new MultiNode();
							currentNode.add(currentFineNode);
						}

						lastNode = check;
						lastToken = token;

						TokenEntry entry = new TokenEntry( token, check.getTokenIndex(), check.getPathElement() );
						currentNode.add( entry );
						currentFineNode.add( entry );

						check.appendTo( currentFineNode );
					}
				}
				else{
					// a complex node: consists of several tokens
					PreprocessorElement element = check.getElement();
					if( element != null ){
						ElementStream stream = new ElementStream( element, true );

						List<MultiNode> fineNodes = new ArrayList<MultiNode>();
						MultiNode lastFineNode = null;
						
						while( stream.hasNext() ){
							token = stream.next().getToken();
							if( token != null && token.hasLocation() && token.getFile() != null ){
								if( lastToken == null || !align( lastToken, token )){
									currentNode = new MultiNode();
									for( MultiNode node : nodes ){
										node.add( currentNode );
									}

									currentFineNode = new MultiNode();
									currentNode.add( currentFineNode );
								}
								
								if( currentFineNode != null && lastFineNode != currentFineNode ){
									lastFineNode = currentFineNode;
									fineNodes.add( currentFineNode );
								}
								
								lastToken = token;
								lastNode = check;
								
								TokenEntry entry = new TokenEntry( token, check.getTokenIndex(), check.getPathElement() );
								currentNode.add( entry );
								currentFineNode.add( entry );
							}
						}
						
						if( fineNodes.size() > 0 ){
							check.appendTo( fineNodes.toArray( new MultiNode[ fineNodes.size() ]));
						}
					}
				}
			}
		}
		
		private boolean align( PreprocessorToken token, PreprocessorToken next ){
			if( token.getEndLocation() != next.getBeginLocation() )
				return false;

			if( !token.getFile().equals( next.getFile() ))
				return false;

			return true;			
		}

		private boolean alignFine( SingleNode next ){
			// assert: align( next )
			return lastChild() == next.firstChild();
		}

		private SingleNode lastChild(){
			int size = children.size();
			if( size == 0 )
				return null;
			return children.get( size-1 );
		}

		private SingleNode firstChild(){
			if( children.size() == 0 )
				return null;
			return children.get( 0 );
		}

		public PreprocessorToken getToken() {
			return token;
		}

		public int getTokenIndex() {
			return tokenIndex;
		}

		public PreprocessorElement getElement() {
			return element;
		}

		public InclusionPath getPathElement() {
			return pathElement;
		}

		/**
		 * Inserts a new token in the subtree below this root.
		 * @param token the new token
		 * @param tokenIndex the index of the token in the token-stream
		 */
		public void insert( PreprocessorToken token, int tokenIndex ){
			if( inclusion ){
				InclusionPath first = token.getPath();
				InclusionPath[] path = first == null ? new InclusionPath[]{} : first.toPath();

				insert( token, path, -1, tokenIndex );
			}
			else{
				insert( token, new InclusionPath[]{}, -1, tokenIndex );
			}
		}

		/**
		 * Inserts a new token into this tree.
		 * @param token the new token
		 * @param path the path to the token, might be empty
		 * @param index the index which <code>this</code> has on the path
		 * @param tokenIndex the index of the token in the token-stream
		 * @return <code>true</code> if the element was inserted, <code>false</code>
		 * if the value of <code>this</code> does not match the value of <code>path[ index ]</code>  
		 */
		public boolean insert( PreprocessorToken token, InclusionPath[] path, int index, int tokenIndex ){
			if( index >= 0 ){
				if( index == path.length ){
					if( this.token == null && element == null ){
						this.token = token;
						this.tokenIndex = tokenIndex;
						return true;
					}
					return false;
				}
				else{
					if( element == null && this.token == null ){
						pathElement = path[index];
						element = path[index].getElement();
						this.token = element.getToken();
					}
					else if( element != path[index].getElement() ){
						return false;
					}
				}
			}

			int size = children.size();
			if( size > 0 ){
				if( children.get( size-1 ).insert( token, path, index+1, tokenIndex ))
					return true;
			}

			children.add( new SingleNode() );
			children.get( size ).insert( token, path, index+1, tokenIndex );

			return true;
		}
	}
}