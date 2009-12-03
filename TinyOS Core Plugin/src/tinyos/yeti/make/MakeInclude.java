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
package tinyos.yeti.make;

/**
 * A {@link MakeInclude} describes a directory or a file which is somehow
 * included when building a project.
 * @author Benjamin Sigg
 */
public class MakeInclude{
    public static MakeInclude[] combine( MakeInclude[] alpha, MakeInclude[] beta ) {
        if( alpha == null )
            return beta;
        
        if( beta == null )
            return alpha;
        
        MakeInclude[] result = new MakeInclude[ alpha.length + beta.length ];
        System.arraycopy( alpha, 0, result, 0, alpha.length );
        System.arraycopy( beta, 0, result, alpha.length, beta.length );
        return result;
    }
    
    public static MakeInclude[] combine( MakeInclude[]... includes ) {
        int sum = 0;
        for( MakeInclude[] array : includes ){
            if( array != null )
                sum += array.length;
        }
        
        MakeInclude[] result = new MakeInclude[ sum ];
        int offset = 0;
        for( MakeInclude[] array : includes ){
            if( array != null && array.length > 0 ){
                System.arraycopy( array, 0, result, offset, array.length );
                offset += array.length;
            }
        }
        
        return result;
    }
    
    /* 
    public static enum Type{
        /** a single directory which gets included on build *
        SOURCE,
        /** a single directory which contains system files *
        SYSTEM,
        /** a file or directory whose declarations are present in any other file *
        GLOBAL
    }*/
    /*
    public static Type type( String name ){
        if( "global".equals( name ))
            return Type.GLOBAL;
        
        if( "source".equals( name ))
            return Type.SOURCE;
        
        if( "system".equals( name ))
            return Type.SYSTEM;
        
        throw new IllegalArgumentException( "unknown type: " + name );
    }
    
    public static String type( Type type ){
        switch( type ){
            case GLOBAL: return "global";
            case SOURCE: return "source";
            case SYSTEM: return "system";
            default: throw new IllegalArgumentException( "unknown type: " + type );
        }
    }
    */
    
    public static enum Include{
    	/** does not react on include directive */
    	NONE,
    	/** can only be included through directive with " */
    	SOURCE,
    	/** can be included by any directive */
    	SYSTEM;
    }
    
    public static String include( Include include ){
    	switch( include ){
    		case NONE: return "none";
    		case SOURCE: return "source";
    		case SYSTEM: return "system";
    		default: throw new IllegalArgumentException( "unknown include: " + include );
    	}
    }
    
    public static Include include( String name ){
    	if( "none".equals( name ))
    		return Include.NONE;
    	if( "source".equals( name ))
    		return Include.SOURCE;
    	if( "system".equals( name ))
    		return Include.SYSTEM;
    	
    	throw new IllegalArgumentException( "unknown include: " + name );
    }
    
    private String path;
    
    private boolean recursive;
    private boolean ncc;
    private boolean global;
    private Include include;
    
    public MakeInclude( String path ){
        this( path, Include.NONE, false, false, false );
    }
    
    public MakeInclude( String path, Include include, boolean recursive, boolean ncc, boolean global ){
    	this.path = path;
    	this.recursive = recursive;
    	this.ncc = ncc;
    	this.include = include;
    	this.global = global;
    }
    
    public String getPath(){
        return path;
    }
    
    public boolean isRecursive(){
        return recursive;
    }
    
    public boolean isNcc(){
		return ncc;
	}
    
    public boolean isGlobal(){
		return global;
	}
    
    public Include getInclude(){
		return include;
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + (global ? 1231 : 1237);
		result = prime * result + ((include == null) ? 0 : include.hashCode());
		result = prime * result + (ncc ? 1231 : 1237);
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + (recursive ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals( Object obj ){
		if( this == obj )
			return true;
		if( obj == null )
			return false;
		if( getClass() != obj.getClass() )
			return false;
		MakeInclude other = (MakeInclude)obj;
		if( global != other.global )
			return false;
		if( include == null ){
			if( other.include != null )
				return false;
		}
		else if( !include.equals( other.include ) )
			return false;
		if( ncc != other.ncc )
			return false;
		if( path == null ){
			if( other.path != null )
				return false;
		}
		else if( !path.equals( other.path ) )
			return false;
		if( recursive != other.recursive )
			return false;
		return true;
	}
}
