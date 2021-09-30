/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Tools.Events;

import ij.gui.Roi;
import java.util.EventObject;

/**
 *
 * @author Florian Levet
 */
public class RoiChangedEvent extends EventObject {
    public Roi m_roi;
    public int m_index;

    public RoiChangedEvent( Object source, int _index, Roi _roi ) {
        super(source);
        m_roi = _roi;
        m_index = _index;
    }
}
