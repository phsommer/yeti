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

import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.ep.parser.IMissingResourceRecorder;
import tinyos.yeti.preprocessor.IncludeFile;
import tinyos.yeti.preprocessor.IncludeProvider;

public class RecordingIncludeProvider implements IncludeProvider{
    private IncludeProvider provider;
    private IMissingResourceRecorder recorder;
    
    public RecordingIncludeProvider( IncludeProvider provider, IMissingResourceRecorder recorder ){
        this.provider = provider;
        this.recorder = recorder;
    }
    
    public void parsingFinished(){
        recorder = null;
    }
    
    public IncludeFile searchSystemFile( String filename, IProgressMonitor monitor ){
        IncludeFile result = provider.searchSystemFile( filename, monitor );
        if( recorder != null && result == null ){
            recorder.missingSystemFile( filename );
        }
        return result;
    }
    
    public IncludeFile searchUserFile( String filename, IProgressMonitor monitor ){
        IncludeFile result = provider.searchUserFile( filename, monitor );
        if( recorder != null && result == null ){
            recorder.missingUserFile( filename );
        }
        return result;
    }
}
