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

import io.github.doclerk.core.model.DocExpression;


public class TemplateStringExpression extends DocExpression<String> {

    private final Expression<String> expression;
    private final ExpressionParser ep;

    public TemplateStringExpression(String expression, ExpressionContext context) {
        super(context);
        this.ep = new ExpressionParser(context);
        this.expression = expression == null ? new NullExpression<>() : parseYmlString(expression, context);
    }

    @Override
    public String evaluate() {
        return expression.evaluate();
    }

    final Expression<String> parseYmlString(String expression, ExpressionContext context) {
        List<Expression<String>> expr = new LinkedList<>();
        StringBuilder token = new StringBuilder();
        int depth = 0;
        boolean inFunc = false;
        boolean inVar = false;
        for (int i = 0; i < expression.length(); i++) {
            char a = expression.charAt(i);
            String tokenStr;
            switch (a) {
                case '$':
                    if (!inVar && !inFunc) {
                        tokenStr = token.toString();
                        if (!tokenStr.isEmpty()) {
                            expr.add(new StringExpression(tokenStr));
                            token = new StringBuilder();
                        }
                        inFunc = true;
                    }
                    token.append(a);
                    break;
                case '(':
                    token.append(a);
                    if (inFunc) {
                        depth++;
                    }
                    break;
                case ')':
                    token.append(a);
                    if (inFunc) {
                        depth--;
                        if (depth == 0) {
                            expr.add(ep.parseExpression(token.toString(), String.class));
                            inFunc = false;
                            token = new StringBuilder();
                        }
                    }
                    break;
                case '&':
                    if (!inFunc) {
                        tokenStr = token.toString();
                        if (!tokenStr.isEmpty()) {
                            if (inVar) {
                                expr.add(ep.parseExpression(tokenStr, String.class));
                            } else {
                                expr.add(new StringExpression(tokenStr));
                            }
                            token = new StringBuilder();
                        }
                        inVar = true;
                    }
                    token.append(a);
                    break;
                default:
                    if (!Character.isLetterOrDigit(a) && inVar) {
                        expr.add(ep.parseExpression(token.toString(), String.class));
                        inVar = false;
                        token = new StringBuilder();
                    }
                    token.append(a);
            }
        }
        final String tokenStr = token.toString();
        if (!tokenStr.isEmpty() && inVar) {
            expr.add(ep.parseExpression(token.toString(), String.class));
        } else {
            expr.add(new StringExpression(tokenStr));
        }

        return expr.size() > 1 ? new ConcatStrExpression(expr) : expr.get(0);
    }

    private static class ConcatStrExpression implements Expression<String> {

        private final List<Expression<String>> strings;

        ConcatStrExpression(List<Expression<String>> expr) {
            this.strings = expr;
        }

        @Override
        public String evaluate() {
            StringBuilder sb = new StringBuilder();
            for (Expression<String> str : strings) {
                String part;
                try {
                    part = str.evaluate();
                } catch (Exception e) {
                    part = e.getMessage();
                }
                sb.append(part);
            }
            return sb.toString();
        }
    }

}
