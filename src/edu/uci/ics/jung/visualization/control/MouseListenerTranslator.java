/*
 * Copyright (c) 2003, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
/*
 * Created on Feb 17, 2004
 */
package edu.uci.ics.jung.visualization.control;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

/**
 * This class translates mouse clicks into vertex clicks
 * 
 * @author danyelf
 */
public class MouseListenerTranslator<V, E> extends MouseAdapter {

	private VisualizationViewer<V,E> vv;
	private GraphMouseListener<V> gel;

	/**
	 * @param gel listens for mouse events
	 * @param vv the viewer used for visualization
	 */
	public MouseListenerTranslator(GraphMouseListener<V> gel, VisualizationViewer<V,E> vv) {
		this.gel = gel;
		this.vv = vv;
	}
	
	/**
	 * Transform the point to the coordinate system in the
	 * VisualizationViewer, then use either PickSuuport
	 * (if available) or Layout to find a Vertex
	 * @param point
	 * @return
	 */
	private V getVertex(Point2D point) {
	    // adjust for scale and offset in the VisualizationViewer
	    Point2D p = point;
	    	//vv.getRenderContext().getBasicTransformer().inverseViewTransform(point);
	    GraphElementAccessor<V,E> pickSupport = vv.getPickSupport();
        Layout<V,E> layout = vv.getGraphLayout();
	    V v = null;
	    if(pickSupport != null) {
	        v = pickSupport.getVertex(layout, p.getX(), p.getY());
	    } 
	    return v;
	}
	/**
	 * @see java.awt.event.MouseListener#mouseClicked(MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
	    V v = getVertex(e.getPoint());
		if ( v != null ) {
			gel.graphClicked(v, e );
		}
	}

	/**
	 * @see java.awt.event.MouseListener#mousePressed(MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		V v = getVertex(e.getPoint());
		if ( v != null ) {
			gel.graphPressed(v, e );
		}
	}

	/**
	 * @see java.awt.event.MouseListener#mouseReleased(MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		V v = getVertex(e.getPoint());
		if ( v != null ) {
			gel.graphReleased(v, e );
		}
	}
}
