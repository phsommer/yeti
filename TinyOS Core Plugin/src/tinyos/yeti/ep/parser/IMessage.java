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
package tinyos.yeti.ep.parser;

import java.util.Map;

import org.eclipse.core.resources.IMarker;

import tinyos.yeti.ep.IParseFile;

/**
 * An {@link IMessage} represents a single message, like a warning or an error.
 * @author Benjamin Sigg
 */
public interface IMessage {
    public enum Severity{
        ERROR, WARNING, INFO
    }
    
    /**
     * Gets the importance of this message
     * @return the importance, not <code>null</code>
     */
    public Severity getSeverity();
    
    /**
     * Gets describing text for this message.
     * @return the description
     */
    public String getMessage();
    
    /**
     * Gets a string that represents the message-text of this {@link IMessage}
     * as unique key. The message key should only depend on the text, if the text
     * contains information that depends other {@link IMessage}s
     * (like the number of messages in the document), then this information has 
     * to be filtered out.
     * @return the key
     */
    public String getMessageKey();
    
    /**
     * Gets the file that was parsed when this message was created.
     * @return the file that was parsed, not necessary the file in which the
     * error occurred
     */
    public IParseFile getParseFile();
    
    /**
     * Gets all the locations where this message appears.
     * @return the regions, most often only one region. This array can be
     * <code>null</code> but must not contain <code>null</code> entries
     */
    public IFileRegion[] getRegions();
    
    /**
     * Gets a map of additional information for the quickfixer which might
     * be used to work on this message. The map will be used for an {@link IMarker},
     * so don't use the keys of {@link IMarker} for the map. The map may
     * contain {@link String}, {@link Integer}, {@link Boolean} or <code>null</code>
     * as value.
     * @return the map or <code>null</code>
     */
    public Map<String, Object> getQuickfixInfos();
}
