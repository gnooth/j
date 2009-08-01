/*
 * DowncaseStream.java
 *
 * Copyright (C) 2004-2005 Peter Graves
 * $Id: DowncaseStream.java,v 1.2 2005/10/16 02:03:28 piso Exp $
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

public final class DowncaseStream extends CaseFrobStream
{
    public DowncaseStream(Stream target) throws ConditionThrowable
    {
        super(target);
    }

    public void _writeChar(char c) throws ConditionThrowable
    {
        target._writeChar(LispCharacter.toLowerCase(c));
    }

    public void _writeString(String s) throws ConditionThrowable
    {
        target._writeString(s.toLowerCase());
    }

    public void _writeLine(String s) throws ConditionThrowable
    {
        target._writeLine(s.toLowerCase());
    }
}
