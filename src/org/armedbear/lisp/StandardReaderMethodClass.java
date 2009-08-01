/*
 * StandardReaderMethodClass.java
 *
 * Copyright (C) 2005 Peter Graves
 * $Id: StandardReaderMethodClass.java,v 1.1 2005/12/27 19:02:37 piso Exp $
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

public final class StandardReaderMethodClass extends StandardClass
{
  // From StandardMethodClass.java:
  public static final int SLOT_INDEX_GENERIC_FUNCTION = 0;
  public static final int SLOT_INDEX_LAMBDA_LIST      = 1;
  public static final int SLOT_INDEX_SPECIALIZERS     = 2;
  public static final int SLOT_INDEX_QUALIFIERS       = 3;
  public static final int SLOT_INDEX_FUNCTION         = 4;
  public static final int SLOT_INDEX_FAST_FUNCTION    = 5;
  public static final int SLOT_INDEX_DOCUMENTATION    = 6;

  // Added:
  public static final int SLOT_INDEX_SLOT_NAME        = 7;

  public StandardReaderMethodClass()
  {
    super(Symbol.STANDARD_READER_METHOD,
          list1(StandardClass.STANDARD_READER_METHOD));
    Package pkg = PACKAGE_SYS;
    LispObject[] instanceSlotNames =
      {
        Symbol.GENERIC_FUNCTION,
        pkg.intern("LAMBDA-LIST"),
        pkg.intern("SPECIALIZERS"),
        pkg.intern("QUALIFIERS"),
        Symbol.FUNCTION,
        pkg.intern("FAST-FUNCTION"),
        Symbol.DOCUMENTATION,
        pkg.intern("SLOT-NAME")
      };
    setClassLayout(new Layout(this, instanceSlotNames, NIL));
    setFinalized(true);
  }

  public LispObject allocateInstance()
  {
    return new StandardReaderMethod();
  }
}
