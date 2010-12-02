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

import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.model.missing.IMissingResource;


/**
 * Used to collect missing resources.
 * @author Benjamin Sigg
 */
public interface IMissingResourceRecorder{
    /**
     * Records that some file of an include directive is missing
     * @param name the name of the file
     */
    public void missingUserFile( String name );
    
    /**
     * Records that some file of an include directive is missing
     * @param name the name of the file
     */
    public void missingSystemFile( String name );
    
    /**
     * Records that some declarations can't be found
     * @param name the name of the declaration
     * @param kind the kind of declaration that was searched
     */
    public void missingDeclaration( String name, Kind... kind );
    
    /**
     * Adds another missing resource to the list of missing resources.
     * @param resource the additional resource
     */
    public void missing( IMissingResource resource );
}
