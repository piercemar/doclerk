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
package io.github.doclerk.core.model.std;

import io.github.doclerk.core.expression.ExpressionContext;
import io.github.doclerk.core.expression.MapContext;
import io.github.doclerk.core.model.DocPart;
import io.github.doclerk.core.model.DocParts;
import io.github.doclerk.core.model.DocRoot;
import java.util.Collections;
import java.util.Set;

/**
 * Blank implementation of DocRoot.
 * <p>
 * Typically used as input to the first DoclerkModule of a chain.
 */
public class BlankDocRoot implements DocRoot {

    @Override
    public ExpressionContext getExpressionContext() {
        return new MapContext(null, Collections.emptyMap(), Collections.emptyMap());
    }

    /**
     * @return an unmodifiable empty set.
     */
    @Override
    public Set<DocPart> getParts() {
        return Collections.emptySet(); //JDK10+ Set.of()
    }

    /**
     * @return {@code null}
     */
    @Override
    public String getId() {
        return null;
    }

    /**
     * @return {@code null}
     */
    @Override
    public DocParts getParent() {
        return null;
    }

    /**
     * @return {@code null}
     */
    @Override
    public String getTitle() {
        return null;
    }

}
