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
package tinyos.yeti.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.utility.preferences.BufferedPreferenceStore;
import tinyos.yeti.utility.preferences.TextAttributeConstants;

/**
 * Allows the user to change the preferences regarding syntax highlighting
 * (colors, fonts, styles).
 * @author Benjamin Sigg
 */
public class SyntaxHighlightPage extends PreferencePage implements IWorkbenchPreferencePage{
    private List attributes;

    private FieldEditor color;
    private FieldEditor bold;
    private FieldEditor italic;

    private String currentAttribute = null;

    private BufferedPreferenceStore store;
    
    public SyntaxHighlightPage(){
        setTitle( "Syntax" );
    }
    
    @Override
    public boolean performOk(){
    	String key = currentAttribute;
    	showAttribute( null );
        store.transmit();
        showAttribute( key );
        return super.performOk();
    }
    
    @Override
    protected void performDefaults(){
        String key = currentAttribute;
        showAttribute( null );
        
        for( String change : TextAttributeConstants.ALL_KEYS ){
            toDefault( change );
        }
        
        showAttribute( key );
    }
    
    protected void toDefault( String key ){
        store.setToDefault( TextAttributeConstants.toColorKey( key ) );
        store.setToDefault( TextAttributeConstants.toStyleBoldKey( key ) );
        store.setToDefault( TextAttributeConstants.toStyleItalicKey( key ) );
    }
    
    @Override
    protected Control createContents( Composite parent ){
        Composite base = new Composite( parent, SWT.NONE );
        base.setLayout( new GridLayout( 2, false ) );

        attributes = new List( base, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL );
        attributes.setLayoutData( new GridData( SWT.FILL, SWT.FILL, false, false ) );

        for( String label : TextAttributeConstants.ALL_LABELS ){
            attributes.add( label );
        }

        Control contents = createAttributeContents( base );
        contents.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, true, true ) );

        attributes.addSelectionListener( new SelectionListener(){
            public void widgetDefaultSelected( SelectionEvent e ){
                updateSelection();
            }
            public void widgetSelected( SelectionEvent e ){
                updateSelection();
            }
        });

        attributes.select( 0 );
        updateSelection();
        
        return base;
    }

    protected Control createAttributeContents( Composite parent ){
        store = new BufferedPreferenceStore( TinyOSPlugin.getDefault().getPreferenceStore() );
        Composite base = new Composite( parent, SWT.NONE );

        color = new ColorFieldEditor( "", "Color", base );
        bold = new BooleanFieldEditor( "", "Bold", base );
        italic = new BooleanFieldEditor( "", "Italic", base );

        int columns = Math.max( color.getNumberOfControls(), Math.max( bold.getNumberOfControls(), italic.getNumberOfControls() ));

        base.setLayout( new GridLayout( columns, false ) );
        
        color.fillIntoGrid( base, columns );
        bold.fillIntoGrid( base, columns );
        italic.fillIntoGrid( base, columns );
        
        color.setPreferenceStore( store );
        bold.setPreferenceStore( store );
        italic.setPreferenceStore( store );
        
        return base;
    }

    private void updateSelection(){
        int index = attributes.getSelectionIndex();
        String key = TextAttributeConstants.ALL_KEYS[ index ];
        showAttribute( key );
    }

    protected void showAttribute( String key ){
        if( (currentAttribute == null && key != null) || !currentAttribute.equals( key )){

            if( currentAttribute != null ){
                color.store();
                bold.store();
                italic.store();
            }

            if( key != null ){
                color.setPreferenceName( TextAttributeConstants.toColorKey( key ) );
                color.load();

                bold.setPreferenceName( TextAttributeConstants.toStyleBoldKey( key ) );
                bold.load();

                italic.setPreferenceName( TextAttributeConstants.toStyleItalicKey( key ) );
                italic.load();
            }

            currentAttribute = key;
        }
    }

    public void init( IWorkbench workbench ){
        // nothing to do
    }

    @Override
    public void dispose(){
        super.dispose();

        color.store();
        color.setPreferenceStore( null );
        
        bold.store();
        bold.setPreferenceStore( null );
        
        italic.store();
        italic.setPreferenceStore( null );
    }
}
