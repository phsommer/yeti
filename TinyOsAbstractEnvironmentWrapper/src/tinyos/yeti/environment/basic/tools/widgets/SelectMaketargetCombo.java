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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.make.IMakeTarget;
import tinyos.yeti.make.IProjectMakeTargets;
import tinyos.yeti.make.targets.IMakeTargetMorpheable;

/**
 * Allows the user to select an {@link IMakeTarget} of a project.
 * @author Benjamin Sigg
 */
public class SelectMaketargetCombo{
    private boolean allowNullEntry;

    private IMakeTargetMorpheable[] choices;
    private int selection = -1;

    private ProjectTOS project;
    private Combo combo;

    public void createControl( Composite parent ){
        combo = new Combo( parent, SWT.DROP_DOWN | SWT.READ_ONLY );
        fillCombo();
    }
    
    public Control getControl(){
        return combo;
    }
    
    public void setProject( ProjectTOS project ){
        this.project = project;
        fillCombo();
    }
    
    public void setAllowNullEntry( boolean allowNullEntry ){
        this.allowNullEntry = allowNullEntry;
        fillCombo();
    }
    
    public IMakeTargetMorpheable getSelection(){
        int index = combo.getSelectionIndex();
        
        if( index < 0 )
            return null;
        
        return choices[ index ];
    }
    
    public void select( IMakeTargetMorpheable target ){
        if( choices != null ){
            selection = -1;
            for( int i = 0, n = choices.length; i<n; i++ ){
                if( choices[i] == target ){
                    selection = i;
                    break;
                }
            }
            fillCombo();
        }
    }

    private void fillCombo(){
        if( project == null || combo == null || combo.isDisposed() )
            return;
        
        combo.removeAll();
        
        IProjectMakeTargets projectTargets = project.getMakeTargets();

        IMakeTargetMorpheable[] targets = projectTargets.getSelectableTargets();
        IMakeTargetMorpheable defaultTarget = projectTargets.getSelectedTarget();
        
        if( allowNullEntry ){
            choices = new IMakeTargetMorpheable[ targets.length + 1 ];
            System.arraycopy( targets, 0, choices, 1, targets.length );
        }
        else{
            choices = targets;
        }
        
        int selection = this.selection;
        for( int i = 0, n = choices.length; i<n; i++ ){
            String name = choices[i] == null ? "" : projectTargets.getNameForSelectable( choices[i] );
            if( choices[i] == defaultTarget ){
                if( this.selection == -1 ){
                    selection = i;
                }
                name = name + " (build)";
            }
            combo.add( name );
        }
        if( combo.getItemCount() > selection ){
            combo.select( selection );
            this.selection = -1;
        }
    }
}
