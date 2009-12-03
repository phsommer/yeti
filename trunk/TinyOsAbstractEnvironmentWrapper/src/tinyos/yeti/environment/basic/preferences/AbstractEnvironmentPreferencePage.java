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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.progress.UIJob;

import tinyos.yeti.environment.basic.TinyOSAbstractEnvironmentPlugin;
import tinyos.yeti.environment.basic.path.AbstractPathManager;
import tinyos.yeti.environment.basic.path.IPathTranslator;
import tinyos.yeti.environment.basic.preferences.widgets.ArchitecturePathTable;
import tinyos.yeti.environment.basic.preferences.widgets.ComboFieldEditor;
import tinyos.yeti.environment.basic.preferences.widgets.PathTranslatingDirectoryFieldEditor;
import tinyos.yeti.environment.basic.preferences.widgets.PathTranslatingFileFieldEditor;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.widgets.helper.MultiProgressMonitor;
import tinyos.yeti.widgets.helper.ProgressBarMonitor;

/**
 * A page on which different paths are shown, the user can change those paths.
 * The paths include TOSROOT, TOSDIR, TOSAPPS, MAKERULES and the architectures.
 * @author Benjamin Sigg
 */
public abstract class AbstractEnvironmentPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage{
    public static enum StoreKey{
        TOSROOT, TOSDIR, TOSAPPS, MAKERULES, GCC_INCLUDES, TREE
    }

    private final String NULL_ARCHITECTURE = "<default>";

    private ComboFieldEditor tree;
    private DirectoryFieldEditor tosroot;
    private String tosrootOldValue;
    private DirectoryFieldEditor tosdir;
    private DirectoryFieldEditor tosapps;
    private FileFieldEditor makerules;
    private StringFieldEditor gccIncludes;

    private ArchitecturePathTable architectures;

    private ArchitectureJob architectureJob = new ArchitectureJob();

    private ProgressBar progress;
    private Composite base;

    /** whether this page has been initialized */
    private boolean initialized = false;
    /** whether the {@link #architectureJob} has been scheduled at least once */
    private boolean scheduled = false;

    public AbstractEnvironmentPreferencePage(){
        super( GRID );
    }
    
    public boolean isDisposed(){
    	return base.isDisposed();
    }

    public String getTosroot(){
        return synchron( new Invocation<String>(){
            public String run() {
                return tosroot.getStringValue();
            }
        });
    }

    public String getTosdir(){
        return synchron( new Invocation<String>(){
            public String run() {
                return tosdir.getStringValue();
            }
        });
    }

    public String getMakerules(){
        return synchron( new Invocation<String>(){
            public String run() {
                return makerules.getStringValue();
            }
        });
    }

    public String getTosapps() {
        return synchron( new Invocation<String>(){
            public String run() {
                return tosapps.getStringValue();
            }
        });
    }
    
    public String getTreeLayout(){
        return synchron( new Invocation<String>(){
            public String run(){
                return tree.getSelection();
            }
        });
    }

    protected static interface Invocation<R>{
        public R run();
    }

    /**
     * Runs <code>invocation</code> in the ui thread.
     * @param <R> the kind of result this method returns
     * @param invocation the code to execute
     * @return the result of <code>invocation</code> or <code>null</code> if this
     * widget is disposed
     */
    @SuppressWarnings("unchecked")
    protected <R> R synchron( final Invocation<R> invocation ){
    	try{
	    	Display display = base.getDisplay();
	        if( display.getThread() == Thread.currentThread() ){
	            return invocation.run();
	        }
	        else{
	            final Object[] result = new Object[]{ null };
	
	            display.syncExec( new Runnable(){
	                public void run() {
	                    result[0] = invocation.run();
	                }
	            });
	
	            return (R)result[0];
	        }
    	}
    	catch( SWTException ex ){
    		if( ex.code == SWT.ERROR_WIDGET_DISPOSED ){
    			return null;
    		}
    		else{
    			TinyOSAbstractEnvironmentPlugin.log( new Status( 
    					IStatus.ERROR, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, ex.getMessage(), ex ) );
    			return null;
    		}
    	}
    }

    /**
     * Gets the key that is used for the {@link IPreferenceStore} when storing
     * or reading the given internal key.
     * @param key some key of a field of this page
     * @return the internal key
     */
    protected abstract String getStoreKey( StoreKey key );

    /**
     * Gets the path of <code>architecture</code>.
     * @param architecture some architecture, <code>null</code> for the
     * default architecture
     * @param override <code>true</code> if the user path, <code>false</code>
     * if the default path should be returned.
     * @return the path, may be an empty {@link String}
     */
    protected abstract String getArchitecturePath( String architecture, boolean override );

    /**
     * Tells whether the path for <code>architecture</code> is overridden.
     * @param architecture some architecture, <code>null</code> for the default
     * architecture.
     * @return <code>true</code> if the user path, <code>false</code> if the 
     * default path should be used for <code>architecture</code>
     */
    protected abstract boolean isArchitectureOverriden( String architecture );

    /**
     * Stores the settings of an architecture.
     * @param architecture the name of the architecture, <code>null</code> for the
     * default architecture
     * @param override <code>true</code> if the user path, <code>false</code>
     * if the default path is to be used
     * @param userPath the user path, may be an empty {@link String}
     */
    protected abstract void storeArchitecture( String architecture, boolean override, String userPath );

    /**
     * Gets a list of architectures which can be accessed with the current settings
     * of this page.
     * @return the list of architectures or <code>null</code> if this widget
     * is disposed
     */
    protected abstract String[] getVisibleArchitectures();

    /**
     * Resets the default values of the given set of architectures. This should
     * change the default value in the {@link IPreferenceStore}.
     * @param architectures the set of architectures, <code>null</code> indicates
     * that all architectures are to be updated
     * @param monitor a monitor to report progress, may not be <code>null</code>
     */
    protected abstract void resetArchitectureDefaultsPaths( String[] architectures, IProgressMonitor monitor );

    /**
     * Gets the environment for which this page is used.
     * @return the environment
     */
    protected abstract IEnvironment getEnvironment();
    
    /**
     * Gets a translator for paths.
     * @return a translator
     */
    protected abstract IPathTranslator getPathTranslator();

    @Override
    protected Control createContents( Composite parent ){
        base = new Composite( parent, SWT.NONE );
        base.setLayout( new GridLayout( 1, false ) );
        
        Group pathGroup = new Group( base, SWT.NONE );
        pathGroup.setLayout( new GridLayout( 1, false ) );
        pathGroup.setText( "System Paths" );
        Control editors = super.createContents( pathGroup );
        editors.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        pathGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );

        Group tableGroup = new Group( base, SWT.NONE );
        tableGroup.setLayout( new GridLayout( 1, false ) );
        tableGroup.setText( "Architecture Paths" );
        createArchitectureTable( tableGroup );
        tableGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        progress = new ProgressBar( base, SWT.HORIZONTAL );
        progress.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        progress.setEnabled( false );

        return base;
    }

    @Override
    protected void createFieldEditors() {
        IPathTranslator translator = getPathTranslator();
        
        tree = new ComboFieldEditor( getStoreKey( StoreKey.TREE ), "TinyOS Tree Layout", 
                new String[][]{
                    { "TinyOS 1.x (note: only changes paths, still behaves as if using TinyOS 2.x)", AbstractPathManager.TREE_TINYOS_1X },
                    { "TinyOS 2.x", AbstractPathManager.TREE_TINYOS_2X }},
                getFieldEditorParent());
        tree.getLabelControl( getFieldEditorParent() ).setToolTipText( "What layout the source tree of TinyOS has." );
        addField( tree );
        
        tosroot = new PathTranslatingDirectoryFieldEditor( translator, getStoreKey( StoreKey.TOSROOT ), 
                "&TinyOS Root Directory:", getFieldEditorParent()); 
        tosroot.setEmptyStringAllowed(false);
        setTooltip( tosroot, "Path to the directory which contains the TinyOS installation, also known as environment variable 'TOSROOT'." );

        tosdir = new PathTranslatingDirectoryFieldEditor( translator, getStoreKey( StoreKey.TOSDIR ),
                "TinyOS T&OS Directory:", getFieldEditorParent());
        tosdir.setEmptyStringAllowed(false);
        setTooltip( tosdir, "Path to the directory which contains the TinyOS system *.nc files, also known as environment variable 'TOSDIR'." );

        tosapps= new PathTranslatingDirectoryFieldEditor( translator, getStoreKey( StoreKey.TOSAPPS ), 
                "&TinyOS Application Directory:", getFieldEditorParent());
        tosapps.setEmptyStringAllowed(false);
        setTooltip( tosapps, "Path to the directory which contains the TinyOS example applications." );

        makerules = new PathTranslatingFileFieldEditor( translator, getStoreKey( StoreKey.MAKERULES ),
                "TinyOS App &Makerules:", getFieldEditorParent());
        setTooltip( makerules, "Path to the file 'Makerules', also known as environment variable 'MAKERULES'." );

        gccIncludes = new StringFieldEditor( getStoreKey( StoreKey.GCC_INCLUDES ),
                "GCC Include Directories", getFieldEditorParent() );
        setTooltip( gccIncludes, "Paths used in all architectures, ordinary empty." );

        addField( tree );
        addField( tosroot );
        addField( tosdir );       
        addField( tosapps );      
        addField( makerules );
        addField( gccIncludes );

        linkEditor( tree );
        linkEditor( tosroot );
        linkEditor( tosdir );
        linkEditor( makerules );
        
        tosroot.getTextControl( getFieldEditorParent() ).addModifyListener( new ModifyListener(){
            public void modifyText( ModifyEvent e ){
                copyTosroot();
            }
        });
    }
    
    /**
     * Copy the contents of {@link #tosroot} to the other fields if their
     * prefix is the same as the old value.
     */
    private void copyTosroot(){
    	String value = tosroot.getStringValue();
    	
    	if( tosrootOldValue != null ){
    		for( StringButtonFieldEditor editor : new StringButtonFieldEditor[]{tosdir, tosapps, makerules} ){
    			String current = editor.getStringValue();
    			if( current.startsWith( tosrootOldValue )){
    				current = current.substring( tosrootOldValue.length() );
    				current = value + current;
    				editor.setStringValue( current );
    			}
    		}
    	}
    	
    	tosrootOldValue = value;
    }

    protected void createArchitectureTable( Composite parent ){
        Composite base = new Composite( parent, SWT.NONE );
        base.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        GridLayout baseLayout = new GridLayout( 1, false );
        baseLayout.marginWidth = 0;
        baseLayout.marginHeight = 0;
        base.setLayout( baseLayout );
        
        architectures = new ArchitecturePathTable();
        architectures.createControl( base );
        architectures.getControl().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ));
        
        Composite buttons = new Composite( base, SWT.NONE );
        buttons.setLayoutData( new GridData( SWT.RIGHT, SWT.CENTER, false, false ) );
        GridLayout buttonsLayout = new GridLayout( 2, false );
        buttonsLayout.marginWidth = 1;
        buttonsLayout.marginHeight = 0;
        buttons.setLayout( buttonsLayout );
        
        Button schedule = new Button( buttons, SWT.PUSH );
        schedule.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        schedule.setText( "Update Default Paths" );
        schedule.setToolTipText( "Schedules an update of the default paths, using the systems paths that are specified on this page.\n" +
        		"If an update is already running, then a new update will be started afterwards.\n" +
        		"If you edit the system paths while the update is running, then new paths are used depending on the amount of work that is already done." );
        schedule.addSelectionListener( new SelectionListener(){
            public void widgetDefaultSelected( SelectionEvent e ) {
                architectureJob.start();
            }
            public void widgetSelected( SelectionEvent e ) {
                architectureJob.start();   
            }
        });
        
        Button copy = new Button( buttons, SWT.PUSH );
        copy.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        copy.setText( "Reset User Path" );
        copy.setToolTipText( "Copies the default path of the selected entry and uses it as user path, effectifly replacing the current user path." );
        copy.addSelectionListener( new SelectionListener(){
            public void widgetDefaultSelected( SelectionEvent e ) {
                architectures.copyDefaultPaths();
            }
            public void widgetSelected( SelectionEvent e ) {
                architectures.copyDefaultPaths();
            }
        });
    }

    protected void linkEditor( ComboFieldEditor editor ){
        editor.getComboBoxControl( getFieldEditorParent() ).addSelectionListener( new SelectionListener(){
            public void widgetDefaultSelected( SelectionEvent e ){
                architectureJob.start();
            }

            public void widgetSelected( SelectionEvent e ){
                architectureJob.start();
            }
        });
    }
    
    protected void linkEditor( StringFieldEditor editor ){
        editor.getTextControl( getFieldEditorParent() ).addModifyListener( new ModifyListener(){
            public void modifyText( ModifyEvent e ){
                architectureJob.start();
            }
        });
    }

    @Override
    protected void initialize() {
        super.initialize();
        getEnvironment().runAfterStartup( new Runnable(){
            public void run() {
                Job job = new UIJob( "Update UI" ){
                    @Override
                    public IStatus runInUIThread( IProgressMonitor monitor ) {
                        monitor.beginTask( "Update", IProgressMonitor.UNKNOWN );
                        if( !scheduled ){
                            updateArchitectures( getVisibleArchitectures() );
                        }
                        monitor.done();
                        return Status.OK_STATUS;
                    }
                };
                job.setPriority( Job.INTERACTIVE );
                job.setSystem( true );
                job.schedule();
            }
        });
        initialized = true;
    }

    @Override
    public boolean performOk() {
        if( !super.performOk() )
            return false;

        for( int i = 0, n = architectures.getRowCount(); i<n; i++ ){
            String key = architectures.getArchitecture( i );
            String architecture = NULL_ARCHITECTURE.equals( key ) ? null : key;

            boolean override = architectures.isOverridden( i );
            String path = architectures.getUserPath( i );
            storeArchitecture( architecture, override, path );
        }

        return true;
    }
    
    /**
     * Always returns <code>true</code> to allow the user changing the
     * page even if some paths are not valid.
     */
    @Override
    public boolean isValid(){
        return true;
    }
    
    @Override
    public boolean okToLeave(){
        return true;
    }

    protected void updateArchitectures( String[] visible ){
        String[] check = new String[ visible.length+1 ];
        check[0] = NULL_ARCHITECTURE;
        System.arraycopy(visible, 0, check, 1, visible.length );

        for( String key : check ){
            String architecture = NULL_ARCHITECTURE.equals( key ) ? null : key;
            if( architectures.isShowing( key )){
                architectures.update( key, getArchitecturePath( architecture, false ));
            }
            else{
                architectures.add(
                        key,
                        isArchitectureOverriden( architecture ),
                        getArchitecturePath( architecture, false ),
                        getArchitecturePath( architecture, true ));
            }
        }

        //         setTooltip( editor, "These paths are used to find header files for any platform whose architecture is '" + architecture + "'." );
    }

    protected void setTooltip( StringFieldEditor editor, String tooltip ){
        editor.getLabelControl( getFieldEditorParent() ).setToolTipText( tooltip );
        editor.getTextControl( getFieldEditorParent() ).setToolTipText( tooltip );
    }

    public void init( IWorkbench workbench ) {
        // ignore
    }

    /**
     * Returns a new {@link IProgressMonitor} that forwards calls to
     * <code>monitor</code> but also to the progress bar on this page.
     * @param monitor the monitor which might be <code>null</code>
     * @return the new monitor
     */
    public IProgressMonitor monitor( IProgressMonitor monitor ){
        if( monitor == null )
            return new ProgressBarMonitor( progress );

        return monitor = new MultiProgressMonitor( monitor, new ProgressBarMonitor( progress ));
    }

    private class ArchitectureJob extends Job{
        public ArchitectureJob(){
            super( "Update Architectures" );
            setPriority( INTERACTIVE );
        }

        public void start(){
            if( initialized ){
                scheduled = true;
                
                // wait with any changes until the environment is properly started up
                // that way we might be able to use the already existing default values
                getEnvironment().runAfterStartup( new Runnable(){
                    public void run() {
                        schedule( 500 );     
                    }
                });
            }
        }

        @Override
        protected IStatus run( IProgressMonitor monitor ){
            monitor = monitor( monitor );
            monitor.beginTask( "Update Architectures", 100 );
            monitor.worked( 10 );
            
            final String[] visible = getVisibleArchitectures();
            if( visible == null ){
            	monitor.done();
            	return Status.CANCEL_STATUS;
            }
            monitor.worked( 10 );
            
            if( visible.length > 0 ){
                resetArchitectureDefaultsPaths( visible, new SubProgressMonitor( monitor, 80 ));
            }
            else{
                monitor.worked( 80 );
            }

            Job ui = new UIJob( "Update UI"){
                @Override
                public IStatus runInUIThread( IProgressMonitor monitor ){
                    monitor.beginTask( "Update UI", IProgressMonitor.UNKNOWN );
                    updateArchitectures( visible );
                    monitor.done();
                    return Status.OK_STATUS;
                }
            };
            ui.setPriority( getPriority() );
            ui.setSystem( true );
            ui.schedule();

            try{
                ui.join();
            }
            catch ( InterruptedException e ){
                // ignore, this is only eye-candy, no need to really wait for the other job
            }
            monitor.done();

            return Status.OK_STATUS;
        }
    }
}
