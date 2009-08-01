/*
 * OutputBuffer.java
 *
 * Copyright (C) 2000-2003 Peter Graves
 * $Id: OutputBuffer.java,v 1.5 2005/11/18 19:07:18 piso Exp $
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

public final class OutputBuffer extends Buffer
{
  private OutputBuffer()
  {
    supportsUndo  = false;
    type = TYPE_OUTPUT;
    mode = PlainTextMode.getMode();
    formatter = new PlainTextFormatter(this);
    lineSeparator = System.getProperty("line.separator");
    readOnly = true;
    setTransient(true);
    setProperty(Property.VERTICAL_RULE, 0);
    setProperty(Property.SHOW_LINE_NUMBERS, false);
    setProperty(Property.HIGHLIGHT_MATCHING_BRACKET, false);
    setProperty(Property.HIGHLIGHT_BRACKETS, false);
    setInitialized(true);
  }

  public static OutputBuffer getOutputBuffer(String text)
  {
    OutputBuffer outputBuffer = new OutputBuffer();
    outputBuffer.setText(text);
    return outputBuffer;
  }

  public int load()
  {
    if (!isLoaded())
      {
        if (getFirstLine() == null)
          {
            appendLine("");
            renumber();
          }
        setLoaded(true);
      }
    return LOAD_COMPLETED;
  }

  public String getFileNameForDisplay()
  {
    return title != null ? title : "";
  }
}
