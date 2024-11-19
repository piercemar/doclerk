/*
 * Copyright 2024 piercemar.
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
package io.github.doclerk.core.module;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import io.github.doclerk.core.model.DocRoot;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface DoclerkModule {

    static final Logger doclerkModuleLogger = LogManager.getLogger(DoclerkModule.class);

    <T extends DoclerkModule> T withContext(ModuleContext ctx);

    DoclerkModuleInfo moduleInfo();

    DocRoot run(DocRoot docRoot);
    
    static Set<DoclerkModule> findAll() {
        Set<DoclerkModule> modules = new HashSet<>(16);
        try (ScanResult scanResult
                = new ClassGraph()
                        .enableClassInfo()
                        .scan()) {
                    for (ClassInfo moduleClassInfo : scanResult.getClassesImplementing(DoclerkModule.class)) {
                        Class<DoclerkModule> moduleClass = moduleClassInfo.loadClass(DoclerkModule.class);
                        if ((moduleClass.getModifiers() & Modifier.ABSTRACT) == 0) {
                            try {
                                modules.add(moduleClass.getDeclaredConstructor().newInstance());
                            } catch (InstantiationException
                                    | IllegalAccessException
                                    | NoSuchMethodException
                                    | SecurityException
                                    | IllegalArgumentException
                                    | InvocationTargetException ex) {
                                doclerkModuleLogger.error("Failed to instantiate module class {}", moduleClass.getName(), ex);
                            }
                        }
                    }
                }
                return modules;
    }
}
