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
package tinyos.yeti.wizards.importing;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.wizards.Messages;
import tinyos.yeti.wizards.content.EnvironmentCombo;
import tinyos.yeti.wizards.content.PlatformCombo;

public class FileSystemWizardPage extends WizardPage {
    private static final String WizardNewProjectCreationPage_projectNameEmpty = 
        Messages.getString("TiynOSNewProjectCreationPage.projectNameEmpty"); //$NON-NLS-1$
    
    private DirectoryFieldEditor importDirectory;
    private Text projectName;

    private EnvironmentCombo environment;
    private PlatformCombo platform;
    
    
    public FileSystemWizardPage(){
        super( "import page", "Import TinyOS Project from source", null );
        setDescription( "Import a set of source files as new project." );
    }

    public void createControl( Composite parent ){
        Composite base = new Composite( parent, SWT.NONE );
        base.setLayout( new GridLayout( 1, true ) );
        
        Composite top = new Composite( base, SWT.NONE );
        top.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );
        top.setLayout( new GridLayout( 1, true ) );
        
        Composite importDirectoryBase = new Composite( top, SWT.NONE );
        importDirectoryBase.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        importDirectory = new DirectoryFieldEditor( "directorySelect", "Select Directory: ", importDirectoryBase );
        importDirectoryBase.setLayout( new GridLayout( importDirectory.getNumberOfControls(), false ) );
        importDirectory.fillIntoGrid( importDirectoryBase, importDirectory.getNumberOfControls() );
        importDirectory.setEmptyStringAllowed( false );
        
        Composite projectBase = new Composite( top, SWT.NONE );
        projectBase.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        projectBase.setLayout( new GridLayout( 2, false ) );
        
        Label projectLabel = new Label( projectBase, SWT.NONE );
        projectLabel.setText( "Project: " );
        projectLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false ) );
        projectName = new Text( projectBase, SWT.BORDER );
        projectName.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        
        Label environmentLabel = new Label( projectBase, SWT.NONE );
        environmentLabel.setText( "Environment: " );
        environmentLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false ) );
        environment = new EnvironmentCombo();
        environment.getControl( projectBase ).setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        
        Label platformLabel = new Label( projectBase, SWT.NONE );
        platformLabel.setText( "Platform: " );
        platformLabel.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false ) );
        platform = new PlatformCombo();
        platform.getControl( projectBase ).setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        platform.setEnvironment( environment );
        
        projectName.addModifyListener( new ModifyListener(){
            public void modifyText( ModifyEvent e ){
                validatePage();
            }
        });
        
        platform.getControl( null ).addSelectionListener( new SelectionListener(){
            public void widgetSelected( SelectionEvent e ){
                validatePage();
            }
            public void widgetDefaultSelected( SelectionEvent e ){
                validatePage();
            }
        });
        
        importDirectory.getTextControl( importDirectoryBase ).addModifyListener( new ModifyListener(){
            public void modifyText( ModifyEvent e ){
                validatePage();
            }
        });
        
        // importDirectory.setPage( this );
        setControl( base );
        
        validatePage();
    }
    
    public String getProjectName(){
        return projectName.getText();
    }
    
    public String getImportDirectory(){
        return importDirectory.getStringValue();
    }
    
    public IEnvironment getEnvironment(){
        return environment.getEnvironment();
    }
    
    public IPlatform getPlatform(){
        return platform.getPlatform();
    }
    
    protected boolean validatePage(){
        File directory = new File( importDirectory.getStringValue() );
        if( !(directory.exists() && directory.isDirectory()) ){
            setMessage( "Invalid directory: '" + importDirectory.getStringValue() + "'", ERROR );
            setPageComplete( false );
            return false;
        }
        
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        String project = getProjectName();
        if (project.equals("")) { //$NON-NLS-1$
            setMessage( WizardNewProjectCreationPage_projectNameEmpty, ERROR );
            setPageComplete( false );
            return false;
        }

        IStatus nameStatus = workspace.validateName(project, IResource.PROJECT);
        if( !nameStatus.isOK() ){
            setMessage( nameStatus.getMessage(), ERROR );
            setPageComplete( false );
            return false;
        }
        
        if( workspace.getRoot().getProject( project ).exists() ){
            setMessage( "Project '" + project + "' already exists.", ERROR );
            setPageComplete( false );
            return false;
        }

        if( environment.getEnvironment() == null ){
            setMessage( "No environment set", ERROR );
            setPageComplete( false );
            return false;
        }
        
        if( platform.getPlatform() == null ){
            setMessage( "No platform set", ERROR );
            setPageComplete( false );
            return false;
        }
        
        setPageComplete( true );
        setMessage( "Create a new project by copying source files from another location." );
        return true;
    }
    
}
