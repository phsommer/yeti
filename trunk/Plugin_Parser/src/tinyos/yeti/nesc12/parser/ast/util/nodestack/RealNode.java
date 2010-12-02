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
package tinyos.yeti.nesc12.parser.ast.util.nodestack;

import tinyos.yeti.ep.parser.IASTModelConnectionFilter;
import tinyos.yeti.ep.parser.IASTModelNode;
import tinyos.yeti.ep.parser.IASTModelNodeConnection;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.nesc12.ep.ModelConnection;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.parser.FileRegion;
import tinyos.yeti.nesc12.parser.ast.Range;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;

public class RealNode implements Node{
    private ModelNode node;
    private Range range;
    
    public RealNode( ModelNode node ){
        this.node = node;
    }
    
    public String getIdentifier() {
    	return node.getIdentifier();
    }
    
    public ModelNode getReal(){
        return node;
    }
    
    public void pop( ModelNode node ){
        // ignore
    }

    public void addChild( ModelNode node, ASTNode ast ){
        this.node.addChild( node, ast );
    }

    public void addConnection( ModelConnection connection ){
        node.addChild( connection );
    }

    public void removeChild( final String target ){
        node.removeConnections( new IASTModelConnectionFilter(){
            public boolean include( IASTModelNode parent, IASTModelNodeConnection connection ){
                if( connection.isReference() )
                    return false;
                
                return connection.getIdentifier().equals( target );
            }
        });
    }
    
    public void addFileRegion( FileRegion region ){
        node.addRegion( region );
    }

    public void setRange( Range range ){
	    this.range = range;	
    }
    
    public Range getRange(){
    	return range;
    }
    
    public IFileRegion getRegion(){
	    return node.getRegion();
    }
    
    public void addReference( ModelNode node, ASTNode ast ){
        node.addReference( node, ast );
    }

    public ModelConnection getConnection(){
        return null;
    }

    public ModelNode getNode(){
        return node;
    }

    public void pop(){
        // nothing to do
    }

    public void putErrorFlag(){
        node.putErrorFlag();
    }

    public void putWarningFlag(){
        node.putWarningFlag();   
    }

    public void setConnection( ModelConnection connection ){
        // ignore
    }

    public void setNode( ModelNode node ){
        // ignore
    }
}
