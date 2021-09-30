/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Tools.Graph;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 *
 * @author Florian Levet
 */
public interface EdgeGraph {
    public SeedGraph getV1();
    public SeedGraph getV2();
    public int getId();
    public void setId( int _val );
    public boolean isDendrite();
    public boolean marked();
    public void setMarked( boolean _val );
    public void setDendrite( boolean _val );
    public ArrayList < Point2D.Double > getSkel();
    public ArrayList < Point > getSkel2();
    public int getLength();
    public boolean isEdge( SeedGraph v1, SeedGraph v2 );
}
