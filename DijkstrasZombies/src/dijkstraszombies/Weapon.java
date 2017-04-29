/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dijkstraszombies;

import java.util.ArrayList;

/**
 *
 * @author mason
 */
public class Weapon {
    private final int damage;
    private final double bulletSpeed;
    private final Integer maxAmmo;
    private int ammoCount = 0;
    private ArrayList<Bullet> bullets = new ArrayList<>();
    
    public Weapon(int damage, double bulletSpeed) {
        this(damage, bulletSpeed, null);
    }
    
    public Weapon(int damage, double bulletSpeed, Integer maxAmmo) {
        this.damage = damage;
        this.bulletSpeed = bulletSpeed;
        this.maxAmmo = maxAmmo;
    }
    
    public void useAmmo() {
        if(ammoCount > 0) {
            ammoCount--;
        }
    }
    
    public void addAmmo(int count) {
        ammoCount += count;
    }
    
    public int getMaxAmmo() {
        return maxAmmo;
    }
    
    public int getDamage() {
        return damage;
    }
    
    public double getBulletSpeed() {
        return bulletSpeed;
    }
    
    public void fireBullet(Bullet bullet) {
        bullets.add(bullet);
        useAmmo();
    }
    
    public void destroyBullet(Bullet bullet) {
        bullets.remove(bullet);
    }
    
    public ArrayList<Bullet> getBullets() {
        return bullets;
    }
}
