package com.sixlegs.bigtwo.data;

import com.threerings.parlor.card.data.*;
import junit.framework.*;

public class BigTwoHandsTest
extends HandsTestCase
{
    private static final BigTwoHandComparator HAND_COMPARATOR = new BigTwoHandComparator();
    private static final BigTwoHandEvaluator HAND_EVALUATOR = new BigTwoHandEvaluator();
    
    public BigTwoHandsTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(BigTwoHandsTest.class);
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
    }

    public void testFourOfAKind()
    {
    }

    public void testFullHouse()
    {
    }

    // TODO: add void assertDescending(Hand[]) helper function

    public void testFlush()
    {
        Hand h1 = parseHand("9h 7h 6h 5h 3h", FLUSH);
        Hand h2 = parseHand("2c Jc 9c 6c 4c", FLUSH);
        Hand h3 = parseHand("Ac Kc Qc Tc 7c", FLUSH);
        assertTrue(compare(h1, h2) > 0);
        assertTrue(compare(h2, h3) > 0);
    }
    
    public void testStraight()
    {
        parseHand("3h 4d 5c 6s 7d", STRAIGHT);
        parseHand("2d 3h 4d 5c 6s", INVALID_HAND);
        parseHand("As 2d 3h 4d 5c", INVALID_HAND);
        parseHand("Td Jh Qc Kc As", STRAIGHT);
        parseHand("Jh Qc Kc As 2d", STRAIGHT);
        parseHand("Qc Kc As 2d 3h", INVALID_HAND);

        Hand h1 = parseHand("Kd Qc Jc Th 9h", STRAIGHT);
        Hand h2 = parseHand("Qs Jd Td 9d 8d", STRAIGHT);
        Hand h3 = parseHand("Qd Js Ts 9s 8s", STRAIGHT);
        assertTrue(compare(h1, h2) > 0);
        assertTrue(compare(h2, h3) > 0);
    }

    public void testThreeOfAKind()
    {
    }

    public void testPair()
    {
        assertTrue(compare(parseHand("9s 9d", PAIR), parseHand("9h 9c", PAIR)) > 0);
        assertTrue(compare(parseHand("Qc Qd", PAIR), parseHand("Js Jh", PAIR)) > 0);
        assertTrue(compare(parseHand("Qd Qs", PAIR), parseHand("Qc Qh", PAIR)) > 0);
    }

    public void testHighCard()
    {
    }
}
