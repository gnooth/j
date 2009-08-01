/*
 * UndoBoundary.java
 *
 * Copyright (C) 2003 Peter Graves
 * $Id: UndoBoundary.java,v 1.2 2003/08/01 16:21:01 piso Exp $
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

import javax.swing.undo.AbstractUndoableEdit;

// A marker class.
public final class UndoBoundary extends AbstractUndoableEdit
{
    private static final UndoBoundary instance = new UndoBoundary();

    private UndoBoundary()
    {
    }

    public static final UndoBoundary getInstance()
    {
        return instance;
    }
}
