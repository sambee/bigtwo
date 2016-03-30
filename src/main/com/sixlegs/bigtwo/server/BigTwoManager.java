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

package com.sixlegs.bigtwo.server;

import com.samskivert.util.Interval;
import com.sixlegs.bigtwo.data.*;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.PlaceManager;
import com.threerings.parlor.card.data.*;
import com.threerings.parlor.card.server.CardGameManager;
import com.threerings.parlor.game.data.GameAI;
import com.threerings.parlor.turn.server.TurnGameManager;
import com.threerings.parlor.turn.server.TurnGameManagerDelegate;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.server.PresentsServer;
import com.threerings.util.MessageBundle;
import com.threerings.util.Name;
import java.util.*;
import static com.sixlegs.bigtwo.data.BigTwoCodes.*;

/**
 * Handles the server side of the game.
 */
public class BigTwoManager
extends CardGameManager
implements TurnGameManager
{
    private static final Comparator<Card> CARD_COMPARATOR = new BigTwoCardComparator(true);

    protected BigTwoObject _gameobj;

    private BigTwoLogic[] _logic = new BigTwoLogic[4];
    private Hand[] _hands;
    private TurnGameManagerDelegate _turndel;
    private Deck _deck;
    private long _timeTurnStarted;
    private boolean _first = true;

    public BigTwoManager()
    {
        addDelegate(_turndel = new TurnGameManagerDelegate(this){
            @Override protected void setFirstTurnHolder() {
                Card lowCard = null;
                for (int i = 0; i < _hands.length; i++) {
                    Card card = Collections.min(_hands[i], CARD_COMPARATOR);
                    if (lowCard == null || CARD_COMPARATOR.compare(card, lowCard) < 0) {
                        lowCard = card;
                        _turnIdx = i;
                    }
                }
                _gameobj.setLowCard(lowCard);
            }
            @Override public void setNextTurnHolder() {
                super.setNextTurnHolder();
                // we flip over the top hand here because there is no way to determine
                // the next turn index from within turnWillStart
                if (_gameobj.topHand != null && _gameobj.owner == _turnIdx)
                    _gameobj.setTopHand(null);
            }
            @Override public void playerWasReplaced (int pidx, Name oplayer, Name nplayer) {
                super.playerWasReplaced(pidx, oplayer, nplayer);
                if (isPlayersTurn(pidx))
                    makeMove(pidx);
            }
        });
    }

    @Override public void didStartup ()
    {
        super.didStartup();
        _gameobj = (BigTwoObject)super._gameobj;
    }

    @Override protected PlaceObject createPlaceObject ()
    {
        return new BigTwoObject();
    }

    @Override protected void gameWillStart ()
    {
        // need to deal before calling super.gameWillStart so that
        // setFirstTurnHolder can look at cards
        _gameobj.startTransaction();
        _gameobj.setOwner(-1);
        _gameobj.setTopHand(null);
        _gameobj.setWinners(new boolean[4]);
        _gameobj.setCardCounts(new int[]{ 13, 13, 13, 13 });
        _gameobj.commitTransaction();

        _deck = new Deck();
        _deck.shuffle();
        _hands = dealHands(_deck, 13);
        for (int i = 0; i < 4; i++) {
            if (isAI(i))
                _logic[i] = new BigTwoLogic(_hands[i]);
        }

        if (_first) {
            _first = false;
            message("m.hint_sort");
        }
        
        super.gameWillStart();
    }

    @Override protected void gameDidEnd ()
    {
        super.gameDidEnd();

        int winnerIndex = _gameobj.getWinnerIndex();
        if (winnerIndex >= 0)
            message("m.won_the_game", _gameobj.players[winnerIndex]);
        (new Interval(PresentsServer.omgr){
            public void expired () {
                if (_gameobj.isActive())
                    startGame();
            }
        }).schedule(3000L);
    }

    protected void makeMove(int pidx)
    {
        if (_turndel.isPlayersTurn(pidx)) {
            if (isAI(pidx)) {
                Hand play = _logic[pidx].chooseHand(_gameobj, pidx);
                if (play != null) {
                    _logic[pidx].invalidate(play);
                    handlePlayRequest(play);
                    return;
                }
            }
            endTurnWithDelay(500);
        }
    }

    @Override protected void assignWinners(boolean[] winners)
    {
        super.assignWinners(winners);
        if (_gameobj.owner >= 0 && _hands[_gameobj.owner].size() == 0)
            winners[_gameobj.owner] = true;
    }

    public void turnDidStart()
    {
        _timeTurnStarted = System.currentTimeMillis();
        final int pidx = _turndel.getTurnHolderIndex();
        boolean unbeatable = _gameobj.isUnbeatable(_gameobj.cardCounts[pidx]);
        if (unbeatable || isAI(pidx)) {
            // if hand is obviously unbeatable, we auto-pass for humans and AI players alike
            (new Interval(PresentsServer.omgr){
                public void expired () {
                    if (_gameobj.isInPlay())
                        makeMove(pidx);
                }
            }).schedule(unbeatable ? 500L : 1500L);
        }
    }

    public void play(BodyObject player, Hand hand)
    {
        if (isPlayersTurn(player))
            handlePlayRequest(hand);
    }

    public void pass(BodyObject player)
    {
        if (isPlayersTurn(player))
            endTurnWithDelay(500);
    }

    private boolean isPlayersTurn(BodyObject player)
    {
        return _playerOids[_turndel.getTurnHolderIndex()] == player.getOid();
    }

    private void handlePlayRequest(Hand play)
    {
        int pidx = _turndel.getTurnHolderIndex();
        Hand full = _hands[pidx];
        if (full.containsAll(play) && _gameobj.isPlayable(play)) {
            full.removeAll(play);
            _gameobj.startTransaction();
            _gameobj.setCardCountsAt(full.size(), pidx);
            _gameobj.setOwner(pidx);
            _gameobj.setTopHand(play);
            _gameobj.commitTransaction();
            if (full.size() == 0) {
                endGame();
            } else {
                endTurnWithDelay(500);
            }
        }
    }

    private void endTurnWithDelay(final long min)
    {
        long elapsed = System.currentTimeMillis() - _timeTurnStarted;
        if (elapsed >= min) {
            _turndel.endTurn();
        } else {
            (new Interval(PresentsServer.omgr){
                public void expired () {
                    endTurnWithDelay(min);
                }
            }).schedule(min - elapsed);
        }
    }

    @Override protected void playerGameDidEnd (int pidx)
    {
        replacePlayer(pidx, new Name("AI " + (pidx + 1)));
    }

    @Override protected void playerWasReplaced(int pidx, Name oplayer, Name nplayer)
    {
        message("m.replacing_player", oplayer, nplayer);
        setAI(pidx, new GameAI(0, 0));
        _logic[pidx] = new BigTwoLogic(_hands[pidx]);
    }

    private void message(String key, Object... args)
    {
        String msg = (args.length == 0) ? key :
            MessageBundle.tcompose(key, args);
        systemMessage(BIGTWO_MSG_BUNDLE, msg);
    }
}
