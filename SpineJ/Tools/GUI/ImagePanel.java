/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SpineJ.Tools.GUI;

/**
 *
 * @author Florian Levet
 */
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class ImagePanel extends JPanel{

    private BufferedImage m_image = null;
    
    public ImagePanel(){
        super();
    }

    public ImagePanel( BufferedImage _img ) {
       super();
       m_image = _img;
    }

    @Override
    public void paintComponent( Graphics g ) {
        super.paintComponent( g );
        g.drawImage( m_image, 0, 0, this.getWidth(), this.getHeight(), null ); // see javadoc for more info on the parameters            
    }
    
    public void changeImage( BufferedImage _img ){
        m_image = _img;
    }
    
    public BufferedImage getImage(){
        return m_image;
    }

}