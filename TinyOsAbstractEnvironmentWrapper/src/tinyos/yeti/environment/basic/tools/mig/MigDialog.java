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
package tinyos.yeti.environment.basic.tools.mig;

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
import org.eclipse.swt.widgets.Text;
import org.xml.sax.SAXException;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.environment.basic.TinyOSAbstractEnvironmentPlugin;
import tinyos.yeti.environment.basic.tools.widgets.CSharpTool;
import tinyos.yeti.environment.basic.tools.widgets.CTool;
import tinyos.yeti.environment.basic.tools.widgets.JavaTool;
import tinyos.yeti.environment.basic.tools.widgets.NesCFileSelection;
import tinyos.yeti.environment.basic.tools.widgets.OutputFileChoice;
import tinyos.yeti.environment.basic.tools.widgets.PythonTool;
import tinyos.yeti.environment.basic.tools.widgets.SaveAsGroup;
import tinyos.yeti.environment.basic.tools.widgets.SelectMaketargetCombo;
import tinyos.yeti.environment.basic.tools.widgets.ToolSelection;
import tinyos.yeti.utility.XReadStack;
import tinyos.yeti.utility.XWriteStack;

/**
 * A dialog for setting up the mig-tool.
 * @author Benjamin Sigg
 */
public class MigDialog extends TitleAreaDialog{
    private ProjectTOS project;

    private MigSetting result;
    
    private IFile migSettingFile;
    private SaveAsGroup migFileName = new SaveAsGroup( "mig" );

    private SelectMaketargetCombo maketarget = new SelectMaketargetCombo();
    private OutputFileChoice outputFile = new OutputFileChoice();
    
    private NesCFileSelection msgFormatFile = new NesCFileSelection();
    private Text msgType;
    
    private ToolSelection tool = new ToolSelection( new JavaTool(), new PythonTool(), new CSharpTool(), new CTool() );
    
    public MigDialog( Shell parentShell ){
        super( parentShell );
    }
    
    public MigSetting openDialog( IFile file ){
        result = null;
        migSettingFile = file;
        open();
        return result;
    }
    
    public void setProject( ProjectTOS project ){
        this.project = project;
        maketarget.setProject( project );
        msgFormatFile.setProject( project );
    }
    
    @Override
    protected void okPressed(){
        result = createSetting();
        
        String name = migFileName.getName();
        if( name != null ){
            saveAs( name );
        }
        
        super.okPressed();
    }
    
    public MigSetting createSetting(){
        MigSetting setting = new MigSetting( project );
        setting.setTarget( maketarget.getSelection() );
        setting.setMessageFormatFile( msgFormatFile.getFile() );
        setting.setMessageType( msgType.getText() );
        setting.setOutput( outputFile.getFile() );
        tool.write( setting );
        
        return setting;
    }
    
    public void putSetting( MigSetting setting ){
        maketarget.select( setting.getTarget() );
        msgFormatFile.setFile( nonull( setting.getMessageFormatFile() ) );
        outputFile.setFile( nonull( setting.getOutput() ) );
        msgType.setText( nonull( setting.getMessageType() ) );
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
        MigSetting setting = createSetting();
        setting.write( xml );
        xml.pop();
    }
    
    private void load( IFile file ){
        try{
            String path = file.getProjectRelativePath().toString();
            migFileName.setName( path );
            
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
            MigSetting setting = new MigSetting( project );
            setting.read( xml );
            putSetting( setting );
            xml.pop();
        }
    }
    

    @Override
    protected Control createDialogArea( Composite parent ){
        setTitle( "MIG" );
        getShell().setText( "Message Interface Generator" );
        setMessage( "message interface generator for nesC: a tool to generate code to\n" +
        		"process nesC messages (which are specified by C types)." );
        
        Composite base = new Composite( parent, SWT.NONE );
        
        base.setLayout( new GridLayout( 1, false ));
        base.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ));
        
        // name
        migFileName.createControl( base );
        migFileName.getControl().setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ));
        
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
        
        // format
        Label msgFormatFileLabel = new Label( optionsGroup, SWT.NONE );
        msgFormatFileLabel.setText( "Message Format File" );
        msgFormatFileLabel.setToolTipText( "A nesC file that uses the type which is to be extracted" );
        msgFormatFileLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ));
        
        msgFormatFile.createControl( optionsGroup );
        msgFormatFile.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        
        // type
        Label msgTypeLabel = new Label( optionsGroup, SWT.NONE );
        msgTypeLabel.setText( "Message Type" );
        msgTypeLabel.setToolTipText( "The C type of the message to process, must be defined as struct, nx_struct, union or nx_union" );
        
        msgType = new Text( optionsGroup, SWT.SINGLE | SWT.BORDER );
        msgType.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        
        // output file
        Label outputFileLabel = new Label( optionsGroup, SWT.NONE );
        outputFileLabel.setText( "Output file" );
        outputFileLabel.setToolTipText( "Path to the file which will be created by ncg" );
        outputFileLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
        
        outputFile.createControl( optionsGroup );
        outputFile.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        
        // tool
        Group toolGroup = new Group( base, SWT.NONE );
        toolGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        toolGroup.setLayout( new GridLayout() );
        toolGroup.setText( "Tool" );
        tool.createControl( toolGroup );
        tool.getControl().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        
        if( migSettingFile != null ){
            load( migSettingFile );
        }
        
        return base;
    }
}
