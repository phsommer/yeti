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
package tinyos.yeti.preprocessor.lexer;

import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.preprocessor.parser.PreprocessorElement;

/**
 * Tells a path from the base file through inclusion and macro directives
 * to some included file or element.<br>
 * Each path-object describes one node of the whole path, since each node knows
 * its parent, a whole path is described by one path-object. <code>InclusionPath</code>s
 * are immutable.
 * @author Benjamin Sigg
 */
public class InclusionPath {
    public static InclusionPath include( InclusionPath parent, PreprocessorElement filename ){
        return new InclusionPath( parent, filename, true );
    }
    
    public static InclusionPath macro( InclusionPath parent, PreprocessorElement identifier ){
        return new InclusionPath( parent, identifier, false );
    }
    
    private PreprocessorElement element;
    private boolean include;
    
    private boolean fullMacro;
    private boolean fullInclude;
    
    private InclusionPath parent;
    
    private InclusionPath( InclusionPath parent, PreprocessorElement element, boolean include ){
        this.parent = parent;
        this.element = element;
        this.include = include;
        
        fullMacro = isMacro() || (parent != null && parent.macro());
        fullInclude = isInclude() || (parent != null && parent.include());
    }
    
    public InclusionPath getParent() {
        return parent;
    }
    
    public InclusionPath getRoot(){
        if( parent == null )
            return this;
        
        return parent.getRoot();
    }
    
    public InclusionPath[] toPath(){
    	List<InclusionPath> path = new ArrayList<InclusionPath>();
    	toPath( path );
    	return path.toArray( new InclusionPath[ path.size() ]);
    }
    
    private void toPath( List<InclusionPath> path ){
    	if( parent != null )
    		parent.toPath( path );
    	path.add( this );
    }
    
    /**
     * Tells whether this path has at least one include element.
     * @return <code>true</code> if this path has one include element
     */
    public boolean include(){
        return fullInclude;
    }
    
    /**
     * Tells whether the last path element is an include.
     * @return <code>true</code> if the last path element is an include
     */
    public boolean isInclude() {
        return include;
    }

    /**
     * Tells whether this path has at least one macro element.
     * @return <code>true</code> if this path has one macro element
     */
    public boolean macro(){
        return fullMacro;
    }
    
    /**
     * Tells whether the last element of the path is a macro.
     * @return <code>true</code> if the last element is a macro
     */
    public boolean isMacro(){
        return !include;
    }
    
    public int length(){
        if( parent == null )
            return 1;
        else
            return parent.length()+1;
    }
    
    public PreprocessorElement getElement() {
        return element;
    }
    
    public InclusionPath include( PreprocessorElement filename ){
        return include( this, filename );
    }
    
    public InclusionPath macro( PreprocessorElement identifier ){
        return macro( this, identifier );
    }
}
