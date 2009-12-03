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
package tinyos.yeti.editors;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.MarkerUtilities;

import tinyos.yeti.Debug;
import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.markerresolutions.NesCMarkerUtilities;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.IMessage;
import tinyos.yeti.nature.MissingNatureException;

public class NesCProblemMarker {
    /** key for the number of marked {@link IMarker}s */
    private static final QualifiedName SIZE = new QualifiedName( TinyOSPlugin.PLUGIN_ID, "marker.size" );
    
    /** timestamp of the last synchronizing of a resource */
    private static final QualifiedName TIMESTAMP = new QualifiedName( TinyOSPlugin.PLUGIN_ID, "marker.timestamp" );
    
    /**
     * Used for {@link IMarker}s that have an {@link IMessage} as source,
     * the value {@link Boolean#TRUE} is put into the markers map.
     */
    public static final String MARKER_MESSAGE_SOURCE = TinyOSPlugin.PLUGIN_ID + ".message_source";
    
    /**
     * Updates the messages concerning <code>resource</code>. This method
     * starts a new job and returns immediately.
     * @param resource the resource to update
     * @param parseFile the file that is parsed
     * @param messages the new messages, may be <code>null</code>
     */
    public static void synchronizeMessages( final IResource resource, final IParseFile parseFile, final IMessage[] messages ) {
    	UIJob job = new UIJob( "Update messages" ){
    		@Override
    		public IStatus runInUIThread( IProgressMonitor monitor ){
    			try{
    				monitor.beginTask( "Update messages", messages == null ? 0 : messages.length );
    				maybeClearInfo( resource );
    				resource.deleteMarkers( IMarker.PROBLEM, true, IResource.DEPTH_ONE );

    				if( messages != null ){
    					IProject project = resource.getProject();
    					ProjectTOS tos = null;
    					if( project != null ){
    						TinyOSPlugin plugin = TinyOSPlugin.getDefault();
    						if( plugin != null ){
    							try{
    								tos = plugin.getProjectTOS( project );
    							}
    							catch( MissingNatureException ex ){
    								// silent
    							}
    						}
    					}

    					for( IMessage message : messages ){
    						IFileRegion[] regions = message.getRegions();
    						if( regions != null && regions.length > 0 ){
    							for( IFileRegion region : regions ){
    								if( region.getParseFile() != null && region.getParseFile().equals( parseFile )){
    									putMessage( resource, region.getOffset(), region.getLength(), region.getLine(), message );
    								}
    								else if( tos != null ){
    									boolean set = false;
    									IParseFile pFile = region.getParseFile();
    									if( pFile != null ){
    										File file = pFile.toFile();
    										if( file != null && project != null ){
    											IPath projectPath = project.getLocation();
    											if( projectPath != null ){
    												IPath path = new Path( file.getAbsolutePath() );
    												if( projectPath.isPrefixOf( path )){
    													path = path.removeFirstSegments( projectPath.segmentCount() );
    													IResource foreignResource = project.findMember( path );
    													if( foreignResource != null ){
    														putMessage( foreignResource, region.getOffset(), region.getLength(), region.getLine(), message );
    														set = true;
    													}
    												}
    											}
    										}
    									}
    									// TODO should not ignore messages for other files
    									if( !set ){
    										//putMessage( resource, 0, 0, -1, message );
    										Debug.warning( "Message from another file is ignored: " + message.getMessage() );
    									}
    								}
    							}
    						}
    						else{
    							putMessage( resource, -1, -1, -1, message );
    						}
    					}
    				}
    			}
    			catch( CoreException ex ){
    				monitor.done();
    				return new Status( IStatus.ERROR, TinyOSPlugin.PLUGIN_ID, ex.getMessage(), ex );
    			}
    	        monitor.done();
    			return Status.OK_STATUS;
    		}
    	};
    	
    	job.setPriority( Job.INTERACTIVE );
    	job.setSystem( true );
    	job.schedule();
    }

    /**
     * Inserts a new message into <code>resource</code>.
     * @param resource where to add the message
     * @param offset the first character in the message region
     * @param length the number of characters in the message region
     * @param line the line where the message stands
     * @param message the message itself
     */
    private static void putMessage( IResource resource, int offset, int length, int line, IMessage message ) throws CoreException{
    	if( !resource.isAccessible() || !resource.getProject().isOpen() )
    		return;
    	
        Map<String, Object> map = new HashMap<String, Object>();
        map.put( MARKER_MESSAGE_SOURCE, Boolean.TRUE );
        
        Map<String, Object> quickfix = message.getQuickfixInfos();
        if( quickfix != null ){
            map.putAll( quickfix );
        }

        if( offset != -1 ){
            MarkerUtilities.setCharStart( map, offset );
            if( length != -1 ){
                MarkerUtilities.setCharEnd( map, offset + length );
            }
        }

        if( line != -1 )
            MarkerUtilities.setLineNumber( map, line );

        MarkerUtilities.setMessage( map, message.getMessage() );
        NesCMarkerUtilities.setMessageKey( map, message.getMessageKey() );

        map.put( IMarker.PRIORITY, IMarker.PRIORITY_HIGH );
        IMessage.Severity severity = message.getSeverity();
        if( severity != IMessage.Severity.INFO ){
            if( isStored( resource, offset, length, message.getMessageKey() ))
                severity = IMessage.Severity.INFO;
        }
        
        switch( severity ){
            case ERROR:
                map.put( IMarker.SEVERITY, IMarker.SEVERITY_ERROR );
                break;
            case INFO:
                map.put( IMarker.SEVERITY, IMarker.SEVERITY_INFO );
                break;
            case WARNING:
                map.put( IMarker.SEVERITY, IMarker.SEVERITY_WARNING );
                break;
        }

        MarkerUtilities.createMarker( resource, map, IMarker.PROBLEM );
    }
    
    public static void putMessage( IResource resource, IMessage message ) throws CoreException{
    	putMessage( resource, -1, -1, -1, message );
    }
    
    /**
     * Converts <code>marker</code> to a marker that is of severity "info".
     * @param marker the marker to convert
     */
    @SuppressWarnings("unchecked")
    public static void convertToInformation( IMarker marker ){
        try{
            if( !marker.exists() )
                return;
            IResource resource = marker.getResource();
            Map<String, Object> attributes = marker.getAttributes();
            if( attributes == null )
                return;
            
            int offset = MarkerUtilities.getCharStart( marker );
            int length = MarkerUtilities.getCharEnd( marker ) - offset;
            
            String key = NesCMarkerUtilities.getMessageKey( marker );
            
            store( resource, offset, length, key );
            
            marker.delete();
            attributes.put( IMarker.SEVERITY, IMarker.SEVERITY_INFO );
            MarkerUtilities.createMarker( resource, attributes, IMarker.PROBLEM );
            
        }
        catch( CoreException ex ){
            // should not happen, put if it happens there is no need to 
            // repair anything, this is not a critical operation
            TinyOSPlugin.getDefault().getLog().log( ex.getStatus() );
        }
    }
    
    private static void store( IResource resource, int offset, int length, String text ) throws CoreException{
        // update number of stored keys
        String sizeString = resource.getPersistentProperty( SIZE );
        int count = 1;
        if( sizeString != null )
            count = Integer.parseInt( sizeString ) + 1;
        resource.setPersistentProperty( SIZE, String.valueOf( count ) );
        
        // store new marker
        QualifiedName key = key( count-1 );
        resource.setPersistentProperty( key, value( offset, length, text ) );
    }
    
    
    
    private static boolean isStored( IResource resource, int offset, int length, String text ) throws CoreException{
        String sizeString = resource.getPersistentProperty( SIZE );
        if( sizeString == null )
            return false;
        
        int count = Integer.parseInt( sizeString );
        String value = value( offset, length, text );
        
        for( int i = 0; i < count; i++ ){
            if( value.equals( resource.getPersistentProperty( key( i ) ) ))
                return true;
        }
        
        return false;
    }
    
    private static void maybeClearInfo( IResource resource ) throws CoreException{
        String timestampString = resource.getPersistentProperty( TIMESTAMP );
        long timestamp = resource.getModificationStamp();
        if( timestampString == null ){
            clearInfo( resource );
        }
        else{
            long oldTimestamp = Long.valueOf( timestampString );
            if( oldTimestamp != timestamp ){
                clearInfo( resource );
            }
        }
        resource.setPersistentProperty( TIMESTAMP, String.valueOf( timestamp ) );
    }
    
    /**
     * Ensures that no message gets converted into an info message when
     * {@link #synchronizeMessages(IResource, IParseFile, IMessage[])} is called.
     * @param resource the resource to check
     * @throws CoreException if the resource can't be accessed
     */
    private static void clearInfo( IResource resource ) throws CoreException{
        String sizeString = resource.getPersistentProperty( SIZE );
        if( sizeString == null )
            return;
        
        int count = Integer.parseInt( sizeString );
        resource.setPersistentProperty( SIZE, null );
        
        for( int i = 0; i < count; i++ ){
            resource.setPersistentProperty( key( i ), null );
        }
    }
    
    private static QualifiedName key( int index ){
        return new QualifiedName( TinyOSPlugin.PLUGIN_ID, "marker.index." + index );
    }
    
    private static String value( int offset, int length, String text ){
        return offset + "." + length + "." + text;
    }
}
