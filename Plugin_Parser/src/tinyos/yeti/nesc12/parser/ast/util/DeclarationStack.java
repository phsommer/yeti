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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tinyos.yeti.ep.parser.IDeclaration;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.IDeclaration.Kind;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.nesc12.ep.declarations.BaseDeclaration;
import tinyos.yeti.nesc12.ep.declarations.TypedDeclaration;
import tinyos.yeti.nesc12.ep.nodes.ModelAttribute;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.LazyRangeDescription;
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.SimpleField;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;

/**
 * The declaration stack collects {@link BaseDeclaration}s and sets their
 * {@link ASTModelPath} as soon as known.
 * @author Benjamin Sigg
 */
public class DeclarationStack implements Iterable<BaseDeclaration>{
	private List<BaseDeclaration> declarations = new ArrayList<BaseDeclaration>();
	private Level level;
	private AnalyzeStack stack;
	
	public DeclarationStack( AnalyzeStack stack ){
		this.stack = stack;
	}
	
	public IDeclaration[] getDeclarations(){
		return declarations.toArray( new IDeclaration[ declarations.size() ] );
	}
	
	public Iterator<BaseDeclaration> iterator(){
		return declarations.iterator();
	}
	
	public void push(){
		push( null, null );
	}
	
	public void push( BaseDeclaration declaration ){
		push( declaration, null );
	}
	
	public SimpleField set( Modifiers modifiers, Type type, Name name, ModelAttribute[] attributes, Value initialValue, LazyRangeDescription range ){
		SimpleField field = new SimpleField( modifiers, type, name, attributes, initialValue, range, null );
		set( field );
		return field;
	}
	
	public void push( String id ){
		push( null, id );
	}
	
	public void push( BaseDeclaration declaration, String id ){
		if( level == null ){
			level = new Level( null );
			level.set( declaration, id );
		}
		else{
			level = level.push( declaration, id );
		}
	}
	
	public void set( SimpleField field ){
		level.set( field );
	}
	
	public void set( String id ){
		set( null, id );
	}
	
	public void set( BaseDeclaration declaration ){
		set( declaration, null );
	}
	
	public void set( BaseDeclaration declaration, String id ){
		level.set( declaration, id );
	}
	
    /**
     * Adds a new declaration of a null-type
     * @param kind the kind of declaration 
     * @param name the name of the element
     * @param tags the tags of the node
     * @returns the new declaration
     */
    public BaseDeclaration set( Kind kind, String name, TagSet tags ){
        BaseDeclaration declaratioin = new BaseDeclaration( kind, name, name, stack.getParseFile(), null, tags );
        set( declaratioin );
        return declaratioin;
    }

    /**
     * Adds a new declaration of a null-type
     * @param kind the kind of declaration 
     * @param type the type of this declaration
     * @param name the name of the element
     * @param tags the tags of the node
     * @return the new declaration
     */
    public BaseDeclaration set( Kind kind, Type type, String name, TagSet tags ){
        TypedDeclaration declaration = new TypedDeclaration( kind, type, name, name, stack.getParseFile(), null, tags );
        set( declaration );
        return declaration;
    }
	
	public void pop(){
		pop( null, null );
	}
	
	public void pop( String id ){
		pop( null, id );
	}
	
	public void pop( BaseDeclaration declaration ){
		pop( declaration, null );
	}
	
	public void pop( BaseDeclaration declaration, String id ){
		level = level.pop( declaration, id );
		if( level != null ){
			level.shrink();
		}
	}
	
	private class Level{
		private BaseDeclaration declaration;
		private String id;
		
		private Level parent;
		private List<Level> children;
		private ASTModelPath path;
		
		private List<SimpleField> fields;
		
		public Level( Level parent ){
			this.parent = parent;
		}
		
		public Level getParent(){
			return parent;
		}
		
		public ASTModelPath getPath(){
			if( path == null ){
				if( id == null )
					return null;
				
				if( parent == null ){
					path = new ASTModelPath( stack.getParseFile(), id );
				}
				else{
					ASTModelPath parentPath = parent.getPath();
					if( parentPath == null )
						return null;
				
					path = parentPath.getChild( id );
				}
			}
			
			return path;
		}
		
		public void setDeclaration( BaseDeclaration declaration ){
			this.declaration = declaration;
		}
		
		public BaseDeclaration getDeclaration(){
			return declaration;
		}
		
		public Level push( BaseDeclaration declaration, String id ){
			if( children == null ){
				children = new ArrayList<Level>();
			}
			Level level = new Level( this );
			children.add( level );
			level.set( declaration, id );
			return level;
		}
		
		public Level pop( BaseDeclaration declaration, String id ){
			set( declaration, id );
			return parent;
		}
		
		public void set( BaseDeclaration declaration, String id ){
			if( declaration != null ){
				this.declaration = declaration;
				declarations.add( declaration );
			}
			
			if( id == null && declaration != null )
				this.id = declaration.getName();
			else if( id != null )
				this.id = id;
			
			shrink();
		}
		
		public void set( SimpleField field ){
			if( fields == null ){
				fields = new ArrayList<SimpleField>();
			}
			fields.add( field );
			shrink();
		}
		
		/**
		 * Tries to set the path of this declaration and of all
		 * children.
		 * @return <code>true</code> if all paths are set and this
		 * level can be deleted, <code>false</code> if paths are not yet set.
		 */
		public boolean shrink(){
			ASTModelPath path = getPath();
			if( path == null )
				return false;
			
			if( declaration != null ){
				declaration.setPath( path );
			}
			
			if( fields != null ){
				for( SimpleField field : fields ){
					field.setPath( path );
				}
				fields.clear();
			}
			
			if( children != null ){
				for( int i = children.size()-1; i >= 0; i-- ){
					if( children.get( i ).shrink() ){
						children.remove( i );
					}
				}
			}
			
			return children == null || children.isEmpty();
		}
	}
}
