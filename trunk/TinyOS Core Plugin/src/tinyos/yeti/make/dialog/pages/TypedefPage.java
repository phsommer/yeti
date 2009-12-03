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

import tinyos.yeti.make.MakeTypedef;
import tinyos.yeti.make.dialog.AbstractMakeTargetDialogPage;
import tinyos.yeti.make.dialog.IMakeTargetInformation;
import tinyos.yeti.make.dialog.pages.CustomizationControls.Selection;
import tinyos.yeti.make.targets.MakeTargetPropertyKey;
import tinyos.yeti.make.targets.MakeTargetSkeleton;

public class TypedefPage extends AbstractMakeTargetDialogPage<MakeTargetSkeleton> implements ICustomizeablePage{
    private static final String ID_ADD = "add";
    private static final String ID_EDIT = "edit";
    private static final String ID_DELETE = "delete";
    private static final String ID_UP = "up";
    private static final String ID_DOWN = "down";
    
    private Table table;
    private Buttons buttons;
    
    private CustomizationControls customization;
    
    public TypedefPage( boolean showCustomization ){
        super( "Typedefs" );
        
        setDefaultMessage( "Types which are used in any file of this plugin, but are not forwarded to 'ncc'" );
        
        if( showCustomization ){
        	customization = new CustomizationControls();
        	customization.setPage( this );
        }
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
        
        TableColumn name = new TableColumn( table, SWT.LEFT );
        name.setText( "Name" );
        name.setResizable( true );
        name.setMoveable( false );
        name.setWidth( 100 );
        
        TableColumn type = new TableColumn( table, SWT.LEFT );
        type.setText( "Type" );
        type.setResizable( true );
        type.setMoveable( false );
        type.setWidth( 200 );
        
        buttons = new Buttons();
        buttons.createControl( base );
        buttons.getControl().setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false ) );
    }
    
    protected void doAdd(){
        TypedefDialog dialog = new TypedefDialog( getControl().getShell() );
        if( dialog.open( null, null )){
            String name = dialog.getName();
            String type = dialog.getType();
            
            if( name.length() > 0 && type.length() > 0 ){
                TableItem item = new TableItem( table, SWT.NONE );
                item.setText( new String[]{ name, type } );
            }
            contentChanged();
        }
    }
    
    protected void doEdit(){
        int index = table.getSelectionIndex();
        if( index >= 0 ){
            TableItem item = table.getItem( index );
            TypedefDialog dialog = new TypedefDialog( getControl().getShell() );
            if( dialog.open( item.getText( 1 ), item.getText( 0 ))){
                item.setText( new String[]{ dialog.getName(), dialog.getType() } );
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

    public void show( MakeTargetSkeleton maketarget, IMakeTargetInformation information ){
        table.removeAll();
        
        MakeTypedef[] typedefs = maketarget.getCustomTypedefs();
        if( typedefs != null ){
            for( MakeTypedef typedef : typedefs ){
                TableItem item = new TableItem( table, SWT.NONE );
                item.setText( new String[]{ typedef.getName(), typedef.getType() } );
            }
        }
        
        if( customization != null ){
        	customization.setSelection(
        			maketarget.isUseLocalProperty( MakeTargetPropertyKey.TYPEDEFS ),
        			maketarget.isUseDefaultProperty( MakeTargetPropertyKey.TYPEDEFS ));
        }
    }

    public void store( MakeTargetSkeleton maketarget ){
        int size = table.getItemCount();
        MakeTypedef[] typedefs = new MakeTypedef[ size ];
        for( int i = 0; i < size; i++ ){
            TableItem item = table.getItem( i );
            typedefs[i] = new MakeTypedef( item.getText( 1 ), item.getText( 0 ) );
        }
        
        maketarget.setCustomTypedefs( typedefs );
        
        if( customization != null ){
        	Selection selection = customization.getSelection();
        	maketarget.setUseLocalProperty( MakeTargetPropertyKey.TYPEDEFS, selection.isLocal() );
        	maketarget.setUseDefaultProperty( MakeTargetPropertyKey.TYPEDEFS, selection.isDefaults() );
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
