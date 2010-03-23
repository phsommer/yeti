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
package tinyos.yeti.preprocessor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A {@link RangeDescription} describes the origin of characters in a given
 * range of the output of a preprocessor. Ranges are ordered in a directed graph,
 * the roots of this graph is what stands in the original file, the leafs what stands in
 * the file created by the preprocessor.<br>
 * Ranges are either rough or fine. There is always one level of rough, and one 
 * level of fine ranges in the graph. A fine level is an in depth description
 * of the rough level above. A rough level has links to its finer description
 * and to the next rough level. A fine level has links only to the next rough 
 * level. The root level is a rough level.<br>
 * In general the graph is only a tree.
 * @author Benjamin Sigg
 */
public class RangeDescription{
    /** indicates that this range contains a macro that was executed */
    public static final int CONTAINS_MACRO = 1;    
    
    /** indicates that this range is within a macro that was executed */
    public static final int IN_MACRO = 2;

    /** indicates that this range contains an included file */
    public static final int CONTAINS_INCLUDE = 4;
    
    /** indicates that this range is within an included file */
    public static final int IN_INCLUDE = 8;
    
    private List<Range> ranges = new ArrayList<Range>();
    
    private int left;
    private int right;
    
    private List<Range> roots = new ArrayList<Range>();
    
    /**
     * Creates a new range.
     * @param left the index of the first character in the output file or -1
     * @param right the index of the last character in the output file or -1
     */
    public RangeDescription( int left, int right ){
        this.left = left;
        this.right = right;
    }
    
    /**
     * Gets the index of the first character in the output file in which this
     * range is defined.
     * @return the begin of this range
     */
    public int getLeft(){
        return left;
    }
    
    /**
     * Gets the index of the last character in the output file in which this
     * range is defined.
     * @return the end of this range
     */
    public int getRight(){
        return right;
    }
    
    /**
     * Tries to find the range that describes the origin of the element
     * which is encompassed by this range.
     * @return the origin, may be <code>null</code>
     */
    public Range getSource(){
    	Range root = getFirstSplit();
    	Range result;
    	
    	class RVisitor implements RangeDescription.Visitor<Range>{
    		private int flag;
    		private boolean leaf;
    		
    		public RVisitor( int flag, boolean leaf ){
    			this.flag = flag;
    			this.leaf = leaf;
    		}
    		
    		public Range visit( RangeDescription.Range range, boolean rough ){
    			if( (range.sourceFlags() & flag) == 0 ){
    				if( range.file() != null ){
    					if( !leaf || (rough && (range.roughRanges() == null || range.roughRanges().length == 0 ))){
    						return range;
    					}
    				}
    			}
    			
    			return null;
    		}
    	};
    	
    	/*
    	 * Problematic are macros building a structure, assume "A B C" where
    	 * A, B and C are macros. One should select "A B C" as a whole and not
    	 * one of the macros alone. Just searching for a leaf would do exactly
    	 * that.
    	 * 
    	 * On the other hand just selecting the biggest range is not good either,
    	 * as an "include" statement might be longer than the element that
    	 * has to be selected.
    	 * 
    	 * Also leaves should be preferred over non-leaves as they are nearer
    	 * by the element to select. 
    	 */
    	
    	// does not contain includes nor macros
    	result = visit( root, new RVisitor( RangeDescription.CONTAINS_INCLUDE | RangeDescription.CONTAINS_MACRO, true ));
    	if( result != null )
    		return result;
    	
    	// does not contain includes
    	result = visit( root, new RVisitor( RangeDescription.CONTAINS_INCLUDE, true ));
    	if( result != null )
    		return result;
    	
    	result = visit( root, new RVisitor( 0, true ));
    	if( result != null )
    		return result;
    	
    	result = visit( root, new RVisitor( RangeDescription.CONTAINS_INCLUDE | RangeDescription.CONTAINS_MACRO, false ));
    	if( result != null )
    		return result;
    	
    	result = visit( root, new RVisitor( RangeDescription.CONTAINS_INCLUDE, false ));
    	if( result != null )
    		return result;
    	
    	result = visit( root, new RVisitor( 0, false ));
    	if( result != null )
    		return result;
    	
    	return result;
    }
    
    /**
     * Travels through the ranges of this description until the first
     * range is found that has not exactly one child. Meaning until either
     * the only leaf is found or the topmost range that splits up in subranges.
     * If there is no root, or there is more than one root, then <code>null</code>
     * is returned.
     * @return the topmost range with child-count not equal to 0 or <code>null</code>
     * if the root-count is not 1.
     */
    public Range getFirstSplit(){
    	if( getRootCount() != 1 )
    		return null;
    	
    	Range range = getRoot( 0 );
    	while( true ){
    		Range[] rough = range.roughRanges();
    		if( rough == null || rough.length != 1 )
    			return range;
    		range = rough[0];
    	}
    }
    
    /**
     * Marks <code>range</code> as beeing a root range.
     * @param range the new root range, should be created by this object
     */
    public void addRoot( Range range ){
    	roots.add( range );
    }
    
    /**
     * Adds a new input-range to this range.
     * @param left the index of the first character in the input file
     * @param right the index of the last character in the input file
     * @param line the line in which the first character is
     * @param sourceFlags how the range got included into the output file
     * @param file the file in which the new range lies
     * @param roughRanges the ranges of which the new range consists, must have all the <code>fineSubRanges</code> as children
     * @param fineRanges the ranges of which the new range consists, can be <code>null</code>
     * @return the new range
     */
    public Range addRough( int left, int right, int line, int sourceFlags, FileInfo file, Range[] roughRanges, Range[] fineRanges ){
    	return add( left, right, line, sourceFlags, file, roughRanges, fineRanges );
    }
    
    /**
     * Adds a new input-range to this range.
     * @param left the index of the first character in the input file
     * @param right the index of the last character in the input file
     * @param line the line in which the first character is
     * @param sourceFlags how the range got included into the output file
     * @param file the file in which the new range lies
     * @param roughRanges the ranges of which the new range consists, can be <code>null</code>
     * @return the new range
     */
    public Range addFine( int left, int right, int line, int sourceFlags, FileInfo file, Range[] roughRanges ){
    	return add( left, right, line, sourceFlags, file, roughRanges, null );
    }
    
    /**
     * Adds a new input-range to this range.
     * @param left the index of the first character in the input file
     * @param right the index of the last character in the input file
     * @param line the line in which the first character is
     * @param sourceFlags how the range got included into the output file
     * @param file the file in which the new range lies
     * @param roughRanges the ranges of which the new range consists, must have all the <code>fineSubRanges</code> as children
     * @param fineRanges the ranges of which the new range consists, can be <code>null</code>
     * @return the new range
     */
    public Range add( int left, int right, int line, int sourceFlags, FileInfo file, Range[] roughRanges, Range[] fineRanges ){
    	Range range = new RangeImpl( left, right, line, sourceFlags, file, roughRanges, fineRanges );
    	ranges.add( range );
    	return range;
    }
    
    public <T> T visit( Visitor<T> visitor ){
    	for( Range root : roots ){
    		T result = visit( visitor, root, 0 );
    		if( result != null )
    			return result;
    	}
    	return null;
    }
    
    /**
     * Visits all ranges of <code>root</code>, a value of <code>null</code>
     * means to visit all ranges of <code>this</code> {@link RangeDescription}.
     * @param <T> the result of the visitor
     * @param root the root to visit
     * @param visitor the visitor to use
     * @return the result of the visitor, may be <code>null</code>
     */
    public <T> T visit( Range root, Visitor<T> visitor ){
    	if( root == null ){
    		return visit( visitor );
    	}
    	else{
    		return visit( visitor, root, 0 );
    	}
    }
    
    private <T> T visit( Visitor<T> visitor, Range range, int depth ){
    	boolean rough = depth % 2 == 0;
    	T result = visitor.visit( range, rough );
    	if( result != null )
    		return result;
    	
    	Range[] children = rough ? range.fineRanges() : range.roughRanges();
    	if( children != null ){
    		depth++;
    		for( Range child : children ){
    			result = visit( visitor, child, depth );
    			if( result != null )
    				return result;
    		}
    	}
    	return null;
    }
    
    @Override
    public String toString(){
    	final StringBuilder builder = new StringBuilder();
    	for( Range root : roots ){
    		toString( root, builder, 0 );
    	}
    	return builder.toString();
    }
    
    private void toString( Range range, StringBuilder builder, int level ){
    	for( int i = 0; i < level; i++ )
    		builder.append( "  " );
    	
    	boolean rough = level % 2 == 0;
    	
    	builder.append( rough ? "R" : "F" );
    	builder.append( range );
    	builder.append( "\n" );
    	
    	Range[] children;
    	if( rough ){
    		children = range.fineRanges();
    	}
    	else{
    		children = range.roughRanges();
    	}
    	if( children != null ){
    		for( Range child : children ){
    			toString( child, builder, level+1 );
    		}
    	}
    }
    
    public int getRangeCount(){
    	return ranges.size();
    }
    
    public Range getRange( int index ){
    	return ranges.get( index );
    }
    
    public int getRootCount(){
    	return roots.size();
    }
    
    public Range getRoot( int index ){
    	return roots.get( index );
    }
    
    public Range[] getRoots(){
    	return roots.toArray( new Range[ roots.size() ]);
    }
    
    /*@Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append( "RangeDescription[" );
        builder.append( "left=" );
        builder.append( left );
        builder.append( ", right=" );
        builder.append( right );
        builder.append( ", ranges={" );
        for( int i = 0, n = size(); i<n; i++ ){
            if( i > 0 )
                builder.append( ", " );
            builder.append( roughRanges.get( i ) );
        }
        builder.append( "}]" );
        return builder.toString();
    }*/
    
    public static interface Visitor<T>{
    	/**
    	 * Visits <code>range</code>.
    	 * @param range the range that is visited
    	 * @param rough whether the range is in a rough level or not
    	 * @return <code>null</code> if other nodes should be visited, an
    	 * element not <code>null</code> if the operation should be canceled
    	 */
    	public T visit( Range range, boolean rough );
    }
    
    /**
     * An input-range specifies a range in one of the input files.
     * @author Benjamin Sigg
     */
    public static interface Range {
    	/**
    	 * A unique identifier for this range, only valid within the context
    	 * of this {@link RangeDescription}.
    	 * @return the unique identifier
    	 */
    	public int internalIndex();
    	
        /**
         * Gets the index of the first character in the input file.
         * @return the begin of this range
         */
        public int left();
        
        /**
         * Gets the index of the last character in the input file.
         * @return the end of this range
         */
        public int right();
        
        /**
         * Gets the number of characters encompassed by this range.
         * @return the number of characters
         */
        public int length();
        
        /**
         * Gets the line in which this range starts.
         * @return the line of the first character
         */
        public int line();
        
        /**
         * How this range was included into the output file.
         * @return some flags from {@link RangeDescription}
         */
        public int sourceFlags();
        
        /**
         * Gets the file to which this range is related.
         * @return the file in which this range lies
         */
        public FileInfo file();
        
        /**
         * Gets some parent, this is a {@link Range} from the next
         * upper range. If this is a rough range, then the parent is
         * fine and vice versa. Is <code>null</code> if this range
         * is a root. Note that there could be more than one parent, but
         * in general there is only one.
         * @return some parent
         */
        public Range parent();
        
        /**
         * Gets the next level of rough ranges. A rough range may span over
         * several other ranges that are quite different (e.g. some of them
         * are macros, other not). A rough range may have fine and rough
         * children.
         * @return the ranges, may be empty or <code>null</code>
         */
        public Range[] roughRanges();
        
        /**
         * Gets the next level of fine ranges. A fine range may span over
         * several other ranges that have only minor differences (e.g. they
         * must all be the same macro). A fine range only has rough children.
         * @return the ranges, may be empty or <code>null</code>
         */
        public Range[] fineRanges();
    }
    
    private class RangeImpl implements Range{
        private int left;
        private int right;
        private int line;
        private int sourceFlag;
        private FileInfo file;
        private Range[] roughRanges;
        private Range[] fineRanges;
        private int uniqueIndex;
        private Range parent;
        
        public RangeImpl( int left, int right, int line, int sourceFlag, FileInfo file, Range[] roughRanges, Range[] fineRanges ){
            this.left = left;
            this.right = right;
            this.line = line;
            this.sourceFlag = sourceFlag;
            this.file = file;
            
            this.roughRanges = roughRanges;
            this.fineRanges = fineRanges;
            
            uniqueIndex = RangeDescription.this.ranges.size();
            
            if( fineRanges == null )
            	setParents( roughRanges );
            
            setParents( fineRanges );
        }
        
        private void setParents( Range[] children ){
        	if( children != null ){
        		for( Range range : children ){
        			((RangeImpl)range).parent( this );
        		}
        	}
        }
        
        public Iterator<Range> iterator() {
        	return new Iterator<Range>(){
        		int index = 0;
        		
        		public boolean hasNext() {
        			return index < roughRanges.length;
        		}
        		
        		public Range next() {
        			return roughRanges[ index++ ];
        		}
        		
        		public void remove() {
        			throw new UnsupportedOperationException();
        		}
        	};
        }
        
        public Range parent() {
        	return parent;
        }
        
        public void parent( Range parent ){
        	if( this.parent == null ){
        		this.parent = parent;
        	}
        }
        
        public int internalIndex() {
        	return uniqueIndex;
        }
        
        public int left() {
            return left;
        }
        public int right() {
            return right;
        }
        public int length(){
	        return right-left+1;
        }
        public int line() {
            return line;
        }
        public int sourceFlags() {
            return sourceFlag;
        }
        public FileInfo file() {
            return file;
        }
        
        public Range[] roughRanges() {
        	return roughRanges;
        }
        
        public Range[] fineRanges() {
        	return fineRanges;
        }
        
        @Override
        public String toString() {
            return "[file=" + file + ", lines=[" + line + "-" + line + "], range={" + left + "-" + right + "}]";
        }
    }
}
