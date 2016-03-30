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

import com.threerings.parlor.card.data.*;

public class BigTwoHandComparator
extends PokerHandComparator
{
    private static final BigTwoHandComparator INSTANCE = new BigTwoHandComparator();
    
    public BigTwoHandComparator()
    {
        super(new BigTwoHandEvaluator());
    }

    protected int compare(int type1, int type2, Card[] compare1, Card[] compare2)
    {
        if (type1 == type2) {
            switch (type1) {
            case PokerCodes.FLUSH:
                int c = compare2[0].getSuit() - compare1[0].getSuit();
                if (c != 0)
                    return c;
                break;
            case PokerCodes.STRAIGHT_FLUSH:
            case PokerCodes.PAIR:
            case PokerCodes.HIGH_CARD:
                if (compare2[0].getNumber() == compare1[0].getNumber())
                    return compare2[0].getSuit() - compare1[0].getSuit();
                break;
            }
        }
        return super.compare(type1, type2, compare1, compare2);
    }
}
