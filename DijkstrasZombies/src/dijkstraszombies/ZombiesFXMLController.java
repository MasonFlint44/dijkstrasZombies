/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dijkstraszombies;

import graph.Graph;
import graph.Vertex;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 *
 * @author mason
 */
public class ZombiesFXMLController implements Initializable {
    // Mouse coordinates
    private double mouseX;
    private double mouseY;
    
    // Mouse click properties
    private boolean click = false;
    private int clickCount = 0;
   
    // Key pressed values
    private boolean left = false;
    private boolean right = false;
    private boolean up = false;
    private boolean down = false;
    private boolean spaceBar = false;
    
    // Player values
    private final Character player = new Character() {
        @Override
        public void healthChanged(double health) {
            Platform.runLater(() -> {
                healthText.setText("Health: " + player.getHealth());
            });
        }
    };
    private final double playerWidth = 9;
    private final double playerHeight = 9;
    private final Color playerColor = Color.RED;
    private final int playerHealth = 100;
    private final double playerSpeed = 1.5;
    private int killCount = 0;
    private Rectangle playerRect;
    private Point playerCenter;
    private Vertex playerVertex;
    
    // Bullet values
    private final double bulletWidth = 4;
    private final double bulletHeight = 4;
    private final Color bulletColor = Color.BLACK;
//    private final double offset = (playerWidth / 2) + (bulletWidth / 2);
//    private final double centerWidth = (playerWidth / 2) + (bulletWidth / 2);
//    private final double centerHeight = (playerHeight / 2) - (bulletHeight / 2);
    
    // Powerup values
    private final int powerupSpawnRate = 1200;
    private int powerupSpawnCounter = 0;
    
    // Timers
    private final Timer movementTimer = new Timer("MovementTimer");
    private final Timer bulletMoveTimer = new Timer("BulletMove");
    private final Timer zombieMoveTimer = new Timer("ZombieMove");
    private final Timer dijkstraTimer = new Timer("DijkstraTimer");
    private final Timer powerupTimer = new Timer("PowerupTimer");
    
    // Grid values
    private final Graph graph;
    private final int gridWidth = 50;
    private final int gridHeight = 50;
    private double cellWidth;
    private double cellHeight;
    
    // Zombie values
    private final int zombieSpawnRate = 30;
    private final int zombieMaxCount = 100;
    private final double zombieSpeed = 1;
    private final int zombieHealth = 1;
    
    // Collections
    private final ArrayList<Character> zombieList = new ArrayList<>();
    private final HashMap<Bullet, Rectangle> bullets = new HashMap<>();
    private final HashMap<Character, Rectangle> zombies = new HashMap<>();
    private final HashMap<Character, ArrayList<Vertex>> dijkstraPaths = new HashMap<>();
    private final HashMap<Character, Corners> zombieCorners = new HashMap<>();
    private final ArrayList<Powerup> powerupList = new ArrayList<>();
    private final HashMap<Powerup, Rectangle> powerups = new HashMap<>();
    
    // Rate counters
    private int fireCounter = 0;
    private int zombieSpawnCounter = 0;
    
    // Scene properties
    private final double sceneWidth = 500;
    private final double sceneHeight = 500;
    
    // Flags
    private volatile boolean pauseGame = false;
    
    // UI Elements
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private GridPane gridPane;
    @FXML
    private Text healthText;
    @FXML
    private Text killText;
    @FXML
    private Text pauseText;
    
    public ZombiesFXMLController() {
        // Build player
        player.setHealth(playerHealth);
        player.setSpeed(playerSpeed);
        player.wield(new Weapon(1, 7, 15));
        
        // Construct graph
        graph = new Graph(gridWidth * gridHeight);
        gridify(graph, gridWidth, gridHeight);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Platform.runLater(() -> {
            anchorPane.setPrefSize(sceneWidth, sceneHeight);
            gridPane.setPrefSize(sceneWidth, sceneHeight);
        });
        
        cellWidth = sceneWidth / gridWidth;
        cellHeight = sceneHeight / gridHeight;
        
        // Build walls
        // Top wall
        for(int i  = 408; i <= 441; i++) {
            graph.disconnect(i);
            
            int index = i;
            Platform.runLater(() -> {
                Rectangle rect = new Rectangle(cellWidth, cellHeight, Color.BLACK);
                GridPane.setColumnIndex(rect, index % gridWidth);
                GridPane.setRowIndex(rect, index / gridWidth);
                gridPane.getChildren().add(rect);
            });
            
            if(i == 422) {
                i += 4;
            }
        }
        
        // Bottom wall
        for(int i  = 2058; i <= 2091; i++) {
            graph.disconnect(i);
            
            int index = i;
            Platform.runLater(() -> {
                Rectangle rect = new Rectangle(cellWidth, cellHeight, Color.BLACK);
                GridPane.setColumnIndex(rect, index % gridWidth);
                GridPane.setRowIndex(rect, index / gridWidth);
                gridPane.getChildren().add(rect);
            });
            
            if(i == 2072) {
                i += 4;
            }
        }
        
        // Left wall
        for(int i = 408; i <= 2058; i += gridWidth) {
            graph.disconnect(i);
            
            int index = i;
            Platform.runLater(() -> {
                Rectangle rect = new Rectangle(cellWidth, cellHeight, Color.BLACK);
                GridPane.setColumnIndex(rect, index % gridWidth);
                GridPane.setRowIndex(rect, index / gridWidth);
                gridPane.getChildren().add(rect);
            });
            
            if(i == 1108) {
                i += 4 * gridWidth;
            }
        }
        
        // Right wall
        for(int i = 441; i <= 2091; i += gridWidth) {
            graph.disconnect(i);
            
            int index = i;
            Platform.runLater(() -> {
                Rectangle rect = new Rectangle(cellWidth, cellHeight, Color.BLACK);
                GridPane.setColumnIndex(rect, index % gridWidth);
                GridPane.setRowIndex(rect, index / gridWidth);
                gridPane.getChildren().add(rect);
            });
            
            if(i == 1141) {
                i += 4 * gridWidth;
            }
        }
        
        // Set up UI
        Platform.runLater(() -> {
            pauseText.setVisible(false);
            killText.setText("Kills: " + killCount);
            
            player.setX((sceneWidth / 2) - (playerWidth / 2));
            player.setY((sceneHeight / 2) - (playerWidth / 2));
            
            playerRect = new Rectangle(player.getX(), player.getY(), playerWidth, playerHeight);
            playerRect.setFill(playerColor);
        });
        
        // Get mouse position
        anchorPane.setOnMouseMoved((mouseEvent) -> {
            mouseX = mouseEvent.getX();
            mouseY = mouseEvent.getY();
        });
        
        anchorPane.setOnMouseDragged((mouseEvent) -> {
            mouseX = mouseEvent.getX();
            mouseY = mouseEvent.getY();
        });
            
        // Get mouse clicked value
        anchorPane.setOnMousePressed((mouseEvent) -> {
            if(click != true) {
                click = true;
                clickCount++;
            }
        });
        
        anchorPane.setOnMouseReleased((mouseEvent) -> {
            click = false;
        });
        
        // Calculate paths of zombies
        TimerTask dijkstraTask = new TimerTask() {
            @Override
            public void run() {
                if(!pauseGame) {
                    for(int i = 0; i < zombieList.size(); i++) {
                        Character zombie = zombieList.get(i);
                        ArrayList<Vertex> zombiePath = graph.dijkstras(getGridIdentity(zombie.getX() + playerWidth / 2, zombie.getY() + playerHeight / 2), playerVertex.getIdentity());
                        dijkstraPaths.put(zombie, zombiePath);
                    }
                }
            }
        };
        dijkstraTimer.scheduleAtFixedRate(dijkstraTask, 0, 15);
        
        // Move player
        TimerTask movementTask = new TimerTask() {
            @Override
            public void run() {
                if(!pauseGame) {
                    // Calculate degrees of rotation
                    player.setTheta(Math.atan2(mouseY - (player.getY() + (playerHeight / 2)), mouseX - (player.getX() + (playerWidth / 2))));

                    // Get center of player
                    playerCenter = new Point(player.getX() + (playerWidth / 2), player.getY() + (playerHeight / 2));

                    double xMovement = 0;
                    double yMovement = 0;

                    // Move player
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

                    // Get list of edge points for movement tracking
                    Corners playerCorners = new Corners(playerCenter, player.getTheta(), playerWidth, playerHeight);
                    ArrayList<Point> edgePoints = getEdgePoints(playerCorners);

                    // Check player for wall collisions
                    checkForWallCollisions(edgePoints, playerCorners, player, playerCenter, xMovement, yMovement);

                    Platform.runLater(() -> {
                        // Rotate rectangle 
                        playerRect.setRotate(getDegrees(player.getTheta()));

                        // Apply translations
                        playerRect.setX(player.getX());
                        playerRect.setY(player.getY());
                    });
                    
                    playerCenter = new Point(player.getX() + (playerWidth / 2), player.getY() + (playerHeight / 2));
                    playerCorners = new Corners(playerCenter, player.getTheta(), playerWidth, playerHeight);

                    // Check for collisions with zombies
                    for(int i = 0; i < zombieList.size(); i++) {
                        Character zombie = zombieList.get(i);
                        Corners zombieCorner = zombieCorners.get(zombie);

                        if(zombieCorner == null) {
                            continue;
                        }

                        Point zombieCenter = new Point(zombie.getX() + (playerWidth / 2), zombie.getY() + (playerHeight / 2));

                        if(getDistance(playerCenter, zombieCenter) <= 2 * Math.sqrt(Math.pow(playerWidth / 2, 2) + Math.pow(playerHeight / 2, 2))) {
                            if(overlaps(playerCorners, playerCenter, zombieCorner, zombieCenter)) {
                                if(player.getHealth() > 0) {
                                    player.damage(zombie.getWeapon().getDamage());
                                } else {
                                    pauseGame = true;
                                    pauseText.setVisible(true);
                                }
                            }
                        }
                    }
                    
                    // Check for collisions with powerups
                    for(int i = 0; i < powerupList.size(); i++) {
                        Powerup powerup = powerupList.get(i);
                        Point powerupCenter = new Point(powerup.getX() + playerWidth / 2, powerup.getY() + playerHeight / 2);
                        Corners powerupCorners = new Corners(powerupCenter, 0, playerWidth, playerHeight);
                        
                        if(getDistance(playerCenter, powerupCenter) <= 2 * Math.sqrt(Math.pow(playerWidth / 2, 2) + Math.pow(playerHeight / 2, 2))) {
                            if(overlaps(playerCorners, playerCenter, powerupCorners, powerupCenter)) {
                                powerup.power();
                                powerupList.remove(powerup);
                                
                                Platform.runLater(() -> {
                                    Rectangle powerupRect = powerups.remove(powerup);
                                    try {
                                        anchorPane.getChildren().remove(powerupRect);
                                    } catch(Exception e) {
                                        // Do nothing
                                    }
                                });
                            }
                        }
                    }

                    // Used for zombies to track player
                    playerVertex = graph.getVertex(getGridIdentity(playerCenter.getX() + xMovement, playerCenter.getY() + yMovement));
                    
                } else {
                    if(spaceBar) {
                        //Reset game
                        Platform.runLater(() -> {
                            for(Character resetZombie: zombieList) {
                                anchorPane.getChildren().remove(zombies.get(resetZombie));
                            }
                            zombieList.clear();
                            zombies.clear();

                            killCount = 0;
                            killText.setText("Kills: " + killCount);

                            for(Weapon weapon: player.getWeapons()) {
                                for(Bullet bullet: weapon.getBullets()) {
                                    anchorPane.getChildren().remove(bullets.get(bullet));
                                }
                                weapon.getBullets().clear();
                            }
                            
                            for(Powerup powerup : powerupList) {
                                anchorPane.getChildren().remove(powerups.get(powerup));
                                powerup.dispose();
                            }
                            powerups.clear();
                            powerupList.clear();

                            dijkstraPaths.clear();

                            player.setMaxHealth(100);
                            player.setHealth(player.getMaxHealth());
                            healthText.setText("Health: " + player.getHealth());

                            player.setX(sceneWidth / 2);
                            player.setY(sceneWidth / 2);
                            player.setTheta(0);

                            playerRect.setX(player.getX());
                            playerRect.setY(player.getY());
                            playerRect.setRotate(player.getTheta());

                            playerCenter = new Point(player.getX() + (playerWidth / 2), player.getY() + (playerHeight / 2));

                            pauseGame = false;
                            pauseText.setVisible(false);
                        });
                    }
                }
            }
        };
        movementTimer.scheduleAtFixedRate(movementTask, 0, 15);
        
        TimerTask zombieMoveTask = new TimerTask() {
            @Override
            public void run() {
                if(!pauseGame) {
                    // Spawn zombies
                    if (zombieSpawnCounter++  >= zombieSpawnRate && zombieList.size() < zombieMaxCount) {
                        zombieSpawnCounter = 0;

                        Character zombie = new Character();
                        zombie.setHealth(zombieHealth);
                        zombie.setSpeed(zombieSpeed);
                        zombie.wield(new Weapon(1, 0, 0));

                        // Randomly spawn zombie along edge of screen
                        double random = Math.random();
                        
                        // Top edge
                        if(random < 0.25) {
                            zombie.setX(Math.random() * (sceneWidth - playerWidth));
                            zombie.setY(0);
                        } 
                        // Right edge
                        else if(random < 0.5) {
                            zombie.setX(sceneWidth - playerWidth);
                            zombie.setY(Math.random() * (sceneHeight - playerHeight));
                        }
                        // Bottom edge
                        else if(random < 0.75) {
                            zombie.setX(Math.random() * (sceneWidth - playerWidth));
                            zombie.setY(sceneHeight - playerHeight);
                        }
                        // Left edge
                        else {
                            zombie.setX(0);
                            zombie.setY(Math.random() * (sceneHeight - playerHeight));
                        }

                        Platform.runLater(() -> {
                            Rectangle zombieRect = new Rectangle(zombie.getX(), zombie.getY(), playerWidth, playerHeight);
                            zombieRect.setFill(Color.GREEN);
                            anchorPane.getChildren().add(zombieRect);

                            zombieList.add(zombie);
                            zombies.put(zombie, zombieRect);
                        });
                    }
                    
                    // Move zombies
                    for(int i = 0; i < zombieList.size(); i++) {
                        Character zombie = zombieList.get(i);

                        ArrayList<Vertex> zombiePath = dijkstraPaths.get(zombie);
                        if(zombiePath == null) {
                            continue;
                        }

                        Point zombieCenter = new Point(zombie.getX() + (playerWidth / 2), zombie.getY() + (playerHeight / 2));

                        if(zombiePath.size() > 1){
                            try {
                                int index = zombiePath.indexOf(graph.getVertex(getGridIdentity(zombieCenter.getX(), zombieCenter.getY())));

                                if(index < 0) {
                                    continue;
                                }

                                double yDifference = (((int)(zombiePath.get(index + 1).getIdentity() / gridWidth) * cellHeight) + cellHeight / 2) - (zombie.getY() + (playerHeight / 2));
                                double xDifference = ((zombiePath.get(index + 1).getIdentity() % gridWidth * cellWidth) + cellWidth / 2) - (zombie.getX() + (playerWidth / 2)) ;

                                zombie.setTheta(Math.atan2(yDifference , xDifference));

                                double xZombieMove = zombie.getSpeed() * Math.cos(zombie.getTheta());
                                double yZombieMove = zombie.getSpeed() * Math.sin(zombie.getTheta());

                                Corners corners = new Corners(zombieCenter, zombie.getTheta(), playerWidth, playerHeight);
                                ArrayList<Point> zombieEdges = getEdgePoints(corners);
                                checkForWallCollisions(zombieEdges, corners, zombie, zombieCenter, xZombieMove, yZombieMove);
                                zombie.translate(xZombieMove, yZombieMove);

                                // Store corners of zombie to check for bullet collisions
                                zombieCorners.put(zombie, new Corners(new Point(zombie.getX() + playerWidth / 2, zombie.getY() + playerHeight / 2), zombie.getTheta(), playerWidth, playerHeight));

                            } catch(IndexOutOfBoundsException ex) {
                                // Do nothing
                            }
                        } else {
                            zombie.setTheta(Math.atan2(playerCenter.getY() - zombieCenter.getY(), playerCenter.getX() - zombieCenter.getX()));
                            double xZombieMove = zombie.getSpeed() * Math.cos(zombie.getTheta());
                            double yZombieMove = zombie.getSpeed() * Math.sin(zombie.getTheta());
                            zombie.translate(xZombieMove, yZombieMove);
                        }

                        Platform.runLater(() -> {
                            try {
                                Rectangle zombieRect = zombies.get(zombie);

                                zombieRect.setX(zombie.getX());
                                zombieRect.setY(zombie.getY());
                                zombieRect.setRotate(getDegrees(zombie.getTheta()));
                            } catch(NullPointerException ex) {
                                // Do nothing
                            }
                        });
                    }
                }
            }
        };
        zombieMoveTimer.scheduleAtFixedRate(zombieMoveTask, 0, 15);
        
        TimerTask bulletMoveTask = new TimerTask(){
            @Override
            public void run() {
                if(!pauseGame) {
                    // Fire bullets
                    if(fireCounter++ >= player.getWeapon().getFireRate()) {
                        fireCounter = 0;

                        if(click || clickCount > 0) {
                            clickCount = 0;

                            Bullet bullet = player.fireWeapon();
//                            bullet.setX((player.getX() + centerWidth) + (offset * Math.cos(player.getTheta())));
//                            bullet.setY((player.getY() + centerHeight) + (offset * Math.sin(player.getTheta())));
                            bullet.setX(player.getX() + (playerWidth / 2) - (bulletWidth / 2));
                            bullet.setY(player.getY() + (playerHeight / 2) - (bulletWidth / 2));

                            Platform.runLater(() -> {
                                Rectangle bulletRect = new Rectangle(bullet.getX(), bullet.getY() , bulletWidth, bulletHeight);
                                bulletRect.setFill(bulletColor);
                                bulletRect.setRotate(getDegrees(bullet.getTheta()));

                                bullets.put(bullet, bulletRect);
                                anchorPane.getChildren().add(bulletRect);
                            });
                        }
                    }
                    
                    // Move bullets
                    for(Weapon weapon : player.getWeapons()) {
                        for(int i = weapon.getBullets().size() - 1; i >= 0; i--) {
                            Bullet bullet = weapon.getBullets().get(i);

                            if(bullet == null) {
                                continue;
                            }

                            bullet.move();

                            Point bulletCenter = new Point(bullet.getX() + (bulletWidth / 2), bullet.getY() + (bulletHeight / 2));

                            Rectangle bulletRect = bullets.get(bullet);

                            if(bulletRect == null) {
                                continue;
                            }

                            Platform.runLater(() -> {
                                bulletRect.setX(bullet.getX());
                                bulletRect.setY(bullet.getY());
                            });

                            // Check for wall collisions
                            Corners bulletCorners = new Corners(bulletCenter, bullet.getTheta(), bulletWidth, bulletHeight);
                            ArrayList<Point> bulletEdges = getEdgePoints(bulletCorners);
                            if(checkForBulletCollision(bulletEdges, bulletCorners, bulletCenter)) {
                                Platform.runLater(() -> {
                                    anchorPane.getChildren().remove(bulletRect);
                                });
                                bullets.remove(bullet.destroy());
                            }
                        }
                    }
                }
            }
        };
        bulletMoveTimer.scheduleAtFixedRate(bulletMoveTask, 0, 15);
        
        TimerTask powerupTask = new TimerTask() {
            @Override
            public void run() {
                for(int i = 0; i < powerupList.size(); i++) {
                    Powerup powerup = powerupList.get(i);
                    powerup.setTheta(powerup.getTheta() + .05236);
                    
                    Platform.runLater(() -> {
                        try {
                            powerups.get(powerup).setRotate(getDegrees(powerup.getTheta()));
                        } catch(Exception e) {
                            // Do nothing
                        }
                    });
                }
                
                if(pauseGame) {
                    return;
                }

                if(powerupSpawnCounter++ < powerupSpawnRate) {
                    return;
                }
                
                // Generate powerup
                powerupSpawnCounter = 0;
                
                double random = Math.random();
                Powerup powerup;
                if(random < 0.5) {
                    powerup = new FiercePowerup(player);
                } else {
                    powerup = new HealthPowerup(player);
                }
                
                powerupList.add(powerup);
                
                // Ensure powerup is not on a wall
                double x;
                double y;
                do {
                    x = Math.random() * (sceneWidth - playerWidth);
                    y = Math.random() * (sceneHeight - playerHeight);
                } while(graph.getVertex(getGridIdentity(x + playerWidth / 2, y + playerHeight / 2)).getNeighborCount() == 0);

                powerup.setX(x);
                powerup.setY(y);

                Platform.runLater(() -> {
                    Rectangle powerupRect = new Rectangle(powerup.getX(), powerup.getY(), playerWidth, playerHeight);
                    powerupRect.setFill(powerup.getColor());

                    powerups.put(powerup, powerupRect);
                    anchorPane.getChildren().add(powerupRect);
                    
                    powerup.addTimeoutListener(() -> {
                        powerupList.remove(powerup);
                        
                        Platform.runLater(() -> {
                            anchorPane.getChildren().remove(powerups.remove(powerup));
                        });
                    });
                });
            }
        };
        powerupTimer.scheduleAtFixedRate(powerupTask, 0, 15);
        
        Platform.runLater(() -> {
            anchorPane.getChildren().add(playerRect);
        });
    }
    
    private boolean checkForBulletCollision(ArrayList<Point> edgePoints, Corners corners, Point center) {
        // Check for collisions with zombies
        for(int i = 0; i < zombieList.size(); i++) {
            Character zombie = zombieList.get(i);
            Corners zombieCorner = zombieCorners.get(zombie);
            
            if(zombieCorner == null) {
                continue;
            }
            
            Point zombieCenter = new Point(zombie.getX() + (playerWidth / 2), zombie.getY() + (playerHeight / 2));
            
            if(getDistance(center, zombieCenter) <= Math.sqrt(Math.pow(bulletWidth / 2, 2) + Math.pow(bulletHeight / 2, 2)) + Math.sqrt(Math.pow(playerWidth / 2, 2) + Math.pow(playerHeight / 2, 2))) {
                if(overlaps(corners, center, zombieCorner, zombieCenter)) {
                    zombie.damage(player.getWeapon().getDamage());
                    
                    if(zombie.getHealth() <= 0) {
                        killCount++;

                        Platform.runLater(() -> {
                            killText.setText("Kills: " + killCount);

                            try {
                                zombieList.remove(zombie);
                                Rectangle rect = zombies.remove(zombie);
                                anchorPane.getChildren().remove(rect);
                            } catch(ArrayIndexOutOfBoundsException ex) {
                                // Do nothing
                            }
                        });
                        return true;
                    }
                }
            }
        }
        
        // Check for collisions with exterior walls
        if(corners.getLeft().getX() < 0) {
            return true;
        } else if(corners.getRight().getX() > anchorPane.getWidth()) {
            return true;
        }
        if(corners.getUp().getY() < 0) {
            return true;
        } else if(corners.getDown().getY() > anchorPane.getHeight()) {
            return true;
        }
        
        // Check for collisions with interior walls
        for(Point point: edgePoints) {
            Vertex vertex = graph.getVertex(getGridIdentity(point));
            if(vertex.getNeighborCount() == 0) {
                return true;
            }
        }
        return false;
    }
    
    private void checkForWallCollisions(ArrayList<Point> edgePoints, Corners corners, Character character, Point center, double xMovement, double yMovement) {
        //Check for collisions with exterior walls
        if(corners.getLeft().getX() + xMovement < 0) {
            character.translate(0 - (corners.getLeft().getX() + xMovement), 0);
        } else if(corners.getRight().getX() + xMovement > sceneWidth) {
            character.translate(sceneWidth - (corners.getRight().getX() + xMovement), 0);
        }
        if(corners.getUp().getY() + yMovement < 0) {
            character.translate(0, 0 - (corners.getUp().getY()  + yMovement));
        } else if(corners.getDown().getY()  + yMovement > sceneHeight) {
            character.translate(0, sceneHeight - (corners.getDown().getY() + yMovement));
        }
        
        // Check for collisions with interior walls
        for(Point point: edgePoints) {
            Point translatedPoint = new Point(point.getX() + xMovement, point.getY() + yMovement);
            Vertex vertex = graph.getVertex(getGridIdentity(translatedPoint));
            if(vertex.getNeighborCount() == 0) {
                Point vertexCenter = new Point(((vertex.getIdentity() % gridWidth) * cellWidth) + (cellWidth / 2), ((vertex.getIdentity() / gridWidth) * cellHeight) + (cellHeight / 2));
                double edge;
                double translation;
                if(Math.abs(vertexCenter.getX() - center.getX()) > Math.abs(vertexCenter.getY() - center.getY())) {
                    if(center.getX() < vertexCenter.getX()) {
                        edge = (vertex.getIdentity() % gridWidth) * cellWidth;
                    } else {
                        edge = ((vertex.getIdentity() % gridWidth) + 1) * cellWidth;
                    }
                    translation = edge - translatedPoint.getX();
                    xMovement += translation;
                    character.translate(translation, 0);
                } else {
                    if(center.getY() < vertexCenter.getY()) {
                        edge = (vertex.getIdentity() / gridWidth) * cellHeight;
                    } else {
                        edge = ((vertex.getIdentity() / gridWidth) + 1) * cellHeight;
                    }
                    translation = edge - translatedPoint.getY();
                    yMovement += translation;
                    character.translate(0, translation);
                }
            }
        }
    }
    
    private ArrayList<Point> getEdgePoints(Corners corners) {
        Point left = corners.getLeft();
        Point right = corners.getRight();
        Point up = corners.getUp();
        Point down = corners.getDown();
        
        ArrayList<Point> edgePoints = new ArrayList<>();

        edgePoints.add(left);
        edgePoints.add(right);
        edgePoints.add(up);
        edgePoints.add(down);

        // Calculate points for leftward movement
        double slope = (up.getY() - left.getY()) / (up.getX() - left.getX());
        for(int leftIndex = getGridX(left.getX()) + 1; leftIndex <= getGridX(up.getX()); leftIndex++) {
            double x = leftIndex * cellWidth;
            double y = slope * (x - left.getX()) + left.getY();
            edgePoints.add(new Point(x, y));
        }
        slope = (down.getY() - left.getY()) / (down.getX() - left.getX());
        for(int leftIndex = getGridX(left.getX()) + 1; leftIndex <= getGridX(down.getX()); leftIndex++) {
            double x = leftIndex * cellWidth;
            double y = slope * (x - left.getX()) + left.getY();
            edgePoints.add(new Point(x, y));
        }

        // Calculate points for rightward movement
        slope = (up.getY() - right.getY()) / (up.getX() - right.getX());
        for(int rightIndex = getGridX(right.getX()) - 1; rightIndex >= getGridX(up.getX()); rightIndex--) {
            double x = (rightIndex + 1) * cellWidth;
            double y = slope * (x - right.getX()) + right.getY();
            edgePoints.add(new Point(x, y));
        }
        slope = (down.getY() - right.getY()) / (down.getX() - right.getX());
        for(int rightIndex = getGridX(right.getX()) - 1; rightIndex >= getGridX(down.getX()); rightIndex--) {
            double x = (rightIndex + 1) * cellWidth;
            double y = slope * (x - right.getX()) + right.getY();
            edgePoints.add(new Point(x, y));
        }                   

        // Calculate points for upward movement
        slope = (left.getY() - up.getY()) / (left.getX() - up.getX());
        for(int upIndex = getGridY(left.getY()); upIndex > getGridY(up.getY()); upIndex--) {
            double y = upIndex * cellHeight;
            double x = (y - up.getY()) / slope + up.getX();
            edgePoints.add(new Point(x, y));
        }
        slope = (right.getY() - up.getY()) / (right.getX() - up.getX());
        for(int upIndex = getGridY(right.getY()); upIndex > getGridY(up.getY()); upIndex--) {
            double y = upIndex * cellHeight;
            double x = (y - up.getY()) / slope + up.getX();
            edgePoints.add(new Point(x, y));
        }

        // Calculate points for downward movement
        slope = (left.getY() - down.getY()) / (left.getX() - down.getX());
        for(int downIndex = getGridY(left.getY()) + 1; downIndex < getGridY(down.getY()); downIndex++) {
            double y = downIndex * cellHeight;
            double x = (y - down.getY()) / slope + down.getX();
            edgePoints.add(new Point(x, y));
        }
        slope = (right.getY() - down.getY()) / (right.getX() - down.getX());
        for(int downIndex = getGridY(right.getY()) + 1; downIndex < getGridY(down.getY()); downIndex++) {
            double y = downIndex * cellHeight;
            double x = (y - down.getY()) / slope + down.getX();
            edgePoints.add(new Point(x, y));
        }
            
        return edgePoints;
    }
    
    private void setArrowValues(KeyCode keyCode, boolean bool) {
        switch(keyCode) {
            case LEFT:
            case A:
                if(left != bool) {
                    left = bool;
                }
                break;
            case RIGHT:
            case D:
                if(right != bool) {
                    right = bool;
                }
                break;
            case UP:
            case W:
                if(up != bool) {
                    up = bool;
                }
                break;
            case DOWN:
            case S:
                if(down != bool) {
                    down = bool;
                }
                break;
            case SPACE:
                if(spaceBar != bool) {
                    spaceBar = bool;
                }
                break;
        }
    }
    
    private double getDegrees(double theta) {
        return theta * (180 / Math.PI);
    }
    
    // Cancel timers - gets called on exit in main method
    public void dispose() {
        movementTimer.cancel();
        dijkstraTimer.cancel();
        bulletMoveTimer.cancel();
        zombieMoveTimer.cancel();
        powerupTimer.cancel();
        for(Powerup powerup : powerupList) {
            powerup.dispose();
        }
    }
    
    // Add listeners to the scene
    public void sceneReady(Scene scene) {
        // Get arrow key values
        scene.setOnKeyPressed((keyEvent) -> {
            setArrowValues(keyEvent.getCode(), true);
        });
        
        scene.setOnKeyReleased((keyEvent) -> {
            setArrowValues(keyEvent.getCode(), false);
        });
    }
    
    public boolean overlaps(Corners corners1, Point center1, Corners corners2, Point center2) {
        if(center1.getX() <= center2.getX()) {
            if(rectangleContainsPoint(corners2, corners1.getRight()) || rectangleContainsPoint(corners1, corners2.getLeft())) {
                return true;
            }
        } else {
            if(rectangleContainsPoint(corners2, corners1.getLeft()) || rectangleContainsPoint(corners1, corners2.getRight())) {
                return true;
            }
        }
        if(center1.getY() <= center2.getY()) {
            if(rectangleContainsPoint(corners2, corners1.getDown()) || rectangleContainsPoint(corners1, corners2.getUp())) {
                return true;
            }
        } else {
            if(rectangleContainsPoint(corners2, corners1.getUp()) || rectangleContainsPoint(corners1, corners2.getDown())) {
                return true;
            }
        }
        return false;
    }
    
    // Use vector math to check if given rectangle contains point m
    public boolean rectangleContainsPoint(Corners rectCorners, Point m) {
        Point am = new Point(m.getX() - rectCorners.getLeft().getX(), m.getY() - rectCorners.getLeft().getY());
        Point ab = new Point(rectCorners.getUp().getX() - rectCorners.getLeft().getX(), rectCorners.getUp().getY() - rectCorners.getLeft().getY());
        Point ad = new Point(rectCorners.getDown().getX() - rectCorners.getLeft().getX(), rectCorners.getDown().getY() - rectCorners.getLeft().getY());

        double dotAmAb = dot(am, ab);
        if(0 <= dotAmAb && dotAmAb <= dot(ab, ab)) {
            double dotAmAd = dot(am, ad);
            if(0 <= dotAmAd && dotAmAd <= dot(ad, ad)) {
                return true;
            }
        }
        return false;
    }
    
    public double dot(Point a, Point b) {
        return a.getX() * b.getX() + a.getY() * b.getY();
    }
    
    public double getDistance(Point a, Point b) {
        return Math.sqrt(Math.pow(b.getY() - a.getY(), 2) + Math.pow(b.getX() - a.getX(), 2));
    }
    
    public Point getMidpoint(Point a, Point b) {
        return new Point((a.getX() + b.getX()) / 2, (a.getY() + b.getY()) / 2);
    }
    
    public int getGridX(double x) {
        return (int)(x / cellWidth);
    }
    
    public int getGridY(double y) {
        return (int)(y / cellWidth);
    }
    
    public int getGridIdentity(Point point) {
        return getGridIdentity(point.getX(), point.getY());
    }
    
    public int getGridIdentity(double x, double y) {
        int value = (getGridY(y) * gridWidth) + getGridX(x);
        
        if(value < 0) {
            return 0;
        } else if (value > (gridWidth * gridHeight) - 1) {
            return (gridWidth * gridHeight) - 1;
        }
        
        return value;
    }
    
    // Build edges for a grid-shaped graph of size width and height
    // Assumes graph was initialized with size (width * height)
    public void gridify(Graph graph, int width, int height) {
        for(int i = 0; i < width * height; i++) {
            // Verticals
            if(i < (height - 1) * width) {
                graph.addEdge(graph.getVertex(i), graph.getVertex(i + width));
                //Diagonals
                if((i + 1) % width != 0) {
                    graph.addEdge(graph.getVertex(i), graph.getVertex(i + width + 1), Math.sqrt(2));
                }
                if(i % width != 0) {
                    graph.addEdge(graph.getVertex(i), graph.getVertex(i + width - 1), Math.sqrt(2));
                }
            }
            // Horizontals
            if((i + 1) % width != 0) {
                graph.addEdge(graph.getVertex(i), graph.getVertex(i + 1));
            }
        }
    }
}
