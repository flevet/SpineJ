/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Widgets;

import SpineJ.Analysis.NeuronObject;
import SpineJ.Analysis.NeuronSegmentor;
import SpineJ.Tools.GUI.Packer;
import ij.IJ;
import ij.WindowManager;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.frame.RoiManager;
import ij.text.TextWindow;
import java.awt.ComponentOrientation;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;

/**
 *
 * @author Florian Levet
 */
public class NeuronDisplayDialog extends Frame implements ActionListener {
    protected NeuronSegmentor m_dss = null;
    protected ImageCanvasNeuron m_canvas = null;

    protected JRadioButton originalRadio, binarizedRadio, outlineRadio, graphRadio, trianglesRadio, skeletonRadio, m_currentAnalyzedSpine, m_spinesAnalyzed, m_selectionSpine, m_selectionNeckline, m_definitionNeckEnd, m_deleteSpine = null;
    protected JRadioButton m_displaySpineHead, m_displaySpineNeck, m_displayNeckline, m_displayNecklineShape, m_displayNeckSkeleton, m_displaySkeletonLongest, m_displayHeadEllApprox, m_displaySpineLabel, m_displayDiscreteRoi;
    protected JRadioButton m_displayNeckLineUsedForFit = null, m_displayNecklineWidthFromFit = null;
    protected JButton m_analyzeSpineButton, m_exportShapeSpines, m_flattenButton = null, m_deleteCurrentSpineButton = null, m_exportSpinesTxt = null, m_exportAllIntensityProfiles = null, m_automaticIdentificationSpines = null, m_loadDataSpines = null;
    protected JPanel m_spinePanel;
    protected TextWindow twindow = null;
    protected Calibration m_cal = null;
    protected JTextField m_fromLineWTF = null, m_toLineWTF = null, m_lineHTF = null, m_lineStepWTF = null, m_initialCTF = null, m_initialDTF = null, m_distanceMergeNodesTF = null, m_minFitGoodnessTF = null;
    
    public NeuronDisplayDialog( NeuronSegmentor _dss, ImageCanvasNeuron _canvas, Calibration _cal ){
        m_dss = _dss;
        m_canvas = _canvas;
        m_cal = _cal;

        ButtonGroup imageGroup = new ButtonGroup();
        JPanel imagePanel = new JPanel();
        imagePanel.applyComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
        imagePanel.setBorder( BorderFactory.createTitledBorder( "Display Image" ) );
        Packer pkImage = new Packer( imagePanel );
        originalRadio = new JRadioButton();
        imageGroup.add( originalRadio );
        originalRadio.setText( "Original" );
        originalRadio.setSelected( true );
        binarizedRadio = new JRadioButton();
        imageGroup.add( binarizedRadio );
        binarizedRadio.setText( "Binarized" );
        m_flattenButton = new JButton( "Flatten" );
        pkImage.pack( originalRadio ).gridx( 0 ).gridy( 0 ).fillx().inset( 3, 3, 3, 3 );
        pkImage.pack( binarizedRadio ).gridx( 0 ).gridy( 1 ).fillx().inset( 3, 3, 3, 3 );
        pkImage.pack( m_flattenButton ).gridx( 0 ).gridy( 2 ).fillx().inset( 3, 3, 3, 3 );

        JPanel displayPanel = new JPanel();
        displayPanel.applyComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
        displayPanel.setBorder( BorderFactory.createTitledBorder( "Display Segmentation" ) );
        Packer pkDisplay = new Packer( displayPanel );
        outlineRadio = new JRadioButton();
        outlineRadio.setSelected( true );
        outlineRadio.setText( "Outline" );
        graphRadio = new JRadioButton();
        graphRadio.setSelected( false );
        graphRadio.setText( "Graph" );
        trianglesRadio = new JRadioButton();
        trianglesRadio.setText( "Triangles" );
        skeletonRadio = new JRadioButton();
        skeletonRadio.setText( "Skeleton" );
        m_currentAnalyzedSpine = new JRadioButton();
        m_currentAnalyzedSpine.setText( "Current Analyzed Spine" );
        m_currentAnalyzedSpine.setEnabled( false );
        m_currentAnalyzedSpine.setSelected( true );
        m_spinesAnalyzed = new JRadioButton();
        m_spinesAnalyzed.setText( "Analyzed Spines" );
        m_spinesAnalyzed.setSelected( true );
        m_spinesAnalyzed.setEnabled( true );

        int y = 0;
        pkDisplay.pack( outlineRadio ).gridx( 0 ).gridy( y++ ).fillx().inset( 3, 3, 3, 3 );
        pkDisplay.pack( graphRadio ).gridx( 0 ).gridy( y++ ).fillx().inset( 3, 3, 3, 3 );
        pkDisplay.pack( trianglesRadio ).gridx( 0 ).gridy( y++ ).fillx().inset( 3, 3, 3, 3 );
        pkDisplay.pack( skeletonRadio ).gridx( 0 ).gridy( y++ ).fillx().inset( 3, 3, 3, 3 );
        pkDisplay.pack( m_spinesAnalyzed ).gridx( 0 ).gridy( y++ ).fillx().inset( 3, 3, 3, 3 );
        pkDisplay.pack( m_currentAnalyzedSpine ).gridx( 0 ).gridy( y++ ).fillx().inset( 3, 3, 3, 3 );
        
        JPanel selectionPanel = new JPanel();
        selectionPanel.applyComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
        selectionPanel.setBorder( BorderFactory.createTitledBorder( "Selection Type" ) );
        Packer pkSelection = new Packer( selectionPanel );
        m_selectionSpine = new JRadioButton();
        m_selectionSpine.setSelected( false );
        m_selectionSpine.setText( "Select Spine" );
        m_selectionNeckline = new JRadioButton();
        m_selectionNeckline.setSelected( false );
        m_selectionNeckline.setText( "Select Neckline" );
        m_definitionNeckEnd = new JRadioButton();
        m_definitionNeckEnd.setSelected( false );
        m_definitionNeckEnd.setText( "Define neck end" );
        m_deleteSpine = new JRadioButton();
        m_deleteSpine.setSelected( false );
        m_deleteSpine.setText( "Delete spine" );
        y = 0;
        pkSelection.pack( m_selectionSpine ).gridx( 0 ).gridy( y++ ).fillx().inset( 3, 3, 3, 3 );
        pkSelection.pack( m_selectionNeckline ).gridx( 0 ).gridy( y++ ).fillx().inset( 3, 3, 3, 3 );
        pkSelection.pack( m_definitionNeckEnd ).gridx( 0 ).gridy( y++ ).fillx().inset( 3, 3, 3, 3 );
        pkSelection.pack( m_deleteSpine ).gridx( 0 ).gridy( y++ ).fillx().inset( 3, 3, 3, 3 );
        
        m_spinePanel = new JPanel();
        m_spinePanel.applyComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
        m_spinePanel.setBorder( BorderFactory.createTitledBorder( "Spine" ) );
        Packer pkActin = new Packer( m_spinePanel );
        m_analyzeSpineButton = new JButton();
        m_analyzeSpineButton.setText( "Analyze Spine" );
        m_exportShapeSpines = new JButton();
        m_exportShapeSpines.setText( "Export spine shapes/results" );
        m_deleteCurrentSpineButton = new JButton( "Delete Current Spine" );
        m_exportSpinesTxt = new JButton( "Export heads to RoiManager" );
        m_exportAllIntensityProfiles = new JButton( "Export all profiles" );
        m_loadDataSpines = new JButton( "Load data spines" );
        pkActin.pack( m_analyzeSpineButton ).gridx( 0 ).gridy( 0 ).remainx().fillx().inset( 3, 3, 3, 3 );
        pkActin.pack( m_exportShapeSpines ).gridx( 0 ).gridy( 1 ).remainx().fillx().inset( 3, 3, 3, 3 );
        pkActin.pack( m_deleteCurrentSpineButton ).gridx( 0 ).gridy( 2 ).remainx().fillx().inset( 3, 3, 3, 3 );
        pkActin.pack( m_exportSpinesTxt ).gridx( 0 ).gridy( 3 ).remainx().fillx().inset( 3, 3, 3, 3 );
        pkActin.pack( m_exportAllIntensityProfiles ).gridx( 0 ).gridy( 4 ).gridw( 2 ).remainx().fillx().inset( 3, 3, 3, 3 );
        //pkActin.pack( m_loadDataSpines ).gridx( 0 ).gridy( 5 ).gridw( 2 ).remainx().fillx().inset( 3, 3, 3, 3 );
        m_analyzeSpineButton.setEnabled( true );
        m_exportShapeSpines.setEnabled( true );
        m_loadDataSpines.setEnabled(true);
        m_exportSpinesTxt.setEnabled( true );
        
        JPanel actinDisplayPanel = new JPanel();
        actinDisplayPanel.applyComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
        actinDisplayPanel.setBorder( BorderFactory.createTitledBorder( "Display Spine" ) );
        Packer pkActinDisplay = new Packer( actinDisplayPanel );
        m_displaySpineHead = new JRadioButton();
        m_displaySpineHead.setSelected( true );
        m_displaySpineHead.setText( "Spine Head" );
        m_displaySpineNeck = new JRadioButton();
        m_displaySpineNeck.setSelected( true );
        m_displaySpineNeck.setText( "Spine Neck" );
        m_displayNeckline = new JRadioButton();
        m_displayNeckline.setSelected( true );
        m_displayNeckline.setText( "Neckline for Analysis" );
        m_displayNecklineShape = new JRadioButton();
        m_displayNecklineShape.setSelected( false );
        m_displayNecklineShape.setText( "Neckline Shape for Analysis" );
        m_displayNeckSkeleton = new JRadioButton();
        m_displayNeckSkeleton.setSelected( true );
        m_displayNeckSkeleton.setText( "Neck Skeleton" );
        m_displaySkeletonLongest = new JRadioButton();
        m_displaySkeletonLongest.setSelected( false );
        m_displaySkeletonLongest.setText( "Longest Skeleton" );
        m_displayHeadEllApprox = new JRadioButton();
        m_displayHeadEllApprox.setSelected( false );
        m_displayHeadEllApprox.setText( "Head Ellipsoid approx." );
        m_displaySpineLabel = new JRadioButton();
        m_displaySpineLabel.setSelected( false );
        m_displaySpineLabel.setText( "Spine Label" );
        m_displayDiscreteRoi = new JRadioButton();
        m_displayDiscreteRoi.setSelected( false );
        m_displayDiscreteRoi.setText( "Discrete ROI" );
        pkActinDisplay.pack( m_displaySpineHead ).gridx( 0 ).gridy( 0 ).fillx().inset( 3, 3, 3, 3 );
        pkActinDisplay.pack( m_displaySpineNeck ).gridx( 0 ).gridy( 1 ).fillx().inset( 3, 3, 3, 3 );
        pkActinDisplay.pack( m_displayNeckline ).gridx( 0 ).gridy( 2 ).fillx().inset( 3, 3, 3, 3 );
        pkActinDisplay.pack( m_displayNecklineShape ).gridx( 0 ).gridy( 3 ).fillx().inset( 3, 3, 3, 3 );
        pkActinDisplay.pack( m_displayNeckSkeleton ).gridx( 0 ).gridy( 4 ).fillx().inset( 3, 3, 3, 3 );
        pkActinDisplay.pack( m_displaySkeletonLongest ).gridx( 0 ).gridy( 5 ).fillx().inset( 3, 3, 3, 3 );
        pkActinDisplay.pack( m_displayHeadEllApprox ).gridx( 0 ).gridy( 6 ).fillx().inset( 3, 3, 3, 3 );
        pkActinDisplay.pack( m_displaySpineLabel ).gridx( 0 ).gridy( 7 ).fillx().inset( 3, 3, 3, 3 );
        pkActinDisplay.pack( m_displayDiscreteRoi ).gridx( 0 ).gridy( 8 ).fillx().inset( 3, 3, 3, 3 );
        
        boolean calibrationInUm = "µm".equals(m_cal.getXUnit()) || "um".equals(m_cal.getXUnit()) || "micron".equals(m_cal.getXUnit());
        JPanel parametersPanel = new JPanel();
        parametersPanel.applyComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
        parametersPanel.setBorder( BorderFactory.createTitledBorder( "Parameters" ) );
        Packer pkParameters = new Packer( parametersPanel );
        JLabel fromLineWLbl = new JLabel( "Width from (" + m_cal.getXUnit() + "):" );
        m_fromLineWTF = new JTextField( "" + (calibrationInUm ? 0.4 : 400));//( int )( 20 * m_cal.pixelWidth ) );
        JLabel toLineWLbl = new JLabel( "Width to (" + m_cal.getXUnit() + "):" );
        m_toLineWTF = new JTextField( "" + (calibrationInUm ? 0.8 : 1200));//( int )( 20 * m_cal.pixelWidth ) );
        JLabel stepLineWLbl = new JLabel( "Width step (" + m_cal.getXUnit() + "):" );
        m_lineStepWTF = new JTextField( "" + (calibrationInUm ? 0.05 : 25));//( int )( 20 * m_cal.pixelWidth ) );
        JLabel lineHLbl = new JLabel( "Height (" + m_cal.getXUnit() + "):" );
        m_lineHTF = new JTextField( "" + (calibrationInUm ? 0.1 : 50) );//( int )( 3 * m_cal.pixelWidth ) );
        JLabel minFitGoodnessLbl = new JLabel( "Min fit goodness:" );
        m_minFitGoodnessTF = new JTextField( "" + 0.8 );//( int )( 3 * m_cal.pixelWidth ) );
        pkParameters.pack( fromLineWLbl ).gridx( 0 ).gridy( 0 )./*remainx().fillx().*/inset( 3, 3, 3, 3 );
        pkParameters.pack( m_fromLineWTF ).gridx( 1 ).gridy( 0 ).remainx().fillx().inset( 3, 3, 3, 3 );
        pkParameters.pack( toLineWLbl ).gridx( 0 ).gridy( 1 )./*remainx().fillx().*/inset( 3, 3, 3, 3 );
        pkParameters.pack( m_toLineWTF ).gridx( 1 ).gridy( 1 ).remainx().fillx().inset( 3, 3, 3, 3 );
        pkParameters.pack( stepLineWLbl ).gridx( 0 ).gridy( 2 )./*remainx().fillx().*/inset( 3, 3, 3, 3 );
        pkParameters.pack( m_lineStepWTF ).gridx( 1 ).gridy( 2 ).remainx().fillx().inset( 3, 3, 3, 3 );
        pkParameters.pack( lineHLbl ).gridx( 0 ).gridy( 3 )./*remainx().fillx().*/inset( 3, 3, 3, 3 );
        pkParameters.pack( m_lineHTF ).gridx( 1 ).gridy( 3 ).remainx().fillx().inset( 3, 3, 3, 3 );
        pkParameters.pack( minFitGoodnessLbl ).gridx( 0 ).gridy( 4 )./*remainx().fillx().*/inset( 3, 3, 3, 3 );
        pkParameters.pack( m_minFitGoodnessTF ).gridx( 1 ).gridy( 4 ).remainx().fillx().inset( 3, 3, 3, 3 );
        
        ButtonGroup necklineGroup = new ButtonGroup();
        JPanel widthNecklinePanel = new JPanel();
        widthNecklinePanel.applyComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
        widthNecklinePanel.setBorder( BorderFactory.createTitledBorder( "Neckline width" ) );
        Packer pkWidthNeckline = new Packer( widthNecklinePanel );// = null,  = null;
        m_displayNeckLineUsedForFit = new JRadioButton();
        m_displayNeckLineUsedForFit.setSelected( true );
        m_displayNeckLineUsedForFit.setText( "Width used for fit" );
        necklineGroup.add(m_displayNeckLineUsedForFit);
        m_displayNecklineWidthFromFit = new JRadioButton();
        m_displayNecklineWidthFromFit.setSelected( false );
        m_displayNecklineWidthFromFit.setText( "Width computed from fit" );
        necklineGroup.add(m_displayNecklineWidthFromFit);
        pkWidthNeckline.pack( m_displayNeckLineUsedForFit ).gridx( 0 ).gridy( 0 ).fillx().inset( 3, 3, 3, 3 );
        pkWidthNeckline.pack( m_displayNecklineWidthFromFit ).gridx( 0 ).gridy( 1 ).fillx().inset( 3, 3, 3, 3 );
        
        JPanel emptyPanel = new JPanel(), emptyPanel2 = new JPanel(), emptyPanel3 = new JPanel();
        
        JPanel automaticPanel = new JPanel();
        automaticPanel.applyComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
        automaticPanel.setBorder( BorderFactory.createTitledBorder( "Automatic spine identification" ) );
        Packer pkAutomatic = new Packer( automaticPanel );
        JLabel distMergeWLbl = new JLabel( "Merging nodes dist (" + m_cal.getXUnit() + "):" );
        m_distanceMergeNodesTF = new JTextField( "" + ("nm".equals(m_cal.getXUnit()) ? 800 : 0.8));//( int )( 20 * m_cal.pixelWidth ) );
        m_automaticIdentificationSpines = new JButton( "Go" );
        pkAutomatic.pack( distMergeWLbl ).gridx( 0 ).gridy( 0 )./*remainx().fillx().*/inset( 3, 3, 3, 3 );
        pkAutomatic.pack( m_distanceMergeNodesTF ).gridx( 1 ).gridy( 0 ).remainx().fillx().inset( 3, 3, 3, 3 );
        pkAutomatic.pack( m_automaticIdentificationSpines ).gridx( 0 ).gridy( 1 ).remainx().fillx().inset( 3, 3, 3, 3 );
        
        m_initialCTF = new JTextField( "" + ( int )( 8 * m_cal.pixelWidth ) );
        m_initialDTF = new JTextField( "" + ( int )( 16 * m_cal.pixelWidth ) );

        originalRadio.addActionListener( this );
        binarizedRadio.addActionListener( this );
        m_flattenButton.addActionListener( this );
        m_deleteCurrentSpineButton.addActionListener( this );
        m_analyzeSpineButton.addActionListener( this );
        outlineRadio.addActionListener( this );
        graphRadio.addActionListener( this );
        trianglesRadio.addActionListener( this );
        skeletonRadio.addActionListener( this );
        m_currentAnalyzedSpine.addActionListener( this );
        m_spinesAnalyzed.addActionListener( this );
        m_displaySpineHead.addActionListener( this );
        m_displaySpineNeck.addActionListener( this );
        m_displaySpineLabel.addActionListener( this );
        m_displayDiscreteRoi.addActionListener( this );
        m_exportShapeSpines.addActionListener( this );
        m_loadDataSpines.addActionListener( this );
        m_exportSpinesTxt.addActionListener( this );
        m_selectionSpine.addActionListener( this );
        m_displayNeckline.addActionListener( this );
        m_displayNecklineShape.addActionListener( this );
        m_displayNeckSkeleton.addActionListener( this );
        m_displaySkeletonLongest.addActionListener( this );
        m_displayHeadEllApprox.addActionListener( this );
        m_selectionNeckline.addActionListener( this );
        m_definitionNeckEnd.addActionListener( this );
        m_deleteSpine.addActionListener( this );
        m_exportAllIntensityProfiles.addActionListener( this );
        m_automaticIdentificationSpines.addActionListener( this );
        m_displayNeckLineUsedForFit.addActionListener( this );
        m_displayNecklineWidthFromFit.addActionListener( this );
        
        m_canvas.setDisplayOutline( outlineRadio.isSelected() );
        m_canvas.setDisplayGraph( graphRadio.isSelected() );
        m_canvas.setDisplayTriangles( trianglesRadio.isSelected() );
        m_canvas.setDisplaySkeleton( skeletonRadio.isSelected() );
        m_canvas.setDisplayCurrentSpine( m_currentAnalyzedSpine.isSelected() );//
        m_canvas.setDisplayAnalyzedSpine( m_spinesAnalyzed.isSelected() );
        m_currentAnalyzedSpine.setEnabled( !m_spinesAnalyzed.isSelected() );
        m_canvas.setDisplaySpineHead( m_displaySpineHead.isSelected() );
        m_canvas.setDisplaySpineNeck( m_displaySpineNeck.isSelected() );
        m_canvas.setDisplayNecklines( m_displayNeckline.isSelected() );
        m_canvas.setDisplayNecklineShapes( m_displayNecklineShape.isSelected() );
        m_canvas.setDisplayNeckSkeleton( m_displayNeckSkeleton.isSelected() );
        m_canvas.setActinDisplaySkeletonLongest( m_displaySkeletonLongest.isSelected() );
        m_canvas.setDisplayHeadEllipsoidApprox( m_displayHeadEllApprox.isSelected() );
        m_canvas.setDisplaySpineLabel( m_displaySpineLabel.isSelected() );
        m_canvas.setDisplayDiscreteRoi( m_displayDiscreteRoi.isSelected() );

        JPanel panel = new JPanel();
        Packer pk = new Packer( panel );
        pk.pack( imagePanel ).gridx( 0 ).gridy( 0 ).fillboth().inset( 3, 3, 3, 3 );
        pk.pack( displayPanel ).gridx( 0 ).gridy( 1 ).fillboth().gridh( 3 ).inset( 3, 3, 3, 3 );
        pk.pack( selectionPanel ).gridx( 0 ).gridy( 4 ).fillboth().gridh( 2 ).inset( 3, 3, 3, 3 );
        pk.pack( m_spinePanel ).gridx( 1 ).gridy( 0 ).fillboth().gridh( 1 ).inset( 3, 3, 3, 3 );
        pk.pack( actinDisplayPanel ).gridx( 1 ).gridy( 1 ).fillboth().gridh( 5 ).inset( 3, 3, 3, 3 );
        pk.pack( parametersPanel ).gridx( 2 ).gridy( 0 ).fillboth()/*.gridh( 2 )*/.inset( 3, 3, 3, 3 );
        pk.pack( widthNecklinePanel ).gridx( 2 ).gridy( 1 ).fillboth()/*.gridh( 2 )*/.inset( 3, 3, 3, 3 );
        pk.pack( automaticPanel ).gridx( 2 ).gridy( 2 ).fillboth()/*.gridh( 2 )*/.inset( 3, 3, 3, 3 );
        pk.pack( emptyPanel ).gridx( 2 ).gridy( 3 ).fillboth()/*.gridh( 2 )*/.inset( 3, 3, 3, 3 );
        pk.pack( emptyPanel2 ).gridx( 2 ).gridy( 4 ).fillboth()/*.gridh( 2 )*/.inset( 3, 3, 3, 3 );
        pk.pack( emptyPanel3 ).gridx( 2 ).gridy( 5 ).fillboth()/*.gridh( 2 )*/.inset( 3, 3, 3, 3 );
        this.add( panel );
        this.setSize( 700, 550 );
        this.setTitle( "Interaction Dialog" );
        this.setVisible(true);
    }

    public void actionPerformed( ActionEvent e ) {
        Object o = e.getSource();
        if( o instanceof JRadioButton ){
            if( o == originalRadio )
                m_canvas.setOriginalImage();
            else if(o == binarizedRadio)
                m_canvas.setBinarizedImage();
            else if( o == outlineRadio )
                m_canvas.setDisplayOutline( outlineRadio.isSelected() );
            else if( o == graphRadio )
                m_canvas.setDisplayGraph( graphRadio.isSelected() );
            else if( o == trianglesRadio )
                m_canvas.setDisplayTriangles( trianglesRadio.isSelected() );
            else if( o == skeletonRadio )
                m_canvas.setDisplaySkeleton( skeletonRadio.isSelected() );
            else if( o == m_currentAnalyzedSpine )
                m_canvas.setDisplayCurrentSpine( m_currentAnalyzedSpine.isSelected() );//
            else if( o == m_spinesAnalyzed ){
                m_canvas.setDisplayAnalyzedSpine( m_spinesAnalyzed.isSelected() );
                m_currentAnalyzedSpine.setEnabled( !m_spinesAnalyzed.isSelected() );
            }
            else if( o == m_displaySpineHead )
                m_canvas.setDisplaySpineHead( m_displaySpineHead.isSelected() );
            else if( o == m_displaySpineNeck )
                m_canvas.setDisplaySpineNeck( m_displaySpineNeck.isSelected() );
            else if( o == m_displayNeckline )
                m_canvas.setDisplayNecklines( m_displayNeckline.isSelected() );
            else if( o == m_displayNecklineShape )
                m_canvas.setDisplayNecklineShapes( m_displayNecklineShape.isSelected() );
            else if( o == m_displayNeckSkeleton )
                m_canvas.setDisplayNeckSkeleton( m_displayNeckSkeleton.isSelected() );
            else if( o == m_displaySkeletonLongest )
                m_canvas.setActinDisplaySkeletonLongest( m_displaySkeletonLongest.isSelected() );
            else if( o == m_displayHeadEllApprox )
                m_canvas.setDisplayHeadEllipsoidApprox( m_displayHeadEllApprox.isSelected() );
            else if( o == m_displaySpineLabel )
                m_canvas.setDisplaySpineLabel( m_displaySpineLabel.isSelected() );
            else if( o == m_displayDiscreteRoi )
                m_canvas.setDisplayDiscreteRoi( m_displayDiscreteRoi.isSelected() );
            else if( o == m_selectionSpine ){
                m_canvas.setSelectionSpine( m_selectionSpine.isSelected() );
                if( m_selectionSpine.isSelected() ){
                    m_selectionNeckline.setSelected( false );
                    m_canvas.setSelectionNeckline( m_selectionNeckline.isSelected() );
                    m_definitionNeckEnd.setSelected( false );
                    m_canvas.setDefinitionNeckEnd( m_definitionNeckEnd.isSelected() );
                    m_deleteSpine.setSelected( false );
                    m_canvas.setDeleteSpine( m_deleteSpine.isSelected() );
                }
            }
            else if( o == m_selectionNeckline ){
                m_canvas.setSelectionNeckline( m_selectionNeckline.isSelected() );
                if( m_selectionNeckline.isSelected() ){
                    m_selectionSpine.setSelected( false );
                    m_canvas.setSelectionSpine( m_selectionSpine.isSelected() );
                    m_definitionNeckEnd.setSelected( false );
                    m_canvas.setDefinitionNeckEnd( m_definitionNeckEnd.isSelected() );
                    m_deleteSpine.setSelected( false );
                    m_canvas.setDeleteSpine( m_deleteSpine.isSelected() );
                }
            }
            else if( o == m_definitionNeckEnd ){
                m_canvas.setDefinitionNeckEnd( m_definitionNeckEnd.isSelected() );
                if( m_definitionNeckEnd.isSelected() ){
                    m_selectionSpine.setSelected( false );
                    m_canvas.setSelectionSpine( m_selectionSpine.isSelected() );
                    m_selectionNeckline.setSelected( false );
                    m_canvas.setSelectionNeckline( m_selectionNeckline.isSelected() );
                    m_deleteSpine.setSelected( false );
                    m_canvas.setDeleteSpine( m_deleteSpine.isSelected() );
                }
            }
            else if( o == m_deleteSpine ){
                m_canvas.setDeleteSpine( m_deleteSpine.isSelected() );
                if( m_definitionNeckEnd.isSelected() ){
                    m_selectionSpine.setSelected( false );
                    m_canvas.setSelectionSpine( m_selectionSpine.isSelected() );
                    m_selectionNeckline.setSelected( false );
                    m_canvas.setSelectionNeckline( m_selectionNeckline.isSelected() );
                    m_definitionNeckEnd.setSelected( false );
                    m_canvas.setDefinitionNeckEnd( m_definitionNeckEnd.isSelected() );
                }
            }
            else if(o == m_displayNeckLineUsedForFit)
                m_canvas.setNecklineUsedForFit( m_displayNeckLineUsedForFit.isSelected() );
            else if(o == m_displayNecklineWidthFromFit)
                m_canvas.setNecklineUsedForFit( !m_displayNecklineWidthFromFit.isSelected() );
            m_canvas.repaint();
        }
        else if(o instanceof JButton)
        {
            if( o == m_analyzeSpineButton ){
                m_canvas.analyseCurrentRoiForSpine();
            }
            else if( o == m_exportShapeSpines ){
                m_canvas.exportDataSpines();
            }
            else if( o == m_flattenButton ){
                m_canvas.myFlatten();
            }
            else if( o == m_deleteCurrentSpineButton )
                m_canvas.deleteCurrentSpine();
            else if( o == m_exportSpinesTxt ){
                ArrayList < NeuronObject > spines = m_canvas.getAnalyzedSpines();
                    if( spines == null || spines.isEmpty() ) return;
                IJ.run("ROI Manager...");
                Frame frame = WindowManager.getFrame("ROI Manager");
                RoiManager roiManager = (RoiManager) frame;
                roiManager.setVisible( true );
                if(roiManager.getCount() != 0)
                    roiManager.runCommand( "Delete" );
                int cptObject = 0;
                for( NeuronObject spine : spines ){
                    ArrayList < Point2D.Double > head = spine.getHead();
                    if(head == null || head.isEmpty()) continue;
                    int cpt = 0;
                    float[] xs = new float[head.size()], ys = new float[head.size()];
                    for( Point2D.Double p : head){
                        xs[cpt] = (float)p.x;
                        ys[cpt++] = (float)p.y;
                    }
                    Roi roi = new PolygonRoi(xs, ys, Roi.FREEROI);
                    roiManager.add(roi, cptObject++);
                }
            }
            else if( o == m_exportAllIntensityProfiles ){
                m_canvas.exportAllProfilesAllSpines();
            }
            else if(o == m_automaticIdentificationSpines){
                m_canvas.analyseAllSpines();
            }
        }
    }
    
    public double getFromLineWidth(){
        return Double.parseDouble( m_fromLineWTF.getText() );
    }
    public double getToLineWidth(){
        return Double.parseDouble( m_toLineWTF.getText() );
    }
    public double getStepLineWidth(){
        return Double.parseDouble( m_lineStepWTF.getText() );
    }
   public double getLineHeight(){
       return Double.parseDouble( m_lineHTF.getText() );
   }
   public double getInitialC(){
       return Double.parseDouble( m_initialCTF.getText() );
   }
   public int getInitialD(){
       return Integer.parseInt( m_initialDTF.getText() );
   }
   public double getDistanceMergeGraphNodes(){
       return Double.parseDouble( m_distanceMergeNodesTF.getText() );
   }
   public double getMinFitGoodness(){
       return Double.parseDouble( m_minFitGoodnessTF.getText() );
   }
}
