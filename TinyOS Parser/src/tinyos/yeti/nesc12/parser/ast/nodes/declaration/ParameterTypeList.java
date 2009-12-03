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
package tinyos.yeti.nesc12.parser.ast.nodes.declaration;

import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.BaseType;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractListASTNode;

public class ParameterTypeList extends AbstractListASTNode<ParameterDeclaration> {
    private boolean openEnded = false;

    public ParameterTypeList(){
        super( "ParameterTypeList" );
    }

    public ParameterTypeList( ParameterDeclaration child ){
        this();
        add( child );
    }

    public Field[] resolveFields(){
        if( isResolved( "fields" ))
            return resolved( "fields" );

        Field[] fields = new Field[ getChildrenCount() ];
        for( int i = 0; i < fields.length; i++ ){
            ParameterDeclaration decl = getNoError( i );
            if( decl != null )
                fields[i] = decl.resolveField();
        }

        return resolved( "fields", fields );
    }

    /**
     * Probes all declartions and returns the types of all parameters.
     * @param gaps whether to allow <code>null</code> values in the array
     * or to return a smaller array.
     * @return the types of the parameters
     */
    public Type[] resolveTypes( boolean gaps ){
        String id = "types"+gaps;
        if( isResolved( id ))
            return resolved( id );

        Type[] result = new Type[ getChildrenCount() ];
        for( int i = 0, n = result.length; i<n; i++ ){
            ParameterDeclaration decl = getTypedChild( i );
            if( decl != null ){
                result[i] = decl.resolveType();
            }
        }

        if( result.length == 1 ){
            if( result[0] != null ){
                if( result[0].equals( BaseType.VOID )){
                    return resolved( id, new Type[]{} );
                }
            }
        }

        if( !gaps ){
            int count = 0;
            for( int i = 0, n = result.length; i<n; i++ ){
                if( result[i] != null )
                    count++;
            }
            if( count < result.length ){
                Type[] replace = new Type[ count ];
                int c = 0;
                for( int i = 0, n = result.length; i<n; i++ ){
                    if( result[i] != null )
                        replace[c++] = result[i];
                }
                result = replace;
            }
        }

        return resolved( id, result );
    }

    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
    }

    @Override
    public ParameterTypeList add( ParameterDeclaration node ) {
        super.add( node );
        return this;
    }

    public void setOpenEnded( boolean openEnded ) {
        this.openEnded = openEnded;
    }

    public boolean isOpenEnded() {
        return openEnded;
    }

    @Override
    protected void checkChild( ParameterDeclaration child ) throws ASTException {

    }

    @Override
    protected boolean visit( ASTVisitor visitor ) {
        return visitor.visit( this );
    }

    @Override
    protected void endVisit( ASTVisitor visitor ) {
        visitor.endVisit( this );
    }
}
