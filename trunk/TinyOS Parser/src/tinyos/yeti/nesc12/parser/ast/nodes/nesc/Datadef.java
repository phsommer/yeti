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
package tinyos.yeti.nesc12.parser.ast.nodes.nesc;

import java.util.Collection;

import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.Declaration;

public class Datadef extends AbstractFixedASTNode implements ConfigurationDeclaration{
    public Datadef(){
        super( "Datadef", "declaration" );
    }
    
    public Datadef( Declaration declaration ){
        this();
        setDeclaration( declaration );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO Auto-generated method stub
        super.resolve( stack );
    }
    
    public void reportComponents(Collection<Component> components) {
    	// ignore
    }
    
    public void setDeclaration( Declaration declaration ){
        setField( 0, declaration );
    }
    
    public Declaration getDeclaration(){
        return (Declaration)getNoError( 0 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( !(node instanceof Declaration ))
            throw new ASTException( node, "Must be a Declaration" );
    }
    
    @Override
    protected boolean visit( ASTVisitor visitor ) {
        return visitor.visit( this );
    }

    @Override
    protected void endVisit( ASTVisitor visitor ) {
        visitor.endVisit( this );
    }
}
