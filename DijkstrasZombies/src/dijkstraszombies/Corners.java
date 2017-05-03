/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dijkstraszombies;

/**
 *
 * @author mason
 */
public class Corners {
    Point left;
    Point right;
    Point up;
    Point down;
    
    /* 
        Given the center of a rectangle: center,
        the rotation in radians: theta,
        the width of the rectangle: width,
        & the height of the rectangle: height,
        find the corners of said rectangle, such that:
        
        Left +----------+ Up
             |          |
        Down +----------+ Right
    
        Left, up, down, and right correspond to their
        respective extremes of the rectangle.  In the event
        that there are two points corresponding to an extreme
        of the rectangle, they will be ordered as above.
    */
    public Corners(Point center, double theta, double width, double height) {
        double length = Math.sqrt(Math.pow(width / 2, 2) + Math.pow(height / 2, 2));
        double adjTheta = Math.atan2(height / 2, width / 2);
        double xDiff = length * Math.cos(theta + adjTheta);
        double yDiff = length * Math.sin(theta + adjTheta);

        Point corner1 = new Point(center.getX() - xDiff, center.getY() - yDiff);
        Point corner2 = new Point(center.getX() + xDiff, center.getY() + yDiff);
        Point corner3 = new Point(center.getX() + yDiff, center.getY() - xDiff);
        Point corner4 = new Point(center.getX() - yDiff, center.getY() + xDiff);
        
        if(theta == -Math.PI) {
            left = corner2;
            right = corner1;
            up = corner4;
            down = corner3;
        } else if(theta <= -Math.PI / 2) {
            left = corner3;
            right = corner4;
            up = corner2;
            down = corner1;
        } else if(theta <= 0) {
            left = corner1;
            right = corner2;
            up = corner3;
            down = corner4;
        } else if(theta <= Math.PI / 2) {
            left = corner4;
            right = corner3;
            up = corner1;
            down = corner2;
        } else if(theta <= Math.PI) {
            left = corner2;
            right = corner1;
            up = corner4;
            down = corner3;
        }
    }
    
    public Point getLeft() {
        return left;
    }
    
    public Point getRight() {
        return right;
    }
    
    public Point getUp() {
        return up;
    }
    
    public Point getDown() {
        return down;
    }
    
    @Override
    public String toString() {
        return "Left: " + left.toString() + "\n" + 
                "Right: " + right.toString() + "\n" + 
                "Up: " + up.toString() + "\n" + 
                "Down: " + down.toString();
    }
}
