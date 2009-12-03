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

public interface INesCComponent extends INesCNode{
	InspectionKey<INesCInterfaceReference> USED_INTERFACE = new InspectionKey<INesCInterfaceReference>( INesCInterfaceReference.class, "used" );
	InspectionKey<INesCInterfaceReference> PROVIDED_INTERFACE = new InspectionKey<INesCInterfaceReference>( INesCInterfaceReference.class, "provided" );
	
	InspectionKey<INesCFunction> USED_FUNCTION = new InspectionKey<INesCFunction>( INesCFunction.class, "used" );
	InspectionKey<INesCFunction> PROVIDED_FUNCTION = new InspectionKey<INesCFunction>( INesCFunction.class, "provided" );

	/**
	 * The name of this component as written in the source code.
	 * @return the name
	 */
	public String getName();
}
