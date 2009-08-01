/*
 * AndTerm.java
 *
 * Copyright (C) 2002 Peter Graves
 * $Id: AndTerm.java,v 1.1.1.1 2002/09/24 16:09:44 piso Exp $
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

import java.util.ArrayList;

public final class AndTerm extends MailboxFilter
{
    private final ArrayList filters;

    public AndTerm(MailboxFilter first, MailboxFilter second)
    {
        filters = new ArrayList();
        filters.add(first);
        filters.add(second);
    }

    public void add(MailboxFilter filter)
    {
        filters.add(filter);
    }

    public final boolean accept(MailboxEntry entry)
    {
        for (int i = 0; i < filters.size(); i++) {
            MailboxFilter filter = (MailboxFilter) filters.get(i);
            if (!filter.accept(entry))
                return false;
        }
        return true;
    }
}
