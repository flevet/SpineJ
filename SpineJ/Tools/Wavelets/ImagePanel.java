/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Tools.Wavelets;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author Florian Levet
 */
public class ImagePanel extends JPanel{
    private BufferedImage imageSrc = null, image = null;
    private ImageInfos ii = null;

    public ImagePanel(){
        super();
        this.addComponentListener( new ComponentListener() {
            public void componentResized( ComponentEvent e ){
                generateImage();
            }
            public void componentMoved( ComponentEvent e ) {}
            public void componentShown( ComponentEvent e ) {}
            public void componentHidden( ComponentEvent e ) {}
        });

        this.addMouseListener( new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                if( e.getButton() == MouseEvent.BUTTON1 ){
                    ImagePlus imp = new ImagePlus( "Debug", ii.getProcessor().duplicate() );
                    imp.show();
                }
            }

            public void mousePressed(MouseEvent e) {}

            public void mouseReleased(MouseEvent e) {}

            public void mouseEntered(MouseEvent e) {}

            public void mouseExited(MouseEvent e) {}
        });
    }

    public void setImage( ImageInfos _image ){
        ii = _image;
        imageSrc = ii.getImage();
        generateImage();      
    }

    public void setImage( ImageProcessor _image ){
        imageSrc = _image.getBufferedImage();
        generateImage();
    }

    public void generateImage(){
        if( imageSrc == null ) return;
        int destW, destH;
        if( this.getWidth() == 0 )
            destW = 10;
        else
            destW = this.getWidth();
        if( this.getHeight() == 0 )
            destH = 10;
        else
            destH = this.getHeight();
        image = new BufferedImage( destW, destH, BufferedImage.TYPE_INT_RGB );
        Graphics2D g = image.createGraphics();
        AffineTransform at = AffineTransform.getScaleInstance( ( double )destW / imageSrc.getWidth(), ( double )destH / imageSrc.getHeight() );
        g.drawRenderedImage( imageSrc,at );
    }

    @Override
    public void setSize( int _w, int _h ){
        super.setSize( _w, _h );
        generateImage();
    }

    @Override
    public void paint( Graphics g )
    {
            super.paintComponent(g);
            if(image != null) // Si l'image existe, ...
                g.drawImage( image, 0, 0, this ); // ... on la dessine
    }
}
