/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Tools.Graph;

/**
 *
 * @author Florian Levet
 */
public class Edge implements Comparable<Edge> {

    private final int v;
    private final int w;
    private final double weight;
    private final EdgeGraph edge;

   /**
     * Create an edge between v and w with given weight.
     */
    public Edge(int v, int w, double weight, EdgeGraph edge) {
        this.v = v;
        this.w = w;
        this.weight = weight;
        this.edge = edge;
    }

   /**
     * Return the weight of this edge.
     */
    public double weight() {
        return weight;
    }

   /**
     * Return either endpoint of this edge.
     */
    public int either() {
        return v;
    }

    public boolean connectedToVertex( int vertex ){
        return ( v == vertex || w == vertex );
    }
    public boolean sameExtremities( int v, int w ){
        return ( ( this.v == v && this.w == w ) || ( this.v == w && this.w == v ) );
    }

   /**
     * Return the endpoint of this edge that is different from the given vertex
     * (unless a self-loop).
     */
    public int other(int vertex) {
        if      (vertex == v) return w;
        else if (vertex == w) return v;
        else throw new RuntimeException("Illegal endpoint");
    }

    public EdgeGraph edge(){
        return edge;
    }

   /**
     * Compare edges by weight.
     */
    public int compareTo(Edge that) {
        if      (this.weight() < that.weight()) return -1;
        else if (this.weight() > that.weight()) return +1;
        else                                    return  0;
    }

    @Override
    public boolean equals( Object o ){
        if( o instanceof Edge ){
            Edge other = ( Edge )o;
            return ( v == other.v && w == other.w && weight == other.weight && edge.getId() == other.edge.getId() );
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + this.v;
        hash = 29 * hash + this.w;
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.weight) ^ (Double.doubleToLongBits(this.weight) >>> 32));
        return hash;
    }

   /**
     * Return a string representation of this edge.
     */
    public String toString() {
        return String.format("%d-%d %.5f", v, w, weight);
    }
}
