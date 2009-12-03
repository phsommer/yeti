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
package tinyos.yeti.nesc12.parser.ast.nodes;

import tinyos.yeti.ep.parser.standard.ASTModelNode;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.NoSuchASTFieldException;

/**
 * A fixed {@link ASTModelNode} has a fixed set of fields, all with different name.
 * Each field represents a child {@link ASTModelNode}. The number of fields
 * is equal to {@link ASTModelNode#getChildrenCount()}.
 * @author Benjamin Sigg
 */
public interface FixedASTNode extends ASTNode{
    /**
     * Gets the unique name of the index'th field.
     * @param index the location of the field
     * @return the name of the field
     */
    public String getFieldName( int index );
    
    /**
     * Gets the name of the field which contains <code>node</code>.
     * @param node the node to search
     * @return the name of the field or <code>null</code>
     */
    public String getFieldName( ASTNode node );
    
    /**
     * Gets the location of the field with name <code>name</code>.
     * @param name the name of the field
     * @return the location or -1 if <code>name</code> is invalid
     */
    public int getFieldLocation( String name );
    
    /**
     * Gets the location of the field which contains <code>node</code>
     * @param node the node to search
     * @return the location or -1
     */
    public int getFieldLocation( ASTNode node );
    
    /**
     * Gets the value of the index'th field.
     * @param index the location of the field
     * @return the value of the field
     */
    public ASTNode getField( int index );
    
    /**
     * Gets the value of the field <code>name</code>
     * @param name the name of the field
     * @return the value
     * @throws NoSuchASTFieldException if <code>name</code> is invalid
     */
    public ASTNode getField( String name ) throws NoSuchASTFieldException;
    
    /**
     * Sets the index'th field.
     * @param index the location of the field
     * @param node the new value of the field, can be <code>null</code>
     * @throws ASTException if <code>node</code> can't be part of a valid
     * syntax tree. The value <code>null</code> and the {@link ErrorASTNode}
     * will always be accepted. This is only a syntactic check, the resulting
     * code may still be invalid. 
     */
    public void setField( int index, ASTNode node ) throws ASTException;
    
    /**
     * Sets the field <code>name</code>.
     * @param name the name of the field
     * @param node the new value of the field, can be <code>null</code>
     * @throws ASTException if <code>node</code> can't be part of a valid
     * syntax tree. The value <code>null</code> and the {@link ErrorASTNode}
     * will always be accepted. This is only a syntactic check, the resulting
     * code may still be invalid.
     * @throws NoSuchASTFieldException if <code>name</code> is invalid 
     */
    public void setField( String name, ASTNode node ) throws ASTException;
}
