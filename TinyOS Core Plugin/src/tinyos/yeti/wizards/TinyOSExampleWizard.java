/**
 * <p>Titre : EfENewProjectWizard</p>
 * <p>Description: Assistant nouveau projet</p>
 * <p>Projet : EfEPlugin</p>
 * <p>Description Projet : Eiffel for Eclipse</p>
 * <p>Derni�re Modification : 06-04-04</p>
 * <p>Copyright : Copyright (c) 2004</p>
 * <p>Soci�t� : Universit� de Tours - DESS SIR Blois</p>
 * @author Audineau F. - H�lias M.
 * @version 1.0 
 **/

package tinyos.yeti.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.utility.ProjectTOSUtility;

public class TinyOSExampleWizard extends BasicNewResourceWizard implements INewWizard, IExecutableExtension {
    protected TinyOSExampleWizardPage projectPage;

    protected IConfigurationElement configurationElement;
    protected IProject newProject;

    public TinyOSExampleWizard() {
        setWindowTitle( "TinyOS Import Example" );
    }

    @Override
    public boolean performFinish() {
        IRunnableWithProgress projectCreationOperation =
            new WorkspaceModifyDelegatingOperation(
                    getProjectCreationRunnable());
        try {
            getContainer().run(false, true, projectCreationOperation);
        }
        catch( Exception e ){
            TinyOSPlugin.getDefault().getLog().log( new Status( Status.ERROR, TinyOSPlugin.PLUGIN_ID, 0, e.getMessage(), e ) );
            return false;
        }

        BasicNewProjectResourceWizard.updatePerspective(configurationElement);
        selectAndReveal(newProject);

        return true;
    }

    protected IRunnableWithProgress getProjectCreationRunnable() {
        return new IRunnableWithProgress() {
            public void run( IProgressMonitor monitor ) throws InvocationTargetException, InterruptedException {
                ProjectTOS project = null;
                try{
                    if( monitor == null )
                        monitor = new NullProgressMonitor();

                    monitor.beginTask( "Create Project", 400 );

                    project = ProjectTOSUtility.createEmptyProject(
                            projectPage.getProjectHandle(),
                            projectPage.getLocationPath(),
                            new SubProgressMonitor( monitor, 100 ) );

                    if( project == null || monitor.isCanceled() ){
                        monitor.done();
                        return;
                    }

                    File source = projectPage.getEnvironment().getExampleAppDirectory( projectPage.getApplicationName() );
                    byte[] makefile = ProjectTOSUtility.copyToProject( project, source, new SubProgressMonitor( monitor, 100 ) );
                    
                    String application;
                    
                    if( makefile == null ){
                        ProjectTOSUtility.createMakefile( project, new SubProgressMonitor( monitor, 100 ) );
                        application = null;
                    }
                    else{
                        String content = new String( makefile );
                        ProjectTOSUtility.createMakefile( project, content, new SubProgressMonitor( monitor, 100 ) );
                        application = ProjectTOSUtility.getApplicationFromMakefile( content );
                    }

                    // create make options
                    String target = projectPage.getTarget();
                    if( target != null ){
                    	ProjectTOSUtility.createDefaultMakeTargetSkeleton( project, target, application, new SubProgressMonitor( monitor, 100 ));
                    }

                    // save Environment
                    ProjectTOSUtility.createEnvironmentEntry( project, projectPage.getEnvironment() );
                }
                catch( CoreException e ){
                    throw new InvocationTargetException( e );
                }
                finally {
                    if( project != null ){
                        project.initialize();
                    }
                    monitor.done();
                }
            }
        };
    }

    @Override
    public void addPages() {
        super.addPages();
        projectPage = new TinyOSExampleWizardPage("NewProjectTinyOS");
        projectPage.setTitle("Import an existing TinyOS-Application");
        projectPage.setDescription("");
        addPage(projectPage);
    }

    public void setInitializationData(
            IConfigurationElement config,
            String propertyName,
            Object data) throws CoreException {
        configurationElement = config;
    }


}