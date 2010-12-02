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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.MultiPageEditorSite;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.outline.NesCOutlinePage;
import tinyos.yeti.ep.IEditorInputConverter;
import tinyos.yeti.ep.INesCMultiPageEditorPart;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.jobs.PublicJob;
import tinyos.yeti.model.IProjectModelListener;
import tinyos.yeti.nature.MissingNatureException;

public class MultiPageNesCEditor extends MultiPageEditorPart implements IResourceChangeListener, ITextEditor, IProjectModelListener{

    private NesCEditor editor;
    private INesCMultiPageEditorPart[] parts;
    private int activePage = 0;

    private ProjectTOS project;

    public MultiPageNesCEditor() {
        super();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
        parts = TinyOSPlugin.getDefault().getMultiPageEditorParts();
    }

    @Override
    protected void setInput( IEditorInput input ){
        super.setInput( input );

        IProject project = null;

        IEditorInputConverter converter = TinyOSPlugin.getDefault().getEditorInputConverter( input );
        if( converter != null ){
        	project = converter.getProject( input );
        }
        
        if( project == null )
            setProject( null );
        else{
        	try{
        		setProject( TinyOSPlugin.getDefault().getProjectTOS( project ) );
        	}
        	catch( MissingNatureException ex ){
        		// silent
        	}
        }
    }

    private void setProject( ProjectTOS project ){
        if( this.project != null )
            this.project.getModel().removeProjectModelListener( this );

        this.project = project;
        if( this.project != null )
            this.project.getModel().addProjectModelListener( this );
    }

    public void changed( IParseFile parseFile, boolean continuous ){
        // ignore
    }

    public void changed( IParseFile[] parseFiles ){
        // ignore
    }

    public void initialized(){
        if( !getContainer().isDisposed() ){
            if( editor != null )
                editor.reconcileAsync();

            getContainer().getDisplay().asyncExec( new Runnable(){
                public void run(){
                    activePage = getActivePage();
                    int index = activePage-1;
                    if( index >= 0 ){
                        parts[ index ].setSelected( true );
                    }
                }
            });   
        }
    }

    /**
     * Searches the one page on this multi-page-editor whose type equals
     * <code>clazz</code>.
     * @param <E> the type of the part
     * @param clazz the type of the page
     * @return the page or <code>null</code>
     */
    @SuppressWarnings( "unchecked" )
    public <E extends INesCMultiPageEditorPart> E getPart( Class<E> clazz ){
        for( INesCMultiPageEditorPart part : parts ){
            if( clazz.equals( part.getClass() ))
                return (E)part;
        }
        return null;
    }

    public NesCEditor getNesCEditor(){
        return editor;
    }
    
    @Override
    protected IEditorSite createSite( IEditorPart editor ){
    	if( editor == getNesCEditor() ){
    		return new MultiPageEditorSite( this, editor ){
    			@Override
    			public String getId(){
	    			IWorkbenchPartSite site = getSite();
	    			if( site == null )
	    				return "";
	    			return site.getId();
    			}
    		};
    	}
    	
    	return super.createSite( editor );
    }

    @Override
    public void dispose() {
    	for( INesCMultiPageEditorPart part : parts ){
    		part.dispose();
    	}
    	
        super.dispose();
        ResourcesPlugin.getWorkspace().removeResourceChangeListener( this );
        setProject( null );
    }

    @Override
    protected void createPages() {
        editor = new NesCEditor();

        try {
            int index = addPage(editor, getEditorInput());
            setPartName(getEditorInput().getName());
            setPageText(index,"Editor");

            /*
            Composite graphComposite = new Composite(getContainer(),SWT.NONE);
            view = new ComponentGraphView(graphComposite, editor);
            index = addPage(graphComposite);
            setPageText(index, "Component Graph");
             */

            for( INesCMultiPageEditorPart part : parts ){
                Control control = part.createControl( getContainer(), editor );
                index = addPage( control );
                setPageText( index, part.getPartName() );
            }

        }
        catch( PartInitException e ){
        	TinyOSPlugin.log( e );
        }
    }



    @Override
    public void doSave(IProgressMonitor monitor) {
    	if( project != null ){
    		project.getModel().runJob( new PublicJob( "Save" ){
    			@Override
    			public IStatus run( IProgressMonitor monitor ){
    				editor.doSave( monitor );
    				return Status.OK_STATUS;
    			}
    		}, monitor );
    	}
    	else{
    		editor.doSave(monitor);
    	}
    }

    /**
     * Saves the multi-page editor's document as another file.
     * Also updates the text for page 0's tab, and updates this multi-page editor's input
     * to correspond to the nested editor's.
     */
    @Override
    public void doSaveAs() {
        IEditorPart editor = getEditor(0);
        editor.doSaveAs();
        setPageText(0, editor.getTitle());
        setInput(editor.getEditorInput());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter( Class required ){
        //this.addSelectionChangedListener(openAction);
        if (IContentOutlinePage.class.equals(required)) {
            Object o = editor.getAdapter(required);
            if( o instanceof NesCOutlinePage ){
                for( INesCMultiPageEditorPart part : parts )
                    part.setOutlinePage( (NesCOutlinePage)o );
            }
            return o;
        } else {
            return editor.getAdapter(required);
        }
    }

    /* (non-Javadoc)
     * Method declared on IEditorPart
     */
    public void gotoMarker(IMarker marker) {
        setActivePage(0);
        IDE.gotoMarker(getEditor(0), marker);
    }

    public void changeToGraph() {
        pageChange(1);
    }

    /**
     * Calculates the contents of page 2 when the it is activated.
     */
    @Override
    protected void pageChange( int newPageIndex ){
        if( activePage > 0 )
            parts[ activePage-1 ].setSelected( false );

        super.pageChange(newPageIndex);
        activePage = newPageIndex;

        if( activePage > 0 )
            parts[ activePage-1 ].setSelected( true );
    }

    @Override
    public boolean isSaveAsAllowed() {
        return editor.isSaveAsAllowed();
    }

    public void resourceChanged( final IResourceChangeEvent event ){
        if( event.getType() == IResourceChangeEvent.POST_CHANGE && editor != null ){
            IResource resource = editor.getResource();
            IProject project = editor.getProject();
         
            if( (resource != null && !resource.isAccessible()) || ( project != null && !project.isAccessible() )){
                // this editor is no longer valid, close it
                
                Display.getDefault().asyncExec(new Runnable(){
                    public void run(){
                        IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
                        for( IWorkbenchPage page : pages ){
                            page.closeEditor( MultiPageNesCEditor.this, true );
                        }
                    }            
                });
            }
        }
    }

//  *************************************************************
//  Implementation of ITextEditor interface
//  needed for console->filelink to open at specific line
//  *************************************************************
    public IDocumentProvider getDocumentProvider() {
        return editor.getDocumentProvider();
    }

    public void close(boolean save) {
        editor.close(save);
    }

    public boolean isEditable() {
        return editor.isEditable();
    }

    public void doRevertToSaved() {
        editor.doRevertToSaved();
    }

    public void setAction(String actionID, IAction action) {
        editor.setAction(actionID,action);
    }

    public IAction getAction(String actionId) {
        return editor.getAction(actionId);
    }

    public void setActionActivationCode(String actionId, char activationCharacter, int activationKeyCode, int activationStateMask) {
        editor.setActionActivationCode(actionId, activationCharacter, activationKeyCode, activationStateMask);
    }

    public void removeActionActivationCode(String actionId) {
        editor.removeActionActivationCode(actionId);
    }

    public boolean showsHighlightRangeOnly() {
        return editor.showsHighlightRangeOnly();
    }

    public void showHighlightRangeOnly(boolean showHighlightRangeOnly) {
        editor.showHighlightRangeOnly(showHighlightRangeOnly);
    }

    public void setHighlightRange(int offset, int length, boolean moveCursor) {
        editor.setHighlightRange(offset,length,moveCursor);
    }

    public IRegion getHighlightRange() {
        return editor.getHighlightRange();
    }

    public void resetHighlightRange() {
        editor.resetHighlightRange();
    }

    public ISelectionProvider getSelectionProvider() {
        return editor.getSelectionProvider();
    }

    public void selectAndReveal(int offset, int length) {
        editor.selectAndReveal(offset,length);
    }
}