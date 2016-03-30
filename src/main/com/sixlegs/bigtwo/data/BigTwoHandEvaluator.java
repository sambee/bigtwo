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

public class BigTwoHandEvaluator
extends PokerHandEvaluator
{
    public BigTwoHandEvaluator()
    {
        super(new BigTwoCardComparator(true));
    }

    @Override public int evaluate(List<Card> hand, Card[] compare)
    {
        int type = super.evaluate(hand, compare);
        if (hand == null || hand.size() != getExpectedSize(type))
            return PokerCodes.INVALID_HAND;
        return type;
    }

    @Override public boolean isStraight(List<Card> hand)
    {
        if (hand.size() != 5)
            return false;
        for (int i = 1; i < 5; i++) {
            int n1 = hand.get(i - 1).getNumber();
            int n2 = hand.get(i).getNumber();
            if (remap2(n2) - remap2(n1) != 1)
                return false;
        }
        return true;
    }

    private static int remap2(int number)
    {
        return (number == 2) ? (CardCodes.ACE + 1) : number;
    }

    private static final int getExpectedSize(int type)
    {
        switch (type) {
        case PokerCodes.STRAIGHT_FLUSH:
        case PokerCodes.FOUR_OF_A_KIND:
        case PokerCodes.FULL_HOUSE:
        case PokerCodes.FLUSH:
        case PokerCodes.STRAIGHT:
            return 5;
        case PokerCodes.THREE_OF_A_KIND:
            return 3;
        case PokerCodes.PAIR:
            return 2;
        case PokerCodes.HIGH_CARD:
            return 1;
        default:
            return 0;
        }
    }
}
