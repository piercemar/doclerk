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

import io.github.doclerk.mod.out.xlsxtemplate.renderer.POIRichTextStringNodeRenderer;
import net.sf.jett.event.CellEvent;
import net.sf.jett.event.CellListener;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;


public class TestPlanXlsxCellPostProcessor implements CellListener {

    private final Parser parser = Parser.builder().build();

    @Override
    public boolean beforeCellProcessed(CellEvent ce) {
        return true;
    }

    @Override
    public void cellProcessed(CellEvent ce) {
        final Cell cell = ce.getCell();
        //Strip-down MarkDown content
        final Object newValue = ce.getNewValue();
        if (newValue instanceof RichTextString) {
            Node document = parser.parse(((RichTextString)newValue).getString());
            final XSSFRichTextString renderedContent = new XSSFRichTextString();
            final Workbook workbook = cell.getSheet().getWorkbook();
            final Font cellFont = workbook.getFontAt(cell.getCellStyle().getFontIndex());
            NodeRenderer renderer = new POIRichTextStringNodeRenderer(renderedContent, workbook, cellFont);
            renderer.render(document);
            cell.setCellValue(renderedContent);
        }
        //Force auto-height
        cell.getRow().setHeight((short) -1);
    }

}
