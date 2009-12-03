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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeConnection;
import tinyos.yeti.ep.parser.inspection.INesCField;
import tinyos.yeti.ep.parser.inspection.INesCInspector;
import tinyos.yeti.ep.parser.inspection.INesCInterface;
import tinyos.yeti.ep.parser.inspection.INesCNode;
import tinyos.yeti.ep.parser.inspection.InspectionKey;
import tinyos.yeti.nesc12.ep.BindingResolver;
import tinyos.yeti.nesc12.ep.DeclarationResolver;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
import tinyos.yeti.nesc12.ep.nodes.InterfaceModelNode;
import tinyos.yeti.nesc12.parser.ast.ICancellationMonitor;
import tinyos.yeti.nesc12.parser.ast.elements.types.GenericType;

/**
 * An interface of the form "interface Name<g1, g2>{ body }"
 * @author Benjamin Sigg
 */
public class NesCInterface extends AbstractNesCBinding implements INesCInterface{
    /** the raw interface */
    private InterfaceModelNode raw;
    
    /** the generic types that could still be applied */
    private GenericType[] generics;
    
    /** how to translate types from {@link #raw} to this interface */
    private Map<GenericType, Type> translation;
    
    /** indices to apply for each field */
    private Field[] indices;
    
    /** the (translated) fields */
    private Field[] fields;
    
    private DeclarationResolver declarations;
    private BindingResolver bindings;
    
    public NesCInterface( DeclarationResolver declarations, BindingResolver bindings, InterfaceModelNode raw ){
        super( "Interface" );
        this.raw = raw;
        this.declarations = declarations;
        this.bindings = bindings;
    }
    
    private NesCInterface( DeclarationResolver declarations, BindingResolver bindings, InterfaceModelNode raw, Field[] indices, GenericType[] generics, Map<GenericType, Type> translation ){
        this( declarations, bindings, raw );
        this.indices = indices;
        this.generics = generics;
        this.translation = translation;
    }
    
    public int getSegmentCount() {
        return 3;
    }
    
    @Override
    public String getBindingValue() {
        return getName();
    }
    
    public String getSegmentName( int segment ) {
        switch( segment ){
            case 0: return "Parameters";
            case 1: return "Fields";
            default: return null;
        }
    }
    
    public int getSegmentSize( int segment ) {
        switch( segment ){
            case 0: return getGenericCount();
            case 1: return getFieldCount();
            default: return 0;
        }
    }
    
    public Binding getSegmentChild( int segment, int index ) {
        switch( segment ){
            case 0: return getGeneric( index );
            case 1: return getField( index );
            default: return null;
        }
    }
    
    public String getName() {
        return raw.getIdentifier();
    }
    
    public InterfaceModelNode getNode(){
		return raw;
	}
    
    private void resolveGenerics(){
        if( generics == null ){
            generics = raw.getGenerics();
        }
        
        if( generics == null )
            generics = new GenericType[]{};
    }
    
    public int getGenericCount(){
        resolveGenerics();
        return generics.length;
    }
    
    public GenericType getGeneric( int index ){
        resolveGenerics();
        return generics[index];
    }
    
    private void resolveFields(){
        if( fields == null ){
            List<Field> fields = new ArrayList<Field>();
            for( ModelConnection connection : raw.getConnections() ){
                ICancellationMonitor monitor = bindings.getCancellationMonitor();
                ModelNode node = declarations.resolve( connection, monitor.getProgressMonitor() );
                monitor.checkCancellation();
                
                if( node instanceof FieldModelNode ){
                    Field field = (FieldModelNode)node;
                    if( translation != null )
                        field = field.replace( indices, translation );

                    if( field != null )
                        fields.add( field );
                }
            }
            this.fields = fields.toArray( new Field[ fields.size() ] );
        }
    }
    
    public int getFieldCount(){
        resolveFields();
        return fields.length;
    }
    
    public Field getField( int index ){
        resolveFields();
        return fields[index];
    }
    
    public Field getField( String name ){
        resolveFields();
        for( Field field : fields ){
            if( name.equals( Name.toIdentifier( field.getName() ) ) )
                return field;
        }
        return null;
    }
    
    public IASTModelNodeConnection asConnection(){
    	return null;
    }
    
    public IASTModelNode asNode(){
    	return raw;
    }
    
    public int getReferenceKindCount(){
    	return 2;
    }
    
    public InspectionKey<?> getReferenceKind( int index ){
    	switch( index ){
    		case 0: return EVENT;
    		case 1: return COMMAND;
    		default: return null;
    	}
    }
    
    @SuppressWarnings("unchecked")
	public <K extends INesCNode> K[] getReferences( InspectionKey<K> key, INesCInspector inspector ){
    	if( EVENT.equals( key )){
    		Field[] fields = getEvents();
    		INesCField[] result = new INesCField[ fields.length ];
    		System.arraycopy( fields, 0, result, 0, fields.length );
    		return (K[])result;
    	}
    	if( COMMAND.equals( key )){
    		Field[] fields = getCommands();
    		INesCField[] result = new INesCField[ fields.length ];
    		System.arraycopy( fields, 0, result, 0, fields.length );
    		return (K[])result;
    	}
    	return null;
    }
    
    public Field[] getEvents(){
        resolveFields();
        List<Field> events = new ArrayList<Field>();
        for( Field field : fields ){
            Modifiers modifiers = field.getModifiers();
            if( modifiers != null && modifiers.isEvent() ){
                events.add( field );
            }
        }
        return events.toArray( new Field[ events.size() ] );
    }
    
    public Field[] getCommands(){
        resolveFields();
        List<Field> commands = new ArrayList<Field>();
        for( Field field : fields ){
            Modifiers modifiers = field.getModifiers();
            if( modifiers != null && modifiers.isCommand() ){
                commands.add( field );
            }
        }
        return commands.toArray( new Field[ commands.size() ] );
    }

    /**
     * Replaces the generic types of this interface by the contents of
     * <code>generic</code> and returns a new interface that represents
     * <code>this</code> with the replacements.
     * @param indices indices to add for each function
     * @param generic the replacements for the currently used generic types,
     * can be <code>null</code>
     * @return the interface with the replacements, can be <code>this</code>
     * put will never be <code>null</code>
     */
    public NesCInterface replace( Field[] indices, Type[] generic ){
        if( (generic == null || generic.length == 0) && (indices == null || indices.length == 0) )
            return this;
        
        resolveGenerics();
        
        Map<GenericType, Type> map = new HashMap<GenericType, Type>();
        List<GenericType> missing = new ArrayList<GenericType>();
        if( generics != null && generic != null ){
            for( int i = 0, n = Math.min( generic.length, generics.length ); i<n; i++ ){
                if( generics[i] != null && generic[i] != null )
                    map.put( generics[i], generic[i] );
                else if( generics[i] != null )
                    missing.add( generics[i] );
            }

            for( int i = Math.min( generic.length, generics.length ), n = generics.length; i<n; i++ ){
                if( generics[i] != null )
                    missing.add( generics[i] );
            }
        }

        return new NesCInterface( declarations, bindings, raw, indices, missing.toArray( new GenericType[ missing.size() ] ), map );
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append( "interface " );
        builder.append( getName() );
        resolveGenerics();
        if( generics.length > 0 ){
            builder.append( "<" );
            for( int i = 0, n = generics.length; i<n; i++ ){
                if( i > 0 )
                    builder.append( ", " );
                builder.append( generics[i] == null ? "null" : generics[i].getName() );
            }
            builder.append( ">" );
        }
        builder.append( "{" );
        resolveFields();
        for( int i = 0, n = fields.length; i<n; i++ ){
            if( i > 0 )
                builder.append( ", " );
            builder.append( fields[i] );
        }
        builder.append( "}" );
        return builder.toString();
    }
}
