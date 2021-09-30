/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SpineJ.Analysis;

import SpineJ.Tools.Misc.HoleRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.ByteProcessor;
import ij.process.FloodFiller;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 *
 * @author Florian Levet
 */
public class HoleRoiNeuron extends HoleRoi{
    protected Point2D.Double m_centroid = new Point2D.Double();
    
    public HoleRoiNeuron( Roi _roi, ImageProcessor _bp, FloodFiller _ff ){
        this( _roi, _bp, _ff, 20.f );
    }
    
    public HoleRoiNeuron( Roi _roi, ImageProcessor _bp, FloodFiller _ff, float _minSizeHoles ){
        super( _roi );
        ImageProcessor mask = getMask();
        m_maskWithHoles = ( ByteProcessor )mask.duplicate();
            
        int x = -1, y = -1;
        for( int i = 0; i < mask.getWidth() && x == -1; i++ )
            for( int j = 0; j < mask.getHeight() && y == -1; j++ )
                if( mask.get( i, j ) == 255 ){
                    x = i;
                    y = j;
                }
        x += getBounds().x;
        y += getBounds().y;
        _ff.particleAnalyzerFill( x, y, 0, 0, m_maskWithHoles, getBounds() );
        
        // Here, the mask for the object is perfect. Unfortunately, we have to add a step to be sure that the intern difference between 4 connectivity 
        //and 8 connectivy in the particle analyzer does not mess things up with the hole detection
        //To do so we apply a 4-connectivity flood filler on all the holes that touche the edges of the pic
        ImageProcessor edgeHolesFloodFilled = m_maskWithHoles.duplicate();
        edgeHolesFloodFilled.setColor( 255 );
        FloodFiller ff2 = new FloodFiller( edgeHolesFloodFilled );
        int x2 = 0, x3 = edgeHolesFloodFilled.getWidth() - 1, y2 = 0, y3 = edgeHolesFloodFilled.getHeight() - 1;
        for( int val = 0; val < edgeHolesFloodFilled.getHeight(); val++ ){
            if( edgeHolesFloodFilled.get( x2, val ) == 0 )
                ff2.fill( x2, val );
            if( edgeHolesFloodFilled.get( x3, val ) == 0 )
                ff2.fill( x3, val );
        }
        for( int val = 0; val < edgeHolesFloodFilled.getWidth(); val++ ){
            if( edgeHolesFloodFilled.get( val, y2 ) == 0 )
                ff2.fill( val, y2 );
            if( edgeHolesFloodFilled.get( val, y3 ) == 0 )
                ff2.fill( val, y3 );
        }
        //To determine the holes, we use the wand tools and the flood filler one with a 4-connectivity cause the ParticleAnalyzer does not work in 4-connectivity option
        Wand wand = new Wand( edgeHolesFloodFilled );
        ArrayList < Roi > allHoles = new ArrayList < Roi >();
        for( x = 1; x < edgeHolesFloodFilled.getWidth() - 1; x++ )
            for( y = 1; y < edgeHolesFloodFilled.getHeight() -1; y++ ){
                if( edgeHolesFloodFilled.get( x, y ) != 0 ) continue;
                wand.autoOutline( x, y, 0, 0, Wand.FOUR_CONNECTED );
                allHoles.add( new PolygonRoi( wand.xpoints, wand.ypoints, wand.npoints, Roi.FREEROI ) );
                ff2.fill( x, y );
            }
        m_maskWithHoles.setColor( 255 );
        if( allHoles.isEmpty() )
            m_holes = new Roi[0];
        else{
            m_maskWithHoles.setColor( 255 );
            ArrayList < Roi > tmp = new ArrayList < Roi >();
            for( int n = 0; n < allHoles.size(); n++ ){
                Roi roi = allHoles.get( n );
                double area = ImageStatistics.getStatistics( roi.getMask(), ParticleAnalyzer.AREA, null ).area;
                if( area > _minSizeHoles )
                    tmp.add( roi );
                else{
                    m_maskWithHoles.setRoi( roi );
                    m_maskWithHoles.fill( roi.getMask() );
                    m_maskWithHoles.resetRoi();
                }
            }
            m_holes = new Roi[tmp.size()];
            m_holes = tmp.toArray( m_holes );

        }
            
        Rectangle r = getBounds();
        for( int i = 0; i < m_maskWithHoles.getWidth(); i++ )
            for( int j = 0; j < m_maskWithHoles.getHeight(); j++ )
                if( m_maskWithHoles.get( i, j ) == 255 )
                    m_area++;
        for( int i = 0; i < m_maskWithHoles.getWidth(); i++ )
            for( int j = 0; j < m_maskWithHoles.getHeight(); j++ )
                if( m_maskWithHoles.get( i, j ) == 255 ){
                    x = r.x + i; y = r.y + j;
                    m_centroid.setLocation( m_centroid.x + ( x / m_area ), m_centroid.y + ( y / m_area ) );
                }
    }
    
    @Override
    public ImageProcessor getMaskWithHoles(){
        return m_maskWithHoles;
    }
    
    public void fill( ImageProcessor _ip, int _foreground, int _background ){
        int w = _ip.getWidth(), h = _ip.getHeight();
        _ip.setColor( _foreground );
        _ip.setRoi( this );
        _ip.fill( getMask() );
        
        _ip.setColor( _background );
        for (Roi m_hole : m_holes) {
            _ip.setRoi(m_hole);
            _ip.fill(m_hole.getMask());
        }
        _ip.resetRoi();
    }
    
    @Override
    public Point2D.Double getCentroid(){
        return m_centroid;
    }
}
