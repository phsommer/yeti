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
package tinyos.yeti.environment.unix2.validation;

import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;


public class DifferenceListDialog{
	private String[] keys;
	private Map<String,String> envp;
	private Map<String,String> shell;
	
	private Button doNotAskAgainEverButton;
	private boolean doNotAskAgainEver = false;
	
	private Button doNotAskAgainSessionButton;
	private boolean doNotAskAgainSession = false;
	
	/*
	 *  When comparing the environment variables of Eclipse and bash, different values
	 *  were found. These variables are required for building and debugging 
	 *  TinyOS applications. Different values may lead to unexpected behavior,
	 *  or even failure. To minimize the differences start Eclipse as a sub-process
	 *  of bash.
	 *  
	 *  Below is a list of variables with different values:
	 *   
	 *  [] Do not ask again
	 *  [] Do not ask again in this session
	 *  
	 *  Would you like to continue despite these warnings?
	 *  							Continue	Cancel
	 * 
	 */
	
	public boolean open( String[] keys, Map<String,String> envp, Map<String,String> shell ){
		this.keys = keys;
		this.envp = envp;
		this.shell = shell;
		ListDialog dialog = new ListDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
		int result = dialog.open();
		return result == Dialog.OK;
	}
	
	private void createExplanation( Composite parent ){
		Group group = new Group( parent, SWT.BORDER );
		group.setText( "Explanation" );
		group.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
		group.setLayout( new GridLayout( 1, false ) );
		
		Label text = new Label( group,  SWT.WRAP );
		text.setText( 
				"When comparing the environment variables known to Eclipse and used in the shell, different values were found. " +
				"Some of these variables may be required for building and debugging TinyOS applications, " +
				"different values can lead to unexpected behavior or even failure.\n" +
				"To minimize the differences start Eclipse as a sub-process of the shell (i.e. open the command line, navigate to the directory in which " +
				"Eclipse is installed, and enter './eclipse')." );
		text.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
	}
	
	private void createTable( Composite parent ){
		Group group = new Group( parent, SWT.BORDER );
		group.setText( "List of variables with different values" );
		group.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
		group.setLayout( new GridLayout( 1, false ) );
		
		Table table = new Table( group, SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY | SWT.BORDER );
		table.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
		table.setHeaderVisible( true );
		
		TableColumn keyColumn = new TableColumn( table, SWT.NONE );
		keyColumn.setText( "Variable" );
		keyColumn.setWidth( 200 );
		
		TableColumn envpColumn = new TableColumn( table, SWT.NONE );
		envpColumn.setText( "Eclipse" );
		envpColumn.setWidth( 200 );
		
		TableColumn shellColumn = new TableColumn( table, SWT.NONE );
		shellColumn.setText( "Shell" );
		shellColumn.setWidth( 200 );
		
		for( String key : keys ){
			TableItem item= new TableItem( table, SWT.NONE );
			item.setText( new String[]{ key, valueOf( envp.get( key ) ), valueOf( shell.get( key )) } );
		}
	}
	
	private void createQuestions( Composite parent ){
		Group group = new Group( parent, SWT.BORDER );
		group.setText( "Future" );
		group.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );
		group.setLayout( new GridLayout( 1, false ) );
		
		doNotAskAgainSessionButton = new Button( group, SWT.CHECK );
		doNotAskAgainSessionButton.setText( "Do not check again in this session" );
		doNotAskAgainSessionButton.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );
		
		doNotAskAgainEverButton = new Button( group, SWT.CHECK );
		doNotAskAgainEverButton.setText( "Do not check again" );
		doNotAskAgainEverButton.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );
		
		doNotAskAgainSessionButton.addSelectionListener( new SelectionListener(){
			public void widgetSelected( SelectionEvent e ){
				doNotAskAgainSession = doNotAskAgainSessionButton.getSelection();
			}
			public void widgetDefaultSelected( SelectionEvent e ){
				doNotAskAgainSession = doNotAskAgainSessionButton.getSelection();
			}
		});
		
		doNotAskAgainEverButton.addSelectionListener( new SelectionListener(){
			public void widgetDefaultSelected( SelectionEvent e ){
				doNotAskAgainEver = doNotAskAgainEverButton.getSelection();
			}
			public void widgetSelected( SelectionEvent e ){
				doNotAskAgainEver = doNotAskAgainEverButton.getSelection();
			}
		});
	}
	
	private String valueOf( String string ){
		if( string == null )
			return "";
		return string;
	}
	
	public boolean isDoNotAskAgainEver(){
		return doNotAskAgainEver;
	}
	
	public boolean isDoNotAskAgainSession(){
		return doNotAskAgainSession;
	}
	
	private class ListDialog extends Dialog{
		public ListDialog( Shell parent ){
			super( parent );
			setBlockOnOpen( true );
		}
		
		@Override
		protected boolean isResizable(){
			return true;
		}
		
		@Override
		protected void configureShell( Shell newShell ){
			super.configureShell( newShell );
			newShell.setText( "Environment variables" );
		}
		
		@Override
		protected Control createDialogArea( Composite parent ){
			Composite result = (Composite)super.createDialogArea( parent );
			
			createExplanation( result );
			createTable( result );
			createQuestions( result );
			
			return result;
		}
	}
}
