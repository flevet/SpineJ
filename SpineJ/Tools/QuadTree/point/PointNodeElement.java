package SpineJ.Tools.QuadTree.point;

import SpineJ.Tools.QuadTree.AbstractNodeElement;
import java.awt.geom.Point2D;


@SuppressWarnings("serial")
public class PointNodeElement<T> extends AbstractNodeElement<T> {

	public PointNodeElement(Point2D.Double coordinates, T element) {
		super(coordinates, element);
	}
        

}
