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

import com.google.common.base.Function;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

/**
 * Renders Vertex Labels, but can also supply Shapes for vertices.
 * This has the effect of making the vertex segment the actual vertex
 * shape. The user will probably want to center the vertex segment
 * on the vertex location.
 * 
 * @author Tom Nelson
 *
 * @param <V> the vertex type
 * @param <V> the edge type
 */
public class VertexLabelAsShapeRenderer<V,E> 
	implements Renderer.VertexLabel<V,E>, Function<V,Shape> {

	protected Map<V,Shape> shapes = new HashMap<V,Shape>();
	protected RenderContext<V,E> rc;
	
	public VertexLabelAsShapeRenderer(RenderContext<V, E> rc) {
		this.rc = rc;
	}

	public Component prepareRenderer(RenderContext<V,E> rc, VertexLabelRenderer graphLabelRenderer, Object value, 
			boolean isSelected, V vertex) {
		return rc.getVertexLabelRenderer().getVertexLabelRendererComponent(rc.getScreenDevice(), value,
				rc.getVertexFontTransformer().apply(vertex), isSelected, vertex);
	}

	/**
	 * Labels the specified vertex with the specified segment.
	 * Uses the font specified by this instance's 
	 * <code>VertexFontFunction</code>.  (If the font is unspecified, the existing
	 * font for the graphics context is used.)  If vertex segment centering
	 * is active, the segment is centered on the position of the vertex; otherwise
     * the segment is offset slightly.
     */
    public void labelVertex(RenderContext<V,E> rc, Layout<V,E> layout, V v, String label) {
    	Graph<V,E> graph = layout.getGraph();
        if (rc.getVertexIncludePredicate().apply(Context.getInstance(graph,v)) == false) {
        	return;
        }
        GraphicsDecorator g = rc.getGraphicsContext();
        Component component = prepareRenderer(rc, rc.getVertexLabelRenderer(), label,
        		rc.getPickedVertexState().isPicked(v), v);
        Dimension d = component.getPreferredSize();
        
        int h_offset = -d.width / 2;
        int v_offset = -d.height / 2;
        
        Point2D p = layout.apply(v);
        p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p);

        int x = (int)p.getX();
        int y = (int)p.getY();

        g.draw(component, rc.getRendererPane(), x+h_offset, y+v_offset, d.width, d.height, true);

        Dimension size = component.getPreferredSize();
        Rectangle bounds = new Rectangle(-size.width/2 -2, -size.height/2 -2, size.width+4, size.height);
        shapes.put(v, bounds);
    }

	public Shape apply(V v) {
		Component component = prepareRenderer(rc, rc.getVertexLabelRenderer(), rc.getVertexLabelTransformer().apply(v),
				rc.getPickedVertexState().isPicked(v), v);
        Dimension size = component.getPreferredSize();
        Rectangle bounds = new Rectangle(-size.width/2 -2, -size.height/2 -2, size.width+4, size.height);
        return bounds;
//		Shape shape = shapes.get(v);
//		if(shape == null) {
//			return new Rectangle(-20,-20,40,40);
//		}
//		else return shape;
	}

	public Renderer.VertexLabel.Position getPosition() {
		return Renderer.VertexLabel.Position.CNTR;
	}

	public Renderer.VertexLabel.Positioner getPositioner() {
		return new Positioner() {
			public Renderer.VertexLabel.Position getPosition(float x, float y, Dimension d) {
				return Renderer.VertexLabel.Position.CNTR;
			}};
	}

	public void setPosition(Renderer.VertexLabel.Position position) {
		// noop
	}

	public void setPositioner(Renderer.VertexLabel.Positioner positioner) {
		//noop
	}
}
