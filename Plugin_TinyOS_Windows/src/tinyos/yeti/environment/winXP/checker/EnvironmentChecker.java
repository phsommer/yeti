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
package tinyos.yeti.environment.winXP.checker;

import org.eclipse.core.runtime.CoreException;

import tinyos.yeti.environment.basic.checker.AbstractEnvironmentChecker;
import tinyos.yeti.environment.winXP.Environment;
import tinyos.yeti.environment.winXP.path.PathManager;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.IProjectCheckerCallback;

public class EnvironmentChecker extends AbstractEnvironmentChecker{
	@Override
	protected void check( IProjectCheckerCallback callback ) throws CoreException{
		PathManager paths = Environment.getEnvironment().getPathManager();
		
		// check cygwin
		checkDir( "Cygwin directory", paths.getCygwinRoot(), callback );
		checkFile( "cygwin-bash", paths.getCygwinBash(), callback );
		if( checkFile( "cygpath", paths.getCygwinCygpath(), callback )){
			checkDir( "TOSROOT", paths.getTosRootPath(), callback );
			checkDir( "TOSDIR", paths.getTosDirectoryPath(), callback );
			checkFile( "MAKERULES", paths.getMakerulesPath(), callback );
			checkDir( "examples directory", paths.getExampleDirectory(), callback );
		}
	}

	@Override
	protected IEnvironment getEnvironment(){
		return Environment.getEnvironment();
	}
}
