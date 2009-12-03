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
package tinyos.yeti.nesc12.ep.nodes;

import tinyos.yeti.ep.parser.Tag;
import tinyos.yeti.ep.parser.standard.ASTModelNode;
import tinyos.yeti.ep.storage.TagSetFactory;
import tinyos.yeti.nesc12.ep.NesC12ASTModel;

/**
 * A helper class used to store information about {@link ASTModelNode}s.
 * @author Benjamin Sigg
 */
public final class NesC12TagSetFactory extends TagSetFactory{
    public NesC12TagSetFactory(){
        setTags( new Tag[]{
                Tag.AST_CONNECTION_ICON_RESOLVE,
                Tag.AST_CONNECTION_LABEL_RESOLVE,
                Tag.AST_CONNECTION_GRAPH_ICON_RESOLVE,
                Tag.AST_CONNECTION_GRAPH_LABEL_RESOLVE,
                Tag.ASYNC,
                Tag.ATTRIBUTE,
                Tag.BINARY_COMPONENT,
                Tag.COMMAND,
                Tag.COMPONENT,
                Tag.CONFIGURATION,
                Tag.CONNECTION,
                Tag.CONNECTION_BOTH,
                Tag.CONNECTION_LEFT,
                Tag.CONNECTION_RIGHT,
                Tag.DATA_OBJECT,
                Tag.EVENT,
                Tag.FIGURE,
                Tag.FUNCTION,
                Tag.INCLUDED,
                Tag.INTERFACE,
                Tag.MODULE,
                Tag.OUTLINE,
                Tag.PROVIDES,
                Tag.RENAMED,
                Tag.STRUCT,
                Tag.TASK,
                Tag.UNION,
                Tag.USES,
                Tag.NO_BASE_EXPANSION,
                Tag.MACRO,
                Tag.IDENTIFIABLE,

                NesC12ASTModel.ENUMERATION,
                NesC12ASTModel.ENUMERATION_CONSTANT,
                NesC12ASTModel.ERROR,
                NesC12ASTModel.FIELD,
                NesC12ASTModel.PARAMETERS,
                NesC12ASTModel.TYPE,
                NesC12ASTModel.TYPEDEF,
                NesC12ASTModel.UNIT,
                NesC12ASTModel.WARNING,
                NesC12ASTModel.COMPONENTS,
                NesC12ASTModel.CONNECTIONS,
                NesC12ASTModel.IMPLEMENTATION,
                NesC12ASTModel.SPECIFICATION,
                NesC12ASTModel.GENERIC,
                NesC12ASTModel.COMPLETE_FUNCTION, 
                NesC12ASTModel.MODULE_IMPLEMENTATION,
                NesC12ASTModel.CONFIGURATION_IMPLEMENTATION,
                NesC12ASTModel.INCLUDES,
            }
        );
    }
}
