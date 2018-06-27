/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dijkstraszombies;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javafx.scene.paint.Color;

/**
 *
 * @author mason
 */
public abstract class Powerup {
    protected final Character player;
    protected final ArrayList<Character> zombies;
    protected final Color color;
    protected final ArrayList<ChangeListener> timeoutListeners = new ArrayList<>();
    
    private final Timer timeoutTimer;
    private final TimerTask timeoutTask;
    private final Timer powerTimer;
    private final TimerTask powerTask;
    private final int powerTimeInMilliseconds;
    private double x;
    private double y;
    private double theta = 0;
    
    public Powerup(Character player, ArrayList<Character> zombies, Color color, int timeoutInMilliseconds, int powerTimeInMilliseconds) {
        this.player = player;
        this.zombies = zombies;
        this.color = color;
        this.powerTimeInMilliseconds = powerTimeInMilliseconds;
        
        timeoutTimer = new Timer(getClass().getSimpleName() + "TimeoutTimer");
        powerTimer = new Timer(getClass().getSimpleName() + "PowerTimer");
        
        timeoutTask = new TimerTask() {
            @Override
            public void run() {
                timeout();
            }
        };
        if(timeoutInMilliseconds > 0) {
            timeoutTimer.schedule(timeoutTask, timeoutInMilliseconds);
        }
        
        powerTask = new TimerTask() {
           @Override
           public void run() {
               cleanup();
           }
        };
    }
    
    public void power() {
        timeoutTimer.cancel();
        if(powerTimeInMilliseconds > 0) {
            powerTimer.schedule(powerTask, powerTimeInMilliseconds);
        } else {
            cleanup();
        }
    }
    
    public void cleanup() {
        dispose();
    }
    
    public void dispose() {
        powerTimer.cancel();
        timeoutTimer.cancel();
    }
    
    private void timeout() {
        for(ChangeListener listener : timeoutListeners) {
            listener.changed();
        }
        dispose();
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setX(double x) {
        this.x = x;
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
    
    public Color getColor() {
        return color;
    }
    
    public void addTimeoutListener(ChangeListener listener) {
        timeoutListeners.add(listener);
    }
}
