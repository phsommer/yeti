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
package tinyos.yeti.views.make;

import java.util.Set;

import org.eclipse.swt.widgets.Display;

import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.make.MakeTarget;
import tinyos.yeti.make.MakeTargetManager;

public class CutTargetAction extends CopyTargetAction{
	public CutTargetAction( Display display ){
		super( display );
		setText( "Cut" );
	}
	
	@Override
	public void run() {
		super.run();
		Set<MakeTarget> targets = getSelection();
		MakeTargetManager manager = TinyOSPlugin.getDefault().getTargetManager();
		
		for( MakeTarget target : targets ){
			manager.removeTarget( target );
		}
	}
}
