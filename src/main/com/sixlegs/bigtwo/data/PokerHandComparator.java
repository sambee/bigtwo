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

import java.util.*;
import com.threerings.parlor.card.data.*;

public class PokerHandComparator
implements Comparator<Hand>
{
    private PokerHandEvaluator eval;
    private Comparator<Card> cmp;

    public PokerHandComparator()
    {
        this(new PokerHandEvaluator());
    }
    
    public PokerHandComparator(PokerHandEvaluator eval)
    {
        this.eval = eval;
        cmp = eval.getCardComparator();
    }

    // TODO: error checking (different sizes, etc.)
    public int compare(Hand hand1, Hand hand2)
    {
        Card[] compare1 = new Card[5];
        Card[] compare2 = new Card[5];
        int type1 = eval.evaluate(hand1, compare1);
        int type2 = eval.evaluate(hand2, compare2);
        return compare(type1, type2, compare1, compare2);
    }

    protected int compare(int type1, int type2, Card[] compare1, Card[] compare2)
    {
        int c = type1 - type2;
        if (c != 0)
            return c;
        if (type1 == PokerCodes.INVALID_HAND)
            return 0;
        for (int i = 0; i < 5; i++) {
            if (compare1[i] == null)
                break;
            c = cmp.compare(compare1[i], compare2[i]);
            if (c != 0)
                return c;
        }
        return 0;
    }
}
