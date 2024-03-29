/*
 * Copyright (c) 2005, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 * Created on Aug 23, 2005
 */
package edu.uci.ics.jung.visualization.renderers;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

public class BasicEdgeLabelRenderer<V,E> implements Renderer.EdgeLabel<V,E> {
	
	public Component prepareRenderer(RenderContext<V,E> rc, EdgeLabelRenderer graphLabelRenderer, Object value, 
			boolean isSelected, E edge) {
		return rc.getEdgeLabelRenderer().getEdgeLabelRendererComponent(rc.getScreenDevice(), value,
				rc.getEdgeFontTransformer().apply(edge), isSelected, edge);
	}
    
    public void labelEdge(RenderContext<V,E> rc, Layout<V,E> layout, E e, String label) {
    	if(label == null || label.length() == 0) return;
    	
    	Graph<V,E> graph = layout.getGraph();
        // don't draw edge if either incident vertex is not drawn
        Pair<V> endpoints = graph.getEndpoints(e);
        V v1 = endpoints.getFirst();
        V v2 = endpoints.getSecond();
        if (!rc.getEdgeIncludePredicate().apply(Context.getInstance(graph,e)))
            return;

        if (!rc.getVertexIncludePredicate().apply(Context.getInstance(graph,v1)) ||
            !rc.getVertexIncludePredicate().apply(Context.getInstance(graph,v2)))
            return;

        Point2D p1 = layout.apply(v1);
        Point2D p2 = layout.apply(v2);
        p1 = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p1);
        p2 = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p2);
        float x1 = (float) p1.getX();
        float y1 = (float) p1.getY();
        float x2 = (float) p2.getX();
        float y2 = (float) p2.getY();

        GraphicsDecorator g = rc.getGraphicsContext();
        float distX = x2 - x1;
        float distY = y2 - y1;
        double totalLength = Math.sqrt(distX * distX + distY * distY);

        double closeness = rc.getEdgeLabelClosenessTransformer().apply(Context.getInstance(graph, e)).doubleValue();

        int posX = (int) (x1 + (closeness) * distX);
        int posY = (int) (y1 + (closeness) * distY);

        int xDisplacement = (int) (rc.getLabelOffset() * (distY / totalLength));
        int yDisplacement = (int) (rc.getLabelOffset() * (-distX / totalLength));
        
        Component component = prepareRenderer(rc, rc.getEdgeLabelRenderer(), label, 
                rc.getPickedEdgeState().isPicked(e), e);
        
        Dimension d = component.getPreferredSize();

        Shape edgeShape = rc.getEdgeShapeTransformer().apply(e);
        
        double parallelOffset = 1;

        parallelOffset += rc.getParallelEdgeIndexFunction().getIndex(null, e);

        parallelOffset *= d.height;
        if(edgeShape instanceof Ellipse2D) {
            parallelOffset += edgeShape.getBounds().getHeight();
            parallelOffset = -parallelOffset;
        }
        
        
        AffineTransform old = g.getTransform();
        AffineTransform xform = new AffineTransform(old);
        xform.translate(posX+xDisplacement, posY+yDisplacement);
        double dx = x2 - x1;
        double dy = y2 - y1;
        if(rc.getEdgeLabelRenderer().isRotateEdgeLabels()) {
            double theta = Math.atan2(dy, dx);
            if(dx < 0) {
                theta += Math.PI;
            }
            xform.rotate(theta);
        }
        if(dx < 0) {
            parallelOffset = -parallelOffset;
        }
        
        xform.translate(-d.width/2, -(d.height/2-parallelOffset));
        g.setTransform(xform);
        g.draw(component, rc.getRendererPane(), 0, 0, d.width, d.height, true);

        g.setTransform(old);
    }

}
