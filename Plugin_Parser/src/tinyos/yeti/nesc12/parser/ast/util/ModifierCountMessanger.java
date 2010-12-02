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
package tinyos.yeti.nesc12.parser.ast.util;

import java.util.List;

import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclaratorSpecifier;

/**
 * Used by the {@link ModifierCount} to report errors.
 * @author Benjamin Sigg
 *
 */
public interface ModifierCountMessanger{
    public void reportMissing( AnalyzeStack stack, int expected, List<DeclaratorSpecifier> found );
    public void reportForbidden( AnalyzeStack stack, int forbidden, List<DeclaratorSpecifier> found );
    public void reportMultiOccurence( AnalyzeStack stack, int mask, List<DeclaratorSpecifier> found );
    public void reportNotSet( AnalyzeStack stack, int set, List<DeclaratorSpecifier> found );
}
