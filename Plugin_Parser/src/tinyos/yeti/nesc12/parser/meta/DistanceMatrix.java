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

import java.awt.Point;
import java.util.Collection;

/**
 * Stores the distances of pairs of elements.
 * 
 * @author Benamin Sigg
 *
 * @param <C> the type of the clusters
 * @param <N> the type of elements the clusters are built upon
 */
public abstract class DistanceMatrix<C, N> {
    private double[][] matrix;
    private C[] clusters;
    private Distance<C> distance;

    /**
     * Initializes a new matrix, does not build the cluster-tree
     * @param elements the elements of the base clusters
     */
    @SuppressWarnings( "unchecked" )
    public DistanceMatrix( Collection<N> elements, Distance<C> distance ){
        this.distance = distance;
        matrix = new double[ elements.size() ][];
        clusters = (C[])new Object[ elements.size() ];

        int index = 0;
        for( N element : elements )
            clusters[index++] = create( element );

        for( int i = 0; i < clusters.length; i++ ){
            matrix[i] = new double[i];

            for( int j = 0; j < i; j++  ){
                matrix[i][j] = distance.distance( clusters[i], clusters[j] );
            }
        }
    }

    protected abstract C create( N element );

    protected abstract C create( C a, C b );

    public int getClusterSize(){
        return clusters.length;
    }

    public C getCluster( int index ){
        return clusters[ index ];
    }

    /**
     * Merges clusters <code>location.x</code> and <code>location.y</code>
     * into one new cluster.
     * @param location the location of the cluster
     */
    public void cluster( Point location ){
        // merge
        C a = clusters[ location.x ];
        C b = clusters[ location.y ];

        int newCluster = Math.min( location.x, location.y );
        int oldCluster = Math.max( location.x, location.y );

        clusters[ newCluster ] = create( a, b );
        clusters[ oldCluster ] = null;

        rebuildMatrix( newCluster, oldCluster );
    }

    protected void rebuildMatrix( int newCluster, int oldCluster ){
        // rebuild matrix
        for( int j = 0; j < newCluster; j++ ){
            if( clusters[j] != null )
                matrix[ newCluster ][j] = distance.distance( clusters[ newCluster ], clusters[ j ]);
        }

        for( int i = newCluster+1; i < clusters.length; i++ ){
            if( clusters[i] != null )
                matrix[i][ newCluster ] = distance.distance( clusters[i], clusters[ newCluster ] );
        }
    }

    protected void rebuildMatrix(){
        for( int i = 0; i < clusters.length; i++ ){
            if( clusters[i] != null ){
                for( int j = 0; j < i; j++  ){
                    if( clusters[j] != null ){
                        matrix[i][j] = distance.distance( clusters[i], clusters[j] );
                    }
                }
            }
        }
    }

    /**
     * Searches the row and column with the minimal distance.
     * @param buffer point to write the result into, can be <code>null</code>
     * @return either <code>buffer</code> or a new point
     */
    public Point findMin( Point buffer ){
        if( buffer == null )
            buffer = new Point();

        int x = -1;
        int y = -1;
        double min = Double.POSITIVE_INFINITY;

        for( int i = 0; i < matrix.length; i++ ){
            if( clusters[i] != null ){
                for( int j = 0; j < i; j++ ){
                    if( clusters[j] != null ){
                        if( matrix[i][j] <= min ){
                            min = matrix[i][j];
                            x = i;
                            y = j;
                        }
                    }
                }
            }
        }

        if( x != -1 && y != -1 && min == Double.POSITIVE_INFINITY )
            throw new IllegalStateException( "Distance of elements is positive infinity" );

        buffer.x = x;
        buffer.y = y;

        return buffer;
    }
}
