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
package tinyos.yeti.nesc12.parser;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.INesCDefinitionCollectorCallback;
import tinyos.yeti.preprocessor.IncludeFile;
import tinyos.yeti.preprocessor.SimpleIncludeFile;

public class ProjectIncludeProvider implements NesC12IncludeProvider{
    private ProjectTOS project;
    private INesCDefinitionCollectorCallback callback;

    public ProjectIncludeProvider( ProjectTOS project ){
        this.project = project;
    }

    public void setCallback(INesCDefinitionCollectorCallback callback) {
        this.callback = callback;
    }

    public IncludeFile searchSystemFile( String filename, IProgressMonitor monitor ){
        return searchFile( filename, true, monitor );
    }

    public IncludeFile searchUserFile( String filename, IProgressMonitor monitor ){
        return searchFile( filename, false, monitor );
    }
    
    private IncludeFile searchFile( String filename, boolean system, IProgressMonitor monitor ){
        if( monitor == null )
            monitor = new NullProgressMonitor();

        try{
            monitor.beginTask( "Search file '" + filename + "'", 1000 );
            
            TinyOSPlugin plugin = TinyOSPlugin.getDefault();
            if( plugin == null )
            	return null;

            File file = plugin.locate( project.getProject(), filename, system, new SubProgressMonitor( monitor, 100 ) );
            if( file == null || monitor.isCanceled() )
                return null;
            
            monitor.worked( 100 );

            if( callback != null )
                callback.fileIncluded( file, false, new SubProgressMonitor( monitor, 800 ) );

            if( monitor.isCanceled() )
                return null;
            
            IParseFile info = project.getModel().parseFile( file );
            if( info == null )
                return new SimpleIncludeFile( file, null );    
            else
                return new SimpleIncludeFile( file, new NesC12FileInfo( info ) );
        }
        finally{
            monitor.done();
        }
    }
}
