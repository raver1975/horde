/*
 * Created on Jul 11, 2008
 *
 * Copyright (c) 2008, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.scoring.util;

import com.google.common.base.Function;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * Assigns weights to directed edges (the edge of the vertex/edge pair) depending on 
 * whether the vertex is the edge's source or its destination.
 * If the vertex v is the edge's source, assigns 1/outdegree(v).
 * Otherwise, assigns 1/indegree(w).
 * Throws <code>IllegalArgumentException</code> if the edge is not directed.
 */
public class UniformInOut<V,E> implements Function<VEPair<V,E>, Double>
{
	/**
	 * The graph for which the edge weights are defined.
	 */
    protected Graph<V,E> graph;
    
    /**
     * Creates an instance for the specified graph.
     * @param graph the graph for which the edge weights will be defined
     */
    public UniformInOut(Graph<V,E> graph)
    {
        this.graph = graph;
    }
    
    public Double apply(VEPair<V,E> ve_pair)
    {
    	V v = ve_pair.getV();
    	E e = ve_pair.getE();
    	if (graph.getEdgeType(e) != EdgeType.DIRECTED)
    		throw new IllegalArgumentException("This Function only" +
    				" operates on directed edges");
    	return 1.0 / (graph.isSource(v, e) ? 
    			graph.outDegree(v) : 
    			graph.inDegree(v));
    }
}
