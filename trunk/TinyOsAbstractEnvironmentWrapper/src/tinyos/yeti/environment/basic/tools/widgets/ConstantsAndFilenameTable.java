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
package tinyos.yeti.environment.basic.tools.widgets;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.environment.basic.tools.ncg.NcgSetting;
import tinyos.yeti.make.dialog.pages.NavigationButtons;
import tinyos.yeti.wizards.content.HeaderFileTree;
import tinyos.yeti.wizards.content.ResourceTree;
import tinyos.yeti.wizards.content.SelectResourceDialog;

/**
 * A table showing header files and constant names. The use can edit the entries 
 * @author Benjamin Sigg
 */
public class ConstantsAndFilenameTable{
    private static final String ID_DELETE = "delete";
    private static final String ID_EDIT = "edit";
    private static final String ID_ADD_FILE = "file";
    private static final String ID_ADD_CONSTANT = "constant";
    
    private Composite base;
    private List list;
    private ProjectTOS project;
    
    public void createControl( Composite parent ){
        base = new Composite( parent, SWT.NONE );
        base.setLayout( new GridLayout( 2, false ) );
        
        list = new List( base, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
        list.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        
        Buttons buttons = new Buttons();
        buttons.createControl( base );
        buttons.getControl().setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, false, false ) );
    }
    
    public void setProject( ProjectTOS project ){
        this.project = project;
    }
    
    public Control getControl(){
        return base;
    }
    
    public void read( NcgSetting setting ){
        list.removeAll();
        String[] names = setting.getFilenamesOrConstants();
        if( names != null ){
            for( String name : names ){
                if( name != null ){
                    list.add( name );
                }
            }
        }
    }
    
    public void write( NcgSetting setting ){
        String[] names = new String[ list.getItemCount() ];
        for( int i = 0, n = names.length; i<n; i++ ){
            names[i] = list.getItem( i );
        }
        setting.setFilenamesOrConstants( names );
    }
    
    private void addFile(){
        String result = editFile( null );
        if( result != null ){
            list.add( result );
        }
    }
    
    private void addConstant(){
        String result = editConstant( null );
        if( result != null ){
            list.add( result );
        }
    }
    
    private void edit(){
        int index = list.getSelectionIndex();
        if( index >= 0 ){
            String item = list.getItem( index );
            String result;
            if( item.contains( "." )){
                // a file
                result = editFile( item );
            }
            else{
                result = editConstant( item );
            }
            if( result != null ){
                list.setItem( index, result );
            }
        }
    }
    
    private String editFile( String file ){
        SelectResourceDialog dialog = new SelectResourceDialog( getControl().getShell() ){
            @Override
            protected ResourceTree createTree( Composite parent ){
                HeaderFileTree tree = new HeaderFileTree( parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL );
                return tree;
            }
        };
        dialog.setTitle( "Select Header File" );
        dialog.setProject( project.getProject() );
        dialog.setBlockOnOpen( true );
        
        if( file != null ){
            IPath path = new Path( file );
            IFile resource = project.getProject().getFile( path );
            if( !resource.exists() ){
            	IContainer container = project.getLegacySourceContainer();
            	resource = container.getFile( path );
            }
            
            if( resource.exists() ){
                dialog.setSelection( resource );
            }
        }
        
        if( dialog.open() == SelectResourceDialog.OK ){
            IResource resource = dialog.getSelection();
            if( resource != null && resource instanceof IFile ){
                IPath path = resource.getProjectRelativePath();
                return path.toString();
            }
        }
        
        return null;
    }
    
    private String editConstant( String constant ){
        InputDialog input = new InputDialog( getControl().getShell(),
                "Constant", 
                "A constant must only consist of letters, numbers and '_'",
                constant, 
                new ConstantValidator() );
        int result = input.open();
        if( result == InputDialog.OK ){
            return input.getValue();
        }
        
        return null;
    }
    
    private void delete(){
        int[] indices = list.getSelectionIndices();
        if( indices != null )
            list.remove( indices );
    }
    
    private class Buttons extends NavigationButtons{
        public Buttons(){
            super( new String[]{ ID_ADD_FILE, ID_ADD_CONSTANT, ID_EDIT, ID_DELETE }, 
                    new String[]{ "Add file", "Add constant", "Edit", "Remove" } );
        }
        
        @Override
        protected void doOperation( String id ){
            if( ID_ADD_FILE.equals( id )){
                addFile();
            }
            if( ID_ADD_CONSTANT.equals( id )){
                addConstant();
            }
            if( ID_EDIT.equals( id )){
                edit();
            }
            if( ID_DELETE.equals( id )){
                delete();
            }
        }
    }
}
