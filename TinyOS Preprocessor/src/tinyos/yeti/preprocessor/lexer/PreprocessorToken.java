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
package tinyos.yeti.preprocessor.lexer;

import tinyos.yeti.preprocessor.FileInfo;

/**
 * A {@link PreprocessorToken} is created by the {@link Lexer},
 * it represents some token of the source code 
 * (for example a keyword...)
 * @author Benjamin Sigg
 *
 */
public class PreprocessorToken {
    
    private Symbols kind;
    private FileInfo file;
    private int line;
    private int[] begin;
    private int[] end;
    private String text;
    
    private InclusionPath path;
    
    public PreprocessorToken( Symbols kind, String text, InclusionPath path ){
        this( kind, null, -1, null, null, text, path );
    }
    
    public PreprocessorToken( Symbols kind, FileInfo file, int line, int[] begin, int[] end, String text, InclusionPath path ){
        this.text = text;
        this.kind = kind;
        this.line = line;
        this.file = file;
        this.begin = begin;
        this.end = end;
        this.path = path;
    }
    
    @Override
    public String toString() {
        return kind.name() + "[" + text + "]";
    }
    
    public int getLine() {
        return line;
    }
    
    public boolean hasLocation(){
        return begin != null && end != null;
    }
    
    public int[] getBegin() {
        return begin;
    }
    
    public int getBeginLocation(){
        return begin[0];
    }
    
    public int[] getEnd() {
        return end;
    }
    
    public int getEndLocation(){
        return end[ end.length-1 ];
    }
    
    public Symbols getKind() {
        return kind;
    }
    
    public FileInfo getFile() {
        return file;
    }
    
    public String getText() {
        return text;
    }
    
    public InclusionPath getPath() {
        return path;
    }
}
