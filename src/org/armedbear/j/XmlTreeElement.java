/*
 * XmlTreeElement.java
 *
 * Copyright (C) 2000-2002 Peter Graves
 * $Id: XmlTreeElement.java,v 1.2 2003/06/04 00:09:58 piso Exp $
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

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

public final class XmlTreeElement
{
    private final String name;
    private final Attributes attributes;
    private final int lineNumber;
    private final int columnNumber;

    public XmlTreeElement(String name, Attributes attributes, int lineNumber,
        int columnNumber)
    {
        this.name = name;
        // Must copy attributes!
        this.attributes = new AttributesImpl(attributes);
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    public final String getName()
    {
        return name;
    }

    // This is used for the text in the sidebar tree.
    public String toString()
    {
        return getStatusText();
    }

    public String getStatusText()
    {
        FastStringBuffer sb = new FastStringBuffer(name);
        for (int i = 0; i < attributes.getLength(); i++)
            appendNameAndValue(sb, attributes.getQName(i), attributes.getValue(i));
        return sb.toString();
    }

    private void appendNameAndValue(FastStringBuffer sb, String name, String value)
    {
        sb.append(' ');
        sb.append(name);
        sb.append("=");
        final char quoteChar = value.indexOf('"') < 0 ? '"' : '\'';
        sb.append(quoteChar);
        sb.append(value);
        sb.append(quoteChar);
    }

    public final int getLineNumber()
    {
        return lineNumber;
    }

    public final int getColumnNumber()
    {
        return columnNumber;
    }
}
