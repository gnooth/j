/*
 * MailboxLine.java
 *
 * Copyright (C) 1998-2002 Peter Graves
 * $Id: MailboxLine.java,v 1.1.1.1 2002/09/24 16:09:46 piso Exp $
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

import org.armedbear.j.Line;
import org.armedbear.j.TextLine;

public final class MailboxLine extends TextLine implements Line
{
    private final MailboxEntry entry;
    private final int depth;

    public MailboxLine(MailboxEntry entry)
    {
        super(entry.toString());
        this.entry = entry;
        this.depth = 1;
    }

    public MailboxLine(MailboxEntry entry, int depth)
    {
        super(entry.toString(depth));
        this.entry = entry;
        this.depth = depth;
    }

    public final MailboxEntry getMailboxEntry()
    {
        return entry;
    }

    public final int getDepth()
    {
        return depth;
    }
}
