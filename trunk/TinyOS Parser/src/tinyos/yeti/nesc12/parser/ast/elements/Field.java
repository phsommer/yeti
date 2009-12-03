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

import java.util.Map;

import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.inspection.INesCField;
import tinyos.yeti.ep.parser.inspection.INesCFunction;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.ep.storage.GenericArrayFactory;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.nesc12.ep.nodes.FieldModelNode;
import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.parser.ast.elements.types.GenericType;
import tinyos.yeti.preprocessor.RangeDescription;

public interface Field extends Binding, INesCField, INesCFunction{
	public static final IGenericFactory<Field[]> ARRAY_FACTORY = new GenericArrayFactory<Field>(){
		@Override
		public Field[] create(int size) {
			return new Field[ size ];
		}
	};
	
    /**
     * If this is also a node, than this method returns the node
     * that represents this. Otherwise this method returns <code>null</code>.
     * @return the node which represents this field or <code>null</code>
     */
    public FieldModelNode asNode();
    
    /**
     * Returns a simple version of this field that contains only the information
     * needed for a type. The method {@link #asNode()} would return
     * <code>null</code> if called on the result of this method.
     * @return the minimal information needed for a field
     */
    public SimpleField asSimple();
    
    /**
     * Gets the name of this field
     * @return the name, might be <code>null</code>
     */
    public Name getName();

    public void setType( Type type );

    public Type getType();
    
    public void setArgumentNames( Name[] names );
    
    /**
     * If this object is a function, then it might have some named arguments.
     * Note that these arguments are not always available.
     * Missing arguments do not imply that this is not a function. The resulting array,
     * if not <code>null</code>, may contain <code>null</code> entries for arguments
     * whose name could not be determined. The length of the result, if not <code>null</code>,
     * is equal to the number of arguments that this function has.
     * @return the names of the arguments or <code>null</code>
     */
    public Name[] getArgumentNames();

    /**
     * Sets the indices of this field.
     * @param fields the indices, can be <code>null</code>
     * @see #getIndices()
     */
    public void setIndices( Field[] fields );
    
    /**
     * If this object is a function in a module and derived from an interface,
     * then this object might have indices. Indices are written in rectangular 
     * brackets like "int Interface.work[int id]( ... )" where "id" would be
     * a field.
     * @return the list of indices
     */
    public Field[] getIndices();
    
    /**
     * Tries to create a string that resembles the declaration of this field. Results
     * might be things like "static int x" or "int main( int )".
     * @param name the name to use for the declaration, can be <code>null</code> 
     * if the name of <code>this</code> should be used
     * @return the declaration, can be <code>null</code>
     */
    public String getDeclaration( String name );
    
    public void resolveNameRanges();
    
    /**
     * Gets the range either of the name, if not present of the type, if not
     * present of anything near the field.
     * @return the range
     */
    public RangeDescription getRange();
    
    /**
     * Gets the path to the {@link IASTModelNode} that represents this field.
     * @return the path or <code>null</code> if the node is unknown or does
     * not exist.
     */
    public ASTModelPath getPath();
    
    /**
     * Checks the type of this field and creates a field where generic
     * types are replaced.
     * @param indices a new set of indices
     * @param generic the map that tells how to replace which generic type
     * with which concrete type.
     * @return this or a new field
     */
    public Field replace( Field[] indices, Map<GenericType, Type> generic );
    
    public void setInitialValue( Value initialValue );
    
    public Value getInitialValue();

    public void setModifiers( Modifiers modifiers );

    public Modifiers getModifiers();
    
    /**
     * Gets a set of attributes which are associated with this field
     * @return the attributes, may be <code>null</code>
     */
    public ModelAttribute[] getAttributes();
    
    /**
     * Tells whether this field has all the properties required to represent
     * a real field.
     * @return <code>true</code> if this really is a field
     */
    public boolean isField();
    
    /**
     * Tells whether this field has all the properties required to represent
     * a real function.
     * @return <code>true</code> if this really is a function
     */
    public boolean isFunction();

}