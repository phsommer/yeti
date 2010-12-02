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
package tinyos.yeti.preprocessor.parser.elements;

import java.util.List;

import tinyos.yeti.preprocessor.lexer.Macro;
import tinyos.yeti.preprocessor.lexer.PreprocessorToken;
import tinyos.yeti.preprocessor.lexer.Symbols;
import tinyos.yeti.preprocessor.parser.ElementVisitor;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;
import tinyos.yeti.preprocessor.parser.stream.SkipableElementStream;
import tinyos.yeti.preprocessor.parser.stream.SkipableElementStream.SkipCondition;

public class Define extends PreprocessorElement implements Macro{
    private Identifier identifier;
    private IdentifierList parameters;
    private TokenSequence tokens;
    private VarArg vararg = VarArg.NO;
    
    private boolean inProgress = false;

    public Define( PreprocessorToken token, Identifier identifier ){
        super( token );
        this.identifier = identifier;
    }
    
    public Define( PreprocessorToken token, Identifier identifier, IdentifierList parameters, TokenSequence tokens ){
        super( token );
        this.identifier = identifier;
        this.parameters = parameters;
        this.tokens = tokens;
    }
    
    public boolean validSubstitution( Define define ) {
        if( define.getVarArg() != getVarArg() )
            return false;
        
        List<Identifier> defParameters = define.parameters();
        List<Identifier> parameters = parameters();
        int defParamCount = defParameters == null ? -1 : defParameters.size();
        int paramCount = parameters == null ? -1 : parameters.size();
        
        if( defParamCount != paramCount )
            return false;
        
        for( int i = 0; i < paramCount; i++ ){
            String defName = defParameters.get( i ).name();
            String name = parameters.get( i ).name();
            if( !name.equals( defName ))
                return false;
        }
        
        // ok, now only the token sequence must be the same
        SkipableElementStream defStream = new SkipableElementStream( define.tokens(), true );
        SkipableElementStream stream = new SkipableElementStream( tokens(), true );
        
        SkipCondition noToken = SkipableElementStream.missingText();
        
        while( true ){
            defStream.skip( noToken );
            stream.skip( noToken );
            if( !defStream.hasNext() || !stream.hasNext() )
                break;
            
            PreprocessorToken defToken = defStream.next().getToken();
            PreprocessorToken token = stream.next().getToken();
            
            boolean defSpace = defToken.getKind() == Symbols.WHITESPACE;
            boolean space = token.getKind() == Symbols.WHITESPACE;
            
            if( defSpace && space ){
                defStream.skipWhitespaces();
                stream.skipWhitespaces();
            }
            else{
                if( defToken.getText() == null ){
                    if( token.getText() != null && token.getText().length() > 0 )
                        return false;
                }
                else if( !defToken.getText().equals( token.getText() ))
                    return false;
            }
        }
        
        if( defStream.hasNext() ){
            defStream.skipWhitespaces();
            if( defStream.hasNext() )
                return false;
        }
        
        if( stream.hasNext() ){
            stream.skipWhitespaces();
            if( stream.hasNext() )
                return false;
        }
        
        return true;
    }
    
    public PreprocessorElement getLocation() {
        return identifier;
    }
    
    public void setInProgress( boolean inProgress ) {
        this.inProgress = inProgress;
    }
    
    public boolean isInProgress() {
        return inProgress;
    }
    
    public void setVarArg( VarArg vararg ) {
        this.vararg = vararg;
    }
    
    public VarArg getVarArg() {
        return vararg;
    }
    
    public String getName() {
        return identifier();
    }
    
    public String[] getParameters() {
        if( parameters == null )
            return null;
        
        List<Identifier> identifiers = parameters.identifiers();
        String[] ids = new String[ identifiers.size() ];
        int i = 0;
        for( Identifier id : identifiers )
            ids[i++] = id.name();
        
        return ids;
    }
    
    public PreprocessorElement getTokenSequence( PreprocessorElement element ) {
        return tokens();
    }
    
    @Override
    public PreprocessorElement[] getChildren() {
        int count = 0;
        if( identifier != null )
            count++;
        if( parameters != null )
            count++;
        if( tokens != null )
            count++;
        
        PreprocessorElement[] result = new PreprocessorElement[ count ];
        count = 0;
        if( identifier != null )
            result[ count++ ] = identifier;
        if( parameters != null )
            result[ count++ ] = parameters;
        if( tokens != null )
            result[ count ] = tokens;
        return result;
    }
    
    @Override
    public int getChildrenCount() {
        return 3;
    }
    
    @Override
    public PreprocessorElement getChild( int index ) {
        switch( index ){
            case 0: return identifier;
            case 1: return parameters;
            case 2: return tokens;
        }
        return null;
    }
    
    @Override
    public void visit( ElementVisitor visitor ) {
        visitor.visit( this );
        if( identifier != null )
            identifier.visit( visitor );
        if( parameters != null )
            parameters.visit( visitor );
        if( tokens != null )
            tokens.visit( visitor );
        visitor.endVisit( this );
    }
    
    @Override
    public void visitReverse( ElementVisitor visitor ) {
        visitor.visit( this );
        if( tokens != null )
            tokens.visit( visitor );
        if( parameters != null )
            parameters.visit( visitor );
        if( identifier != null )
            identifier.visit( visitor );
        visitor.endVisit( this );
    }
    
    public String identifier(){
        return identifier.name();
    }
    
    public List<Identifier> parameters(){
        if( parameters == null ){
            return null;
        }
        return parameters.identifiers();
    }
    
    public void setParameters( IdentifierList parameters ) {
        this.parameters = parameters;
    }
    
    public TokenSequence tokens(){
        return tokens;
    }

    public void setTokens( TokenSequence tokens ) {
        this.tokens = tokens;
    }
    
    @Override
    protected void toString( StringBuilder builder, int tabs ) {
        toString( builder, tabs, "define", "vararg: " + vararg, identifier, parameters, tokens );
    }
    
    public boolean requireInformNext(){
        return false;
    }
    
    public void informNext( String[] arguments ){
        // ignore
    }
}
