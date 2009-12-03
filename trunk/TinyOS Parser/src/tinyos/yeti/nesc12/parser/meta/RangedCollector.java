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
package tinyos.yeti.nesc12.parser.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;

/**
 * Creates a {@link RangedCollection} by scanning a tree of scopes.
 * @param <K> the kind of keys for the ranges
 * @param <T> the kind of values associated with the ranges
 * @author Benjamin Sigg
 */
public class RangedCollector<K,T> {
    private List<Level> levels = new ArrayList<Level>();
    private int levelCount = 0;

    private Map<Entry, Integer> running = new HashMap<Entry, Integer>();

    private AnalyzeStack analyzeStack;
    private RangedCollection<T> collection;

    public RangedCollector( AnalyzeStack stack ){
        this.analyzeStack = stack;
        collection = createCollection();

        push();
    }

    protected RangedCollection<T> createCollection(){
        return new RangedCollection<T>();
    }

    /**
     * Stops everything at <code>outputLocation</code> and creates the optimized
     * collection.
     * @param outputLocation the location of end of file
     * @return the collection of ranges
     */
    public RangedCollection<T> close( int outputLocation ){
        levelCount = 0;
        levels.clear();

        for( Map.Entry<Entry, Integer> entry : running.entrySet() ){
            put( entry.getValue().intValue(), outputLocation, entry.getKey().value );
        }

        running.clear();

        collection.optimize();

        return collection;
    }

    /**
     * Informs this collector that a new scope was opened.
     */
    public void push(){
        levelCount++;
        if( levelCount >= levels.size() ){
            levels.add( new Level( levelCount-1 ) );
        }
    }

    /**
     * Informs this collector that the current scope was closed.
     * @param outputLocation the location in the output file where the
     * scope was closed
     */
    public void pop( int outputLocation ){
        levels.get( --levelCount ).clean( outputLocation );
    }

    /**
     * Informs this collector that a new range was issued.
     * @param outputLocation where exactly the new range begins
     * @param key the key for the range
     * @param value the value associated with the range
     * @param top how far the typedef is away from the top of the stack
     */
    public void active( int outputLocation, K key, T value, int top ){
        active( outputLocation, key, value, top, false );
    }

    /**
     * Informs this collector that a new range was issued.
     * @param outputLocation where exactly the new range begins
     * @param key the key for the range
     * @param value the value associated with the range
     * @param top how far the typedef is away from the top of the stack
     * @param override if <code>true</code> then the new value can override
     * an existing value with the same key
     */
    public void active( int outputLocation, K key, T value, int top, boolean override ){
        levels.get( levelCount-top-1 ).active( outputLocation, key, value, override );
    }

    /**
     * Informs this collector that a name was issued that is not a range but
     * might shadow an existing range.
     * @param outputLocation the location of the shadowing range
     * @param key the key for the range
     * @param top how far the field is away from the top of the stack
     */
    public void notactive( int outputLocation, K key, int top ){
        levels.get( levelCount-top-1 ).notactive( outputLocation, key );
    }

    private void put( int outputBegin, int outputEnd, T identifier ){
        int inputBegin = analyzeStack.getParser().getNearestInputLocation( outputBegin );
        int inputEnd = analyzeStack.getParser().getNearestInputLocation( outputEnd );

        collection.put( inputBegin, inputEnd, identifier );
    }

    private class Entry{
        public K key;
        public T value;

        public Entry( K key ){
            this.key = key;
        }

        public Entry( K key, T value ){
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals( Object obj ){
            return key.equals( ((Entry)obj).key );
        }

        @Override
        public int hashCode(){
            return key.hashCode();
        }
    }

    private class Level{
        private int height;

        private Set<Entry> actives = new HashSet<Entry>();
        private Set<K> nonactives = new HashSet<K>();

        public Level( int height ){
            this.height = height;
        }

        public void clean( int outputLocation ){
            // stop any ranges that are defined only in this level
            loop:for( Entry name : actives ){
                // check not active in upper level
                for( int i = height-1; i >= 0; --i ){
                    Level level = levels.get( i );
                    if( level.isNotActive( name.key ))
                        break;
                    
                    if( level.isActive( name.key ))
                        continue loop;
                }

                // ready to remove
                Integer begin = running.remove( name );
                if( begin != null ){
                    put( begin.intValue(), outputLocation, name.value );
                }
            }

            // restart ranges that were set to not active in this level
            for( K key : nonactives ){
                for( int i = height-1; i >= 0; i-- ){
                    Level level = levels.get( i );
                    if( level.isNotActive( key ))
                        break;
                    
                    Entry entry = level.getActiveEntry( key );
                    if( entry != null ){
                        running.put( entry, outputLocation );
                        break;
                    }
                }
            }
        
            actives.clear();
            nonactives.clear();
        }

        public boolean alive( Entry entry ){
            return actives.contains( entry ) || nonactives.contains( entry.key ); 
        }

        public void active( int outputLocation, K key, T identifier, boolean override ){
            Entry entry = new Entry( key, identifier );

            if( actives.add( entry )){
                nonactives.remove( entry.key );

                // check that name is not hidden by name of other level
                for( int i = height+1; i < levelCount; i++ ){
                    Level level = levels.get( i );
                    if( level != this && level.alive( entry ))
                        return;
                }

                // activate new range
                running.put( entry, outputLocation );
            }
            if( override ){
                Integer location = running.get( entry );
                running.put( entry, location );

                for( int i = 0; i <= height; i++ ){
                    Level level = levels.get( i );
                    level.override( entry );
                }
            }
        }

        private void override( Entry entry ){
            if( actives.contains( entry )){
                actives.remove( entry );
                actives.add( entry );
            }
        }
        
        public Entry getActiveEntry( K key ){
            for( Entry check : actives ){
                if( check.key.equals( key )){
                    return check;
                }
            }
            return null;
        }

        public boolean isActive( K key ){
            return getActiveEntry( key ) != null;
        }
        
        public boolean isNotActive( K key ){
            return nonactives.contains( key );
        }
        
        public void notactive( int outputLocation, K key ){
            if( nonactives.add( key )){
                Entry entry = getActiveEntry( key );
                
                if( entry != null ){
                    // delete entry, it cannot be automatically reactivated in
                    // this level
                    actives.remove( entry );
                }
                else{
                    for( int i = height-1; i >= 0 && entry == null; i-- ){
                        entry = levels.get( i ).getActiveEntry( key );
                    }
                }
                
                if( entry != null ){
                    // check that name is not hidden by an entry of another level
                    for( int i = height+1; i < levelCount; i++ ){
                        if( levels.get( i ).alive( entry ))
                            return;
                    }

                    // deactivate range
                    Integer begin = running.remove( entry );
                    if( begin != null ){
                        put( begin.intValue(), outputLocation, entry.value );
                    }
                }
            }
        }
    }
}
