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

import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.nodes.FieldModelConnection;
import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
import tinyos.yeti.nesc12.ep.nodes.InterfaceReferenceModelConnection;
import tinyos.yeti.nesc12.ep.nodes.ModuleModelNode;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterface;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterfaceReference;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.Expression;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;

public class NesCName extends AbstractFixedASTNode{
    public static final String INTERFACE = "interface";
    public static final String FUNCTION = "function";
    
    public NesCName(){
        super( "NesCName", INTERFACE, FUNCTION );
    }
    
    public NesCName( ASTNode interfaceName, ASTNode function ){
        this();
        setField( INTERFACE, interfaceName );
        setField( FUNCTION, function );
    }
    
    public NesCName( Identifier interfaceName, ParameterizedIdentifier function ){
        this();
        setInterface( interfaceName );
        setFunction( function );
    }
    
    public Field resolveField(){
        return resolved( "field" );
    }
    
    public NesCInterfaceReference resolveInterface(){
    	return resolved( "interface" );
    }
    
    /**
     * Tells whether the field of this name was declared in the uses/provides
     * list either directly or indirectly
     * @return <code>true</code> if declared in the list, <code>false</code>
     * otherwise
     * @throws NullPointerException if this name was not resolved or is not
     * in a module
     */
    public boolean resolveFromUsesProvidesList(){
        return resolved( "uplist" );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
        stack.checkCancellation();
        
        ModuleModelNode module = stack.get( Module.MODULE );
        
        if( stack.isReportErrors() ){
            if( module == null ){
                stack.error( "invalid identifier outside of module", this );
            }
        }
        
        if( module != null ){
            ParameterizedIdentifier parameterized = getFunction();
            Identifier id = parameterized == null ? null : parameterized.getIdentifier();
            String function = id == null ? null : id.getName();
            
            if( function != null ){
                Identifier idInterface = getInterface();
                String interfaze = idInterface == null ? null : idInterface.getName();
                
                if( interfaze == null ){
                    resolveLocal( module, function, stack );
                }
                else{
                    resolveExtern( module, interfaze, function, stack );
                }
            }
        }
        
        if( stack.isCreateReferences() ){
        	NesCInterfaceReference reference = resolveInterface();
        	if( reference != null ){
        		NesCInterface interfaze = reference.getRawReference();
        		if( interfaze != null ){
        			ASTModelPath target = interfaze.getNode().getPath();
        			stack.reference( getInterface(), target );
        			
        		 	ParameterizedIdentifier function = getFunction();
        		 	Identifier functionId = function == null ? null : function.getIdentifier();
        		 	String functionName = functionId == null ? null : functionId.getName();
        		 	
        		 	if( functionName != null ){
        		 		Field field = interfaze.getField( functionName );
        		 		if( field != null ){
        		 			stack.reference( function, field.getPath() );
        		 		}
        		 	}
        		}
        	}
        }
    }
    
    private void resolveLocal( ModuleModelNode module, String name, AnalyzeStack stack ){
        FieldModelConnection fieldConnection = module.getUsesProvidesFunction( name, stack.getCancellationMonitor() );
        if( fieldConnection != null ){
            resolved( "uplist", true );
            
            ModelNode node = stack.getDeclarationResolver().resolve( fieldConnection, stack.requestProgressMonitor() );
            stack.checkCancellation();
            
            if( node instanceof FieldModelNode ){
                resolved( "field", (FieldModelNode)node );
            }
        }
        else{
            resolved( "uplist", false );
            
            FieldModelConnection connection = module.searchFunction( name, stack.getCancellationMonitor() );
            if( connection == null ){
                if( stack.isReportErrors() ){
                    stack.error( "missing declaration for '" + name + "'", this );
                }
            }
            else{
                ModelNode node = stack.getDeclarationResolver().resolve( connection, stack.requestProgressMonitor() );
                stack.checkCancellation();
                
                if( node instanceof FieldModelNode ){
                    FieldModelNode field = (FieldModelNode)node;
                    resolved( "field", field );
                }
            }
        }
    }
    
    private void resolveExtern( ModuleModelNode module, String interfaceName, String functionName, AnalyzeStack stack ){
        resolved( "uplist", true );
        InterfaceReferenceModelConnection reference = module.getUsesProvides( interfaceName, stack.getCancellationMonitor() );
        if( reference == null ){
            if( stack.isReportErrors() ){
                stack.error( "used/provided interface '" + interfaceName + "' not declared", getInterface() );
            }
        }
        else{
            NesCInterfaceReference interfaceReference = reference.resolve( stack.getBindingResolver() );
            if( interfaceReference == null )
                return;
            
            resolved( "interface", interfaceReference );
            
            NesCInterface interfaze = interfaceReference.getParameterizedReference();
            if( interfaze == null )
                return;
            
            Field field = interfaze.getField( functionName );
            if( field == null && stack.isReportErrors() ){
                stack.error( "missing declaration for '" + functionName + "' in '" + interfaceName + "'", getFunction() );
            }
            resolved( "field", field );
            
            if( stack.isReportErrors() ){
            	checkIndices( reference, stack );
            }
        }
    }
    
    private void checkIndices( InterfaceReferenceModelConnection reference, AnalyzeStack stack ){
    	ParameterizedIdentifier function = getFunction();
    	if( function == null )
    		return;
    	
    	Identifier id = function.getIdentifier();
    	if( id == null )
    		return;
    	
    	Expression expr = function.getExpression();
    	reference.checkIndices( stack, id, expr );
    }
    
    public void setInterface( Identifier interfaceName ){
        setField( 0, interfaceName );
    }
    public Identifier getInterface(){
        return (Identifier)getNoError( 0 );
    }
    
    public void setFunction( ParameterizedIdentifier function ){
        setField( 1, function );
    }
    public ParameterizedIdentifier getFunction(){
        return (ParameterizedIdentifier)getNoError( 1 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ) {
            if( !( node instanceof Identifier ) )
                throw new ASTException( node, "Must be an Identifier" );
        }
        if( index == 1 ) {
            if( !( node instanceof ParameterizedIdentifier ) )
                throw new ASTException( node, "Must be a ParameterizedIdentifier" );
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
