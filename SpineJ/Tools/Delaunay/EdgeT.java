/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Tools.Delaunay;

import SpineJ.Tools.Graph.EdgeGraph;
import SpineJ.Tools.Graph.SeedGraph;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Florian Levet
 */
public class EdgeT implements EdgeGraph{
    SeedT v1 = null, v2 = null;
    ArrayList < Point2D.Double > skel = new ArrayList < Point2D.Double >();
    ArrayList < TriangleT > m_tri = new ArrayList < TriangleT >(), m_triTrimmed = new ArrayList < TriangleT >();
    protected int id;
    protected boolean m_marked, m_originallyNotTrimmed, m_isDendrite;
    protected double m_area;
    //protected Vector < VoronoiCell > cells = null;

    static public int nb_edges = 0;

    public EdgeT( SeedT _v1, SeedT _v2 ){
        v1 = _v1;
        v2 = _v2;
        id = nb_edges++;
        skel = new ArrayList < Point2D.Double >();
        m_marked = false;
        m_originallyNotTrimmed = true;
        m_area = 0.;
    }

    public SeedT getV1(){
        return v1;
    }
    public void setV1(SeedT v){
        v1 = v;
    }
    public SeedT getV2(){
        return v2;
    }
    public void setV2(SeedT v){
        v2 = v;
    }

    public void setId(int _id){
        id = _id;
    }
    public int getId(){
        return id;
    }

    public void setSkel(ArrayList < Point2D.Double > _skel){
        skel = _skel;
    }
    public ArrayList < Point2D.Double > getSkel(){
        return skel;
    }
    public void recomputeSkeleton(){
        skel.clear();
        TriangleT prec = v1.getTriangle(), cur = null, prec2 = null;
        if( !prec.isTrimmed() )
            skel.add( prec.getBarycenter() );
        for( int i = 0; i < m_tri.size(); i++ ){
            cur = m_tri.get( i );
            skel.add( cur.getCenterSharedEdge( prec ) );
            prec2 = prec;
            prec = cur;
        }
        cur = v2.getTriangle();
        if( !cur.isTrimmed() )
            skel.add( cur.getBarycenter() );
        else{
            cur = prec.getOtherTriangleWithoutMarkedCheck( prec2 );
            if( cur.isTransitionnal() )
                skel.add( cur.getCenterSharedEdge( prec ) );
        }
        PointT.filter( skel );
    }
    
    public void recomputeSkeletonWithMarked(){
        skel.clear();
        for(TriangleT t : m_tri)
            t.setMarked(true);
        for(TriangleT t : m_triTrimmed)
            if(t.isJuntionnal())
                t.setMarked(true);
        v2.getTriangle().setMarked(true);
        TriangleT cur = v1.getTriangle(), prec = null;
        //if( !cur.isTrimmed() )
            skel.add( cur.getBarycenter() );
        boolean done = false;
        while(!done){
            cur.setMarked(false);
            prec = cur;
            cur = null;
            for(int i = 0; i < prec.getNeighbors().size() && cur == null; i++){
                TriangleT tmp = prec.getNeighbors().get(i);
                if(tmp.isMarked())
                    cur = tmp;
            }
            if(cur != null){
                if(cur.isTransitionnal())
                    skel.add( cur.getCenterSharedEdge( prec ) );
                else if(cur == v2.getTriangle()){
                    //if( !cur.isTrimmed() )
                        skel.add( cur.getBarycenter() );
                    done = true;
                }
                else if(cur.isJuntionnal()){
                    skel.add( cur.getBarycenter() );
                }
            }
            else
                done = true;
        }
        
        PointT.filter( skel );
        
        for(TriangleT t : m_tri)
            t.setMarked(false);
        for(TriangleT t : m_triTrimmed)
            t.setMarked(false);
        v2.getTriangle().setMarked(false);
    }
    
    public ArrayList < TriangleT > getTriangles(){
        return m_tri;
    }
    public void setTriangles( ArrayList < TriangleT > _tri ){
        m_tri = _tri;
    }
    public ArrayList < TriangleT > getTrianglesTrimmed(){
        return m_triTrimmed;
    }
    public void setTrianglesTrimmed( ArrayList < TriangleT > _tri ){
        m_triTrimmed = _tri;
        if( !_tri.isEmpty() )
            m_originallyNotTrimmed = false;
    }
    public void addTriangles( ArrayList < TriangleT > _tri ){
        m_tri.addAll( _tri );
    }
    public void addTriangle( TriangleT _tri ){
        m_tri.add( _tri );
    }
    public void addTrianglesTrimmed( ArrayList < TriangleT > _tri ){
        m_triTrimmed.addAll( _tri );
    }
    public boolean isEdgeTrimmed(){
        boolean test = m_tri.isEmpty() && !m_originallyNotTrimmed;//m_triTrimmed.isEmpty();
        return test;
    }

    public ArrayList < TriangleT > getNeighborTrianglesFromTriangleSeed( TriangleT _t ){
        if( m_tri.isEmpty() ) return null;
        if( _t.isNeighbor( m_tri.get( 0 ) ) )
            return m_tri;
        if( _t.isNeighbor( m_tri.get( m_tri.size() - 1 ) ) ){
            Collections.reverse( m_tri );
            return m_tri;
        }
        return null;
    }
    public TriangleT getNeighborTriangleFromTriangleSeed( SeedT _s ){
        TriangleT t = _s.getTriangle();
        if( m_tri.isEmpty() ){
            if( v1 != _s ){
                if( t.isNeighbor( v1.getTriangle() ) )
                    return v1.getTriangle();
                }
            else{
                if( t.isNeighbor( v2.getTriangle() ) )
                    return v2.getTriangle();
            }
        }
        else{
            for(TriangleT tmp : m_tri)
                if(t.isNeighbor(tmp))
                    return tmp;
        }
        return null;
    }

    public SeedT getOtherSeed(SeedT v){
        if(v.equals(v1))
            return v2;
        if(v.equals(v2))
            return v1;
        return null;
    }

    public void setMarked(boolean _d){
        m_marked = _d;
    }
    public boolean marked(){
        return m_marked;
    }
    public void setDendrite(boolean _d){
        m_isDendrite = _d;
        for(TriangleT t : m_tri)
            t.setMarked(_d);
        for(TriangleT t : m_triTrimmed)
            t.setMarked(_d);
    }
    public boolean isDendrite(){
        return m_isDendrite;
    }

    public void replaceSeed(SeedT src, SeedT dest){
        if(v1.equals(src))
            v1 = dest;
        if(v2.equals(src))
            v2 = dest;
    }

    public boolean isEdge( SeedGraph v1, SeedGraph v2){
        return ((this.v1.equals(v1) && this.v2.equals(v2)) || (this.v1.equals(v2) && this.v2.equals(v1)));
    }
    public boolean isNeighborWithEdge( EdgeT _e ){
        return ( this.getV1().equals( _e.getV1() ) && this.getV2().equals( _e.getV2() ) ) || ( this.getV1().equals( _e.getV2() ) && this.getV2().equals( _e.getV1() ) );
    }

    public String printPixels(){
        String s = new String();
        for(int i = 0; i < skel.size(); i++)
            s += (skel.get(i).toString() + " ---- ");
        return s;
    }

    public int getLength(){
        return skel.size();
    }

    public double length(){
        double x = v2.getX() - v1.getX();
        double y = v2.getY() - v1.getY();
        return Math.sqrt(x*x + y*y);
    }

    public void computeArea(){
        m_area = 0.;
        for( int i = 0; i < m_tri.size(); i++ )
            m_area += m_tri.get( i ).getArea();
        for( int i = 0; i < m_triTrimmed.size(); i++ )
            m_area += m_triTrimmed.get( i ).getArea();
    }
    public double getArea(){
        return m_area;
    }

    public void setAsDendrite( boolean _val ){
        m_isDendrite = _val;
        for( int i = 0; i < m_tri.size(); i++ )
            m_tri.get( i ).setMarked( _val );
        for( int i = 0; i < m_triTrimmed.size(); i++ )
            m_triTrimmed.get( i ).setMarked( _val );
    }

    public void copyPix(EdgeT o){
        for(int i = 0; i < o.skel.size(); i++){
            Point2D.Double p = o.skel.get(i);
            if(!skel.contains(p))
                skel.add(p);
        }
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof EdgeT))
            return false;
        EdgeT v = (EdgeT)o;
        return ((v1.equals(v.v1) && v2.equals(v.v2)) || (v1.equals(v.v2) && v2.equals(v.v1)));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.v1 != null ? this.v1.hashCode() : 0);
        hash = 67 * hash + (this.v2 != null ? this.v2.hashCode() : 0);
        hash = 67 * hash + (this.skel != null ? this.skel.hashCode() : 0);
        hash = 67 * hash + this.id;
        return hash;
    }

    @Override
    public String toString(){
        return "e{" + v1.toString() + ", " + v2.toString() + ", id=" + id + ", done=" + m_marked + ", dendrite=" + m_isDendrite + "}";
    }

    public ArrayList<Point> getSkel2() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public boolean contains( double x, double y ){
        Point2D.Double p = new Point2D.Double( x, y );
        TriangleT t = null;
        for( int n = 0; n < m_tri.size(); n++ ){
            t = m_tri.get( n );
            if( t.contains( p ) )
                return !t.isMarked();
        }
        for( int n = 0; n < m_triTrimmed.size(); n++ ){
            t = m_triTrimmed.get( n );
            if( t.contains( p ) )
                return !t.isMarked();
        }
        t = v1.getTriangle();
        if( t.contains( p ) )
            return !t.isMarked();
        for( int n = 0; n < v1.getTriangleTrimmed().size(); n++ ){
            t = v1.getTriangleTrimmed().get( n );
            if( t.contains( p ) )
                return !t.isMarked();
        }
        t = v2.getTriangle();
        if( t.contains( p ) )
            return !t.isMarked();
        for( int n = 0; n < v2.getTriangleTrimmed().size(); n++ ){
            t = v2.getTriangleTrimmed().get( n );
            if( t.contains( p ) )
                return !t.isMarked();
        }
        return false;
    }
}
