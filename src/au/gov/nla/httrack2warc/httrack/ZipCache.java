/*
 * Copyright (c) 2017 National Library of Australia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package au.gov.nla.httrack2warc.httrack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class ZipCache implements Cache {
    private final ZipFile zipFile;

    public ZipCache(Path zipPath) throws IOException {
        this.zipFile = new ZipFile(zipPath.toFile());
    }

    @Override
    public void close() throws IOException {
        zipFile.close();
    }

    @Override
    public CacheEntry getEntry(String url) {
        return new Entry(zipFile.getEntry(url));
    }

    private class Entry implements CacheEntry {
        private final ZipEntry entry;

        Entry(ZipEntry entry) {
            this.entry = entry;
        }

        @Override
        public long getSize() {
            return entry.getSize();
        }

        @Override
        public InputStream openStream() throws IOException {
            return zipFile.getInputStream(entry);
        }

        @Override
        public boolean hasData() {
            return getSize() > 0;
        }
    }
}