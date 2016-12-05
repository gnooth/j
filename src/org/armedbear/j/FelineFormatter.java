/*
 * FelineFormatter.java
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

public final class FelineFormatter extends Formatter implements Constants
{
  private static final int FELINE_STATE_NEUTRAL         = STATE_NEUTRAL;
  private static final int FELINE_STATE_LINE_COMMENT    = STATE_LAST + 1;
  private static final int FELINE_STATE_BLOCK_COMMENT   = STATE_COMMENT;
  private static final int FELINE_STATE_PAREN_COMMENT   = STATE_LAST + 3;
  private static final int FELINE_STATE_AFTER_COLON     = STATE_LAST + 4;
  private static final int FELINE_STATE_NAME            = STATE_LAST + 5;
  private static final int FELINE_STATE_QUOTE           = STATE_QUOTE;
  private static final int FELINE_STATE_WORD            = STATE_LAST + 7;

  private static final int FELINE_FORMAT_TEXT           = 0;
  private static final int FELINE_FORMAT_COMMENT        = 1;
  private static final int FELINE_FORMAT_NAME           = 2;
  private static final int FELINE_FORMAT_BRACE          = 3;
  private static final int FELINE_FORMAT_STRING         = 4;
  private static final int FELINE_FORMAT_KEYWORD        = 5;

  public FelineFormatter(Buffer buffer)
  {
    this.buffer = buffer;
  }

  private static final boolean isDefiner(String s)
  {
    if (s.equals(":"))
      return true;
    if (s.length() > 4 && s.endsWith(":")) {
      if (s.equals("test:"))
        return true;
      if (s.equals("help:"))
        return true;
      if (s.equals("global:"))
        return true;
      if (s.equals("constant:"))
        return true;
    }
    return false;
  }

  public LineSegmentList formatLine(Line line)
  {
    clearSegmentList();
    final String text = getDetabbedText(line);
    final int limit = text.length();
    int state = line.flags();
    if (limit > 0)
      {
        int start = 0;
        int i = 0;
        while (i < limit)
          {
            char c = text.charAt(i);
            if (state == FELINE_STATE_QUOTE)
              {
                if (c == '"')
                  {
                    addSegment(text, start, i+1, FELINE_FORMAT_STRING);
                    state = FELINE_STATE_NEUTRAL;
                    start = i + 1;
                  }
                ++i;
                continue;
              }
            if (state == FELINE_STATE_AFTER_COLON)
              {
                if (c != ' ')
                  {
                    if (i > start)
                      addSegment(text, start, i, FELINE_FORMAT_TEXT);
                    state = FELINE_STATE_NAME;
                    start = i;
                  }
                ++i;
                continue;
              }
            if (state == FELINE_STATE_NAME)
              {
                if (c == ' ')
                  {
                    if (i > start)
                      addSegment(text, start, i, FELINE_FORMAT_NAME);
                    state = FELINE_STATE_NEUTRAL;
                    start = i;
                  }
                ++i;
                continue;
              }
            if (state == FELINE_STATE_PAREN_COMMENT)
              {
                if (c == ')')
                  {
                    addSegment(text, start, i+1, FELINE_FORMAT_COMMENT);
                    state = FELINE_STATE_NEUTRAL;
                    start = i + 1;
                  }
                ++i;
                continue;
              }
            if (state == FELINE_STATE_BLOCK_COMMENT)
              {
                if (c == '-' && i < limit - 1 && text.charAt(i+1) == '}')
                  {
                    if (i > start)
                      addSegment(text, start, i, FELINE_FORMAT_COMMENT);
                    addSegment(text, i, i+2, FELINE_FORMAT_COMMENT);
                    state = FELINE_STATE_NEUTRAL;
                    start = i + 2;
                    i += 2;
                    continue;
                  }
                else
                  {
                    ++i;
                    continue;
                  }
              }
            if (state == FELINE_STATE_WORD && c == ' ')
              {
                if (i > start)
                  {
                    String word = text.substring(start, i);
                    if (isDefiner(word))
                      {
                        addSegment(text, start, i, FELINE_FORMAT_KEYWORD);
                        state = FELINE_STATE_AFTER_COLON;
                        start = i;
                        ++i;
                        continue;
                      }
                    else if (isKeyword(word))
                      addSegment(text, start, i, FELINE_FORMAT_KEYWORD);
                    else
                      addSegment(text, start, i, FELINE_FORMAT_TEXT);
                    state = FELINE_STATE_NEUTRAL;
                    start = i;
                  }
                ++i;
                continue;
              }
            if (state == FELINE_STATE_NEUTRAL)
              {
                if (c == '"')
                  {
                    if (i > start)
                      addSegment(text, start, i, FELINE_FORMAT_TEXT);
                    state = FELINE_STATE_QUOTE;
                    start = i;
                    ++i;
                    continue;
                  }
                if (c == '(')
                  {
                    if (i > 0 && text.charAt(i-1) > ' ')
                      {
                        ++i;
                        continue;
                      }
                    if (i >= limit-1 || text.charAt(i+1) > ' ')
                      {
                        ++i;
                        continue;
                      }
                    if (i > start)
                      addSegment(text, start, i, FELINE_FORMAT_TEXT);
                    state = FELINE_STATE_PAREN_COMMENT;
                    start = i;
                    ++i;
                    if (i < limit)
                      ++i;
                    continue;
                  }
                if (c == '-')
                  {
                    if (i < limit - 1 && text.charAt(i+1) == '-')
                      {
                        if (i > start)
                          addSegment(text, start, i, FELINE_FORMAT_TEXT);
                        addSegment(text, i, FELINE_FORMAT_COMMENT);
                        return segmentList;
                      }
                  }
                if (c == '{')
                  {
                    if (i < limit - 1 && text.charAt(i+1) == '-')
                      {
                        if (i > start)
                          addSegment(text, start, i, FELINE_FORMAT_TEXT);
                        state = FELINE_STATE_BLOCK_COMMENT;
                        start = i;
                        i += 2;
                      }
                    else
                      ++i;
                    continue;
                  }
                if (c != ' ')
                  {
                    if (i > start)
                      addSegment(text, start, i, FELINE_FORMAT_TEXT);
                    state = FELINE_STATE_WORD;
                    start = i;
                    ++i;
                    continue;
                  }

              }
            ++i;
          }
        int format = FELINE_FORMAT_TEXT;
        if (state == FELINE_STATE_LINE_COMMENT
            || state == FELINE_STATE_BLOCK_COMMENT
            || state == FELINE_STATE_PAREN_COMMENT)
          format = FELINE_FORMAT_COMMENT;
        else if (state == FELINE_STATE_NAME)
          format = FELINE_FORMAT_NAME;
        else if (state == FELINE_STATE_QUOTE)
          format = FELINE_FORMAT_STRING;
        else if (state == FELINE_STATE_WORD)
          {
            String word = text.substring(start);
            if (isKeyword(word))
              format = FELINE_FORMAT_KEYWORD;
          }
        addSegment(text, start, format);
      }
    return segmentList;
  }

  public boolean parseBuffer()
  {
    int state = FELINE_STATE_NEUTRAL;
    Line line = buffer.getFirstLine();
    boolean changed = false;
    while (line != null)
      {
        if (state != line.flags())
          {
            line.setFlags(state);
            changed = true;
          }
        final String text = line.getText();
        final int limit = line.length();
        int i = 0;
        while (i < limit)
          {
            char c = text.charAt(i);
            if (state == FELINE_STATE_BLOCK_COMMENT)
              {
                if (c == '-' && text.regionMatches(i, "-}", 0, 2))
                  {
                    state = FELINE_STATE_NEUTRAL;
                    i += 2;
                  } else
                    ++i;
                continue;
              }
            if (state == FELINE_STATE_QUOTE)
              {
                if (c == '"')
                  state = FELINE_STATE_NEUTRAL;
                ++i;
                continue;
              }
            if (state == FELINE_STATE_NEUTRAL)
              {
                if (c == '{' && text.regionMatches(i, "{-", 0, 2))
                  {
                    state = FELINE_STATE_BLOCK_COMMENT;
                    i += 2;
                    continue;
                  }
                if (c == '-' && text.regionMatches(i, "--", 0, 2))
                  break;
                if (c == '"')
                  {
                    state = FELINE_STATE_QUOTE;
                    ++i;
                    continue;
                  }
              }
            ++i;
          }
        line = line.next();
      }
    buffer.setNeedsParsing(false);
    return changed;
  }

  public FormatTable getFormatTable()
  {
    if (formatTable == null)
      {
        formatTable = new FormatTable(null);
        formatTable.addEntryFromPrefs(FELINE_FORMAT_TEXT, "text");
        formatTable.addEntryFromPrefs(FELINE_FORMAT_COMMENT, "comment");
        formatTable.addEntryFromPrefs(FELINE_FORMAT_BRACE, "brace");
        formatTable.addEntryFromPrefs(FELINE_FORMAT_NAME, "function");
        formatTable.addEntryFromPrefs(FELINE_FORMAT_STRING, "string");
        formatTable.addEntryFromPrefs(FELINE_FORMAT_KEYWORD, "keyword");
      }
    return formatTable;
  }
}
