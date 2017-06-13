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
public class Character {
    private int health;
    private int maxHealth;
    private double x;
    private double y;
    private double theta;
    private double speed;
    private Weapon weapon;
    private ArrayList<Weapon> weapons = new ArrayList<>(1);
    private double armorPercentage = 0.0;
    
    public int getHealth() {
        return health;
    }
    
    public void setHealth(int health) {
        if(health > maxHealth) {
            maxHealth = health;
        }
        this.health = health;
        healthChanged(health);
    }
    
    public void damage(int damage) {
       health -= damage * (1 - (armorPercentage / 100));
       healthChanged(health);
    }
    
    public double getArmorPercentage() {
        return armorPercentage;
    }
    
    public void setArmorPercentage(double armor) {
        armorPercentage = armor;
    }
    
    public int getMaxHealth() {
        return maxHealth;
    }
    
    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
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
    
    public void setTheta(double theta) {
        this.theta = theta;
    }
    
    public double getSpeed() {
        return speed;
    }
    
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
    public void wield(Weapon weapon) {
        if(!weapons.contains(weapon)) {
            weapons.add(weapon);
        }
        this.weapon = weapon;
    }
    
    public void wield(int index) {
        wield(weapons.get(index));
    }
    
    public void addWeapon(Weapon weapon) {
        weapons.add(weapon);
    }
    
    public void removeWeapon(Weapon weapon) {
        weapons.remove(weapon);
    }
    
    public void removeWeapon(int index) {
        weapons.remove(index);
    }
    
    public void translate(double x, double y) {
        setX(getX() + x);
        setY(getY() + y);
    }
    
    public Weapon getWeapon() {
        return weapon;
    }
    
    public Bullet fireWeapon() {
        Bullet bullet = new Bullet(getWeapon(), getTheta());
        weapon.fireBullet(bullet);
        return bullet;
    }
    
    public ArrayList<Weapon> getWeapons() {
        return weapons;
    }
    
    public void setWeapons(ArrayList<Weapon> weapons) {
        this.weapons = weapons;
    }
    
    public void healthChanged(double health) {
        
    }
}
