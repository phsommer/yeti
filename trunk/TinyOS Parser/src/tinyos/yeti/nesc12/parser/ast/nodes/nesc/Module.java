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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tinyos.yeti.ep.figures.ModuleContent;
import tinyos.yeti.ep.parser.IASTFigureContent;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.parser.standard.ASTModel;
import tinyos.yeti.nesc12.Parser;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.NesC12ASTModel;
import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
import tinyos.yeti.nesc12.ep.nodes.ModuleModelNode;
import tinyos.yeti.nesc12.parser.ParserInsights;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.Range;
import tinyos.yeti.nesc12.parser.ast.elements.CombinedName;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterface;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterfaceReference;
import tinyos.yeti.nesc12.parser.ast.elements.NesCModule;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.ErrorASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.Key;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.ExternalDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.util.ModifierValidator;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;
import tinyos.yeti.nesc12.parser.ast.util.pushers.FieldPusherFactory;
import tinyos.yeti.nesc12.parser.ast.util.validators.ModuleImplementationValidator;

public class Module extends AbstractFixedASTNode implements ExternalDeclaration{
    public static void main( String[] args ) {
        String code = 
                "interface y<t>{ command t send(); }" +
                "interface a{}" +
                
                "typedef int message_t;" +
                "typedef int am_id_t;" +
                "typedef unsigned short int uint8_t;" +
                
                "generic module x(int register id, typedef def){ " +
                    "uses interface y<def> as z[short]; " +
                    "provides interface a; " +
                "} " +
                "implementation{ " +
                     "default event message_t* Snoop.receive[am_id_t id](message_t* msg, void* payload, uint8_t len){" +
                         "return msg;" +
                     "}" +
                "}";
        
        Module m = Parser.quickParser( code, Module.class );
        System.out.println( m );
    }
    
    private boolean generic;
    
    public static final Key<ModuleModelNode> MODULE = new Key<ModuleModelNode>( "module" );
    
    public static final String NAME = "name";
    public static final String PARAMETERS = "parameters";
    public static final String ATTRIBUTES = "attributes";
    public static final String CONNECTIONS = "connections";
    public static final String IMPLEMENTATION = "implementation";
    
    public Module(){
        super( "Module", NAME, PARAMETERS, ATTRIBUTES, CONNECTIONS, IMPLEMENTATION );
    }
    
    public Module( boolean generic, Identifier name, TemplateParameterList parameters, AttributeList attributes, AccessList connections, NesCExternalDefinitionList implementation ){
        this();
        setGeneric( generic );
        setName( name );
        setParameters( parameters );
        setAttributes( attributes );
        setConnections( connections );
        setImplementation( implementation );
    }
    
    public Module( boolean generic, ASTNode name, ASTNode parameters, ASTNode attributes, ASTNode connections, ASTNode implementation ){
        this();
        setGeneric( generic );
        setField( NAME, name );
        setField( PARAMETERS, parameters );
        setField( ATTRIBUTES, attributes );
        setField( CONNECTIONS, connections );
        setField( IMPLEMENTATION, implementation );
    }
    
    public ModuleModelNode resolveNode(){
        return resolved( "node" );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        stack.folding( this );
        
        stack.pushScope( FieldPusherFactory.MODULE );
        
        Identifier id = getName();
        if( id == null ){
            resolveChecked( stack );
        }
        else{
        	stack.getDeclarationStack().push( id.getName() );
        	
            if( stack.isCreateDeclarations() ){
                TagSet tags = TagSet.get( Tag.MODULE, Tag.COMPONENT );
                if( isGeneric() )
                    tags.add( NesC12ASTModel.GENERIC );
                
                stack.getDeclarationStack().set( Kind.MODULE, id.getName(), tags );
            }
            
            if( stack.isCreateModel() ){
                Name name = stack.name( id );
                NodeStack nodes = stack.getNodeStack();
                
                ModuleModelNode node = new ModuleModelNode( isGeneric(), name );
                resolved( "node", node );
                nodes.include( node, this );
                nodes.addChild( node, this );
                node.setDocumentation( getComments() );
                node.setAttributes( getAttributes() );
                
                nodes.pushNode( node );
                nodes.setRange( getRange() );
                nodes.addLocation( name.getRange() );
                nodes.addLocation( this );
                nodes.preventChildrenClose( 2 );
                
                stack.put( Access.COMPONENT, node );
                stack.put( MODULE, node );
                stack.put( NesCExternalDefinitionList.IMPLEMENTATION );
                resolveChecked( stack );
                stack.remove( NesCExternalDefinitionList.IMPLEMENTATION );
                stack.put( Access.COMPONENT, null );
                stack.put( MODULE, null );
                stack.putComponent( node );
                
                if( stack.isReportErrors() ){
                    checkMissingFunctions( node, stack );
                }
                
                if( stack.isCreateGraph() )
                    createGraph( stack, node );
                
                nodes.popNode( null );
            }
            else{
                stack.put( NesCExternalDefinitionList.IMPLEMENTATION );
                resolveChecked( stack );
                stack.remove( NesCExternalDefinitionList.IMPLEMENTATION );
            }
            
            stack.getDeclarationStack().pop();
            
            if( stack.isReportErrors() ){
                String name = id.getName();
                if( !stack.isParseFileName( name, "nc" ) ){
                    stack.warning( "Module '" + name + "' should be defined in a file '" + name + ".nc'", id );
                }
                checkGenericParameter( stack );
            }
        }
        
        if( !(getParent() instanceof ErrorASTNode )){
            // if the parent is an error, then we can assume that there are
            // more nodes in the tree which should belong to this module
            // but are not in it because the tree is degenerated
            stack.popScope( getRight() );
        }
    }
    
    private void checkGenericParameter( AnalyzeStack stack ){
    	Identifier name = getName();
    	if( name == null )
    		return;
    	
    	if( isGeneric() && getParameters() == null ){
    		stack.error( "Module '" + name.getName() + "' marked as generic but missing the generic parameters", name );
    	}
    	else if( !isGeneric() && getParameters() != null ){
    		stack.error( "Module '" + name.getName() + "' not marked as generic but has generic parameters", name );
    	}
    }
    
    @Override
    public Range getCommentAnchor(){
	    Identifier name = getName();
	    if( name == null )
	    	return null;
	    return name.getRange();
    }
    
    private void resolveChecked( AnalyzeStack stack ){
        if( stack.isReportErrors() ){
        	resolveComments( stack );
            resolve( 0, stack );
            resolve( 1, stack );
            resolve( 2, stack );
            resolve( 3, stack );
            stack.checkCancellation();
            
            stack.put( ModifierValidator.MODIFIER_VALIDATOR, new ModuleImplementationValidator() );
            resolve( 4, stack );
        }
        else{
            super.resolve( stack );
        }
    }
    
    /**
     * Searches missing events and commands and reports them.
     * @param node the representation of this module
     * @param stack the stack containing more information
     */
    private void checkMissingFunctions( ModuleModelNode node, AnalyzeStack stack ){
        NesCModule module = node.resolve( stack.getBindingResolver() );
        if( module == null )
            return;
        
        // collect all implemented functions
        Set<Name> implemented = new HashSet<Name>();
        for( int i = 0, n = module.getFieldCount(); i<n; i++ ){
            Name name = module.getField( i ).getName();
            if( name != null )
                implemented.add( name );
        }
        
        // check each interface and function
        for( int i = 0, n = module.getUsesCount(); i<n; i++ ){
            NesCInterfaceReference reference = module.getUses( i );
            NesCInterface interfaze = reference.getParameterizedReference();
            if( interfaze != null ){
                Field[] events = interfaze.getEvents();
                for( Field event : events ){
                	if( event.getName() != null ){
	                    Name name = new CombinedName( null, reference.getName(), event.getName() );
	                    if( !implemented.contains( name )){
	                        stack.error( "missing implementation for event '" + 
	                                event.getType().toLabel( event.getName().toIdentifier(), Type.Label.SMALL ) + "'", 
	                                ParserInsights.moduleMissingFunction( reference.getName().toIdentifier(), event.getName().toIdentifier(), true, this ),
	                                reference.getName().getRange() );
	                        
	                        error( stack, reference );
	                    }
                	}
                }
            }
        }
        
        for( int i = 0, n = module.getProvidesCount(); i<n; i++ ){
            NesCInterfaceReference reference = module.getProvides( i );
            NesCInterface interfaze = reference.getParameterizedReference();
            if( interfaze != null ){
                Field[] commands = interfaze.getCommands();
                for( Field command : commands ){
                	if( command.getName() != null ){
                		Name name = new CombinedName( null, reference.getName(), command.getName() );
                		if( !implemented.contains( name )){
                			stack.error( "missing implementation for command '" + 
                					command.getType().toLabel( command.getName().toIdentifier(), Type.Label.SMALL ) + "'",
                					ParserInsights.moduleMissingFunction( reference.getName().toIdentifier(), command.getName().toIdentifier(), false, this ),
                					reference.getName().getRange() );

                			error( stack, reference );
                		}
                	}
                }
            }
        }
    }
    
    private void error( AnalyzeStack stack, NesCInterfaceReference reference ){
    	stack.getNodeStack().putErrorFlag( reference.getModel(), 0 );
    	/*
        ModelConnection model = reference.getModel();
        if( model != null ){
        	model.getTags().add( NesC12ASTModel.ERROR );
        }
        
        AccessList list = getConnections();
        if( list != null ){
        	ModelNode nodeList = list.resolveModel();
        	if( nodeList != null ){
        		nodeList.getTags().add( NesC12ASTModel.ERROR );
        	}
        }
        
        stack.getNodeStack().putErrorFlag();*/
    }
    
    private void createGraph( AnalyzeStack stack, ModuleModelNode module ){
        IASTModelPath parent = null;
        
        for( ModelConnection connection : module.getConnections() ){
            if( connection.getTags().contains( ASTModel.IMPLEMENTATION )){
                IASTModelNode node = stack.getModel().getNode( connection );
                if( node != null )
                    parent = node.getPath();
                
                break;
            }
        }
        
        if( parent == null ){
            ModuleContent content = new ModuleContent( null, null, null, null, null );
            content.addPath( module.getPath().getChild( "implementation" ) );
            module.setContent( content );
        }
        else{
        	NesCModule nesC = module.resolve( stack.getBindingResolver() );
        	
        	// does not look good, but perhaps it could be activated later
        	
        	/*
        	// provided
        	List<IASTFigureContent> provided = new ArrayList<IASTFigureContent>();
        	for( int i = 0, n = nesC.getProvidesCount(); i<n; i++ ){
        		NesCInterfaceReference reference = nesC.getProvides( i );
        		if( reference != null ){
        			IASTModelPath path = null;
        			NesCInterface interfaze = reference.getRawReference();
        			if( interfaze != null ){
        				IASTModelNode interfaceNode = interfaze.asNode();
        				if( interfaceNode != null ){
        					path = interfaceNode.getPath();
        				}
        			}
        			InterfaceReferenceModelConnection connection = reference.getModel();
        			LabelContent content = new LabelContent( connection.getLabel(), connection.getTags(), path );
        			provided.add( content );
        		}
        	}
        	for( int i = 0, n = nesC.getProvidesFunctionCount(); i<n; i++ ){
        		Field reference = nesC.getProvidesFunction( i );
        		if( reference != null && reference.asNode() != null ){
        			IASTFigureContent content = reference.asNode().getContent();
        			if( content != null ){
        				provided.add( content );
        			}
        		}
        	}
        	
        	// used
        	List<IASTFigureContent> used = new ArrayList<IASTFigureContent>();
        	for( int i = 0, n = nesC.getUsesCount(); i<n; i++ ){
        		NesCInterfaceReference reference = nesC.getUses( i );
        		if( reference != null ){
        			IASTModelPath path = null;
        			NesCInterface interfaze = reference.getRawReference();
        			if( interfaze != null ){
        				IASTModelNode interfaceNode = interfaze.asNode();
        				if( interfaceNode != null ){
        					path = interfaceNode.getPath();
        				}
        			}
        			InterfaceReferenceModelConnection connection = reference.getModel();
        			LabelContent content = new LabelContent( connection.getLabel(), connection.getTags(), path );
        			used.add( content );
        		}
        	}
        	for( int i = 0, n = nesC.getUsesFunctionCount(); i<n; i++ ){
        		Field reference = nesC.getUsesFunction( i );
        		if( reference != null && reference.asNode() != null ){
        			IASTFigureContent content = reference.asNode().getContent();
        			if( content != null ){
        				used.add( content );
        			}
        		}
        	}
        	*/
        	// functions
        	List<IASTFigureContent> commands = new ArrayList<IASTFigureContent>();
            List<IASTFigureContent> events = new ArrayList<IASTFigureContent>();
            List<IASTFigureContent> tasks = new ArrayList<IASTFigureContent>();

            for( int i = 0, n = nesC.getFieldCount(); i<n; i++ ){
            	Field field = nesC.getField( i );
            	if( field != null && field.isFunction() && field.asNode() != null ){
            		FieldModelNode function = field.asNode();
            	    if( function.getTags().contains( Tag.EVENT ))
	                    events.add( function.getContent() );
	
	                if( function.getTags().contains( Tag.COMMAND ))
	                    commands.add( function.getContent() );
	
	                if( function.getTags().contains( Tag.TASK ))
	                    tasks.add( function.getContent() );
            	}
            }

            ModuleContent content = new ModuleContent( 
            		null, // provided.toArray( new IASTFigureContent[ provided.size() ] ),
            		null, // used.toArray( new IASTFigureContent[ used.size() ] ),
                    commands.toArray( new IASTFigureContent[ commands.size() ] ),
                    events.toArray( new IASTFigureContent[ events.size() ] ),
                    tasks.toArray( new IASTFigureContent[ tasks.size() ] ));
            content.addPath( module.getPath().getChild( "implementation" ) );
            module.setContent( content );
        }
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
    
    public void setImplementation( NesCExternalDefinitionList implementation ){
        setField( 4, implementation );
    }
    public NesCExternalDefinitionList getImplementation(){
        return (NesCExternalDefinitionList)getNoError( 4 );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ) {
            if( !( node instanceof Identifier ) )
                throw new ASTException( node, "Must be an Identifier" );
        }
        if( index == 1 ) {
            if( !( node instanceof TemplateParameterList ) )
                throw new ASTException( node, "Must be a ComponentParameters" );
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
            if( !( node instanceof NesCExternalDefinitionList ) )
                throw new ASTException( node, "Must be an NesCExternalDefinitionList" );
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
