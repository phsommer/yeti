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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import tinyos.yeti.environment.basic.tools.BaseSetting;

public class ToolSelection {
	private IToolPage[] tools;
	private TabFolder folder;
	
	public ToolSelection( IToolPage... pages ){
		tools = pages;
	}
	
	public void createControl( Composite parent ){
		folder = new TabFolder( parent, SWT.TOP );
		
		for( IToolPage page : tools ){
            TabItem item = new TabItem( folder, SWT.NONE );
            item.setText( page.getToolLabel() );
            page.createControl( folder );
            item.setControl( page.getControl() );
        }
        folder.setSelection( 0 );
	}
	
	public Control getControl(){
		return folder;
	}
	
	public void write( BaseSetting setting ){
		setting.setTool( tools[ folder.getSelectionIndex() ].getToolName() );
        for( IToolPage tool : tools ){
            tool.write( setting );
        }
        
	}
	
	public void read( BaseSetting setting ){
		String tool = setting.getTool();
        for( int i = 0, n = tools.length; i<n; i++ ){
            if( tools[i].getToolName().equals( tool )){
                folder.setSelection( i );
                break;
            }
        }
        
        for( IToolPage page : tools ){
            page.read( setting );
        }
	}
}
