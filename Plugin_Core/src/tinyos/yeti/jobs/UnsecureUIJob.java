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
package tinyos.yeti.jobs;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

import tinyos.yeti.model.ProjectModel;

/**
 * This job does not allow joining if the joining thread is
 * a {@link ProjectModel#secureThread() secure thread}. This prevents
 * deadlocks.
 * @author Benjamin Sigg
 */
public abstract class UnsecureUIJob extends UIJob{
	private ProjectModel model;
	
	public UnsecureUIJob( ProjectModel model, String name ){
		super( name );
		this.model = model;
	}
	
	public UnsecureUIJob( ProjectModel model, Display jobDisplay, String name ){
		super( jobDisplay, name );
		this.model = model;
	}


	public void joinSecure() throws InterruptedException{
		if( model != null && model.secureThread() ){
			throw new IllegalStateException( "a secure thread is not allowed to wait for an UI thread" );
		}
		join();
	}
}
