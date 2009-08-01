/*
 * FileLine.java
 *
 * Copyright (C) 1998-2002 Peter Graves
 * $Id: FileLine.java,v 1.2 2002/12/11 01:23:19 piso Exp $
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

public final class FileLine extends TextLine
{
    private boolean visited;

    public FileLine(File file, boolean listEachOccurrence)
    {
        super(listEachOccurrence ? "File: " + file.canonicalPath() : file.canonicalPath());
    }

    public FileLine(String fileName)
    {
        super("File: " + fileName);
    }

    public final String getCanonicalPath()
    {
        String text = getText();
        if (text.startsWith("File: "))
            return text.substring(6);
        else
            return text;
    }

    public final void markVisited()
    {
        visited = true;
    }

    public final boolean visited()
    {
        return visited;
    }
}
