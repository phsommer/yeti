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
package tinyos.yeti.nesc12.parser.ast.visitors;

import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.ErrorASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.ArrayAbstractDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.ArrayDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.ArrayDesignator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.AttributeDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.AttributedDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DataObjectDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DataObjectFieldDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DataObjectFieldDeclarationList;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DataObjectFieldDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DataObjectFieldDeclaratorList;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DataObjectSpecifier;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.Declaration;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclarationSpecifierList;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DeclaratorName;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DesignationInitializer;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DesignatorList;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.DirectDeclaratorSpecifier;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.EnumConstant;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.EnumConstantList;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.EnumDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.EnumType;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.FieldDesignator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.FunctionAbstractDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.FunctionDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.IdentifierList;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.IncompleteDataObject;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitDeclaratorList;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InitializerList;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.InterfaceDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.MultiInitializer;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.NesCNameDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.ParameterDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.ParameterTypeList;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.ParameterizedDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.ParenthesizedAbstractDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.ParenthesizedDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.Pointer;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.PointerAbstractDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.PointerDeclarator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.PrimitiveSpecifier;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.RangeDesignator;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.SingleInitializer;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.StorageClass;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.TypeName;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.TypeQualifier;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.TypedefName;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.VariableLength;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.DeclarationList;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.FunctionDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.TranslationUnit;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.ArgumentExpressionList;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.ArithmeticExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.ArithmeticOperator;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.ArraySubscripting;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.AssignmentExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.AssignmentOperator;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.CallExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.CallKind;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.CastExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.CharacterConstant;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.CompoundLiteral;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.ConditionalExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.EnumerationConstant;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.ExpressionList;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.FieldAccess;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.FloatingConstant;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.FunctionCall;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.IdentifierExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.IntegerConstant;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.ParenthesizedExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.PointerAccess;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.PostfixExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.PostfixOperator;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.PrefixExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.SizeofExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.StatementExpression;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.StringLiteral;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.StringLiteralList;
import tinyos.yeti.nesc12.parser.ast.nodes.expression.UnaryOperator;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Access;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.AccessList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Attribute;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.AttributeList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.BinaryComponent;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ComponentList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Configuration;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ConfigurationDeclarationList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Connection;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Datadef;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.DatadefList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Endpoint;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ExtensionExternalDefinition;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.GenericArgumentList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.IdentifierParameter;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.IdentifierParameterList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Includes;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Interface;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.InterfaceParameter;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.InterfaceParameterList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.InterfaceReference;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.InterfaceType;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Module;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.NesCExternalDefinitionList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.NesCName;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.NewComponent;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ParameterizedIdentifier;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ParameterizedInterface;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.ParameterizedInterfaceList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.RefComponent;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.TemplateParameter;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.TemplateParameterList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.TypeList;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Wire;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.ASMArgument;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.ASMArgumentList;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.ASMArgumentsList;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.ASMCall;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.AtomicStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.BreakStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.CaseStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.CompoundStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.ContinueStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.DefaultStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.DoWhileStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.EmptyStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.ExpressionStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.ForStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.GotoStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.IfStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.LabeledStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.ReturnStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.SwitchStatement;
import tinyos.yeti.nesc12.parser.ast.nodes.statement.WhileStatement;

public abstract class ConvergingASTVisitor implements ASTVisitor {
    public abstract boolean convergedVisit( ASTNode node );
    
    public abstract void convergedEndVisit( ASTNode node );
    
    public void endVisit( Wire node ) {
        convergedEndVisit( node );
    }
    
    public void endVisit( ParameterizedDeclarator node ) {
        convergedEndVisit( node );
    }
    
    public void endVisit( IdentifierExpression node ) {
        convergedEndVisit( node );
    }
    
    public void endVisit( ArithmeticOperator node ) {
        convergedEndVisit( node );    
    }
    
    public void endVisit( AssignmentOperator node ) {
        convergedEndVisit( node );    
    }
    
    public void endVisit( CallKind node ) {
        convergedEndVisit( node );    
    }
    
    public void endVisit( UnaryOperator node ) {
        convergedEndVisit( node );
    }
    
    public void endVisit( PostfixOperator node ) {
        convergedEndVisit( node );
    }
    
    public void endVisit( ErrorASTNode node ) {
        convergedEndVisit( node );
    }

    public void endVisit( Identifier node ) {
        convergedEndVisit( node );
    }

    public void endVisit( Interface node ) {
        convergedEndVisit( node );
    }

    public void endVisit( InterfaceParameterList node ) {
        convergedEndVisit( node );
    }

    public void endVisit( InterfaceParameter node ) {
        convergedEndVisit( node );
    }

    public void endVisit( Module node ) {
        convergedEndVisit( node );
    }

    public void endVisit( TemplateParameterList node ) {
        convergedEndVisit( node );
    }

    public void endVisit( TemplateParameter node ) {
        convergedEndVisit( node );

    }

    public void endVisit( NesCExternalDefinitionList node ) {
        convergedEndVisit( node );

    }

    public void endVisit( ASMArgument node ){
        convergedEndVisit( node );
    }
    
    public void endVisit( ASMArgumentList node ){
        convergedEndVisit( node );
    }
    
    public void endVisit( ASMArgumentsList node ){
        convergedEndVisit( node );
    }

    public void endVisit( ExtensionExternalDefinition node ) {
        convergedEndVisit( node );

    }

    public void endVisit( Configuration node ) {
        convergedEndVisit( node );

    }

    public void endVisit( ConfigurationDeclarationList node ) {
        convergedEndVisit( node );

    }

    public void endVisit( Connection node ) {
        convergedEndVisit( node );

    }

    public void endVisit( Endpoint node ) {
        convergedEndVisit( node );

    }

    public void endVisit( ComponentList node ) {
        convergedEndVisit( node );

    }

    public void endVisit( RefComponent node ) {
        convergedEndVisit( node );

    }

    public void endVisit( NewComponent node ) {
        convergedEndVisit( node );

    }

    public void endVisit( GenericArgumentList node ) {
        convergedEndVisit( node );

    }

    public void endVisit( BinaryComponent node ) {
        convergedEndVisit( node );

    }

    public void endVisit( NesCName node ) {
        convergedEndVisit( node );

    }

    public void endVisit( ParameterizedIdentifier node ) {
        convergedEndVisit( node );

    }

    public void endVisit( IdentifierParameterList node ) {
        convergedEndVisit( node );

    }

    public void endVisit( IdentifierParameter node ) {
        convergedEndVisit( node );

    }

    public void endVisit( AttributeList node ) {
        convergedEndVisit( node );

    }

    public void endVisit( Attribute node ) {
        convergedEndVisit( node );

    }
    
    public void endVisit( DatadefList node ) {
        convergedEndVisit( node );

    }

    public void endVisit( Datadef node ) {
        convergedEndVisit( node );

    }

    public void endVisit( ASMCall node ) {
        convergedEndVisit( node );

    }

    public void endVisit( AccessList node ) {
        convergedEndVisit( node );

    }

    public void endVisit( Access node ) {
        convergedEndVisit( node );

    }

    public void endVisit( ParameterizedInterfaceList node ) {
        convergedEndVisit( node );

    }

    public void endVisit( ParameterizedInterface node ) {
        convergedEndVisit( node );

    }

    public void endVisit( InterfaceReference node ) {
        convergedEndVisit( node );

    }

    public void endVisit( InterfaceType node ) {
        convergedEndVisit( node );

    }

    public void endVisit( TypeList node ) {
        convergedEndVisit( node );

    }

    public void endVisit( EnumerationConstant node ) {
        convergedEndVisit( node );

    }

    public void endVisit( IntegerConstant node ) {
        convergedEndVisit( node );

    }

    public void endVisit( FloatingConstant node ) {
        convergedEndVisit( node );

    }

    public void endVisit( CharacterConstant node ) {
        convergedEndVisit( node );

    }

    public void endVisit( StringLiteral node ) {
        convergedEndVisit( node );

    }

    public void endVisit( StringLiteralList node ) {
        convergedEndVisit( node );

    }

    public void endVisit( ParenthesizedExpression node ) {
        convergedEndVisit( node );

    }

    public void endVisit( ArraySubscripting node ) {
        convergedEndVisit( node );

    }

    public void endVisit( FunctionCall node ) {
        convergedEndVisit( node );

    }

    public void endVisit( CallExpression node ) {
        convergedEndVisit( node );

    }

    public void endVisit( ArgumentExpressionList node ) {
        convergedEndVisit( node );

    }

    public void endVisit( FieldAccess node ) {
        convergedEndVisit( node );

    }

    public void endVisit( PointerAccess node ) {
        convergedEndVisit( node );

    }

    public void endVisit( PostfixExpression node ) {
        convergedEndVisit( node );

    }

    public void endVisit( CompoundLiteral node ) {
        convergedEndVisit( node );

    }

    public void endVisit( PrefixExpression node ) {
        convergedEndVisit( node );

    }
    
    public void endVisit( StatementExpression node ){
        convergedEndVisit( node );
    }

    public void endVisit( SizeofExpression node ) {
        convergedEndVisit( node );

    }

    public void endVisit( CastExpression node ) {
        convergedEndVisit( node );

    }

    public void endVisit( ArithmeticExpression node ) {
        convergedEndVisit( node );

    }

    public void endVisit( ConditionalExpression node ) {
        convergedEndVisit( node );

    }

    public void endVisit( AssignmentExpression node ) {
        convergedEndVisit( node );

    }

    public void endVisit( ExpressionList node ) {
        convergedEndVisit( node );

    }

    public void endVisit( Declaration node ) {
        convergedEndVisit( node );

    }

    public void endVisit( DeclarationSpecifierList node ) {
        convergedEndVisit( node );

    }

    public void endVisit( DirectDeclaratorSpecifier node ) {
        convergedEndVisit( node );

    }

    public void endVisit( InitDeclaratorList node ) {
        convergedEndVisit( node );

    }

    public void endVisit( InitDeclarator node ) {
        convergedEndVisit( node );

    }

    public void endVisit( StorageClass node ) {
        convergedEndVisit( node );

    }

    public void endVisit( PrimitiveSpecifier node ) {
        convergedEndVisit( node );

    }

    public void endVisit( DataObjectDeclaration node ) {
        convergedEndVisit( node );

    }

    public void endVisit( AttributeDeclaration node ) {
        convergedEndVisit( node );

    }

    public void endVisit( IncompleteDataObject node ) {
        convergedEndVisit( node );

    }

    public void endVisit( DataObjectSpecifier node ) {
        convergedEndVisit( node );

    }

    public void endVisit( DataObjectFieldDeclarationList node ) {
        convergedEndVisit( node );

    }

    public void endVisit( DataObjectFieldDeclaration node ) {
        convergedEndVisit( node );

    }

    public void endVisit( DataObjectFieldDeclaratorList node ) {
        convergedEndVisit( node );

    }

    public void endVisit( DataObjectFieldDeclarator node ) {
        convergedEndVisit( node );

    }

    public void endVisit( EnumDeclaration node ) {
        convergedEndVisit( node );

    }

    public void endVisit( EnumType node ) {
        convergedEndVisit( node );

    }

    public void endVisit( EnumConstantList node ) {
        convergedEndVisit( node );

    }

    public void endVisit( EnumConstant node ) {
        convergedEndVisit( node );

    }

    public void endVisit( TypeQualifier node ) {
        convergedEndVisit( node );

    }

    public void endVisit( PointerDeclarator node ) {
        convergedEndVisit( node );

    }

    public void endVisit( AttributedDeclarator node ) {
        convergedEndVisit( node );

    }

    public void endVisit( DeclaratorName node ) {
        convergedEndVisit( node );

    }

    public void endVisit( ParenthesizedDeclarator node ) {
        convergedEndVisit( node );

    }

    public void endVisit( ArrayDeclarator node ) {
        convergedEndVisit( node );

    }

    public void endVisit( VariableLength node ) {
        convergedEndVisit( node );

    }

    public void endVisit( FunctionDeclarator node ) {
        convergedEndVisit( node );

    }

    public void endVisit( NesCNameDeclarator node ) {
        convergedEndVisit( node );

    }

    public void endVisit( InterfaceDeclarator node ) {
        convergedEndVisit( node );

    }

    public void endVisit( Pointer node ) {
        convergedEndVisit( node );

    }

    public void endVisit( ParameterTypeList node ) {
        convergedEndVisit( node );

    }

    public void endVisit( ParameterDeclaration node ) {
        convergedEndVisit( node );

    }

    public void endVisit( IdentifierList node ) {
        convergedEndVisit( node );

    }

    public void endVisit( TypeName node ) {
        convergedEndVisit( node );

    }

    public void endVisit( PointerAbstractDeclarator node ) {
        convergedEndVisit( node );

    }

    public void endVisit( ParenthesizedAbstractDeclarator node ) {
        convergedEndVisit( node );

    }

    public void endVisit( ArrayAbstractDeclarator node ) {
        convergedEndVisit( node );

    }

    public void endVisit( FunctionAbstractDeclarator node ) {
        convergedEndVisit( node );

    }

    public void endVisit( TypedefName node ) {
        convergedEndVisit( node );

    }

    public void endVisit( InitializerList node ) {
        convergedEndVisit( node );

    }

    public void endVisit( SingleInitializer node ) {
        convergedEndVisit( node );

    }

    public void endVisit( MultiInitializer node ) {
        convergedEndVisit( node );

    }

    public void endVisit( DesignationInitializer node ) {
        convergedEndVisit( node );

    }

    public void endVisit( DesignatorList node ) {
        convergedEndVisit( node );

    }

    public void endVisit( ArrayDesignator node ) {
        convergedEndVisit( node );

    }
    
    public void endVisit( RangeDesignator node ){
        convergedEndVisit( node );
    }

    public void endVisit( FieldDesignator node ) {
        convergedEndVisit( node );

    }

    public void endVisit( LabeledStatement node ) {
        convergedEndVisit( node );

    }

    public void endVisit( CaseStatement node ) {
        convergedEndVisit( node );

    }

    public void endVisit( DefaultStatement node ) {
        convergedEndVisit( node );

    }

    public void endVisit( CompoundStatement node ) {
        convergedEndVisit( node );

    }

    public void endVisit( EmptyStatement node ) {
        convergedEndVisit( node );

    }

    public void endVisit( ExpressionStatement node ) {
        convergedEndVisit( node );

    }

    public void endVisit( IfStatement node ) {
        convergedEndVisit( node );

    }

    public void endVisit( SwitchStatement node ) {
        convergedEndVisit( node );

    }

    public void endVisit( WhileStatement node ) {
        convergedEndVisit( node );

    }

    public void endVisit( DoWhileStatement node ) {
        convergedEndVisit( node );

    }

    public void endVisit( ForStatement node ) {
        convergedEndVisit( node );

    }

    public void endVisit( GotoStatement node ) {
        convergedEndVisit( node );

    }

    public void endVisit( ContinueStatement node ) {
        convergedEndVisit( node );

    }

    public void endVisit( BreakStatement node ) {
        convergedEndVisit( node );

    }

    public void endVisit( ReturnStatement node ) {
        convergedEndVisit( node );

    }

    public void endVisit( AtomicStatement node ) {
        convergedEndVisit( node );

    }

    public void endVisit( TranslationUnit node ) {
        convergedEndVisit( node );
    }

    public void endVisit( FunctionDefinition node ) {
        convergedEndVisit( node );
    }

    public void endVisit( DeclarationList node ) {
        convergedEndVisit( node );
    }
    
    public void endVisit( Includes node ){
        convergedEndVisit( node );
    }
    
    public boolean visit( Wire node ) {
        return convergedVisit( node );
    }
    
    public boolean visit( IdentifierExpression node ) {
        return convergedVisit( node );
    }
    
    public boolean visit( ParameterizedDeclarator node ){
        return convergedVisit( node );
    }
    
    public boolean visit( ArithmeticOperator node ) {
        return convergedVisit( node );
    }
    
    public boolean visit( AssignmentOperator node ) {
        return convergedVisit( node );
    }
    
    public boolean visit( CallKind node ) {
        return convergedVisit( node );
    }
    
    public boolean visit( PostfixOperator node ) {
        return convergedVisit( node );
    }
    
    public boolean visit( UnaryOperator node ) {
        return convergedVisit( node );
    }

    public boolean visit( ErrorASTNode node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( Identifier node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( Interface node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( InterfaceParameterList node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( InterfaceParameter node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( Module node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( TemplateParameterList node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( TemplateParameter node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( NesCExternalDefinitionList node ) {
        return convergedVisit( node );
        
    }
    
    public boolean visit( ASMArgument node ){
        return convergedVisit( node );
    }
    
    public boolean visit( ASMArgumentList node ){
        return convergedVisit( node );
    }
    
    public boolean visit( ASMArgumentsList node ){
        return convergedVisit( node );
    }

    public boolean visit( ExtensionExternalDefinition node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( Configuration node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( ConfigurationDeclarationList node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( Connection node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( Endpoint node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( ComponentList node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( RefComponent node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( NewComponent node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( GenericArgumentList node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( BinaryComponent node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( NesCName node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( ParameterizedIdentifier node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( IdentifierParameterList node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( IdentifierParameter node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( AttributeList node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( Attribute node ) {
        return convergedVisit( node );
        
    }
    
    public boolean visit( DatadefList node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( Datadef node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( ASMCall node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( AccessList node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( Access node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( ParameterizedInterfaceList node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( ParameterizedInterface node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( InterfaceReference node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( InterfaceType node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( TypeList node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( EnumerationConstant node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( IntegerConstant node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( FloatingConstant node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( CharacterConstant node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( StringLiteral node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( StringLiteralList node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( ParenthesizedExpression node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( ArraySubscripting node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( FunctionCall node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( CallExpression node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( ArgumentExpressionList node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( FieldAccess node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( PointerAccess node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( PostfixExpression node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( CompoundLiteral node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( PrefixExpression node ) {
        return convergedVisit( node );
    }

    public boolean visit( StatementExpression node ){
        return convergedVisit( node );
    }
    
    public boolean visit( SizeofExpression node ) {
        return convergedVisit( node );
    }

    public boolean visit( CastExpression node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( ArithmeticExpression node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( ConditionalExpression node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( AssignmentExpression node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( ExpressionList node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( Declaration node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( DeclarationSpecifierList node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( DirectDeclaratorSpecifier node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( InitDeclaratorList node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( InitDeclarator node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( StorageClass node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( PrimitiveSpecifier node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( DataObjectDeclaration node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( AttributeDeclaration node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( IncompleteDataObject node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( DataObjectSpecifier node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( DataObjectFieldDeclarationList node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( DataObjectFieldDeclaration node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( DataObjectFieldDeclaratorList node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( DataObjectFieldDeclarator node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( EnumDeclaration node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( EnumType node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( EnumConstantList node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( EnumConstant node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( TypeQualifier node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( PointerDeclarator node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( AttributedDeclarator node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( DeclaratorName node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( ParenthesizedDeclarator node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( ArrayDeclarator node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( VariableLength node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( FunctionDeclarator node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( NesCNameDeclarator node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( InterfaceDeclarator node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( Pointer node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( ParameterTypeList node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( ParameterDeclaration node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( IdentifierList node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( TypeName node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( PointerAbstractDeclarator node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( ParenthesizedAbstractDeclarator node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( ArrayAbstractDeclarator node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( FunctionAbstractDeclarator node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( TypedefName node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( InitializerList node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( SingleInitializer node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( MultiInitializer node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( DesignationInitializer node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( DesignatorList node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( ArrayDesignator node ) {
        return convergedVisit( node );
        
    }
    
    public boolean visit( RangeDesignator node ){
        return convergedVisit( node );
    }

    public boolean visit( FieldDesignator node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( LabeledStatement node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( CaseStatement node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( DefaultStatement node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( CompoundStatement node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( EmptyStatement node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( ExpressionStatement node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( IfStatement node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( SwitchStatement node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( WhileStatement node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( DoWhileStatement node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( ForStatement node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( GotoStatement node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( ContinueStatement node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( BreakStatement node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( ReturnStatement node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( AtomicStatement node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( TranslationUnit node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( FunctionDefinition node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( DeclarationList node ) {
        return convergedVisit( node );
        
    }

    public boolean visit( Includes node ){
        return convergedVisit( node );
    }
}
