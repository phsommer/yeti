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
package tinyos.yeti.environment.basic.tools;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;

public class TitleAreaDialogCheckResultCollector implements ICheckResultCollector {
	private String message;
	private int severity = IMessageProvider.NONE;

	private TitleAreaDialog dialog;
	
	public TitleAreaDialogCheckResultCollector( TitleAreaDialog dialog ){
		this.dialog = dialog;
	}
	
	public void finish(){
		dialog.setMessage( message, severity );
	}
	
	private void put( String message, int type ){
		switch( type ){
			case IMessageProvider.INFORMATION:
				if( severity == IMessageProvider.NONE ){
					severity = IMessageProvider.INFORMATION;
					this.message = message;
				}
				break;
			case IMessageProvider.WARNING:
				if( severity == IMessageProvider.NONE || severity == IMessageProvider.INFORMATION ){
					severity = IMessageProvider.WARNING;
					this.message = message;
				}
				break;
			case IMessageProvider.ERROR:
				if( severity == IMessageProvider.NONE || severity == IMessageProvider.INFORMATION || severity == IMessageProvider.WARNING ){
					severity = IMessageProvider.ERROR;
					this.message = message;
				}
				break;
		}
	}
	
	public void error( String error ){
		put( error, IMessageProvider.ERROR );
	}

	public void information( String message ){
		put( message, IMessageProvider.INFORMATION );
	}

	public void warning( String warning ){
		put( warning, IMessageProvider.WARNING );
	}
}
