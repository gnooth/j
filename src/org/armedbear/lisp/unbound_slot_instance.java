/*
 * unbound_slot_instance.java
 *
 * Copyright (C) 2004 Peter Graves
 * $Id: unbound_slot_instance.java,v 1.3 2007/02/23 21:17:36 piso Exp $
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

// ### unbound-slot-instance
public final class unbound_slot_instance extends Primitive
{
    private unbound_slot_instance()
    {
        super("unbound-slot-instance");
    }

    public LispObject execute(LispObject arg) throws ConditionThrowable
    {
        if (arg instanceof UnboundSlot)
            return ((UnboundSlot)arg).getInstance();
        return error(new TypeError(arg, Symbol.UNBOUND_SLOT));
    }

    private static final unbound_slot_instance CELL_ERROR_NAME =
        new unbound_slot_instance();
}
