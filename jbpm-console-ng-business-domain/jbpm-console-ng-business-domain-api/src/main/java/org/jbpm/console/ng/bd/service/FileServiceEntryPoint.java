/*
 * Copyright 2013 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.console.ng.bd.service;

import org.jboss.errai.bus.server.annotations.Remote;
import org.kie.commons.java.nio.file.Path;

/**
 *
 * @author salaboy
 */
@Remote
public interface FileServiceEntryPoint {
    
    String createProcessDefinitionFile(String name);
    
    void fetchChanges();

    byte[] loadFile(Path file);

    Iterable<Path> loadFilesByType(String path, String fileType); 
    
    org.uberfire.backend.vfs.Path getPath(String path);
}
