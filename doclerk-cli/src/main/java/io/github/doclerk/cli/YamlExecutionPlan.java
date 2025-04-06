/*
 * Copyright 2025 piercemar.
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
package io.github.doclerk.cli;

import io.github.doclerk.core.exec.SimpleModuleExecution;
import io.github.doclerk.core.exec.DoclerkExecutionPlan;
import io.github.doclerk.core.exec.DoclerkExecution;
import io.github.doclerk.core.exec.DoclerkModuleExecution;
import io.github.doclerk.core.exec.DoclerkModuleParameters;
import io.github.doclerk.core.exec.SimpleExecution;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import java.util.List;

public class YamlExecutionPlan implements DoclerkExecutionPlan {

    private final List<DoclerkExecution> executions = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public YamlExecutionPlan(Reader reader) {
        LoadSettings settings = LoadSettings.builder().build();
        Load load = new Load(settings);
        // Blunt unsafe parsing :(
        // TODO: check types correctly or find a better way to use SnakeYAML
        final Map<String, Object> root = (Map<String, Object>) load.loadFromReader(reader);
        final List<Map<String, Object>> plan = (List<Map<String, Object>>) root.get("plan");
        for (Map<String, Object> planExec : plan) {
            final List<DoclerkModuleExecution> modExecs = new LinkedList<>();
            final List<Map<String, Object>> modules = (List<Map<String, Object>>) planExec.get("modules");
            if (modules != null) {
                for (Map<String, Object> modExec : modules) {
                    modExecs.add(
                            new SimpleModuleExecution(
                                    modExec.get("module").toString(),
                                    new DoclerkModuleParameters((Map<String, String>) modExec.get("params"))
                            )
                    );
                }
            }
            executions.add(new SimpleExecution(planExec.get("id").toString(), modExecs));
        }
    }

    @Override
    public List<DoclerkExecution> getExecutions() {
        return Collections.unmodifiableList(executions);
    }

}
