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

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import io.github.doclerk.core.exception.DoclerkException;
import io.github.doclerk.core.model.DocPart;
import io.github.doclerk.core.model.DocParts;
import io.github.doclerk.core.model.DocRoot;
import io.github.doclerk.core.expression.Expression;
import io.github.doclerk.core.expression.TemplateStringExpression;
import java.util.stream.Stream;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

public class TestPlanYamlSection implements DocParts {

    private final DocRoot root;
    private final String id;
    private final Expression<String> title;
    private final Set<DocPart> scenarios;

    public TestPlanYamlSection(DocRoot root, String id, String title, Path folder) {
        this.root = root;
        this.id = id;
        this.title = new TemplateStringExpression(title, root.getExpressionContext());
        try (Stream<Path> files = Files.list(folder)) {
            scenarios = files.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".yml"))
                    .map(p -> readFile(Utils.fileNameWithoutExtension(p, ".yml"), p))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } catch (IOException e) {
            throw new DoclerkException("Failed to list files inside folder " + folder.toString(), e);
        }
    }

    @Override
    public Set<DocPart> getParts() {
        return Collections.unmodifiableSet(scenarios);
    }

    @Override
    public String getTitle() {
        return title.evaluate();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public DocParts getParent() {
        return root;
    }

    @Override
    public DocRoot getRoot() {
        return root;
    }

    private TestPlanYamlScenario readFile(String scenarioId, Path file) {
        LoadSettings settings = LoadSettings.builder().build();
        Load load = new Load(settings);
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            @SuppressWarnings("unchecked")
            Map<String, ?> scenario = (Map<String, ?>) load.loadFromReader(reader);
            return new TestPlanYamlScenario(this, scenarioId, scenario);
        } catch (IOException ex) {
            throw new DoclerkException("Failed to read file " + file.toString(), ex);
        }
    }

}
