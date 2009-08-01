/*
 * StaticTextField.java
 *
 * Copyright (C) 1998-2003 Peter Graves
 * $Id: StaticTextField.java,v 1.2 2003/07/24 15:17:26 piso Exp $
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

import java.awt.Graphics;
import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.UIManager;

public final class StaticTextField extends JTextField
{
    public StaticTextField()
    {
        super();
        init();
    }

    public StaticTextField(int columns)
    {
        super(columns);
        init();
    }

    public StaticTextField(String text)
    {
        super(text);
        init();
    }

    public StaticTextField(String text, int columns)
    {
        super(text, columns);
        init();
    }

    private final void init()
    {
        setAlignmentX(LEFT_ALIGNMENT);
        setEditable(false);
        setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 1));
        setBackground(UIManager.getColor("control"));
    }

    public boolean isFocusTraversable()
    {
        return false;
    }

    public void paintComponent(Graphics g)
    {
        Display.setRenderingHints(g);
        super.paintComponent(g);
    }
}
