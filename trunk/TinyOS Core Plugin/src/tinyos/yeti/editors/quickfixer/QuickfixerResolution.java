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
package tinyos.yeti.editors.quickfixer;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.texteditor.ITextEditor;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.IASTRequest;
import tinyos.yeti.editors.IDocumentMap;
import tinyos.yeti.editors.MultiPageNesCEditor;
import tinyos.yeti.editors.NesCDocumentMap;
import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.fix.IMultiMarkerResolution;
import tinyos.yeti.ep.fix.ISingleMarkerResolution;
import tinyos.yeti.ep.parser.INesCAST;

/**
 * A base-class for other classes wrapping around a {@link ISingleMarkerResolution} or
 * a {@link IMultiMarkerResolution}. 
 * @author Benjamin Sigg
 */
public abstract class QuickfixerResolution implements IMarkerResolution, IMarkerResolution2{
    private IResource resource;
    private ProjectTOS project;
    private IParseFile file;
    
    public QuickfixerResolution( IResource resource, ProjectTOS project, IParseFile file ){
        this.resource = resource;
        this.project = project;
        this.file = file;
    }
    
    public final void run( final IMarker marker ){
        TinyOSPlugin plugin = TinyOSPlugin.getDefault();
        
        try{
            ITextEditor textEditor = plugin.openFileInTextEditor( resource.getProject(), resource.getFullPath(), false );
            NesCEditor editor = null;
            
            if( textEditor instanceof MultiPageNesCEditor ){
                editor = ((MultiPageNesCEditor)textEditor).getNesCEditor();
            }
            else if( textEditor instanceof NesCEditor ){
                editor = (NesCEditor)textEditor;
            }
            
            if( editor != null ){
                
                IDocument document = editor.getDocument();
                final NesCDocumentMap map = new NesCDocumentMap( document );
                
                editor.getASTAsync( new IASTRequest(){
                    public void granted( NesCEditor editor, INesCAST ast ){
                        run( map, ast, file, project );
                    }
                });
            }
        }
        catch ( CoreException e ){
            plugin.getLog().log( e.getStatus() );
        }
    }
    
    protected abstract void run( IDocumentMap document, INesCAST ast, IParseFile file, ProjectTOS project );
}
