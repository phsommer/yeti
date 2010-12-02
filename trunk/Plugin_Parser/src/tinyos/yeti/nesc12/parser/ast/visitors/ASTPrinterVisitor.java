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
package tinyos.yeti.nesc12.parser.ast.visitors;

import tinyos.yeti.nesc12.lexer.Token;
import tinyos.yeti.nesc12.parser.ast.Range;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.FixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.TokenASTNode;
import tinyos.yeti.nesc12.parser.preprocessor.comment.NesCDocComment;
import tinyos.yeti.preprocessor.PreprocessorReader;
import tinyos.yeti.preprocessor.RangeDescription;


public class ASTPrinterVisitor extends ConvergingASTVisitor{
    private StringBuilder builder = new StringBuilder();
    private int level = 0;
    private PreprocessorReader reader;
    
    public ASTPrinterVisitor(){
        
    }
    
    public ASTPrinterVisitor( PreprocessorReader reader ){
        this.reader = reader;
    }
    
    @Override
    public String toString() {
        return builder.toString();
    }
    
    @Override
    public boolean convergedVisit( ASTNode node ) {
    	NesCDocComment[] comments = node.getComments();
    
    	for( int i = 0; i < level; i++ )
            builder.append( "  " );

    	if( comments != null ){
    		for( NesCDocComment comment : comments ){
    			builder.append( comment );
    			builder.append( "\n" );
    			for( int i = 0; i < level; i++ )
    				builder.append( "  " );
    		}
    	}
    	
        ASTNode parent = node.getParent();
        if( level > 0 && parent != null && parent instanceof FixedASTNode ){
            builder.append( ((FixedASTNode)parent).getFieldName( node ) );
            builder.append( ": " );
        }
        
        builder.append( node.getASTNodeName() ); 
        
        if( node instanceof TokenASTNode ){
            Token token = ((TokenASTNode)node).getToken();
            if( token != null ){
                builder.append( " [" );
                builder.append( toString( token.getText() ) );
                builder.append( "]" );
            }
        }
        
        if( reader != null ){
            Range range = node.getRange();
            if( range != null ){
                RangeDescription desc = reader.range( range.getLeft(), range.getRight(), false );
                
                builder.append( " (" );
                desc.visit( new RangeDescription.Visitor<Object>(){
                	private boolean first = false;
                	
                	public Object visit( RangeDescription.Range range, boolean rough) {
                		if( rough ){
                			if( first )
                                first = false;
                            else
                                builder.append( ", " );
                            
                            builder.append( range.file() );
                            builder.append( " - " );
                            builder.append( range.left() );
                            builder.append( " - " );
                            builder.append( range.right() );		
                		}
                		
                		return null;
                	}
                });
                builder.append( ")" );
            }
        }
        
        builder.append( "\n" );
        
        level++;
        return true;
    }
    
    private String toString( Object value ){
        if( value == null )
            return "null";
        
        String check = value.toString();
        return check.replaceAll( "\n", "\\\\n" ).replaceAll( "\r", "\\\\r" );
    }
    
    @Override
    public void convergedEndVisit( ASTNode node ) {
        level--;
    }
}
