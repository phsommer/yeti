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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A make-exclude can exclude some directories from any search of files.
 * @author Benjamin Sigg
 */
public class MakeExclude{
	
	public static final MakeExclude[] DEFAULT_EXCLUDES = {
			new MakeExclude( ".*\\.svn.*" ) 
		};
	
    private String regex;
    private Pattern pattern;
    private Matcher matcher;
    
    public MakeExclude( String regex ) throws PatternSyntaxException{
        if( regex == null )
            throw new IllegalArgumentException( "regex must not be null" );
        
        this.regex = regex;
        
        pattern = Pattern.compile( regex );
    }
    
    public String getPattern(){
        return regex;
    }
    
    public boolean exclude( String path ){
        if( matcher == null )
            matcher = pattern.matcher( path );
        else
            matcher.reset( path );
        
        return matcher.matches();
    }
    
    @Override
    public int hashCode(){
         return regex.hashCode();
    }
    
    @Override
    public boolean equals( Object obj ){
        if( obj == this )
            return true;
        
        return (obj instanceof MakeExclude) && regex.equals( ((MakeExclude)obj).regex );
    }
}
