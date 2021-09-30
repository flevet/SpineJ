/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Tools.Wavelets;

import ij.process.ImageProcessor;
import java.awt.image.BufferedImage;

/**
 *
 * @author Florian Levet
 */
public class ImageInfos {
    protected ImageProcessor m_ip = null;
    protected BufferedImage m_image = null;
    protected double m_mean = 0., m_noiseUniversal = 0., m_noiseRacine = 0., m_min = Double.MAX_VALUE, m_max = Double.MIN_VALUE, m_sigmaCut = 3., m_noiseRapport = 3., m_threshold = 1.;

    public ImageInfos( ImageProcessor _ip ){
        m_ip = _ip;
        m_image = m_ip.getBufferedImage();

        int w = _ip.getWidth(), h = _ip.getHeight();
        double size = w*h;
        for( int i = 0; i < w; i++ )
            for( int j = 0; j < h; j++ ){
                double val = _ip.getPixelValue( i, j );
                if( val < m_min )
                    m_min = val;
                if ( val > m_max )
                    m_max = val;
                m_mean += ( val / size );
            }
    }
    public void setNoises( double _noiseU, double _noiseR ){
        m_noiseUniversal = _noiseU;
        m_noiseRacine = _noiseR;
    }
    public BufferedImage getImage(){
        return m_image;
    }
    public ImageProcessor getProcessor(){
        return m_ip;
    }
    public double getRapportNoise(){
        return m_noiseRapport;
    }
    public double getNoiseUniversal(){
        return m_noiseUniversal;
    }
    public double getNoiseRacine(){
        return m_noiseRacine;
    }
    public double getNoise( int _typeNoise ){
        if (_typeNoise == NoiseFinder.NOISE_UNIVERSAL )
            return m_noiseUniversal;
        else
            return m_noiseRacine;
    }

    public double getMin(){
        return m_min;
    }
    public double getMax(){
        return m_max;
    }
    public double getMean(){
        return m_mean;
    }

    public void setRapportNoise( double _rapp ){
        m_noiseRapport = _rapp;
    }
    public void setProcessor( ImageProcessor _ip ){
        m_ip = _ip;
    }
}
