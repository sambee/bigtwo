//
// $Id: OrientableImageSprite.java 3 2005-08-15 05:31:31Z herbyderby $
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

import com.threerings.media.image.Mirage;
import com.threerings.media.sprite.*;
import com.threerings.media.util.MultiFrameImage;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.*;

/**
 * An image sprite that uses AWT's rotation methods to render itself in
 * different orientations.
 */
public class OrientableImageSprite extends ImageSprite
{
    /**
     * Creates a new orientable image sprite.
     */
    public OrientableImageSprite ()
    {}
    
    /**
     * Creates a new orientable image sprite.
     *
     * @param image the image to render
     */
    public OrientableImageSprite (Mirage image)
    {
        super(image);
    }
    
    /**
     * Creates a new orientable image sprite.
     *
     * @param frames the frames to render
     */
    public OrientableImageSprite (MultiFrameImage frames)
    {
        super(frames);
    }
    
    /**
     * Computes and the rotation transform for the
     * given orientation.
     */ 
    public static double getAngle(int orient)
    {
        switch (orient) {
            case NORTH:
            default:
                return 0;
            
            case SOUTH:
                return Math.PI;
                    
            case EAST:
                return Math.PI*0.5;
                
            case WEST:
                return -Math.PI*0.5;
                
            case NORTHEAST:
                return Math.PI*0.25;
            
            case NORTHWEST:
                return -Math.PI*0.25;
                
            case SOUTHEAST:
                return Math.PI*0.75;
                
            case SOUTHWEST:
                return -Math.PI*0.75;
                
            case NORTHNORTHEAST:
                return Math.PI*0.125;
                
            case NORTHNORTHWEST:
                return -Math.PI*0.125;
                
            case SOUTHSOUTHEAST:
                return Math.PI*0.875;
                
            case SOUTHSOUTHWEST:
                return -Math.PI*0.875;
                
            case EASTNORTHEAST:
                return Math.PI*0.375;
                
            case EASTSOUTHEAST:
                return Math.PI*0.625;
                
            case WESTNORTHWEST:
                return -Math.PI*0.375;
                
            case WESTSOUTHWEST:
                return -Math.PI*0.625;
        }
    }        
    
    // Documentation inherited.
    protected void updateRenderOrigin ()
    {
        int frameWidth = _frames.getWidth(_frameIdx);
        int frameHeight = _frames.getHeight(_frameIdx);
        if (_orient == NORTH) {
            _transform.setToIdentity();
        } else {
            _transform.setToRotation(getAngle(_orient),
                                     _ox - _oxoff + frameWidth / 2,
                                     _oy - _oyoff + frameHeight / 2);
        }
        _unrotated = new Rectangle(_ox - _oxoff,
                                   _oy - _oyoff,
                                   frameWidth,
                                   frameHeight);
        if (_orient == NORTH) {
            _bounds = _unrotated;
        } else {
            _bounds = _transform.createTransformedShape(_unrotated).getBounds();
        }
    }

    // Documentation inherited.
    protected void accomodateFrame (int frameIdx, int width, int height)
    {
        updateRenderOrigin();
    }
    
    // Documentation inherited.
    public void setOrientation (int orient)
    {
        if (_orient != orient) {
            super.setOrientation(orient);
            layout();
        }
    }

    // Documentation inherited.
    public void paint (Graphics2D gfx)
    {
        if (_orient == NORTH) {
            super.paint(gfx);
        } else {
            AffineTransform save = gfx.getTransform();
            gfx.transform(_transform);
            if (_frames != null) {
                _frames.paintFrame(gfx, _frameIdx, _ox - _oxoff, _oy - _oyoff);
            } else {
                super.paint(gfx);
            }
            gfx.setTransform(save);
        }
    }

    // Documentation inherited.
    public boolean hitTest (int x, int y)
    {
        try {
            if (_orient == NORTH) {
                return super.hitTest(x, y);
            }
            if (!_bounds.contains(x, y) || _frames == null) {
                return false;
            }
            Point point = new Point(x, y);
            _transform.inverseTransform(point, point);
            if (!_unrotated.contains(point.x, point.y)) {
                return false;
            }
            return _frames.hitTest(_frameIdx,
                                   point.x - (_ox - _oxoff),
                                   point.y - (_oy - _oyoff));

        } catch (NoninvertibleTransformException e) {
            throw new RuntimeException("Noninvertible transform?");
        }
    }

    protected Rectangle _unrotated;
    protected AffineTransform _transform = new AffineTransform();
}
