/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SpineJ.Tools.Geometry;

// file: QuickHull.java
// date: 06/09/09
import ij.gui.Roi;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class QuickHull {
    // variables

    int pNum = 100;
    double xPoints[];
    double yPoints[];
    double xPoints2[];
    double yPoints2[];
    Point2D.Double[] m_hull = null;
    int num;

    // check whether point p is right of line ab
    public double right(int a, int b, int p) {
        return (xPoints[a] - xPoints[b]) * (yPoints[p] - yPoints[b]) - (xPoints[p] - xPoints[b]) * (yPoints[a] - yPoints[b]);
    }

    // square distance point p to line ab
    public float distance(int a, int b, int p) {
        float x, y, u;
        u = (((float) xPoints[p] - (float) xPoints[a]) * ((float) xPoints[b] - (float) xPoints[a]) + ((float) yPoints[p] - (float) yPoints[a]) * ((float) yPoints[b] - (float) yPoints[a]))
                / (((float) xPoints[b] - (float) xPoints[a]) * ((float) xPoints[b] - (float) xPoints[a]) + ((float) yPoints[b] - (float) yPoints[a]) * ((float) yPoints[b] - (float) yPoints[a]));
        x = (float) xPoints[a] + u * ((float) xPoints[b] - (float) xPoints[a]);
        y = (float) yPoints[a] + u * ((float) yPoints[b] - (float) yPoints[a]);
        return ((x - (float) xPoints[p]) * (x - (float) xPoints[p]) + (y - (float) yPoints[p]) * (y - (float) yPoints[p]));
    }

    public int farthestpoint(int a, int b, ArrayList<Integer> al) {
        float maxD, dis;
        int maxP, p;
        maxD = -1;
        maxP = -1;
        for (int i = 0; i < al.size(); i++) {
            p = al.get(i);
            if ((p == a) || (p == b)) {
                continue;
            }
            dis = distance(a, b, p);
            if (dis > maxD) {
                maxD = dis;
                maxP = p;
            }
        }
        return maxP;
    }

    public void quickhull(int a, int b, ArrayList<Integer> al) {
        //System.out.println("a:"+a+",b:"+b+" size: "+al.size());
        if (al.isEmpty()) {
            return;
        }

        int c, p;

        c = farthestpoint(a, b, al);

        ArrayList<Integer> al1 = new ArrayList<Integer>();
        ArrayList<Integer> al2 = new ArrayList<Integer>();

        for (int i = 0; i < al.size(); i++) {
            p = al.get(i);
            if ((p == a) || (p == b)) {
                continue;
            }
            if (right(a, c, p) > 0) {
                al1.add(p);
            } else if (right(c, b, p) > 0) {
                al2.add(p);
            }
        }

        quickhull(a, c, al1);
        xPoints2[num] = xPoints[c];
        yPoints2[num] = yPoints[c];
        num++;
        quickhull(c, b, al2);
    }

    public void quickconvexhull(double[] _xs, double[] _ys) {
        xPoints = _xs;
        yPoints = _ys;
        xPoints2 = new double[xPoints.length];
        yPoints2 = new double[yPoints.length];
        pNum = xPoints.length;

        // find two points: right (bottom) and left (top)
        int r, l;
        r = l = 0;
        for (int i = 1; i < pNum; i++) {
            if ((xPoints[r] > xPoints[i]) || (xPoints[r] == xPoints[i] && yPoints[r] > yPoints[i])) {
                r = i;
            }
            if ((xPoints[l] < xPoints[i]) || (xPoints[l] == xPoints[i] && yPoints[l] < yPoints[i])) {
                l = i;
            }
        }

        //System.out.println("l: "+l+", r: "+r);

        ArrayList<Integer> al1 = new ArrayList<Integer>();
        ArrayList<Integer> al2 = new ArrayList<Integer>();

        double upper;
        for (int i = 0; i < pNum; i++) {
            if ((i == l) || (i == r)) {
                continue;
            }
            upper = right(r, l, i);
            if (upper > 0) {
                al1.add(i);
            } else if (upper < 0) {
                al2.add(i);
            }
        }

        xPoints2[num] = xPoints[r];
        yPoints2[num] = yPoints[r];
        num++;
        quickhull(r, l, al1);
        xPoints2[num] = xPoints[l];
        yPoints2[num] = yPoints[l];
        num++;
        quickhull(l, r, al2);

        m_hull = new Point2D.Double[num];
        for (int n = 0; n < num; n++)
            m_hull[n] = new Point2D.Double(xPoints2[n], yPoints2[n]);
        xPoints = yPoints = xPoints2 = yPoints2 = null;
    }

    public boolean contains( Roi _roi ){
        Polygon p = _roi.getPolygon();
        for( int n = 0; n < p.npoints; n++ )
            if( contains( p.xpoints[n], p.ypoints[n] ) )
                return true;
        return false;
    }

    public boolean contains(double x, double y) {
        if (m_hull.length <= 2) {
            return false;
        }
        int hits = 0;

        double lastx = m_hull[m_hull.length - 1].x;
        double lasty = m_hull[m_hull.length - 1].y;
        double curx, cury;

        // Walk the edges of the polygon
        for (int i = 0; i < m_hull.length; lastx = curx, lasty = cury, i++) {
            curx = m_hull[i].x;
            cury = m_hull[i].y;

            if (cury == lasty) {
                continue;
            }

            double leftx;
            if (curx < lastx) {
                if (x >= lastx) {
                    continue;
                }
                leftx = curx;
            } else {
                if (x >= curx) {
                    continue;
                }
                leftx = lastx;
            }

            double test1, test2;
            if (cury < lasty) {
                if (y < cury || y >= lasty) {
                    continue;
                }
                if (x < leftx) {
                    hits++;
                    continue;
                }
                test1 = x - curx;
                test2 = y - cury;
            } else {
                if (y < lasty || y >= cury) {
                    continue;
                }
                if (x < leftx) {
                    hits++;
                    continue;
                }
                test1 = x - lastx;
                test2 = y - lasty;
            }

            if (test1 < (test2 / (lasty - cury) * (lastx - curx))) {
                hits++;
            }
        }

        return ((hits & 1) != 0);
    }

    public Point2D.Double [] getHull(){
        return m_hull;
    }
}
