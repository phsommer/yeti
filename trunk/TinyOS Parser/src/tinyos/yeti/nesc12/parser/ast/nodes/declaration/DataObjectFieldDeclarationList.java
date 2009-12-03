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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.Name;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.ArrayType;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractListASTNode;

public class DataObjectFieldDeclarationList extends AbstractListASTNode<DataObjectFieldDeclaration> {
    public DataObjectFieldDeclarationList(){
        super( "DataObjectFieldDeclarationList" );
    }
    
    public DataObjectFieldDeclarationList( DataObjectFieldDeclaration child ){
        this();
        add( child );
    }
    
    @Override
    public DataObjectFieldDeclarationList add( DataObjectFieldDeclaration node ) {
        super.add( node );
        return this;
    }
    
    /**
     * Tries to find all fields that are described in this declaration list.
     * @return the list of fields that were found
     */
    public List<Field> resolveFields(){
        if( isResolved( "fields" ))
            return resolved( "fields" );
        
        List<Field> fields = new LinkedList<Field>(); 
        
        for( int i = 0, n = getChildrenCount(); i<n; i++ ){
            DataObjectFieldDeclaration decl = getNoError( i );
            if( decl != null ){
                decl.resolveFields( fields );
            }
        }
        
        return resolved( "fields", fields );
    }

    @Override
    public void resolve( AnalyzeStack stack ) {
        super.resolve( stack );
        
        if( stack.isReportErrors() ){
            checkFieldDeclarations( stack );
        }
    }
    
    private void checkFieldDeclarations( AnalyzeStack stack ){
        List<Field> fields = resolveFields();
        if( fields == null )
            return;

        // the stack will handle all checks when leaving the current scope
        for( Field field : fields ){
            if( field != null ){
                if( field.getName() != null ){
                    stack.putField( field, field.getRange().getRight(), false );
                }
                if( field.getType() != null ){
                    if( field.getType().asFunctionType() != null ){
                        Name name = field.getName();
                        if( name != null )
                            stack.error( "function '" + name.toIdentifier() + "' may not be declared in data object", name.getRange() );
                        else
                            stack.error( "function may not be declared in data object", field.getRange() );
                        
                        if( field.asNode() != null )
                            field.asNode().putErrorFlag();
                    }
                }
            }
        }
    }

    public void checkIncompleteTypes( AnalyzeStack stack, boolean struct ){
        if( !struct )
            return;

        List<Field> fields = resolveFields();
        if( fields == null || fields.size() == 0 )
            return;

        // There may not be any field with unknown size for the fields
        for( Field field : fields ){
            Type type = field.getType();
            if( type != null ){
                if( type.asDataObjectType() != null ){
                    if( type.asDataObjectType().isUnsized() ){
                        stack.error( "structure with incomplete array element may not be used in another structure", field.getRange() );
                        if( field.asNode() != null )
                            field.asNode().putErrorFlag();
                    }
                }
            }
        }

        // There may not be any incomplete types as fields, with the exception
        // of the last field which might be an incomplete array.
        int size = fields.size();
        int index = 0;
        Iterator<Field> iterator = fields.iterator();

        while( index < size && iterator.hasNext() ){
            Field next = iterator.next();
            Type type = next == null ? null : next.getType();
            if( type != null ){
                if( type.isIncomplete() ){
                    if( type.asArrayType() != null ){
                        if( index+1 < size ){
                            stack.error( "incomplete array type may be used only as last field of a structure", next.getRange() );
                            if( next.asNode() != null )
                                next.asNode().putErrorFlag();
                        }
                    }
                    else{
                        stack.error( "incomplete type may not be used in a structure", next.getRange() );
                        if( next.asNode() != null )
                            next.asNode().putErrorFlag();
                    }
                }
                else if( type.asArrayType() != null && type.asArrayType().getSize() == ArrayType.Size.VARIABLE ){
                    if( index+1 < size ){
                        stack.error( "variable sized array may be used only as last field of structure", next.getRange() );
                        if( next.asNode() != null )
                            next.asNode().putErrorFlag();
                    }
                }
            }

            index++;
        }
    }

    @Override
    protected void checkChild( DataObjectFieldDeclaration child ) throws ASTException {
        
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
