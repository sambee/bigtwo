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

import com.threerings.parlor.card.data.Card;
import com.threerings.parlor.card.data.CardCodes;
import com.threerings.parlor.card.data.CardGameObject;
import com.threerings.parlor.card.data.Hand;
import com.threerings.parlor.turn.data.TurnGameObject;
import com.threerings.util.Name;
import java.util.Comparator;

/**
 * Maintains the shared state of the game.
 */
public class BigTwoObject extends CardGameObject implements TurnGameObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>lowCard</code> field. */
    public static final String LOW_CARD = "lowCard";

    /** The field name of the <code>topHand</code> field. */
    public static final String TOP_HAND = "topHand";

    /** The field name of the <code>turnHolder</code> field. */
    public static final String TURN_HOLDER = "turnHolder";

    /** The field name of the <code>owner</code> field. */
    public static final String OWNER = "owner";

    /** The field name of the <code>cardCounts</code> field. */
    public static final String CARD_COUNTS = "cardCounts";
    // AUTO-GENERATED: FIELDS END

    private static final BigTwoHandComparator HAND_COMPARATOR = new BigTwoHandComparator();
    private static final Card BIG_TWO = new Card(2, CardCodes.SPADES);

    public Card lowCard;
    public Hand topHand;
    public Name turnHolder;
    public int owner = -1;
    public int[] cardCounts = { 13, 13, 13, 13 };

    public String getTurnHolderFieldName()
    {
        return TURN_HOLDER;
    }

    public Name getTurnHolder()
    {
        return turnHolder;
    }

    public Name[] getPlayers ()
    {
        return players;
    }

    public int getAdjacentPlayer(int pidx, int offset)
    {
        pidx = (pidx + offset) % players.length;
        if (pidx < 0)
            pidx += players.length;
        return pidx;
    }

    public int getTurnHolderIndex(int offset)
    {
        return getAdjacentPlayer(getPlayerIndex(turnHolder), offset);
    }

    /**
     * Returns true if it should be obvious to all players that the top hand is unbeatable.
     * If the 2 of spades has already been played we don't look at other cards because
     * keeping track of the played cards is part of the skill of Big Two and we don't
     * want auto-skipping to give information to lazy players.
     */
    public boolean isUnbeatable(int handSize)
    {
        if (topHand == null)
            return false;
        if (topHand.size() > handSize)
            return true;
        return topHand.size() <= 3 && isBigTwo();
    }

    /**
     * TODO
     */
    public boolean isBigTwo()
    {
        return topHand.contains(BIG_TWO);
    }

    /**
     * TODO
     */
    public boolean isPlayable(Hand hand)
    {
        if (hand == null)
            return false;
        if (topHand != null) {
            if (hand.size() != topHand.size())
                return false;
        } else if (owner < 0) {
            if (!hand.contains(lowCard))
                return false;
        }
        return HAND_COMPARATOR.compare(hand, topHand) > 0;
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>lowCard</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setLowCard (Card value)
    {
        Card ovalue = this.lowCard;
        requestAttributeChange(
            LOW_CARD, value, ovalue);
        this.lowCard = value;
    }

    /**
     * Requests that the <code>topHand</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setTopHand (Hand value)
    {
        Hand ovalue = this.topHand;
        requestAttributeChange(
            TOP_HAND, value, ovalue);
        this.topHand = value;
    }

    /**
     * Requests that the <code>turnHolder</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setTurnHolder (Name value)
    {
        Name ovalue = this.turnHolder;
        requestAttributeChange(
            TURN_HOLDER, value, ovalue);
        this.turnHolder = value;
    }

    /**
     * Requests that the <code>owner</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setOwner (int value)
    {
        int ovalue = this.owner;
        requestAttributeChange(
            OWNER, Integer.valueOf(value), Integer.valueOf(ovalue));
        this.owner = value;
    }

    /**
     * Requests that the <code>cardCounts</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setCardCounts (int[] value)
    {
        int[] ovalue = this.cardCounts;
        requestAttributeChange(
            CARD_COUNTS, value, ovalue);
        this.cardCounts = (value == null) ? null : (int[])value.clone();
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>cardCounts</code> field be set to the specified value.
     * The local value will be updated immediately and an event will be
     * propagated through the system to notify all listeners that the
     * attribute did change. Proxied copies of this object (on clients)
     * will apply the value change when they received the attribute
     * changed notification.
     */
    public void setCardCountsAt (int value, int index)
    {
        int ovalue = this.cardCounts[index];
        requestElementUpdate(
            CARD_COUNTS, index, Integer.valueOf(value), Integer.valueOf(ovalue));
        this.cardCounts[index] = value;
    }
    // AUTO-GENERATED: METHODS END
}

