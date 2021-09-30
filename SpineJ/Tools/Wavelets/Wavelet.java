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
abstract public class Wavelet {
    protected int nbWavelets, width, height;
    
    abstract public FloatProcessor[] getWavelets();
    abstract public FloatProcessor[] getResults();
    abstract public FloatProcessor getWavelet( int _index );
    abstract public FloatProcessor getResult( int _index );

    protected int determineWaveletLevel( int _width, int _height ) {
        if ( _width < _height ) {
            return findMaxLevelWavelet( _width );
        } else {
            return findMaxLevelWavelet( _height );
        }
    }

    protected int findMaxLevelWavelet( int _l ) {
        int n = 1;
        int count = 2;
        if ( _l < count ) {
            return -1;
        }
        while ( _l > count ) {
            count *= 2;
            n++;
        }
        return n - 1;
    }

    protected void initWavelets(int nbWavelets, FloatProcessor[] allWavelets, FloatProcessor[] allResults, int width, int height) {
        for (int i = 0; i < nbWavelets; i++) {
            allWavelets[i] = new FloatProcessor(width, height);
            allResults[i] = new FloatProcessor(width, height);
        }
    }

    protected FloatProcessor convertPoissonNoiseToGaussianNoise(ImageProcessor ip, FloatProcessor[] allWavelets) {
        FloatProcessor tmp = new FloatProcessor(ip.getWidth(), ip.getHeight());
        for (int i = 0; i < ip.getWidth(); i++) {
            for (int j = 0; j < ip.getHeight(); j++) {
                Double d = new Double((double)ip.getPixelValue(i,j));
                tmp.setf(i, j, d.floatValue());
                allWavelets[0].setf(i, j, d.floatValue());
            }
        }
        return tmp;
    }

    protected void computeWavelets(int nbWavelets, FloatProcessor tmp, FloatProcessor[] allWavelets, FloatProcessor[] allResults, int width, int height) {
        int[] neighbors = {-2, -1, 0, 1, 2};
        for (int i = 0; i < nbWavelets; i++) {
            allWavelets[i] = applyKernel( tmp, neighbors );

            for (int j = 0; j < neighbors.length; j++) {
                neighbors[j] = neighbors[j] * 2;
            }

            for (int k = 0; k < width; k++) {
                for (int j = 0; j < height; j++) {
                    float sub = tmp.getPixelValue(k, j) - allWavelets[i].getPixelValue(k, j);
                    allResults[i].setf(k, j, sub);
                }
            }

            if ((i + 1) < nbWavelets) {
                for (int k = 0; k < width; k++) {
                    for (int j = 0; j < height; j++) {
                        allWavelets[i + 1].setf(k, j, allWavelets[i].getPixelValue(k, j));
                    }
                }
            }
            tmp = allWavelets[i];
        }
    }

    protected FloatProcessor applyKernel( FloatProcessor ip, int[] neighbors ) {
        double[] kernel = {1./16., 1./4., 3./8., 1./4., 1./16.};
        FloatProcessor ip2 = new FloatProcessor(ip.getWidth(), ip.getHeight());
        FloatProcessor tmp = new FloatProcessor(ip.getWidth(), ip.getHeight());
        for (int i = 0; i < ip.getWidth(); i++) {
            for (int j = 0; j < ip.getHeight(); j++) {
                float sum = 0;
                for (int k = 0; k < kernel.length; k++) {
                    int dx = i - neighbors[k];
                    if (dx < 0) {
                        dx = -dx;
                    }
                    if (dx >= ip.getWidth()) {
                        dx = ip.getWidth() - 1 - (dx - ip.getWidth());
                    }
                    sum += kernel[k] * ip.getPixelValue(dx, j);
                }
                tmp.setf(i, j, sum);
            }
        }

        for (int j = 0; j < ip.getHeight(); j++) {
            for (int i = 0; i < ip.getWidth(); i++) {
                float sum = 0;
                for (int k = 0; k < kernel.length; k++) {
                    int dy = j - neighbors[k];
                    if (dy < 0) {
                        dy = -dy;
                    }
                    if (dy >= ip.getHeight()) {
                        dy = ip.getHeight() - 1 - (dy - ip.getHeight());
                    }
                    sum += kernel[k] * tmp.getPixelValue(i, dy);
                }
                ip2.setf(i, j, sum);
            }
        }
        return ip2;
    }
    protected FloatProcessor applyKernelLowScale( FloatProcessor ip, int[] neighbors ) {
        int dx2[] = {-1, 0, 1, 1, 1, 0, -1, -1, 0};
        int dy2[] = {1, 1, 1, 0, -1, -1, -1, 0, 0};
        double[] kernel = {1./16., 1./8., 1./16., 1./8., 1./16., 1./8., 1./16., 1./8., 10.};
        FloatProcessor ip2 = new FloatProcessor(ip.getWidth(), ip.getHeight());
        FloatProcessor tmp = new FloatProcessor(ip.getWidth(), ip.getHeight());
        for (int i = 0; i < ip.getWidth(); i++) {
            for (int j = 0; j < ip.getHeight(); j++) {
                float sum = 0;
                for (int k = 0; k < kernel.length; k++) {
                    int dx = i + dx2[k] * neighbors[3];
                    if (dx < 0) {
                        dx = -dx;
                    }
                    if (dx >= ip.getWidth()) {
                        dx = ip.getWidth() - 1 - (dx - ip.getWidth());
                    }
                    int dy = j + dy2[k] * neighbors[3];
                    if (dy < 0) {
                        dy = -dy;
                    }
                    if (dy >= ip.getHeight()) {
                        dy = ip.getHeight() - 1 - (dy - ip.getHeight());
                    }
                    sum += kernel[k] * ip.getPixelValue(dx, dy);
                }
                tmp.setf(i, j, sum);
            }
        }
        return tmp;
    }
    protected FloatProcessor applyKernelLinearInterpolation( FloatProcessor ip, int[] neighbors ) {
        int dx2[] = {-1, 0, 1, 1, 1, 0, -1, -1, 0};
        int dy2[] = {1, 1, 1, 0, -1, -1, -1, 0, 0};
        double[] kernel = {1./16., 1./8., 1./16., 1./8., 1./16., 1./8., 1./16., 1./8., 1./4.};
        FloatProcessor ip2 = new FloatProcessor(ip.getWidth(), ip.getHeight());
        FloatProcessor tmp = new FloatProcessor(ip.getWidth(), ip.getHeight());
        for (int i = 0; i < ip.getWidth(); i++) {
            for (int j = 0; j < ip.getHeight(); j++) {
                float sum = 0;
                for (int k = 0; k < kernel.length; k++) {
                    int dx = i + dx2[k] * neighbors[3];
                    if (dx < 0) {
                        dx = -dx;
                    }
                    if (dx >= ip.getWidth()) {
                        dx = ip.getWidth() - 1 - (dx - ip.getWidth());
                    }
                    int dy = j + dy2[k] * neighbors[3];
                    if (dy < 0) {
                        dy = -dy;
                    }
                    if (dy >= ip.getHeight()) {
                        dy = ip.getHeight() - 1 - (dy - ip.getHeight());
                    }
                    sum += kernel[k] * ip.getPixelValue(dx, dy);
                }
                tmp.setf(i, j, sum);
            }
        }
        return tmp;
    }

    public int getNbWavelets(){
        return nbWavelets;
    }
    public int getWidth(){
        return width;
    }
    public int getHeight(){
        return height;
    }
}
