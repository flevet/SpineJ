/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SpineJ.Analysis;

import SpineJ.Tools.Graph.EdgeGraph;
import SpineJ.Tools.Delaunay.TriangleT;
import SpineJ.Tools.Geometry.CatmullCurve;
import SpineJ.Tools.Geometry.DoublePolygon;
import SpineJ.Tools.Geometry.StraightLine;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 *
 * @author Florian Levet
 */
public class SpineStatisticsGenerator {
    
    static public void determineSpineCharacteristics( NeuronObject _spine, double _x, double _y, double _fromWidth, double _toWidth, double _stepWidth, double _height, double _minFitValue, ImagePlus _imp, ImageProcessor _ip ){
        double [] infos = new double[3];
        Point2D.Double p = new Point2D.Double( _x, _y ), intersection = new Point2D.Double();
        _spine.setPointDelimitatingNeckHead(p);
        int index = _spine.insideInfluenceRegionIndex( p );
        TriangleT triangle = _spine.insideSpecificTriangle( p );
        infos = skeletonTriangle( _spine, triangle, p, intersection, _spine.getEdges().get( index ), _spine.getSkeletons()[index] );
        ArrayList < Point2D.Double > skel = _spine.getSkeletonPath();
        
        //Compute nbLines dependant on height line
        _spine.clearShapesNeckAnalysis();
            ArrayList < StraightLine > samplingLines = getLineShapeFromSkeletonArcLength(skel, _height);
            for(StraightLine line : samplingLines){
                 NeckShape nshape = new NeckShape( line, _fromWidth, _toWidth, _stepWidth, _height, _minFitValue, _imp, _ip );
                 if(nshape.getGoodnessFit() > _minFitValue){
                    _spine.addShapeNeckAnalysis( nshape );
                 }
            }
            _spine.setLengthInfos( infos );
    }
    
    static ArrayList < StraightLine > getLineShapeFromSkeletonArcLength(ArrayList < Point2D.Double > _origSkelPoints, double _heightLines){
        double skelLength = 0., nbLines = 0, step = 0., halfStep = 0., currentT = 0.;
        for( int n = 1; n < _origSkelPoints.size(); n++ )
            skelLength += _origSkelPoints.get( n ).distance( _origSkelPoints.get( n - 1 ) );
        nbLines = Math.ceil(skelLength / (_heightLines * 0.75));
        step = 1. / nbLines;
        halfStep = step / 2.;
        
        ArrayList < StraightLine > samplingLines = new ArrayList < StraightLine >();
        CatmullCurve cc = new CatmullCurve(_origSkelPoints, 0, 1,  _origSkelPoints.size());
        currentT += halfStep;
        while(currentT < 1.){
            Point2D.Double p1 = cc.getArcLengthPoint(currentT);
            Point2D.Double p2 = cc.getArcLengthPoint(currentT + step);
            StraightLine l = new StraightLine( p1, p2, StraightLine.PARALLELE_LINE );
            l.setNormalizedStep(currentT);
            samplingLines.add(l);
            currentT += step;
        }
        return samplingLines;
    }
    
    static double [] skeletonTriangle( NeuronObject _spine, TriangleT _triangle, Point2D.Double _pos, Point2D.Double _intersection, EdgeGraph _edge, CatmullCurve _skel ){
        double [] distance = new double[3];
        StraightLine lineThickness = null;
        
        StraightLine l1 = new StraightLine( _triangle.getA().getX(), _triangle.getA().getY(), _triangle.getB().getX(), _triangle.getB().getY(), StraightLine.PARALLELE_LINE );
        StraightLine l2 = new StraightLine( _triangle.getB().getX(), _triangle.getB().getY(), _triangle.getC().getX(), _triangle.getC().getY(), StraightLine.PARALLELE_LINE );
        StraightLine l3 = new StraightLine( _triangle.getC().getX(), _triangle.getC().getY(), _triangle.getA().getX(), _triangle.getA().getY(), StraightLine.PARALLELE_LINE );
        Point2D.Double inter1 = l1.orthProjection( _pos.getX(), _pos.getY() );
        Point2D.Double inter2 = l2.orthProjection( _pos.getX(), _pos.getY() );
        Point2D.Double inter3 = l3.orthProjection( _pos.getX(), _pos.getY() );
        double d1 = inter1.distance( _pos.getX(), _pos.getY() );
        double d2 = inter2.distance( _pos.getX(), _pos.getY() );
        double d3 = inter3.distance( _pos.getX(), _pos.getY() );
        Line2D.Double sepHeadNeck = new Line2D.Double();
        if( d1 < d2 ){
            if( d1 < d3 ){
                lineThickness = l1;
                sepHeadNeck.setLine(_triangle.getA().getX(), _triangle.getA().getY(), _triangle.getB().getX(), _triangle.getB().getY());
            }
            else{
                lineThickness = l3;
                sepHeadNeck.setLine( _triangle.getC().getX(), _triangle.getC().getY(), _triangle.getA().getX(), _triangle.getA().getY());
            }
        }
        else{
            if( d2 < d3 ){
                lineThickness = l2;
                sepHeadNeck.setLine(_triangle.getB().getX(), _triangle.getB().getY(), _triangle.getC().getX(), _triangle.getC().getY());
            }
            else{
                lineThickness = l3;
                sepHeadNeck.setLine( _triangle.getC().getX(), _triangle.getC().getY(), _triangle.getA().getX(), _triangle.getA().getY());
            }
        }
        Point2D.Double inter = null;
        int indexInter = -1;
        int size = _skel.points.npoints;
        for( int i = 1; i < size && inter == null; i++ ){
            double x1 = _skel.points.xpoints[i-1], y1 = _skel.points.ypoints[i-1];
            double x2 = _skel.points.xpoints[i%size], y2 = _skel.points.ypoints[i%size];
            StraightLine line = new StraightLine( x1, y1, x2, y2, StraightLine.PARALLELE_LINE );
            inter = lineThickness.intersectionOneSegment( line );
            if( inter != null )
                indexInter = i;
        }
        if( inter != null ){
            double [] dTmp = determineSkeletonLength( _spine, _spine.determineSkeletonPathToActin( _edge ), _edge, inter, indexInter );
            System.arraycopy(dTmp, 0, distance, 0, 3);
        }
        
        cutNeckFromHead( _spine, lineThickness );
        _spine.setSeparationHeadNeck(sepHeadNeck);
        
        return distance;
    }
    
    static double [] determineSkeletonLength( NeuronObject _spine, ArrayList < EdgeGraph > _skeletonPath, EdgeGraph _edgeInter, Point2D.Double _intersect, int _indexInter ){
        double [] distances = new double[3];
        ArrayList < Point2D.Double > spath = new ArrayList < Point2D.Double >();
        for( int l = 0; l < _skeletonPath.size() - 1; l++ ){
            CatmullCurve skel2 = _spine.getSkeletonFromEdge( _skeletonPath.get( l ) );
            for( int l1 = 0; l1 < skel2.points.npoints - 1; l1++ )
                spath.add( new Point2D.Double( skel2.points.xpoints[l1], skel2.points.ypoints[l1] ) );
        }
        CatmullCurve skel2 = _spine.getSkeletonFromEdge( _edgeInter );
        for( int l1 = 0; l1 < _indexInter; l1++ )
            spath.add( new Point2D.Double( skel2.points.xpoints[l1], skel2.points.ypoints[l1] ) );
        spath.add( new Point2D.Double( _intersect.x, _intersect.y ) );
        double d = 0.;
        for( int n = 1; n < spath.size(); n++ )
            d += spath.get( n ).distance( spath.get( n - 1 ) );
        distances[1] = d;
        distances[2] = _spine.getLongestPathWithEdge( _edgeInter );
        distances[0] = distances[1] / distances[2]; 
        _spine.m_skeletonPath = spath;
        return distances;
    }
    
    static void cutNeckFromHead( NeuronObject _spine, StraightLine _line ){
        DoublePolygon outline = _spine.getOutline();
        Point2D.Double p1 = _line.getP1(), p2 = _line.getP2();
        ArrayList < Point2D.Double > neck = new ArrayList < Point2D.Double >(), head = new ArrayList < Point2D.Double >();
        boolean isNeck = true;
        double xp1 = p1.getX(), yp1 = p1.getY(), xp2 = p2.getX(), yp2 = p2.getY();
        for( int n = 0; n < outline.npoints; n++ ){
            if( ( outline.xpoints[n] == xp1 && outline.ypoints[n] == yp1 ) || outline.xpoints[n] == xp2 && outline.ypoints[n] == yp2 ){
                if( isNeck )
                    neck.add( new Point2D.Double( outline.xpoints[n], outline.ypoints[n] ) );
                else
                    head.add( new Point2D.Double( outline.xpoints[n], outline.ypoints[n] ) );
                isNeck = !isNeck;
            }
            if( isNeck )
                    neck.add( new Point2D.Double( outline.xpoints[n], outline.ypoints[n] ) );
                else
                    head.add( new Point2D.Double( outline.xpoints[n], outline.ypoints[n] ) );
        }
        Point2D.Double basis = ( Point2D.Double )_spine.getSpineBasis().getP1();
        boolean found = false;
        for( int n = 0; n < head.size() && !found; n++ )
            found = ( head.get( n ).equals( basis ) );
        if( found ){
            ArrayList < Point2D.Double > tmp = head;
            head = neck;
            neck = tmp;
        }
        _spine.setNeckAndHead( neck, head );
    }
}
