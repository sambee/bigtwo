package com.sixlegs.bigtwo.data;

import com.threerings.parlor.card.data.*;
import junit.framework.*;

public class PokerHandsTest
extends HandsTestCase
{
    private static final PokerHandComparator HAND_COMPARATOR = new PokerHandComparator();
    private static final PokerHandEvaluator HAND_EVALUATOR = new PokerHandEvaluator();
    
    public PokerHandsTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(PokerHandsTest.class);
    }

    protected PokerHandComparator getHandComparator()
    {
        return HAND_COMPARATOR;
    }
    
    protected PokerHandEvaluator getHandEvaluator()
    {
        return HAND_EVALUATOR;
    }
    
    public void testStraightFlush()
    {
        parseHand("7c 8c 9c Tc Jc", STRAIGHT_FLUSH);
        parseHand("Tc Jc Qc Kc Ac", STRAIGHT_FLUSH); // royal flush
        parseHand("Ah 2h 3h 4h 5h", STRAIGHT_FLUSH); // low ace
        parseHand("Kd Ad 2d 3d 4d", FLUSH); // no wraparounds
    }

    public void testFourOfAKind()
    {
        Hand h1 = parseHand("3s 3c 3d 3h Ac", FOUR_OF_A_KIND);
        Hand h2 = parseHand("4s 4c 4d 4h 2c", FOUR_OF_A_KIND);
        assertTrue(compare(h1, h2) < 0);
    }

    public void testFullHouse()
    {
        Hand h1 = parseHand("9s 9d 9h 4c 4s", FULL_HOUSE);
        Hand h2 = parseHand("8s 8d 8h Ac As", FULL_HOUSE);
        assertTrue(compare(h1, h2) > 0);
    }

    public void testFlush()
    {
        parseHand("Ks Js 9s 3s 2s", FLUSH);
        parseHand("Kd Jd 7d 6d 5d", FLUSH);
    }
    
    public void testStraight()
    {
        parseHand("Qs Jd Th 9s 8c", STRAIGHT);
        parseHand("Tc Jd Qh Ks Ah", STRAIGHT); // high ace
        parseHand("As 2c 3d 4h 5s", STRAIGHT); // low ace
        parseHand("2c Ah Ks Qc Jc", HIGH_CARD); // no wraparounds
        parseHand("As 3d 4h 5s 5h", PAIR); // almost an ace-low straight, but not
    }

    public void testThreeOfAKind()
    {
        parseHand("5c 5d 5h 3s 2s", THREE_OF_A_KIND);
        parseHand("4d 4h 4s Kc Qd", THREE_OF_A_KIND);
    }

    public void testTwoPair()
    {
        parseHand("Jc Js 2d 2h 4h", TWO_PAIR);
        parseHand("Tc Ts 9d 9h 8h", TWO_PAIR);
        parseHand("8c 8s 6d 6h 3h", TWO_PAIR);
        parseHand("8d 8h 5s 5c Kh", TWO_PAIR);
        parseHand("Qc Qs 5d 5h 8h", TWO_PAIR);
        parseHand("Qd Qh 5s 5c 4h", TWO_PAIR);
    }

    public void testPair()
    {
        parseHand("6s 6h 4s 3d 2h", PAIR);
        parseHand("5s 5h As Kd Qh", PAIR);
        parseHand("Js Jh As 9d 3h", PAIR);
        parseHand("Jd Jc Ac 8d 7h", PAIR);
    }

    public void testHighCard()
    {
        parseHand("As Jh 9s 5d 3h", HIGH_CARD);
        parseHand("Ac Td 9h 6s 4c", HIGH_CARD);
    }
}
