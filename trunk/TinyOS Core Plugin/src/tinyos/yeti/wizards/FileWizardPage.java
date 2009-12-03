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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.wizards.content.GenericListener;
import tinyos.yeti.wizards.content.ProjectContainerSelection;

/**
 * A {@link WizardPage} that contains facilities to create a new file in
 * a project.
 * @author Benjamin Sigg
 *
 */
public abstract class FileWizardPage extends WizardPage{
    private IWorkbench workbench;
    private IStructuredSelection selection;

    private ProjectContainerSelection container;

    public FileWizardPage( String pageName, String title, ImageDescriptor titleImage ){
        super( pageName, title, titleImage );
    }

    public FileWizardPage( String pageName ){
        super( pageName );
    }

    public void init( IWorkbench workbench, IStructuredSelection selection ){
        this.workbench = workbench;
        this.selection = selection;

        if( container != null ){
            container.init( workbench, selection );
        }
    }

    public void createContainerControl( Composite parent, int style ){
        container = new ProjectContainerSelection( parent, style );
        container.addListener( new GenericListener<IContainer>(){
            public void trigger( IContainer value ){
                checkAllValidity();
            }
        });

        if( workbench != null || selection != null ){
            container.init( workbench, selection );
        }
    }

    public void addContainerSelectionListener( GenericListener<? super IContainer> listener ){
        container.addListener( listener );
    }

    public void removeContainerSelectionListener( GenericListener<? super IContainer> listener ){
        container.removeListener( listener );
    }

    public IContainer getSelectedContainer(){
        return container.getContainer();
    }

    public Control getContainerControl(){
        return container.getControl();
    }

    protected final void checkAllValidity(){
        boolean valid = checkValidity();
        setPageComplete( valid );
        if( valid )
            setErrorMessage( null );
    }

    protected boolean checkValidity(){
        if( getSelectedContainer() == null ){
            setErrorMessage( "No parent resource selected for new file" );
            return false;
        }

        return true;
    }

    /**
     * Ensures that <code>filename</code> either ends with one of the given
     * extensions, or adds an extension.
     * @param filename some name of a file, can be invalid
     * @param extensions the available extensions, if <code>null</code> or empty
     * nothing will happen and the method just returns <code>filename</code>
     * @return either <code>filename</code> or <code>filename + . + extensions[0]</code>
     */
    protected String ensureExtension( String filename, String[] extensions ){
        if( extensions == null || extensions.length == 0 )
            return filename;

        for( String extension : extensions ){
            if( filename.length() > extension.length() ){
                if( filename.charAt( filename.length()-extension.length()-1) == '.' ){
                    if( filename.endsWith( extension )){
                        return filename;
                    }
                }
            }
        }

        return filename + "." + extensions[0];
    }
    
    /**
     * Ensures that filename has no extension
     * @param filename some filename that might have an extension
     * @param extensions the extension
     * @return the filename without the given extensions
     */
    protected String withoutExtension( String filename, String[] extensions ){
        if( extensions == null || extensions.length == 0 )
            return filename;
        
        for( String extension : extensions ){
            if( filename.length() > extension.length() ){
                if( filename.charAt( filename.length()-extension.length()-1) == '.' ){
                    if( filename.endsWith( extension )){
                        return filename.substring( 0, filename.length()-1-extension.length() );
                    }
                }
            }
        }
        
        return filename;
    }

    /**
     * Checks whether a new file called <code>filename</code> can be created. If
     * not, then this wizard pages completion state and error message are set. 
     * @param filename the name or path of the file
     * @param extensions a set of extensions, the file must have one of them (will
     * be added automatically if missing)
     * @param parent the folder in which the element will be put
     * @return <code>true</code> if no errors were found
     */
    protected boolean checkFileValidity( String filename, String[] extensions ){
        filename = filename.trim();
        filename = ensureExtension( filename, extensions );
        String core = withoutExtension( filename, extensions ).trim();
        
        if( core.length() == 0 ){
            setErrorMessage( "Filename must not be empty" );
            return false;
        }
        
        if( !checkValidity( filename ) ){
            return false;
        }

        IContainer container = getSelectedContainer();
        if( container != null ){
            if( container.findMember( filename ) != null ){
                setErrorMessage( "File '" + filename + "' already exists" );
                return false;
            }
        }

        return true;
    }

    private boolean checkValidity( String filename ){
        IStatus status = ResourcesPlugin.getWorkspace().validateName( filename, IResource.FILE );
        if( status.getCode() == IStatus.OK )
            return true;
        else{
            setErrorMessage( status.getMessage() );
            return false;
        }
    }

    /**
     * Starts a job that will create a new file and returns after that job
     * is finished.
     * @param filename the name or path of the file
     * @param extensions a set of available extensions for the file, the first
     * one will be added to the filename if no other is set
     * @param content the initial content of the file, might be <code>null</code>
     * @param showAfterCreation if set, then the file will be opened in the editor area
     * @throws CoreException if creation of the file is not successful
     */
    protected void createFile( String filename, String[] extensions, String content, boolean showAfterCreation ) throws CoreException{
        IContainer container = getSelectedContainer();
        if( container == null )
            throw new CoreException( new Status( Status.ERROR, TinyOSPlugin.PLUGIN_ID, "no parent selected" ));
        
        filename = ensureExtension( filename.trim(), extensions );
        IFile file = container.getFile( new Path( filename ) );
        
        if( content == null )
            content = "";
        
        file.create( new ByteArrayInputStream( content.getBytes() ), false, null );
        
        if( showAfterCreation ){
            showFile( file );
        }
    }
    
    protected void showFile( IFile file ){
        IEditorDescriptor editor = workbench.getEditorRegistry().getDefaultEditor( file.getName() );
        if( editor == null )
            editor = workbench.getEditorRegistry().findEditor( IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID );
        
        IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
        try{
            page.openEditor( new FileEditorInput( file ), editor.getId() );
        }
        catch( PartInitException e ){
            TinyOSPlugin.getDefault().getLog().log( e.getStatus() );
        }
    }
}










