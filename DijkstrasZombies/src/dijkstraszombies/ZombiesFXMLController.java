/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dijkstraszombies;

import java.net.URL;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author mason
 */
public class ZombiesFXMLController implements Initializable {
    private double mouseX;
    private double mouseY;
   
    private boolean left = false;
    private boolean right = false;
    private boolean up = false;
    private boolean down = false;
    
    private boolean click = false;
    private int clickCount = 0;
    
    private final double playerWidth = 20;
    private final double playerHeight = 10;
    private final Color playerColor = Color.RED;
    
    private final double bulletWidth = 10;
    private final double bulletHeight = 5;
    private final Color bulletColor = Color.BLACK;
    private final double offset = (playerWidth / 2) + (bulletWidth / 2);
    private final double centerWidth = (playerWidth / 2) - (bulletWidth / 2);
    private final double centerHeight = (playerHeight / 2) - (bulletHeight / 2);
    
    private final Timer movePlayerTimer = new Timer("MovePlayerTimer");
    private final Timer shootTimer = new Timer("ShootTimer");
    
    private Rectangle playerRect;
    private final Player player = new Player();
    private final Hashtable<Bullet, Rectangle> bullets = new Hashtable<>();
    
    @FXML
    private AnchorPane root;
    
    public ZombiesFXMLController() {
        player.setHealth(100);
        player.setSpeed(1.5);
        player.wield(new Weapon(1, 5));
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        player.setX((root.getPrefWidth() / 2) - (playerWidth / 2));
        player.setY((root.getPrefHeight() / 2) - (playerWidth / 2));
        
        playerRect = new Rectangle(player.getX(), player.getY(), playerWidth, playerHeight);
        playerRect.setFill(playerColor);
        
        // Get mouse position
        root.setOnMouseMoved((mouseEvent) -> {
            mouseX = mouseEvent.getX();
            mouseY = mouseEvent.getY();
        });
        
        root.setOnMouseDragged((mouseEvent) -> {
            mouseX = mouseEvent.getX();
            mouseY = mouseEvent.getY();
        });
        
        // Get mouse clicked value
        root.setOnMousePressed((mouseEvent) -> {
            if(click != true) {
                click = true;
                clickCount++;
            }
        });
        
        root.setOnMouseReleased((mouseEvent) -> {
            click = false;
        });
        
        TimerTask shootTask = new TimerTask() {
            @Override()
            public void run() {
                if(click || clickCount > 0) {
                    clickCount = 0;
                    
                    Platform.runLater(() -> {
                        Bullet bullet = player.fireWeapon();
                        bullet.setX((player.getX() + centerWidth) + (offset * Math.cos(player.getTheta())));
                        bullet.setY((player.getY() + centerHeight) + (offset * Math.sin(player.getTheta())));

                        Rectangle bulletRect = new Rectangle(bullet.getX(), bullet.getY() , bulletWidth, bulletHeight);
                        bulletRect.setFill(bulletColor);
                        bulletRect.setRotate(getDegrees(bullet.getTheta()));

                        bullets.put(bullet, bulletRect);
                        
                        root.getChildren().add(bulletRect);
                    });
                }
            }
        };
        shootTimer.scheduleAtFixedRate(shootTask, 0, 400);
        
        // Move rectangle
        TimerTask movePlayerTask = new TimerTask() {
            @Override
            public void run() {
                // Calculate degrees of rotation
                player.setTheta(Math.atan2(mouseY - (player.getY() + (playerHeight / 2)), mouseX - (player.getX() + (playerWidth / 2))));
                
                Platform.runLater(() -> {
                    double xMovement = 0;
                    double yMovement = 0;
                    
                    // Move rectangle
                    if(left) {
                        double distance = player.getSpeed();
                        if(up || down) {
                            distance = player.getSpeed() / Math.sqrt(2);
                        }
                        player.translate(-distance, 0);
                        xMovement -= distance;
                    }
                    if(right) {
                        double distance = player.getSpeed();
                        if(up || down) {
                            distance = player.getSpeed() / Math.sqrt(2);
                        }
                        player.translate(distance, 0);
                        xMovement += distance;
                    }
                    if(up) {
                        double distance = player.getSpeed();
                        if(left || right) {
                            distance = player.getSpeed() / Math.sqrt(2);
                        }
                        player.translate(0, -distance);
                        yMovement -= distance;
                    }
                    if(down) {
                        double distance = player.getSpeed();
                        if(left || right) {
                            distance = player.getSpeed() / Math.sqrt(2);
                        }
                        player.translate(0, distance);
                        yMovement += distance;
                    }
                    
                    // Rotate rectangle 
                    playerRect.setRotate(getDegrees(player.getTheta()));
                    
                    // If out of bounds, move back in bounds
                    Bounds rectBounds = playerRect.getBoundsInParent();
                    if(rectBounds.getMinX() + xMovement < 0) {
                        player.translate(0 - (rectBounds.getMinX() + xMovement), 0);
                    } else if(rectBounds.getMaxX() + xMovement > root.getWidth()) {
                        player.translate(root.getWidth() - (rectBounds.getMaxX() + xMovement), 0);
                    }
                    if(rectBounds.getMinY() + yMovement < 0) {
                        player.translate(0, 0 - (rectBounds.getMinY() + yMovement));
                    } else if(rectBounds.getMaxY() + yMovement > root.getHeight()) {
                        player.translate(0, root.getHeight() - (rectBounds.getMaxY() + yMovement));
                    }
                    
                    // Apply translations
                    playerRect.setX(player.getX());
                    playerRect.setY(player.getY());
                    
                    // Move bullets
                    for(Weapon weapon : player.getWeapons()) {
                        for(int i = weapon.getBullets().size() - 1; i >= 0; i--) {
                            Bullet bullet = weapon.getBullets().get(i);
                            bullet.move();
                            
                            Rectangle bulletRect = bullets.get(bullet);
                            bulletRect.setX(bullet.getX());
                            bulletRect.setY(bullet.getY());
                            
                            Bounds bulletBounds = bulletRect.getBoundsInParent();
                            if(bulletBounds.getMaxX() < 0) {
                                root.getChildren().remove(bulletRect);
                                bullets.remove(bullet.destroy());
                                continue;
                            } else if(bulletBounds.getMinX() > root.getWidth()) {
                                root.getChildren().remove(bulletRect);
                                bullets.remove(bullet.destroy());
                                continue;
                            }
                            if(bulletBounds.getMaxY() < 0) {
                                root.getChildren().remove(bulletRect);
                                bullets.remove(bullet.destroy());
                            } else if(bulletBounds.getMinY() > root.getHeight()) {
                                root.getChildren().remove(bulletRect);
                                bullets.remove(bullet.destroy());
                            }
                        }
                    }
                });
            }
        };
        movePlayerTimer.scheduleAtFixedRate(movePlayerTask, 0, 15);
        
        root.getChildren().add(playerRect);
    }
    
        private void setArrowValues(KeyCode keyCode, boolean bool) {
        switch(keyCode) {
            case LEFT:
                if(left != bool) {
                    left = bool;
                }
                break;
            case RIGHT:
                if(right != bool) {
                    right = bool;
                }
                break;
            case UP:
                if(up != bool) {
                    up = bool;
                }
                break;
            case DOWN:
                if(down != bool) {
                    down = bool;
                }
                break;
        }
    }
    
    private double getDegrees(double theta) {
        return theta * (180 / Math.PI);
    }
    
    public void dispose() {
        movePlayerTimer.cancel();
        shootTimer.cancel();
    }
    
    public void sceneReady(Scene  scene) {
        // Get arrow key values
        scene.setOnKeyPressed((keyEvent) -> {
            setArrowValues(keyEvent.getCode(), true);
        });
        
        scene.setOnKeyReleased((keyEvent) -> {
            setArrowValues(keyEvent.getCode(), false);
        });
    }
}
