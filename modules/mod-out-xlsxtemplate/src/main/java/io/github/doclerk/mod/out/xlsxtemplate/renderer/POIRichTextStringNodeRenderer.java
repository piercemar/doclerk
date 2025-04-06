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
package io.github.doclerk.mod.out.xlsxtemplate.renderer;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.Document;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;
import org.commonmark.renderer.NodeRenderer;


public class POIRichTextStringNodeRenderer extends AbstractVisitor implements NodeRenderer {

    private static final String LINE_BREAK = "\r\n";
    private static final String INDENT = "  ";

    private final XSSFRichTextString out;
    private final Workbook wb;
    private final Stack<Font> fontStack = new Stack<>();
    private final Stack<AtomicInteger> liIndex = new Stack<>();
    private final Stack<String> liPrefix = new Stack<>();
    private int indentLevel = 0;

    public POIRichTextStringNodeRenderer(XSSFRichTextString out, Workbook wb, Font defaultFont) {
        this.out = out;
        this.wb = wb;
        this.fontStack.push(defaultFont);
    }

    private static EnumSet<FontProperty> getFontProperties(Font font) {
        EnumSet<FontProperty> props = EnumSet.noneOf(FontProperty.class);
        if (font.getBold()) {
            props.add(FontProperty.BOLD);
        }
        if (font.getItalic()) {
            props.add(FontProperty.ITALIC);
        }
        if (font.getStrikeout()) {
            props.add(FontProperty.STRIKEOUT);
        }
        if (font.getUnderline() == Font.U_SINGLE) {
            props.add(FontProperty.UNDERLINE);
        }
        if (font.getCharSet() == Font.SYMBOL_CHARSET) {
            props.add(FontProperty.SYMBOL);
        }
        if (font.getTypeOffset() == Font.SS_SUB) {
            props.add(FontProperty.SUBSCRIPT);
        }
        if (font.getTypeOffset() == Font.SS_SUPER) {
            props.add(FontProperty.SUPERSCRIPT);
        }
        return props;
    }

    private static Font getFont(Workbook wb, String ftName, short ftHeight, short ftColor, Set<FontProperty> ftProps) {
        final boolean ftBold = ftProps.contains(FontProperty.BOLD);
        final boolean ftItalic = ftProps.contains(FontProperty.ITALIC);
        final boolean ftStrikeout = ftProps.contains(FontProperty.STRIKEOUT);
        final short ftOffset = ftProps.contains(FontProperty.SUBSCRIPT)
                ? Font.SS_SUB
                : (ftProps.contains(FontProperty.SUPERSCRIPT)
                ? Font.SS_SUPER
                : Font.SS_NONE);
        final byte ftUnderline = ftProps.contains(FontProperty.UNDERLINE) ? Font.U_SINGLE : Font.U_NONE;

        Font font = wb.findFont(ftBold ? Font.BOLDWEIGHT_BOLD : Font.BOLDWEIGHT_NORMAL, ftColor, ftHeight, ftName, ftItalic, ftStrikeout, ftOffset, ftUnderline);
        if (font == null) {
            font = wb.createFont();
            font.setBold(ftBold);
            font.setColor(ftColor);
            font.setFontHeight(ftHeight);
            font.setFontName(ftName);
            font.setItalic(ftItalic);
            font.setStrikeout(ftStrikeout);
            font.setTypeOffset(ftOffset);
            font.setUnderline(ftUnderline);
        }

        return font;
    }

    private static Font getFont(Workbook wb, Font ftSrc, Set<FontProperty> ftProps) {
        final short ftColor = ftSrc.getColor();
        final short ftHeight = ftSrc.getFontHeight();
        final String ftName = ftSrc.getFontName();
        return getFont(wb, ftName, ftHeight, ftColor, ftProps);
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return new HashSet<>(Arrays.asList(
                Document.class,
                Heading.class,
                Paragraph.class,
                BlockQuote.class,
                BulletList.class,
                FencedCodeBlock.class,
                HtmlBlock.class,
                ThematicBreak.class,
                IndentedCodeBlock.class,
                Link.class,
                ListItem.class,
                OrderedList.class,
                Image.class,
                Emphasis.class,
                StrongEmphasis.class,
                Text.class,
                Code.class,
                HtmlInline.class,
                SoftLineBreak.class,
                HardLineBreak.class
        ));
    }

    @Override
    public void render(Node node) {
        node.accept(this);
    }

    @Override
    public void visit(Text text) {
        render(text.getLiteral());
    }

    @Override
    public void visit(StrongEmphasis strongEmphasis) {
        fontStack.push(getAlteredFont(fontStack.peek(), FontProperty.BOLD, true));
        visitChildren(strongEmphasis);
        fontStack.pop();
    }

    @Override
    public void visit(SoftLineBreak softLineBreak) {
        render(LINE_BREAK);
    }

    @Override
    public void visit(Paragraph paragraph) {
        if (paragraph.getPrevious() != null) {
            render(LINE_BREAK);
        }
        visitChildren(paragraph);
        render(LINE_BREAK);
    }

    @Override
    public void visit(OrderedList orderedList) {
        indentLevel++;
        liIndex.push(new AtomicInteger(1));
        liPrefix.push("%1$s. ");
        visitChildren(orderedList);
        liPrefix.pop();
        liIndex.pop();
        indentLevel--;
    }

    @Override
    public void visit(ListItem listItem) {
        for (int i = 0; i < indentLevel; i++) {
            render(INDENT);
        }
        render(String.format(liPrefix.peek(), liIndex.peek().getAndIncrement()));
        visitChildren(listItem);
    }

    @Override
    public void visit(Link link) {
        Font currentFont = fontStack.peek();
        Set<FontProperty> fontProperties = getFontProperties(currentFont);
        fontProperties.add(FontProperty.UNDERLINE);
        fontStack.push(getFont(wb, currentFont.getFontName(), currentFont.getFontHeight(), HSSFColor.BLUE.index, fontProperties));
        super.visit(link);
        fontStack.pop();
    }

    @Override
    public void visit(IndentedCodeBlock indentedCodeBlock) {
        render("[indent]");
        super.visit(indentedCodeBlock); // Generated
        render("[/indent]");
    }

    @Override
    public void visit(Image image) {
        render("[img]");
        super.visit(image); // Generated
        render("[/img]");
    }

    @Override
    public void visit(HtmlBlock htmlBlock) {
        render("[html]");
        super.visit(htmlBlock); // Generated
        render("[/html]");
    }

    @Override
    public void visit(HtmlInline htmlInline) {
        render("[htmlinline]");
        super.visit(htmlInline); // Generated
        render("[/htmlinline]");
    }

    @Override
    public void visit(ThematicBreak thematicBreak) {
        render("[thematicbreak]");
        super.visit(thematicBreak); // Generated
        render("[/thematicbreak]");
    }

    @Override
    public void visit(Heading heading) {
        int h = heading.getLevel();
        render("[" + h + "]");
        super.visit(heading); // Generated
        render("[/" + h + "]");
    }

    @Override
    public void visit(HardLineBreak hardLineBreak) {
        render(LINE_BREAK);
    }

    @Override
    public void visit(FencedCodeBlock fencedCodeBlock) {
        render("[fence]");
        super.visit(fencedCodeBlock); // Generated
        render("[/fence]");
    }

    @Override
    public void visit(Emphasis emphasis) {
        fontStack.push(getAlteredFont(fontStack.peek(), FontProperty.ITALIC, true));
        visitChildren(emphasis);
        fontStack.pop();
    }

    @Override
    public void visit(Document document) {
        super.visit(document);
    }

    @Override
    public void visit(Code code) {
        render("[code]");
        super.visit(code); // Generated
        render("[/code]");
    }

    @Override
    public void visit(BulletList bulletList) {
        indentLevel++;
        liIndex.push(new AtomicInteger(1));
        liPrefix.push("• ");
        visitChildren(bulletList);
        liPrefix.pop();
        liIndex.pop();
        indentLevel--;
    }

    @Override
    public void visit(BlockQuote blockQuote) {
        render("[quote]");
        super.visit(blockQuote); // Generated
        render("[/quote]");
    }

    private void render(String text) {
        out.append(text, (XSSFFont) fontStack.peek());
    }

    private Font getAlteredFont(Font font, FontProperty prop, boolean on) {
        final EnumSet<FontProperty> newFontProps = getFontProperties(font);
        if (on) {
            newFontProps.add(prop);
        } else {
            newFontProps.remove(prop);
        }
        return getFont(wb, font, newFontProps);
    }

    private enum FontProperty {
        BOLD, ITALIC, STRIKEOUT, UNDERLINE, SUBSCRIPT, SUPERSCRIPT, SYMBOL
    }

}
