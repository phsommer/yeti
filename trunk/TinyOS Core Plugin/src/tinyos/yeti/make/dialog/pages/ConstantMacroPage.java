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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import tinyos.yeti.ep.parser.macros.ConstantMacro;
import tinyos.yeti.make.dialog.AbstractMakeTargetDialogPage;
import tinyos.yeti.make.dialog.IMakeTargetInformation;
import tinyos.yeti.make.dialog.pages.CustomizationControls.Selection;
import tinyos.yeti.make.targets.MakeTargetPropertyKey;
import tinyos.yeti.make.targets.MakeTargetSkeleton;

public class ConstantMacroPage extends AbstractMakeTargetDialogPage<MakeTargetSkeleton> implements ICustomizeablePage{
    private org.eclipse.swt.widgets.List list;
    private List<ConstantMacro> macros = new LinkedList<ConstantMacro>();
    
    private Button buttonAdd;
    private Button buttonEdit;
    private Button buttonDelete;
    
    private CustomizationControls customization;
    
    public ConstantMacroPage( boolean showCustomization ){
        super( "Macros" );
        setDefaultMessage( "Macros which are applied to any file that gets parsed by Eclipse. Note that ncc will not be affected by these settings." );
        if( showCustomization ){
        	customization = new CustomizationControls();
        	customization.setPage( this );
        }
    }
    
    public void setCustomEnabled( boolean enabled ){
	    list.setEnabled( enabled );
	    buttonAdd.setEnabled( enabled );
	    buttonEdit.setEnabled( enabled );
	    buttonDelete.setEnabled( enabled );
	    contentChanged();
    }
    
    public void show( MakeTargetSkeleton maketarget, IMakeTargetInformation information ){
        list.removeAll();
        macros.clear();
        
        ConstantMacro[] macros = maketarget.getCustomMacros();
        if( macros != null ){
            for( ConstantMacro macro : macros ){
                list.add( toString( macro ) );
                this.macros.add( macro );
            }
        }
        
        if( customization != null ){
        	customization.setSelection( 
        			maketarget.isUseLocalProperty( MakeTargetPropertyKey.MACROS ),
        			maketarget.isUseDefaultProperty( MakeTargetPropertyKey.MACROS ) );
        }
    }

    public void store( MakeTargetSkeleton maketarget ){
        maketarget.setCustomMacros( macros.toArray( new ConstantMacro[ macros.size() ] ) );
        
        if( customization != null ){
        	Selection selection = customization.getSelection();
        	maketarget.setUseLocalProperty( MakeTargetPropertyKey.MACROS, selection.isLocal() );
        	maketarget.setUseDefaultProperty( MakeTargetPropertyKey.MACROS, selection.isDefaults() );
        }
    }
    
    private String toString( ConstantMacro macro ){
        return macro.getName() + ": " + macro.getConstant();
    }
    
    private void handleAdd(){
        ConstantMacroDialog dialog = new ConstantMacroDialog( getControl().getShell() );
        ConstantMacro macro = dialog.open( null );
        if( macro != null ){
            list.add( toString( macro ));
            macros.add( macro );
            contentChanged();
        }
    }
    
    private void handleEdit(){
        int index = list.getSelectionIndex();
        if( index >= 0 ){
            ConstantMacroDialog dialog = new ConstantMacroDialog( getControl().getShell() );
            ConstantMacro macro = dialog.open( macros.get( index ) );
            if( macro != null ){
                list.setItem( index, toString( macro ) );
                macros.set( index, macro );
                contentChanged();
            }
        }
    }
    
    private void handleDelete(){
        int index = list.getSelectionIndex();
        if( index >= 0 ){
            list.remove( index );
            macros.remove( index );
            contentChanged();
        }
    }
    
    public void createControl( Composite parent ){
        Composite pane = new Composite( parent, SWT.NONE );
        setControl( pane );
        pane.setLayout( new GridLayout( 2, false ) );
        
        Label description = new Label( pane, SWT.NONE );
        description.setText( "Macros used in each file" );
        description.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
        
        if( customization != null ){
        	customization.createControl( pane, true );
        	customization.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
        }
        
        list = new org.eclipse.swt.widgets.List( pane, SWT.BORDER | SWT.SINGLE );
        list.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 1, 1 ) );
        
        Composite buttons = new Composite( pane, SWT.NONE );
        buttons.setLayout( new GridLayout( 1, true ) );
        buttons.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false, 1, 1 ) );
        
        buttonAdd = new Button( buttons, SWT.PUSH );
        buttonAdd.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        buttonAdd.setText( "Add" );
        
        buttonEdit = new Button( buttons, SWT.PUSH );
        buttonEdit.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        buttonEdit.setText( "Edit" );
        
        buttonDelete = new Button( buttons, SWT.PUSH );
        buttonDelete.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        buttonDelete.setText( "Delete" );
        
        buttonAdd.addSelectionListener( new SelectionListener(){
            public void widgetSelected( SelectionEvent e ){
                handleAdd();
            }
            public void widgetDefaultSelected( SelectionEvent e ){
                handleAdd();
            }
        });
        
        buttonEdit.addSelectionListener( new SelectionListener(){
            public void widgetSelected( SelectionEvent e ){
                handleEdit();
            }
            public void widgetDefaultSelected( SelectionEvent e ){
                handleEdit();
            }
        });
        
        buttonDelete.addSelectionListener( new SelectionListener(){
            public void widgetSelected( SelectionEvent e ){
                handleDelete();
            }
            public void widgetDefaultSelected( SelectionEvent e ){
                handleDelete();
            }
        });
    }   
}
