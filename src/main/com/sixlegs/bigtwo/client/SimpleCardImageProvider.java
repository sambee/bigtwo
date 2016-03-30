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

import com.sixlegs.bigtwo.client.card.CardImageProvider;
import com.threerings.media.image.BufferedMirage;
import com.threerings.media.image.Mirage;
import com.threerings.parlor.card.data.Card;
import com.threerings.parlor.card.data.CardCodes;
import java.awt.image.BufferedImage;

class SimpleCardImageProvider
implements CardImageProvider
{
    private BufferedMirage[][] cards;
    private BufferedMirage[][] micro;
    
    public SimpleCardImageProvider(BufferedImage cardsImage, BufferedImage microImage)
    {
        cards = splitImage(cardsImage, 4, 14);
        micro = splitImage(microImage, 4, 14);
    }

    private static BufferedMirage[][] splitImage(BufferedImage img, int rows, int columns)
    {
        int h = img.getHeight() / rows;
        int w = img.getWidth() / columns;
        BufferedMirage[][] array = new BufferedMirage[rows][columns];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                array[y][x] = new BufferedMirage(img.getSubimage(x * w, y * h, w, h));
            }
        }
        return array;
    }
    
    public Mirage getCardImage(Card card)
    {
        return getCardImage(cards, card);
    }

    public Mirage getMicroCardImage(Card card)
    {
        return getCardImage(micro, card);
    }

    public Mirage getCardBackImage()
    {
        return cards[0][13];
    }

    public Mirage getMicroCardBackImage()
    {
        return micro[0][13];
    }

    private static Mirage getCardImage(BufferedMirage[][] images, Card card)
    {
        if (card.isJoker()) {
            return images[(card.getNumber() == CardCodes.RED_JOKER) ? 3 : 2][13];
        } else {
            return images[card.getSuit()][card.getNumber() - 2];
        }
    }
}
