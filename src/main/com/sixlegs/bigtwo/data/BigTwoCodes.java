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

public interface BigTwoCodes
{
    /** The message bundle identifier for translation messages. */
    public static final String BIGTWO_MSG_BUNDLE = "bigtwo";

    public static final String SORT_RANK = "sort_rank";
    public static final String SORT_SUIT = "sort_suit";
    public static final String SORT_SINGLES = "sort_singles";

    public static final String SUGGEST = "suggest";
    public static final String AUTO_PASS = "auto_pass";
    
    public static final String PLAY = "play";
    public static final String PASS = "pass";
    public static final String SELECT = "select";
    public static final String BACK_TO_LOBBY = "back_to_lobby";
}
