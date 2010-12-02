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
package tinyos.yeti.nesc12.parser.ast.elements;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import tinyos.yeti.ep.parser.IASTModelNodeConnection;
import tinyos.yeti.ep.parser.inspection.INesCField;
import tinyos.yeti.ep.parser.inspection.INesCInspector;
import tinyos.yeti.ep.parser.inspection.INesCNode;
import tinyos.yeti.ep.parser.inspection.InspectionKey;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.parser.NesC12Inspector;
import tinyos.yeti.nesc12.parser.ast.elements.Type.Label;
import tinyos.yeti.nesc12.parser.ast.elements.types.FunctionType;
import tinyos.yeti.nesc12.parser.ast.elements.types.GenericType;
import tinyos.yeti.preprocessor.RangeDescription;

/**
 * A field is something like <code>int x = 5;</code>, where "int" is the type,
 * "x" the name and "5" the constant value. A field may have a name, type
 * and/or an initial value. Functions are also considered to be a field.
 * @author Benjamin Sigg
 *
 */
public class SimpleField extends AbstractNesCBinding implements Field{
    public static final IGenericFactory<SimpleField> FACTORY = new IGenericFactory<SimpleField>(){
        public SimpleField create(){
            return new SimpleField();
        }
        
        public void write( SimpleField value, IStorage storage ) throws IOException{
            storage.write( value.name );
            storage.write( value.modifiers );
            storage.write( value.type );
            storage.write( value.initialValue );
            storage.write( value.range );
            storage.write( value.arguments );
            storage.write( value.indices );
            storage.write( value.path );
            storage.write( value.attributes );
        }
        
        public SimpleField read( SimpleField value, IStorage storage ) throws IOException{
            value.name = storage.read();
            value.modifiers = storage.read();
            value.type = storage.read();
            value.initialValue = storage.read();
            value.range = storage.read();
            value.arguments = storage.read();
            value.indices = storage.read();
            value.path = storage.read();
            value.attributes = storage.read();
            
            return value;
        }
    };
    
    private Name name;
    private Modifiers modifiers;
    private Type type;
    private Value initialValue;
    private LazyRangeDescription range;
    private ASTModelPath path;
    
    private Field[] indices;
    private Name[] arguments;
    private Field[] argumentFields;
    private ModelAttribute[] attributes;
    
    private SimpleField(){
        super( "Field" );
    }
        
    public SimpleField( Modifiers modifiers, Type type, Name name, ModelAttribute[] attributes, Value initialValue, LazyRangeDescription range, ASTModelPath path ){
        super( "Field" );
        
        if( modifiers != null ){
            modifiers = modifiers.subModifier( ~Modifiers.ALL_TYPE_QUALIFIER );
        }
        
        this.modifiers = modifiers;
        this.type = type;
        this.name = name;
        this.initialValue = initialValue;
        this.range = range;
        this.path = path;
        this.attributes = attributes;
    }
    
    public IASTModelNodeConnection asConnection(){
    	return null;
    }
    
    public int getReferenceKindCount(){
    	if( isFunction() )
    		return 2;
    	else
    		return 0;
    }
    
    public InspectionKey<?> getReferenceKind( int index ){
    	if( isFunction() ){
    		switch( index ){
    			case 0: return ARGUMENT;
    			case 1: return DOCUMENTATION_REFERENCE;
    			default: return null;
    		}
    	}
    	return null;
    }
    
    @SuppressWarnings("unchecked")
	public <K extends INesCNode> K[] getReferences( InspectionKey<K> key, INesCInspector inspector ){
    	if( isFunction() ){
    		if( ARGUMENT.equals( key )){
    			resolveArgumentFields();
    			
    			INesCField[] result = new INesCField[ argumentFields.length ];
    			System.arraycopy( argumentFields, 0, result, 0, result.length );
    			return (K[])result;
    		}
    		if( DOCUMENTATION_REFERENCE.equals( key )){
    			if( name != null ){
	    			ASTModelPath parentPath = path.getParent();
	    			NesC12AST ast = ((NesC12Inspector)inspector).getAst();
	    			ModelNode parent = ast.getResolver().resolve( parentPath, null );
	    			INesCNode parentNode = inspector.getNode( parent );
	    			
	    			String[] names = name.segments();
	    			
	    			if( parentNode instanceof NesCModule ){
	    				if( names.length == 2 ){
	    					NesCModule module = (NesCModule)parentNode;
	    					NesCInterfaceReference reference = module.getInterfaceReference( names[0] );
	    					if( reference != null ){
	    						NesCInterface interfaze = reference.getRawReference();
	    						if( interfaze != null ){
	    							Field function = interfaze.getField( names[1] );
	    							if( function.getType() != null && function.getType().equals( getType() )){
	    								return (K[])new INesCNode[]{ function };
	    							}
	    						}
	    					}
	    				}
	    			}
	    			else if( parentNode instanceof NesCInterface ){
	    				if( names.length == 1 ){
	    					return (K[])new INesCNode[]{ parentNode };
	    				}
	    			}
    			}
    		}
    	}
    	
    	return null;
    }
    
    private void resolveArgumentFields(){
    	if( argumentFields != null )
    		return;
    	
    	if( !isFunction() ){
    		argumentFields = new Field[]{};
    	}
    	else{
    		FunctionType function = getType().asFunctionType();
    		
	    	argumentFields = new Field[ Math.min( arguments == null ? 0 : arguments.length, function == null ? 0 : function.getArgumentCount() ) ];
	    	
	    	for( int i = 0; i < argumentFields.length; i++ ){
	    		argumentFields[i] = new SimpleField( null, function.getArgument( i ), arguments[i], null, null, null, path );
	    	}
    	}
    }
    
    public void resolveNameRanges(){
        if( name != null )
            name.resolveRange();
        
        if( type != null )
            type.resolveNameRanges();
        
        if( initialValue != null )
            initialValue.resolveNameRanges();
        
        if( range != null )
            range.resolve();
        
        if( arguments != null ){
            for( Name name : arguments ){
                if( name != null )
                    name.resolveRange();
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
    
    public RangeDescription getRange(){
        if( name != null ){
            RangeDescription result = name.getRange();
            if( result != null )
                return result;
        }
        
        if( range != null ){
            return range.getRange();
        }
        
        return null;
    }
    
    public FieldModelNode asNode() {
        return null;
    }
    
    public SimpleField asSimple() {
        return this;
    }
    
    @Override
    public String getBindingValue(){
        Type type = getType();
        Name name = getName();
        if( type == null && name == null )
            return null;
        if( type == null )
            return name.toIdentifier();
        if( name == null )
            return type.toLabel( null, Label.SMALL );
        
        return type.toLabel( name.toIdentifier(), Label.SMALL );
    }
    
    public int getSegmentCount() {
        return 6;
    }
    
    public int getSegmentSize( int segment ) {
        switch( segment ){
            case 0:
            case 1:
            case 2: return 1;
            case 3: return indices == null ? 0 : indices.length;
            case 4: return arguments == null ? 0 : arguments.length;
            case 5: return attributes == null ? 0 : attributes.length;
            default: return 0;
        }
    }
    
    public String getSegmentName( int segment ) {
        switch( segment ){
            case 0: return "name";
            case 1: return "type";
            case 2: return "value";
            case 3: return "indices";
            case 4: return "arguments";
            case 5: return "attributes";
            default: return null;
        }
    }
    
    public Binding getSegmentChild( int segment, int index ) {
        switch( segment ){
            case 0: return getName();
            case 1: return getType();
            case 2: return getInitialValue();
            case 3: return indices[ index ];
            case 4: return arguments[ index ];
            case 5: return attributes[ index ];
            default: return null;
        }
    }

    public void setArgumentNames( Name[] arguments ){
        this.arguments = arguments;
    }
    
    public Name[] getArgumentNames(){
        return arguments;
    }
    
    
    /**
     * Gets the name of this field
     * @return the name, may be <code>null</code> when this field is
     * used as an argument in a function
     */
    public Name getName() {
        return name;
    }
    
    public void setAttributes( ModelAttribute[] attributes ){
		this.attributes = attributes;
	}
    
    public ModelAttribute[] getAttributes(){
    	return attributes;
    }
    
    public String getFieldName(){
    	if( name == null )
    		return null;
    	
    	return name.toIdentifier();
    }
    
    public Type getType() {
        return type;
    }
    
    public ASTModelPath getPath(){
	    return path;
    }
    
    public void setPath( ASTModelPath path ){
		this.path = path;
	}
    
    public boolean isField(){
    	return type != null && type.asFunctionType() == null;
    }
    
    public boolean isFunction(){
    	return type != null && type.asFunctionType() != null;
    }
    
    public String getFieldType(){
    	if( type == null )
    		return null;
    	
    	return type.toLabel( null, Label.DECLARATION );
    }
    
    public String getFunctionName(){
    	return getFieldName();
    }
    
    public String getFunctionResultType(){
    	if( type == null )
    		return null;
    	FunctionType function = type.asFunctionType();
    	if( function == null )
    		return null;
    	
    	return function.getResult().toLabel( null, Label.DECLARATION );
    }
    
    /**
     * Checks the type of this field and creates a field where generic
     * types are replaced.
     * @param generic the map that tells how to replace which generic type
     * with which concrete type.
     * @return this or a new field
     */
    public SimpleField replace( Field[] indices, Map<GenericType, Type> generic ){
        Type check = type == null ? null : type.replace( generic );
        if( check == type && (indices == null || indices.length == 0 ))
            return this;
        
        SimpleField result = new SimpleField( modifiers, check, name, attributes, initialValue, range, path );
        result.setArgumentNames( getArgumentNames() );
        result.setIndices( indices );
        return result;
    }
    
    public void setType( Type type ) {
        this.type = type;
    }
    
    public Value getInitialValue() {
        return initialValue;
    }
    
    public void setInitialValue( Value initialValue ) {
        this.initialValue = initialValue;
    }

    public void setModifiers( Modifiers modifiers ){
        this.modifiers = modifiers;
    }
    
    public Modifiers getModifiers(){
        return modifiers;
    }
    
    public void setIndices( Field[] fields ){
        this.indices = fields;
    }
    
    public Field[] getIndices(){
        return indices;
    }
    
    public String getDeclaration( String name ){
        if( name == null ){
            if( this.name != null )
                name = this.name.toIdentifier();
        }
        
        StringBuilder builder = new StringBuilder();
        
        if( modifiers != null ){
            builder.append( modifiers );
            builder.append( " " );
        }
        if( type != null ){
            if( type.asFunctionType() != null ){
                FunctionType function = type.asFunctionType();
                
                builder.append( function.getResult().toLabel( null, Type.Label.DECLARATION ) );
                if( name != null ){
                    builder.append( " " );
                    builder.append( name );
                }
                
                if( indices != null ){
                    builder.append( "[" );
                    for( int i = 0, n = indices.length; i<n; i++ ){
                        if( i != 0 )
                            builder.append( ", " );
                        
                        if( indices[i] != null )
                            builder.append( indices[i].getDeclaration( null ) );
                    }
                    builder.append( "]" );
                }
                
                builder.append( "(" );
                
                for( int i = 0, n = function.getArgumentCount(); i<n; i++ ){
                    Type argumentType = function.getArgument( i );
                    Name argumentName = (arguments == null) ? null : arguments[i];
                    
                    if( argumentType == null ){
                        if( argumentName != null ){
                            builder.append( argumentName.toIdentifier() );
                        }
                    }
                    else{
                        builder.append( argumentType.toLabel( argumentName == null ? null : argumentName.toIdentifier(), Type.Label.DECLARATION ) );
                    }
                    
                    if( i+1 < n )
                        builder.append( ", " );
                }
                builder.append( ")" );
            }
            else{
                builder.append( type.toLabel( name, Type.Label.DECLARATION ) );
            }
        }
        
        return builder.toString();
    }
    
    @Override
    public String toString() {
        return "field[name=" + name + ", modifiers= " + modifiers + ", type=" + type + ", value=" + initialValue + "]";
    }

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode( arguments );
        result = prime * result + Arrays.hashCode( indices );
        result = prime * result
                + ( ( modifiers == null ) ? 0 : modifiers.hashCode() );
        result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
        result = prime * result + ( ( type == null ) ? 0 : type.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj ){
        if( this == obj )
            return true;
        if( obj == null )
            return false;
        if( getClass() != obj.getClass() )
            return false;
        final SimpleField other = ( SimpleField )obj;
        if( !Arrays.equals( arguments, other.arguments ) )
            return false;
        if( !Arrays.equals( indices, other.indices ) )
            return false;
        if( modifiers == null ){
            if( other.modifiers != null )
                return false;
        }else if( !modifiers.equals( other.modifiers ) )
            return false;
        if( name == null ){
            if( other.name != null )
                return false;
        }else if( !name.equals( other.name ) )
            return false;
        if( type == null ){
            if( other.type != null )
                return false;
        }else if( !type.equals( other.type ) )
            return false;
        return true;
    }
}
