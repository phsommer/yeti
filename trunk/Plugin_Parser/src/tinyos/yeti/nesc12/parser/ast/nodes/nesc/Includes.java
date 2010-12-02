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
package tinyos.yeti.nesc12.parser.ast.nodes.nesc;

import tinyos.yeti.nesc12.lexer.Token;
import tinyos.yeti.nesc12.parser.ast.ASTException;
import tinyos.yeti.nesc12.parser.ast.ASTVisitor;
import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.AbstractFixedASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.declaration.IdentifierList;
import tinyos.yeti.nesc12.parser.ast.nodes.definition.ExternalDeclaration;
import tinyos.yeti.nesc12.parser.ast.nodes.general.Identifier;
import tinyos.yeti.preprocessor.IncludeFile;
import tinyos.yeti.preprocessor.IncludeProvider;

public class Includes extends AbstractFixedASTNode implements ExternalDeclaration{
    private Token keyword;
    
    public static final String FILES = "files";
    
    public Includes(){
        super( "Includes", FILES );
    }
    
    public Includes( Token keyword, IdentifierList files ){
        this();
        setKeyword( keyword );
        setFiles( files );
    }
    
    public Includes( Token keyword, ASTNode files ){
        this();
        setKeyword( keyword );
        setField( FILES, files );
    }
    
    public void setKeyword( Token keyword ){
        this.keyword = keyword;
        if( keyword == null )
            setLeft( -1 );
        else
            setLeft( keyword.getLeft() );
    }
    
    public Token getKeyword(){
        return keyword;
    }
    
    public void setFiles( IdentifierList files ){
        setField( 0, files );
    }
    
    public IdentifierList getFiles(){
        return (IdentifierList)getNoError( 0 );
    }
    
    @Override
    public void resolve( AnalyzeStack stack ){
        super.resolve( stack );
        stack.checkCancellation();
        
        if( stack.isReportErrors() ){
            stack.warning( "the directive 'includes' is deprecated, it should be replaced by '#include'", keyword );
            
            // search files
            IdentifierList files = getFiles();
            if( files != null ){
                for( int i = 0, n = files.getChildrenCount(); i<n; i++ ){
                    Identifier file = files.getTypedChild( i );
                    if( file != null ){
                        checkFile( file, stack );
                    }
                }
            }
        }
    }
    
    private void checkFile( Identifier filename, AnalyzeStack stack ){
        IncludeProvider include = stack.getIncludeProvider();
        if( include == null ){
            stack.error( "can't find file '" + filename.getName() + "'", filename );
            return;
        }
        
        String name = filename.getName();
        
        if( !name.endsWith( ".h" ))
            name += ".h";
        
        IncludeFile file = include.searchUserFile( name, stack.requestProgressMonitor() );
        if( file == null ){
            stack.error( "can't find file '" + name + "'", filename );
        }
    }
    
    @Override
    protected void checkField( int index, ASTNode node ) throws ASTException{
        if( !( node instanceof IdentifierList ) )
            throw new ASTException( node, "Must be an IdentifierList" );
    }

    @Override
    protected void endVisit( ASTVisitor visitor ){
        visitor.endVisit( this );
    }

    @Override
    protected boolean visit( ASTVisitor visitor ){
        return visitor.visit( this );
    }
    
}
