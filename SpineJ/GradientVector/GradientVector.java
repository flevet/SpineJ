/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.GradientVector;

import ij.ImagePlus;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

/**
 *
 * @author Florian Levet
 */
public class GradientVector {

    static public int AxeX = 0;
    static public int AxeY = 1;

    public float costs[][][] = null;
    float scalef = 0.25f;//1.f;

    public GradientVector(){
        
    }

    public float [][][] getCosts(){
        return costs;
    }

    public void correctCosts( ImageProcessor ip ){
        for( int j = 0; j < ip.getHeight(); j++ )
            for( int i = 0; i < ip.getWidth(); i++ )
                    costs[0][j][i] = ip.get(i, j);
    }

    public ImageProcessor generateCostByteImage( ImageProcessor _image ){
        costs = run( _image, true, scalef );
        double pixels[] = new double[ _image.getWidth() * _image.getHeight() ];
        int cpt = 0;
	for( int j = 0; j < _image.getHeight(); j++ )
		for( int i = 0; i < _image.getWidth(); i++ )
			pixels[cpt++] = costs[0][j][i];
        FloatProcessor fp = new FloatProcessor(_image.getWidth(), _image.getHeight(), pixels);
        ImagePlus imp = new ImagePlus("res", fp);
        ImageConverter ic = new ImageConverter(imp);
        ic.convertToGray8();
        
        return imp.getProcessor();
    }

    public void generateCostImage( double pixels[], ImageProcessor _image )
    {
	costs = run( _image, true, scalef );
	int cpt = 0;
	for( int j = 0; j < _image.getHeight(); j++ )
		for( int i = 0; i < _image.getWidth(); i++ )
			pixels[cpt++] = costs[0][j][i];

        FloatProcessor fp = new FloatProcessor(_image.getWidth(), _image.getHeight(), pixels);
        ImagePlus imp = new ImagePlus("Costs", fp.duplicate());
        imp.show();

        imp = new ImagePlus("ArrayListField", _image.duplicate());
        imp.show();
        VectorField1 vf = new VectorField1(imp, costs);

        imp = new ImagePlus("ArrayListFieldCost", fp.duplicate());
        imp.show();
        vf = new VectorField1(imp, costs);
    }

    public void generateCostImageSimple( ImageProcessor _image )
    {
	costs = run( _image, true, scalef );
    }

    protected float[][][] run( ImageProcessor _image, boolean _flag, float _f )
    {
	int w = _image.getWidth(), h = _image.getHeight();
	float af[] = run( _image, _f, 2, 0 );
	float af1[] = run( _image, _f, 1, 1 );
	float af2[] = run( _image, _f, 0, 2 );

	float af3[][][] = new float[3][h][w];

	float af4[][] = af3[0];
	float af5[][] = af3[1];
	float af6[][] = af3[2];
	float f1 = _flag ? 1.0f : -1.f;
	float f2 = 0;
	float f3 = 0;
	for(; f2 < h; f2++)
	{
		for(int i = 0; i < w;)
		{
			float f4 = f1 * (af[(int)f3] + af2[(int)f3]);
			float f5 = f1 * (af[(int)f3] - af2[(int)f3]);
			float f7 = (float)Math.sqrt((float)(4.f * af1[(int)f3] * af1[(int)f3] + f5 * f5));
			float f8 = (f4 + 2.0f * f7) / 3.f;
			float f9 = (f4 - 2.0f * f7) / 3.f;
			float f10 = Math.abs(f8);
			float f11 = Math.abs(f9);
			if(f10 > f11)
			{
				if(f8 > 0.0f)
					af4[(int)f2][i] = 0.0f;
				else
					af4[(int)f2][i] = f10;
				af5[(int)f2][i] = f5 - f7;
			} else
			{
				if(f9 > 0.0f)
					af4[(int)f2][i] = 0.0f;
				else
					af4[(int)f2][i] = f11;
				af5[(int)f2][i] = f5 + f7;
			}
			af6[(int)f2][i] = 2.0f * f1 * af1[(int)f3];
			i++;
			f3++;
		}
	}
	f2 = af4[0][0];
	f3 = f2;
	for(int j = 0; j < h; j++)
	{
		for(int k = 0; k < w; k++)
		{
			if(af4[j][k] > f3)
			{
				f3 = af4[j][k];
				continue;
			}
			if(af4[j][k] < f2)
				f2 = af4[j][k];
		}

	}

	float f6 = 255.f / (f3 - f2);
	int l = 0;
	int i1 = 0;
	for(; l < h; l++)
	{
		for(int j1 = 0; j1 < w;)
		{
			af4[l][j1] = 255.f - (af4[l][j1] - f2) * f6;
			float f12 = (float)Math.sqrt(af5[l][j1] * af5[l][j1] + af6[l][j1] * af6[l][j1]);
			if(f12 > 0.0f)
			{
				af5[l][j1] /= f12;
				af6[l][j1] /= f12;
			}
			j1++;
			i1++;
		}

	}

	return af3;
    }

    void displayImagePlus(String title, float pixels[], int w, int h){
        double [] pix = new double[w*h];
        for(int i = 0; i < w*h; i++)
            pix[i] = pixels[i];
        FloatProcessor fp = new FloatProcessor(w, h, pix);
        ImagePlus toto = new ImagePlus(title, fp);
        toto.show();
    }

    float [] run( ImageProcessor _image, double _scale, int _xorder, int _yorder )
    {
        int w = _image.getWidth(), h = _image.getHeight();
        float pix[][] = _image.getFloatArray();
        float pixels[] = new float[w*h];
        int cpt = 0;
        for(int j = 0; j < h; j++)
            for(int i = 0; i < w; i++)
            {
                pixels[cpt++] = pix[i][j];
            }

	if( w > 1 ){
		double xscale = _scale;
		double kernel[] = kernelComputation( xscale,_xorder, w );
		int klenm1 = kernel.length - 1;
		double ain[] = new double[w + 2*klenm1];
		double aout[] = new double[w];
		for( int y = 0; y < h; ++y ) {
			int x = -klenm1;
			get( AxeX, x, y, w, h, ain, pixels, w + 2*klenm1);
			convolve(ain,aout,kernel,w);
			x = 0;
			set( AxeX, x, y, w, h, aout, pixels, w );
		}
	}

	if( h > 1 ){
		double yscale = _scale;
		double kernel[] = kernelComputation( yscale, _yorder, h);
		int klenm1 = kernel.length - 1;
		double ain[] = new double[h + 2*klenm1];
		double aout[] = new double[h];
		for ( int x=0; x < w; ++x ) {
			int y = -klenm1;
			get( AxeY, x, y, w, h, ain, pixels, h + 2*klenm1 );
			convolve(ain,aout,kernel,h);
			y = 0;
			set(AxeY, x, y, w, h, aout, pixels, h );
		}
	}
	return pixels;
    }

    void get( int _axe, int _coordX, int _coordY, int _w, int _h, double _values[], float _pixels[], int _length )
    {
	int length = _length;
	if( _axe == AxeX ){
		int vxstart = 0;
		int exstart = _coordX;
		if (exstart < 0) {
			exstart = 0;
			vxstart = -_coordX;
		}
		int exstop = _coordX + length;
		if ( exstop > _w )
			exstop = _w;
		for ( int x=exstart, vx=vxstart; x<exstop; ++x, ++vx )
			_values[vx] = _pixels[_coordY * _w + x];
	}
	else if( _axe == AxeY ){
		int vystart = 0;
		int eystart = _coordY;
		if (eystart < 0) {
			eystart = 0;
			vystart = -_coordY;
		}
		int eystop = _coordY + length;
		if (eystop > _h)
			eystop = _h;
		for (int y=eystart, vy = vystart; y<eystop; ++y, ++vy)
			_values[vy] = _pixels[y * _w + _coordX];
	}
    }

    void set( int _axe, int _coordX, int _coordY, int _w, int _h, double _values[], float _pixels[], int _length )
    {
	int length = _length;
	if( _axe == AxeX ){
		int vxstart = 0;
		int exstart = _coordX;
		if (exstart < 0) {
			exstart = 0;
			vxstart = -_coordX;
		}
		int exstop = _coordX + length;
		if ( exstop > _w )
			exstop = _w;
		for ( int x=exstart, vx=vxstart; x<exstop; ++x, ++vx )
			_pixels[_coordY * _w + x] = (float)_values[vx] ;
	}
	else if( _axe == AxeY ){
		int vystart = 0;
		int eystart = _coordY;
		if (eystart < 0) {
			eystart = 0;
			vystart = -_coordY;
		}
		int eystop = _coordY + length;
		if (eystop > _h)
			eystop = _h;
		for (int y=eystart, vy = vystart; y<eystop; ++y, ++vy)
			_pixels[y * _w + _coordX] = (float)_values[vy];
	}
    }

    double[] kernelComputation( double s, int d, int m )
    {
	// Initialize:
	double r = 5;
	if (d == 0) r = 3;
	else if (d <= 2) r = 4;
	int h = (int)(s*r) + 1;
	if (h > m) h = m;
	double kernel[] = new double[h];
	kernel[0] = (d == 0) ? 1 : 0;

	// Compute kernel:
	if (h > 1) {
		double is2 = 1/(s*s);
		double is4 = is2*is2;
		double is6 = is4*is2;
		double is8 = is6*is2;
		double is10 = is8*is2;
		double mis2 = -0.5*is2;
		double sq2pi = Math.sqrt(2*Math.PI);
		switch (d) {
				case 0: {
					double integral = 0;
					for (int k=0; k<h; ++k) {
						kernel[k] = Math.exp(k*k*mis2);
						integral += kernel[k];
					}
					integral *= 2.0;
					integral -= kernel[0];
					for (int k=0; k<h; ++k)
						kernel[k] /= integral;
					break;
						}
				case 1: {
					double c = -is2/(sq2pi*s);
					for (int k=1; k<h; ++k) {
						double k2 = k*k;
						kernel[k] = c*k*Math.exp(k2*mis2);
					}
					break;
						}
				case 2: {
					double c = is2/(sq2pi*s);
					for (int k=0; k<h; ++k) {
						double k2 = k*k;
						kernel[k] = c*(k2*is2 - 1)*Math.exp(k2*mis2);
					}
					break;
						}
				case 3: {
					double c = -is4/(sq2pi*s);
					for (int k=1; k<h; ++k) {
						double k2 = k*k;
						kernel[k] = c*k*(k2*is2 - 3)*Math.exp(k2*mis2);
					}
					break;
						}
				case 4: {
					double c = is4/(sq2pi*s);
					for (int k=0; k<h; ++k) {
						double k2 = k*k;
						kernel[k] = c*(k2*k2*is4 - 6*k2*is2 + 3)*Math.exp(k2*mis2);
					}
					break;
						}
				case 5: {
					double c = -is6/(sq2pi*s);
					for (int k=1; k<h; ++k) {
						double k2 = k*k;
						kernel[k] = c*k*(k2*k2*is4 - 10*k2*is2 + 15)*Math.exp(k2*mis2);
					}
					break;
						}
				case 6: {
					double c = is6/(sq2pi*s);
					for (int k=0; k<h; ++k) {
						double k2 = k*k;
						double k4 = k2*k2;
						kernel[k] = c*(k4*k2*is6 - 15*k4*is4 + 45*k2*is2 - 15)*Math.exp(k2*mis2);
					}
					break;
						}
				case 7: {
					double c = -is8/(sq2pi*s);
					for (int k=1; k<h; ++k) {
						double k2 = k*k;
						double k4 = k2*k2;
						kernel[k] = c*k*(k4*k2*is6 - 21*k4*is4 + 105*k2*is2 - 105)*Math.exp(k2*mis2);
					}
					break;
						}
				case 8: {
					double c = is8/(sq2pi*s);
					for (int k=0; k<h; ++k) {
						double k2 = k*k;
						double k4 = k2*k2;
						kernel[k] = c*(k4*k4*is8 - 28*k4*k2*is6 + 210*k4*is4 - 420*k2*is2 + 105)*Math.exp(k2*mis2);
					}
					break;
						}
				case 9: {
					double c = -is10/(sq2pi*s);
					for (int k=1; k<h; ++k) {
						double k2 = k*k;
						double k4 = k2*k2;
						kernel[k] = c*k*(k4*k4*is8 - 36*k4*k2*is6 + 378*k4*is4 - 1260*k2*is2 + 945)*Math.exp(k2*mis2);
					}
					break;
						}
				case 10: {
					double c = is10/(sq2pi*s);
					for (int k=0; k<h; ++k) {
						double k2 = k*k;
						double k4 = k2*k2;
						double k6 = k4*k2;
						kernel[k] = c*(k6*k4*is10 - 45*k4*k4*is8 + 630*k6*is6 - 3150*k4*is4 + 4725*k2*is2 - 945)*Math.exp(k2*mis2);
					}
					break;
						 }
		}
	}
	return kernel;
    }

    void convolve( double ain[], double aout[], double kernel[], int _length )
    {
	// Mirror borders in input array:
	int khlenm1 = kernel.length - 1;
	int aolenm1 = _length - 1;
	for (int k=0, lm=khlenm1, lp=khlenm1, hm=khlenm1+aolenm1, hp=khlenm1+aolenm1; k<khlenm1; ++k) {
		ain[--lm] = ain[++lp];
		ain[++hp] = ain[--hm];
	}

	// Convolve with kernel:
	double sign = (kernel[0] == 0) ? -1 : 1;
	for (int io=0, ii=khlenm1; io<=aolenm1; ++io, ++ii) {
		double convres = ain[ii]*kernel[0];
		for (int k=1, iimk=ii, iipk=ii; k<=khlenm1; ++k)
			convres += (ain[--iimk] + sign*ain[++iipk])*kernel[k];
		aout[io] = convres;
	}
    }
}
