/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Tools.Delaunay;

/**
 *
 * @author Florian Levet
 */
public class CircleT {
    // state data for a circle of given radius centered on a given origin
    private PointT m_origin;
    private double m_radius;
    private PointT m_a, m_b;

    public PointT getOrigin() {
        return m_origin;
    }

    public double getRadius() {
        return m_radius;
    }

    public CircleT( PointT newOrigin, double newRadius, PointT _a, PointT _b ) {
        m_origin = newOrigin;
        m_radius = newRadius;
        m_a = _a;
        m_b = _b;
    }

    public boolean contains( PointT _p ){
        if( _p == m_a || _p == m_b ) return true;
        double length = m_origin.distanceTo( _p );
        return ( length <= m_radius );
    }
}
