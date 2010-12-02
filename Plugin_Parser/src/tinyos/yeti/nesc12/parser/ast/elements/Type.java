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

import tinyos.yeti.nesc12.parser.ast.elements.types.ArrayType;
import tinyos.yeti.nesc12.parser.ast.elements.types.BaseType;
import tinyos.yeti.nesc12.parser.ast.elements.types.ConstType;
import tinyos.yeti.nesc12.parser.ast.elements.types.DataObjectType;
import tinyos.yeti.nesc12.parser.ast.elements.types.EnumType;
import tinyos.yeti.nesc12.parser.ast.elements.types.FunctionType;
import tinyos.yeti.nesc12.parser.ast.elements.types.GenericType;
import tinyos.yeti.nesc12.parser.ast.elements.types.PointerType;
import tinyos.yeti.nesc12.parser.ast.elements.types.TypedefType;

/**
 * Represents a type. A type specifies how to treat a block of bytes when the
 * programm is compiled.<br>
 * There are different types:
 * <ul>
 *  <li>base types: void, int, float, unsigned long int, ...</li>
 *  <li>pointers: int*, void*, ...</li>
 *  <li>arrays: int[], int[4], ...</li>
 *  <li>functions: int(int,int[])</li>
 *  <li>structs and unions: union{ int x; }</li>
 *  <li>or any of the above plus some modifiers like "static", "const", ...</li>
 * </ul>
 * 
 * @author Benjamin Sigg
 */
public interface Type extends Generic{        
    public enum Label{
        /** a label that should fit onto one line */
        SMALL,
        /** an extended description revealing some of the internals of a type */
        EXTENDED,
        /** a label that will be used as declaration in the source and thus should be valid syntax */
        DECLARATION
    }
    
    public enum Initializer{
        /** just the next child is asked, not recursive */
        CHILD,
        
        /** the parent of the leaf is searched */
        PARENT,
        
        /** the bottom most type is searched */
        LEAF
    }
    
    /**
     * Creates a type that is similar to this, but all generic types
     * have been replaced by the content of <code>generic</code>.
     * @param generic the replacements
     * @return a type that is similar to this but has replaced generic types
     */
    public Type replace( Map<GenericType, Type> generic );
    
    /**
     * Tries to find out the size of this type.
     * @return the size of this type in bytes or -1 if there are not enough
     * informations
     */
    public int sizeOf();

    /**
     * Whether this describes an incomplete type. The size of an incomplete
     * type is always unknown. Normally any type with name can be incomplete,
     * fields of incomplete types can't be used unless the type is resolved
     * later.
     * @return whether this is an incomplete type
     */
    public boolean isIncomplete();
    
    /**
     * Casts this type into a {@link BaseType}
     * @return the representation of this or <code>null</code>
     */
    public BaseType asBase();
    
    /**
     * Casts this type into a {@link FunctionType}
     * @return the representation of this or <code>null</code>
     */
    public FunctionType asFunctionType();
    
    /**
     * Casts this type into a {@link DataObjectType}
     * @return the representation of this or <code>null</code>
     */
    public DataObjectType asDataObjectType();

    /**
     * Casts this type into an {@link ArrayType}
     * @return the representation of this or <code>null</code>
     */
    public ArrayType asArrayType();

    /**
     * Casts this type into a {@link PointerType}
     * @return the representation of this or <code>null</code>
     */
    public PointerType asPointerType();

    /**
     * Casts this type into an {@link EnumType}
     * @return the representation of this or <code>null</code>
     */
    public EnumType asEnumType();
    
    /**
     * Casts this type into a {@link ConstType}
     * @return the representation of this or <code>null</code>
     */
    public ConstType asConstType();

    /**
     * Casts this type into a {@link GenericType}
     * @return the representation of this or <code>null</code>
     */
    public GenericType asGenericType();
    
    /**
     * Casts this type into a {@link TypedefType}.
     * @return the representation of this or <code>null</code>
     */
    public TypedefType asTypedefType();
    
    /**
     * Tries to cast a value to a value that matches this type.
     * @param value the value
     * @return the casted value or <code>null</code>
     */
    public Value cast( Value value );
    
    /**
     * Gets the number of elements this type needs in a complete initializer.
     * @return the number of elements, 0 if not specified for this type. 
     */
    public int getInitializerLength();
    
    /**
     * Whether this type can be the leaf type of an initializer or not.
     * @return <code>true</code> if this can be a leaf
     */
    public boolean isLeafInitializerType();
    
    /**
     * If this type is used in an initializer, gets the index'th element
     * that would be initialized. This is the same as calling
     * <code>getInitializerType( index, initializer, null );</code>.
     * @param index the location of the element
     * @param initializer whether to call {@link #getInitializerType(int, tinyos.yeti.nesc12.parser.ast.elements.Type.Initializer)}
     * again and how many times to call
     * @return the type of the element at that location or <code>null</code> if
     * the location is invalid 
     */
    public Type getInitializerType( int index, Initializer initializer );
    
    /**
     * If this type is used in an initializer, gets the index'th element
     * that would be initialized.
     * @param index the location of the element
     * @param initializer whether to call {@link #getInitializerType(int, tinyos.yeti.nesc12.parser.ast.elements.Type.Initializer)}
     * again and how many times to call
     * @param self what should be returned instead of <code>this</code>, if <code>null</code>
     * no replacement has to be made.
     * @return the type of the element at that location or <code>null</code> if
     * the location is invalid 
     */
    public Type getInitializerType( int index, Initializer initializer, TypedefType self );
    
    /**
     * Gets the default value for static variables.
     * @return the default value or <code>null</code> if no such value exists
     */
    public Value getStaticDefaultValue();
    
    /**
     * Gets a short but unique identifier for this type.
     * @return the identifier
     */
    /*
     * prefixes:
     * t : (unresolveable) typedef
     * p : pointer
     * g : generic
     * dx : data object
     * c : const
     * <BIG LETTER> : base
     * a : array
     * f : function
     */
    public String id( boolean typedefVisible );
    
    /**
     * Gets a short but unique identifier for this type. If a type
     * finds itself in <code>putin</code>, it will use the string instead of
     * <code>id</code>. Every type will insert itself into <code>putin</code>.
     * @param putin predefined ids
     * @param typedefVisible <code>true</code> if typedefs should be clearly
     * marked as beeing typedefs, or <code>false</code> if they should be hidden
     * @return the identifier
     */
    public String id( Map<Type, String> putin, boolean typedefVisible  );
    
    /**
     * Creates a good looking string that includes <code>name</code> and
     * this type.
     * @param name some name, might be <code>null</code>
     * @param label how the label should look like
     * @return a human readable combination of <code>this</code> and <code>name</code>
     */
    public String toLabel( String name, Label label );
}
