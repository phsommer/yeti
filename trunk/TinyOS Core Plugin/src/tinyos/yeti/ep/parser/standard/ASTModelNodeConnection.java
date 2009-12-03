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
import tinyos.yeti.ep.parser.IASTModelAttribute;
import tinyos.yeti.ep.parser.IASTModelNodeConnection;
import tinyos.yeti.ep.parser.IASTModelPath;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.TagSet;

public class ASTModelNodeConnection implements IASTModelNodeConnection{
	private String identifier;
    private String label;
    private ASTModelPath path;
    private IFileRegion[] regions;
    private TagSet tags;
    private boolean reference;

    public ASTModelNodeConnection( ASTModelNode parent, boolean reference,
            String identifier, String label, IFileRegion[] regions, TagSet tags ){
        super();
        
        path = parent.getPath();
        
        this.reference = reference;
        this.identifier = identifier;
        this.label = label;
        this.regions = regions;
        this.tags = tags;
    }

    public String getIdentifier(){
        return identifier;
    }
    
    public IASTModelAttribute[] getAttributes(){
    	return null;
    }
    
    public String getLabel(){
        return label;
    }

    public IParseFile getParseFile() {
        return path.getParseFile();
    }
    
    public ASTModelPath getPath() {
        return path;
    }

    public TagSet getTags(){
        return tags;
    }

    public boolean isReference(){
        return reference;
    }

    public IFileRegion getRegion(){
        if( regions == null || regions.length == 0 )
            return null;
        return regions[0];
    }

    public IFileRegion[] getRegions(){
        return regions;
    }
    
    public IASTModelPath getReferencedPath(){
        return null;
    }
}
