/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SpineJ.Widgets;

import SpineJ.Analysis.NeckShape;
import SpineJ.Analysis.NeuronObject;
import SpineJ.Tools.GUI.ImagePanel;
import SpineJ.Tools.GUI.Packer;
import SpineJ.Tools.Geometry.DoublePolygon;
import ij.gui.YesNoCancelDialog;
import ij.measure.Calibration;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.awt.Choice;
import java.awt.ComponentOrientation;
import java.awt.Frame;
import java.awt.Label;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

/**
 *
 * @author Florian Levet
 */
public class SpineAnalysisDialog extends Frame implements ItemListener, ActionListener{
    protected ArrayList < NeuronObject > m_spines = null;
    protected int m_currentSpine = 0, m_currentNeckline = 0;
    protected Choice m_lineChoice = null, m_spineChoice = null;
    protected ImagePanel m_plotProfilePanel = null, m_plotProfilePanel2 = null;
    protected Label m_lengthNeckLbl = null, m_lengthSpineLbl = null, m_rapportLengthLbl = null, m_minorHeadLbl = null, m_majorHeadLbl = null, m_aspectRatioHeadLbl = null, m_currentLineD = null, m_currentGoodnessFit = null;
    protected Label m_perimeterHead = null, m_areaHead = null, m_smallestWidthNeck = null, m_avgWidthNeck = null;
    protected JButton m_deleteNeckLine = null, m_exportIntensityProfileButton = null, m_nextButton = null;
    protected Calibration m_cal = null;
    protected String m_directory = null, m_generalName= null;
        
    public SpineAnalysisDialog( ArrayList < NeuronObject > _spines, Calibration _cal, ImageCanvasNeuron _canva ){
        m_spines = _spines;
        m_cal = _cal;
        m_directory = _canva.getDirectoryOriginalImage();
        m_generalName = _canva.getImage().getTitle();
        
        init( _canva );
    }
    
    private void init( ImageCanvasNeuron _canva ){
        int y = 0;
        
        JPanel spinePanel = new JPanel();
        spinePanel.applyComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
        spinePanel.setBorder( BorderFactory.createTitledBorder( "Spine" ) );
        Packer pkSpine = new Packer( spinePanel );
        m_spineChoice = new Choice();
        m_lineChoice = new Choice();
        m_nextButton = new JButton( "Next neckline" );
        m_deleteNeckLine = new JButton( "Delete neckline" );
        m_exportIntensityProfileButton = new JButton( "Export profile" );
        pkSpine.pack( m_spineChoice ).gridx( 0 ).gridy( y++ ).fillboth( 1, 1 ).inset( 3, 3, 3, 3 );
        pkSpine.pack( m_lineChoice ).gridx( 0 ).gridy( y++ ).fillboth( 1, 1 ).inset( 3, 3, 3, 3 );
        pkSpine.pack( m_nextButton ).gridx( 0 ).gridy( y++ ).fillboth( 1, 1 ).inset( 3, 3, 3, 3 );
        pkSpine.pack( m_deleteNeckLine ).gridx( 0 ).gridy( y++ ).fillboth( 1, 1 ).inset( 3, 3, 3, 3 );
        pkSpine.pack( m_exportIntensityProfileButton ).gridx( 0 ).gridy( y++ ).fillboth( 1, 1 ).inset( 3, 3, 3, 3 );
        
        JPanel lengthPanel = new JPanel();
        lengthPanel.applyComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
        lengthPanel.setBorder( BorderFactory.createTitledBorder( "Length" ) );
        Packer pkLength = new Packer( lengthPanel );
        m_lengthNeckLbl = new Label();
        m_lengthSpineLbl = new Label();
        m_rapportLengthLbl = new Label();
        y = 0;
        pkLength.pack( m_lengthNeckLbl ).gridx( 0 ).gridy( y++ ).fillboth( 1, 1 ).inset( 3, 3, 3, 3 );
        pkLength.pack( m_lengthSpineLbl ).gridx( 0 ).gridy( y++ ).fillboth( 1, 1 ).inset( 3, 3, 3, 3 );
        pkLength.pack( m_rapportLengthLbl ).gridx( 0 ).gridy( y++ ).fillboth( 1, 1 ).inset( 3, 3, 3, 3 );
        
        JPanel ellipsoidPanel = new JPanel();
        ellipsoidPanel.applyComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
        ellipsoidPanel.setBorder( BorderFactory.createTitledBorder( "Ellipsoid" ) );
        Packer pkEllipsoid = new Packer( ellipsoidPanel );
        m_minorHeadLbl = new Label();
        m_majorHeadLbl = new Label();
        m_aspectRatioHeadLbl = new Label();
        y = 0;
        pkEllipsoid.pack( m_minorHeadLbl ).gridx( 0 ).gridy( y++ ).fillboth( 1, 1 ).inset( 3, 3, 3, 3 );
        pkEllipsoid.pack( m_majorHeadLbl ).gridx( 0 ).gridy( y++ ).fillboth( 1, 1 ).inset( 3, 3, 3, 3 );
        pkEllipsoid.pack( m_aspectRatioHeadLbl ).gridx( 0 ).gridy( y++ ).fillboth( 1, 1 ).inset( 3, 3, 3, 3 );
        
        JPanel headPanel = new JPanel();
        headPanel.applyComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
        headPanel.setBorder( BorderFactory.createTitledBorder( "Head" ) );
        Packer pkHead = new Packer( headPanel );
        m_perimeterHead = new Label();
        m_areaHead = new Label();
        y = 0;
        pkHead.pack( m_perimeterHead ).gridx( 0 ).gridy( y++ ).fillboth( 1, 1 ).inset( 3, 3, 3, 3 );
        pkHead.pack( m_areaHead ).gridx( 0 ).gridy( y++ ).fillboth( 1, 1 ).inset( 3, 3, 3, 3 );
        
        JPanel neckwidthPanel = new JPanel();
        neckwidthPanel.applyComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
        neckwidthPanel.setBorder( BorderFactory.createTitledBorder( "Neckwidth" ) );
        Packer pkNeckwidth = new Packer( neckwidthPanel );
        m_smallestWidthNeck = new Label();
        m_avgWidthNeck = new Label();
        y = 0;
        pkNeckwidth.pack( m_smallestWidthNeck ).gridx( 0 ).gridy( y++ ).fillboth( 1, 1 ).inset( 3, 3, 3, 3 );
        pkNeckwidth.pack( m_avgWidthNeck ).gridx( 0 ).gridy( y++ ).fillboth( 1, 1 ).inset( 3, 3, 3, 3 );
        
        JPanel miscPanel = new JPanel();
        miscPanel.applyComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
        miscPanel.setBorder( BorderFactory.createTitledBorder( "Misc" ) );
        Packer pkMisc = new Packer( miscPanel );
        m_currentLineD = new Label();
        m_currentGoodnessFit = new Label();
        y = 0;
        pkMisc.pack( m_currentLineD ).gridx( 0 ).gridy( y++ ).fillboth( 1, 1 ).inset( 3, 3, 3, 3 );
        pkMisc.pack( m_currentGoodnessFit ).gridx( 0 ).gridy( y++ ).fillboth( 1, 1 ).inset( 3, 3, 3, 3 );
        
        JPanel profilePlotPanel = new JPanel();
        profilePlotPanel.applyComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
        profilePlotPanel.setBorder( BorderFactory.createTitledBorder( "Plots" ) );
        Packer pkProfilePlot = new Packer( profilePlotPanel );
        m_plotProfilePanel = new ImagePanel();
        m_plotProfilePanel2 = new ImagePanel();
        y = 0;
        pkProfilePlot.pack( m_plotProfilePanel ).gridx( 0 ).gridy( 0 ).gridh( 7 ).fillboth( 12, 7 ).remainx().inset( 3, 3, 3, 3 );
        pkProfilePlot.pack( m_plotProfilePanel2 ).gridx( 0 ).gridy( 7 ).gridh( 7 ).fillboth( 12, 7 ).remainx().remainy().inset( 3, 3, 3, 3 );
        
        defineElementsOfDialog();
        
        JPanel statisticsPanel = new JPanel();
        statisticsPanel.applyComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
        statisticsPanel.setBorder( BorderFactory.createTitledBorder( "Spine Statistics" ) );
        Packer pkStats = new Packer( statisticsPanel );
        y = 0;
        pkStats.pack( spinePanel ).gridx( 0 ).gridy( y++ ).fillboth( 1, 1 ).inset( 3, 3, 3, 3 );
        pkStats.pack( lengthPanel ).gridx( 0 ).gridy( y++ ).fillboth( 1, 1 ).inset( 3, 3, 3, 3 );
        pkStats.pack( ellipsoidPanel ).gridx( 0 ).gridy( y++ ).fillboth( 1, 1 ).inset( 3, 3, 3, 3 );
        pkStats.pack( headPanel ).gridx( 0 ).gridy( y++ ).fillboth( 1, 1 ).inset( 3, 3, 3, 3 );
        pkStats.pack( neckwidthPanel ).gridx( 0 ).gridy( y++ ).fillboth( 1, 1 ).inset( 3, 3, 3, 3 );
        pkStats.pack( miscPanel ).gridx( 0 ).gridy( y++ ).fillboth( 1, 1 ).inset( 3, 3, 3, 3 );
        pkStats.pack( profilePlotPanel ).gridx( 1 ).gridy( 0 ).gridh( 7 ).fillboth( 12, 7 ).inset( 3, 3, 3, 3 );
        
        JPanel panel = new JPanel();
        Packer pk = new Packer( panel );
        pk.pack( statisticsPanel ).gridx( 0 ).gridy( 0 ).gridw( 1 ).fillboth().inset( 3, 3, 3, 3 );
        this.add( panel );
        this.setSize( m_plotProfilePanel.getImage().getWidth() + 200, ( int )( m_plotProfilePanel.getImage().getHeight() * 2.15 ) + 50 );
        this.setTitle( "Spine Statistics Dialog" );
        this.setVisible(true);
        
        m_lineChoice.addItemListener( this );
        m_spineChoice.addItemListener( this );
        m_deleteNeckLine.addActionListener( this );
        m_exportIntensityProfileButton.addActionListener( this );
        m_nextButton.addActionListener( this );
        
        
        m_lineChoice.addItemListener( _canva );
        m_spineChoice.addItemListener( _canva );
        m_deleteNeckLine.addActionListener( _canva );
        m_nextButton.addActionListener( _canva );
    }
    
    public void changeCurrentSpine( int _current ){
        m_currentSpine = _current;
        m_currentNeckline = 0;
        
        defineElementsOfDialog();
        
        repaint();
    }
    
    public void changeCurrentSpineAndNeckline( int _currentSpine, int _currentNeckline ){
        m_currentSpine = _currentSpine;
        m_currentNeckline = _currentNeckline;
        
        defineElementsOfDialog();
        
        repaint();
    }
    
    private void defineElementsOfDialog(){
        DecimalFormat dfOneDigit = new DecimalFormat( "#.###" ), dfTwoDigits = new DecimalFormat( "#.###" );
        ArrayList < NeckShape > lines = m_spines.get( m_currentSpine ).getNeckAnalysis();
        double lengthInfos [] = m_spines.get( m_currentSpine ).getLengthInfos();
        double headApproxInfos [] = m_spines.get( m_currentSpine ).getParamsEllipsoid();
        
        double minD = Double.MAX_VALUE, avgD = 0.;
        m_spineChoice.removeAll();
        if( m_spines != null && !m_spines.isEmpty() ){
            for( int n = 0; n < m_spines.size(); n++ )
                m_spineChoice.add( " Spine " + ( n + 1 ) );
            m_spineChoice.select( m_currentSpine );
        }
        m_lineChoice.removeAll();
        if( lines != null && !lines.isEmpty() ){
            for( int n = 0; n < lines.size(); n++ ){
                m_lineChoice.add( " Neck line " + ( n + 1 ) );
                double d = lines.get( n ).getD();
                avgD += d;
                if( d < minD )
                    minD = d;
            }
            avgD /= ( double )lines.size();
            m_lineChoice.select( m_currentNeckline );
        }
        m_lengthNeckLbl.setText( "Neck: " + dfOneDigit.format( lengthInfos[1] * m_cal.pixelWidth ) + " " + m_cal.getXUnit() );
        m_lengthSpineLbl.setText( "Spine: " + dfOneDigit.format( lengthInfos[2] * m_cal.pixelWidth ) + " " + m_cal.getXUnit() );
        m_rapportLengthLbl.setText( "Ratio: " + dfTwoDigits.format( lengthInfos[0] * 100. ) + "%" );
        double dx = headApproxInfos[2] - headApproxInfos[0];
        double dy = headApproxInfos[3] - headApproxInfos[1];
        double major = Math.sqrt( dx*dx + dy*dy );
        double minor = major * headApproxInfos[4];
        m_minorHeadLbl.setText( "Minor axis: " + dfOneDigit.format( minor * m_cal.pixelWidth ) + " " + m_cal.getXUnit() );
        m_majorHeadLbl.setText( "Major axis: " + dfOneDigit.format( major * m_cal.pixelWidth ) + " " + m_cal.getXUnit() );
        m_aspectRatioHeadLbl.setText( "Aspect Ratio: " + dfTwoDigits.format( headApproxInfos[4] ) );// = null,  = null,  = null;
        
        ArrayList < Point2D.Double > head = m_spines.get( m_currentSpine ).getHead();
        if( head != null ){
            DoublePolygon tmp = new DoublePolygon( head );
            m_perimeterHead.setText( "Perimeter: " + dfOneDigit.format( tmp.getPerimeter() * m_cal.pixelWidth ) + " " + m_cal.getXUnit() );
            m_areaHead.setText( "Area: " + dfOneDigit.format( tmp.getArea() * ( m_cal.pixelWidth * m_cal.pixelWidth ) ) + " " + m_cal.getXUnit() + "2" );
        }
        else{
            m_perimeterHead.setText( "Perimeter: 0 " + m_cal.getXUnit() );
            m_areaHead.setText( "Area: 0 " + m_cal.getXUnit() );
        }
        
        if( minD != Double.MAX_VALUE )
            m_smallestWidthNeck.setText( "Smallest: " + dfOneDigit.format( minD ) + " " + m_cal.getXUnit() );
        else
            m_smallestWidthNeck.setText( "Smallest: 0 " + m_cal.getXUnit() );
        if( avgD != 0. )
            m_avgWidthNeck.setText( "Average: " + dfOneDigit.format( avgD ) + " " + m_cal.getXUnit() );
        else
            m_avgWidthNeck.setText( "Average: 0 " + m_cal.getXUnit() );
        
        changeProcessors();
    }
    
    private void changeProcessors(){
        ArrayList < NeckShape > lines = m_spines.get( m_currentSpine ).getNeckAnalysis();
        if( lines == null || lines.isEmpty() ){
            ByteProcessor bp = new ByteProcessor( 528, 255 );
            bp.setColor( 255 );
            bp.fill();
            
            BufferedImage img = bp.getBufferedImage();
            m_plotProfilePanel.changeImage( img );
            BufferedImage img2 = bp.getBufferedImage();
            m_plotProfilePanel2.changeImage( img2 );
            m_currentLineD.setText( "FWHM: 0 " + m_cal.getXUnit() );
            m_currentGoodnessFit.setText( "Fit goodness: 0" );
        }
        else{
            DecimalFormat dfOneDigit = new DecimalFormat( "#.###" ), dfTwoDigits = new DecimalFormat( "#.##" );
            NeckShape line = lines.get( m_currentNeckline );

            BufferedImage img = line.getProfileProcessor().getBufferedImage();
            m_plotProfilePanel.changeImage( img );

            ImageProcessor gaussFit = line.getGaussFitProcessor();
            BufferedImage img2 = null;
            if( gaussFit != null )
                img2 = gaussFit.getBufferedImage();
            else
                img2 = line.getProfileProcessor().getBufferedImage();
            m_plotProfilePanel2.changeImage( img2 );
            m_currentLineD.setText( "FWHM: " + dfOneDigit.format( line.getD() ) + " " + m_cal.getXUnit() );
            m_currentGoodnessFit.setText( "Fit goodness: " + dfTwoDigits.format( line.getGoodnessFit() ) );
        }
    }

    public void itemStateChanged( ItemEvent _e ) {
        Object obj = _e.getSource();
        if( obj == m_lineChoice ){
            m_currentNeckline = m_lineChoice.getSelectedIndex();
            changeProcessors();
            repaint();
        }
        else if( obj == m_spineChoice ){
            changeCurrentSpine( m_spineChoice.getSelectedIndex() );
            repaint();
        }
    }

    public void actionPerformed( ActionEvent e ) {
        Object o = e.getSource();
        if( o == m_deleteNeckLine ){
            if( m_spines.isEmpty() ) return;
            ArrayList < NeckShape > lines = m_spines.get( m_currentSpine ).getNeckAnalysis();
            if( lines != null && !lines.isEmpty() ){
                lines.remove( m_currentNeckline );
                if( m_currentNeckline >= lines.size() )
                    m_currentNeckline = lines.size() - 1;
                defineElementsOfDialog();
                repaint();
            }
        }
        else if( o == m_exportIntensityProfileButton ){
            if( m_spines.isEmpty() ) return;
            ArrayList < NeckShape > lines = m_spines.get( m_currentSpine ).getNeckAnalysis();
            if( lines.isEmpty() ) return;
            NeckShape ns = lines.get( m_currentNeckline );
            double [] intensityProfile = ns.getIntensityProfile();          
            double [] xs = new double[intensityProfile.length];
            for( int n = 0; n < intensityProfile.length; n++ )
                xs[n] = n * m_cal.pixelWidth;
            String name = m_generalName;
            name = name.substring( 0, name.lastIndexOf( "." ) );
            name = name + "-spine_" + ( m_currentSpine + 1 ) + "-neckline_" + ( m_currentNeckline + 1 ) + ".txt";

            if( m_directory == null ){
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
                int returnVal = chooser.showOpenDialog( this );
                if( returnVal == JFileChooser.APPROVE_OPTION ){
                    File file = chooser.getSelectedFile();
                    m_directory = file.getName();
                }
                else
                    return;
            }
            boolean overwrite = false;
            if( new File( m_directory + name ).exists() ){
                YesNoCancelDialog dialog = new YesNoCancelDialog( this, "Overwrite result file ?", "Do you want to overwrite " + name );
                if( dialog.yesPressed() )
                    overwrite = true;
                else if( dialog.cancelPressed() )
                    overwrite = false;
            }
            else
                overwrite = true;                
            if( overwrite ){
                Writer writer = null;
                    try {
                        String separator = System.getProperty("line.separator");
                        writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( m_directory + name ), "utf-8" ) );
                        for( int n = 0; n < intensityProfile.length; n++ )
                            writer.write( "" + xs[n] + " " + intensityProfile[n] + separator );
                    } catch ( IOException ex ){
                    // report
                    } finally {
                        try { writer.close(); } catch (Exception ex) {}
                    }
            }
        }
        else if( o == m_nextButton ){
            int index = m_lineChoice.getSelectedIndex();
            if( index != m_lineChoice.getItemCount() - 1 ){
                m_lineChoice.select( index + 1 );
                m_currentNeckline = m_lineChoice.getSelectedIndex();
                changeProcessors();
                repaint();
            }
        }
    }
    
    public int getCurrentSpine(){
        return m_currentSpine;
    }
    public void setCurrentSpine( int _index ){
        m_currentSpine = _index;
    }
    public int getCurrentNeckline(){
        return m_currentNeckline;
    }
    public void setCurrentNeckline( int _index ){
        m_currentNeckline = _index;
    }
    
}
