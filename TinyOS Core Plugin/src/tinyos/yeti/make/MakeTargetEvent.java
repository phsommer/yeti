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
package tinyos.yeti.make;

import java.util.EventObject;

import org.eclipse.core.resources.IProject;

public class MakeTargetEvent extends EventObject{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public static final int TARGET_ADD = 1;
    public static final int TARGET_CHANGED = 2;
    public static final int DEFAULT_TARGET_CHANGED = 3;
    public static final int TARGET_REMOVED = 4;
    

    public static final int PROJECT_ADDED = 10;
    public static final int PROJECT_REMOVED = 11;
    public static final int PROJECT_REFRESH = 12;
    
    public static final int SELECTED_TARGET_CHANGED = 20;

    private MakeTarget target;
    private IProject project;
    private int type;

    public MakeTargetEvent( Object source, int type, MakeTarget target ){
        super( source );
        this.type = type;
        this.target = target;
    }

    public MakeTargetEvent( Object source, int type, IProject project ){
        super( source );
        this.type = type;
        this.project = project;
    }

    public int getType() {
        return type;
    }

    public IProject getProject(){
        if( project != null )
        	return project;
        if( target != null )
        	return target.getProject();
        return null;
    }

    public MakeTarget getTarget() {
        return target;
    }

}
