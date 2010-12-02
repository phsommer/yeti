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
package tinyos.yeti.nesc12.ep.rules.quickfix;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.progress.UIJob;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.nesc12.parser.ParserInsights;
import tinyos.yeti.preprocessor.output.Insight;
import tinyos_parser.NesC12ParserPlugin;

public class JumpShadowField implements ISingleQuickfixRule{
	public void suggest( Insight error, QuickfixCollector collector ){
		if( error == null )
			return;
		
		if( error.getId() != ParserInsights.FIELD_SHADOWING )
			return;
		
		IFileRegion source = ParserInsights.region( error.get( ParserInsights.FIELD_SHADOWING_LOCATION, null ), collector.getProject() );
		IFileRegion destination = ParserInsights.region( error.get( ParserInsights.FIELD_SHADOWED_LOCATION, null ), collector.getProject() );

		if( source != null ){
			collector.addSingle( new JumpQuickfix( "Select shadowing field", source ) );
		}

		if( destination != null ){
			collector.addSingle( new JumpQuickfix( "Select shadowed field", destination ) );
		}
	}
	
	private static class JumpQuickfix implements ISingleQuickfix{
		private IFileRegion destination;
		private String label;
		
		public JumpQuickfix( String label, IFileRegion destination ){
			this.label = label;
			this.destination = destination;
		}
		
		public String getDescription(){
			return null;
		}

		public Image getImage(){
			return null;
		}

		public String getLabel(){
			return label;
		}

		public void run( Insight error, QuickfixInformation information ){
			UIJob open = new UIJob( "Open File" ){
				@Override
				public IStatus runInUIThread( IProgressMonitor monitor ){
					monitor.beginTask( "Open File", 1 );
					TinyOSPlugin plugin = TinyOSPlugin.getDefault();
					if( plugin == null ){
						monitor.done();
						return Status.OK_STATUS;
					}
					
					try{
						plugin.openFileInTextEditor( destination );
					}
					catch( CoreException e ){
						IStatus status = new Status( IStatus.ERROR, NesC12ParserPlugin.PLUGIN_ID, e.getMessage(), e );
						NesC12ParserPlugin.getDefault().getLog().log( status );
					}
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			open.setSystem( true );
			open.setPriority( Job.INTERACTIVE );
			open.schedule();
		}
		
	}
}
