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
package tinyos.yeti.properties.source;

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;
import org.eclipse.ui.dialogs.PropertyPage;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.model.NesCPath;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.properties.source.LinkFolderDialog.LinkedFolder;

public class NesCPathPropertyPage extends PropertyPage implements IWorkbenchPropertyPage{
	private List listOfFolders;
	private FutureMemberSet futureMembers;
	private NesCPath paths;
	
	private Button addButton;
	private Button linkButton;
	private Button removeButton;
	
	private CheckedTreeSelectionDialog dialog;
	private LinkFolderDialog linkDialog;
	
	public NesCPathPropertyPage(){
		noDefaultAndApplyButton();
	}
	
	@Override
	protected Control createContents( Composite parent ){
		Composite content = new Composite( parent, SWT.NONE );
		content.setLayout( new GridLayout( 2, false ) );
		content.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
		
		Label info = new Label( content, SWT.NONE );
		info.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 2, 1 ) );
		info.setText( "Source folders" );
		
		listOfFolders = new List( content, SWT.MULTI | SWT.BORDER );
		listOfFolders.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
		
		Composite buttons = new Composite( content, SWT.NONE );
		buttons.setLayoutData( new GridData( SWT.CENTER, SWT.TOP, false, false ) );
		buttons.setLayout( new GridLayout( 1, false ) );
		
		addButton = new Button( buttons, SWT.PUSH );
		addButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
		addButton.setText( "Add Folder..." );
		linkButton = new Button( buttons, SWT.PUSH );
		linkButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
		linkButton.setText( "Link Source..." );
		removeButton = new Button( buttons, SWT.PUSH );
		removeButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
		removeButton.setText( "Remove" );
		
		addButton.addSelectionListener( new SelectionListener(){
			public void widgetDefaultSelected( SelectionEvent e ){
				performAdd();
			}
			public void widgetSelected( SelectionEvent e ){
				performAdd();	
			}
		});
		
		linkButton.addSelectionListener( new SelectionListener(){
			public void widgetDefaultSelected( SelectionEvent e ){
				performLink();
			}
			public void widgetSelected( SelectionEvent e ){
				performLink();
			}
		});
		
		removeButton.addSelectionListener( new SelectionListener(){
			public void widgetDefaultSelected( SelectionEvent e ){
				performRemove();
			}
			public void widgetSelected( SelectionEvent e ){
				performRemove();
			}
		});
		
		
		listOfFolders.addSelectionListener( new SelectionListener(){
			public void widgetDefaultSelected( SelectionEvent e ){
				updateButtons();
			}
			public void widgetSelected( SelectionEvent e ){
				updateButtons();
			}
		});
		
		fill();
		updateButtons();
		
		return content;
	}
	
	private void fill(){
		NesCPath paths = getPaths();
		listOfFolders.removeAll();
		futureMembers = new FutureMemberSet( paths );
		
		IFolder[] folders = paths.getSourceFolders();
		if( folders != null ){
			for( IFolder folder : folders ){
				if( folder.isLinked() ){
					listOfFolders.add( folder.getProjectRelativePath().toOSString() + " - " + folder.getLocation().toOSString() );
				}
				else{
					listOfFolders.add( folder.getProjectRelativePath().toOSString() );
				}
			}
		}
	}
	
	private void addToListOfFolders( IFolder folder, IPath path ){
		if( path == null ){
			if( futureMembers.addSourceFolder( folder ) ){
				listOfFolders.add( folder.getProjectRelativePath().toOSString() );
			}
		}
		else{
			if( futureMembers.linkSourceFolder( folder, path ) ){
				listOfFolders.add( folder.getProjectRelativePath().toOSString() + " - " + path.toOSString() );
			}
		}
	}
	
	protected void performAdd(){
		CheckedTreeSelectionDialog dialog = getDialog();
		dialog.setBlockOnOpen( true );
		int result = dialog.open();
		if( CheckedTreeSelectionDialog.OK == result ){
			Object[] selection = dialog.getResult();
			if( selection != null ){
				// ensure nothing added twice...
				for( Object selected : selection ){
					IFolder folder = (IFolder)selected;
					addToListOfFolders( folder, null );
				}
			}
		}
		updateButtons();
		checkCorrectness();
	}
	
	protected void performLink(){
		LinkFolderDialog linkDialog = getLinkDialog();
		LinkedFolder folder = linkDialog.openDialog();
		if( folder != null ){
			addToListOfFolders( folder.getFolderName(), folder.getFolderPath() );
		}
		checkCorrectness();
	}
	
	protected void performRemove(){
		int[] selection = listOfFolders.getSelectionIndices();
		listOfFolders.remove( selection );
		futureMembers.remove( selection );
		checkCorrectness();
		updateButtons();
	}
	
	private void updateButtons(){
		int[] selection = listOfFolders.getSelectionIndices();
		addButton.setEnabled( true );
		removeButton.setEnabled( selection.length > 0 );
	}
	
	private CheckedTreeSelectionDialog getDialog(){
		if( dialog == null ){
			ContentProvider provider = new ContentProvider();
			dialog = new CheckedTreeSelectionDialog( getControl().getShell(), provider, provider );
			dialog.setInput( getElement() );
		}
		return dialog;
	}
	
	private LinkFolderDialog getLinkDialog(){
		if( linkDialog == null ){
			linkDialog = new LinkFolderDialog( getControl().getShell(), this );
		}
		return linkDialog;
	}
	
	public IProject getProject(){
		return ((IResource)getElement().getAdapter( IResource.class )).getProject();	
	}
	
	private NesCPath getPaths(){
		if( paths != null )
			return paths;
		
		try{
		    IProject project = getProject();
		    ProjectTOS tos = TinyOSPlugin.getDefault().getProjectTOS( project );
		    paths = tos.getPaths();
		    return paths;
		}
		catch( MissingNatureException ex ){
			TinyOSPlugin.log( ex );
			return null;
		}
	}
	
	@Override
	public boolean performOk(){
		if( !checkCorrectness() )
			return false;
		
		futureMembers.close();
		
		return true;
	}
	
	public FutureMemberSet getFutureMembers(){
		return futureMembers;
	}
	
	private boolean checkCorrectness(){
		String message = futureMembers.getErrorMessage();
		if( message != null ){
			setMessage( message, IMessageProvider.ERROR );
			return false;
		}
		
		message = futureMembers.getWarningMessage();
		if( message != null ){
			setMessage( message, IMessageProvider.WARNING );
			return true;
		}
		
		setMessage( null, NONE );
		return true;
	}
	
	private class ContentProvider implements ILabelProvider, ITreeContentProvider{
		public Image getImage( Object element ){
			return null;
		}

		public String getText( Object element ){
			return ((IResource)element).getName();
		}

		public void addListener( ILabelProviderListener listener ){
			// ignore
		}

		public void dispose(){
			// ignore
		}

		public boolean isLabelProperty( Object element, String property ){
			return false;
		}

		public void removeListener( ILabelProviderListener listener ){
			// ignore
		}

		public Object[] getChildren( Object parentElement ){
			try{
				IResource[] members = ((IContainer)parentElement).members();
				java.util.List<IResource> result = new ArrayList<IResource>();
				
				for( IResource child : members ){
					if( child instanceof IContainer ){
						result.add( child );
					}
				}
				
				return result.toArray();
			}
			catch( CoreException ex ){
				TinyOSPlugin.log( ex );
				return new Object[]{};
			}
		}

		public Object getParent( Object element ){
			return ((IResource)element).getParent();
		}

		public boolean hasChildren( Object element ){
			return true;
		}

		public Object[] getElements( Object inputElement ){
			return getChildren( inputElement );
		}

		public void inputChanged( Viewer viewer, Object oldInput, Object newInput ){
			viewer.refresh();
		}
	}
}
