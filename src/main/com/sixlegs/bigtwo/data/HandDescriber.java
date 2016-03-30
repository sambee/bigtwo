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
import com.threerings.util.MessageBundle;

public class HandDescriber
{
    private MessageBundle msgs;
    private PokerHandEvaluator eval;
    
    public HandDescriber(MessageBundle msgs, PokerHandEvaluator eval)
    {
        this.msgs = msgs;
        this.eval = eval;
    }

    public String describe(Hand hand)
    {
        Card[] compare = new Card[5];
        switch (eval.evaluate(hand, compare)) {
        case PokerCodes.STRAIGHT_FLUSH:
            return msgs.get("m.straight_flush", singular(compare[0]));
        case PokerCodes.FOUR_OF_A_KIND:
            return msgs.get("m.four_of_a_kind", plural(compare[0]));
        case PokerCodes.FULL_HOUSE:
            return msgs.get("m.full_house", plural(compare[0]), plural(compare[1]));
        case PokerCodes.FLUSH:
            return msgs.get("m.flush", singular(compare[0]), suit(compare[0], false));
        case PokerCodes.STRAIGHT:
            return msgs.get("m.straight", singular(compare[0]));
        case PokerCodes.THREE_OF_A_KIND:
            return msgs.get("m.three_of_a_kind", plural(compare[0]));
        case PokerCodes.TWO_PAIR:
            return msgs.get("m.two_pair", plural(compare[0]), plural(compare[1]));
        case PokerCodes.PAIR:
            return msgs.get("m.pair", plural(compare[0]));
        case PokerCodes.HIGH_CARD:
            return msgs.get("m.high_card", singular(compare[0]), suit(compare[0], true));
        default:
            return msgs.get("m.invalid_hand");
        }
    }

    private String singular(Card card)
    {
        return msgs.get("m.card_" + card.toString().charAt(0));
    }

    private String plural(Card card)
    {
        return msgs.get("m.cards_" + card.toString().charAt(0));
    }

    private String suit(Card card, boolean plural)
    {
        switch (card.getSuit()) {
        case CardCodes.SPADES:
            return msgs.get(plural ? "m.spades" : "m.spade");
        case CardCodes.HEARTS:
            return msgs.get(plural ? "m.hearts" : "m.heart");
        case CardCodes.CLUBS:
            return msgs.get(plural ? "m.clubs" : "m.club");
        case CardCodes.DIAMONDS:
            return msgs.get(plural ? "m.diamonds" : "m.diamond");
        }
        return null;
    }
}
