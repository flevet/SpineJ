/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Tools.Wavelets;

/**
 *
 * @author Florian Levet
 */
import SpineJ.Tools.Events.RoiChangedEvent;
import SpineJ.Tools.Events.RoiChangedListener;
import ij.ImagePlus;
import ij.gui.EllipseRoi;
import ij.gui.ImageCanvas;
import ij.gui.Roi;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class ImageCanvasRoi extends ImageCanvas {
    protected BufferedImage offScreenImage = null;
    protected int offScreenImage_width,  offScreenImage_height;
    protected static Font smallFont,  largeFont;
    protected static final int LIST_OFFSET = 100000;
    protected  Roi[] rois = null;
    protected int m_roiSelected = -1;
    protected boolean showRois = true, m_displayOriginalProcessor = true;
    protected ImageProcessor m_originalProcessor = null, m_binaryProcessor = null;
    // Create the listener list
    protected javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

    public ImageCanvasRoi( ImagePlus _imp ) {
        super(_imp);
        m_originalProcessor = _imp.getProcessor();
    }

    public void setRois( Roi[] _rois ){
        rois = _rois;
        m_roiSelected = 0;
    }
    
    public void setBinaryProcessor( ImageProcessor _binary ){
        m_binaryProcessor = _binary;
    }
    public ImageProcessor getBinaryProcessor(){
        return m_binaryProcessor;
    }
    public ImageProcessor getOriginalProcessor(){
        return m_originalProcessor;
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
        offScreenGraphics.setColor(Color.red);
        if( showRois ){
            if( rois != null )
            {
                offScreenGraphics.setStroke(new BasicStroke(2.f));
                for(int i = 0; i < rois.length; i++){
                    offScreenGraphics.setColor( Color.RED );
                    Roi roi = rois[i];
                    drawRoi( roi, offScreenGraphics );
                }
            }
        }
        g.drawImage(offScreenImage, 0, 0, this);
    }

    @Override
    public void repaint() {
        super.repaint();
        this.paint(this.getGraphics());
    }

    protected void drawRoi( Roi roi, Graphics2D g ) {
        if (roi == null) {
            return;
        }
        drawFloatPolygon( roi.getFloatPolygon(), g );

    }
    protected void drawEllipseRoi( EllipseRoi roi, Graphics2D g ) {
        Rectangle r = roi.getBounds();
        int[] xs = roi.getXCoordinates();
        int[] ys = roi.getYCoordinates();
        for (int j = 0; j < xs.length; j++) {
            xs[j] = screenX(r.x + xs[j]);
            ys[j] = screenY(r.y + ys[j]);
        }
        g.draw(new Polygon(xs, ys, xs.length));
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

    protected void drawFloatPolygon( FloatPolygon p, Graphics2D g ) {
        int[] xs = new int[p.npoints];
        int[] ys = new int[p.npoints];
        for (int j = 0; j < p.npoints; j++) {
            xs[j] = screenX( ( int )p.xpoints[j] );
            ys[j] = screenY( ( int )p.ypoints[j] );
        }
        g.draw(new Polygon(xs, ys, xs.length));
    }

    protected void drawRoiLabel(Graphics2D g, int index, Rectangle r, Color color) {
        int x = screenX(r.x);
        int y = screenY(r.y);
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
        g.setColor(color);
    }

    void draw(Graphics g, double ax, double ay, double bx, double by) {
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
        g.drawLine((int) x0, (int) y0, (int) x1, (int) y1);
    }

    void draw(Graphics g, double ax, double ay, int pointRadius) {
        double m = magnification;
        int x = (int) ((ax - srcRect.x) * m);
        int y = (int) ((ay - srcRect.y) * m);
        //g.setColor(imp.getRoi().getColor());
        g.fillOval(x - pointRadius, y - pointRadius, pointRadius + pointRadius, pointRadius + pointRadius);
    }

    protected void drawContour(Graphics g, Point.Double[] points) {
        for (int i = 1; i < points.length; i++) {
            Point.Double a = points[i - 1];
            Point.Double b = points[i];
            draw(g, a.x, a.y, b.x, b.y);
            draw(g, b.x, b.y, 3);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        String modifiers = MouseEvent.getMouseModifiersText(e.getModifiers());
        if ( ( modifiers.indexOf( "Ctrl" ) != -1 ) && ( e.getButton() == MouseEvent.BUTTON1 ) ){
            showRois = !showRois;
            repaint();
        }
        else if ( ( modifiers.indexOf( "Ctrl" ) != -1 ) && ( e.getButton() == MouseEvent.BUTTON3 ) ){
            m_displayOriginalProcessor = !m_displayOriginalProcessor;
            if( m_displayOriginalProcessor )
                imp.setProcessor( m_originalProcessor );
            else
                imp.setProcessor( m_binaryProcessor );
            repaint();
        }
        else if ( ( modifiers.indexOf( "Maj" ) != -1 ) && ( e.getButton() == MouseEvent.BUTTON1 ) ){
            determineRoiSelected( offScreenX( e.getX() ), offScreenY( e.getY() ) );
            repaint();
        }
        else
            super.mousePressed( e );
    }

     // This methods allows classes to register for MyEvents
    public void addRoiChangedListener( RoiChangedListener listener) {
        listenerList.add( RoiChangedListener.class, listener );
    }

    // This methods allows classes to unregister for MyEvents
    public void removeRoiChangedListener( RoiChangedListener listener ) {
        listenerList.remove( RoiChangedListener.class, listener );
    }

    // This private class is used to fire MyEvents
    void fireRoiChangedEvent( RoiChangedEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for ( int i=0; i<listeners.length; i+=2 ) {
            if ( listeners[i]== RoiChangedListener.class ) {
                ( ( RoiChangedListener )listeners[i+1] ).roiChangedOccurred(evt);
            }
        }
    }

    protected void determineRoiSelected( int _x, int _y ){//m_roiSelected
        boolean found = false;
        int roi = -1;
        for( int i = 0; i < rois.length && !found; i++ ){
            Polygon p = rois[i].getPolygon();
            if( p.contains( _x, _y ) ){
                roi = i;
                found = true;
            }
        }
        if( m_roiSelected != roi ){
            m_roiSelected = roi;
            fireRoiChangedEvent( new RoiChangedEvent( this, m_roiSelected, rois[m_roiSelected] ) );
        }
    }
}