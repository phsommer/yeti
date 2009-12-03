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
package tinyos.yeti.environment.basic.tools.ncg;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.xml.sax.SAXException;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.environment.basic.TinyOSAbstractEnvironmentPlugin;
import tinyos.yeti.environment.basic.tools.TitleAreaDialogCheckResultCollector;
import tinyos.yeti.environment.basic.tools.widgets.CTool;
import tinyos.yeti.environment.basic.tools.widgets.ConstantsAndFilenameTable;
import tinyos.yeti.environment.basic.tools.widgets.JavaTool;
import tinyos.yeti.environment.basic.tools.widgets.NesCFileSelection;
import tinyos.yeti.environment.basic.tools.widgets.OutputFileChoice;
import tinyos.yeti.environment.basic.tools.widgets.PythonTool;
import tinyos.yeti.environment.basic.tools.widgets.SaveAsGroup;
import tinyos.yeti.environment.basic.tools.widgets.SelectMaketargetCombo;
import tinyos.yeti.environment.basic.tools.widgets.ToolSelection;
import tinyos.yeti.utility.XReadStack;
import tinyos.yeti.utility.XWriteStack;

public class NcgDialog extends TitleAreaDialog{
    private ProjectTOS project;
    
    private IFile ncgSettingFile;
    private SaveAsGroup ncgFileName = new SaveAsGroup( "ncg" );
    private SelectMaketargetCombo maketarget = new SelectMaketargetCombo();
    private NesCFileSelection nesCFile = new NesCFileSelection();
    private OutputFileChoice outputFile = new OutputFileChoice();
    private ConstantsAndFilenameTable names = new ConstantsAndFilenameTable();
    private ToolSelection tool = new ToolSelection(
    		new JavaTool(),
            new PythonTool(),
            new CTool());
    
    private NcgSetting result;
    
    public NcgDialog( Shell parentShell ){
        super( parentShell );
        setBlockOnOpen( true );
    }
    
    public void setProject( ProjectTOS project ){
        this.project = project;
        maketarget.setProject( project );
        names.setProject( project );
        nesCFile.setProject( project );
    }
    
    public NcgSetting openDialog( IFile file ){
        result = null;
        ncgSettingFile = file;
        open();
        return result;
    }
    
    @Override
    protected void okPressed(){
        result = createSetting();
        
        String name = ncgFileName.getName();
        if( name != null ){
            saveAs( name );
        }
        
        super.okPressed();
    }
    
    public NcgSetting createSetting(){
        NcgSetting setting = new NcgSetting( project );
        setting.setTarget( maketarget.getSelection() );
        setting.setNesCFile( nesCFile.getFile() );
        setting.setOutput( outputFile.getFile() );
        names.write( setting );
        tool.write( setting );
        
        return setting;
    }
    
    public void putSetting( NcgSetting setting ){
        maketarget.select( setting.getTarget() );
        nesCFile.setFile( nonull( setting.getNesCFile() ));
        outputFile.setFile( nonull( setting.getOutput() ) );
        names.read( setting );
        tool.read( setting );
    }
    
    private String nonull( String text ){
        if( text == null )
            return "";
        
        return text;
    }
    
    private void saveAs( String name ){
        try{
            XWriteStack xml = new XWriteStack();
            write( xml );
            IFile file = project.getProject().getFile( name );
            
            if( file.exists() ){
                file.setContents( xml.toInputStream(), true, true, null );
            }
            else{
                file.create( xml.toInputStream(), true, null );
            }
        }
        catch( CoreException ex ){
            TinyOSAbstractEnvironmentPlugin.getDefault().getLog().log( ex.getStatus() );
        }
    }
    
    private void write( XWriteStack xml ){
        xml.push( "setting" );
        NcgSetting setting = createSetting();
        setting.write( xml );
        xml.pop();
    }
    
    private void load( IFile file ){
        try{
            String path = file.getProjectRelativePath().toString();
            ncgFileName.setName( path );
            
            InputStream in = file.getContents();
            XReadStack xml = new XReadStack( in );
            in.close();
            read( xml );
        }
        catch( IOException e ){
            TinyOSAbstractEnvironmentPlugin.getDefault().getLog().log( 
                    new Status( IStatus.WARNING, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, 0, e.getMessage(), e ) );
        }
        catch( SAXException e ){
            TinyOSAbstractEnvironmentPlugin.getDefault().getLog().log( 
                    new Status( IStatus.WARNING, TinyOSAbstractEnvironmentPlugin.PLUGIN_ID, 0, e.getMessage(), e ) );
        }
        catch( CoreException e ){
            TinyOSAbstractEnvironmentPlugin.getDefault().getLog().log( e.getStatus() );
        }
    }
    
    private void read( XReadStack xml ){
        if( xml.hasNext( "setting" )){
            xml.next( "setting" );
            NcgSetting setting = new NcgSetting( project );
            setting.read( xml );
            putSetting( setting );
            xml.pop();
        }
    }
   
    public void check(){
    	TitleAreaDialogCheckResultCollector check = new TitleAreaDialogCheckResultCollector( this );
    	check.information( "nesC constant generator: extracts constants from nesC files\n" +
        		"for use with other applications." );
    	
    	check.finish();
    }
    
    @Override
    protected Control createDialogArea( Composite parent ){
        setTitle( "NCG" );
        getShell().setText( "nesC Constant Generator" );
        
        Composite base = new Composite( parent, SWT.NONE );
        
        base.setLayout( new GridLayout( 1, false ));
        base.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ));
        
        // name
        ncgFileName.createControl( base );
        ncgFileName.getControl().setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ));
        
        // General options
        Group optionsGroup = new Group( base, SWT.NONE );
        optionsGroup.setText( "General" );
        optionsGroup.setLayout( new GridLayout( 2, false ) );
        optionsGroup.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );
        
        // Basic options (maketarget)
        Label maketargetLabel = new Label( optionsGroup, SWT.NONE );
        maketargetLabel.setText( "Paths (optional)" );
        maketargetLabel.setToolTipText( "A Make-Option which will be read in order to set up paths" );
        maketargetLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        
        maketarget.setAllowNullEntry( true );
        maketarget.createControl( optionsGroup );
        maketarget.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        
        // nesC file
        Label nesCFileLabel = new Label( optionsGroup, SWT.NONE );
        nesCFileLabel.setText( "nesC file" );
        nesCFileLabel.setToolTipText( "The name of the file which will be scanned" );
        nesCFileLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ));
        
        nesCFile.createControl( optionsGroup );
        nesCFile.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        
        // output file
        Label outputFileLabel = new Label( optionsGroup, SWT.NONE );
        outputFileLabel.setText( "Output file" );
        outputFileLabel.setToolTipText( "Path to the file which will be created by ncg" );
        outputFileLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        
        outputFile.createControl( optionsGroup );
        outputFile.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        
        // names
        Group namesGroup = new Group( base, SWT.NONE );
        namesGroup.setLayout( new GridLayout() );
        namesGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        namesGroup.setText( "Constants and Filenames" );
        names.createControl( namesGroup );
        names.getControl().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        
        // tool
        Group toolGroup = new Group( base, SWT.NONE );
        toolGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        toolGroup.setLayout( new GridLayout() );
        toolGroup.setText( "Tool" );
        tool.createControl( toolGroup );
        tool.getControl().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        
        if( ncgSettingFile != null ){
            load( ncgSettingFile );
        }
        
        check();
        
        return base;
    }
}
