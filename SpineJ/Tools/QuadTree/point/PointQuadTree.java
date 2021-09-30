package SpineJ.Tools.QuadTree.point;

import SpineJ.Tools.QuadTree.AbstractQuadTree;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * Creates a new QuadTree that can hold the given type of elements
 * 
 * @author jotschi
 * 
 * @param <T>
 */
public class PointQuadTree<T> extends AbstractQuadTree<T> {

    protected PointNode<T> rootNode;

    /**
     * Create a new QuadTree with the give start coordinates and size
     *
     * @param startCorrdinates
     * @param size
     */
    public PointQuadTree(Point2D.Double startCoordinates, Point2D.Double size) {
        super(startCoordinates, size);
        this.rootNode = new PointNode<T>(startCoordinates, size, 0);
    }

    public PointQuadTree(Point2D.Double startCoordinates, Point2D.Double size, int maxDepth, int maxChildren) {
        super(startCoordinates, size);
        this.rootNode = new PointNode<T>(startCoordinates, size, 0, maxDepth, maxChildren);
    }

    /**
     * Add a new element to the QuadTree
     *
     * @param x
     * @param y
     * @param element
     */
    public void insert(double x, double y, T element) {
        insert(new Point2D.Double(x, y), element);
    }

    /**
     * Add a new element to the QuadTree that has a specific dimension/size
     *
     * @param point
     * @param size
     * @param element
     */
    public void insert(Point2D.Double point, Point2D.Double size, T element) {

        // Check if the element coordinates are within bounds of the quadtree
        if (point.x > startCoordinates.x + size.x || point.x < startCoordinates.x) {
            throw new IndexOutOfBoundsException(
                    "The x coordinate must be within bounds of ["
                    + startCoordinates.x + "] to [" + size.x + "]");
        }
        if (point.y > startCoordinates.y + size.y || point.y < startCoordinates.y) {
            throw new IndexOutOfBoundsException(
                    "The y coordinate must be within bounds of ["
                    + startCoordinates.y + "] to [" + size.y + "]");
        }

        // Check if the right bottom corner is within bounds
        if (point.x + size.x > startCoordinates.x + size.x || point.x < startCoordinates.x) {
            throw new IndexOutOfBoundsException(
                    "The x coordinate must be within bounds of ["
                    + startCoordinates.x + "] to [" + size.x + "]");
        }
        if (point.y + size.y > startCoordinates.y + size.y || point.y < startCoordinates.y) {
            throw new IndexOutOfBoundsException(
                    "The y coordinate must be within bounds of ["
                    + startCoordinates.y + "] to [" + size.y + "]");
        }

        this.rootNode.insert(new PointNodeElement<T>(point, element));

    }

    /**
     * Add a new element to the QuadTree
     *
     * @param point
     * @param element
     */
    public void insert(Point2D.Double point, T element) {

        // Check if the element coordinates are within bounds of the quadtree
        if (point.x > startCoordinates.x + size.x
                || point.x < startCoordinates.x) {
            throw new IndexOutOfBoundsException(
                    "The x coordinate must be within bounds of ["
                    + startCoordinates.x + "] to [" + size.x + "]");
        }
        if (point.y > startCoordinates.y + size.y
                || point.y < startCoordinates.y) {
            throw new IndexOutOfBoundsException(
                    "The y coordinate must be within bounds of ["
                    + startCoordinates.y + "] to [" + size.y + "]");
        }

        this.rootNode.insert(new PointNodeElement<T>(point, element));
    }

    public PointNodeElement<T> insertOrGet(double x, double y, T element, double _minSqrLength) {
        PointNode.m_minSqrLength = _minSqrLength;
        return insertOrGet(new Point2D.Double(x, y), element);
    }
    public PointNodeElement<T> insertOrGet(Point2D.Double point, T element) {

        // Check if the element coordinates are within bounds of the quadtree
        if (point.x > startCoordinates.x + size.x
                || point.x < startCoordinates.x) {
            throw new IndexOutOfBoundsException(
                    "The x coordinate must be within bounds of ["
                    + startCoordinates.x + "] to [" + size.x + "]");
        }
        if (point.y > startCoordinates.y + size.y
                || point.y < startCoordinates.y) {
            throw new IndexOutOfBoundsException(
                    "The y coordinate must be within bounds of ["
                    + startCoordinates.y + "] to [" + size.y + "]");
        }

        return this.rootNode.insertOrGet(new PointNodeElement<T>(point, element));
    }

    public PointNodeElement<T> get( double x, double y, double _minSqrLength ) {
        return get( new Point2D.Double( x, y ), _minSqrLength );
    }
    public PointNodeElement<T> get( Point2D.Double point, double _minSqrLength ) {
        PointNode.m_minSqrLength = _minSqrLength;
        // Check if the element coordinates are within bounds of the quadtree
        if (point.x > startCoordinates.x + size.x
                || point.x < startCoordinates.x) {
            throw new IndexOutOfBoundsException(
                    "The x coordinate must be within bounds of ["
                    + startCoordinates.x + "] to [" + size.x + "]");
        }
        if (point.y > startCoordinates.y + size.y
                || point.y < startCoordinates.y) {
            throw new IndexOutOfBoundsException(
                    "The y coordinate must be within bounds of ["
                    + startCoordinates.y + "] to [" + size.y + "]");
        }

        return this.rootNode.get( point );
    }

    /**
     * Returns the rootNode of this tree
     *
     * @return
     */
    public PointNode<T> getRootNode() {
        return this.rootNode;
    }

    /**
     * Returns all elements wihtin the cell that matches the given coordinates
     *
     * @param coordinates
     * @return
     */
    public ArrayList<PointNodeElement<T>> getElements( Point2D.Double coordinates ) {
        return this.rootNode.getElements(coordinates);
    }

    @Override
    public void clear() {
        this.rootNode.clear();
    }

    public ArrayList<PointNodeElement<T>> getNeighboringCells(double _cx, double _cy, double _cr) {
        return getNeighboringCells(new Point2D.Double(_cx, _cy), _cr);
    }

    public ArrayList<PointNodeElement<T>> getNeighboringCells(Point2D.Double _center, double _cr) {
        ArrayList<PointNodeElement<T>> cells = new ArrayList<PointNodeElement<T>>();
        this.rootNode.getNeighboringCells(_center, _cr, cells);
        return cells;
    }
}
