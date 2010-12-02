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
package tinyos.yeti.nesc12.ep.rules.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.editors.IDocumentMap;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.fix.ISingleMarkerResolution;
import tinyos.yeti.ep.parser.INesCAST;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.preprocessor.output.Insight;
import tinyos_parser.NesC12ParserPlugin;

public class SingleMarkerResolution implements ISingleMarkerResolution{
    private ISingleQuickfix fix;

    public SingleMarkerResolution( ISingleQuickfix fix ){
        this.fix = fix;
    }

    public String getDescription(){
        return fix.getDescription();
    }

    public Image getImage(){
        return fix.getImage();
    }

    public String getLabel(){
        return fix.getLabel();
    }

    @SuppressWarnings("unchecked")
    public void run( IMarker marker, INesCAST ast, IDocumentMap document, IParseFile file, ProjectTOS project ){
        try{
            Insight error = new Insight( marker.getAttributes() );
            fix.run( error, new QuickfixInformation( (NesC12AST)ast, document, file, project ));
        }
        catch( CoreException e ){
            NesC12ParserPlugin.getDefault().getLog().log( e.getStatus() );
        }
    }
}
