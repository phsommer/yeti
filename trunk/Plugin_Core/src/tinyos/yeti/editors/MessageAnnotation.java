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

import org.eclipse.jface.text.source.Annotation;

import tinyos.yeti.ep.parser.IMessage;

public class MessageAnnotation extends Annotation {
    public static final String ERROR_ANNOTATION_TYPE= "tinyos.error"; //$NON-NLS-1$
    public static final String WARNING_ANNOTATION_TYPE= "tinyos.warning"; //$NON-NLS-1$
    public static final String INFO_ANNOTATION_TYPE= "tinyos.info"; //$NON-NLS-1$
    
    public MessageAnnotation( IMessage message ){
        setText( message.getMessage() );
        
        switch( message.getSeverity() ){
            case ERROR:
                setType( ERROR_ANNOTATION_TYPE );
                break;
            case WARNING:
                setType( WARNING_ANNOTATION_TYPE );
                break;
            case INFO:
            default:
                setType( INFO_ANNOTATION_TYPE );
                break;
        }
    }
}
