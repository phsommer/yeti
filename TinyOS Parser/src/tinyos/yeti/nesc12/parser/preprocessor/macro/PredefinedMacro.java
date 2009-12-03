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
package tinyos.yeti.nesc12.parser.preprocessor.macro;

import tinyos.yeti.ep.parser.IMacro;
import tinyos.yeti.preprocessor.lexer.Macro;
import tinyos.yeti.preprocessor.lexer.macro.GenericMacro;

/**
 * A wrapper around an {@link IMacro} creating a {@link Macro} for the
 * preprocessor.
 * @author Benjamin Sigg
 */
public class PredefinedMacro extends GenericMacro{
    private IMacro macro;
    
    public static Macro instance( IMacro macro ){
    	if( macro instanceof Macro )
    		return (Macro)macro;
    	
    	return new PredefinedMacro( macro );
    }
    
    private PredefinedMacro( IMacro macro ){
        super( macro.getName(), parameters( macro ), vararg( macro ), "" );
        this.macro = macro;
    }
    
    private static String[] parameters( IMacro macro ){
        if( !macro.isFunctionMacro() )
            return null;
        
        int count = macro.getArgumentCount();
        String[] result = new String[ count ];
        for( int i = 0; i<count; i++ )
            result[i] = "arg" + count;
        return result;
    }
    
    private static VarArg vararg( IMacro macro ){
        if( macro.isVararg() )
            return VarArg.YES_UNNAMED;
        else
            return VarArg.NO;
    }
    
    @Override
    public boolean requireInformNext(){
        return true;
    }
    
    @Override
    public void informNext( String[] arguments ){
        if( arguments.length < macro.getArgumentCount() || (arguments.length > macro.getArgumentCount() && !macro.isVararg() )){
            setTokenSequence( macro.getName() );
        }
        else{
            String result = macro.run( arguments );
            setTokenSequence( result );
        }
    }
}
