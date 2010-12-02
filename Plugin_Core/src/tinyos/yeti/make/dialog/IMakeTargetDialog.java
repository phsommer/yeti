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
package tinyos.yeti.make.dialog;

/**
 * An abstract view of the make target dialog for an {@link IMakeTargetDialogPage}
 * @author Benjamin Sigg
 */
public interface IMakeTargetDialog{
    public static enum Severity{ INFO, WARNING, ERROR, DESCRIPTION };
    
    public void setMessage( IMakeTargetDialogPage<?> page, String message, Severity severity );
    
    /**
     * To be called whenever some content changed on the page.
     */
    public void contentChanged();
}
