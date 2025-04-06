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

import io.github.doclerk.core.model.DocParts;
import io.github.doclerk.core.expression.Expression;
import io.github.doclerk.core.expression.TemplateStringExpression;


public class TestPlanYamlStep implements TestPlanStep {

    private final DocParts parent;
    private final String id;
    private final Expression<String> title;
    private Expression<String> description;
    private Expression<String> expectedResult;
    private Expression<String> requiredData;
    private Expression<String> preCondition;
    private Expression<String> comment;

    public TestPlanYamlStep(DocParts parent, String id, String title) {
        this.parent = parent;
        this.id = id;
        this.title = new TemplateStringExpression(title, getRoot().getExpressionContext());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title.evaluate();
    }

    @Override
    public String getDescription() {
        return description.evaluate();
    }

    public void setDescription(String description) {
        this.description = new TemplateStringExpression(description, getRoot().getExpressionContext());
    }

    @Override
    public String getExpectedResult() {
        return expectedResult.evaluate();
    }

    public void setExpectedResult(String expectedResult) {
        this.expectedResult = new TemplateStringExpression(expectedResult, getRoot().getExpressionContext());
    }

    @Override
    public String getRequiredData() {
        return requiredData.evaluate();
    }

    public void setRequiredData(String requiredData) {
        this.requiredData = new TemplateStringExpression(requiredData, getRoot().getExpressionContext());
    }

    @Override
    public String getPreCondition() {
        return preCondition.evaluate();
    }

    public void setPreCondition(String preCondition) {
        this.preCondition = new TemplateStringExpression(preCondition, getRoot().getExpressionContext());
    }

    @Override
    public DocParts getParent() {
        return parent;
    }

    public void setComment(String comment) {
        this.comment = new TemplateStringExpression(comment, getRoot().getExpressionContext());
    }

    @Override
    public String getComment() {
        return comment.evaluate();
    }

}
