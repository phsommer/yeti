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
package tinyos.yeti.ep.parser.inspection;

/**
 * Describes a preprocessed file.
 * @author Benjamin Sigg
 */
public interface ITranslationUnit extends INesCNode{
	InspectionKey<INesCComponent> COMPONENT = new InspectionKey<INesCComponent>( INesCComponent.class, "components" );
	InspectionKey<INesCModule> MODULE = new InspectionKey<INesCModule>( INesCModule.class, "components - modules" );
	InspectionKey<INesCConfiguration> CONFIGURATION = new InspectionKey<INesCConfiguration>( INesCConfiguration.class, "components - configurations" );
	InspectionKey<INesCBinaryComponent> BINARY_COMPONENT = new InspectionKey<INesCBinaryComponent>( INesCBinaryComponent.class, "components - binary components" );
	InspectionKey<INesCInterface> INTERFACE = new InspectionKey<INesCInterface>( INesCInterface.class, "interfaces" );
	InspectionKey<INesCField> FIELD = new InspectionKey<INesCField>( INesCField.class, "fields" );
	InspectionKey<INesCFunction> FUNCTION = new InspectionKey<INesCFunction>( INesCFunction.class, "functions" );
	InspectionKey<INesCTypedef> TYPEDEF = new InspectionKey<INesCTypedef>( INesCTypedef.class, "typedefs" );
}
