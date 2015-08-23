/*
 * ForthMode.java
 *
 * Copyright (C) 2015 Peter Graves <gnooth@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.armedbear.j;

import java.awt.event.KeyEvent;

public final class ForthMode extends AbstractMode implements Constants, Mode
{
    private static final ForthMode mode = new ForthMode();

    private ForthMode()
    {
        super(FORTH_MODE, FORTH_MODE_NAME);
        setProperty(Property.INDENT_SIZE, 4);
    }

    public static ForthMode getMode()
    {
        return mode;
    }

    public String getCommentStart()
    {
        return "\\ ";
    }

    public Formatter getFormatter(Buffer buffer)
    {
        return new ForthFormatter(buffer);
    }

    protected void setKeyMapDefaults(KeyMap km)
    {
        km.mapKey(KeyEvent.VK_TAB, 0, "tab");
        km.mapKey(KeyEvent.VK_TAB, CTRL_MASK, "insertTab");
        km.mapKey(KeyEvent.VK_ENTER, 0, "newlineAndIndent");
        km.mapKey(KeyEvent.VK_T, CTRL_MASK, "findTag");
        km.mapKey(KeyEvent.VK_PERIOD, ALT_MASK, "findTagAtDot");
    }

    public Tagger getTagger(SystemBuffer buffer)
    {
        return new ForthTagger(buffer);
    }

    public boolean isTaggable()
    {
        return true;
    }

    public boolean canIndent()
    {
        return true;
    }

    public boolean canIndentPaste()
    {
        return false;
    }

    public boolean isIdentifierStart(char c)
    {
        return !Character.isWhitespace(c);
    }

    public boolean isIdentifierPart(char c)
    {
        return !Character.isWhitespace(c);
    }

    public int getCorrectIndentation(Line line, Buffer buffer)
    {
        final int indentSize = buffer.getIndentSize();
        final Line model = findModel(line);
        if (model == null)
            return 0;
        final int modelIndent = buffer.getIndentation(model);
        final int indented = modelIndent + indentSize;
        String modelTrim =
            Utilities.detab(model.getText(), buffer.getTabWidth()).trim().toLowerCase();
        int index = modelTrim.indexOf(" \\ ");
        if (index >= 0)
            modelTrim = modelTrim.substring(0, index).trim();
        if (modelTrim.endsWith(" ;"))
            return 0;
        if (modelTrim.startsWith(": "))
            return indented;
        if (modelTrim.equals("begin") || modelTrim.endsWith(" begin"))
            return indented;
        if (modelTrim.equals("while") || modelTrim.endsWith(" while"))
            return indented;
        if (modelTrim.equals("if") || modelTrim.endsWith(" if"))
            return indented;
        if (modelTrim.equals("else") || modelTrim.endsWith(" else"))
            return indented;
        if (modelTrim.equals("do") || modelTrim.endsWith(" do"))
            return indented;
        if (modelTrim.equals("?do") || modelTrim.endsWith(" ?do"))
            return indented;
        String trim =
            Utilities.detab(line.getText(), buffer.getTabWidth()).trim().toLowerCase();
        index = trim.indexOf(" \\ ");
        if (index >= 0)
            trim = trim.substring(0, index).trim();
        final int outdented = modelIndent - indentSize;
        if (trim.equals("while") || trim.equals("repeat"))
            return outdented;
        if (trim.startsWith("while ") || trim.startsWith("repeat "))
            return outdented;
        if (trim.equals("until") || trim.startsWith("until "))
            return outdented;
        if (trim.equals("then") || trim.equals("else"))
            return outdented;
        if (trim.startsWith("then ") || trim.startsWith("else "))
            return outdented;
        if (trim.equals("loop") || trim.equals("+loop"))
            return outdented;
        if (trim.startsWith("loop ") || trim.startsWith("+loop"))
            return outdented;
        if (trim.startsWith(": "))
            return 0;
        if (trim.equals(";") || trim.startsWith("; "))
            return 0;
        return modelIndent;
    }

    private Line findModel(Line line)
    {
        Line model = line.previous();
        while (model != null) {
            if (model.isBlank() || model.trim().startsWith("\\ "))
                model = model.previous();
            else
                break;
        }
        return model;
    }
}
