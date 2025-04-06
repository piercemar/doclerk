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
package io.github.doclerk.mod.out.xlsxtemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.jett.transform.ExcelTransformer;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import io.github.doclerk.core.exception.DoclerkException;
import io.github.doclerk.core.model.DocPart;
import io.github.doclerk.core.model.DocPrinter;
import io.github.doclerk.core.model.DocRoot;
import io.github.doclerk.core.model.Titled;

public class XlsxTemplateDocPrinter implements DocPrinter {

    protected final OutputStream out;
    private final URL templateURL;

    public XlsxTemplateDocPrinter(URL templateURL, OutputStream out) {
        this.templateURL = templateURL;
        this.out = out;
    }

    @Override
    public void print(DocRoot doc) {
        Map<String, Object> context = new HashMap<>(8);
        context.put("root", doc);
        context.put("now", Instant.now());
        context.put("env", System.getenv());
        context.put("system", Collections.unmodifiableMap(System.getProperties()));

        List<String> templateSheets = new ArrayList<>(2 + doc.getParts().size());
        List<String> sheetNames = new ArrayList<>(2 + doc.getParts().size());
        List<Map<String, Object>> beansList = new ArrayList<>(2 + doc.getParts().size());
        templateSheets.add("Historique");
        sheetNames.add("Historique");
        beansList.add(context);
        for (DocPart docPart : doc.getParts()) {
            templateSheets.add("_section_");
            sheetNames.add(((Titled)docPart).getTitle());
            Map<String, Object> sectionCtx = new HashMap<>(context);
            sectionCtx.put("section", docPart);
            beansList.add(sectionCtx);
        }
        templateSheets.add("Légendes");
        sheetNames.add("Légendes");
        beansList.add(context);

        try (InputStream templateStream = templateURL.openStream()) {
            ExcelTransformer transformer = new ExcelTransformer();
            transformer.addCellListener(new TestPlanXlsxCellPostProcessor());
            Workbook wb = transformer.transform(
                    templateStream,
                    templateSheets,
                    sheetNames,
                    beansList);
            wb.write(out);
            out.flush();
        } catch (IOException e) {
            throw new DoclerkException("failed to open template URI: " + templateURL.toString(), e);
        } catch (InvalidFormatException e) {
            throw new DoclerkException("failed to transform: " + templateURL.toString(), e);
        }
    }

}
