/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Tools.Geometry;

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.FloatPolygon;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 *
 * @author Florian Levet
 */
public class DoublePolygon {
        /** The number of points. */
    public int npoints;

    /* The array of x coordinates. */
    public double xpoints[];

    /* The array of y coordinates. */
    public double ypoints[];


    /** Constructs a FloatPolygon. */
    public DoublePolygon(double xpoints[], double ypoints[], int npoints) {
        this.npoints = npoints;
        this.xpoints = xpoints;
        this.ypoints = ypoints;
    }
    
    public DoublePolygon( Point2D.Double _a, Point2D.Double _b, Point2D.Double _c ){
        npoints = 3;
        xpoints = new double[npoints];
        ypoints = new double[npoints];
        xpoints[0] = _a.x;
        ypoints[0] = _a.y;
        xpoints[1] = _b.x;
        ypoints[1] = _b.y;
        xpoints[2] = _c.x;
        ypoints[2] = _c.y;
    }
    
    public DoublePolygon( ArrayList < Point2D.Double > _points ){
        npoints = _points.size();
        xpoints = new double[npoints];
        ypoints = new double[npoints];
        for( int n = 0; n < npoints; n++ ){
            xpoints[n] = _points.get( n ).getX();
            ypoints[n] = _points.get( n ).getY();
        }
    }

    /** Returns 'true' if the point (x,y) is inside this polygon. This is a Java
    version of the remarkably small C program by W. Randolph Franklin at
    http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html#The%20C%20Code
    */
    public boolean contains(double x, double y) {
        boolean inside = false;
        for (int i=0, j=npoints-1; i<npoints; j=i++) {
            if (((ypoints[i]>y)!=(ypoints[j]>y)) &&
            (x<(xpoints[j]-xpoints[i])*(y-ypoints[i])/(ypoints[j]-ypoints[i])+xpoints[i]))
            inside = !inside;
        }
        return inside;
    }

    public FloatPolygon getFloatPolygon(){
        float xs [] = new float[npoints], ys [] = new float[npoints];
        for( int n = 0; n < npoints; n++ ){
            xs[n] = ( float )xpoints[n];
            ys[n] = ( float )ypoints[n];
        }
        return new FloatPolygon( xs, ys, npoints );
    }
    
    public Polygon getPolygon(){
        int xs [] = new int[npoints], ys [] = new int[npoints];
        for( int n = 0; n < npoints; n++ ){
            xs[n] = ( int )( xpoints[n] + .5 );
            ys[n] = ( int )( ypoints[n] + .5 );
        }
        return new Polygon( xs, ys, npoints );
    }
    
    public PolygonRoi getPolygonRoi(){
        float xs [] = new float[npoints], ys [] = new float[npoints];
        for( int n = 0; n < npoints; n++ ){
            xs[n] = ( float )xpoints[n] + .5f;
            ys[n] = ( float )ypoints[n] + .5f;
        }
        return new PolygonRoi( xs, ys, npoints, Roi.POLYGON );
    }
    
    public double getArea(){
        double area = 0.;
        for( int n = 1; n <= npoints; n++ ){
            double x1 = xpoints[n-1], y1 = ypoints[n-1], x2 = xpoints[n%npoints], y2 = ypoints[n%npoints];
            area += x1 * y2 - x2 * y1;
        }
        return Math.abs( area / 2. );
    }
    
    public double getPerimeter(){
        double p = 0.;
        for( int n = 1; n <= npoints; n++ ){
            double x1 = xpoints[n-1], y1 = ypoints[n-1], x2 = xpoints[n%npoints], y2 = ypoints[n%npoints];
            p += Point2D.Double.distance( x1, y1, x2, y2 );
        }
        return p;
    }
}
