/*
 * FelineSyntaxIterator.feline
 *
 * Copyright (C) 2016 Peter Graves <gnooth@gmail.com>
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.armedbear.j;

// Supports movement through the syntactically important text of a buffer, i.e.
// skipping whitespace and comments.
public final class FelineSyntaxIterator extends DefaultSyntaxIterator
    implements Constants
{
    public FelineSyntaxIterator(Position pos)
    {
        super(pos);
    }

    // Caller must make sure parseBuffer() has been called so flags will be
    // correct.
    public char[] hideSyntacticWhitespace(Line line)
    {
        if (line.flags() == STATE_COMMENT)
            return hideSyntacticWhitespace(line.getText(), STATE_COMMENT);
        if (line.flags() == STATE_QUOTE)
            return hideSyntacticWhitespace(line.getText(), STATE_QUOTE);
        return hideSyntacticWhitespace(line.getText(), STATE_NEUTRAL);
    }

    public char[] hideSyntacticWhitespace(String s)
    {
        return hideSyntacticWhitespace(s, STATE_NEUTRAL);
    }

    // Replaces comments with space characters and double-quoted strings with
    // 'X' characters.
    private char[] hideSyntacticWhitespace(String s, int initialState)
    {
      boolean paren = false;
      final char[] chars = s.toCharArray();
      final int length = chars.length;
      int state = initialState;
      for (int i = 0; i < length; i++)
        {
          char c = chars[i];

          if (c == '\\' && i < length-1)
            {
              // Escape character.
              chars[i++] = ' ';
              chars[i] = ' ';
              continue;
            }
          if (state == STATE_QUOTE)
            {
              chars[i] = 'X';
              if (c == '"')
                state = STATE_NEUTRAL;
              continue;
            }
          if (state == STATE_COMMENT)
            {
              if (paren)
                {
                  if (c == ')')
                    {
                      // ( ) comment ending
                      chars[i] = ' ';
                      state = STATE_NEUTRAL;
                    } else
                      chars[i] = ' ';
                  continue;
                }
              if (c == '-' && i < length-1 && chars[i+1] == '}')
                {
                  // {- -} comment ending
                  chars[i++] = ' ';
                  chars[i] = ' ';
                  state = STATE_NEUTRAL;
                } else
                  chars[i] = ' ';
              continue;
            }

          // reaching here, STATE_NEUTRAL...
          if (c == '"')
            {
              chars[i] = ' ';
              state = STATE_QUOTE;
              continue;
            }
          if (c == '(')
            {
              // ( ) comment starting
              chars[i] = ' ';
              state = STATE_COMMENT;
              paren = true;
              continue;
            }
          if (c == '{')
            {
              if (i < length-1)
                {
                  if (chars[i+1] == '-')
                    {
                      // {- -} comment starting
                      chars[i++] = ' ';
                      chars[i] = ' ';
                      state = STATE_COMMENT;
                      continue;
                    }
                  if (chars[i+1] == '/')
                    {
                      // "//" comment starting
                      for (int j = i; j < length; j++)
                        chars[j] = ' ';
                      return chars;
                    }
                }
            }
          if (c == '-')
            {
              if (i < length-1)
                {
                  if (chars[i+1] == '-')
                    {
                      // "--" comment starting
                      for (int j = i; j < length; j++)
                        chars[j] = ' ';
                      return chars;
                    }
                }
            }
        }
      return chars;
    }
}
