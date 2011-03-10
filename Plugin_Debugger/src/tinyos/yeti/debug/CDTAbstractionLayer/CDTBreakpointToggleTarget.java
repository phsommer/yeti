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
package tinyos.yeti.debug.CDTAbstractionLayer;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import tinyos.yeti.debug.ITinyOSDebugConstants;
import tinyos.yeti.debug.TinyOSDebugPlugin;


public class CDTBreakpointToggleTarget implements IToggleBreakpointsTarget {
 	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleLineBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleLineBreakpoints( IWorkbenchPart part, ISelection selection ) throws CoreException {
		String errorMessage;
		if ( part instanceof ITextEditor ) {
			ITextEditor textEditor = (ITextEditor)part;
			IEditorInput input = textEditor.getEditorInput();
			if ( input == null ) {
				errorMessage = "ToggleBreakpointAdapter.Empty_editor_1";
			}
			else {
				IDocument document = textEditor.getDocumentProvider().getDocument( input );
				if ( document == null ) {
					errorMessage =  "ToggleBreakpointAdapter.Missing_document_1" ;
				}
				else {
					IResource resource = getResource( textEditor );
					if ( resource == null ) {
						errorMessage = "ToggleBreakpointAdapter.Missing_resource_1";
					}
					else {
						int lineNumber = (((ITextSelection)selection).getStartLine()) + 1;
						String sourceHandle = getSourceHandle( input );
						IBreakpoint breakpoint = CDIDebugModel.lineBreakpointExists( sourceHandle, resource, lineNumber );
						if ( breakpoint != null ) {
							DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint( breakpoint, true );
						}
						else {
							IBreakpoint b = CDIDebugModel.createLineBreakpoint( sourceHandle, 
									resource,
									ICBreakpointType.REGULAR,
									lineNumber, 
									true, 
									0, 
									"", 
									true );
							b.getMarker().setAttribute(ITinyOSDebugConstants.BREAKPOINT_SOURCE_HANDLE_ATTRIBUTE, sourceHandle);
						}
						return;

					}
				}
			}
		}
		else {
			errorMessage = "RunToLineAdapter.Operation_is_not_supported_1" ;
		}
		throw new CoreException( (IStatus) new Status( IStatus.ERROR, TinyOSDebugPlugin.getUniqueIdentifier(), ITinyOSDebugConstants.INTERNAL_ERROR, errorMessage, null ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleLineBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleLineBreakpoints( IWorkbenchPart part, ISelection selection ) {
		return ( selection instanceof ITextSelection );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void toggleMethodBreakpoints( IWorkbenchPart part, ISelection selection ) throws CoreException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleMethodBreakpoints( IWorkbenchPart part, ISelection selection ) {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	
	public void toggleWatchpoints( IWorkbenchPart part, ISelection selection ) throws CoreException {
		/*
		IVariable variable = getVariableFromSelection( part, selection );
		if ( variable != null ) {
			toggleVariableWatchpoint( part, variable );
		}
		*/
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public boolean canToggleWatchpoints( IWorkbenchPart part, ISelection selection ) {
		//return getVariableFromSelection( part, selection ) != null;
		return false;
	}

	protected static IResource getResource( IWorkbenchPart part ) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		if ( part instanceof IEditorPart ) {
			IEditorInput editorInput = ((IEditorPart)part).getEditorInput();
			IResource resource = null;
			if ( editorInput instanceof IFileEditorInput ) {
				resource = ((IFileEditorInput)editorInput).getFile();
			}
			if (resource != null)
				return resource;

			/* This file is not in a project, let default case handle it */
			ILocationProvider provider = (ILocationProvider)editorInput.getAdapter( ILocationProvider.class );
			if ( provider != null ) {
				IPath location = provider.getPath( editorInput );
				if ( location != null ) {
					IFile[] files = root.findFilesForLocation( location );
					if ( files.length > 0 )
						return files[0];
				}
			}
		}
		return root;
	}

	private String getSourceHandle( IEditorInput input ) throws CoreException {
		if ( input instanceof IFileEditorInput ) {
			return ((IFileEditorInput)input).getFile().getLocation().toOSString();
		}
		if ( input instanceof IStorageEditorInput ) {
			return ((IStorageEditorInput)input).getStorage().getFullPath().toOSString();
		}
		if ( input instanceof IPathEditorInput ) {
			return ((IPathEditorInput)input).getPath().toOSString();
		}
		if ( input instanceof IURIEditorInput)
		{
			IPath uriPath = URIUtil.toPath(((IURIEditorInput)input).getURI());
			if (uriPath != null)
				return uriPath.toOSString();
		}
		return "";
	}
}
