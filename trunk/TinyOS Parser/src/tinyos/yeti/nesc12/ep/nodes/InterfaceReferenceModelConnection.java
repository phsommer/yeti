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
package tinyos.yeti.nesc12.ep.nodes;

import java.io.IOException;

import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.ep.storage.ReferenceFactory;
import tinyos.yeti.nesc12.ep.BindingResolver;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Binding;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterfaceReference;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.TypeFactory;
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionMap;
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionTable;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.Expression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.ExpressionList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ParameterizedInterface;

/**
 * Represents the reference to an interface or array of interfaces as they could stand
 * in a uses/provides clause: "uses interface reference &lt; parameters &gt; as name[ index ]"
 * @author Benjamin Sigg
 */
public class InterfaceReferenceModelConnection extends ModelConnection {
    public static final IGenericFactory<InterfaceReferenceModelConnection> FACTORY = 
        new ReferenceFactory<InterfaceReferenceModelConnection>( ModelConnection.FACTORY ){

        public InterfaceReferenceModelConnection create(){
            return new InterfaceReferenceModelConnection();
        }
        
        @Override
        public void write( InterfaceReferenceModelConnection value, IStorage storage ) throws IOException{
            super.write( value, storage );
            
            storage.write( value.reference );
            storage.write( value.name );
            
            TypeFactory.writeTypeArray( value.parameters, storage );
            storage.write( value.indices );
            
            storage.out().writeBoolean( value.used );
            storage.out().writeBoolean( value.provided );
        }
        
        @Override
        public InterfaceReferenceModelConnection read( InterfaceReferenceModelConnection value, IStorage storage ) throws IOException{
            super.read( value, storage );
            
            value.reference = storage.read();
            value.name = storage.read();
            
            value.parameters = TypeFactory.readTypeArray( storage );
            value.indices = storage.read();
            
            value.used = storage.in().readBoolean();
            value.provided = storage.in().readBoolean();
            
            return value;
        }
    };
    
    private Name reference;
    private Name name;
    private Type[] parameters;
    private Field[] indices;
    
    private boolean used;
    private boolean provided;
    
    private ParameterizedInterface astReference;
    
    protected InterfaceReferenceModelConnection(){
        // nothing
    }
    
    /**
     * Create a new connection
     * @param reference the name of the raw referenced interface
     * @param node the node that created this connection
     * @param astReference the {@link ParameterizedInterface} that is represented by this
     * connection (used to resolve bindings).
     * @param parameters the type parameters of the interface
     * @param name the name behind the "as" keyword or <code>null</code>
     * @param used whether this connection is in a "used" clause
     * @param provided whether this connection is in a "provided" clause
     * @param indices the kind of elements that the index expects
     */
    public InterfaceReferenceModelConnection( Name reference, ASTNode node, ParameterizedInterface astReference, Type[] parameters, Name name, boolean used, boolean provided, Field[] indices ){
        super( reference.toIdentifier(), node );
        setReference( true );
        
        TagSet tags = new TagSet();
        if( used )
            tags.add( Tag.USES );
        if( provided )
            tags.add( Tag.PROVIDES );
        if( name != null )
            tags.add( Tag.RENAMED );
        
        tags.add( Tag.AST_CONNECTION_ICON_RESOLVE );
        tags.add( Tag.AST_CONNECTION_GRAPH_ICON_RESOLVE );
        tags.add( Tag.AST_CONNECTION_LABEL_RESOLVE );
        tags.add( Tag.AST_CONNECTION_GRAPH_LABEL_RESOLVE );
        tags.add( Tag.INTERFACE );
        setTags( tags );
        
        setAttributes( astReference.resolveAttributes() );
        
        if( name == null )
            setLabel( reference.toIdentifier() );
        else
            setLabel( name.toIdentifier() + " (" + reference.toIdentifier() + ")" );
        
        this.name = name;
        this.reference = reference;
        this.parameters = parameters;
        this.used = used;
        this.provided = provided;
        this.indices = indices;
        this.astReference = astReference;
    }
    
    @Override
    protected void resolveNameRanges(){
        if( reference != null )
            reference.resolveRange();
        
        if( name != null )
            name.resolveRange();
        
        if( parameters != null ){
            for( Type parameter : parameters ){
                if( parameter != null ){
                    parameter.resolveNameRanges();
                }
            }
        }
        
        if( indices != null ){
            for( Field field : indices ){
                if( field != null ){
                    field.resolveNameRanges();
                }
            }
        }
    }
    
    public ASTModelPath getFullPath(){
        return getPath().getChild( getName().toIdentifier() );
    }
    
    public Name getReference(){
        return reference;
    }
    
    public Type[] getParameters(){
        return parameters;
    }
    
    public Name getName(){
        if( name == null )
            return reference;
        return name;
    }
    
    public Name getRename(){
        return name;
    }
    
    public boolean isUsed(){
        return used;
    }
    
    public boolean isProvided(){
        return provided;
    }
    
    public Field[] getIndex(){
        return indices;
    }
    
    public NesCInterfaceReference resolve( BindingResolver bindings ){
        String id = "interface reference: " + getIdentifier() + " as " + getRename();
        Binding result = bindings.getBinding( getPath(), id );
        if( result == null ){
            result = new NesCInterfaceReference( getDeclarationResolver(), astReference, this, bindings );
            bindings.putBinding( getPath(), id, result );
        }
        
        if( result instanceof NesCInterfaceReference )
            return (NesCInterfaceReference)result;
        
        return null;
    }
 
    /**
     * Checks whether <code>expr</code> resolves to the correct number and
     * types of indices needed for this reference. Reports errors and warnings
     * if not so.
     * @param stack used to report messages
     * @param errorLocation the node that should be used to report errors with
     * no other location
     * @param expr the expression, can be <code>null</code>
     */
    public void checkIndices( AnalyzeStack stack, ASTNode errorLocation, Expression expr ){
    	Expression[] expressions;
    	Type[] put;
    	if( expr instanceof ExpressionList ){
    		ExpressionList list = (ExpressionList)expr;
    		expressions = new Expression[ list.getChildrenCount() ];
    		for( int i = 0, n = expressions.length; i<n; i++ )
    			expressions[i] = list.getNoError( i );
    	}
    	else{
    		if( expr != null )
    			expressions = new Expression[]{ expr };
    		else
    			expressions = new Expression[]{};
    	}
    	
    	put = new Type[ expressions.length ];
    	for( int i = 0; i < put.length; i++ ){
    		if( expressions[i] != null )
    			put[i] = expressions[i].resolveType();
    	}
    	
    	int indicesLength = indices == null ? 0 : indices.length;
    	int putLength = put.length;
    	
    	for( int i = 0, n = Math.min( indicesLength, putLength ); i<n; i++ ){
    		if( indices[i] != null ){
    			Type index = indices[i].getType();
    			if( index != null && put[i] != null ){
    				ConversionMap map = ConversionMap.assignment( stack, expressions[i], null );
    				ConversionTable.instance().check( put[i], index, map );
    			}
    		}
    	}
    	
    	for( int i = putLength; i < indicesLength; i++ ){
    		if( indices[i] != null ){
    			Name name = indices[i].getName();
    			String label = indices[i].getType().toLabel( Name.toIdentifier( name ), Type.Label.SMALL );
    			stack.error( "Missing index " + (i+1) + " '" + label + "'", errorLocation );
    		}
    	}
    	
    	for( int i = indicesLength; i < putLength; i++ ){
    		if( expressions[i] != null ){
    			stack.error( "No index '" + (i+1) + "' specified", expressions[i] );
    		}
    	}
    }
}
