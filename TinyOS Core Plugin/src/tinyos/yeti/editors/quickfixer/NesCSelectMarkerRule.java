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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.ui.IMarkerHelpRegistry;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;
import org.eclipse.ui.texteditor.TextEditorAction;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.NesCEditor;
import tinyos.yeti.editors.NesCProblemMarker;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.fix.IMultiMarkerResolution;
import tinyos.yeti.ep.fix.IMultiQuickFixer;

/**
 * An action that opens the quick assist popup-menu. This action should be 
 * invoked when the user clicked on the vertical annotation ruler. 
 * @author Benjamin Sigg
 */
public class NesCSelectMarkerRule extends TextEditorAction{
    private NesCEditor editor;
    
    public NesCSelectMarkerRule( ResourceBundle bundle, String prefix, NesCEditor editor) {
        super( bundle, prefix, editor );
        this.editor = editor;
    }
        
    @SuppressWarnings("unchecked")
    @Override
    public void run(){
        IVerticalRuler ruler = editor.getEditorVerticalRuler();
        IDocument document = editor.getDocument();
        if( ruler == null || document == null )
            return;
        
        int line = ruler.getLineOfLastMouseButtonActivity();
        IAnnotationModel model = ruler.getModel();
        if( model != null ){
            List<SimpleMarkerAnnotation> found = new ArrayList<SimpleMarkerAnnotation>();
            Iterator<Annotation> iterator = model.getAnnotationIterator();
            while( iterator.hasNext() ){
                Annotation annotation = iterator.next();
                if( annotation instanceof SimpleMarkerAnnotation ){
                    Position position = model.getPosition( annotation );
                    if( position != null ){
                        try{
                            if( line == document.getLineOfOffset( position.getOffset() ) ){
                                found.add( (SimpleMarkerAnnotation)annotation );
                            }
                        }
                        catch ( BadLocationException e ){
                            // try the next one...
                        }
                    }
                }
            }

            if( !found.isEmpty() ){
                check( found.toArray( new SimpleMarkerAnnotation[ found.size() ] ), model );
            }
        }
    }
    
    private void check( SimpleMarkerAnnotation[] selection, final IAnnotationModel model ){
        /*
         * First sort the annotations by their importance, then choose the
         * first one which has a quickfix attached
         */
        Arrays.sort( selection, new Comparator<SimpleMarkerAnnotation>(){
            public int compare( SimpleMarkerAnnotation a, SimpleMarkerAnnotation b ){
                IMarker ma = a.getMarker();
                IMarker mb = b.getMarker();
                
                int priorityA = ma.getAttribute( IMarker.PRIORITY, IMarker.PRIORITY_LOW );
                int priorityB = mb.getAttribute( IMarker.PRIORITY, IMarker.PRIORITY_LOW );
                
                int result = comparePriority( priorityA, priorityB );
                if( result != 0 )
                    return result;
                
                int severityA = ma.getAttribute( IMarker.SEVERITY, IMarker.SEVERITY_INFO );
                int severityB = mb.getAttribute( IMarker.SEVERITY, IMarker.SEVERITY_INFO );
                
                result = compareSeverity( severityA, severityB );
                if( result != 0 )
                    return result;
                
                Position positionA = model.getPosition( a );
                Position positionB = model.getPosition( b );
                if( positionA.getOffset() < positionB.getOffset() )
                    return -1;
                
                if( positionA.getOffset()  > positionB.getOffset() )
                    return 1;
                
                return 0;
            }
        });
        
        IMarkerHelpRegistry registry = IDE.getMarkerHelpRegistry();
        
        // the position of the first annotation which yielded results
        Position position = null;
        IMarker marker = null;
        
        List<IMarkerResolution> result = new ArrayList<IMarkerResolution>();
        for( SimpleMarkerAnnotation annotation : selection ){
            boolean check = false;
            
            if( position != null ){
                Position nextPosition = model.getPosition( annotation );
                check = position.offset == nextPosition.offset && position.length == nextPosition.length;
            }
            else{
                check = true;
            }

            if( check ){
                IMarkerResolution[] resolutions = registry.getResolutions( annotation.getMarker() );
                if( resolutions != null && resolutions.length > 0 ){
                    if( position == null ){
                        position = model.getPosition( annotation );
                        marker = annotation.getMarker();
                    }
                    
                    for( IMarkerResolution resolution : resolutions ){
                        result.add( resolution );
                    }
                }
            }
        }
        

        List<IMarkerResolution> additional = checkMulti( selection, model, position );
        additional.addAll( result );
        
        
        if( position != null ){
            editor.selectAndReveal( position.getOffset(), position.getLength() );
            editor.assist( marker, additional.toArray( new IMarkerResolution[ additional.size() ] ) );
        }
    }
    
    private List<IMarkerResolution> checkMulti( SimpleMarkerAnnotation[] annotations, IAnnotationModel model, Position position ){
        List<IMarkerResolution> result = new ArrayList<IMarkerResolution>();
        
        ProjectTOS project = editor.getProjectTOS();
        IParseFile file = editor.getParseFile();
        IResource resource = editor.getResource();
        
        if( project != null && file != null ){
            List<IMarker> interesting = new ArrayList<IMarker>();
            for( SimpleMarkerAnnotation annotation : annotations ){
                IMarker marker = annotation.getMarker();
                if( marker.getAttribute( NesCProblemMarker.MARKER_MESSAGE_SOURCE, false )){
                    if( position != null ){
                        Position check = model.getPosition( annotation );
                        if( check.offset == position.offset && check.length == position.length ){
                            interesting.add( marker );                            
                        }
                    }
                    else{
                        interesting.add( marker );
                    }
                }
            }
            
            IMarker[] markers = interesting.toArray( new IMarker[ interesting.size() ] );
            if( markers.length > 0 ){
                IMultiQuickFixer[] multi = TinyOSPlugin.getDefault().getMultiQuickFixers();
                for( IMultiQuickFixer fixer : multi ){
                    IMultiMarkerResolution[] resolutions = fixer.getResolutions( markers, file, project );
                    if( resolutions != null ){
                        for( IMultiMarkerResolution resolution : resolutions ){
                            result.add( new MultiQuickfixerResolution( resolution, markers, resource, project, file ));
                        }
                    }
                }
            }
        }
        
        return result;
    }
    
    private int comparePriority( int a, int b ){
        switch( a ){
            case IMarker.PRIORITY_HIGH:
                switch( b ){
                    case IMarker.PRIORITY_HIGH: 
                        return 0;
                    case IMarker.PRIORITY_NORMAL:
                    case IMarker.PRIORITY_LOW:
                        return -1;
                }
                break;
            case IMarker.PRIORITY_NORMAL:
                switch( b ){
                    case IMarker.PRIORITY_HIGH:
                        return 1;
                    case IMarker.PRIORITY_NORMAL:
                        return 0;
                    case IMarker.PRIORITY_LOW:
                        return -1;
                }
                break;
            case IMarker.PRIORITY_LOW:
                switch( b ){
                    case IMarker.PRIORITY_HIGH:
                    case IMarker.PRIORITY_NORMAL:
                        return 1;
                    case IMarker.PRIORITY_LOW:
                        return 0;
                }
                break;
        }
        
        return 0;
    }
    
    private int compareSeverity( int a, int b ){
        switch( a ){
            case IMarker.SEVERITY_ERROR:
                switch( b ){
                    case IMarker.SEVERITY_ERROR: 
                        return 0;
                    case IMarker.SEVERITY_WARNING:
                    case IMarker.SEVERITY_INFO:
                        return -1;
                }
                break;
            case IMarker.SEVERITY_WARNING:
                switch( b ){
                    case IMarker.SEVERITY_ERROR:
                        return 1;
                    case IMarker.SEVERITY_WARNING:
                        return 0;
                    case IMarker.SEVERITY_INFO:
                        return -1;
                }
                break;
            case IMarker.SEVERITY_INFO:
                switch( b ){
                    case IMarker.SEVERITY_ERROR:
                    case IMarker.SEVERITY_WARNING:
                        return 1;
                    case IMarker.SEVERITY_INFO:
                        return 0;
                }
                break;
        }
        
        return 0;
    }
}