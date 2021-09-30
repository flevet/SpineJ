/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SpineJ.Tools.Misc;

import ij.IJ;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.measure.Calibration;
import ij.measure.CurveFitter;
import ij.measure.Minimizer;
import ij.process.ImageProcessor;
import ij.util.Tools;
import java.awt.Color;
import java.util.ArrayList;

/**
 *
 * @author Florian Levet
 */
public class MyCustomFitter {
    double[] m_x = null, m_y = null;
    double m_d = 0., m_goodnessFit = 0.;

    CurveFitter m_cf;
    String m_equation = "y = a + (b-a)*exp(-(x-c)*(x-c)/(0.360674*d*d))";
    
    static double m_initialC = 8., m_initialD = 16.;
    
    public MyCustomFitter( ArrayList <Double> _x, ArrayList <Double> _y ){
        m_x = new double[_x.size()];
        m_y = new double[_y.size()];
        for(int i = 0; i < _x.size(); i++){
            m_x[i] = _x.get(i);
            m_y[i] = _y.get(i);
        }
        
        m_cf = new CurveFitter( m_x, m_y );
       
    }
    
    public MyCustomFitter( double _x [], double _y [] ){
        m_x = _x;
        m_y = _y;
        
        m_cf = new CurveFitter( m_x, m_y );
       
    }
    
    public ImageProcessor doFit( Calibration _cal ){
        try {
            double initialParameters [] = { 0., 0., m_initialC, m_initialD };
            for( int n = 0; n < initialParameters.length; n++ )
                IJ.log( "" + initialParameters[n] );
            m_cf.doFit( CurveFitter.GAUSSIAN );
            if ( m_cf.getStatus() == Minimizer.INITIALIZATION_FAILURE ) {
                IJ.beep();
                IJ.showStatus(m_cf.getStatusString());
                IJ.log("Curve Fitting Error:\n"+m_cf.getStatusString());
                return null;
            }
            if ( Double.isNaN( m_cf.getSumResidualsSqr() ) ) {
                IJ.beep();
                IJ.showStatus( "Error: fit yields Not-a-Number" );
                return null;
            }
            
        } catch ( Exception e ) {
            IJ.handleException( e );
            return null;
        }

        m_d = 2.354819 * m_cf.getParams()[3];
        m_goodnessFit = m_cf.getRSquared();
        IJ.log( m_cf.getResultString() );
        
        return plot( m_cf, _cal );
    }
    
    public ImageProcessor plot( CurveFitter cf, Calibration _cal ) {
        double[] x = cf.getXPoints();
        double[] y = cf.getYPoints();
        if (cf.getParams().length<cf.getNumParams()) {
            Plot plot = new Plot(cf.getFormula(),"Distance (" + _cal.getXUnit() + ")","GrayValue",x,y);
            plot.setColor(Color.BLUE);
            plot.addLabel(0.02, 0.1, cf.getName());
            plot.addLabel(0.02, 0.2, cf.getStatusString());
            return plot.getProcessor();
        }
        double[] a = Tools.getMinMax(x);
        double xmin=a[0], xmax=a[1]; 
        a = Tools.getMinMax(y);
        double ymin=a[0], ymax=a[1]; 
        float[] px = new float[100];
        float[] py = new float[100];
        double inc = (xmax-xmin)/99.0;
        double tmp = xmin;
        for (int i=0; i<100; i++) {
            px[i]=(float)tmp;
            tmp += inc;
        }
        double[] params = cf.getParams();
        for (int i=0; i<100; i++)
            py[i] = (float)cf.f(params, px[i]);
        a = Tools.getMinMax(py);
        ymin = Math.min(ymin, a[0]);
        ymax = Math.max(ymax, a[1]);
        Plot plot = new Plot(cf.getFormula(),"Distance (" + _cal.getXUnit() + ")","GrayValue",px,py);
        plot.setLimits(xmin, xmax, ymin, ymax);
        plot.setColor(Color.RED);
        plot.addPoints(x, y, PlotWindow.CIRCLE);
        plot.setColor(Color.BLUE);
        double yloc = 0.1;
        double yinc = 0.085;
        plot.addLabel(0.02, yloc, cf.getName()); yloc+=yinc;
        plot.addLabel(0.02, yloc, cf.getFormula());  yloc+=yinc;
        double[] p = cf.getParams();
        int n = cf.getNumParams();
        char pChar = 'a';
        for (int i = 0; i < n; i++) {
            plot.addLabel(0.02, yloc, pChar+" = "+IJ.d2s(p[i],5,9));
            yloc+=yinc;
            pChar++;
        }
        plot.addLabel(0.02, yloc, "R^2 = "+IJ.d2s(cf.getRSquared(),4));  yloc+=yinc;
        plot.setColor(Color.BLUE);
        return plot.getProcessor();                                    
    }
    
    public double getD(){
        return Math.abs(m_d);
    }
    public double getGoodnessFit(){
        return m_goodnessFit;
    }
    
    static public void setInitialC( double _val ){
        m_initialC = _val;
    }
    static public void setInitialD( double _val ){
        m_initialD = _val;
    }
    static public double getInitialC(){
        return m_initialC;
    }
    static public double getInitialD(){
        return m_initialD;
    }
}
