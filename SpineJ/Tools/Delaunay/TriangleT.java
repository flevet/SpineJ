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
public class TriangleT {
    protected PointT m_a, m_b, m_c;
    Point2D.Double m_barycenter;
    protected ArrayList < TriangleT > m_neighbors = new ArrayList < TriangleT >();
    protected boolean m_marked = false, m_trimmed = false;

    public TriangleT( PointT _a, PointT _b, PointT _c ){
        m_a = _a;
        m_b = _b;
        m_c = _c;
        double x = ( m_a.x + m_b.x + m_c.x ) / 3.;
        double y = ( m_a.y + m_b.y + m_c.y ) / 3.;
        m_barycenter = new Point2D.Double( x, y );
        m_a.addTriangle( this );
        m_b.addTriangle( this );
        m_c.addTriangle( this );
    }
    
    public TriangleT( Point2D.Double _a, Point2D.Double _b, Point2D.Double _c ){
        m_a = new PointT( _a.x, _a.y );
        m_b = new PointT( _b.x, _b.y );
        m_c = new PointT( _c.x, _c.y );
        double x = ( m_a.x + m_b.x + m_c.x ) / 3.;
        double y = ( m_a.y + m_b.y + m_c.y ) / 3.;
        m_barycenter = new Point2D.Double( x, y );
        m_a.addTriangle( this );
        m_b.addTriangle( this );
        m_c.addTriangle( this );
    }

    public void addNeighbor( TriangleT _t ){
        m_neighbors.add( _t );
    }

    public boolean isNewNeighbor( TriangleT _t ){
        if( this == _t || m_neighbors.contains( _t ) )
            return false;
        boolean a = ( m_a == _t.m_a || m_a == _t.m_b || m_a == _t.m_c );
        boolean b = ( m_b == _t.m_a || m_b == _t.m_b || m_b == _t.m_c );
        boolean c = ( m_c == _t.m_a || m_c == _t.m_b || m_c == _t.m_c );
        return ( ( a && b && !c ) || ( !a && b && c ) || ( a && !b && c ) );
    }
    public boolean isNeighbor( TriangleT _t ){
        return m_neighbors.contains( _t );
    }
    public ArrayList < TriangleT > getNeighbors(){
        return m_neighbors;
    }

    public PointT getA(){
        return m_a;
    }
    public PointT getB(){
        return m_b;
    }
    public PointT getC(){
        return m_c;
    }
    public void addShift( double _x, double _y ){
        m_a.x += _x; m_a.y += _y;
        m_b.x += _x; m_b.y += _y;
        m_c.x += _x; m_c.y += _y;
    }
    public ArrayList < PointT > getPoints(){
        ArrayList < PointT > tmp = new ArrayList < PointT >();
        tmp.add( m_a );
        tmp.add( m_b );
        tmp.add( m_c );
        return tmp;
    }

    public void setMarked( boolean _val ){
        m_marked = _val;
    }
    public boolean isMarked(){
        return m_marked;
    }
    public void setTrimmed( boolean _val ){
        m_trimmed = _val;
    }
    public boolean isTrimmed(){
        return m_trimmed;
    }

    public boolean isTerminal(){
        return m_neighbors.size() == 1;
    }
    public boolean isTransitionnal(){
        return m_neighbors.size() == 2;
    }
    public boolean isJuntionnal(){
        return m_neighbors.size() == 3;
    }

    public TriangleT getOtherTriangle( TriangleT _tri ){
        for( int i = 0; i < m_neighbors.size(); i++ ){
            TriangleT cur = m_neighbors.get( i );
            if( cur != _tri && ! cur.isMarked() )
                return cur;
        }
        return null;
    }
    public TriangleT getOtherTriangleWithoutMarkedCheck( TriangleT _tri ){
        for( int i = 0; i < m_neighbors.size(); i++ ){
            TriangleT cur = m_neighbors.get( i );
            if( cur != _tri )
                return cur;
        }
        return null;
    }
    public Point2D.Double getBarycenter(){
        return m_barycenter;
    }
    public void setBarycenter( Point2D.Double _p ){
        m_barycenter.setLocation( _p );
    }
    //Recomputation is only for junctionnal triangle, is needed for having a correct skeleton if one and only one  neighboring triangles was trimmed
    public void recomputeBarycenter(){
        if( !this.isJuntionnal() ) return;
        int cpt = 0;
        Point2D.Double p1 = null, p2 = null;
        for( int i = 0; i < m_neighbors.size(); i++ ){
            TriangleT tmp = m_neighbors.get( i );
            if( tmp.isTrimmed() )
                cpt++;
            else{
                if( p1 == null )
                    p1 = this.getCenterSharedEdge( tmp );
                else
                    p2 = this.getCenterSharedEdge( tmp );
            }
        }
        if( cpt != 2 ) return;
        m_barycenter.setLocation( ( p1.x + p2.x ) / 2., ( p1.y + p2.y ) / 2. );
    }
    public Point2D.Double getCenterSharedEdge( TriangleT _t ){
        try{
            boolean a = ( m_a == _t.m_a || m_a == _t.m_b || m_a == _t.m_c );
            boolean b = ( m_b == _t.m_a || m_b == _t.m_b || m_b == _t.m_c );
            boolean c = ( m_c == _t.m_a || m_c == _t.m_b || m_c == _t.m_c );
            PointT p1 = null, p2 = null;
            if( a && b ){
                p1 = m_a;
                p2 = m_b;
            }
            else if( b && c ){
                p1 = m_b;
                p2 = m_c;
            }
            else if( c && a ){
                p1 = m_c;
                p2 = m_a;
            }
            return new Point2D.Double( ( p1.x + p2.x ) / 2., ( p1.y + p2.y ) / 2. );
        }
        catch( Exception e ){
            return new Point2D.Double( 0., 0. );
        }
    }
    public PointT[] getSharedEdge( TriangleT _t ){
        try{
            PointT[] pts = new PointT[2];
            boolean a = ( m_a == _t.m_a || m_a == _t.m_b || m_a == _t.m_c );
            boolean b = ( m_b == _t.m_a || m_b == _t.m_b || m_b == _t.m_c );
            boolean c = ( m_c == _t.m_a || m_c == _t.m_b || m_c == _t.m_c );
            PointT p1 = null, p2 = null;
            if( a && b ){
                pts[0] = m_a;
                pts[1] = m_b;
            }
            else if( b && c ){
                pts[0] = m_b;
                pts[1] = m_c;
            }
            else if( c && a ){
                pts[0] = m_c;
                pts[1] = m_a;
            }
            return pts;
        }
        catch( Exception e ){
            return null;
        }
    }
    public CircleT getCircleSharedEdge( TriangleT _t ){
        boolean a = ( m_a == _t.m_a || m_a == _t.m_b || m_a == _t.m_c );
        boolean b = ( m_b == _t.m_a || m_b == _t.m_b || m_b == _t.m_c );
        boolean c = ( m_c == _t.m_a || m_c == _t.m_b || m_c == _t.m_c );
        PointT p1 = null, p2 = null;
        if( a && b ){
            p1 = m_a;
            p2 = m_b;
        }
        else if( b && c ){
            p1 = m_b;
            p2 = m_c;
        }
        else if( c && a ){
            p1 = m_c;
            p2 = m_a;
        }
        PointT circle = new PointT( ( p1.x + p2.x ) / 2., ( p1.y + p2.y ) / 2. );
        double radius = p1.distanceTo( p2 );
        return new CircleT( circle, radius / 2., p1, p2 );
    }

    public double getArea(){
        double area = ( ( m_b.x * m_a.y  ) - ( m_a.x * m_b.y ) ) + ( ( m_c.x * m_b.y  ) - ( m_b.x * m_c.y ) ) + ( ( m_a.x * m_c.y  ) - ( m_c.x * m_a.y ) );
        return Math.abs( area / 2. );
    }


    // check whether a given point falls inside the triangle
    public boolean contains( Point2D.Double _p ) {
        double o1 = getOrientationResult( m_a, m_b, _p );
        double o2 = getOrientationResult( m_b, m_c, _p );
        double o3 = getOrientationResult( m_c, m_a, _p );

        return (o1 == o2) && (o2 == o3);
    }

    private int getOrientationResult( Point2D.Double _p1, Point2D.Double _p2, Point2D.Double _p) {
        double orientation = ( ( _p2.x - _p1.x ) * ( _p.y - _p1.y ) ) - ( ( _p.x - _p1.x ) * ( _p2.y - _p1.y ) );
        if (orientation > 0) {
            return 1;
        }
        else if (orientation < 0) {
            return -1;
        }
        else {
            return 0;
        }
    }
    
    public PointT getThirdPoint( Point2D.Double _a, Point2D.Double _b ){
        if( _a.x == m_a.x && _a.y == m_a.y ){
            if( _b.x == m_b.x && _b.y == m_b.y )
                return m_c;
            else
                return m_b;
        }
        if( _a.x == m_b.x && _a.y == m_b.y ){
            if( _b.x == m_c.x && _b.y == m_c.y )
                return m_a;
            else
                return m_c;
        }
        if( _a.x == m_c.x && _a.y == m_c.y ){
            if( _b.x == m_b.x && _b.y == m_b.y )
                return m_a;
            else
                return m_b;
        }
        return null;
    }
    
    public boolean isNearTrimmedTriangle()
    {
        for( int i = 0; i < m_neighbors.size(); i++ ){
            TriangleT cur = m_neighbors.get( i );
            if(cur.isTrimmed())
                return true;
        }
        return false;
    }
    
    public boolean isTouchingBorderImage(int _w, int _h)
    {
        PointT[] pts = {m_a, m_b, m_c};
        for(int n = 0; n < pts.length; n++){
            PointT pt = pts[n];
            if(pt.x < 1 || pt.x > (_w - 1) || pt.y < 1 || pt.y > (_h - 1))
                return true;
        }
        return false;
    }
    

    @Override
    public String toString(){
        return ( "Triangle " + m_a.toString() + ", " + m_b.toString() + ", " + m_c.toString() + " --> n[" + m_neighbors.size() + "]" );
        //return ( "Barycenter " + getBarycenter().toString() + " --> n[" + m_neighbors.size() + "]" );
    }

    @Override
    public boolean equals( Object o ){
        if( !( o instanceof TriangleT ) )
            return false;
        TriangleT t = ( TriangleT )o;
        boolean a = ( m_a == t.m_a || m_a == t.m_b || m_a == t.m_c );
        boolean b = ( m_b == t.m_a || m_b == t.m_b || m_b == t.m_c );
        boolean c = ( m_c == t.m_a || m_c == t.m_b || m_c == t.m_c );
        return ( a && b && c );
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + (this.m_a != null ? this.m_a.hashCode() : 0);
        hash = 19 * hash + (this.m_b != null ? this.m_b.hashCode() : 0);
        hash = 19 * hash + (this.m_c != null ? this.m_c.hashCode() : 0);
       //hash = 19 * hash + (this.m_neighbors != null ? this.m_neighbors.hashCode() : 0);
        return hash;
    }
}
