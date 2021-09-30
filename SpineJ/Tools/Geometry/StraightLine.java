/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SpineJ.Tools.Geometry;

import ij.process.ByteProcessor;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 *
 * @author Florian Levet
 */
public class StraightLine {

    public static int PARALLELE_LINE = 0, PERPENDICULAR_LINE = 1;
    double m_a, m_b, m_c;
    int type;
    Point2D.Double m_p1, m_p2;
    double m_normalizedStep;

    public StraightLine(int _x1, int _y1, int _x2, int _y2, int _type) {
        this((double) _x1, (double) _y1, (double) _x2, (double) _y2, _type);
    }

    public StraightLine( Point2D.Double _p1, Point2D.Double _p2, int _type ){
        this( _p1.x, _p1.y, _p2.x, _p2.y, _type );
    }
    public StraightLine(double _x1, double _y1, double _x2, double _y2, int _type) {
        type = _type;
        double vx = _x2 - _x1;
        double vy = _y2 - _y1;
        m_p1 = new Point2D.Double( _x1, _y1 );
        m_p2 = new Point2D.Double( _x2, _y2 );

        /*Nomalization of the vector director*/
        double length = Math.sqrt(vx * vx + vy * vy);
        vx /= length;
        vy /= length;

        if (type == PARALLELE_LINE) {
            m_a = -vy;
            m_b = vx;
        }
        else if (type == PERPENDICULAR_LINE) {
            m_a = vx;
            m_b = vy;
        }
        m_c = -( m_a * _x2 + m_b * _y2 );
    }
    
    public Point2D.Double getP1(){
        return m_p1;
    }
    public Point2D.Double getP2(){
        return m_p2;
    }
    public double getA(){
        return m_a;
    }
    public double getB(){
        return m_b;
    }
    public double getC(){
        return m_c;
    }
    
    public double getNormalizedStep(){
        return m_normalizedStep;
    }
    public void setNormalizedStep( double _val){
        m_normalizedStep = _val;
    }

    public Point2D.Double orthProjection( double _x, double _y ){
        return orthProjection( new Point.Double( _x, _y ) );
    }
    public Point2D.Double orthProjection( Point2D.Double _p ){
        double a = m_p2.y - m_p1.y;
        double b = m_p1.x - m_p2.x;
        double c = ( m_p1.y - m_p2.y ) * m_p1.x + ( m_p2.x - m_p1.x ) * m_p1.y;
        double AA = ( m_p2.x - m_p1.x );
        double BB = ( m_p2.y - m_p1.y );
        double y = (-(a * _p.x) - (a * BB * _p.y / AA) - c) / (b - (a * BB / AA));
        double x = (-c - b * y ) / a;
        return new Point2D.Double( x, y );
    }

    //Intersection point has to be inside the two segments
    public Point2D.Double intersectionSegments( StraightLine _line ){
        if( this.m_a * _line.m_b - this.m_b * _line.m_a == 0 )
            return null;
        double a = this.m_p2.x - this.m_p1.x, b = this.m_p2.y - this.m_p1.y;
        double r = ( ( this.m_p1.y - _line.m_p1.y ) * ( _line.m_p2.x - _line.m_p1.x ) - ( this.m_p1.x - _line.m_p1.x ) * ( _line.m_p2.y - _line.m_p1.y ) );
        r /= ( ( this.m_p2.x - this.m_p1.x ) * ( _line.m_p2.y - _line.m_p1.y ) - ( this.m_p2.y - this.m_p1.y ) * ( _line.m_p2.x - _line.m_p1.x ) );
        double s = ( ( this.m_p1.y - _line.m_p1.y ) * ( this.m_p2.x - this.m_p1.x ) - ( this.m_p1.x - _line.m_p1.x ) * ( this.m_p2.y - this.m_p1.y ) );
        s /= ( ( this.m_p2.x - this.m_p1.x ) * ( _line.m_p2.y - _line.m_p1.y ) - ( this.m_p2.y - this.m_p1.y ) * ( _line.m_p2.x - _line.m_p1.x ) );
        if( 0. <= r && r <= 1. && 0. <= s && s <= 1. )
            return new Point2D.Double( this.m_p1.x + r * a, this.m_p1.y + r * b );
        return null;
    }
    
    //intersection point has to be only inside the _line segment, the this segment is treated as a straightline
    public Point2D.Double intersectionOneSegment( StraightLine _line ){
        if( this.m_a * _line.m_b - this.m_b * _line.m_a == 0 )
            return null;
        double a = this.m_p2.x - this.m_p1.x, b = this.m_p2.y - this.m_p1.y;
        double r = ( ( this.m_p1.y - _line.m_p1.y ) * ( _line.m_p2.x - _line.m_p1.x ) - ( this.m_p1.x - _line.m_p1.x ) * ( _line.m_p2.y - _line.m_p1.y ) );
        r /= ( ( this.m_p2.x - this.m_p1.x ) * ( _line.m_p2.y - _line.m_p1.y ) - ( this.m_p2.y - this.m_p1.y ) * ( _line.m_p2.x - _line.m_p1.x ) );
        double s = ( ( this.m_p1.y - _line.m_p1.y ) * ( this.m_p2.x - this.m_p1.x ) - ( this.m_p1.x - _line.m_p1.x ) * ( this.m_p2.y - this.m_p1.y ) );
        s /= ( ( this.m_p2.x - this.m_p1.x ) * ( _line.m_p2.y - _line.m_p1.y ) - ( this.m_p2.y - this.m_p1.y ) * ( _line.m_p2.x - _line.m_p1.x ) );
        if( 0. <= s && s <= 1. )
            return new Point2D.Double( this.m_p1.x + r * a, this.m_p1.y + r * b );
        return null;
    }
    
    //intersection point does not need to be inside the two segments, they are treated as straightlines 
    public Point2D.Double intersectionLine( StraightLine _line ){
        if( this.m_a * _line.m_b - this.m_b * _line.m_a == 0 )
            return null;
        double a = this.m_p2.x - this.m_p1.x, b = this.m_p2.y - this.m_p1.y;
        double r = ( ( this.m_p1.y - _line.m_p1.y ) * ( _line.m_p2.x - _line.m_p1.x ) - ( this.m_p1.x - _line.m_p1.x ) * ( _line.m_p2.y - _line.m_p1.y ) );
        r /= ( ( this.m_p2.x - this.m_p1.x ) * ( _line.m_p2.y - _line.m_p1.y ) - ( this.m_p2.y - this.m_p1.y ) * ( _line.m_p2.x - _line.m_p1.x ) );
        double s = ( ( this.m_p1.y - _line.m_p1.y ) * ( this.m_p2.x - this.m_p1.x ) - ( this.m_p1.x - _line.m_p1.x ) * ( this.m_p2.y - this.m_p1.y ) );
        s /= ( ( this.m_p2.x - this.m_p1.x ) * ( _line.m_p2.y - _line.m_p1.y ) - ( this.m_p2.y - this.m_p1.y ) * ( _line.m_p2.x - _line.m_p1.x ) );
        return new Point2D.Double( this.m_p1.x + r * a, this.m_p1.y + r * b );
    }

    public double eval( Point2D.Double _p ){
        return eval( _p.x, _p.y );
    }
    public double eval( double _x, double _y ){
        return m_a * _x + m_b * _y + m_c;
    }

    public double evalSign( double _x, double _y ){
        double val = eval( _x, _y );
        return val / Math.abs( val );
    }

    public Point2D.Double findPoint( double _x, double _y, double _k ){
        double x = _x + _k * m_a;
        double y = _y + _k * m_b;
        return new Point2D.Double( x, y );
    }
    
    public Point2D.Double findOrthoPoint( double _x, double _y, double _k ){
        double x = _x + _k * -m_b;
        double y = _y + _k * m_a;
        return new Point2D.Double( x, y );
    }
    
    public boolean isOrthProjInRange( double _x, double _y ) {
        double dx = m_p2.getX() - m_p1.getX();
        double dy = m_p2.getY() - m_p1.getY();
        double innerProduct = (_x - m_p1.getX())*dx + (_y - m_p1.getY())*dy;
        return 0 <= innerProduct && innerProduct <= dx*dx + dy*dy;
    }
    
    public void lineTracingWithValueOnObject( ByteProcessor bp, int _valObject, int _valLine ){
        lineTracingWithValueOnObject(bp, (int)m_p1.x, (int)m_p1.y, (int)m_p2.x, (int)m_p2.y, _valObject, _valLine);
    }
    
    static public void lineTracingWithValueOnObject( ByteProcessor bp, int xi, int yi, int xf, int yf, int _valObject, int _valLine ) {
        int dx, dy, i, xinc, yinc, cumul, x, y;
        x = xi;
        y = yi;
        dx = xf - xi;
        dy = yf - yi;
        xinc = (dx > 0) ? 1 : -1;
        yinc = (dy > 0) ? 1 : -1;
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        if(bp.get(x, y) == _valObject)
            bp.set( x, y, _valLine);
        if (dx > dy) {
            cumul = dx / 2;
            for (i = 1; i <= dx; i++) {
                x += xinc;
                cumul += dy;
                if (cumul >= dx) {
                    cumul -= dx;
                    y += yinc;
                }
                if(bp.get(x, y) == _valObject)
                    bp.set( x, y, _valLine);
            }
        } else {
            cumul = dy / 2;
            for (i = 1; i <= dy; i++) {
                y += yinc;
                cumul += dx;
                if (cumul >= dy) {
                    cumul -= dy;
                    x += xinc;
                }
                if(bp.get(x, y) == _valObject)
                    bp.set( x, y, _valLine);
            }
        }
        if(bp.get(x, y) == _valObject)
            bp.set( x, y, _valLine);
    }
    
    static public void lineTracingWithValueOnObject( ByteProcessor bp, int xi, int yi, int xf, int yf, int _valObject, int _valLine, ArrayList <Point> _modifiedPoints ) {
        int dx, dy, i, xinc, yinc, cumul, x, y;
        x = xi;
        y = yi;
        dx = xf - xi;
        dy = yf - yi;
        xinc = (dx > 0) ? 1 : -1;
        yinc = (dy > 0) ? 1 : -1;
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        if(bp.get(x, y) == _valObject){
            bp.set( x, y, _valLine);
            _modifiedPoints.add(new Point(x, y));
        }
        if (dx > dy) {
            cumul = dx / 2;
            for (i = 1; i <= dx; i++) {
                x += xinc;
                cumul += dy;
                if (cumul >= dx) {
                    cumul -= dx;
                    y += yinc;
                }
                if(bp.get(x, y) == _valObject){
                    bp.set( x, y, _valLine);
                    _modifiedPoints.add(new Point(x, y));
                }
            }
        } else {
            cumul = dy / 2;
            for (i = 1; i <= dy; i++) {
                y += yinc;
                cumul += dx;
                if (cumul >= dy) {
                    cumul -= dy;
                    x += xinc;
                }
                if(bp.get(x, y) == _valObject){
                    bp.set( x, y, _valLine);
                    _modifiedPoints.add(new Point(x, y));
                }
            }
        }
        if(bp.get(x, y) == _valObject){
            bp.set( x, y, _valLine);
            _modifiedPoints.add(new Point(x, y));
        }
        //return lastPixel;
    }

    //Bresenham Line Tracing Algorithm
    static public Point lineTracing( ByteProcessor bp, int xi, int yi, int xf, int yf ) {
        int dx2[] = {-1, 0, 1, 1, 1, 0, -1, -1};
        int dy2[] = {1, 1, 1, 0, -1, -1, -1, 0};
        Point lastPixel = new Point( -1, -1 );
        int dx, dy, i, xinc, yinc, cumul, x, y;
        x = xi;
        y = yi;
        dx = xf - xi;
        dy = yf - yi;
        xinc = (dx > 0) ? 1 : -1;
        yinc = (dy > 0) ? 1 : -1;
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        bp.set( x, y, 0);
        for( int j = 0; j < dx2.length; j++ )
            bp.putPixel( x + dx2[j], y + dy2[j], 0 );
        if (dx > dy) {
            cumul = dx / 2;
            for (i = 1; i <= dx; i++) {
                x += xinc;
                cumul += dy;
                if (cumul >= dx) {
                    cumul -= dx;
                    y += yinc;
                }
                bp.set( x, y, 0);
                for( int j = 0; j < dx2.length; j++ )
                    bp.putPixel( x + dx2[j], y + dy2[j], 0 );
            }
        } else {
            cumul = dy / 2;
            for (i = 1; i <= dy; i++) {
                y += yinc;
                cumul += dx;
                if (cumul >= dy) {
                    cumul -= dy;
                    x += xinc;
                }
                bp.set( x, y, 0);
                for( int j = 0; j < dx2.length; j++ )
                    bp.putPixel( x + dx2[j], y + dy2[j], 0 );
            }
        }
        bp.set( x, y, 0);
        for( int j = 0; j < dx2.length; j++ )
            bp.putPixel( x + dx2[j], y + dy2[j], 0 );
        return lastPixel;
    }

        //Bresenham Line Tracing Algorithm
    static public Point lineTracingPointDetermination( ByteProcessor bp, int xi, int yi, int xf, int yf ) {
        Point lastPixel = new Point( -1, -1 );
        int dx, dy, i, xinc, yinc, cumul, x, y;
        x = xi;
        y = yi;
        dx = xf - xi;
        dy = yf - yi;
        xinc = (dx > 0) ? 1 : -1;
        yinc = (dy > 0) ? 1 : -1;
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        if (dx > dy) {
            cumul = dx / 2;
            for (i = 1; i <= dx; i++) {
                x += xinc;
                cumul += dy;
                if (cumul >= dx) {
                    cumul -= dx;
                    y += yinc;
                }
                if( bp.getPixel(x, y) == 0 )
                    return lastPixel;
                lastPixel.setLocation( x, y );
            }
        } else {
            cumul = dy / 2;
            for (i = 1; i <= dy; i++) {
                y += yinc;
                cumul += dx;
                if (cumul >= dy) {
                    cumul -= dy;
                    x += xinc;
                }
                if( bp.getPixel(x, y) == 0 )
                    return lastPixel;
                lastPixel.setLocation( x, y );
            }
        }
        if( bp.getPixel(x, y) == 0 )
            return lastPixel;
        /*lastPixel.setLocation( x, y );
        return lastPixel;*/
        return new Point( -1, -1 );
    }
    
    @Override
    public boolean equals( Object o ){
        if( !( o instanceof StraightLine ) )
            return false;
        StraightLine l = ( StraightLine )o;
        if( this.type != l.type ) return false;
        return ( m_p1.equals( l.m_p1 ) && m_p2.equals( l.m_p2 ) ) || ( m_p1.equals( l.m_p2 ) && m_p2.equals( l.m_p1 ) );
    }

}
