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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import tinyos.yeti.EnvironmentManager;
import tinyos.yeti.ep.IEnvironment;

public class EnvironmentCombo{
    private IEnvironment[] environments;
    
    private Combo combo;
    
    public Combo getControl( Composite parent ){
        if( combo == null ){
            environments = EnvironmentManager.getDefault().getEnvironmentsArray();
            
            IEnvironment base = EnvironmentManager.getDefault().getDefaultEnvironment();
            
            combo = new Combo( parent, SWT.BORDER | SWT.READ_ONLY );
            
            for( IEnvironment environment : environments ){
                combo.add( environment.getEnvironmentName() );
            }
            
            for( int i = 0, n = environments.length; i<n; i++ ){
                if( environments[i] == base ){
                    combo.select( i );
                    break;
                }
            }
        }
        return combo;
    }
    
    public IEnvironment getEnvironment(){
        int index = combo.getSelectionIndex();
        if( index >= 0 )
            return environments[ index ];
        
        return null;
    }
}
