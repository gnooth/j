/*
 * SymbolHashTable.java
 *
 * Copyright (C) 2004-2005 Peter Graves
 * $Id: SymbolHashTable.java,v 1.3 2005/11/07 02:29:50 piso Exp $
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

import java.util.ArrayList;
import java.util.List;

public final class SymbolHashTable
{
    private static final float LOAD_FACTOR = 0.75f;

    private int threshold;
    private HashEntry[] buckets;
    private int count;

    private int mask;

    public SymbolHashTable(int size)
    {
        buckets = new HashEntry[calculateInitialCapacity(size)];
        threshold = (int) (size * LOAD_FACTOR);
        mask = buckets.length - 1;
    }

    private static int calculateInitialCapacity(int size)
    {
        int capacity = 1;
        while (capacity < size)
            capacity <<= 1;
        return capacity;
    }

    public Symbol get(SimpleString key)
    {
        HashEntry e = buckets[key.sxhash() & mask];
        while (e != null) {
            try {
                if (key.equal(e.symbol.name))
                    return e.symbol; // Return the symbol.
            }
            catch (Throwable t) {
                Debug.trace(t); // Shouldn't happen.
            }
            e = e.next;
        }
        return null;
    }

    public Symbol get(SimpleString key, int hash)
    {
        HashEntry e = buckets[hash & mask];
        while (e != null) {
            try {
                if (key.equal(e.symbol.name))
                    return e.symbol; // Return the symbol.
            }
            catch (Throwable t) {
                Debug.trace(t); // Shouldn't happen.
            }
            e = e.next;
        }
        return null;
    }

    public void put(final SimpleString key, final Symbol symbol)
    {
        int index = key.sxhash() & mask;
        HashEntry e = buckets[index];
        while (e != null) {
            try {
                if (key.equal(e.symbol.name)) {
                    if (e.symbol != symbol) {
                        Debug.trace("replacing existing key for " + key.getStringValue() +
                                    " in package " + e.symbol.getPackage().writeToString());
                        Thread.dumpStack();
                        e.symbol = symbol;
                    }
                    return;
                }
            }
            catch (Throwable t) {
                Debug.trace(t); // FIXME
            }
            e = e.next;
        }
        // Not found. We need to add a new entry.
        if (++count > threshold) {
            rehash();
            // We need a new index for the bigger table.
            index = key.sxhash() & mask;
        }
        e = new HashEntry(symbol);
        e.next = buckets[index];
        buckets[index] = e;
    }

    public void put(Symbol symbol)
    {
        int index = symbol.sxhash() & mask;
        HashEntry e = buckets[index];
        while (e != null) {
            try {
                if (symbol.name.equal(e.symbol.name)) {
                    if (e.symbol != symbol) {
                        Debug.trace("replacing existing key for " + symbol.getName());
                        Thread.dumpStack();
                        e.symbol = symbol; // Replace existing key.
                    }
                    return;
                }
            }
            catch (Throwable t) {
                Debug.trace(t); // FIXME
            }
            e = e.next;
        }
        // Not found. We need to add a new entry.
        if (++count > threshold) {
            rehash();
            // Need a new hash value to suit the bigger table.
            index = symbol.sxhash() & mask;
        }
        e = new HashEntry(symbol);
        e.next = buckets[index];
        buckets[index] = e;
    }

    public LispObject remove(LispObject key)
    {
        if (key instanceof Symbol)
            key = ((Symbol)key).name;
        int index = key.sxhash() & mask;
        HashEntry e = buckets[index];
        HashEntry last = null;
        while (e != null) {
            try {
                if (key.equal(e.symbol.name)) {
                    if (last == null)
                        buckets[index] = e.next;
                    else
                        last.next = e.next;
                    --count;
                    return e.symbol; // The key is the value!
                }
            }
            catch (Throwable t) {
                Debug.trace(t); // FIXME
            }
            last = e;
            e = e.next;
        }
        return null;
    }

    private void rehash()
    {
        HashEntry[] oldBuckets = buckets;
        int newCapacity = buckets.length * 2;
        threshold = (int) (newCapacity * LOAD_FACTOR);
        buckets = new HashEntry[newCapacity];
        mask = buckets.length - 1;
        for (int i = oldBuckets.length; i-- > 0;) {
            HashEntry e = oldBuckets[i];
            while (e != null) {
                final int index = e.symbol.sxhash() & mask;
                HashEntry dest = buckets[index];
                if (dest != null) {
                    while (dest.next != null)
                        dest = dest.next;
                    dest.next = e;
                } else
                    buckets[index] = e;
                HashEntry next = e.next;
                e.next = null;
                e = next;
            }
        }
    }

    public List getSymbols()
    {
        ArrayList list = new ArrayList();
        for (int i = 0; i < buckets.length; i++) {
            HashEntry e = buckets[i];
            while (e != null) {
                list.add(e.symbol);
                e = e.next;
            }
        }
        return list;
    }

    private static class HashEntry
    {
        Symbol symbol;
        HashEntry next;

        HashEntry(Symbol symbol)
        {
            this.symbol = symbol;
        }
    }
}
