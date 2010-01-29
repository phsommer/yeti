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
package tinyos.yeti;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import tinyos.yeti.editors.ExternalEditorInput;
import tinyos.yeti.editors.ExternalStorageDocumentProvider;
import tinyos.yeti.editors.FileStorage;
import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.editors.format.INesCFormattingStrategyFactory;
import tinyos.yeti.editors.nesc.IEditorTokenScanner;
import tinyos.yeti.editors.nesc.NesCCodeScanner;
import tinyos.yeti.editors.nesc.NescContextType;
import tinyos.yeti.editors.nesc.PreprocessorDirectiveScanner;
import tinyos.yeti.editors.nesc.SingleTokenScanner;
import tinyos.yeti.editors.nesc.doc.NesCCommentScanner;
import tinyos.yeti.editors.nesc.doc.NesCDocScanner;
import tinyos.yeti.editors.nesc.doc.NescDocContextType;
import tinyos.yeti.editors.outline.OutlineFilterFactory;
import tinyos.yeti.editors.quickfixer.QuickFixer;
import tinyos.yeti.ep.IEditorInputConverter;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.INesCMultiPageEditorPart;
import tinyos.yeti.ep.INesCPresentationReconcilerDefaults;
import tinyos.yeti.ep.INesCPresentationReconcilerFactory;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.NesCPresentationReconcilerFactory;
import tinyos.yeti.ep.fix.IMultiQuickFixer;
import tinyos.yeti.ep.fix.ISingleQuickFixer;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.INesCParserFactory;
import tinyos.yeti.launch.LaunchManager;
import tinyos.yeti.make.IMakeTarget;
import tinyos.yeti.make.IMakeTargetListener;
import tinyos.yeti.make.MakeTargetManager;
import tinyos.yeti.make.targets.IMakeTargetMorpheable;
import tinyos.yeti.model.IModelConfiguration;
import tinyos.yeti.model.NesCPath;
import tinyos.yeti.model.ProjectCacheFactory;
import tinyos.yeti.model.ProjectChecker;
import tinyos.yeti.model.local.LocalModelConfiguration;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc.NesCParserFactory;
import tinyos.yeti.preferences.PreferenceConstants;
import tinyos.yeti.utility.TinyOSProjects;
import tinyos.yeti.utility.preferences.IMultiPreferenceProvider;
import tinyos.yeti.utility.preferences.MultiPreferenceProvider;
import tinyos.yeti.utility.preferences.OldPluginPreferenceStore;
import tinyos.yeti.utility.preferences.PreferenceToken;
import tinyos.yeti.utility.preferences.TextAttributeConstants;
import tinyos.yeti.views.ThumbnailView;

/**
 * The main plugin class
 */
public class TinyOSPlugin extends AbstractUIPlugin{
    public static final String ID_MAKE_OPTIONS_VIEW = "TinyOS.view.makeOptions";
    public static final String ID_THUMBNAIL_VIEW = "TinyOS.view.thumbnail";
    public static final String ID_ALL_FILES_VIEW = "TinyOS.view.interfaces";

    public static final String ID_NEW_HEADER_WIZARD = "tinyos.wizards.header";
    public static final String ID_NEW_INTERFACE_WIZARD = "tinyos.wizards.interface";
    public static final String ID_NEW_MODULE_WIZARD = "tinyos.wizards.module";
    public static final String ID_NEW_CONFIGURATION_WIZARD = "tinyos.wizards.configuration";

    public static final String ID_MENU_ACTIONSET = "tinyos.menuActionSet";
    
    public static final String TINYOS_CONSOLE_ID = "tinyos.console.id";

    // // the plugin nature
    // public static final String NATURE = "TinyOS.TinyOSProject";
    //
    // the plugin id
    // private static String PLUGIN_ID = "TinyOS";

    // The shared instance.
    private static TinyOSPlugin plugin;

    private Map<IConsole, TinyOSConsole> consoles = new HashMap<IConsole, TinyOSConsole>();

    private MakeTargetManager fTargetManager;
    private ProjectManager fProjectManager;
    private TinyOSProjects projects;

    private INesCParserFactory parserFactory;
    private ISingleQuickFixer[] singleQuickFixers;
    private IMultiQuickFixer[] multiQuickFixers;
    private INesCPresentationReconcilerFactory reconciler;
    private INesCFormattingStrategyFactory[] formattings;

    private IPreferenceStore fCombinedPreferenceStore;

    /** The Plugins Template Store */
    private TemplateStore fTemplateStore;

    private ContributionContextTypeRegistry fContextTypeRegistry;

    private IModelConfiguration modelConfiguration;

    /** Key to store custom templates. */
    private static final String CUSTOM_TEMPLATES_KEY = "TinyOSPlugin.customtemplates"; //$NON-NLS-1$

    public static final String PLUGIN_ID = "tinyos.yeti.core";

    private IMultiPreferenceProvider preferences;
    
    private IEditorInputConverter[] editorInputConverters;
    
    private ProjectCacheFactory[] projectCaches;
    
    private OutlineFilterFactory[] outlineFilters;
    
    private IPreferenceStore preferenceStore;

    private ProjectChecker projectChecker;
    
    private EnvironmentManager environments;
    
    private LaunchManager launchManager;
    
    /**
     * Tells whether eclipse is currently on shutdown, jobs will be asked to
     * stop as soon as possible.
     * @return <code>true</code> if eclipse is shutting down
     */
    public static boolean isClosing(){
        return PlatformUI.getWorkbench().isClosing();
    }

    public void log( String msg ){
        if( msg == null ){
            log( "null", null );
        }else{
            log( msg, null );
        }
    }

    public void log( String msg, Exception e ){
        getLog().log( new Status( Status.INFO, PLUGIN_ID, Status.OK, msg, e ) );
    }

    /**
     * The constructor.
     */
    public TinyOSPlugin(){
        plugin = this;

        environments = new EnvironmentManager( this );
        launchManager = new LaunchManager();
        
        projectChecker = new ProjectChecker();
    }

    /**
     * This method is called upon plug-in activation
     */
    @Override
    public void start( BundleContext context ) throws Exception{
        super.start( context );

        Debug.connect( this );
        
        NesCIcons.icons().loadAttributes( this );
    }

    /**
     * This method is called when the plug-in is stopped
     */
    @Override
    public void stop( BundleContext context ) throws Exception{
        savePluginPreferences();
        try{
            if( fTargetManager != null ){
                fTargetManager.shutdown();
                fTargetManager = null;
            }
        }finally{
            super.stop( context );
            plugin = null;
        }
    }

    /**
     * Returns the shared instance.
     */
    public static TinyOSPlugin getDefault(){
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in
     * relative path.
     * 
     * @param path
     *                the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor( String path ){
        return AbstractUIPlugin.imageDescriptorFromPlugin( PLUGIN_ID, path );
    }

    /**
     * Gets the preferences of this plugin.
     * @return the preferences, read only
     */
    public IMultiPreferenceProvider getPreferences(){
        if( preferences == null ){
            preferences = new MultiPreferenceProvider( getPreferenceStore() );
        }

        return preferences;
    }

    @Override
    public IPreferenceStore getPreferenceStore(){
    	if( preferenceStore == null ){
    		preferenceStore = new OldPluginPreferenceStore( super.getPreferenceStore(), "TinyOS" );
    	}
    	
    	return preferenceStore;
    }

    /**
     * Returns a combined preference store, this store is read-only.
     * 
     * @return the combined preference store
     * 
     * @since 3.0
     */
    public IPreferenceStore getCombinedPreferenceStore(){
        if( fCombinedPreferenceStore == null ){
            IPreferenceStore generalTextStore = EditorsUI.getPreferenceStore();
            fCombinedPreferenceStore = new ChainedPreferenceStore(
                    new IPreferenceStore[]{
                            getPreferenceStore(),
                            new PreferencesAdapter( TinyOSPlugin.getDefault()
                                    .getPluginPreferences() ), generalTextStore } );
        }
        return fCombinedPreferenceStore;
    }

    public static IWorkspace getWorkspace(){
        return ResourcesPlugin.getWorkspace();
    }
    
    /**
     * Tries to open and reveal the file denoted by <code>region</code>.
     * @param region a region in some file
     * @return the editor in which the file is shown
     * @throws CoreException in case of a problem
     */
    public ITextEditor openFileInTextEditor( IFileRegion region ) throws CoreException{
    	ITextEditor editor = TinyOSPlugin.getDefault().openFileInTextEditor( region.getParseFile().getProject(), region.getParseFile().getPath() );
    	if( editor != null && region != null ){
    		editor.selectAndReveal( region.getOffset(), region.getLength() );
    	}
    	return editor;
    }
    
    public ITextEditor openFileInTextEditor( IParseFile file ) throws CoreException{
    	File real = file.toFile();
    	if( real == null )
    		return null;
    	return openFileInTextEditor( file.getProject(), new Path( real.getAbsolutePath() ), true );
    }

    /**
     * Open a file in the Workbench that may or may not exist in the workspace.
     * Must be run on the UI thread.
     * 
     * @param project the project in whose context this file is opened, may be <code>null</code>
     * @param filename
     * @return the editor that was opened
     * @throws CoreException
     */
    public ITextEditor openFileInTextEditor( ProjectTOS project, String filename ) throws CoreException{
    	return openFileInTextEditor( project == null ? null : project.getProject(), filename );
    }
    
    public ITextEditor openFileInTextEditor( IProject project, String filename ) throws CoreException{
        // reject directories
        if( new File( filename ).isDirectory() )
            return null;

        IPath path = new Path( filename );

        return openFileInTextEditor( project, path, true );
    }

    public ITextEditor openFileInTextEditor( ProjectTOS project, IPath path, boolean absolutePath ) throws CoreException{
    	return openFileInTextEditor( project == null ? null : project.getProject(), path, absolutePath );
    }
    
    public ITextEditor openFileInTextEditor( IProject project, IPath path, boolean absolutePath ) throws CoreException{
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow window = workbench.getWorkbenchWindows()[0];
        IWorkbenchPage page = window.getActivePage();

        // If the file exists in the workspace, open it
        IFile file;
        if( absolutePath )
            file = getFile( project, path );
        else
            file = getWorkspace().getRoot().getFile( path );
        IEditorPart editor;
        ITextEditor textEditor;
        if( file != null && file.exists() ){
            // editor = IDE.openEditor(page, file, true);
            IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor( file.getName() );
            if( desc == null ){
                desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor( "test.txt" );
            }
            editor = page.openEditor( new FileEditorInput( file ), desc.getId() );
            textEditor = ( ITextEditor )editor.getAdapter( ITextEditor.class );

        }else{
            // Otherwise open the stream directly
        	Debug.info( "open stream directly: '" + path + "'" );
            if( page == null )
                return null;
            FileStorage storage = new FileStorage( path );
            IEditorRegistry registry = getWorkbench().getEditorRegistry();
            IEditorDescriptor desc = registry.getDefaultEditor( path.toString() );
            if( desc == null ){
                desc = PlatformUI.getWorkbench().getEditorRegistry()
                .getDefaultEditor( "test.txt" );
                // desc = registry.getDefaultEditor();
            }
            IEditorInput input = new ExternalEditorInput( storage,
                    getProjectManager().getlastProject() );
            editor = page.openEditor( input, desc.getId() );
            textEditor = ( ITextEditor )editor.getAdapter( ITextEditor.class );
            // If the storage provider is not ours, we can't guarantee
            // read/write.
            if( textEditor != null ){
                IDocumentProvider documentProvider = textEditor
                .getDocumentProvider();
                boolean canModify = getCombinedPreferenceStore().getBoolean(
                        PreferenceConstants.ENABLE_MODIFY_ON_OUTSIDE_FILES );
                if( !canModify ){
                    storage.setReadOnly();
                }
                if( !( documentProvider instanceof ExternalStorageDocumentProvider ) ){
                    storage.setReadOnly();
                }
            }
        }
        return textEditor;
    }

    /**
     * Searches for a file with the given absolute path, also checks linked 
     * folders of <code>project</code>.
     * @param project the project in whose context to open the file
     * @param path absolute path to a file
     * @return the file or <code>null</code> if not available in the workspace
     */
    public static IFile getFile( IProject project, IPath path ) throws CoreException{
    	 IFile file = getWorkspace().getRoot().getFileForLocation( path );
    	 if( file != null )
    		 return file;
    	 
    	 if( project == null )
    		 return null;
    	 
    	 for( IResource member : project.members() ){
    		 if( member instanceof IFolder ){
    			 IFolder folder = (IFolder)member;
    			 if( folder.isLinked() ){
    				 IPath location = folder.getLocation();
    				 if( location.isPrefixOf( path )){
    					 return folder.getFile( path.removeFirstSegments( location.segmentCount() ) );
    				 }
    			 }
    		 }
    	 }
    	 
    	 return null;
    }
    
    /**
     * Gets the console of this plugin. This method runs in the ui-thread
     * and blocks until it is finished.
     * @return the console
     */
    public TinyOSConsole getConsole(){
    	Display display = PlatformUI.getWorkbench().getDisplay();
    	if( display.getThread() != Thread.currentThread() ){
    		class Run implements Runnable{
    			public TinyOSConsole result;
    			
    			public void run(){
    				result = getConsole();
    			}
    		}
    		
    		Run run = new Run();
    		display.syncExec( run );
    		return run.result;
    	}
    	else{
    		synchronized( TinyOSConsole.class ){
    			ConsolePlugin plugin = ConsolePlugin.getDefault();
    			if( plugin == null )
    				return null;
    			IConsoleManager conMan = plugin.getConsoleManager();
    			IConsole[] existing = conMan.getConsoles();
    			for( int i = 0; i < existing.length; i++ ){
    				if( existing[i] != null ){
    					if( existing[i] instanceof TextConsole ){
    						if( Boolean.TRUE.equals( ((TextConsole)existing[i]).getAttribute( TINYOS_CONSOLE_ID ))){
    							return getConsole( conMan, ( MessageConsole )existing[i] );
    						}
    					}
    				}
    			}

    			// no console found, so create a new one
    			MessageConsole myConsole = new MessageConsole( "TinyOS", null );
    			myConsole.setAttribute( TINYOS_CONSOLE_ID, Boolean.TRUE );
    			conMan.addConsoles( new IConsole[]{ myConsole } );

    			// show console
    			revealConsole();
    			return getConsole( conMan, myConsole );
    		}
    	}
    }

    private TinyOSConsole getConsole( final IConsoleManager manager, final MessageConsole console ){
        TinyOSConsole result = consoles.get( console );
        if( result == null ){
            result = new TinyOSConsole( console );
            consoles.put( console, result );

            manager.addConsoleListener( new IConsoleListener(){
                public void consolesAdded( IConsole[] consoles ){
                    // ignore
                }
                public void consolesRemoved( IConsole[] removed ){
                    for( IConsole check : removed ){
                        if( check == console ){
                            consoles.remove( check );
                            manager.removeConsoleListener( this );
                            break;
                        }
                    }
                }
            });
        }
        return result;
    }

    public void revealConsole(){
        IWorkbench wb = PlatformUI.getWorkbench();
        IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
        IWorkbenchPage page = null;
        if( win != null ){
            page = win.getActivePage();
            if( page == null )
                return;
            IConsole myConsole = getConsole().getConsole();

            String id = IConsoleConstants.ID_CONSOLE_VIEW;
            IConsoleView view;
            try{
                view = (IConsoleView)page.showView( id );
                view.display( myConsole );
            }
            catch ( PartInitException e ){
            	log( e );
            }
        }
    }
    /*
    // never tested this method... perhaps it can be used later
    public void createNewHeaderFile( final IContainer directory ){
        Job job = new UIJob( "Create new file" ){
            @Override
            public IStatus runInUIThread( IProgressMonitor monitor ){
                monitor.beginTask( "Create new file", IProgressMonitor.UNKNOWN );

                IWorkbench workbench = getWorkbench();
                Shell shell = workbench.getActiveWorkbenchWindow().getShell();

                NewHeaderWizard wizard = new NewHeaderWizard();
                WizardDialog dialog = new WizardDialog( shell, wizard );
                wizard.init( workbench, new StructuredSelection( directory ) );
                dialog.open();

                monitor.done();
                return Status.OK_STATUS;
            }
        };
        job.setPriority( Job.INTERACTIVE );
        job.setSystem( true );
        job.schedule();
    }
     */
    public MakeTargetManager getTargetManager(){
        if( fTargetManager == null ){
            fTargetManager = new MakeTargetManager();
            fTargetManager.startUp();
        }
        return fTargetManager;
    }

    public void addMakeTargetListener( IMakeTargetListener listener ){
        getTargetManager().addListener( listener );
    }
    
    public void removeMakeTargetListener( IMakeTargetListener listener ){
    	getTargetManager().removeListener( listener );
    }
    
    public ProjectChecker getProjectChecker(){
		return projectChecker;
	}

    public static void log( Throwable e ){
        if( e instanceof InvocationTargetException )
            e = ( ( InvocationTargetException )e ).getTargetException();
        IStatus status = new Status( IStatus.ERROR, PLUGIN_ID, IStatus.OK, e.getMessage(), e );
        log( status );
    }

    public static Shell getActiveWorkbenchShell(){
        IWorkbenchWindow window = getActiveWorkbenchWindow();
        if( window != null ){
            return window.getShell();
        }
        return null;
    }

    public static IWorkbenchWindow getActiveWorkbenchWindow(){
        return getDefault().getWorkbench().getActiveWorkbenchWindow();
    }

    /**
     * Utility method with conventions
     */
    public static void errorDialog( Shell shell, String title, String message,
            IStatus s ){
        log( s );
        // if the 'message' resource string and the IStatus' message are the
        // same,
        // don't show both in the dialog
        if( s != null && message.equals( s.getMessage() ) ){
            message = null;
        }
        ErrorDialog.openError( shell, title, message, s );
    }

    /**
     * Utility method with conventions
     */
    public static void errorDialog( Shell shell, String title, String message,
            Throwable t ){
        log( t );
        IStatus status;
        if( t instanceof CoreException ){
            status = ( ( CoreException )t ).getStatus();
            // if the 'message' resource string and the IStatus' message are the
            // same,
            // don't show both in the dialog
            if( status != null && message.equals( status.getMessage() ) ){
                message = null;
            }
        }else{
            status = new Status( IStatus.ERROR,
                    PLUGIN_ID, -1, "Internal Error: ", t ); //$NON-NLS-1$	
        }
        ErrorDialog.openError( shell, title, message, status );
    }

    public static void log( IStatus status ){
        ResourcesPlugin.getPlugin().getLog().log( status );
    }

    public INesCParserFactory getParserFactory(){
        if( parserFactory == null ){
            parserFactory = loadExtensionPoint( "Parser", "parser", "class" );

            if( parserFactory == null ){
                // use the backup factory of this plugin
                parserFactory = new NesCParserFactory();
                singleQuickFixers = new ISingleQuickFixer[]{ new QuickFixer() };
            }
        }

        return parserFactory;
    }
    
    public IEditorInputConverter getEditorInputConverter( IEditorInput input ){
    	for( IEditorInputConverter converter : getEditorInputConverters() ){
    		if( converter.matches( input )){
    			return converter;
    		}
    	}
    	return null;
    }
    
    public IEditorInputConverter[] getEditorInputConverters(){
    	if( editorInputConverters == null ){
    		List<IEditorInputConverter> list = loadExtensionPoints( "EditorInputConverter", "converter", "class" );
    		editorInputConverters = list.toArray( new IEditorInputConverter[ list.size() ] );
    	}
    	return editorInputConverters;
    }

    public ISingleQuickFixer[] getSingleQuickFixers(){
        if( singleQuickFixers == null ){
            List<ISingleQuickFixer> list = loadExtensionPoints( "Quickfixer", "single", "class" );
            singleQuickFixers = list.toArray( new ISingleQuickFixer[ list.size() ] );
        }

        return singleQuickFixers;
    }

    public IMultiQuickFixer[] getMultiQuickFixers(){
        if( multiQuickFixers == null ){
            List<IMultiQuickFixer> list = loadExtensionPoints( "Quickfixer", "multi", "class" );
            multiQuickFixers = list.toArray( new IMultiQuickFixer[ list.size() ] );
        }

        return multiQuickFixers;
    }

    public INesCPresentationReconcilerFactory getPresentationReconcilerFactory(){
        if( reconciler == null ){
            reconciler = loadExtensionPoint( "Reconciler", "reconciler", "reconciler" );

            if( reconciler == null ){
                reconciler = new NesCPresentationReconcilerFactory();
            }
        }

        return reconciler;
    }
    
    public INesCFormattingStrategyFactory[] getFormattingFactories(){
    	if( formattings == null ){
    	    List<INesCFormattingStrategyFactory> list = loadExtensionPoints( "Formatting", "formatter", "class" );
    	    if( list == null )
    	    	formattings = new INesCFormattingStrategyFactory[]{};
    	    else
    	    	formattings = list.toArray( new INesCFormattingStrategyFactory[ list.size() ] );
    	}
    	return formattings;
    }
    
    public INesCFormattingStrategyFactory getFormattingFactory(){
    	String name = getPreferenceStore().getString( PreferenceConstants.CF_CODE_FORMATTING_STRATEGY );
    	INesCFormattingStrategyFactory[] factories = getFormattingFactories();
    	for( INesCFormattingStrategyFactory factory : factories ){
    		if( factory.getId().equals( name ) && factory.isFormatter() )
    			return factory;
    	}
    	
    	for( INesCFormattingStrategyFactory factory : factories ){
    		if( factory.isFormatter() )
    			return factory;
    	}
    	
    	return null;
    }
    
    public boolean formattingStrategyExists(){
    	INesCFormattingStrategyFactory[] factories = getFormattingFactories();
    	if( factories == null )
    		return false;
    	for( INesCFormattingStrategyFactory factory : factories ){
    		if( factory.isFormatter() ){
    			return true;
    		}
    	}
    	return false;
    }
    
    public INesCFormattingStrategyFactory getIndenterFactory(){
    	String name = getPreferenceStore().getString( PreferenceConstants.CF_INDENTATION_STRATEGY );
    	INesCFormattingStrategyFactory[] factories = getFormattingFactories();
    	for( INesCFormattingStrategyFactory factory : factories ){
    		if( factory.getId().equals( name ) && factory.isIndenter() )
    			return factory;
    	}
    	
    	for( INesCFormattingStrategyFactory factory : factories ){
    		if( factory.isIndenter() )
    			return factory;
    	}
    	
    	return null;
    }
    
    public boolean intendingStrategyExists(){
    	INesCFormattingStrategyFactory[] factories = getFormattingFactories();
    	if( factories == null )
    		return false;
    	for( INesCFormattingStrategyFactory factory : factories ){
    		if( factory.isIndenter() ){
    			return true;
    		}
    	}
    	return false;    	
    }

    /**
     * Gets a token scanner for nesc code.
     * @param name one of the constants defined in {@link INesCPresentationReconcilerDefaults}
     * @return the new scanner
     */
    public IEditorTokenScanner getScanner( String name ){
    	/*
    	 * Note: don't use a cache for the token scanners, some of them
    	 * (like the one for NesC 1.2) store states and cannot be shared
    	 * among many documents.
    	 */
    	
     	IEditorTokenScanner result = loadNamedExtensionPoint( "Reconciler", "scanner", name );
     	
     	if( result == null ){
     		if( INesCPresentationReconcilerDefaults.SCANNER_NAME_NESC_DOC.equals( name ) ){
     			result = new NesCDocScanner( getPreferences() );
     		}
     		else if( INesCPresentationReconcilerDefaults.SCANNER_NAME_SINGLELINE_COMMENT.equals( name )){
     			result = new NesCCommentScanner( getPreferences(), TextAttributeConstants.COMMENT_SINGLE_LINE );
     		}
     		else if( INesCPresentationReconcilerDefaults.SCANNER_NAME_MULTILINE_COMMENT.equals( name )){
     			result = new NesCCommentScanner( getPreferences(), TextAttributeConstants.COMMENT_MULTI_LINE );
     		}
     		else if( INesCPresentationReconcilerDefaults.SCANNER_NAME_STRING.equals( name )){
     			result = new SingleTokenScanner( new PreferenceToken<TextAttribute>( TextAttributeConstants.STRING, getPreferences().getTextAttributes() ));
     		}
     		else if( INesCPresentationReconcilerDefaults.SCANNER_NAME_PREPROCESSOR.equals( name )){
     			result = new PreprocessorDirectiveScanner( getPreferences() );
     		}
     		else if( INesCPresentationReconcilerDefaults.SCANNER_NAME_DEFAULT.equals( name )){
     			result = new NesCCodeScanner( getPreferences() );
     		}
     	}
     	return result;
    }
  
    public IModelConfiguration getModelConfiguration(){
        if( modelConfiguration == null ){
            // modelConfiguration = new StandardModelConfiguration();
            modelConfiguration = new LocalModelConfiguration();
        }
        return modelConfiguration;
    }

    /**
     * Loads the extension point that contains information about attributes.
     * @return the name-resource pairs
     */
    public Map<String, URL> loadMetaAttributes(){
    	IExtensionRegistry registry = Platform.getExtensionRegistry();
    	IExtensionPoint point = registry.getExtensionPoint( PLUGIN_ID, "MetaAttribute" );
    	
    	Map<String, URL> result = new HashMap<String, URL>();
    	
    	if( point != null ){
	    	for( IExtension extension : point.getExtensions() ){
	    		for( IConfigurationElement element : extension.getConfigurationElements() ){
	    			if( element.getName().equals( "attribute" )){
	    				String id = element.getAttribute( "id" );
	    				
	    				String resource = element.getAttribute( "icon" );
	    				
	    				IContributor contributor = element.getContributor();
	    				String pluginName = contributor.getName();
	    				
	    				Bundle bundle = Platform.getBundle( pluginName );
	    				if( bundle != null ){
		    				URL url = bundle.getEntry( resource );
			    			if( url != null ){
			    				result.put( id, url );
			    			}
	    				}
	    			}
	    		}
	    	}
    	}
    	
    	return result;
    }
    
    /**
     * Returns all the available project caches.
     * @return the caches
     */
    public ProjectCacheFactory[] getProjectCaches(){
    	if( projectCaches != null )
    		return projectCaches;
    	
    	List<ProjectCacheFactory> result = new ArrayList<ProjectCacheFactory>();
    	
    	IExtensionRegistry reg = Platform.getExtensionRegistry();
        IExtensionPoint extPoint = reg.getExtensionPoint( PLUGIN_ID + ".ProjectCache"  );
        
        for( IExtension ext : extPoint.getExtensions() ){
            for( IConfigurationElement element : ext.getConfigurationElements() ){
                if( element.getName().equals( "cache" ) ){
                	result.add( new ProjectCacheFactory( element ) );
                }
            }
        }

        projectCaches = result.toArray( new ProjectCacheFactory[ result.size() ] );
        return projectCaches;
    }
    
    public OutlineFilterFactory[] getOutlineFilters(){
    	if( outlineFilters != null )
    		return outlineFilters;
    	
    	List<OutlineFilterFactory> result = new ArrayList<OutlineFilterFactory>();
    	
    	IExtensionRegistry reg = Platform.getExtensionRegistry();
        IExtensionPoint extPoint = reg.getExtensionPoint( PLUGIN_ID + ".OutlineFilter"  );
        
        for( IExtension ext : extPoint.getExtensions() ){
            for( IConfigurationElement element : ext.getConfigurationElements() ){
                if( element.getName().equals( "filter" ) ){
                	result.add( new OutlineFilterFactory( element ) );
                }
            }
        }

        outlineFilters = result.toArray( new OutlineFilterFactory[ result.size() ] );
        return outlineFilters;    	
    }

    @SuppressWarnings( "unchecked" )
    private <E> E loadExtensionPoint( String point, String extension,
            String executableAttribute ){
        IExtensionRegistry reg = Platform.getExtensionRegistry();
        IExtensionPoint extPoint = reg.getExtensionPoint( PLUGIN_ID + "."
                + point );

        for( IExtension ext : extPoint.getExtensions() ){
            for( IConfigurationElement element : ext.getConfigurationElements() ){
                if( element.getName().equals( extension ) ){
                    if( element.getAttribute( executableAttribute ) != null ){
                        try{
                            return ( E )element.createExecutableExtension( executableAttribute );
                        } 
                        catch ( CoreException e ){
                            getLog().log( e.getStatus() );
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Searches for an extension point that has two attributes: name is a 
     * string that equals <code>nameAttributeValue</code>, and "class" contains
     * the name of the class to create.
     * @param <E> the kind of element to create
     * @param point the name of the extension point
     * @param extension the internal extension
     * @param nameAttributeValue the value of the attribute "name"
     * @return a new instance or <code>null</code>
     */
    @SuppressWarnings( "unchecked" )
    private <E> E loadNamedExtensionPoint( String point, String extension, String nameAttributeValue ){
        IExtensionRegistry reg = Platform.getExtensionRegistry();
        IExtensionPoint extPoint = reg.getExtensionPoint( PLUGIN_ID + "." + point );

        for( IExtension ext : extPoint.getExtensions() ){
            for( IConfigurationElement element : ext.getConfigurationElements() ){
                if( element.getName().equals( extension ) ){
                	if( nameAttributeValue.equals( element.getAttribute( "name" ) )){
                		if( element.getAttribute( "class" ) != null ){
                		    try{
                                return ( E )element.createExecutableExtension( "class" );
                            } 
                            catch ( CoreException e ){
                                getLog().log( e.getStatus() );
                            }	
                		}
                    }
                }
            }
        }

        return null;
    }
    
    /**
     * Gets a new set of editor parts
     * 
     * @return the fresh set of parts
     */
    public INesCMultiPageEditorPart[] getMultiPageEditorParts(){
        List<INesCMultiPageEditorPart> parts = loadExtensionPoints( "Editor", "multiPagePart", "class" );
        return parts.toArray( new INesCMultiPageEditorPart[parts.size()] );
    }

    @SuppressWarnings( "unchecked" )
    private <E> List<E> loadExtensionPoints( String point, String extension, String executableAttribute ){
        IExtensionRegistry reg = Platform.getExtensionRegistry();
        IExtensionPoint extPoint = reg.getExtensionPoint( PLUGIN_ID + "." + point );
        List<E> result = new ArrayList<E>();

        for( IExtension ext : extPoint.getExtensions() ){
            for( IConfigurationElement element : ext.getConfigurationElements() ){
                if( element.getName().equals( extension ) ){
                    if( Debug.DEBUG || !Boolean.parseBoolean( element.getAttribute( "debug" ) )){
                        try{
                            result.add( ( E )element.createExecutableExtension( executableAttribute ) );
                        }   
                        catch ( CoreException e ){
                            getLog().log( e.getStatus() );
                        }
                    }
                }
            }
        }

        return result;
    }

    public ProjectManager getProjectManager(){
        if( fProjectManager == null ){
            fProjectManager = new ProjectManager();
        }
        return fProjectManager;
    }
    
    public TinyOSProjects getProjects(){
    	if( projects == null ){
    		projects = new TinyOSProjects();
    	}
    	return projects;
    }

    public ProjectTOS getProjectTOS( IProject project ) throws MissingNatureException{
        return getProjectTOS( project, true );
    }

    public ProjectTOS getProjectTOS( IProject project, boolean initialize ) throws MissingNatureException{
        return getProjectManager().getProjectTOS( project, initialize );
    }


    public ProjectTOS getProjectTOS(){
        IProject p = getProjectManager().getlastProject();
        if( p != null ){
        	try{
        		return getProjectManager().getProjectTOS( p, true );
        	}
        	catch( MissingNatureException ex ){
        		log( ex );
        		return null;
        	}
        }
        return null;
    }
    
    public NesCPath getPaths( IProject project ){
    	return getProjectManager().getPaths( project );
    }
    
    public EnvironmentManager getEnvironments(){
		return environments;
	}
    
    public LaunchManager getLaunchManager(){
		return launchManager;
	}

    public File locate( IResource resource, String filename, boolean systemFile, IProgressMonitor monitor ){
        if( monitor == null )
            monitor = new NullProgressMonitor();

        monitor.beginTask( "Locate '" + filename + "'", 1000 );

        if( resource != null ){
        	IMakeTargetMorpheable morph = getTargetManager().getSelectedTarget( resource.getProject() );
        	IMakeTarget me = morph == null ? null : morph.toMakeTarget();
        	
            if( me != null ){
                if( resource.getProject().isAccessible() ){
                    IEnvironment environment = getEnvironments().getEnvironment( resource
                            .getProject() );
                    if( environment == null )
                        return null;

                    File result = environment
                    .locate( filename,
                            resource.getProject(), // Project
                            me.getIncludes(),// directives
                            me.getExcludes(),//exclude
                            me.getSensorBoards(), // board(s)
                            me.getTarget(), // Platform
                            me.isNostdinc(), // nostinct
                            systemFile, new SubProgressMonitor( monitor, 1000 ) );
                    monitor.done();
                    return result;
                }
            }
        }

        monitor.done();
        return null;
    }

    public File locate( String filename, boolean systemFile, IProgressMonitor monitor ){
        IProject lastProject = getProjectManager().getlastProject();
        return locate( filename, systemFile, monitor, lastProject );
    }

    public File locate( String filename, boolean systemFile, IProgressMonitor monitor, IProject project ){
        if( monitor == null )
            monitor = new NullProgressMonitor();

        monitor.beginTask( "Locate file '" + filename + "'", 1000 );
        
    	IMakeTargetMorpheable morph = getTargetManager().getSelectedTarget( project );
    	IMakeTarget me = morph == null ? null : morph.toMakeTarget();
        
        if( ( me != null ) && ( project != null ) ){
            if( project.isAccessible() ){
                File result = getEnvironments().getEnvironment( project ).locate(
                        filename,
                        project, // Project
                        	me.getIncludes(), // directives
                        	me.getExcludes(), // exclude
                        	me.getSensorBoards(), // board
                        	me.getTarget(), // Platform
                        	me.isNostdinc(), // nostinct
                        	systemFile,  // file
                        	new SubProgressMonitor( monitor, 1000 ));

                monitor.done();
                return result;
            }
        }
        monitor.done();
        return null;
    }

    /*
     * public NesCModel getModel(IFile f) { return
     * getProjectTOS(f.getProject()).getModel(f); }
     *  /* @param name without extension .nc @return
     * 
     * public NesCModel getModel(String name) { File f =
     * TinyOSPlugin.getDefault().locate(name+".nc");
     * 
     * if ((f!=null)&&(f.isFile())) { return new
     * NesCModel(getProjectManager().getlastProject(), f, new Declaration[0],
     * new LinkedList()); } return null; }
     */
    // public NesCModel getModel(IPath path) {
    // File f = path.toFile();
    // if ((f!=null)&&(f.isFile())) {
    // return new NesCModel(getProjectManager().getlastProject(), f,
    // new Declaration[0], new LinkedList());
    // }
    // return null;
    // }
    /**
     * Returns this plug-in's context type registry.
     * 
     * @return the context type registry for this plug-in instance
     */
    public ContextTypeRegistry getContextTypeRegistry(){
        if( fContextTypeRegistry == null ){
            // create an configure the contexts available in the template editor
            fContextTypeRegistry = new ContributionContextTypeRegistry();
            fContextTypeRegistry.addContextType( NescDocContextType.NESCDOC_CONTEXTTYPE );
            fContextTypeRegistry.addContextType( NescContextType.NESC_CONTEXTTYPE );

        }
        return fContextTypeRegistry;
    }

    public Image getImage( String key ){
        Image image = getImageRegistry().get( key );
        if( image == null ){
            ImageDescriptor d = getImageDescriptor( key );
            image = d.createImage();
            getImageRegistry().put( key, image );
        }
        return image;
    }

    /**
     * Returns this plug-in's template store.
     * 
     * @return the template store of this plug-in instance
     */
    public TemplateStore getTemplateStore(){
        if( fTemplateStore == null ){
            fTemplateStore = new ContributionTemplateStore(
                    getContextTypeRegistry(), getPreferenceStore(),
                    CUSTOM_TEMPLATES_KEY );
            try{
                fTemplateStore.load();
            }catch ( IOException e ){
                e.printStackTrace();
            }
        }
        return fTemplateStore;
    }

    public static IWorkbenchPage getActivePage(){
        return getDefault().internalGetActivePage();
    }

    private IWorkbenchPage internalGetActivePage(){
        IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
        if( window == null )
            return null;
        return getWorkbench().getActiveWorkbenchWindow().getActivePage();
    }

    @Deprecated
    public OutputStream getConsoleStream(){
        return getConsole().getConsole().newMessageStream();
    }

    @Deprecated
    public void wirteToConsole( String s ){
        MessageConsole c = getConsole().getConsole();
        if( c == null )
            return;
        OutputStream o = c.newMessageStream();
        try{
            o.write( ( s + "\n" ).getBytes() );
            o.close();
        }catch ( IOException e ){
            e.printStackTrace();
        }
    }

    public void showThumbnailView(){
        try{
            IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();

            if( getPreferenceStore().getBoolean( PreferenceConstants.THUMBNAIL_POPUP )){
                IViewPart ivp = page.showView( ThumbnailView.ID );
                ((ThumbnailView)ivp).refresh();
            }
            else{
                IViewReference[] references = page.getViewReferences();
                if( references != null ){
                    for( IViewReference reference : references ){
                        IViewPart part = reference.getView( false );
                        if( part instanceof ThumbnailView ){
                            ThumbnailView thumbnail = (ThumbnailView)part;
                            if( !thumbnail.isDisposed() ){
                                thumbnail.refresh();
                            }
                        }
                    }
                }
            }

        }
        catch ( PartInitException e ){
            getLog().log( e.getStatus() );
        }
    }

    /**
     * Returns the standard display to be used. The method first checks, if the
     * thread calling this method has an associated display. If so, this display
     * is returned. Otherwise the method returns the default display.
     */
    public static Display getStandardDisplay(){
        Display display;
        display = Display.getCurrent();
        if( display == null )
            display = Display.getDefault();
        return display;
    }
    
    public static void warning( Exception e ){
        TinyOSPlugin plugin = getDefault();
        if( plugin != null )
            plugin.getLog().log( new Status( IStatus.WARNING, PLUGIN_ID, 0, e.getMessage(), e ) );
    }

    public static void warning( IStatus status ){
        TinyOSPlugin plugin = getDefault();
        if( plugin != null )
            plugin.getLog().log( new Status( IStatus.WARNING, status.getPlugin(), status.getCode(), status.getMessage(), status.getException() ) );
    }
}
