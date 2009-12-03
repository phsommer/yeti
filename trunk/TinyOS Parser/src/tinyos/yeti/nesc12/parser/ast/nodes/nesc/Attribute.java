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

import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.nesc12.Parser;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.declarations.IgnoreDeclaration;
import tinyos.yeti.nesc12.ep.declarations.TypedDeclaration;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.NesCAttribute;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.types.DataObjectType;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.Initializer;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitializerList;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.util.InitializerCounter;

public class Attribute extends AbstractFixedASTNode{
    public static void main( String[] args ) {
        String code = 
            "struct @blu{ int x[2]; int y; };" +
            "int main() @blu() { return 1; }";
            //"int x @blu( .x={1, 2} ) = 4;";
        
        Attribute a = Parser.quickParser( code, Attribute.class );
        System.out.println( a );
        
        System.out.println( a.resolveAttribute() );
    }
    
    public static final String NAME = "name";
    public static final String ARGUMENTS = "arguments";
    
    public Attribute(){
        super( "Attribute", NAME, ARGUMENTS );
    }
    
    public Attribute( ASTNode name, ASTNode arguments ){
        this();
        setField( NAME, name );
        setField( ARGUMENTS, arguments );
    }
    
    public Attribute( Identifier name, InitializerList arguments ){
        this();
        setName( name );
        setArguments( arguments );
    }
    
    /**
     * Gets the type which represents the layout of this attribute.
     * @return the layout of this attribute
     */
    public DataObjectType resolveType(){
        return resolved( "type" );
    }
    
    /**
     * Gets the value which is created by the initializer of this attribute.
     * @return the value or <code>null</code>
     */
    public Value resolveValue(){
        return resolved( "value" );
    }
    
    public NesCAttribute resolveAttribute(){
        if( isResolved( "attribute" ))
            return resolved( "attribute" );
        
        return resolved( "attribute", new NesCAttribute( this ));
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        cleanResolved();

        resolveComments( stack );
        resolve( 0, stack );
        Identifier name = getName();
        DataObjectType type = null;
        if( name != null ){
        	ASTModelPath path = null;
            boolean ignore = false;
            type = stack.getAttribute( name );
            if( type != null ){
            	ModelNode node = stack.getAttributeModel( name );
            	if( node != null )
            		path = node.getPath();
            }
            if( type == null ){
                IDeclaration declaration = stack.resolveDeclaration( name.getName(), true, Kind.ATTRIBUTE );
                if( declaration instanceof TypedDeclaration ){
                    type = ((TypedDeclaration)declaration).getType().asDataObjectType();
                    path = ((TypedDeclaration)declaration).getPath();
                }
                else if( declaration instanceof IgnoreDeclaration ){
                    ignore = true;
                }
            }
            
            if( stack.isReportErrors() && type == null && !ignore ){
                stack.error( "attribute not declared '" + name.getName() + "'", name );
            }
            stack.reference( this, path );
            resolved( "type", type );
        }
        
        if( type == null || getNoError( 1 ) == null ){
            resolve( 1, stack );
        }
        else{
            InitializerCounter counter = new InitializerCounter( stack, true, type );
            stack.put( Initializer.INITIALIZER_COUNTER, counter );
            counter.open( this );
            resolve( 1, stack );
            counter.close();
            stack.remove( Initializer.INITIALIZER_COUNTER );
            resolved( "value", counter.result( getNoError( 1 )) );
        }
    }
    
    public void setName( Identifier name ){
        setField( 0, name );
    }
    public Identifier getName(){
        return (Identifier)getNoError( 0 );
    }
    
    public void setArguments( InitializerList arguments ){
        setField( 1, arguments );
    }
    public InitializerList getArguments(){
        return (InitializerList)getNoError( 1 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ) {
            if( !( node instanceof Identifier ) )
                throw new ASTException( node, "Must be an Identifier" );
        }
        if( index == 1 ) {
            if( !( node instanceof InitializerList ) )
                throw new ASTException( node, "Must be an InitializerList" );
        }
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
