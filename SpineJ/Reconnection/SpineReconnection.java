/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SpineJ.Reconnection;

import SpineJ.GradientVector.GradientDijkstra;
import SpineJ.GradientVector.GradientVector;
import SpineJ.Widgets.SpineReconnectionCanvas;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.measure.Measurements;
import ij.plugin.filter.EDM;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Point;
import java.util.ArrayList;

/**
 *
 * @author Florian Levet
 */
public class SpineReconnection {
    protected ImageProcessor m_ip = null, m_binary = null, m_costs = null, m_binaryBefore = null;;
    protected GradientVector m_gv = null;
    
    protected Roi [] m_rois = null;
    protected int [] m_areas = null;
    
    protected ShortProcessor m_roisProcessor = null;
    protected ByteProcessor m_skeletons = null, m_edm = null;
    protected ColorProcessor m_result = null, m_resultBefore = null;
    protected SpineReconnectionCanvas m_src = null;
    
    public SpineReconnection( ImageProcessor _ip ){
        m_ip = _ip;
        m_binary = m_ip;
        m_gv = new GradientVector();
        m_costs = m_gv.generateCostByteImage( m_ip.duplicate() );
        
        determineRois();
        
        ImagePlus imp = new ImagePlus( "Reconnection spines", m_ip );
        m_src = new SpineReconnectionCanvas( imp, m_result, this );
        ImageWindow iw = new ImageWindow( imp, m_src );
        iw.setVisible( true );
    }
    
    public SpineReconnection( ImageProcessor _ip, ImageProcessor _binary, Roi [] _rois ){
        m_ip = _ip;
        m_binary = _binary;
        m_binary.invert();
        m_gv = new GradientVector();
        m_costs = m_gv.generateCostByteImage( m_ip.duplicate() );
        
        determineRois( _rois );
        
        ImagePlus imp = new ImagePlus( "Reconnection spines", m_ip );
        m_src = new SpineReconnectionCanvas( imp, m_result/*m_binary*/, this );
        ImageWindow iw = new ImageWindow( imp, m_src );
        iw.setVisible( true );
    }
    
    public ImageProcessor getBinaryDisplayImage(){
        return m_result;
    }
    public ImageProcessor getBinaryImage(){
        ColorProcessor cp = (ColorProcessor)m_src.getBinaryImage();
        ByteProcessor bp = new ByteProcessor( cp.getWidth(), cp.getHeight());
        bp.setColor( Color.black );
        bp.fill();
        for( int x = 0; x < cp.getWidth(); x++ )
            for( int y = 0; y < cp.getHeight(); y++ ){
                
                if(!cp.getColor(x, y).equals(Color.black)){
                    bp.set( x, y, 255 );
                }
            }
        return bp;
    }
    public SpineReconnectionCanvas getCanvas(){
        return m_src;
    }
    public void close(){
        m_src.getImage().changes = false;
        m_src.getImage().close();
    }
    
    private void determineRois(){
        if( !( m_ip instanceof ByteProcessor ) ) return;
        
        ImageProcessor tmp = m_ip.duplicate();
        tmp.invert();
        int options = ParticleAnalyzer.ADD_TO_MANAGER + ParticleAnalyzer.CLEAR_WORKSHEET;// -> 64+2048 -> CLEAR_WORKSHEET, ADD_TO_MANAGER | 2053 with the 4 of the OUTLINES
        int measurements = Measurements.AREA + Measurements.CIRCULARITY + Measurements.CENTROID;
        ParticleAnalyzer pa = new ParticleAnalyzer( options, measurements, null, 0., Double.MAX_VALUE, 0., Double.MAX_VALUE );
        ImagePlus useless2 = new ImagePlus( "Useless", tmp.duplicate() );
        pa.analyze(useless2);
        Frame frame = WindowManager.getFrame("ROI Manager");
        if (frame == null) {
            IJ.run("ROI Manager...");
        }
        frame = WindowManager.getFrame("ROI Manager");
        RoiManager roiManager = (RoiManager) frame;
        roiManager.runCommand( "Show None" );
        roiManager.setVisible( false );
        determineRois( roiManager.getRoisAsArray() );
        roiManager.dispose();
    }
    
    private void determineRois( Roi [] _rois ){
        m_result = new ColorProcessor( m_binary.getWidth(), m_binary.getHeight() );
        m_result.setColor( Color.black );
        m_result.fill();
        int [] color = new int[3];
        color[0] = color[1] = color[2] = 255;
        for( int x = 0; x < m_binary.getWidth(); x++ )
            for( int y = 0; y < m_binary.getHeight(); y++ )
                if( m_binary.get( x, y ) == 255 )
                    m_result.putPixel( x, y, color );
        m_resultBefore = ( ColorProcessor )m_result.duplicate();
        m_binaryBefore = m_binary.duplicate();
        
                    
        m_rois = _rois;
        m_areas = determineAreas( m_rois );
        m_roisProcessor = new ShortProcessor( m_ip.getWidth(), m_ip.getHeight() );
        m_roisProcessor.setColor( m_rois.length );
        m_roisProcessor.fill();
        for( int n = 0; n < m_rois.length; n++ ){
            m_roisProcessor.setColor( n );
            m_roisProcessor.setRoi( m_rois[n] );
            m_roisProcessor.fill( m_rois[n].getMask() );
            m_roisProcessor.resetRoi();
        }
        
        m_skeletons = ( ByteProcessor )m_binary.duplicate();
        m_skeletons.invert();
        m_skeletons.skeletonize();
        
        m_edm = ( ByteProcessor )m_binary.duplicate();
        EDM edm = new EDM();
        edm.toEDM( m_edm );
    }
    
    protected int[] determineAreas( Roi [] rois ){
        int [] areas = new int[rois.length];
        for(int n = 0; n < rois.length; n++){
            ImageProcessor mask = rois[n].getMask();
            int area = 0;
            if(mask == null)
                area = (int)(rois[n].getBounds().getWidth()*rois[n].getBounds().getHeight());
            else{
                for(int i = 0; i < mask.getWidth(); i++)
                    for(int j = 0; j < mask.getHeight(); j++)
                        if(mask.get(i, j) == 255)
                            area++;
            }
            areas[n] = area;
        }
        return areas;
    }
    
    protected void testReconnection( ArrayList < Point > _path, int _indexN, int _indexS ){
        ByteProcessor bp = new ByteProcessor( m_ip.getWidth(), m_ip.getHeight() );
        bp.setColor( 255 );
        bp.fill();
        
        for( int n = 0; n < _path.size(); n++ ){
            int x = _path.get( n ).x, y = _path.get( n ).y;
            int index = m_roisProcessor.get( x, y );
            if( index != _indexN && index != _indexS ){
                bp.set( x, y, 0 );
            }
        }
        
        int val = m_edm.get( _path.get( _path.size() - 1 ).x, _path.get( _path.size() - 1 ).y );
        val /= 2;
        if( val <= 0 ) val = 1;
        if( val > 5 ) val = 5;
        for( int k = 0; k < val; k++ )
            bp.dilate();
        
        m_binaryBefore = m_binary.duplicate();
        m_resultBefore = ( ColorProcessor )m_result.duplicate();
        int [] color = new int[3];
        color[0] = 93;
        color[1] = 255;
        color[2] = 139;
        for( int x = 0; x < m_ip.getWidth(); x++ )
            for( int y = 0; y < m_ip.getHeight(); y++ )
                if( bp.get( x, y ) == 0 && m_binaryBefore.get(x, y) != 255){
                    m_binary.set( x, y, 255 );
                    m_result.putPixel( x, y, color );
                }
        
    }
    
    public ArrayList < Point > determineDirectionImage( int x1, int y1, int x2, int y2 ){
        int indexNeuron = m_roisProcessor.get( x1, y1 ), indexSpine = m_roisProcessor.get( x2, y2 );
        if( indexNeuron == m_rois.length || indexSpine == m_rois.length ) return null;
        
        int xN = 0, yN = 0, xS = 0, yS = 0;
        if( m_areas[indexNeuron] > m_areas[indexSpine] ){
            xN = x1; yN = y1; xS = x2; yS = y2;
        }
        else{
            int tmp = indexSpine;
            indexSpine = indexNeuron;
            indexNeuron = tmp;
            xN = x2; yN = y2; xS = x1; yS = y1;
        }
        Point end = new Point( xN, yN ), start = new Point( xS, yS );
        GradientDijkstra gd = new GradientDijkstra();
        byte [][] dijs = gd.run( m_gv.getCosts(), start.x, start.y );
        ArrayList < Point > path = determinePath( dijs, start, end, 0, null );
        testReconnection( path, indexNeuron, indexSpine );
        return path;
    }
    protected ArrayList < Point > determinePath( byte[][] dirs, Point start, Point end, int id, ShortProcessor sp ){
        ArrayList < Point > currSegment = new ArrayList < Point >();
        if(end.x != start.x || end.y != start.y)
        {
            currSegment.add(new Point(end));
            for(Point point = new Point(end); point.x != start.x || point.y != start.y;)
            {
                switch(dirs[point.y][point.x])
                {
                case 0: // '\0'
                    point.x = start.x;
                    point.y = start.y;
                    break;

                case 1: // '\001'
                    point.x--;
                    point.y--;
                    break;

                case 2: // '\002'
                    point.y--;
                    break;

                case 3: // '\003'
                    point.x++;
                    point.y--;
                    break;

                case 4: // '\004'
                    point.x--;
                    break;

                case 5: // '\005'
                    point.x++;
                    break;

                case 6: // '\006'
                    point.x--;
                    point.y++;
                    break;

                case 7: // '\007'
                    point.y++;
                    break;

                case 8: // '\b'
                    point.x++;
                    point.y++;
                    break;
                }
                currSegment.add(new Point(point));
            }
        }
        return currSegment;
    }

    public void undo() {
        m_binary = m_binaryBefore;
        m_result = m_resultBefore;
    }
}
