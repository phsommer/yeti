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

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.storage.DeclarationKindFactory;

/**
 * A declaration of a new element like a function, interface, module, ... All
 * declarations must have a name and a kind, it is up to the parser-plugin whether
 * declarations carry additional informations like an extended type, 
 * the initial value, ... 
 * @author Benjamin Sigg
 */
public interface IDeclaration {
    public static final DeclarationKindFactory FACTORY_KIND = new DeclarationKindFactory();
    
    /**
     * The kind of declaration the enclosing {@link IDeclaration} represents.<br>
     * Note: this enumeration is likely to change in the future (when new elements
     * are added to NesC), clients should always prepare for the case that
     * an unknown kind is encountered.
     */
    public static enum Kind{
        INTERFACE( true ), MODULE( true ), CONFIGURATION( true ), BINARY_COMPONENT( true ), 
        STRUCT( false ), UNION( false ), ATTRIBUTE( false ),
        FIELD( false ), FUNCTION( false ),
        TYPEDEF( false ),
        ENUMERATION( false ), ENUMERATION_CONSTANT( false );
        
        private boolean unincludedAccess;
        
        private Kind( boolean unincludedAccess ){
            this.unincludedAccess = unincludedAccess;
        }
        
        /**
         * Tells whether element of this kind can be accessed without any sort
         * of inclusion (neither explicit nor implicit). An interface would be
         * accessible since a "uses interface X" clause can stand anywhere. A
         * field however would not be accessible as it always must be included
         * (even it that happens implicit by the "uses interface X" clause). 
         * @return <code>true</code> if this kind of element is always visible
         */
        public boolean isUnincludedAccess(){
            return unincludedAccess;
        }
    }
    
    /**
     * Gets the name of this declaration.
     * @return the name
     */
    public String getName();
    
    /**
     * Gets the kind of element this declaration introduces.
     * @return the kind of declaration
     */
    public Kind getKind();
    
    /**
     * Gets a human readable name for this declaration.
     * @return the name that is used only to represent this declaration
     * on the graphical user interface
     */
    public String getLabel();
    
    /**
     * Gets the file which was parsed in order to get this declaration.
     * @return the parse file, might be <code>null</code> for built in types
     */
    public IParseFile getParseFile();
    
    /**
     * Gets the path to the {@link IASTModelNode} that would describe this
     * declaration.
     * @return the path to the node, might be <code>null</code>
     */
    public IASTModelPath getPath();
    
    /**
     * Gets the set of {@link Tag}s that an {@link IASTModelNode} would have
     * that declares this element. 
     * @return the tags, might be <code>null</code>
     */
    public TagSet getTags();
}
