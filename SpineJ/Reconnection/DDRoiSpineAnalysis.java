/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SpineJ.Reconnection;

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;

/**
 *
 * @author Florian Levet
 */
public class DDRoiSpineAnalysis extends PolygonRoi{
    protected Roi [] m_holes = null;
    protected ByteProcessor m_maskWithHoles = null;
    protected int m_area = 0;
    
    public DDRoiSpineAnalysis( Roi _roi ){
        super( _roi.getPolygon(), _roi.getType() );
    }
    public DDRoiSpineAnalysis( Roi _roi, ImageProcessor _bin ){
        super( _roi.getPolygon(), _roi.getType() );
        generateHoles( _bin );
        computeArea();
    }
    public Roi [] getHoles(){
        return m_holes;
    }
    public void generateHoles( ImageProcessor _bin ){
        ArrayList < Roi > holes = new ArrayList < Roi >();
        m_maskWithHoles = ( ByteProcessor )getMask().duplicate();
        Rectangle r = getBounds();
        ImageProcessor original = _bin.duplicate();
        for( int i = 0; i < m_maskWithHoles.getWidth(); i++ )
            for( int j = 0; j < m_maskWithHoles.getHeight(); j++ ){
                if( m_maskWithHoles.get( i, j ) == 255 ){
                    int x = r.x + i, y = r.y + j;
                    if( original.get( x, y ) == 255 ){
                        Wand w = new Wand( original );
                        w.autoOutline( x, y, 255, 255, Wand.FOUR_CONNECTED );
                        PolygonRoi proi = new PolygonRoi( w.xpoints, w.ypoints, w.npoints, Roi.FREEROI );
                        original.setRoi( proi );
                        original.fill( proi.getMask() );
                        original.resetRoi();
                        holes.add( proi );
                    }
                }
            }
        if( !holes.isEmpty() ){
            m_holes = new Roi[holes.size()];
            m_holes = holes.toArray( m_holes );
        }
        if( m_holes != null )
            for( int n = 0; n < m_holes.length; n++ ){
                Rectangle r2 = m_holes[n].getBounds();
                ImageProcessor mask = m_holes[n].getMask();
                for( int i = 0; i < mask.getWidth(); i++ )
                    for( int j = 0; j < mask.getHeight(); j++ )
                        if( mask.get( i, j ) == 255 ){
                            int x = r2.x + i - r.x, y = r2.y + j - r.y;
                            m_maskWithHoles.set( x, y, 0 );
                        }
            }
    }
    public int nbHoles(){
        return ( m_holes == null ) ? 0 : m_holes.length;
    }
    public ImageProcessor getMaskWithHoles(){
        return m_maskWithHoles;
    }
    
    public void computeArea(){
        m_area = 0;
        for( int i = 0; i < m_maskWithHoles.getWidth(); i++ )
            for( int j = 0; j < m_maskWithHoles.getHeight(); j++ )
                if( m_maskWithHoles.get( i, j ) == 0 )
                    m_area++;
    }
    
    public int getArea(){
        return m_area;
    }
    
    public void fill(ImageProcessor _ip){
        _ip.setColor(Color.WHITE);
        _ip.fill(this.getMask());
        _ip.setColor(Color.BLACK);
        for(Roi roi: m_holes)
            _ip.fill(roi.getMask());
    }
    
    @Override
    public boolean contains( int _x, int _y ){
        if( !super.contains( _x, _y ) )
            return false;
        boolean outside = false;
        if( m_holes != null )
            for( int n = 0; n < m_holes.length && !outside; n++ )
                outside = m_holes[n].contains( _x, _y );
        return !outside;
    }
}
