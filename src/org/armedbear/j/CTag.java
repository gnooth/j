/*
 * CTag.java
 *
 * Copyright (C) 2002 Peter Graves
 * $Id: CTag.java,v 1.1.1.1 2002/09/24 16:09:00 piso Exp $
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

public final class CTag extends LocalTag
{
    public CTag(String name, Position pos)
    {
        super(name, pos);
    }

    public String getLongName()
    {
        String s = signature.trim();
        if (s.startsWith("DEFUN")) {
            // Emacs source.
            return name;
        }
        // Strip comment if any.
        int index = s.indexOf("//");
        if (index >= 0)
            s = s.substring(0, index).trim();
        index = s.indexOf(')');
        if (index >= 0)
            s = s.substring(0, index + 1);
        if (s.endsWith("("))
            s = s.substring(0, s.length() - 1);
        return s;
    }
}
