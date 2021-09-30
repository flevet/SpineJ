package SpineJ.Tools.QuadTree;

import java.awt.geom.Point2D;

public abstract class AbstractQuadTree<T> {

	protected Point2D.Double size;
	protected Point2D.Double startCoordinates;

	public AbstractQuadTree(Point2D.Double startCoordinates, Point2D.Double size) {
		this.size = size;
		this.startCoordinates = startCoordinates;
	}

	/**
	 * Returns the size
	 * 
	 * @return
	 */
	public Point2D.Double getSize() {
		return this.size;
	}

	/**
	 * Returns the startCoordinates
	 * 
	 * @return
	 */
	public Point2D.Double getStartCoordinates() {
		return this.startCoordinates;
	}

	/**
	 * Clear the QuadTree
	 */
	public abstract void clear();

	/**
	 * Return the root node of this quad tree
	 * 
	 * @return
	 */
	public abstract AbstractNode<T> getRootNode();

}
