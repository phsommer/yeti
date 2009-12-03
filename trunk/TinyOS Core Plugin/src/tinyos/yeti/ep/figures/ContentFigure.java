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
package tinyos.yeti.ep.figures;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ToolbarLayout;

import tinyos.yeti.ep.parser.IASTFigure;
import tinyos.yeti.ep.parser.IASTFigureContent;
import tinyos.yeti.ep.parser.IASTFigureFactory;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.jobs.CancelingJob;
import tinyos.yeti.jobs.UnsecureUIJob;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.utility.Icon;

/**
 * A {@link ContentFigure} shows a title and uses a {@link IASTFigureContent}
 * to draw something beneath the title.
 * @author Benjamin Sigg
 */
public class ContentFigure extends DragableFigure implements IASTFigure, IRepresentation{
    private TitleFigure titleFigure;
    private SeparatorLine line;
    private IASTFigure contentFigure;

    private IASTFigureContent content;
    private IASTFigureFactory factory;

    private UpdateJob updateJob;

    private int expandASTRequest = -1;
    private IExpandCallback expandCallback = null;

    public ContentFigure( String title, Icon image, IASTFigureContent content, IASTModelNode node, IASTFigureFactory factory ){
        this.content = content;
        this.factory = factory;

        titleFigure = new TitleFigure( factory.getHoverManager(), title, image, factory.getFontBig(), node, false );
        titleFigure.addActionListener( new ActionListener(){
            public void actionPerformed( ActionEvent event ) {
                update();
            }
        });
        
        line = new SeparatorLine();
        line.setBorder( new MarginBorder( 0, 0, 2, 0 ) );

        setLayoutManager( new ToolbarLayout() );
        setHighlighted( Highlight.NONE, null ); 
        setBackgroundColor( ColorConstants.white );
        setOpaque( true );

        add( titleFigure );
    }

    @Override
    public synchronized void expandAST( int depth, IExpandCallback callback ) {
        titleFigure.expandAST( depth, null );
        expandASTRequest = depth;
        expandCallback = callback;

        callContentFigureExpand( callback );
    }
    
    private synchronized void callContentFigureExpand( IExpandCallback callback ){
        if( contentFigure != null && expandASTRequest >= 0 ){
            int depth = expandASTRequest;
            expandASTRequest = -1;
            expandCallback = null;
            contentFigure.expandAST( depth, ExpandCallbackUtility.forward( this, callback ) );
        }
    }

    @Override    
    public void collapseAST() {
        titleFigure.collapseAST();
    }

    @Override
    public synchronized void layoutAST() {
        if( contentFigure != null ){
            contentFigure.layoutAST();
        }
    }

    @Override
    public void setHighlighted( Highlight highlighted, IASTModelPath path ) {
        setBorder( ASTFigureDefaults.border( highlighted ) );
    }

    private synchronized void update(){
        if( updateJob != null ){
            updateJob.cancel();
        }

        updateJob = new UpdateJob();
        updateJob.schedule();
    }

    public void setExpanded( boolean expanded ){
        if( titleFigure.isExpanded() != expanded ){
            titleFigure.setExpanded( expanded );
            update();
        }
    }

    private class UpdateJob extends CancelingJob{
        public UpdateJob(){
            super( "Update Figure '" + titleFigure.getTitle() + "'" );
            setPriority( Job.DECORATE );
        }
        
        @Override
        public IStatus run( IProgressMonitor monitor ){
            try{
                final int STATE_EXPANDED = 0;
                final int STATE_CONTENT = 1;
                final boolean[] states = new boolean[2];

                IExpandCallback callback = expandCallback;
                if( callback == null )
                    callback = new NullExpandedCallback();
                
                final BarrierExpandCallback barrier = new BarrierExpandCallback( callback );
                
                UnsecureUIJob findRequestJob = new UnsecureUIJob( factory.getProject().getModel(), "Determine Request" ){
                    @Override
                    public IStatus runInUIThread( IProgressMonitor monitor ){
                        monitor.beginTask( "Read States", 1 );
                        states[ STATE_EXPANDED ] = titleFigure.isExpanded();
                        states[ STATE_CONTENT ] = contentFigure != null;
                        monitor.done();
                        return Status.OK_STATUS;
                    }
                };
                findRequestJob.setPriority( getPriority() );
                findRequestJob.setSystem( true );
                findRequestJob.schedule();

                while( findRequestJob.getState() != NONE ){
                    try{
                        findRequestJob.joinSecure();
                    }
                    catch ( InterruptedException e ){
                        // ignore
                    }
                }

                UnsecureUIJob update = null;
                ProjectModel model = factory.getProject().getModel();

                if( !states[STATE_EXPANDED] && states[ STATE_CONTENT ] ){
                    // not expanded but has content
                    update = new UnsecureUIJob( model, "Collapse" ){
                        @Override
                        public IStatus runInUIThread( IProgressMonitor monitor ){
                            monitor.beginTask( "Collapse", 1 );
                            remove( contentFigure );
                            remove( line );

                            contentFigure.collapseAST();
                            contentFigure = null;

                            revalidate();

                            monitor.done();
                            return Status.OK_STATUS;
                        }
                    };
                }
                else if( states[STATE_EXPANDED] && !states[ STATE_CONTENT ]){
                    // expanded but has no content

                    final IASTFigure contentFigure = content.createContent( factory, monitor );
                    
                    if( monitor.isCanceled() ){
                        monitor.done();
                        return Status.CANCEL_STATUS;
                    }

                    update = new UnsecureUIJob( model, "Expand" ){
                        @Override
                        public IStatus runInUIThread( IProgressMonitor monitor ){
                            monitor.beginTask( "Expand", 1 );

                            add( contentFigure );
                            add( line );

                            if( expandASTRequest >= 0 ){
                                ContentFigure.this.contentFigure = contentFigure;
                                callContentFigureExpand( barrier );
                            }
                            else{
                                ContentFigure.this.contentFigure = contentFigure;
                                contentFigure.expandAST( 0, barrier );
                            }

                            revalidate();

                            monitor.done();
                            return Status.OK_STATUS;
                        }
                    };
                }
                else if( states[STATE_EXPANDED] && states[ STATE_CONTENT ]){
                    update = new UnsecureUIJob( model, "Expand" ){
                        @Override
                        public IStatus runInUIThread( IProgressMonitor monitor ){
                            monitor.beginTask( "Expand", 1 );
                            
                            if( expandASTRequest >= 0 ){
                                callContentFigureExpand( barrier );
                            }
                            else{
                                contentFigure.expandAST( 0, barrier );
                            }
                            
                            revalidate();
                            
                            monitor.done();
                            return Status.OK_STATUS;
                        }
                    };
                }
                else{
                    update = new UnsecureUIJob( model, "Revalidate" ){
                        @Override
                        public IStatus runInUIThread( IProgressMonitor monitor ){
                            monitor.beginTask( "Revalidate", 1 );
                            revalidate();
                            monitor.done();
                            return Status.OK_STATUS;
                        }
                    };
                }

                update.setSystem( true );
                update.setPriority( getPriority() );
                update.schedule();

                while( update.getState() != NONE ){
                    try{
                        update.joinSecure();
                    }
                    catch ( InterruptedException e ){
                        // ignore
                    }
                }

                barrier.open();
                
                monitor.done();
            }
            finally{
                synchronized( ContentFigure.this ){
                    if( updateJob == this )
                        updateJob = null;
                }
            }

            return Status.OK_STATUS;
        }        
    }
}
