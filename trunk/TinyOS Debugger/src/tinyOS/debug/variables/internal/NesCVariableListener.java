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
package tinyOS.debug.variables.internal;

import java.util.Arrays;
import java.util.Vector;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IVariable;

import tinyOS.debug.TinyOSDebugPlugin;
import tinyOS.debug.variables.INesCVariableListener;
import tinyOS.debug.variables.IVariableDescriptor;
import tinyOS.debug.variables.IVariableManager;

/**
 * Listens for debug events and adds Nesc Variables to the list of registered variables for all debug events that originate from CDT.
 * @author snellen
 *
 */
public class NesCVariableListener implements IDebugEventSetListener, INesCVariableListener {

	/**
	 * Construct a NesCVariableListener and start listening for debug events.
	 * @param varMan The variable manager to operate on.
	 */
	public NesCVariableListener(IVariableManager varMan) {
		super();
		m_varMan = varMan;
		DebugPlugin.getDefault().addDebugEventListener( this );
	}

	/* (non-Javadoc)
	 * @see tinyOS.debug.util.variables.internal.INesCVariableListener#targetIsRunning()
	 */
	public boolean targetIsRunning() {
		return m_isRunning;
	}

	/* (non-Javadoc)
	 * @see tinyOS.debug.util.variables.internal.INesCVariableListener#targetIsTerminated()
	 */
	public boolean targetIsTerminated() {
		return m_isTerminated;
	}

	/* (non-Javadoc)
	 * @see tinyOS.debug.util.variables.internal.INesCVariableListener#stop()
	 */
	public void stop() {
		DebugPlugin.getDefault().removeDebugEventListener( this );
	}

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		for(DebugEvent event:events) {
			if(event.getKind() == DebugEvent.TERMINATE) {
				m_isTerminated = true;
				m_isRunning = false;
			}
			if(event.getKind() == DebugEvent.RESUME) {
				m_isRunning = true;
			}
			if(event.getKind() == DebugEvent.SUSPEND) {
				m_isRunning = false;
			}
			if(event.getKind() == DebugEvent.CREATE) {
				m_isRunning = true;
				m_isTerminated = false;
			}
		}

		addNescVariables(events);
	}

	private void addNescVariables(DebugEvent[] events) {
		if(m_isTerminated)
			return;

		
		for(DebugEvent event : events)
		{	
			Object element = event.getSource();

			if ( element instanceof IAdaptable ) {
				IAdaptable adaptable = (IAdaptable)element;

				ILaunch launch = (ILaunch) adaptable.getAdapter(ILaunch.class);
				NesCVariableNameParser varPars = new NesCVariableNameParser(new NesCSeparatorFromCDTLaunch(launch));
				
				IVariable[] variables = m_varMan.getRegisteredVariables(adaptable);
				if (variables != null ) {
					// Save the name of all registered variables
					Vector<String> existingVars = new Vector<String>();
					for(IVariable var : variables){
						try {
							existingVars.add(var.getName());
						} catch (DebugException ex) {
							TinyOSDebugPlugin.getDefault().log("Exception while adding variable: "+var.toString(), ex);
						}
					}
					// Compile a list of all potential NescC Variables that are not yet registered
					Vector<IVariableDescriptor> descriptors = new Vector<IVariableDescriptor>();
					for(IVariableDescriptor globDesc : m_varMan.getAvailableVariableDescriptors(adaptable)){
						if(varPars.isNesCVariable(globDesc.getName()) 
								&& !existingVars.contains(globDesc.getName()) ) {
							descriptors.add(globDesc);	
						}
					}
					// Add all NesC variables
					if(descriptors.size() > 0) {
						descriptors.addAll(Arrays.asList(m_varMan.getRegisteredVariableDescriptors(adaptable)));
						IVariableDescriptor[] array = (IVariableDescriptor[])descriptors.toArray(new IVariableDescriptor[descriptors.size()]);
						m_varMan.registerVariables(array, adaptable);
					}
				}

			}
		}
	}

	private boolean m_isTerminated = false;
	private boolean m_isRunning = false;
	private IVariableManager m_varMan;

}
