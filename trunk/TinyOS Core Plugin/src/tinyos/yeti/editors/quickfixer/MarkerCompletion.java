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
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;

public class MarkerCompletion implements ICompletionProposal{
    private IMarker marker;
    private IMarkerResolution resolution;
    
    public MarkerCompletion( IMarker marker, IMarkerResolution resolution ){
        this.marker = marker;
        this.resolution = resolution;
    }
    
    public void apply( IDocument document ){
        resolution.run( marker );
    }

    public String getAdditionalProposalInfo(){
        if( resolution instanceof IMarkerResolution2 )
            return ((IMarkerResolution2)resolution).getDescription();
        
        return null;
    }

    public IContextInformation getContextInformation(){
        return null;
    }

    public String getDisplayString(){
        return resolution.getLabel();
    }

    public Image getImage(){
        if( resolution instanceof IMarkerResolution2 )
            return ((IMarkerResolution2)resolution).getImage();
        
        return null;
    }

    public Point getSelection( IDocument document ){
        return null;
    }
}