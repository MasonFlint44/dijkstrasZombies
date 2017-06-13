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
    private int damage;
    private final double bulletSpeed;
    private final Integer maxAmmo;
    private int ammoCount = 0;
    private int fireRate;
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private double fireRatePercentage = 100;
    
    public Weapon(int damage, double bulletSpeed, int fireRate) {
        this(damage, bulletSpeed, fireRate,null);
    }
    
    public Weapon(int damage, double bulletSpeed, int fireRate, Integer maxAmmo) {
        this.damage = damage;
        this.bulletSpeed = bulletSpeed;
        this.fireRate = fireRate;
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
    
    public int getFireRate() {
        return (int)Math.floor(fireRate / (fireRatePercentage / 100));
    }
    
    public void setFireRatePercentage(double fireRatePercentage) {
        this.fireRatePercentage = fireRatePercentage;
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
    
    public void setDamage(int damage) {
        this.damage = damage;
    }
    
    public void setFireRate(int fireRate) {
        this.fireRate = fireRate;
    }
}
