/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SpineJ.Tools.Misc;

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.awt.geom.Point2D;

/**
 *
 * @author Florian Levet
 */
public abstract class HoleRoi extends PolygonRoi{
    protected Roi [] m_holes = null;
    protected ByteProcessor m_maskWithHoles = null;
    protected double m_area = 0.;
    
    public HoleRoi( Roi _roi ){
        super( _roi.getPolygon(), _roi.getType() );
    }
    public Roi [] getHoles(){
        return m_holes;
    }
    public double getArea(){
        return m_area;
    }
    public abstract Point2D.Double getCentroid();
    public abstract ImageProcessor getMaskWithHoles();
    
}
