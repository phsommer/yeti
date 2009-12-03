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
import java.util.List;

import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.NesCAttribute;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractListASTNode;

public class AttributeList extends AbstractListASTNode<Attribute> {
    public AttributeList(){
        super( "AttributeList" );
    }
    
    /**
     * Gets an array containing all the attributs defined in this list.
     * @return an array of attributes, can contain <code>null</code> for
     * erroneous attributes
     */
    public NesCAttribute[] resolveAttributes(){
        if( isResolved( "attributes" ))
            return resolved( "attributes" );
        
        NesCAttribute[] result = new NesCAttribute[ getChildrenCount() ];
        for( int i = 0, n = getChildrenCount(); i<n; i++ ){
            Attribute attribute = getNoError( i );
            if( attribute != null )
                result[i] = attribute.resolveAttribute();
        }
        
        return resolved( "attributes", result );
    }
    
    public ModelAttribute[] resolveModelAttributes(){
    	if( isResolved( "modelAttributes" ))
    		return resolved( "modelAttributes" );
    	
    	NesCAttribute[] base = resolveAttributes();
    	if( base == null || base.length == 0 )
    		return resolved( "modelAttributes", null );
    	
    	List<ModelAttribute> list = new ArrayList<ModelAttribute>();
    	for( NesCAttribute attribute : base ){
    		if( attribute != null ){
    			ModelAttribute model = attribute.toModelAttribute();
    			if( model != null ){
    				list.add( model );
    			}
    		}
    	}
    	
    	if( list.isEmpty() )
    		return resolved( "modelAttributes", null );
    	
    	return resolved( "modelAttributes", list.toArray( new ModelAttribute[ list.size() ] ) );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
    }
    
    public AttributeList( Attribute child ){
        this();
        add( child );
    }
    
    @Override
    public AttributeList add( Attribute node ) {
        super.add( node );
        return this;
    }
    
    @Override
    protected void checkChild( Attribute child ) throws ASTException {
        
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
