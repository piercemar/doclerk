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
package io.github.doclerk.mod.out.xlsxtemplate;

import io.github.doclerk.core.exception.TransformationException;
import io.github.doclerk.core.model.DocRoot;
import io.github.doclerk.core.module.AbstractModule;
import io.github.doclerk.core.module.HashMapModuleContext;
import io.github.doclerk.core.module.ModuleContext;
import io.github.doclerk.core.module.ModuleContextKeys;
import io.github.doclerk.core.module.DoclerkModuleInfo;
import io.github.doclerk.core.module.SimpleDoclerkModuleInfo;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class XlsxTemplateModule extends AbstractModule<XlsxTemplateModule> {

    private static final SimpleDoclerkModuleInfo MODULE_INFO = new SimpleDoclerkModuleInfo("XlsxTemplate", "dev");
    private static final String CONTEXT_SCOPE_XLSXTEMPLATE = "xlsxtemplate.";
    private static final String CONTEXT_KEY_NAME = CONTEXT_SCOPE_XLSXTEMPLATE + "name";
    private static final String CONTEXT_KEY_TEMPLATE_URI = CONTEXT_SCOPE_XLSXTEMPLATE + "template";
    private static final String DEFAULT_NAME = "template.xlsx";
    private static final String DEFAULT_TEMPLATE_URI = "classpath:/template.xlsx";
    private static final String DEFAULT_OUTPUT_URI = ".";

    @Override
    public DocRoot run(DocRoot root) {
        try (OutputStream out = getOutputStream()) {
            final URL templateURL = getTemplateUri().toURL();
            final XlsxTemplateDocPrinter docPrinter = new XlsxTemplateDocPrinter(templateURL, out);
            docPrinter.print(root);
            return root;
        } catch (URISyntaxException | IOException ex) {
            throw new TransformationException("Failed to generate testplan", ex);
        }

    }

    @Override
    public DoclerkModuleInfo moduleInfo() {
        return MODULE_INFO;
    }

    @Override
    protected ModuleContext defaultContext() {
        Map<String, String> defaultContext = new HashMap<>(1);
        defaultContext.put(CONTEXT_KEY_NAME, DEFAULT_NAME);
        defaultContext.put(CONTEXT_KEY_TEMPLATE_URI, DEFAULT_TEMPLATE_URI);

        return new HashMapModuleContext(defaultContext);
    }

    URI getTemplateUri() throws URISyntaxException {
        traceContext(CONTEXT_KEY_TEMPLATE_URI);
        final String computedValue = context.getValue(CONTEXT_KEY_TEMPLATE_URI)
                .orElse(DEFAULT_TEMPLATE_URI);
        return computeUri(computedValue);
    }

    private OutputStream getOutputStream() throws IOException {
        final Path outputPath = Paths.get(getOutputUri());
        Files.createDirectories(outputPath.getParent());
        return Files.newOutputStream(outputPath);
    }


    URI getOutputUri() throws IOException {
        traceContext(ModuleContextKeys.WORK_DIR_URI);
        final URI computedUri = context.getValue(ModuleContextKeys.WORK_DIR_URI)
                .map(v -> v.endsWith("/") ? v : v + "/")
                .map(URI::create)
                .orElse(Paths.get(DEFAULT_OUTPUT_URI).toUri())
                .resolve(encodeURL(context.getValue(CONTEXT_KEY_NAME).orElse(DEFAULT_NAME)))
                .normalize();
        logger.debug("Computed output URI: {}", computedUri);
        return computedUri;
    }

    private String encodeURL(String urlPart) throws UnsupportedEncodingException {
        return URLEncoder.encode(urlPart, StandardCharsets.UTF_8.name());
    }
}
