/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Widgets;

/**
 *
 * @author Florian Levet
 */
import SpineJ.Analysis.NeckShape;
import SpineJ.Analysis.NeuronObject;
import SpineJ.Analysis.NeuronSegmentor;
import SpineJ.Analysis.SpineStatisticsGenerator;
import SpineJ.Tools.Delaunay.DelaunayT;
import SpineJ.Tools.Delaunay.EdgeT;
import SpineJ.Tools.Delaunay.PointT;
import SpineJ.Tools.Delaunay.SeedT;
import SpineJ.Tools.Delaunay.TriangleT;
import SpineJ.Tools.Geometry.CatmullCurve;
import SpineJ.Tools.Geometry.DoublePolygon;
import SpineJ.Tools.Geometry.StraightLine;
import SpineJ.Tools.Graph.Edge;
import SpineJ.Tools.Graph.EdgeGraph;
import SpineJ.Tools.Graph.EdgeWeightedGraph;
import SpineJ.Tools.Graph.SeedGraph;
import SpineJ.Tools.Misc.MyCustomFitter;
import SpineJ.Tools.QuadTree.AbstractNodeElement;
import ij.ImagePlus;
import ij.gui.*;
import ij.io.FileSaver;
import ij.io.SaveDialog;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.process.ByteProcessor;
import ij.process.FloatPolygon;
import ij.process.FloodFiller;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.text.TextWindow;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JFileChooser;

public class ImageCanvasNeuron extends ImageCanvas implements ItemListener, WindowListener, KeyListener, ActionListener{

    protected BufferedImage offScreenImage = null;
    protected int offScreenImage_width,  offScreenImage_height;
    protected static Font smallFont,  largeFont;
    protected static final int LIST_OFFSET = 100000;
    protected ImageProcessor m_original, m_binary;
    protected boolean m_displayOriginal = true;
    protected boolean m_displayOutline = false, m_displayTriangles = false, m_displayGraph = false, m_displaySkeleton = false, m_displayRois = true, m_currentAnalyzedSpine = true, m_displaySpineAnalyzed = true, m_displayActinSpineShapeDiscrete = true;
    protected boolean m_displaySpineHead = true, m_displaySpineNeck = true, m_displayNeckline = true, m_displayNecklineShape = true, m_displayNeckSkeleton = true, m_displaySkeletonLongest = true, m_displayHeadEllApprox = true, m_displaySpineLabel = true;
    protected boolean m_selectionSpine = false, m_selectionNeckline = false, m_definitionNeckEnd = false, m_displayDiscreteRoi = true, m_deleteSpine = false;
    protected boolean m_displayNecklineUsedForFit = true;
    
    protected ArrayList < AbstractNodeElement < NeuronSegmentor.LinkPointGraph > > m_selectedElements = new ArrayList < AbstractNodeElement < NeuronSegmentor.LinkPointGraph > >();
    protected Point.Double m_mouse = new Point2D.Double(), m_seed1 = null, m_seed2 = null;
    protected int m_radiusSelection = 40, m_radiusDisplay = 5;

    protected NeuronSegmentor m_dss = null;
    protected Cursor m_cursor = null, m_customCursor = null;
    protected double m_sizeCursor = 3.;

    protected ArrayList < SeedGraph > m_test = null;
    protected Roi [] m_rois = null;
    protected Point2D.Double [] m_positionRoi = null;
    protected String m_directoryOriginalImage = null;
    protected ArrayList < Roi > m_spineActinRoi = null;
    protected Point2D.Double [] m_spineActinPosition = null;
    protected NeuronObject m_spine = null;
    public ArrayList < Point2D.Double > projs = new ArrayList < Point2D.Double >();//
    protected ArrayList < DoublePolygon > m_shapeSpines = new ArrayList < DoublePolygon >();
    protected ArrayList < Roi > m_shapeSpinesDiscrete = new ArrayList < Roi >();
    
    protected ArrayList < NeuronObject > m_spines = new ArrayList < NeuronObject >(), m_dendrites = new ArrayList < NeuronObject >();
    protected StraightLine m_separationNeckHead = null;
    protected SpineAnalysisDialog m_saDialog = null;
    
    protected ArrayList < EdgeGraph > m_gedges = null;
    protected double m_x, m_y, m_rad, m_distanceMergeGraphNodes = 1000;
    
    protected NeuronDisplayDialog m_neuronDisplayD = null;


    public ImageCanvasNeuron( ImagePlus _imp ){
        super( _imp );
        m_original = _imp.getProcessor().duplicate();
        m_binary = m_original.duplicate();
        this.addKeyListener( this );
    }
    
    public ImageCanvasNeuron( ImagePlus _imp, ImageProcessor _binary ){
        super( _imp );
        m_original = _imp.getProcessor().duplicate();
        m_binary = _binary.duplicate();
        this.addKeyListener( this );
    }

    public ImageCanvasNeuron( ImagePlus _imp, NeuronSegmentor _dss ){
        this( _imp );
        createCustomCursor();
        setSegmentor( _dss );
    }

    public void setBinaryImage( ImageProcessor _ip ){
        m_binary = _ip;
    }
    
    public void setSegmentor( NeuronSegmentor _dss ){
        m_dss = _dss;
        m_binary = _dss.getBinaryProc();
        m_dss.setImageCanvas( this );
        m_displayRois = false;
        m_displayOutline = m_displayGraph = true;
    }
    
    public void setNeuronDisplayDialog( NeuronDisplayDialog _ndd ){
        m_neuronDisplayD = _ndd;
    }

    @Override
    public void paint(Graphics g) {
        final int srcRectWidthMag = (int) (srcRect.width * magnification);
        final int srcRectHeightMag = (int) (srcRect.height * magnification);

        if (offScreenImage == null || offScreenImage_width != srcRectWidthMag || offScreenImage_height != srcRectHeightMag) {
            offScreenImage = new BufferedImage(srcRectWidthMag, srcRectHeightMag, BufferedImage.TYPE_INT_ARGB);//createImage(srcRectWidthMag, srcRectHeightMag);
            offScreenImage_width = srcRectWidthMag;
            offScreenImage_height = srcRectHeightMag;
        }

        Graphics2D offScreenGraphics = offScreenImage.createGraphics();//.getGraphics();
        offScreenGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        offScreenGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        super.paint(offScreenGraphics);
        Stroke s = offScreenGraphics.getStroke();
        offScreenGraphics.setStroke(new BasicStroke(1.f));
        if( m_displayRois && m_rois != null ){
            offScreenGraphics.setColor( Color.red );
            for( int n = 0; n < m_rois.length; n++ )
                drawRoi( m_rois[n], offScreenGraphics );
        }
        
        
        if( m_dss != null ){
            offScreenGraphics.setStroke(new BasicStroke(1.f));
            DelaunayT[] delaunays = m_dss.getMultiDelaunays();
            if( delaunays != null )
                drawDelaunay( offScreenGraphics, delaunays );
            offScreenGraphics.setStroke(new BasicStroke(1.f));
            ArrayList < Line2D.Double > lines = m_dss.getSkeletonLines();
            if( lines != null && m_displaySkeleton )
                drawSkeletonLine( offScreenGraphics, new Color(255, 221, 0) );
            offScreenGraphics.setStroke(new BasicStroke(2.f));
            ArrayList < EdgeWeightedGraph > graphs = m_dss.getGraphDetector().getGraphs();
            if( graphs != null && m_displayGraph )
                drawSkeleton( offScreenGraphics, graphs, new Color(0, 200, 0), new Color(255, 221, 0) );
        }
        offScreenGraphics.setStroke(new BasicStroke(2.f));
        if( m_displaySpineAnalyzed )
            for( int n = 0; n < m_spines.size(); n++ )
                drawAnalyzedSpines( offScreenGraphics, n );
        else if( m_currentAnalyzedSpine && m_saDialog != null )
            drawAnalyzedSpines( offScreenGraphics, m_saDialog.getCurrentSpine() );
        if( m_separationNeckHead != null ){
            offScreenGraphics.setColor( Color.orange );
            offScreenGraphics.setStroke(new BasicStroke(2.f));
            draw(offScreenGraphics, m_separationNeckHead.getP1().getX(), m_separationNeckHead.getP1().getY(), m_separationNeckHead.getP2().getX(), m_separationNeckHead.getP2().getY() );
            
        }
        g.drawImage(offScreenImage, 0, 0, this);

            
    }

    @Override
    public void repaint() {
        super.repaint();
        this.paint(this.getGraphics());
    }

    @Override
    public void mousePressed(MouseEvent e) {
        String modifiers = MouseEvent.getMouseModifiersText(e.getModifiers());
        if( m_selectionNeckline || ((modifiers.indexOf("Maj") != -1) && (e.getButton() == MouseEvent.BUTTON3))){
            double x0 = offScreenXD( e.getX() );
            double y0 = offScreenYD( e.getY() );
            
            boolean found = false;
            for( int n = 0; n < m_spines.size() && !found; n++ ){
                NeuronObject spine = m_spines.get( n );
                ArrayList < NeckShape > lines = spine.getNeckAnalysis();
                if( lines != null ){
                    for( int i = 0; i < lines.size() && !found; i++ ){
                        found = lines.get( i ).isInside( ( int )x0, ( int )y0 );
                        if( found )
                            m_saDialog.changeCurrentSpineAndNeckline( n, i );
                    }
                }
            }
            repaint();
        }
        else if ( m_selectionSpine || (modifiers.indexOf("Maj") != -1) && (e.getButton() == MouseEvent.BUTTON1)){
            double x0 = offScreenXD( e.getX() );
            double y0 = offScreenYD( e.getY() );
            
            boolean found = false;
            for( int n = 0; n < m_spines.size() && !found; n++ ){
                NeuronObject spine = m_spines.get( n );
                DoublePolygon outline = spine.getOutline();
                if( !outline.contains( x0, y0 ) ) continue;
                m_saDialog.changeCurrentSpine( n );
                found = true;
            }
            repaint();
        }
        else if ((modifiers.indexOf("Ctrl") != -1) && (e.getButton() == MouseEvent.BUTTON1)){
            m_displayRois = !m_displayRois;
            repaint();
        }
        else if( m_definitionNeckEnd || (modifiers.indexOf("Ctrl") != -1) && (e.getButton() == MouseEvent.BUTTON3) ){
            double x0 = offScreenXD( e.getX() );
            double y0 = offScreenYD( e.getY() );
            
            defineSeparationNeckHead(x0, y0);
            repaint();
         }
        else if( m_deleteSpine || (modifiers.indexOf("Alt") != -1) && (e.getButton() == MouseEvent.BUTTON1) ){
            double x0 = offScreenXD( e.getX() );
            double y0 = offScreenYD( e.getY() );
            
            boolean found = false;
            for( int n = 0; n < m_spines.size() && !found; n++ ){
                NeuronObject spine = m_spines.get( n );
                DoublePolygon outline = spine.getOutline();
                if( !outline.contains( x0, y0 ) ) continue;
                found = true;
                m_saDialog.changeCurrentSpine( n );
                deleteCurrentSpine();
            }
        }
        else
            super.mousePressed(e);
    }
    
    protected void defineSeparationNeckHead(double _x, double _y){
        boolean found = false;
        for( int n = 0; n < m_spines.size() && !found; n++ ){
            NeuronObject spine = m_spines.get( n );
            DoublePolygon outline = spine.getOutline();
            if( !outline.contains( _x, _y ) ) continue;
            found = true;
            Calibration cal = imp.getCalibration();
            if( m_neuronDisplayD == null ){
                MyCustomFitter.setInitialC( 8. * cal.pixelWidth );
                MyCustomFitter.setInitialD( 16. * cal.pixelWidth );
                SpineStatisticsGenerator.determineSpineCharacteristics( spine, _x, _y, 10., 20., 1.25, 3., 0.8, imp, m_original.duplicate() );
            }
            else{
                MyCustomFitter.setInitialC( m_neuronDisplayD.getInitialC() );
                MyCustomFitter.setInitialD( m_neuronDisplayD.getInitialD() );
                //line parameter are in pixels
                double fromW = m_neuronDisplayD.getFromLineWidth() / cal.pixelWidth, toW = m_neuronDisplayD.getToLineWidth() / cal.pixelWidth, stepW = m_neuronDisplayD.getStepLineWidth() / cal.pixelWidth, h = m_neuronDisplayD.getLineHeight() / cal.pixelWidth;
                SpineStatisticsGenerator.determineSpineCharacteristics( spine, _x, _y, fromW, toW, stepW, h, m_neuronDisplayD.getMinFitGoodness(), imp, m_original.duplicate() );
            }

            DoublePolygon head = new DoublePolygon( spine.getHead() );
            PolygonRoi proi = new PolygonRoi( head.getPolygon(), Roi.FREEROI );
            ByteProcessor bp = new ByteProcessor( this.getWidth(), this.getHeight() );
            bp.setRoi( proi );
            int options = Measurements.CENTROID+Measurements.ELLIPSE;
            ImageStatistics stats = ImageStatistics.getStatistics( bp, options, null );
            double dx = stats.major*Math.cos(stats.angle/180.0*Math.PI)/2.0;
            double dy = - stats.major*Math.sin(stats.angle/180.0*Math.PI)/2.0;
            double x1 = stats.xCentroid - dx;
            double x2 = stats.xCentroid + dx;
            double y1 = stats.yCentroid - dy;
            double y2 = stats.yCentroid + dy;
            double aspectRatio = stats.minor/stats.major;
            EllipseRoi roi2 = new EllipseRoi(x1,y1,x2,y2,aspectRatio);
            spine.setShapeHead( roi2 );
            roi2.setImage( imp );

            if( m_saDialog == null )
                m_saDialog = new SpineAnalysisDialog( m_spines, imp.getCalibration(), this );
            else
                m_saDialog.changeCurrentSpine( n );

        }
    }

    protected void drawRoi( Roi roi, Graphics2D g ) {
        if (roi == null) {
            return;
        }
        if( roi instanceof EllipseRoi ){
            EllipseRoi ell = ( EllipseRoi )roi;
            ell.draw( g );
        }
        else
            drawFloatPolygon( roi.getFloatPolygon(), 0, 0, g );
    }
    
    protected void drawDoublePolygon( DoublePolygon p, int startx, int starty, Graphics2D g ) {
        int[] xs = new int[p.npoints];
        int[] ys = new int[p.npoints];
        for (int j = 0; j < p.npoints; j++) {
            xs[j] = screenX( ( int )( startx + p.xpoints[j] ) );
            ys[j] = screenY( ( int )( starty + p.ypoints[j] ) );
        }
        g.draw(new Polygon(xs, ys, xs.length));
    }

    protected void drawFloatPolygon( FloatPolygon p, int startx, int starty, Graphics2D g ) {
        int[] xs = new int[p.npoints];
        int[] ys = new int[p.npoints];
        for (int j = 0; j < p.npoints; j++) {
            xs[j] = screenX( ( int )( startx + p.xpoints[j] ) );
            ys[j] = screenY( ( int )( starty + p.ypoints[j] ) );
        }
        g.draw(new Polygon(xs, ys, xs.length));
    }
    
    protected void draw( Graphics2D _g, DoublePolygon _p ) {
        for( int n = 1; n <= _p.npoints; n++ ){
            double x1 = _p.xpoints[n-1], y1 = _p.ypoints[n-1];
            double x2 = _p.xpoints[n%_p.npoints], y2 = _p.ypoints[n%_p.npoints];
            draw( _g, x1, y1, x2, y2 );
        }
    }

    protected void drawPolygon(Polygon p, Graphics2D g) {
        int[] xs = new int[p.npoints];
        int[] ys = new int[p.npoints];
        for (int j = 0; j < p.npoints; j++) {
            xs[j] = screenX(p.xpoints[j]);
            ys[j] = screenY(p.ypoints[j]);
        }
        g.draw(new Polygon(xs, ys, xs.length));
    }

    void draw(Graphics2D g, double ax, double ay, double bx, double by) {
        if (g == null) {
            ImageProcessor ip = imp.getProcessor();
            ip.drawLine((int) ax,
                    (int) ay,
                    (int) bx,
                    (int) by);
            return;
        }

        double m = magnification;
        double x0 = (ax - srcRect.x) * m;
        double y0 = (ay - srcRect.y) * m;
        double x1 = (bx - srcRect.x) * m;
        double y1 = (by - srcRect.y) * m;
        g.draw(new Line2D.Double( x0, y0, x1, y1 ) );
    }

    void drawRect(Graphics2D g, double ax, double ay, double bx, double by) {
        if (g == null) {
            ImageProcessor ip = imp.getProcessor();
            ip.drawRect((int) ax, (int) ay, (int) bx, (int) by);
            return;
        }

        double m = magnification;
        double x0 = (ax - ( double )srcRect.x) * m;
        double y0 = (ay - ( double )srcRect.y) * m;
        double x1 = (ax + bx - ( double )srcRect.x) * m;
        double y1 = (ay + by - ( double )srcRect.y) * m;
        g.draw(new Line2D.Double( x0, y0, x1, y0 ) );
        g.draw(new Line2D.Double( x1, y0, x1, y1 ) );
        g.draw(new Line2D.Double( x1, y1, x0, y1 ) );
        g.draw(new Line2D.Double( x0, y1, x0, y0 ) );
    }

    void draw(Graphics2D g, double ax, double ay, double pointRadius) {
        double m = magnification;
        double x0 = (ax - srcRect.x) * m;
        double y0 = (ay - srcRect.y) * m;
        g.draw( new Ellipse2D.Double( x0 - pointRadius, y0 - pointRadius, pointRadius + pointRadius, pointRadius + pointRadius) );
    }
    void fill(Graphics2D g, double ax, double ay, double pointRadius) {
        double m = magnification;
        double x0 = (ax - srcRect.x) * m;
        double y0 = (ay - srcRect.y) * m;
        g.fill( new Ellipse2D.Double( x0 - pointRadius, y0 - pointRadius, pointRadius + pointRadius, pointRadius + pointRadius) );
    }

    protected void drawContour(Graphics2D g, Point.Double[] points) {
        for (int i = 1; i < points.length; i++) {
            Point.Double a = points[i - 1];
            Point.Double b = points[i];
            draw(g, a.x, a.y, b.x, b.y);
            fill(g, b.x, b.y, m_radiusDisplay);
        }
    }
    protected void drawContour(Graphics2D g, double[] xpoints, double[] ypoints) {
        for (int i = 1; i < xpoints.length; i++) {
            draw(g, xpoints[i-1], ypoints[i-1], xpoints[i], ypoints[i]);
            fill(g, xpoints[i], ypoints[i], m_radiusDisplay);
        }
    }

    protected void drawContourCtrlPoints(Graphics2D g, ArrayList < CatmullCurve[] > ccs) {
        if(ccs == null)return;
        g.setColor(Color.RED);
        for(int k = 0; k < ccs.size(); k++){
            CatmullCurve[] contours = ccs.get(k);
            for (int j = 0; j < contours.length; j++) {
                drawContour(g, contours[j].controlPoints.xpoints, contours[j].controlPoints.ypoints);
            }
        }
    }

    protected void drawContourPoints(Graphics2D g, ArrayList < CatmullCurve[] > ccs) {
        if(ccs == null)return;
        g.setColor(Color.RED);
        for(int k = 0; k < ccs.size(); k++){
            CatmullCurve[] contours = ccs.get(k);
            for (int j = 0; j < contours.length; j++) {
                drawContour(g, contours[j].points.xpoints, contours[j].points.ypoints);
            }
        }
    }
    
    protected void drawEdges( Graphics2D g, ArrayList < EdgeGraph > _edges ) {
        g.setColor( Color.blue );
        for( EdgeGraph e : _edges ){
            EdgeT et = ( EdgeT )e;
            Point2D.Double p1 = et.getV1().getPos(), p2 = et.getV2().getPos();
            draw( g, p1.x, p1.y, p2.x, p2.y );
        }
    }

    protected void drawDelaunay( Graphics2D g, DelaunayT[] _dt) {
        if ( m_displayTriangles ) {
            g.setColor(Color.red);
            for( int n = 0; n < _dt.length; n++ ){
                ArrayList < TriangleT > triangles = _dt[n].m_triangles2;
                for( int i = 0; i < triangles.size(); i++ ){
                    TriangleT t = triangles.get( i );
                    if( t.isTrimmed() ) continue;
                    PointT a = t.getA(), b = t.getB(), c = t.getC();
                    draw( g, a.x, a.y, b.x, b.y );
                    draw( g, b.x, b.y, c.x, c.y );
                    draw( g, c.x, c.y, a.x, a.y );
                }
            }
            g.setColor( Color.magenta);
            for( int n = 0; n < _dt.length; n++ ){
                ArrayList < TriangleT > triangles = _dt[n].m_triangles2;
                for( int i = 0; i < triangles.size(); i++ ){
                    TriangleT t = triangles.get( i );
                    if( !t.isTrimmed() ) continue;
                    PointT a = t.getA(), b = t.getB(), c = t.getC();
                    draw( g, a.x, a.y, b.x, b.y );
                    draw( g, b.x, b.y, c.x, c.y );
                    draw( g, c.x, c.y, a.x, a.y );
                }
            }
        }
        if ( m_displayOutline ) {
            g.setStroke(new BasicStroke(2.f));
            g.setColor( new Color(255, 221, 0) );
                        
            for(NeuronObject dendrite : m_dendrites){
                Line2D.Double[] shape = dendrite.getShape();
                for( int j = 0; j < shape.length; j++ ){
                    Line2D.Double l = shape[j];
                    draw( g, l.x1, l.y1, l.x2, l.y2 );
                }
            }
        }
    }
    
    protected void drawSkeleton( Graphics2D g, ArrayList < EdgeWeightedGraph > _graphs, Color _cSpine, Color _cDen ) {
        for( int i = 0; i < _graphs.size(); i++ ){
            EdgeWeightedGraph graph = _graphs.get( i );
            for( Edge e : graph.edges() ){
                EdgeGraph edge = e.edge();
                if( edge.isDendrite() )
                    g.setColor( _cDen );
                else
                    g.setColor( _cSpine );
                SeedGraph v1 = edge.getV1();
                SeedGraph v2 = edge.getV2();
                draw( g, v1.getX(), v1.getY(), v2.getX(), v2.getY() );
            }
        }
        for( int i = 0; i < _graphs.size(); i++ ){
            ArrayList < SeedGraph > seeds = _graphs.get( i ).getSeeds();
            for(int j = 0; j < seeds.size(); j++){
                SeedGraph v1 = seeds.get(j);
                if(v1.isMarked())
                    g.setColor( _cDen );
                else
                    g.setColor( _cSpine );
                fill( g, v1.getX(), v1.getY(), m_radiusDisplay );
            }
        }
    }

    protected void drawSkeletonLine( Graphics2D g, ArrayList < Line2D.Double > _lines ) {
        for (int i = 0; i < _lines.size(); i++) {
            Line2D.Double l = _lines.get(i);
            draw(g, l.getX1(), l.getY1(), l.getX2(), l.getY2());
        }
    }
    
    protected void drawSkeletonLine( Graphics2D g, Color c ) {
        g.setColor( new Color(128, 128, 0) );
        ArrayList < ArrayList < NeuronObject > > objs = m_dss.getDendrites();
        for( int i = 0; i < objs.size(); i++ ){
            ArrayList < NeuronObject > objList = objs.get( i );
            for( int j = 0; j < objList.size(); j++ ){
                CatmullCurve [] ccs = objList.get( j ).getSkeletons();
                if( ccs != null ){
                    for( int k = 0; k < ccs.length; k++ ){
                        CatmullCurve cc = ccs[k];
                        for( int l = 1; l < cc.points.npoints; l++ ){
                            draw( g, cc.points.xpoints[l-1], cc.points.ypoints[l-1], cc.points.xpoints[l], cc.points.ypoints[l] );
                            fill( g, cc.points.xpoints[l-1], cc.points.ypoints[l-1], m_radiusDisplay );
                            fill( g, cc.points.xpoints[l], cc.points.ypoints[l], m_radiusDisplay );
                        }
                    }
                }
            }
        }
        
        objs = m_dss.getSpines();
        for( int i = 0; i < objs.size(); i++ ){
            ArrayList < NeuronObject > objList = objs.get( i );
            for( int j = 0; j < objList.size(); j++ ){
                CatmullCurve [] ccs = objList.get( j ).getSkeletons();
                if( ccs != null ){
                    for( int k = 0; k < ccs.length; k++ ){
                        CatmullCurve cc = ccs[k];
                        if(cc != null){
                            for( int l = 1; l < cc.points.npoints; l++ ){
                                draw( g, cc.points.xpoints[l-1], cc.points.ypoints[l-1], cc.points.xpoints[l], cc.points.ypoints[l] );
                            }
                        }
                    }
                }
            }
        }
    }
    
    protected void drawAnalyzedSpines( Graphics2D _g, int _index ){
        int indexS = 0, indexNShape = 0;
        if( m_saDialog != null ){
            indexS = m_saDialog.getCurrentSpine();
            indexNShape = m_saDialog.getCurrentNeckline();
        }
        _g.setColor( Color.red );
        NeuronObject spine = m_spines.get( _index );
        if( m_displaySkeletonLongest ){
            ArrayList < Point2D.Double > points = spine.m_skeletonLongest;
            _g.setColor( Color.PINK );
            if( points != null )
                for( int n = 1; n < points.size(); n++ )
                    draw( _g, points.get( n ).x, points.get( n ).y, points.get( n - 1 ).x, points.get( n - 1 ).y );
        }

        if( m_displayNeckSkeleton ){
            ArrayList < Point2D.Double > points = spine.getSkeletonPath();
            _g.setColor( Color.RED );
            if( points != null ){
                for( int n = 1; n < points.size(); n++ )
                    draw( _g, points.get( n ).x, points.get( n ).y, points.get( n - 1 ).x, points.get( n - 1 ).y );
            }
        }

        if( m_displayNeckline ){
            ArrayList < NeckShape > lines = spine.getNeckAnalysis();
            if( lines != null ){
                for( int n = 0; n < lines.size(); n++ ){
                    boolean selected = _index == indexS && n == indexNShape;
                    if( selected )
                        _g.setColor( Color.ORANGE );
                    else
                        _g.setColor( Color.red );
                    StraightLine line = lines.get( n ).getLineBasis();
                    double w = m_displayNecklineUsedForFit ? (lines.get( n ).getWidth() / 2.) : (lines.get( n ).getFittedWidth() / 2.);
                    Point2D.Double p1 = line.findPoint( line.getP1().getX(), line.getP1().getY(), -w );
                    Point2D.Double p2 = line.findPoint( line.getP1().getX(), line.getP1().getY(), w );

                    draw( _g, p1.getX(), p1.getY(), p2.getX(), p2.getY() );
                }
            }
        }

        if( m_displayNecklineShape ){
            ArrayList < NeckShape > lines = spine.getNeckAnalysis();
            if( lines != null ){
                for( int n = 0; n < lines.size(); n++ ){
                    boolean selected = _index == indexS && n == indexNShape;
                    if( selected )
                        _g.setColor( new Color( 0, 0, 255, 75 ) );
                    else
                        _g.setColor( new Color( 255, 0, 0, 75 ) );
 
                    PolygonRoi proi = m_displayNecklineUsedForFit ? lines.get(n).getNeckShapeUsedForFit() : lines.get(n).getNeckShapeFromFit();
                    FloatPolygon fp = proi.getFloatPolygon();
                    Point2D.Double pp1 = new Point2D.Double(fp.xpoints[0], fp.ypoints[0]), pp2 = new Point2D.Double(fp.xpoints[1], fp.ypoints[1]);
                    Point2D.Double pp3 = new Point2D.Double(fp.xpoints[2], fp.ypoints[2]), pp4 = new Point2D.Double(fp.xpoints[3], fp.ypoints[3]);
                    
                    double m = magnification;
                    int [] xs = new int[4], ys = new int [4];
                    xs[0] = ( int )( ( pp1.getX() - srcRect.x ) * m ); xs[1] = ( int )( ( pp2.getX() - srcRect.x ) * m ); xs[2] = ( int )( ( pp3.getX() - srcRect.x ) * m ); xs[3] = ( int )( ( pp4.getX() - srcRect.x ) * m );
                    ys[0] = ( int )( ( pp1.getY() - srcRect.y ) * m ); ys[1] = ( int )( ( pp2.getY() - srcRect.y ) * m ); ys[2] = ( int )( ( pp3.getY() - srcRect.y ) * m ); ys[3] = ( int )( ( pp4.getY() - srcRect.y ) * m );
                    _g.fill( new Polygon( xs, ys, xs.length ) );

                }
            }
        }

        if( m_displaySpineHead ){
            _g.setColor( Color.GREEN );
            ArrayList < Point2D.Double > points = spine.getHead();
            if( points != null ){
                int size = points.size();
                for( int n = 1; n <= size; n++ ){
                    Point2D.Double prec = points.get( n - 1 ), cur = points.get( n % size );
                    draw( _g, prec.getX(), prec.getY(), cur.getX(), cur.getY() );
                }
            }
        }
        
        if( m_displaySpineNeck ){
            _g.setColor( Color.RED );
            ArrayList < Point2D.Double > points = spine.getNeck();
            if( points != null ){
                int size = points.size();
                for( int n = 1; n <= size; n++ ){
                    Point2D.Double prec = points.get( n - 1 ), cur = points.get( n % size );
                    draw( _g, prec.getX(), prec.getY(), cur.getX(), cur.getY() );
                }
            }
        }

        if( m_displayHeadEllApprox ){
            Roi headShape = spine.getShapeHead();
            if( headShape != null ){
                headShape.setStrokeColor( Color.PINK );
                drawRoi( headShape, _g );
            }
        }

        if( m_displaySpineLabel ){
            Color c = ( _index == indexS ) ? Color.RED : Color.YELLOW;
            drawSpineLabel( _g, _index, spine.getBoundingBox(), c );
        }
        
        if( m_displayDiscreteRoi ){
          _g.setColor(new Color(0, 200, 0));
            draw(_g, spine.getOutline());
        }
        
        if( spine.m_intenseP != null ){
            _g.setColor(Color.WHITE);
            fill( _g, spine.m_intenseP.getX(), spine.m_intenseP.getY(), m_radiusDisplay );
        }
        if( spine.m_precP != null ){
            _g.setColor(Color.MAGENTA);
            fill( _g, spine.m_precP.getX(), spine.m_precP.getY(), m_radiusDisplay );
            fill( _g, spine.m_succP.getX(), spine.m_succP.getY(), m_radiusDisplay );
        }
        if( spine.m_proj != null ){
            _g.setColor(Color.GREEN);
            fill( _g, spine.m_proj.getX(), spine.m_proj.getY(), m_radiusDisplay );
        }
    }
    
    protected void drawSpineLabel( Graphics2D g, int index, Rectangle2D.Double r, Color color ) {
            int x = screenX( ( int )r.x );
            int y = screenY( ( int )r.y );
            double mag = getMagnification();
            int width = (int) (r.width * mag);
            int height = (int) (r.height * mag);
            int size = width > 40 && height > 40 ? 12 : 9;
            if (size == 12) {
                g.setFont(largeFont);
            } else {
                g.setFont(smallFont);
            }
            boolean drawingList = index >= LIST_OFFSET;
            if (drawingList) {
                index -= LIST_OFFSET;
            }
            String label = "" + (index + 1);
            FontMetrics metrics = g.getFontMetrics();
            int w = metrics.stringWidth(label);
            x = x + width / 2 - w / 2;
            y = y + height / 2 + Math.max(size / 2, 6);
            int h = metrics.getHeight();
            g.setColor(color);
            g.fillRoundRect(x - 1, y - h + 2, w + 1, h - 3, 5, 5);
            g.setColor(Color.BLACK);
            g.drawString(label, x, y - 2);
        }

    public void changeOriginalImage( ImageProcessor _im ){
        m_original = _im;
    }
    public void setOriginalImage(){
        imp.setProcessor( m_original.duplicate() );
    }
    public void setBinarizedImage(){
        imp.setProcessor( m_binary.duplicate() );
    }

    private void createCustomCursor(){
        //Create an empty byte array
        byte[]imageByte=new byte[0];
        //Create image for cursor using empty array
        Image cursorImage=Toolkit.getDefaultToolkit().createImage(imageByte);
        m_customCursor = Toolkit.getDefaultToolkit().createCustomCursor( cursorImage, new Point( 0,0 ), "empty" );
    }

    public Cursor getCustomCursor(){
        return m_customCursor;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        super.mouseEntered(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        super.mouseExited(e);
        if ( m_cursor != null ) {
            setCursor( m_cursor );
            m_cursor = null;
            m_mouse = null;
            repaint();
        }
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
        double x0 = offScreenXD( e.getX() );
        double y0 = offScreenYD( e.getY() );

        boolean found = false;
        for( int n = 0; n < m_spines.size() && !found; n++ ){
            NeuronObject spine = m_spines.get( n );
            DoublePolygon outline = spine.getOutline();
            if( !outline.contains( x0, y0 ) ) continue;
            found = true;
            TriangleT t = spine.insideSpecificTriangle( new Point2D.Double( x0, y0 ) );
            if( t == null ) continue;
            StraightLine l1 = new StraightLine( t.getA().getX(), t.getA().getY(), t.getB().getX(), t.getB().getY(), StraightLine.PARALLELE_LINE );
            StraightLine l2 = new StraightLine( t.getB().getX(), t.getB().getY(), t.getC().getX(), t.getC().getY(), StraightLine.PARALLELE_LINE );
            StraightLine l3 = new StraightLine( t.getC().getX(), t.getC().getY(), t.getA().getX(), t.getA().getY(), StraightLine.PARALLELE_LINE );
            Point2D.Double inter1 = l1.orthProjection( x0, y0 );
            Point2D.Double inter2 = l2.orthProjection( x0, y0 );
            Point2D.Double inter3 = l3.orthProjection( x0, y0 );
            double d1 = inter1.distance( x0, y0 );
            double d2 = inter2.distance( x0, y0 );
            double d3 = inter3.distance( x0, y0 );
            if( d1 < d2 ){
                if( d1 < d3 )
                    m_separationNeckHead = l1;
                else
                    m_separationNeckHead = l3;
            }
            else{
                if( d2 < d3 )
                    m_separationNeckHead = l2;
                else
                    m_separationNeckHead = l3;
            }
        }
        if( !found )
            m_separationNeckHead = null;
        repaint();
    }

    public void setDisplayOutline( boolean _val ){
        m_displayOutline = _val;
    }
    public boolean getDisplayOutline(){
        return m_displayOutline;
    }
    public void setDisplayTriangles( boolean _val ){
        m_displayTriangles = _val;
    }
    public boolean getDisplayTriangles(){
        return m_displayTriangles;
    }
    public void setDisplayGraph( boolean _val ){
        m_displayGraph = _val;
    }
    public boolean getDisplayGraph(){
        return m_displayGraph;
    }
    public void setDisplaySkeleton( boolean _val ){
        m_displaySkeleton = _val;
    }
    public boolean getDisplaySkeleton(){
        return m_displaySkeleton;
    }
    public ImageProcessor getOriginalImage(){
        return m_original;
    }
    public boolean getDisplayRois(){
        return m_displayRois;
    }
    public void setDisplayRois( boolean _val ){
        m_displayRois = _val;
    }
    void setDisplayCurrentSpine( boolean _val ) {
        m_currentAnalyzedSpine = _val;
    }
    public void setDisplayAnalyzedSpine( boolean _val ){
        m_displaySpineAnalyzed = _val;
    }
    
    public void setActinDisplaySpineShapeDiscrete( boolean _val ){
        m_displayActinSpineShapeDiscrete = _val;
    }
    
    void setDisplaySpineHead( boolean _val ) {
        m_displaySpineHead = _val;
    }

    void setDisplaySpineNeck( boolean _val ) {
        m_displaySpineNeck = _val;
    }

    void setDisplayNecklines( boolean _val ) {
        m_displayNeckline = _val;
    }

    void setDisplayNecklineShapes( boolean _val ) {
        m_displayNecklineShape = _val;
    }

    void setDisplayNeckSkeleton( boolean _val ) {
        m_displayNeckSkeleton = _val;
    }
    
    public void setActinDisplaySkeletonLongest( boolean _val ){
        m_displaySkeletonLongest = _val;
    }

    void setDisplayHeadEllipsoidApprox( boolean _val ) {
        m_displayHeadEllApprox = _val;
    }
    
    void setDisplaySpineLabel( boolean _val ) {
        m_displaySpineLabel = _val;
    }//
    
    void setDisplayDiscreteRoi( boolean _val ) {
        m_displayDiscreteRoi = _val;
    }
    
    void setSelectionSpine( boolean _val ) {
        m_selectionSpine = _val;
    }
    
    void setSelectionNeckline( boolean _val ) {
        m_selectionNeckline = _val;
    }
    
    void setDefinitionNeckEnd( boolean _val ) {
        m_definitionNeckEnd = _val;
    }
    
    void setDeleteSpine( boolean _val ) {
        m_deleteSpine = _val;
    }

    public void setRois( Roi [] _rois, int _step ){
        m_rois = _rois;
    }

    public void setDirectoryOriginalImage( String _s ){
        m_directoryOriginalImage = _s;
    }
    public String getDirectoryOriginalImage(){
        return m_directoryOriginalImage;
    }
 
    public void addSpineAnalyzed( DoublePolygon _shape ){
        m_shapeSpines.add( _shape );
    }
    public DoublePolygon getSpineAnalyzed( int _index ){
        return m_shapeSpines.get( _index );
    }
    
    public int getNbSpineAnalyzedDiscrete(){
        return m_shapeSpinesDiscrete.size();
    }
    public void addSpineAnalyzedDiscrete( Roi _shape ){
        m_shapeSpinesDiscrete.add( _shape );
    }
    public Roi getSpineAnalyzedDiscrete( int _index ){
        return m_shapeSpinesDiscrete.get( _index );
    }
    public void addSpine( NeuronObject _spine ){
        m_spines.add( _spine );
    }
    public ArrayList < NeuronObject > getAnalyzedSpines(){
        return m_spines;
    }
    
    protected ImagePlus myFlatten(){
        String title = imp.getTitle();
        title = title.substring(0, title.indexOf(".tif"));
        title += "_flatten.tif";
        ImagePlus flattenImp = new ImagePlus( title, offScreenImage );
        flattenImp.show();
        return flattenImp;
    }

    public void itemStateChanged( ItemEvent e ) {
        Object o = e.getSource();
        if( o instanceof Choice )
            repaint();
    }
    public void actionPerformed( ActionEvent e ) {
        Object o = e.getSource();
        if( o instanceof JButton )
            repaint();
    }
        
    public void keyTyped( KeyEvent e ) {
    }

    public void keyPressed( KeyEvent e ) {
        if( e.getKeyCode() == KeyEvent.VK_A )
            analyseCurrentRoiForSpine();
        else if( e.getKeyCode() == KeyEvent.VK_Q )
            deleteCurrentSpine();
        else if( e.getKeyCode() == KeyEvent.VK_O )
            analyseAllSpines();
        else if( e.getKeyCode() == KeyEvent.VK_DELETE  )
            deleteCurrentSpine();
        else if( e.getKeyCode() == KeyEvent.VK_D  ){
            Roi roi = this.getImage().getRoi();
            if( roi != null )
                deleteSpinesInROI(roi);
        }
        else if( e.getKeyCode() == KeyEvent.VK_S  ){
            m_dss.saveDelaunayAsSVG(this.getImage().getRoi());
        }
        else if( e.getKeyCode() == KeyEvent.VK_W  ){
            saveSpinesAsSVG(this.getImage().getRoi());
        }
    }

    public void keyReleased( KeyEvent e ) {
    }
    
    public void analyseAllSpinesMainDendrite(){
        if(m_dss.getGraphDetector().getGraphs().isEmpty()) return;
        
        for(int n = m_spines.size() - 1; n >= 0; n--)
                m_spines.remove( n );
        
        ArrayList < ArrayList < EdgeGraph > > spines = m_dss.getGraphDetector().getAllSpinesFromOneGraph(0, imp.getWidth(), imp.getHeight(), imp.getCalibration(), m_neuronDisplayD.getDistanceMergeGraphNodes());
        for(ArrayList < EdgeGraph > s : spines){
            NeuronObject spine = new NeuronObject( s, false, 100., 500. );
            boolean touchingBorder = spine.createDiscreteSpineShape( m_binary );
            if(!touchingBorder && spine.getDiscreteShape() != null){
                this.addSpineAnalyzed( spine.getOutline() );
                this.addSpineAnalyzedDiscrete( spine.getDiscreteShape() );
                this.addSpine( spine );
            }
            else{
                for(EdgeGraph eTmp : spine.getEdges()){
                    eTmp.setDendrite(true);
                    eTmp.getV1().setMarked(true);
                    eTmp.getV2().setMarked(true);
                }
            }
        }
        m_dss.generateSpinesAndDendrites();
        m_dendrites = m_dss.getDendritesInOneList();
        if( m_saDialog == null ){
            m_saDialog = new SpineAnalysisDialog( m_spines, imp.getCalibration(), this );
            m_saDialog.addWindowListener( this );
        }
        repaint();
    }
    
    public void analyseAllSpines(){
        if(m_dss.getGraphDetector().getGraphs().isEmpty()) return;
        
        for(int n = m_spines.size() - 1; n >= 0; n--)
                m_spines.remove( n );
        
        ArrayList < ArrayList < EdgeGraph > > spines = m_dss.getGraphDetector().getAllSpinesAsGraph(imp.getWidth(), imp.getHeight(), imp.getCalibration(), m_neuronDisplayD.getDistanceMergeGraphNodes());
        for(ArrayList < EdgeGraph > s : spines){
            NeuronObject spine = new NeuronObject( s, false, 100., 500. );
            boolean touchingBorder = spine.createDiscreteSpineShape( m_binary );
            if(!touchingBorder && spine.getDiscreteShape() != null){
                this.addSpineAnalyzed( spine.getOutline() );
                this.addSpineAnalyzedDiscrete( spine.getDiscreteShape() );
                this.addSpine( spine );
            }
            else{
                for(EdgeGraph eTmp : spine.getEdges()){
                    eTmp.setDendrite(true);
                    eTmp.getV1().setMarked(true);
                    eTmp.getV2().setMarked(true);
                }
            }
        }
        m_dss.generateSpinesAndDendrites();
        m_dendrites = m_dss.getDendritesInOneList();
        if( m_saDialog == null ){
            m_saDialog = new SpineAnalysisDialog( m_spines, imp.getCalibration(), this );
            m_saDialog.addWindowListener( this );
        }
        repaint();
    }
    
    public void deleteSpinesInROI(Roi _roi){
        if( _roi != null ){
            int indexes[] = new int[m_spines.size()], nbSpinesToRemove = 0, cpt = 0;
            for(NeuronObject spine : m_spines){
                ArrayList < EdgeGraph > edges = spine.getEdges();
                boolean found = false;
                for(int n = 0; n < edges.size() && !found; n++){
                    EdgeGraph edge = edges.get(n);
                    found = _roi.contains((int)edge.getV1().getX(), (int)edge.getV1().getY()) ||
                            _roi.contains((int)edge.getV2().getX(), (int)edge.getV2().getY());
                }
                if(found)
                    indexes[nbSpinesToRemove++] = cpt;
                cpt++;
            }
            for(int n = nbSpinesToRemove - 1; n >= 0; n--){
                NeuronObject spine = m_spines.get(indexes[n]);
                for (EdgeGraph e : spine.getEdges())
                    e.setDendrite(true);
                for(SeedGraph sg : spine.getGraph().getSeeds())
                    sg.setMarked(true);
                m_spines.remove( indexes[n] );
            }
            
            if(!m_spines.isEmpty()){
                int currentSpine = m_saDialog.getCurrentSpine();
                if( currentSpine >= m_spines.size() )
                    currentSpine = m_spines.size() - 1;
                if( currentSpine >= 0 )
                    m_saDialog.changeCurrentSpine( currentSpine );
            }
            m_dss.generateSpinesAndDendrites();
            m_dendrites = m_dss.getDendritesInOneList();
        }
    }
    
    public void analyseCurrentRoiForSpine(){
        Roi roi = this.getImage().getRoi();
        if( roi != null ){
            deleteSpinesInROI(roi);
            
            Rectangle r = roi.getBounds();
            double cx = r.getX() + r.getWidth() / 2., cy = r.getY() + r.getHeight() / 2.;
            double x = r.x, y = r.y;
            double radius = Math.sqrt( ( cx - x ) * ( cx - x ) + ( cy - y ) * ( cy - y ) );
            m_gedges = m_dss.setRoiAsSpine( cx, cy, radius, roi );
            m_dendrites = m_dss.getDendritesInOneList();
            
            NeuronObject spine = new NeuronObject( m_gedges, false, 100., 500. );
            spine.createDiscreteSpineShape( m_binary );

            this.addSpineAnalyzed( spine.getOutline() );
            this.addSpineAnalyzedDiscrete( spine.getDiscreteShape() );
            this.addSpine( spine );
            this.getImage().killRoi();
            if( m_saDialog == null ){
                m_saDialog = new SpineAnalysisDialog( m_spines, imp.getCalibration(), this );
                m_saDialog.addWindowListener( this );
            }
        }
        repaint();
    }
    
    public void deleteCurrentSpine(){
        int currentSpine = m_saDialog.getCurrentSpine();
        NeuronObject spine = m_spines.get(currentSpine);
        for (EdgeGraph e : spine.getEdges())
            e.setDendrite(true);
        for(SeedGraph sg : spine.getGraph().getSeeds())
            sg.setMarked(true);
        m_spines.remove( currentSpine );
        if( currentSpine >= m_spines.size() )
            currentSpine--;
        if( currentSpine >= 0 )
            m_saDialog.changeCurrentSpine( currentSpine );
        m_dss.generateSpinesAndDendrites();
        m_dendrites = m_dss.getDendritesInOneList();
        repaint();
    }
   
    public TextWindow exportDataSpines(){
        Calibration cal = imp.getCalibration();
        String directory = this.getDirectoryOriginalImage();
        if( directory == null ){
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
            int returnVal = chooser.showOpenDialog( this );
            if( returnVal == JFileChooser.APPROVE_OPTION ){
                File file = chooser.getSelectedFile();
                directory = file.getName();
            }
            else
                return null;
        }
        String generalName = this.getImage().getTitle();
        generalName = generalName.substring( 0, generalName.lastIndexOf( "." ) );

        String nameXls = generalName + ".xls";
        boolean overwrite = false;
        if( new File( directory + nameXls ).exists() ){
            YesNoCancelDialog dialog = new YesNoCancelDialog( null, "Overwrite result file ?", "Do you want to overwrite " + nameXls );
            if( dialog.yesPressed() )
                overwrite = true;
            else if( dialog.cancelPressed() )
                overwrite = false;
        }
        else
            overwrite = true;                
        if( !overwrite ) return null;
        if( m_spines == null || m_spines.isEmpty() ) return null;
        DecimalFormat df = new DecimalFormat( "#.###" );
        TextWindow textw = null;
        String headings = "# Spine (" + cal.getXUnit() + ")\tLength neck (" + cal.getXUnit() + ")\tLength spine (" + cal.getXUnit() + ")\tRatio (%)\tMinor axis (" + cal.getXUnit() + ")\tMajor axis (" + cal.getXUnit() + ")\tAspect Ratio\tPerimeter Head (" + cal.getXUnit() + ")\tArea Head (" + cal.getXUnit() + "²)\tSmallest neckwidth (" + cal.getXUnit() + ")\tMedian neckwidth (" + cal.getXUnit() + ")\tAverage neckwidth (" + cal.getXUnit() + ")\t# Neckline\tFWHM (" + cal.getXUnit() + ")\tFit goodness";
        String title = this.getImage().getTitle();
        int index = title.lastIndexOf( "." );
        title = title.substring( 0, index );
        title += ".xls";
        textw = new TextWindow(title, headings, "", 1200, 800);
        textw.setVisible( true );
        for( int n = 0; n < m_spines.size(); n++ ){
            NeuronObject spine = m_spines.get( n );
            ArrayList < Point2D.Double > head = spine.getHead();
            double minD = 0., avgD = 0.;
            double perimeter = 0., area = 0.;
            if( head != null ){
                DoublePolygon tmp = new DoublePolygon( head );
                perimeter = tmp.getPerimeter();
                area = tmp.getArea();
            }
            ArrayList < NeckShape > lines = spine.getNeckAnalysis();                      
            if( lines == null || lines.isEmpty() ) continue;
            double[] allDs = new double[lines.size()];
            minD = Double.MAX_VALUE;
            avgD = 0.;
            for( int i = 0; i < lines.size(); i++ ){
                double d = lines.get( i ).getD();
                allDs[i] = d;
                avgD += d;
                if( d < minD )
                    minD = d;
            }
            avgD /= ( double )lines.size();
            Arrays.sort(allDs);
            double median;
            if (allDs.length % 2 == 0)
                median = ((double)allDs[allDs.length/2] + (double)allDs[allDs.length/2 - 1])/2;
            else
                median = (double) allDs[allDs.length/2];
            double lengthInfos [] = spine.getLengthInfos();
            double headApproxInfos [] = spine.getShapeHead().getParams();
            double dx = headApproxInfos[2] - headApproxInfos[0];
            double dy = headApproxInfos[3] - headApproxInfos[1];
            double major = Math.sqrt( dx*dx + dy*dy );
            double minor = major * headApproxInfos[4];
            String s = "" + ( n + 1 ) + "\t" + df.format( lengthInfos[1] * cal.pixelWidth ) + "\t" + df.format( lengthInfos[2] * cal.pixelWidth ) + "\t" + df.format( lengthInfos[0] * 100. ) + "\t" + df.format( minor * cal.pixelWidth ) + "\t" + df.format( major * cal.pixelWidth ) + "\t" + df.format( headApproxInfos[4] ) + "\t" + df.format( perimeter * cal.pixelWidth ) + "\t" + df.format( area * ( cal.pixelWidth * cal.pixelWidth ) ) + "\t" + df.format( minD ) + "\t" + df.format( median ) + "\t" + df.format( avgD ) + "\t";
            if( lines != null && !lines.isEmpty() ){
                for( int i = 0; i < lines.size(); i++ ){
                    s += ( i + 1 ) + "\t" + df.format( lines.get( i ).getD() ) + "\t" + df.format( lines.get( i ).getGoodnessFit() ) + "\n";
                    s += "\t\t\t\t\t\t\t\t\t\t\t\t";
                }
            }
            else{
                s += "0\t0\t0";
            }
            textw.append( s );
        }
        textw.getTextPanel().saveAs( directory + nameXls );
        return textw;
    }
    
    public void importAllProfilesAllSpines(){
        String dir = null;
        if( dir == null ){
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
            int returnVal = chooser.showOpenDialog( this );
            if( returnVal == JFileChooser.APPROVE_OPTION ){
                File file = chooser.getSelectedFile();
                dir = file.getAbsolutePath();
            }
            else
                return;
        }
        Calibration cal = imp.getCalibration();
        ArrayList < ArrayList < MyCustomFitter > > fitters = new ArrayList < ArrayList < MyCustomFitter > >(); 
        File folder = new File(dir);
        File[] listOfFiles = folder.listFiles();
        int maxIndexSpine = -1;
        for(File f : listOfFiles){
            String filename = f.getName();
            if(f.isFile() && filename.endsWith(".txt")){
                String tmp = filename.substring(filename.indexOf("spine_") + 6);
                tmp = tmp.substring(0, tmp.indexOf("-neckline"));
                int index = new Integer(tmp);
                if(index > maxIndexSpine) maxIndexSpine = index;
            }
        }
        fitters.ensureCapacity(maxIndexSpine);
        while(fitters.size() < maxIndexSpine) fitters.add(new ArrayList < MyCustomFitter >());
        for(File f : listOfFiles){
            String filename = f.getName();
            if(f.isFile() && filename.endsWith(".txt") && filename.contains("spine") && filename.contains("neckline")){
                System.out.println(f.getAbsoluteFile());
                String tmp = filename.substring(filename.indexOf("spine_") + 6);
                tmp = tmp.substring(0, tmp.indexOf("-neckline"));
                System.out.println(tmp);
                int index = new Integer(tmp);
                ArrayList <Double> xs = new ArrayList <Double>(), ys = new ArrayList <Double>();
                
                try{
                    InputStream ips=new FileInputStream( f.getAbsoluteFile() );
                    InputStreamReader ipsr=new InputStreamReader(ips);
                    BufferedReader br=new BufferedReader(ipsr);
                    String line;
                    line = br.readLine();
                    while(line != null){
                        String [] words = line.split( " " );
                        xs.add(new Double(words[0]));
                        ys.add(new Double(words[1]));
                        line = br.readLine();
                    }
                    MyCustomFitter fit = new MyCustomFitter(xs, ys);
                    fit.doFit(cal);
                    fitters.get(index - 1).add(fit);
                }
                catch (Exception e){
                    System.out.println(e.toString());
                }
            }
        }
        
        DecimalFormat df = new DecimalFormat( "#.###" );
        TextWindow textw = null;
        String headings = "# Spine (" + cal.getXUnit() + ")\tLength neck (" + cal.getXUnit() + ")\tLength spine (" + cal.getXUnit() + ")\tRatio (%)\tMinor axis (" + cal.getXUnit() + ")\tMajor axis (" + cal.getXUnit() + ")\tAspect Ratio\tPerimeter Head (" + cal.getXUnit() + ")\tArea Head (" + cal.getXUnit() + "²)\tSmallest neckwidth (" + cal.getXUnit() + ")\tMedian neckwidth (" + cal.getXUnit() + ")\tAverage neckwidth (" + cal.getXUnit() + ")\t# Neckline\tFWHM (" + cal.getXUnit() + ")\tFit goodness";
        String title = this.getImage().getTitle();
        int index = title.lastIndexOf( "." );
        title = title.substring( 0, index );
        title += ".xls";
        textw = new TextWindow(title, headings, "", 1200, 800);
        textw.setVisible( true );
        for( int n = 0; n < fitters.size(); n++ ){
            ArrayList < MyCustomFitter > fitter = fitters.get(n);
            
            if(fitter.isEmpty()) continue;
            
            double minD = 0., avgD = 0.;

            double[] allDs = new double[fitter.size()];
            minD = Double.MAX_VALUE;
            avgD = 0.;
            for( int i = 0; i < fitter.size(); i++ ){
                double d = fitter.get( i ).getD();
                allDs[i] = d;
                avgD += d;
                if( d < minD )
                    minD = d;
            }
            avgD /= ( double )fitter.size();
            Arrays.sort(allDs);
            double median;
            if (allDs.length % 2 == 0)
                median = ((double)allDs[allDs.length/2] + (double)allDs[allDs.length/2 - 1])/2;
            else
                median = (double) allDs[allDs.length/2];
            String s = "" + ( n + 1 ) + "\t\t\t\t\t\t\t\t\t" + df.format( minD ) + "\t" + df.format( median ) + "\t" + df.format( avgD ) + "\t";
            if( fitter != null && !fitter.isEmpty() ){
                for( int i = 0; i < fitter.size(); i++ ){
                    s += ( i + 1 ) + "\t" + df.format( fitter.get( i ).getD() ) + "\t" + df.format( fitter.get( i ).getGoodnessFit() ) + "\n";
                    //textw.append( s );
                    s += "\t\t\t\t\t\t\t\t\t\t\t\t";
                }
            }
            else{
                s += "0\t0\t0";
                //textw.append( s );
            }
            textw.append( s );
        }
        textw.getTextPanel().saveAs( dir + "conversion.xls" );
    }
    
    public void exportAllProfilesAllSpines(){
        if( m_spines.isEmpty() ) return;
        String dir = this.getDirectoryOriginalImage();
        if( dir == null ){
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
            int returnVal = chooser.showOpenDialog( this );
            if( returnVal == JFileChooser.APPROVE_OPTION ){
                File file = chooser.getSelectedFile();
                dir = file.getName();
            }
            else
                return;
        }
        for( int i = 0; i < m_spines.size(); i++ ){
            ArrayList < NeckShape > lines = m_spines.get( i ).getNeckAnalysis();
            for( int j = 0; j < lines.size(); j++ ){
                NeckShape ns = lines.get( j );
                double [] intensityProfile = ns.getIntensityProfile();          
                double [] xs = new double[intensityProfile.length];
                for( int n = 0; n < intensityProfile.length; n++ )
                    xs[n] = n * imp.getCalibration().pixelWidth;
                String name = this.getImage().getTitle();
                name = name.substring( 0, name.lastIndexOf( "." ) );
                name = name + "-spine_" + ( i + 1 ) + "-neckline_" + ( j + 1 ) + ".txt";

                boolean overwrite = false;
                if( new File( dir + name ).exists() ){
                    YesNoCancelDialog dialog = new YesNoCancelDialog( null, "Overwrite result file ?", "Do you want to overwrite " + name );
                    if( dialog.yesPressed() )
                        overwrite = true;
                    else if( dialog.cancelPressed() )
                        overwrite = false;
                }
                else
                    overwrite = true;                
                if( overwrite ){
                    Writer writer = null;
                        try {
                            String separator = System.getProperty("line.separator");
                            writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( dir + name ), "utf-8" ) );
                            for( int n = 0; n < intensityProfile.length; n++ )
                                writer.write( "" + xs[n] + " " + intensityProfile[n] + separator );
                        } catch ( IOException ex ){
                        // report
                        } finally {
                            try { writer.close(); } catch (Exception ex) {}
                        }
                }
            }
        }
    }
    
    public void saveSpinesAsSVG(Roi _roi){
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
                 
            //Graph
            ArrayList < EdgeWeightedGraph > graphs = m_dss.getGraphDetector().getGraphs();
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
                for (SeedGraph sg :graph.getSeeds()){
                    writer.write("<circle cx =\"" + sg.getX() + "\" cy=\"" + sg.getY() + "\" r=\"2\" stroke=\"#ffff00\" stroke-width=\"1\"/>\n");
                }
            }
            
            for(NeuronObject dendrite : m_dendrites){
                Line2D.Double[] shape = dendrite.getShape();
                for( int j = 0; j < shape.length; j++ ){
                    Line2D.Double l = shape[j];
                    writer.write( "<line x1 =\"" + l.x1 + "\" y1=\"" + l.y1 + "\" x2=\"" + l.x2 + "\" y2=\"" + l.y2 + "\" stroke=\"#0000ff\" stroke-width=\"1\"/>\n");
                }
            }
            
            for(NeuronObject spine : m_spines){
                DoublePolygon dp = spine.getOutline();
                 writer.write( "<polygon points=\"" );
                 for(int n = 0; n < dp.npoints; n++)
                     writer.write("" + dp.xpoints[n] + "," + dp.ypoints[n] + " ");
                 writer.write("\" stroke=\"#00C800\" stroke-width=\"1\"/>\n");
                 
                 if(spine.getNeckAnalysis() != null){
                    for(NeckShape ns : spine.getNeckAnalysis()){
                        PolygonRoi proi = ns.getNeckShapeUsedForFit();//m_displayNecklineUsedForFit ? ns.getNeckShapeUsedForFit() : ns.getNeckShapeFromFit();
                        FloatPolygon fp = proi.getFloatPolygon();
                        writer.write( "<polygon points=\"" );
                         for(int n = 0; n < fp.npoints; n++)
                             writer.write("" + fp.xpoints[n] + "," + fp.ypoints[n] + " ");
                         writer.write("\" stroke=\"#ff0000\" stroke-width=\"1\"/>\n");
                         Line2D.Double lineFit = ns.getLineFromFit();
                         writer.write( "<line x1 =\"" + lineFit.x1 + "\" y1=\"" + lineFit.y1 + "\" x2=\"" + lineFit.x2 + "\" y2=\"" + lineFit.y2 + "\" stroke=\"#000000\" stroke-width=\"1\"/>\n");
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
    
    public void setNecklineUsedForFit(boolean _val){
        m_displayNecklineUsedForFit = _val;
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        if( m_saDialog != null ){
            m_saDialog.dispose();
            m_saDialog = null;
        }
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }
}