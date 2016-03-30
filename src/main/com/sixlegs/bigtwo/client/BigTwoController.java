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

import com.samskivert.swing.event.CommandEvent;
import com.sixlegs.bigtwo.data.*;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;
import com.threerings.parlor.card.client.CardGameController;
import com.threerings.parlor.card.data.*;
import com.threerings.parlor.turn.client.TurnGameControllerDelegate;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.dobj.MessageListener;
import com.threerings.toybox.util.ToyBoxContext;
import com.threerings.util.MessageBundle;
import com.threerings.util.Name;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import static com.sixlegs.bigtwo.data.BigTwoCodes.*;

/**
 * Manages the client side mechanics of the game.
 */
public class BigTwoController
extends CardGameController
{

    /** Our game panel. */
    protected BigTwoPanel _panel;

    /** Our game distributed object. */
    protected BigTwoObject _gameobj;

    private MessageBundle _msgs;
    private BodyObject _self;
    private TurnGameControllerDelegate _delegate;
    private Hand _selected;

    public BigTwoController()
    {
        addDelegate(_delegate = new TurnGameControllerDelegate(this));
    }

    @Override public void willEnterPlace (PlaceObject plobj)
    {
        super.willEnterPlace(plobj);
        _gameobj = (BigTwoObject)plobj;
    }

    public void receivedHand(Hand hand)
    {
        _panel.board.receivedHand(hand);
    }

    @Override public void gameDidStart()
    {
        _panel.board.gameDidStart();
    }

    public void turnDidChange(Name turnHolder)
    {
        // we disable the pass button if the top hand is unbeatable because the
        // server will end up passing for us
        int turnHolderIndex = _delegate.getTurnHolderIndex();
        _panel.board.pass.setEnabled(_delegate.isOurTurn() &&
                                     _gameobj.owner >= 0 &&
                                     _gameobj.owner != turnHolderIndex &&
                                     !_gameobj.isUnbeatable(_gameobj.cardCounts[turnHolderIndex]));
        _panel.board.turnDidChange(turnHolder, _delegate.isOurTurn());
    }

    @Override public void didLeavePlace (PlaceObject plobj)
    {
        super.didLeavePlace(plobj);
        _gameobj = null;
    }

    @Override protected PlaceView createPlaceView (CrowdContext ctx)
    {
        _panel = new BigTwoPanel((ToyBoxContext)ctx, this);
        _msgs = ((ToyBoxContext)ctx).getMessageManager().getBundle(BigTwoCodes.BIGTWO_MSG_BUNDLE);
        _self = (BodyObject)ctx.getClient().getClientObject();
        return _panel;
    }

    public boolean handleAction(ActionEvent action)
    {
        String cmd = action.getActionCommand();
        if (cmd.equals(PLAY)) {
            _gameobj.manager.invoke("play", _selected);
            anticipateEndTurn();
        } else if (cmd.equals(PASS)) {
            _gameobj.manager.invoke("pass");
            anticipateEndTurn();
        } else if (cmd.equals(SELECT)) {
            _selected = (Hand)((CommandEvent)action).getArgument();
            _panel.board.play.setEnabled(_gameobj.isPlayable(_selected));
        } else if (cmd.equals(BACK_TO_LOBBY)) {
            _ctx.getLocationDirector().moveBack();
        } else if (cmd.equals(SUGGEST)) {
            _panel.board.setSuggest(((JCheckBox)action.getSource()).isSelected());
        } else if (cmd.equals(AUTO_PASS)) {
            _panel.board.setAutoPass(((JCheckBox)action.getSource()).isSelected());
        } else {
            return super.handleAction(action);
        }
        return true;
    }

    private void anticipateEndTurn()
    {
        _panel.board.play.setEnabled(false);
        _panel.board.pass.setEnabled(false);
    }
}
