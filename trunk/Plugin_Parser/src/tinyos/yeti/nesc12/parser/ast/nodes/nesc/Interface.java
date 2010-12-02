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

import static tinyos.yeti.ep.parser.ASTNodeFilterFactory.and;
import static tinyos.yeti.ep.parser.ASTNodeFilterFactory.or;
import static tinyos.yeti.ep.parser.ASTNodeFilterFactory.parent;
import static tinyos.yeti.ep.parser.ASTNodeFilterFactory.subset;

import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.ep.figures.InterfaceContent;
import tinyos.yeti.ep.parser.IASTFigureContent;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.nesc12.Parser;
import tinyos.yeti.nesc12.ep.declarations.BaseDeclaration;
import tinyos.yeti.nesc12.ep.nodes.InterfaceModelNode;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.Range;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.Flag;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.ExternalDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.util.ModifierValidator;
import tinyos.yeti.nesc12.parser.ast.util.NodeStack;
import tinyos.yeti.nesc12.parser.ast.util.pushers.FieldPusherFactory;
import tinyos.yeti.nesc12.parser.ast.util.validators.InterfaceValidator;

public class Interface extends AbstractFixedASTNode implements ExternalDeclaration{
    public static void main( String[] args ) {
        String code = "interface Kappa<t>{\n" +
        "command t put( int x );\n" +
        "}";
        Interface i = Parser.quickParser( code, Interface.class );
        System.out.println( i );
    }

    public static final Flag INTERFACE = new Flag( "interface" );

    public static final String NAME = "name";
    public static final String PARAMETERS = "parameters";
    public static final String ATTRIBUTES = "attributes";
    public static final String BODY = "body";

    public Interface(){
        super( "Interface", NAME, PARAMETERS, ATTRIBUTES, BODY );
    }

    public Interface( ASTNode name, ASTNode parameters, ASTNode attributes, ASTNode body ){
        this();
        setField( NAME, name );
        setField( PARAMETERS, parameters );
        setField( ATTRIBUTES, attributes );
        setField( BODY, body );
    }

    public Interface( Identifier name, InterfaceParameterList parameters, AttributeList attributes, DatadefList body ){
        this();
        setName( name );
        setParameters( parameters );
        setAttributes( attributes );
        setBody( body );
    }

    @Override
    public void resolve( AnalyzeStack stack ) {
        stack.folding( this );

        stack.pushScope( FieldPusherFactory.INTERFACE );
        if( stack.isReportErrors() ){
            stack.put( ModifierValidator.MODIFIER_VALIDATOR, new InterfaceValidator() );
        }

        Identifier name = getName();
        if( name != null ){
        	stack.getDeclarationStack().push( name.getName() );
        	
        	if( stack.isCreateDeclarations() ){
                BaseDeclaration declaration = stack.getDeclarationStack().set( Kind.INTERFACE, name.getName(), TagSet.get( Tag.INTERFACE ) );
                declaration.setFileRegion( stack.getRegion( name ) );
            }

            if( stack.isCreateModel() ){
                InterfaceModelNode interfaze = new InterfaceModelNode( name.getName() );
                interfaze.setDocumentation( getComments() );
                interfaze.setAttributes( getAttributes() );
                NodeStack nodes = stack.getNodeStack();

                nodes.include( interfaze, this );
                nodes.addChild( interfaze, this );
                
                stack.put( INTERFACE );
                nodes.pushNode( interfaze );
                nodes.setRange( getRange() );
                nodes.addLocation( getName() );
                nodes.addLocation( this );
                
                super.resolve( stack );
                stack.checkCancellation();
                stack.remove( INTERFACE );

                InterfaceParameterList parameters = getParameteres();
                if( parameters != null ){
                    interfaze.setGenerics( parameters.resolveGenericTypes() );
                }

                stack.putInterface( interfaze );
                if( stack.isCreateGraph() )
                    createGraph( stack, interfaze );
            }
            else{
                super.resolve( stack );
                stack.checkCancellation();
            }
            
            stack.getDeclarationStack().pop();
            
            if( stack.isReportErrors() ){
                if( !stack.isParseFileName( name.getName(), "nc" ) ){
                    stack.warning( "Interface '" + name.getName() + "' should be defined in a file '" + name.getName() + ".nc'", name );
                }
            }
        }
        else{
            super.resolve( stack );
            stack.checkCancellation();
        }

        stack.popScope( getRight() );

        if( stack.isCreateModel() )
            stack.getNodeStack().popNode( null );
    }

    private void createGraph( AnalyzeStack stack, InterfaceModelNode interfaze ){
        IASTModelNode[] functions = stack.getModel().getNodes(
                and( parent( interfaze.getPath() ), or( subset( Tag.EVENT ), subset( Tag.COMMAND ))));
        
        List<IASTFigureContent> events = new ArrayList<IASTFigureContent>();
        List<IASTFigureContent> commands = new ArrayList<IASTFigureContent>();

        for( IASTModelNode function : functions ){
            if( function.getTags().contains( Tag.EVENT ))
                events.add( function.getContent() );

            if( function.getTags().contains( Tag.COMMAND ))
                commands.add( function.getContent() );
        }

        InterfaceContent content = new InterfaceContent(
                commands.toArray( new IASTFigureContent[ commands.size() ] ),
                events.toArray( new IASTFigureContent[ events.size() ] ));

        interfaze.setContent( content );
    }
    
    @Override
    public Range getCommentAnchor(){
	    Identifier name = getName();
	    if( name == null )
	    	return null;
	    return name.getRange();
    }
    
    public void setName( Identifier name ){
        setField( 0, name );
    }
    public Identifier getName(){
        return (Identifier)getNoError( 0 );
    }

    public void setParameters( InterfaceParameterList parameters ){
        setField( 1, parameters );
    }
    public InterfaceParameterList getParameteres(){
        return (InterfaceParameterList)getNoError( 1 );
    }

    public void setAttributes( AttributeList attributes ){
        setField( 2, attributes );
    }
    public AttributeList getAttributes(){
        return (AttributeList)getNoError( 2 );
    }

    public void setBody( DatadefList body ){
        setField( 3, body );
    }
    public DatadefList getBody(){
        return (DatadefList)getNoError( 3 );
    }

    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ) {
            if( !( node instanceof Identifier ) )
                throw new ASTException( node, "Must be an Identifier" );
        }
        if( index == 1 ) {
            if( !( node instanceof InterfaceParameterList ) )
                throw new ASTException( node, "Must be a InterfaceParameterList" );
        }
        if( index == 2 ) {
            if( !( node instanceof AttributeList ) )
                throw new ASTException( node, "Must be a NesCAttributeList" );
        }
        if( index == 3 ) {
            if( !( node instanceof DatadefList ) )
                throw new ASTException( node, "Must be a DatadefList" );
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
