/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Tools.Graph;

import java.util.ArrayList;

/*************************************************************************
 *  Compilation:  javac EdgeWeightedGraph.java
 *  Execution:    java EdgeWeightedGraph filename.txt
 *  Dependencies: Bag.java Edge.java In.java StdOut.java
 *  Data files:   http://algs4.cs.princeton.edu/43mst/tinyEWG.txt
 *
 *  An edge-weighted undirected graph, implemented using adjacency lists.
 *  Parallel edges and self-loops are permitted.
 *
 *  % java EdgeWeightedGraph tinyEWG.txt
 *  8 16
 *  0: 6-0 0.58000  0-2 0.26000  0-4 0.38000  0-7 0.16000
 *  1: 1-3 0.29000  1-2 0.36000  1-7 0.19000  1-5 0.32000
 *  2: 6-2 0.40000  2-7 0.34000  1-2 0.36000  0-2 0.26000  2-3 0.17000
 *  3: 3-6 0.52000  1-3 0.29000  2-3 0.17000
 *  4: 6-4 0.93000  0-4 0.38000  4-7 0.37000  4-5 0.35000
 *  5: 1-5 0.32000  5-7 0.28000  4-5 0.35000
 *  6: 6-4 0.93000  6-0 0.58000  3-6 0.52000  6-2 0.40000
 *  7: 2-7 0.34000  1-7 0.19000  0-7 0.16000  5-7 0.28000  4-7 0.37000
 *
 *************************************************************************/

/**
 *  The <tt>EdgeWeightedGraph</tt> class represents an undirected graph of vertices
 *  named 0 through V-1, where each edge has a real-valued weight.
 *  It supports the following operations: add an edge to the graph,
 *  in the graph, iterate over all of the neighbors incident to a vertex.
 *  Parallel edges and self-loops are permitted.
 *  <p>
 *  For additional documentation, see <a href="http://algs4.cs.princeton.edu/43mst">Section 4.3</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 */


public class EdgeWeightedGraph {
    private final int V;
    private int E;
    private Bag<Edge>[] adj;
    protected ArrayList < SeedGraph > m_seeds = null;
    protected boolean m_selected = true;

   /**
     * Create an empty edge-weighted graph with V vertices.
     */
    public EdgeWeightedGraph(int V) {
        if (V < 0) throw new RuntimeException("Number of vertices must be nonnegative");
        this.V = V;
        this.E = 0;
        adj = (Bag<Edge>[]) new Bag[V];
        for (int v = 0; v < V; v++) adj[v] = new Bag<Edge>();
    }

   /**
     * Create a random edge-weighted graph with V vertices and E edges.
     * The expected running time is proportional to V + E.
     */
    /*public EdgeWeightedGraph(int V, int E) {
        this(V);
        if (E < 0) throw new RuntimeException("Number of edges must be nonnegative");
        for (int i = 0; i < E; i++) {
            int v = (int) (Math.random() * V);
            int w = (int) (Math.random() * V);
            double weight = Math.round(100 * Math.random()) / 100.0;
            Edge e = new Edge(v, w, weight);
            addEdge(e);
        }
    }*/

   /**
     * Copy constructor.
     */
    public EdgeWeightedGraph(EdgeWeightedGraph G) {
        this(G.V());
        this.E = G.E();
        for (int v = 0; v < G.V(); v++) {
            // reverse so that adjacency list is in same order as original
            Stack<Edge> reverse = new Stack<Edge>();
            for (Edge e : G.adj[v]) {
                reverse.push(e);
            }
            for (Edge e : reverse) {
                adj[v].add(e);
            }
        }
    }

   /**
     * Return the number of vertices in this graph.
     */
    public int V() {
        return V;
    }

   /**
     * Return the number of edges in this graph.
     */
    public int E() {
        return E;
    }

    public Edge getEdge( int v, int w ){
        for( Edge e : edges() )
            if( e.sameExtremities( v, w ) )
                return e;
        return null;
    }


   /**
     * Add the edge e to this graph.
     */
    public void addEdge(Edge e) {
        int v = e.either();
        int w = e.other(v);
        adj[v].add(e);
        adj[w].add(e);
        E++;
    }

    public void removeEdge( Edge e ) {
        E--;
        adj[e.either()].remove( e );
        adj[e.other( e.either() )].remove( e );
    }


   /**
     * Return the edges incident to vertex v as an Iterable.
     * To iterate over the edges incident to vertex v, use foreach notation:
     * <tt>for (Edge e : graph.adj(v))</tt>.
     */
    public Iterable<Edge> adj(int v) {
        return adj[v];
    }

   /**
     * Return all edges in this graph as an Iterable.
     * To iterate over the edges, use foreach notation:
     * <tt>for (Edge e : graph.edges())</tt>.
     */
    public Iterable<Edge> edges() {
        Bag<Edge> list = new Bag<Edge>();
        for (int v = 0; v < V; v++) {
            int selfLoops = 0;
            for (Edge e : adj(v)) {
                if (e.other(v) > v) {
                    list.add(e);
                }
                // only add one copy of each self loop
                else if (e.other(v) == v) {
                    if (selfLoops % 2 == 0) list.add(e);
                    selfLoops++;
                }
            }
        }
        return list;
    }



   /**
     * Return a string representation of this graph.
     */
    @Override
    public String toString() {
        String NEWLINE = System.getProperty("line.separator");
        StringBuilder s = new StringBuilder();
        s.append(V).append(" ").append(E).append(NEWLINE);
        for (int v = 0; v < V; v++) {
            s.append(v).append(": ");
            for (Edge e : adj[v]) {
                s.append(e).append("  ");
            }
            s.append(NEWLINE);
        }
        return s.toString();
    }

   /**
     * Test client.
     */
    /*public static void main(String[] args) {
        In in = new In(args[0]);
        EdgeWeightedGraph G = new EdgeWeightedGraph(in);
        StdOut.println(G);
    }*/

    public void setSeeds( ArrayList < SeedGraph > _seeds ){
        m_seeds = _seeds;
    }
    public ArrayList < SeedGraph > getSeeds(){
        return m_seeds;
    }

    public EdgeWeightedGraph regenerateGraph(){
        ArrayList < SeedGraph > seeds = new ArrayList < SeedGraph >();
        for( int i = 0; i < V; i++ ){
            if( !adj[i].isEmpty() )
                seeds.add( m_seeds.get( i ) );
        }
        for( int i = 0; i < seeds.size(); i++ )
            seeds.get( i ).setId( i );
        EdgeWeightedGraph graph = new EdgeWeightedGraph( seeds.size() );
        graph.setSeeds( seeds );
        for( Edge e : this.edges() ){
            Edge edge = new Edge( m_seeds.get( e.either() ).getId(), m_seeds.get( e.other( e.either() ) ).getId(), 1, e.edge() );
            graph.addEdge( edge );
        }
        return graph;
    }

    public boolean isSelected(){
        return m_selected;
    }
    public void setSelected( boolean _selected ){
        m_selected = _selected;
    }
}

