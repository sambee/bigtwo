//
// $Id: CardPanel.java 28 2006-11-21 05:18:59Z herbyderby $
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
import com.threerings.media.FrameManager;
import com.threerings.media.VirtualMediaPanel;
import com.threerings.media.sprite.Sprite;
import com.threerings.parlor.card.data.CardCodes;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputAdapter;

/**
 * Extends VirtualMediaPanel to provide services specific to rendering
 * and manipulating playing cards.
 */
public abstract class CardPanel extends VirtualMediaPanel
implements CardCodes
{
    /** A nice default green card table background color. */
    protected static final Color DEFAULT_BACKGROUND = new Color(0x326D36);

    protected CardSprite _activeCardSprite;
    protected MouseEvent _mouseEvent;

    protected void paintBehind (Graphics2D gfx, Rectangle dirtyRect)
    {
        gfx.setColor(DEFAULT_BACKGROUND);
        gfx.fill(dirtyRect);
        super.paintBehind(gfx, dirtyRect);
    }

    /**
     * Constructor.
     *
     * @param frameManager the frame manager
     */
    public CardPanel (FrameManager frameManager)
    {
        super(frameManager);
        
        // add a listener for mouse events
        CardListener cl = new CardListener();
        addMouseListener(cl);
        addMouseMotionListener(cl);
    }
    
    /** Listens for mouse interactions with cards. */
    protected class CardListener extends MouseInputAdapter
    {
        public void mousePressed (MouseEvent me)
        {
            if (_activeCardSprite != null &&
                isManaged(_activeCardSprite)) {
                _handleX = _activeCardSprite.getX() - me.getX();
                _handleY = _activeCardSprite.getY() - me.getY();        
                _hasBeenDragged = false;
                if (me.isPopupTrigger()) {
                    // TODO: events are not always consumed
                    me.consume();
                    _activeCardSprite.queueNotification(
                        new CardSpritePopupOp(_activeCardSprite, me)
                    );
                }
            }
        }
        
        public void mouseReleased (MouseEvent me)
        {
            if (_activeCardSprite != null &&
                isManaged(_activeCardSprite)) {
                if (_hasBeenDragged) {
                    _activeCardSprite.queueNotification(
                        new CardSpriteDraggedOp(_activeCardSprite, me)
                    );
                }
                if (me.isPopupTrigger()) {
                    // TODO: events are not always consumed
                    me.consume();
                    _activeCardSprite.queueNotification(
                        new CardSpritePopupOp(_activeCardSprite, me)
                    );
                }
            }
        }
        
        public void mouseClicked (MouseEvent me)
        {
            if (_activeCardSprite != null &&
                isManaged(_activeCardSprite)) {
                _activeCardSprite.queueNotification(
                    new CardSpriteClickedOp(_activeCardSprite, me)
                );
            }
        }
        
        public void mouseMoved (MouseEvent me)
        {
            _mouseEvent = me;
            
            updateActiveCardSprite();
        }
        
        public void mouseDragged (MouseEvent me)
        {
            _mouseEvent = me;
            
            if (_activeCardSprite != null &&
                isManaged(_activeCardSprite) &&
                _activeCardSprite.isDraggable()) {
                _activeCardSprite.setLocation(
                    me.getX() + _handleX,
                    me.getY() + _handleY
                ); 
                _hasBeenDragged = true;
                
            } else {
                updateActiveCardSprite();
            }
        }
        
        public void mouseEntered (MouseEvent me)
        {
            _mouseEvent = me;
        }
        
        public void mouseExited (MouseEvent me)
        {
            _mouseEvent = me;
        }
        
        protected int _handleX, _handleY;
        protected boolean _hasBeenDragged;
    }
    
    /** Calls CardSpriteObserver.cardSpritePopup. */ 
    protected static class CardSpritePopupOp implements
        ObserverList.ObserverOp
    {
        public CardSpritePopupOp (CardSprite sprite, MouseEvent me)
        {
            _sprite = sprite;
            _me = me;
        }
        
        public boolean apply (Object observer)
        {
            if (observer instanceof CardSpriteObserver) {
                ((CardSpriteObserver)observer).cardSpritePopup(_sprite,
                    _me);
            }
            return true;
        }
        
        protected CardSprite _sprite;
        protected MouseEvent _me;
    }
    
    /** Calls CardSpriteObserver.cardSpriteClicked. */ 
    protected static class CardSpriteClickedOp implements
        ObserverList.ObserverOp
    {
        public CardSpriteClickedOp (CardSprite sprite, MouseEvent me)
        {
            _sprite = sprite;
            _me = me;
        }
        
        public boolean apply (Object observer)
        {
            if (observer instanceof CardSpriteObserver) {
                ((CardSpriteObserver)observer).cardSpriteClicked(_sprite,
                    _me);
            }
            return true;
        }
        
        protected CardSprite _sprite;
        protected MouseEvent _me;
    }
    
    /** Calls CardSpriteObserver.cardSpriteEntered. */ 
    protected static class CardSpriteEnteredOp implements
        ObserverList.ObserverOp
    {
        public CardSpriteEnteredOp (CardSprite sprite, MouseEvent me)
        {
            _sprite = sprite;
            _me = me;
        }
        
        public boolean apply (Object observer)
        {
            if (observer instanceof CardSpriteObserver) {
                ((CardSpriteObserver)observer).cardSpriteEntered(_sprite,
                    _me);
            }
            return true;
        }
        
        protected CardSprite _sprite;
        protected MouseEvent _me;
    }
    
    /** Calls CardSpriteObserver.cardSpriteExited. */ 
    protected static class CardSpriteExitedOp implements
        ObserverList.ObserverOp
    {
        public CardSpriteExitedOp (CardSprite sprite, MouseEvent me)
        {
            _sprite = sprite;
            _me = me;
        }
        
        public boolean apply (Object observer)
        {
            if (observer instanceof CardSpriteObserver) {
                ((CardSpriteObserver)observer).cardSpriteExited(_sprite, _me);
            }
            return true;
        }
        
        protected CardSprite _sprite;
        protected MouseEvent _me;
    }
    
    /** Calls CardSpriteObserver.cardSpriteDragged. */
    protected static class CardSpriteDraggedOp implements
        ObserverList.ObserverOp
    {
        public CardSpriteDraggedOp (CardSprite sprite, MouseEvent me)
        {
            _sprite = sprite;
            _me = me;
        }
        
        public boolean apply (Object observer)
        {
            if (observer instanceof CardSpriteObserver) {
                ((CardSpriteObserver)observer).cardSpriteDragged(_sprite,
                    _me);
            }
            return true;
        }
        
        protected CardSprite _sprite;
        protected MouseEvent _me;
    }

    protected CardSprite getActiveCardSprite()
    {
        return _activeCardSprite;
    }

    /**
     * Updates the active card sprite based on the location of the mouse
     * pointer.
     */
    protected void updateActiveCardSprite ()
    {
        // can't do anything if we don't know where the mouse pointer is
        if (_mouseEvent == null) {
            return;
        }
        
        Sprite newHighestHit = _spritemgr.getHighestHitSprite(
            _mouseEvent.getX(), _mouseEvent.getY());
        
        CardSprite newActiveCardSprite =
            (newHighestHit instanceof CardSprite ? 
                (CardSprite)newHighestHit : null);
        
        if (_activeCardSprite != newActiveCardSprite) {
            if (_activeCardSprite != null &&
                isManaged(_activeCardSprite)) {
                _activeCardSprite.queueNotification(
                    new CardSpriteExitedOp(_activeCardSprite,
                        _mouseEvent)
                );
            }
            _activeCardSprite = newActiveCardSprite;
            if (_activeCardSprite != null) {
                _activeCardSprite.queueNotification(
                    new CardSpriteEnteredOp(_activeCardSprite,
                        _mouseEvent)
                );
            }
        }
    }
}
