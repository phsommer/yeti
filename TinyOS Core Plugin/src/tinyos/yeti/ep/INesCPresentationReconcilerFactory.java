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
package tinyos.yeti.ep;

import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * This factory creates the token scanner for NesC documents. Currently a NesC
 * document gets partitioned into these content types:
 * <ul>
 *   <li>NesC Doc</li>
 *   <li>Single line comments</li>
 *   <li>Multi line comments</li>
 *   <li>Preprocessor directives</li>
 *   <li>Strings (not in directives)</li>
 *   <li>default (=the whole rest)</li>
 * </ul>
 * Each of this content types gets its own scanner.
 * @author Benjamin Sigg
 */
public interface INesCPresentationReconcilerFactory {
	/**
	 * Creates the new token scanner.
	 * @param sourceViewer the viewer for which the scanner will be used
	 * @param defaults access to default scanners for various content types
	 * and to the names of the content types.
	 * @return a new reconciler
	 */
    public IPresentationReconciler create( ISourceViewer sourceViewer, INesCPresentationReconcilerDefaults defaults );
}
