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
package tinyos.yeti.nesc12.parser.ast.util;

import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.Type.Initializer;
import tinyos.yeti.nesc12.parser.ast.elements.types.ArrayType;
import tinyos.yeti.nesc12.parser.ast.elements.types.BaseType;
import tinyos.yeti.nesc12.parser.ast.elements.types.DataObjectType;
import tinyos.yeti.nesc12.parser.ast.elements.types.ArrayType.Size;
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionMap;
import tinyos.yeti.nesc12.parser.ast.elements.types.conversion.ConversionTable;
import tinyos.yeti.nesc12.parser.ast.elements.values.ArrayValue;
import tinyos.yeti.nesc12.parser.ast.elements.values.DataObject;
import tinyos.yeti.nesc12.parser.ast.elements.values.StringValue;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;

public class InitializerCounter {
    /*
     * Example:
     * struct{ int x; int y[4]; int y; } a = { 1, .y[3] = 5, { 2 }};
     * struct{ int x; struct{ int a; int b;} y; int z; } c = { .y{ 1, 2 } };
     * 
     * {2} should give a warning 
     * 
     *

    public static void main( String[] args ) throws Exception{
        // String test = "int test = 4;";
        // String test = "struct{ int x; struct{ int a; int b;} y; int z; } test = { 7, .y{ 1, 2 }, 3 };";
        // String test = "struct{ int x; int y[4]; int z; } test = { 1, .y[2] = 5, 4, 3 };";
        // String test = "struct{ int x; int y[4]; int z; } test = { 1, {5, [2]=4}, 3 };";
        // String test = "struct{ int x; int y[4]; int z; } test = { 1, {5, 4}, 3, 2, [5]=6 };";
        // String test = "int test = 1.25;";
        // String test = "int *test(int) = 0;";
         String test = "int *test[4] = { 0 };";

        String code = "int main(){ " + test + "}" ;
        PurgingReader purging = new PurgingReader( new StringReader( code ) );

        Preprocessor pre = new Preprocessor();
        final PreprocessorReader reader = pre.open( "file.c", purging );

        Lexer lexer = new NesCLexer( reader );
        parser parser = new parser( lexer );

        lexer.setScopeStack( parser.scopes() );
        Symbol result = parser.parse();

        ASTNode node = (ASTNode)result.value;

        final ASTMessageHandler handler = new ASTMessageHandler(){
            public void report( Severity severity, String message, ASTNode... nodes ) {
                if( nodes.length == 0 )
                    System.out.println( severity + ": " + message );
                else{
                    Range range = nodes[0].getRange();
                    RangeDescription description = reader.range( range.getLeft(), range.getRight() );

                    System.out.println( severity + ": " + message + " [" + description.getLeft( 0 ) + "/" + description.getRight( 0 ) + "]" );
                }
            }
        };

        node.accept( new ASTVisitorAdapter(){
            @Override
            public boolean visit( Declaration node ){
                AnalyzeStack stack = new AnalyzeStack( handler, null );
                node.resolve( stack );
                SimpleField field = stack.getField( new SimpleName( new Identifier( "test" ) ));
                if( field == null )
                    System.out.println( "failed" );
                else
                    System.out.println( field.getType() + " " + field.getName() + " = " + field.getInitialValue() );
                return false;
            }
        });

        /*
        AnalyzeStack stack = new AnalyzeStack( handler );

        Type type = DataObjectType.struct( new SimpleField[]{
                new SimpleField( BaseType.S_INT, new Identifier( "x" )),
                new SimpleField( DataObjectType.struct( new SimpleField[]{
                    new SimpleField( BaseType.S_INT, new Identifier( "a" ) ),
                    new SimpleField( BaseType.S_INT, new Identifier( "b" ) )
                    }), new Identifier( "y" )),
                new SimpleField( BaseType.S_INT, new Identifier( "z" ))
        });

        InitializerCounter counter = new InitializerCounter( stack, type );

        counter.open();
        counter.put( new IntegerValue( BaseType.S_INT, 1 ), null );
        counter.put( new IntegerValue( BaseType.S_INT, 2 ), null );
        counter.put( new IntegerValue( BaseType.S_INT, 3 ), null );
        counter.put( new IntegerValue( BaseType.S_INT, 4 ), null );
        counter.close();

        Value result = counter.result();
        System.out.println( result );*
    } // */

    private AnalyzeStack stack;
    private Type type;
    private Level level;
    private boolean staticField;
    
    public InitializerCounter( AnalyzeStack stack, boolean staticField, Type type ){
        this.stack = stack;
        this.type = type;
        this.staticField = staticField;
    }

    /**
     * Starts with a new object
     * @param location where to report messages
     */
    public void open( ASTNode location ){
        if( level == null )
            level = new Level( type, false, location );
        else
            level.open( location );
    }

    /**
     * Ends the current object
     */
    public void close(){
        if( level != null )
            level.close();
    }

    /**
     * Called for a designation like [12]
     * @param index the new index in the current array
     * @param location where the index is specified
     */
    public void index( int index, ASTNode location ){
        if( level == null )
            level = new Level( type, true, location );
        level.index( index, location );
    }

    /**
     * Called for a designation like [1 ... 4]
     * @param begin the begin of the range
     * @param end the end of the range
     * @param beginNode the node which is the source of <code>begin</code>
     * @param endNode the node which is the source of <code>end</code>
     * @param location the location of the whole designation
     */
    public void range( int begin, int end, ASTNode beginNode, ASTNode endNode, ASTNode location ){
        if( level == null )
            level = new Level( type, true, location );
        level.range( begin, end, beginNode, endNode );
    }

    /**
     * Called for a designation like .field
     * @param name the name of the new field of the current data object
     * @param source the node which represents <code>name</code>
     * @return the field that was selected, might be <code>null</code>
     */
    public Field field( Name name, ASTNode source ){
        if( level == null )
            level = new Level( type, true, source );
        return level.field( name );
    }

    /**
     * Called to finish the current production
     * @param type the type of the value
     * @param value a constant value, can be <code>null</code>
     * @param source the node which represents <code>value</code>
     */
    public void put( Type type, Value value, ASTNode source ){
        if( level == null )
            level = new Level( this.type, true, source );
        level.put( type, value, source );
    }

    /**
     * Gets the result of this counter.
     * @param location location for errors concerning the whole initializer
     * @return the result, might be <code>null</code>
     */
    public Value result( ASTNode location ){
        if( level == null )
            return null;

        Value result = null;
        if( level.value != null )
            result = level.value.getResult();

        if( level.scalar ){
            if( !scalar( level.type ) && !level.isScalarInitializer() ){
                stack.error( "scalar initializer for non-scalar type", location );
            }
        }
        else{
            if( scalar( level.type ) && !level.isScalarInitializer() ){
                stack.warning( "braces around scalar initializer", location );
            }
        }

        return result;
    }

    /**
     * Checks whether the assignment <code>variable x = value</code> is legal or not.
     * @param variable the type of the variable
     * @param initializer the type of <code>value</code>
     * @param value the value to assign
     * @param location the location used to report errors
     * @param stack to report errors or locations
     * @return <code>true</code> if the assignment is legal, <code>false</code>
     */
    public static boolean checkScalarForNonScalar( Type variable, Type initializer, Value value, ASTNode location, AnalyzeStack stack ){
    	if( scalar( initializer ) && !scalar( variable )){
    		if( checkScalarForNonScalar( variable, value, location, stack ) )
    			return true;
    		
    		stack.error( "scalar initializer for non-scalar type", location );
    		return false;
    	}
    	
    	ConversionTable.instance().check( initializer, variable, ConversionMap.assignment( stack, location, value ) );
    	return true;
    }
    
    /**
     * Checks whether scalar <code>value</code> can be the correct initialization
     * for non scalar <code>type</code>.
     * @param type the type to initialize
     * @param value the initialize value
     * @param location location to report errors
     * @param stack to report errors or warnings
     * @return <code>true</code> if this is possible
     */
    public static boolean checkScalarForNonScalar( Type type, Value value, ASTNode location, AnalyzeStack stack ){
        // handle string / char arrays
        if( type.asArrayType() != null ){
            BaseType base = type.asArrayType().getRawType().asBase();
            if( base != null && base.isIntegerType() ){
                if( value instanceof StringValue ){
                    // that should work
                    StringValue string = (StringValue)value;
                    if( !string.isCharacter() ){

                        int length = -1;
                        ArrayType.Size size = type.asArrayType().getSize();

                        if( size == ArrayType.Size.SPECIFIED ){
                            length = type.asArrayType().getLength();
                        }

                        if( length >= 0 ){
                            if( string.getStringLength() > length ){
                                stack.warning( "initializer-string for array is too long", location );
                            }
                        }

                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Gets the number of elements of an initializer that this type needs.
     * @param type some type
     * @return the number of elements
     */
    private static int length( Type type ){
        if( type == null )
            return 0;

        return type.getInitializerLength();
    }

    /**
     * Tells whether <code>type</code> is a scalar, meaning that it should
     * not be initialized using curly brackets.
     * @param type the type to check, might be <code>null</code>
     * @return whether <code>type</code> is scalar
     */
    private static boolean scalar( Type type ){
        if( type == null )
            return false;

        if( type.asArrayType() != null )
            return false;

        if( type.asDataObjectType() != null )
            return false;

        return length( type ) == 1;
    }

    private Type get( Type type, int index, Initializer initializer, ASTNode location, boolean reportErrors ){
        if( type == null )
            return null;

        if( index < 0 ){
            if( reportErrors && stack.isReportErrors() ){
                stack.error( "array index is smaller than 0 in initializer", location );
            }
            return null;
        }
        else{
            Type result = type.getInitializerType( index, initializer );
            if( reportErrors && result == null ){ 
                if( stack.isReportErrors() && index >= type.getInitializerLength() ){
                    stack.warning( "too many elements in initializer", location );
                }
            }
            return result;
        }
    }

    public Type currentType(){
        if( level == null )
            return type;

        return level.type;
    }

    private ValueFactory create( Type type, boolean inDataObject ){
        if( type == null )
            return new SimpleFactory( null );

        if( type.asArrayType() != null ){
            return new ArrayFactory( type.asArrayType(), inDataObject );
        }
        if( type.asDataObjectType() != null ){
            return new DataObjectFactory( type.asDataObjectType() );
        }
        return new SimpleFactory( type );
    }

    private class Level{
        private Level parent;
        private Level child;

        /** the type to which this level will be assigned */
        private Type type;

        /** the index of the element that is currently in work */
        private int index;

        /** how many ranges are stored (maximal) */
        private int rangeDepth = 0;

        /** the offset of the range to fill */
        private int[] rangeOffset = new int[5];

        /** distance between elements in the range */
        private int[] rangeDistance = new int[5];

        /** the length of the range to fill */
        private int[] rangeLength = new int[5];

        /** 
         * The type the designator currently uses.
         * For example after a .x, fieldType would be the type of x. It is
         * <code>null</code> if no designator has yet been used.
         */
        private Type fieldType;

        /**
         * Whether a chain has started. If <code>true</code> then {@link #fieldType}
         * should be used in {@link #index(int, ASTNode)} and in
         * {@link #field(Name)} as starting point, otherwise {@link #type}
         * should be used.
         */
        private boolean chain = false;

        private ValueFactory value;

        /**
         * The index of the field that was accessed by the last
         * call to {@link #open(ASTNode)}
         */
        private int openFieldIndex = -1;

        /** whether this top level is scalar */
        private boolean scalar;

        public Level( Type type, boolean scalar, ASTNode location ){
            this.type = type;
            this.scalar = scalar;
            this.value = create( type, false );

            if( stack.isReportErrors() && type.isIncomplete() ){
                stack.error( "initializer for incomplete type", location );
            }
        }

        public Level( Level parent, ValueFactory value, Type type, ASTNode location ){
            this.parent = parent;
            this.parent.child = this;
            this.type = type;
            this.value = value;
            if( value != null ){
                if( stack.isReportErrors() ){
                    value.checkAssignValid( location );
                }
            }
        }

        public void put( Type valueType, Value value, ASTNode source ){
            Type field = get( type, index, Initializer.LEAF, source, true );

            boolean keepOpenFieldIndex = false;

            if( !tryScalarForNonScalar( valueType, value, source )){
                if( stack.isReportErrors() && valueType != null ){
                    if( field != null && valueType != null ){
                        ConversionTable.instance().check(
                                valueType,
                                field, 
                                ConversionMap.assignment( stack, source, value ) );
                    }
                }
                if( stack.isReportErrors() && this.value != null ){
                    if( openFieldIndex != -1 ){
                        ValueFactory factory = this.value.getSubFactory( index, false );
                        if( factory != null ){
                            if( this.value.indexOf( factory ) == openFieldIndex ){
                                // reusing the last factory... that should not
                                // happen if the factory were used up us suggested
                                // by openFieldIndex
                                keepOpenFieldIndex = true;
                                stack.warning( "too many elements in initializer", source );
                            }
                        }
                    }

                    this.value.checkAssignValid( index, source );
                }

                if( this.value != null ){
                    if( field != null && value != null )
                        value = field.cast( value );

                    // this.value.put( index, value );
                    put( index, value, false );
                }

                index++;
            }

            if( !keepOpenFieldIndex )
                openFieldIndex = -1;

            chain = false;
            clearRanges();
        }

        private boolean tryScalarForNonScalar( Type valueType, Value value, ASTNode source ){
            Type field = get( type, index, Initializer.PARENT, source, false );
            if( field == null )
                return false;

            if( checkScalarForNonScalar( field, value, source, stack ) ){
                put( index, value, true );

                index += field.getInitializerLength();

                return true;
            }

            return false;
        }

        /**
         * Tells whether the value of this level was initialized using a
         * scalar expression.
         * @return <code>true</code> if a scalar expression was used
         */
        public boolean isScalarInitializer(){
            if( value == null )
                return false;

            return value.isScalarInitialized();
        }

        /**
         * Stores <code>value</code> at <code>index</code>. This method
         * also respect any ranges that were defined in this {@link Level} or
         * the levels above.
         * @param index the location where <code>value</code> should be put
         * in respect to the {@link #value} of this level.
         * @param value the value to store
         * @param scalarForNonScalar if set, then <code>value</code> is scalar
         * and initializes a non-scalar field
         */
        private void put( int index, Value value, boolean scalarForNonScalar ){
            if( parent != null ){
                parent.put( index, value, scalarForNonScalar );
            }
            else {
                putChild( index, 0, this.value, value, scalarForNonScalar );
            }
        }

        /**
         * Stores <code>value</code> in <code>factory</code>, maybe at more than
         * one location. This method respects any range that was defined in
         * this or the {@link #child}.
         * @param index location to store <code>value</code>, only used if there
         * are no ranges in the current level.
         * @param range the current range
         * @param factory the factory in which <code>value</code> will be stored
         * @param value the value to store
         * @param scalarForNonScalar if set, then <code>value</code> is scalar
         * and initializes a non-scalar field
         */
        private void putChild( int index, int range, ValueFactory factory, Value value, boolean scalarForNonScalar ){
            if( child == null ){
                putNeighbour( 0, index, range, factory, value, scalarForNonScalar );
            }
            else if( range == rangeDepth ){
                child.putChild( index, 0, factory, value, scalarForNonScalar );
            }
            else{
                int offset = rangeOffset[ range ];
                int distance = rangeDistance[ range ];
                int length = rangeLength[ range ];
                range++;
                for( int i = 0; i<length; i++ ){
                    ValueFactory subFactory = factory.getSubFactory( offset, false );
                    if( subFactory != null ){
                        putChild( index, range, subFactory, value, scalarForNonScalar );
                        offset += distance;
                    }
                }
            }
        }

        /**
         * Stores <code>value</code> in <code>factory</code>. This method
         * respects any range declared in this {@link Level}.
         * @param offset where to put <code>value</code> assuming there are ranges.
         * @param index where to put <code>value</code> assuming there are not ranges.
         * @param range the next range to analyze, if too big then <code>value</code>
         * has to be stored now.
         * @param factory the factory into which <code>value</code> will be stored
         * @param value the value to store
         * @param scalarForNonScalar if set, then <code>value</code> is scalar
         * and initializes a non-scalar field
         */
        private void putNeighbour( int offset, int index, int range, ValueFactory factory, Value value, boolean scalarForNonScalar ){
            if( range == rangeDepth ){
                if( range == 0 )
                    factory.put( index, value, scalarForNonScalar );
                else
                    factory.put( offset, value, scalarForNonScalar );
            }
            else{
                offset += rangeOffset[ range ];
                int distance = rangeDistance[ range ];
                int length = rangeLength[ range ];
                range++;
                for( int i = 0; i < length; i++ ){
                    putNeighbour( offset, index, range, factory, value, scalarForNonScalar );
                    offset += distance;
                }
            }
        }

        public void open( ASTNode location ){
            Type current;
            if( fieldType == null || !chain ){
                current = get( type, index, Initializer.CHILD, location, true );
            }
            else{
                current = fieldType;
            }
            int lastOpenFieldIndex = openFieldIndex;
            openFieldIndex = -1;
            ValueFactory factory = null;
            if( this.value != null ){
                factory = value.getSubFactory( index, false );
                if( factory != null ){
                    int nextOpenFieldIndex = value.indexOf( factory );
                    if( nextOpenFieldIndex == lastOpenFieldIndex ){
                        // the same factory returned twice means that
                        // an automatically expanding factory is reached
                        factory = null;
                        if( stack.isReportErrors() ){
                            stack.warning( "too many elements in initializer", location );
                        }
                    }

                    openFieldIndex = nextOpenFieldIndex;
                }
            }

            level = new Level( this, factory, current, location );
            int length = length( current );
            index += length;
            if( stack.isReportErrors() && scalar( current ) ){
                stack.warning( "braces around scalar initializer", location );
            }
        }

        public void close(){
            if( parent != null ){
                parent.child = null;
                level = parent;
            }
        }

        private void pushRange( int offset, int distance, int length ){
            if( rangeDepth >= rangeLength.length ){
                int[] temp = new int[ rangeDepth*2+1 ];
                System.arraycopy( rangeLength, 0, temp, 0, rangeLength.length );
                rangeLength = temp;

                temp = new int[ rangeDepth*2+1 ];
                System.arraycopy( rangeOffset, 0, temp, 0, rangeLength.length );
                rangeOffset = temp;

                temp = new int[ rangeDepth*2+1 ];
                System.arraycopy( rangeDistance, 0, temp, 0, rangeLength.length );
                rangeDistance = temp; 
            }
            rangeLength[ rangeDepth ] = length;
            rangeOffset[ rangeDepth ] = offset;
            rangeDistance[ rangeDepth ] = distance;
            rangeDepth++;
        }

        private void clearRanges(){
            rangeDepth = 0;
        }

        public void index( int index, ASTNode location ){
            Type current = fieldType;
            int recover = this.index;
            if( !chain || current == null ){
                chain = true;
                this.index = 0;
                current = type;
            }

            ArrayType array = current.asArrayType();
            if( array != null ){
                if( stack.isReportErrors() ){
                    if( index < 0 ){
                        stack.error( "array index out of bounds (too small)", location );
                    }
                    if( array.getSize() == ArrayType.Size.SPECIFIED && index >= array.getLength() ){
                        stack.error( "array index out of bounds (too big)", location );
                    }
                }

                fieldType = array.getRawType();
                if( fieldType != null ){
                    int length = length( fieldType );
                    this.index += length * index;
                }
                openFieldIndex = -1;
                pushRange( this.index, 0, 1 );
            }
            else{
                this.index = recover;
                if( stack.isReportErrors() ){
                    stack.error( "array index in non-array initializer", location );
                }
            }
        }

        public void range( int begin, int end, ASTNode beginNode, ASTNode endNode ){
            Type current = fieldType;
            int recover = this.index;
            if( !chain || current == null ){
                chain = true;
                this.index = 0;
                current = type;
            }

            if( stack.isReportErrors() ){
                if( end < begin ){
                    stack.error( "empty range", beginNode, endNode );
                }
            }

            ArrayType array = current.asArrayType();
            if( array != null ){
                if( stack.isReportErrors() ){
                    if( begin < 0 ){
                        stack.error( "array index out of bounds (too small)", beginNode );
                    }
                    if( end < 0 ){
                        stack.error( "array index out of bounds (too small)", endNode );
                    }

                    if( array.getSize() == ArrayType.Size.SPECIFIED ){
                        if( begin >= array.getLength() ){
                            stack.error( "array index out of bounds (too big)", beginNode );
                        }
                        if( end >= array.getLength() ){
                            stack.error( "array index out of bounds (too big)", endNode );
                        }
                    }
                }

                fieldType = array.getRawType();
                int distance = 1;
                if( fieldType != null ){
                    distance = length( fieldType );
                    this.index += distance * begin;
                }
                openFieldIndex = -1;
                pushRange( index, distance, end - begin + 1 );
            }
            else{
                this.index = recover;
                if( stack.isReportErrors() ){
                    stack.error( "array index in non-array initializer", beginNode, endNode );
                }
            }
        }

        public Field field( Name name ){
            Field field = null;

            Type current = fieldType;
            int recover = this.index;

            if( !chain || current == null ){
                chain = true;
                this.index = 0;
                current = type;
            }

            DataObjectType data = current.asDataObjectType();
            if( data != null ){
                int length = 0;
                if( data.isStruct() || data.isAttribute() ){
                    boolean found = false;
                    for( int i = 0, n = data.getFieldCount(); !found && i<n; i++ ){
                        field = data.getField( i );

                        if( name.equals( field.getName() ) ){
                            found = true;
                            openFieldIndex = -1;
                            fieldType = field.getType();
                        }
                        else{
                            length += length( field.getType() );
                        }
                    }
                    if( !found ){
                        this.index = recover;
                        if( stack.isReportErrors() ){
                            stack.error( "unknown field '" + name + "' specified in initializer", name.getRange() );
                        }
                    }
                }
                else{
                    field = data.getField( name );
                    if( field != null ){
                        fieldType = field.getType();
                        openFieldIndex = -1;
                    }
                    else{
                        this.index = recover;
                        if( stack.isReportErrors() ){                            
                            stack.error( "unknown field '" + name + "' specified in initializer", name.getRange() );
                        }
                    }
                }

                this.index += length;
                pushRange( index, 0, 1 );
            }
            else{
                this.index = recover;
                if( stack.isReportErrors() ){
                    stack.error( "field name not in record or union initializer", name.getRange() );
                }
            }

            return field;
        }
    }


    private abstract class ValueFactory{
        public abstract Type getType();
        public abstract void put( int index, Value value, boolean scalarForNonScalar );
        public abstract ValueFactory getSubFactory( int index, boolean recursive );
        public abstract int indexOf( ValueFactory factory );
        public abstract Value getResult();
        public abstract void checkAssignValid( ASTNode location );
        public abstract void checkAssignValid( int index, ASTNode location );

        public abstract boolean isLeaf();
        public abstract boolean isScalarInitialized();
    }

    private class ArrayFactory extends ValueFactory{
        private ArrayType type;
        private List<ValueFactory> values = new ArrayList<ValueFactory>();
        private int rawLength;

        private boolean inDataObject;
        private Value scalarValue = null;

        public ArrayFactory( ArrayType type, boolean inDataObject ){
            this.type = type;
            this.inDataObject = inDataObject;

            rawLength = length( type.getRawType() );

            if( type.getSize() == Size.SPECIFIED ){
                for( int i = 0, n = type.getLength(); i<n; i++ )
                    values.add( create( type.getRawType(), inDataObject ));
            }
        }

        @Override
        public Type getType(){
            return type;
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        @Override
        public void checkAssignValid( ASTNode location ){
            if( inDataObject ){
                if( type != null && type.getSize() == ArrayType.Size.VARIABLE ){
                    stack.error( "initializer for variable array type which is part of a struct", location );
                }
            }
        }

        @Override
        public void put( int index, Value value, boolean scalarForNonScalar ) {
            if( index < 0 )
                return;

            int rawIndex = index / rawLength;
            if( type.getSize() == Size.SPECIFIED ){
                if( rawIndex >= type.getLength() )
                    return;
            }

            while( values.size() <= rawIndex )
                values.add( create( type.getRawType(), inDataObject ) );

            ValueFactory child = values.get( rawIndex );
            if( child.isLeaf() && scalarForNonScalar ){
                scalarValue = value;
            }
            else{
                child.put( index - rawIndex * rawLength, value, scalarForNonScalar );
            }
        }

        @Override
        public void checkAssignValid( int index, ASTNode location ){
            checkAssignValid( location );

            if( index < 0 )
                return;

            int rawIndex = index / rawLength;
            if( type.getSize() == Size.SPECIFIED ){
                if( rawIndex >= type.getLength() )
                    return;
            }

            while( values.size() <= rawIndex )
                values.add( create( type.getRawType(), inDataObject ) );

            values.get( rawIndex ).checkAssignValid( index - rawIndex * rawLength, location );
        }

        @Override
        public ValueFactory getSubFactory( int index, boolean recursive ) {
            if( index < 0 )
                return null;

            int rawIndex = index / rawLength;
            if( type.getSize() == Size.SPECIFIED ){
                if( rawIndex >= type.getLength() )
                    return null;
            }

            while( values.size() <= rawIndex )
                values.add( create( type.getRawType(), inDataObject ) );

            if( recursive )
                return values.get( rawIndex ).getSubFactory( index - rawIndex * rawLength, true );
            else
                return values.get( rawIndex );
        }

        @Override
        public int indexOf(ValueFactory factory) {
            return values.indexOf( factory );
        }

        @Override
        public boolean isScalarInitialized() {
            return scalarValue != null;
        }

        @Override
        public Value getResult() {
            if( scalarValue != null )
                return scalarValue;

            Value[] result = new Value[ values.size() ];
            for( int i = 0, n = result.length; i<n; i++ )
                result[i] = values.get( i ).getResult();
            return new ArrayValue( type, result );
        }
    }

    private class DataObjectFactory extends ValueFactory{
        private DataObjectType type;

        private ValueFactory[] fields;
        private int[] lengths;

        private Value scalarValue = null;

        public DataObjectFactory( DataObjectType type ){
            this.type = type;
            fields = new ValueFactory[ type.getFieldCount() ];
            lengths = new int[ fields.length ];

            for( int i = 0, n = type.getFieldCount(); i<n; i++ ){
                fields[i] = create( type.getField( i ).getType(), true );
                lengths[i] = length( type.getField( i ).getType() );
            }
        }

        @Override
        public Type getType(){
            return type;
        }

        @Override
        public int indexOf(ValueFactory factory) {
            for( int i = 0, n = fields.length; i<n; i++ ){
                if( fields[ i ] == factory )
                    return i;
            }

            return -1;
        }

        @Override
        public void checkAssignValid( ASTNode location ){
            // ok
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        @Override
        public void put( int index, Value value, boolean scalarForNonScalar ) {
            if( index < 0 )
                return;

            for( int i = 0, n = lengths.length; i<n; i++ ){
                if( index < lengths[i] ){
                    if( fields[i].isLeaf() && scalarForNonScalar ){
                        scalarValue = value;
                    }
                    else{
                        fields[i].put( index, value, scalarForNonScalar );
                    }
                    return;
                }
                else{
                    index -= lengths[i];
                }
            }
        }

        @Override
        public void checkAssignValid( int index, ASTNode location ){
            if( index < 0 )
                return;

            for( int i = 0, n = lengths.length; i<n; i++ ){
                if( index < lengths[i] ){
                    fields[i].checkAssignValid( index, location );
                    return;
                }
                else{
                    if( i+1 < n ){
                        index -= lengths[i];
                    }
                }
            }

            // check last element, might be an array
            if( fields.length > 0 ){
                ValueFactory last = fields[ fields.length-1 ];
                Type type = last.getType();
                if( type != null ){
                    ArrayType array = type.asArrayType();
                    if( array != null ){
                        if( array.getSize() == ArrayType.Size.VARIABLE || array.getSize() == ArrayType.Size.INCOMPLETE ){
                            last.checkAssignValid( index, location );
                        }
                    }
                }
            }
        }

        @Override
        public ValueFactory getSubFactory( int index, boolean recursive ) {
            if( index < 0 )
                return null;

            for( int i = 0, n = lengths.length; i<n; i++ ){
                if( index < lengths[i] ){
                    if( recursive )
                        return fields[i].getSubFactory( index, true );
                    else
                        return fields[i];
                }
                else{
                    if( i+1 < n ){
                        index -= lengths[i];
                    }
                }
            }

            // check last element, might be an array
            if( fields.length > 0 ){
                ValueFactory last = fields[ fields.length-1 ];
                Type type = last.getType();
                if( type != null ){
                    ArrayType array = type.asArrayType();
                    if( array != null ){
                        if( array.getSize() == ArrayType.Size.VARIABLE || array.getSize() == ArrayType.Size.INCOMPLETE ){
                            if( recursive )
                                return last.getSubFactory( index, true );
                            else
                                return last;
                        }
                    }
                }
            }

            return null;
        }

        @Override
        public boolean isScalarInitialized() {
            return scalarValue != null;
        }

        @Override
        public Value getResult() {
            if( scalarValue != null )
                return scalarValue;

            DataObject result = new DataObject( type );
            for( int i = 0, n = fields.length; i<n; i++ ){
                result.setValue( i, fields[i].getResult() );
            }
            return result;
        }
    }

    private class SimpleFactory extends ValueFactory{
        private Value value;
        private Type type;

        public SimpleFactory( Type type ){
            this.type = type;
        }

        @Override
        public Type getType(){
            return type;
        }

        @Override
        public void checkAssignValid( ASTNode location ){
            // ok
        }

        @Override
        public void checkAssignValid( int index, ASTNode location ){
            // ok
        }

        @Override
        public boolean isLeaf() {
            return true;
        }

        @Override
        public boolean isScalarInitialized() {
            return true;
        }

        @Override
        public void put( int index, Value value, boolean scalarForNonScalar ) {
            if( index == 0 )
                this.value = value;
        }

        @Override
        public Value getResult() {
            if( value == null && type != null && (level.parent != null || staticField) )
                return type.getStaticDefaultValue();

            return value;
        }

        @Override
        public ValueFactory getSubFactory( int index, boolean recursive ) {
            if( index == 0 )
                return this;
            else
                return null;
        }

        @Override
        public int indexOf(ValueFactory factory) {
            if( factory == this )
                return 0;
            else
                return 1;
        }
    }
}
