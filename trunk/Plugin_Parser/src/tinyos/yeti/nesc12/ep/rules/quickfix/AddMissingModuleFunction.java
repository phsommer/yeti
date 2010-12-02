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
package tinyos.yeti.nesc12.ep.rules.quickfix;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.swt.graphics.Image;

import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.nesc12.ep.NesC12AST;
import tinyos.yeti.nesc12.ep.rules.RuleUtility;
import tinyos.yeti.nesc12.parser.ParserInsights;
import tinyos.yeti.nesc12.parser.ast.elements.Field;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterface;
import tinyos.yeti.nesc12.parser.ast.elements.NesCInterfaceReference;
import tinyos.yeti.nesc12.parser.ast.elements.NesCModule;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.Module;
import tinyos.yeti.nesc12.parser.ast.nodes.nesc.NesCExternalDefinitionList;
import tinyos.yeti.preprocessor.output.Insight;

public class AddMissingModuleFunction implements ISingleQuickfixRule, IMultiQuickfixRule{
    public void suggest( Insight error, QuickfixCollector collector ){
        if( error.getId() == ParserInsights.MODULE_MISSING_FUNCTION ){
            boolean event = error.get( ParserInsights.MODULE_MISSING_FUNCTION_IS_EVENT_BOOLEAN, false );
            boolean command = !event;
            
            collector.addSingle( new Quickfix( 
                    error.get( ParserInsights.MODULE_MISSING_FUNCTION_INTERFACE_NAME_STRING, null ),
                    error.get( ParserInsights.MODULE_MISSING_FUNCTION_NAME_STRING, null ),
                    event, command ) );
        }
    }
    
    public void suggest( Insight[] errors, QuickfixCollector collector ){
        String interfaceName = null;
        
        int count = 0;
        boolean event = false;
        boolean command = false;
        
        for( Insight error : errors ){
            if( error.getId() == ParserInsights.MODULE_MISSING_FUNCTION ){
                if( interfaceName == null ){
                    interfaceName = error.get( ParserInsights.MODULE_MISSING_FUNCTION_INTERFACE_NAME_STRING, null ); 
                }
                
                count++;
                event = event || error.get( ParserInsights.MODULE_MISSING_FUNCTION_IS_EVENT_BOOLEAN, false );
                command = command || !error.get( ParserInsights.MODULE_MISSING_FUNCTION_IS_EVENT_BOOLEAN, true );
            }
        }
        
        if( count >= 2 ){
            collector.addMulti( new Quickfix( interfaceName, null, event, command ) );
        }
    }

    private class Quickfix implements ISingleQuickfix, IMultiQuickfix{
        private String interfaceName;
        private String name;
        private boolean event;
        private boolean command;

        public Quickfix( String interfaceName, String name, boolean event, boolean command ){
            this.interfaceName = interfaceName;
            this.name = name;
            this.event = event;
            this.command = command;
        }

        public String getDescription(){
            return null;
        }

        public Image getImage(){
            if( event && !command)
                return NesCIcons.icons().get( NesCIcons.ICON_EVENT );
            else if( command && !event )
                return NesCIcons.icons().get( NesCIcons.ICON_COMMAND );
            else
                return NesCIcons.icons().get( NesCIcons.ICON_CFUNCTION );
        }

        public String getLabel(){
            if( name != null ){
                if( interfaceName == null )
                    return "Add '" + name + "'";
                else
                    return "Add '" + interfaceName + "." + name + "'";
            }
            else{
                if( interfaceName == null )
                    return "Add all missing functions";
                else
                    return "Add all missing functions of '" + interfaceName + "'";
            }
        }

        public void run( Insight error, QuickfixInformation information ){
            try{
                if( interfaceName != null ){
                    NesC12AST ast = information.getAst();
                    ASTNode node = ParserInsights.location( error.get( ParserInsights.MODULE_MISSING_FUNCTION_NODE_PATH_STRING, null ), ast.getRoot() );
                    if( !( node instanceof Module ))
                        return;

                    Module module = (Module)node;

                    int next = getInsertionPosition( module, information );
                    if( next < 0 )
                        return;

                    String whitespaces = RuleUtility.whitespaceLineBegin( next, information.getDocument() );
                    CharSequence result = run( error, name, whitespaces, module, information );

                    if( result != null ){
                        information.replace( next, 0, result.toString() );
                    }
                }
            }
            catch( BadLocationException ex ){
                ex.printStackTrace();
            }
        }

        public void run( Insight[] errors, QuickfixInformation information ){
            try{
                if( interfaceName != null ){
                    NesC12AST ast = information.getAst();
                    ASTNode node = ParserInsights.location( errors[0].get( ParserInsights.MODULE_MISSING_FUNCTION_NODE_PATH_STRING, null ), ast.getRoot() );
                    if( !( node instanceof Module ))
                        return;

                    Module module = (Module)node;

                    int next = getInsertionPosition( module, information );
                    if( next < 0 )
                        return;

                    String whitespaces = RuleUtility.whitespaceLineBegin( next, information.getDocument() );
                    StringBuilder builder = new StringBuilder();
                    for( Insight error : errors ){
                        CharSequence result = run( error, error.get( ParserInsights.MODULE_MISSING_FUNCTION_NAME_STRING, null ),
                                whitespaces, module, information );
                        if( result != null ){
                            builder.append( result );
                        }
                    }

                    information.replace( next, 0, builder.toString() );
                }
            }
            catch( BadLocationException ex ){
                ex.printStackTrace();
            }
        }

        protected int getInsertionPosition( Module module, QuickfixInformation information ) throws BadLocationException{
            NesCExternalDefinitionList implementation = module.getImplementation();
            if( implementation == null )
                return -1;

            int next = information.getAst().getOffsetAtBegin( implementation ).getInputfileOffset();
            next = RuleUtility.nextBlock( next, information.getDocument() );
            if( next < 0 )
                return -1;
            next = RuleUtility.blockEnd( next+1, information.getDocument() );
            if( next < 0 )
                return -1;

            return next;
        }

        protected CharSequence run( Insight error, String name, String whitespaces, Module module, QuickfixInformation information ) throws BadLocationException{
            NesCModule binding = module.resolveNode().resolve( information.getAst().getBindingResolver() );
            if( binding == null )
                return null;

            NesCInterfaceReference reference;
            if( event )
                reference = binding.getUses( interfaceName );
            else
                reference = binding.getProvides( interfaceName );

            if( reference == null )
                return null;

            NesCInterface interfaze = reference.getParameterizedReference();
            if( interfaze == null )
                return null;

            Field field = interfaze.getField( name );
            if( field == null )
                return null;


            String tab = RuleUtility.getTab();

            StringBuilder insert = new StringBuilder();
            insert.append( "\n" );
            insert.append( whitespaces );
            insert.append( tab );
            insert.append( field.getDeclaration( interfaceName + "." + name ) );
            insert.append( "{\n" );
            insert.append( whitespaces );
            insert.append( tab );
            insert.append( tab );
            insert.append( "// TODO Auto-generated method stub" );
            insert.append( "\n" );
            insert.append( whitespaces );
            insert.append( tab );
            insert.append( "}\n" );
            
            return insert;
        }
    }
}
