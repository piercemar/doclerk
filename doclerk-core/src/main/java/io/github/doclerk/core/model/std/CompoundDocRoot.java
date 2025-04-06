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
package io.github.doclerk.core.model.std;

import io.github.doclerk.core.expression.Expression;
import io.github.doclerk.core.expression.ExpressionContext;
import io.github.doclerk.core.expression.FunctionPrototype;
import io.github.doclerk.core.expression.UndefinedFunctionException;
import io.github.doclerk.core.expression.UndefinedVariableException;
import io.github.doclerk.core.model.DocPart;
import io.github.doclerk.core.model.DocParts;
import io.github.doclerk.core.model.DocRoot;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.BinaryOperator;

/**
 * Helper DocRoot for compositing two or more DocRoots.
 */
public class CompoundDocRoot implements DocRoot {

    private final DocRootMergePolicy mergePolicy;
    private final ArrayList<DocRoot> roots;

    public CompoundDocRoot(DocRootMergePolicy mergePolicy, DocRoot firstRoot, DocRoot secondRoot, DocRoot... otherRoots) {
        Objects.requireNonNull(firstRoot, "firstRoot may not be null");
        Objects.requireNonNull(secondRoot, "secondRoot may not be null");
        Objects.requireNonNull(otherRoots, "otherRoots may not be null");
        this.mergePolicy = mergePolicy;
        this.roots = new ArrayList<>(2 + otherRoots.length);
        this.roots.add(firstRoot);
        this.roots.add(secondRoot);
        for (DocRoot otherRoot : otherRoots) {
            this.roots.add(Objects.requireNonNull(otherRoot, "otherRoots may not contain null reference"));
        }
    }

    @Override
    public ExpressionContext getExpressionContext() {
        return new ExpressionContext() {
            @Override
            public <T> Expression<T> getVar(String key) {
                // Replace with roots.reversed() when on JDK21+
                ListIterator<DocRoot> li = roots.listIterator(roots.size());
                while (li.hasPrevious()) {
                    DocRoot dr = li.previous();
                    try {
                        return dr.getExpressionContext().getVar(key);
                    } catch (UndefinedVariableException uve) {
                        //try next
                    }
                }
                throw new UndefinedVariableException(key);
            }

            @Override
            public <T> FunctionPrototype<T> getFunction(String name) {
                // Replace with roots.reversed() when on JDK21+
                ListIterator<DocRoot> li = roots.listIterator(roots.size());
                while (li.hasPrevious()) {
                    DocRoot dr = li.previous();
                    try {
                        return dr.getExpressionContext().getFunction(name);
                    } catch (UndefinedFunctionException uve) {
                        //try next
                    }
                }
                throw new UndefinedFunctionException(name);
            }

        };
    }

    @Override
    public Set<DocPart> getParts() {
        Set<DocPart> parts = roots.get(0).getParts();
        for (int i = 1; i < roots.size(); i++) {
            parts = mergePolicy.mergeDocParts(parts, roots.get(i).getParts());
        }
        return parts;
    }

    @Override
    public String getId() {
        String id = roots.get(0).getId();
        for (int i = 1; i < roots.size(); i++) {
            id = mergePolicy.mergeId(id, roots.get(i).getId());
        }
        return id;
    }

    @Override
    public DocParts getParent() {
        return null;
    }

    @Override
    public String getTitle() {
        String title = roots.get(0).getTitle();
        for (int i = 1; i < roots.size(); i++) {
            title = mergePolicy.mergeTitle(title, roots.get(i).getTitle());
        }
        return title;
    }

    public static class DocRootMergePolicy {

        private final BinaryOperator<String> idMerger;
        private final BinaryOperator<String> titleMerger;
        private final BinaryOperator<Set<DocPart>> docPartsMerger;

        public DocRootMergePolicy(BinaryOperator<String> idMerger, BinaryOperator<String> titleMerger, BinaryOperator<Set<DocPart>> docPartMerger) {
            this.idMerger = idMerger;
            this.titleMerger = titleMerger;
            this.docPartsMerger = docPartMerger;
        }

        public String mergeId(String currentId, String otherId) {
            return idMerger.apply(currentId, otherId);
        }

        public String mergeTitle(String currentTitle, String otherTitle) {
            return titleMerger.apply(currentTitle, otherTitle);
        }

        public Set<DocPart> mergeDocParts(Set<DocPart> currentParts, Set<DocPart> otherParts) {
            return docPartsMerger.apply(currentParts, otherParts);
        }

    }

}
