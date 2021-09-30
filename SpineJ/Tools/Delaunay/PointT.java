/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Tools.Delaunay;

import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 *
 * @author Florian Levet
 */
public class PointT extends Point2D.Double{
    protected ArrayList < TriangleT > m_triangles = new ArrayList < TriangleT >();
    protected PointT m_prec = null, m_next = null;

    public PointT( double _x, double _y ){
        super( _x, _y );
    }
    public PointT( Point2D.Double _p ){
        super( _p.x, _p.y );
    }
    public PointT( PointT _p ){
        this.m_triangles = _p.m_triangles;
        m_prec = _p.m_prec;
        m_next = _p.m_next;
    }

    public void addTriangle( TriangleT _t ){
        m_triangles.add( _t );
    }

    public void setPrec( PointT _p ){
        m_prec = _p;
    }
    public void setNext( PointT _p ){
        m_next = _p;
    }
    public PointT getPrec(){
        return m_prec;
    }
    public PointT getNext(){
        return m_next;
    }
    public boolean isNeighborWith( PointT _p ){
        return _p.equals( m_prec ) || _p.equals( m_next );
    }

    public ArrayList < TriangleT > getTriangles(){
        return m_triangles;
    }

    public double distanceTo( PointT _o ){
        return Point2D.Double.distance( this.x, this.y, _o.x, _o.y );
    }

    @Override
    public boolean equals( Object o ){
        if( !( o instanceof PointT ) )
            return false;
        PointT p = ( PointT )o;
        return ( x == p.x && y == p.y );
    }

    @Override
    public int hashCode() {
        int hash = 7 * super.hashCode();
        //hash = 23 * hash + (this.m_triangles != null ? this.m_triangles.hashCode() : 0);
        return hash;
    }

    public static void filter( ArrayList < Point2D.Double > _ps){
        for(int i = 1; i < _ps.size()-1; i++){
            double x = ( _ps.get(i-1).x+_ps.get(i).x*2.+_ps.get(i+1).x )/ 4.;
            double y = ( _ps.get(i-1).y+_ps.get(i).y*2.+_ps.get(i+1).y )/ 4.;
            _ps.get( i ).setLocation( x, y );
        }
    }
}
