/*
 * FromMailboxFilter.java
 *
 * Copyright (C) 2002 Peter Graves
 * $Id: FromMailboxFilter.java,v 1.1.1.1 2002/09/24 16:10:01 piso Exp $
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

package org.armedbear.j.mail;

import java.util.List;
import org.armedbear.j.FastStringReader;
import org.armedbear.j.Utilities;

public final class FromMailboxFilter extends MailboxFilter
{
    private String pattern;
    private boolean ignoreCase;

    public FromMailboxFilter(FastStringReader reader)
    {
        this.pattern = reader.readToken();
        ignoreCase = Utilities.isLowerCase(pattern);
    }

    public boolean accept(MailboxEntry entry)
    {
        MailAddress[] from = entry.getFrom();
        if (from != null) {
            if (ignoreCase) {
                for (int i = from.length-1; i >= 0; i--) {
                    if (from[i].matchesIgnoreCase(pattern))
                        return true;
                }
            } else {
                for (int i = from.length-1; i >= 0; i--) {
                    if (from[i].matches(pattern))
                        return true;
                }
            }
        }
        return false;
    }
}
