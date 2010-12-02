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
package tinyos.yeti.nesc12.parser.ast;

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

public interface ASTVisitor {

    public boolean visit( ErrorASTNode node );

    public void endVisit( ErrorASTNode node );

    public boolean visit( Identifier node );

    public void endVisit( Identifier node );

    public boolean visit( Includes node );
    
    public void endVisit( Includes node );
    
    public boolean visit( Interface node );

    public void endVisit( Interface node );

    public boolean visit( InterfaceParameterList node );

    public void endVisit( InterfaceParameterList node );

    public boolean visit( InterfaceParameter node );

    public void endVisit( InterfaceParameter node );

    public boolean visit( Module node );

    public void endVisit( Module node );

    public boolean visit( TemplateParameterList node );

    public void endVisit( TemplateParameterList node );

    public boolean visit( TemplateParameter node );

    public void endVisit( TemplateParameter node );

    public boolean visit( NesCExternalDefinitionList node );

    public void endVisit( NesCExternalDefinitionList node );

    public boolean visit( ExtensionExternalDefinition node );

    public void endVisit( ExtensionExternalDefinition node );

    public boolean visit( Configuration node );

    public void endVisit( Configuration node );

    public boolean visit( ConfigurationDeclarationList node );

    public void endVisit( ConfigurationDeclarationList node );

    public boolean visit( Connection node );

    public void endVisit( Connection node );
    
    public boolean visit( Wire node );
    
    public void endVisit( Wire node );

    public boolean visit( Endpoint node );

    public void endVisit( Endpoint node );

    public boolean visit( ComponentList node );

    public void endVisit( ComponentList node );

    public boolean visit( RefComponent node );

    public void endVisit( RefComponent node );

    public boolean visit( NewComponent node );

    public void endVisit( NewComponent node );

    public boolean visit( GenericArgumentList node );

    public void endVisit( GenericArgumentList node );

    public boolean visit( BinaryComponent node );

    public void endVisit( BinaryComponent node );

    public boolean visit( NesCName node );

    public void endVisit( NesCName node );

    public boolean visit( ParameterizedIdentifier node );

    public void endVisit( ParameterizedIdentifier node );

    public boolean visit( IdentifierParameterList node );

    public void endVisit( IdentifierParameterList node );

    public boolean visit( IdentifierParameter node );

    public void endVisit( IdentifierParameter node );

    public boolean visit( AttributeList node );

    public void endVisit( AttributeList node );

    public boolean visit( Attribute node );

    public void endVisit( Attribute node );

    public boolean visit( DatadefList node );

    public void endVisit( DatadefList node );

    public boolean visit( Datadef node );

    public void endVisit( Datadef node );

    public boolean visit( ASMCall node );

    public void endVisit( ASMCall node );
    
    public boolean visit( ASMArgumentsList node );
    
    public void endVisit( ASMArgumentsList node );
    
    public boolean visit( ASMArgumentList node );
    
    public void endVisit( ASMArgumentList node );
    
    public boolean visit( ASMArgument node );
    
    public void endVisit( ASMArgument node );

    public boolean visit( AccessList node );

    public void endVisit( AccessList node );

    public boolean visit( Access node );

    public void endVisit( Access node );

    public boolean visit( ParameterizedInterfaceList node );

    public void endVisit( ParameterizedInterfaceList node );

    public boolean visit( ParameterizedInterface node );

    public void endVisit( ParameterizedInterface node );

    public boolean visit( InterfaceReference node );

    public void endVisit( InterfaceReference node );

    public boolean visit( InterfaceType node );

    public void endVisit( InterfaceType node );

    public boolean visit( TypeList node );

    public void endVisit( TypeList node );

    public boolean visit( EnumerationConstant node );

    public void endVisit( EnumerationConstant node );

    public boolean visit( IntegerConstant node );

    public void endVisit( IntegerConstant node );

    public boolean visit( FloatingConstant node );

    public void endVisit( FloatingConstant node );

    public boolean visit( CharacterConstant node );

    public void endVisit( CharacterConstant node );

    public boolean visit( StringLiteral node );

    public void endVisit( StringLiteral node );

    public boolean visit( StringLiteralList node );

    public void endVisit( StringLiteralList node );
    
    public boolean visit( IdentifierExpression node );
    
    public void endVisit( IdentifierExpression node );

    public boolean visit( ParenthesizedExpression node );

    public void endVisit( ParenthesizedExpression node );

    public boolean visit( ArraySubscripting node );

    public void endVisit( ArraySubscripting node );

    public boolean visit( FunctionCall node );

    public void endVisit( FunctionCall node );

    public boolean visit( CallExpression node );

    public void endVisit( CallExpression node );
    
    public boolean visit( CallKind node );
    
    public void endVisit( CallKind node );

    public boolean visit( ArgumentExpressionList node );

    public void endVisit( ArgumentExpressionList node );

    public boolean visit( FieldAccess node );

    public void endVisit( FieldAccess node );

    public boolean visit( PointerAccess node );

    public void endVisit( PointerAccess node );

    public boolean visit( PostfixExpression node );

    public void endVisit( PostfixExpression node );
    
    public boolean visit( PostfixOperator node );
    
    public void endVisit( PostfixOperator node );

    public boolean visit( CompoundLiteral node );

    public void endVisit( CompoundLiteral node );

    public boolean visit( PrefixExpression node );

    public void endVisit( PrefixExpression node );
    
    public boolean visit( StatementExpression node );
    
    public void endVisit( StatementExpression node );
    
    public boolean visit( UnaryOperator node );
    
    public void endVisit( UnaryOperator node );

    public boolean visit( SizeofExpression node );

    public void endVisit( SizeofExpression node );

    public boolean visit( CastExpression node );

    public void endVisit( CastExpression node );

    public boolean visit( ArithmeticExpression node );

    public void endVisit( ArithmeticExpression node );
    
    public boolean visit( ArithmeticOperator node );
    
    public void endVisit( ArithmeticOperator node );

    public boolean visit( ConditionalExpression node );

    public void endVisit( ConditionalExpression node );

    public boolean visit( AssignmentExpression node );

    public void endVisit( AssignmentExpression node );
    
    public boolean visit( AssignmentOperator node );
    
    public void endVisit( AssignmentOperator node );

    public boolean visit( ExpressionList node );

    public void endVisit( ExpressionList node );

    // Declarations
    public boolean visit( Declaration node );

    public void endVisit( Declaration node );

    public boolean visit( DeclarationSpecifierList node );

    public void endVisit( DeclarationSpecifierList node );

    public boolean visit( DirectDeclaratorSpecifier node );

    public void endVisit( DirectDeclaratorSpecifier node );

    public boolean visit( InitDeclaratorList node );

    public void endVisit( InitDeclaratorList node );

    public boolean visit( InitDeclarator node );

    public void endVisit( InitDeclarator node );

    public boolean visit( StorageClass node );

    public void endVisit( StorageClass node );

    public boolean visit( PrimitiveSpecifier node );

    public void endVisit( PrimitiveSpecifier node );

    public boolean visit( DataObjectDeclaration node );

    public void endVisit( DataObjectDeclaration node );

    public boolean visit( AttributeDeclaration node );

    public void endVisit( AttributeDeclaration node );

    public boolean visit( IncompleteDataObject node );

    public void endVisit( IncompleteDataObject node );

    public boolean visit( DataObjectSpecifier node );

    public void endVisit( DataObjectSpecifier node );

    public boolean visit( DataObjectFieldDeclarationList node );

    public void endVisit( DataObjectFieldDeclarationList node );

    public boolean visit( DataObjectFieldDeclaration node );

    public void endVisit( DataObjectFieldDeclaration node );

    public boolean visit( DataObjectFieldDeclaratorList node );

    public void endVisit( DataObjectFieldDeclaratorList node );

    public boolean visit( DataObjectFieldDeclarator node );

    public void endVisit( DataObjectFieldDeclarator node );

    public boolean visit( EnumDeclaration node );

    public void endVisit( EnumDeclaration node );

    public boolean visit( EnumType node );

    public void endVisit( EnumType node );

    public boolean visit( EnumConstantList node );

    public void endVisit( EnumConstantList node );

    public boolean visit( EnumConstant node );

    public void endVisit( EnumConstant node );

    public boolean visit( TypeQualifier node );

    public void endVisit( TypeQualifier node );

    public boolean visit( PointerDeclarator node );

    public void endVisit( PointerDeclarator node );

    public boolean visit( AttributedDeclarator node );

    public void endVisit( AttributedDeclarator node );

    public boolean visit( DeclaratorName node );

    public void endVisit( DeclaratorName node );

    public boolean visit( ParenthesizedDeclarator node );

    public void endVisit( ParenthesizedDeclarator node );

    public boolean visit( ArrayDeclarator node );

    public void endVisit( ArrayDeclarator node );

    public boolean visit( VariableLength node );

    public void endVisit( VariableLength node );

    public boolean visit( FunctionDeclarator node );

    public void endVisit( FunctionDeclarator node );

    public boolean visit( NesCNameDeclarator node );

    public void endVisit( NesCNameDeclarator node );

    public boolean visit( ParameterizedDeclarator node );
    
    public void endVisit( ParameterizedDeclarator node );
    
    public boolean visit( InterfaceDeclarator node );

    public void endVisit( InterfaceDeclarator node );

    public boolean visit( Pointer node );

    public void endVisit( Pointer node );

    public boolean visit( ParameterTypeList node );

    public void endVisit( ParameterTypeList node );

    public boolean visit( ParameterDeclaration node );

    public void endVisit( ParameterDeclaration node );

    public boolean visit( IdentifierList node );

    public void endVisit( IdentifierList node );

    public boolean visit( TypeName node );

    public void endVisit( TypeName node );

    public boolean visit( PointerAbstractDeclarator node );

    public void endVisit( PointerAbstractDeclarator node );

    public boolean visit( ParenthesizedAbstractDeclarator node );

    public void endVisit( ParenthesizedAbstractDeclarator node );

    public boolean visit( ArrayAbstractDeclarator node );

    public void endVisit( ArrayAbstractDeclarator node );

    public boolean visit( FunctionAbstractDeclarator node );

    public void endVisit( FunctionAbstractDeclarator node );

    public boolean visit( TypedefName node );

    public void endVisit( TypedefName node );

    public boolean visit( InitializerList node );

    public void endVisit( InitializerList node );

    public boolean visit( SingleInitializer node );

    public void endVisit( SingleInitializer node );

    public boolean visit( MultiInitializer node );

    public void endVisit( MultiInitializer node );

    public boolean visit( DesignationInitializer node );

    public void endVisit( DesignationInitializer node );

    public boolean visit( DesignatorList node );

    public void endVisit( DesignatorList node );

    public boolean visit( ArrayDesignator node );

    public void endVisit( ArrayDesignator node );
    
    public boolean visit( RangeDesignator node );
    
    public void endVisit( RangeDesignator node );

    public boolean visit( FieldDesignator node );

    public void endVisit( FieldDesignator node );

    // Statements
    public boolean visit( LabeledStatement node );

    public void endVisit( LabeledStatement node );

    public boolean visit( CaseStatement node );

    public void endVisit( CaseStatement node );

    public boolean visit( DefaultStatement node );

    public void endVisit( DefaultStatement node );

    public boolean visit( CompoundStatement node );

    public void endVisit( CompoundStatement node );

    public boolean visit( EmptyStatement node );

    public void endVisit( EmptyStatement node );

    public boolean visit( ExpressionStatement node );

    public void endVisit( ExpressionStatement node );

    public boolean visit( IfStatement node );

    public void endVisit( IfStatement node );

    public boolean visit( SwitchStatement node );

    public void endVisit( SwitchStatement node );

    public boolean visit( WhileStatement node );

    public void endVisit( WhileStatement node );

    public boolean visit( DoWhileStatement node );

    public void endVisit( DoWhileStatement node );

    public boolean visit( ForStatement node );

    public void endVisit( ForStatement node );

    public boolean visit( GotoStatement node );

    public void endVisit( GotoStatement node );

    public boolean visit( ContinueStatement node );

    public void endVisit( ContinueStatement node );

    public boolean visit( BreakStatement node );

    public void endVisit( BreakStatement node );

    public boolean visit( ReturnStatement node );

    public void endVisit( ReturnStatement node );

    public boolean visit( AtomicStatement node );

    public void endVisit( AtomicStatement node );

    public boolean visit( TranslationUnit node );

    public void endVisit( TranslationUnit node );

    public boolean visit( FunctionDefinition node );

    public void endVisit( FunctionDefinition node );

    public boolean visit( DeclarationList node );

    public void endVisit( DeclarationList node );

}