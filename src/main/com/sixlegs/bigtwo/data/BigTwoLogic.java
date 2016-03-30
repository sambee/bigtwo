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
import java.util.*;

public class BigTwoLogic
{
    private static final Comparator<Card> CARD_COMPARATOR = new BigTwoCardComparator(true);
    private static final BigTwoHandEvaluator HAND_EVALUATOR = new BigTwoHandEvaluator();
    private static final BigTwoHandComparator HAND_COMPARATOR = new BigTwoHandComparator();

    private List<List<Hand>> handsBySize = new ArrayList<List<Hand>>();
    private Map<Card,List<Hand>> handsByCard = new HashMap<Card,List<Hand>>();
    
    public BigTwoLogic(Hand start)
    {
        for (int i = 0; i < 6; i++)
            handsBySize.add(new ArrayList<Hand>());
        
        Hand copy = new Hand();
        copy.addAll(start);
        Collections.sort(copy, CARD_COMPARATOR);

        int[] counts = new int[16];
        for (Card card : copy)
            counts[card.getNumber()]++;

        List<List<Card>> cardsBySuit = new ArrayList<List<Card>>();
        for (int i = 0; i < 4; i++)
            cardsBySuit.add(new ArrayList<Card>());
        
        int index = 0;
        while (index < copy.size()) {
            Card card = copy.get(index);
            cardsBySuit.get(card.getSuit()).add(card);
            int n = counts[card.getNumber()];
            List<Card> subList = copy.subList(index, index + n);
            for (int r = 1; r <= n; r++)
                handsBySize.get(r).addAll(generateHands(subList, r));
            index += n;
        }

        // use brute force to look for straights
        for (int i = 0; i <= copy.size() - 5; i++) {
            List<Card> subList = copy.subList(i, i + 5);
            if (HAND_EVALUATOR.isStraight(subList))
                handsBySize.get(5).addAll(generateHands(subList, 5));
        }

        // create flushes
        for (int i = 0; i < 4; i++) {
            List<Card> suited = cardsBySuit.get(i);
            if (suited.size() >= 5)
                handsBySize.get(5).addAll(generateHands(suited, 5));
        }

        // create full houses
        handsBySize.get(5).addAll(combineHands(handsBySize.get(3), handsBySize.get(2)));

        // create four-of-a-kinds, then remove four-card hands
        handsBySize.get(5).addAll(combineHands(handsBySize.get(4), handsBySize.get(1)));
        handsBySize.get(4).clear();

        for (List<Hand> hands : handsBySize) {
            for (Hand hand : hands) {
                for (Card card : hand) {
                    List<Hand> list = handsByCard.get(card);
                    if (list == null)
                        handsByCard.put(card, list = new ArrayList<Hand>());
                    list.add(hand);
                }
            }
        }
            
        for (List<Hand> hands : handsBySize)
            Collections.sort(hands, HAND_COMPARATOR);
        for (List<Hand> hands : handsByCard.values())
            Collections.sort(hands, HAND_COMPARATOR);
        
        // System.err.println("handsByCard=" + handsByCard);
        // System.err.println("handsBySize=" + Arrays.asList(handsBySize));
    }

    public Hand chooseHand(BigTwoObject obj, int pidx)
    {
        if (obj.owner < 0) {
            // for now, choose best hand with the 3d
            List<Hand> hands = handsByCard.get(new Card(3, PokerCodes.DIAMONDS));
            return hands.get(hands.size() - 1);
        }

        boolean protect = obj.cardCounts[obj.getAdjacentPlayer(pidx, 1)] == 1;
        if (obj.topHand == null) {
            // pick a big, low-ranking hand
            for (int i = 5; i >= 1; i--) {
                List<Hand> hands = handsBySize.get(i);
                if (hands.size() > 0)
                    return hands.get((i == 1 && protect) ? hands.size() - 1 : 0);
            }
        } else {
            List<Hand> hands = handsBySize.get(obj.topHand.size());
            if (hands.size() > 0 && obj.topHand.size() == 1 && protect) {
                Hand hand = hands.get(hands.size() - 1);
                if (HAND_COMPARATOR.compare(hand, obj.topHand) > 0)
                    return hand;
            } else {
                // pick lowest hand that beats top hand
                for (Hand hand : hands) {
                    if (HAND_COMPARATOR.compare(hand, obj.topHand) > 0) {
                        // TODO: make sure hand isn't ridiculous (e.g. a flush with three twos)
                        return hand;
                    }
                }
            }
        }
        return null;
    }

    public void invalidate(Hand play)
    {
        for (Card card : play) {
            for (Hand hand : handsByCard.remove(card))
                handsBySize.get(hand.size()).remove(hand);
        }
    }

    private static List<Hand> generateHands(List<Card> source, int r)
    {
        // System.err.print(source.size() + " choose " + r + " from " + source + " -> ");
        CombinationGenerator cg = new CombinationGenerator(source.size(), r);
        List<Hand> result = new ArrayList<Hand>(cg.getTotal());
        while (cg.hasMore()) {
            int[] indices = cg.getNext();
            Hand hand = new Hand();
            for (int i = 0; i < indices.length; i++)
                hand.add(source.get(indices[i]));
            Collections.sort(hand, CARD_COMPARATOR);
            result.add(hand);
        }
        // System.err.println(result);
        return result;
    }

    private static List<Hand> combineHands(List<Hand> a, List<Hand> b)
    {
        List<Hand> result = new ArrayList<Hand>(a.size() * b.size());
        for (List<Card> cards : a) {
            Set<Card> set = new HashSet<Card>(cards);
            for (Hand hand : b) {
                if (!containsAny(set, hand)) {
                    // no overlap, hand is valid
                    Hand combined = new Hand();
                    combined.addAll(set);
                    combined.addAll(hand);
                    Collections.sort(combined, CARD_COMPARATOR);
                    result.add(combined);
                }
            }
        }
        return result;
    }

    private static boolean containsAny(Set set, Collection c)
    {
        for (Object obj : c) {
            if (set.contains(obj))
                return true;
        }
        return false;
    }
}
