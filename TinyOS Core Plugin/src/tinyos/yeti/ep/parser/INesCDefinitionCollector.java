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
package tinyos.yeti.ep.parser;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import tinyos.yeti.nesc.IMultiReader;

/**
 * An {@link INesCDefinitionCollector} is used to collect global definitions
 * that should be available when interfaces are included through uses/provides
 * or when components are included through the keyword "components".<br>
 * A collector is a simplified parser that does not parse nesC elements like
 * interfaces or components.
 * @author Benjamin Sigg
 */
public interface INesCDefinitionCollector{
    /**
     * Called whenever a new declaration was given to the {@link INesCDefinitionCollectorCallback},
     * the declaration becomes available to all subsequent calls.
     * @param declaration an additional declaration
     */
    public void addDeclaration( IDeclaration declaration );
    
    /**
     * Adds a new macro to this collector.
     * @param macro the new macro
     */
    public void addMacro( IMacro macro );
    
    /**
     * If this value is set, then only included files and elements should
     * be reported to the {@link INesCDefinitionCollectorCallback}, new
     * declarations can be omitted.
     * @param includesOnly <code>true</code> if only inclusions matter
     */
    public void setReportIncludesOnly( boolean includesOnly );
    
    /**
     * Whether macros should be reported as well. Normally they are not
     * reported.
     * @param macros whether to report macros
     */
    public void setReportMacros( boolean macros );
    
    /**
     * Parse the file <code>reader</code>.
     * @param reader the file to parse
     * @param callback the object which should be informed about anything that
     * this collector finds
     * @param monitor used to inform the user about states and to cancel
     * @throws IOException if an IO error happens
     */
    public void parse( IMultiReader reader, INesCDefinitionCollectorCallback callback, IProgressMonitor monitor ) throws IOException;
    
}
