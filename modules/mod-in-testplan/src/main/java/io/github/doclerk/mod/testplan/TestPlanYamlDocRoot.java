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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import io.github.doclerk.core.exception.DoclerkException;
import io.github.doclerk.core.expression.Expression;
import io.github.doclerk.core.expression.FunctionPrototype;
import io.github.doclerk.core.expression.MapContext;
import io.github.doclerk.core.expression.TemplateStringExpression;
import io.github.doclerk.core.model.DocPart;
import io.github.doclerk.core.model.DocParts;
import io.github.doclerk.core.model.DocRoot;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

public class TestPlanYamlDocRoot implements DocRoot {

    private String id;
    private String title;
    private Set<DocPart> sections;
    private Map<String, Expression<?>> properties;
    private Map<String, FunctionPrototype<?>> macros;
    private final MapContext rootContext;

    public TestPlanYamlDocRoot(Path input) {
        try (Reader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            LoadSettings settings = LoadSettings.builder().build();
            Load load = new Load(settings);
            @SuppressWarnings("unchecked")
            Map<String, Object> root = (Map<String, Object>) load.loadFromReader(reader);
            if (root == null) {
                throw new DoclerkException("Invalid Yaml file: " + input.toString());
            }

            @SuppressWarnings("unchecked")
            final Map<String, String> propMap = (Map<String, String>) root.getOrDefault("properties", Collections.emptyMap());
            properties = readProperties(propMap);

            @SuppressWarnings("unchecked")
            final List<String> macroList = (List<String>) root.getOrDefault("macros", Collections.emptyList());
            Set<Path> macroFolders = new LinkedHashSet<>(macroList.size() + 1);
            macroFolders.add(input.getParent().resolve("macro"));
            macroList.forEach(folder -> macroFolders.add(input.getParent().resolve(folder).normalize().toAbsolutePath()));
            macros = readMacros(macroFolders);
            rootContext = new MapContext(null, properties, macros);

            title = (String) root.get("title");

            @SuppressWarnings("unchecked")
            final Map<String, ?> sectionMap = (Map<String, ?>) root.get("sections");
            sections = readSections(sectionMap, input);

        } catch (IOException e) {
            throw new DoclerkException("Failed to read Yaml root", e);
        }
    }

    @Override
    public Set<DocPart> getParts() {
        return sections;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public DocParts getParent() {
        return null;
    }

    @Override
    public DocRoot getRoot() {
        return this;
    }

    @Override
    public MapContext getExpressionContext() {
        return rootContext;
    }

    protected TestPlanYamlSection readSection(String id, Map<String, Object> node, Path folder) {
        @SuppressWarnings("unchecked")
        final String nodeTitle = (String) node.get("title");
        return new TestPlanYamlSection(this, id, nodeTitle, folder);
    }

    private Map<String, FunctionPrototype<?>> readMacros(Set<Path> macroFolders) {
        Map<String, FunctionPrototype<?>> result = new LinkedHashMap<>(32);
        for (Path macroFolder : macroFolders) {
            if (Files.exists(macroFolder) && Files.isDirectory(macroFolder)) {
                try (Stream<Path> files = Files.list(macroFolder)) {
                    result.putAll(files
                            .filter(Files::isRegularFile)
                            .filter(p -> p.getFileName().toString().endsWith(".yml"))
                            .collect(Collectors.toMap(
                                    p -> Utils.fileNameWithoutExtension(p, ".yml"),
                                    this::readMacro
                            )));
                } catch (IOException e) {
                    throw new DoclerkException("failed to read macros in " + macroFolder.toString(), e);
                }
            }
        }
        return result;
    }

    private <T> FunctionPrototype<T> readMacro(Path path) {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            LoadSettings settings = LoadSettings.builder().build();
            Load load = new Load(settings);

            @SuppressWarnings("unchecked")
            Map<String, Object> macroDef = (Map<String, Object>) load.loadFromReader(reader);

            return (args) -> {
                @SuppressWarnings("unchecked")
                final List<String> argNames = (List<String>) macroDef.get("args");
                final Map<String, Expression<?>> params = new HashMap<>(8);
                if (argNames != null && args != null) {
                    int i = 0;
                    for (Expression<?> arg : args) {
                        params.put(argNames.get(i), arg);
                        i++;
                    }
                }
                final String def = (String) macroDef.get("return");
                @SuppressWarnings("unchecked")
                final Expression<T> returnExpr = (Expression<T>) new TemplateStringExpression(def, new MapContext(rootContext, params, null));
                return returnExpr.evaluate();
            };
        } catch (IOException ex) {
            throw new DoclerkException("failed to read macro definition file" + path.toString(), ex);
        }
    }

    private Map<String, Expression<?>> readProperties(final Map<String, String> propMap) {
        return propMap != null
                ? propMap.entrySet().stream().collect(
                        Collectors.toMap(
                                e -> e.getKey(),
                                e -> new TemplateStringExpression(e.getValue(), rootContext)
                        )
                )
                : Collections.emptyMap();
    }

    private Set<DocPart> readSections(final Map<String, ?> sectionMap, Path input) {
        return sectionMap.entrySet().stream()
                .map(e -> {
                    @SuppressWarnings("unchecked")
                    final Map<String, Object> section = (Map<String, Object>) e.getValue();
                    return readSection(e.getKey(), section, input.getParent().resolve(e.getKey()));
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
