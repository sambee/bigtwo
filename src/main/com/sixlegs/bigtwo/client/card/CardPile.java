//
// $Id: CardPile.java 28 2006-11-21 05:18:59Z herbyderby $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.sixlegs.bigtwo.client.card;

import com.samskivert.util.ObserverList;
import com.samskivert.util.Predicate;
import com.threerings.media.MediaPanel;
import com.threerings.media.sprite.PathAdapter;
import com.threerings.media.sprite.PathObserver;
import com.threerings.media.sprite.Sprite;
import com.threerings.media.util.LinePath;
import com.threerings.media.util.Path;
import com.threerings.parlor.card.client.*;
import com.threerings.parlor.card.data.*;
import com.threerings.util.DirectionCodes;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.*;

public class CardPile
{
    /** The selection mode in which cards are not selectable. */
    public static final int NONE = 0;
    
    /** The selection mode in which the user can select a single card. */
    public static final int SINGLE = 1;
    
    /** The selection mode in which the user can select multiple cards. */
    public static final int MULTIPLE = 2;

    protected CardPanel _panel;
    protected CardImageProvider _images;
    protected int _cardWidth;
    protected int _cardHeight;
    protected List<CardSprite> _sprites = new ArrayList<CardSprite>();
    protected Point _location = new Point();
    protected int _spacing;
    protected int _selectableCardOffset;
    protected int _selectedCardOffset;
    protected int _direction = DirectionCodes.EAST;
    protected int _cardOrientation = DirectionCodes.NORTH;
    protected boolean _vertical;
    protected Comparator<Card> _comparator;
    protected int _selectionMode;
    protected Predicate<CardSprite> _selectionPredicate;
    protected ObserverList<CardSelectionObserver> _selectionObservers =
      ObserverList.newList(ObserverList.FAST_UNSAFE_NOTIFY);
    protected List<CardSprite> _selectedSprites = new ArrayList<CardSprite>();
    protected HandSpriteObserver _spriteObserver = new HandSpriteObserver();

    public CardPile(CardPanel panel, CardImageProvider images)
    {
        _panel = panel;
        _images = images;
        _cardWidth = _images.getCardBackImage().getWidth();
        _cardHeight = _images.getCardBackImage().getHeight();
    }

    public int getSpacing()
    {
        return _spacing;
    }
    
    public int getX()
    {
        return _location.x;
    }

    public int getY()
    {
        return _location.y;
    }

    public void setDirection(int direction)
    {
        _vertical = false;
        switch (direction) {
        case DirectionCodes.NORTH:
        case DirectionCodes.SOUTH:
            _vertical = true;
            /* fall-through */
        case DirectionCodes.EAST:
        case DirectionCodes.WEST:
            _direction = direction;
            break;
        default:
            throw new IllegalArgumentException("Only north, south, east, and west are allowed");
        }
    }

    public void setCardOrientation(int orientation)
    {
        _cardOrientation = orientation;
    }

    public void setSpacing(int spacing)
    {
        _spacing = spacing;
    }

    public void setLocation(int x, int y)
    {
        _location.setLocation(x, y);
        adjustHand(0, false);
    }

    public void setSelectableCardOffset(int offset)
    {
        _selectableCardOffset = offset;
    }

    public void setSelectedCardOffset(int offset)
    {
        _selectedCardOffset = offset;
    }

    public void setComparator(Comparator<Card> cmp)
    {
        _comparator = cmp;
        sort();
    }

    protected void sort()
    {
        if (_comparator != null) {
            if (_comparator instanceof HandAwareComparator)
                ((HandAwareComparator)_comparator).setHand(getHand());
            Collections.sort(_sprites, new CardSpriteComparator(_comparator));
        }
    }

    public void setSelectionMode(int mode)
    {
        _selectionMode = mode;
        updateOffsets();
    }

    public void setSelectionPredicate (Predicate<CardSprite> pred)
    {
        _selectionPredicate = pred;
        updateOffsets();
    }

    public CardSprite getSelectedCard ()
    {
        return _selectedSprites.size() == 0 ?
            null : _selectedSprites.get(0);
    }

    public CardSprite[] getSelectedCards ()
    {
        return (CardSprite[])_selectedSprites.toArray(
            new CardSprite[_selectedSprites.size()]);
    }

    public CardSprite[] getRandomCards (int count)
    {
        if (count > _sprites.size())
            throw new IllegalArgumentException("Want " + count + ", have " + _sprites.size());
        List<CardSprite> copy = new ArrayList<CardSprite>(_sprites);
        Collections.shuffle(copy);
        return copy.subList(0, count).toArray(new CardSprite[count]);
    }

    public int getCount()
    {
        return _sprites.size();
    }

    public CardSprite[] getAllCards()
    {
        return _sprites.toArray(new CardSprite[_sprites.size()]);
    }

    public void selectCard (final CardSprite sprite)
    {
        // make sure it's not already selected
        if (_selectedSprites.contains(sprite)) {
            return;
        }
        
        // if in single card mode and there's another card selected,
        // deselect it
        if (_selectionMode == SINGLE) {
            CardSprite oldCard = getSelectedCard();
            if (oldCard != null) {
                deselectCard(oldCard);
            }
        }
        
        // add to list and update offset
        _selectedSprites.add(sprite);
        updateOffset(sprite);
        
        // notify the observers
        ObserverList.ObserverOp<CardSelectionObserver> op =
            new ObserverList.ObserverOp<CardSelectionObserver>() {
                public boolean apply (CardSelectionObserver obs) {
                    obs.cardSpriteSelected(sprite);
                    return true;
                }
            };
        _selectionObservers.apply(op);
    }

    /**
     * Programmatically deselects a card in the hand.
     */
    public void deselectCard (final CardSprite sprite)
    {
        // make sure it's selected
        if (!_selectedSprites.contains(sprite)) {
            return;
        }
        
        // remove from list and update offset
        _selectedSprites.remove(sprite);
        updateOffset(sprite);
        
        // notify the observers
        ObserverList.ObserverOp<CardSelectionObserver> op =
            new ObserverList.ObserverOp<CardSelectionObserver>() {
                public boolean apply (CardSelectionObserver obs) {
                    obs.cardSpriteDeselected(sprite);
                    return true;
                }
            };
        _selectionObservers.apply(op);
    }

    /**
     * Clears any existing hand sprite selection.
     */
    public void clearSelection ()
    {
        CardSprite[] cards = getSelectedCards();
        for (int i = 0; i < cards.length; i++) {
            deselectCard(cards[i]);
        }
    }

    /**
     * Adds an object to the list of observers to notify when cards in the
     * hand are selected/deselected.
     */
    public void addCardSelectionObserver (CardSelectionObserver obs)
    {
        _selectionObservers.add(obs);
    }
    
    /**
     * Removes an object from the card selection observer list.
     */
    public void removeCardSelectionObserver (CardSelectionObserver obs)
    {
        _selectionObservers.remove(obs);
    }

    /**
     * Fades a hand of cards in.
     *
     * @param hand the hand of cards
     * @param fadeDuration the amount of time to spend fading in
     * the entire hand
     */
    public void setHand (Hand hand, long fadeDuration)
    {
        // make sure no cards are hanging around
        clearHand();

        // create the sprites
        int size = hand.size();
        for (int i = 0; i < size; i++) {
            _sprites.add(new CardSprite(_images, (Card)hand.get(i)));
        }
        sort();

        // fade them in at proper locations and layers
        long cardDuration = (size == 0) ? 0 : fadeDuration / size;
        for (int i = 0; i < size; i++) {
            CardSprite cs = _sprites.get(i);
            Point p = getLocation(i, cs, false);
            cs.setOrientation(_cardOrientation);
            cs.setRenderOrder(i);
            cs.setLocation(p.x, p.y);
            cs.addSpriteObserver(_spriteObserver);
            _panel.addSprite(cs);
            cs.fadeIn(i * cardDuration, cardDuration);
        }
        
        // make sure we have the right card sprite active
        _panel.updateActiveCardSprite();
    }
    
    /**
     * Fades a hand of cards in face-down.
     *
     * @param size the size of the hand
     * @param fadeDuration the amount of time to spend fading in
     * each card
     */
    public void setHand (int size, long fadeDuration)
    {
        // fill hand will null entries to signify unknown cards
        Hand hand = new Hand();
        for (int i = 0; i < size; i++) {
            hand.add(null);
        }
        setHand(hand, fadeDuration);
    }

    /**
     * Returns the first sprite in the hand that corresponds to the
     * specified card, or null if the card is not in the hand.
     */
    public CardSprite getSprite (Card card)
    {
        for (CardSprite cs : _sprites) {
            if (card.equals(cs.getCard()))
                return cs;
        }
        return null;
    }

    public Hand getHand()
    {
        Hand hand = new Hand();
        for (CardSprite cs : _sprites)
            hand.add(cs.getCard());
        return hand;
    }
    
    /**
     * Clears all cards from the hand.
     */
    public void clearHand ()
    {
        clearSelection();
        for (Iterator<CardSprite> it = _sprites.iterator(); it.hasNext(); ) {
            _panel.removeSprite(it.next());
            it.remove();
        }
    }

    public void setFacingUp(boolean facingUp)
    {
        for (CardSprite cs : _sprites)
            cs.setFacingUp(facingUp);
    }

    public static void flyBetween(CardPile src, CardPile dst, CardSprite[] cards,
                                  boolean replaceHand, long flightDuration)
    {
        if (src._panel != dst._panel)
            throw new IllegalStateException("Panels must be the same");
        if (cards.length == 0)
            throw new IllegalArgumentException("Card required");

        int order = 0;
        PathObserver remover = null;
        if (replaceHand) {
            final MediaPanel panel = dst._panel;
            final List<CardSprite> copy = new ArrayList<CardSprite>(dst._sprites);
            dst._sprites.clear();
            dst._selectedSprites.clear();
            for (CardSprite cs : copy)
                cs.setRenderOrder(order++);

            remover = new PathDoneCallback(){
                public void pathDone(Sprite sprite, Path path, boolean completed, long when) {
                    for (CardSprite toRemove : copy) {
                        if (panel.isManaged(toRemove))
                            panel.removeSprite(toRemove);
                    }
                }
            };
        }

        // first create the sprites and add them to the list
        for (CardSprite cs : cards) {
            src._selectedSprites.remove(cs);
            src._sprites.remove(cs);
            dst._sprites.add(cs);
        }
        dst.sort();

        if (replaceHand)
            dst._sprites.get(0).addSpriteObserver(remover);

        for (int i = 0; i < dst._sprites.size(); i++) {
            CardSprite cs = dst._sprites.get(i);
            cs.setRenderOrder(order++);
            cs.setOrientation(dst._cardOrientation);
            cs.move(new LinePath(dst.getLocation(i, cs), flightDuration));
        }
        // settle the hand
        src.adjustHand(flightDuration, false);
    }

    /**
     * Expands or collapses the hand to accommodate new cards or cover the
     * space left by removed cards.  Skips unmanaged sprites.  Clears out
     * any selected cards.
     *
     * @param adjustDuration the amount of time to spend settling the cards
     * into their new locations
     * @param updateLayers whether or not to update the layers of the cards
     */
    public void adjustHand (long adjustDuration, boolean updateLayers)
    {
        // clear out selected cards
        clearSelection();

        // Move each card to its proper position (and, optionally, layer)
        int size = _sprites.size();
        for (int i = 0; i < size; i++) {
            CardSprite cs = _sprites.get(i);
            if (!_panel.isManaged(cs)) {
                continue;
            }
            cs.setOrientation(_cardOrientation);
            if (updateLayers) {
                cs.setRenderOrder(i);
            }
            Point dest = getLocation(i, cs, false);
            if (adjustDuration > 0) {
                cs.move(new LinePath(dest, adjustDuration));
            } else {
                cs.setLocation(dest.x, dest.y);
            }
        }
    }
    
    /**
     * Updates the offsets of all the cards in the hand.  If there is only
     * one selectable card, that card will always be raised slightly.
     */
    protected void updateOffsets ()
    {
        // make active card sprite is up-to-date
        _panel.updateActiveCardSprite();

        for (CardSprite cs : _sprites) {
            if (!cs.isMoving())
                updateOffset(cs);
        }
    }
    
    protected void updateOffset (CardSprite sprite)
    {
        int hc = getHeightCoord(sprite, true);
        if (_vertical) {
            sprite.setLocation(hc, sprite.getY());
        } else {
            sprite.setLocation(sprite.getX(), hc);
        }
    }

    protected Point getLocation (int idx, CardSprite sprite)
    {
        return getLocation(idx, sprite, true);
    }
    
    protected Point getLocation (int idx, CardSprite sprite, boolean calcOffset)
    {
        int wc = getWidthCoord(idx);
        int hc = getHeightCoord(sprite, calcOffset);
        if (_vertical) {
            return new Point(hc, wc);
        } else {
            return new Point(wc, hc);
        }
    }
        
    protected int getWidthCoord (int idx)
    {
        int cardSize = _vertical ? _cardHeight : _cardWidth;
        int width = (_sprites.size() - 1) * _spacing + cardSize;
        switch (_direction) {
        case DirectionCodes.EAST:  return (_location.x - width / 2) + idx * _spacing;
        case DirectionCodes.SOUTH: return (_location.y - width / 2) + idx * _spacing;
        case DirectionCodes.WEST:  return (_location.x + width / 2) - cardSize - idx * _spacing;
        case DirectionCodes.NORTH: return (_location.y + width / 2) - cardSize - idx * _spacing;
        }
        return 0;
    }

    protected int getHeightCoord (CardSprite sprite, boolean calcOffset)
    {
        int offset = calcOffset ? getOffset(sprite) : 0;
        switch (_direction) {
        case DirectionCodes.EAST:  return _location.y - _cardHeight / 2 - offset;
        case DirectionCodes.WEST:  return _location.y - _cardHeight / 2 + offset;
        case DirectionCodes.SOUTH: return _location.x - _cardWidth / 2 + offset;
        case DirectionCodes.NORTH: return _location.x - _cardWidth / 2 - offset;
        }
        return 0;
    }

    protected int getOffset (CardSprite sprite)
    {
        if (_selectedSprites.contains(sprite)) {
            return _selectedCardOffset;
        } else if (isSelectable(sprite) &&
            (sprite == _panel.getActiveCardSprite() || isOnlySelectable(sprite))) {
            return _selectableCardOffset;
        } else {
            return 0;
        }
    }

    /**
     * Given the current selection mode and predicate, determines if the
     * specified sprite is selectable.
     */
    protected boolean isSelectable (CardSprite sprite)
    {
        return _selectionMode != NONE &&
            (_selectionPredicate == null ||
                _selectionPredicate.isMatch(sprite));
    }
    
    /**
     * Determines whether the specified sprite is the only selectable sprite
     * in the hand according to the selection predicate.
     */
    protected boolean isOnlySelectable (CardSprite sprite)
    {
        // if there's no predicate, last remaining card is only selectable
        if (_selectionPredicate == null) {
            return _sprites.size() == 1 && _sprites.contains(sprite);
        }
        
        // otherwise, look for a sprite that fits the predicate and isn't the
        // parameter
        for (CardSprite cs : _sprites) {
            if (cs != sprite && _selectionPredicate.isMatch(cs))
                return false;
        }
        return true;
    }
    
    /** Listens for interactions with cards in hand. */
    protected class HandSpriteObserver extends PathAdapter
        implements CardSpriteObserver
    {
        public void pathCompleted (Sprite sprite, Path path, long when)
        {
            _panel.updateActiveCardSprite();
            maybeUpdateOffset((CardSprite)sprite);
        }
        
        public void cardSpriteClicked (CardSprite sprite, MouseEvent me)
        {
            // select, deselect, or play card in hand
            if (_selectedSprites.contains(sprite) &&
                _selectionMode != NONE) {
                deselectCard(sprite);
                
            } else if (_sprites.contains(sprite) && isSelectable(sprite)) {
                selectCard(sprite);
            }
        }

        public void cardSpriteEntered (CardSprite sprite, MouseEvent me)
        {
            maybeUpdateOffset(sprite);
        }
        
        public void cardSpriteExited (CardSprite sprite, MouseEvent me)
        {
            maybeUpdateOffset(sprite);
        }
        
        public void cardSpriteDragged (CardSprite sprite, MouseEvent me)
        {}
        
        protected void maybeUpdateOffset (CardSprite sprite)
        {
            // update the offset if it's in the hand and isn't moving
            if (_sprites.contains(sprite) && !sprite.isMoving())
                updateOffset(sprite);
        }

        public void cardSpritePopup (CardSprite sprite, MouseEvent me)
        {}
    };
}
