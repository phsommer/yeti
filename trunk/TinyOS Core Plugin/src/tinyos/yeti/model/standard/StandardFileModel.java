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
package tinyos.yeti.model.standard;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.eclipse.core.resources.IContainer;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.IStorage;
import tinyos.yeti.model.FileModel;
import tinyos.yeti.model.ParseFile;
import tinyos.yeti.model.ProjectModel;

public class StandardFileModel extends FileModel<StandardFileModel.StandardParseFile>{
    public static final IGenericFactory<StandardParseFile> PARSE_FILE_FACTORY = 
        new IGenericFactory<StandardParseFile>(){
        
        public StandardParseFile create(){
            return null;
        }

        public void write( StandardParseFile value, IStorage storage ) throws IOException{
            storage.out().writeUTF( value.toFile().getAbsolutePath() );
        }
        
        public StandardParseFile read( StandardParseFile value, IStorage storage ) throws IOException{
            File file = new File( storage.in().readUTF() );
            return (StandardParseFile)storage.getProject().getModel().parseFile( file );
        }
    };
    
    public StandardFileModel( ProjectModel model ){
        super( model );
    }
    
    public IParseFile parseFile( String path ){
    	return parseFile( new File( path ) );
    }
    
    @Override
    protected StandardParseFile create( File file ){
        return new StandardParseFile( file, true, getModel().getProject() );
    }
    
    @Override
    protected StandardParseFile create( File file, Set<File> projectFiles ){
        StandardParseFile parseFile = new StandardParseFile( file, false, getModel().getProject() );
        parseFile.projectFileResolved = true;
        if( projectFiles.contains( file )){
        	parseFile.projectSourceFolder = resolveProjectFile( parseFile );
        }
        return parseFile;
    }

    @Override
    protected void setIndex( StandardParseFile file, int index ){
        file.setIndex( index );
    }

    @Override
    protected void setProjectFileResolved( StandardParseFile file, boolean resolved ){
        file.projectFileResolved = resolved;
    }

    public File locate( IParseFile file ){
        if( file instanceof StandardParseFile )
            return ((StandardParseFile)file).toFile();
        
        return null;
    }
    
    public class StandardParseFile extends ParseFile{
        private boolean projectFileResolved = false;
        private IContainer projectSourceFolder = null;
        
        public StandardParseFile( File file, boolean resolve, ProjectTOS project ){
            super( file, project );
            if( resolve ){
                projectFileResolved = true;
                projectSourceFolder = resolveProjectFile( this );
            }
        }

        public boolean isProjectFile() {
            if( !projectFileResolved ){
                projectFileResolved = true;
                projectSourceFolder = resolveProjectFile( this );
            }
            return projectSourceFolder != null;
        }
        
        public IContainer getProjectSourceContainer(){
        	return projectSourceFolder;
        }
    }
}
