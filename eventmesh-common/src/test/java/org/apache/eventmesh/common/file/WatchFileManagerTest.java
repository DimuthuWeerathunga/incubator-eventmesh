/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.eventmesh.common.file;

import org.apache.eventmesh.common.utils.ThreadUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

public class WatchFileManagerTest {

    @Test
    public void testWatchFile() {
        String resourceUrl = WatchFileManagerTest.class.getResource("/configuration.properties").getFile();
        File file = new File(resourceUrl);
        final FileChangeListener fileChangeListener = new FileChangeListener() {
            @Override
            public void onChanged(FileChangeContext changeContext) {
                Assert.assertEquals(file.getName(), changeContext.getFileName());
                Assert.assertEquals(file.getParent(), changeContext.getDirectoryPath());
            }

            @Override
            public boolean support(FileChangeContext changeContext) {
                return changeContext.getWatchEvent().context().toString().contains(file.getName());
            }
        };
        WatchFileManager.registerFileChangeListener(file.getParent(), fileChangeListener);

        Path path = Paths.get(resourceUrl);
        Properties properties = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException e) {
            Assert.fail("Test failed to load from file");
        }
        properties.setProperty("eventMesh.server.newAdd", "newAdd");
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            properties.store(writer, "newAdd");
        } catch (IOException e) {
            Assert.fail("Test failed to write to file");
        }
        ThreadUtils.sleep(500, TimeUnit.MILLISECONDS);
    }
}
