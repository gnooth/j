/*
 * room.java
 *
 * Copyright (C) 2003-2005 Peter Graves
 * $Id: room.java,v 1.10 2007/02/23 21:17:36 piso Exp $
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

package org.armedbear.lisp;

// ### room
public final class room extends Primitive
{
    private room()
    {
        super("room", "&optional x");
    }

    public LispObject execute(LispObject[] args) throws ConditionThrowable
    {
        if (args.length > 1)
            return error(new WrongNumberOfArgumentsException(this));
        Runtime runtime = Runtime.getRuntime();
        long total = 0;
        long free = 0;
        long maxFree = 0;
        while (true) {
            try {
                runtime.gc();
                Thread.sleep(100);
                runtime.runFinalization();
                Thread.sleep(100);
                runtime.gc();
                Thread.sleep(100);
            }
            catch (InterruptedException e) {}
            total = runtime.totalMemory();
            free = runtime.freeMemory();
            if (free > maxFree)
                maxFree = free;
            else
                break;
        }
        long used = total - free;
        Stream out = getStandardOutput();
        StringBuffer sb = new StringBuffer("Total memory ");
        sb.append(total);
        sb.append(" bytes");
        sb.append(System.getProperty("line.separator"));
        sb.append(used);
        sb.append(" bytes used");
        sb.append(System.getProperty("line.separator"));
        sb.append(free);
        sb.append(" bytes free");
        sb.append(System.getProperty("line.separator"));
        out._writeString(sb.toString());
        out._finishOutput();
        return number(used);
    }

    private static final Primitive ROOM = new room();
}
