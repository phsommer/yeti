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
package tinyos.yeti.views.cgraph;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.figures.ContentFigure;
import tinyos.yeti.ep.figures.TitleFigure;
import tinyos.yeti.ep.parser.IASTFigure;
import tinyos.yeti.ep.parser.IASTFigureContent;
import tinyos.yeti.ep.parser.IASTFigureFactory;
import tinyos.yeti.ep.parser.IASTModel;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeConnection;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.inspection.INesCInspector;
import tinyos.yeti.jobs.FerryJob;
import tinyos.yeti.jobs.ResolveConnectionJob;
import tinyos.yeti.jobs.ResolveDeclarationJob;
import tinyos.yeti.jobs.ResolvePathJob;
import tinyos.yeti.utility.Icon;

/**
 * A factory that uses the information of a {@link ProjectTOS} to create
 * new figures.
 * @author Benjamin Sigg
 */
public class ASTFigureFactory implements IASTFigureFactory{
    public static final Font ARIAL8 = new Font(null,"Arial",8,SWT.NORMAL);
    public static final Font ARIAL8_BOLD = new Font(null,"Arial",8,SWT.BOLD);
    public static final Font ARIAL10 = new Font(null,"Arial",10,SWT.NORMAL);
    
    private IASTModel model;
    private INesCInspector inspector;
    private ProjectTOS project;
    private HoverManager hover;
    
    /**
     * Creates a new factory.
     * @param model the model that has to be inquired first, can be <code>null</code>
     * @param inspector to inspect the file, can be <code>null</code>
     * @param project the associated project, can be <code>null</code>
     */
    public ASTFigureFactory( IASTModel model, INesCInspector inspector, ProjectTOS project ){
        this.project = project;
        this.inspector = inspector;
        this.model = model;
        hover = new HoverManager( this );
    }
    
    public void setControl( ComponentGraphView view, FigureCanvas control ){
    	hover.setControl( view, control );
    }
    
    public Font getFontBig(){
        return ARIAL10;
    }
    
    public Font getFontNormal() {
        return ARIAL8;
    }
    
    public Font getFontTooltip(){
    	return ARIAL8;
    }
    
    public Font getFontTooltipBold(){
	    return ARIAL8_BOLD;
    }
    
    public ProjectTOS getProject() {
        return project;
    }
    
    public INesCInspector getInspector(){
	    return inspector;
    }
    
    public HoverManager getHoverManager(){
	    return hover;
    }
    
    public IASTFigure create( IASTModelNode node, IProgressMonitor monitor ) {
        return create( node, null, null, monitor );
    }
    
    private IASTFigure create( IASTModelNode node, String label, Icon icon, IProgressMonitor monitor ) {
        if( monitor != null )
            monitor.beginTask( "Create figure '" + node.getLabel() + "'", 1 );
       
        TagSet tags = icon == null ? null : icon.getTags();
        
        if( label == null || (tags != null && !tags.contains( Tag.AST_CONNECTION_GRAPH_LABEL_RESOLVE )))
            label = node.getLabel();
        
        if( tags == null || !tags.contains( Tag.AST_CONNECTION_GRAPH_ICON_RESOLVE ) ){
            tags = node.getTags();
            icon = new Icon( tags, node.getAttributes() );
        }
        
        IASTFigure figure = create( node, icon, label );
        if( monitor != null )
            monitor.done();
        return figure;
    }
    
    private IASTFigure create( IASTModelNode node, Icon icon, String label ) {
    	IASTFigureContent content = node.getContent();
        if( content == null ){
            TitleFigure result = new TitleFigure( hover, label, icon, getFontBig(), node, true );
            result.setDragable( true );
            result.setPaths( node.getPath() );
            return result;
        }
        else{
            ContentFigure result = new ContentFigure( label, icon, content, node, this );
            result.setDragable( true );
            result.setPaths( node.getPath() );
            return result;
        }
    }
    
    public IASTFigure create( final IASTModelPath path, String label, Icon icon, IProgressMonitor monitor ){
        if( monitor == null )
            monitor = new NullProgressMonitor();
        
        monitor.beginTask( "Create figure '" + label + "'", 30 );
        
        IASTModelNode node = null;
        if( path != null ){
        	if( model != null ){
        		if( project.getModel().secureThread() ){
        			node = model.getNode( path );
        		}
        		else{
        			FerryJob<IASTModelNode> job = new FerryJob<IASTModelNode>( "Find " + label ){
						@Override
						public IStatus run( IProgressMonitor monitor ){
							monitor.beginTask( "Find", 1 );
							this.content = model.getNode( path );
							monitor.done();
							return Status.OK_STATUS;
						}
					};
					project.getModel().runJob( job, new SubProgressMonitor( monitor, 10 ) );
					node = job.getContent();
        		}
        	}
        	if( node == null ){
	            if( project.getModel().secureThread() ){
	                node = project.getModel().getNode( path, new SubProgressMonitor( monitor, 10 ) );
	            }
	            else{
	                ResolvePathJob job = new ResolvePathJob( project.getModel(), path );
	                job.setPriority( Job.INTERACTIVE );
	                project.getModel().runJob( job, new SubProgressMonitor( monitor, 10 ) );
	                node = job.getContent();
	            }
        	}
            if( monitor.isCanceled() ){
                monitor.done();
                return null;
            }
        }
        
        IASTFigure result;
        if( node != null ){
            result = create( node, label, icon, new SubProgressMonitor( monitor, 10 ) );
        }
        else{
            TitleFigure figure = new TitleFigure( hover, label, icon, getFontBig(), null, true );
            figure.setDragable( true );
            if( path != null ){
            	figure.setPaths( path );
            }
            result = figure;
        }
        
        monitor.done();
        return result;
    }
    
    public IASTModelNode getNode( final IASTModelPath path, IProgressMonitor monitor ){
    	IASTModelNode node = null;
    	monitor.beginTask( "Find node", 20 );
    	
    	if( path != null ){
        	if( model != null ){
        		if( project.getModel().secureThread() ){
        			node = model.getNode( path );
        		}
        		else{
        			FerryJob<IASTModelNode> job = new FerryJob<IASTModelNode>( "Find node" ){
						@Override
						public IStatus run( IProgressMonitor monitor ){
							monitor.beginTask( "Find", 1 );
							this.content = model.getNode( path );
							monitor.done();
							return Status.OK_STATUS;
						}
					};
					project.getModel().runJob( job, new SubProgressMonitor( monitor, 10 ) );
					node = job.getContent();
        		}
        	}
        	if( node == null ){
	            if( project.getModel().secureThread() ){
	                node = project.getModel().getNode( path, new SubProgressMonitor( monitor, 10 ) );
	            }
	            else{
	                ResolvePathJob job = new ResolvePathJob( project.getModel(), path );
	                job.setPriority( Job.INTERACTIVE );
	                project.getModel().runJob( job, new SubProgressMonitor( monitor, 10 ) );
	                node = job.getContent();
	            }
        	}
            if( monitor.isCanceled() ){
                monitor.done();
                return null;
            }
        }
    	monitor.done();
    	return node;
    }
    

    public IASTFigure create( final IASTModelNodeConnection connection, IProgressMonitor monitor ) {
        if( monitor == null )
            monitor = new NullProgressMonitor();
        
        monitor.beginTask( "Create figure '" + connection.getLabel() + "'", 20 );
        try{
            IASTModelNode node = null;
            if( model != null ){
            	if( project.getModel().secureThread() ){
            		node = model.getNode( connection );
            	}
            	else{
            		FerryJob<IASTModelNode> job = new FerryJob<IASTModelNode>( "Get " + connection.getLabel() ){
            			@Override
            			public IStatus run( IProgressMonitor monitor ){
            				monitor.beginTask( "Get", 1 );
            				content = model.getNode( connection );
            				monitor.done();
            				return Status.OK_STATUS;
            			}
            		};
            		project.getModel().runJob( job, new SubProgressMonitor( monitor, 10 ) );
            		node = job.getContent();
            	}
            }
            if( node == null ){
                if( project.getModel().secureThread() ){
                    node = project.getModel().getNode( connection, new SubProgressMonitor( monitor, 10 ) );
                }
                else{
                    ResolveConnectionJob job = new ResolveConnectionJob( project.getModel(), connection );
                    job.setPriority( Job.INTERACTIVE );
                    project.getModel().runJob( job, new SubProgressMonitor( monitor, 10 ) );
                    node = job.getContent();
                }
            }

            if( node != null ){
                Icon icon;
                if( connection.getTags().contains( Tag.AST_CONNECTION_ICON_RESOLVE  )){
                	icon = new Icon( connection );
                }
                else{
                	icon = new Icon( node );
                }

                String label;
                if( connection.getTags().contains( Tag.AST_CONNECTION_LABEL_RESOLVE ))
                    label = connection.getLabel();
                else
                    label = node.getLabel();

                return create( node, icon, label );
            }

            TitleFigure result = new TitleFigure( 
            		hover,
                    connection.getLabel(), 
                    new Icon( connection ),
                    getFontBig(),
                    null,
                    true );
            result.setDragable( true );
            return result;
        }
        finally{
            monitor.done();
        }
    }

    public IASTFigure create( IDeclaration declaration, IProgressMonitor monitor ) {
        if( monitor == null )
            monitor = new NullProgressMonitor();
        
        monitor.beginTask( "Create figure '" + declaration.getLabel() + "'", 20 );
        try{
            IASTModelNode node = null;
            if( project != null ){
                if( project.getModel().secureThread() ){
                    node = project.getModel().getNode( declaration, new SubProgressMonitor( monitor, 10 ) );
                }
                else{
                    ResolveDeclarationJob job = new ResolveDeclarationJob( project.getModel(), declaration );
                    job.setPriority( Job.INTERACTIVE );
                    project.getModel().runJob( job, new SubProgressMonitor( monitor, 10 ) );
                    node = job.getContent();
                }
            }

            if( node != null )
                return create( node, new SubProgressMonitor( monitor, 10 ) );

            TitleFigure result = new TitleFigure(
            		hover,
                    declaration.getLabel(),
                    new Icon( declaration ),
                    getFontBig(),
                    null,
                    true );
            result.setDragable( true );
            result.setPaths( declaration.getPath() );
            return result;
        }
        finally{
            monitor.done();
        }
    }

}
