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
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.TinyOSPlugin;
import tinyos.yeti.editors.IDocumentMap;
import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.fix.IMultiMarkerResolution;
import tinyos.yeti.ep.fix.IMultiQuickFixer;
import tinyos.yeti.ep.fix.ISingleMarkerResolution;
import tinyos.yeti.ep.fix.ISingleQuickFixer;
import tinyos.yeti.ep.parser.INesCAST;
import tinyos.yeti.marker.ProblemMarkerSupport;
import tinyos.yeti.preferences.PreferenceConstants;

/**
 * A Quickfixer that suggest to transform an error or warning into an info message.
 * @author Benjamin Sigg
 *
 */
public class ErrorToInfoQuickFixer implements ISingleQuickFixer, IMultiQuickFixer{
	private boolean active = true;
	
	public ErrorToInfoQuickFixer(){
		active = TinyOSPlugin.getDefault().getPreferenceStore().getBoolean( PreferenceConstants.ERROR_TO_INFO );
		
		TinyOSPlugin.getDefault().getPreferenceStore().addPropertyChangeListener( new IPropertyChangeListener(){
			public void propertyChange( PropertyChangeEvent event ){
				if( event.getProperty().equals( PreferenceConstants.ERROR_TO_INFO )){
					active = TinyOSPlugin.getDefault().getPreferenceStore().getBoolean( PreferenceConstants.ERROR_TO_INFO );			
				}
			}
		});
	}
	
    public ISingleMarkerResolution[] getResolutions( IMarker marker, IParseFile parseFile, ProjectTOS project ){
    	if( !active )
    		return null;
    	
        int severity = marker.getAttribute( IMarker.SEVERITY, IMarker.SEVERITY_INFO );
        if( severity != IMarker.SEVERITY_WARNING && severity != IMarker.SEVERITY_ERROR )
            return null;
        
        String message = marker.getAttribute( IMarker.MESSAGE, "" );
        
        return new ISingleMarkerResolution[]{ new Resolution( "Change severity to 'info' (" + message + ")" ) };
    }
    
    public IMultiMarkerResolution[] getResolutions( IMarker[] markers, IParseFile parseFile, ProjectTOS project ){
    	if( !active )
    		return null;
    	
        int count = 0;
        
        for( IMarker marker : markers ){
            int severity = marker.getAttribute( IMarker.SEVERITY, IMarker.SEVERITY_INFO );
            if( severity == IMarker.SEVERITY_WARNING || severity == IMarker.SEVERITY_ERROR ){
                count++;
                
                if( count >= 2 )
                    break;
            }
        }
        if( count >= 2 ){
            return new IMultiMarkerResolution[]{ new Resolution( "Change severity to 'info' (all markers)" ) };
        }
        
        return null;
    }
    
    private static class Resolution implements ISingleMarkerResolution, IMultiMarkerResolution{
        private String label;
        
        public Resolution( String label ){
            this.label = label;
        }
        
        public String getLabel(){
            return label;
        }
        
        public Image getImage(){
            return NesCIcons.icons().get( NesCIcons.ICON_CONVERT_TO_INFO );
        }
        
        public String getDescription(){
            return null;
        }

        public void run( IMarker[] markers, INesCAST ast, IDocumentMap document, IParseFile file, ProjectTOS project ){
            for( IMarker marker : markers ){
                int severity = marker.getAttribute( IMarker.SEVERITY, IMarker.SEVERITY_INFO );
                if( severity == IMarker.SEVERITY_WARNING || severity == IMarker.SEVERITY_ERROR ){
                    ProblemMarkerSupport.convertToInformation( marker );
                }
            }
        }
        
        public void run( IMarker marker, INesCAST ast, IDocumentMap document, IParseFile file, ProjectTOS project ){
            ProblemMarkerSupport.convertToInformation( marker );
        }
    }
}
