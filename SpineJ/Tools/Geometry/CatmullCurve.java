/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Tools.Geometry;

import SpineJ.Tools.Misc.HoleRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 *
 * @author Florian Levet
 */
public class CatmullCurve {    
    public DoublePolygon controlPoints = null, points = null;
    public Point2D.Double[] arcLengthes = null;
    protected double totalLength, stepArcLength, distForCurves;
    protected double maxX, maxY, minH, maxH;
    protected Point2D.Double vorig = new Point2D.Double(0., -1.);
    protected double x_max_scale, x_min_scale, y_max_scale, y_min_scale;
    protected static boolean debug = false, debug2 = false;

    public CatmullCurve( ArrayList < Point2D.Double > _crtP, double _minH, double _maxH, int nb){
        Point2D.Double [] crtP = new Point2D.Double[_crtP.size()];
        crtP = _crtP.toArray( crtP );
        init( crtP, _minH, _maxH, nb );
    }

    public CatmullCurve( Point2D.Double[] _crtP, double _minH, double _maxH, int nb ){
        init( _crtP, _minH, _maxH, nb );
    }

    private void init( Point2D.Double[] _crtP, double _minH, double _maxH, int nb ){
        minH = _minH;
        maxH = _maxH;
        totalLength = 0.;
        x_max_scale = Double.MIN_VALUE;
        y_max_scale = Double.MIN_VALUE;
        for(int i = 0; i < _crtP.length; i++){
            if(_crtP[i].x > x_max_scale)
                x_max_scale = _crtP[i].x;
            if(_crtP[i].y > y_max_scale)
                y_max_scale = _crtP[i].y;
        }
        double [] xs = new double[_crtP.length + 2], ys = new double[_crtP.length + 2];
        for(int i = 0; i < _crtP.length; i++){
            xs[i+1] = _crtP[i].x;
            ys[i + 1] = _crtP[i].y;
        }
        xs[0] = _crtP[0].x;
        ys[0] = _crtP[0].y;
        xs[xs.length - 1] = _crtP[_crtP.length-1].x;
        ys[ys.length - 1] = _crtP[_crtP.length-1].y;

        double som = 0.;
        arcLengthes = new Point2D.Double[xs.length - 3];
        for(int i = 2; i < xs.length - 1; i++){
            double vx = xs[i] - xs[i-1];
            double vy = ys[i] - ys[i-1];
            double l = Math.sqrt(vx*vx + vy*vy);
            arcLengthes[i-2] = new Point2D.Double(l, som);
            som += l;
        }
        totalLength = som;
        stepArcLength = 1. / ((double)arcLengthes.length);
        controlPoints = new DoublePolygon( xs, ys, xs.length );
        if( nb > 3 )
            arcLengthParametrization(nb);
        else
            points = new DoublePolygon( xs, ys, xs.length );
    }
    
    public Point2D.Double getArcLengthPoint(double _t){
        int index = getControlPointWithT(_t);
        //We found the position of the future added point in the segment (which is between [0,1])
        double lengthFactorT = _t * totalLength;
        double diffLength = lengthFactorT - arcLengthes[index].getY();
        double tInSegment = diffLength / arcLengthes[index].getX();

        index = index+1;//For control points -> the first point is double
        Point2D p1, p2, p3, p4;

        double px = (controlPoints.xpoints[index]*3.0-controlPoints.xpoints[index+1]*3.0+controlPoints.xpoints[index+2]-controlPoints.xpoints[index-1])*Math.pow(tInSegment,3.)/2.0;
        double py = (controlPoints.ypoints[index]*3.0-controlPoints.ypoints[index+1]*3.0+controlPoints.ypoints[index+2]-controlPoints.ypoints[index-1])*Math.pow(tInSegment,3.)/2.0;
        p1 = new Point2D.Double(px, py);
        px = (controlPoints.xpoints[index-1]*2.0 - controlPoints.xpoints[index]*5.0 + controlPoints.xpoints[index+1]*4.0-controlPoints.xpoints[index+2])*Math.pow(tInSegment,2.)/2.0;
        py = (controlPoints.ypoints[index-1]*2.0 - controlPoints.ypoints[index]*5.0 + controlPoints.ypoints[index+1]*4.0-controlPoints.ypoints[index+2])*Math.pow(tInSegment,2.)/2.0;
        p2 = new Point2D.Double(px, py);
        px = ( controlPoints.xpoints[index+1] - controlPoints.xpoints[index-1])*(tInSegment)/2.0;
        py = ( controlPoints.ypoints[index+1] - controlPoints.ypoints[index-1])*(tInSegment)/2.0;
        p3 = new Point2D.Double(px, py);
        px = controlPoints.xpoints[index]*2.0/2.0;
        py = controlPoints.ypoints[index]*2.0/2.0;
        p4 = new Point2D.Double(px, py);

        double x = p1.getX() + p2.getX() + p3.getX() + p4.getX();
        double y = p1.getY() + p2.getY() + p3.getY() + p4.getY();
        return new Point2D.Double(x, y);
    }

    protected void arcLengthParametrization(int size){
        double [] xs = new double[size], ys = new double[size];
        xs[0] = controlPoints.xpoints[0];
        ys[0] = controlPoints.ypoints[0];
        xs[xs.length-1] = controlPoints.xpoints[controlPoints.npoints-1];
        ys[ys.length-1] = controlPoints.ypoints[controlPoints.npoints-1];

        double step = 1. / ((double)size - 1.);
        double t = step;

        int indexPoint = 1;

        while(t < 1.){
            //We found the good segment with respect to the control points
            int index = getControlPointWithT(t);
            //We found the position of the future added point in the segment (which is between [0,1])
            double lengthFactorT = t * totalLength;
            double diffLength = lengthFactorT - arcLengthes[index].getY();
            double tInSegment = diffLength / arcLengthes[index].getX();

            index = index+1;//For control points -> the first point is double
            Point2D p1, p2, p3, p4;

            double px = (controlPoints.xpoints[index]*3.0-controlPoints.xpoints[index+1]*3.0+controlPoints.xpoints[index+2]-controlPoints.xpoints[index-1])*Math.pow(tInSegment,3.)/2.0;
            double py = (controlPoints.ypoints[index]*3.0-controlPoints.ypoints[index+1]*3.0+controlPoints.ypoints[index+2]-controlPoints.ypoints[index-1])*Math.pow(tInSegment,3.)/2.0;
            p1 = new Point2D.Double(px, py);
            px = (controlPoints.xpoints[index-1]*2.0 - controlPoints.xpoints[index]*5.0 + controlPoints.xpoints[index+1]*4.0-controlPoints.xpoints[index+2])*Math.pow(tInSegment,2.)/2.0;
            py = (controlPoints.ypoints[index-1]*2.0 - controlPoints.ypoints[index]*5.0 + controlPoints.ypoints[index+1]*4.0-controlPoints.ypoints[index+2])*Math.pow(tInSegment,2.)/2.0;
            p2 = new Point2D.Double(px, py);
            px = ( controlPoints.xpoints[index+1] - controlPoints.xpoints[index-1])*(tInSegment)/2.0;
            py = ( controlPoints.ypoints[index+1] - controlPoints.ypoints[index-1])*(tInSegment)/2.0;
            p3 = new Point2D.Double(px, py);
            px = controlPoints.xpoints[index]*2.0/2.0;
            py = controlPoints.ypoints[index]*2.0/2.0;
            p4 = new Point2D.Double(px, py);

            xs[indexPoint] = p1.getX() + p2.getX() + p3.getX() + p4.getX();
            ys[indexPoint++] = p1.getY() + p2.getY() + p3.getY() + p4.getY();

            t += step;
        }
        points = new DoublePolygon( xs, ys, xs.length );
        computeMax();
    }

    public void filter( DoublePolygon _ps ){
        filter( _ps, 1 );
    }
    public void filter( DoublePolygon ps, int _nb ){
        for( int n = 0; n < _nb; n++ ){
            for(int i = 1; i < ps.npoints-1; i++){
                double x = (ps.xpoints[i-1]+ps.xpoints[i]*2.+ps.xpoints[i+1])/4.;
                double y = (ps.ypoints[i-1]+ps.ypoints[i]*2.+ps.ypoints[i+1])/4.;
                ps.xpoints[i] = x;
                ps.ypoints[i] = y;
            }
        }
    }

    public double getX(int index){
        if(index < 0 || index >= points.npoints)
            return -1.;
        else
            return points.xpoints[index];
    }
    public double getY(int index){
        if(index < 0 || index >= points.npoints)
            return -1.;
        else
            return points.ypoints[index];
    }
    public double getMaxX(){
        return maxX;
    }
    public double getMaxY(){
        return maxY;
    }
    public double getMaxH(){
        return maxH;
    }
    public double getMinH(){
        return minH;
    }

    protected int getControlPointWithT(double t){
        double lengthFactorT = t * totalLength;

        for(int i = 0; i < arcLengthes.length; i++)
            if(arcLengthes[i].getY() > lengthFactorT)
                return i-1;
        return arcLengthes.length-1;
    }

    protected void computeMax(){
        maxX = Double.MIN_VALUE;
        maxY = Double.MIN_VALUE;
        for(int i = 0; i < points.npoints; i++){
            if(points.ypoints[i] > maxY)
                maxY = points.ypoints[i];
            if(points.xpoints[i] > maxX)
                maxX = points.xpoints[i];
        }
    }

    public double computeAngles(Rectangle r){
        double[] angles = new double[points.npoints];
        angles[0] = 0.;
        angles[angles.length-1] = 180.;
        for(int i = 1; i < points.npoints; i++){
            angles[i] = computeAngle(points.xpoints[i-1], points.ypoints[i-1], points.xpoints[i], points.ypoints[i], i);
        }
        double x_dist = points.xpoints[points.npoints-1] - points.xpoints[0];
        double x_limit = x_dist / 3.;
        double a_limit = 15.;//angle limit in degres
        int i_limit = -1;
        boolean finished = false;
        for(int i = 0; i < angles.length && !finished; i++){
            if(angles[i] < a_limit) {
                i_limit = -1;
                continue;
            }
            if(i_limit == -1) i_limit = i;
            double d = points.xpoints[i] - points.xpoints[i_limit];
            if(d > x_limit)
                finished = true;
        }
        if(i_limit == -1){
            System.out.println("Problem finding the threshold level");
            return 0.;
        }
        else{
            double x_step = x_dist / r.getWidth();
            double step = points.xpoints[i_limit] / x_dist;
            System.out.println("i_limit = " + i_limit + ", x correspondant = " + points.xpoints[i_limit]+", x_step = " + x_step + ", step = " + step);
            x_dist = maxH-minH;
            x_step = x_dist / r.getWidth();
            System.out.println("MaxH = " + maxH + ", et minH = " + minH +"\nx_step = " + x_step + ", width = " + r.getWidth());
            System.out.println("Threshold level -> " + (minH+(step*x_dist)));
            return (minH+(step*x_dist));
        }
    }

    public double computeAngle(double x1, double y1, double x2, double y2, int i){
        double TINY = 0.000000001;
        double x = x2 - x1 + TINY;
        double y = y2 - y1 + TINY;
        double xx = x*x;
        double yy = y*y;
        double norm = Math.sqrt(xx+yy);
        Point2D v = new Point2D.Double(x/norm, y/norm);
        double dot = vorig.getX()*v.getX() + vorig.getY()*v.getY();
        double angle = Math.acos(dot) / Math.PI * 180.;
        System.out.println("Point [" + x2 + ", " + y2 + "]");
        System.out.println("Angle for ArrayList " + i + " ---> " + angle);
        return angle;
    }

    public void drawCrtlPoints(Graphics g, Rectangle r){
        g.setColor(Color.GREEN);
        if(controlPoints != null){
            for(int i = 0; i < controlPoints.npoints; i++){
                double x2 = controlPoints.xpoints[i];
                double y2 = controlPoints.ypoints[i];

                double rapX = ((double)x2 / (double)maxX);
                double x2b = ((double)r.getWidth() * rapX);
                double rapY = ((double)y2 / (double)maxY);
                double y2b = ((double)r.getHeight() * rapY);

                g.fillOval((int)x2b,(int)r.getHeight()-(int)y2b,3,3);
            }
        }
    }

    public void draw(Graphics g, Rectangle r){
        g.setColor(Color.RED);
        if(points != null){
            for(int i = 1; i < points.npoints; i++){
                double x1 = points.xpoints[i-1];
                double y1 = points.ypoints[i-1];
                double x2 = points.xpoints[i];
                double y2 = points.ypoints[i];

                double rapX = ((double)x1 / (double)maxX);
                double x1b = ((double)r.getWidth() * rapX);
                double rapY = ((double)y1 / (double)maxY);
                double y1b = ((double)r.getHeight() * rapY);

                rapX = ((double)x2 / (double)maxX);
                double x2b = ((double)r.getWidth() * rapX);
                rapY = ((double)y2 / (double)maxY);
                double y2b = ((double)r.getHeight() * rapY);

                g.drawLine((int)x1b,(int)r.getHeight()-(int)y1b,(int)x2b,(int)r.getHeight()-(int)y2b);
                g.fillOval((int)x2b,(int)r.getHeight()-(int)y2b,3,3);
            }
        }
    }

    static protected CatmullCurve generateCCurveFromRoi2(Roi r, int startx, int starty, int w, int h, int type, float divisor){
        PolygonRoi roi = (PolygonRoi)r;
        int [] xs = roi.getXCoordinates();
        int [] ys = roi.getYCoordinates();
        int nb = roi.getNCoordinates();
        Rectangle rect = roi.getBounds();
        int rx = (int)rect.getX();
        int ry = (int)rect.getY();
        Point.Double [] points = new Point.Double[nb+1];
        if(type == 0)//Neuron
            for(int i = 0; i < nb; i++)
                points[i] = new Point.Double(startx+rx+xs[i], starty+ry+ys[i]);
        else if(type == 1)//Hole
            for(int i = 0; i < nb; i++){
                int x = rx+xs[i];
                int y = ry+ys[i];
                double dx = 0;
                double dy = 0;
                double x2 = x + dx;
                double y2 = y + dy;
                points[i] = new Point.Double(startx+x2, starty+y2);
            }
        points[points.length-1] = new Point.Double(points[0].getX(), points[0].getY());
        int nb_min = 10;
        int test = (int)((float)points.length / divisor);
        int size = (test > nb_min) ? test : nb_min;
        CatmullCurve cc = new CatmullCurve(points, 0, 1, size);

        cc.filter(cc.points);
        cc.filter(cc.points);
        cc.filter(cc.points);
        return cc;
    }
    
    public PolygonRoi toRoi(){
        float[] xs = new float[points.npoints];
        float[] ys = new float[points.npoints];
        for( int i = 0; i < points.npoints; i++ ){
            xs[i] = ( float )points.xpoints[i];
            ys[i] = ( float )points.ypoints[i];
        }
        return new PolygonRoi( xs, ys, xs.length, Roi.POLYGON );
    }

    public PolygonRoi toRoiCtrlPoints(){
        float[] xs = new float[controlPoints.npoints];
        float[] ys = new float[controlPoints.npoints];
        for( int i = 0; i < controlPoints.npoints; i++ ){
            xs[i] = ( float )controlPoints.xpoints[i];
            ys[i] = ( float )controlPoints.ypoints[i];
        }
        return new PolygonRoi( xs, ys, xs.length, Roi.POLYGON );
    }

    public PolygonRoi toRoiPolygon(){
        int[] xs = new int[controlPoints.npoints];
        int[] ys = new int[controlPoints.npoints];
        for( int i = 0; i < controlPoints.npoints; i++ ){
            xs[i] = ( int )controlPoints.xpoints[i];
            ys[i] = ( int )controlPoints.ypoints[i];
        }
        return new PolygonRoi( xs, ys, xs.length, Roi.POLYGON );
    }

    public PolygonRoi toRoiSpecificType( int _type ){
        float[] xs = new float[points.npoints];
        float[] ys = new float[points.npoints];
        for( int i = 0; i < points.npoints; i++ ){
            xs[i] = ( float )points.xpoints[i];
            ys[i] = ( float )points.ypoints[i];
        }
        return new PolygonRoi( xs, ys, xs.length, _type );
    }
    
    static public CatmullCurve [] execute(ImageProcessor ip, HoleRoi neuron){
        if(debug2)
            System.out.println("Begin ContourFinder::execute");
        int w = ip.getWidth();
        int h = ip.getHeight();

        ByteProcessor bp = (ByteProcessor)(ip.duplicate());
        bp.invert();
        Roi [] holes = neuron.getHoles();
        int nbHoles = 0;
        if(holes != null)
            nbHoles = holes.length;
        if(debug)
            if( holes != null && holes.length > 0 )
                System.out.println("Nb Holes -> " + holes.length);

        CatmullCurve [] contours = new CatmullCurve[1 + nbHoles];//Contains the neuron and holes contours, neuron is in position 0 of the array
        contours[0] = generateCCurveFromRoi2(neuron, 0, 0, w, h, 0, 1);
        for(int i = 0; i < nbHoles; i++)
            contours[i+1] = generateCCurveFromRoi2(holes[i], neuron.getBounds().x, neuron.getBounds().y, w, h, 1, 1);
        if(debug2)
            System.out.println("End ContourFinder::execute");
        return contours;
    }
    
    static public CatmullCurve execute( int _w, int _h, Roi neuron){
        return generateCCurveFromRoi2( neuron, 0, 0, _w, _h, 0, 1 );
    }
    
    //In this case, we don't use the possible holes
    static public CatmullCurve [] executeForGraph(ImageProcessor ip, HoleRoi neuron){
        if(debug2)
            System.out.println("Begin ContourFinder::executeForGraph");
        int w = ip.getWidth();
        int h = ip.getHeight();

        ByteProcessor bp = (ByteProcessor)(ip.duplicate());
        bp.invert();

        CatmullCurve [] contours = new CatmullCurve[1 + 0];//Contains the neuron and 0 hole contours, neuron is in position 0 of the array
        contours[0] = generateCCurveFromRoi2(neuron, 0, 0, w, h, 0, 3);
        if(debug2)
            System.out.println("End ContourFinder::executeForGraph");
        return contours;
    }
}
