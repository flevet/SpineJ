/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Tools.Wavelets;

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
 *
 * @author Florian Levet
 */
public class ATrousWavelet2 extends Wavelet {
    FloatProcessor[] allWavelets = null;
    FloatProcessor[] allResults = null;

    @Override
    public FloatProcessor[] getWavelets() {
        return allWavelets;
    }

    @Override
    public FloatProcessor[] getResults() {
        return allResults;
    }

    @Override
    public FloatProcessor getWavelet( int _index ) {
        return allWavelets[_index];
    }

    @Override
    public FloatProcessor getResult( int _index ) {
        return allResults[_index];
    }

    public ATrousWavelet2( ImageProcessor _original ) {
        width = _original.getWidth();
        height = _original.getHeight();
        nbWavelets = determineWaveletLevel( width, height );
        allWavelets = new FloatProcessor[nbWavelets];
        allResults = new FloatProcessor[nbWavelets];
        initWavelets( nbWavelets, allWavelets, allResults, width, height );
        FloatProcessor fp = convertPoissonNoiseToGaussianNoise( _original, allWavelets );
        computeWavelets( nbWavelets, fp, allWavelets, allResults, width, height );
    }

    public ATrousWavelet2( ImageProcessor _original, int _nbWavelets ) {
        width = _original.getWidth();
        height = _original.getHeight();
        nbWavelets = _nbWavelets;
        allWavelets = new FloatProcessor[_nbWavelets];
        allResults = new FloatProcessor[_nbWavelets];
        initWavelets( _nbWavelets, allWavelets, allResults, width, height );
        FloatProcessor fp = convertPoissonNoiseToGaussianNoise( _original, allWavelets );
        computeWavelets( _nbWavelets, fp, allWavelets, allResults, width, height );
    }
}
