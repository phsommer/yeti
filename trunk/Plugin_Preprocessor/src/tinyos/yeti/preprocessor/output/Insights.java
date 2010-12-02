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
package tinyos.yeti.preprocessor.output;



/**
 * A set of id for different messages. The integer constants of this class
 * are used in {@link Insight} for {@link Insight#setId(int)},
 * the {@link String} constants are keys for attributes of <code>Insight</code>.
 * @author Benjamin Sigg
 */
public abstract class Insights{
    public static final int UNKNOWN = -1;
    
    public static final int    INCLUDE_NESTING_TO_DEEP = 0;
    public static final String INCLUDE_NESTING_TO_DEEP_SIZE_INT = "insight.intd.size";
    public static Insight includeNestingToDeep( int size ){
        return Insight.base( INCLUDE_NESTING_TO_DEEP ).put( INCLUDE_NESTING_TO_DEEP_SIZE_INT, size );
    }
    
    public static final int     ELSE_WITHOUT_BEGIN = 1;
    public static Insight elseWithoutBegin(){
        return Insight.base( ELSE_WITHOUT_BEGIN );
    }
    
    public static final int     ENDIF_WITHOUT_BEGIN = 2;
    public static Insight endifWithoutBegin(){
        return Insight.base( ENDIF_WITHOUT_BEGIN );
    }
    
    public static final int     MACRO_REDEFINED = 3;
    public static final String  MACRO_REDEFINED_NAME_STRING = "insight.mr.name";
    public static Insight macroRedefined( String name ){
        return Insight.base( MACRO_REDEFINED ).put( MACRO_REDEFINED_NAME_STRING, name );
    }
    
    public static final int     MACRO_WRONG_NUMBER_OF_ARGUMENTS = 4;
    public static final String  MACRO_WRONG_NUMBER_OF_ARGUMENTS_NAME_STRING = "insight.mwnoa.name";
    public static final String  MACRO_WRONG_NUMBER_OF_ARGUMENTS_FOUND_INT = "insight.mwnoa.found";
    public static final String  MACRO_WRONG_NUMBER_OF_ARGUMENTS_EXPECTED_INT = "insight.mwnoa.expected";
    public static final String  MACRO_WRONG_NUMBER_OF_ARGUMENTS_VARARG_BOOLEAN = "insight.mwnoa.vararg";
    public static Insight macroWrongNumberOfArguments( String name, int found, int expected, boolean vararg ){
        return Insight.base( MACRO_WRONG_NUMBER_OF_ARGUMENTS )
            .put( MACRO_WRONG_NUMBER_OF_ARGUMENTS_NAME_STRING, name )
            .put( MACRO_WRONG_NUMBER_OF_ARGUMENTS_FOUND_INT, found )
            .put( MACRO_WRONG_NUMBER_OF_ARGUMENTS_EXPECTED_INT, expected )
            .put( MACRO_WRONG_NUMBER_OF_ARGUMENTS_VARARG_BOOLEAN, vararg );
    }
    
    public static final int     EXPRESSION_EVALUATION_FAILED = 5;
    public static Insight expressionEvaluationFailed(){
        return Insight.base( EXPRESSION_EVALUATION_FAILED );
    }
    
    public static final int     EXPRESSION_DIVISION_BY_ZERO = 6;
    public static Insight expressionDivisionByZero(){
        return Insight.base( EXPRESSION_DIVISION_BY_ZERO );
    }
    
    public static final int     EXPRESSION_IDENTIFIER_REPLACED_BY_ZERO = 7;
    public static Insight expressionIdentifierReplacedByZero(){
        return Insight.base( EXPRESSION_IDENTIFIER_REPLACED_BY_ZERO );
    }
    
    public static final int     EXPRESSION_INVALID_CHARACTER = 8;
    public static final String  EXPRESSION_INVALID_CHARACTER_TEXT_STRING = "insight.eic.string";
    public static Insight expressionInvalidCharacter( String text ){
        return Insight.base( EXPRESSION_INVALID_CHARACTER )
            .put( EXPRESSION_INVALID_CHARACTER_TEXT_STRING, text );
    }
    
    public static final int     EXPRESSION_INVALID = 9;
    public static Insight expressionInvalid(){
        return Insight.base( EXPRESSION_INVALID );
    }
    
    public static final int     EXPRESSION_EMPTY_BRACKETS = 10;
    public static Insight expressionEmptyBrackets(){
        return Insight.base( EXPRESSION_EMPTY_BRACKETS );
    }
    
    public static final int     DIRECTIVE_ERROR = 11;
    public static Insight directiveError(){
        return Insight.base( DIRECTIVE_ERROR );
    }
    
    public static final int     DIRECTIVE_WARNING = 11;
    public static Insight directiveWarning(){
        return Insight.base( DIRECTIVE_ERROR );
    }
    
    public static final int     DIRECTIVE_PRAGMA_IGNORED = 12;
    public static Insight directivePragmaIgnored(){
        return Insight.base( DIRECTIVE_PRAGMA_IGNORED );
    }
    
    public static final int     DIRECTIVE_IFDEF_MISSING_IDENTIFIER_ARGUMENT = 13;
    public static Insight directiveIfdefMissingIdentifierArgument(){
        return Insight.base( DIRECTIVE_IFDEF_MISSING_IDENTIFIER_ARGUMENT );
    }
    
    public static final int     DIRECTIVE_IFNDEF_MISSING_IDENTIFIER_ARGUMENT = 14;
    public static Insight directiveIfndefMissingIdentifierArgument(){
        return Insight.base( DIRECTIVE_IFNDEF_MISSING_IDENTIFIER_ARGUMENT );
    }
    
    public static final int     DIRECTIVE_ELSE_WITH_ARGUMENT = 15;
    public static Insight directiveElseWithArgument(){
        return Insight.base( DIRECTIVE_ELSE_WITH_ARGUMENT );
    }
    
    public static final int     DIRECTIVE_UNDEF_MISSING_IDENTIFIER_ARGUMENT = 16;
    public static Insight directiveUndefMissingIdentifierArgument(){
        return Insight.base( DIRECTIVE_UNDEF_MISSING_IDENTIFIER_ARGUMENT );
    }
    
    public static final int     UNKNOWN_SYNTAX_ERROR = 17;
    public static Insight unknownSyntaxError(){
        return Insight.base( UNKNOWN_SYNTAX_ERROR );
    }
    
    public static final int     DIRECTIVE_INCLUDE_INVALID_ARGUMENT = 18;
    public static Insight directiveIncludeInvalidArgument(){
        return Insight.base( DIRECTIVE_INCLUDE_INVALID_ARGUMENT );
    }
    
    public static final int     DIRECTIVE_INCLUDE_MISSING_FILE = 19;
    public static final String  DIRECTIVE_INCLUDE_MISSING_FILE_FILENAME_STRING = "insight.dimf.filename";
    public static final String  DIRECTIVE_INCLUDE_MISSING_FILE_SYSTEMFILE_BOOLEAN = "insight.dimf.systemfile";
    public static Insight directiveIncludeMissingFile( String filename, boolean system ){
        return Insight.base( DIRECTIVE_INCLUDE_MISSING_FILE )
            .put( DIRECTIVE_INCLUDE_MISSING_FILE_FILENAME_STRING, filename )
            .put( DIRECTIVE_INCLUDE_MISSING_FILE_SYSTEMFILE_BOOLEAN, system );
    }
    
    public static final int     DIRECTIVE_LINE = 20;
    public static Insight directiveLine(){
        return Insight.base( DIRECTIVE_LINE );
    }
    
    public static final int     UNKNOWN_DIRECTIVE = 21;
    public static final String  UNKNOWN_DIRECTIVE_NAME_STRING = "ud.name";
    public static Insight unknownDirective( String name ){
        return Insight.base( UNKNOWN_DIRECTIVE )
            .put( UNKNOWN_DIRECTIVE_NAME_STRING, name );
    }
    
    public static final int     DIRECTIVE_ENDIF_WITH_ARGUMENT = 22;
    public static Insight directiveEndifWithArgument(){
        return Insight.base( DIRECTIVE_ENDIF_WITH_ARGUMENT );
    }
    
    public static final int		BAD_CHARACTER = 23;
    public static final String	BAD_CHARACTER_VALUE = "insight.bc.character";
    public static Insight badCharacter( String character ){
    	return Insight.base( BAD_CHARACTER ).put( BAD_CHARACTER_VALUE, character );
    }
    
    private Insights(){
        // nothing
    }
}
