
import SpineJ.Widgets.ImageCanvasNeuron;
import SpineJ.Widgets.NeuronDisplayDialog;
import SpineJ.Analysis.NeuronSegmentor;
import SpineJ.Widgets.ReconnectionDialog;
import SpineJ.Reconnection.SpineReconnection;
import SpineJ.Filtering.WaveletDialogSpineAnalysis;
import ij.*;
import ij.gui.ImageWindow;
import ij.gui.StackWindow;
import ij.io.FileInfo;
import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JButton;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Florian Levet
 */
public class SpineJ_ implements PlugInFilter, ActionListener, WindowListener{
    ImagePlus m_imp = null;
    WaveletDialogSpineAnalysis m_wdsa = null;
    protected JButton m_ok = null, m_continue = null;
    protected SpineReconnection m_sr = null;
    protected ReconnectionDialog m_rd = null;
    protected Calibration m_cal = null;
    protected NeuronDisplayDialog m_sdd = null;

    public int setup( String string, ImagePlus ip ) {
        m_imp = ip;
        m_cal = m_imp.getCalibration();
        return DOES_ALL;
    }

    public void run( ImageProcessor ip ) {
        Prefs.blackBackground = false;
        Prefs.requireControlKey = true; 
        
        m_wdsa = new WaveletDialogSpineAnalysis( m_imp );
        m_ok = m_wdsa.getOkButton();
        m_ok.addActionListener( this );
    }

    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if( o == m_ok ){
            m_sr = m_wdsa.reconnectSpines();
            m_wdsa.myClose();
            m_rd = new ReconnectionDialog( m_sr.getCanvas() );
            m_continue = m_rd.getContinueButton();
            m_continue.addActionListener( this );
        }
        else if( o == m_continue ){
            m_rd.dispose();
            
            ImageProcessor ip = m_sr.getBinaryImage();
            if( !( ip instanceof ByteProcessor ) ) return;
            int sizeBordure = 0, size = 0;
            ByteProcessor bp = new ByteProcessor( ip.getWidth() + sizeBordure * 2, ip.getHeight() + sizeBordure * 2 );
            bp.setColor( Color.black );
            bp.fill();
            for( int x = 0, x2 = sizeBordure; x < ip.getWidth(); x++, x2++ )
                for( int y = 0, y2 = sizeBordure; y < ip.getHeight(); y++, y2++ )
                    bp.set( x2, y2, ip.get( x, y ) );

            boolean trimTriangles = true;
            NeuronSegmentor dss = new NeuronSegmentor( bp, size, trimTriangles, true );
            ImageCanvasNeuron ic = new ImageCanvasNeuron( m_imp, bp );
            FileInfo finfo = m_imp.getOriginalFileInfo();
            if( finfo != null )
                ic.setDirectoryOriginalImage( finfo.directory );
            ic.setSegmentor( dss );
            ImageWindow iw = null;
            if( m_imp instanceof CompositeImage ){
                CompositeImage ci = ( CompositeImage )m_imp;
                ci.setMode( CompositeImage.GRAYSCALE );
                iw = new StackWindow( m_imp, ic );
            }
            else
                iw = new ImageWindow( m_imp, ic );
            iw.addWindowListener( ic );
            iw.setVisible( true );
            
            m_imp.changes = false;
            m_sr.close();
            
            IJ.run("ROI Manager...");
            Frame frame = WindowManager.getFrame("ROI Manager");
            RoiManager roiManager = (RoiManager) frame;
            roiManager.setVisible( false );
            roiManager.runCommand( "Delete" );
            roiManager.close();

            if( m_sdd == null ){
                m_sdd = new NeuronDisplayDialog( dss, ic, m_cal );
                ic.setNeuronDisplayDialog( m_sdd );
                m_sdd.addWindowListener( this );
            }
            
            ic.analyseAllSpinesMainDendrite();
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        Object o = e.getSource();
        if( o == m_sdd ){
            m_sdd.dispose();
            m_sdd = null;
        }
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }
}
