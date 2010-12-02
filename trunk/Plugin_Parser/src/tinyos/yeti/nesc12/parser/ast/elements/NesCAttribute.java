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

import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.parser.ast.elements.types.DataObjectType;
import tinyos.yeti.nesc12.parser.ast.elements.values.DataObject;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Attribute;

/**
 * A single instance of an attribute which a clause like "@name( 1, 2, 3 )"
 * could produce.
 * @author Benjamin Sigg
 */
public class NesCAttribute implements Binding{
    private Attribute attribute;
    private Field[] fields;
    
    public NesCAttribute( Attribute attribute ){
        this.attribute = attribute;
    }
    
	public String getBindingType(){
		return "Attribute";
	}

	public String getBindingValue(){
		Identifier name = attribute.getName();
		if( name != null )
			return name.getName();
		return null;
	}
	
	public int getSegmentCount(){
		return 1;
	}
	
	public Binding getSegmentChild( int segment, int index ){
		return getField( index );
	}

	public String getSegmentName( int segment ){
		return "Fields";
	}

	public int getSegmentSize( int segment ){
		return getFieldCount();
	}
    
    private void resolveFields(){
        if( fields == null ){
            DataObjectType type = attribute.resolveType();
            if( type != null ){
                fields = new Field[ type.getFieldCount() ];
                
                Value value = attribute.resolveValue();
                if( value instanceof DataObject ){
                    DataObject data = (DataObject)value;
                    for( int i = 0, n = Math.min( fields.length, data.getFieldCount() ); i<n; i++ ){
                        Field base = type.getField( i );
                        Value fieldValue = data.getValue( i );
                        if( fieldValue != null ){
                            base = new SimpleField( null, base.getType(), base.getName(), null, fieldValue, null, base.getPath() );
                        }
                        fields[i] = base;
                    }
                    for( int i = Math.min( fields.length, data.getFieldCount() ), n = fields.length; i<n; i++ ){
                        fields[i] = type.getField( i );
                    }
                }
                else{
                    for( int i = 0, n = fields.length; i<n; i++ ){
                        fields[i] = type.getField( i );
                    }
                }
            }
            
            if( fields == null )
                fields = new Field[]{};
        }
    }
    
    /**
     * Gets the name of this attribute.
     * @return the name
     */
    public Identifier getName(){
        return attribute.getName();
    }
    
    public String getAttributeName(){
    	Identifier name = getName();
    	if( name == null )
    		return null;
    	return name.getName();
    }
    
    public ModelAttribute toModelAttribute(){
    	String name = getAttributeName();
    	if( name == null )
    		return null;
    	return new ModelAttribute( name );
    }
    
    /**
     * Gets the number of fields that are specified in the attribute.
     * @return the number of fields
     */
    public int getFieldCount(){
        resolveFields();
        return fields.length;
    }
    
    /**
     * Gets the index'th field of the attribute. If the attribute is valid,
     * then type, name and value of the field are non <code>null</code>.
     * @param index the index of the field
     * @return the field which can contain <code>null</code> values or
     * be <code>null</code>
     */
    public Field getField( int index ){
        resolveFields();
        return fields[ index ];
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append( "attribute" );
        builder.append( "[name=" );
        builder.append( getName() == null ? null : getName().getName() );
        builder.append( ", fields={" );
        for( int i = 0, n = getFieldCount(); i<n; i++ ){
            if( i > 0 )
                builder.append( ", " );
            builder.append( getField( i ) );
        }
        builder.append( "}]" );
        return builder.toString();
    }
}
