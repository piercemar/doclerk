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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import io.github.doclerk.core.expression.Expression;
import io.github.doclerk.core.expression.ExpressionContext;
import io.github.doclerk.core.expression.TemplateStringExpression;
import io.github.doclerk.core.model.DocPart;
import io.github.doclerk.core.model.DocParts;

public class TestPlanYamlScenario implements DocParts {

    private final DocParts parent;
    private final String id;
    private final Expression<String> title;
    private final Expression<String> context;
    private final Expression<String> goal;
    private final Set<DocPart> steps;

    public TestPlanYamlScenario(DocParts parent, String id, Map<String, ?> data) {
        this.parent = parent;
        this.id = id;

        final ExpressionContext ec = getRoot().getExpressionContext();
        this.title = new TemplateStringExpression((String) data.get("title"), ec);
        this.goal = new TemplateStringExpression((String) data.get("goal"), ec);
        this.context = new TemplateStringExpression((String) data.get("context"), ec);

        Optional<List<?>> stepList = Optional.ofNullable((List<?>) data.get("steps"));
        steps = stepList.orElse(Collections.emptyList()).stream()
                .filter(Objects::nonNull)
                .map(this::readStep)
                .collect(Collectors.toCollection(LinkedHashSet::new));

    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Set<DocPart> getParts() {
        return Collections.unmodifiableSet(steps);
    }

    @Override
    public String getTitle() {
        return title.evaluate();
    }

    public String getContext() {
        return context.evaluate();
    }

    public String getGoal() {
        return goal.evaluate();
    }

    @Override
    public DocParts getParent() {
        return parent;
    }

    private TestPlanYamlStep readYamlStep(String id, Map<String, String> stepData) {
        final TestPlanYamlStep step = new TestPlanYamlStep(this, id, stepData.get("title"));
        step.setDescription(stepData.get("description"));
        step.setPreCondition(stepData.get("preCondition"));
        step.setRequiredData(stepData.get("requiredData"));
        step.setExpectedResult(stepData.get("expectedResult"));
        step.setComment(stepData.get("comment"));
        return step;
    }

    private TestPlanStep readStep(Object obj) {
        if (obj instanceof String && ((String) obj).startsWith("§")) {
            // It's a reference
            return new TestPlanStepReference(this, null, (String) obj);
        } else {
            // It's a YAML step definition
            @SuppressWarnings("unchecked")
            final Map<String, String> stepData = (Map<String, String>) obj;
            return readYamlStep(null, stepData);
        }
    }

}
