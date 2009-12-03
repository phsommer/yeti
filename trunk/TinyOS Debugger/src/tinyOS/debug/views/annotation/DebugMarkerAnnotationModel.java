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
package tinyOS.debug.views.annotation;

import java.io.File;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;

import tinyOS.debug.ITinyOSDebugConstants;

// Shamelessly copied from org.eclipse.cdt.debug.internal.ui.DebugMarkerAnnotationModel
public class DebugMarkerAnnotationModel extends AbstractMarkerAnnotationModel implements IBreakpointsListener {

	private File fFile;

	public DebugMarkerAnnotationModel( File file ) {
		super();
		fFile = file;
	}

	protected IMarker[] retrieveMarkers() throws CoreException {
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints( CDIDebugModel.getPluginIdentifier() );
		IMarker[] markers = new IMarker[breakpoints.length];
		for ( int i = 0; i < markers.length; ++i ) {
			markers[i] = breakpoints[i].getMarker();
		}
		return markers;
	}

	protected void deleteMarkers( IMarker[] markers ) throws CoreException {
	}

	protected void listenToMarkerChanges( boolean listen ) {
		if ( listen )
			DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener( this );
		else
			DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener( this );
	}

	protected boolean isAcceptable( IMarker marker ) {
		IBreakpoint b = DebugPlugin.getDefault().getBreakpointManager().getBreakpoint( marker );
		if ( b != null ) {
			return isAcceptable( b );
		}
		return false;
	}

	protected File getFile() {
		return fFile;
	}

	public void breakpointsAdded( IBreakpoint[] breakpoints ) {
		for ( int i = 0; i < breakpoints.length; ++i ) {
			if ( isAcceptable( breakpoints[i] ) ) {
				addMarkerAnnotation( breakpoints[i].getMarker() );
				fireModelChanged();
			}
		}
	}

	public void breakpointsRemoved( IBreakpoint[] breakpoints, IMarkerDelta[] deltas ) {
		for ( int i = 0; i < breakpoints.length; ++i ) {
			if ( isAcceptable( breakpoints[i] ) ) {
				removeMarkerAnnotation( breakpoints[i].getMarker() );
				fireModelChanged();
			}
		}
	}

	public void breakpointsChanged( IBreakpoint[] breakpoints, IMarkerDelta[] deltas ) {
		for ( int i = 0; i < breakpoints.length; ++i ) {
			if ( isAcceptable( breakpoints[i] ) ) {
				modifyMarkerAnnotation( breakpoints[i].getMarker() );
				fireModelChanged();
			}
		}
	}

	private boolean isAcceptable( IBreakpoint b ) {
		String handle = b.getMarker().getAttribute(ITinyOSDebugConstants.BREAKPOINT_SOURCE_HANDLE_ATTRIBUTE, null);
		if (handle != null) {
			File file = new File( handle );
			return file.equals( getFile() );
		}
		return false;
	}

}
