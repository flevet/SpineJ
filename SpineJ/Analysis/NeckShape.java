/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SpineJ.Analysis;

import SpineJ.Tools.Misc.MyCustomFitter;
import SpineJ.Tools.Geometry.StraightLine;
import SpineJ.Widgets.MyProfilePlot;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 *
 * @author Florian Levet
 * Shape is a parallelepiped. This class is used to define a parallelepiped which is used to approximate the intensity profile at this location.
 * This profile intensity is fitted with a gaussian.
 */
public class NeckShape {
    protected StraightLine m_lineBasis = null;
    protected double m_width, m_height, m_intensityProfile [] = null, m_fittedWidth;
    protected ImageProcessor m_profileProcessor = null, m_gaussFitProcessor = null;
    protected PolygonRoi m_rectangleBasisFit = null, m_rectangleWidthByFit = null;
    protected double m_d = 0., m_goodnessFit = 0.;
    
    NeckShape( StraightLine _line, double _fromWidth, double _toWidth, double _stepWidth, double _height, double _minFitValue, ImagePlus _imp, ImageProcessor _ip ){
        m_lineBasis = _line;
        m_width = _toWidth;
        m_height = _height;
        Calibration cal = _imp.getCalibration();
        
        //We test width of the line from 400 nm to _width nm
        ArrayList <Double> widths = new ArrayList <Double>();
        double cur = _fromWidth;
        while(cur < _toWidth){
            widths.add(cur);
            cur += _stepWidth;
        }
        if(widths.get(widths.size() - 1) != _toWidth)
            widths.add(_toWidth);
        
        double curR = Double.MAX_VALUE;// 0.;
        for(Double w : widths){
            double halfWidth = w / 2., halfHeight = m_height / 2.;
            Point2D.Double p1 = m_lineBasis.findPoint(m_lineBasis.getP1().getX(), m_lineBasis.getP1().getY(), halfWidth );
            Point2D.Double p2 = m_lineBasis.findPoint(m_lineBasis.getP1().getX(), m_lineBasis.getP1().getY(), -halfWidth );
            /*Point2D.Double pp1 = m_lineBasisFit.findOrthoPoint( p1.getX(), p1.getY(), halfHeight );
            Point2D.Double pp2 = m_lineBasisFit.findOrthoPoint( p2.getX(), p2.getY(), halfHeight );
            Point2D.Double pp3 = m_lineBasisFit.findOrthoPoint( p2.getX(), p2.getY(), -halfHeight );
            Point2D.Double pp4 = m_lineBasisFit.findOrthoPoint( p1.getX(), p1.getY(), -halfHeight );*/

            Line line = new Line( p1.getX(), p1.getY(), p2.getX(), p2.getY() );
            line.setStrokeWidth( m_height/*3.f*/ );
            line.setImage( _imp );
            _imp.setRoi( line );

            MyProfilePlot plot = new MyProfilePlot();
            plot.execute( _imp, line );
            double[] intensityProfile = plot.getProfile();

            double [] xsGauss = new double[intensityProfile.length];
            for( int n = 0; n < intensityProfile.length; n++ )
                xsGauss[n] = n * cal.pixelWidth;
            MyCustomFitter mcf = new MyCustomFitter( xsGauss, intensityProfile );
            ImageProcessor gaussFitProcessorTmp = mcf.doFit( cal );
            
            if(mcf.getGoodnessFit() > _minFitValue && mcf.getD() < curR){
                /*float xs [] = new float[4], ys [] = new float[4];
                xs[0] = ( float )pp1.getX(); xs[1] = ( float )pp2.getX(); xs[2] = ( float )pp3.getX(); xs[3] = ( float )pp4.getX();
                ys[0] = ( float )pp1.getY(); ys[1] = ( float )pp2.getY(); ys[2] = ( float )pp3.getY(); ys[3] = ( float )pp4.getY();
                FloatPolygon fp = new FloatPolygon( xs, ys, xs.length );*/
                m_rectangleBasisFit = createRectangleFromLineAndDimension(m_lineBasis, halfWidth, halfHeight);// new PolygonRoi( fp, Roi.FREEROI );
                m_profileProcessor = plot.getProfileProcessor();
                m_gaussFitProcessor = gaussFitProcessorTmp;
                m_intensityProfile = intensityProfile;
                m_d = mcf.getD();
                m_goodnessFit = mcf.getGoodnessFit();
                m_width = w;
                curR = m_d;//m_goodnessFit;
                
                m_fittedWidth = m_d / cal.pixelWidth;
                m_rectangleWidthByFit = createRectangleFromLineAndDimension(m_lineBasis, m_fittedWidth / 2.f, halfHeight);
            }
            _imp.killRoi();
        }
    }
    
    public ImageProcessor getProfileProcessor(){
        return m_profileProcessor;
    }
    public ImageProcessor getGaussFitProcessor(){
        return m_gaussFitProcessor;
    }
    
    public double [] getIntensityProfile(){
        return m_intensityProfile;
    }
    
    public StraightLine getLineBasis(){
        return m_lineBasis;
    }
        
    public double getD(){
        return m_d;
    }
    public double getGoodnessFit(){
        return m_goodnessFit;
    }
    
    public boolean isInside( int _x, int _y ){
        if( m_rectangleBasisFit == null ) return false;
        return m_rectangleBasisFit.contains( _x, _y );
    }
    
    public double getWidth(){
        return m_width;
    }
    public double getHeight(){
        return m_height;
    }
    
    public double getFittedWidth(){
        return m_fittedWidth;
    }
    
    public PolygonRoi getNeckShapeUsedForFit(){
        return m_rectangleBasisFit;
    }
    
    public PolygonRoi getNeckShapeFromFit(){
        return m_rectangleWidthByFit;
    }
    
    private PolygonRoi createRectangleFromLineAndDimension(StraightLine _line, double _w, double _h){
        Point2D.Double p1 = _line.findPoint(_line.getP1().getX(), _line.getP1().getY(), _w );
        Point2D.Double p2 = _line.findPoint(_line.getP1().getX(), _line.getP1().getY(), -_w );
        Point2D.Double pp1 = _line.findOrthoPoint( p1.getX(), p1.getY(), _h );
        Point2D.Double pp2 = _line.findOrthoPoint( p2.getX(), p2.getY(), _h );
        Point2D.Double pp3 = _line.findOrthoPoint( p2.getX(), p2.getY(), -_h );
        Point2D.Double pp4 = _line.findOrthoPoint( p1.getX(), p1.getY(), -_h );
        float xs [] = new float[4], ys [] = new float[4];
        xs[0] = ( float )pp1.getX(); xs[1] = ( float )pp2.getX(); xs[2] = ( float )pp3.getX(); xs[3] = ( float )pp4.getX();
        ys[0] = ( float )pp1.getY(); ys[1] = ( float )pp2.getY(); ys[2] = ( float )pp3.getY(); ys[3] = ( float )pp4.getY();
        FloatPolygon fp = new FloatPolygon( xs, ys, xs.length );
        return new PolygonRoi( fp, Roi.FREEROI );
    }
    
    public Line2D.Double getLineUsedForFit(){
        Point2D.Double p1 = m_lineBasis.findPoint(m_lineBasis.getP1().getX(), m_lineBasis.getP1().getY(), m_width / 2.f );
        Point2D.Double p2 = m_lineBasis.findPoint(m_lineBasis.getP1().getX(), m_lineBasis.getP1().getY(), -m_width / 2.f );
        return new Line2D.Double(p1, p2);
    }
    
    public Line2D.Double getLineFromFit(){
        Point2D.Double p1 = m_lineBasis.findPoint(m_lineBasis.getP1().getX(), m_lineBasis.getP1().getY(), m_fittedWidth / 2.f );
        Point2D.Double p2 = m_lineBasis.findPoint(m_lineBasis.getP1().getX(), m_lineBasis.getP1().getY(), -m_fittedWidth / 2.f );
        return new Line2D.Double(p1, p2);
    }
}
