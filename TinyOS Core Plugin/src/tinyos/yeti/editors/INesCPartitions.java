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
package tinyos.yeti.editors;

import org.eclipse.jface.text.IDocument;

public interface INesCPartitions {

	/**
	 * The identifier of the Nesc partitioning.
	 */
	String NESC_PARTITIONING= "___Nesc_partitioning";
	
	String PREPROCESSOR_DIRECTIVE = "__Nesc_preprocessor_directive";
	
	/**
	 * The identifier single-line partition type
	 */
	String NESC_SINGLE_LINE_COMMENT = "__Nesc_singleComment";
	
	/**
	 * The identifier multi-line  comment partition
	 * content type.
	 */
	String MULTI_LINE_COMMENT = "__Nesc_multiComment";
	
	/**
	 * The identifier of the NesCDoc partition content type.
	 */
	String NESC_DOC = "__Nesc_nescDoc";
	
	/**
	 * The identifier for string paration content type
	 */
	String NESC_STRING = "__Nesc_string";
	
	String DEFAULT = IDocument.DEFAULT_CONTENT_TYPE;
	
	/**
	 * Convenience Array with all partition types included
	 */
	String[] PARTITION_TYPES = { 
			DEFAULT,
			PREPROCESSOR_DIRECTIVE,
			NESC_DOC, 
			MULTI_LINE_COMMENT, 
			NESC_SINGLE_LINE_COMMENT,
			NESC_STRING };



}
