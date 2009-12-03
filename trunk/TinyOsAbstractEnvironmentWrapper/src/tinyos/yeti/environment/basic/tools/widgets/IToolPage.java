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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import tinyos.yeti.environment.basic.tools.BaseSetting;

/**
 * The settings needed by some tool
 * @author Benjamin Sigg
 */
public interface IToolPage{
    public String getToolName();
    
    public String getToolLabel();
    
    public void createControl( Composite parent );
    
    public Control getControl();
    
    public void write( BaseSetting setting );
    
    public void read( BaseSetting setting );
}
