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

import tinyos.yeti.ep.parser.standard.ASTModelPath;
import tinyos.yeti.nesc12.ep.ModelNode;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.DataObjectType;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;

public class IncompleteDataObject extends AbstractFixedASTNode implements TypeSpecifier{
    public static final String SPECIFIER = "specifier";
    public static final String NAME = "name";

    public IncompleteDataObject(){
        super( "IncompleteDataObject", SPECIFIER, NAME );
    }

    public IncompleteDataObject( ASTNode specifier, ASTNode name ){
        this();
        setField( SPECIFIER, specifier );
        setField( NAME, name );
    }

    public IncompleteDataObject( DataObjectSpecifier specifier, Identifier name ){
        this();
        setSpecifier( specifier );
        setName( name );
    }

    public boolean isStorageClass() {
        return false;
    }

    public boolean isSpecifier() {
        return true;
    }

    public boolean isPrimitive() {
        return false;
    }

    public boolean isDataObject() {
        return true;
    }

    public boolean isEnum() {
        return false;
    }

    public boolean isTypedefName() {
        return false;
    }
    
    public boolean isAttribute(){
        return false;
    }

    public Type resolveType(){
        if( isResolved( "type" ))
            return resolved( "type" );

        DataObjectSpecifier dataSpecifier = getSpecifier();
        if( dataSpecifier == null )
            return resolved( "type" );

        DataObjectSpecifier.Specifier specifier = dataSpecifier.getSpecifier();
        if( specifier == null )
            return resolved( "type" );

        Identifier name = getName();
        switch( specifier ){
            case NX_STRUCT:
                return resolved( "type", DataObjectType.nxstruct( name ));
            case STRUCT:
                return resolved( "type", DataObjectType.struct( name ));
            case NX_UNION:
                return resolved( "type", DataObjectType.nxunion( name ));
            case UNION:
                return resolved( "type", DataObjectType.union( name ));
        }

        return resolved( "type", null );
    }

    @Override
    public void resolve( AnalyzeStack stack ) {
        // TODO method not implemented
        super.resolve( stack );
        stack.checkCancellation();

        Identifier name = getName();
        if( name != null ){
            Type type = stack.getTypeTag( name.getName() );
            boolean resolved = false;
            
            if( type != null ){
                if( checkValidComplete( stack, type ) ){
                    resolved( "type", type );
                    resolved = true;
                }
                
                if( stack.isCreateReferences() ){
                	ASTModelPath path = null;
                	ModelNode node = stack.getTypeTagModel( type.id( true ) );
                	if( node != null )
                		path = node.getPath();
                	if( path == null )
                		path = stack.getTypeTagPath( name.getName() );
                	stack.reference( this, path );
                }
            }
            
            if( !resolved ){
                type = resolveType();
                if( type != null ){
                    stack.putTypeTag( name, type, null, name.getRange().getRight() );
                }
            }
        }
    }

    /**
     * Checks whether <code>type</code> is a valid substitution for this
     * incomplete type.
     * @param stack used to report errors
     * @param type the type to check
     * @return <code>true</code> if the type is valid
     */
    private boolean checkValidComplete( AnalyzeStack stack, Type type ){
        DataObjectType data = type.asDataObjectType();

        Identifier name = getName();
        DataObjectSpecifier dataSpecifier = getSpecifier();
        DataObjectSpecifier.Specifier specifier = dataSpecifier == null ? null : dataSpecifier.getSpecifier();

        if( data == null ){
            if( stack.isReportErrors() ){
                if( specifier == null ){
                    stack.error( "existing tag '" + name.getName() + "' is of wrong kind", name );
                }
                else{
                    stack.error( "existing tag '" + name.getName() + "' is not a '" + specifier + "'" , name );
                }
            }

            return false;
        }

        if( data.getKind() == null || specifier == null )
            return false;

        switch( data.getKind() ){
            case ATTRIBUTE:
                if( stack.isReportErrors() )
                    stack.error( "existing tag '" + name.getName() + "' is of type 'attribute', not '" + specifier + "'", name );
                return false;
            case NX_STRUCT:
                if( specifier != DataObjectSpecifier.Specifier.NX_STRUCT ){
                    if( stack.isReportErrors() )
                        stack.error( "existing tag '" + name.getName() + "' is of type 'nx_struct', not '" + specifier + "'", name );
                    return false;
                }
                break;
            case NX_UNION:
                if( specifier != DataObjectSpecifier.Specifier.NX_UNION ){
                    if( stack.isReportErrors() )
                        stack.error( "existing tag '" + name.getName() + "' is of type 'nx_union', not '" + specifier + "'", name );
                    return false;
                }
                break;
            case STRUCT:
                if( specifier != DataObjectSpecifier.Specifier.STRUCT ){
                    if( stack.isReportErrors() )
                        stack.error( "existing tag '" + name.getName() + "' is of type 'struct', not '" + specifier + "'", name );
                    return false;
                }
                break;
            case UNION:
                if( specifier != DataObjectSpecifier.Specifier.UNION ){
                    if( stack.isReportErrors() )
                        stack.error( "existing tag '" + name.getName() + "' is of type 'union', not '" + specifier + "'", name );
                    return false;
                }
                break;
        }

        return true;
    }

    public DataObjectSpecifier getSpecifier(){
        return (DataObjectSpecifier)getNoError( 0 );
    }
    public void setSpecifier( DataObjectSpecifier specifier ){
        setField( 0, specifier );
    }

    public Identifier getName(){
        return (Identifier)getNoError( 1 );
    }
    public void setName( Identifier name ){
        setField( 1, name );
    }

    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ){
            if( !( node instanceof DataObjectSpecifier))
                throw new ASTException( node, "Must be a DataObjectSpecifier" );
        }

        if( index == 1 ){
            if( !( node instanceof Identifier ))
                throw new ASTException( node, "Must be an Identifier" );
        }
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
