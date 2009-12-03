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
package tinyos.yeti.nesc12.parser.preprocessor.comment;

import tinyos.yeti.nesc12.parser.ast.AnalyzeStack;
import tinyos.yeti.nesc12.parser.ast.nodes.ASTNode;
import tinyos.yeti.preprocessor.FileInfo;

public class NesCDocComment{
	private int offsetInFile;
	private FileInfo file;
	private String comment;
	private boolean topLevel;
	private DocAnalysis analysis;
	
	public NesCDocComment( int offsetInFile, FileInfo file, String comment, boolean topLevel ){
		this.offsetInFile = offsetInFile;
		this.file = file;
		this.comment = comment;
		this.topLevel = topLevel;
		this.analysis = new DocAnalysis( comment, offsetInFile );
	}
	
	public void resolve( ASTNode owner, AnalyzeStack stack ){
		analysis.resolve( owner, stack );
	}
	
	public DocAnalysis getAnalysis(){
		return analysis;
	}
	
	public IDocTag[] getTags(){
		return analysis.getTags();
	}
	
	public int getOffsetInFile(){
		return offsetInFile;
	}
	
	public FileInfo getFile(){
		return file;
	}
	
	public String getComment(){
		return comment;
	}
	
	public boolean isTopLevel(){
		return topLevel;
	}
	
	@Override
	public String toString(){
		return comment;
	}
}
