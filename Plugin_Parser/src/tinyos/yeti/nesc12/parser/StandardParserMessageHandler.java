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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import tinyos.yeti.Debug;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IMessage;
import tinyos.yeti.nesc12.ParserMessageHandler;
import tinyos.yeti.preprocessor.RangeDescription;
import tinyos.yeti.preprocessor.RangeDescription.Range;
import tinyos.yeti.preprocessor.output.Insight;

/**
 * A message handler that creates a list of {@link IMessage}s.
 * @author Benjamin Sigg
 */
public class StandardParserMessageHandler implements ParserMessageHandler{
    /** the file for which the messages should be reported */
    private IParseFile file;

    /** the errors that were found */
    private List<IMessage> errors = new LinkedList<IMessage>();

    /** 
     * The number of base errors, meaning the number of errors that are truly
     * shown and that are not generated to show an additional problem related
     * to another error.
     */
    private int count = 0;

    /**
     * Creates a new handler.
     * @param file the file for which messages should be reported. If
     * <code>null</code>, then all messages will be reported.
     */
    public StandardParserMessageHandler( IParseFile file ){
        this.file = file;
    }

    /**
     * Gets the errors that were reported.
     * @return the list of errors
     */
    public List<IMessage> getErrors() {
        return errors;
    }

    public void error( String message, boolean preprocessor, Insight insight, RangeDescription... ranges ) {
        report( IMessage.Severity.ERROR, message, preprocessor, insight, ranges );
    }

    public void message( String message, boolean preprocessor, Insight insight, RangeDescription... ranges ) {
        report( IMessage.Severity.INFO, message, preprocessor, insight, ranges );
    }

    public void warning( String message, boolean preprocessor, Insight insight, RangeDescription... ranges ) {
        report( IMessage.Severity.WARNING, message, preprocessor, insight, ranges );
    }

    private void report( IMessage.Severity severity, String message, boolean preprocessor, Insight insight, RangeDescription[] ranges ){
        boolean first = true;

        if( insight != null ){
            if( preprocessor )
                insight = ParserInsights.setPreprocessor( insight );
            else
                insight = ParserInsights.setParser( insight );

            insight = insight.seal();
        }

        for( RangeDescription range : ranges ){
        	List<RangeDescription.Range> stack = new ArrayList<Range>();
        	
        	for( int i = 0, n = range.getRootCount(); i<n; i++ ){
        		RangeDescription.Range root = range.getRoot( i );
        		first = reportRough( stack, root, severity, message, preprocessor, insight, first );
            }
        }
    }
    
    private boolean reportRough( List<RangeDescription.Range> path, RangeDescription.Range range, IMessage.Severity severity, String message, boolean preprocessor, Insight insight, boolean first ){
    	RangeDescription.Range[] fines = range.fineRanges();
    	if( fines != null ){
    		for( RangeDescription.Range fine : fines ){
    			if( fine.parent() == range ){
    				first = reportFine( path, fine, severity, message, preprocessor, insight, first );
    			}
    		}
    	}
    	return first;
    }
    
    private boolean reportFine( List<RangeDescription.Range> path, RangeDescription.Range range, IMessage.Severity severity, String message, boolean preprocessor, Insight insight, boolean first ){
        boolean containsMacro = (range.sourceFlags() & RangeDescription.CONTAINS_MACRO) != 0;
        boolean containsInclude = (range.sourceFlags() & RangeDescription.CONTAINS_INCLUDE) != 0;
        boolean inMacro = (range.sourceFlags() & RangeDescription.IN_MACRO) != 0;
        boolean inInclude = (range.sourceFlags() & RangeDescription.IN_INCLUDE) != 0;

        if( !containsInclude || severity == IMessage.Severity.ERROR ){
            IParseFile rangeFile = null;
            NesC12FileInfo fileInfo = (NesC12FileInfo)range.file();
            if( fileInfo != null )
            	rangeFile = fileInfo.getParseFile();
            if( rangeFile == null )
            	rangeFile = this.file;

            // remove this line, and macros will get tagged if they are
            // used in a statement with errors.
            if( this.file == null || this.file.equals( rangeFile )){
            	if( first ){
            		count++;
            	}

            	StringBuilder builder = new StringBuilder();

            	if( !first )
            		builder.append( "[" );

            	if( Debug.DEBUG ){
            		builder.append( "#" );
            		builder.append( count );
            		builder.append( ": " );
            	}

            	StringBuilder key = new StringBuilder();

            	if( containsMacro || containsInclude || preprocessor || inMacro || inInclude ){
            		key.append( "(" );
            		if( preprocessor )
            			key.append( "preprocessor" );

            		if( containsMacro ){
            			if( preprocessor )
                			key.append( "; " );
            			
            			key.append( "contains macro" );
            		}

            		if( containsInclude ){
            			if( preprocessor || containsMacro )
                			key.append( "; " );
            			
            			key.append( "in included file" );
            		}

            		if( containsMacro || containsInclude ){
            			key.append( "; see " );
            			leafs( range, key );
            		}
            		
            		if( inMacro || inInclude ){
            			if( preprocessor || containsInclude || containsMacro ){
            				key.append( "; " );
            			}
            			key.append( "origin " );
            			for( int j = 0, m = path.size(); j<m; j++ ){
            				if( j > 0 )
            					key.append( ", " );
            				key.append( small( path.get( j )));
            			}
            		}
            		key.append( ") " );
            	}

            	key.append( message );

            	builder.append( key );
            	if( !first )
            		builder.append( "]" );

            	Message error = new Message( this.file, severity, builder.toString(), key.toString(), insight );
            	int length = range.right() - range.left();

            	if( length <= 0 ){
            		error.addRegion( rangeFile, Math.max( 0, range.left()-1 ), 1, range.line() );
            	}
            	else{
            		error.addRegion( rangeFile, range.left(), length, range.line() );
            	}

            	errors.add( error );

            	first = false;
            }
        }
        
        RangeDescription.Range[] roughs = range.roughRanges();
        if( roughs != null ){
        	path.add( path.size(), range );
        	for( RangeDescription.Range rough : roughs ){
        		if( rough.parent() == range ){
        			first = reportRough( path, rough, severity, message, preprocessor, insight, first );
        		}
        	}
        	path.remove( path.size()-1 );
        }
        
        return first;
    }
        
    private void leafs( RangeDescription.Range range, StringBuilder builder ){
    	Range[] subs = range.roughRanges();
    	if( subs != null ){
    		boolean first = true;
    		for( RangeDescription.Range sub : subs ){
    			first = leafs( sub, builder, first );
    		}
    	}
    }
    
    private boolean leafs( RangeDescription.Range range, StringBuilder builder, boolean first ){
    	Range[] subs = range.roughRanges();

    	if( first ){
    		first = false;
    	}
    	else{
    		builder.append( ", " );
    	}
    	builder.append( small( range ));

    	if( subs != null ){
    		for( RangeDescription.Range sub : subs ){
    			first = leafs( sub, builder, first );
    		}
    	}
    	return first;
    }
    
    private String small( RangeDescription.Range range ){
    	return ((NesC12FileInfo) range.file()).getName() + ": " + range.line();
    }
}
