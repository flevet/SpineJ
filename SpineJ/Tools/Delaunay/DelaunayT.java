/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SpineJ.Tools.Delaunay;

import SpineJ.Tools.Geometry.CatmullCurve;
import SpineJ.Tools.Geometry.DoublePolygon;
import SpineJ.Tools.Graph.EdgeGraph;
import SpineJ.Tools.Graph.SeedGraph;
import SpineJ.Tools.QuadTree.point.PointQuadTree;
import ij.process.ByteProcessor;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Incremental Delaunay Triangulation
 *
 * @author Original : Java-code by X.Philippeau - Pseudo-code by Guibas and Stolfi, modified by Florian Levet
 *
 * @see Primitives for the Manipulation of General Subdivisions
 *      and the Computation of Voronoi Diagrams (Leonidas Guibas,Jorge Stolfi)
 */
public class DelaunayT {

    // starting edge for walk (see locate() method)
    private QuadEdge startingEdge = null;
    // list of quadEdge belonging to Delaunay triangulation
    private List<QuadEdge> quadEdge = new ArrayList<QuadEdge>();
    protected CatmullCurve[] m_contours = null;
    public PointQuadTree<PointT> m_treeTriangles = null;
    public PointQuadTree<SeedT> m_treeGraph = null;
    public ArrayList < PointT > m_allPoints = new ArrayList < PointT >();
    public ArrayList<TriangleT> m_triangles2 = new ArrayList<TriangleT>();
    public ArrayList<Point2D.Double[]> m_edges = new ArrayList<Point2D.Double[]>(), m_triangles = new ArrayList<Point2D.Double[]>(), m_voronoi = new ArrayList<Point2D.Double[]>();
    public ArrayList<Line2D.Double> m_skeleton = new ArrayList<Line2D.Double>();
    public ArrayList< SeedGraph > m_seedsSkel = new ArrayList< SeedGraph >();
    public ArrayList< EdgeGraph > m_edgesSkel = new ArrayList< EdgeGraph >();

    public ArrayList<CircleT> m_circles = new ArrayList<CircleT>();

    protected double m_areaSpine = 40.;

    

    // Bounding box of the triangulation

    class BoundingBox {

        double minx, miny, maxx, maxy;
        Point2D.Double a = new Point2D.Double(); // lower left
        Point2D.Double b = new Point2D.Double(); // lower right
        Point2D.Double c = new Point2D.Double(); // upper right
        Point2D.Double d = new Point2D.Double(); // upper left
    }
    private BoundingBox bbox = new BoundingBox();

    /**
     * Constuctor:
     */
    public DelaunayT() {

        bbox.minx = Double.MAX_VALUE;
        bbox.maxx = Double.MIN_VALUE;
        bbox.miny = Double.MAX_VALUE;
        bbox.maxy = Double.MIN_VALUE;

        // create the QuadEdge graph of the bounding box
        QuadEdge ab = QuadEdge.makeEdge(bbox.a, bbox.b);
        QuadEdge bc = QuadEdge.makeEdge(bbox.b, bbox.c);
        QuadEdge cd = QuadEdge.makeEdge(bbox.c, bbox.d);
        QuadEdge da = QuadEdge.makeEdge(bbox.d, bbox.a);
        QuadEdge.splice(ab.sym(), bc);
        QuadEdge.splice(bc.sym(), cd);
        QuadEdge.splice(cd.sym(), da);
        QuadEdge.splice(da.sym(), ab);

        this.startingEdge = ab;
    }
    
    public void execute( int _w, int _h, ArrayList < ArrayList < Point2D.Double > > _points ){
        m_treeTriangles = new PointQuadTree< PointT >(new Point2D.Double(0, 0), new Point2D.Double(_w, _h), 8, 6);
        for( ArrayList < Point2D.Double > apoints : _points ){
            for( Point2D.Double point : apoints ){
                PointT p = new PointT( point.x, point.y );
                insertPoint( p );
                m_treeTriangles.insert( p.x, p.y, p );
                m_allPoints.add( p );
            }
        }
        execute( _w, _h );  
    }
    
    public void execute( ArrayList < Point2D.Double > _points, int _w, int _h ){
        m_treeTriangles = new PointQuadTree< PointT >(new Point2D.Double(0, 0), new Point2D.Double(_w, _h), 8, 6);
        for( Point2D.Double point : _points ){
            PointT p = new PointT( point.x, point.y );
            insertPoint( p );
            m_treeTriangles.insert( p.x, p.y, p );
            m_allPoints.add( p );
        }
        execute( _w, _h );  
    }
    
    public void execute( int _w, int _h ){
        // do not process edges pointing to/from surrouding triangle
        // --> mark them as already computed
        for (QuadEdge q : this.quadEdge) {
            q.mark = false;
            q.sym().mark = false;
            if (q.orig() == bbox.a || q.orig() == bbox.b || q.orig() == bbox.c || q.orig() == bbox.d) {
                q.mark = true;
            }
            if (q.dest() == bbox.a || q.dest() == bbox.b || q.dest() == bbox.c || q.dest() == bbox.d) {
                q.sym().mark = true;
            }
        }
        
        // compute the 2 triangles associated to each quadEdge
        for (QuadEdge qe : quadEdge) {
            // first triangle
            QuadEdge q1 = qe;
            QuadEdge q2 = q1.lnext();
            QuadEdge q3 = q2.lnext();
            if (!q1.mark && !q2.mark && !q3.mark) {
                PointT[] points = new PointT[3];
                points[0] = m_treeTriangles.get(q1.orig(), 0.1).getElement();
                points[1] = m_treeTriangles.get(q2.orig(), 0.1).getElement();
                points[2] = m_treeTriangles.get(q3.orig(), 0.1).getElement();
                double x = (points[0].getX() + points[1].getX() + points[2].getX()) / 3., y = (points[0].getY() + points[1].getY() + points[2].getY()) / 3.;
                TriangleT tri = new TriangleT(points[0], points[1], points[2]);
                m_triangles2.add(tri);
                for (int n = 0; n < points.length; n++) {
                    ArrayList<TriangleT> tris = points[n].getTriangles();
                    for (int i = 0; i < tris.size(); i++) {
                        TriangleT t = tris.get(i);
                        if (tri.isNewNeighbor(t)) {
                            tri.addNeighbor(t);
                            t.addNeighbor(tri);
                        }
                    }
                }
            }

            // second triangle
            QuadEdge qsym1 = qe.sym();
            QuadEdge qsym2 = qsym1.lnext();
            QuadEdge qsym3 = qsym2.lnext();
            if (!qsym1.mark && !qsym2.mark && !qsym3.mark) {
                PointT[] points = new PointT[3];
                points[0] = m_treeTriangles.get(qsym1.orig(), 0.1).getElement();
                points[1] = m_treeTriangles.get(qsym2.orig(), 0.1).getElement();
                points[2] = m_treeTriangles.get(qsym3.orig(), 0.1).getElement();
                double x = (points[0].getX() + points[1].getX() + points[2].getX()) / 3., y = (points[0].getY() + points[1].getY() + points[2].getY()) / 3.;
                TriangleT tri = new TriangleT(points[0], points[1], points[2]);
                m_triangles2.add(tri);
                for (int n = 0; n < points.length; n++) {
                    ArrayList<TriangleT> tris = points[n].getTriangles();
                    for (int i = 0; i < tris.size(); i++) {
                        TriangleT t = tris.get(i);
                        if (tri.isNewNeighbor(t)) {
                            tri.addNeighbor(t);
                            t.addNeighbor(tri);
                        }
                    }
                }
            }

            // mark as used
            qe.mark = true;
            qe.sym().mark = true;
        }
        //computeTriangles();
        for( int i = 0; i < m_triangles2.size(); i++ )
            m_triangles2.get( i ).setMarked( false );
    }
    
    public void clearDatas(){
        m_treeTriangles.clear();
        m_allPoints.clear();
        quadEdge.clear();
        m_triangles.clear();
        //m_triangles2.clear();
    }

    public void execute( CatmullCurve[] _contours, int _w, int _h, TriangleT [][] _links, boolean _trimTriangles ) {
        m_contours = _contours;
        m_treeTriangles = new PointQuadTree<PointT>(new Point2D.Double(0, 0), new Point2D.Double(_w, _h), 8, 6);
        for (int l = 0; l < m_contours.length; l++) {
            PointT first = new PointT(m_contours[l].points.xpoints[0], m_contours[l].points.ypoints[0]);
            if( first.x < 0 ) first.x = 0;
            if( first.x >= _w ) first.x = _w - 1;
            if( first.y < 0 ) first.y = 0;
            if( first.y >= _h ) first.y = _h - 1;
            insertPoint(first);
            m_treeTriangles.insert(first.x, first.y, first);
            m_allPoints.add( first );
            PointT prec = first, current = null;
            for (int j = 1; j < m_contours[l].points.npoints - 1; j++) {//The last point is not added because it is the same as the first, addition causes problems with the delaunay triangulation
                current = new PointT(m_contours[l].points.xpoints[j], m_contours[l].points.ypoints[j]);
                if( current.x < 0 ) current.x = 0;
                if( current.x > _w ) current.x = _w;
                if( current.y < 0 ) current.y = 0;
                if( current.y > _h ) current.y = _h;
                insertPoint(current);
                m_treeTriangles.insert(current.x, current.y, current);
                m_allPoints.add( current );

                prec.setNext(current);
                current.setPrec(prec);
                prec = current;
            }
            current.setNext(first);
            first.setPrec(current);
        }

        // do not process edges pointing to/from surrouding triangle
        // --> mark them as already computed
        for (QuadEdge q : this.quadEdge) {
            q.mark = false;
            q.sym().mark = false;
            if (q.orig() == bbox.a || q.orig() == bbox.b || q.orig() == bbox.c || q.orig() == bbox.d) {
                q.mark = true;
            }
            if (q.dest() == bbox.a || q.dest() == bbox.b || q.dest() == bbox.c || q.dest() == bbox.d) {
                q.sym().mark = true;
            }
        }

        // compute the 2 triangles associated to each quadEdge
        for (QuadEdge qe : quadEdge) {
            // first triangle
            QuadEdge q1 = qe;
            QuadEdge q2 = q1.lnext();
            QuadEdge q3 = q2.lnext();
            if (!q1.mark && !q2.mark && !q3.mark) {
                PointT[] points = new PointT[3];
                points[0] = m_treeTriangles.get(q1.orig(), 0.1).getElement();
                points[1] = m_treeTriangles.get(q2.orig(), 0.1).getElement();
                points[2] = m_treeTriangles.get(q3.orig(), 0.1).getElement();
                double x = (points[0].getX() + points[1].getX() + points[2].getX()) / 3., y = (points[0].getY() + points[1].getY() + points[2].getY()) / 3.;
                if (isInNeuron(x, y, m_contours)) {
                    TriangleT tri = new TriangleT(points[0], points[1], points[2]);
                    m_triangles2.add(tri);
                    for (int n = 0; n < points.length; n++) {
                        ArrayList<TriangleT> tris = points[n].getTriangles();
                        for (int i = 0; i < tris.size(); i++) {
                            TriangleT t = tris.get(i);
                            if (tri.isNewNeighbor(t)) {
                                tri.addNeighbor(t);
                                t.addNeighbor(tri);
                            }
                        }
                    }
                    //Determine which pixels are inside the triangle
                    int xmin = Integer.MAX_VALUE, xmax = Integer.MIN_VALUE, ymin = Integer.MAX_VALUE, ymax = Integer.MIN_VALUE;
                    for( int n = 0; n < points.length; n++ ){
                        if( points[n].x < xmin )
                            xmin = ( int )points[n].x;
                        if( points[n].x > xmax )
                            xmax = ( int )points[n].x;
                        if( points[n].y < ymin )
                            ymin = ( int )points[n].y;
                        if( points[n].y > ymax )
                            ymax = ( int )points[n].y;
                    }
                    if(_links != null){
                        for( int x2 = xmin; x2 <= xmax; x2++ )
                            for( int y2 = ymin; y2 <= ymax; y2++ ){
                                if( tri.contains( new Point2D.Double( x2, y2 ) ) )
                                    _links[x2][y2] = tri;
                            }
                    }
                }
            }

            // second triangle
            QuadEdge qsym1 = qe.sym();
            QuadEdge qsym2 = qsym1.lnext();
            QuadEdge qsym3 = qsym2.lnext();
            if (!qsym1.mark && !qsym2.mark && !qsym3.mark) {
                PointT[] points = new PointT[3];
                points[0] = m_treeTriangles.get(qsym1.orig(), 0.1).getElement();
                points[1] = m_treeTriangles.get(qsym2.orig(), 0.1).getElement();
                points[2] = m_treeTriangles.get(qsym3.orig(), 0.1).getElement();
                double x = (points[0].getX() + points[1].getX() + points[2].getX()) / 3., y = (points[0].getY() + points[1].getY() + points[2].getY()) / 3.;
                if (isInNeuron(x, y, m_contours)) {
                    TriangleT tri = new TriangleT(points[0], points[1], points[2]);
                    m_triangles2.add(tri);
                    for (int n = 0; n < points.length; n++) {
                        ArrayList<TriangleT> tris = points[n].getTriangles();
                        for (int i = 0; i < tris.size(); i++) {
                            TriangleT t = tris.get(i);
                            if (tri.isNewNeighbor(t)) {
                                tri.addNeighbor(t);
                                t.addNeighbor(tri);
                            }
                        }
                    }

                    //Determine which pixels are inside the triangle
                    int xmin = Integer.MAX_VALUE, xmax = Integer.MIN_VALUE, ymin = Integer.MAX_VALUE, ymax = Integer.MIN_VALUE;
                    for( int n = 0; n < points.length; n++ ){
                        if( points[n].x < xmin )
                            xmin = ( int )points[n].x;
                        if( points[n].x > xmax )
                            xmax = ( int )points[n].x;
                        if( points[n].y < ymin )
                            ymin = ( int )points[n].y;
                        if( points[n].y > ymax )
                            ymax = ( int )points[n].y;
                    }
                    if(_links != null){
                    for( int x2 = xmin; x2 <= xmax; x2++ )
                        for( int y2 = ymin; y2 <= ymax; y2++ ){
                            if( tri.contains( new Point2D.Double( x2, y2 ) ) )
                                _links[x2][y2] = tri;
                        }
                    }
                }
            }

            // mark as used
            qe.mark = true;
            qe.sym().mark = true;
        }
        computeTriangles();

        //Trimming
        for( TriangleT t : m_triangles2 )
            t.setTrimmed( false );
        if( _trimTriangles ){
            for( TriangleT t : m_triangles2 ){
                if( !t.isTerminal() ) continue;
                //t.setMarked( true );
                ArrayList < PointT > points = t.getPoints();
                TriangleT neigh = t.getNeighbors().get( 0 );
                CircleT circle = t.getCircleSharedEdge( neigh );
                ArrayList < CircleT > cs = new ArrayList < CircleT >();
                cs.add(circle);
                boolean allInside = true;
                Iterator < PointT > it = points.iterator();
                while( it.hasNext() && allInside )
                    allInside = allInside && circle.contains( it.next() );
                if( !neigh.isTransitionnal() )
                    t.setTrimmed( allInside );
                else{
                    while( allInside ){
                        t.setTrimmed( true );
                        TriangleT tmp = t;
                        t = neigh;
                        neigh = t.getOtherTriangle( tmp );
                        circle = t.getCircleSharedEdge( neigh );
                        cs.add(circle);
                        PointT p = t.getA();
                        if( !points.contains( p ) )
                            points.add( p );
                        p = t.getB();
                        if( !points.contains( p ) )
                            points.add( p );
                        p = t.getC();
                        if( !points.contains( p ) )
                            points.add( p );
                        allInside = true;
                        it = points.iterator();
                        while( it.hasNext() && allInside )
                            allInside = allInside && circle.contains( it.next() );
                        if( !neigh.isTransitionnal() ){
                            if( allInside )
                                t.setTrimmed( true );
                            allInside = false;
                        }
                    }
                    if( !cs.isEmpty()){m_circles.add(cs.get(cs.size()-1));
                    }
                }
            }
        }

        //Graph Construction
        m_treeGraph = new PointQuadTree < SeedT > ( new Point2D.Double(0, 0), new Point2D.Double(_w, _h), 8, 6 );
        for (int i = 0; i < m_triangles2.size(); i++) {
            TriangleT tri = m_triangles2.get(i);
            if (!tri.isJuntionnal()) {
                continue;
            }
            for (int j = 0; j < tri.getNeighbors().size(); j++) {
                TriangleT prec = tri;
                TriangleT cur = tri.getNeighbors().get(j);
                ArrayList < TriangleT > triangles = new ArrayList < TriangleT >(), trianglesTrimmed = new ArrayList < TriangleT >();
                if (cur.isMarked()) {
                    cur = null;
                }
                Point2D.Double precPosition = prec.getBarycenter(), curPosition = null;
                while (cur != null) {
                    TriangleT tmp = cur;
                    curPosition = cur.getCenterSharedEdge(prec);
                    if( prec.isTrimmed() )
                        trianglesTrimmed.add( prec );
                    else if( !prec.isJuntionnal() )
                        triangles.add( prec );
                    precPosition = curPosition;
                    if (cur.isTransitionnal()) {
                        cur.setMarked(true);
                        cur = cur.getOtherTriangle(prec);
                    } else {
                        curPosition = cur.getBarycenter();
                        cur = null;
                    }
                    prec = tmp;
                }
                if( prec.isTrimmed() )
                    trianglesTrimmed.add( prec );
                else if( !prec.isJuntionnal() )
                    triangles.add( prec );
                if( tri != prec ){
                    SeedT s1 = new SeedT( tri.getBarycenter() );
                    s1.setTriangle( tri );
                    SeedT s2 = new SeedT( prec.getBarycenter() );
                    s2.setTriangle( prec );
                    SeedT tmp = m_treeGraph.insertOrGet( s1.getX(), s1.getY(), s1, 0.1 ).getElement();
                    if (s1 == tmp) {
                        m_seedsSkel.add( s1 );
                    } else {
                        s1 = tmp;
                    }
                    tmp = m_treeGraph.insertOrGet( s2.getX(), s2.getY(), s2, 0.1 ).getElement();
                    if (s2 == tmp) {
                        m_seedsSkel.add(s2);
                    } else {
                        s2 = tmp;
                    }
                    EdgeT edge = new EdgeT(s1, s2);
                    if( !s1.contains( edge ) ){
                        edge.setTriangles( triangles );
                        edge.setTrianglesTrimmed( trianglesTrimmed );
                        s1.addEdge( edge );
                        s2.addEdge( edge );
                        m_edgesSkel.add(edge);
                        edge.recomputeSkeleton();
                    }
                }
            }
        }
        
        //Now we remove edges that contains traingles that were all trimmed, i.e. this is a trimmed edge
        ArrayList < EdgeGraph > toRemove = new ArrayList < EdgeGraph >();
        for (int i = 0; i < m_seedsSkel.size(); i++){
            SeedT seed = ( SeedT )m_seedsSkel.get( i );
            if( seed.degree() != 3 ) continue;
            //Determined how many of its edges are trimmed
            ArrayList < EdgeT > edges = new ArrayList < EdgeT >(), edgesTrimmed = new ArrayList < EdgeT >();
            for( int j = 0; j < seed.getEdges().size(); j++ ){
                EdgeT tmp = seed.getEdges().get( j );
                if( tmp.isEdgeTrimmed() )
                    edgesTrimmed.add( tmp );
                else
                    edges.add( tmp );
            }
            if( edgesTrimmed.size() == 2 ){
                EdgeT e = edges.get( 0 );
                EdgeT trim1 = edgesTrimmed.get( 0 ), trim2 = edgesTrimmed.get( 1 );
                TriangleT t = e.getNeighborTriangleFromTriangleSeed( seed );
                seed.addTrianglesTrimmed( trim1.getTrianglesTrimmed() );
                seed.addTrianglesTrimmed( trim2.getTrianglesTrimmed() );
                e.addTrianglesTrimmed( trim1.getTrianglesTrimmed() );
                e.addTrianglesTrimmed( trim2.getTrianglesTrimmed() );
                toRemove.add( trim1 );
                toRemove.add( trim2 );
            }
            //if one edge is trimmed, we remove it and fusion the two other edges in one with the triangles trimmed from the removed edge added
            else if(edgesTrimmed.size() == 1)
            {
                EdgeT trim = edgesTrimmed.get( 0 );
                EdgeT e1 = edges.get( 0 ), e2 = edges.get( 1 );
                TriangleT t1 = e1.getNeighborTriangleFromTriangleSeed( seed ), t2 = e2.getNeighborTriangleFromTriangleSeed( seed );
                Point2D.Double p1 = t1.getCenterSharedEdge( seed.getTriangle() ), p2 = t2.getCenterSharedEdge( seed.getTriangle() );
                seed.getTriangle().setBarycenter( new Point2D.Double( ( p1.x + p2.x ) / 2., ( p1.y + p2.y ) / 2. ) );
                seed.setLocation( new Point2D.Double( ( p1.x + p2.x ) / 2., ( p1.y + p2.y ) / 2. ) );
                seed.addTrianglesTrimmed( trim.getTrianglesTrimmed() );
                e1.recomputeSkeleton();
                e2.recomputeSkeleton();
                e1.addTrianglesTrimmed( trim.getTrianglesTrimmed() );
                toRemove.add( trim );
            }
        }
       for( int i = 0; i < toRemove.size(); i++ ){
            EdgeGraph e = toRemove.get( i );
            EdgeT edge = ( EdgeT )e;
            edge.getV1().removeEdge( edge );
            edge.getV2().removeEdge( edge );
            m_edgesSkel.remove( e );
        }
        for( int i = m_seedsSkel.size() - 1; i >= 0; i-- )
            if( m_seedsSkel.get( i ).degree() == 0 )
                m_seedsSkel.remove( i );
        
        for( int i = 0; i < m_triangles2.size(); i++ )
            m_triangles2.get( i ).setMarked( false );
        //Removing these edges we can have graph seed that have only 2 edges
        //We remove these seeds and merge their 2 edges in one
        boolean removedOneSeed = true;
        while(removedOneSeed){
            removedOneSeed = false;
            for (int i = 0; i < m_seedsSkel.size() && !removedOneSeed; i++){
                SeedT seed = ( SeedT )m_seedsSkel.get( i );
                if( seed.degree() != 2 ) continue;
                EdgeT e1 = seed.getEdges().get( 0 ), e2 = seed.getEdges().get( 1 );
                SeedT s1 = e1.getOtherSeed( seed ), s2 = e2.getOtherSeed( seed );
                EdgeT edge = new EdgeT(s1, s2);
                edge.setTriangles( e1.getTriangles() );
                edge.addTriangles( e2.getTriangles() );
                edge.addTriangle( seed.getTriangle() );
                edge.setTrianglesTrimmed( e1.getTrianglesTrimmed());
                edge.addTrianglesTrimmed(e2.getTrianglesTrimmed());
                edge.addTrianglesTrimmed(seed.getTriangleTrimmed());
                s1.addEdge( edge );
                s2.addEdge( edge );
                m_edgesSkel.add(edge);
                edge.recomputeSkeletonWithMarked();
                
                e1.getV1().removeEdge( e1 );
                e1.getV2().removeEdge( e1 );
                m_edgesSkel.remove( e1 );
                e2.getV1().removeEdge( e2 );
                e2.getV2().removeEdge( e2 );
                m_edgesSkel.remove( e2 );
                m_seedsSkel.remove( i );
                removedOneSeed = true;
            }
        }
        for( int i = m_seedsSkel.size() - 1; i >= 0; i-- )
            if( m_seedsSkel.get( i ).degree() == 0 )
                m_seedsSkel.remove( i );
        
        for(EdgeGraph eg : m_edgesSkel)
        {
            EdgeT e = (EdgeT)eg;
            ArrayList <TriangleT> tmp = removeDuplicates(e.getTriangles());
            if(tmp.size() != e.getTriangles().size())
                e.setTriangles(tmp);
            tmp = removeDuplicates(e.getTrianglesTrimmed());
            if(tmp.size() != e.getTrianglesTrimmed().size())
                e.setTrianglesTrimmed(tmp);
        }

        //If the skeleton is only one edge with two extremal triangle, the algorithm fails
        //Thus we add a special case to threat this particular case
        if( m_seedsSkel.isEmpty() ){
            int cpt = 0;
            for (int i = 0; i < m_triangles2.size(); i++) {
                TriangleT tri = m_triangles2.get(i);
                if ( tri.isTerminal() )
                    cpt++;
            }
            if( cpt == 2 ){
                TriangleT cur = null;
                ArrayList < TriangleT > triangles = new ArrayList < TriangleT >(), trianglesTrimmed = new ArrayList < TriangleT >();
                ArrayList<Point2D.Double> skeleton = new ArrayList<Point2D.Double>();
                for (int i = 0; i < m_triangles2.size() && cur == null; i++) {
                    TriangleT tri = m_triangles2.get(i);
                    if ( tri.isTerminal() )
                        cur = tri;
                }
                SeedT s1 = new SeedT( cur.getBarycenter() );
                s1.setTriangle( cur );
                if( cur.isTrimmed() )
                    trianglesTrimmed.add( cur );
                else{
                    triangles.add( cur );
                    skeleton.add( cur.getBarycenter() );
                }
                m_seedsSkel.add( s1 );
                
                TriangleT next = cur.getNeighbors().get( 0 );
                while( next != null ){
                    cur.setMarked( true );
                    if( cur.isTrimmed() )
                        trianglesTrimmed.add( cur );
                    else{
                        triangles.add( cur );
                        skeleton.add( cur.getCenterSharedEdge( next ) );
                    }
                    cur = next;
                    next = next.getOtherTriangle( cur );
                }
                
                SeedT s2 = new SeedT( cur.getBarycenter() );
                s2.setTriangle( cur );
                if( cur.isTrimmed() )
                    trianglesTrimmed.add( cur );
                else{
                    triangles.add( cur );
                    skeleton.add( cur.getBarycenter() );
                }
                m_seedsSkel.add( s2 );
                EdgeT edge = new EdgeT(s1, s2);
                PointT.filter( skeleton );
                edge.setSkel(skeleton);
                edge.setTriangles( triangles );
                edge.setTrianglesTrimmed( trianglesTrimmed );
                s1.addEdge( edge );
                s2.addEdge( edge );
                //edge.recomputeSkeleton();
                m_edgesSkel.add(edge);
            }
        }
        for (int i = 0; i < m_seedsSkel.size(); i++) {
            m_seedsSkel.get(i).setId(i);
        }
        for( int i = 0; i < m_edgesSkel.size(); i++ ){
            EdgeT edge = ( EdgeT )m_edgesSkel.get( i );
            edge.setId( i );
            edge.setDendrite( false );
            edge.computeArea();
        }
        for (TriangleT m_triangles21 : m_triangles2) {
            m_triangles21.setMarked(false);
        }
        
    }

    public void determineSpines( SeedT _seed, double _area ){
        double tmp = _area + _seed.getArea();
        for( int i = 0; i < _seed.getEdges().size(); i++ ){
            EdgeT edge = ( EdgeT )_seed.getEdges().get( i );
            //if( edge.done() ) continue;
            
            double area = tmp + edge.getArea();
            _seed.getTriangle().setMarked( true );
            edge.setAsDendrite( true );
            edge.setDendrite( true );
            if( tmp + edge.getArea() < m_areaSpine ){
                SeedT other = edge.getOtherSeed( _seed );
                determineSpines( other, area );
            }
        }
    }

    /**
     * update the dimension of the bounding box
     *
     * @param minx,miny,maxx,maxy summits of the rectangle
     */
    public void setBoundigBox(double minx, double miny, double maxx, double maxy) {
        // update saved values
        bbox.minx = minx;
        bbox.maxx = maxx;
        bbox.miny = miny;
        bbox.maxy = maxy;

        // extend the bounding-box to surround min/max
        double centerx = (minx + maxx) / 2.;
        double centery = (miny + maxy) / 2.;
        double x_min = ((minx - centerx - 1.) * 10. + centerx);
        double x_max = ((maxx - centerx + 1.) * 10. + centerx);
        double y_min = ((miny - centery - 1.) * 10. + centery);
        double y_max = ((maxy - centery + 1.) * 10. + centery);

        // set new positions
        bbox.a.x = x_min;
        bbox.a.y = y_min;
        bbox.b.x = x_max;
        bbox.b.y = y_min;
        bbox.c.x = x_max;
        bbox.c.y = y_max;
        bbox.d.x = x_min;
        bbox.d.y = y_max;
    }

    // update the size of the bounding box (cf locate() method)
    private void updateBoundigBox(Point2D.Double p) {
        double minx = Math.min(bbox.minx, p.x);
        double maxx = Math.max(bbox.maxx, p.x);
        double miny = Math.min(bbox.miny, p.y);
        double maxy = Math.max(bbox.maxy, p.y);
        setBoundigBox(minx, miny, maxx, maxy);
        //System.out.println("resizing bounding-box: "+minx+" "+miny+" "+maxx+" "+maxy);
    }

    /**
     * Returns an edge e of the triangle containing the point p
     * (Guibas and Stolfi)
     *
     * @param p the point to localte
     * @return the edge of the triangle
     */
    private QuadEdge locate(Point2D.Double p) {

        /* outside the bounding box ? */
        if (p.x < bbox.minx || p.x > bbox.maxx || p.y < bbox.miny || p.y > bbox.maxy) {
            updateBoundigBox(p);
        }

        QuadEdge e = startingEdge;
        while (true) {
            /* duplicate point ? */
            if (p.x == e.orig().x && p.y == e.orig().y) {
                return e;
            }
            if (p.x == e.dest().x && p.y == e.dest().y) {
                return e;
            }

            /* walk */
            if (QuadEdge.isAtRightOf(e, p)) {
                e = e.sym();
            } else if (!QuadEdge.isAtRightOf(e.onext(), p)) {
                e = e.onext();
            } else if (!QuadEdge.isAtRightOf(e.dprev(), p)) {
                e = e.dprev();
            } else {
                return e;
            }
        }
    }

    /**
     *  Inserts a new point into a Delaunay triangulation
     *  (Guibas and Stolfi)
     *
     * @param p the point to insert
     */
    public void insertPoint(Point2D.Double p) {
        QuadEdge e = locate(p);

        // point is a duplicate -> nothing to do
        if (p.x == e.orig().x && p.y == e.orig().y) {
            return;
        }
        if (p.x == e.dest().x && p.y == e.dest().y) {
            return;
        }

        // point is on an existing edge -> remove the edge
        if (QuadEdge.isOnLine(e, p)) {
            e = e.oprev();
            this.quadEdge.remove(e.onext().sym());
            this.quadEdge.remove(e.onext());
            QuadEdge.deleteEdge(e.onext());
        }

        // Connect the new point to the vertices of the containing triangle
        // (or quadrilateral in case of the point is on an existing edge)
        QuadEdge base = QuadEdge.makeEdge(e.orig(), p);
        this.quadEdge.add(base);

        QuadEdge.splice(base, e);
        this.startingEdge = base;
        do {
            base = QuadEdge.connect(e, base.sym());
            this.quadEdge.add(base);
            e = base.oprev();
        } while (e.lnext() != startingEdge);

        // Examine suspect edges to ensure that the Delaunay condition is satisfied.
        do {
            QuadEdge t = e.oprev();

            if (QuadEdge.isAtRightOf(e, t.dest())
                    && QuadEdge.inCircle(e.orig(), t.dest(), e.dest(), p)) {
                // flip triangles
                QuadEdge.swapEdge(e);
                e = e.oprev();
            } else if (e.onext() == startingEdge) {
                return; // no more suspect edges
            } else {
                e = e.onext().lprev();  // next suspect edge
            }
        } while (true);
    }

    public void computeAll() {
        computeEdges();
        computeTriangles();
        computeVoronoi();
    }

    /**
     *  compute and return the list of edges
     */
    public void computeEdges() {
        m_edges.clear();
        // do not return edges pointing to/from surrouding triangle
        for (QuadEdge q : this.quadEdge) {
            if (q.orig() == bbox.a || q.orig() == bbox.b || q.orig() == bbox.c || q.orig() == bbox.d) {
                continue;
            }
            if (q.dest() == bbox.a || q.dest() == bbox.b || q.dest() == bbox.c || q.dest() == bbox.d) {
                continue;
            }
            m_edges.add(new Point2D.Double[]{q.orig(), q.dest()});
        }
    }

    /**
     *  compute and return the list of triangles
     */
    public void computeTriangles() {
        m_triangles.clear();

        // do not process edges pointing to/from surrouding triangle
        // --> mark them as already computed
        for (QuadEdge q : this.quadEdge) {
            q.mark = false;
            q.sym().mark = false;
            if (q.orig() == bbox.a || q.orig() == bbox.b || q.orig() == bbox.c || q.orig() == bbox.d) {
                q.mark = true;
            }
            if (q.dest() == bbox.a || q.dest() == bbox.b || q.dest() == bbox.c || q.dest() == bbox.d) {
                q.sym().mark = true;
            }
        }

        // compute the 2 triangles associated to each quadEdge
        for (QuadEdge qe : quadEdge) {
            // first triangle
            QuadEdge q1 = qe;
            QuadEdge q2 = q1.lnext();
            QuadEdge q3 = q2.lnext();
            if (!q1.mark && !q2.mark && !q3.mark) {
                m_triangles.add(new Point2D.Double[]{q1.orig(), q2.orig(), q3.orig()});
            }

            // second triangle
            QuadEdge qsym1 = qe.sym();
            QuadEdge qsym2 = qsym1.lnext();
            QuadEdge qsym3 = qsym2.lnext();
            if (!qsym1.mark && !qsym2.mark && !qsym3.mark) {
                m_triangles.add(new Point2D.Double[]{qsym1.orig(), qsym2.orig(), qsym3.orig()});
            }

            // mark as used
            qe.mark = true;
            qe.sym().mark = true;
        }
    }

    public void computeVoronoi() {
        m_voronoi.clear();

        // do not process edges pointing to/from surrouding triangle
        // --> mark them as already computed
        for (QuadEdge q : this.quadEdge) {
            q.mark = false;
            q.sym().mark = false;
            if (q.orig() == bbox.a || q.orig() == bbox.b || q.orig() == bbox.c || q.orig() == bbox.d) {
                q.mark = true;
            }
            if (q.dest() == bbox.a || q.dest() == bbox.b || q.dest() == bbox.c || q.dest() == bbox.d) {
                q.sym().mark = true;
            }
        }

        for (QuadEdge qe : quadEdge) {

            // walk throught left and right region
            for (int b = 0; b <= 1; b++) {
                QuadEdge qstart = (b == 0) ? qe : qe.sym();
                if (qstart.mark) {
                    continue;
                }

                // new region start
                List<Point2D.Double> poly = new ArrayList<Point2D.Double>();

                // walk around region
                QuadEdge qregion = qstart;
                while (true) {
                    qregion.mark = true;

                    // compute CircumCenter if needed
                    if (qregion.rot().orig() == null) {
                        QuadEdge q1 = qregion;
                        Point2D.Double p0 = q1.orig();
                        QuadEdge q2 = q1.lnext();
                        Point2D.Double p1 = q2.orig();
                        QuadEdge q3 = q2.lnext();
                        Point2D.Double p2 = q3.orig();

                        double ex = p1.x - p0.x, ey = p1.y - p0.y;
                        double nx = p2.y - p1.y, ny = p1.x - p2.x;
                        double dx = (p0.x - p2.x) * 0.5, dy = (p0.y - p2.y) * 0.5;
                        double s = (ex * dx + ey * dy) / (ex * nx + ey * ny);
                        double cx = (p1.x + p2.x) * 0.5 + s * nx;
                        double cy = (p1.y + p2.y) * 0.5 + s * ny;

                        Point2D.Double p = new Point2D.Double(cx, cy);
                        qregion.rot().setOrig(p);
                    }

                    poly.add(qregion.rot().orig());

                    qregion = qregion.onext();
                    if (qregion == qstart) {
                        break;
                    }
                }

                // add region to output list
                m_voronoi.add(poly.toArray(new Point2D.Double[0]));
            }
        }
    }

    protected boolean isInNeuron(double x, double y, CatmullCurve[] polygons) {
        DoublePolygon neuron = polygons[0].points;
        if (!neuron.contains(x, y)) {
            return false;
        }
        for (int i = 1; i < polygons.length; i++) {
            if (polygons[i].points.contains(x, y)) {
                return false; //the point x,y is inside a hole of the neuron
            }
        }
        return true;
    }
    
    public void filter( ByteProcessor _bp ){
        ArrayList < Integer > toErase = new ArrayList < Integer >();
        for( int n = 0; n < m_triangles2.size(); n++ ){
            TriangleT t = m_triangles2.get( n );
            boolean keep = true;
            keep = _bp.getPixel( ( int )t.getA().getX(), ( int )t.getA().getY() ) == 255;
            keep = keep && ( _bp.getPixel( ( int )t.getB().getX(), ( int )t.getB().getY() ) == 255 );
            keep = keep && ( _bp.getPixel( ( int )t.getC().getX(), ( int )t.getC().getY() ) == 255 );
            if( !keep )
                toErase.add( n );
        }
        for( int n = toErase.size() - 1; n >= 0; n-- )
            m_triangles2.remove( ( int )toErase.get( n ) );
    }
    
    // Function to remove duplicates from an ArrayList 
    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list) 
    { 
        // Create a new LinkedHashSet 
        Set<T> set = new LinkedHashSet<>(); 
        // Add the elements to set 
        set.addAll(list);
        ArrayList <T> nl = new ArrayList<T>();
        nl.addAll(set);
        return nl;
    } 
}
