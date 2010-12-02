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
package tinyos.yeti.make.dialog.pages;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.make.dialog.AbstractMakeTargetDialogPage;
import tinyos.yeti.make.dialog.IMakeTargetInformation;
import tinyos.yeti.make.dialog.pages.CustomizationControls.Selection;
import tinyos.yeti.make.targets.MakeTargetPropertyKey;
import tinyos.yeti.make.targets.MakeTargetSkeleton;
import tinyos.yeti.wizards.content.NesCFileTree;

/**
 * To select the base component for compiling
 * @author Benjamin Sigg
 */
public class ComponentPage extends AbstractMakeTargetDialogPage<MakeTargetSkeleton> implements ICustomizeablePage{
    private NesCFileTree tree;
    private Text component;
    
    private boolean onChange = false;
    
    private CustomizationControls customization;
    
    private MakeTargetSkeleton sceleton;
    
    public ComponentPage( boolean showDefaultCustomChoice ){
        super( "Application" );
    
        if( showDefaultCustomChoice ){
        	customization = new CustomizationControls();
        }
        setDefaultMessage( "Select the applications top-level component. This component will be the entry point for ncc when compiling the application." );
        setImage( NesCIcons.icons().get( NesCIcons.ICON_APPLICATION ) );
    }
    
    public void setCustomEnabled( boolean enabled ){
	    tree.getControl().setEnabled( enabled );
	    component.setEnabled( enabled );
	    check();
	    contentChanged();
    }
    
    public void createControl( Composite parent ){
        Composite panel = new Composite( parent, SWT.NONE );
        panel.setLayout( new GridLayout( 1, false ) );
        
        Label label = new Label( panel, SWT.NONE );
        label.setText( "Top-level component:" );
        label.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );
        
        if( customization != null ){
        	customization.createControl( panel, false );
        	customization.setPage( this );
        	customization.getControl().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
        }
        
        tree = new NesCFileTree( panel, SWT.BORDER );
        tree.getControl().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        
        component = new Text( panel, SWT.BORDER );
        component.setLayoutData( new GridData( SWT.FILL, SWT.TOP, true, false ) );
        
        tree.getTree().addSelectionChangedListener( new ISelectionChangedListener(){
            public void selectionChanged( SelectionChangedEvent event ){
                if( !onChange ){
                    try{
                        onChange = true;
                        IResource selected = tree.getResource();
                        if( selected != null && "nc".equals( selected.getFileExtension())){
                            IPath path = selected.getProjectRelativePath();
                            String text = path.toString();
                            component.setText( text );
                        }
                    }
                    finally{
                        onChange = false;
                    }
                }
                
                check();
                contentChanged();
            }
        });
        
        component.addModifyListener( new ModifyListener(){
            public void modifyText( ModifyEvent e ){
                if( !onChange ){
                    try{
                        onChange = true;
                        String text = component.getText();
                        if( !text.endsWith( ".nc" ))
                        	text += ".nc";
                        tree.selectFile( text );
                    }
                    finally{
                        onChange = false;
                    }
                }
                
                check();
                contentChanged();
            }
        });
        
        setControl( panel );
    }
    
    @Override
    public void check( MakeTargetSkeleton maketarget, IMakeTargetInformation information ){
        tree.setProject( maketarget.getProject() );
        check();
    }
    
    private void check(){
    	if( customization != null && customization.getSelection() == Selection.DEFAULT ){
    		check( sceleton.getBackupProperty( MakeTargetPropertyKey.COMPONENT_FILE ));
    	}
    	else{
    		if( component != null && sceleton != null && sceleton.getProject() != null ){
    			try{
    				check( sceleton.getProject().getFile( new Path( component.getText() ) ) );
    			}
    			catch( IllegalArgumentException ex ){
    				// ignore
    			}
    		}
    	}
    }
    
    private void check( IFile file ){
        if( file == null )
            setWarning( "No main-component selected" );
        else{
        	if( !file.exists() ){
        		setError( "File for component does not exist: '" + file.getName() + "'" );
        		return;
        	}
        	if( !"nc".equals( file.getFileExtension() ) ){
        		setError( "File must be a *.nc file: '" + file.getName() + "'" );
        		return;
        	}
        	
        	setDefaultMessage();
        }
    }

    public void show( MakeTargetSkeleton maketarget, IMakeTargetInformation information ){
    	IFile value = maketarget.getCustomComponentFile();
    	this.sceleton = maketarget;
        
        tree.setProject( maketarget.getProject() );
        if( value != null ){
        	tree.select( value );
            component.setText( value.getProjectRelativePath().toOSString() );
        }
        else{
        	String text = maketarget.getCustomComponent();
        	if( text != null && !text.equals( "" )){
        		text += ".nc";
        		component.setText( text );
        	}
        	else{
        		component.setText( "" );
        	}
        }
        
        if( customization != null ){
        	IFile file = maketarget.getBackupProperty( MakeTargetPropertyKey.COMPONENT_FILE );
        	customization.setDefaultValue( file == null ? null : file.getProjectRelativePath().toOSString() );
        	
        	if( maketarget.isUseLocalProperty( MakeTargetPropertyKey.COMPONENT_FILE )){
        		customization.setSelection( Selection.CUSTOM );
        	}
        	else{
        		customization.setSelection( Selection.DEFAULT );
        	}
        }
        
        check();
    }

    public void store( MakeTargetSkeleton maketarget ){
    	if( maketarget.getProject() == null ){
    		String text = component.getText();
    		if( text.endsWith(  ".nc" ))
    			text = text.substring( 0, text.length()-3 );
    		maketarget.setCustomComponent( text );
    		maketarget.setCustomComponentFile( null );
	        if( customization != null ){
	        	boolean local = customization.getSelection() == Selection.CUSTOM;
	        	maketarget.setUseDefaultProperty( MakeTargetPropertyKey.COMPONENT_FILE, !local );
	        	maketarget.setUseLocalProperty( MakeTargetPropertyKey.COMPONENT_FILE, local );
	        }			
		}
    	else{
	    	IPath path = new Path( component.getText() );
	    	IFile file = null;
	    	try{
	    		file = maketarget.getProject().getFile( path );
	    	}
	    	catch( IllegalArgumentException ex ){
	    		// ignore
	    	}
	    	maketarget.setCustomComponentFile( file );
	    	
	        if( customization != null ){
	        	boolean local = customization.getSelection() == Selection.CUSTOM;
	        	maketarget.setUseDefaultProperty( MakeTargetPropertyKey.COMPONENT_FILE, !local );
	        	maketarget.setUseLocalProperty( MakeTargetPropertyKey.COMPONENT_FILE, local );
	        }
    	}
    }
    
    
}
