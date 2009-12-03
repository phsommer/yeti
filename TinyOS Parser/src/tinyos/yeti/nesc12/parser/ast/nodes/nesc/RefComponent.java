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
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.declarations.IgnoreDeclaration;
import tinyos.yeti.nesc12.ep.nodes.ComponentReferenceModelConnection;
import tinyos.yeti.nesc12.ep.nodes.GenericComponentModelNode;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.NesCComponent;
import tinyos.yeti.nesc12.parser.ast.elements.SimpleName;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;

public class RefComponent extends AbstractFixedASTNode implements Component{
    public static final String NAME = "name";
    public static final String RENAME = "rename";
    
    public RefComponent(){
        super( "RefComponent", NAME, RENAME );
    }
    
    public RefComponent( ASTNode name, ASTNode rename ){
        this();
        setField( NAME, name );
        setField( RENAME, rename );
    }
    
    public RefComponent( Identifier name, Identifier rename ){
        this();
        setName( name );
        setRename( rename );
    }
    
    public NesCComponent resolveComponent(){
        return resolved( "component" );
    }
    
    public ModelConnection resolveConnection() {
    	return resolved( "model" );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        if( stack.isCreateModel() ){
        	NodeStack nodes = stack.getNodeStack();
            nodes.pushNode( null );
            nodes.setRange( getRange() );
        }
        
        super.resolve( stack );
        stack.checkCancellation();
        
        if( stack.isCreateModel() ){
            Identifier name = getName();
            if( name != null ){
                Identifier rename = getRename();
                
                ComponentReferenceModelConnection ref = new ComponentReferenceModelConnection( 
                        name.getName(), rename == null ? null : rename.getName(), name, false, null );
                
                resolved( "model", ref );
                stack.getNodeStack().addConnection( ref, 1 );
                stack.getNodeStack().setConnection( ref );
                stack.getNodeStack().addLocation( name );
                stack.getNodeStack().addLocation( this );
                
                Integer implementationStart = stack.get( ConfigurationDeclarationList.IMPLEMENTATION_START );
                if( implementationStart != null ){
                	stack.putComponentReference( ref, implementationStart );
                }
                
                stack.reference( this, ref );
                
                if( stack.isReportErrors() ){
                	ModelNode component = stack.getComponent( new SimpleName( null, name.getName() ) );
                    IDeclaration declaration = null;
                    if( component == null ){
                        declaration = stack.resolveDeclaration( name.getName(), true, Kind.MODULE, Kind.BINARY_COMPONENT, Kind.CONFIGURATION );
                    }
                    
                    if( !(declaration instanceof IgnoreDeclaration) ){
                        if( component == null && declaration == null ){
                            stack.error( "missing declaration for component '" + name.getName() + "'", name );
                        }
                        if( component == null && declaration != null ){
                            component = stack.getDeclarationResolver().resolve( declaration, stack.requestProgressMonitor() );
                            stack.checkCancellation();
                        }
                        if( component != null && component instanceof GenericComponentModelNode ){
                            if( ((GenericComponentModelNode)component).isGeneric() ){
                                stack.error( "component '" + name.getName() + "' is generic, requires the form 'new " + name.getName() + "'", name );
                            }
                        }
                    }
                }
            }
        }
        
        if( stack.isCreateModel() ){
            stack.getNodeStack().popNode( null );
        }
    }
    
    public String getFinalName() {
    	Identifier name = getFinalNameNode();
    	if( name != null )
    		return name.getName();
    	return null;
    }
    
    public Identifier getFinalNameNode() {
    	Identifier name = getRename();
    	if( name != null )
    		return name;
    	return getName();
    }
    
    public void setName( Identifier name ){
        setField( 0, name );
    }
    public Identifier getName(){
        return (Identifier)getNoError( 0 );
    }
    
    public void setRename( Identifier rename ){
        setField( 1, rename );
    }
    public Identifier getRename(){
        return (Identifier)getNoError( 1 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( !( node instanceof Identifier ) )
            throw new ASTException( node, "Must be an Identifier" );
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
