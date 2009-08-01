/*
 * PlainTextMode.java
 *
 * Copyright (C) 1998-2004 Peter Graves
 * $Id: PlainTextMode.java,v 1.3 2004/09/21 00:03:35 piso Exp $
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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.armedbear.j;

import java.awt.event.KeyEvent;

public final class PlainTextMode extends AbstractMode implements Constants, Mode
{
    private static final PlainTextMode mode = new PlainTextMode();

    private PlainTextMode()
    {
        super(PLAIN_TEXT_MODE, PLAIN_TEXT_MODE_NAME);
        setProperty(Property.HIGHLIGHT_MATCHING_BRACKET, false);
        setProperty(Property.HIGHLIGHT_BRACKETS, false);
    }

    public static final PlainTextMode getMode()
    {
        return mode;
    }

    protected void setKeyMapDefaults(KeyMap km)
    {
        km.mapKey(KeyEvent.VK_F12, CTRL_MASK | SHIFT_MASK,
                  "wrapParagraphsInRegion");
    }
}
