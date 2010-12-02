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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.IPlatform;

public class PlatformCombo{
    private IEnvironment environment;
    private IPlatform[] platforms;
    private Combo combo;

    public Combo getControl( Composite parent ){
        if( combo == null ){
            combo = new Combo( parent, SWT.BORDER | SWT.READ_ONLY );
        }

        return combo;
    }

    public void setEnvironment( final EnvironmentCombo environment ){
        environment.getControl( null ).addSelectionListener( new SelectionListener(){
            public void widgetDefaultSelected( SelectionEvent e ){
                setEnvironment( environment.getEnvironment() );
            }
            public void widgetSelected( SelectionEvent e ){
                setEnvironment( environment.getEnvironment() );
            }
        });
        setEnvironment( environment.getEnvironment() );
    }

    public void setEnvironment( IEnvironment environment ){
        if( this.environment != environment ){
            this.environment = environment;
            combo.removeAll();
            if( environment != null ){
                platforms = environment.getPlatforms();
                for( IPlatform platform : platforms ){
                    combo.add( platform.getName() );
                }
                if( platforms.length > 0 ){
                    combo.select( 0 );
                }
            }
        }
    }

    public IPlatform getPlatform(){
        int index = combo.getSelectionIndex();
        if( platforms == null || index < 0 || index >= platforms.length )
            return null;

        return platforms[ index ];
    }
}
