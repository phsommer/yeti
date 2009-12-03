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
package tinyos.yeti.ep.figures;

import tinyos.yeti.ep.parser.IASTFigureContent;
import tinyos.yeti.ep.storage.IGenericFactory;
import tinyos.yeti.ep.storage.ReferenceFactory;

/**
 * Standard factory to create figures for a module.
 * @author Benjamin Sigg
 */
public class ModuleContent extends BlockContent implements IASTFigureContent{
    public static final IGenericFactory<ModuleContent> FACTORY = new ReferenceFactory<ModuleContent>( BlockContent.FACTORY ){
        public ModuleContent create(){
            return new ModuleContent();
        }
    };
    
    protected ModuleContent(){
        // nothing
    }
    
    public ModuleContent( IASTFigureContent[] provided, IASTFigureContent[] used, IASTFigureContent[] commands, IASTFigureContent[] events, IASTFigureContent[] tasks ){
    	addBlock( provided );
    	addBlock( used );
        addBlock( commands );
        addBlock( events );
        addBlock( tasks );
    }
}
