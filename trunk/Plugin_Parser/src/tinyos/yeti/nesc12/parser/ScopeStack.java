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
package tinyos.yeti.nesc12.parser;

import java.util.HashSet;
import java.util.Set;

import tinyos.yeti.nesc12.Parser;

/**
 * This class represents the stack of scopes that is accessible during
 * parse time. 
 * @author Benjamin Sigg
 */
public class ScopeStack {
    /** the current scope */
    private Scope scope = new Scope( null );
    
    private Parser parser;
    
    private int level;
    
    /**
     * Creates a new stack.
     * @param parser the owner of this stack
     */
    public ScopeStack( Parser parser ){
        this.parser = parser;
    }
    
    /**
     * Gets the parser of this stack.
     * @return the owner
     */
    public Parser getParser() {
        return parser;
    }
    
    /**
     * Opens a new scope
     */
    public void push(){
        scope = new Scope( scope );
        level++;
    }
    
    /**
     * Remembers the child of the current scope to be used for the next
     * recycle called on the scope. The child will
     * be reused no more than once.
     */
    public void remember(){
    	scope.remember = scope.child;
    }
    
    /**
     * Reopens the last scope that was popped.
     */
    public void recycle(){
    	if( scope.remember != null ){
    		scope.child = scope.remember;
    		scope.remember = null;
    		scope = scope.child;
    		level++;
    	}
    	else if( scope.child != null ){
            scope = scope.child;
            level++;
        }
        else{
            push();
        }
    }
    
    /**
     * Cleans the stack, recycling of old elements is no longer
     * possible after this method
     */
    public void clean(){
        scope.child = null;
    }
    
    /**
     * Closes the topmost scope
     */
    public void pop(){
    	if( scope.child != null ){
    		scope.child.remember = null;
    		scope.child.child = null;
    	}
    	
        scope = scope.getParent();
        level--;
        if( scope == null ){
            scope = new Scope( null );
            level = 0;
        }
    }
    
    public int getLevel(){
        return level;
    }
    
    /**
     * Defines a new type in the current scope.
     * @param name the name of the type
     */
    public void addTypedef( String name ){
        scope.typedef( name );
    }
    
    public void addField( String name ){
        scope.field( name );
    }
    
    /**
     * Adds a new enum constant to the toplevel scope
     * @param name the name of the enum constant
     */
    public void addEnumToplevel( String name ){
        scope.getRoot().addEnum( name );
    }
    
    /**
     * Adds a new enum constant to the current scope.
     * @param name the name of the enum constant
     */
    public void addEnum( String name ){
        Scope parent = scope.getParent();
        if( parent != null )
            parent.addEnum( name );
        else
            scope.addEnum( name );
    }
    
    /**
     * Tells whether a given type is currently defined or not.
     * @param name the name of the type
     * @return <code>true</code> if the type is defined, <code>false</code> otherwise
     */
    public boolean isTypedef( String name ){
        return scope.isTypedef( name );
    }
    
    public boolean isEnum( String name ){
        return scope.isEnum( name );
    }
    
    public void typedef(){
        scope.typedef = true;
    }
    
    public boolean isTypedef(){
        return scope.typedef;
    }
    
    public void wipe(){
        scope.typedef = false;
    }
    
    private class Scope{
        private Scope parent;
        private Scope child;
        private Scope remember;
        
        private Set<String> typedefs;
        private Set<String> enums;
        private Set<String> fields;
        
        public boolean typedef;
        
        public Scope( Scope parent ){
            this.parent = parent;
            
            if( parent != null ){
                Scope old = parent.child;
                parent.child = this;
                
                if( old != null ){
                    old = old.child;
                    if( old != null ){
                        old.parent = this;
                        this.child = old;
                    }
                }
            }
        }
        
        public Scope getParent() {
            return parent;
        }
        
        public Scope getRoot(){
            if( parent == null )
                return this;
            
            return parent.getRoot();
        }
        
        public void field( String name ){
            if( fields == null )
                fields = new HashSet<String>();
            
            fields.add( name );
        }
        
        public void typedef( String name ){
            if( typedefs == null )
                typedefs = new HashSet<String>();
            
            typedefs.add( name );
        }
        
        public boolean isTypedef( String name ){
            if( fields != null && fields.contains( name ))
                return false;
            
            if( typedefs != null && typedefs.contains( name ))
                return true;
            
            if( parent != null )
                return parent.isTypedef( name );
            
            return false;
        }
       
        public void addEnum( String name ){
            if( enums == null )
                enums = new HashSet<String>();
            
            enums.add( name );
        }
        
        public boolean isEnum( String name ){
            if( enums != null && enums.contains( name ))
                return true;
            
            if( parent != null )
                return parent.isEnum( name );
            
            return false;
        }
    }
}
