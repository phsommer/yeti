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
package tinyos.yeti.nesc12.parser.ast.util.pushers;

import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.preprocessor.RangeDescription;

/**
 * Used to report errors concerning the redefinition of typedefs, fields and 
 * functions.
 * @author Benjamin Sigg
 */
public abstract class FieldPusher{
    protected AnalyzeStack stack;
    protected List<Definition> definitions = new ArrayList<Definition>();

    protected String name;

    public FieldPusher( String name, AnalyzeStack stack ){
        this.name = name;
        this.stack = stack;
    }

    public String getName(){
        return name;
    }
    
    public AnalyzeStack getStack(){
        return stack;
    }
    
    protected List<Definition> getDefinitions(){
        return definitions;
    }
    
    public void pushField( Name name, Field field, boolean forwardDeclaration, ModelNode model ){
        definitions.add( new Definition( name, null, field, false, forwardDeclaration, model ) );
    }

    public void pushType( Name name, Type type, ModelNode model ){
        definitions.add( new Definition( name, type, null, true, false, model ) );
    }

    /**
     * Checks the errors that come from redefinition and redeclarations.
     */
    public abstract void resolve();

    protected void warning( String message, Name locations ){
        stack.warning( message, ranges( locations ) );
    }
    
    protected void warning( String message, List<Name> locations ){
        stack.warning( message, ranges( locations ) );
    }

    protected void error( String message, Name locations ){
        stack.error( message, ranges( locations ) );
    }
    
    protected void error( String message, List<Name> locations ){
        stack.error( message, ranges( locations ) );
    }

    protected RangeDescription[] ranges( Name location ){
        RangeDescription range = location.getRange();
        if( range != null )
        	return new RangeDescription[]{ range };
        return new RangeDescription[]{};
    }
    
    protected RangeDescription[] ranges( List<Name> locations ){
        List<RangeDescription> result = new ArrayList<RangeDescription>( locations.size() );
        for( Name name : locations ){
            RangeDescription range = name.getRange();
            if( range != null )
                result.add( range );
        }
        return result.toArray( new RangeDescription[ result.size() ] );
    }

    protected class Definition{
        public Name name;
        private Type type;
        private Field field;

        public boolean typedef;
        public boolean forwardDeclaration;
        
        private ModelNode model;

        public Definition( Name name, Type type, Field field, boolean typedef, boolean forwardDeclaration, ModelNode model ){
            this.name = name;
            this.type = type;
            this.field = field;
            this.typedef = typedef;
            this.forwardDeclaration = forwardDeclaration;
            this.model = model;
        }

        public Type type(){
            if( field == null )
                return type;
            else
                return field.getType();
        }
        
        public Modifiers modifiers(){
            if( field == null )
                return null;
            
            return field.getModifiers();
        }
        
        public Name getName(){
            return name;
        }
        
        public Field getField(){
            return field;
        }
        
        public void putErrorFlag(){
            if( model != null ){
                model.putErrorFlag();
                
                ASTModelPath path = model.getPath();
                if( path != null ){
                	path = path.getParent();
                	
                	while( path != null ){
                		IASTModelNode node = stack.getModel().getNode( path );
                		if( node instanceof ModelNode ){
                			ModelNode mnode = (ModelNode)node;
                			if( !mnode.putErrorFlag() ){
                				break;
                			}
                		}
                		path = path.getParent();
                	}
                }
            }
        }
        
        public void putWarningFlag(){
            if( model != null ){
                model.putWarningFlag();
                
                ASTModelPath path = model.getPath();
                if( path != null ){
                	path = path.getParent();
                	
                	while( path != null ){
                		IASTModelNode node = stack.getModel().getNode( path );
                		if( node instanceof ModelNode ){
                			ModelNode mnode = (ModelNode)node;
                			if( !mnode.putWarningFlag() ){
                				break;
                			}
                		}
                		path = path.getParent();
                	}
                }
            }
        }
    }
}
