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
package tinyos.yeti.nesc12.parser;

import java.io.File;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.preprocessor.RangeDescription;
import tinyos.yeti.preprocessor.output.Insight;
import tinyos.yeti.preprocessor.output.Insights;

/**
 * Factory methods for {@link Insight}s.
 * @author Benjamin Sigg
 * @see Insights
 */
public abstract class ParserInsights{
    public static String location( ASTNode node ){
        StringBuilder builder = new StringBuilder();
        location( builder, node );
        return builder.toString();
    }
    private static void location( StringBuilder builder, ASTNode node ){
        ASTNode parent = node.getParent();
        if( parent != null ){
            location( builder, parent );
            for( int i = 0, n = parent.getChildrenCount(); i<n; i++ ){
                if( parent.getChild( i ) == node ){
                    if( builder.length() > 0 )
                        builder.append( "." );
                    builder.append( i );
                    return;
                }
            }
        }
    }
    public static ASTNode location( String path, ASTNode root ){
        if( path == null )
            return null;
        String[] split = path.split( "\\." );
        ASTNode node = root;
        for( String segment : split ){
            if( node == null )
                return null;
            
            int index = Integer.parseInt( segment );
            if( index >= node.getChildrenCount() )
                return null;
            
            node = node.getChild( index );
        }
        return node;
    }
    
    public static String path( ASTModelPath path ){
    	StringBuilder builder = new StringBuilder();
    	
    	// file
    	File file = path.getParseFile().toFile();
    	String filePath = file.getAbsolutePath();
    	builder.append( filePath.length() );
    	builder.append( "." );
    	builder.append( filePath );
    	
    	// array
    	builder.append( array( path.getNodes() ) );
    	
    	return builder.toString();
    }

    /**
     * Tries to read a {@link ASTModelPath} from <code>path</code>.
     * @param path the string to convert
     * @param project the project to which the path belongs
     * @return the path or <code>null</code> if it cannot be correctly resolved
     */
    public static ASTModelPath path( String path, ProjectTOS project ){
    	// file
    	int offset = path.indexOf( '.' );
    	int length = Integer.parseInt( path.substring( 0, offset ) );
    	offset++;
    	String file = path.substring( offset, offset+length );
    	offset += length;
    	
    	IParseFile parseFile = project.getModel().parseFile( new File( file ) );
    	if( parseFile == null )
    		return null;
    	
    	// path
    	String[] nodes = array( path.substring( offset ));
    	
    	return new ASTModelPath( parseFile, nodes );
    }

    public static String array( String[] array ){
    	StringBuilder builder = new StringBuilder();
    	builder.append( array.length );
    	builder.append( '.' );
    	
    	for( String item : array ){
    		builder.append( item.length() );
    		builder.append( '.' );
    		builder.append( item );
    	}
    	
    	return builder.toString();
    }
    
    public static String[] array( String array ){
    	int offset = array.indexOf( '.' );
    	int length = Integer.parseInt( array.substring( 0, offset ) );
    	offset++;
    	
    	String[] result = new String[ length ];
    	for( int i = 0; i < result.length; i++ ){
    		int next = array.indexOf( '.', offset+1 );
    		length = Integer.parseInt( array.substring( offset, next ) );
    		offset = next+1;
    		result[i] = array.substring( offset, offset+length );
    		offset += length;
    	}
    	
    	return result;
    }
    
    public static String region( RangeDescription range ){
    	if( range == null )
    		return null;
    	RangeDescription.Range source = range.getSource();
    	if( source == null )
    		return null;
    	
    	StringBuilder builder = new StringBuilder();
    	
        String path = source.file().getPath();
        builder.append( path.length() );
        builder.append( '.' );
        builder.append( path );
        
        builder.append( source.left() );
        builder.append( '.' );
        builder.append( source.right() - source.left() );
        builder.append( '.' );
        builder.append( source.line() );
        
        return builder.toString();
    }
    
    public static IFileRegion region( String region, ProjectTOS project ){
    	// file
    	int offset = region.indexOf( '.' );
    	int length = Integer.parseInt( region.substring( 0, offset ) );
    	offset++;
    	String file = region.substring( offset, offset+length );
    	offset += length;
    	
    	IParseFile parseFile = project.getModel().parseFile( new File( file ) );
    	if( parseFile == null )
    		return null;
    	
    	// location
    	int next = region.indexOf( '.', offset );
    	int regionOffset = Integer.parseInt( region.substring( offset, next ) );
    	offset = next+1;
    	next = region.indexOf( '.', offset );
    	int regionLength = Integer.parseInt( region.substring( offset, next ) );
    	offset = next+1;
    	int regionLine = Integer.parseInt( region.substring( offset ) );
    	
    	return new FileRegion( parseFile, regionOffset, regionLength, regionLine );
    }
    
    public static final String  INSIGHT_ORIGIN_INT = "insight.p.origin";
    public static final int     INSIGHT_ORIGIN_PREPROCESSOR_FLAG = 1;
    public static final int     INSIGHT_ORIGIN_PARSER_FLAG = 2;
    public static Insight setPreprocessor( Insight insight ){
        return insight.put( INSIGHT_ORIGIN_INT, INSIGHT_ORIGIN_PREPROCESSOR_FLAG );
    }
    public static boolean isPreprocessor( Insight insight ){
        return insight.get( INSIGHT_ORIGIN_INT, 0 ) == INSIGHT_ORIGIN_PREPROCESSOR_FLAG;
    }
    public static Insight setParser( Insight insight ){
        return insight.put( INSIGHT_ORIGIN_INT, INSIGHT_ORIGIN_PARSER_FLAG );
    }
    public static boolean isParser( Insight insight ){
        return insight.get( INSIGHT_ORIGIN_INT, 0 ) == INSIGHT_ORIGIN_PARSER_FLAG;
    }
    
    public static final int     MODULE_MISSING_FUNCTION = 1;
    public static final String  MODULE_MISSING_FUNCTION_INTERFACE_NAME_STRING = "insight.mmf.i.name";
    public static final String  MODULE_MISSING_FUNCTION_NAME_STRING = "insight.mmf.f.name";
    public static final String  MODULE_MISSING_FUNCTION_NODE_PATH_STRING = "insight.mmf.node";
    public static final String  MODULE_MISSING_FUNCTION_IS_EVENT_BOOLEAN = "insight.mmf.event";
    public static Insight moduleMissingFunction( String interfaceName, String functionName, boolean event, ASTNode node ){
        return Insight.base( MODULE_MISSING_FUNCTION )
            .put( MODULE_MISSING_FUNCTION_INTERFACE_NAME_STRING, interfaceName )
            .put( MODULE_MISSING_FUNCTION_NAME_STRING, functionName )
            .put( MODULE_MISSING_FUNCTION_IS_EVENT_BOOLEAN, event )
            .put( MODULE_MISSING_FUNCTION_NODE_PATH_STRING, location( node ) );
    }
    
    public static final int		FIELD_SHADOWING = 2;
    /** The name of the field */
    public static final String	FIELD_SHADOWING_NAME = "insight.fs.name";

    public static final String	FIELD_SHADOWING_LOCATION = "insight.fs.source.location";
    public static final String	FIELD_SHADOWED_LOCATION = "insight.fs.destination.location"; 
    public static Insight shadowingField( String fieldName, RangeDescription shadowing, RangeDescription shadowed ){
    	return Insight.base( FIELD_SHADOWING )
    		.put( FIELD_SHADOWING_NAME, fieldName )
    		.put( FIELD_SHADOWING_LOCATION, region( shadowing ) )
    		.put( FIELD_SHADOWED_LOCATION, region( shadowed ) );
    }
    
}
