/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dijkstraszombies;

import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author mason
 */
public class Bullet {
    private final Weapon parent;
    private final double theta;
    private double x;
    private double y;
    
    public Bullet(Weapon parent, double theta) {
        this.parent = parent;
        this.theta = theta;
    }
    
    public Bullet(Weapon parent, double theta, double x, double y) {
        this(parent, theta);
        this.x = x;
        this.y = y;
    }
    
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    public double getTheta() {
        return theta;
    }
    
    public Bullet destroy() {
        parent.destroyBullet(this);
        return this;
    }
    
    public void move() {
        setX(getX() + parent.getBulletSpeed() * Math.cos(theta));
        setY(getY() + parent.getBulletSpeed() * Math.sin(theta));
    }
}
