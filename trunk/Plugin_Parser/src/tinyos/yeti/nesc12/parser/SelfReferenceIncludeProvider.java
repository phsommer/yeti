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
import java.io.IOException;
import java.io.Reader;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.INesCDefinitionCollectorCallback;
import tinyos.yeti.nesc.IMultiReader;
import tinyos.yeti.preprocessor.FileInfo;
import tinyos.yeti.preprocessor.IncludeFile;
import tinyos.yeti.preprocessor.IncludeProvider;
import tinyos.yeti.preprocessor.SimpleIncludeFile;

/**
 * An {@link IncludeProvider} that can check whether a file includes itself
 * and return the file as it is seen by the parser and not as it is on the
 * disk.
 * @author Benjamin Sigg
 */
public class SelfReferenceIncludeProvider implements NesC12IncludeProvider{
    private IParseFile parseFile;
    private IMultiReader reader;

    private ProjectTOS project;

    private INesCDefinitionCollectorCallback callback;

    public SelfReferenceIncludeProvider( IParseFile parseFile, IMultiReader reader, ProjectTOS project ){
        this.parseFile = parseFile;
        this.reader = reader;
        this.project = project;
    }

    public void setCallback(INesCDefinitionCollectorCallback callback) {
        this.callback = callback;
    }

    public IncludeFile searchSystemFile( String filename, IProgressMonitor monitor ) {
        if( monitor == null )
            monitor = new NullProgressMonitor();

        try{
            monitor.beginTask( "Search file '" + filename + "'", 1000 );

            File file = TinyOSPlugin.getDefault().locate( project.getProject(), filename, true, new SubProgressMonitor( monitor, 100 ) );
            if( file == null || monitor.isCanceled() )
                return null;

            monitor.worked( 100 );

            if( callback != null )
                callback.fileIncluded( file, false, new SubProgressMonitor( monitor, 800 ) );

            if( monitor.isCanceled() )
                return null;

            IParseFile parseFile = project.getModel().parseFile( file );

            return new SimpleIncludeFile( file, new NesC12FileInfo( parseFile ) );
        }
        finally{
            monitor.done();
        }
    }

    public IncludeFile searchUserFile( String filename, IProgressMonitor monitor ) {
        if( monitor == null )
            monitor = new NullProgressMonitor();

        try{
            monitor.beginTask( "Search file '" + filename + "'", 1000 );

            TinyOSPlugin plugin = TinyOSPlugin.getDefault();
            if( plugin == null )
                return null;

            final File file = plugin.locate( project.getProject(), filename, false, new SubProgressMonitor( monitor, 100 ) );
            if( file == null || !file.isFile() ){
            	return null;
            }
            
            if( monitor.isCanceled() ){
            	return null;
            }
            
            monitor.worked( 100 );

            final IParseFile parsed = project.getModel().parseFile( file );

            if( callback != null )
                callback.fileIncluded( file, false, new SubProgressMonitor( monitor, 800 ) );
            
            if( monitor.isCanceled() )
                return null;

            if( parseFile.equals( parsed )){
                return new IncludeFile(){
                    private FileInfo info = new NesC12FileInfo( parsed );

                    public FileInfo getFile(){
                        return info;
                    }

                    public Reader read() {
                        try{
                            return reader.open();
                        }
                        catch( IOException ex ){
                            return null;
                        }
                    }
                };
            }
            else{
                return new SimpleIncludeFile( file, new NesC12FileInfo( parsed ) );
            }
        }
        finally{
            monitor.done();
        }
    }


}
