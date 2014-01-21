/**
 * Copyright (c) 2002-2014 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
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
 */
package org.neo4j.kernel;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.neo4j.kernel.impl.nioneo.store.FileLock;
import org.neo4j.kernel.impl.nioneo.store.FileSystemAbstraction;

public class CannedFileSystemAbstraction implements FileSystemAbstraction
{
    private final boolean fileExists;
    private final IOException cannotCreateStoreDir;
    private final IOException cannotOpenLockFile;
    private final boolean lockSuccess;

    public CannedFileSystemAbstraction( boolean fileExists,
                                        IOException cannotCreateStoreDir,
                                        IOException cannotOpenLockFile,
                                        boolean lockSuccess )
    {
        this.fileExists = fileExists;
        this.cannotCreateStoreDir = cannotCreateStoreDir;
        this.cannotOpenLockFile = cannotOpenLockFile;
        this.lockSuccess = lockSuccess;
    }

    @Override
    public FileChannel open( String fileName, String mode ) throws IOException
    {
        if ( cannotOpenLockFile != null )
        {
            throw cannotOpenLockFile;
        }

        return null;
    }

    @Override
    public FileLock tryLock( String fileName, FileChannel channel ) throws IOException
    {
        return lockSuccess ? SYMBOLIC_FILE_LOCK : null;
    }

    @Override
    public FileChannel create( String fileName ) throws IOException
    {
        throw new UnsupportedOperationException( "TODO" );
    }

    @Override
    public boolean fileExists( String fileName )
    {
        return fileExists;
    }

    @Override
    public long getFileSize( String fileName )
    {
        throw new UnsupportedOperationException( "TODO" );
    }

    @Override
    public boolean deleteFile( String fileName )
    {
        throw new UnsupportedOperationException( "TODO" );
    }

    @Override
    public boolean renameFile( String from, String to ) throws IOException
    {
        throw new UnsupportedOperationException( "TODO" );
    }

    @Override
    public void copyFile( String from, String to ) throws IOException
    {
        throw new UnsupportedOperationException( "TODO" );
    }

    @Override
    public void autoCreatePath( File path ) throws IOException
    {
        if ( cannotCreateStoreDir != null )
        {
            throw cannotCreateStoreDir;
        }
    }

    private static final FileLock SYMBOLIC_FILE_LOCK = new FileLock()
    {
        @Override
        public void release() throws IOException
        {

        }
    };
}