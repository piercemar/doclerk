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

import java.text.MessageFormat;
import java.util.ResourceBundle;
import io.github.doclerk.core.model.DocParts;
import io.github.doclerk.core.model.Titled;

public class TestPlanStepReference implements TestPlanStep {

    private static final ResourceBundle bundle = ResourceBundle.getBundle("Bundle");

    private final DocParts parent;
    private final String id;
    private final String sectionId;
    private final String scenarioId;
    private final String stepId;

    public TestPlanStepReference(DocParts parent, String id, String reference) {
        this.parent = parent;
        this.id = id;
        String[] refs = reference.substring(1).split("§", 3);
        if (refs.length > 1) {
            this.sectionId = refs[0];
            this.scenarioId = refs[1];
        } else {
            throw new IllegalArgumentException("invalid reference: " + reference);
        }
        this.stepId = refs.length > 2 ? refs[2] : null;
    }

    @Override
    public String getDescription() {
        final String sectionPart = MessageFormat.format(bundle.getString("step_ref_desc_0"), new Object[]{getSectionTitle(sectionId)});
        final String scenarioPart = MessageFormat.format(bundle.getString("step_ref_desc_1"), new Object[]{scenarioId});
        final String stepPart = stepId != null ? MessageFormat.format(bundle.getString("step_ref_desc_2"), new Object[]{stepId}) : "";
        return MessageFormat.format(bundle.getString("step_ref_desc"), new Object[]{sectionPart, scenarioPart, stepPart});
    }

    @Override
    public String getExpectedResult() {
        return "";
    }

    @Override
    public String getPreCondition() {
        return "";
    }

    @Override
    public String getRequiredData() {
        return "";
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTitle() {

        return MessageFormat.format(bundle.getString("step_ref_title"), new Object[]{getSectionTitle(sectionId), scenarioId});
    }

    @Override
    public DocParts getParent() {
        return parent;
    }

    @Override
    public String getComment() {
        return "";
    }

    private String getSectionTitle(String id) {
        return getRoot().getParts().stream()
                .filter(s -> s.getId().equals(id))
                .map(p -> ((Titled) p).getTitle())
                .findFirst()
                .orElse("Undefined section: " + id);
    }

}
