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
package tinyos.yeti.environment.basic.preferences;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import tinyos.yeti.environment.basic.preferences.widgets.PlatformSelection;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.make.EnvironmentVariable;
import tinyos.yeti.make.dialog.IMakeTargetInformation;
import tinyos.yeti.make.dialog.pages.EnvironmentVariablesPage;

/**
 * A page that can be used to set the {@link EnvironmentVariable}s of some {@link IPlatform}s.
 * @author Benjamin Sigg
 */
public abstract class AbstractPlatformEnvironmentVariablesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage{
    private PlatformSelection selection;
    private EnvironmentVariablesPage variables;

    private final Object NULL_SELECTION = new Object();
    private Object currentSelection;
    private Map<Object, EnvironmentVariable[]> selections = new HashMap<Object, EnvironmentVariable[]>();

    /**
     * Gets the variables specified for <code>platform</code>. This must not
     * include variables that are specified for all platforms.
     * @param platform the platform to access, <code>null</code> if the 
     * general variables should be accessed.
     * @return the variables for <code>platform</code>, can be empty or <code>null</code>
     */
    protected abstract EnvironmentVariable[] getVariables( IPlatform platform );

    /**
     * Get the defaults values of <code>platform</code>.
     * @param platform a platform or <code>null</code> if the general variables
     * should be accessed.
     * @return the variables for <code>platform</code>, can be empty or <code>null</code>
     */
    protected abstract EnvironmentVariable[] getDefaults( IPlatform platform );

    /**
     * Connects <code>variables</code> to <code>platform</code>.
     * @param platform the platform whose variables are changed or <code>null</code>
     * if the general variables are changed.
     * @param variables the new variables, may be empty or <code>null</code>
     */
    protected abstract void setVariables( IPlatform platform, EnvironmentVariable[] variables );

    /**
     * Gets a list of all available platforms.
     * @return the list of platforms
     */
    protected abstract IPlatform[] getPlatforms();
    
    /**
     * Gets the environment for which this page is used.
     * @return the environment
     */
    protected abstract IEnvironment getEnvironment();

    public void init( IWorkbench workbench ){
        // nothing
    }

    @Override
    public boolean performOk(){
        if( !super.performOk() )
            return false;

        storeCurrent();

        setVariables( null, selections.get( NULL_SELECTION ) );
        for( IPlatform platform : getPlatforms() ){
            setVariables( platform, selections.get( platform ) );
        }

        return true;
    }

    @Override
    protected void performDefaults(){
        super.performDefaults();

        IPlatform platform = null;
        if( currentSelection != NULL_SELECTION )
            platform = (IPlatform)currentSelection;

        selections.put( currentSelection, getDefaults( platform ) );
        showCurrent();
    }

    private void storeCurrent(){
        if( currentSelection != null ){
            EnvironmentVariable[] entries = variables.getEntries();
            selections.put( currentSelection, entries );
        }
    }

    private void showCurrent(){
        currentSelection = selection.getChoice();
        if( currentSelection == null )
            currentSelection = NULL_SELECTION;

        EnvironmentVariable[] variables = selections.get( currentSelection );

        if( variables == null )
            variables = new EnvironmentVariable[]{};

        this.variables.show( variables, new Information() );
    }
    
    @Override
    protected Control createContents( Composite parent ){
        Composite base = new Composite( parent, SWT.NONE );
        base.setLayout( new GridLayout( 1, false ) );

        Composite top = new Composite( base, SWT.NONE );
        top.setLayout( new GridLayout( 2, false ) );
        top.setLayoutData( new GridData( SWT.LEFT, SWT.FILL, true, false ) );

        Label selectionLabel = new Label( top, SWT.NONE );
        selectionLabel.setText( "Platform: " );
        selectionLabel.setToolTipText( "The platform whose environment variables should be displayed" );
        selectionLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false ) );

        selection = new PlatformSelection(){
            @Override
            protected void changed( IPlatform newChoice ){
                storeCurrent();
                showCurrent();
            }
        };
        selection.createControl( top );
        selection.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

        variables = new EnvironmentVariablesPage( false );
        variables.createControl( base );
        variables.getControl().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        IPlatform[] platforms = getPlatforms();
        selections.put( NULL_SELECTION, getVariables( null ) );
        for( IPlatform platform : platforms ){
            selections.put( platform, getVariables( platform ) );
        }

        selection.setChoices( platforms );
        showCurrent();

        return base;
    }
    
    private class Information implements IMakeTargetInformation{
        public IEnvironment getEnvironment() {
            return AbstractPlatformEnvironmentVariablesPreferencePage.this.getEnvironment();
        }

        public IPlatform[] getPlatforms() {
            // not needed
            return new IPlatform[]{};
        }

        public IPlatform getSelectedPlatform() {
            // not needed
            return null;
        }        
    }
}
