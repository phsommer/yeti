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
package tinyos.yeti.ep;

/**
 * A simple implementation of the {@link IMakeExtra} interface. All
 * values are stored in properties that can be set from outside.
 * @author Benjamin Sigg
 */
public class MakeExtra implements Cloneable{
    private String name;
    
    private String parameterName;
    private String parameterValue;
    
    private boolean askParameterAtCompileTime;
    
    public MakeExtra( IMakeExtraDescription description ){
    	name = description.getName();
    	parameterName = description.getParameterName();
    }
    
    public MakeExtra( String name ){
    	this.name = name;
    }
    
    @Override
    public Object clone() {
        try{
            return super.clone();
        }
        catch( CloneNotSupportedException ex ){
            ex.printStackTrace();
            return null;
        }
    }
    
    public MakeExtra copy() {
        return (MakeExtra)clone();
    }
    
    public void setName( String name ){
        this.name = name;
    }
    
    public String getName(){
        return name;
    }

    public void setParameterName( String parameterName ){
        this.parameterName = parameterName;
    }
    
    public String getParameterName(){
        return parameterName;
    }

    public void setParameterValue( String text ){
        parameterValue = text;
    }
    
    public String getParameterValue(){
        return parameterValue;
    }

    public boolean hasParameter(){
        return parameterName != null;
    }

    public boolean isValid( String parameterValue ){
        return true;
    }

    public void setAskParameterAtCompileTime( boolean value ){
        askParameterAtCompileTime = value;
    }
    
    public boolean askParameterAtCompileTime(){
        return askParameterAtCompileTime;
    }
}
