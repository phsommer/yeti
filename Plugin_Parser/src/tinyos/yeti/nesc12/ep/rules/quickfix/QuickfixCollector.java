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
package tinyos.yeti.nesc12.ep.rules.quickfix;

import java.util.ArrayList;
import java.util.List;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.fix.IMultiMarkerResolution;
import tinyos.yeti.ep.fix.ISingleMarkerResolution;

public class QuickfixCollector{
    private IParseFile file;
    private ProjectTOS project;
    
    private List<ISingleQuickfix> singleFixes = new ArrayList<ISingleQuickfix>();
    private List<IMultiQuickfix> multiFixes = new ArrayList<IMultiQuickfix>();
    
    public QuickfixCollector( IParseFile file, ProjectTOS project ){
        this.file = file;
        this.project = project;
    }
    
    public void addSingle( ISingleQuickfix quickfix ){
        if( quickfix != null )
            singleFixes.add( quickfix );
    }
    
    public void addMulti( IMultiQuickfix quickfix ){
        if( quickfix != null )
            multiFixes.add( quickfix );
    }
    
    /**
     * Gets all the currently stored fixes and clears the internal list
     * of fixes.
     * @return the list of stored fixes, can be <code>null</code>
     */
    public ISingleMarkerResolution[] getSingleResolutions(){
        if( singleFixes == null )
            return null;
        
        ISingleMarkerResolution[] result = new ISingleMarkerResolution[ singleFixes.size() ];
        int index = 0;
        for( ISingleQuickfix fix : singleFixes ){
            result[index++] = new SingleMarkerResolution( fix );
        }
        
        singleFixes.clear();
        return result;
    }

    /**
     * Gets all the currently stored fixes and clears the internal list
     * of fixes.
     * @return the list of stored fixes, can be <code>null</code>
     */
    public IMultiMarkerResolution[] getMultiResolutions(){
        if( multiFixes == null )
            return null;
        
        IMultiMarkerResolution[] result = new IMultiMarkerResolution[ multiFixes.size() ];
        int index = 0;
        for( IMultiQuickfix fix : multiFixes ){
            result[index++] = new MultiMarkerResolution( fix );
        }
        
        multiFixes.clear();
        return result;
    }
    
    public IParseFile getFile(){
        return file;
    }
    
    public ProjectTOS getProject(){
        return project;
    }
}
