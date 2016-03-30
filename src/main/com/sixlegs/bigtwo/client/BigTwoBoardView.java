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

package com.sixlegs.bigtwo.client;

import com.samskivert.swing.Controller;
import com.samskivert.swing.Label;
import com.samskivert.swing.LabelStyleConstants;
import com.sixlegs.bigtwo.client.card.*;
import com.sixlegs.bigtwo.data.*;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.media.animation.*;
import com.threerings.media.image.*;
import com.threerings.media.sprite.ButtonSprite;
import com.threerings.media.sprite.LabelSprite;
import com.threerings.media.sprite.Sprite;
import com.threerings.parlor.card.data.*;
import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.ElementUpdateListener;
import com.threerings.presents.dobj.ElementUpdatedEvent;
import com.threerings.toybox.util.ToyBoxContext;
import com.threerings.util.DirectionCodes;
import com.threerings.util.MessageBundle;
import com.threerings.util.Name;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.*;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * Displays the main game interface (the board).
 */
public class BigTwoBoardView
extends CardPanel
implements PlaceView, AttributeChangeListener, ElementUpdateListener, ActionListener
{
    private static final long FLIGHT_DURATION = 400;
    private static final long FADE_DURATION = 200;
    private static final int OUTER_MARGIN = 20;
    private static final int LABEL_OFFSET = 15;
    private static final int BUTTON_GAP = 5;
    private static final int HAND_UP_SPACING = 25; // TODO: percentage of card width?
    private static final int HAND_DOWN_SPACING = 10;
    private static final int SELECTED_OFFSET = HAND_UP_SPACING;
    private static final Font LABEL_FONT = new Font("Arial", 0, 12);
    private static final Font ACTION_FONT = new Font("Arial", Font.BOLD, 16);
    private static final Font HAND_FONT = new Font("Arial", Font.BOLD, 24);

    private ToyBoxContext _ctx;

    /** A reference to our game object. */
    protected BigTwoObject _gameobj;

    protected ButtonSprite play;
    protected ButtonSprite pass;

    private BodyObject _self;
    private MessageBundle _msgs;
    private HandDescriber _desc;
    
    private int _selfIndex = -1;
    private CardPile _top;
    private CardPile[] _piles = new CardPile[4];
    private LabelSprite[] _labels = new LabelSprite[4];
    private LabelSprite _ownerLabel;
    private int _cardWidth;
    private int _cardHeight;
    private Comparator<Card> _cmp = new BigTwoCardComparator(true);
    private BigTwoLogic _logic;
    private boolean _suggest;
    private boolean _autoPass;
    private JPopupMenu _popup;

    public BigTwoBoardView (ToyBoxContext ctx)
    {
        super(ctx.getFrameManager());
        _ctx = ctx;
    }

    public Dimension getPreferredSize()
    {
        return new Dimension(400, 400);
    }

    public void willEnterPlace (PlaceObject plobj)
    {
        _gameobj = (BigTwoObject)plobj;
        _gameobj.addListener(this);

        _msgs = _ctx.getMessageManager().getBundle(BigTwoCodes.BIGTWO_MSG_BUNDLE);
        _desc = new HandDescriber(_msgs, new BigTwoHandEvaluator());
        _self = (BodyObject)_ctx.getClient().getClientObject();
        CardImageProvider images =
            new SimpleCardImageProvider(_ctx.loadImage("media/cards.png"),
                                        _ctx.loadImage("media/cards_micro.png"));
        _cardWidth = images.getCardBackImage().getWidth();
        _cardHeight = images.getCardBackImage().getHeight();
        _piles[0] = new CardPile(this, images){
            public void selectCard(CardSprite sprite) {
                int maxSelection = (_gameobj.topHand != null) ? _gameobj.topHand.size() : 5;
                if (maxSelection == 1 &&
                    _selectedSprites.size() == 1 &&
                    _selectedSprites.get(0) != sprite)
                    deselectCard((CardSprite)_selectedSprites.get(0));
                if (_selectedSprites.size() < maxSelection) {
                    super.selectCard(sprite);
                    selectHand(getSelectedCards());
                }
            }
            public void deselectCard(CardSprite sprite) {
                super.deselectCard(sprite);
                selectHand(getSelectedCards());
            }
        };
        _piles[0].setSelectedCardOffset(SELECTED_OFFSET);

        _piles[1] = new CardPile(this, images);
        _piles[1].setDirection(DirectionCodes.SOUTH);
        _piles[1].setCardOrientation(DirectionCodes.EAST);
        _piles[2] = new CardPile(this, images);
        _piles[2].setDirection(DirectionCodes.WEST);
        _piles[2].setCardOrientation(DirectionCodes.SOUTH);
        _piles[3] = new CardPile(this, images);
        _piles[3].setDirection(DirectionCodes.NORTH);
        _piles[3].setCardOrientation(DirectionCodes.WEST);

        _top = new CardPile(this, images);
        _top.setSpacing(HAND_UP_SPACING);
        _top.setComparator(new BigTwoCardComparator(true));

        for (int i = 0; i < 4; i++) {
            _labels[i] = new CenteredLabelSprite(new Label("", LabelStyleConstants.OUTLINE,
                                                           Color.green, Color.black, LABEL_FONT));
            _labels[i].setLocation(0, 0);
        }

        _ownerLabel = new CenteredLabelSprite(new Label("", LabelStyleConstants.BOLD,
                                                        new Color(0x19361B), Color.black, LABEL_FONT));
        _ownerLabel.setLocation(0, 0);

        play = createButton(_msgs.get("m.play"), BigTwoCodes.PLAY);
        pass = createButton(_msgs.get("m.pass"), BigTwoCodes.PASS);
                    
        _popup = new JPopupMenu();
        _popup.add(createMenuItem("m.sort_rank", "rank"));
        _popup.add(createMenuItem("m.sort_suit", "suit"));

        // hack to update board state when an observer enters
        if (_gameobj.isInPlay())
            gameDidStart();
    }


    private JMenuItem createMenuItem(String id, String cmd)
    {
        JMenuItem menuItem = new JMenuItem(_msgs.get(id));
        menuItem.setActionCommand(cmd);
        menuItem.addActionListener(this);
        return menuItem;
    }

    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();
        if ("rank".equals(cmd)) {
            setComparator(new BigTwoCardComparator(true));
        } else if ("suit".equals(cmd)) {
            setComparator(new BySuitComparator());
        }
    }    

    private ButtonSprite createButton(String text, String command)
    {
        Label label = new Label(text, LabelStyleConstants.NORMAL, Color.yellow, Color.blue, LABEL_FONT);
        ButtonSprite button = new ButtonSprite(label,
                                               ButtonSprite.ROUNDED, 10, 10,
                                               DEFAULT_BACKGROUND,
                                               Color.green,
                                               command,
                                               null);
        button.setEnabled(false);
        return button;
    }

    public void didLeavePlace (PlaceObject plobj)
    {
        _gameobj.removeListener(this);
        _gameobj = null;
    }

    public void setSuggest(boolean suggest)
    {
        _suggest = suggest;
        if (_suggest && _gameobj.getTurnHolderIndex(0) == _selfIndex)
            selectHand(_logic.chooseHand(_gameobj, _selfIndex));
    }

    public void setAutoPass(boolean autoPass)
    {
        _autoPass = autoPass;
    }

    public void setComparator(Comparator<Card> cmp)
    {
        _cmp = cmp;
        _piles[0].setComparator(_cmp);
        _piles[0].adjustHand(0, true);
    }

    public void receivedHand(Hand hand)
    {
        _piles[0].setComparator(_cmp);
        _piles[0].setSpacing(25);
        _piles[0].setHand(hand, FADE_DURATION);
        for (Card card : hand) {
            _piles[0].getSprite(card).addSpriteObserver(new CardSpriteAdapter(){
                public void cardSpritePopup (CardSprite sprite, MouseEvent me) {
                    _popup.show(me.getComponent(), me.getX(), me.getY());
                }
            });
        }
        _logic = new BigTwoLogic(hand);
    }

    public void attributeChanged(AttributeChangedEvent event)
    {
        attributeChanged(event.getName());
    }

    private void attributeChanged(String name)
    {
        if (name.equals(BigTwoObject.TOP_HAND)) {
            if (_gameobj.topHand == null) {
                if (_gameobj.owner < 0) {
                    _top.clearHand();
                } else {
                    _top.setFacingUp(false);
                    _top.setSpacing(0);
                    _top.adjustHand(FLIGHT_DURATION, false);
                }

            } else {
                CardSprite[] cards;
                int pileIndex = getPileIndex(_gameobj.getTurnHolderIndex(0));
                CardPile from = _piles[pileIndex];
                if (pileIndex == 0 && _selfIndex >= 0) {
                    _logic.invalidate(_gameobj.topHand);
                    // TODO: move this code into CardPile?
                    cards = new CardSprite[_gameobj.topHand.size()];
                    for (int i = 0; i < cards.length; i++) {
                        cards[i] = _piles[0].getSprite(_gameobj.topHand.get(i));
                    }
                } else {
                    cards = from.getRandomCards(_gameobj.topHand.size());
                    for (int i = 0; i < cards.length; i++) {
                        cards[i].setCard(_gameobj.topHand.get(i));
                    }
                }
                _top.setSpacing(HAND_UP_SPACING);
                CardPile.flyBetween(from, _top, cards, true, FLIGHT_DURATION);
                if (cards.length > 2) {
                    showText(_desc.describe(_gameobj.topHand),
                             HAND_FONT,
                             Color.yellow,
                             getWidth() / 2,
                             getHeight() / 3);
                }
                if (_gameobj.isBigTwo()) {
                    showText(_msgs.get("m.big_two"),
                             HAND_FONT,
                             Color.yellow,
                             getWidth() / 2,
                             getHeight() / 3 + 30);
                }
            }
        } else if (name.equals(BigTwoObject.OWNER)) {
            setText(_ownerLabel,
                    (_gameobj.owner < 0) ? "" : _gameobj.players[_gameobj.owner].toString());
            
        } else if (name.equals(BigTwoObject.WINNERS)) {
            int winnerIndex = _gameobj.getWinnerIndex();
            if (winnerIndex >= 0) {
                CardPile pile = _piles[getPileIndex(winnerIndex)];
                showText(_msgs.get("m.winner"), ACTION_FONT, Color.green, pile.getX(), pile.getY());
            }
        }
    }

    public void elementUpdated(ElementUpdatedEvent event)
    {
        String name = event.getName();
        if (name.equals(BigTwoObject.PLAYERS))
            updateLabel(event.getIndex());
    }

    // rotate indices around so that we are on the bottom
    private int getPileIndex(int pidx)
    {
        if (_selfIndex <= 0)
            return pidx;
        return _gameobj.getAdjacentPlayer(pidx, -_selfIndex);
    }

    private void updateLabel(int pidx)
    {
        setText(_labels[getPileIndex(pidx)], _gameobj.players[pidx].toString());
    }

    private void setText(LabelSprite sprite, String text)
    {
        sprite.invalidate();
        Label label = sprite.getLabel();
        label.setText(text);
        label.layout(this);
        sprite.updateBounds();
        sprite.invalidate();
    }

    public void gameDidStart()
    {
        _selfIndex = _gameobj.getPlayerIndex(_self.username);
        int count = _gameobj.players.length;
        for (int i = 0; i < count; i++) {
            updateLabel(i);
            if (i != _selfIndex) {
                int pileIndex = getPileIndex(i);
                _piles[pileIndex].setComparator(null);
                _piles[pileIndex].setSpacing(HAND_DOWN_SPACING);
                _piles[pileIndex].setHand(_gameobj.cardCounts[i], FADE_DURATION);
            }
        }
    }

    public void turnDidChange(Name turnHolder, boolean ourTurn)
    {
        int prevIndex = _gameobj.getTurnHolderIndex(-1);
        if (_gameobj.owner >= 0 && prevIndex != _gameobj.owner) {
            // TODO: this may show extra "Pass" when AI player takes over
            CardPile pile = _piles[getPileIndex(prevIndex)];
            showText(_msgs.get("m.pass"), ACTION_FONT, Color.green, pile.getX(), pile.getY());
        }
        if (_selfIndex >= 0) {
            _piles[0].clearSelection();
            _piles[0].setSelectionMode(CardPile.NONE);
            if (ourTurn) {
                Hand hand = _logic.chooseHand(_gameobj, _selfIndex);
                if (_autoPass && hand == null) {
                    // we don't pass if the hand is unbeatable because we know the server will
                    if (!_gameobj.isUnbeatable(_piles[0].getCount()))
                        Controller.postAction(this, BigTwoCodes.PASS, null);
                } else if (hand != null && hand.size() == _piles[0].getCount()) {
                    selectHand(hand);
                    Controller.postAction(this, BigTwoCodes.PLAY, null);
                } else {
                    _piles[0].setSelectionMode(CardPile.MULTIPLE);
                    if (_suggest && hand != null)
                        selectHand(hand);
                }
            }
        }
    }

    public void addNotify()
    {
        super.addNotify();
        // we must add buttons after board is visible so label layout is performed correctly
        addSprite(play);
        addSprite(pass);
        
        for (int i = 0; i < 4; i++)
            addSprite(_labels[i]);
        addSprite(_ownerLabel);
    }

    public void doLayout ()
    {
        super.doLayout();
        _piles[0].setLocation(getWidth() / 2, getHeight() - OUTER_MARGIN - _cardHeight / 2);
        _piles[1].setLocation(OUTER_MARGIN + _cardWidth / 2, getHeight() / 2);
        _piles[2].setLocation(getWidth() / 2, OUTER_MARGIN + _cardHeight / 2);
        _piles[3].setLocation(getWidth() - OUTER_MARGIN - _cardWidth / 2, getHeight() / 2);
        _top.setLocation(getWidth() / 2, getHeight() / 2);

        int sideY = _piles[1].getY() - (12 * HAND_DOWN_SPACING + _cardHeight) / 2 - LABEL_OFFSET;
        _labels[0].setLocation(_piles[0].getX(), _piles[0].getY() - _cardHeight / 2 - LABEL_OFFSET);
        _labels[1].setLocation(_piles[1].getX(), sideY);
        _labels[2].setLocation(_piles[2].getX(), _piles[2].getY() + _cardHeight / 2 + LABEL_OFFSET);
        _labels[3].setLocation(_piles[3].getX(), sideY);
        _ownerLabel.setLocation(_top.getX(), _top.getY() + _cardHeight / 2 + LABEL_OFFSET);

        int playWidth = play.getBounds().width;
        int passWidth = pass.getBounds().width;
        int playX = _top.getX() - (playWidth + passWidth + BUTTON_GAP) / 2;
        play.setLocation(playX, _ownerLabel.getY() + LABEL_OFFSET);
        pass.setLocation(playX + playWidth + BUTTON_GAP, _ownerLabel.getY() + LABEL_OFFSET);
    }

    private void selectHand(Hand hand)
    {
        _piles[0].clearSelection();
        if (hand != null) {
            for (Card card : hand)
                _piles[0].selectCard(_piles[0].getSprite(card));
        }
        Controller.postAction(this, BigTwoCodes.SELECT, hand);
    }

    private void selectHand(CardSprite[] sprites)
    {
        Hand hand = new Hand();
        for (CardSprite cs : sprites)
            hand.add(cs.getCard());
        Controller.postAction(this, BigTwoCodes.SELECT, hand);
    }

    private void showText(String message, Font font, Color color, int x, int y)
    {
        Label label = new Label(message, LabelStyleConstants.OUTLINE | LabelStyleConstants.SHADOW,
                                color, Color.black, font);
        label.layout(this);
        Dimension size = label.getSize();
        addAnimation(new FloatingTextAnimation(label, x - size.width / 2, y - size.height / 2));
    }
}
