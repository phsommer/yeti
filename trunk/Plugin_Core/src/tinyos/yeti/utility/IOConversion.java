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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class IOConversion {

	static String lineSep = System.getProperty("line.separator");
	 
	public static String getStringFromStream(InputStream m) throws IOException {
	  
	   BufferedReader br = new BufferedReader(new InputStreamReader(m));
	   String nextLine = "";
	   StringBuffer sb = new StringBuffer();
	   while ((nextLine = br.readLine()) != null) {
	     sb.append(nextLine);
	     sb.append(lineSep);
	   }
	   return sb.toString();
	}

	public static String getStringFromStream(Reader reader){
		StringBuffer sb = new StringBuffer();
		try {
		BufferedReader in = new BufferedReader(reader);
		String nextLine = "";
		
		
		while ((nextLine = in.readLine()) != null) {
		     sb.append(nextLine);
		     sb.append(lineSep);
		   }
		} catch (Exception e) {
			
		}
		return sb.toString();
	}
}
