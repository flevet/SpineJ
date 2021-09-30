package SpineJ.Tools.QuadTree;

import java.awt.geom.Point2D;

/**
 * Container class that holds the object within the quadtree
 * 
 * @author jotschi
 * 
 */
@SuppressWarnings("serial")
public abstract class AbstractNodeElement<T> extends Point2D.Double {

	private T element;

	/**
	 * Create a new NodeElement that holds the element at the given coordinates.
	 * 
	 * @param x
	 * @param y
	 * @param element
	 */
	public AbstractNodeElement(Point2D.Double coordinates, T element) {
		super(coordinates.x, coordinates.y);
		this.element = element;
	}

	public AbstractNodeElement(T element) {
		this.element = element;
	}

	/**
	 * Returns the element that is contained within this NodeElement
	 * 
	 * @return
	 */
	public T getElement() {
		return element;
	}

        public void move( double _x, double _y ){
            super.x = _x;
            super.y = _y;
        }

}
