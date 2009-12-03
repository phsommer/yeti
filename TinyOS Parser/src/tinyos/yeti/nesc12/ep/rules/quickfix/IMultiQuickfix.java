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

import org.eclipse.swt.graphics.Image;

import tinyos.yeti.preprocessor.output.Insight;

/**
 * A suggestion how to repair a set of errors.
 * @author Benjamin Sigg
 */
public interface IMultiQuickfix {
    public String getLabel();
    public String getDescription();
    public Image getImage();
    
    /**
     * Executes this quickfix.
     * @param errors the errors to repair
     * @param information information of the document in which the error can be found
     */
    public void run( Insight[] errors, QuickfixInformation information );
}
