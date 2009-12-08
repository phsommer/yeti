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
package tinyos.yeti.make.dialog.pages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import tinyos.yeti.make.dialog.AbstractMakeTargetDialogPage;
import tinyos.yeti.make.dialog.IMakeTargetInformation;
import tinyos.yeti.make.dialog.pages.CustomizationControls.Selection;
import tinyos.yeti.make.targets.MakeTargetPropertyKey;
import tinyos.yeti.make.targets.MakeTargetSkeleton;

public abstract class KeyValuePage<T> extends AbstractMakeTargetDialogPage<MakeTargetSkeleton> implements ICustomizeablePage{
    private static final String ID_ADD = "add";
    private static final String ID_EDIT = "edit";
    private static final String ID_DELETE = "delete";
    private static final String ID_UP = "up";
    private static final String ID_DOWN = "down";
    
    private Table table;
    private Buttons buttons;
    
    private CustomizationControls customization;
    
    public KeyValuePage( boolean showCustomization, String title, String message ){
        super( title );
        
        setDefaultMessage( message );
        
        if( showCustomization ){
        	customization = new CustomizationControls();
        	customization.setPage( this );
        }
    }
    
	public abstract String getNewDialogTitle();

	public abstract String getEditDialogTitle();

	public abstract String getDialogExample();

	public abstract String getValueName();

	public abstract String getKeyName();
	
	public abstract String checkValid( String[] keys, String[] values );
    
	/**
	 * Checks whether the content of this page is valid.
	 * @return an error message or <code>null</code>
	 */
	protected String checkValid(){
		int size = table.getItemCount();
		
        String[] keys = new String[ size ];
        String[] values = new String[ size ];
		
        for( int i = 0; i < size; i++ ){
            TableItem item = table.getItem( i );
            keys[i] = item.getText( 0 );
            values[i] = item.getText( 1 );
        }
        
        return checkValid( keys, values );
	}
	
	protected void recheckValid(){
		String error = checkValid();
		if( error == null )
			setDefaultMessage();
		else
			setError( error );
	}
	
    public void setCustomEnabled( boolean enabled ){
    	table.setEnabled( enabled );
    	buttons.setCustomEnabled( enabled );
    	contentChanged();
    }

    public void createControl( Composite parent ){
        Composite base = new Composite( parent, SWT.NONE );
        base.setLayout( new GridLayout( 2, false ) );
        setControl( base );
        
    	if( customization != null ){
    		customization.createControl( base, true );
    		customization.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
    	}
        
        table = new Table( base, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI );
        table.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        table.setHeaderVisible( true );
        
        TableColumn key = new TableColumn( table, SWT.LEFT );
        key.setText( getKeyName() );
        key.setResizable( true );
        key.setMoveable( false );
        key.setWidth( 100 );
        
        TableColumn value = new TableColumn( table, SWT.LEFT );
        value.setText( getValueName() );
        value.setResizable( true );
        value.setMoveable( false );
        value.setWidth( 200 );
        
        buttons = new Buttons();
        buttons.createControl( base );
        buttons.getControl().setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false ) );
    }
    
    @Override
    protected void contentChanged(){
    	recheckValid();
    	super.contentChanged();
    }
    
    protected void doAdd(){
        KeyValueDialog dialog = new KeyValueDialog( getControl().getShell(), this );
        if( dialog.open( null, null )){
            String key = dialog.getKey();
            String value = dialog.getValue();
            
            if( key.length() > 0 && value.length() > 0 ){
                TableItem item = new TableItem( table, SWT.NONE );
                item.setText( new String[]{ key, value } );
            }
            contentChanged();
        }
    }
    
    protected void doEdit(){
        int index = table.getSelectionIndex();
        if( index >= 0 ){
            TableItem item = table.getItem( index );
            KeyValueDialog dialog = new KeyValueDialog( getControl().getShell(), this );
            if( dialog.open( item.getText( 1 ), item.getText( 0 ))){
                item.setText( new String[]{ dialog.getKey(), dialog.getValue() } );
            }
            contentChanged();
        }
    }
    
    protected void doDelete(){
        int[] indices = table.getSelectionIndices();
        if( indices != null ){
            table.remove( indices );
            contentChanged();
        }
    }
    
    protected void doUp(){
        doMove( -1 );
    }
    
    protected void doDown(){
        doMove( 1 );
    }
    
    protected void doMove( int delta ){
        int index = table.getSelectionIndex();
        if( index < 0 )
            return;
        
        int next = index + delta;
        if( next < 0 || next >= table.getItemCount() )
            return;
        
        TableItem first = table.getItem( index );
        TableItem second = table.getItem( next );
        
        String[] firstTemp = new String[]{ first.getText( 0 ), first.getText( 1 ) };
        String[] secondTemp = new String[]{ second.getText( 0 ), second.getText( 1 ) };
        
        first.setText( secondTemp );
        second.setText( firstTemp );
        
        table.setSelection( next );
        contentChanged();
    }

    protected abstract String getKey( T entry );
    
    protected abstract String getValue( T entry );
    
    protected abstract MakeTargetPropertyKey<T[]> getKey();
    
    protected abstract T create( String key, String value );
    
    public void show( MakeTargetSkeleton maketarget, IMakeTargetInformation information ){
        table.removeAll();
        
        MakeTargetPropertyKey<T[]> key = getKey();
        T[] entries = maketarget.getLocalProperty( key );
        if( entries != null ){
            for( T entry : entries ){
                TableItem item = new TableItem( table, SWT.NONE );
                item.setText( new String[]{ getKey( entry ), getValue( entry ) } );
            }
        }
        
        if( customization != null ){
        	customization.setSelection(
        			maketarget.isUseLocalProperty( key ),
        			maketarget.isUseDefaultProperty( key ));
        }
        
        recheckValid();
    }

    public void store( MakeTargetSkeleton maketarget ){
        int size = table.getItemCount();

        MakeTargetPropertyKey<T[]> key = getKey();
        T[] entries = key.array( size );
        for( int i = 0; i < size; i++ ){
            TableItem item = table.getItem( i );
            entries[i] = create( item.getText( 0 ), item.getText( 1 ) );
        }
        
        maketarget.putLocalProperty( key, entries );
        
        if( customization != null ){
        	Selection selection = customization.getSelection();
        	maketarget.setUseLocalProperty( key, selection.isLocal() );
        	maketarget.setUseDefaultProperty( key, selection.isDefaults() );
        }
    }
    
    private class Buttons extends NavigationButtons{
        public Buttons(){
            super( new String[]{
                    ID_ADD, ID_EDIT, ID_DELETE, ID_UP, ID_DOWN
            }, new String[]{
                    "Add", "Edit", "Delete", "Up", "Down"
            } );
        }
        
        @Override
        protected void doOperation( String id ){
            if( ID_ADD.equals( id )){
                doAdd();
            }
            if( ID_EDIT.equals( id )){
                doEdit();
            }
            if( ID_DELETE.equals( id )){
                doDelete();
            }
            if( ID_UP.equals( id )){
                doUp();
            }
            if( ID_DOWN.equals( id )){
                doDown();
            }
        }
    }
}
