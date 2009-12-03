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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.make.MakeExclude;
import tinyos.yeti.make.dialog.AbstractMakeTargetDialogPage;
import tinyos.yeti.make.dialog.IMakeTargetInformation;
import tinyos.yeti.make.dialog.pages.CustomizationControls.Selection;
import tinyos.yeti.make.targets.MakeTargetPropertyKey;
import tinyos.yeti.make.targets.MakeTargetSkeleton;

public class ExcludePage extends AbstractMakeTargetDialogPage<MakeTargetSkeleton> implements ICustomizeablePage{
    private List list;
    
    private Button buttonAdd;
    private Button buttonEdit;
    private Button buttonDelete;
    
    private CustomizationControls customizing;
    
    public ExcludePage( boolean showCustomizing ){
        super( "Excludes" );
        if( showCustomizing ){
        	customizing = new CustomizationControls();
        	customizing.setPage( this );
        }
        setImage( NesCIcons.icons().get( NesCIcons.ICON_EXCLUDE_LIST ) );
    }

    public void setCustomEnabled( boolean enabled ){
    	list.setEnabled( enabled );
    	buttonAdd.setEnabled( enabled );
    	buttonEdit.setEnabled( enabled );
    	buttonDelete.setEnabled( enabled );
    	contentChanged();
    }
    
    public void createControl( Composite parent ){
        Composite base = new Composite( parent, SWT.NONE );
        base.setLayout( new GridLayout( 1, false ) );
        
        Composite info = new Composite( base, SWT.NONE );
        info.setLayout( new GridLayout( 1, true ) );
        info.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );
        
        String[] infos = new String[]{
                "Exclude directories from searches by the plugin or ncc.",
                "Overrides any setting made on the 'Includes'-page.",
                "Is not recursive: excluded parent does not mean excluded child.",
                "Some directories can only be excluded from the Eclipse Plugin, not from ncc" };
        for( String text : infos ){
            Label infoLabel = new Label( info, SWT.NONE );
            infoLabel.setText( text );
            infoLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        }
        
        if( customizing != null ){
        	customizing.createControl( base, true );
        	customizing.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ));
        }
        
        Composite lower = new Composite( base, SWT.NONE );
        lower.setLayout( new GridLayout( 2, false ) );
        lower.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        
        list = new List( lower, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER );
        list.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        
        Composite buttons = new Composite( lower, SWT.NONE );
        buttons.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false ) );
        buttons.setLayout( new GridLayout( 1, false ) );
        
        buttonAdd = new Button( buttons, SWT.PUSH );
        buttonAdd.setText( "Add" );
        buttonAdd.setLayoutData( new GridData( GridData.FILL, GridData.CENTER, true, false ) );
        buttonAdd.addSelectionListener( new AddAction() );
        
        buttonEdit = new Button( buttons, SWT.PUSH );
        buttonEdit.setText( "Edit" );
        buttonEdit.setLayoutData( new GridData( GridData.FILL, GridData.CENTER, true, false ) );
        buttonEdit.addSelectionListener( new EditAction() );
        
        buttonDelete = new Button( buttons, SWT.PUSH );
        buttonDelete.setText( "Delete" );
        buttonDelete.setLayoutData( new GridData( GridData.FILL, GridData.CENTER, true, false ) );
        buttonDelete.addSelectionListener( new DeleteAction() );
        
        setControl( base );
    }
    
    public void show( MakeTargetSkeleton maketarget, IMakeTargetInformation information ){
        list.removeAll();
        MakeExclude[] excludes = maketarget.getCustomExcludes();
        if( excludes != null ){
            for( MakeExclude exclude : excludes ){
                list.add( exclude.getPattern() );
            }
        }
        
        if( customizing != null ){
        	customizing.setSelection( 
        			maketarget.isUseLocalProperty( MakeTargetPropertyKey.EXCLUDES ),
        			maketarget.isUseDefaultProperty( MakeTargetPropertyKey.EXCLUDES ) );
        }
    }
    
    public void store( MakeTargetSkeleton maketarget ){
        int size = list.getItemCount();
        if( size == 0 )
            maketarget.setCustomExcludes( null );
        else{
            MakeExclude[] excludes = new MakeExclude[ size ];
            for( int i = 0; i < size; i++ ){
                excludes[i] = new MakeExclude( list.getItem( i ));
            }
            maketarget.setCustomExcludes( excludes );
        }
        
        if( customizing != null ){
        	Selection selection = customizing.getSelection();
        	maketarget.setUseLocalProperty( MakeTargetPropertyKey.EXCLUDES, selection.isLocal() );
        	maketarget.setUseDefaultProperty( MakeTargetPropertyKey.EXCLUDES, selection.isDefaults() );
        }
    }
    
    protected void performAdd(){
        String regex = edit( null );
        if( regex != null ){
            list.add( regex );
            contentChanged();
        }
    }
    
    protected void performEdit(){
        int index = list.getSelectionIndex();
        if( index >= 0 ){
            String regex = edit( list.getItem( index ));
            if( regex != null ){
                int[] selection = list.getSelectionIndices();
                list.remove( index );
                list.add( regex, index );
                if( selection != null ){
                    list.setSelection( selection );
                }
                contentChanged();
            }
        }
    }
    
    protected String edit( String regex ){
        InputDialog dialog = new InputDialog( Display.getCurrent().getActiveShell(),
                "Exclude Directories",
                "Regular expression for excluded directories.\n" +
                "See the description of 'java.util.regex.Pattern' for a full list of valid constructs.\n" +
                "Some often used constructs may be:\n" +
                "  '.':  any character\n" +
                "  'X*': X zero or more times\n" +
                "  'X+': X one or more times\n" +
                "  'X?': X once or not at all\n" +
                "  '\\':  nothing, but quotes the next character", regex, new Validator() );
        
        if( dialog.open() == Window.OK ){
            return dialog.getValue();
        }
        
        return null;
    }
    
    protected void performDelete(){
        int[] indices = list.getSelectionIndices();
        if( indices != null ){
            list.remove( indices );
            contentChanged();
        }
    }

    private class AddAction implements SelectionListener{
        public void widgetSelected( SelectionEvent e ){
            performAdd();
        }
        public void widgetDefaultSelected( SelectionEvent e ){
            performAdd();
        }
    }

    private class EditAction implements SelectionListener{
        public void widgetSelected( SelectionEvent e ){
            performEdit();
        }
        public void widgetDefaultSelected( SelectionEvent e ){
            performEdit();
        }
    }

    private class DeleteAction implements SelectionListener{
        public void widgetSelected( SelectionEvent e ){
            performDelete();
        }
        public void widgetDefaultSelected( SelectionEvent e ){
            performDelete();
        }
    }
    
    private class Validator implements IInputValidator{
        public String isValid( String newText ){
            if( newText == null || newText.length() == 0 )
                return "Missing input";
            
            if( newText.trim().length() == 0 )
                return "Pattern must contain more than whitespaces";
            
            try{
                Pattern.compile( newText );
            }
            catch( PatternSyntaxException ex ){
                ex.getMessage();
            }
            
            return null;
        }
    }
}
