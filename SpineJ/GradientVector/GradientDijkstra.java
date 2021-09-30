package SpineJ.GradientVector;
import ij.IJ;

public class GradientDijkstra
{
    static int dijkrange = 2500;
    static float gamma = 0.7F;
    
    public GradientDijkstra()
    {
        ccost = null;
        istat = null;
        dirs = (byte[][])null;
    }

    public byte[][] run(float af[][][], int _x, int _y)
    {
        float af1[][] = af[0];
        float af2[][] = af[1];
        float af3[][] = af[2];
        int i = af1.length;
        int j = af1[0].length;
        int k = i - 1;
        int l = j - 1;
        int i1 = _y;
        int j1 = _x;
        if(i1 <= 0 || i1 >= k || j1 <= 0 || j1 >= l){
            IJ.log("" + i1 + ", " + k + ", " + j1 + ", " + l);
            throw new IllegalArgumentException("Starting point on or outside border of image");
        }
        int k1 = i1 * j + j1;
        int l1 = i * j;
        if(dirs == null || dirs.length != i || dirs[0].length != j)
        {
            dirs = new byte[i][j];
            ccost = new int[l1];
            istat = new int[l1];
        }
        int i2 = j - 2;
        int j2 = i - 2;
        int k2 = 1;
        int l2 = 1;
        int i3 = i2;
        int j3 = j2;
        int k3 = GradientDijkstra.dijkrange / 2;
        if(GradientDijkstra.dijkrange < i2)
        {
            k2 = j1 - k3;
            i3 = j1 + k3;
            if(k2 < 1)
            {
                k2 = 1;
                i3 = GradientDijkstra.dijkrange;
            }
            if(i3 > i2)
            {
                i3 = i2;
                k2 = l - GradientDijkstra.dijkrange;
            }
        }
        if(GradientDijkstra.dijkrange < j2)
        {
            l2 = i1 - k3;
            j3 = i1 + k3;
            if(l2 < 1)
            {
                l2 = 1;
                j3 = GradientDijkstra.dijkrange;
            }
            if(j3 > j2)
            {
                j3 = j2;
                l2 = k - GradientDijkstra.dijkrange;
            }
        }
        int l3 = 0;
        int i4 = 0;
        for(; l3 < l2; l3++)
        {
            for(int i5 = 0; i5 < j;)
            {
                istat[i4] = 0x7fffffff;
                dirs[l3][i5] = 0;
                i5++;
                i4++;
            }

        }

        l3 = j3 + 1;
        i4 = (j3 + 1) * j;
        for(; l3 < i; l3++)
        {
            for(int j5 = 0; j5 < j;)
            {
                istat[i4] = 0x7fffffff;
                dirs[l3][j5] = 0;
                j5++;
                i4++;
            }

        }

        l3 = l2;
        for(int j4 = l2 * j; l3 <= j3; j4 += j)
        {
            int k5 = 0;
            for(int j6 = j4; k5 < k2; j6++)
            {
                istat[j6] = 0x7fffffff;
                dirs[l3][k5] = 0;
                k5++;
            }

            k5 = i3 + 1;
            for(int k6 = j4 + i3 + 1; k5 < j; k6++)
            {
                istat[k6] = 0x7fffffff;
                dirs[l3][k5] = 0;
                k5++;
            }

            l3++;
        }

        l3 = l2;
        for(int k4 = l2 * j; l3 <= j3; k4 += j)
        {
            int l5 = k2;
            for(int l6 = k4 + k2; l5 <= i3; l6++)
            {
                dirs[l3][l5] = 0;
                ccost[l6] = 0x7fffffff;
                istat[l6] = 0x7ffffffe;
                l5++;
            }

            l3++;
        }

        GradientQueueElement aqueueelement[] = new GradientQueueElement[256];
        for(int l4 = 0; l4 < 256; l4++)
            aqueueelement[l4] = new GradientQueueElement();

        int ai[] = new int[9];
        ai[8] = -j - 1;
        ai[7] = -j;
        ai[6] = -j + 1;
        ai[5] = -1;
        ai[4] = 1;
        ai[3] = j - 1;
        ai[2] = j;
        ai[1] = j + 1;
        ai[0] = 0;
        ccost[k1] = 0;
        byte byte0 = -1;
        int i7 = 0;
        aqueueelement[i7].add(k1);
        boolean flag = true;
        float f = GradientDijkstra.gamma;
        float f1 = 1.0F - f;
label0:
        do
        {
            if(!flag)
                break;
            int j7 = aqueueelement[i7].remove();
            istat[j7] = 0x7fffffff;
            int k7 = j7 / j;
            int l7 = j7 % j;
            for(int i8 = 1; i8 < 9; i8++)
            {
                int j8 = j7 + ai[i8];
                if(istat[j8] == 0x7fffffff)
                    continue;
                int k8 = j8 / j;
                int l8 = j8 % j;
                float f2 = k8 - k7;
                float f3 = l8 - l7;
                float f4 = (float)Math.sqrt(f2 * f2 + f3 * f3);
                f2 /= f4;
                f3 /= f4;
                int i9 = ccost[j8];
                int j9 = ccost[j7] + (int)(f * af1[k8][l8] + f1 * 127F * (float)(Math.sqrt(1.0F - Math.abs(af3[k7][l7] * f2 + af2[k7][l7] * f3)) + Math.sqrt(1.0F - Math.abs(af3[k8][l8] * f2 + af2[k8][l8] * f3))));
                if(j9 >= i9)
                    continue;
                ccost[j8] = j9;
                dirs[k8][l8] = (byte)i8;
                if(istat[j8] == 0x7ffffffe)
                {
                    istat[j8] = aqueueelement[j9 & 0xff].add(j8);
                } else
                {
                    int k9 = i9 & 0xff;
                    int l9 = istat[j8];
                    aqueueelement[k9].remove(l9);
                    istat[aqueueelement[k9].get(l9)] = l9;
                    istat[j8] = aqueueelement[j9 & 0xff].add(j8);
                }
            }

            int i6 = i7;
            do
            {
                if(aqueueelement[i7].size() != 0)
                    continue label0;
                i7 = ++i7 & 0xff;
            } while(i7 != i6);
            flag = false;
        } while(true);
        return dirs;
    }

    private final int INFINITE = 0x7fffffff;
    private final int PROCESSED = 0x7fffffff;
    private final int FREE = 0x7ffffffe;
    private int ccost[];
    private int istat[];
    private byte dirs[][];
}