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

public class PokerHandEvaluator
{
    private static final Card[] IGNORE = new Card[5];

    private CardComparator cmp;

    public PokerHandEvaluator()
    {
        this(new CardComparator());
    }

    protected PokerHandEvaluator(CardComparator cmp)
    {
        this.cmp = cmp;
    }

    public CardComparator getCardComparator()
    {
        return cmp;
    }

    public int evaluate(List<Card> hand, Card[] compare)
    {
        if (compare == null)
            compare = IGNORE;
        Arrays.fill(compare, null);
        if (hand == null)
            return PokerCodes.INVALID_HAND;
        int size = hand.size();
        if (size == 0 || size > 5)
            return PokerCodes.INVALID_HAND;

        List<Card> sorted = new ArrayList<Card>(hand);
        Collections.sort(sorted, cmp);

        List<Card> reversed = new ArrayList<Card>(sorted);
        Collections.reverse(reversed);
        Card[] cards = reversed.toArray(new Card[size]);

        boolean eq01 = equals(cards, 0, 1);
        boolean eq12 = equals(cards, 1, 2);
        boolean eq23 = equals(cards, 2, 3);
        boolean eq34 = equals(cards, 3, 4);
        boolean none = !eq01 && !eq12 && !eq23 && !eq34;

        if (none && isFlush(cards) && isStraight(sorted)) {
            compare[0] = cards[0];
            return PokerCodes.STRAIGHT_FLUSH;

        } else if (eq12 && eq23 && (eq01 || eq34)) {
            compare[0] = cards[2];
            compare[1] = (cards.length > 4) ? cards[eq01 ? 4 : 0] : null;
            return PokerCodes.FOUR_OF_A_KIND;

        } else if (eq01 && eq34 && (eq12 || eq23)) {
            compare[0] = cards[2];
            compare[1] = cards[eq12 ? 4 : 0];
            return PokerCodes.FULL_HOUSE;

        } else if (isFlush(cards)) {
            System.arraycopy(cards, 0, compare, 0, cards.length);
            return PokerCodes.FLUSH;

        } else if (none && size == 5 && isStraight(sorted)) {
            System.arraycopy(cards, 0, compare, 0, cards.length);
            return PokerCodes.STRAIGHT;

        } else if ((eq01 && eq12) || (eq12 && eq23) || (eq23 && eq34)) {
            compare[0] = cards[2];
            compare[1] = (cards.length > 3) ? cards[eq01 ? 3 : 0] : null;
            compare[2] = (cards.length > 4) ? cards[eq34 ? 1 : 4] : null;
            return PokerCodes.THREE_OF_A_KIND;

        } else if (eq12 && eq34) {
            return twoPairHelper(cards, compare, 1, 3, 0);
        } else if (eq01 && eq23) {
            return twoPairHelper(cards, compare, 0, 2, 4);
        } else if (eq01 && eq34) {
            return twoPairHelper(cards, compare, 0, 3, 2);

        } else if (eq01) {
            return pairHelper(cards, compare, 0, 2, 3, 4);
        } else if (eq12) {
            return pairHelper(cards, compare, 1, 0, 3, 4);
        } else if (eq23) {
            return pairHelper(cards, compare, 2, 0, 1, 4);
        } else if (eq34) {
            return pairHelper(cards, compare, 3, 0, 1, 2);

        } else {
            System.arraycopy(cards, 0, compare, 0, cards.length);
            return PokerCodes.HIGH_CARD;
        }
    }

    /**
     * Returns true if the hand is a straight. Most subclasses will need to
     * override this method, especially if a custom card comparator is used.
     * The default implementation returns true for standard Poker straights,
     * where Ace can be either high or low.
     * @param hand the hand to evaluate; <b>must be sorted</b> according to this
     * evaluator's card comparator
     * @return true if the hand is a straight
     */
    public boolean isStraight(List<Card> hand)
    {
        if (hand.size() != 5)
            return false;
        for (int i = 1; i < 5; i++) {
            int n1 = hand.get(i - 1).getNumber();
            int n2 = hand.get(i).getNumber();
            if (n2 - n1 != 1) {
                if (i == 4 && n1 == 5 && n2 == CardCodes.ACE)
                    continue;
                return false;
            }
        }
        return true;
    }

    private static int twoPairHelper(Card[] cards, Card[] compare, int i0, int i1, int i2)
    {
        boolean firstBigger = cards[i0].getNumber() > cards[i1].getNumber();
        compare[0] = cards[firstBigger ? i0 : i1];
        compare[1] = cards[firstBigger ? i1 : i0];
        compare[2] = (cards.length > i2) ? cards[i2] : null;
        return PokerCodes.TWO_PAIR;
    }

    private static int pairHelper(Card[] cards, Card[] compare, int i0, int i1, int i2, int i3)
    {
        compare[0] = cards[i0];
        compare[1] = (cards.length > i1) ? cards[i1] : null;
        compare[2] = (cards.length > i2) ? cards[i2] : null;
        compare[3] = (cards.length > i3) ? cards[i3] : null;
        return PokerCodes.PAIR;
    }

    private static boolean equals(Card[] cards, int i0, int i1)
    {
        int size = cards.length;
        if (i0 >= size || i1 >= size)
            return false;
        return cards[i0].getNumber() == cards[i1].getNumber();
    }

    private static boolean isFlush(Card[] cards)
    {
        if (cards.length != 5)
            return false;
        int suit = cards[0].getSuit();
        for (int i = 1; i < cards.length; i++) {
            if (cards[i].getSuit() != suit)
                return false;
        }
        return true;
    }
}
