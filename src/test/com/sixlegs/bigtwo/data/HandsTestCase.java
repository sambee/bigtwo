package com.sixlegs.bigtwo.data;

import com.threerings.parlor.card.data.*;
import java.util.StringTokenizer;
import junit.framework.*;

// TODO: support testmethods
abstract public class HandsTestCase
extends TestCase
implements PokerCodes
{
    protected HandsTestCase(String name)
    {
        super(name);
    }

    protected Hand parseHand(String str, int expectType)
    {
        Hand hand = parseHand(str);
        Assert.assertEquals(expectType, getType(hand));
        return hand;
    }

    protected static Hand parseHand(String str)
    {
        Hand hand = new Hand();
        StringTokenizer st = new StringTokenizer(str);
        while (st.hasMoreTokens())
            hand.add(parseCard(st.nextToken()));
        return hand;
    }

    private static Card parseCard(String str)
    {
        if (str.length() != 2)
            throw new IllegalArgumentException("Invalid card: " + str);
        if (str.equals("RJ")) {
            return new Card(RED_JOKER, 0);
        } else if (str.equals("BJ")) {
            return new Card(BLACK_JOKER, 0);
        } else {
            return new Card(parseCardNumber(str.charAt(0)),
                            parseCardSuit(str.charAt(1)));
        }
    }

    private static int parseCardNumber(char c)
    {
        switch (c) {
        case 'T': return 10;
        case 'J': return JACK;
        case 'Q': return QUEEN;
        case 'K': return KING;
        case 'A': return ACE;
        default:
            int number = c - '0';
            if (number < 2 || number > 9)
                throw new IllegalArgumentException("Invalid number: " + c);
            return number;
        }
    }

    private static int parseCardSuit(char c)
    {
        switch (c) {
        case 'c': return CLUBS;
        case 's': return SPADES;
        case 'h': return HEARTS;
        case 'd': return DIAMONDS;
        default:
            throw new IllegalArgumentException("Invalid suit: " + c);
        }
    }

    protected int getType(Hand hand)
    {
        return getHandEvaluator().evaluate(hand, null);
    }

    protected int compare(Hand hand1, Hand hand2)
    {
        return getHandComparator().compare(hand1, hand2);
    }

    abstract protected PokerHandComparator getHandComparator();
    abstract protected PokerHandEvaluator getHandEvaluator();
}
