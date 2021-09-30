package SpineJ.Tools.QuadTree.point;

import SpineJ.Tools.QuadTree.AbstractNode;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

/**
 * Node that represents each 'cell' within the quadtree. The Node will contains
 * elements {@link AbstractNodeElement} that itself will contain the final data
 * within the tree.
 * 
 * @author jotschi
 * 
 * @param <T>
 */
public class PointNode<T> extends AbstractNode {
    public static double m_minSqrLength = 1.;

    protected Map<Cell, PointNode<T>> nodes = new HashMap<Cell, PointNode<T>>();
    /**
     * Holds all elements for this node
     */
    protected ArrayList<PointNodeElement<T>> elements = new ArrayList<PointNodeElement<T>>();

    public PointNode(Point2D.Double startCoordinates, Point2D.Double bounds, int depth) {
        super(startCoordinates, bounds, depth);
    }

    /**
     *
     * @param startCoordinates
     * @param bounds
     * @param depth
     * @param maxDepth
     * @param maxChildren
     */
    public PointNode(Point2D.Double startCoordinates, Point2D.Double bounds, int depth,
            int maxDepth, int maxChildren) {
        super(startCoordinates, bounds, depth, maxDepth, maxChildren);
    }

    /**
     * Returns the subnodes of this node
     *
     * @return
     */
    public Map<Cell, PointNode<T>> getSubNodes() {
        return this.nodes;
    }

    /**
     * Returns the cell of this element
     *
     * @param element
     * @return
     */
    protected Cell findIndex(Point2D.Double coordinates) {
        // Compute the sector for the coordinates
        boolean left = (coordinates.x > (startCoordinates.x + bounds.x / 2.)) ? false
                : true;
        boolean top = (coordinates.y > (startCoordinates.y + bounds.y / 2.)) ? false
                : true;

        // top left
        Cell index = Cell.TOP_LEFT;
        if (left) {
            // left side
            if (!top) {
                // bottom left
                index = Cell.BOTTOM_LEFT;
            }
        } else {
            // right side
            if (top) {
                // top right
                index = Cell.TOP_RIGHT;
            } else {
                // bottom right
                index = Cell.BOTTOM_RIGHT;

            }
        }
        return index;
    }

    /**
     * Returns all elements for this node
     *
     * @return
     */
    public ArrayList<PointNodeElement<T>> getElements() {
        return this.elements;
    }

    /**
     * Returns all elements within the cell that matches the given coordinates
     *
     * @param coordinates
     * @return
     */
    public ArrayList<PointNodeElement<T>> getElements(Point2D.Double coordinates) {

        // Check if this node has already been subdivided. Therefor this node
        // should contain no elements
        if (nodes.size() > 0) {
            Cell index = findIndex(coordinates);
            PointNode<T> node = this.nodes.get(index);
            return node.getElements(coordinates);
        } else {
            return this.elements;
        }
    }

    /**
     * Insert the element into this node. If needed a subdivision will be
     * performed
     *
     * @param element
     */
    public void insert(PointNodeElement<T> element) {
        // If this Node has already been subdivided just add the elements to the
        // appropriate cell
        if (!this.nodes.isEmpty()) {
            Cell index = findIndex(element);
            this.nodes.get(index).insert(element);
            return;
        }

        // Add the element to this node
        this.elements.add(element);

        // Only subdivide the node if it contain more than MAX_CHILDREN and is
        // not the deepest node
        if (!(this.depth >= maxDepth) && this.elements.size() > maxElements) {
            this.subdivide();

            // Recall insert for each element. This will move all elements of
            // this node into the new nodes at the appropriate cell
            for (PointNodeElement<T> current : elements) {
                this.insert(current);
            }
            // Remove all elements from this node since they were moved into
            // subnodes
            this.elements.clear();

        }

    }

    public PointNodeElement<T> insertOrGet(PointNodeElement<T> element) {
        // If this Node has already been subdivided just add the elements to the
        // appropriate cell
        if (!this.nodes.isEmpty()) {
            Cell index = findIndex(element);
            return this.nodes.get(index).insertOrGet(element);
        }

        PointNodeElement<T> existing = null;
        for (int i = 0; i < this.elements.size() && existing == null; i++) {
            PointNodeElement<T> item = this.elements.get(i);
            double length = Point2D.Double.distanceSq( item.getX(), item.getY(), element.getX(), element.getY() );
            if ( length <= m_minSqrLength )
            //if( item.getX() == element.getX() && item.getY() == element.getY() )
                existing = item;
        }
        if (existing != null) {
            return existing;
        }

        // Add the element to this node
        this.elements.add(element);

        // Only subdivide the node if it contain more than MAX_CHILDREN and is
        // not the deepest node
        if (!(this.depth >= maxDepth) && this.elements.size() > maxElements) {
            this.subdivide();

            // Recall insert for each element. This will move all elements of
            // this node into the new nodes at the appropriate cell
            for (PointNodeElement<T> current : elements) {
                this.insert(current);
            }
            // Remove all elements from this node since they were moved into
            // subnodes
            this.elements.clear();

        }
        return element;
    }

    public PointNodeElement<T> get( Point2D.Double element ) {
        // If this Node has already been subdivided just add the elements to the
        // appropriate cell
        if (!this.nodes.isEmpty()) {
            Cell index = findIndex(element);
            return this.nodes.get(index).get(element);
        }

        PointNodeElement<T> existing = null;
        for (int i = 0; i < this.elements.size() && existing == null; i++) {
            PointNodeElement<T> item = this.elements.get(i);
            //double length = Point2D.Double.distanceSq( item.getX(), item.getY(), element.getX(), element.getY() );
            //if ( length <= m_minSqrLength )
            if( item.getX() == element.getX() && item.getY() == element.getY() )
                existing = item;
        }
        return existing;
    }

    /**
     * Subdivide the current node and add subnodes
     */
    public void subdivide() {
        int depth = this.depth + 1;

        double bx = this.startCoordinates.x;
        double by = this.startCoordinates.y;

        // Create the bounds for the new cell
        Point2D.Double newBounds = new Point2D.Double(this.bounds.x / 2., this.bounds.y / 2.);

        // Add new bounds to current start coordinates to calculate the new
        // start coordinates
        double newXStartCoordinate = bx + newBounds.x;
        double newYStartCoordinate = by + newBounds.y;

        PointNode<T> cellNode = null;

        // top left
        cellNode = new PointNode<T>(new Point2D.Double(bx, by), newBounds, depth,
                this.maxDepth, this.maxElements);
        this.nodes.put(Cell.TOP_LEFT, cellNode);

        // top right
        cellNode = new PointNode<T>(new Point2D.Double(newXStartCoordinate, by),
                newBounds, depth, this.maxDepth, this.maxElements);
        this.nodes.put(Cell.TOP_RIGHT, cellNode);

        // bottom left
        cellNode = new PointNode<T>(new Point2D.Double(bx, newYStartCoordinate),
                newBounds, depth, this.maxDepth, this.maxElements);
        this.nodes.put(Cell.BOTTOM_LEFT, cellNode);

        // bottom right
        cellNode = new PointNode<T>(new Point2D.Double(newXStartCoordinate,
                newYStartCoordinate), newBounds, depth, this.maxDepth,
                this.maxElements);
        this.nodes.put(Cell.BOTTOM_RIGHT, cellNode);
    }

    /**
     * Clears this node and all subnodes
     */
    public void clear() {
        for (PointNode<T> node : nodes.values()) {
            node.clear();
        }
        elements.clear();
        nodes.clear();
    }

    public void getNeighboringCells(Point2D.Double _center, double _cr, ArrayList<PointNodeElement<T>> _cells) {
        // Check if this node has already been subdivided. Therefor this node
        // should contain no elements
        /*if (nodes.size() > 0) {
        PointNode<T> node = this.nodes.get(Cell.TOP_LEFT);
        node.getNeighboringCells(_center, _dmin, _dmax, _cells);
        node = this.nodes.get(Cell.TOP_RIGHT);
        node.getNeighboringCells(_center, _dmin, _dmax, _cells);
        node = this.nodes.get(Cell.BOTTOM_LEFT);
        node.getNeighboringCells(_center, _dmin, _dmax, _cells);
        node = this.nodes.get(Cell.BOTTOM_RIGHT);
        node.getNeighboringCells(_center, _dmin, _dmax, _cells);
        } else {
        if (isInsideRec(this.startCoordinates, this.bounds, _center)) {
        _cells.addAll(this.elements);
        } else {
        double x0 = this.startCoordinates.x, y0 = this.startCoordinates.y, x1 = this.startCoordinates.x + this.bounds.x, y1 = this.startCoordinates.y + this.bounds.y;
        boolean b1 = circleLineIntersect(x0, y0, x1, y0, _center, _dmin, _dmax);
        boolean b2 = circleLineIntersect(x1, y0, x1, y1, _center, _dmin, _dmax);
        boolean b3 = circleLineIntersect(x1, y1, x0, y1, _center, _dmin, _dmax);
        boolean b4 = circleLineIntersect(x0, y1, x0, y0, _center, _dmin, _dmax);
        if (b1 || b2 || b3 || b4) {
        _cells.addAll(this.elements);
        }
        }
        }*/
        double x0 = this.startCoordinates.x, y0 = this.startCoordinates.y, x1 = this.startCoordinates.x + this.bounds.x, y1 = this.startCoordinates.y + this.bounds.y;
        boolean b1 = circleLineIntersect(x0, y0, x1, y0, _center, _cr);
        boolean b2 = circleLineIntersect(x1, y0, x1, y1, _center, _cr);
        boolean b3 = circleLineIntersect(x1, y1, x0, y1, _center, _cr);
        boolean b4 = circleLineIntersect(x0, y1, x0, y0, _center, _cr);
        boolean insideCenter = isInsideRec(this.startCoordinates, this.bounds, _center);
        boolean insideRec = isRecInsideCircle(x0, y0, x1, y1, _center, _cr);
        if (insideRec || insideCenter || b1 || b2 || b3 || b4) {
            if (nodes.size() > 0) {
                PointNode<T> node = this.nodes.get(Cell.TOP_LEFT);
                node.getNeighboringCells(_center, _cr, _cells);
                node = this.nodes.get(Cell.TOP_RIGHT);
                node.getNeighboringCells(_center, _cr, _cells);
                node = this.nodes.get(Cell.BOTTOM_LEFT);
                node.getNeighboringCells(_center, _cr, _cells);
                node = this.nodes.get(Cell.BOTTOM_RIGHT);
                node.getNeighboringCells(_center, _cr, _cells);
            } else {
                _cells.addAll(this.elements);
            }
        }
    }

    public static boolean isRecInsideCircle(double _x0, double _y0, double _x1, double _y1, Point2D.Double _center, double _cr) {
        double l1 = Point2D.Double.distance(_x0, _y0, _center.x, _center.y);
        double l2 = Point2D.Double.distance(_x1, _y0, _center.x, _center.y);
        double l3 = Point2D.Double.distance(_x1, _y1, _center.x, _center.y);
        double l4 = Point2D.Double.distance(_x0, _y1, _center.x, _center.y);
        return (l1 <= _cr && l2 <= _cr && l3 <= _cr && l4 <= _cr);
    }

    public static boolean isInsideRec(Point2D.Double _orig, Point2D.Double _bounds, Point2D.Double _center) {
        double x0 = _orig.x, y0 = _orig.y, x1 = _orig.x + _bounds.x, y1 = _orig.y + _bounds.y;
        return (x0 <= _center.x && _center.x <= x1 && y0 <= _center.y && _center.y <= y1);
    }

    public static boolean isLineIntersectCircle(double lx1, double ly1, double lx2, double ly2, Point2D.Double _center, double _cr) {
        Point2D.Double point = closestPointOnLine(lx1, ly1, lx2, ly2, _center);
        Point2D.Double vec = new Point2D.Double(point.x - _center.x, point.y - _center.y);
        double length = Math.sqrt(vec.x * vec.x + vec.y * vec.y);
        return (length <= _cr && lx1 <= point.x && point.x <= lx2 && ly1 <= point.y && point.y <= ly2);
    }

    public static Point2D.Double closestPointOnLine(double lx1, double ly1, double lx2, double ly2, Point2D.Double _center) {
        double A1 = ly2 - ly1;
        double B1 = lx1 - lx2;
        double C1 = (ly2 - ly1) * lx1 + (lx1 - lx2) * ly1;
        double C2 = -B1 * _center.x + A1 * _center.y;
        double det = A1 * A1 - -B1 * B1;
        double cx = 0;
        double cy = 0;
        if (det != 0) {
            cx = ((A1 * C1 - B1 * C2) / det);
            cy = ((A1 * C2 - -B1 * C1) / det);
        } else {
            cx = _center.x;
            cy = _center.y;
        }
        return new Point2D.Double(cx, cy);
    }

    protected static boolean circleLineIntersect(double x1, double y1, double x2, double y2, Point2D.Double _center, double _dmax) {
        return circleLineIntersect(x1, y1, x2, y2, _center.x, _center.y, _dmax);
    }

    protected static boolean circleLineIntersect(double x1, double y1, double x2, double y2, double cx, double cy, double _cr) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double a = dx * dx + dy * dy;
        double b = 2 * (dx * (x1 - cx) + dy * (y1 - cy));
        double c = cx * cx + cy * cy;
        c += x1 * x1 + y1 * y1;
        c -= 2 * (cx * x1 + cy * y1);
        c -= _cr * _cr;
        double bb4ac = b * b - 4 * a * c;

        //println(bb4ac);

        if (bb4ac < 0) {  // Not intersecting
            return false;
        } else {

            double mu = (-b + Math.sqrt(b * b - 4 * a * c)) / (2 * a);
            double ix1 = x1 + mu * (dx);
            double iy1 = y1 + mu * (dy);
            mu = (-b - Math.sqrt(b * b - 4 * a * c)) / (2 * a);
            double ix2 = x1 + mu * (dx);
            double iy2 = y1 + mu * (dy);

            // The intersection points
            //ellipse(ix1, iy1, 10, 10);
            //ellipse(ix2, iy2, 10, 10);

            double testX;
            double testY;
            // Figure out which point is closer to the circle
            if (Point2D.Double.distance(x1, y1, cx, cy) < Point2D.Double.distance(x2, y2, cx, cy)) {
                testX = x2;
                testY = y2;
            } else {
                testX = x1;
                testY = y1;
            }

            double d1 = Point2D.Double.distance(testX, testY, ix1, iy1);
            double d2 = Point2D.Double.distance(testX, testY, ix2, iy2);
            double dSeg = Point2D.Double.distance(x1, y1, x2, y2);
            if (Point2D.Double.distance(testX, testY, ix1, iy1) < Point2D.Double.distance(x1, y1, x2, y2) || Point2D.Double.distance(testX, testY, ix2, iy2) < Point2D.Double.distance(x1, y1, x2, y2)) {
                return true;
            } else {
                return false;
            }
        }
    }
}
