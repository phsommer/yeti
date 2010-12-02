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
package tinyos.yeti.environment.unix2.example;

import java.io.File;

import tinyos.yeti.environment.basic.example.AbstractExampleManager;
import tinyos.yeti.environment.unix2.Environment;
import tinyos.yeti.ep.IEnvironment;

public class ExampleManager extends AbstractExampleManager{
    private Environment environment;

    public ExampleManager( Environment environment ){
        this.environment = environment;
    }
    
    @Override
    protected File getExampleDirectory(){
    	String path = environment.getPathManager().getAppDir();
    	return environment.getPathManager().modelToSystem( path );
    }
    
    @Override
    protected IEnvironment getEnvironment(){
	    return environment;
    }
}
