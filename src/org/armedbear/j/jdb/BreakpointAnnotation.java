/*
 * BreakpointAnnotation.java
 *
 * Copyright (C) 2002-2003 Peter Graves
 * $Id: BreakpointAnnotation.java,v 1.2 2003/05/18 01:26:27 piso Exp $
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

package org.armedbear.j.jdb;

import org.armedbear.j.Annotation;

public final class BreakpointAnnotation extends Annotation
{
    public BreakpointAnnotation(ResolvableBreakpoint bp)
    {
        super(bp, bp.isResolved() ? (char) 0x2022 : (char) 0x25e6);
    }

    public ResolvableBreakpoint getBreakpoint()
    {
        return (ResolvableBreakpoint) getUserObject();
    }
}
