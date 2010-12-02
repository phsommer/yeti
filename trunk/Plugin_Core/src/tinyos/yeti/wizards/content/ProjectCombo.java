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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;

/**
 * Combo showing all available projects.
 * @author Benjamin Sigg
 */
public class ProjectCombo extends GenericObserved<IProject>{
    private Combo combo;
    private IProject[] projects;
    
    public ProjectCombo( Composite parent, int style ){
        combo = new Combo( parent, style | SWT.DROP_DOWN | SWT.READ_ONLY );
    }
    
    public Control getControl(){
        return combo;
    }
    
    private void changed(){
        trigger( getProject() );
    }
    
    public IProject getProject(){
        int index = combo.getSelectionIndex();
        if( index < 0 )
            return null;
        
        return projects[ index ];
    }
    
    public void init( IWorkbench workbench, IStructuredSelection selection ){
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        projects = workspace.getRoot().getProjects();
        projects = ResourceUtil.open( projects );
        
        for( IProject project : projects ){
            combo.add( project.getName() );
        }
        
        IProject project = ResourceUtil.getProject( selection );
        if( project == null && projects.length > 0 ){
            project = projects[0];
        }
        
        for( int i = 0, n = projects.length; i<n; i++ ){
            if( projects[i] == project ){
                combo.select( i );
                break;
            }
        }
        
        combo.addSelectionListener( new SelectionListener(){
            public void widgetDefaultSelected( SelectionEvent e ){
                changed();
            }
            public void widgetSelected( SelectionEvent e ){
                changed();
            }
        });
    }
}
