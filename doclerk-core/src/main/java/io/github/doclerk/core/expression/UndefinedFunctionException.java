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

import io.github.doclerk.core.exception.DoclerkException;

public class UndefinedFunctionException extends DoclerkException {

    private static final long serialVersionUID = 1L;

    /**
     *
     * @param funcName the function name
     */
    public UndefinedFunctionException(String funcName) {
        super("Undefined function: " + funcName);
    }

}
