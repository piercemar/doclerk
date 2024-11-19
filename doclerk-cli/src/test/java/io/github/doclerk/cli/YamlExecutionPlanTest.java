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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.doclerk.core.exec.DoclerkExecution;
import io.github.doclerk.core.exec.DoclerkModuleExecution;
import java.io.StringReader;
import java.util.List;
import org.junit.jupiter.api.Test;

public class YamlExecutionPlanTest {

    public YamlExecutionPlanTest() {
    }

    /**
     * Test of getExecutions method, of class YamlExecutionPlan.
     */
    @Test
    public void testCtor() {
        StringReader reader = new StringReader(
                "plan:\n"
                + "- id: 1\n"
                + "  modules:\n"
                + "    - module: TestModule\n"
                + "      params:\n"
                + "        param1: val1\n"
                + "        param2: val2\n"
                + "    - module: TestModule\n"
                + "      params:\n"
                + "        param1: val3\n"
                + "        param2: val4\n"
        );
        YamlExecutionPlan plan = new YamlExecutionPlan(reader);
        assertNotNull(plan);
        final List<DoclerkExecution> executions = plan.getExecutions();
        assertNotNull(executions);
        assertEquals(1, executions.size());
        final DoclerkExecution exec = executions.get(0);
        assertEquals("1", exec.getId());
        final List<DoclerkModuleExecution> moduleExecutions = exec.getModuleExecutions();
        assertNotNull(moduleExecutions);
        assertEquals(2, moduleExecutions.size());
        assertEquals("TestModule", moduleExecutions.get(0).getModuleName());
        assertEquals("val1", moduleExecutions.get(0).getParameters().get("param1"));
        assertEquals("val2", moduleExecutions.get(0).getParameters().get("param2"));
        assertEquals("TestModule", moduleExecutions.get(1).getModuleName());
        assertEquals("val3", moduleExecutions.get(1).getParameters().get("param1"));
        assertEquals("val4", moduleExecutions.get(1).getParameters().get("param2"));
        
    }

}
