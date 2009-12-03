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
package tinyos.yeti.wizards;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import tinyos.yeti.utility.StringUtility;

/**
 * This is a sample new wizard. Its role is to create a new file 
 * resource in the provided container. If the container resource
 * (a folder or a project) is selected in the workspace 
 * when the wizard is opened, it will accept it as the target
 * container. The wizard creates one file with the extension
 * "nc". If a sample multi-page editor (also available
 * as a template) is registered for the same extension, it will
 * be able to open it.
 * @deprecated no longer in use, it would be used in the extension point 'org.eclipse.ui.newWizards'
 */
@Deprecated
public class TinyOSNewWizard extends Wizard implements INewWizard {
    private TinyOSNewWizardPage page;
    private ISelection selection;

    private String newFileConfigurationContent = Messages.getString("TinyOSNewWizard.newFileConfigurationContent"); //$NON-NLS-1$
    private String newFileModuleContent = Messages.getString("TinyOSNewWizard.newFileModuleContent"); //$NON-NLS-1$
    private String newFileMakeContent = Messages.getString("TinyOSNewWizard.newFileMakeContent"); //$NON-NLS-1$
    private String newFileMakeIntro = Messages.getString("TinyOSNewWizard.newFileMakeIntro");
    // private String newMakeOptionsContent = Messages.getString("TinyOSNewWizard.newMakeOptionsContent"); //$NON-NLS-1$
    private String moduleNameSuffix = "M";

    /**
     * Constructor for TinyOSNewWizard.
     */
    public TinyOSNewWizard() {
        super();
        setNeedsProgressMonitor(true);
    }

    /**
     * Adding the page to the wizard.
     */

    @Override
    public void addPages() {
        page = new TinyOSNewWizardPage(selection);
        addPage(page);
    }

    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    @Override
    public boolean performFinish() {
        final String containerName = page.getContainerName();
        final String fileName = page.getFileName();
        IRunnableWithProgress op = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                try {
                    monitor.beginTask("Creating Files", 4); //$NON-NLS-1$

                    // Create configuration - file
                    createFile(containerName, fileName, 
                            StringUtility.replaceAll(newFileConfigurationContent,
                                    "$file",
                                    StringUtility.replaceLast(fileName,".nc","")), 
                                    monitor);

                    // Create module file
                    String moduleFile = StringUtility.replaceLast(fileName,".",moduleNameSuffix+".");
                    createFile(containerName,moduleFile, 
                            newFileModuleContent.replaceFirst("\\$file",moduleFile.replaceAll("\\.nc","")),
                            monitor);

                    // Create make file

                    createFile(containerName, "Makefile", newFileMakeIntro+
                            "COMPONENT="+StringUtility.replaceLast(fileName,".nc","")+"\n"+StringUtility.replaceLast(newFileMakeContent,"$file",StringUtility.replaceLast(fileName,".nc","")),
                            monitor);


                } catch (CoreException e) {
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.done();
                }
            }

        };
        try {
            getContainer().run(true, false, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), "Error", realException.getMessage()); //$NON-NLS-1$
            return false;
        }
        return true;
    }


    private void createFile(String containerName, String fileName, String content, IProgressMonitor monitor) 
    throws CoreException {		
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(new Path(containerName));
        if (!resource.exists() || !(resource instanceof IContainer)) {
            throwCoreException("Container \"" + containerName + "\" does not exist."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        IContainer container = (IContainer) resource;
        monitor.worked(1);
        final IFile file = container.getFile(new Path(fileName));
        try {
            InputStream stream = new ByteArrayInputStream(content.getBytes());
            if (file.exists()) {
                file.setContents(stream, true, true, monitor);
            } else {
                file.create(stream, true, monitor);
            }
            stream.close();
        } catch (IOException e) {
        }
        monitor.worked(1);
    }

    /**
     * The worker method. It will find the container, create the
     * file if missing or just replace its contents, and open
     * the editor on the newly created file.
     */
    /*
	private void doFinish(
		String containerName,
		String fileName,
		IProgressMonitor monitor)
		throws CoreException {
		// Replace Modulname in String
		newFileConfigurationContent = newFileConfigurationContent.replaceAll("$file",fileName); //$NON-NLS-1$

		// create a sample file
		monitor.beginTask("Creating " + fileName, 2); //$NON-NLS-1$
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist."); //$NON-NLS-1$ //$NON-NLS-2$
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		try {
			InputStream stream = openContentStream();
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		}
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing..."); //$NON-NLS-1$
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}
     */

    private void throwCoreException(String message) throws CoreException {
        IStatus status =
            new Status(IStatus.ERROR, "TinyOS", IStatus.OK, message, null); //$NON-NLS-1$
        throw new CoreException(status);
    }

    /**
     * We will accept the selection in the workbench to see if
     * we can initialize from it.
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }
}