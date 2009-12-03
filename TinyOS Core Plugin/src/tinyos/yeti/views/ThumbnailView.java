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
package tinyos.yeti.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import tinyos.yeti.editors.MultiPageNesCEditor;
import tinyos.yeti.views.cgraph.ComponentGraphView;
import tinyos.yeti.views.cgraph.IGraphViewRequest;

/**
 * View for displaying a smaller size of the graph component in the multipagenesceditor
 */
public class ThumbnailView extends ViewPart implements IPartListener {
    public static final String ID = "TinyOS.view.thumbnail";

    private Composite parent;
    private Composite thumbnail;

    private IWorkbenchPart lastActive;
    private ComponentGraphView lastView;

    private IWorkbenchPage page;
    
    public ThumbnailView() {
        super();
    }
    
    @Override
    public void init( IViewSite site ) throws PartInitException{
        super.init( site );
        
        page = site.getPage();
        if( page != null ){
            page.addPartListener( this );
        }
    }

    @Override
    public void dispose() {
        if( lastView != null ){
            lastView.setThumbnailView( null );
        }
        setThumbnail( null );
        if( page != null ){
            page.removePartListener( this );
        }
        super.dispose();
    }

    @Override
    public void createPartControl(Composite parent) {
        this.parent = parent;

        parent.setLayout(new GridLayout(1,false));
    }

    public boolean isDisposed(){
        if( parent == null )
            return false;
        
        return parent.isDisposed();
    }
    
    private void setThumbnail( final Composite c ){
        if(( c == null ) || ( c.isDisposed() ) || parent.isDisposed() ){
            if( thumbnail != null ) 
                thumbnail.dispose();
            return;
        }

        UIJob job = new UIJob( "Update Thumbnail View" ){
            @Override
            public IStatus runInUIThread( IProgressMonitor monitor ){
                if( thumbnail != null )
                    thumbnail.dispose();
                
                if( !isDisposed() ){
                    if( c != null ){
                        thumbnail = c;
                        thumbnail.setLayoutData( new GridData(GridData.FILL_BOTH) );
                    }
                    try {
                        parent.layout();
                        parent.redraw();
                    }
                    catch(Exception e) {
                        thumbnail.dispose();
                    }
                }
                else if( c != null )
                    c.dispose();
                
                return Status.OK_STATUS;
            }
        };
        
        job.setPriority( Job.DECORATE );
        job.schedule();
    }

    @Override
    public void setFocus() {

    }

    private void updateThumbnail( IWorkbenchPart part ){
        if( part instanceof MultiPageNesCEditor ){
            final ComponentGraphView cgv = ((MultiPageNesCEditor)part).getPart( ComponentGraphView.class );
            if( cgv !=  null ){
                if( lastView != null ){
                    lastView.setThumbnailView( null );
                }
                
                lastView = cgv;
                cgv.request( new IGraphViewRequest(){
                    public void granted( ComponentGraphView view ){
                        if( cgv == lastView ){
                            if( !isDisposed() ){
                                if( cgv.isDisposed() ){
                                    setThumbnail( null );
                                }
                                else{
                                    lastView.setThumbnailView( ThumbnailView.this );
                                    setThumbnail( cgv.createThumbnail( parent ) );
                                }
                            }
                        }
                    }
                });
            } 
        }
    }

    public void refresh() {
        IWorkbenchPage page = getSite().getPage();
        updateThumbnail( page.getActiveEditor() );
        setLastActive( page.getActivePart() );
    }

    public void partActivated(IWorkbenchPart part) {
        setLastActive(part);
    }

    public void partBroughtToTop(IWorkbenchPart part) {
        updateThumbnail(part);
        setLastActive(part);
    }

    public void partClosed(IWorkbenchPart part) {
        if( part instanceof MultiPageNesCEditor ){
            if( (lastActive == null) || (lastActive.equals(part)) ){
                if( thumbnail != null)
                    thumbnail.dispose();
            }
        }
        else if( part == this ){
            if( lastView != null ){
                lastView.setThumbnailView( null );
                lastView = null;
            }
            setThumbnail( null );
        }
                                
    }

    public void partDeactivated(IWorkbenchPart part) {

    }

    public void partOpened(IWorkbenchPart part) {
        setLastActive(part);
        if( part == this ){
            refresh();
        }
    }

    private void setLastActive(IWorkbenchPart part)  {
        if (part instanceof MultiPageNesCEditor) {
            lastActive = part;
        }
    }
}
