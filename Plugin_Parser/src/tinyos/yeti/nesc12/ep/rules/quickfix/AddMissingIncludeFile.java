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
package tinyos.yeti.nesc12.ep.rules.quickfix;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.graphics.Image;

import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.nesc12.parser.ParserInsights;
import tinyos.yeti.preprocessor.output.Insight;
import tinyos.yeti.preprocessor.output.Insights;
import tinyos.yeti.wizards.NewHeaderWizard;
import tinyos_parser.NesC12ParserPlugin;

public class AddMissingIncludeFile implements ISingleQuickfixRule{

    public void suggest( Insight error, QuickfixCollector collector ){
        if( !collector.getFile().isProjectFile() )
            return;

        if( !ParserInsights.isPreprocessor( error ))
            return;

        if( error.getId() != Insights.DIRECTIVE_INCLUDE_MISSING_FILE )
            return;

        if( error.get( Insights.DIRECTIVE_INCLUDE_MISSING_FILE_SYSTEMFILE_BOOLEAN, true ))
            return;

        String name = error.get( Insights.DIRECTIVE_INCLUDE_MISSING_FILE_FILENAME_STRING, null );
        if( name == null )
            return;

        if( name.length() >= 3 && name.endsWith( ".h" )){
            collector.addSingle( new CreateMissingFile( name ) );
        }
    }

    private class CreateMissingFile implements ISingleQuickfix{
        private String name;

        public CreateMissingFile( String name ){
            this.name = name;
        }

        public String getDescription(){
            return null;
        }

        public Image getImage(){
            return NesCIcons.icons().get( NesCIcons.ICON_INCLUDE );
        }

        public String getLabel(){
            return "Create file '" + name + "'";
        }

        public void run( Insight error, QuickfixInformation information ){
            IFile file = information.getProject().getModel().resource( information.getFile() );
            if( file != null ){
                IPath path = file.getProjectRelativePath();

                path = path.removeLastSegments( 1 ).append( name );
                final IFile next = information.getProject().getProject().getFile( path );

                if( !next.exists() ){
                    Job job = new Job( "Create file '" + name + "'" ){
                        @Override
                        protected IStatus run( IProgressMonitor monitor ){
                            String content = NewHeaderWizard.getSkeleton( name );
                            try{
                                next.create( new ByteArrayInputStream( content.getBytes() ), true, monitor );
                            }
                            catch ( CoreException e ){
                                NesC12ParserPlugin.getDefault().getLog().log( e.getStatus() );
                            }
                            monitor.done();
                            return Status.OK_STATUS;
                        }
                    };
                    job.setPriority( Job.SHORT );
                    job.setRule( next.getParent() );
                    job.schedule();
                }
            }
        }
    }
}
