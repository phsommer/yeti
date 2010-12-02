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
package tinyos.yeti.creation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Rule{
    public String name;
    private List<List<Rule>> lines = new ArrayList<List<Rule>>();
    public Set<Rule> children = new HashSet<Rule>();
    public boolean marked = false;
    
    private boolean visiting = false;
    
    public Rule( String name ){
        this.name = name;
    }
    
    public void mark(){
        marked = true;
    }
    
    public void markchildren(){
        for( Rule rule : children )
            rule.mark();
    }
    
    public void markall(){
        if( visiting )
            return;
        visiting = true;
        marked = true;
        System.out.println( name );
        
        for( Rule rule : children ){
            rule.markall();
        }
    }
    
    public void cleanup(){
        if( visiting ){
            visiting = false;
            for( Rule rule : children ){
                rule.cleanup();
            }
        }
    }
    
    public void umark(){
        marked = false;
    }
    
    public void print(){
        System.out.println( name + ":" );
        for( List<Rule> line : lines ){
            System.out.print( "\t" );
            for( Rule rule : line ){
                System.out.print( " " );
                System.out.print( rule.name );
            }
            System.out.println();
        }
    }
    
    public void cup(){
        System.out.println( name + " ::=" );
        boolean first = true;
        
        for( List<Rule> line : lines ){
            System.out.print( "\t" );
            if( first ){
                System.out.print( "  " );
                first = false;
            }
            else{
                System.out.print( "| " );
            }
            for( Rule rule : line ){
                System.out.print( " " );
                System.out.print( rule.name );
            }
            System.out.println();
        }
        
        System.out.println( "\t;" );
    }
    
    public void children(){
        for( Rule rule : children ){
            System.out.println( rule.name );
        }
    }
    
    public void line( String line, Map<String, Rule> rules ){
        line = line.trim();
        StringBuilder builder = new StringBuilder();
        boolean block = false;
        char before = 0;
        
        List<Rule> lineList = new ArrayList<Rule>();
        
        for( int i = 0, n = line.length(); i<n; i++ ){
            char next = line.charAt( i );
            if( next == '\'' ){
                block = !block;
            }
            if( before == '/' && next == '*'){
                block = true;
            }
            if( before == '*' && next == '/' ){
                block = false;
            }
     
            if( !block && Character.isWhitespace( next ) ){
                if( builder.length() > 0 ){
                    String name = builder.toString();
                    name = check( name );
                    Rule rule = rules.get( name );
                    if( rule == null ){
                        rule = new Rule( name );
                        rules.put( name, rule );
                    }
                    lineList.add( rule );
                    children.add( rule );
                    builder.setLength( 0 );
                }
                before = 0;
            }
            else{
                builder.append( next );
                before = next;
            }
        }
        
        if( builder.length() > 0 ){
            String name = builder.toString();
            name = check( name );
            Rule rule = rules.get( name );
            if( rule == null ){
                rule = new Rule( name );
                rules.put( name, rule );
            }
            children.add( rule );
            lineList.add( rule );
        }
        
        if( lineList.size() > 0 ){
            lines.add( lineList );
        }
    }
    
    private String check( String name ){
        int index = name.indexOf( ":" );
        if( index > 0 )
            return name.substring( 0, index );
        
        return name;
    }
}