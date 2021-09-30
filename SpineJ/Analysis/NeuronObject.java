/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Analysis;

import SpineJ.Tools.Graph.*;
import SpineJ.Tools.Delaunay.EdgeT;
import SpineJ.Tools.Delaunay.PointT;
import SpineJ.Tools.Delaunay.SeedT;
import SpineJ.Tools.Delaunay.TriangleT;
import SpineJ.Tools.Geometry.CatmullCurve;
import SpineJ.Tools.Geometry.DoublePolygon;
import SpineJ.Tools.Geometry.QuickHull;
import ij.gui.EllipseRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Wand;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Florian Levet
 */
public class NeuronObject {
    protected double m_area, m_perimeter, m_shapeIndex;
    protected boolean m_isDendrite, m_selected;
    protected Color m_color;
    protected Line2D.Double[] m_shape = null;
    protected ArrayList < EdgeGraph > m_edges = null;
    protected Rectangle2D.Double m_bbox = null;//BoundingBox
    protected QuickHull m_hull = null;
    protected CatmullCurve [] m_skeletons = null;
    protected EdgeWeightedGraph m_graph;
    protected BreadthFirstPaths m_path = null;
    public ArrayList < Point2D.Double > m_skeletonPath = null;
    public ArrayList < Point2D.Double > m_skeletonLongest = null;
    protected DoublePolygon m_outline = null;
    protected Line2D m_spineBasis = null, m_separationHeadNeck = null;
    protected Roi m_discreteShape = null;
    protected ArrayList < Point2D.Double > m_neck = null, m_head = null;
    protected /*PolygonRoi*/EllipseRoi m_shapeHead = null;
    protected ArrayList < NeckShape > m_neckAnalysis = null;
    protected double [] m_infosLength = null;
    protected Point2D.Double m_pointDelimination = null;
    
    //For Tiago Analysis
    public Point2D.Double m_intenseP = null, m_precP = null, m_succP = null, m_proj = null;
    public double intIntensityChan1 = 0., intIntensityChan2 = 0., intIntensityChan3 = 0.; 

    public NeuronObject( ArrayList < EdgeGraph > _edges, boolean _isDendrite, double _min, double _max ){
        m_edges = _edges;
        m_isDendrite = _isDendrite;
        m_selected = true;

        ArrayList < Line2D > lines = new ArrayList < Line2D >();
        m_area = 0.;
        int cpt = 0;
        for( int n = 0; n < m_edges.size(); n++ ){
            EdgeT edge = ( EdgeT )m_edges.get( n );
            for( TriangleT t : edge.getTriangles() ){
                Line2D.Double l = addCorrectSides( t, lines );
                if( l != null ){
                    m_spineBasis = l;
                    cpt++;
                }
                if( t.isMarked() != m_isDendrite ) continue;
                m_area += t.getArea();
            }
            for( TriangleT t : edge.getTrianglesTrimmed() ){
                Line2D.Double l = addCorrectSides( t, lines );
                if( l != null ){
                    m_spineBasis = l;
                    cpt++;
                }
                if( t.isMarked() != m_isDendrite ) continue;
                m_area += t.getArea();
            }
            TriangleT t = edge.getV1().getTriangle();
            if( t.isMarked() == m_isDendrite ){
                Line2D.Double l = addCorrectSides( t, lines );
                if( l != null ){
                    m_spineBasis = l;
                    cpt++;
                }
                m_area += t.getArea();
            }
            t = edge.getV2().getTriangle();
            if( t.isMarked() == m_isDendrite ){
                Line2D.Double l = addCorrectSides( t, lines );
                if( l != null ){
                    m_spineBasis = l;
                    cpt++;
                }
                m_area += t.getArea();
            }
        }
        
        //Determine duplicates in the lines, brute force for now
        /*for(int i = 0; i < lines.size(); i++)
            for(int j = i+1; j < lines.size(); j++){
                
            }*/
        m_shape = new Line2D.Double[lines.size()];
        m_shape = lines.toArray( m_shape );
        
        if( m_isDendrite )
                m_color = Color.yellow;
            else{
                /*if( m_area < _min )
                    m_color = Color.CYAN;
                else if( m_area < _max )*/
                    m_color = Color.RED;
                /*else
                    m_color = Color.MAGENTA;*/
            }
        
        if( !_isDendrite ){
            lines.remove( m_spineBasis );
            int size = lines.size();
            ArrayList < Point2D > outline = new ArrayList < Point2D >();
            //Find the first edge that contains one of the point of the spineBasis
            Line2D lTmp = null;
            if( m_spineBasis != null )
                for( int n = 0; n < lines.size() && lTmp == null; n++ ){
                    Line2D line = lines.get( n );
                    if( line.getP1().equals( m_spineBasis.getP1() ) || line.getP1().equals( m_spineBasis.getP2() ) ){
                        lTmp = line;
                        outline.add( lTmp.getP1() );
                        outline.add( lTmp.getP2() );
                    }
                    if( line.getP2().equals( m_spineBasis.getP1() ) || line.getP2().equals( m_spineBasis.getP2() ) ){
                        lTmp = line;
                        outline.add( lTmp.getP2() );
                        outline.add( lTmp.getP1() );
                    }
                }
            else{
                lTmp = lines.get( 0 );
                outline.add( lTmp.getP1() );
                outline.add( lTmp.getP2() );
            }
            lines.remove( lTmp );
            int cur = 2;
            boolean found = true;
            while( cur < size && found ){
                found = false;
                int index = outline.size() - 1;
                lTmp = null;
                for( int n = 0; n < lines.size() && !found; n++ ){
                    lTmp = lines.get( n );
                    if( lTmp.getP1().equals( outline.get( index ) ) ){ 
                        found = true;
                        outline.add( lTmp.getP2() );
                    }
                    if( lTmp.getP2().equals( outline.get( index ) ) ){
                        found = true;
                        outline.add( lTmp.getP1() );
                    }
                }
                if( found )
                    lines.remove( lTmp );
            }
            double [] xs = new double[outline.size()], ys = new double[outline.size()];
            for( int n = 0; n < outline.size(); n++ ){
                Point2D p = outline.get( n );
                xs[n] = p.getX();
                ys[n] = p.getY();
            }
            m_outline = new DoublePolygon( xs, ys, xs.length );

            double bx, by, bw, bh;
            Line2D.Double l = m_shape[0];
            bx = l.x1; by = l.y1; bw = l.x1; bh = l.y1;
            for( int n = 0; n < m_shape.length; n++ ){
                l = m_shape[n];
                if( l.x1 < bx )
                    bx = l.x1;
                if( l.x1 > bw )
                    bw = l.x1;
                if( l.y1 < by )
                    by = l.y1;
                if( l.y1 > bh )
                    bh = l.y1;
                if( l.x2 < bx )
                    bx = l.x2;
                if( l.x2 > bw )
                    bw = l.x2;
                if( l.y2 < by )
                    by = l.y2;
                if( l.y2 > bh )
                    bh = l.y2;
            }
            m_perimeter = 0.;
            for( int n = 1; n < m_outline.npoints; n++ )
                m_perimeter += Point2D.distance( m_outline.xpoints[n], m_outline.ypoints[n], m_outline.xpoints[n-1], m_outline.ypoints[n-1] );
            if( m_spineBasis != null ){
                Point2D.Double v = new Point2D.Double( m_spineBasis.getX2() - m_spineBasis.getX1(), m_spineBasis.getY2() - m_spineBasis.getY1() );
                m_perimeter += Math.sqrt( v.x * v.x + v.y * v.y );
            }
            m_shapeIndex = ( 4. * Math.PI * m_area ) / ( m_perimeter * m_perimeter );
            //bx--; by--; bw++; bh++;
            m_bbox = new Rectangle2D.Double( bx, by, bw - bx, bh - by );
            xs = new double[2*m_shape.length]; ys = new double[2*m_shape.length];
            for( int j = 0, i = 0; j < m_shape.length; j++, i+=2 ){
                xs[i] = m_shape[j].x1;
                xs[i+1] = m_shape[j].x2;
                ys[i] = m_shape[j].y1;
                ys[i+1] = m_shape[j].y2;
            }
            m_hull = new QuickHull();
            m_hull.quickconvexhull( xs, ys );

            //Contruction of the graph representing the spine
            ArrayList < SeedGraph > seeds = new ArrayList < SeedGraph >();
            for( int n = 0; n < m_edges.size(); n++ ){
                SeedGraph seed = m_edges.get( n ).getV1();
                if( !seeds.contains( seed ) ) seeds.add( seed );
                seed = m_edges.get( n ).getV2();
                if( !seeds.contains( seed ) ) seeds.add( seed );
            }
            m_graph = new EdgeWeightedGraph( seeds.size() );
            m_graph.setSeeds( seeds );
            for( int n = 0; n < m_edges.size(); n++ ){
                EdgeT edge = ( EdgeT )m_edges.get( n );
                int index1 = seeds.indexOf( edge.getV1() ), index2 = seeds.indexOf( edge.getV2() );
                m_graph.addEdge( new Edge( index1, index2, 1, edge ) );
            }
            for( int n = 0; n < m_edges.size() && m_path == null; n++ ){
                EdgeT edge = ( EdgeT )m_edges.get( n );
                SeedT seed = ( SeedT )edge.getV1(); 
                if( seed.getTriangle().isMarked() )
                    m_path = new BreadthFirstPaths( m_graph, seeds.indexOf( seed ) );
                seed = ( SeedT )edge.getV2(); 
                if( seed.getTriangle().isMarked() )
                    m_path = new BreadthFirstPaths( m_graph, seeds.indexOf( seed ) );
            }

            //From the graph, we generate the skeleton in the path finding way -> means we start from the start point of the skeleton in the neck
            //Aim is to have all the skeleton points in the same  direction to ease the length computation
            //It is possible just when a spine is selected
            //otherwise, we don't have a m_path determined than we just compute the skeleton from the edges
            m_skeletons = new CatmullCurve[m_edges.size()];
            if( m_path == null ){
                for( int n = 0; n < m_edges.size(); n++ ){
                    EdgeT edge = ( EdgeT )m_edges.get( n );
                    ArrayList < Point2D.Double > originalSkel = edge.getSkel(), actualSkel = new ArrayList < Point2D.Double >();
                    for( int i = 0; i < originalSkel.size(); i++ )
                        //if( m_hull.contains( originalSkel.get( i ).x, originalSkel.get( i ).y ) )
                            actualSkel.add( originalSkel.get( i ) );
                    //Ading the first point of the skeleton, the mid point of the triangle edge of the corresponding junctionnal seed
                    /*SeedT seed = ( SeedT )edge.getV1();
                    TriangleT tri = seed.getTriangle();
                    if( tri.isMarked() ){
                        Point2D.Double pos = tri.getCenterSharedEdge( edge.getNeighborTriangleFromTriangleSeed( seed ) );
                        actualSkel.add( 0, pos );
                    }
                    seed = ( SeedT )edge.getV2();
                    tri = seed.getTriangle();
                    if( tri.isMarked() ){
                        Point2D.Double pos = tri.getCenterSharedEdge( edge.getNeighborTriangleFromTriangleSeed( seed ) );
                        actualSkel.add( pos );
                    }*/
                    if( !actualSkel.isEmpty() ){
                        m_skeletons[n] = new CatmullCurve( actualSkel, 0, 1,  m_edges.get( n ).getSkel().size() );
                        if( m_skeletons[n].points.npoints > 3 )
                            m_skeletons[n].filter( m_skeletons[n].points, 3 );
                    }
                }
            }
            else{
                boolean [] marks = new boolean[m_edges.size()];
                for( int n = 0; n < m_edges.size(); n++ ){
                    marks[n] = m_edges.get( n ).marked();
                    m_edges.get( n ).setMarked( false );
                }
                ArrayList < Integer > extremalSeeds = new ArrayList < Integer >();
                for( int n = 0; n < m_graph.V(); n++ )
                    if( GraphClientWeighted.degree( m_graph, n ) == 1 )
                        extremalSeeds.add( n );
                for( int n = 0; n < extremalSeeds.size(); n++ ){
                    int v = extremalSeeds.get( n );
                    ArrayList < Integer > path = new ArrayList < Integer >();
                    for( Integer vert : m_path.pathTo( v ) )
                        path.add( vert );
                    for( int p = 1; p < path.size(); p++ ){
                        EdgeGraph edge = seeds.get( path.get( p - 1 ) ).getEdgeWith( seeds.get( path.get( p ) ) );
                        if( edge != null && !edge.marked() ){
                            boolean inverted = !( seeds.get( path.get( p - 1 ) ).equals( edge.getV1() ) && seeds.get( path.get( p ) ).equals( edge.getV2() ) );
                            ArrayList < Point2D.Double > originalSkel = ( ArrayList < Point2D.Double > )edge.getSkel().clone(), actualSkel = new ArrayList < Point2D.Double >();
                            //Collections.copy( originalSkel, edge.getSkel() );
                            if( inverted )
                                Collections.reverse( originalSkel );
                            for( int i = 0; i < originalSkel.size(); i++ )
                                if( m_hull.contains( originalSkel.get( i ).x, originalSkel.get( i ).y ) )
                                    actualSkel.add( originalSkel.get( i ) );
                            //Ading the first point of the skeleton, the mid point of the triangle edge of the corresponding junctionnal seed
                            SeedT seed = ( SeedT )edge.getV1();
                            TriangleT tri = seed.getTriangle();
                            if( tri.isMarked() ){
                                Point2D.Double pos = tri.getCenterSharedEdge( ( ( EdgeT )edge ).getNeighborTriangleFromTriangleSeed( seed ) );
                                if( inverted )
                                    actualSkel.add( pos );
                                else
                                    actualSkel.add( 0, pos );
                            }
                            seed = ( SeedT )edge.getV2();
                            tri = seed.getTriangle();
                            if( tri.isMarked() ){
                                Point2D.Double pos = tri.getCenterSharedEdge( ( ( EdgeT )edge ).getNeighborTriangleFromTriangleSeed( seed ) );
                                if( inverted )
                                    actualSkel.add( 0, pos );
                                else
                                    actualSkel.add( pos );
                            }
                            int index = m_edges.indexOf( edge );
                            m_skeletons[index] = new CatmullCurve( actualSkel, 0, 1,  originalSkel.size() );
                            //m_skeletons[index].filter( m_skeletons[index].points, 3 );
                            edge.setMarked( true );
                        }
                    }
                }
                for( int n = 0; n < m_edges.size(); n++ )
                    m_edges.get( n ).setMarked( marks[n]  );
            }
        }
        //if( m_outline != null )
        //    m_discreteShape = m_outline.getPolygonRoi();
    }
    
    //Return if the spine is touching the border of the image
    public boolean createDiscreteSpineShape( /*ImagePlus _imp, */ImageProcessor _binarized ){
        int addingD = 5;
        //Creation of the discrete shape of the spine
        int x = ( int )m_bbox.x - addingD, y = ( int )m_bbox.y - addingD, w = ( int )m_bbox.width + 2 * addingD, h = ( int )m_bbox.height + 2 * addingD;
        if( x < 0 ) x = 0;
        if( y < 0 ) y = 0;
        if( x >= _binarized.getWidth() ) x = _binarized.getWidth() - 1;
        if( y >= _binarized.getHeight() ) y = _binarized.getHeight() - 1;
        _binarized.setRoi( new Roi( x, y, w, h ) );
        ImageProcessor crop = _binarized.crop();
        _binarized.resetRoi();
        
        for( int i = 0; i < crop.getWidth(); i++ )
            for( int j = 0; j < crop.getHeight(); j++ ){
                double xt = i + x, yt = j + y;
                //if( this.contains( xt + .5, yt + .5 ) )
                if( this.contains( xt, yt ) || this.contains( xt + 1, yt ) || this.contains( xt + 1, yt + 1 ) || this.contains( xt, yt + 1 ))
                    crop.set( i, j, 125 );
                else
                    crop.set( i, j, 0 );
            }
        
        //System.out.println("x=" + x + ", y=" + y );
        int w2 = _binarized.getWidth() - 1, h2 = _binarized.getHeight() - 1;
        for( int i = 0; i < crop.getWidth(); i++ )
            for( int j = 0; j < crop.getHeight(); j++ ){
                if( crop.get( i, j ) == 125 ){
                    double xt = i + x, yt = j + y;
                    if(xt == 0 || yt == 0 || xt == w2 || yt == h2)
                        return true;
                }
            }
        int xc = -1, yc = -1;
        for( int i = 0; i < crop.getWidth() && xc == -1; i++ )
            for( int j = 0; j < crop.getHeight() && yc == -1; j++ ){
                if( crop.get( i, j ) == 125 ){
                    xc = i;
                    yc = j;
                }
            }
        //ImagePlus imp = new ImagePlus("test", crop.duplicate());
        //imp.show();
        Wand wand = new Wand( crop );
        wand.autoOutline( xc, yc, 125, 125 );
        int [] xs = new int[wand.npoints], ys = new int[wand.npoints];
        for( int n = 0; n < wand.npoints; n++ ){
            xs[n] = wand.xpoints[n] + x;
            ys[n] = wand.ypoints[n] + y;
        }
        
        /*StraightLine l1 = new StraightLine( m_spineBasis.getX1() - x, m_spineBasis.getY1() - y, m_spineBasis.getX2() - x, m_spineBasis.getY2() - y, StraightLine.PARALLELE_LINE );
        StraightLine l2 = new StraightLine( 0, 0, 0, crop.getHeight() - 1, StraightLine.PARALLELE_LINE );
        StraightLine l3 = new StraightLine( crop.getWidth() - 1, 0, crop.getWidth() - 1, crop.getHeight() - 1, StraightLine.PARALLELE_LINE );
        Point2D.Double inter1 = l1.intersectionLine( l2 ), inter2 = l1.intersectionLine( l3 );
        ImageMisc.lineTracing( crop, ( int )inter1.x, ( int )inter1.y, ( int )inter2.x, ( int )inter2.y, 255, 125 );
        int xc = 0, yc = 0;
        if( m_edges.size() == 1 ){
            DoublePolygon pol = m_skeletons[0].points;
            xc = ( int )pol.xpoints[pol.npoints / 2] - x;
            yc = ( int )pol.ypoints[pol.npoints / 2] - y;
        }
        else{
            DoublePolygon pol = m_skeletons[0].points;
            xc = ( int )pol.xpoints[pol.npoints-1] - x;
            yc = ( int )pol.ypoints[pol.npoints-1] - y;
        }
        crop.setValue( 125 );
        ImagePlus imp = new ImagePlus("test", crop.duplicate());
        imp.show();
        FloodFiller ff = new FloodFiller( crop );
        //ff.fill( xc, yc );
        int [] dxt = {-1,0,1,0,0}, dyt = {0, -1, 0, 1, 0};
        for( int i = 0; i < m_edges.size(); i++ ){
            DoublePolygon pol = m_skeletons[i].points;
            for( int n = 0; n < pol.npoints; n++ ){
                int xp = ( int )pol.xpoints[n] - x, yp = ( int )pol.ypoints[n] - y;
                if( crop.getPixel( xp, yp ) == 255 )
                    for( int j = 0; j < dxt.length; j++ ){
                        int val = crop.getPixel( xp + dxt[j], yp + dyt[j] );
                        if( val == 255 )
                            ff.fill( xp + dxt[j], yp + dyt[j] );
                    }
            }
        }
        imp = new ImagePlus("test", crop.duplicate());
        imp.show();
        Wand wand = new Wand( crop );
        wand.autoOutline( xc, yc, 125, 125 );
        int [] xs = new int[wand.npoints], ys = new int[wand.npoints];
        for( int n = 0; n < wand.npoints; n++ ){
            xs[n] = wand.xpoints[n] + x;
            ys[n] = wand.ypoints[n] + y;
        }*/
        try{
            m_discreteShape = new PolygonRoi( xs, ys, wand.npoints, Roi.FREEROI );
        }
        catch(Exception e){
            System.out.println("Problem with discrete spine");
        }
        return false;
    }

    public boolean isDendrite(){
        return m_isDendrite;
    }
    public boolean isSelected(){
        return m_selected;
    }
    public void setSelected( boolean _val ){
        m_selected = _val;
    }
    public Line2D.Double[] getShape(){
        return m_shape;
    }
    public double getArea(){
        return m_area;
    }
    public double getPerimeter(){
        return m_perimeter;
    }
    public double getShapeIndex(){
        return m_shapeIndex;
    }
    public Color getColor(){
        return m_color;
    }
    public void setColor( Color _c ){
        m_color = _c;
    }

    public ArrayList < EdgeGraph > getEdges(){
        return m_edges;
    }

    public Rectangle2D.Double getBoundingBox(){
        return m_bbox;
    }

    public Point2D.Double [] getConvexHull(){
        return m_hull.getHull();
    }

    public boolean contains( Roi _roi ){
        return m_hull.contains( _roi );
    }
    
    public boolean contains( double x, double y ){
        for( int n = 0; n < m_edges.size(); n++ ){
            EdgeT edge = ( EdgeT )m_edges.get( n );
            if( edge.contains( x, y ) )
                return true;
        }
        return false;
    }
    
    public CatmullCurve [] getSkeletons(){
        return m_skeletons;
    }
    
    public ArrayList < Point2D.Double > getSkeletonPath(){
        return m_skeletonPath;
    }
    
    public DoublePolygon getOutline(){
        return m_outline;
    }
    
    public Roi getDiscreteShape(){
        return m_discreteShape;
    }
    
    public CatmullCurve getSkeletonFromEdge( EdgeGraph _e ){
        int index = m_edges.indexOf( _e );
        if( index == -1 )
            return null;
        else
            return m_skeletons[index];
    }
    
    public ArrayList < EdgeGraph > determineSkeletonPathToActin( SeedT _seed ){
        ArrayList < SeedGraph > seeds = m_graph.getSeeds();
        int index = seeds.indexOf( _seed );
        ArrayList < Integer > path = new ArrayList < Integer >();
        for( Integer vert : m_path.pathTo( index ) )
            path.add( vert );
        ArrayList < EdgeGraph > skeletonPath = new ArrayList < EdgeGraph >();
        for( int l = 1; l < path.size(); l++ ){
            EdgeGraph edge = seeds.get( path.get( l - 1 ) ).getEdgeWith( seeds.get( path.get( l ) ) );
            if( edge != null )
                skeletonPath.add( edge );
        }
        return skeletonPath;
    }
    
    public ArrayList < EdgeGraph > determineSkeletonPathToActin( EdgeGraph _e ){
        ArrayList < EdgeGraph > skeletonPath;
        EdgeT e = ( EdgeT )_e;
        ArrayList < SeedGraph > seeds = m_graph.getSeeds();
        int index1 = seeds.indexOf( e.getV1() ), index2 = seeds.indexOf( e.getV2() );
        ArrayList < Integer > path = new ArrayList < Integer >();
        for( Integer vert : m_path.pathTo( index1 ) )
            path.add( vert );
        ArrayList < EdgeGraph > edges = new ArrayList < EdgeGraph >();
        for( int l = 1; l < path.size(); l++ ){
            EdgeGraph edge = seeds.get( path.get( l - 1 ) ).getEdgeWith( seeds.get( path.get( l ) ) );
            if( edge != null )
                edges.add( edge );
        }
        path = new ArrayList < Integer >();
        for( Integer vert : m_path.pathTo( index2 ) )
            path.add( vert );
        ArrayList < EdgeGraph > edges2 = new ArrayList < EdgeGraph >();
        for( int l = 1; l < path.size(); l++ ){
            EdgeGraph edge = seeds.get( path.get( l - 1 ) ).getEdgeWith( seeds.get( path.get( l ) ) );
            if( edge != null )
                edges2.add( edge );
        }
        if( edges.size() > edges2.size() )
            skeletonPath = edges;
        else
            skeletonPath = edges2;
        /*System.out.println( "Size edges -> " + skeletonPath.size() );
        for( EdgeGraph e1 : skeletonPath )
            System.out.println( e1.toString() );*/
        return skeletonPath;
    }
    
    public double getLongestPathWithEdge( EdgeGraph _e ){
        double distance = 0.;
        ArrayList < SeedGraph > seeds = m_graph.getSeeds();
        ArrayList < Integer > extremalSeeds = new ArrayList < Integer >();
        for( int n = 0; n < m_graph.V(); n++ )
            if( GraphClientWeighted.degree( m_graph, n ) == 1 )
                extremalSeeds.add( n );
        for( int v : extremalSeeds ){
            ArrayList < Integer > path = new ArrayList < Integer >();
            for( Integer vert : m_path.pathTo( v ) )
                path.add( vert );
            ArrayList < EdgeGraph > edges = new ArrayList < EdgeGraph >();
            for( int l = 1; l < path.size(); l++ ){
                EdgeGraph edge = seeds.get( path.get( l - 1 ) ).getEdgeWith( seeds.get( path.get( l ) ) );
                if( edge != null )
                    edges.add( edge );
            }
            if( !edges.contains( _e ) ) continue;
            ArrayList < Point2D.Double > skeletonLongest = new ArrayList < Point2D.Double >();
            for( EdgeGraph e : edges ){
                CatmullCurve skel = this.getSkeletonFromEdge( e );
                for( int i = 0; i < skel.points.npoints - 1; i++ ){
                    skeletonLongest.add( new Point2D.Double( skel.points.xpoints[i], skel.points.ypoints[i] ) );
                }
            }
            CatmullCurve skel = this.getSkeletonFromEdge( edges.get( edges.size() - 1 ) );
            skeletonLongest.add( new Point2D.Double( skel.points.xpoints[skel.points.npoints-1], skel.points.ypoints[skel.points.npoints-1] ) );
            double d = 0.;
            for( int n = 1; n < skeletonLongest.size(); n++ )
                d += skeletonLongest.get( n ).distance( skeletonLongest.get( n - 1 ) );
            if( d > distance ){
                distance = d;
                m_skeletonLongest = skeletonLongest;
            }
        }
        return distance;
    }

    public int insideInfluenceRegionIndex( Point2D.Double _p ){
        for( int n = 0; n < m_edges.size(); n++ ){
            EdgeT edge = ( EdgeT )m_edges.get( n );
            ArrayList < TriangleT > tri = edge.getTriangles();
            for( int i = 0; i < tri.size(); i++ )
                if( tri.get( i ).contains( _p ) )
                    return n;
            tri = edge.getTrianglesTrimmed();
            for( int i = 0; i < tri.size(); i++ )
                if( tri.get( i ).contains( _p ) )
                    return n;
            if( edge.getV1().contains( _p ) || edge.getV2().contains( _p ) )
                return n;
        }
        return -1;
    }
    public EdgeT insideInfluenceRegion( Point2D.Double _p ){
        for( int n = 0; n < m_edges.size(); n++ ){
            EdgeT edge = ( EdgeT )m_edges.get( n );
            ArrayList < TriangleT > tri = edge.getTriangles();
            for( int i = 0; i < tri.size(); i++ )
                if( tri.get( i ).contains( _p ) )
                    return edge;
            tri = edge.getTrianglesTrimmed();
            for( int i = 0; i < tri.size(); i++ )
                if( tri.get( i ).contains( _p ) )
                    return edge;
        }
        return null;
    }
    public TriangleT insideSpecificTriangle( Point2D.Double _p ){
        for( int n = 0; n < m_edges.size(); n++ ){
            EdgeT edge = ( EdgeT )m_edges.get( n );
            ArrayList < TriangleT > tri = edge.getTriangles();
            for( int i = 0; i < tri.size(); i++ )
                if( tri.get( i ).contains( _p ) )
                    return tri.get( i );
            tri = edge.getTrianglesTrimmed();
            for( int i = 0; i < tri.size(); i++ )
                if( tri.get( i ).contains( _p ) )
                    return tri.get( i );
        }
        return null;
    }
    
    public void setNeckAndHead( ArrayList < Point2D.Double > _neck, ArrayList < Point2D.Double > _head ){
        m_neck = _neck;
        m_head = _head;
    }
    public ArrayList < Point2D.Double > getNeck(){
        return m_neck;
    }
    public ArrayList < Point2D.Double > getHead(){
        return m_head;
    }
    public Line2D getSpineBasis(){
        return m_spineBasis;
    }
    public Line2D getSeparationHeadNeck(){
        return m_separationHeadNeck;
    }
    public void setSeparationHeadNeck(Line2D _l){
        m_separationHeadNeck = _l;
    }
    public void setShapeHead( /*PolygonRoi*/EllipseRoi _roi ){
        m_shapeHead = _roi;
    }
    public /*PolygonRoi*/EllipseRoi getShapeHead(){
        return m_shapeHead;
    }
    
    public void clearShapesNeckAnalysis(){
        if( m_neckAnalysis != null )
            m_neckAnalysis.clear();
    }
    public void addShapeNeckAnalysis( NeckShape _nshape ){
        if( m_neckAnalysis == null )
            m_neckAnalysis = new ArrayList < NeckShape >();
        m_neckAnalysis.add( _nshape );
    }
    public void removeFirstShapeNeck(){
        if( m_neckAnalysis != null && m_neckAnalysis.size() > 1 ){
            m_neckAnalysis.remove( 0 );
        }
    }
    public void setNeckAnalysis( ArrayList < NeckShape > _neckAnalysis ){
        m_neckAnalysis = _neckAnalysis;
    }
    public ArrayList < NeckShape > getNeckAnalysis(){
        return m_neckAnalysis;
    }
    
    public double [] getLengthInfos(){
        if( m_infosLength == null ){
            double [] infosLength = new double[3];
            for( int n = 0; n < infosLength.length; n++ )
                infosLength[n] = 0.;
            return infosLength;
        }
        else
            return m_infosLength;
    }
    public void setLengthInfos( double [] _infos ){
        m_infosLength = _infos;
    }
    
        
    public double [] getParamsEllipsoid(){
        if( m_shapeHead == null ){
            double [] params = new double[5];
            for( int n = 0; n < params.length; n++ )
                params[n] = 0.;
            return params;
        }
        else
            return m_shapeHead.getParams();
    }
    
    public double getIntegratedIntensityChannel( int _channel ){
        switch( _channel ){
            case 1:
                return intIntensityChan1;
            case 2:
                return intIntensityChan2;
            case 3:
                return intIntensityChan3;
            default:
                return 0.;
        } 
    }
    
    public EdgeWeightedGraph getGraph(){
        return m_graph;
    }
    
    public Point2D.Double getPointDelimitatingNeckHead(){
        return m_pointDelimination;
    }
    public void setPointDelimitatingNeckHead(Point2D.Double _p){
        m_pointDelimination = _p;
    }

    static Line2D.Double addCorrectSides( TriangleT _triangle, ArrayList < Line2D > _lines ){
        Line2D.Double l = null;
        //if(_triangle.isJuntionnal()) return l;
        PointT a = _triangle.getA(), b = _triangle.getB(), c = _triangle.getC();
        int sizeBefore = _lines.size();
        if( a.getPrec() == b || a.getNext() == b )
            _lines.add( new Line2D.Double( a.x, a.y, b.x, b.y ) );
        if( a.getPrec() == c || a.getNext() == c )
            _lines.add( new Line2D.Double( c.x, c.y, a.x, a.y ) );
        if( b.getPrec() == c || b.getNext() == c )
            _lines.add( new Line2D.Double( c.x, c.y, b.x, b.y ) );
        if(_lines.size() - sizeBefore == 3)
            System.out.println("3 edges were added...");
        //Used to add the edge at the basis of the spine
        for( TriangleT t2 : _triangle.getNeighbors() ){
            if( _triangle.isMarked() != t2.isMarked() ){
                PointT[] pts = _triangle.getSharedEdge( t2 );
                if( pts != null ){
                    l = new Line2D.Double( pts[0].x, pts[0].y, pts[1].x, pts[1].y );
                    _lines.add( l );
                }
            }
        }
        return l;
    }

    static public void generateObjects( ArrayList < NeuronObject > _dendrites, ArrayList < NeuronObject > _spines, EdgeWeightedGraph _graph, double _min, double _max ){
        if( !_graph.isSelected() ) return;
        for( Edge e : _graph.edges() ){
            EdgeGraph edge = e.edge();
            edge.setMarked( false );
        }
        //First do spines. This can help in the case of an empty spine, to transfer it to dendrite
        for( Edge e : _graph.edges() ){
            EdgeGraph edge = e.edge();
            if( edge.marked() || edge.isDendrite()) continue;
            ArrayList < EdgeGraph > edges = new ArrayList < EdgeGraph >();
            recursiveAdding( e, edges, _graph, edge.isDendrite() );
            NeuronObject nobj = new NeuronObject( edges, edge.isDendrite(), _min, _max );
            if(nobj.getOutline().npoints != 0)
                _spines.add( nobj );
            else{
                for(EdgeGraph eTmp : edges){
                    eTmp.setMarked(false);
                    eTmp.setDendrite(true);
                    eTmp.getV1().setMarked(true);
                    eTmp.getV2().setMarked(true);
                }
            }
        }
        
        //then do dendrite
        for( Edge e : _graph.edges() ){
            EdgeGraph edge = e.edge();
            if( edge.marked() || !edge.isDendrite() ) continue;
            ArrayList < EdgeGraph > edges = new ArrayList < EdgeGraph >();
            recursiveAdding( e, edges, _graph, edge.isDendrite() );
            _dendrites.add( new NeuronObject( edges, edge.isDendrite(), _min, _max ) );
        }
    }

    static void recursiveAdding( Edge _e, ArrayList < EdgeGraph > _edges, EdgeWeightedGraph _graph, boolean _isDendrite ){
        EdgeGraph edge = _e.edge();
        _edges.add( edge );
        edge.setMarked( true );
        for( Edge e : _graph.adj( _e.either() ) ){
            EdgeGraph edge2 = e.edge();
            if( !edge2.marked() && edge2.isDendrite() == _isDendrite )
                recursiveAdding( e, _edges, _graph, _isDendrite );
        }
        for( Edge e : _graph.adj( _e.other( _e.either() ) ) ){
            EdgeGraph edge2 = e.edge();
            if( !edge2.marked() && edge2.isDendrite() == _isDendrite )
                recursiveAdding( e, _edges, _graph, _isDendrite );
        }
    }
}
