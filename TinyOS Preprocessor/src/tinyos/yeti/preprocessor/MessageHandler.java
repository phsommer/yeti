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
package tinyos.yeti.preprocessor;

import tinyos.yeti.preprocessor.output.Insight;
import tinyos.yeti.preprocessor.parser.PreprocessorElement;

/**
 * A {@link MessageHandler} is used by the {@link Preprocessor} to report
 * errors, warnings and messages.
 * @author Benjamin Sigg
 */
public interface MessageHandler {
    public static enum Severity{
        ERROR, WARNING, MESSAGE
    }
    
    /**
     * Called when the preprocessor wants to report a message. Please note that
     * the {@link PreprocessorElement#getOutputLength() length} and 
     * {@link PreprocessorElement#getOutputOffset() offset} of the
     * <code>elements</code> are not yet calculated.
     * @param severity the importance of the message
     * @param message the message to report, might be <code>null</code> in rare cases
     * @param information additional information about the message, this information
     * is intended to be useful for tools which do automatic error correction, it
     * can't be read by humans
     * @param elements the elements which are affected by the message.
     */
    public void handle( Severity severity, String message, Insight information, PreprocessorElement...elements );
}
