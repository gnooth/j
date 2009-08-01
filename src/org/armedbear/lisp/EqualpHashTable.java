/*
 * EqualpHashTable.java
 *
 * Copyright (C) 2004-2006 Peter Graves
 * $Id: EqualpHashTable.java,v 1.9 2006/01/12 03:52:40 piso Exp $
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

public final class EqualpHashTable extends HashTable
{
  public EqualpHashTable(int size, LispObject rehashSize,
                         LispObject rehashThreshold)
  {
    super(size, rehashSize, rehashThreshold);
  }

  public Symbol getTest()
  {
    return Symbol.EQUALP;
  }

  public LispObject get(LispObject key)
  {
    final int index = key.psxhash() % buckets.length;
    HashEntry e = buckets[index];
    while (e != null)
      {
        try
          {
            if (key.equalp(e.key))
              return e.value;
          }
        catch (ConditionThrowable t)
          {
            Debug.trace(t);
          }
        e = e.next;
      }
    return null;
  }

  public void put(LispObject key, LispObject value) throws ConditionThrowable
  {
    int index = key.psxhash() % buckets.length;
    HashEntry e = buckets[index];
    while (e != null)
      {
        if (key.equalp(e.key))
          {
            e.value = value;
            return;
          }
        e = e.next;
      }
    // Not found. We need to add a new entry.
    if (++count > threshold)
      {
        rehash();
        // Need a new hash value to suit the bigger table.
        index = key.psxhash() % buckets.length;
      }
    e = new HashEntry(key, value);
    e.next = buckets[index];
    buckets[index] = e;
  }

  public LispObject remove(LispObject key) throws ConditionThrowable
  {
    final int index = key.psxhash() % buckets.length;
    HashEntry e = buckets[index];
    HashEntry last = null;
    while (e != null)
      {
        if (key.equalp(e.key))
          {
            if (last == null)
              buckets[index] = e.next;
            else
              last.next = e.next;
            --count;
            return e.value;
          }
        last = e;
        e = e.next;
      }
    return null;
  }

  protected void rehash()
  {
    HashEntry[] oldBuckets = buckets;
    int newCapacity = buckets.length * 2 + 1;
    threshold = (int) (newCapacity * loadFactor);
    buckets = new HashEntry[newCapacity];
    for (int i = oldBuckets.length; i-- > 0;)
      {
        HashEntry e = oldBuckets[i];
        while (e != null)
          {
            final int index = e.key.psxhash() % buckets.length;
            HashEntry dest = buckets[index];
            if (dest != null)
              {
                while (dest.next != null)
                  dest = dest.next;
                dest.next = e;
              }
            else
              buckets[index] = e;
            HashEntry next = e.next;
            e.next = null;
            e = next;
          }
      }
  }
}
