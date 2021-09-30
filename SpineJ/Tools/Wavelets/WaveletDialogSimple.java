/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Tools.Wavelets;

import SpineJ.Tools.GUI.Packer;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import java.awt.Choice;
import java.awt.ComponentOrientation;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.*;

/**
 *
 * @author Florian Levet
 */
public class WaveletDialogSimple extends Frame implements ActionListener, ItemListener {
    protected JRadioButton filteredRadio, filteredBinRadio;
    protected ImagePanel[] imagePanels = null;
    protected ImagePanel[] imageResPanels = null;
    protected ImageInfos[] imageInfos = null;
    protected ImageInfos[] imageResults = null;
    protected InfosPanel[] infosPanels = null;
    protected Wavelet wavelet;
    protected double[] noiseUniversal, noiseRacine = null;
    protected int typeNoise = NoiseFinder.NOISE_RACINE, firstPanel = 0, nbPanels = 4, sizeWavelets;
    protected JButton prevButton, nextButton;
    protected JButton waveletsButton, coeffsButton, filteredButton, reconstructedButton;
    protected ImagePlus m_imp = null;
    protected Packer packerElement1 = null;
    protected JPanel m_panelDisplay;
    protected Choice m_choice;

    public ShortProcessor imageAppo;
    public int getWclose;
    public double m_rapport;

    public WaveletDialogSimple(){

    }

    public WaveletDialogSimple( ImagePlus _imp ){
        this( _imp, -1 );
    }
    
    public WaveletDialogSimple( ImagePlus _imp, int _size, double[] _factors ){
        m_imp = _imp;
        wavelet = new ATrousWavelet2( _imp.getProcessor(), _size );
        sizeWavelets = _size;
        imageInfos = new ImageInfos[sizeWavelets];
        imageResults = new ImageInfos[sizeWavelets];
        infosPanels = new InfosPanel[sizeWavelets];
        for( int i = 0; i < sizeWavelets; i++ ){
            imageInfos[i] = new ImageInfos( wavelet.getResult( i ) );
            imageInfos[i].setRapportNoise(_factors[i]);
            infosPanels[i] = new InfosPanel( _factors[i] > 0. ? InfosPanel.ADDED : InfosPanel.NONE );
        }
        determineNoise();
        filter();
    }

    public WaveletDialogSimple( ImagePlus _imp, int _size ){
        m_imp = _imp;
        filteredRadio = new JRadioButton();
        filteredRadio.setSelected( false );
        filteredBinRadio = new JRadioButton();
        filteredBinRadio.setSelected( true );
        prevButton = new JButton();
        prevButton.setText( "<" );
        nextButton = new JButton();
        nextButton.setText( ">" );

        wavelet = new ATrousWavelet2( _imp.getProcessor() );
        if( _size == -1 )
            sizeWavelets = wavelet.getNbWavelets();
        else
            sizeWavelets = _size;
        imageInfos = new ImageInfos[sizeWavelets];
        imageResults = new ImageInfos[sizeWavelets];
        for( int i = 0; i < sizeWavelets; i++ )
            imageInfos[i] = new ImageInfos( wavelet.getResult( i ) );
        determineNoise();
        filter();

        imagePanels = new ImagePanel[sizeWavelets];
        imageResPanels = new ImagePanel[sizeWavelets];
        infosPanels = new InfosPanel[sizeWavelets];
        for( int i = 0; i < sizeWavelets; i++ ){
            imagePanels[i] = new ImagePanel();
            imageResPanels[i] = new ImagePanel();
            infosPanels[i] = new InfosPanel( imageInfos[i], i );
            infosPanels[i].addActionListener( this );
        }
        generatePanels( imagePanels, imageInfos );
        generatePanels( imageResPanels, imageResults );

        //JFrame frame = new JFrame();
        JPanel panel = new JPanel();
        Packer pk = new Packer( panel );
        JPanel p1 = constructElement1();
        m_panelDisplay = constructElement2();
        addListener();
        pk.pack( p1 ).gridx( 0 ).gridy( 0 ).filly().inset( 3, 3, 3, 3 );
        pk.pack( m_panelDisplay ).gridx( 1 ).gridy( 0 ).fillboth().inset( 3, 3, 3, 3 );
        this.add( panel );
        this.setSize( 1500, 1000 );
        this.setTitle( "Filtering " + m_imp.getTitle() );
        this.setVisible(true);

        displayPanels();
    }

    protected void addListener(){
        filteredRadio.addActionListener( this );
        filteredBinRadio.addActionListener( this );

        waveletsButton.addActionListener( this );
        coeffsButton.addActionListener( this );
        filteredButton.addActionListener( this );
        reconstructedButton.addActionListener( this );

        //appoButton.addActionListener( this );

        prevButton.addActionListener( this );
        nextButton.addActionListener( this );

        m_choice.addItemListener( this );
    }

    private void filter(){
        for( int i = 0; i < imageInfos.length; i++ ){
            filter( i );
        }
    }
    protected void filter( int i ){
        ImageInfos ii = imageInfos[i];
        ImageProcessor org = ii.getProcessor();
        float thresh = ( float )( determineThreshold( ii ) );
        if( filteredRadio.isSelected() ){
            FloatProcessor fp = new FloatProcessor( org.getWidth(), org.getHeight() );
            
            for( int x = 0; x < org.getWidth(); x++ )
                for( int y = 0; y < org.getHeight(); y++ ){
                    float val = org.getPixelValue( x, y );
                    val = performThreshold( val, thresh, org, x, y );
                    fp.putPixelValue( x, y, val );
                }
            imageResults[i] = new ImageInfos( fp );
        }
        else{
            ByteProcessor bp = new ByteProcessor( org.getWidth(), org.getHeight() );
            bp.setColor( 0 );
            bp.fill();
            for( int x = 0; x < org.getWidth(); x++ )
                for( int y = 0; y < org.getHeight(); y++ ){
                    float val = org.getPixelValue( x, y );
                    val = performThreshold( val, thresh, org, x, y );
                    if( val > 0 )
                        bp.set( x, y, 255 );
                }
            imageResults[i] = new ImageInfos( bp );
        }
    }

    private double determineThreshold( ImageInfos _ii ){
        return _ii.getRapportNoise() * _ii.getNoise( typeNoise );
    }

    protected float performThreshold( float val, float thresh, ImageProcessor _ip, int _x, int _y ){
        return ( ( val > thresh ) ? val : 0 );

    }

    protected void determineNoise(){
        float sigmaClip = 3.f;
        NoiseFinder nf = new NoiseFinder( false, NoiseFinder.NOISE_UNIVERSAL, 3.f, wavelet.getResults() );
        noiseUniversal = nf.getAllNoiseWavelets();
        nf = new NoiseFinder( false, NoiseFinder.NOISE_RACINE, sigmaClip, wavelet.getResults() );
        noiseRacine = nf.getAllNoiseWavelets();
        for( int i = 0; i < wavelet.getNbWavelets(); i++ )
            imageInfos[i].setNoises( noiseUniversal[i], noiseRacine[i] );
    }

    protected JPanel constructElement1(){
        ButtonGroup displayButtonGroup = new ButtonGroup();

        JPanel debugPanel = new JPanel();
        debugPanel.applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        debugPanel.setBorder(BorderFactory.createTitledBorder("Debug"));
        Packer pkDebug = new Packer( debugPanel );
        waveletsButton = new JButton();
        waveletsButton.setText( "Wavelets" );
        coeffsButton = new JButton();
        coeffsButton.setText( "Coefficients" );
        filteredButton = new JButton();
        filteredButton.setText( "Filtered" );
        reconstructedButton = new JButton();
        reconstructedButton.setText( "Reconstructed" );
        pkDebug.pack( waveletsButton ).gridx( 0 ).gridy( 0 ).inset( 3, 3, 3, 3 );
        pkDebug.pack( coeffsButton ).gridx( 1 ).gridy( 0 ).inset( 3, 3, 3, 3 );
        pkDebug.pack( filteredButton ).gridx( 0 ).gridy( 1 ).inset( 3, 3, 3, 3 );
        pkDebug.pack( reconstructedButton ).gridx( 1 ).gridy( 1 ).inset( 3, 3, 3, 3 );

        JPanel displayPanel = new JPanel();
        displayPanel.applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        displayPanel.setBorder(BorderFactory.createTitledBorder("Display"));
        Packer pkDisplay = new Packer( displayPanel );
        filteredRadio.setText( "Filtered" );
        displayButtonGroup.add( filteredRadio );
        filteredBinRadio.setText( "Filtered Bin" );
        displayButtonGroup.add( filteredBinRadio );
        m_choice = new Choice();
        for( int i = 0; i < sizeWavelets; i++ )
            m_choice.add( "" + ( i + 1 ) );
        m_choice.select( nbPanels - 1 );
        pkDisplay.pack( filteredRadio ).gridx( 0 ).gridy( 0 ).inset( 3, 3, 3, 3 );
        pkDisplay.pack( filteredBinRadio ).gridx( 1 ).gridy( 0 ).inset( 3, 3, 3, 3 );
        pkDisplay.pack( m_choice ).gridx( 2 ).gridy( 0 ).inset( 3, 3, 3, 3 );

        JPanel p = new JPanel();
        packerElement1 = new Packer( p );
        packerElement1.pack( debugPanel ).gridx( 0 ).gridy( 4 ).fillx().inset( 5, 5, 5, 5 );
        packerElement1.pack( displayPanel ).gridx( 0 ).gridy( 5 ).fillx().inset( 5, 5, 5, 5 );
        return p;
    }

    private JPanel constructElement2(){
        JPanel imagePanel = new JPanel();
        imagePanel.applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        imagePanel.setBorder(BorderFactory.createTitledBorder("Infos"));
        Packer pkImage = new Packer( imagePanel );
        pkImage.pack( prevButton ).gridx( 0 ).gridy( 0 ).filly().remainy().inset( 3, 3, 3, 3 );
        for( int i = 0, j = 1; i < sizeWavelets; i++, j++ )
            pkImage.pack( imagePanels[i] ).gridx( j ).gridy( 0 ).fillboth().inset( 3, 3, 3, 3 );
        for( int i = 0, j = 1; i < sizeWavelets; i++, j++ )
            pkImage.pack( infosPanels[i] ).gridx( j ).gridy( 1 ).fillx().inset( 3, 3, 3, 3 );
        for( int i = 0, j = 1; i < sizeWavelets; i++, j++ )
            pkImage.pack( imageResPanels[i] ).gridx( j ).gridy( 2 ).fillboth().inset( 3, 3, 3, 3 );
        pkImage.pack( nextButton ).gridx( sizeWavelets + 1 ).gridy( 0 ).filly().remainy().inset( 3, 3, 3, 3 );
        return imagePanel;
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        Object o = e.getSource();
        if( o instanceof JRadioButton ){
            if( o == filteredRadio || o == filteredBinRadio ){
                filter();
                generatePanels( imageResPanels, imageResults );
                for (ImagePanel imageResPanel : imageResPanels) {
                    imageResPanel.repaint();
                }
            }
        }
        else if( o instanceof JButton ){
            if( o == prevButton ){
                if( firstPanel > 0 ){
                    firstPanel--;
                    displayPanels();
                }
            }
            else if( o == nextButton ){
                if( firstPanel < ( sizeWavelets - nbPanels ) ){
                    firstPanel++;
                    displayPanels();
                }
            }
            else if( o == waveletsButton ){
                FloatProcessor[] waves = wavelet.getWavelets();
                ImageStack is = new ImageStack( m_imp.getWidth(), m_imp.getHeight() );
                for( int i = 0; i < waves.length; i++ ){
                    String title = "Wavelet " + ( i + 1 );
                    is.addSlice( title, waves[i] );
                }
                ImagePlus wavePlus = new ImagePlus( "Wavelets", is );
                wavePlus.show();
            }
            else if( o == coeffsButton ){
                FloatProcessor[] coeffs = wavelet.getResults();
                ImageStack is = new ImageStack( m_imp.getWidth(), m_imp.getHeight() );
                for( int i = 0; i < coeffs.length; i++ ){
                    String title = "Coefficient " + ( i + 1 );
                    is.addSlice( title, coeffs[i] );
                }
                ImagePlus coeffPlus = new ImagePlus( "Coefficients", is );
                coeffPlus.show();
            }
            else if( o == filteredButton ){
                ImageStack is = new ImageStack( m_imp.getWidth(), m_imp.getHeight() );
                for( int i = 0; i < imageResults.length; i++ ){
                    String title = "Filtered " + ( i + 1 );
                    is.addSlice( title, imageResults[i].getProcessor() );
                }
                ImagePlus filtPlus = new ImagePlus( "Filtereds", is );
                filtPlus.show();
            }
            else if( o == reconstructedButton ){
                if( filteredBinRadio.isSelected() ) return;
                int start = sizeWavelets - 1;
                FloatProcessor aBefore = wavelet.getWavelet( sizeWavelets - 1 );
                aBefore.setColor( 0 );
                aBefore.fill();
                FloatProcessor [] fps = new FloatProcessor[sizeWavelets];
                for(int i = start; i >= 0; i--) {
                    fps[i] = new FloatProcessor( m_imp.getWidth(), m_imp.getHeight() );
                    FloatProcessor denoise = ( FloatProcessor )imageResults[i].getProcessor();
                    for(int j = 0; j < fps[i].getWidth(); j++)
                        for(int k = 0; k < fps[i].getHeight(); k++)
                            fps[i].setf( j, k, ( denoise.getPixelValue( j, k ) + ( aBefore.getPixelValue( j, k ) / 3.f ) )  );
                    aBefore = fps[i];
                }
                ImageStack is = new ImageStack( m_imp.getWidth(), m_imp.getHeight() );
                for( int i = 0; i < fps.length; i++ ){
                    String title = "Reconstructed " + ( i + 1 );
                    is.addSlice( title, fps[i] );
                }
                ImagePlus reconsPlus = new ImagePlus( "Reconstructeds", is );
                reconsPlus.show();
            }
        }
        else if( o instanceof JTextField ){
            for( int i = 0; i < sizeWavelets; i++ ){
                JTextField field = infosPanels[i].getTextField();
                if( field == o ){
                    double rapport = 3.;
                    try{
                        rapport = Double.valueOf( field.getText() );
                    }
                    catch( Exception ex ){
                        rapport = 3.;
                    }
                    imageInfos[i].setRapportNoise( rapport );
                    filter( i );
                    imageResPanels[i].setImage( imageResults[i] );
                    imageResPanels[i].repaint();
                }
            }
        }
    }

    private void generatePanels( ImagePanel [] _panels, ImageInfos [] _iis ){
        for( int i = 0; i < sizeWavelets; i++ ){
            _panels[i].setImage( _iis[i] );
        }
    }

    private void displayPanels(){
        for( int i = 0; i < firstPanel; i++ ){
            imagePanels[i].setVisible( false );
            imageResPanels[i].setVisible( false );
            infosPanels[i].setVisible( false );
        }
        for( int i = firstPanel; i < firstPanel + nbPanels; i++ ){
            imagePanels[i].setVisible( true );
            imageResPanels[i].setVisible( true );
            infosPanels[i].setVisible( true );
        }
        for( int i = firstPanel + nbPanels; i < imagePanels.length; i++ ){
            imagePanels[i].setVisible( false );
            imageResPanels[i].setVisible( false );
            infosPanels[i].setVisible( false );
        }
        setSize( this.getWidth()+1, this.getHeight()+1 );
    }

    @Override
    public void itemStateChanged( ItemEvent e ) {
        String s = ( String )e.getItem();
        if( nbPanels != Integer.parseInt( s ) ){
            nbPanels = Integer.parseInt( s );
            displayPanels();
        }
    }
}