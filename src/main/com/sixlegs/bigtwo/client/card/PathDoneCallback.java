//
// $Id: PathDoneCallback.java 9 2005-08-15 05:46:43Z herbyderby $
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

import com.threerings.media.sprite.PathObserver;
import com.threerings.media.sprite.Sprite;
import com.threerings.media.util.Path;

abstract public class PathDoneCallback
implements PathObserver
{
    public void pathCompleted (Sprite sprite, Path path, long when)
    {
        pathDone(sprite, path, true, when);
    }

    public void pathCancelled(Sprite sprite, Path path)
    {
        pathDone(sprite, path, false, 0);
    }

    abstract public void pathDone(Sprite sprite, Path path, boolean completed, long when);
}
          
