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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

/**
 *
 * @see IStorage
 */
public class FileStorage extends PlatformObject implements IStorage {
    private boolean forceReadOnly;
    private final IPath path;
    private final File file;

    private class StreamUtil {
        public void transferStreams(InputStream source, OutputStream destination) throws IOException {
            try {
                byte[] buffer = new byte[8192];
                while (true) {
                    int bytesRead = source.read(buffer);
                    if (bytesRead == -1)
                        break;
                    destination.write(buffer, 0, bytesRead);
                }
            } finally {
                try {
                    source.close();
                } catch (IOException e) {
                }
                try {
                    destination.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Two FileStorages are equal if their IPaths are equal.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof FileStorage))
            return false;
        FileStorage other = (FileStorage) obj;
        return path.equals(other.path);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.resources.IStorage#getContents()
     */
    public InputStream getContents() throws CoreException {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            // throw new CoreException(new Status(IStatus.ERROR, PHPeclipsePlugin.PLUGIN_ID, IStatus.ERROR, e.toString(), e));
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.resources.IStorage#getFullPath()
     */
    public IPath getFullPath() {
        return this.path;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.resources.IStorage#getName()
     */
    public String getName() {
        return this.path.lastSegment();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.resources.IStorage#isReadOnly()
     */
    public boolean isReadOnly() {
        return forceReadOnly || !file.canWrite();
    }

    /**
     * Method FileStorage.
     * @param path
     */
    public FileStorage(IPath path) {
        this.path = path;
        this.file = path.toFile();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return path.toOSString();
    }

    /**
     * @param stream
     * @param overwrite
     * @param b
     * @param monitor
     */
    public void setContents(InputStream stream, boolean overwrite, boolean b, IProgressMonitor monitor) throws CoreException {
        try {
            StreamUtil util = new StreamUtil();
            util.transferStreams(stream, new FileOutputStream(file));
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
    }

    /**
     * Some document providers (notably CompilationUnitDocumentProvider)
     * can't handle read/write storage.
     */
    public void setReadOnly() {
        forceReadOnly = true;
    }


}