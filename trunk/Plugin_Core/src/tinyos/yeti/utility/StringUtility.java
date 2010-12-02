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
package tinyos.yeti.utility;

public class StringUtility {
	
	public static String replaceLast(String string, String toReplace , String replacement) {
		int i = string.lastIndexOf(toReplace);
		if (i == -1) {
			return string;
		} else {
			return (string.substring(0,i)
					+replacement 
					+string.substring(i+toReplace.length()));
		}
	}
	
	public static String replaceAll(String string, String toReplace , String replacement) {
		int index;
		while ((index = string.indexOf(toReplace)) != -1){
			string = string.substring(0,index)+ 
					 replacement +
					 string.substring(index+toReplace.length());
		}
		return string;
	}


	
	public static void main(String[] args) {
		System.out.println(
				StringUtility.replaceAll("123�451123�111","123�","lo")
		);
	}

}
