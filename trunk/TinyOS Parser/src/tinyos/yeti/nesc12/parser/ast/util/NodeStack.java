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
import java.util.LinkedList;
import java.util.List;

import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.ep.NesC12ASTModel;
import tinyos.yeti.nesc12.ep.StandardModelConnection;
import tinyos.yeti.nesc12.ep.nodes.DataObjectTypeModelConnection;
import tinyos.yeti.nesc12.ep.nodes.TypedefModelConnection;
import tinyos.yeti.nesc12.ep.nodes.UnitModelNode;
import tinyos.yeti.nesc12.parser.FileRegion;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.Range;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.DataObjectType;
import tinyos.yeti.nesc12.parser.ast.elements.types.EnumType;
import tinyos.yeti.nesc12.parser.ast.elements.types.TypedefType;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.util.nodestack.ImaginaryNode;
import tinyos.yeti.nesc12.parser.ast.util.nodestack.Node;
import tinyos.yeti.nesc12.parser.ast.util.nodestack.RealNode;
import tinyos.yeti.nesc12.parser.ast.util.nodestack.StackLevel;
import tinyos.yeti.nesc12.parser.preprocessor.DirectiveLinker;
import tinyos.yeti.preprocessor.RangeDescription;

/**
 * The {@link NodeStack} is used to build up a stack of {@link ModelNode}s, 
 * where some nodes might be set to <code>null</code> and be created only
 * at the push-operation.<br>
 * The stack is organized in levels. Each level holds tags and connections
 * for a node which might not yet exist. The node can be set later or discarded.
 * In that case, all children are lost.<br>
 * The {@link NodeStack} tries to collapse its internal representation of the model
 * as soon as possible, if calling {@link #preventChildrenClose(int)} then the stack
 * changes its behavior for a subtree. Methods like {@link #putErrorFlag(ModelConnection, int)}
 * require this option to be activated.</br>
 * Note that it is possible to push a node more than once onto the stack, but
 * it must not appear more than once on the stack at any given time.
 * @author Benjamin Sigg
 *
 */
public class NodeStack{
    private LinkedList<StackLevel> stack = new LinkedList<StackLevel>();
    private NesC12ASTModel model;
    private AnalyzeStack analyze;
    
    private int errorCount;
    private int warningCount;
    
    private boolean onDirectiveAdding = false;
    
    public NodeStack( AnalyzeStack stack, NesC12ASTModel model ){
        this.analyze = stack;
        this.model = model;
    }
    
    public AnalyzeStack getAnalyzeStack(){
        return analyze;
    }
    
    /**
     * Gets the children of the node that is currently on the top of this
     * stack.
     * @return the expected list of children
     */
    public Node[] getChildren(){
    	StackLevel level = stack.getLast();
    	return level.getChildrenNodes();
    }
    
    /**
     * Adds <code>node</code> directly to the model without doing
     * any more checks. Note that this is not the preferred way to interact
     * with the model.
     * @param node the new node
     * @param override if <code>true</code>, then the new node can override
     * an existing node with the same path
     */
    public void addToModel( ModelNode node, boolean override ){
        node.setDeclarationResolver( analyze.getDeclarationResolver() );
        model.addNode( node, override );
    }
    
    public void addChild( ModelNode node, ASTNode ast ){
        addChild( node, 0, ast );
    }
    
    /**
     * Adds a child to the node <code>top</code> steps away from the top
     * of the stack.
     * @param node the new child
     * @param top how far to go up in the stack until the parent of <code>node</code>
     * is found
     * @param ast the node responsible for the new connection
     */
    public void addChild( ModelNode node, int top, ASTNode ast ){
        StackLevel level = peekLevel( top );
        if( level != null ){
            level.getNode().addChild( node, ast );
        }
    }
    
    /**
     * Removes the connection to the child <code>target</code> of the node
     * <code>top</code> steps away from the top of the stack.
     * @param target the identifier of the node to remove
     * @param top how far away from the top of the stack, at least 0
     */
    public void removeChild( String target, int top ){
        StackLevel level = peekLevel( top );
        if( level != null ){
            level.getNode().removeChild( target );
        }
    }
    
    public void addReference( ModelNode node, ASTNode ast ){
        addReference( node, 0, ast );
    }

    public void addReference( ModelNode node, int top, ASTNode ast ){
        StackLevel level = peekLevel( top );
        if( level != null ){
            level.getNode().addReference( node, ast );
        }        
    }
    
    public void addConnection( ModelConnection connection ){
        addConnection( connection, 0 );
    }
    
    /**
     * Adds a new child to the node <code>top</code> steps away from
     * the top of the stack.
     * @param connection the new connection
     * @param top how far to go up in the stack until the parent of
     * <code>connection</code> is found
     */
    public void addConnection( ModelConnection connection, int top ){
        StackLevel level = peekLevel( top );
        if( level != null ){
            level.getNode().addConnection( connection );
        }
    }
    
    public void addLocation( RangeDescription range ){
        StackLevel level = peekLevel();
        if( level != null ){
            setupRange( level.getNode(), range );
        }
    }
    
    public void addLocation( ASTNode node ){
        StackLevel level = peekLevel();
        if( level != null ){
            setupRange( level.getNode(), node );
        }
    }
    
    public void setRange( Range range ){
    	StackLevel level = peekLevel();
    	if( level != null ){
    		level.getNode().setRange( range );
    	}
    }
    
    /**
     * Sets the node for the current level. The value is ignored if the node 
     * is already set.
     * @param node the node, <code>null</code> will be ignored
     */
    public void setNode( ModelNode node ){
        StackLevel level = peekLevel();
        if( level != null ){
            level.getNode().setNode( node );
            level.tryLocate();
        }
    }
    
    /**
     * Instead of a node, the current level is set to use a connection. Children
     * for connections are ignored. But some tags of the connection might
     * get changed.
     * @param connection the connection which points to the current node
     */
    public void setConnection( ModelConnection connection ){
        StackLevel level = peekLevel();
        if( level != null ){
            level.getNode().setConnection( connection );
        }
    }
    
    /**
     * Sets the override flag. It indicates that a {@link ModelNode} with the
     * same path as this node might already exist and that this other node
     * must be replaced by the value of this node.
     * @param override <code>true</code> if overriding should be performed
     */
    public void setOverride( boolean override ){
    	StackLevel level = peekLevel();
    	if( level != null ){
    		level.setOverride( override );
    	}
    }
    
    /**
     * Pushes a new node onto the stack. The argument <code>node</code>
     * can be <code>null</code> and set later.
     * @param node the node that gets pushed, might be <code>null</code>
     */
    public void pushNode( ModelNode node ){
        if( node == null )
            pushNode( new ImaginaryNode() );
        else{
            pushNode( new RealNode( node ));
        }
    }
    
    private void pushNode( Node node ){
        StackLevel parent = peekLevel();
        StackLevel level = new StackLevel( this, parent, node );
        if( parent != null )
            parent.addChild( level );
        
        stack.addLast( level );
        level.tryLocate();
    }
    
    private StackLevel peekLevel(){
        if( stack.isEmpty() )
            return null;
        
        return stack.getLast();
    }
    
    private StackLevel peekLevel( int top ){
        if( top < stack.size() ){
            return stack.get( stack.size()-1-top );
        }
        
        return null;
    }
    
    /**
     * Gets the topmost node
     * @return the top node or <code>null</code>
     */
    public Node peekNode(){
        StackLevel level = peekLevel();
        if( level == null )
            return null;
        
        return level.getNode();
    }
    
    /**
     * Pops the top node. If not already set, then elements might be added to
     * <code>node</code>.
     * @param node the node into which to fill all children that were found
     * until now
     */
    public void popNode( ModelNode node ){
        if( node != null )
            setNode( node );
        
        peekLevel().popped();
        
        StackLevel level = stack.removeLast();
        StackLevel top = peekLevel();
        
        List<StackLevel> closed = new ArrayList<StackLevel>();
        
        if( top == null ){
            level.close( closed );
        }
        else{
            top.close( level, closed );
        }
        
        for( StackLevel closedLevel : closed ){
        	closedLevel.executeOnPop();
        }
    }

    public void tryAddDirectives(){
    	if( onDirectiveAdding )
    		return;
    	
    	try{
    		onDirectiveAdding = true;
	    	tryAddDirectives( analyze.getMacroLinker() );
	    	tryAddDirectives( analyze.getIncludeLinker() );
    	}
    	finally{
    		onDirectiveAdding = false;
    	}
    }
    
    private void tryAddDirectives( DirectiveLinker<?> linker ){
    	if( linker != null ){
    		if( peekLevel( 1 ) == null ){
	    		linker.transmitNodes( this );
	    	}
	    	else{
		    	Range range = peekLevel().getNode().getRange();
		    	if( range == null )
		    		return;
		    	
		    	linker.transmitNodes( this, range );
	    	}
		}
    }
    
    /**
     * Executes <code>runnable</code> once the level at distance <code>top</code>
     * from the top level is popped.
     * @param runnable the code to execute, not <code>null</code>
     * @param top the level to access, where 0 is the current top level
     */
    public void executeOnPop( Runnable runnable, int top ){
    	peekLevel( top ).executeOnPop( runnable );
    }
    
    public int size(){
        return stack.size();
    }
    
    public boolean isEmpty(){
        return size() == 0;
    }
    
    /**
     * Sets the {@link Tag#INCLUDED include tag} if <code>ast</code>
     * is included. 
     * @param node some node of the model
     * @param ast some ast node
     */
    public void include( ModelNode node, ASTNode ast ){
        if( ast.isIncluded() ){
            node.getTags().add( Tag.INCLUDED );
        }
    }

    /**
     * Sets the {@link Tag#OUTLINE outline tag} if <code>node</code> is
     * a toplevel node.
     * @param node some node
     * @param top the location of the parent of <code>node</code>, counted
     * from the top of the stack
     */
    public void outline( ModelNode node, int top ){
        StackLevel level = peekLevel( top );
        if( level == null )
            node.getTags().add( Tag.OUTLINE );
        else if( level.getNode().getNode() instanceof UnitModelNode )
            node.getTags().add( Tag.OUTLINE );
    }
    
    private void setupRange( Node node, ASTNode location ){
        RangeDescription ranges = null;
        if( location != null )
            ranges = analyze.getParser().resolveLocation( true, location );

        setupRange( node, ranges );
    }

    private void setupRange( final Node node, RangeDescription location ){
    	FileRegion region = analyze.getRegion( location );
    	if( region != null ){
    		node.addFileRegion( region );
    	}
    }
    
    /**
     * From the moment this method is called no child of the current
     * level can be closed. The tree below this level begins to grow
     * until this level is popped. At that moment this property is
     * canceled and the tree might be closed.
     * @param levels how far the setting should extend. A value of 0 would indicate that
     * exactly this level is affected, which does not make sense. The minimum is 1 to have any
     * effect. A value of -1 stands for infinity.
     */
    public void preventChildrenClose( int levels ){
    	peekLevel().setPreventChildrenClose( levels );
    }
    
    /**
     * Searches for <code>child</code> and adds an error flag to it. This method
     * only works if {@link #preventChildrenClose(int)} was called for the current
     * level or a parent level.
     * @param child the element to search, the first occurrence will be marked as
     * erroneous
     * @param top how deep in the stack the search begins
     */
    public void putErrorFlag( ModelConnection child, int top ){
    	putFlagOnChild( peekLevel( top ), child, true );
    }
    
    /**
     * Searches for <code>child</code> and adds an error flag to it. This method
     * only works if {@link #preventChildrenClose(int)} was called for the current
     * level or a parent level.
     * @param child the element to search, the first occurrence will be marked as
     * erroneous
     * @param top how deep in the stack the search begins
     */
    public void putErrorFlag( ModelNode child, int top ){
    	putFlagOnChild( peekLevel( top ), child, true );
    }
    
    
    private boolean putFlagOnChild( StackLevel level, Object child, boolean error ){
    	boolean onPath = false;
    	Node node = level.getNode();
    	
    	if( node.getNode() == child || node.getConnection() == child ){
    		// found the child
    		onPath = true;
    	}
    	else{
    		// recursive search for the child
    		for( StackLevel next : level ){
    			onPath = putFlagOnChild( next, child, error );
    			if( onPath ){
    				break;
    			}
    		}
    	}
    	
    	if( onPath ){
    		if( error ){
    			node.putErrorFlag();
    		}
    		else{
    			node.putWarningFlag();
    		}
    	}
    	
    	return onPath;
    }
    
    public void putErrorFlag(){
        errorCount++;
        
        for( StackLevel level : stack )
            level.getNode().putErrorFlag();
    }
    
    public int getErrorCount(){
        return errorCount;
    }
    
    /**
     * Searches for <code>child</code> and adds a warning flag to it. This method
     * only works if {@link #preventChildrenClose(int)} was called for the current
     * level or a parent level.
     * @param child the element to search, the first occurrence will be marked as
     * erroneous
     * @param top how deep in the stack the search begins
     */
    public void putWarningFlag( ModelConnection child, int top ){
    	putFlagOnChild( peekLevel( top ), child, false );
    }
    
    /**
     * Searches for <code>child</code> and adds a warning flag to it. This method
     * only works if {@link #preventChildrenClose(int)} was called for the current
     * level or a parent level.
     * @param child the element to search, the first occurrence will be marked as
     * erroneous
     * @param top how deep in the stack the search begins
     */
    public void putWarningFlag( ModelNode child, int top ){
    	putFlagOnChild( peekLevel( top ), child, false );
    }
    
    public void putWarningFlag(){
        warningCount++;
        
        for( StackLevel level : stack )
            level.getNode().putWarningFlag();
    }
    
    public int getWarningCount(){
        return warningCount;
    }
    
    /**
     * Adds a reference to the type <code>type</code> to the topmost
     * node, but only if the type is a complex type
     * @param type the type, <code>null</code> will be ignored
     */
    public void addType( Type type ){
        if( type == null )
            return;
        
        TypedefType typedef = type.asTypedefType();
        if( typedef != null ){
            ModelNode node = analyze.getTypedefModel( typedef.id( true ) );
            if( node != null ){
                addReference( node, null );
            }
            else{
                addConnection( new TypedefModelConnection( typedef, null ) );
            }
            return;
        }
        
        EnumType enumType = type.asEnumType();
        if( enumType != null ){
            ModelNode node = analyze.getTypeTagModel( type.id( true ) );
            if( node != null ){
                addReference( node, null );
            }
            else{
                StandardModelConnection connection = new StandardModelConnection( type.id( false ), null );
                connection.setReference( true );
                connection.setTags( TagSet.get( NesC12ASTModel.ENUMERATION, NesC12ASTModel.TYPE ) );
                
                String name = enumType.getName();
                if( name == null )
                    connection.setLabel( "enum" );
                else
                    connection.setLabel( name );
                
                addConnection( connection );
            }
            return;
        }

        DataObjectType data = type.asDataObjectType();
        if( data != null ){
            ModelNode node = analyze.getTypeTagModel( data.id( true ) );
            if( node != null ){
                addReference( node, null );
            }
            else{
                addConnection( new DataObjectTypeModelConnection( data, null ) );
            }
            return;
        }
        
        if( type.asPointerType() != null ){
            addType( type.asPointerType().getRawType() );
            return;
        }
        
        if( type.asArrayType() != null ){
            addType( type.asArrayType().getRawType() );
            return;
        }
    }
}
