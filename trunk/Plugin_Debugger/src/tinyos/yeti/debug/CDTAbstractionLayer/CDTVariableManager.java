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
package tinyos.yeti.debug.CDTAbstractionLayer;

import java.util.Vector;

import org.eclipse.cdt.debug.core.ICGlobalVariableManager;
import org.eclipse.cdt.debug.core.model.IGlobalVariableDescriptor;
import org.eclipse.cdt.debug.internal.core.CGlobalVariableManager;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.cdt.debug.internal.core.model.CVariableFactory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;

import tinyos.yeti.debug.TinyOSDebugPlugin;
import tinyos.yeti.debug.variables.IVariableDescriptor;
import tinyos.yeti.debug.variables.IVariableManager;
import tinyos.yeti.debug.variables.internal.VariableDescriptor;

public class CDTVariableManager implements IVariableManager {

	@Override
	public IVariableDescriptor[] getRegisteredVariableDescriptors(IAdaptable adaptable) {
		ICGlobalVariableManager gvm = (ICGlobalVariableManager) adaptable.getAdapter( ICGlobalVariableManager.class );
		if (gvm != null) {
			Vector<IVariableDescriptor> descriptors = new Vector<IVariableDescriptor>();
			for(IGlobalVariableDescriptor globDesc : gvm.getDescriptors()){
				descriptors.add(new VariableDescriptor(globDesc.getName(), globDesc.getPath()));
			}
			IVariableDescriptor[] array = (IVariableDescriptor[])descriptors.toArray(new IVariableDescriptor[descriptors.size()]);
			return array;
		}
		return null;
	}

	@Override
	public IVariable[] getRegisteredVariables(IAdaptable adaptable) {
		ICGlobalVariableManager igvm = (ICGlobalVariableManager) adaptable.getAdapter( ICGlobalVariableManager.class );
		if (igvm != null) {
			if( igvm instanceof CGlobalVariableManager) {
				CGlobalVariableManager gvm = (CGlobalVariableManager)igvm;
				return gvm.getGlobals();
			}
		}
		return null;
	}

	@Override
	public IVariableDescriptor[] getAvailableVariableDescriptors(IAdaptable adaptable) {
		CDebugTarget dbgTarget = (CDebugTarget) adaptable.getAdapter(CDebugTarget.class);
		if (dbgTarget != null) {
			try {
				Vector<IVariableDescriptor> descriptors = new Vector<IVariableDescriptor>();
				for(IGlobalVariableDescriptor globDesc : dbgTarget.getGlobals()){
					descriptors.add(new VariableDescriptor(globDesc.getName(), globDesc.getPath()));
				}
				IVariableDescriptor[] array = (IVariableDescriptor[])descriptors.toArray(new IVariableDescriptor[descriptors.size()]);
				return array;
			} catch (DebugException e) {
				TinyOSDebugPlugin.getDefault().log("Exception while getting global variables.",e);
			}
		}
		return null;
	}

	@Override
	public boolean registerVariables(IVariableDescriptor[] vars, IAdaptable adaptable) {
		ICGlobalVariableManager gvm = (ICGlobalVariableManager) adaptable.getAdapter( ICGlobalVariableManager.class );
		if (gvm != null) {
				Vector<IGlobalVariableDescriptor> descriptors = new Vector<IGlobalVariableDescriptor>();
				for(IVariableDescriptor desc : vars){
					descriptors.add( CVariableFactory.createGlobalVariableDescriptor(desc.getName(), desc.getPath()) );
				}

				if(descriptors.size() > 0) {
					IGlobalVariableDescriptor[] array = (IGlobalVariableDescriptor[])descriptors.toArray(new IGlobalVariableDescriptor[descriptors.size()]);
					try {
						gvm.addGlobals(array);
					} catch (DebugException e) {
						TinyOSDebugPlugin.getDefault().log("Exception while adding globals.",e);
					}
				}
				return true;
		}
		return false;
	}
}
