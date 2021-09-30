/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package SpineJ.Tools.Events;

import java.util.EventListener;

/**
 *
 * @author Florian Levet
 */
public interface RoiChangedListener extends EventListener{
    public void roiChangedOccurred( RoiChangedEvent evt);
}
