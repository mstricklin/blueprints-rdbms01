package edu.utexas.arlut.ciads;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.rdbms.RdbmsGraph;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
    public static void main( String[] args ) {

        RdbmsGraph g = new RdbmsGraph();
        Vertex v0 = g.addVertex(null);
        Vertex v1 = g.addVertex(null);
        g.addEdge(null, v0, v1, "sam");

        Vertex v4 = g.addVertex(null);
//        g.dump();
        g.cacheDump();
        log.info("");

        g.removeVertex(v4);
        g.cacheDump();

        g.commit();
        log.info("");
        g.cacheDump();

        v0 = g.addVertex(null);
        log.info("");
        g.cacheDump();
    }
}
