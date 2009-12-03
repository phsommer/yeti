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

import java.io.File;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Allows the user to create and link a new {@link IFolder}.
 * @author Benjamin Sigg
 *
 */
public class LinkFolderDialog extends TitleAreaDialog{
	private Text folderName;
	
	private Text folderPath;
	private Button browseFolderPath;
	
	private NesCPathPropertyPage page;
	private LinkedFolder result;
	
	public LinkFolderDialog( Shell parentShell, NesCPathPropertyPage page ){
		super( parentShell );
		setShellStyle( SWT.DIALOG_TRIM | SWT.RESIZE  );
		setBlockOnOpen( true );
		this.page = page;
	}
	
	public LinkedFolder openDialog(){
		result = null;
		if( open() == OK ){
			return result;
		}
		else
			return null;
	}
	
	@Override
	public void create(){
		super.create();
		
		setTitle( "Source Folder" );
		setMessage( "Link additional source to project '" + page.getProject().getName() + "'" );
	}

	@Override
	protected Control createDialogArea( Composite parent ){
		Composite center = new Composite( parent, SWT.NONE );
		center.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
		center.setLayout( new GridLayout( 1, false ) );
		
		Composite content = new Composite( center, SWT.NONE );
		content.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );
		content.setLayout( new GridLayout( 2, false ) );
		
		Label folderPathLabel = new Label( content, SWT.NONE );
		folderPathLabel.setText( "Linked folder location:" );
		folderPathLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
		
		folderPath = new Text( content, SWT.SINGLE | SWT.BORDER );
		folderPath.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 1, 1 ) );
		
		browseFolderPath = new Button( content, SWT.PUSH );
		browseFolderPath.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false, 1, 1 ) );
		browseFolderPath.setText( "Browse..." );
		
		Label folderNameLabel = new Label( content, SWT.NONE );
		folderNameLabel.setText( "Folder name:" );
		folderNameLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2,1  ) );
		
		folderName = new Text( content, SWT.SINGLE | SWT.BORDER );
		folderName.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 1, 1 ) );
	
		browseFolderPath.addSelectionListener( new SelectionListener(){
			public void widgetDefaultSelected( SelectionEvent e ){
				performBrowse();	
			}
			public void widgetSelected( SelectionEvent e ){
				performBrowse();
			}
		});
		
		folderPath.addModifyListener( new ModifyListener(){
			public void modifyText( ModifyEvent e ){
				checkCorrectness();	
			}
		});
		
		folderName.addModifyListener( new ModifyListener(){
			public void modifyText( ModifyEvent e ){
				checkCorrectness();	
			}
		});
		
		return center;
	}
	
	private void performBrowse(){
		DirectoryDialog dialog = new DirectoryDialog( getShell() );
		dialog.setFilterPath( folderPath.getText() );
		dialog.setText( "Source Folder" );
		dialog.setMessage( "Select folder with source to include." );
		String path = dialog.open();
		if( path != null ){
			folderPath.setText( path );
			folderName.setText( new Path( path ).lastSegment() );
			checkCorrectness();
		}
	}
	
	@Override
	protected void okPressed(){
		if( checkCorrectness() ){
			result = new LinkedFolder( 
					page.getProject().getFolder( folderName.getText() ),
					new Path( folderPath.getText() ));
			setReturnCode( OK );
			close();
		}
	}
	
	private boolean checkCorrectness(){
		// check folder location
		String folderPath = this.folderPath.getText();
		if( folderPath.length() == 0 ){
			setMessage( "Linked folder not specified", IMessageProvider.ERROR );
			return false;
		}
		
		File file = new File( folderPath );
		if( !file.exists() ){
			setMessage( "Linked folder does not exist", IMessageProvider.ERROR );
			return false;
		}
		
		if( !file.isDirectory() ){
			setMessage( "Linked path is not a directory", IMessageProvider.ERROR );
			return false;
		}
		
		// check folder name
		String folderName = this.folderName.getText();
		if( folderName.length() == 0 ){
			setMessage( "Folder name not specified", IMessageProvider.ERROR );
			return false;
		}
		
		IFolder folder = page.getProject().getFolder( folderName );
		if( page.getFutureMembers().folderExists( folder ) ){
			setMessage( "Folder '" + folderName + "' already exists", IMessageProvider.ERROR );
			return false;
		}
		
		setMessage( "Link additional source to project '" + page.getProject().getName() + "'" );
		return true;
	}
	
	public static class LinkedFolder {
		private IFolder folderName;
		private IPath folderPath;

		public LinkedFolder( IFolder folderName, IPath folderPath ){
			this.folderName = folderName;
			this.folderPath = folderPath;
		}
		
		public IFolder getFolderName(){
			return folderName;
		}
		
		public IPath getFolderPath(){
			return folderPath;
		}
	}
}
