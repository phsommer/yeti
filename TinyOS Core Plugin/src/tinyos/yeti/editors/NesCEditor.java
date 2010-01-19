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
package tinyos.yeti.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.quickassist.QuickAssistAssistant;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import tinyos.yeti.Debug;
import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.action.FormatSourceAction;
import tinyos.yeti.editors.action.IndentSourceAction;
import tinyos.yeti.editors.action.ToggleCommentAction;
import tinyos.yeti.editors.outline.NesCOutlinePage;
import tinyos.yeti.editors.quickfixer.NesCSelectMarkerRule;
import tinyos.yeti.editors.spelling.NesCSpellingSupport;
import tinyos.yeti.ep.IEditorInputConverter;
import tinyos.yeti.ep.IEnvironment;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.IASTModel;
import tinyos.yeti.ep.parser.IASTModelElement;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeConnection;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.IFoldingRegion;
import tinyos.yeti.ep.parser.IMessage;
import tinyos.yeti.ep.parser.INesCAST;
import tinyos.yeti.ep.parser.INesCParser;
import tinyos.yeti.ep.parser.inspection.INesCInspector;
import tinyos.yeti.ep.parser.reference.IASTReference;
import tinyos.yeti.ep.parser.standard.ASTModel;
import tinyos.yeti.jobs.CancelingJob;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.nature.MissingNatureException;
import tinyos.yeti.nesc.NesCAST;
import tinyos.yeti.preferences.PreferenceConstants;
import tinyos.yeti.utility.preferences.IPreferenceListener;
import tinyos.yeti.utility.preferences.IPreferenceProvider;


/**
 * The editor for a NesC-file. Each editor holds an {@link NesCAST} and a fully
 * resolved {@link ASTModel} of its content.
 */
public class NesCEditor extends TextEditor {

    /** Preference key for matching brackets */
    protected final static String MATCHING_BRACKETS=  PreferenceConstants.BRACKET_HIGHLIGHT;
    /** Preference key for matching brackets color */
    protected final static String MATCHING_BRACKETS_COLOR=  PreferenceConstants.BRACKET_HIGHLIGHT_COLOR;

//  /** Preference key for current line highlighting color */
    protected final static String CURRENT_LINE_HIGHLIGHT = PreferenceConstants.CURRENT_LINE_HIGHLIGHT;
    protected final static String CURRENT_LINE_HIGHLIGHT_COLOR = PreferenceConstants.CURRENT_LINE_HIGHLIGHT_COLOR;

    protected final static char[] BRACKETS= { '{', '}', '(', ')', '[', ']' };

    /** the abstract syntax tree of the current content of this editor */
    private INesCAST ast;

    /** the high level view of {@link #ast} */
    private IASTModel astModel;

    /** references within the current document */
    private IASTReference[] references;
    
    /** The outline page */
    private NesCOutlinePage outlinePage;

    /** The editor's bracket matcher */
    private NesCPairMatcher fBracketMatcher= new NesCPairMatcher(BRACKETS);

    /** The editor's projection support */
    private ProjectionSupport fProjectionSupport;

    /** The editor's annotation model */
    private ProjectionAnnotationModel projectionAnnotationModel = null;
    private List<Annotation> messagesAnnotations = new ArrayList<Annotation>();

    /** the source viewer of this editor */
    private ProjectionViewer sourceViewer;
    
    private NesCSourceViewerConfiguration configuration;

    private List<INesCEditorParserClient> parserClients = new ArrayList<INesCEditorParserClient>();

    private List<IASTRequest> pendingASTRequests = new LinkedList<IASTRequest>();

    private IMarker currentAssistMarker;
    private IMarkerResolution[] currentMarkerResolutions;

    private Job reconcileAsync;
    
    private IEditorInputConverter editorInputConverter;

    private NesCSpellingSupport spellingSupport;
    
    private boolean disposed = false;
    
    private IPreferenceProvider<TextAttribute> textAttributePreferences;
    private IPreferenceListener<TextAttribute> textAttributeListener = new IPreferenceListener<TextAttribute>(){
        public void preferenceChanged( IPreferenceProvider<TextAttribute> provider, String name ) {
            invalidateTextPresentation();
        }
    };

    public NesCEditor() {
        textAttributePreferences = TinyOSPlugin.getDefault().getPreferences().getTextAttributes();
        textAttributePreferences.addPreferenceListener( textAttributeListener );
        spellingSupport = new NesCSpellingSupport( this );
    }

    @Override
    public void dispose() {
    	disposed = true;
        ProjectTOS project = getProjectTOS();
        IResource resource = getResource();

        if( isDirty() ){
            if( project != null && resource != null ){
                project.getBuilder().doBuild( resource, false );
            }
        }

        textAttributePreferences.removePreferenceListener( textAttributeListener );


        if( project != null && resource != null && resource.exists() ){
            project.getBuilder().doBuildAfterInitialization( resource );
        }

        ast = null;

        if (fBracketMatcher != null) {
            fBracketMatcher.dispose();
            fBracketMatcher= null;
        }
        super.dispose();
    }
    
    public boolean isDisposed(){
    	return disposed;
    }

    public void invalidateTextPresentation(){
        ISourceViewer viewer = getSourceViewer();
        if( viewer != null ){
            StyledText text = viewer.getTextWidget();
            if( text != null ){
                text.getDisplay().asyncExec( new Runnable(){
                    public void run(){
                        ISourceViewer viewer = getSourceViewer();
                        if( viewer != null && !viewer.getTextWidget().isDisposed() )
                            viewer.invalidateTextPresentation();           
                    }
                });
            }
        }
    }

    @Override
    public void doSave( IProgressMonitor progressMonitor ){
        stopBuild();
        super.doSave( progressMonitor );
    }

    @Override
    public void doSaveAs(){
        stopBuild();
        super.doSaveAs();
    }

    private void stopBuild(){
        ProjectTOS project = getProjectTOS();
        if( project != null ){
            project.getBuilder().cancelBuild( false );
        }
    }

    /**
     * Asynchronously parses the contents of this editor again.
     */
    public synchronized void reconcileAsync(){
        ProjectTOS project = getProjectTOS();
        IEnvironment environment = project == null ? null : project.getEnvironment();

        Runnable reconcileAsyncRunnable = new Runnable(){
            public void run(){
                synchronized( NesCEditor.this ){
                    if( reconcileAsync == null ){
                        reconcileAsync = new Job( "Reconcile" ){
                            @Override
                            protected IStatus run( IProgressMonitor monitor ){
                                synchronized( NesCEditor.this ){
                                    reconcileAsync = null;
                                }

                                NesCConcilingStrategy strategy = new NesCConcilingStrategy( NesCEditor.this );
                                IDocument document = getDocument();
                                if( document != null ){
                                    strategy.setDocument( document );
                                    strategy.reconcile( true );
                                }

                                return Status.OK_STATUS;
                            }
                        };

                        reconcileAsync.setPriority( Job.DECORATE );

                        IResource resource = getResource();
                        if( resource != null ){
                            reconcileAsync.setRule( resource );   
                        }
                        reconcileAsync.schedule();
                    }
                }
            }
        };

        if( environment == null ){
            reconcileAsyncRunnable.run();
        }
        else{
            environment.runAfterStartup( reconcileAsyncRunnable );
        }
    }

    public void showMessages( final IMessage[] messages ){
        Job job = new UIJob( "Update Annotations" ){
            @Override
            public IStatus runInUIThread( IProgressMonitor monitor ){
                monitor.beginTask( "Update", IProgressMonitor.UNKNOWN );
                Debug.enter();
                updateMessages( messages );
                Debug.leave();
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        job.setSystem( true );
        job.setPriority( Job.INTERACTIVE );
        job.schedule();
    }

    private void updateMessages( IMessage[] messages ){
        IAnnotationModel annotationModel = getAnnotationModel();

        if( annotationModel == null )
            return;

        for( Annotation annotation : messagesAnnotations ){
            annotationModel.removeAnnotation( annotation );
        }

        messagesAnnotations.clear();

        if( messages != null ){
            IParseFile file = getParseFile();
            if( file != null ){
                int count = 0;

                for( IMessage message : messages ){
                    if( count == 1000 )
                        break;

                    IFileRegion[] regions = message.getRegions();
                    if( regions != null ){
                        for( IFileRegion region : regions ){
                            if( file.equals( region.getParseFile() )){
                                MessageAnnotation annotation = new MessageAnnotation( message );
                                annotationModel.addAnnotation( annotation, new Position( region.getOffset(), region.getLength() ) );
                                messagesAnnotations.add( annotation );
                                count++;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public void addParserClient( INesCEditorParserClient client ){
        parserClients.add( client );
    }

    public void removeParserClient( INesCEditorParserClient client ){
        parserClients.remove( client );
    }

    /**
     * Called before a parser for the current file is used. This editor 
     * might change the settings of <code>parser</code> in order to 
     * read more information.
     * @param parser the parser that will be used
     */
    public void setupParser( INesCParser parser ){
        parser.setCreateAST( true );
        parser.setCreateReferences( true );

        ProjectTOS tos = getProjectTOS();
        if( tos != null ){
            parser.setASTModel( tos.newASTModel() );
        }
        else{
            TinyOSPlugin plugin = TinyOSPlugin.getDefault();
            if( plugin != null ){
                parser.setASTModel( plugin.getParserFactory().createModel( getProject() ) );
            }
        }

        parser.setResolveFullModel( true );
        parser.setCreateFoldingRegions( true );

        for( INesCEditorParserClient client : parserClients.toArray( new INesCEditorParserClient[ parserClients.size() ] ))
            client.setupParser( this, parser );
    }

    /**
     * Called after <code>parser</code> parsed the document of this editor.
     * The editor might read additional information from <code>parse</code>r.
     * @param result whether parsing was successful or not
     * @param parser the parser that was used
     */
    public void closeParser( boolean result, INesCParser parser ){
        this.ast = parser.getAST();
        this.astModel = parser.getASTModel();
        this.references = parser.getReferences();

        INesCInspector inspector = parser.getInspector();
        if( inspector != null )
        	inspector.open();
        
        for( INesCEditorParserClient client : parserClients.toArray( new INesCEditorParserClient[ parserClients.size() ] ))
            client.closeParser( this, result, parser );
        
        if( inspector != null )
        	inspector.close();

        synchronized( pendingASTRequests ){
            for( IASTRequest request : pendingASTRequests ){
                request.granted( this, ast );
            }
            pendingASTRequests.clear();
        }
        
        final IFoldingRegion[] folding = parser.getFoldingRegions();
        Job job = new UIJob( "Update Editor UI"){
            @Override
            public IStatus runInUIThread( IProgressMonitor monitor ){
                monitor.beginTask( "Update", IProgressMonitor.UNKNOWN );
                Debug.enter();
                updateFoldingStructure( folding );
                Debug.leave();
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        job.setSystem( true );
        job.setPriority( Job.INTERACTIVE );
        job.schedule();
    }
    
    /**
     * Gets the references which were present when parsing this document the
     * last time.
     * @return the references, may be <code>null</code>.
     */
    public IASTReference[] getReferences(){
		return references;
	}

    /**
     * Gets the abstract syntax tree that describes the contents of this editor.
     * The editor has automatically an AST associated.
     * @return the ast or <code>null</code> if the parser had an error.
     */
    public INesCAST getAST(){
        return ast;
    }

    /**
     * Calls <code>request</code> when an AST is available. The result will
     * be returned in a Job that is {@link ProjectModel#secureThread() secure}.
     * @param request the request to fulfill
     */
    public void getASTAsync( IASTRequest request ){
        synchronized( pendingASTRequests ){
            pendingASTRequests.add( request );
        }

        final INesCAST ast = this.ast;

        if( ast == null ){
            reconcileAsync();
        }
        else{
            Job job = new CancelingJob( "Grant AST Request"){
                @Override
                public IStatus run( IProgressMonitor monitor ){
                    monitor.beginTask( "Send AST", IProgressMonitor.UNKNOWN );
                    synchronized( pendingASTRequests ){
                        for( IASTRequest request : pendingASTRequests ){
                            request.granted( NesCEditor.this, ast );
                        }
                        pendingASTRequests.clear();
                    }
                    monitor.done();
                    return Status.OK_STATUS;
                }
            };

            job.setSystem( true );
            job.setPriority( Job.SHORT );
            IProject project = getProject();
            if( project != null )
                job.setRule( project );
            job.schedule();
        }
    }

    /**
     * Gets an {@link IASTModel} of the contents of this editor. Modifying
     * the ast-model is a bad idea.
     * @return the contents, might be <code>null</code> in case of an error
     */
    public IASTModel getASTModel(){
        return astModel;
    }

    /**
     * Gets the project whose resource is currently edited in this editor.
     * @return the project, can be <code>null</code>
     */
    public IProject getProject(){
    	IEditorInput input = getEditorInput();
    	IProject project = null;
    	
    	if( editorInputConverter != null ){
    		project = editorInputConverter.getProject( input );
    	}

        if( project == null ){
        	ProjectTOS tos = TinyOSPlugin.getDefault().getProjectTOS();
        	if( tos != null )
        		project = tos.getProject();
        }

        return project;
    }

    public ProjectTOS getProjectTOS(){
        IProject project = getProject();
        if( project == null )
            return null;
        TinyOSPlugin plugin = TinyOSPlugin.getDefault();
        if( plugin == null )
            return null;
        try{
        	return plugin.getProjectTOS( project );
        }
        catch( MissingNatureException ex ){
        	// silent
        	return null;
        }
    }

    /**
     * Gets the name of the file that is currently edited in this editor.
     * @return the name of the file, may be <code>null</code>
     */
    public IParseFile getParseFile(){
        if( editorInputConverter == null )
        	return null;
    	
        ProjectTOS project = getProjectTOS();
        if( project == null )
        	return null;
        
        return editorInputConverter.getFile( getEditorInput(), project.getModel() );
    }

    /**
     * Gets the resource that is currently edited in this editor.
     * @return the resource, may be <code>null</code>
     */
    public IResource getResource(){
    	if( editorInputConverter == null )
    		return null;
    	
        IEditorInput input = getEditorInput();
        if( input == null )
        	return null;
    	
    	return editorInputConverter.getResource( input );
    }

    @Override
    protected void initializeEditor() {
        super.initializeEditor();

        configuration = new NesCSourceViewerConfiguration(INesCPartitions.NESC_PARTITIONING, this);
        setSourceViewerConfiguration( configuration );

        setPreferenceStore(TinyOSPlugin.getDefault().getCombinedPreferenceStore());
        setDocumentProvider(new ExternalStorageDocumentProvider());
        setRangeIndicator(new DefaultRangeIndicator());
        setRulerContextMenuId("#NESCRulerContext"); // Contributors to the ruler context menu should use this value as the targetID
    }

    @Override
    public void init( IEditorSite site, IEditorInput input ) throws PartInitException {
    	editorInputConverter = TinyOSPlugin.getDefault().getEditorInputConverter( input );
    	
        super.init(site, input);
        
        if( input != null ){
            setPartName( input.getName() );
        }
    }

    @Override
    public void close(boolean save) {
        if( !save ){

        }
        super.close(save);
    }

    /** The <code>NesCEditor</code> implementation of this 
     * <code>AbstractTextEditor</code> method performs gets
     * the nesc content outline page if request is for a an 
     * outline page.
     * 
     * @param required the required type
     * @return an adapter for the required type or <code>null</code>
     */ 
    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class required) {			
        if (IContentOutlinePage.class.equals(required)) {
            if (outlinePage == null) {
                outlinePage= new NesCOutlinePage( this );
                outlinePage.setModel( getASTModel() );
            }
            return outlinePage;
        }
        if (ITextOperationTarget.class.equals(required)) {
            return getSourceViewer();

        }

        return super.getAdapter(required);
    }

    @Override
    protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
        super.configureSourceViewerDecorationSupport(support);

        //System.out.println("Cursor Line: "+TinyOSPlugin.getDefault().getPreferenceStore().getBoolean(CURRENT_LINE_HIGHLIGHT));

        support.setCharacterPairMatcher(fBracketMatcher);
        support.setMatchingCharacterPainterPreferenceKeys(MATCHING_BRACKETS, MATCHING_BRACKETS_COLOR);

        support.setCursorLinePainterPreferenceKeys(CURRENT_LINE_HIGHLIGHT, CURRENT_LINE_HIGHLIGHT_COLOR);

        // FIXME: has no effect:
        //support.setCursorLinePainterPreferenceKeys(CURRENT_LINE_HIGHLIGHT,CURRENT_LINE_HIGHLIGHT_COLOR );

        super.configureSourceViewerDecorationSupport(support);

    }

    public IAnnotationModel getAnnotationModel(){
        ISourceViewer viewer = getSourceViewer();
        if( viewer != null )
            return viewer.getAnnotationModel();

        return null;
    }
    
    public NesCSpellingSupport getSpellingSupport(){
		return spellingSupport;
	}

    /*
     * @see org.eclipse.ui.texteditor.ExtendedTextEditor#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        ProjectionViewer viewer= (ProjectionViewer) getSourceViewer();

        fProjectionSupport = new ProjectionSupport(viewer,getAnnotationAccess(),getSharedColors());

//      fProjectionSupport.addSummarizableAnnotationType(
//      "org.eclipse.ui.workbench.texteditor.error");
//      fProjectionSupport.addSummarizableAnnotationType(
//      "org.eclipse.ui.workbench.texteditor.warning");

        fProjectionSupport.install();


        //turn projection mode on
        viewer.doOperation(ProjectionViewer.TOGGLE);

        projectionAnnotationModel = viewer.getProjectionAnnotationModel();

        // set scope
        IContextService contextService = (IContextService)getSite().getService( IContextService.class );
        if( contextService != null ){
            // that should not happen, but better be safe
            contextService.activateContext( "TinyOS.nescEditorScope" );
        }
        
        // getSite().getKeyBindingService().setScopes(new String[]{"TinyOS.nescEditorScope"});

        reconcileAsync();
    }

    @Override
    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {

        fAnnotationAccess= createAnnotationAccess();
        fOverviewRuler= createOverviewRuler(getSharedColors());

        sourceViewer = new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles );
        
        // ensure decoration support has been created and configured.
        getSourceViewerDecorationSupport(sourceViewer);
        return sourceViewer;
    }


    @Override
    protected SourceViewerDecorationSupport getSourceViewerDecorationSupport(ISourceViewer viewer) {
        if (fSourceViewerDecorationSupport == null) {
            fSourceViewerDecorationSupport= new NesCSourceViewerDecorationSupport(viewer, getOverviewRuler(), getAnnotationAccess(), getSharedColors());
//          fSourceViewerDecorationSupport.install(TinyOSPlugin.getDefault().getCombinedPreferenceStore());	
            configureSourceViewerDecorationSupport(fSourceViewerDecorationSupport);

        }
        return fSourceViewerDecorationSupport;
    }

    public IDocument getDocument() {
        ISourceViewer viewer = getSourceViewer();
        if( viewer == null )
            return null;

        return getSourceViewer().getDocument();
    }

    /**
     * Jumps to the matching bracket.
     */
    public void gotoMatchingBracket() {

        ISourceViewer sourceViewer= getSourceViewer();
        IDocument document= sourceViewer.getDocument();
        if (document == null)
            return;

        IRegion selection= getSignedSelection(sourceViewer);

        int selectionLength= Math.abs(selection.getLength());
        if (selectionLength > 1) {
            setStatusLineErrorMessage("GotoMatchingBracket.error.invalidSelection");		
            sourceViewer.getTextWidget().getDisplay().beep();
            return;
        }

        // #26314
        int sourceCaretOffset= selection.getOffset() + selection.getLength();
        if (isSurroundedByBrackets(document, sourceCaretOffset))
            sourceCaretOffset -= selection.getLength();

        IRegion region= fBracketMatcher.match(document, sourceCaretOffset);
        if (region == null) {
            setStatusLineErrorMessage("GotoMatchingBracket.error.noMatchingBracket");	
            sourceViewer.getTextWidget().getDisplay().beep();
            return;		
        }

        int offset= region.getOffset();
        int length= region.getLength();

        if (length < 1)
            return;

        int anchor= fBracketMatcher.getAnchor();
        // http://dev.eclipse.org/bugs/show_bug.cgi?id=34195
        int targetOffset= (NesCPairMatcher.RIGHT == anchor) ? offset + 1: offset + length;

        boolean visible= false;
        if (sourceViewer instanceof ITextViewerExtension5) {
            ITextViewerExtension5 extension= (ITextViewerExtension5) sourceViewer;
            visible= (extension.modelOffset2WidgetOffset(targetOffset) > -1);
        } else {
            IRegion visibleRegion= sourceViewer.getVisibleRegion();
            // http://dev.eclipse.org/bugs/show_bug.cgi?id=34195
            visible= (targetOffset >= visibleRegion.getOffset() && targetOffset <= visibleRegion.getOffset() + visibleRegion.getLength());
        }

        if (!visible) {
            setStatusLineErrorMessage("GotoMatchingBracket.error.bracketOutsideSelectedElement");		
            sourceViewer.getTextWidget().getDisplay().beep();
            return;
        }

        if (selection.getLength() < 0)
            targetOffset -= selection.getLength();

        sourceViewer.setSelectedRange(targetOffset, selection.getLength());
        sourceViewer.revealRange(targetOffset, selection.getLength());
    }


    /**
     * Sets the given message as error message to this editor's status line.
     *
     * @param msg message to be set
     */
    @Override
    protected void setStatusLineErrorMessage(String msg) {
        IEditorStatusLine statusLine= (IEditorStatusLine) getAdapter(IEditorStatusLine.class);
        if (statusLine != null)
            statusLine.setMessage(true, msg, null);
    }

    private static boolean isBracket(char character) {
        for (int i= 0; i != BRACKETS.length; ++i)
            if (character == BRACKETS[i])
                return true;
        return false;
    }

    private static boolean isSurroundedByBrackets(IDocument document, int offset) {
        if (offset == 0 || offset == document.getLength())
            return false;

        try {
            return
            isBracket(document.getChar(offset - 1)) &&
            isBracket(document.getChar(offset));

        } catch (BadLocationException e) {
            return false;	
        }
    }

    @Override
    protected void createActions() {
        super.createActions();
        createRulerClickActions();
        
        Action action = new ContentAssistAction(
                ResourceBundle.getBundle("tinyos.yeti.editors.messages"), 
                "ContentAssistProposal.", this); 
        String id = ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS;
        action.setActionDefinitionId(id);
        setAction("ContentAssistProposal", action); 
        markAsStateDependentAction("ContentAssistProposal", true);

        Action a = new ToggleCommentAction(ResourceBundle.getBundle("tinyos.yeti.editors.messages"),"ToggleComment.", this); //$NON-NLS-1$
        a.setActionDefinitionId(INesCEditorActionDefinitionIds.TOGGLE_COMMENT);
        setAction("ToggleComment", a); //$NON-NLS-1$
        markAsStateDependentAction("ToggleComment", true); //$NON-NLS-1$
        configureToggleCommentAction();

        NesCSelectMarkerRule rulerClick = new NesCSelectMarkerRule( ResourceBundle.getBundle( "tinyos.yeti.editors.messages" ), "SelectMarker.", this ); //$NON-NLS-1$
        setAction( ITextEditorActionConstants.RULER_CLICK, rulerClick ); //$NON-NLS-1$

        if( TinyOSPlugin.getDefault().intendingStrategyExists() ){
	        Action c = new IndentSourceAction(ResourceBundle.getBundle("tinyos.yeti.editors.messages"),"IndentSource.", this); //$NON-NLS-1$
	      	c.setActionDefinitionId( INesCEditorActionDefinitionIds.INDENT_SOURCE );
	      	setAction( "Indent", c ); //$NON-NLS-1$
	      	markAsStateDependentAction( "Indent", true ); //$NON-NLS-1$
	        getEditorSite().getActionBars().setGlobalActionHandler( NesCEditorContributor.ID_INDENT_SOURCE, c );
        }

        if( TinyOSPlugin.getDefault().formattingStrategyExists() ){
        	Action c = new FormatSourceAction(ResourceBundle.getBundle("tinyos.yeti.editors.messages"),"FormatSource.", this); //$NON-NLS-1$
        	c.setActionDefinitionId(INesCEditorActionDefinitionIds.FORMAT_SOURCE);
        	setAction("Format", c); //$NON-NLS-1$
        	markAsStateDependentAction("Format", true); //$NON-NLS-1$
        	getEditorSite().getActionBars().setGlobalActionHandler( NesCEditorContributor.ID_FORMAT_SOURCE, c );
        }
    }

    @Override
    protected void editorContextMenuAboutToShow(IMenuManager menu) {
        super.editorContextMenuAboutToShow(menu);
        addAction(menu, ITextEditorActionConstants.GROUP_EDIT, "ToggleComment");  //$NON-NLS-1$
        addAction(menu, ITextEditorActionConstants.GROUP_EDIT, "Format");
        addAction(menu, ITextEditorActionConstants.GROUP_EDIT, "Indent");
    }
    
    private void createRulerClickActions(){
    	//TinyOSPlugin.getDefault().
    }

    public IVerticalRuler getEditorVerticalRuler(){
        return getVerticalRuler();
    }

    public ContentAssistant getEditorContentAssistant(){
        return configuration.getContentAssistant();
    }
    
    public ProjectionViewer getEditorSourceViewer(){
        return sourceViewer;
    }

    public String getEditorDocumentPartitioning(){
        return configuration.getConfiguredDocumentPartitioning( getEditorSourceViewer() );
    }

    public void redraw(){
        getEditorSourceViewer().getTextWidget().redraw();
    }

    public void assist( IMarker marker, IMarkerResolution[] resolutions ){
        currentAssistMarker = marker;
        currentMarkerResolutions = resolutions;
        sourceViewer.doOperation( ISourceViewer.QUICK_ASSIST );
        currentAssistMarker = null;
        currentMarkerResolutions = null;
    }

    public IMarker getCurrentAssistMarker(){
        return currentAssistMarker;
    }

    public IMarkerResolution[] getCurrentMarkerResolutions(){
        return currentMarkerResolutions;
    }
    
    protected QuickAssistAssistant getQuickAssistAssistant(){
        return configuration.getQuickAssistAssistant();
    }
    
    protected ContentAssistant getContentAssistant(){
        return configuration.getContentAssistant();
    }
    
    public NesCSourceViewerConfiguration getConfiguration(){
		return configuration;
	}
    
    /**
     * Configures the toggle comment action
     */
    private void configureToggleCommentAction() {
        IAction action= getAction("ToggleComment");
        if (action instanceof ToggleCommentAction) {
            ISourceViewer sourceViewer= getSourceViewer();
            SourceViewerConfiguration configuration= getSourceViewerConfiguration();
            ((ToggleCommentAction)action).configure(sourceViewer, configuration);
        }
        getEditorSite().getActionBars().setGlobalActionHandler( NesCEditorContributor.ID_COMMENT_ACTION, action );
    }

//  private void configureFormatAction() {
//  IAction action= getAction("Format");
//  if (action instanceof FormatSourceAction) {
//  ISourceViewer sourceViewer= getSourceViewer();
//  SourceViewerConfiguration configuration= getSourceViewerConfiguration();
//  ((FormatSourceAction)action).configure((SourceViewer)sourceViewer, configuration);
//  }
//  }

    public void selectAndReveal( IFileRegion region ){
    	if( region != null ){
			try{
				IParseFile parseFile = getParseFile();
				if( (parseFile == null && region.getParseFile() == null) || (parseFile != null && parseFile.equals( region.getParseFile() ))){
					selectAndReveal( region.getOffset(), region.getLength() );
				}
			}
			catch( IllegalArgumentException ex ){
				resetHighlightRange();    
			}
		}
    }
    
    public void selectAndReveal( Point selection ){
        selectAndReveal( selection.x, selection.y );
    }
    
    public void setCaretPosition( int offset ){
    	getEditorSourceViewer().getTextWidget().setCaretOffset( offset );
    }
    
    /**
     * Tries to guess which node is currently selected.
     * @return the selected nodes or <code>null</code> if none are present. The
     * array is ordered, the 
     */
    public IASTModelElement[] getSelectedElements(){
    	if( astModel == null )
    		return null;
    	
    	final Point range = getEditorSourceViewer().getSelectedRange();
    	if( range == null || range.x < 0 || range.y < 0 )
    		return null;
    	
    	List<IASTModelElement> list = new ArrayList<IASTModelElement>();
        IParseFile file = getParseFile();
        
    	for( IASTModelNode node : astModel ){
    		if( withinRegion( node.getRegions(), range, file )){
    			list.add( node );
    		}
    		IASTModelNodeConnection[] children = node.getChildren();
    		if( children != null ){
    			for( IASTModelNodeConnection child : children ){
    				if( withinRegion( child.getRegions(), range, file )){
    					list.add( child );
    				}
    			}
    		}
    	}
    	
    	IASTModelElement[] potentials = list.toArray( new IASTModelElement[ list.size() ] );
    	
    	if( potentials.length == 0 )
    		return null;
    	if( potentials.length == 1 )
    		return potentials;
    	
    	Arrays.sort( potentials, new Comparator<IASTModelElement>(){
    		public int compare( IASTModelElement a, IASTModelElement b ){
    			int deltaA = delta( a );
    			int deltaB = delta( b );
    			
    			if( deltaA > deltaB )
    				return 1;
    			if( deltaA < deltaB )
    				return -1;
    			
    			int depthA = depth( a );
    			int depthB = depth( b );
    			
    			if( depthA < depthB )
    				return 1;
    			if( depthA > depthB )
    				return -1;
    			
    			return 0;
    		}
    		
    		private int delta( IASTModelElement node ){
    			int min = Integer.MAX_VALUE;
    			
    			for( IFileRegion region : node.getRegions() ){
    				int result = 0;
    				int deltaX = region.getOffset() - range.x;
    				if( deltaX > 0 )
    					result += deltaX;
    				int deltaY = range.y - region.getOffset() - region.getLength();
    				if( deltaY > 0 )
    					result += deltaY;
    				min = Math.min( min, result );
    			}
    			
    			return min;
    		}
    		
    		private int depth( IASTModelElement element ){
    			if( element instanceof IASTModelNode )
    				return ((IASTModelNode)element).getPath().getDepth();
    			if( element instanceof IASTModelNodeConnection )
    				return ((IASTModelNodeConnection)element).getPath().getDepth() + 1;
    			return 0;
    		}
    	});
    	
    	return potentials;
    }

    private boolean withinRegion( IFileRegion[] regions, Point range, IParseFile file ){
    	if( regions == null )
    		return false;
    	
    	for( IFileRegion region : regions ){
    		if( region.getParseFile() != file )
				continue;
			
			if( range.x + range.y < region.getOffset() )
				continue;
			
			if( range.x > region.getOffset() + region.getLength() )
				continue;
			
			return true;
    	}
    	
    	return false;
    }

    /**
     * Returns the signed current selection.
     * The length will be negative if the resulting selection
     * is right-to-left (RtoL).
     * <p>
     * The selection offset is model based.
     * </p>
     *
     * @param sourceViewer the source viewer
     * @return a region denoting the current signed selection, for a resulting RtoL selections length is < 0
     */
    protected IRegion getSignedSelection(ISourceViewer sourceViewer) {
        StyledText text= sourceViewer.getTextWidget();
        Point selection= text.getSelectionRange();

        if (text.getCaretOffset() == selection.x) {
            selection.x= selection.x + selection.y;
            selection.y= -selection.y;
        }

        selection.x= widgetOffset2ModelOffset(sourceViewer, selection.x);

        return new Region(selection.x, selection.y);
    }

    @Override
    protected void doSetInput(IEditorInput input) throws CoreException {
        super.doSetInput(input);
        configureToggleCommentAction();
//      configureFormatAction();
    }

    /**
     * Updates the folding structure of this editor. This method can also
     * handle regions that are no longer legal due of race conditions.
     * @param foldingPos the next folding regions
     */
    private void updateFoldingStructure( IFoldingRegion[] foldingPos ){
        if( foldingPos == null )
            foldingPos = new IFoldingRegion[]{};

        IDocument document = getDocument();
        
        if( document != null && projectionAnnotationModel != null ){
        	FoldingSupport folding = new FoldingSupport();
        	try{
        		folding.update( document, projectionAnnotationModel, foldingPos );

        		ISourceViewer sourceViewer = getSourceViewer();
        		
        		int pixel = 0;
        		if( sourceViewer != null ){
        			StyledText textWidget = sourceViewer.getTextWidget();
        			pixel = textWidget.getHorizontalPixel();	
        		}
        		
        		projectionAnnotationModel.modifyAnnotations( folding.getRemovedAnnotations(), folding.getAddedAnnotations(), null );
        		
        		if( sourceViewer != null ){
        			StyledText textWidget = sourceViewer.getTextWidget();
        			textWidget.setHorizontalPixel(pixel );
        		}
        	}
			catch( BadLocationException e ){
				Debug.error( e );
			}
        }
    }
    
    /*
     * @see org.eclipse.ui.texteditor.AbstractTextEditor#adjustHighlightRange(int, int)
     */
    @Override
    protected void adjustHighlightRange(int offset, int length) {
        ISourceViewer viewer= getSourceViewer();
        if (viewer instanceof ITextViewerExtension5) {
            ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
            extension.exposeModelRange(new Region(offset, length));
        }
    }




}
