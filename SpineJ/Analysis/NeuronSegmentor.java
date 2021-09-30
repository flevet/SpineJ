/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Analysis;

import SpineJ.Tools.Graph.Edge;
import SpineJ.Tools.Graph.EdgeGraph;
import SpineJ.Tools.Graph.EdgeWeightedGraph;
import SpineJ.Tools.Graph.SeedGraph;
import SpineJ.Tools.Delaunay.DelaunayT;
import SpineJ.Tools.Delaunay.EdgeT;
import SpineJ.Tools.Delaunay.PointT;
import SpineJ.Tools.Delaunay.SeedT;
import SpineJ.Tools.Delaunay.TriangleT;
import SpineJ.Tools.Geometry.CatmullCurve;
import SpineJ.Tools.Misc.HoleRoi;
import SpineJ.Tools.Misc.TimeEstimator;
import SpineJ.Tools.QuadTree.point.PointNodeElement;
import SpineJ.Tools.QuadTree.point.PointQuadTree;
import SpineJ.Widgets.ImageCanvasNeuron;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.io.SaveDialog;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.FloodFiller;
import ij.process.ImageProcessor;
import java.awt.Frame;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author Florian Levet
 */
public class NeuronSegmentor{

    public class LinkPointGraph{
        LinkPointGraph( SeedGraph _seed, EdgeWeightedGraph _graph ){
            m_point = _seed;
            m_graph = _graph;
        }
        SeedGraph m_point;
        EdgeWeightedGraph m_graph;
    }
    private PointQuadTree < LinkPointGraph > m_tree = null;
    protected ByteProcessor m_binary = null;
    protected ArrayList < CatmullCurve[] > m_contours = null;
    protected DelaunayT[] m_delaunays = null;
    protected GraphDetectorNeuron m_gd = null;
    protected ArrayList < Line2D.Double > m_lines = null;
    protected ArrayList < ArrayList < NeuronObject > > m_dendrites = null, m_spines = null;

    protected ImageCanvasNeuron m_icd = null;
    
    public NeuronSegmentor( ByteProcessor _bp, double _size, boolean _trimTriangles, boolean _onlyKeepBiggestROI ){
        m_binary = _bp;
        TimeEstimator time = new TimeEstimator();
        time.initTime();

        ArrayList < Roi > roisArrayTmp2 = null;
        if(_onlyKeepBiggestROI)
            roisArrayTmp2 = determineBiggestRoimageFiltered2( _size );
        else
            roisArrayTmp2 = determineRoisBiggerThanSizeImageFiltered2(_size);
        time.timeNow( "Determine rois of filtered image" );
        ByteProcessor res = ( ByteProcessor )m_binary.duplicate();
        time.timeNow( "Deleting small rois" );
        ByteProcessor bp = ( ByteProcessor )res;        
        
        m_contours = new ArrayList < CatmullCurve[] >();
        for(int k = 0; k < roisArrayTmp2.size(); k++){
            HoleRoi roi = ( HoleRoi )roisArrayTmp2.get( k );
            ImageProcessor ip = roi.getMaskWithHoles();
            CatmullCurve [] ccs = CatmullCurve.execute( bp, roi );
            m_contours.add(ccs);
        }
        time.timeNow("Determining Roi and contours");
        TriangleT [][] links = null;
        m_delaunays = generateMultiDelaunay( links, _trimTriangles, m_contours );
        if(links != null){
            for( int x = 0; x < bp.getWidth(); x++ )
                for( int y = 0; y < bp.getHeight(); y++ )
                    if( bp.get( x, y ) == 0 )
                        links[x][y] = null;
        }
        time.timeNow("Determining Delaunay/Voronoi");

        ByteProcessor test = ( ByteProcessor )bp.duplicate();
        m_gd = new GraphDetectorNeuron();
        m_gd.execute( test, m_delaunays );
        time.timeNow("Compute Skeleton");

        m_lines = new ArrayList < Line2D.Double >();
        ArrayList < EdgeWeightedGraph > graphs = m_gd.getGraphs();
        for( int n = 0; n < graphs.size(); n++ ){
            for( Edge edge : graphs.get( n ).edges() ){
                ArrayList < Point2D.Double > pix = edge.edge().getSkel();
                for( int l = 1; l < pix.size(); l++ ){
                    Point2D.Double p1 = pix.get( l - 1 ), p2 = pix.get( l );
                    m_lines.add( new Line2D.Double( p1.x, p1.y, p2.x, p2.y ) );
                }
            }
        }
        m_dendrites = new ArrayList < ArrayList < NeuronObject > >();
        m_spines = new ArrayList < ArrayList < NeuronObject > >();
        for( int n = 0; n < m_gd.getGraphs().size(); n++ ){
            ArrayList < NeuronObject > dens = new ArrayList < NeuronObject >(), spins = new ArrayList < NeuronObject >();
            NeuronObject.generateObjects( dens, spins, m_gd.getGraphs().get( n ), 100., 500. );//m_srd.getMin(), m_srd.getMax() );
            m_dendrites.add( dens );
            m_spines.add( spins );
        }

        m_tree = new PointQuadTree < LinkPointGraph > ( new Point2D.Double( 0, 0 ), new Point2D.Double( _bp.getWidth(), _bp.getHeight() ), 8, 6 );
        for( int i = 0; i < m_gd.getGraphs().size(); i++ ){
            EdgeWeightedGraph graph = m_gd.getGraphs().get( i );
            ArrayList < SeedGraph > seeds = graph.getSeeds();
            for( int j = 0; j < seeds.size(); j++ ){
                SeedGraph seed = seeds.get( j );
                m_tree.insert( seed.getX(), seed.getY(), new LinkPointGraph( seed, graph ) );
            }
        }
    }

    private DelaunayT[] generateMultiDelaunay( TriangleT [][] _links, boolean _trimTriangles, ArrayList < CatmullCurve[] > _contours ){
        DelaunayT[] delaunays = new DelaunayT[_contours.size()];
        for(int k = 0; k < _contours.size(); k++){
            CatmullCurve[] ccs = _contours.get(k);
            if( ccs == null )
                System.out.println( "pb" );
            delaunays[k] = new DelaunayT( );
            delaunays[k].execute( ccs, m_binary.getWidth(), m_binary.getHeight(), _links, _trimTriangles );
        }
        return delaunays;
    }
    
    private ArrayList < Roi > determineRoisBiggerThanSizeImageFiltered2( double _size ){
        TimeEstimator time = new TimeEstimator();
        time.initTime();
        
        ByteProcessor bp = ( ByteProcessor )m_binary.duplicate();
        bp.invert();
        
        //Normal way of doing things, keeps all objects
        IJ.run("ROI Manager...");
        Frame frame = WindowManager.getFrame("ROI Manager");
        RoiManager roiManager = (RoiManager) frame;
        roiManager.setVisible( false );
        roiManager.runCommand( "Show None" );
        int options = ParticleAnalyzer.ADD_TO_MANAGER + ParticleAnalyzer.CLEAR_WORKSHEET;// -> 64+2048 -> CLEAR_WORKSHEET, ADD_TO_MANAGER | 2053 with the 4 of the OUTLINES
        int measurements = Measurements.AREA;
        ParticleAnalyzer pa = new ParticleAnalyzer( options, measurements, null, _size, Double.POSITIVE_INFINITY, 0., Double.POSITIVE_INFINITY );
        ImagePlus useless2 = new ImagePlus( "Useless", bp.duplicate() );
        pa.analyze( useless2 );
        Roi [] rois = roiManager.getRoisAsArray();
        roiManager.dispose();
          
        ArrayList < Roi > hrois = new ArrayList < Roi >();
        
        ImageProcessor forFloor = bp.duplicate();
        forFloor.setValue( 192 );
        FloodFiller ff = new FloodFiller( forFloor );
        for( int n = 0; n < rois.length; n++ ){
            //We use Float.MAX_VALUE to remove all holes, otherwise apply another value in pix²
            hrois.add( new HoleRoiNeuron( rois[n], bp.duplicate(), ff, 500.f));//Float.MAX_VALUE ) );
        }
        time.timeNow("Creation of hole rois");
        
        return hrois;
    }
    
    private ArrayList < Roi > determineBiggestRoimageFiltered2( double _size ){
        TimeEstimator time = new TimeEstimator();
        time.initTime();
        
        ByteProcessor bp = ( ByteProcessor )m_binary.duplicate();
        bp.invert();
        
        //Only keep the biggest object -> the main dendrite
        IJ.run("ROI Manager...");
        Frame frame = WindowManager.getFrame("ROI Manager");
        RoiManager roiManager = (RoiManager) frame;
        roiManager.setVisible( false );
        roiManager.runCommand( "Show None" );
        int options = ParticleAnalyzer.ADD_TO_MANAGER + ParticleAnalyzer.CLEAR_WORKSHEET;// -> 64+2048 -> CLEAR_WORKSHEET, ADD_TO_MANAGER | 2053 with the 4 of the OUTLINES
        int measurements = Measurements.AREA;
        ResultsTable rt = new ResultsTable();
        ParticleAnalyzer pa = new ParticleAnalyzer( options, measurements, rt, _size, Double.POSITIVE_INFINITY, 0., Double.POSITIVE_INFINITY );
        ImagePlus useless2 = new ImagePlus( "Useless", bp.duplicate() );
        pa.analyze( useless2 );
        Roi [] rois = roiManager.getRoisAsArray();
        roiManager.dispose();
        ArrayList < Roi > hrois = new ArrayList < Roi >();
        int index = 0;
        float[] areas = rt.getColumn(rt.getColumnIndex("Area"));
        for( int n = 1; n < rois.length; n++ ){
            if(areas[n] > areas[index])
                index = n;
        }
        ImageProcessor forFloor = bp.duplicate();
        forFloor.setValue( 192 );
        FloodFiller ff = new FloodFiller( forFloor );
        //We use Float.MAX_VALUE to remove all holes, otherwise apply another value in pix²
        hrois.add( new HoleRoiNeuron( rois[index], bp.duplicate(), ff, Float.MAX_VALUE ) );//10.f ) );
        time.timeNow("Creation of hole rois");
        return hrois;
    }

    public void resetSegmentation(){
        m_gd.resetSegmentation();
    }
    
    public void saveDelaunayAsSVG(Roi _roi){
        SaveDialog sd = new SaveDialog("Save objects...", "object", ".svg");
        String name = sd.getFileName();
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( sd.getDirectory() + name ), "utf-8" ) );
            
            writer.write( "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
            writer.write( "<svg xmlns=\"http://www.w3.org/2000/svg\"\n");
            writer.write( "     xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n     width=\"2000\" height=\"2000\" viewBox=\"0 0 2000 2000 \">\n");
            writer.write( "<title>d:/gl2ps/type_svg_outSimple.svg</title>\n");
            writer.write( "<desc>\n");
            writer.write( "Creator: Florian Levet\n");
            writer.write( "</desc>\n");
            writer.write( "<defs>\n");
            writer.write( "</defs>\n");
            
            //Delaunay
            for(int n = 0; n < m_delaunays.length; n++){
                DelaunayT delau = m_delaunays[n];
                ArrayList < TriangleT > triangles = delau.m_triangles2;
                for( int i = 0; i < triangles.size(); i++ ){
                    TriangleT t = triangles.get( i );
                    if( t.isTrimmed() ) continue;
                    PointT a = t.getA(), b = t.getB(), c = t.getC();
                    if(_roi == null){
                        writer.write( "<line x1 =\"" + a.x + "\" y1=\"" + a.y + "\" x2=\"" + b.x + "\" y2=\"" + b.y + "\" stroke=\"#ff0000\" stroke-width=\"1\"/>\n");
                        writer.write( "<line x1 =\"" + a.x + "\" y1=\"" + a.y + "\" x2=\"" + c.x + "\" y2=\"" + c.y + "\" stroke=\"#ff0000\" stroke-width=\"1\"/>\n");
                        writer.write( "<line x1 =\"" + b.x + "\" y1=\"" + b.y + "\" x2=\"" + c.x + "\" y2=\"" + c.y + "\" stroke=\"#ff0000\" stroke-width=\"1\"/>\n");
                    }
                    else{
                        boolean inside = _roi.contains((int)a.x, (int)a.y) || _roi.contains((int)b.x, (int)b.y) || _roi.contains((int)c.x, (int)c.y);
                        if(inside){
                            writer.write( "<line x1 =\"" + a.x + "\" y1=\"" + a.y + "\" x2=\"" + b.x + "\" y2=\"" + b.y + "\" stroke=\"#ff0000\" stroke-width=\"1\"/>\n");
                        writer.write( "<line x1 =\"" + a.x + "\" y1=\"" + a.y + "\" x2=\"" + c.x + "\" y2=\"" + c.y + "\" stroke=\"#ff0000\" stroke-width=\"1\"/>\n");
                        writer.write( "<line x1 =\"" + b.x + "\" y1=\"" + b.y + "\" x2=\"" + c.x + "\" y2=\"" + c.y + "\" stroke=\"#ff0000\" stroke-width=\"1\"/>\n");
                        }
                    }
                }
                for( int i = 0; i < triangles.size(); i++ ){
                    TriangleT t = triangles.get( i );
                    if( !t.isTrimmed() ) continue;
                    PointT a = t.getA(), b = t.getB(), c = t.getC();
                    if(_roi == null){
                        writer.write( "<line x1 =\"" + a.x + "\" y1=\"" + a.y + "\" x2=\"" + b.x + "\" y2=\"" + b.y + "\" stroke=\"#ff00ff\" stroke-width=\"1\"/>\n");
                        writer.write( "<line x1 =\"" + a.x + "\" y1=\"" + a.y + "\" x2=\"" + c.x + "\" y2=\"" + c.y + "\" stroke=\"#ff00ff\" stroke-width=\"1\"/>\n");
                        writer.write( "<line x1 =\"" + b.x + "\" y1=\"" + b.y + "\" x2=\"" + c.x + "\" y2=\"" + c.y + "\" stroke=\"#ff00ff\" stroke-width=\"1\"/>\n");
                    }
                    else{
                        boolean inside = _roi.contains((int)a.x, (int)a.y) || _roi.contains((int)b.x, (int)b.y) || _roi.contains((int)c.x, (int)c.y);
                        if(inside){
                            writer.write( "<line x1 =\"" + a.x + "\" y1=\"" + a.y + "\" x2=\"" + b.x + "\" y2=\"" + b.y + "\" stroke=\"#ff00ff\" stroke-width=\"1\"/>\n");
                        writer.write( "<line x1 =\"" + a.x + "\" y1=\"" + a.y + "\" x2=\"" + c.x + "\" y2=\"" + c.y + "\" stroke=\"#ff00ff\" stroke-width=\"1\"/>\n");
                        writer.write( "<line x1 =\"" + b.x + "\" y1=\"" + b.y + "\" x2=\"" + c.x + "\" y2=\"" + c.y + "\" stroke=\"#ff00ff\" stroke-width=\"1\"/>\n");
                        }
                    }
                }
            }
            
            //Skeleton
            for(Line2D.Double line : m_lines){
                if(_roi == null)
                        writer.write( "<line x1 =\"" + line.x1 + "\" y1=\"" + line.y1 + "\" x2=\"" + line.x2 + "\" y2=\"" + line.y2 + "\" stroke=\"#808080\" stroke-width=\"1\"/>\n");
                else{
                        boolean inside = _roi.contains((int)line.x1, (int)line.y1) || _roi.contains((int)line.x2, (int)line.y2);
                        if(inside)
                            writer.write( "<line x1 =\"" + line.x1 + "\" y1=\"" + line.y1 + "\" x2=\"" + line.x2 + "\" y2=\"" + line.y2 + "\" stroke=\"#808080\" stroke-width=\"1\"/>\n");
                    }
            }
            
            //Graph
            ArrayList < EdgeWeightedGraph > graphs = m_gd.getGraphs();
            for( int i = 0; i < graphs.size(); i++ ){
            EdgeWeightedGraph graph = graphs.get( i );
                for( Edge e : graph.edges() ){
                    EdgeGraph edge = e.edge();
                    SeedGraph v1 = edge.getV1();
                    SeedGraph v2 = edge.getV2();
                    if(_roi == null)
                        writer.write( "<line x1 =\"" + v1.getX() + "\" y1=\"" + v1.getY() + "\" x2=\"" + v2.getX() + "\" y2=\"" + v2.getY() + "\" stroke=\"#ffff00\" stroke-width=\"1\"/>\n");
                    else{
                        boolean inside = _roi.contains((int)v1.getX(), (int)v1.getY()) || _roi.contains((int)v2.getX(), (int)v2.getY());
                        if(inside)
                            writer.write( "<line x1 =\"" + v1.getX() + "\" y1=\"" + v1.getY() + "\" x2=\"" + v2.getX() + "\" y2=\"" + v2.getY() + "\" stroke=\"#ffff00\" stroke-width=\"1\"/>\n");
                    }
                }
            }
            
            //Outlines
            /*for(CatmullCurve[] ccs : m_contours){
                for (CatmullCurve cc : ccs) {
                    for(int j = 1; j < cc.points.npoints - 1; j++){//The last point is not added because it is the same as the first, addition causes problems with the delaunay triangulation
                        Point2D.Double p1 = new Point2D.Double( cc.points.xpoints[j-1], cc.points.ypoints[j-1] );
                        Point2D.Double p2 = new Point2D.Double( cc.points.xpoints[j], cc.points.ypoints[j] );
                        if(_roi == null)
                            writer.write( "<line x1 =\"" + p1.x + "\" y1=\"" + p1.y + "\" x2=\"" + p2.x + "\" y2=\"" + p2.y + "\" stroke=\"#0000ff\" stroke-width=\"1\"/>\n");
                        else{
                            boolean inside = _roi.contains((int)p1.x, (int)p1.y) || _roi.contains((int)p2.x, (int)p2.y);
                            if(inside)
                                writer.write( "<line x1 =\"" + p1.x + "\" y1=\"" + p1.y + "\" x2=\"" + p2.x + "\" y2=\"" + p2.y + "\" stroke=\"#0000ff\" stroke-width=\"1\"/>\n");
                        }
                    }
                    Point2D.Double p1 = new Point2D.Double( cc.points.xpoints[cc.points.npoints - 2], cc.points.ypoints[cc.points.npoints - 2] );
                    Point2D.Double p2 = new Point2D.Double( cc.points.xpoints[0], cc.points.ypoints[0] );
                    if(_roi == null)
                        writer.write( "<line x1 =\"" + p1.x + "\" y1=\"" + p1.y + "\" x2=\"" + p2.x + "\" y2=\"" + p2.y + "\" stroke=\"#0000ff\" stroke-width=\"1\"/>\n");
                    else{
                        boolean inside = _roi.contains((int)p1.x, (int)p1.y) || _roi.contains((int)p2.x, (int)p2.y);
                        if(inside)
                            writer.write( "<line x1 =\"" + p1.x + "\" y1=\"" + p1.y + "\" x2=\"" + p2.x + "\" y2=\"" + p2.y + "\" stroke=\"#0000ff\" stroke-width=\"1\"/>\n");
                    }
                }
            }*/
            for(CatmullCurve[] ccs : m_contours){
                for (CatmullCurve cc : ccs) {
                    writer.write( "<polyline fill=\"none\" stroke=\"#0000ff\" \n points=\""); 
                    for(int j = 1; j < cc.points.npoints - 1; j++){//The last point is not added because it is the same as the first, addition causes problems with the delaunay triangulation
                        Point2D.Double p1 = new Point2D.Double( cc.points.xpoints[j-1], cc.points.ypoints[j-1] );
                        Point2D.Double p2 = new Point2D.Double( cc.points.xpoints[j], cc.points.ypoints[j] );
                        if(_roi == null)
                            writer.write( "" + p1.x + "," + p1.y + " ");
                            //writer.write( "<line x1 =\"" + p1.x + "\" y1=\"" + p1.y + "\" x2=\"" + p2.x + "\" y2=\"" + p2.y + "\" stroke=\"#0000ff\" stroke-width=\"1\"/>\n");
                        else{
                            boolean inside = _roi.contains((int)p1.x, (int)p1.y) || _roi.contains((int)p2.x, (int)p2.y);
                            if(inside)
                                writer.write( "" + p1.x + "," + p1.y + " ");
                                //writer.write( "<line x1 =\"" + p1.x + "\" y1=\"" + p1.y + "\" x2=\"" + p2.x + "\" y2=\"" + p2.y + "\" stroke=\"#0000ff\" stroke-width=\"1\"/>\n");
                        }
                    }
                    Point2D.Double p1 = new Point2D.Double( cc.points.xpoints[cc.points.npoints - 2], cc.points.ypoints[cc.points.npoints - 2] );
                    Point2D.Double p2 = new Point2D.Double( cc.points.xpoints[0], cc.points.ypoints[0] );
                    if(_roi == null)
                        writer.write( "" + p1.x + "," + p1.y + " " + p2.x + "," + p2.y + "\"/>\n");
                        //writer.write( "<line x1 =\"" + p1.x + "\" y1=\"" + p1.y + "\" x2=\"" + p2.x + "\" y2=\"" + p2.y + "\" stroke=\"#0000ff\" stroke-width=\"1\"/>\n");
                    else{
                        boolean inside = _roi.contains((int)p1.x, (int)p1.y) || _roi.contains((int)p2.x, (int)p2.y);
                        if(inside)
                            writer.write( "" + p1.x + "," + p1.y + " " + p2.x + "," + p2.y + "\"/>\n");
                            //writer.write( "<line x1 =\"" + p1.x + "\" y1=\"" + p1.y + "\" x2=\"" + p2.x + "\" y2=\"" + p2.y + "\" stroke=\"#0000ff\" stroke-width=\"1\"/>\n");
                    }
                }
            }
            
            
            writer.write( "</svg>" );
        }catch ( IOException ex ){
        // report
        } finally {
            try { writer.close(); } catch (Exception ex) {}
        }
    }

    public ArrayList < PointNodeElement < LinkPointGraph > > getSelectedElementsOfTree( double _x, double _y, double _radius ){
        ArrayList<PointNodeElement<LinkPointGraph>> selectedElements = m_tree.getNeighboringCells( _x, _y, _radius );
        if( selectedElements.isEmpty() )
            return null;
        return selectedElements;
    }
    
    public ArrayList < EdgeGraph > setRoiAsSpine( double _x, double _y, double _radius, Roi _roi ){
        //m_gd.setAllAsDendrite();
        ArrayList < PointNodeElement < LinkPointGraph > > selectedElements = m_tree.getNeighboringCells( _x, _y, _radius );
        if( selectedElements.isEmpty() )
            return null;
        ArrayList < EdgeGraph > gedges = new ArrayList < EdgeGraph >();
        for( int i = 0; i < selectedElements.size(); i++ ){
            PointNodeElement < LinkPointGraph > link = selectedElements.get( i );
            if( !_roi.contains( ( int ) link.getX(), ( int )link.getY() ) ) continue;
            SeedT seed = ( SeedT )link.getElement().m_point;
            seed.getTriangle().setMarked( false );
            ArrayList < EdgeT > edges = seed.getEdges();
            for( int j = 0; j < edges.size(); j++ ){
                EdgeT edge = edges.get( j );
                if( !gedges.contains( edge ) ){
                    edges.get( j ).setAsDendrite( false );
                    gedges.add( edge );
                }
            }
        }

        generateSpinesAndDendrites();
        
        //m_gd.setAllAsDendrite();
        return gedges;
    }
    
    public void generateSpinesAndDendrites(){
        m_dendrites.clear();
        m_spines.clear();
        for( int n = 0; n < m_gd.getGraphs().size(); n++ ){
            ArrayList < NeuronObject > dens = new ArrayList < NeuronObject >(), spins = new ArrayList < NeuronObject >();
            NeuronObject.generateObjects( dens, spins, m_gd.getGraphs().get( n ), 100., 500. );//m_srd.getMin(), m_srd.getMax() );
            Collections.sort( dens, new Comparator(){
                public int compare( Object o1, Object o2 ){
                    NeuronObject s1 = ( NeuronObject )o1, s2 = ( NeuronObject )o2;
                    if ( s1.getArea() < s2.getArea() ) {
                        return -1;
                    }
                    if ( s1.getArea() > s2.getArea() ) {
                        return 1;
                    }
                    return 0;
                }
            });
            Collections.sort( spins, new Comparator(){
                public int compare( Object o1, Object o2 ){
                    NeuronObject s1 = ( NeuronObject )o1, s2 = ( NeuronObject )o2;
                    if ( s1.getArea() < s2.getArea() ) {
                        return -1;
                    }
                    if ( s1.getArea() > s2.getArea() ) {
                        return 1;
                    }
                    return 0;
                }
            });
            m_dendrites.add( dens );
            m_spines.add( spins );
        }
    }
    
    public ArrayList < NeuronObject > getDendritesInOneList(){
        ArrayList < NeuronObject > dens = new ArrayList < NeuronObject >();
        for(ArrayList < NeuronObject > densTmp : m_dendrites)
            dens.addAll(densTmp);
        return dens;
    }

    public void setImageCanvas( ImageCanvasNeuron _icd ){
        m_icd = _icd;
    }
    public PointQuadTree < LinkPointGraph > getTree(){
        return m_tree;
    }

    public ByteProcessor getBinaryProc(){
        return m_binary;
    }
    public ArrayList < CatmullCurve[] > getContours(){
        return m_contours;
    }
    public DelaunayT[] getMultiDelaunays(){
        return m_delaunays;
    }
    public GraphDetectorNeuron getGraphDetector(){
        return m_gd;
    }
    public ArrayList < Line2D.Double > getSkeletonLines(){
        return m_lines;
    }
    public ArrayList < ArrayList < NeuronObject > > getDendrites(){
        return m_dendrites;
    }
    public ArrayList < ArrayList < NeuronObject > > getSpines(){
        return m_spines;
    }
}
