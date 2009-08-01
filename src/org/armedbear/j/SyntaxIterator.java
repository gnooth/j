/*
 * SyntaxIterator.java
 *
 * Copyright (C) 1998-2002 Peter Graves
 * $Id: SyntaxIterator.java,v 1.1.1.1 2002/09/24 16:07:44 piso Exp $
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

// Supports movement through the syntactically important text of a buffer, i.e.
// skipping whitespace and comments.
public interface SyntaxIterator
{
     public static final char DONE = '\uFFFF';

     public Position getPosition();

     public Line getLine();

     public char nextChar();

     public char prevChar();

     public char[] hideSyntacticWhitespace(Line line);

     public char[] hideSyntacticWhitespace(String s);
}
