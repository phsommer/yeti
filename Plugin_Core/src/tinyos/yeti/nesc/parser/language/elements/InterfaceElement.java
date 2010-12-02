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
package tinyos.yeti.nesc.parser.language.elements;

import java.util.ArrayList;

import tinyos.yeti.ProjectTOS;
import tinyos.yeti.editors.NesCIcons;
import tinyos.yeti.ep.IParseFile;
import tinyos.yeti.ep.figures.InterfaceContent;
import tinyos.yeti.ep.figures.LabelContent;
import tinyos.yeti.ep.parser.IASTFigureContent;
import tinyos.yeti.ep.parser.IFileRegion;
import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.TagSet;
import tinyos.yeti.ep.parser.standard.ASTModel;
import tinyos.yeti.ep.parser.standard.ASTModelNode;
import tinyos.yeti.ep.parser.standard.FileRegion;
import tinyos.yeti.model.ProjectModel;
import tinyos.yeti.nesc.FunctionASTModelNode;
import tinyos.yeti.nesc.parser.language.SemanticError;
import tinyos.yeti.nesc.scanner.ITokenInfo;
import tinyos.yeti.nesc.scanner.Token;
import tinyos.yeti.utility.Icon;

public class InterfaceElement extends Element {

    public InterfaceElement(Token token) {
        super(token);
        image = NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_INTERFACE);
    }

    public InterfaceElement(String string, Token t1, Token t2) {
        super(string, t1,t2);
        image = NesCIcons.icons().getImageDescriptor(NesCIcons.ICON_INTERFACE);
    }

    @Override
    public void toNode( ASTModelNode parent, ProjectModel project, ASTModel model, IParseFile file ){
        IFileRegion[] regions = new IFileRegion[]{ new FileRegion( getPositionForOutline(), getLine(), file ) };
        ASTModelNode node = new ASTModelNode( parent, getName(), getName(), getName(), file, regions, TagSet.get( Tag.INTERFACE, Tag.OUTLINE, Tag.FIGURE ) );
        if( !model.addNode( node ) )
            return;

        DeclarationElement[] commandElements = getCommands();
        IASTFigureContent[] commandContent = new IASTFigureContent[ commandElements.length ];
        int index = 0;
        
        for( DeclarationElement command : commandElements ){
            TagSet commandSet = TagSet.get( Tag.FUNCTION, Tag.COMMAND );
            if( command.uses() )
                commandSet.add( Tag.USES );
            if( command.provides() )
                commandSet.add( Tag.PROVIDES );

            regions = new IFileRegion[]{ new FileRegion( command.getPositionForOutline(), getLine(), file ) };
            String label = command.getLabel( command );
            node.addChild( command.getFunctionName(), label, regions, commandSet );
            FunctionASTModelNode function = new FunctionASTModelNode( node, command.getFunctionName(), label, file, regions, command, commandSet );
            model.addNode( function );
            commandContent[index++] = new LabelContent( label, new Icon( commandSet ), function.getPath() );
        }

        DeclarationElement[] eventElements = getEvents();
        IASTFigureContent[] eventContent = new IASTFigureContent[ eventElements.length ];
        index = 0;
        
        for( DeclarationElement event : eventElements ){
            TagSet eventSet = TagSet.get( Tag.FUNCTION, Tag.EVENT );
            if( event.uses() )
                eventSet.add( Tag.USES );
            if( event.provides() )
                eventSet.add( Tag.PROVIDES );

            regions = new IFileRegion[]{ new FileRegion( event.getPositionForOutline(), getLine(), file ) };
            String label = event.getLabel( event );
            node.addChild( event.getFunctionName(), label, regions, eventSet );
            FunctionASTModelNode function = new FunctionASTModelNode( node, event.getFunctionName(), label, file, regions, event, eventSet );
            model.addNode( function );
            eventContent[index++] = new LabelContent( label, new Icon( eventSet ), function.getPath() );
        }
        
        node.setContent( new InterfaceContent( commandContent, eventContent ) );
    }

    @SuppressWarnings("unchecked")
    public DeclarationElement[] getCommands() {
        ArrayList commands = new ArrayList();
        for (int i = 0; i < children.size(); i++) {
            Object o = children.get(i);
            if (o instanceof DeclarationElement) {
                DeclarationElement de = (DeclarationElement) o;
                if (de.command) commands.add(o);
            }
        }
        return (DeclarationElement[]) commands.toArray(new DeclarationElement[commands.size()]);
    }

    @SuppressWarnings("unchecked")
    public DeclarationElement[] getEvents() {
        ArrayList event = new ArrayList();
        for (int i = 0; i < children.size(); i++) {
            Object o = children.get(i);
            if (o instanceof DeclarationElement) {
                DeclarationElement de = (DeclarationElement) o;
                if (de.event) event.add(o);
            }
        }
        return (DeclarationElement[]) event.toArray(new DeclarationElement[event.size()]);
    }

    @SuppressWarnings("unchecked")
    public SemanticError[] getSemanticErrors( ProjectTOS project ) {
        ArrayList errors = new ArrayList();
        // interfaces only have function-declarations with
        // command or event storage classes..
        for (int i = 0; i < children.size(); i++) {
            Object o = children.get(i);
            if (o instanceof DeclarationElement) {
                DeclarationElement de = (DeclarationElement) o;
                if ((de.command==false)&&(de.event==false)) {

                    ITokenInfo it = (ITokenInfo) de.storageClassSpecifierElements.get("task");
                    SemanticError se = null;
                    if (it != null) {
                        se= new SemanticError("Only commands and " +
                                "events can be defined in interfaces",it);
                    } else {
                        se= new SemanticError("Only commands and " +
                                "events can be defined in interfaces",de);

                    }

                    se.expected= new String[]{"COMMAND","EVENT"};
                    errors.add(se);
                }

            }

        }

        if (errors.size() > 0) {
            return (SemanticError[]) errors.toArray(new SemanticError[errors.size()]);
        } 
        return null;
    }

    public boolean isFoldable() {
        return true;
    }

}
