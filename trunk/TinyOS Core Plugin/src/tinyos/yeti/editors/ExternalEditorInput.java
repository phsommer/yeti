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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PlatformUI;

/**
 * An EditorInput for an external file.
 */
public class ExternalEditorInput implements IStorageEditorInput {

    private IStorage externalFile;
    private IProject project = null;

    /**
     * Two ExternalEditorInputs are equal if their IStorage's are equal.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ExternalEditorInput))
            return false;
        ExternalEditorInput other = (ExternalEditorInput) obj;
        return externalFile.equals(other.externalFile);
    }

    public IProject getProject() {
        return project;
    }

    public IStorage getExternalFile(){
        return externalFile;
    }

    /*
     * @see IEditorInput#exists()
     */
    public boolean exists() {
        // External file can not be deleted
        return true;
    }

    /*
     * @see IAdaptable#getAdapter(Class)
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        return null;
    }

    /*
     * @see IEditorInput#getContentType()
     */
    public String getContentType() {
        return externalFile.getFullPath().getFileExtension();
    }

    /*
     * @see IEditorInput#getFullPath()
     */
    public String getFullPath() {
        return externalFile.getFullPath().toString();
    }

    /*
     * @see IEditorInput#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
        return registry.getImageDescriptor(externalFile.getFullPath().getFileExtension());
    }

    /*
     * @see IEditorInput#getName()
     */
    public String getName() {
        return externalFile.getName();
    }

    /*
     * @see IEditorInput#getPersistable()
     */
    public IPersistableElement getPersistable() {
        return null;
    }

    /*
     * see IStorageEditorInput#getStorage()
     */
    public IStorage getStorage() {
        return externalFile;
    }

    /*
     * @see IEditorInput#getToolTipText()
     */
    public String getToolTipText() {
        return externalFile.getFullPath().toString();
    }

    public ExternalEditorInput(IStorage exFile) {
        externalFile = exFile;
        project = null;
    }

    public ExternalEditorInput(IStorage exFile, IProject project2) {
        externalFile = exFile;
        project = project2;
    }
}
