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

import java.util.HashSet;
import java.util.Set;

import tinyos.yeti.ep.figures.ConfigurationContent;
import tinyos.yeti.ep.figures.LazyContent;
import tinyos.yeti.ep.parser.IASTFigureContent;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.nesc12.Parser;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.NesC12ASTModel;
import tinyos.yeti.nesc12.ep.nodes.ComponentReferenceModelConnection;
import tinyos.yeti.nesc12.ep.nodes.ConfigurationModelNode;
import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
import tinyos.yeti.nesc12.ep.nodes.InterfaceReferenceModelConnection;
import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeBindingResolver;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.Range;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.NesCConfiguration;
import tinyos.yeti.nesc12.parser.ast.elements.NesCEndpoint;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterfaceReference;
import tinyos.yeti.nesc12.parser.ast.elements.NesCWire;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.Key;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.ExternalDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;
import tinyos.yeti.nesc12.parser.ast.util.pushers.FieldPusherFactory;
import tinyos.yeti.utility.Icon;

public class Configuration extends AbstractFixedASTNode implements ExternalDeclaration{
    public static void main( String[] args ) {
        String code = 
                "interface A<t>{" +
                    "command t send( int x, int y );" +
                "}" +
                "configuration X{} implementation{}" +
                "configuration MainC{" +
                    "provides interface Boot;" +
                    "uses{ interface A<signed long> as ALong; }" +
                "}" +
                "implementation{" +
                    "components new Example( 1, 2, unsigned long );" +
                    "components X, Second;" +
                    "components new Third( unsigned char, 23 );" +
                    "MainC.Boot[3] -> Example;" +
                    "Sup.er <- Ka.bum;" +
                    "ALong.send -> bla;" +
                "}";
        
        Configuration c = Parser.quickParser( code, Configuration.class, 1 ); 
        System.out.println( c );
    }
    
    public static final Key<ModelNode> SPECIFICATION_NODE = new Key<ModelNode>( "configuration.specification" );
	
    public static final String NAME = "name";
    public static final String PARAMETERS = "parameters";
    public static final String ATTRIBUTES = "attributes";
    public static final String CONNECTIONS = "connections";
    public static final String IMPLEMENTATION = "implementation";
    
    private boolean generic;
    
    public Configuration(){
        super( "Configuration", NAME, PARAMETERS, ATTRIBUTES, CONNECTIONS, IMPLEMENTATION );
    }
    
    public Configuration( boolean generic, Identifier name, TemplateParameterList parameters,
            AttributeList attributes, AccessList connections, ConfigurationDeclarationList implementation ){
        this();
        setGeneric( generic );
        setName( name );
        setParameters( parameters );
        setAttributes( attributes );
        setConnections( connections );
        setImplementation( implementation );
    }
    
    public Configuration( boolean generic, ASTNode name, ASTNode parameters, ASTNode attributes, ASTNode connections, ASTNode implementation ){
        this();
        setGeneric( generic );
        setField( NAME, name );
        setField( PARAMETERS, parameters );
        setField( ATTRIBUTES, attributes );
        setField( CONNECTIONS, connections );
        setField( IMPLEMENTATION, implementation );
    }
    
    public ModelAttribute[] resolveAttributes(){
    	AttributeList list = getAttributes();
    	if( list == null )
    		return null;
    	return list.resolveModelAttributes();
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        stack.folding( this );
        
        stack.pushScope( FieldPusherFactory.COMPONENT );
        
        Identifier id = getName();
        if( id == null ){
            super.resolve( stack );
            stack.checkCancellation();
        }
        else{
        	stack.getDeclarationStack().push( id.getName() );
        	if( stack.isCreateDeclarations() ){
                TagSet tags = TagSet.get( Tag.CONFIGURATION, Tag.COMPONENT );
                if( isGeneric() )
                    tags.add( NesC12ASTModel.GENERIC );
                
                stack.getDeclarationStack().set( Kind.CONFIGURATION, id.getName(), tags );
            }
            
        	ConfigurationModelNode configuration = null;
            if( stack.isCreateModel() ){
                NodeStack nodes = stack.getNodeStack();
                
                Name name = stack.name( id );
                configuration = new ConfigurationModelNode( name, generic );
                configuration.setDocumentation( getComments() );
                configuration.setAttributes( resolveAttributes() );
                
                nodes.include( configuration, this );
                nodes.addChild( configuration, 0, id );
                
                stack.put( Access.COMPONENT, configuration );
                stack.put( Connection.CONFIGURATION, configuration );
                
                nodes.pushNode( configuration );
                
                nodes.setRange( getRange() );
                nodes.addLocation( name.getRange() );
                nodes.addLocation( this );
                
                super.resolve( stack );
                stack.checkCancellation();
                
                
                stack.put( Access.COMPONENT, null );
                stack.put( Connection.CONFIGURATION, null );
                
                nodes.popNode( null );
                if( stack.isCreateGraph() )
                    createGraph( stack, configuration );
            }
            else{
                super.resolve( stack );
            }
            
            
            stack.getDeclarationStack().pop();
            
            
            if( stack.isReportErrors() ){
                String name = id.getName();
                if( !stack.isParseFileName( name, "nc" ) ){
                    stack.warning( "Configuration '" + name + "' should be defined in a file '" + name + ".nc'", id );
                }
                checkGenericParameter( stack );
                checkWiringComplete( stack, configuration );
            }
        }
        
        stack.popScope( getRange().getRight() );
    }

    private void checkGenericParameter( AnalyzeStack stack ){
    	Identifier name = getName();
    	if( name == null )
    		return;
    	
    	if( isGeneric() && getParameters() == null ){
    		stack.error( "Configuration '" + name.getName() + "' marked as generic but missing the generic parameters", name );
    	}
    	else if( !isGeneric() && getParameters() != null ){
    		stack.error( "Configuration '" + name.getName() + "' not marked as generic but has generic parameters", name );
    	}
    }
    
    private void checkWiringComplete( AnalyzeStack stack, ConfigurationModelNode configuration ){
    	if( configuration == null )
    		return;
    	
    	NesCConfiguration config = configuration.resolve( stack.getBindingResolver() );
    	ModelNode specification = stack.get( SPECIFICATION_NODE );
    	
    	// check that all interfaces are at least connected once
    	Set<NesCInterfaceReference> internReferences = new HashSet<NesCInterfaceReference>();
    	
    	for( int i = 0, n = config.getWireCount(); i<n; i++ ){
    	 	NesCWire wire = config.getWire( i );
    	 	addIfInternInterface( wire.getLeft(), internReferences );
    	 	addIfInternInterface( wire.getRight(), internReferences );
    	}
    	
    	for( int i = 0, n = config.getProvidesCount(); i<n; i++ ){
    		if( !internReferences.contains( config.getProvides( i ) )){
    			reportMissingWiring( config.getProvides( i ), stack, configuration, specification );
    		}
    	}
    	
    	for( int i = 0, n = config.getUsesCount(); i<n; i++ ){
    		if( !internReferences.contains( config.getUses( i ) )){
    			reportMissingWiring( config.getUses( i ), stack, configuration, specification );
    		}
    	}
    	
    	// check that all functions are at least connected once
    	Set<Field> internFunctionReferences = new HashSet<Field>();
    	
    	for( int i = 0, n = config.getWireCount(); i<n; i++ ){
    		NesCWire wire = config.getWire( i );
    		addIfInternFunction( wire.getLeft(), internFunctionReferences );
    		addIfInternFunction( wire.getRight(), internFunctionReferences );
    	}
    	
    	for( int i = 0, n = config.getProvidesFunctionCount(); i<n; i++ ){
    		if( !internFunctionReferences.contains( config.getProvidesFunction( i ) )){
    			reportMissingWiring( config.getProvidesFunction( i ), stack, configuration, specification );
    		}
    	}
    	
    	for( int i = 0, n = config.getUsesFunctionCount(); i<n; i++ ){
    		if( !internFunctionReferences.contains( config.getUsesFunction( i ) )){
    			reportMissingWiring( config.getUsesFunction( i ), stack, configuration, specification );
    		}
    	}
    }
    
    private void addIfInternInterface( NesCEndpoint endpoint, Set<NesCInterfaceReference> interfazes ){
    	if( endpoint == null )
    		return;
    	
    	if( !endpoint.isIntern() )
    		return;
    	
    	NesCInterfaceReference reference = endpoint.getInterface();
    	if( reference != null ){
    		interfazes.add( reference );
    	}
    }
    
    private void addIfInternFunction( NesCEndpoint endpoint, Set<Field> functions ){
    	if( endpoint == null )
    		return;
    	
    	if( !endpoint.isIntern() )
    		return;
    	
    	Field reference = endpoint.getFunction();
    	if( reference != null ){
    		functions.add( reference );
    	}
    }
    
    private void reportMissingWiring( NesCInterfaceReference reference, AnalyzeStack stack, ModelNode configNode, ModelNode specificationNode ){
    	InterfaceReferenceModelConnection connection = reference.getModel();
    	connection.getTags().add( NesC12ASTModel.WARNING );
    	configNode.getTags().add( NesC12ASTModel.WARNING );
    	if( specificationNode != null ){
    		specificationNode.getTags().add( NesC12ASTModel.WARNING );
    	}
    	stack.warning( "Interface '" + reference.getName() + "' not connected", connection.getAST() );
    }
    
    private void reportMissingWiring( Field reference, AnalyzeStack stack, ModelNode configNode, ModelNode specificationNode ){
    	if( reference.isFunction() ){
    		FieldModelNode fieldNode = reference.asNode();
    		if( fieldNode != null ){
    			fieldNode.getTags().add( NesC12ASTModel.WARNING );
    		}
	    	configNode.getTags().add( NesC12ASTModel.WARNING );
	    	if( specificationNode != null ){
	    		specificationNode.getTags().add( NesC12ASTModel.WARNING );
	    	}
	    	if( reference.getName() != null ){
	    		stack.warning( "Function '" + reference.getName() + "' not connected", reference.getName().getRange() );
	    	}
    	}
    }
    
    private void createGraph( AnalyzeStack stack, ConfigurationModelNode configuration ){
        ConfigurationContent content = new ConfigurationContent();
        
        // add representations
        ASTModelPath implementationPath = configuration.getPath().getChild( "implementation" );
        
        content.addPath( configuration.getPath().getChild( "specification" ));
        content.addPath( implementationPath );
        content.addPath( implementationPath.getChild( "components" ));
        content.addPath( implementationPath.getChild( "connections" ));
        
        // uses/provides
        for( ModelConnection connection : configuration.listUsesProvides( stack.getCancellationMonitor() ) ){
            stack.checkCancellation();
            
            if( connection instanceof InterfaceReferenceModelConnection ){
                if( connection.getTags().contains( Tag.USES )){
                    content.addUses( createContent( connection, stack ), 
                            ((InterfaceReferenceModelConnection)connection).getName().toIdentifier() );
                }
                if( connection.getTags().contains( Tag.PROVIDES ))
                    content.addProvides( createContent( connection, stack ),
                            ((InterfaceReferenceModelConnection)connection).getName().toIdentifier() );
            }
            else{
                if( connection.getTags().contains( Tag.USES )){
                    content.addUses( createContent( connection, stack ),
                            connection.getIdentifier() );
                }
                if( connection.getTags().contains( Tag.PROVIDES )){
                    content.addProvides( createContent( connection, stack ),
                            connection.getIdentifier() );
                }
            }
        }
        
        // components
        for( ComponentReferenceModelConnection connection : configuration.listComponents( stack.getCancellationMonitor() ) ){
            stack.checkCancellation();
            content.addComponent( createContent( connection, stack ), connection.getName() );
        }
        
        // connections
        NesCConfiguration binding = configuration.resolve( new AnalyzeBindingResolver( stack ) );
        if( binding != null ){
            for( int i = 0, n = binding.getWireCount(); i<n; i++ ){
                NesCWire wire = binding.getWire( i );

                NesCEndpoint left = wire.getLeft();
                NesCEndpoint right = wire.getRight();
                
                if( left != null && right != null ){
                    if( !left.getComponentName().equals( right.getComponentName() )){
                        boolean extern = left.isExtern() && right.isExtern();                        
                        
                        if( wire.isAssign() ){
                            String text;
                            
                            // -1: right to left, 1: left to right
                            int wireDirection = 0;
                            
                            if( left.isUsed() && right.isProvided() ){
                            	text = left.getName() + " -> " + right.getName();
                            	wireDirection = -1;
                            }
                            else if( left.isProvided() && right.isUsed() ){
                            	text = right.getName() + " -> " + left.getName();
                            	wireDirection = 1;
                            }
                            else{
                            	text = left.getName() + " = " + right.getName();
                            	
                            	int leftDirection = 0;
                            	int rightDirection = 0;
                            	
                            	if( left.isIntern() ){
                            		if( left.isUsed() )
                            			leftDirection = -1;
                            		else if( left.isProvided() )
                            			leftDirection = 1;
                            	}
                            	if( right.isIntern() ){
                            		if( right.isUsed() )
                            			rightDirection = 1;
                            		else if( right.isProvided() )
                            			rightDirection = -1;
                            	}
                            	wireDirection = leftDirection + rightDirection;
                            }


                            if( wireDirection < 0 ){
                            	content.addWireRightToLeft( 
	                                    left.getComponentName(),
	                                    right.getComponentName(),
	                                    text, 
	                                    extern,
	                                    wire.getPath() );                            	
                            }
                            else if( wireDirection > 0 ){
	                            content.addWireLeftToRight(
	                                    left.getComponentName(),
	                                    right.getComponentName(),
	                                    text, 
	                                    extern,
	                                    wire.getPath() );
                            }
                            else{
                            	content.addWireEqual(
	                                    left.getComponentName(),
	                                    right.getComponentName(),
	                                    text, 
	                                    extern,
	                                    wire.getPath() );
                            }
                        }
                        else if( wire.isLeftToRight() ){
                            content.addWireLeftToRight(
                                    left.getComponentName(),
                                    right.getComponentName(),
                                    left.getName() + " -> " + right.getName(),
                                    extern,
                                    wire.getPath() );
                        }
                        else if( wire.isRightToLeft() ){
                            content.addWireRightToLeft(
                                    left.getComponentName(),
                                    right.getComponentName(),
                                    right.getName() + " -> " + left.getName(),
                                    extern,
                                    wire.getPath() );
                        }   
                    }
                }
            }
        }
        
        
        configuration.setContent( content );
    }
    
    private IASTFigureContent createContent( ModelConnection connection, AnalyzeStack stack ){
        String label = connection.getLabel();
        
        IASTModelPath path = stack.getDeclarationResolver().resolvePath( connection, stack.getCancellationMonitor().getProgressMonitor() );
        
        return new LazyContent( label, new Icon( connection ), path );
    }
    
    @Override
    public Range getCommentAnchor(){
	    Identifier name = getName();
	    if( name == null )
	    	return null;
	    return name.getRange();
    }
    
    public void setGeneric( boolean generic ) {
        this.generic = generic;
    }
    public boolean isGeneric() {
        return generic;
    }
    
    public void setName( Identifier name ){
        setField( 0, name );
    }
    public Identifier getName(){
        return (Identifier)getNoError( 0 );
    }
    
    public void setParameters( TemplateParameterList parameters ){
        setField( 1, parameters );
    }
    public TemplateParameterList getParameters(){
        return (TemplateParameterList)getNoError( 1 );
    }
    
    public void setAttributes( AttributeList attributes ){
        setField( 2, attributes );
    }
    public AttributeList getAttributes(){
        return (AttributeList)getNoError( 2 );
    }
    
    public void setConnections( AccessList connections ){
        setField( 3, connections );
    }
    public AccessList getConnections(){
        return (AccessList)getNoError( 3 );
    }
    
    public void setImplementation( ConfigurationDeclarationList implementation ){
        setField( 4, implementation );
    }
    public ConfigurationDeclarationList getImplementation(){
        return (ConfigurationDeclarationList)getNoError( 4 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ) {
            if( !( node instanceof Identifier ) )
                throw new ASTException( node, "Must be an Identifier" );
        }
        if( index == 1 ) {
            if( !( node instanceof TemplateParameterList ) )
                throw new ASTException( node, "Must be a TemplateParameterList" );
        }
        if( index == 2 ) {
            if( !( node instanceof AttributeList ) )
                throw new ASTException( node, "Must be a NesCAttributeList" );
        }
        if( index == 3 ) {
            if( !( node instanceof AccessList ) )
                throw new ASTException( node, "Must be an AccessList" );
        }
        if( index == 4 ) {
            if( !( node instanceof ConfigurationDeclarationList ) )
                throw new ASTException( node, "Must be a ConfigurationDeclarationList" );
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
