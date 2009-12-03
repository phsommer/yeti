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
package tinyos.yeti.search.model.scope;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ui.IWorkingSet;

public class WorkingSetScope extends AbstractScope{
	private IWorkingSet[] sets;

	public WorkingSetScope( IWorkingSet[] sets ){
		this.sets = sets;
	}

	public void visit( Visitor visitor, IProgressMonitor monitor ) throws CoreException{
		monitor.beginTask( "Visit", 10 );
		IProgressMonitor collectMonitor = new SubProgressMonitor( monitor, 1 );
		collectMonitor.beginTask( "Visit", 1 );
		
		Set<IFile> files = new HashSet<IFile>();
		
		
		for( IWorkingSet set : sets ){
			IAdaptable[] elements = set.getElements();
			for( IAdaptable element : elements ){
				IResource resource = (IResource)element.getAdapter( IResource.class );
				if( resource instanceof IFile ){
					addToCollection( resource, files );
				}
			}
		}
		
		collectMonitor.done();
		
		IProgressMonitor visitMonitor = new SubProgressMonitor( monitor, 9 );
		visitMonitor.beginTask( "Visit", 9 );
		
		for( IFile file : files ){
			visit( visitor, file, visitMonitor );
			if( monitor.isCanceled() ){
				break;
			}
		}
		
		monitor.done();
	}
	
	public String getDescription(){
		if( sets.length == 0 )
			return "";
		if( sets.length == 1 )
			return sets[0].getLabel();
		if( sets.length == 2 )
			return sets[0].getLabel() + ", " + sets[1].getLabel();
		return sets[0].getLabel() + ", " + sets[1].getLabel() + ", ...";
	}
}
