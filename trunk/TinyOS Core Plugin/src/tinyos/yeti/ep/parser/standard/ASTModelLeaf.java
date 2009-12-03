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
package tinyos.yeti.ep.parser.standard;

import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.parser.*;

public class ASTModelLeaf implements IASTModelNode{
    private ASTModelPath path;
    private String identifier;
    private String name;
    private TagSet tags;
    private String label;
    private IParseFile file;
    private IFileRegion[] origin;
    private IASTFigureContent content;

    public ASTModelLeaf( IASTModelNode parent, String identifier, String name, String label, IParseFile file, IFileRegion[] origin, Tag... tags ){
        this( parent, identifier, name, label, file, origin, TagSet.get( tags ));
    }

    public ASTModelLeaf( IASTModelNode parent, String identifier, String name, String label, IParseFile file, IFileRegion[] origin, TagSet tags ){
        this.identifier = identifier;
        this.name = name;
        this.tags = tags;
        this.label = label;
        this.file = file;
        this.origin = origin;

        if( parent == null )
            path = new ASTModelPath( file, identifier );
        else
            path = (ASTModelPath)parent.getPath().getChild( this );
    }
    
    public IASTModelAttribute[] getAttributes(){
    	return null;
    }

    public IFileRegion getRegion(){
        if( origin == null || origin.length == 0 )
            return null;
        else
            return origin[0];
    }

    public IFileRegion[] getRegions(){
        return origin;
    }

    public IASTModelNodeConnection[] getChildren() {
        return null;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getNodeName(){
    	return name;
    }
    
    public void setNodeName( String name ){
		this.name = name;
	}
    
    public String getLabel() {
        return label;
    }

    public ASTModelPath getPath() {
        return path;
    }
    
    public IASTModelPath getLogicalPath(){
    	return path;
    }

    public IParseFile getParseFile() {
        return file;
    }

    public TagSet getTags() {
        return tags;
    }
    
    public void setContent( IASTFigureContent content ) {
        this.content = content;
    }
    
    public IASTFigureContent getContent() {
        return content;
    }
    
    public void removeConnections( IASTModelConnectionFilter filter ){
        // ignore
    }
    public INesCDocComment getDocumentation(){
    	return null;
    }
}
