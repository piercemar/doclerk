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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.doclerk.core.model.DocExpression;


public class FunctionExpression<R> extends DocExpression<R> {

    protected final String name;
    protected final List<Expression<?>> args;

    public FunctionExpression(ExpressionContext context, String name, Expression<?>... args) {
        this(context, name, Arrays.asList(args));
    }

    public FunctionExpression(ExpressionContext context, String name, List<Expression<?>> args) {
        super(context);
        this.name = name;
        this.args = new ArrayList<>(args);
    }

    @Override
    public R evaluate() {
        return context.<R>getFunction(name).call(args);
    }
}
