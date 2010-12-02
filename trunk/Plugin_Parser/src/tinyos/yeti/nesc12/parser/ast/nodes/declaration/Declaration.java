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

import tinyos.yeti.nesc12.Parser;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.Range;
import tinyos.yeti.nesc12.parser.ast.elements.Modifiers;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.types.BaseType;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.Flag;
import tinyos.yeti.nesc12.parser.ast.nodes.Key;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.ExternalDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.NesCExternalDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.BlockItem;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.Statement;
import tinyos.yeti.nesc12.parser.ast.util.ControlFlow;
import tinyos.yeti.nesc12.parser.ast.util.ModifierValidator;

public class Declaration extends AbstractFixedASTNode implements BlockItem, ExternalDeclaration, Statement, NesCExternalDefinition{
    public static void main( String[] args ) {
        String code = 
                // "const int x = 3;" 
                 "const int *y = &x;";
                // "command int x;";
        
        Declaration decl = Parser.quickParser( code, Declaration.class );
        System.out.println( decl );
    }
    
    public static final String SPECIFIERS = "specifiers";
    public static final String INIT_LIST = "initlist";
    
    public static final Flag SPECIFIERS_WITH_ERROR = new Flag( "error" );
    public static final Flag SPECIFIERS_WITH_WARNING = new Flag( "warning" );
    public static final Key<Declaration> DECLARATION = new Key<Declaration>( "declaration" );
    
    public Declaration(){
        super( "Declaration", SPECIFIERS, INIT_LIST );
    }
    
    public Declaration( ASTNode specifiers, ASTNode initlist ){
        this();
        setField( SPECIFIERS, specifiers );
        setField( INIT_LIST, initlist );
    }
    
    public Declaration( DeclarationSpecifierList specifiers, InitDeclaratorList initlist ){
        this();
        setSpecifiers( specifiers );
        setInitlist( initlist );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ) {
        cleanResolved();
        stack.put( DECLARATION, this );
        
        DeclarationSpecifierList specifiers = getSpecifiers();
        InitDeclaratorList inits = getInitlist();
        
        int nodeErrorCount = 0;
        int nodeWarningCount = 0;
        if( stack.isCreateModel() ){
            nodeErrorCount = stack.getNodeStack().getErrorCount();
            nodeWarningCount = stack.getNodeStack().getWarningCount();    
        }
        
        ModifierValidator baseChecker = null;
        if( stack.isReportErrors() ){
            baseChecker = stack.get( ModifierValidator.MODIFIER_VALIDATOR );
            if( baseChecker != null )
                stack.put( ModifierValidator.MODIFIER_VALIDATOR, new InitDeclaratorChecker( baseChecker ) );
        }
        
        Type type = null;
        
        if( specifiers != null ){
            specifiers.resolve( stack );
            stack.checkCancellation();
            
            if( stack.isReportErrors() ){ 
                specifiers.checkResolvesType( stack );
            }
            type = specifiers.resolveType();

            Modifiers modifiers = specifiers.resolveModifiers();

            if( type != null ){
                stack.put( Initializer.BASE_TYPE, type );
            
                if( modifiers != null && modifiers.isRestrict() ){
                    // TODO check restrict: types other than pointer types derived from object or incomplete types shall not be restrict-qualified                    
                }
            }

            if( modifiers != null )
                stack.put( InitDeclarator.MODIFIERS, modifiers );
        }
        else{
            resolveError( 0, stack );
        }
        
        if( inits != null ){
            boolean typedef = specifiers.isTypedef();
            if( typedef )
                stack.put( InitDeclarator.TYPEDEF );
            
            boolean error = false;
            boolean warning = false;
            
            if( stack.isCreateModel() ){
                error = nodeErrorCount != stack.getNodeStack().getErrorCount();
                if( error )
                    stack.put( SPECIFIERS_WITH_ERROR );

                warning = nodeWarningCount != stack.getNodeStack().getWarningCount();
                if( warning )
                    stack.put( SPECIFIERS_WITH_WARNING );
            }
            
            inits.resolve( stack );
            stack.checkCancellation();
            
            if( error )
            	stack.remove( SPECIFIERS_WITH_ERROR );
            
            if( warning )
            	stack.remove( SPECIFIERS_WITH_WARNING );
            
            if( typedef )
                stack.remove( InitDeclarator.TYPEDEF );
            
            if( stack.isReportErrors() && inits.getChildrenCount() == 0 ){
                boolean report = false;
                if( typedef ){
                    report = true;
                }
                else{
                    if( type != null ){
                        report = type.asDataObjectType() == null && type.asEnumType() == null;
                    }
                }
                
                if( report ){
                    stack.warning( "declaration does declare nothing", this );
                }
            }
        }
        else{
            resolveError( 1, stack );
        }
        
        stack.remove( Initializer.BASE_TYPE );
        stack.remove( InitDeclarator.MODIFIERS );
        stack.remove( DECLARATION );
        
        if( baseChecker != null )
            stack.put( ModifierValidator.MODIFIER_VALIDATOR, baseChecker );
    }
    
	@Override
	public Range getCommentAnchor(){
		return getRange();
	}
    
    public DeclarationSpecifierList getSpecifiers(){
        return (DeclarationSpecifierList)getNoError( 0 );
    }
    
    public void setSpecifiers( DeclarationSpecifierList specifiers ){
        setField( 0, specifiers );
    }
    
    public InitDeclaratorList getInitlist(){
        return (InitDeclaratorList)getNoError( 1 );
    }
    
    public void setInitlist( InitDeclaratorList initlist ){
        setField( 1, initlist );
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException {
        if( index == 0 ){
            if( !( node instanceof DeclarationSpecifierList ))
                throw new ASTException( node, "Must be a DeclarationSpecifierList" );
        }
        
        if( index == 1 ){
            if( !(node instanceof InitDeclaratorList) )
                throw new ASTException( node, "Must be an InitDeclaratorList" );
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
    
    public void flow( ControlFlow flow ){
        flow.follow( this );
    }
    
    public boolean isFunctionEnd(){
        return false;
    }
    
    public Type resolveExpressionType(){
        return BaseType.VOID;
    }
    
    private class InitDeclaratorChecker implements ModifierValidator{
        private ModifierValidator base;
        
        public InitDeclaratorChecker( ModifierValidator base ){
            this.base = base;
        }
        
        public void check( AnalyzeStack stack, DeclarationSpecifierList specifiers, InitDeclarator declaration ){
            base.check( stack, getSpecifiers(), declaration );
        }

        public void check( AnalyzeStack stack, FunctionDefinition definition ){
            base.check( stack, definition );
        }

        public void check( AnalyzeStack stack, ParameterDeclaration declaration ){
            base.check( stack, declaration );
        }
    }
}
