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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractModule<T extends DoclerkModule> implements DoclerkModule {

    private static final String URL_PROTOCOL_CLASSPATH = "classpath:";
    private static final String LOG_TRACE_CONTEXT = "Context: {}={}";
    protected final Logger logger = LogManager.getLogger(moduleInfo().moduleName());

    protected ModuleContext context = defaultContext();

    @Override
    public T withContext(ModuleContext ctx) {
        this.context = new HierarchicalModuleContext(context, ctx);
        return (T) this;
    }

    protected abstract ModuleContext defaultContext();

    protected void traceContext(String key) {
        logger.trace(LOG_TRACE_CONTEXT, key, context.getValue(key));
    }

    protected URI computeUri(String uriString) throws URISyntaxException {
        URI computedUri = new URI(uriString);
        if (uriString.startsWith(URL_PROTOCOL_CLASSPATH)) {
            computedUri = classpathToUri(uriString.substring(URL_PROTOCOL_CLASSPATH.length()));
        } else {
            if (computedUri.getScheme() == null) {
                Path path = Paths.get(uriString);
                if (!path.isAbsolute()) {
                    path = context.getValue(ModuleContextKeys.WORK_DIR_URI)
                            .map(URI::create)
                            .map(Paths::get)
                            .map(base -> base.resolve(uriString))
                            .orElse(path).toAbsolutePath();
                }
                computedUri = path.toUri();
            }
        }
        computedUri = computedUri.normalize();
        logger.debug("Computed template URI: {}", computedUri);
        return computedUri;
    }

    private URI classpathToUri(final String ressource) throws URISyntaxException {
        final URL resUrl = this.getClass().getResource(ressource);
        if (resUrl != null) {
            return resUrl.toURI().normalize();
        } else {
            throw new URISyntaxException(ressource, "classpath resource not found");
        }
    }

}
