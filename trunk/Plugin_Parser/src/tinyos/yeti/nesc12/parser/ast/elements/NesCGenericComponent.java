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

import tinyos.yeti.nesc12.ep.BindingResolver;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
import tinyos.yeti.nesc12.ep.nodes.GenericComponentModelNode;
import tinyos.yeti.nesc12.ep.nodes.GenericTypeModelNode;
import tinyos.yeti.nesc12.parser.ast.ICancellationMonitor;
import tinyos.yeti.nesc12.parser.ast.elements.types.GenericType;

public abstract class NesCGenericComponent extends NesCComponent{
    private GenericComponentModelNode raw;
    private Binding[] parameters;
    
    public NesCGenericComponent( String type, GenericComponentModelNode raw, BindingResolver bindings ){
        super( type, raw, bindings );
        this.raw = raw;
    }
    
    public NesCGenericComponent( String type, GenericComponentModelNode raw, BindingResolver bindings, Map<GenericType, Type> replacements ){
        super( type, raw, bindings, replacements );
        this.raw = raw;
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
            case 0: return "Parameters";
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
            case 0: return getParameterCount();
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
            case 0: return getParameter( index );
            default: return null;
        }
    }
    
    
    /**
     * Tells whether the module is generic, that means whether the 
     * "generic" keyword is set.
     * @return <code>true</code> if the module is generic
     */
    public boolean isGeneric() {
        return raw.isGeneric();
    }

    private void resolveParameters(){
        if( parameters == null ){
            List<Binding> list = new ArrayList<Binding>();
            
            ICancellationMonitor monitor = bindings.getCancellationMonitor();
            
            for( ModelConnection connection : raw.listParameters( monitor ) ){
                ModelNode node = raw.getDeclarationResolver().resolve( connection, monitor.getProgressMonitor() );
                monitor.checkCancellation();
                
                if( node != null ){
                    if( node instanceof FieldModelNode ){
                        list.add( (FieldModelNode)node );
                    }
                    if( node instanceof GenericTypeModelNode ){
                        Binding result = ((GenericTypeModelNode)node).resolve();
                        if( result != null )
                            list.add( result );
                    }
                }
            }
            
            parameters = list.toArray( new Binding[ list.size() ] );
        }
    }
    
    /**
     * Gets the number of parameters this module has. The parameters define
     * types and constants that are accessible within the whole module.
     * @return the number of parameters
     */
    public int getParameterCount(){
        resolveParameters();
        return parameters.length;
    }
    
    /**
     * Gets the index'th parameter.
     * @param index the index of the parameter
     * @return either a {@link Field}, a {@link GenericType} or <code>null</code>
     */
    public Binding getParameter( int index ){
        resolveParameters();
        return parameters[index];
    }
    
}
