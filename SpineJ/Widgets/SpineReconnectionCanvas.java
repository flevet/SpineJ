/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SpineJ.Widgets;

import SpineJ.Reconnection.SpineReconnection;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.process.ImageProcessor;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 *
 * @author Florian Levet
 */
public class SpineReconnectionCanvas extends ImageCanvas {
    protected BufferedImage offScreenImage = null;
    protected int offScreenImage_width,  offScreenImage_height;
    protected static Font smallFont,  largeFont;
    protected static final int LIST_OFFSET = 100000;
    
    protected ImageProcessor m_original = null, m_costs = null;
    protected Point m_p1 = null, m_p2 = null;
    protected ArrayList < Point > m_path = null;
    protected boolean m_displayOriginal = true;
    protected SpineReconnection m_sr = null;
    protected boolean m_forceActivated = false;
    
    public SpineReconnectionCanvas( ImagePlus _imp, ImageProcessor _costs, SpineReconnection _sr ){
        super( _imp );
        m_original = _imp.getProcessor().duplicate();
        m_costs = _costs;
        m_sr = _sr;
        setBinarizedImage();
    }
    
    @Override
    public void paint(Graphics g) {
        final int srcRectWidthMag = (int) ( srcRect.width * magnification );
        final int srcRectHeightMag = (int) ( srcRect.height * magnification );

        if ( offScreenImage == null || offScreenImage_width != srcRectWidthMag || offScreenImage_height != srcRectHeightMag ) {
            offScreenImage = new BufferedImage(srcRectWidthMag, srcRectHeightMag, BufferedImage.TYPE_INT_ARGB);//createImage(srcRectWidthMag, srcRectHeightMag);
            offScreenImage_width = srcRectWidthMag;
            offScreenImage_height = srcRectHeightMag;
        }

        Graphics2D offScreenGraphics = offScreenImage.createGraphics();//.getGraphics();
        offScreenGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        offScreenGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        super.paint(offScreenGraphics);
        offScreenGraphics.setStroke(new BasicStroke(1.f));
        offScreenGraphics.setColor( Color.red );
        if( m_p1 != null )
            fill( offScreenGraphics, m_p1.x, m_p1.y, 2. );
        if( m_p2 != null )
            fill( offScreenGraphics, m_p2.x, m_p2.y, 2. );
        
        g.drawImage(offScreenImage, 0, 0, this);      
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        String modifiers = MouseEvent.getMouseModifiersText(e.getModifiers());
        if ((modifiers.indexOf("Maj") != -1) && (e.getButton() == MouseEvent.BUTTON1)){
            m_displayOriginal = !m_displayOriginal;
            if( m_displayOriginal )
                imp.setProcessor( m_original );
            else
                imp.setProcessor( m_costs );
            repaint();
        }
        else if ( m_forceActivated || ( ( modifiers.indexOf("Ctrl") != -1) && (e.getButton() == MouseEvent.BUTTON1 ) ) ){
            Point point = getCursorLoc();
            int x = point.x, y = point.y;
            if( m_p1 == null )
                m_p1 = new Point( x, y );
            else if( m_p2 == null )
                m_p2 = new Point( x, y );
            if( m_p1 != null && m_p2 != null ){
                m_path = m_sr.determineDirectionImage( m_p1.x, m_p1.y, m_p2.x, m_p2.y );
                m_p1 = m_p2 = null;
            }
            repaint();
        }
        else
            super.mousePressed(e);
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
    
    void draw( Graphics2D g, double ax, double ay, double bx, double by ) {
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
    
    protected void drawPath( Graphics2D _g ) {
        for( int n = 1; n < m_path.size(); n++ )
            draw( _g, m_path.get( n - 1 ).x, m_path.get( n - 1 ).y, m_path.get( n ).x, m_path.get( n ).y );
    }

    void setOriginalImage() {
        m_displayOriginal = true;
        imp.setProcessor( m_original );
    }

    void setBinarizedImage() {
        m_displayOriginal = false;
        imp.setProcessor( m_costs );
    }

    void undoReconnection() {
        m_sr.undo();
        m_costs = m_sr.getBinaryDisplayImage();
        if( !m_displayOriginal )
            setBinarizedImage();
        repaint();
    }
    
    void setForceIsActivated( boolean _val ){
        m_forceActivated = _val;
    }
    
    public ImageProcessor getBinaryImage(){
        m_costs.resetRoi();
        return m_costs;
    }
    
}
