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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.github.doclerk.core.module.DoclerkModuleInfo;
import io.github.doclerk.core.module.ModuleContext;
import io.github.doclerk.core.module.ModuleContextKeys;
import java.net.URISyntaxException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class XlsxTemplateModuleTest {

    private final XlsxTemplateModule instance;
    private final ModuleContext ctx;

    public XlsxTemplateModuleTest(@Mock ModuleContext ctx) {
        this.ctx = ctx;
        instance = new XlsxTemplateModule().withContext(ctx);
    }

    @Test
    public void testModuleInfo() {
        final DoclerkModuleInfo moduleInfo = instance.moduleInfo();
        assertNotNull(moduleInfo);
        assertEquals("XlsxTemplate", moduleInfo.moduleName());
    }

    @Test
    public void testDefaultContext() {
        assertNotNull(instance.defaultContext());
    }

    @Test
    public void testGetTemplateUriClasspath() throws URISyntaxException {
        when(ctx.getValue(eq("xlsxtemplate.template"))).thenReturn(Optional.of("classpath:/template.xlsx"));
        assertNotNull(instance.getTemplateUri());
        when(ctx.getValue(eq("xlsxtemplate.template"))).thenReturn(Optional.of("classpath:/not_existing_template.xlsx"));
        assertThrows(URISyntaxException.class, () -> instance.getTemplateUri());
    }

    @Test
    public void testGetTemplateUriAbsolutePath() throws URISyntaxException {
        when(ctx.getValue(eq("xlsxtemplate.template"))).thenReturn(Optional.of("/path/to/template/template.xlsx"));
        assertEquals("file:///path/to/template/template.xlsx", instance.getTemplateUri().toString());
    }

    @Test
    public void testGetTemplateUriRelativePathToCurrentDir() throws URISyntaxException {
        String userDir = System.getProperty("user.dir");
        when(ctx.getValue(eq("xlsxtemplate.template"))).thenReturn(Optional.of("path/to/template/template.xlsx"));
        assertEquals("file://" + userDir + "/path/to/template/template.xlsx", instance.getTemplateUri().toString());
    }

    @Test
    public void testGetTemplateUriRelativePathToWorkDir() throws URISyntaxException {
        when(ctx.getValue(eq(ModuleContextKeys.WORK_DIR_URI))).thenReturn(Optional.of("file:///workdir"));
        when(ctx.getValue(eq("xlsxtemplate.template"))).thenReturn(Optional.of("path/to/template/template.xlsx"));
        assertEquals("file:///workdir/path/to/template/template.xlsx", instance.getTemplateUri().toString());
    }

    @Test
    public void testGetTemplateUriURL() throws URISyntaxException {
        when(ctx.getValue(eq("xlsxtemplate.template"))).thenReturn(Optional.of("http://hostname/path/to/template/template.xlsx"));
        assertEquals("http://hostname/path/to/template/template.xlsx", instance.getTemplateUri().toString());
    }

    @Test
    public void testGetOutputUri() throws Exception {
    }

}
