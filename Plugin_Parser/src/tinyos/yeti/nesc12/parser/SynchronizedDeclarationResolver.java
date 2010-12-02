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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.jobs.FerryJob;
import tinyos.yeti.nesc12.ep.DeclarationResolver;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;

/**
 * A declaration resolver that synchronizes its calls such that a {@link ProjectTOS}
 * can be accessed from everywhere 
 * @author Benjamin Sigg
 */
public class SynchronizedDeclarationResolver implements DeclarationResolver{
    private ProjectTOS project;
    private DeclarationResolver resolver;
    private int priority = Job.SHORT;
    
    public SynchronizedDeclarationResolver( ProjectTOS project, DeclarationResolver resolver ){
        this.project = project;
        this.resolver = resolver;
    }
    
    public void parsingFinished(){
        resolver.parsingFinished();
    }
    
    public void setPriority( int priority ){
        this.priority = priority;
    }
    
    protected boolean runDirect(){
        Job job = Job.getJobManager().currentJob();
        if( job == null )
            return false;
        
        if( job.getRule() == null )
            return false;
        
        if( job.getRule().contains( project.getProject() ))
            return true;
        
        return false;
    }
    
    public IDeclaration resolve( final String name, IProgressMonitor monitor, final Kind... kind ){
        if( runDirect() ){
            return resolver.resolve( name, monitor, kind );
        }
        
        FerryJob<IDeclaration> job = new FerryJob<IDeclaration>( "resolve" ){
            @Override
            public IStatus run( IProgressMonitor monitor ){
                content = resolver.resolve( name, monitor, kind );
                return Status.OK_STATUS;
            }
        };

        job.setPriority( priority );
        project.getModel().runJob( job, monitor );
        return job.getContent();
    }
    
    public IDeclaration[] resolveAll( IProgressMonitor monitor, final Kind... kind ){
        if( runDirect() ){
            return resolver.resolveAll( monitor, kind );
        }
        
        FerryJob<IDeclaration[]> job = new FerryJob<IDeclaration[]>( "resolve" ){
            @Override
            public IStatus run( IProgressMonitor monitor ){
                content = resolver.resolveAll( monitor, kind );
                return Status.OK_STATUS;
            }
        };

        job.setPriority( priority );
        project.getModel().runJob( job, monitor );
        return job.getContent();
    }

    public ModelNode resolve( final ModelConnection connection, IProgressMonitor monitor ){
        if( runDirect() ){
            return resolver.resolve( connection, monitor );
        }
        
        FerryJob<ModelNode> job = new FerryJob<ModelNode>( "resolve" ){
            @Override
            public IStatus run( IProgressMonitor monitor ){
                content = resolver.resolve( connection, monitor );
                return Status.OK_STATUS;
            }
        };

        job.setPriority( priority );
        project.getModel().runJob( job, monitor );
        return job.getContent();     
    }

    public ModelNode resolve( final IDeclaration declaration, IProgressMonitor monitor ){
        if( runDirect() ){
            return resolver.resolve( declaration, monitor );
        }
        
        FerryJob<ModelNode> job = new FerryJob<ModelNode>( "resolve" ){
            @Override
            public IStatus run( IProgressMonitor monitor ){
                content = resolver.resolve( declaration, monitor );
                return Status.OK_STATUS;
            }
        };

        job.setPriority( priority );
        project.getModel().runJob( job, monitor );
        return job.getContent();     
    }

    public ModelNode resolve( final IASTModelPath path, IProgressMonitor monitor ){
        if( runDirect() ){
            return resolver.resolve( path, monitor );
        }
        
        FerryJob<ModelNode> job = new FerryJob<ModelNode>( "resolve" ){
            @Override
            public IStatus run( IProgressMonitor monitor ){
                content = resolver.resolve( path, monitor );
                return Status.OK_STATUS;
            }
        };

        job.setPriority( priority );
        project.getModel().runJob( job, monitor );
        return job.getContent();     
    }
    
    
    public IASTModelPath resolvePath( final ModelConnection connection, IProgressMonitor monitor ){
        if( runDirect() ){
            return resolver.resolvePath( connection, monitor );
        }
        else{
            FerryJob<IASTModelPath> job = new FerryJob<IASTModelPath>( "resolve" ){
                @Override
                public IStatus run( IProgressMonitor monitor ){
                    content = resolver.resolvePath( connection, monitor );
                    return Status.OK_STATUS;
                }
            };

            job.setPriority( priority );
            project.getModel().runJob( job, monitor );
            return job.getContent();                 
        }
    }
}
