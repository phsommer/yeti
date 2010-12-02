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
package tinyos.yeti.nesc12.view.comparators;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A comparator that uses a set of other comparators and returns the
 * first result that does not indicate that both parties are equal.
 * @author Benjamin Sigg
 *
 */
public class MultiComparator<T> implements Comparator<T>{
    private List<Comparator<? super T>> delegates = new ArrayList<Comparator<? super T>>();
    
    public void add( Comparator<? super T> comparator ){
        delegates.add( comparator );
    }
    
    public int compare( T a, T b ){
        for( Comparator<? super T> delegate : delegates ){
            int result = delegate.compare( a, b );
            if( result != 0 )
                return result;
        }
        
        return 0;
    }
}
