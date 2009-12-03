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
package tinyos.yeti.nesc12.parser.ast.elements.types.conversion;

import tinyos.yeti.nesc12.parser.ast.ASTMessageHandler;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.ASTMessageHandler.Severity;
import tinyos.yeti.nesc12.parser.ast.elements.LazyRangeDescription;
import tinyos.yeti.nesc12.parser.ast.elements.Type;
import tinyos.yeti.nesc12.parser.ast.elements.Value;
import tinyos.yeti.nesc12.parser.ast.elements.Type.Label;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;

/**
 * Helper class used to check the implicit or explicit conversion of types.
 * @author Benjamin Sigg
 */
public class ConversionMap{
    private static final int FLAG_IMPLICIT      = 1;
    private static final int FLAG_EXPLICIT      = 2;
    private static final int FLAG_ASSIGNMENT    = 4;

    public static ConversionMap cast( AnalyzeStack stack, ASTNode location, Value value ){
        return new ConversionMap( stack, stack.range( location ), FLAG_EXPLICIT, value );
    }

    public static ConversionMap assignment( AnalyzeStack stack, ASTNode location, Value value ){
        return new ConversionMap( stack, stack.range( location ), FLAG_IMPLICIT | FLAG_ASSIGNMENT, value );
    }

    public static ConversionMap assignment( AnalyzeStack stack, LazyRangeDescription range, Value value ){
        return new ConversionMap( stack, range, FLAG_IMPLICIT | FLAG_ASSIGNMENT, value );
    }

    private int pointer = 0;
    private AnalyzeStack stack;
    private LazyRangeDescription location;
    private int flag;
    private Value value;

    private ConversionMap( AnalyzeStack stack, LazyRangeDescription location, int flag, Value value ){
        this.stack = stack;
        this.location = location;
        this.flag = flag;
        this.value = value;
    }

    public boolean isExplicit(){
        return (flag & FLAG_EXPLICIT) == FLAG_EXPLICIT;
    }

    public boolean isImplicit(){
        return (flag & FLAG_IMPLICIT) == FLAG_IMPLICIT;
    }

    /**
     * Tells whether a value of the converted type will be assigned to a variable.
     * @return <code>true</code> if an assignment is involved
     */
    public boolean isAssignment(){
        return (flag & FLAG_ASSIGNMENT) == FLAG_ASSIGNMENT;   
    }

    public Value getConvertedValue(){
        if( isPointer() )
            return null;

        return value;
    }

    public void pushPointer(){
        pointer++;
    }

    public void popPointer(){
        pointer--;
    }

    public boolean isPointer(){
        return pointer > 0;
    }

    public void reportError( String message, Type source, Type destination ){
        switch( getErrorSeverity() ){
            case ERROR:
                stack.error( toMessage( message, source, destination ), location.getRange() );
                break;
            case WARNING:
                stack.warning( toMessage( message, source, destination ), location.getRange() );
                break;
            case MESSAGE:
                stack.message( toMessage( message, source, destination ), location.getRange() );
                break;    
        }
    }

    public void reportWarning( String message, Type source, Type destination ){
        switch( getWarningSeverity() ){
            case WARNING:
                stack.warning( toMessage( message, source, destination ), location.getRange() );
                break;
            case MESSAGE:
                stack.message( toMessage( message, source, destination ), location.getRange() );
                break;
        }
    }

    public void reportMessage( String message, Type source, Type destination ){
        stack.message( toMessage( message, source, destination ), location.getRange() );
    }
    
    private String toMessage( String message, Type source, Type destination ){
    	if( source == null && destination == null )
    		return message;
    	if( source == null )
    		return message + ": to '" + destination.toLabel( null, Label.SMALL ) + "'";
    	if( destination == null )
    		return message + ": from '" + source.toLabel( null, Label.SMALL ) + "'";
    	
    	return message + ": from '" + source.toLabel( null, Label.SMALL ) + "' to '" + destination.toLabel( null, Label.SMALL ) + "'";
    }

    public ASTMessageHandler.Severity getErrorSeverity(){
        if( isPointer() ){
            if( isExplicit() ){
                return Severity.MESSAGE;
            }
            else{
                return Severity.WARNING;
            }
        }
        else{
            return Severity.ERROR;
        }
    }


    public ASTMessageHandler.Severity getWarningSeverity(){
        if( isPointer() && isExplicit() ){
            return Severity.MESSAGE;
        }
        else{
            return Severity.WARNING;
        }
    }
}
