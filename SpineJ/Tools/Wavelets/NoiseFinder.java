/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Tools.Wavelets;

import ij.IJ;
import ij.process.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Florian Levet
 */
public class NoiseFinder {
    double m_noiseWavelets[];
    boolean m_debug = false;
    static double m_rapports[] = {0.8868, 0.2046, 0.0862, 0.0598};
    static final public int NOISE_RACINE = 1;
    static final public int NOISE_UNIVERSAL = 0;
    static final public int NOISE_NEIGHBORHOOD = 2;

    public NoiseFinder( boolean _debug, int _typeNoise, float _sigmaCut, FloatProcessor[] allResults ){
        m_debug = _debug;
        switch( _typeNoise ){
            case NOISE_RACINE:
            {
                determineNoiseRacine( allResults, _sigmaCut );
                break;
            }
            case NOISE_UNIVERSAL:
            {
                determineNoiseUniversal( allResults );
                break;
            }
        }
    }

    public double[] getAllNoiseWavelets(){
        return m_noiseWavelets;
    }
    public double getNoiseForWavelet(int index){
        return m_noiseWavelets[index];
    }

    private void determineNoiseUniversal( FloatProcessor[] allResults ){
        //IJ.log( "Determination of universal noise" );
        ImageProcessor ipTmp = allResults[0];
        ArrayList<Float> data = new ArrayList<Float>();
        for (int j = 0; j < ipTmp.getWidth(); j++) {
            for (int k = 0; k < ipTmp.getHeight(); k++) {
                float tmp = ipTmp.getPixelValue(j, k);
                data.add(new Float(Math.abs(tmp)));
            }
        }
        Collections.sort(data);
        Float med1 = data.get( ( allResults[0].getWidth() * allResults[0].getHeight() ) / 2 );
        m_noiseWavelets = new double[allResults.length];
        for(int k = 0; k < allResults.length; k++)
            m_noiseWavelets[k] = (med1.doubleValue() / 0.6745);
        if( m_debug )
            IJ.log( "Value for noise --> " + m_noiseWavelets[0] );
    }

    private void determineNoiseRacine( FloatProcessor[] allResults, float SigmaCut ){
        //IJ.log( "Determination of racine noise" );
        int w = allResults[0].getWidth(), h = allResults[0].getHeight();

        float mean = 0.f, cpt = (float)(w*h-1);
        for(int i = 0; i < w; i++)
            for(int j = 0; j < h; j++)
                mean += (allResults[0].getPixelValue(i, j)/(cpt+1));
        float deviations[] = new float[w*h];
        int current = 0;
        for(int i = 0; i < w; i++)
            for(int j = 0; j < h; j++)
                deviations[current++] = allResults[0].getPixelValue(i, j) - mean;
        float variance = 0.f;
        for(int i = 0; i < w*h; i++)
            variance += (deviations[i]*deviations[i])/cpt;
        double stdDeviation = Math.sqrt(variance);

        //IJ.log( "Before sigmaCut, mean = " + mean + ", variance = " + variance + ", std deviation = " + stdDeviation );

        float mean2 = 0.f;
        float cptDeviation = 0.f;
        for(int i = 0; i < w; i++)
            for(int j = 0; j < h; j++){
                float val = allResults[0].getPixelValue(i, j);
                if( Math.abs( val - mean ) < ( SigmaCut * stdDeviation ) )
                    cptDeviation++;
            }
        for(int i = 0; i < w; i++)
            for(int j = 0; j < h; j++){
                float val = allResults[0].getPixelValue(i, j);
                if( Math.abs( val - mean ) < ( SigmaCut * stdDeviation ) )
                    mean2 += ( val / cptDeviation);
            }
        float variance2 = 0.f;
        for(int i = 0; i < w; i++)
            for(int j = 0; j < h; j++){
                float val = allResults[0].getPixelValue(i, j);
                if( Math.abs( val - mean ) < ( SigmaCut * stdDeviation ) )
                    variance2 += (( (val-mean2)*(val-mean2) ) / cptDeviation);
            }
        double stdDeviation2 = Math.sqrt(variance2);
        //IJ.log( "After sigmaCut, mean = " + mean2 + ", variance = " + variance2 + ", std deviation = " + stdDeviation2 );
        double deviationImage = stdDeviation2 / m_rapports[0];

        double multiplicator = 1.;
        m_noiseWavelets = new double[allResults.length];
        for(int k = 0; k < allResults.length; k++){
            double deviationWavelet;
            if( k == 0 )
                deviationWavelet = stdDeviation2;
            else if( k < 4)
                deviationWavelet = m_rapports[k] * ( /*deviationImage / stdDeviation2*/ 1. / m_rapports[0] ) * stdDeviation2;
            else
                deviationWavelet = m_rapports[3] * ( /*deviationImage / stdDeviation2*/ 1. / m_rapports[0] ) * stdDeviation2;
            m_noiseWavelets[k] = multiplicator * deviationWavelet;
            //IJ.log("For level " + k + ", noise = " + m_noiseWavelets[k] );
        }
    }
}
