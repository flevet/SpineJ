package SpineJ.GradientVector;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.SaveDialog;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

final public class VectorField1 extends Roi
    implements KeyListener
{

    public VectorField1(ImagePlus imageplus, float af[][][])
    {
        super(0, 0, imageplus.getWidth(), imageplus.getHeight());
        setImage(imageplus);
        vf = af;
        imageplus.setRoi(this);
        ic = imageplus.getCanvas();
        ic.addKeyListener(this);
    }

    @Override
    public void draw(Graphics g)
    {
        if(g instanceof Graphics2D)
                ((Graphics2D)g).setStroke(new BasicStroke(3.f));
        float f = (float)ic.getMagnification();
        if(f > 4F)
        {
            int i = (int)((double)f / 2D);
            int j = (int)((double)f / 2D);
            g.setColor(Color.red);
            Rectangle rectangle = ic.getSrcRect();
            int k = rectangle.x + rectangle.width;
            int l = rectangle.y + rectangle.height;
            for(int i1 = rectangle.y; i1 < l; i1++)
            {
                for(int j1 = rectangle.x; j1 < k; j1++)
                {
                    float f1 = ((255F - vf[0][i1][j1]) * maxveclen * f) / 255F;
                    int k1 = (int)(vf[1][i1][j1] * f1) / 2;
                    int l1 = (int)(vf[2][i1][j1] * f1) / 2;
                    g.drawLine((ic.screenX(j1) - k1) + i, (ic.screenY(i1) - l1) + j, ic.screenX(j1) + k1 + i, ic.screenY(i1) + l1 + j);
                    int k2 = (int)(-vf[2][i1][j1] * f1);
                    int l2 = (int)(vf[1][i1][j1] * f1);
               }

            }

        }
    }
    
    public void saveVectorFieldAsSVG(){
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
            
            Rectangle rectangle = ic.getSrcRect();
            int k = rectangle.x + rectangle.width, i = 0, j = 0;
            int l = rectangle.y + rectangle.height;
            for(int i1 = rectangle.y; i1 < l; i1++)
            {
                for(int j1 = rectangle.x; j1 < k; j1++)
                {
                    float f1 = ((255F - vf[0][i1][j1]) * maxveclen * 4.f) / 255F;
                    int k1 = (int)(vf[1][i1][j1] * f1) / 2;
                    int l1 = (int)(vf[2][i1][j1] * f1) / 2;
                    double x1 = (j1 - k1) + i, y1 = (i1 - l1) + j, x2 = j1 + k1 + i, y2 = i1 + l1 + j;
                    writer.write( "<line x1 =\"" + x1 + "\" y1=\"" + y1 + "\" x2=\"" + x2 + "\" y2=\"" + y2 + "\" stroke=\"#ff0000\" stroke-width=\"1\"/>\n");
                }

            }            
            writer.write( "</svg>" );
        }catch ( IOException ex ){
        // report
        } finally {
            try { writer.close(); } catch (Exception ex) {}
        }
    }

    public void keyPressed(KeyEvent keyevent)
    {
        if(keyevent.getKeyCode() == 38)
            maxveclen += 0.05F;
        else
        if(keyevent.getKeyCode() == 40)
            maxveclen -= 0.05F;
        ic.repaint();
    }

    public void keyReleased(KeyEvent keyevent)
    {
    }

    public void keyTyped(KeyEvent keyevent)
    {
    }

    private float vf[][][];
    private static float maxveclen = 1.0F;

}