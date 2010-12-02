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
package tinyos.yeti.nesc12.ep.rules;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.fix.IMultiMarkerResolution;
import tinyos.yeti.ep.fix.IMultiQuickFixer;
import tinyos.yeti.nesc12.ep.rules.quickfix.IMultiQuickfixRule;
import tinyos.yeti.nesc12.ep.rules.quickfix.QuickfixCollector;
import tinyos.yeti.preprocessor.output.Insight;
import tinyos_parser.NesC12ParserPlugin;

public class NesC12MultiQuickFixer implements IMultiQuickFixer{
    @SuppressWarnings("unchecked")
    public IMultiMarkerResolution[] getResolutions( IMarker[] markers, IParseFile parseFile, ProjectTOS project ) {
        try{
            IMultiQuickfixRule[] rules = NesC12ParserPlugin.getDefault().getMultiQuickfixRules();

            Insight[] errors = new Insight[ markers.length ];
            for( int i = 0, n = errors.length; i<n; i++ ){
                errors[i] = new Insight( markers[i].getAttributes() );
            }

            QuickfixCollector collector = new QuickfixCollector( parseFile, project );
            
            for( IMultiQuickfixRule rule : rules ){
                rule.suggest( errors, collector );
            }

            return collector.getMultiResolutions();
        }
        catch( CoreException ex ){
            NesC12ParserPlugin.getDefault().getLog().log( ex.getStatus() );
            return null;
        }
    }
}
