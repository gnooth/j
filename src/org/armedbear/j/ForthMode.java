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
        setProperty(Property.INDENT_SIZE, 3);
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
        km.mapKey(KeyEvent.VK_ENTER, 0, "newlineAndIndent");
    }

    public boolean canIndent()
    {
        return true;
    }

    public boolean canIndentPaste()
    {
        return false;
    }

    public int getCorrectIndentation(Line line, Buffer buffer)
    {
        final int indentSize = buffer.getIndentSize();
        final Line model = findModel(line);
        if (model == null)
            return 0;
//         if (model.getText().trim().endsWith(":"))
//             return indentSize;
        return buffer.getIndentation(model);
    }

    private Line findModel(Line line)
    {
        Line model = line.previous();
        while (model != null && model.isBlank())
            model = model.previous();
        return model;
    }
}
