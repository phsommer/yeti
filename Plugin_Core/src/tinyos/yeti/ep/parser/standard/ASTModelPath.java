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
package tinyos.yeti.ep.parser.standard;

import java.io.IOException;
import java.util.Arrays;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;

public class ASTModelPath implements IASTModelPath {
    public static final IGenericFactory<ASTModelPath> FACTORY = new IGenericFactory<ASTModelPath>(){
        public ASTModelPath create(){
            return new ASTModelPath();
        }
        
        public void write( ASTModelPath value, IStorage storage ) throws IOException{
            storage.write( value.file );
            int size = value.nodes == null ? 0 : value.nodes.length;
            storage.out().writeInt( size );
            for( int i = 0; i < size; i++ ){
                storage.writeString( value.nodes[i] );
            }
        }
        
        public ASTModelPath read( ASTModelPath value, IStorage storage ) throws IOException{
            value.file = storage.read();
            int size = storage.in().readInt();
            value.nodes = new String[ size ];
            for( int i = 0; i < size; i++ ){
                value.nodes[i] = storage.readString();
            }
            return value;
        }
    };
    
    private IParseFile file;
    private String[] nodes;
    
    /** whether {@link #hashCode} is valid */
    private boolean hashCodeValid = false;
    
    /** the hashcode of this path */
    private int hashCode;
    
    protected ASTModelPath(){
        // nothing
    }
    
    public ASTModelPath( IParseFile file, String... nodes ){
        this.file = file;
        if( nodes == null )
            nodes = new String[]{};
        this.nodes = nodes;
    }
    
    /**
     * Creates a new path replacing the file of this path with <code>file</code>
     * where ever the original file occures. 
     * @param file the new file
     * @return a new path
     */
    public ASTModelPath replaceFile( IParseFile file ){
    	String[] newNodes = new String[ nodes.length ];
    	System.arraycopy( nodes, 0, newNodes, 0, nodes.length );
    	
    	String search = this.file.getPath();
    	for( int i = 0; i < newNodes.length; i++ ){
    		if( newNodes[i].equals( search ))
    			newNodes[i] = file.getPath();
    	}
    	
    	return new ASTModelPath( file, newNodes );
    }
    
    public ASTModelPath getParent() {
        if( file == null )
            return null;
        
        if( nodes.length == 0 )
            return new ASTModelPath( null );
        
        String[] sub = new String[ nodes.length-1 ];
        System.arraycopy( nodes, 0, sub, 0, sub.length );
        return new ASTModelPath( file, sub );
    }

    public ASTModelPath getChild( IASTModelNode node ) {
        return getChild( node.getIdentifier() );
    }
    
    public ASTModelPath getChild( String identifier ){
        String[] next = new String[ nodes.length+1 ];
        System.arraycopy( nodes, 0, next, 0, nodes.length );
        next[ nodes.length ] = identifier;
        return new ASTModelPath( file, next );
    }
    
    public boolean isPrefix( ASTModelPath path ){
    	if( !path.getParseFile().equals( file ))
    		return false;
    	
    	String[] pathNodes = path.getNodes();
    	
    	if( pathNodes.length > nodes.length )
    		return false;
    	
    	for( int i = 0; i < pathNodes.length; i++ ){
    		if( !pathNodes[i].equals( nodes[i] ))
    			return false;
    	}
    	
    	return true;
    }
    
    public IParseFile getParseFile() {
        return file;
    }
    
    public int getDepth(){
    	return nodes.length;
    }
    
    public String[] getNodes() {
        return nodes;
    }
    
    @Override
    public int hashCode() {
        if( hashCodeValid )
            return hashCode;
        
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( file == null ) ? 0 : file.hashCode() );
        result = prime * result + Arrays.hashCode( nodes );
        
        hashCode = result;
        hashCodeValid = true;
        
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if( this == obj )
            return true;
        if( obj == null )
            return false;
        if( getClass() != obj.getClass() )
            return false;
        final ASTModelPath other = (ASTModelPath)obj;
        
        if( hashCode() != other.hashCode() )
            return false;
        
        if( file == null ) {
            if( other.file != null )
                return false;
        } else if( !file.equals( other.file ) )
            return false;
        if( !Arrays.equals( nodes, other.nodes ) )
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "Path[file=" + file + ", nodes=" + Arrays.toString( nodes ) + "]";
    }
}
