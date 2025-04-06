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
package io.github.doclerk.core.module;

import java.util.Optional;

public class HierarchicalModuleContext implements ModuleContext {

    private final ModuleContext context;
    private final ModuleContext parent;

    public HierarchicalModuleContext(ModuleContext parent, ModuleContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }
        this.context = context;
        this.parent = parent;
    }

    @Override
    public Optional<String> getValue(String key) {
        final Optional<String> value = context.getValue(key);
        if (!value.isPresent() && parent != null) {
            return parent.getValue(key);
        }
        return value;
    }

}
