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
package tinyos.yeti.environment.winXP.path;

import java.io.File;

import tinyos.yeti.environment.basic.path.IPathTranslator;

/**
 * The list of settings needed to calculate the architecture paths.
 * @author Benjamin Sigg
 */
public class PathSetting{
    private String tosdir;
    private String tosroot;
    private String makerules;
    private String treeLayout;
    
    private File cygwinBash;
    private File cygwinRoot;
    private IPathTranslator translator;
    
    public PathSetting(){
        // nothing
    }
    
    public PathSetting( String tosroot, String tosdir, String makerules,
            String treeLayout,
            File cygwinRoot, File cygwinBash,
            IPathTranslator translator ){
        super();
        this.tosdir = tosdir;
        this.tosroot = tosroot;
        this.makerules = makerules;
        this.treeLayout = treeLayout;
        this.cygwinBash = cygwinBash;
        this.cygwinRoot = cygwinRoot;
        this.translator = translator;
    }
    
    public String getTosdir(){
        return tosdir;
    }
    public void setTosdir( String tosdir ){
        this.tosdir = tosdir;
    }
    public String getTosroot(){
        return tosroot;
    }
    public void setTosroot( String tosroot ){
        this.tosroot = tosroot;
    }
    public String getMakerules(){
        return makerules;
    }
    public void setMakerules( String makerules ){
        this.makerules = makerules;
    }
    public String getTreeLayout(){
        return treeLayout;
    }
    public void setTreeLayout( String treeLayout ){
        this.treeLayout = treeLayout;
    }
    public File getCygwinBash(){
        return cygwinBash;
    }
    public void setCygwinBash( File cygwinBash ){
        this.cygwinBash = cygwinBash;
    }
    public File getCygwinRoot(){
        return cygwinRoot;
    }
    public void setCygwinRoot( File cygwinRoot ){
        this.cygwinRoot = cygwinRoot;
    }
    public IPathTranslator getTranslator(){
        return translator;
    }
    public void setTranslator( IPathTranslator translator ){
        this.translator = translator;
    }
    
    
}
