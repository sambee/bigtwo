//
// $Id: CardSpriteComparator.java 18 2006-11-14 02:56:48Z herbyderby $
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

import com.threerings.parlor.card.data.Card;
import java.util.Comparator;

class CardSpriteComparator
implements Comparator<CardSprite>
{
    private Comparator<Card> _delegate;

    public CardSpriteComparator(Comparator<Card> cmp)
    {
        _delegate = cmp;
    }

    public int compare(CardSprite cs1, CardSprite cs2)
    {
        return _delegate.compare((cs1 != null) ? cs1.getCard() : null,
                                 (cs2 != null) ? cs2.getCard() : null);
    }
}
