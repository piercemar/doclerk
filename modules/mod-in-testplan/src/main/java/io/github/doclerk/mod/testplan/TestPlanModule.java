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
package io.github.doclerk.mod.testplan;

import io.github.doclerk.core.model.DocRoot;
import io.github.doclerk.core.module.AbstractModule;
import io.github.doclerk.core.module.DoclerkModuleInfo;
import io.github.doclerk.core.module.HashMapModuleContext;
import io.github.doclerk.core.module.ModuleContext;
import io.github.doclerk.core.module.ModuleContextKeys;
import io.github.doclerk.core.module.SimpleDoclerkModuleInfo;
import java.net.URI;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class TestPlanModule extends AbstractModule<TestPlanModule> {

    private static final SimpleDoclerkModuleInfo MODULE_INFO = new SimpleDoclerkModuleInfo("TestPlan", "dev");
    private static final String CONTEXT_SCOPE_TESTPLAN = "testplan.";
    private static final String CONTEXT_KEY_ROOT_YML = CONTEXT_SCOPE_TESTPLAN + "root";
    private static final String DEFAULT_INPUT_URI = ".";
    private static final String DEFAULT_ROOT_YML = "root.yml";

    @Override
    public DocRoot run(DocRoot root) {
        return new TestPlanYamlDocRoot(Paths.get(getSourceUri()));
    }

    @Override
    public DoclerkModuleInfo moduleInfo() {
        return MODULE_INFO;
    }

    @Override
    protected ModuleContext defaultContext() {
        Map<String, String> defaultContext = new HashMap<>(1);
        return new HashMapModuleContext(defaultContext);
    }

    private URI getSourceUri() {
        traceContext(ModuleContextKeys.WORK_DIR_URI);
        traceContext(CONTEXT_KEY_ROOT_YML);
        final URI computedUri = context.getValue(ModuleContextKeys.WORK_DIR_URI)
                .map(v -> v.endsWith("/") ? v : v + "/")
                .map(URI::create)
                .orElse(Paths.get(DEFAULT_INPUT_URI).toUri())
                .resolve(context.getValue(CONTEXT_KEY_ROOT_YML).orElse(DEFAULT_ROOT_YML))
                .normalize();
        logger.debug("Computed input URI: {}", computedUri);
        return computedUri;
    }

}
