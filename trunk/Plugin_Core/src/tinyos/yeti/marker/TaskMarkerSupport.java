package tinyos.yeti.marker;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import tinyos.yeti.Debug;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.nesc.IMultiReader;

public class TaskMarkerSupport{
	public static final String TEMPORARY_TASK_MARKER_TYPE = TinyOSPlugin.PLUGIN_ID + ".taskMarker";
	
	public static final String[] TAGS = { "FIXME", "TODO" };
	
	
	/**
	 * This method searches for comments containing the TODO tag and stores these
	 * comments as {@link IMarker} of type {@link #TEMPORARY_TASK_MARKER_TYPE}.
	 * @param resource the resource to analyze
	 * @param reader the contents of the resource
	 */
	public static void synchronizeMessages( IResource resource, IMultiReader reader ){
		List<Task> tasks = new ArrayList<Task>();
		TaskFinder finder = new TaskFinder();
		
		try{
			Reader text = reader.open();
			int current = -1;
			
			int previous = 0;
			int line = 1;
			boolean singleLineComment = false;
			boolean multiLineComment = false;
			boolean string = false;
			boolean character = false;
			
			while( (current = text.read()) != -1 ){
				boolean newline = newline( current );
				
				if( multiLineComment ){
					if( previous == '*' && current == '/' ){
						multiLineComment = false;
						Task task = finder.clean( line );
						if( task != null ){
							tasks.add( task );
						}
					}
					if( newline ){
						finder.push( (char)previous );
						Task task = finder.clean( line );
						if( task != null ){
							tasks.add( task );
						}
						previous = 0;
					}
				}
				else if( singleLineComment ){
					if( newline ){
						singleLineComment = false;
						finder.push( (char)previous );
						Task task = finder.clean( line );
						if( task != null ){
							tasks.add( task );
						}
					}
				}
				else if( string ){
					if( previous != '\\' && current == '"' ){
						string = false;
					}
				}
				else if( character ){
					if( previous != '\\' && current == '\'' ){
						character = false;
					}
				}
				else if( previous == '/' && current == '*' ){
					multiLineComment = true;
					current = 0;
				}
				else if( previous == '/' && current == '/' ){
					singleLineComment = true;
				}
				else if( current == '"' ){
					string = true;
				}
				else if( current == '\'' ){
					character = true;
				}
				

				if( singleLineComment || multiLineComment ){
					if( previous != 0 ){
						finder.push( (char)previous );
					}
				}
				
				if( newline ){
					line++;
					finder.clean();
				}
				
				previous = current;
			}
			
			if( singleLineComment || multiLineComment ){
				finder.push( (char)previous );
				Task task = finder.clean( line );
				if( task != null ){
					tasks.add( task );
				}
			}
		}
		catch( IOException ex ){
			TinyOSPlugin.log( ex );
		}
		
		synchronizeMessages( resource, tasks );
	}
	
	private static boolean newline( int character ){
		return character == '\n' || character == '\r';
	}
	
	private static void synchronizeMessages( final IResource resource, final Collection<Task> messages ){
		Job job = new Job( "Update tasks '" + resource.getName() + "'" ){
			@Override
			protected IStatus run( IProgressMonitor monitor ){
				monitor.beginTask( "Update tasks", IProgressMonitor.UNKNOWN );
				
				try{
					if( !resource.exists() || !resource.isAccessible() ){
						monitor.done();
						return Status.CANCEL_STATUS;
					}
					
					resource.deleteMarkers( TEMPORARY_TASK_MARKER_TYPE, true, IResource.DEPTH_ZERO );
					
					for( Task task : messages ){
						IMarker marker = resource.createMarker( TEMPORARY_TASK_MARKER_TYPE );
						marker.setAttribute( IMarker.LINE_NUMBER, task.line );
						marker.setAttribute( IMarker.MESSAGE, task.message.trim() );
						marker.setAttribute( IMarker.USER_EDITABLE, false );
						marker.setAttribute( IMarker.PRIORITY, task.priority );
						if( Debug.DEBUG ){
							Debug.info( "Task[line=" + task.line + ", priority=" + task.priority + ", message=" + task.message + "]" );
						}
					}
				}
				catch( CoreException e ){
					TinyOSPlugin.log( e );
				}
				
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setSystem( true );
		job.setPriority( Job.DECORATE );
		job.setRule( resource );
		job.schedule();
	}
	
	private static class TaskFinder{
		private StringBuilder builder = new StringBuilder();
		private char[] tag;
		private int offset = 0;
		private int length = 0;
		private int reading = -1;
		
		private char[][] tags;
		private int maxTagLength;
		
		public TaskFinder(){
			tags = new char[ TAGS.length ][];
			for( int i = 0; i < tags.length; i++ ){
				tags[i] = TAGS[i].toCharArray();
				maxTagLength = Math.max( maxTagLength, tags[i].length );
			}
			maxTagLength += 2;
			tag = new char[ maxTagLength ];
		}
		
		public void push( char c ){
			if( reading >= 0 ){
				builder.append( c );
			}
			else{
				tag[ (offset+length) % maxTagLength ] = c;
				if( length < maxTagLength )
					length++;
				else
					offset++;
				
				int index = 0;
				out:for( char[] check : tags ){
					if( length >= check.length + 2 ){
						int checkLength = check.length;
						
						if( !Character.isLetterOrDigit( tag[ (offset+length-checkLength-2  ) % maxTagLength] ) && 
							!Character.isLetterOrDigit( tag[ (offset+length-1) % maxTagLength] ) ){
							
							for( int i = 0; i < checkLength; i++ ){
								if( tag[ (offset+length-checkLength+i-1) % maxTagLength] != check[i] ){
									index++;
									continue out;
								}
							}
							
							reading = index;
							break out;
						}
					}

					index++;
				}
			}
		}
		
		public Task clean( int line ){
			if( reading == -1 ){
				push( ' ' );
			}
			
			if( reading >= 0 ){
				String message = builder.toString();
				
				int priority = IMarker.PRIORITY_NORMAL;
				switch( reading ){
					case 0:
						priority = IMarker.PRIORITY_HIGH;
						break;
					case 1:
						priority = IMarker.PRIORITY_NORMAL;
						break;
				}
				
				
				Task result = new Task( line, TAGS[reading] + " " + message, priority );
				clean();
				return result;
			}
			else{
				clean();
				return null;
			}
		}
		
		public void clean(){
			length = 0;
			reading = -1;
			builder.setLength( 0 );
			Arrays.fill( tag, (char)0 );
		}
	}
	
	private static class Task{
		public int line;
		public String message;
		public int priority;
		
		public Task( int line, String message, int priority ){
			this.line = line;
			this.message = message;
			this.priority = priority;
		}
	}
}
