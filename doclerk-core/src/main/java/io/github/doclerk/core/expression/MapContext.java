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
package io.github.doclerk.core.expression;

import java.util.HashMap;
import java.util.Map;
import io.github.doclerk.core.exception.DoclerkException;


public class MapContext implements ExpressionContext {

    private final ExpressionContext parentContext;
    private final Map<String, Expression<?>> vars;
    private final Map<String, FunctionPrototype<?>> funcs;

    public MapContext(ExpressionContext parentContext, Map<String, Expression<?>> vars, Map<String, FunctionPrototype<?>> funcs) {
        this.parentContext = parentContext;
        this.vars = vars != null ? new HashMap<>(vars) : null;
        this.funcs = funcs != null ? new HashMap<>(funcs) : null;
    }

    @Override
    public <T> Expression<T> getVar(String key) {
        if (vars != null && vars.containsKey(key)) {
            try {
                @SuppressWarnings("unchecked")
                final Expression<T> val = (Expression<T>) vars.get(key);
                return val;
            } catch (ClassCastException e) {
                throw new DoclerkException("Failed to get property " + key, e);
            }
        } else if (parentContext != null) {
            return parentContext.getVar(key);
        } else {
            throw new DoclerkException("undefined var: " + key);
        }
    }

    @Override
    public <T> FunctionPrototype<T> getFunction(String name) {
        if (funcs != null && funcs.containsKey(name)) {
            @SuppressWarnings("unchecked")
            final FunctionPrototype<T> macro = (FunctionPrototype<T>) funcs.get(name);
            return macro;
        } else if (parentContext != null) {
            return parentContext.getFunction(name);
        } else {
            throw new UndefinedFunctionException("Undefined function: " + name);
        }
    }

}
