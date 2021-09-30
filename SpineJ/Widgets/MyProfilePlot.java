/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SpineJ.Widgets;

import ij.ImagePlus;
import ij.gui.*;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import java.awt.Dimension;

/**
 *
 * @author Florian Levet
 */
public class MyProfilePlot extends ProfilePlot{
    
    public MyProfilePlot(){
        super();
    }
    
    public void execute( ImagePlus _imp, Line _roi ){
        this.imp = _imp;
        Calibration cal = imp.getCalibration();
        xInc = cal.pixelWidth;
        units = cal.getUnits();
        yLabel = cal.getValueUnit();
        
        ImageProcessor ip = imp.getProcessor();
        ip.setInterpolate(PlotWindow.interpolate);
        profile = _roi.getPixels();
        if ( profile != null){
            if (cal!=null && cal.pixelWidth!=cal.pixelHeight) {
                    double dx = cal.pixelWidth*(_roi.x2 - _roi.x1);
                    double dy = cal.pixelHeight*(_roi.y2 - _roi.y1);
                    double length = Math.round(Math.sqrt(dx*dx + dy*dy));
                    if (profile.length>1)
                            xInc = length/(profile.length-1);
            }
        }
        
        ImageCanvas ic = imp.getCanvas();
        if (ic!=null)
            magnification = ic.getMagnification();
        else
            magnification = 1.0;
    }
    
    public void execute( ImagePlus _imp, double _profile [] ){
        this.imp = _imp;
        Calibration cal = imp.getCalibration();
        xInc = cal.pixelWidth;
        units = cal.getUnits();
        yLabel = cal.getValueUnit();
        profile = _profile;
        ImageCanvas ic = imp.getCanvas();
        if (ic!=null)
            magnification = ic.getMagnification();
        else
            magnification = 1.0;
    }
    
    public ImageProcessor getProfileProcessor(){
        if (profile==null)
            return null;
        Dimension d = getPlotSize();
        String xLabel = "Distance ("+units+")";
        int n = profile.length;
        if (xValues==null) {
            xValues = new float[n];
            for (int i=0; i<n; i++)
                xValues[i] = (float)(i*xInc);
        }
        float[] yValues = new float[n];
        for (int i=0; i<n; i++)
            yValues[i] = (float)profile[i];
        Plot plot = new Plot("Plot of "+getShortTitle(imp), xLabel, yLabel, xValues, yValues);
        return plot.getProcessor();
    }
        
    String getShortTitle(ImagePlus imp) {
        String title = imp.getTitle();
        int index = title.lastIndexOf('.');
        if (index>0 && (title.length()-index)<=5)
            title = title.substring(0, index);
        return title;
    }
}
