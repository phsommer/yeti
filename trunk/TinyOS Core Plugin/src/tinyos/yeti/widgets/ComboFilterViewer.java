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
package tinyos.yeti.widgets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.NesCIcons;

public class ComboFilterViewer implements IFilterViewer{
    private static final String SEARCHHISTORY = "search"; //$NON-NLS-1$

    private static final int maxNumItems = 20;
    
    protected String initialText = "";
    
    private Control control;
    private Combo filterCombo;
    
    private IAction clearTextAction;    
    private ToolBarManager filterToolBar;

    private List<FilteredTree> trees = new ArrayList<FilteredTree>();
    private FilteredTree focused = null;
    
    private boolean lazyUpdate;
    private Set<FilteredTree> updated;
    
    public ComboFilterViewer( String initialText, boolean lazyUpdate ){
        this.initialText = initialText;
        this.lazyUpdate = lazyUpdate;
        
        if( lazyUpdate )
            updated = new HashSet<FilteredTree>();
    }
    
    public Control getControl(){
        return control;
    }
    
    public void install( FilteredTree tree ){
        trees.add( tree );
        if( filterCombo != null ){
            filterCombo.getAccessible().addAccessibleListener( tree.getAccessibleListener() );
        }
    }
    
    public void uninstall( FilteredTree tree ){
        trees.remove( tree );
        if( filterCombo != null ){
            filterCombo.getAccessible().removeAccessibleListener( tree.getAccessibleListener() );
        }
        if( focused == tree )
            focused = null;
    }
    
    public FilteredTree getFocused(){
        return focused;
    }
    
    public void setFocused( FilteredTree focused ){
        this.focused = focused;
        
        if( lazyUpdate ){
            if( !updated.contains( focused )){
                updated.add( focused );
                focused.refreshFilter( true );
            }
        }
    }
    
    public void createControl( Composite parent ){
        Composite composite = new Composite( parent, SWT.NONE );
        control = composite;
        GridLayout layout = new GridLayout( 2, false );
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout( layout );
        
        createFilterSelection( composite );
        
        filterToolBar = new ToolBarManager( SWT.FLAT | SWT.HORIZONTAL );
        filterToolBar.createControl( composite );
        filterToolBar.getControl().setLayoutData( new GridData( SWT.RIGHT, SWT.CENTER, false, false ) );
        
        createToolbar( filterToolBar );
        
        clearTextAction.setEnabled( false );
        filterToolBar.update(false);
    }
    
    protected void createFilterSelection( Composite parent ){
        filterCombo = new Combo(parent, SWT.DROP_DOWN | SWT.BORDER);
        filterCombo.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        filterCombo.setText( initialText );
        filterCombo.setFont(parent.getFont());
        getPreferenceSearchHistory();
        filterCombo.addTraverseListener( new TraverseListener () {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                    /*if (getViewer().getTree().getItemCount() == 0) {
                        Display.getCurrent().beep();
                        setFilterText(""); //$NON-NLS-1$
                    } else {
                        getViewer().getTree().setFocus();
                    }
                    */
                    
                    if( focused != null )
                        focused.getViewer().getTree().setFocus();
                }
            }
        });
        filterCombo.addFocusListener(new FocusAdapter(){
            @Override
            public void focusLost(FocusEvent e) {
                String [] textValues = filterCombo.getItems();
                String newText = filterCombo.getText();

                if((newText.equals(""))||(newText.equals(initialText)))//$NON-NLS-1$
                    return;

                for (int i = 0; i < textValues.length; i++) {
                    if(textValues[i].equals(newText))
                        return;                                 
                }

                if(textValues.length >= maxNumItems)                            
                    //Discard the oldest search to get space for new search 
                    filterCombo.remove(maxNumItems-1);

                filterCombo.add(newText,0);
            }
        });
        filterCombo.addModifyListener( new ModifyListener(){
            public void modifyText( ModifyEvent e ){
                textChanged();
            }
        });
        filterCombo.addSelectionListener(new SelectionListener(){
            public void widgetSelected(SelectionEvent e) {
                textChanged();
            }
            
            public void widgetDefaultSelected( SelectionEvent e ){
                textChanged();
            }
        });

        filterCombo.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                saveDialogSettings();
            }
        });
        
        for( FilteredTree tree : trees ){
            filterCombo.getAccessible().addAccessibleListener( tree.getAccessibleListener() );
        }
    }
    
    protected void createToolbar( ToolBarManager toolbar ){
        createClearText( toolbar );
    }
    
    /**
     * Create the button that clears the text.
     * 
     * @param filterToolBar
     */
    private void createClearText(ToolBarManager filterToolBar) {
        clearTextAction = new Action("", IAction.AS_PUSH_BUTTON) {//$NON-NLS-1$
            @Override
            public void run() {
                clearText();
            }
        };

        clearTextAction.setImageDescriptor(NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_CLEAR));
        clearTextAction.setDisabledImageDescriptor(NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_CLEAR));

        filterToolBar.add(clearTextAction);
    }
    
    public String getFilterText(){
        if( filterCombo == null )
            return getInitialText();
        
        return filterCombo.getText();
    }
    
    public String getInitialText(){
        return initialText;
    }
    
    private void clearText(){
        filterCombo.setText( "" );
        textChanged();
    }
    
    private void textChanged(){
        String text = getFilterText();
        boolean initial = getInitialText().equals( text );
        
        if( text.length() > 0 && !initial ){
            clearTextAction.setEnabled( true );
        }
        else {
            // disabled toolbar is a hint that there is no text to clear
            // and the list is currently not filtered
            // filterToolBar.getControl().setVisible( preferenceFilter != null);
            clearTextAction.setEnabled( false );
        }
        
        if( lazyUpdate ){
            updated.clear();
            
            if( focused != null ){
                focused.refreshFilter( false );
                updated.add( focused );
            }
        }
        else{
            for( FilteredTree tree : trees )
                tree.refreshFilter( false );
        }
    }
    
    /**
     * Saves the search history.
     */
    private void saveDialogSettings() {   
        IDialogSettings settings =getDialogSettings();

        //If the settings contains the same key, the previous value will be replaced by new one
        settings.put(SEARCHHISTORY,filterCombo.getItems());

    }

    /**
     * Return a dialog setting section for this dialog
     */
    private IDialogSettings getDialogSettings() {
        IDialogSettings settings = TinyOSPlugin.getDefault().getDialogSettings();
        IDialogSettings thisSettings = settings.getSection(getClass().getName());
        if (thisSettings == null)
            thisSettings = settings.addNewSection(getClass().getName());
        return thisSettings;
    }

    /**
     * Get the preferences search history for this eclipse's start, 
     * Note that this history will not be cleared until this eclipse closes
     * 
     */
    public void getPreferenceSearchHistory(){           
        IDialogSettings settings = getDialogSettings();
        String[] search = settings.getArray( SEARCHHISTORY ); //$NON-NLS-1$

        if(search == null)
            return;

        for(int i = 0; i < search.length;i++){
            filterCombo.add(search[i]);
        }
    }
}
