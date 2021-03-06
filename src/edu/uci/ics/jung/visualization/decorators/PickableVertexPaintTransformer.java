/*
* Created on Mar 10, 2005
*
* Copyright (c) 2005, The JUNG Authors 
*
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* https://github.com/jrtom/jung/blob/master/LICENSE for a description.
*/
package edu.uci.ics.jung.visualization.decorators;

import com.google.common.base.Function;
import edu.uci.ics.jung.visualization.picking.PickedInfo;

import java.awt.*;

/**
 * Paints each vertex according to the <code>Paint</code>
 * parameters given in the constructor, so that picked and
 * non-picked vertices can be made to look different.
 */
public class PickableVertexPaintTransformer<V> implements Function<V,Paint> {

    protected Paint fill_paint;
    protected Paint picked_paint;
    protected PickedInfo<V> pi;
    
    /**
     * 
     * @param pi            specifies which vertices report as "picked"
     * @param fill_paint    <code>Paint</code> used to fill vertex shapes
     * @param picked_paint  <code>Paint</code> used to fill picked vertex shapes
     */
    public PickableVertexPaintTransformer(PickedInfo<V> pi, 
    		Paint fill_paint, Paint picked_paint)
    {
        if (pi == null)
            throw new IllegalArgumentException("PickedInfo instance must be non-null");
        this.pi = pi;
        this.fill_paint = fill_paint;
        this.picked_paint = picked_paint;
    }

    public Paint apply(V v)
    {
        if (pi.isPicked(v))
            return picked_paint;
        else
            return fill_paint;
    }

}
