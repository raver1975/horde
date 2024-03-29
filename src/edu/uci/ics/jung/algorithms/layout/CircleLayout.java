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
 * Created on Dec 4, 2003
 */
package edu.uci.ics.jung.algorithms.layout;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import edu.uci.ics.jung.graph.Graph;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * A {@code Layout} implementation that positions vertices equally spaced on a regular circle.
 *
 * @author Masanori Harada
 */
public class CircleLayout<V, E> extends AbstractLayout<V,E> {

	private double radius;
	private List<V> vertex_ordered_list;
	
    protected LoadingCache<V, CircleVertexData> circleVertexDatas =
    	CacheBuilder.newBuilder().build(new CacheLoader<V, CircleVertexData>() {
	    	public CircleVertexData load(V vertex) {
	    		return new CircleVertexData();
	    	}
    });

	public CircleLayout(Graph<V,E> g) {
		super(g);
	}

	/**
	 * @return the radius of the circle.
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * Sets the radius of the circle.  Must be called before {@code initialize()} is called.
	 * @param radius the radius of the circle
	 */
	public void setRadius(double radius) {
		this.radius = radius;
	}

	/**
	 * Sets the order of the vertices in the layout according to the ordering
	 * specified by {@code comparator}.
	 * @param comparator the comparator to use to order the vertices
	 */
	public void setVertexOrder(Comparator<V> comparator)
	{
	    if (vertex_ordered_list == null)
	        vertex_ordered_list = new ArrayList<V>(getGraph().getVertices());
	    Collections.sort(vertex_ordered_list, comparator);
	}

    /**
     * Sets the order of the vertices in the layout according to the ordering
     * of {@code vertex_list}.
     * @param vertex_list a list specifying the ordering of the vertices
     */
	public void setVertexOrder(List<V> vertex_list)
	{
	    if (!vertex_list.containsAll(getGraph().getVertices())) 
	        throw new IllegalArgumentException("Supplied list must include " +
	        		"all vertices of the graph");
	    this.vertex_ordered_list = vertex_list;
	}
	
	public void reset() {
		initialize();
	}

	public void initialize() 
	{
		Dimension d = getSize();
		
		if (d != null) 
		{
		    if (vertex_ordered_list == null) 
		        setVertexOrder(new ArrayList<V>(getGraph().getVertices()));

			double height = d.getHeight();
			double width = d.getWidth();

			if (radius <= 0) {
				radius = 0.45 * (height < width ? height : width);
			}

			int i = 0;
			for (V v : vertex_ordered_list)
			{
				Point2D coord = apply(v);

				double angle = (2 * Math.PI * i) / vertex_ordered_list.size();

				coord.setLocation(Math.cos(angle) * radius + width / 2,
						Math.sin(angle) * radius + height / 2);

				CircleVertexData data = getCircleData(v);
				data.setAngle(angle);
				i++;
			}
		}
	}

	protected CircleVertexData getCircleData(V v) {
		return circleVertexDatas.getUnchecked(v);
	}

	protected static class CircleVertexData {
		private double angle;

		protected double getAngle() {
			return angle;
		}

		protected void setAngle(double angle) {
			this.angle = angle;
		}

		@Override
		public String toString() {
			return "CircleVertexData: angle=" + angle;
		}
	}
}
