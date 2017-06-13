/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dijkstraszombies;

import javafx.scene.paint.Color;

/**
 *
 * @author mason
 */
public class FiercePowerup extends Powerup {
    private final double speed;
    private final double armor;

    public FiercePowerup(Character player) {
        super(player, null, Color.PURPLE, 10000, 5000);
        
        speed = player.getSpeed();
        armor = player.getArmorPercentage();
    }
    
    @Override
    public void power() {
        player.setSpeed(speed * 2);
        for(Weapon weapon : player.getWeapons()) {
            weapon.setFireRatePercentage(400);
        }
        player.setArmorPercentage(100);
        
        super.power();
    }
    
    @Override
    public void cleanup() {
        player.setSpeed(speed);
        for(Weapon weapon : player.getWeapons()) {
            weapon.setFireRatePercentage(100);
        }
        player.setArmorPercentage(armor);
        
        super.cleanup();
    }
}
