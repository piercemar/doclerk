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

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;


public class ExpressionParser {

    private final ExpressionContext context;

    public ExpressionParser(ExpressionContext context) {
        this.context = context;
    }

    public <T> Expression<T> parseExpression(String expression, Class<T> expected) {
        if (expression == null) {
            return new NullExpression<>();
        }
        final String trimmed = expression.trim();
        if (expression.isEmpty() || trimmed.isEmpty()) {
            return assignableFromString(expression, expected, (s) -> new StringExpression(s));
        }
        if (trimmed.startsWith("\"")) {
            return assignableFromString(trimmed, expected, (s) -> parseString(trimmed));
        } else if (trimmed.startsWith("&")) {
            return parseVar(trimmed);
        } else if (trimmed.startsWith("$")) {
            return parseFunction(trimmed, expected);
        } else {
            throw new InvalidSyntaxException("failed to parse expression: " + expression);
        }
    }

    <R> Expression<R> parseFunction(String function, Class<R> expected) {
        String name = "";
        List<Expression<?>> args = new LinkedList<>();
        StringBuilder token = new StringBuilder();
        int depth = 0;
        boolean escape = false;
        boolean inStr = false;
        for (int i = 0; i < function.length(); i++) {
            char a = function.charAt(i);
            if (escape) {
                escape = false;
                token.append(a);
                continue;
            }
            switch (a) {
                case '\\':
                    escape = true;
                    continue;
                case '"':
                    if (!inStr) {
                        inStr = true;
                        depth++;
                    } else {
                        inStr = false;
                        depth--;
                    }
                    token.append(a);
                    break;
                case '(':
                    if (depth == 0) {
                        name = token.toString();
                        token = new StringBuilder();
                    } else {
                        token.append(a);
                    }
                    if (!inStr) {
                        depth++;
                    }
                    break;
                case ')':
                    if (!inStr) {
                        depth--;
                    }
                    if (depth == 0) {
                        final String tokenStr = token.toString();
                        if (!tokenStr.trim().isEmpty()) {
                            args.add(parseExpression(tokenStr, Object.class));
                            token = new StringBuilder();
                        }
                    } else {
                        token.append(a);
                    }
                    break;
                case ',':
                    if (depth == 1) {
                        final String tokenStr = token.toString();
                        if (!tokenStr.trim().isEmpty()) {
                            args.add(parseExpression(token.toString(), Object.class));
                            token = new StringBuilder();
                        } else {
                            throw new InvalidSyntaxException("Unexpected token ',' encountered. Expected: expression");
                        }
                    } else {
                        token.append(a);
                    }
                    break;
                default:
                    token.append(a);
            }
        }
        return new FunctionExpression<>(context, name.substring(1), args);
    }

    Expression<String> parseString(String string) {
        boolean escape = false;
        boolean inStr = false;
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char a = string.charAt(i);
            if (escape) {
                escape = false;
                token.append(a);
                continue;
            }
            switch (a) {
                case ' ':
                    if (inStr) {
                        token.append(a);
                    }
                    break;
                case '\\':
                    escape = true;
                    continue;
                case '"':
                    if (!inStr) {
                        inStr = true;
                        token = new StringBuilder();
                    } else {
                        return new StringExpression(token.toString());
                    }
                    break;
                default:
                    token.append(a);
            }
        }
        throw new InvalidSyntaxException("not a valid string expression: " + string);
    }

    private <T> Expression<T> assignableFromString(String expression, Class<?> expected, Function<String, Expression<String>> producer) {
        if (expected.isAssignableFrom(String.class)) {
            @SuppressWarnings("unchecked")
            final Expression<T> stringExpr = (Expression<T>) producer.apply(expression);
            return stringExpr;
        } else {
            throw new InvalidSyntaxException("Expression \"" + expression + "\" cannot be parsed to an Expression<" + expected.getName() + ">");
        }
    }

    private <T> Expression<T> parseVar(String varName) {
        return new VarExpression<>(varName.substring(1), context);
    }
}
