/*
 * DirectoryCacheEntry.java
 *
 * Copyright (C) 2002 Peter Graves
 * $Id: DirectoryCacheEntry.java,v 1.2 2002/11/30 17:07:00 piso Exp $
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

public final class DirectoryCacheEntry
{
    private final File file;
    private final String listing;
    private final long when;

    public DirectoryCacheEntry(File file, String listing, long when)
    {
        this.file = file;
        this.listing = listing;
        this.when = when;
    }

    public final File getFile()
    {
        return file;
    }

    public final String getListing()
    {
        return listing;
    }

    public final long getWhen()
    {
        return when;
    }
    
    public final String toString()
    {
        return "DirectoryCacheEntry for " + file;
    }
}
