package tinyos.yeti.refactoring;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class RefactoringPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "TinyOS_Refactoring";

	public enum LogLevel {
		CANCEL, ERROR, INFO, OK, WARNING
	}

	// The shared instance
	private static RefactoringPlugin plugin;

	/**
	 * The constructor
	 */
	public RefactoringPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public void log(LogLevel level, String message) {
		int severity;
		switch (level) {
		case CANCEL:
			severity = IStatus.CANCEL;
			break;

		case ERROR:
			severity = IStatus.ERROR;
			break;

		case INFO:
			severity = IStatus.INFO;
			break;

		case OK:
			severity = IStatus.OK;
			break;

		case WARNING:
			severity = IStatus.WARNING;
			break;

		default:
			log(LogLevel.ERROR, message);
			throw new IllegalArgumentException("No Rule for LogLevel \""
					+ level + "\". Message loged as ERROR.");
		}
		this.getLog().log(new Status(severity, PLUGIN_ID, message));
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static RefactoringPlugin getDefault() {
		return plugin;
	}

}
