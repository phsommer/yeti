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
package tinyos.yeti.environment.winXP.execute;

import java.io.File;
import java.util.Map;

import tinyos.yeti.environment.basic.commands.ICommand;
import tinyos.yeti.environment.basic.commands.IExecutionResult;
import tinyos.yeti.environment.basic.path.IPathTranslator;

/**
 * A command that is wrapped around another command and provides the
 * {@link ICygwinCommand} interface.
 * @author Benjamin Sigg
 */
public class CygwinCommandWrapper<R> implements ICommand<R>, ICygwinCommand<R> {
    private ICommand<R> delegate;
    private File cygwinBash;
    private IPathTranslator translator;
    
    public CygwinCommandWrapper( ICommand<R> delegate ){
        this.delegate = delegate;
    }
    
    public CygwinCommandWrapper( ICommand<R> delegate, File cygwinBash, IPathTranslator translator ){
        this.delegate = delegate;
        this.cygwinBash = cygwinBash;
        this.translator = translator;
    }

    public String[] getCommand() {
        return delegate.getCommand();
    }

    public File getDirectory() {
        return delegate.getDirectory();
    }

    public Map<String, String> getEnvironmentParameters() {
         return delegate.getEnvironmentParameters();
    }
    
    public boolean useDefaultParameters() {
    	return delegate.useDefaultParameters();
    }

    public R result( IExecutionResult result ) {
         return delegate.result( result );
    }

    public boolean setup() {
        return delegate.setup();
    }

    public boolean shouldPrintSomething() {
        return delegate.shouldPrintSomething();
    }

    public boolean assumesInteractive(){
    	return true;
    }
    
    public void setCygwinBash( File cygwinBash ) {
        this.cygwinBash = cygwinBash;
    }
    
    public File getCygwinBash() {
        return cygwinBash;
    }
    
    public void setPathTranslator( IPathTranslator translator ) {
        this.translator = translator;
    }
    
    public IPathTranslator getPathTranslator() {
        return translator;
    }
}
