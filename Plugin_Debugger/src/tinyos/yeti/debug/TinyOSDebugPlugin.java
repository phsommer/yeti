/**
 * 
 */
package tinyos.yeti.debug;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import tinyos.yeti.debug.CDTAbstractionLayer.CDTVariableManager;
import tinyos.yeti.debug.variables.INesCVariableListener;
import tinyos.yeti.debug.variables.internal.NesCVariableListener;

/**
 * The plugin class for TinyOS debug core.
 * @author snellen
 *
 */
public class TinyOSDebugPlugin extends AbstractUIPlugin {
	
    // The singleton instance.
    private static TinyOSDebugPlugin plugin;
    
    /**
     * Registers NesC variables with CDT debug targets
     */
    INesCVariableListener nescVariableListener = new NesCVariableListener(new CDTVariableManager());
    
	/**
	 * The plug-in identifier (value <code>"tinyos.yeti.debug"</code>).
	 */
	public static final String PLUGIN_ID = "tinyos.yeti.debug";
	
    /**
     * The constructor.
     */
    public TinyOSDebugPlugin() {
        plugin = this;
    }
    
    /**
     * Returns the shared instance.
     */
    public static TinyOSDebugPlugin getDefault() {
        return plugin;
    }
    
	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		return PLUGIN_ID;
	}
    
	/**
	 * Get this plugin's NesCVariableListener
	 * @return This plugin's NesCVariableListener
	 */
    public INesCVariableListener getNescVariableListener() {
		return nescVariableListener;
	}
	
	/**
	 * Get this plugin's preference store
	 */
    @Override
    public IPreferenceStore getPreferenceStore(){
        return super.getPreferenceStore();
    }
	
    public void log( String msg ){
        if( msg == null ){
            log( "null", null );
        }else{
            log( msg, null );
        }
    }

    public void log( String msg, Exception e ){
    	getLog().log( new Status( IStatus.ERROR, PLUGIN_ID, Status.OK, msg, e ) );
    }
    
    public static Display getStandardDisplay(){
        Display display;
        display = Display.getCurrent();
        if( display == null )
            display = Display.getDefault();
        return display;
    }
    
	/**
	 * Utility method with conventions
	 */
	public void errorDialog(Shell shell, String title, String message, Throwable t) {
		IStatus status;
		if (t instanceof CoreException) {
			status= ((CoreException)t).getStatus();
			// if the 'message' resource string and the IStatus' message are the same,
			// don't show both in the dialog
			if (status != null && message.equals(status.getMessage())) {
				message= null;
			}
		} else {
			status= new Status(IStatus.ERROR, getUniqueIdentifier(), ITinyOSDebugConstants.INTERNAL_ERROR, "Error within TinyOS Debug UI: ", t); //$NON-NLS-1$
			log(status.toString());
		}
		ErrorDialog.openError(shell, title, message, status);
	}
    
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
    public void start( BundleContext context ) throws Exception {
		super.start( context );
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop( BundleContext context ) throws Exception {
		nescVariableListener.stop();
		savePluginPreferences();
		super.stop( context );
	}
}
