package edu.uci.ics.jung.graph;

import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

import java.io.Serializable;
import java.util.Collection;

/**
 * An implementation of <code>Graph</code> that delegates its method calls to a
 * constructor-specified <code>Graph</code> instance.  This is useful for adding
 * additional behavior (such as synchronization or unmodifiability) to an existing
 * instance.
 */
@SuppressWarnings("serial")
public class GraphDecorator<V,E> implements Graph<V,E>, Serializable {
	
	protected Graph<V,E> delegate;

    /**
     * Creates a new instance based on the provided {@code delegate}.
     * @param delegate the graph to which method calls will be delegated
     */
	public GraphDecorator(Graph<V, E> delegate) {
		this.delegate = delegate;
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#addEdge(Object, Collection)
	 */
	public boolean addEdge(E edge, Collection<? extends V> vertices) {
		return delegate.addEdge(edge, vertices);
	}

	/**
	 * @see Hypergraph#addEdge(Object, Collection, EdgeType)
	 */
	public boolean addEdge(E edge, Collection<? extends V> vertices, EdgeType
		edge_type)
	{
		return delegate.addEdge(edge, vertices, edge_type);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#addEdge(Object, Object, Object, edu.uci.ics.jung.graph.util.EdgeType)
	 */
	public boolean addEdge(E e, V v1, V v2, EdgeType edgeType) {
		return delegate.addEdge(e, v1, v2, edgeType);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#addEdge(Object, Object, Object)
	 */
	public boolean addEdge(E e, V v1, V v2) {
		return delegate.addEdge(e, v1, v2);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#addVertex(Object)
	 */
	public boolean addVertex(V vertex) {
		return delegate.addVertex(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#isIncident(Object, Object)
	 */
	public boolean isIncident(V vertex, E edge) {
		return delegate.isIncident(vertex, edge);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#isNeighbor(Object, Object)
	 */
	public boolean isNeighbor(V v1, V v2) {
		return delegate.isNeighbor(v1, v2);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#degree(Object)
	 */
	public int degree(V vertex) {
		return delegate.degree(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#findEdge(Object, Object)
	 */
	public E findEdge(V v1, V v2) {
		return delegate.findEdge(v1, v2);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#findEdgeSet(Object, Object)
	 */
	public Collection<E> findEdgeSet(V v1, V v2) {
		return delegate.findEdgeSet(v1, v2);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getDest(Object)
	 */
	public V getDest(E directed_edge) {
		return delegate.getDest(directed_edge);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#getEdgeCount()
	 */
	public int getEdgeCount() {
		return delegate.getEdgeCount();
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#getEdgeCount(EdgeType)
	 */
	public int getEdgeCount(EdgeType edge_type)
	{
	    return delegate.getEdgeCount(edge_type);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#getEdges()
	 */
	public Collection<E> getEdges() {
		return delegate.getEdges();
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getEdges(edu.uci.ics.jung.graph.util.EdgeType)
	 */
	public Collection<E> getEdges(EdgeType edgeType) {
		return delegate.getEdges(edgeType);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getEdgeType(Object)
	 */
	public EdgeType getEdgeType(E edge) {
		return delegate.getEdgeType(edge);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getDefaultEdgeType()
	 */
	public EdgeType getDefaultEdgeType()
	{
		return delegate.getDefaultEdgeType();
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getEndpoints(Object)
	 */
	public Pair<V> getEndpoints(E edge) {
		return delegate.getEndpoints(edge);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#getIncidentCount(Object)
	 */
	public int getIncidentCount(E edge) {
		return delegate.getIncidentCount(edge);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#getIncidentEdges(Object)
	 */
	public Collection<E> getIncidentEdges(V vertex) {
		return delegate.getIncidentEdges(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#getIncidentVertices(Object)
	 */
	public Collection<V> getIncidentVertices(E edge) {
		return delegate.getIncidentVertices(edge);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getInEdges(Object)
	 */
	public Collection<E> getInEdges(V vertex) {
		return delegate.getInEdges(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#getNeighborCount(Object)
	 */
	public int getNeighborCount(V vertex) {
		return delegate.getNeighborCount(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#getNeighbors(Object)
	 */
	public Collection<V> getNeighbors(V vertex) {
		return delegate.getNeighbors(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getOpposite(Object, Object)
	 */
	public V getOpposite(V vertex, E edge) {
		return delegate.getOpposite(vertex, edge);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getOutEdges(Object)
	 */
	public Collection<E> getOutEdges(V vertex) {
		return delegate.getOutEdges(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getPredecessorCount(Object)
	 */
	public int getPredecessorCount(V vertex) {
		return delegate.getPredecessorCount(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getPredecessors(Object)
	 */
	public Collection<V> getPredecessors(V vertex) {
		return delegate.getPredecessors(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getSource(Object)
	 */
	public V getSource(E directed_edge) {
		return delegate.getSource(directed_edge);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getSuccessorCount(Object)
	 */
	public int getSuccessorCount(V vertex) {
		return delegate.getSuccessorCount(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#getSuccessors(Object)
	 */
	public Collection<V> getSuccessors(V vertex) {
		return delegate.getSuccessors(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#getVertexCount()
	 */
	public int getVertexCount() {
		return delegate.getVertexCount();
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#getVertices()
	 */
	public Collection<V> getVertices() {
		return delegate.getVertices();
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#inDegree(Object)
	 */
	public int inDegree(V vertex) {
		return delegate.inDegree(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#isDest(Object, Object)
	 */
	public boolean isDest(V vertex, E edge) {
		return delegate.isDest(vertex, edge);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#isPredecessor(Object, Object)
	 */
	public boolean isPredecessor(V v1, V v2) {
		return delegate.isPredecessor(v1, v2);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#isSource(Object, Object)
	 */
	public boolean isSource(V vertex, E edge) {
		return delegate.isSource(vertex, edge);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#isSuccessor(Object, Object)
	 */
	public boolean isSuccessor(V v1, V v2) {
		return delegate.isSuccessor(v1, v2);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Graph#outDegree(Object)
	 */
	public int outDegree(V vertex) {
		return delegate.outDegree(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#removeEdge(Object)
	 */
	public boolean removeEdge(E edge) {
		return delegate.removeEdge(edge);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#removeVertex(Object)
	 */
	public boolean removeVertex(V vertex) {
		return delegate.removeVertex(vertex);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#containsEdge(Object)
	 */
	public boolean containsEdge(E edge) {
		return delegate.containsEdge(edge);
	}

	/**
	 * @see edu.uci.ics.jung.graph.Hypergraph#containsVertex(Object)
	 */
	public boolean containsVertex(V vertex) {
		return delegate.containsVertex(vertex);
	}
}
