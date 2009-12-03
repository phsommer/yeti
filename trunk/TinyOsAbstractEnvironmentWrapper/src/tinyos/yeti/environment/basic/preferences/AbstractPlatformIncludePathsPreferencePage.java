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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import tinyos.yeti.environment.basic.preferences.widgets.PlatformSelection;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.make.MakeInclude;
import tinyos.yeti.make.MakeInclude.Include;
import tinyos.yeti.make.dialog.IMakeTargetInformation;
import tinyos.yeti.make.dialog.pages.IncludePage;
import tinyos.yeti.utility.XReadStack;
import tinyos.yeti.utility.XWriteStack;

/**
 * A page that can be used to set the {@link MakeInclude}s of some {@link IPlatform}s.
 * @author Benjamin Sigg
 */
public abstract class AbstractPlatformIncludePathsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage{
    private PlatformSelection selection;
    private IncludePage includes;

    private final Object NULL_SELECTION = new Object();
    private Object currentSelection;
    private Map<Object, MakeInclude[]> selections = new HashMap<Object, MakeInclude[]>();

    /**
     * Gets the includes specified for <code>platform</code>. This must not
     * include paths that are specified for all platforms.
     * @param platform the platform to access, <code>null</code> if the 
     * general paths should be accessed.
     * @return the paths for <code>platform</code>, can be empty or <code>null</code>
     */
    protected abstract MakeInclude[] getIncludes( IPlatform platform );

    /**
     * Get the defaults values of <code>platform</code>.
     * @param platform a platform or <code>null</code> if the general paths
     * should be accessed.
     * @return the paths for <code>platform</code>, can be emtpy or <code>null</code>
     */
    protected abstract MakeInclude[] getDefaults( IPlatform platform );

    /**
     * Connects <code>includes</code> to <code>platform</code>.
     * @param platform the platform whose includes are changed or <code>null</code>
     * if the general paths are changed.
     * @param includes the new paths, may be empty or <code>null</code>
     */
    protected abstract void setIncludes( IPlatform platform, MakeInclude[] includes );

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

    protected void performExport(){
        FileDialog dialog = new FileDialog( getControl().getShell(), SWT.SAVE );
        dialog.setFilterExtensions( new String[]{ "*.xml" } );
        dialog.setFilterNames( new String[]{ "Platform Paths (*.xml)" } );
        String path = dialog.open();
        if( path != null ){
            if( !path.endsWith( ".xml" ))
                path += ".xml";
            
            storeCurrent();

            XWriteStack xml = XWriteStack.open();
            if( xml != null ){
                xml.push( "paths" );
                xml.push( "platform" );
                performExport( xml, selections.get( NULL_SELECTION ) );
                xml.pop();

                for( IPlatform platform : getPlatforms() ){
                    xml.push( "platform" );
                    xml.setAttribute( "name", platform.getName() );
                    performExport( xml, selections.get( platform ) );
                    xml.pop();
                }

                xml.pop();

                xml.write( new File( path ) );
            }
        }
    }

    private void performExport( XWriteStack xml, MakeInclude[] includes ){
        if( includes != null ){
            for( MakeInclude include : includes ){
                xml.push( "include" );
                xml.setAttribute( "recursive", String.valueOf( include.isRecursive() ) );
                // xml.setAttribute( "type", MakeInclude.type( include.getType() ));
                xml.setAttribute( "ncc", String.valueOf( include.isNcc() ) );
                xml.setAttribute( "global", String.valueOf( include.isGlobal() ) );
                xml.setAttribute( "include", MakeInclude.include( include.getInclude() ) );
                xml.setText( include.getPath() );
                xml.pop();
            }
        }
    }

    protected void performImport(){
        FileDialog dialog = new FileDialog( getControl().getShell(), SWT.OPEN );
        dialog.setFilterExtensions( new String[]{ "*.xml" } );
        dialog.setFilterNames( new String[]{ "Platform Paths (*.xml)" } );
        String path = dialog.open();
        if( path != null ){
            XReadStack xml = XReadStack.open( new File( path ));
            if( xml != null ){
                if( xml.search( "paths" ) ){
                    Map<String, IPlatform> platforms = new HashMap<String, IPlatform>();
                    for( IPlatform platform : getPlatforms() ){
                        platforms.put( platform.getName(), platform );
                    }
                    
                    while( xml.hasNext( "platform" )){
                        xml.next( "platform" );
                        String name = xml.getAttribute( "name" );
                        IPlatform platform = null;
                        if( name != null )
                            platform = platforms.get( name );
                        
                        if( platform != null || name == null ){
                            MakeInclude[] includes = performImport( xml );
                            if( platform == null )
                                selections.put( NULL_SELECTION, includes );
                            else
                                selections.put( platform, includes );
                        }
                        
                        xml.pop();
                    }
                }
                
                showCurrent();
            }
        }
    }

    private MakeInclude[] performImport( XReadStack xml ){
        List<MakeInclude> result = new ArrayList<MakeInclude>();
        while( xml.hasNext( "include" )){
            xml.next( "include" );
            boolean recursive = Boolean.parseBoolean( xml.getAttribute( "recursive" ) );
            boolean ncc = false;
            boolean global = false;
            Include include = Include.NONE;
            String type = xml.getAttribute( "type" );
            if( type == null ){
            	ncc = xml.getBoolean( "ncc", false );
            	global = xml.getBoolean( "global", false );
            	include = MakeInclude.include( xml.getString( "include", MakeInclude.include( Include.NONE ) ) );
            }
            else{
            	if( "source".equals( type )){
            		ncc = true;
            		include = Include.SOURCE;
            	}
            	else if( "system".equals( type )){
            		include = Include.SYSTEM;
            	}
            	else if( "global".equals( type )){
            		global = true;
            	}
            }
            String path = xml.getText();
            result.add( new MakeInclude( path, include, recursive, ncc, global ) );
            xml.pop();
        }
        return result.toArray( new MakeInclude[ result.size() ] );
    }

    public void init( IWorkbench workbench ){
        // nothing
    }

    @Override
    public boolean performOk(){
        if( !super.performOk() )
            return false;

        storeCurrent();

        setIncludes( null, selections.get( NULL_SELECTION ) );
        for( IPlatform platform : getPlatforms() ){
            setIncludes( platform, selections.get( platform ) );
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
            List<MakeInclude> list = includes.listIncludes();
            selections.put( currentSelection, list.toArray( new MakeInclude[ list.size() ] ) );
        }
    }

    private void showCurrent(){
        currentSelection = selection.getChoice();
        if( currentSelection == null )
            currentSelection = NULL_SELECTION;

        MakeInclude[] includes = selections.get( currentSelection );

        if( includes == null )
            includes = new MakeInclude[]{};

        this.includes.show( includes, new Information() );
    }
    
    @Override
    protected void contributeButtons( Composite parent ){
        super.contributeButtons( parent );
        
        ((GridLayout) parent.getLayout()).numColumns += 2;
        
        Button buttonImport = new Button( parent, SWT.PUSH );
        buttonImport.setText( "Import" );
        buttonImport.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        buttonImport.addSelectionListener( new SelectionListener(){
            public void widgetDefaultSelected( SelectionEvent e ){
                performImport();
            }
            public void widgetSelected( SelectionEvent e ){
                performImport();
            }
        });
        
        Button buttonExport = new Button( parent, SWT.PUSH );
        buttonExport.setText( "Export" );
        buttonExport.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        buttonExport.addSelectionListener( new SelectionListener(){
            public void widgetDefaultSelected( SelectionEvent e ){
                performExport();
            }
            public void widgetSelected( SelectionEvent e ){
                performExport();
            }
        });
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
        selectionLabel.setToolTipText( "The platform whose paths should be displayed" );
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

        includes = new IncludePage( false, false, false );
        includes.createControl( base );
        includes.getControl().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        IPlatform[] platforms = getPlatforms();
        selections.put( NULL_SELECTION, getIncludes( null ) );
        for( IPlatform platform : platforms ){
            selections.put( platform, getIncludes( platform ) );
        }

        selection.setChoices( platforms );
        showCurrent();

        return base;
    }
    
    private class Information implements IMakeTargetInformation{
        public IEnvironment getEnvironment() {
            return AbstractPlatformIncludePathsPreferencePage.this.getEnvironment();
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
