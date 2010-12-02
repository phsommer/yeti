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
import java.util.List;
import java.util.Map;

import tinyos.yeti.ep.parser.inspection.INesCField;
import tinyos.yeti.ep.parser.inspection.INesCFunction;
import tinyos.yeti.ep.parser.inspection.INesCInspector;
import tinyos.yeti.ep.parser.inspection.INesCModule;
import tinyos.yeti.ep.parser.inspection.INesCNode;
import tinyos.yeti.ep.parser.inspection.InspectionKey;
import tinyos.yeti.nesc12.ep.BindingResolver;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
import tinyos.yeti.nesc12.ep.nodes.ModuleModelNode;
import tinyos.yeti.nesc12.parser.ast.elements.types.GenericType;

/**
 * Represents a NesC-module: "generic module x(typedef p){ uses y} implementation{ command int z(){...}}"
 * @author Benjamin Sigg
 */
public class NesCModule extends NesCGenericComponent implements INesCModule{
    private ModuleModelNode raw;
    
    private Field[] fields;
    
    private InspectionKey<?>[] keys = {
    		FIELDS,
    		FUNCTIONS
    };
    
    public NesCModule( ModuleModelNode raw, BindingResolver bindings ){
        super( "Module", raw, bindings );
        this.raw = raw;
    }
    
    public NesCModule( ModuleModelNode raw, BindingResolver bindings, Map<GenericType, Type> replacements ){
        super( "Module", raw, bindings, replacements );
        this.raw = raw;
    }
    
    @Override
    public int getReferenceKindCount(){
    	return super.getReferenceKindCount() + keys.length;
    }
    
    @Override
    public InspectionKey<?> getReferenceKind( int index ){
    	if( index < keys.length )
    		return keys[ index ];
    	
    	return super.getReferenceKind( index - keys.length );
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public <K extends INesCNode> K[] getReferences( InspectionKey<K> key, INesCInspector inspector ){
    	if( FIELDS.equals( key )){
    		resolveFields();
    		List<INesCField> result = new ArrayList<INesCField>();
    		for( Field field : fields ){
    			if( field.isField() ){
    				result.add( field );
    			}
    		}
    		return (K[])result.toArray( new INesCField[ result.size() ] );
    	}
    	if( FUNCTIONS.equals( key )){
    		resolveFields();
    		List<INesCFunction> result = new ArrayList<INesCFunction>();
    		for( Field field : fields ){
    			if( field.isFunction() ){
    				result.add( field );
    			}
    		}
    		return (K[])result.toArray( new INesCFunction[ result.size() ] );
    	}
    	
    	return super.getReferences( key, inspector );
    }
    
    @Override
    public int getSegmentCount() {
        return super.getSegmentCount() + 1;
    }
    
    @Override
    public String getSegmentName( int segment ) {
        int count = super.getSegmentCount();
        if( segment < count )
            return super.getSegmentName( segment );
        else
            segment -= count;
        
        switch( segment ){
            case 0: return "Fields";
            default: return null;
        }
    }
    
    @Override
    public int getSegmentSize( int segment ) {
        int count = super.getSegmentCount();
        if( segment < count )
            return super.getSegmentSize( segment );
        else
            segment -= count;
        
        switch( segment ){
            case 0: return getFieldCount();
            default: return 0;
        }
    }
    
    @Override
    public Binding getSegmentChild( int segment, int index ) {
        int count = super.getSegmentCount();
        if( segment < count )
            return super.getSegmentChild( segment, index );
        else
            segment -= count;
        
        switch( segment ){
            case 0: return getField( index );
            default: return null;
        }
    }
    
    private void resolveFields(){
        if( fields == null ){
            List<Field> list = new ArrayList<Field>();
            for( ModelConnection connection : raw.listImplementation( bindings.getCancellationMonitor() ) ){
                ModelNode node = raw.getDeclarationResolver().resolve( connection, bindings.getCancellationMonitor().getProgressMonitor() );
                bindings.getCancellationMonitor().checkCancellation();
                
                if( node != null ){
                    if( node instanceof FieldModelNode ){
                        if( replacements != null )
                            list.add( ((Field)node).replace( null, replacements ));
                        else
                            list.add( (FieldModelNode)node );
                    }
                }
            }
            
            fields = list.toArray( new Field[ list.size() ] );
        }
    }
        
    /**
     * Gets the number of fields that were defined in the body of this module.
     * @return the number of fields
     */
    public int getFieldCount(){
        resolveFields();
        return fields.length;
    }
    
    /**
     * Gets the index'th field of the body of this module.
     * @param index the index of the field
     * @return the field or <code>null</code>
     */
    public Field getField( int index ){
        resolveFields();
        return fields[index];
    }
    

    @Override
    public NesCModule replace( Map<GenericType, Type> generics ){
        NesCComponent component = super.replace( generics );
        if( component instanceof NesCModule )
            return (NesCModule)component;
        
        return null;
    }
    
    @Override
    protected NesCModule createComponent( Map<GenericType, Type> generics ){
        return new NesCModule( raw, bindings, generics );
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append( "module[" );
        if( isGeneric() )
            builder.append( "generic=yes" );
        else
            builder.append( "generic=no" );
        
        builder.append( ", name=" );
        if( getName() != null )
            builder.append( getName() );
        
        if( getParameterCount() > 0 ){
            builder.append( ", parameters=(" );
            for( int i = 0, n = getParameterCount(); i<n; i++ ){
                if( i > 0 )
                    builder.append( ", " );
                builder.append( getParameter( i ) );
            }
            builder.append( ")" );
        }
        
        if( getUsesCount() > 0 ){
            builder.append( ", uses={" );
            for( int i = 0, n = getUsesCount(), m = getUsesFunctionCount(); i<n+m; i++ ){
                if( i > 0 )
                    builder.append( ", " );
                
                if( i < n )
                    builder.append( getUses( i ) );
                else
                    builder.append( getUsesFunction( i-n ) );
            }
            builder.append( "}" );
        }
        
        if( getProvidesCount() > 0 ){
            builder.append( ", provides={" );
            for( int i = 0, n = getProvidesCount(), m = getProvidesFunctionCount(); i<n+m; i++ ){
                if( i > 0 )
                    builder.append( ", " );
                if( i < n )
                    builder.append( getProvides( i ) );
                else
                    builder.append( getProvidesFunction( i-n ) );
            }
            builder.append( "}" );
        }
        
        if( getFieldCount() > 0 ){
            builder.append( ", fields={" );
            for( int i = 0, n = getFieldCount(); i<n; i++ ){
                if( i > 0 )
                    builder.append( ", " );
                builder.append( getField( i ) );
            }
            builder.append( "}" );
        }
        builder.append( "]" );
        return builder.toString();
    }
}
