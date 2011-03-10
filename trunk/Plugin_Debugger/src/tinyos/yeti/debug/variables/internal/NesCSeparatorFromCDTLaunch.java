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
package tinyos.yeti.debug.variables.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

import tinyos.yeti.debug.TinyOSDebugPlugin;
import tinyos.yeti.debug.CDTAbstractionLayer.CDTLaunchConfigConst;
import tinyos.yeti.debug.variables.INesCSeparatorProvider;
import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.IPlatform;
import tinyos.yeti.nature.MissingNatureException;

public class NesCSeparatorFromCDTLaunch implements
INesCSeparatorProvider {

	private ILaunch launch;

	public NesCSeparatorFromCDTLaunch(ILaunch launch) {
		super();
		this.launch = launch;
	}

	@Override
	public String getSeparator() {
		if(launch != null) {
			try {
				ILaunchConfiguration config = launch.getLaunchConfiguration();
				if(config != null) {
					String projectName = config.getAttribute(CDTLaunchConfigConst.ATTR_PROJECT_NAME, (String)null);
					String platformName = getPlatformFromProgramName(config.getAttribute(CDTLaunchConfigConst.ATTR_PROGRAM_NAME,(String)null));
					IWorkspace workspace = ResourcesPlugin.getWorkspace();
					IProject project = workspace.getRoot().getProject(projectName);
					if(project != null) {
						ProjectTOS tinyOSProject = TinyOSPlugin.getDefault().getProjectTOS(project);
						if(tinyOSProject != null) {
							IEnvironment env = tinyOSProject.getEnvironment();
							if(env != null) {
								for(IPlatform pf:env.getPlatforms()){
									String name = pf.getName();
									if(name.equals(platformName)) {
										return pf.getNestedCVariableSeparator();
									}
								}
							}
						}
					}
				}
			}
			catch( MissingNatureException ex ){
				TinyOSDebugPlugin.getDefault().log("Exception while getting separator.",ex);
			}
			catch (CoreException e) {
				TinyOSDebugPlugin.getDefault().log("Exception while getting separator.",e);
			}
		}
		return null;
	}

	private String getPlatformFromProgramName(String progName) {
		if(progName != null) {
			String result = new String(progName);
			int index = result.lastIndexOf("/");
			if(index > 0) {
				result = result.substring(0, index);
				index = result.lastIndexOf("/");
				if(index > 0)
					return result.substring(index+1);
				else
					return result;
			}
		}
		return progName;
	}

}
