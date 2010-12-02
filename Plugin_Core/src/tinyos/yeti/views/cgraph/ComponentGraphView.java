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

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.PrintFigureOperation;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.ScalableLayeredPane;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.parts.ScrollableThumbnail;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.INesCEditorParserClient;
import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.editors.outline.NesCOutlinePage;
import tinyos.yeti.ep.INesCMultiPageEditorPart;
import tinyos.yeti.ep.figures.IRepresentation;
import tinyos.yeti.ep.parser.ASTNodeFilterFactory;
import tinyos.yeti.ep.parser.IASTFigure;
import tinyos.yeti.ep.parser.IASTModel;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.INesCParser;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.inspection.INesCInspector;
import tinyos.yeti.jobs.CancelingJob;
import tinyos.yeti.jobs.FerryJob;
import tinyos.yeti.jobs.UnsecureUIJob;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.utility.ImgUtility;
import tinyos.yeti.views.ElementPath;
import tinyos.yeti.views.NodeContentProvider;
import tinyos.yeti.views.ThumbnailView;
import tinyos.yeti.views.cgraph.layout.PrintMergeLayerOperation;

public class ComponentGraphView implements ISelectionChangedListener, INesCMultiPageEditorPart, INesCEditorParserClient{

    private NesCEditor editor;
    private NesCOutlinePage outline;
    private Composite container = null;

    private FigureCanvas canvas;
    private Layer figureLayer;
    private ScalableLayeredPane layers ;
    private ProjectModel model;
    private INesCInspector inspector;
    private INesCInspector openInspector;
    
    private ElementPath highlight;
    private ContentUpdateJob contentUpdateJob;
    private ContentUpdateJobRestart contentUpdateJobRestart = new ContentUpdateJobRestart();

    private List<IGraphViewRequest> requests = new ArrayList<IGraphViewRequest>();
    private volatile boolean setup = false;
    
    private ThumbnailView thumbnailView;

    public Control createControl( Composite parent, NesCEditor editor ) {
        container = new Composite( parent, SWT.NONE );
        container.setLayout( new GridLayout( 1, false ) );
        
        this.editor = editor;
        ProjectTOS project = editor.getProjectTOS();
        if( project != null ){
        	model = project.getModel();
        }
        
        editor.addParserClient( this );
        
        return container;
    }
    
    public void setupParser( NesCEditor editor, INesCParser parser ){
    	parser.setCreateInspector( true );
    }
    
    public void closeParser( NesCEditor editor, boolean successful, INesCParser parser ){
    	if( successful ){
    		if( inspector != null )
    			inspector.close();
    		
    		inspector = parser.getInspector();
    		if( inspector != null )
    			inspector.open();
    	}
    }

    public boolean isDisposed(){
        if( container == null )
            return false;

        return container.isDisposed();
    }

    public void setOutlinePage( NesCOutlinePage outline ) {
        if( this.outline != outline ){
            if( this.outline != null )
                this.outline.removeSelectionChangedListener( this );

            this.outline = outline;

            if( this.outline != null )
                this.outline.addSelectionChangedListener( this );
        }
    }
    
    public void setThumbnailView( ThumbnailView view ){
        this.thumbnailView = view;
    }

    /**
     * This method is intended to be called from the UI thread, but will
     * do its work in a job which later updates the ui.
     */
    private void createContents() {
        synchronized( this ){
            if( contentUpdateJob != null )
                contentUpdateJob.restart();
            else{
                contentUpdateJob = new ContentUpdateJob();
                contentUpdateJob.setPriority( Job.DECORATE );
                contentUpdateJob.schedule();
            }
        }
    }

    /**
     * Creates and adds the figures to <code>figureLayer</code>.
     * @param mx the center in width
     * @param my get center in height
     * @param figureLayer the layer where the new figures will be added
     * @param monitor used to inform the user about the state or to cancel the operation
     * @return the figures that were created
     */
    public Figures createFigures( final int mx, final int my, final Layer figureLayer, IProgressMonitor monitor ){
        final int GAP = 20;

        if( monitor == null )
            monitor = new NullProgressMonitor();

        monitor.beginTask( "Update Graph View", 10 );

        final Figures result = createFigures( new SubProgressMonitor( monitor, 8 ), createFactory() );
        final IASTFigure[] figures = result.figures; 
        if( monitor.isCanceled() )
            return null;

        UnsecureUIJob job = new UnsecureUIJob( model, "Update Graph View" ){
            @Override
            public IStatus runInUIThread( IProgressMonitor monitor ){
                monitor.beginTask( "Update UI", 10 );
                if( isDisposed() ){
                	monitor.done();
                	return Status.CANCEL_STATUS;
                }

                Dimension[] preferredSizes = new Dimension[ figures.length ];
                int height = 0;
                int width = 0;

                for( int i = 0, n = figures.length; i<n; i++ ){
                	if( figureLayer == null ){
                		monitor.done();
                		return Status.CANCEL_STATUS;
                	}
                	
                    figures[i].expandAST( 0, null );

                    figureLayer.add( figures[i] );
                    Dimension preferred = figures[i].getPreferredSize();
                    preferredSizes[i] = preferred;

                    width = Math.max( preferred.width, width );
                    height += preferred.height;
                }

                height += (figures.length-1) * GAP;

                int x = mx - width/2;
                int y = my - height/2;

                for( int i = 0, n = figures.length; i<n; i++ ){
                    figureLayer.setConstraint( figures[i], new Rectangle( x, y, -1, -1 ));
                    y += preferredSizes[i].height + GAP;
                }

                return Status.OK_STATUS;
            }
        };
        
        job.setPriority( Job.DECORATE );
        job.schedule();

        while( job.getState() != Job.NONE && !monitor.isCanceled() ){
            try{
                job.joinSecure();
            }
            catch( InterruptedException ex ){
                // ignore
            }
        }

        monitor.done();
        return result;
    }

    private ASTFigureFactory createFactory(){
    	class Create extends UnsecureUIJob{
    		public ASTFigureFactory result;
    		
    		public Create(){
    			super( model, "Create AST figure factory" );
    			setSystem( true );
    			setPriority( SHORT );
    		}
    		
    		@Override
    		public IStatus runInUIThread( IProgressMonitor monitor ){
    			monitor.beginTask( "Create", 1 );
    			result = new ASTFigureFactory( editor.getASTModel(), inspector, model.getProject() );
    			monitor.done();
    			return Status.OK_STATUS;
    		}
    	}
    	
    	Create create = new Create();
    	create.schedule();
    	try{
			create.joinSecure();
		}
		catch( InterruptedException e ){
			TinyOSPlugin.log( e );
			return null;
		}
    	return create.result;
    }
    
    /**
     * Uses the model of the associated {@link NesCEditor} to create
     * the root figures.
     * @param monitor used to inform the user about the state or to cancel this method
     * @param factory the factory to use for creating new figures, may be <code>null</code>
     * @return the set of root figures, might be of length 0 but does not contain
     * <code>null</code> entries and is not <code>null</code>
     */
    private Figures createFigures( IProgressMonitor monitor, final ASTFigureFactory factory ){
        if( monitor == null )
            monitor = new NullProgressMonitor();

        monitor.beginTask( "Create figures", 1000 );
        try{
        	if( model.secureThread() ){
        		if( openInspector != null )
        			openInspector.close();
        		
        		openInspector = inspector;
        		if( openInspector != null )
        			openInspector.open();
        		
                IASTModel model = editor.getASTModel();
                if( model == null || monitor.isCanceled() )
                    return new Figures( new IASTFigure[]{}, null );

                IASTModelNode[] nodes = model.getNodes( ASTNodeFilterFactory.subset( Tag.FIGURE ) );
                if( nodes == null || nodes.length == 0 || monitor.isCanceled() )
                	return new Figures( new IASTFigure[]{}, null );

                if( factory == null ){
                	return new Figures( new IASTFigure[]{}, null );
                }
                
                IASTFigure[] result = new IASTFigure[ nodes.length ];
                SubProgressMonitor resultMonitor = new SubProgressMonitor( monitor, 1000 );
                resultMonitor.beginTask( "Create figures", nodes.length );

                for( int i = 0, n = result.length; i<n; i++ ){
                    if( monitor.isCanceled() ){
                    	return new Figures( new IASTFigure[]{}, null );
                    }
                    result[i] = factory.create( nodes[i], new SubProgressMonitor( resultMonitor, 1 ) );
                }

                resultMonitor.done();

                return new Figures( result, factory );
            }
            else{
                FerryJob<Figures> job = new FerryJob<Figures>( "Create figures" ){
                    @Override
                    public IStatus run( IProgressMonitor monitor ) {
                        monitor.beginTask( "Create figures", 1000 );
                        content = createFigures( new SubProgressMonitor( monitor, 1000 ), factory );
                        monitor.done();
                        return Status.OK_STATUS;
                    }
                };
                job.setPriority( Job.INTERACTIVE );
                model.runJob( job, new SubProgressMonitor( monitor, 1000 ) );
                if( job.getContent() == null )
                    return new Figures( new IASTFigure[]{}, null );
                return job.getContent();
            }
        }
        finally{
            monitor.done();
        }
    }

    public String getPartName() {
        return "Component graph";
    }

    public void setSelected( boolean selected ) {
        if( selected ){
            init();
        }
        else{
            clear();
        }
    }

    public void init() {
        deleteAll();
        createContents();
        TinyOSPlugin.getDefault().showThumbnailView();
    }

    public void clear(){
        deleteAll();
        if( thumbnailView != null ){
            thumbnailView.refresh();
        }
    }
    
    public void dispose(){
    	if( openInspector != null ){
    		openInspector.close();
    		openInspector = null;
    	}	
    	if( inspector != null ){
    		inspector.close();
    		inspector = null;
    	}
    }
    
    public ScalableLayeredPane getLayers(){
		return layers;
	}

    private void deleteAll() {
    	if( openInspector != null ){
    		openInspector.close();
    		openInspector = null;
    	}
    	
        if (canvas != null) {
            canvas.dispose();
        }
        if (layers != null) {
            layers = null;
        }
        if (figureLayer != null) {
            figureLayer = null;
        }
        if (container != null) {
            container.update();
            container.layout();
        }
    }

    /**
     * outline selection has changed..
     */
    public void selectionChanged(SelectionChangedEvent event) {
        if( layers == null )
            return;

        ISelection sel = event.getSelection();
        Object obj = ((IStructuredSelection) sel).getFirstElement();

        if( obj instanceof NodeContentProvider.Element ){
            NodeContentProvider.Element element = (NodeContentProvider.Element)obj;
            setHighlighted( element.getFullPath() );
        }
    }

    public void setHighlighted( ElementPath path ){
        IFigure root = canvas.getContents();
        
        if( root != null ){
            if( highlight != null ){
                unhighlight( root );
            }
            IFigure selection = setHighlighted( path, 0, new int[ path.getSize() ], root, true );
            if( selection != null ){
                revealInCenterOfCanvas( selection );
            }
        }
        highlight = path;
    }
    
    @SuppressWarnings( "unchecked" )
    private void unhighlight( IFigure root ){
        if( root instanceof IRepresentation ){
            IASTModelPath[] paths = ((IRepresentation)root).getPaths();
            if( paths != null ){
                for( IASTModelPath path : paths ){
                    for( int i = 0, n = highlight.getSize(); i<n; i++ ){
                        if( path.equals( highlight.getModelPath( i ) )){
                            ((IRepresentation)root).setHighlighted( IRepresentation.Highlight.NONE, path );
                            break;
                        }
                    }
                }
            }
        }

        List<IFigure> children = root.getChildren();
        for( IFigure child : children ){
            unhighlight( child );
        }
    }

    /**
     * Searches the elements that are highlighted by <code>path</code>
     * @param path currently selected path in the outline view
     * @param index which element of <code>path</code> to analyze
     * @param count how many same paths have been counted on the given
     * level of <code>path</code>. The size of this array is equal to the
     * number of elements in <code>path</code>
     * @param root the current element to analyze
     * @param selectable whether the selectable element can still be in the current tree branch
     * @return a figure that was selected
     */
    @SuppressWarnings("unchecked")
    private IFigure setHighlighted( ElementPath path, int index, int[] count, IFigure root, boolean selectable ){
        IFigure result = null;
        boolean foundPath = false;

        if( root instanceof IRepresentation ){
            IRepresentation representation = (IRepresentation)root;
            IASTModelPath[] compare = representation.getPaths();

            if( compare != null ){
                if( index < path.getSize() ){
                    for( IASTModelPath check : compare ){
                        if( check.equals( path.getModelPath( index ) )){
                            foundPath = true;
                            if( count[index] == path.getSamePathIndex( index )){
                                count[index]++;
                                break;
                            }
                            else{
                                count[index]++;
                                selectable = false;
                                break;
                            }
                        }
                        else if( remaining( check, path, index )){
                            selectable = false;
                            break;
                        }
                    }
                }

                if( foundPath && selectable ){
                    if( index+1 == path.getSize() ){
                        representation.setHighlighted( IRepresentation.Highlight.SELECTED, path.getModelPath( index ));	
                    }
                    else{
                        representation.setHighlighted( IRepresentation.Highlight.ON_PATH, path.getModelPath( index ));
                    }
                }
                else if( foundPath && index+1 == path.getSize() ){
                    representation.setHighlighted( IRepresentation.Highlight.ALTERNATIVE, path.getModelPath( index ));
                }
                else if( !selectable && index+1 < path.getSize() ){
                    // this second search might be avoidable, but performace really is not an issue here
                    for( IASTModelPath check : compare ){
                        if( check.equals( path.getModelPath( path.getSize()-1 ) )){
                            representation.setHighlighted( IRepresentation.Highlight.ALTERNATIVE, path.getModelPath( index ));
                            break;
                        }
                    }
                }
            }
        }

        int originalIndex = index;
        if( foundPath ){
            index++;
        }

        if( foundPath ){
            IFigure check = setHighlighted( path, index, count, root, selectable );
            if( result == null )
                result = check;
        }
        else{
            List<IFigure> children = root.getChildren();
            for( IFigure child : children ){
                IFigure check = setHighlighted( path, index, count, child, selectable );
                if( result == null ){
                    result = check;
                }
            }
        }
        
        for( int i = originalIndex+1, n = count.length; i<n; i++ )
            count[i] = 0;

        return result; 

        /*
        IFigure result = null;

        if( root instanceof IRepresentation ){
            IRepresentation representation = (IRepresentation)root;
            IASTModelPath[] compare = representation.getPaths();
            if( compare != null ){
                boolean foundHighlight = false;
                boolean foundPath = false;

                for( IASTModelPath check : compare ){
                    if( check.equals( highlight )){
                        foundHighlight = true;
                    }

                    if( check.equals( path )){
                        foundPath = true;
                    }
                }

                if( foundHighlight ){
                    representation.setHighlighted( false, highlight );
                }

                if( foundPath ){
                    representation.setHighlighted( true, path );
                }
            }
        }

        List<IFigure> children = root.getChildren();
        for( IFigure child : children ){
            IFigure check = setHighlighted( path, child );
            if( result == null )
                result = check;
        }

        return result; */
    }

    private boolean remaining( IASTModelPath path, ElementPath paths, int index ){
        for( int i = index+1, n = paths.getSize(); i<n; i++ ){
            if( path.equals( paths.getModelPath( i ) ))
                return true;
        }

        return false;
    }

    private void revealInCenterOfCanvas(IFigure fig) {
        double scale = layers.getScale();
        Rectangle bounds = fig.getBounds();
        Dimension size = canvas.getViewport().getSize();
        canvas.getViewport().setViewLocation(
                (int)((bounds.x + bounds.width/2.0 - size.width/2.0 )*scale),
                (int)((bounds.y + bounds.height/2.0 - size.height/2.0)*scale) );
    }

    @SuppressWarnings("unchecked")
	private void saveAsFile( final String file, final int imgFormat ){
    	IFigure figure;
    	List<IFigure> children = figureLayer.getChildren();
    	if( children.size() == 1 ){
    		figure = children.get( 0 );
    	}
    	else{
    		figure = figureLayer;
    	}
    	
        Dimension size = figure.getPreferredSize();
        Rectangle bounds = figure.getBounds();
        final Image image = new Image(Display.getDefault(), size.width, size.height);
        GC gc = new GC(image);
        SWTGraphics graphics = new SWTGraphics(gc);
        graphics.translate( -bounds.x, -bounds.y );
        figure.paint(graphics);

        Job saveJob = new Job("Saving graph to file: "+file) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
            	ImageData data = image.getImageData();
            	
            	// downscale gifs..
                // makes no sense in having 16/24 bit colors for the graph
            	if( imgFormat == SWT.IMAGE_GIF ){
            		data = ImgUtility.downSample( image );
            	}
                
                if( monitor.isCanceled() )
                	return Status.CANCEL_STATUS;

                FileOutputStream os;
                try {
                    os = new FileOutputStream(file);

                    ImageLoader loader = new ImageLoader();
                    loader.data = new ImageData[] { data };
                    loader.save(os, imgFormat);
                    os.close();
                }
                catch( final Exception e ){
                	return new Status( IStatus.ERROR, TinyOSPlugin.PLUGIN_ID, e.getMessage(), e );
                }

                if( monitor.isCanceled() )
                    return Status.CANCEL_STATUS;

                return Status.OK_STATUS;
            }
        };
        saveJob.setPriority(Job.LONG);
        setup = false;
        saveJob.schedule();

    }    

    public synchronized void request( IGraphViewRequest request ){
        if( setup ){
            request.granted( this );
        }
        else if( !requests.contains( request )){
            requests.add( request );
        }
    }

    public synchronized void withdraw( IGraphViewRequest request ){
        requests.remove( request );
    }

    private synchronized void grantRequests(){
        setup = true;
        IGraphViewRequest[] list = requests.toArray( new IGraphViewRequest[ requests.size() ] );
        requests.clear();
        for( IGraphViewRequest request : list ){
            request.granted( this );
        }
    }

    public Composite createThumbnail( Composite parent ) {
        if( canvas == null || layers == null ) 
            return null;

        Composite base = new Composite( parent, SWT.NONE );
        base.setLayout( new GridLayout( 2, false ) );

        IFigure thumbnail = createThumbnail();

        FigureCanvas figure = new FigureCanvas( base );
        figure.setBackground( ColorConstants.white );
        figure.getViewport().setContentsTracksHeight( true );
        figure.getViewport().setContentsTracksWidth( true );
        figure.setContents( thumbnail );
        figure.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 2, 1 ) );

        Label info = new Label( base, SWT.NONE );
        info.setText( "Zoom:" );
        info.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, false, false, 1, 1 ) );

        final Slider zoom = new Slider( base, SWT.HORIZONTAL );
        zoom.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 1, 1 ) );
        zoom.setValues( 100, 5, 300, 10, 1, 10 );
        zoom.setSelection( (int)(layers.getScale() * 100.5 ));
        zoom.addSelectionListener( new SelectionListener(){
            public void widgetSelected( SelectionEvent e ){
                double selection = zoom.getSelection();
                double level = selection / 100.0;
                layers.setScale( level );
                canvas.getViewport().revalidate();
                canvas.getViewport().repaint();
            }
            public void widgetDefaultSelected( SelectionEvent e ){
                // ignore
            }
        });

        return base;
    }

    private IFigure createThumbnail(){
        ScrollableThumbnail thumbnail = new ScrollableThumbnail( canvas.getViewport() );
        thumbnail.setSource(canvas.getContents());
        return thumbnail;
    }
    
    public static class Figures{
    	public IASTFigure[] figures;
    	public ASTFigureFactory factory;
    	
    	public Figures( IASTFigure[] figures, ASTFigureFactory factory ){
    		this.figures = figures;
    		this.factory = factory;
    	}
    }

    private class ExpandSelectionListener implements SelectionListener {
        private IASTFigure[] roots;
        private int level = 0;

        public ExpandSelectionListener( IASTFigure[] roots, int level ){
            this.roots = roots;
            this.level = level;
        }

        public void widgetSelected(SelectionEvent e) {
            for( IASTFigure figure : roots )
                figure.expandAST( level, null );
        }

        public void widgetDefaultSelected(SelectionEvent e) {
            // ignore
        };
    }

    private class ContentUpdateJobRestart extends Job{
    	public ContentUpdateJobRestart(){
    		super( "Update Graph View" );
    		setPriority( DECORATE );
    		setSystem( true );
    	}
    	
    	@Override
    	protected IStatus run( IProgressMonitor monitor ){
    		monitor.beginTask( "Update Graph View", IProgressMonitor.UNKNOWN );
    		
    		while( contentUpdateJob.getState() != Job.NONE ){
    			try{
					contentUpdateJob.join();
				}
				catch( InterruptedException e ){
					// ignore
				}
    		}
    		setup = false;
    		contentUpdateJob.schedule();
    		monitor.done();
    		return Status.OK_STATUS;
    	}
    }
    
    private class ContentUpdateJob extends CancelingJob{
        public ContentUpdateJob(){
            super( "Update Graph View" );
        }

        public void restart(){
            cancel();
            contentUpdateJobRestart.schedule();
        }

        private void run( UnsecureUIJob job ){
        	job.setPriority( Job.DECORATE );
            job.setSystem( true );
            job.schedule();
            
            while( job.getState() != Job.NONE ){
                try{
                    job.joinSecure();
                }
                catch( InterruptedException ex ){
                    // ignore
                }
            }
        }

        @Override
        public IStatus run( IProgressMonitor monitor ){
            monitor.beginTask( "Update Graph View", 1000 );

            run( new UnsecureUIJob( model, "New Layer" ){
                @Override
                public IStatus runInUIThread( IProgressMonitor monitor ){
                    monitor.beginTask( "New Layer", 10 );

                    // initialize figure layer
                    figureLayer = new Layer();
                    figureLayer.setLayoutManager(new XYLayout());

                    return Status.OK_STATUS;
                }
            } );

            if( monitor.isCanceled() ){
                return Status.CANCEL_STATUS;
            }



            final org.eclipse.swt.graphics.Rectangle bounds = new org.eclipse.swt.graphics.Rectangle( 0, 0, 1, 1 );
            run( new UnsecureUIJob( model, "Read bounds" ){
                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {
                    org.eclipse.swt.graphics.Rectangle containerBounds = container.getBounds();

                    bounds.x = containerBounds.x;
                    bounds.y = containerBounds.y;
                    bounds.width = containerBounds.width;
                    bounds.height = containerBounds.height;

                    return Status.OK_STATUS;
                }
            } );

            final Figures figures = createFigures( 
                    bounds.width / 2,
                    bounds.height / 2, 
                    figureLayer, 
                    new SubProgressMonitor( monitor, 750 ) );

            if( monitor.isCanceled() )
                return Status.CANCEL_STATUS;

            run( new UnsecureUIJob( model, "Show Figures" ){
                @Override
                public IStatus runInUIThread( IProgressMonitor monitor ){
                    monitor.beginTask( "Show", 1 );
                    if( isDisposed() || figureLayer == null ){
                    	monitor.done();
                    	return Status.CANCEL_STATUS;
                    }

                    // --------------------------------
                    layers = new ScalableLayeredPane();

                    layers.setLayoutManager(new StackLayout());
                    layers.add(figureLayer);

                    // Main Drawing Object
                    if( canvas != null ){
                    	canvas.dispose();
                    }
                    
                    canvas = new FigureCanvas(container,
                            SWT.V_SCROLL | 
                            SWT.H_SCROLL );
                    
                    if( figures.factory != null )
                    	figures.factory.setControl( ComponentGraphView.this, canvas );
                    canvas.setContents(layers);
                    canvas.setBackground( ColorConstants.white );
                    canvas.setHorizontalScrollBarVisibility(FigureCanvas.AUTOMATIC);
                    canvas.setVerticalScrollBarVisibility(FigureCanvas.AUTOMATIC);
                    canvas.getViewport().setContentsTracksHeight(true);
                    canvas.getViewport().setContentsTracksWidth(true);

                    canvas.setLayoutData(new GridData(GridData.FILL_BOTH));
                    canvas.pack();

                    container.update();
                    container.layout(); 

                    // Set Menu of the canvas..
                    Menu menu = new Menu(canvas);
                    MenuItem m0 = new MenuItem(menu, SWT.NONE);
                    m0.setText("&Auto Arrange");
                    m0.addSelectionListener(new SelectionListener(){

                        public void widgetSelected(SelectionEvent e) {
                            for( IASTFigure figure : figures.figures )
                                figure.layoutAST();
                        }

                        public void widgetDefaultSelected(SelectionEvent e) {
                            // ignore
                        }
                    });


                    MenuItem m1 = new MenuItem(menu,SWT.NONE);
                    m1.setText("&Collapse");
                    m1.addSelectionListener(new SelectionListener(){
                        public void widgetSelected(SelectionEvent e) {
                            for( IASTFigure figure : figures.figures )
                                figure.collapseAST();
                        }

                        public void widgetDefaultSelected(SelectionEvent e) {
                            // ignore

                        }
                    });
                    MenuItem m2 = new MenuItem(menu,SWT.CASCADE);
                    m2.setText("Expand");

                    Menu expandMenu = new Menu(m2);
                    m2.setMenu(expandMenu);
                    int i;
                    for (i = 0; i < 5; i++) {
                        MenuItem e = new MenuItem(expandMenu,SWT.NONE); 
                        e.setText((i+1) + " levels");
                        e.setData(new Integer(i+1));
                        e.addSelectionListener(new ExpandSelectionListener( figures.figures, i ));
                    }

                    MenuItem m4 = new MenuItem(menu,SWT.CASCADE);
                    m4.setText("&Print");
                    m4.setImage(NesCIcons.icons().get(NesCIcons.ICON_PRINTER));

                    Menu printStyle = new Menu(m4);
                    m4.setMenu(printStyle);

                    MenuItem m41 = new MenuItem(printStyle, SWT.NONE);
                    m41.setText("Fit Page");
                    m41.addSelectionListener(new SelectionListener(){
                        public void widgetSelected(SelectionEvent e) {
                            PrintDialog dialog = new PrintDialog(new Shell());
                            PrinterData printerData = dialog.open();
                            if (printerData == null) return;
                            Printer p = new Printer(printerData);   
                            PrintMergeLayerOperation op = new PrintMergeLayerOperation(p, layers);
                            op.setPrintMode(PrintFigureOperation.FIT_PAGE);     
                            op.run("Application Graph");  // "Test" is the print job name
                            p.dispose();        
                        }

                        public void widgetDefaultSelected(SelectionEvent e) {
                            // ignore
                        }
                    });

                    MenuItem m42 = new MenuItem(printStyle, SWT.NONE);
                    m42.setText("Fit Height");
                    m42.addSelectionListener(new SelectionListener(){
                        public void widgetSelected(SelectionEvent e) {
                            PrintDialog dialog = new PrintDialog(new Shell());
                            PrinterData printerData = dialog.open();
                            if (printerData == null) return;
                            Printer p = new Printer(printerData);   
                            PrintMergeLayerOperation op = new PrintMergeLayerOperation(p, layers);
                            op.setPrintMode(PrintFigureOperation.FIT_HEIGHT);       
                            op.run("Application Graph");  // "Test" is the print job name
                            p.dispose();        
                        }

                        public void widgetDefaultSelected(SelectionEvent e) {
                            // ignore
                        }
                    });

                    MenuItem m43 = new MenuItem(printStyle, SWT.NONE);
                    m43.setText("Fit Width");
                    m43.addSelectionListener(new SelectionListener(){
                        public void widgetSelected(SelectionEvent e) {
                            PrintDialog dialog = new PrintDialog(new Shell());
                            PrinterData printerData = dialog.open();
                            if (printerData == null) return;
                            Printer p = new Printer(printerData);   
                            PrintMergeLayerOperation op = new PrintMergeLayerOperation(p, layers);
                            op.setPrintMode(PrintFigureOperation.FIT_WIDTH);        
                            op.run("Application Graph");  // "Test" is the print job name
                            p.dispose();        
                        }

                        public void widgetDefaultSelected(SelectionEvent e) {
                            // ignore
                        }
                    });

                    MenuItem m5 = new MenuItem(menu,SWT.NONE);
                    m5.setText("&Save as Image");
                    //m5.setImage(NesCIcons.ICON_PRINTER.createImage());
                    m5.addSelectionListener(new SelectionListener(){
                        public void widgetSelected(SelectionEvent e) {
                            SaveFileDialog dialog = new SaveFileDialog(new Shell());
                            String file = dialog.open();

                            if (file == null) 
                                return;

                            saveAsFile( file,dialog.getSwtImageFileFormat() );     
                        }
                        public void widgetDefaultSelected(SelectionEvent e) {
                            // ignore
                        }
                    });
                    canvas.setMenu(menu);

                    grantRequests();

                    monitor.done();
                    return Status.OK_STATUS;
                }
            } );

            monitor.done();
            return Status.OK_STATUS;

        }
    }
}
