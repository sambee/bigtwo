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

import com.threerings.parlor.card.data.CardCodes;

public interface PokerCodes
extends CardCodes
{
    int STRAIGHT_FLUSH = 9;
    int FOUR_OF_A_KIND = 8;
    int FULL_HOUSE = 7;
    int FLUSH = 6;
    int STRAIGHT = 5;
    int THREE_OF_A_KIND = 4;
    int TWO_PAIR = 3;
    int PAIR = 2;
    int HIGH_CARD = 1;
    int INVALID_HAND = 0;
}
