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
package tinyos.yeti.wizards.content;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;

/**
 * A {@link Composite} that allows the selection of one folder of a project.
 * @author Benjamin Sigg
 *
 */
public class ProjectContainerSelection extends GenericObserved<IContainer>{
    private Composite control;
    
    private ProjectCombo project;
    private ResourceTree containers;
    
    public ProjectContainerSelection( Composite parent, int style ){
        control = new Composite( parent, style );
        control.setLayout( new GridLayout( 2, false ) );
        
        Label label = new Label( control, SWT.NONE );
        label.setText( "Project: " );
        label.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );
        
        project = new ProjectCombo( control, SWT.NONE );
        project.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        
        containers = new SourceContainerTree( control, SWT.BORDER );
        containers.getControl().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 2, 1 ) );
    }
    
    public void init( IWorkbench workbench, IStructuredSelection selection ){
        project.init( workbench, selection );
        containers.setProject( project.getProject() );
        
        project.addListener( new GenericListener<IProject>(){
            public void trigger( IProject value ){
                containers.setProject( value );
            }
        });
        
        IContainer container = ResourceUtil.getContainer( selection );
        if( container != null ){
        	containers.setContainers( new IContainer[]{ container });
        }
        
        containers.addListener( this );
    }
    
    public Composite getControl(){
        return control;
    }
    
    public IContainer getContainer(){
        return containers.getContainer();
    }
}
