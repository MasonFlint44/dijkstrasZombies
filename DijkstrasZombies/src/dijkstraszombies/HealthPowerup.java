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
public class HealthPowerup extends Powerup{
    public HealthPowerup(Character player) {
        super(player, null, Color.RED, 10000, 0);
    }

    @Override
    public void power() {
        player.setHealth(player.getHealth() + 10);
        super.power();
    }
}
