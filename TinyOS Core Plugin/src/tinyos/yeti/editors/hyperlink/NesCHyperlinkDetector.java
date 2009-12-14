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
package tinyos.yeti.editors.hyperlink;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.editors.INesCEditorParserClient;
import tinyos.yeti.editors.NesCDocumentMap;
import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.NullParseFile;
import tinyos.yeti.ep.parser.IASTModel;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IFileHyperlink;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.INesCAST;
import tinyos.yeti.ep.parser.INesCParser;
import tinyos.yeti.ep.parser.reference.IASTReference;
import tinyos.yeti.ep.parser.standard.FileHyperlink;
import tinyos.yeti.jobs.FerryJob;
import tinyos.yeti.model.ProjectModel;

public class NesCHyperlinkDetector extends AbstractHyperlinkDetector implements IHyperlinkDetector, INesCEditorParserClient{
    private boolean setup = false;
    private NesCEditor editor;
    private INesCAST ast;
    private IASTReference[] references;
    
    public IHyperlink[] detectHyperlinks( ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks ){
        ensureSetup();
        if( ast != null ){
            IFileHyperlink[] links = ast.getHyperlinks( new DocumentRegion( textViewer, region, new NesCDocumentMap( editor.getDocument() ) ));
            if( links == null ){
            	links = referenceLinks( region );
            }
            if( links != null ){
                IHyperlink[] result = new IHyperlink[ links.length ];
                for( int i = 0, n = result.length; i<n; i++ )
                    result[i] = new NesCHyperlink( links[i] );
                
                return result;
            }
        }
        
        return null;
    }
    
    private void ensureSetup(){
        if( setup )
            return;
        
        setup = true;
        editor = getEditor();
        
        editor.addParserClient( this );
        ast = editor.getAST();
        references = editor.getReferences();
    }

    private IFileHyperlink[] referenceLinks( IRegion region ){
    	if( references == null )
    		return null;
    	
    	ProjectTOS project = editor.getProjectTOS();
    	if( project == null )
    		return null;
    	
    	ProjectModel model = project.getModel();
    	if( model == null )
    		return null;
    	
    	List<IFileHyperlink> result = new ArrayList<IFileHyperlink>();
    	for( IASTReference reference : references ){
    		IFileRegion source = reference.getSource();
    		
    		if( region.getOffset() + region.getLength() > source.getOffset() && 
    				region.getOffset() < source.getOffset() + source.getLength() ){
    			
    			IFileHyperlink link = getLink( source, model, reference.getTarget() );
    			if( link != null ){
    				result.add( link );
    			}
    		}
    	}
    	
    	if( result.isEmpty() )
    		return null;
    	
    	return result.toArray( new IFileHyperlink[ result.size() ] );
    }
    
    private IFileHyperlink getLink( IFileRegion source, final ProjectModel project, final IASTModelPath path ){
    	IASTModel model = editor.getASTModel();
    	IFileRegion target = null;
    	boolean found = false;
    	
    	if( model != null ){
    		IASTModelNode node = model.getNode( path );
    		if( node != null ){
    			target = node.getRegion();
    			found = true;
    		}
    	}
    	
    	if( !found ){
	    	FerryJob<IFileRegion> job = new FerryJob<IFileRegion>( "resolve link" ){
				@Override
				public IStatus run( IProgressMonitor monitor ){
		    		content = project.getRegion( path, null );
		    		return null;
				}
			};
			target = job.getContent();
    	}
		
		if( target == null ){
			IParseFile file = path.getParseFile();
			if( file != null && file != NullParseFile.NULL ){
				return new FileHyperlink( source, file );
			}
			return null;
		}
		else{
			return new FileHyperlink( source, target );
		}
    }
    
    public void setupParser( NesCEditor editor, INesCParser parser ){
    	parser.setCreateReferences( true );
    }
    
    public void closeParser( NesCEditor editor, boolean successful, INesCParser parser ){
    	if( successful ){
    		INesCAST ast = parser.getAST();
    		if( ast != null || this.ast == null )
    			this.ast = ast;
        
    		references = parser.getReferences();
        }
    }
    
    private NesCEditor getEditor(){
        return (NesCEditor)getAdapter( NesCEditor.class );
    }
    
    @Override
    public void dispose(){
        if( editor != null ){
            editor.removeParserClient( this );
        }
        super.dispose();
    }
}
