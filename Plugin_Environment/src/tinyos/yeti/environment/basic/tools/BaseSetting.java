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
package tinyos.yeti.environment.basic.tools;

import java.util.HashMap;
import java.util.Map;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.make.IMakeTarget;
import tinyos.yeti.make.MakeTarget;
import tinyos.yeti.make.targets.IMakeTargetMorpheable;
import tinyos.yeti.utility.XReadStack;
import tinyos.yeti.utility.XWriteStack;

public class BaseSetting {
	protected ProjectTOS project;
    protected Map<String, Object> values = new HashMap<String, Object>();
    
    public BaseSetting( ProjectTOS project ){
        this.project = project;
    }
    

    @SuppressWarnings("unchecked")
    public void write( XWriteStack xml ){
        
        String target = (String)values.get( "target" );
        if( target != null ){
            xml.push( "target" );
            xml.setText( target );
            xml.pop();
        }
        
        String output = getOutput();
        if( output != null ){
            xml.push( "output" );
            xml.setText( output );
            xml.pop();
        }
        
        String driver = getDriver();
        if( driver != null ){
            xml.push( "driver" );
            xml.setText( driver );
            xml.pop();
        }
        
        Map<String,Map<String,String>> tools = (Map<String, Map<String,String>>)values.get( "tools" );
        if( tools != null ){
            xml.push( "tools" );
            String current = getTool();
            if( current != null ){
                xml.setAttribute( "current", current );
            }
            
            for( Map.Entry<String, Map<String,String>> tool : tools.entrySet() ){
                xml.push( "tool" );
                xml.setAttribute( "id", tool.getKey() );
                
                for( Map.Entry<String, String> option : tool.getValue().entrySet() ){
                    xml.push( "option" );
                    xml.setAttribute( "id", option.getKey() );
                    xml.setText( option.getValue() );
                    xml.pop();
                }
                
                xml.pop();
            }
            
            xml.pop();
        }
    }
    
    public void read( XReadStack xml ){
        values.clear();
        
        if( xml.search( "target" )){
            values.put( "target", xml.getText() );
            xml.pop();
        }
        
        if( xml.search( "output" )){
            setOutput( xml.getText() );
            xml.pop();
        }
        
        if( xml.search( "driver" )){
            setDriver( xml.getText() );
            xml.pop();
        }
        
        
        if( xml.search( "tools" )){
            setTool( xml.getAttribute( "current" ) );
            while( xml.hasNext( "tool" )){
                xml.next( "tool" );
                String id = xml.getAttribute( "id" );
                if( id != null ){
                    while( xml.hasNext( "option" )){
                        xml.next( "option" );
                        String optionId = xml.getAttribute( "id" );
                        if( optionId != null ){
                            setToolOption( id, optionId, xml.getText() );
                        }
                        xml.pop();
                    }
                }
                xml.pop();
            }
            xml.pop();
        }
    }
        
    
    public ProjectTOS getProject(){
        return project;
    }
    
    protected String validate( String value ){
        if( value == null )
            return null;
        
        value = value.trim();
        if( value.equals( "" ))
            return null;
        
        return value;
    }

    public void setTarget( IMakeTargetMorpheable morph ){
    	MakeTarget target = morph == null ? null : morph.toMakeTarget();
    	
        if( target == null )
            values.remove( "target" );
        else
            values.put( "target", target.getId() );
    }
    
    public IMakeTarget getTarget(){
        String name = (String)values.get( "target" );
        if( name == null )
            return null;
        
        IMakeTargetMorpheable[] targets = project.getMakeTargets().getSelectableTargets();
        for( IMakeTargetMorpheable target : targets ){
        	IMakeTarget check = target.toMakeTarget();
        	
            if( check.getId().equals( name ))
                return check;
        }
        
        // try names
        MakeTarget[] standards = project.getMakeTargets().getStandardTargets();
        for( MakeTarget target : standards ){
        	if( target.getName().equals( name )){
        		return target;
        	}
        }
        
        return null;
    }
    
    public void setOutput( String path ){
        path = validate( path );
        if( path == null )
            values.remove( "output" );
        else
            values.put( "output", path );
    }
    
    public String getOutput(){
        return (String)values.get( "output" );
    }
    
    public void setDriver( String driver ){
        driver = validate( driver );
        if( driver == null )
            values.remove( "driver" );
        else
            values.put( "driver", driver );
    }
    
    public String getDriver(){
        return (String)values.get( "driver" );
    }
    
    public void setTool( String tool ){
        tool = validate( tool );
        if( tool == null )
            values.remove( "tool" );
        else
            values.put( "tool", tool );
    }
    
    public String getTool(){
        return (String)values.get( "tool" );
    }
    
    @SuppressWarnings("unchecked")
    public String[] getToolOptions( String tool ){
        Map<String,Map<String,String>> tools = (Map<String, Map<String,String>>)values.get( "tools" );
        if( tools == null )
            return new String[]{};
        
        Map<String,String> thistool = tools.get( tool );
        if( thistool == null )
            return new String[]{};
        
        return thistool.keySet().toArray( new String[ thistool.size() ] );
    }
    
    @SuppressWarnings("unchecked")
    public void setToolOption( String tool, String option, String value ){
        value = validate( value );
        Map<String,Map<String,String>> tools = (Map<String, Map<String, String>>)values.get( "tools" );
        if( tools == null ){
            if( value == null )
                return;
            
            tools = new HashMap<String, Map<String,String>>();
            values.put( "tools", tools );
        }
        
        Map<String,String> thistool = tools.get( tool );
        if( thistool == null ){
            if( value == null )
                return;
            
            thistool = new HashMap<String, String>();
            tools.put( tool, thistool );
        }
        
        if( value == null )
            thistool.remove( option );
        else
            thistool.put( option, value );
        
        if( thistool.isEmpty() ){
            tools.remove( tool );
            if( tools.isEmpty() ){
                values.remove( "tools" );
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public String getToolOption( String tool, String option, String defaultValue ){
        Map<String,Map<String,String>> tools = (Map<String, Map<String,String>>)values.get( "tools" );
        if( tools == null )
            return defaultValue;
        
        Map<String,String> thistool = tools.get( tool );
        if( thistool == null )
            return defaultValue;
        
        String result = thistool.get( option );
        if( result == null )
            return defaultValue;
        
        return result;
    }
}
