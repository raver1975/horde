/*
 * Copyright (c) 2008, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */

package edu.uci.ics.jung.io.graphml.parser;

import com.google.common.base.Function;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.io.graphml.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for all element parsers.
 * 
 * @author Nathan Mittler - nathan.mittler@gmail.com
 */
public class ElementParserRegistry<G extends Hypergraph<V, E>, V, E> {

    final private Map<String, ElementParser> parserMap = new HashMap<String, ElementParser>();

    final private ElementParser unknownElementParser = new UnknownElementParser();

    public ElementParserRegistry(KeyMap keyMap, 
            Function<GraphMetadata, G> graphTransformer,
            Function<NodeMetadata, V> vertexTransformer,
            Function<EdgeMetadata, E> edgeTransformer,
            Function<HyperEdgeMetadata, E> hyperEdgeTransformer) {
        
        // Create the parser context.
        ParserContext<G,V,E> context = new ParserContext<G,V,E>(this, keyMap, graphTransformer, 
                vertexTransformer, edgeTransformer, hyperEdgeTransformer);
    
        parserMap.put(GraphMLConstants.DEFAULT_NAME, new StringElementParser<G,V,E>(context));
        parserMap.put(GraphMLConstants.DESC_NAME, new StringElementParser<G,V,E>(context));
        parserMap.put(GraphMLConstants.KEY_NAME, new KeyElementParser<G,V,E>(context));
        parserMap.put(GraphMLConstants.DATA_NAME, new DataElementParser<G,V,E>(context));
        parserMap.put(GraphMLConstants.PORT_NAME, new PortElementParser<G,V,E>(context));
        parserMap.put(GraphMLConstants.NODE_NAME, new NodeElementParser<G,V,E>(context));
        parserMap.put(GraphMLConstants.GRAPH_NAME, new GraphElementParser<G,V,E>(context));
        parserMap.put(GraphMLConstants.ENDPOINT_NAME, new EndpointElementParser<G,V,E>(context));
        parserMap.put(GraphMLConstants.EDGE_NAME, new EdgeElementParser<G,V,E>(context));
        parserMap.put(GraphMLConstants.HYPEREDGE_NAME, new HyperEdgeElementParser<G,V,E>(context));
    }

    public ElementParser getUnknownElementParser() {
        return unknownElementParser;
    }

    public ElementParser getParser(String localName) {
        ElementParser parser = parserMap.get(localName);
        if (parser == null) {
            parser = unknownElementParser;
        }

        return parser;
    }
}
