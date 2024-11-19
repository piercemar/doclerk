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
package io.github.doclerk.cli;

import static org.junit.jupiter.api.Assertions.fail;

import java.net.URL;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

public class DoclerkTest {

    public DoclerkTest() {
    }

    @Test
    public void testCall() {
        try {
            final URL inFileURL = this.getClass().getResource("/doclerk.yml");
            new Doclerk(Paths.get(inFileURL.toURI())).call();
        } catch (Exception ex) {
            fail("Failed with " + ex.getClass() + ": " + ex.getMessage());
        }

    }

}
