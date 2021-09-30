package SpineJ.Tools.Graph;

/*************************************************************************
 *  Compilation:  javac GraphClient.java
 *  Execution:    java GraphClient graph.txt
 *  Dependencies: Graph.java
 *
 *  Typical graph-processing code.
 *
 *  % java GraphClient tinyG.txt
 *  13 13
 *  0: 6 2 1 5
 *  1: 0
 *  2: 0
 *  3: 5 4
 *  4: 5 6 3
 *  5: 3 4 0
 *  6: 0 4
 *  7: 8
 *  8: 7
 *  9: 11 10 12
 *  10: 9
 *  11: 9 12
 *  12: 11 9
 *
 *  vertex of maximum degree = 4
 *  average degree           = 2
 *  number of self loops     = 0
 *
 *************************************************************************/

public class GraphClientWeighted {

    // degree of v
    public static int degree(EdgeWeightedGraph G, int v) {
        int degree = 0;
        for (Edge w : G.adj(v)) degree++;
        return degree;
    }

    // maximum degree
    public static int maxDegree(EdgeWeightedGraph G) {
        int max = 0;
        for (int v = 0; v < G.V(); v++)
            if (degree(G, v) > max)
                max = degree(G, v);
        return max;
    }

    // average degree
    public static int avgDegree(EdgeWeightedGraph G) {
        // each edge incident on two vertices
        return 2 * G.E() / G.V();
    }

    // number of self-loops
    public static int numberOfSelfLoops(EdgeWeightedGraph G) {
       int count = 0;
       for (int v = 0; v < G.V(); v++)
           for (Edge e : G.adj(v)){
               int w = e.other(v);
               if (v == w) count++;
           }
       return count/2;   // self loop appears in adjacency list twice
    }
}

