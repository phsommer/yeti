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

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.nesc12.Parser;
import tinyos.yeti.nesc12.ep.nodes.InterfaceReferenceModelConnection;
import tinyos.yeti.nesc12.parser.FileRegion;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.NesCAttribute;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;

public class ParameterizedInterface extends AbstractFixedASTNode{
    public static void main( String[] args ) {
        String code = "interface y<a,b>{ command a a(b b); }" +
                "configuration x{" +
                    "uses interface y<int, int> as z[int, int sec];" +
                "} implementation{}";
        
        ParameterizedInterface i = Parser.quickParser( code, ParameterizedInterface.class );
        System.out.println( i );
    }
    
    public static final String REFERENCE = "reference";
    public static final String PARAMETERS = "parameters";
    public static final String ATTRIBUTES = "attributes";
    
    public ParameterizedInterface(){
        super( "ParameterizedInterface", REFERENCE, PARAMETERS, ATTRIBUTES );
    }
    
    public ParameterizedInterface( ASTNode reference, ASTNode parameters, ASTNode attributes ){
        this();
        setField( REFERENCE, reference );
        setField( PARAMETERS, parameters );
        setField( ATTRIBUTES, attributes );
    }
    
    public ParameterizedInterface( InterfaceReference reference, IdentifierParameterList parameters, AttributeList attributes ){
        this();
        setReference( reference );
        setParameters( parameters );
        setAttributes( attributes );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        if( stack.isCreateModel() ){
        	NodeStack nodes = stack.getNodeStack();
        	
            nodes.pushNode( null );
            nodes.setRange( getRange() );
            
            super.resolve( stack );
            
            stack.checkCancellation();
            resolveModel( stack );
            nodes.popNode( null );
        }
        else{
            super.resolve( stack );
        }
    }
    
    public NesCAttribute[] resolveAttributes(){
    	if( isResolved( "attributes" ))
    		return resolved( "attributes" );
    	
    	AttributeList list = getAttributes();
    	if( list == null )
    		return resolved( "attributes", null );
    	
    	return resolved( "attributes", list.resolveAttributes() );
    }
    
    private void resolveModel( AnalyzeStack stack ){
        // create connection to interface
        InterfaceReference reference = getReference();
        if( reference == null )
            return;
        
        InterfaceType parameterized = reference.getName();
        if( parameterized == null )
            return;
        
        Identifier name = parameterized.getName();
        if( name == null )
            return;
        
        Name rawName = stack.name( name );
        TypeList typeList = parameterized.getTypes();
        Type[] types = null;
        if( typeList != null ){
            types = typeList.resolveTypes();
        }
        
        Identifier renameIdentifier = reference.getRename();
        Name rename = null;
        if( renameIdentifier != null ){
            rename = stack.name( renameIdentifier );
        }
        
        IdentifierParameterList parameters = getParameters();
        Field[] index = null;
        if( parameters != null ){
            index = parameters.resolveFields( stack );
        }
        
        NodeStack nodes = stack.getNodeStack();
        
        InterfaceReferenceModelConnection connection = new InterfaceReferenceModelConnection( 
                rawName, name, this, types, rename, 
                reference.resolveIsUsed(), reference.resolveIsProvided(),
                index );
        
        connection.addRegion( stack.getRegion( name ));
        connection.addRegion( stack.getRegion( this ));
        
        IDeclaration referenced = stack.getDeclarationResolver().resolve( rawName.toIdentifier(), stack.requestProgressMonitor(), Kind.INTERFACE );
        if( referenced != null ){
            IParseFile file = referenced.getParseFile();
            if( file != null ){
                connection.addRegion( new FileRegion( file, 0, 0, 0 ) );
            }
        }
        
        nodes.setConnection( connection );
        nodes.addConnection( connection, 1 );
        
        stack.putInterfaceReference( connection, getRange().getRight() );
        
        if( stack.isCreateReferences() ){
        	stack.reference( this, connection );
        }
    }
    
    
    public void setReference( InterfaceReference reference ){
        setField( 0, reference );
    }
    public InterfaceReference getReference(){
        return (InterfaceReference)getNoError( 0 );
    }
    
    public void setParameters( IdentifierParameterList parameters ){
        setField( 1, parameters );
    }
    public IdentifierParameterList getParameters(){
        return (IdentifierParameterList)getNoError( 1 );
    }
    
    public void setAttributes( AttributeList attributes ){
        setField( 2, attributes );
    }
    public AttributeList getAttributes(){
        return (AttributeList)getNoError( 2 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ) {
            if( !( node instanceof InterfaceReference ) )
                throw new ASTException( node, "Must be an InterfaceReference" );
        }
        if( index == 1 ) {
            if( !( node instanceof IdentifierParameterList ) )
                throw new ASTException( node, "Must be an IdentifierParameterList" );
        }
        if( index == 2 ) {
            if( !( node instanceof AttributeList ) )
                throw new ASTException( node, "Must be an NesCAttributeList" );
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
