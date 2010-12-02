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
package tinyos.yeti.nesc12.parser.ast.nodes.general;

import tinyos.yeti.nesc12.lexer.Token;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractTokenASTNode;
import tinyos.yeti.preprocessor.output.Insights;

public class Identifier extends AbstractTokenASTNode {
    private String name;
    
    public Identifier( Token token ){
        super( "Identifier", token );
        if( token != null )
            setName( token.getText() );
    }
    
    public Identifier( String name ){
        this( (Token)null );
        setName( name );
    }
    
    public void setName( String name ) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    protected boolean visit( ASTVisitor visitor ) {
        return visitor.visit( this );
    }
    @Override
    protected void endVisit( ASTVisitor visitor ) {
        visitor.endVisit( this );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ){
    	super.resolve( stack );
    	if( stack.isReportErrors() ){
    		if( name.contains( "$" )){
    			stack.error( "Character '$' not supported in NesC", Insights.badCharacter( "$" ), this );
    		}
    	}
    }
    
    @Override
    public int hashCode() {
        return name == null ? 0 : name.hashCode();
    }
    @Override
    public boolean equals( Object obj ) {
        if( obj instanceof Identifier ){
            Identifier id = (Identifier)obj;
            
            if( name == null )
                return id.name == null;
            
            return name.equals( id.name );
        }
        
        return false;
    }
}
