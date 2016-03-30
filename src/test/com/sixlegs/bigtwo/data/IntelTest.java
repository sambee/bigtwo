package com.sixlegs.bigtwo.data;

import com.threerings.parlor.card.data.*;
import com.threerings.util.Name;
import junit.framework.*;

public class IntelTest
extends TestCase
{
    private static final int LOOP = 1000;
    
    public IntelTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(IntelTest.class);
    }

    private static final Card THREE_OF_DIAMONDS = new Card(3, CardCodes.DIAMONDS);

    public void testLooptyLoop()
    {
        int[] winners = new int[4];
        BigTwoObject obj = new BigTwoObject();
        obj.players = new Name[4];
        obj.owner = -1;
        for (int i = 0; i < LOOP; i++) {
            Deck deck = new Deck();
            deck.shuffle();
            Hand[] hands = new Hand[4];
            Intel[] intel = new Intel[4];
            obj.cardCounts = new int[]{ 13, 13, 13, 13 };
            int index = -1;
            for (int j = 0; j < 4; j++) {
                hands[j] = deck.dealHand(13);
                intel[j] = new Intel(hands[j]);
                if (hands[j].contains(THREE_OF_DIAMONDS))
                    index = j;
            }

            for (;;) {
                Hand hand = hands[index];
                Hand chose = intel[index].chooseHand(hand, obj, index);
                if (chose == null) {
                    obj.topHand = null;
                } else {
                    hand.removeAll(chose);
                    obj.topHand = chose;
                    obj.cardCounts[index] = hand.size();
                    obj.owner = index;
                    if (hand.size() == 0) {
                        winners[index]++;
                        break;
                    }
                }
                index = (index + 1) % 4;
            }
        }
        System.err.println("Winners: "
                            + winners[0] + ", "
                            + winners[1] + ", "
                            + winners[2] + ", "
                            + winners[3]);
    }
}
