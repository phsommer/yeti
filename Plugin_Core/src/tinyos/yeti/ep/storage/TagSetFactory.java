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
package tinyos.yeti.ep.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;

public class TagSetFactory implements IGenericFactory<TagSet>{

    /** all tags that might be seen */
    private Tag[] tags = {
        Tag.AST_CONNECTION_ICON_RESOLVE,
        Tag.AST_CONNECTION_LABEL_RESOLVE,
        Tag.AST_CONNECTION_GRAPH_ICON_RESOLVE,
        Tag.AST_CONNECTION_GRAPH_LABEL_RESOLVE,
        Tag.ASYNC,
        Tag.ATTRIBUTE,
        Tag.BINARY_COMPONENT,
        Tag.COMMAND,
        Tag.COMPONENT,
        Tag.CONFIGURATION,
        Tag.CONNECTION,
        Tag.CONNECTION_BOTH,
        Tag.CONNECTION_LEFT,
        Tag.CONNECTION_RIGHT,
        Tag.DATA_OBJECT,
        Tag.EVENT,
        Tag.FIGURE,
        Tag.FUNCTION,
        Tag.INCLUDED,
        Tag.INTERFACE,
        Tag.MODULE,
        Tag.OUTLINE,
        Tag.PROVIDES,
        Tag.RENAMED,
        Tag.STRUCT,
        Tag.TASK,
        Tag.UNION,
        Tag.USES,
        Tag.NO_BASE_EXPANSION,
        Tag.MACRO,
        Tag.IDENTIFIABLE
    };
    
    public void setTags( Tag[] tags ){
        this.tags = tags;
    }

    public TagSet create(){
        return null;
    }
    
    public void write( TagSet set, IStorage storage ) throws IOException{
        DataOutputStream out = storage.out();
        if( set == null ){
            out.writeInt( 0 );
            out.writeInt( tags.length );
        }
        else{
            out.writeInt( set.size() );
            out.writeInt( tags.length );
            for( Tag tag : set ){
                int index = -1;
                for( int i = 0; i < tags.length; i++ ){
                    if( tags[i] == tag ){
                        index = i;
                        break;
                    }
                }
                if( index == -1 )
                    throw new IOException( "unknown tag: " + tag.getId() );
                
                out.writeInt( index );
            }
        }
    }
    
    public TagSet read( TagSet set, IStorage storage ) throws IOException{
        DataInputStream in = storage.in();
        int size = in.readInt();
        if( size == 0 )
            return TagSet.EMPTY;
        
        if( tags.length != in.readInt() ){
            throw new IOException( "file format out of date, clear and ignore" );
        }
        
        TagSet tags = new TagSet();
        for( int i = 0; i < size; i++ ){
            tags.add( this.tags[ in.readInt() ] );
        }
        
        return tags;
    }
}
