/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Tools.Delaunay;

import SpineJ.Tools.Graph.SeedGraph;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 *
 * @author Florian Levet
 */
public class SeedT extends Point2D.Double implements SeedGraph{
    protected ArrayList < EdgeT > m_edges = null;
    protected int id;
    protected TriangleT m_triangle = null;
    protected ArrayList < TriangleT > m_triangleTrimmed = new ArrayList < TriangleT >();


    public SeedT( Point2D.Double _p ){
        super( _p.x, _p.y );
        m_edges = new ArrayList < EdgeT >();
    }

    public void setTriangle( TriangleT _t ){
        m_triangle = _t;
    }
    public TriangleT getTriangle(){
        return m_triangle;
    }
    
    public void setMarked( boolean _val ){
        m_triangle.setMarked(_val);
        for(TriangleT t : m_triangleTrimmed)
            t.setMarked(_val);
    }
    public boolean isMarked(){
        return m_triangle.isMarked();
    }

    public void addEdge(EdgeT e){
        m_edges.add( e );
    }

    public int degree(){
        return m_edges.size();
    }

    public void setId(int _id){
        id = _id;
    }
    public int getId(){
        return id;
    }

    public Point2D.Double getPos(){
        return this;
    }
    public Point getPosition(){
        return new Point((int)getX(), (int)getY());
    }
    public void move( double _x, double _y ){
        this.move( _x, _y );
    }

    public ArrayList < EdgeT > getEdges(){
        return m_edges;
    }

    public EdgeT findUntreatedEdge(){
        for(int i = 0; i < m_edges.size(); i++){
            EdgeT e = m_edges.get(i);
            if(!e.marked()){
                e.setMarked(true);
                return e;
            }
        }
        return null;
    }

    public boolean contains( EdgeT _e ){
        return m_edges.contains( _e );
    }

    public double length( SeedT v ){
        return this.distance( v );
    }

    public void changeSeedForEdges(SeedT dst){
        for(int i = 0; i < m_edges.size(); i++){
            EdgeT edge = m_edges.get(i);
            edge.replaceSeed(this, dst);
            if(!dst.m_edges.contains(edge))
                dst.addEdge(m_edges.get(i));
        }
    }

    public EdgeT getEdgeWith( SeedGraph v ){
        for(int i = 0; i < m_edges.size(); i++)
            if( m_edges.get(i).isEdge( this, v ) )
                return m_edges.get(i);
        return null;
    }

    public EdgeT getSmallEdge(double d){
        for(int i = 0; i < m_edges.size(); i++){
            EdgeT edge = m_edges.get(i);
            double dist = edge.getV1().length(edge.getV2());
            //System.out.println("For edge " + edge.toString() + ", dist = " + dist);
            if(dist < d)
                return edge;
        }
        return null;
    }

    public EdgeT getOtherEdge(EdgeT edge){
        for(int i = 0; i < m_edges.size(); i++)
            if(!edge.equals(m_edges.get(i)))
                return m_edges.get(i);
        return null;
    }

    public void resetEdges(){
        m_edges.clear();
    }

    public void removeEdge(EdgeT edge){
        while(m_edges.remove(edge));
    }

    public double getArea(){
        return m_triangle.getArea();
    }
    
    public boolean hasTrimmedTriangles(){
        return !m_triangleTrimmed.isEmpty();
    }
    public void addTrianglesTrimmed( ArrayList < TriangleT > _tri ){
        m_triangleTrimmed.addAll( _tri );
    }
    public void addTriangleTrimmed( TriangleT _tri ){
        m_triangleTrimmed.add( _tri );
    }
    public void resetTrianglesTrimmed(){
        m_triangleTrimmed.clear();
    }
    public ArrayList < TriangleT > getTriangleTrimmed(){
        return m_triangleTrimmed;
    }
    
    public boolean contains( Point2D.Double _p ){
        if( m_triangle.contains( _p ) )
            return true;
        for( int i = 0; i < m_triangleTrimmed.size(); i++ )
            if( m_triangleTrimmed.get( i ).contains( _p ) )
                return true;
        return false;
    }
    
    public boolean isTouchingBorderImage(int _w, int _h)
    {
        if(m_triangle.isTouchingBorderImage(_w, _h))
            return true;
        for(TriangleT tri : m_triangleTrimmed)
            if(tri.isTouchingBorderImage(_w, _h))
                return true;
        return false;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof SeedT))
            return false;
        SeedT v = (SeedT)o;
        return super.equals( v );
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (this.m_edges != null ? this.m_edges.hashCode() : 0);
        hash = 43 * hash + this.id;
        return hash;
    }


    @Override
    public String toString(){
        return "s[" + getX() + ", " + getY() + ", id=" + id + ", d=" + degree() + "]";
    }

    public String printEdges(){
        String s = new String();
        for(int i = 0; i < m_edges.size(); i++)
            s += (m_edges.get(i).toString() + " ---- ");
        return s;
    }

}
