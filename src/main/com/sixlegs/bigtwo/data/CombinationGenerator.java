/*
Big Two for Game Gardens - a Chinese climbing-style card game
Copyright (C) 2005 Chris Nokleberg

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.
*/

package com.sixlegs.bigtwo.data;

import java.math.BigInteger;

class CombinationGenerator
{
    private int[] a;
    private int n;
    private int r;
    private int numLeft;
    private int total;

    public CombinationGenerator(int n, int r)
    {
        if (r > n)
            throw new IllegalArgumentException();
        if (n < 1)
            throw new IllegalArgumentException();
        this.n = n;
        this.r = r;
        a = new int[r];
        BigInteger nFact = getFactorial(n);
        BigInteger rFact = getFactorial(r);
        BigInteger nminusrFact = getFactorial(n - r);
        total = nFact.divide(rFact.multiply(nminusrFact)).intValue();
        reset();
    }

    private static BigInteger getFactorial(int n)
    {
        BigInteger fact = BigInteger.ONE;
        for (int i = n; i > 1; i--) {
            fact = fact.multiply(new BigInteger(Integer.toString(i)));
        }
        return fact;
    }

    public void reset()
    {
        for (int i = 0; i < a.length; i++)
            a[i] = i;
        numLeft = total;
    }

    public int getNumLeft()
    {
        return numLeft;
    }

    public boolean hasMore()
    {
        return numLeft > 0;
    }

    public int getTotal()
    {
        return total;
    }

    public int[] getNext()
    {
        if (numLeft == total) {
            numLeft--;
            return a;
        }

        int i = r - 1;
        while (a[i] == n - r + i) {
            i--;
        }
        a[i] = a[i] + 1;
        for (int j = i + 1; j < r; j++) {
            a[j] = a[i] + j - i;
        }

        numLeft--;
        return a;
    }
}
