/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Tools.Graph;

import java.awt.Point;


/**
 *
 * @author Florian Levet
 */
public interface SeedGraph {
    public double getX();
    public double getY();
    public Point getPosition();
    public int getId();
    public void setId( int _val );
    public void resetEdges();
    public int degree();
    public EdgeGraph getEdgeWith( SeedGraph v );
    public void setMarked( boolean _val );
    public boolean isMarked();
}
