/*
 * Copyright 2012 Adam Murdoch
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.rubygrapefruit.platform.file;

import net.rubygrapefruit.platform.ThreadSafe;

import java.io.File;

/**
 * Information about a file system. This is a snapshot view and does not change.
 */
@ThreadSafe
public interface FileSystemInfo {
    /**
     * Returns the root directory of this file system.
     */
    @ThreadSafe
    File getMountPoint();

    /**
     * Returns the operating system specific name for the type of this file system.
     */
    @ThreadSafe
    String getFileSystemType();

    /**
     * Returns true if additional details are known for the file system.
     * <p>
     * You must not query additional details if they are unknown.
     * <p>
     * Additional details are:
     * <ul>
     *     <li>{@link #isCasePreserving()}</li>
     *     <li>{@link #isCaseSensitive()}</li>
     * </ul>
     */
    @ThreadSafe
    boolean isDetailsKnown();

    /**
     * Returns true if this file system is a remote file system, or false if local.
     */
    @ThreadSafe
    boolean isRemote();

    /**
     * Returns true if this file system is performance case sensitive searches.
     */
    @ThreadSafe
    boolean isCaseSensitive();

    /**
     * Returns true if this file system preserves file name case.
     */
    @ThreadSafe
    boolean isCasePreserving();

    /**
     * Returns the operating system specific name for this file system.
     */
    @ThreadSafe
    String getDeviceName();
}
