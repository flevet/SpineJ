/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SpineJ.Filtering;

import SpineJ.Reconnection.DDRoiSpineAnalysis;
import SpineJ.Reconnection.SpineReconnection;
import SpineJ.Tools.GUI.Packer;
import SpineJ.Tools.Wavelets.ImageCanvasRoi;
import SpineJ.Tools.Wavelets.InfosPanel;
import SpineJ.Tools.Wavelets.WaveletDialogSimple;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.io.FileInfo;
import ij.measure.Measurements;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import java.awt.ComponentOrientation;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import javax.swing.*;

/**
 *
 * @author Florian Levet
 */
public class WaveletDialogSpineAnalysis extends WaveletDialogSimple implements WindowListener{
    protected JRadioButton m_debugImageButton;
    protected JTextField m_sizeField;
    protected ImagePlus m_impDebug = null;
    protected ImageCanvasRoi ic = null;
    protected ImageWindow iw = null;
    protected JButton m_ok;
    protected int m_indexHoles = -1;
    protected Roi [] m_rois = null;
    protected ArrayList < DDRoiSpineAnalysis > m_ddrois = new ArrayList < DDRoiSpineAnalysis >();
    protected String m_dir = null, m_titleDebug = null;
    protected Rectangle m_boundsDebugImage = null;

    public WaveletDialogSpineAnalysis(){
        super();
    }
    public WaveletDialogSpineAnalysis( ImagePlus _imp ){
        super( _imp );
        FileInfo fi = _imp.getOriginalFileInfo();
        if( fi != null )
            m_dir = fi.directory;
        m_titleDebug = "Debug for " + _imp.getTitle();
        m_impDebug = new ImagePlus( m_titleDebug, m_imp.getProcessor().duplicate() );
        ic = new ImageCanvasRoi( m_impDebug );
        iw = new ImageWindow( m_impDebug, ic );
        iw.setVisible( true );
        iw.addWindowListener( this );
        this.addWindowListener( this );
        this.setTitle( this.getTitle() + " - SpineJ" );
        generateDebugImage();
    }
    
    public WaveletDialogSpineAnalysis( ImagePlus _imp, int _size ){
        super( _imp, _size);
        FileInfo fi = _imp.getOriginalFileInfo();
        if( fi != null )
            m_dir = fi.directory;
        m_titleDebug = "Debug for " + _imp.getTitle();
        m_impDebug = new ImagePlus( m_titleDebug, m_imp.getProcessor().duplicate() );
        ic = new ImageCanvasRoi( m_impDebug );
        iw = new ImageWindow( m_impDebug, ic );
        iw.setVisible( true );
        iw.addWindowListener( this );
        this.addWindowListener( this );
        this.setTitle( this.getTitle() + " - SpineJ" );
        generateDebugImage();
    }
    
    public WaveletDialogSpineAnalysis( ImagePlus _imp, int _size, double[] _factors ){
        super( _imp, _size, _factors );
        closeDialog();
    }
    
    public JButton getOkButton(){
        return m_ok;
    }

    @Override
    protected JPanel constructElement1(){
        JPanel panel = super.constructElement1();
        JPanel miscPanel = new JPanel();
        miscPanel.applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        miscPanel.setBorder(BorderFactory.createTitledBorder("Misc"));
        Packer pkMisc = new Packer( miscPanel );
        m_debugImageButton = new JRadioButton();
        m_debugImageButton.setText( "Segmentation Debug" );
        m_debugImageButton.setSelected( true );
        JLabel sizeLabel = new JLabel();
        sizeLabel.setText( "Min Size:" );
        m_sizeField = new JTextField();
        m_sizeField.setText( "10" );
        m_ok = new JButton();
        m_ok.setText( "Generate Segmentation" );
        pkMisc.pack(m_debugImageButton ).gridx( 0 ).gridy( 0 ).remainx().inset( 3, 3, 3, 3 );
        pkMisc.pack( sizeLabel ).gridx( 0 ).gridy( 1 ).inset( 3, 3, 3, 3 );
        pkMisc.pack( m_sizeField ).gridx( 1 ).gridy( 1 ).remainx().fillx().inset( 3, 3, 3, 3 );
        pkMisc.pack( m_ok ).gridx( 0 ).gridy( 3 ).remainx().inset( 3, 3, 3, 3 );

        packerElement1.pack( miscPanel ).gridx( 0 ).gridy( 6 ).fillx().inset( 5, 5, 5, 5 );

        nbPanels = 3;
        for( int i = 0; i < infosPanels.length; i++ ){
            infosPanels[i].setType( InfosPanel.NONE );
        }
        infosPanels[1].setType( InfosPanel.ADDED );

        return panel;
    }

    @Override
    protected void addListener(){
        super.addListener();
        m_debugImageButton.addActionListener( this );
        m_sizeField.addActionListener( this );
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        super.actionPerformed( e );
        Object o = e.getSource();
        if( o == m_debugImageButton ){
            if(iw != null)
                iw.setVisible(m_debugImageButton.isSelected() );
            else if(m_debugImageButton.isSelected()){
                m_impDebug = new ImagePlus( m_titleDebug, m_imp.getProcessor().duplicate() );
                ic = new ImageCanvasRoi( m_impDebug );
                iw = new ImageWindow( m_impDebug, ic );
                iw.setBounds(m_boundsDebugImage);
                iw.setVisible( true );
                iw.addWindowListener( this );
                generateDebugImage();
            }
        }
        else if( o instanceof JTextField || o instanceof JRadioButton ){
                generateDebugImage();
                m_impDebug.repaintWindow();
        }
     }
    
    public SpineReconnection reconnectSpines(){
        ByteProcessor result = new ByteProcessor( m_imp.getWidth(), m_imp.getHeight() );
        result.setColor( 255 );
        result.fill();
        result.setColor( 0 );
        for( int n = 0; n < m_ddrois.size(); n++ ){
            result.setRoi( m_ddrois.get( n ) );
            result.fill( m_ddrois.get( n ).getMaskWithHoles() );
            result.resetRoi();
        }
        SpineReconnection sr = new SpineReconnection( m_imp.getProcessor().duplicate(), result, m_rois );
        return sr;
    }
    
    public ByteProcessor getSegmentation(){
        ByteProcessor result = new ByteProcessor( m_imp.getWidth(), m_imp.getHeight() );
        result.setColor( 255 );
        result.fill();
        result.setColor( 0 );
        for( int n = 0; n < m_ddrois.size(); n++ ){
            result.setRoi( m_ddrois.get( n ) );
            result.fill( m_ddrois.get( n ).getMaskWithHoles() );
            result.resetRoi();
        }
        return result;
    }
    
    private void closeDialog(){
        imageAppo = generateImageSeg();
        m_rapport = imageInfos[1].getRapportNoise();
        getWclose = WindowEvent.WINDOW_CLOSED;
        try{
            iw.close();
        }
        catch( NullPointerException err ){
            System.out.println( "Debug image was already closed" );
        }
        dispose();
    }
    
    public void myClose(){
        getWclose = WindowEvent.WINDOW_CLOSED;
        try{
            iw.close();
        }
        catch( NullPointerException err ){
            System.out.println( "Debug image was already closed" );
        }
        dispose();
    }

    protected ShortProcessor generateImageSeg(){
        ArrayList < ByteProcessor > imagesAdded = new ArrayList < ByteProcessor >(), imagesRemoved = new ArrayList < ByteProcessor >();
        for( int j = 0; j < wavelet.getNbWavelets(); j++ ){
            boolean added = infosPanels[j].resultAdded(), removed = infosPanels[j].resultRemoved();
            if( added || removed ){
                imageInfos[j].setProcessor( wavelet.getResult( j ) );
                filter( j );
                ByteProcessor bp = new ByteProcessor( m_imp.getWidth(), m_imp.getHeight() );
                bp.setColor( 0 );
                bp.fill();
                ImageProcessor ip = imageResults[j].getProcessor();
                for( int k = 0; k < ip.getWidth(); k++ )
                    for( int l = 0; l < ip.getHeight(); l++ )
                        if( ip.get( k, l ) > 0 )
                            bp.set( k, l, 1 );
                if( added )
                    imagesAdded.add( bp );
                else
                    imagesRemoved.add( bp );
            }
        }
        ShortProcessor sp = new ShortProcessor( m_imp.getWidth(), m_imp.getHeight() );
        sp.setColor( 0 );
        sp.fill();
        for( int j = 0; j < imagesAdded.size(); j++ ){
            ByteProcessor bp = imagesAdded.get( j );
            for( int x = 0; x < bp.getWidth(); x++ )
                for( int y = 0; y < bp.getHeight(); y++ )
                    if( bp.get( x, y ) == 1 )
                        sp.set( x, y, 1 );
        }
        for( int j = 0; j < imagesRemoved.size(); j++ ){
            ByteProcessor bp = imagesRemoved.get( j );
            for( int x = 0; x < bp.getWidth(); x++ )
                for( int y = 0; y < bp.getHeight(); y++ )
                    if( bp.get( x, y ) == 1 )
                        sp.set( x, y, 0 );
        }
        return sp;
    }

    public void windowClosing(WindowEvent w) {
        if (w.getSource()==iw) {
            m_boundsDebugImage = iw.getBounds();
            iw = null;
            m_debugImageButton.setSelected(false);
        }
        if (w.getSource()==this) {
            imageAppo = generateImageSeg();
            m_rapport = imageInfos[1].getRapportNoise();
            getWclose = WindowEvent.WINDOW_CLOSED;
            try{
                iw.close();
            }
            catch( NullPointerException err ){
                System.out.println( "Debug image was already closed" );
            }
            dispose();
        }
    }

    public void windowOpened(WindowEvent e) {}

    public void windowClosed(WindowEvent e) {}

    public void windowIconified(WindowEvent e) {}

    public void windowDeiconified(WindowEvent e) {}

    public void windowActivated(WindowEvent e) {}

    public void windowDeactivated(WindowEvent e) {}

    protected void generateDebugImage(){
        double size = 1.;
        try{
            size = Double.valueOf( m_sizeField.getText() );
        }
        catch( Exception ex ){
            size = 1.;
        }
        ByteProcessor bp = new ByteProcessor( m_imp.getWidth(), m_imp.getHeight() );
        ShortProcessor sp = generateImageSeg();
        for( int i = 0; i < sp.getWidth(); i++ )
                for( int j = 0; j < sp.getHeight(); j++ )
                    if( sp.get( i, j ) > 0 )
                        bp.set( i, j, 255 );
                    else
                        bp.set( i, j, 0 );
        bp.invert(); //uncomment for Mac user
        ByteProcessor binaryObjects = ( ByteProcessor )bp.duplicate();
        IJ.run("ROI Manager...");
        Frame frame = WindowManager.getFrame("ROI Manager");
        RoiManager roiManager = (RoiManager) frame;
        roiManager.setVisible( false );
        int options = ParticleAnalyzer.ADD_TO_MANAGER + ParticleAnalyzer.CLEAR_WORKSHEET;// -> 64+2048 -> CLEAR_WORKSHEET, ADD_TO_MANAGER | 2053 with the 4 of the OUTLINES
        int measurements = Measurements.AREA;
        ParticleAnalyzer pa = new ParticleAnalyzer( options, measurements, null, size, Double.POSITIVE_INFINITY, 0., Double.POSITIVE_INFINITY );
        ImagePlus useless2 = new ImagePlus("Useless", bp);
        pa.analyze(useless2);
        m_rois = roiManager.getRoisAsArray();
        roiManager.runCommand( "Delete" );
        roiManager.close();
        m_rois = determineHoles( m_rois, binaryObjects );
        ic.setRois( m_rois );
        ic.repaint();
        
    }
    
    protected Roi [] determineHoles( Roi [] _rois, ByteProcessor _bp ){
        m_ddrois.clear();
        int nbHoles = 0;
        for( int n = 0; n < _rois.length; n++ ){
            m_ddrois.add( new DDRoiSpineAnalysis( _rois[n], _bp ) );
            nbHoles += m_ddrois.get( n ).nbHoles();
        }
        int cpt = 0;
        Roi [] rois = new Roi[_rois.length + nbHoles];
        for (Roi _roi : _rois) {
            rois[cpt++] = _roi;
        }
        for (DDRoiSpineAnalysis m_ddroi : m_ddrois) {
            if (m_ddroi.nbHoles() == 0) {
                continue;
            }
            Roi[] holes = m_ddroi.getHoles();
            for (Roi hole : holes) {
                rois[cpt++] = hole;
            }
        }
        return rois;
    }
    
    public ArrayList < DDRoiSpineAnalysis > getHoleROIs(){
        return m_ddrois;
    }
}
