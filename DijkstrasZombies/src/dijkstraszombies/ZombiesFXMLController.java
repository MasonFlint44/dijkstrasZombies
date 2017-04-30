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
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author mason
 */
public class ZombiesFXMLController implements Initializable {
    // Mouse coordinates
    private double mouseX;
    private double mouseY;
   
    // Key pressed values
    private boolean left = false;
    private boolean right = false;
    private boolean up = false;
    private boolean down = false;
    
    // Click and number of clicks
    private boolean click = false;
    private int clickCount = 0;
    
    // Player values
    private final double playerWidth = 10;
    private final double playerHeight = 10;
    private final Color playerColor = Color.RED;
    
    // Bullet values
    private final double bulletWidth = 5;
    private final double bulletHeight = 5;
    private final Color bulletColor = Color.BLACK;
    private final double offset = (playerWidth / 2) + (bulletWidth / 2);
    private final double centerWidth = (playerWidth / 2) - (bulletWidth / 2);
    private final double centerHeight = (playerHeight / 2) - (bulletHeight / 2);
    
    // Movement values
    private final Timer movementTimer = new Timer("MovementTimer");
    private int fireCounter = 0;
    
    // Player rectangle values
    private Rectangle playerRect;
    private final Character player = new Character();
    private final Hashtable<Bullet, Rectangle> bullets = new Hashtable<>();
    private Point playerCenter;
    private Vertex playerVertex;
    
    // Grid values
    private final int gridWidth = 100;
    private final int gridHeight = 100;
    private double cellWidth;
    private double cellHeight;
    private final Graph graph;
    
    // Zombie values
    private int zombieSpawnCounter = 0;
    private final int zombieSpawnRate = 180;
    private int zombieCount = 0;
    private final int zombieMaxCount = 20;
    
    private final double zombieSpeed = .5;
    private final int zombieHealth = 50;
    
    private final ArrayList<Character> zombieList = new ArrayList<>();
    private final ArrayList<Rectangle> zombieRectangleList = new ArrayList<>();
    
    private final Timer dijkstraTimer = new Timer("DijkstraTimer");
    private final Hashtable<Character, ArrayList<Vertex>> dijkstraPaths = new Hashtable<>();
    
//    // Draw dijkstra path on screen
//    private final ArrayList<Rectangle> dijkstraPath = new ArrayList<>();
    
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private GridPane gridPane;
    
    public ZombiesFXMLController() {
        player.setHealth(100);
        player.setSpeed(1.5);
        player.wield(new Weapon(1, 5, 20));
        
        graph = new Graph(gridWidth * gridHeight);
        gridify(graph, gridWidth, gridHeight);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cellWidth = gridPane.getPrefWidth() / gridWidth;
        cellHeight = gridPane.getPrefHeight() / gridHeight;
        
        // Top wall
        for(int i  = 1616; i <= 1684; i++) {
            graph.disconnect(i);
            
            Rectangle rect = new Rectangle(cellWidth, cellHeight, Color.BLACK);
            GridPane.setColumnIndex(rect, i % gridWidth);
            GridPane.setRowIndex(rect, i / gridWidth);
            gridPane.getChildren().add(rect);
            
            if(i == 1646) {
                i += 10;
            }
        }
        
        // Bottom wall
        for(int i  = 8416; i <= 8484; i++) {
            graph.disconnect(i);
            
            Rectangle rect = new Rectangle(cellWidth, cellHeight, Color.BLACK);
            GridPane.setColumnIndex(rect, i % gridWidth);
            GridPane.setRowIndex(rect, i / gridWidth);
            gridPane.getChildren().add(rect);
            
            if(i == 8446) {
                i += 10;
            }
        }
        
        // Left wall
        for(int i = 1616; i <= 8416; i += gridWidth) {
            graph.disconnect(i);
            
            Rectangle rect = new Rectangle(cellWidth, cellHeight, Color.BLACK);
            GridPane.setColumnIndex(rect, i % gridWidth);
            GridPane.setRowIndex(rect, i / gridWidth);
            gridPane.getChildren().add(rect);
            
            if(i == 4616) {
                i += 10 * gridWidth;
            }
        }
        
        // Right wall
        for(int i = 1684; i <= 8484; i += gridWidth) {
            graph.disconnect(i);
            
            Rectangle rect = new Rectangle(cellWidth, cellHeight, Color.BLACK);
            GridPane.setColumnIndex(rect, i % gridWidth);
            GridPane.setRowIndex(rect, i / gridWidth);
            gridPane.getChildren().add(rect);
            
            if(i == 4684) {
                i += 10 * gridWidth;
            }
        }
        
        player.setX((anchorPane.getPrefWidth() / 2) - (playerWidth / 2));
        player.setY((anchorPane.getPrefHeight() / 2) - (playerWidth / 2));
        
        playerRect = new Rectangle(player.getX(), player.getY(), playerWidth, playerHeight);
        playerRect.setFill(playerColor);
        
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
        
        // Dijkstra timer
        TimerTask dijkstraTask = new TimerTask() {
            private boolean ready = true;
            
            @Override
            public void run() {
                if(ready) {
                    ready = false;
                    
                    for(int i = 0; i < zombieList.size(); i++) {
                        Character zombie = zombieList.get(i);
                        ArrayList<Vertex> zombiePath = graph.dijkstras(getGridIdentity(zombie.getX() + playerWidth / 2, zombie.getY() + playerHeight / 2), playerVertex.getIdentity());
                        dijkstraPaths.put(zombie, zombiePath);
                    }
                    
                    ready = true;
                }
            }
        };
        dijkstraTimer.scheduleAtFixedRate(dijkstraTask, 0, 15);
        
        // Move player
        TimerTask movementTask = new TimerTask() {
            @Override
            public void run() {
                // Calculate degrees of rotation
                player.setTheta(Math.atan2(mouseY - (player.getY() + (playerHeight / 2)), mouseX - (player.getX() + (playerWidth / 2))));
                
                // Fire bullets
                if(fireCounter++ >= player.getWeapon().getFireRate()) {
                    fireCounter = 0;

                    if(click || clickCount > 0) {
                        clickCount = 0;

                        Bullet bullet = player.fireWeapon();
                        bullet.setX((player.getX() + centerWidth) + (offset * Math.cos(player.getTheta())));
                        bullet.setY((player.getY() + centerHeight) + (offset * Math.sin(player.getTheta())));

                        Platform.runLater(() -> {
                            Rectangle bulletRect = new Rectangle(bullet.getX(), bullet.getY() , bulletWidth, bulletHeight);
                            bulletRect.setFill(bulletColor);
                            bulletRect.setRotate(getDegrees(bullet.getTheta()));

                            bullets.put(bullet, bulletRect);
                            anchorPane.getChildren().add(bulletRect);
                        });
                    }
                }
                
                // Spawn zombies
                if (zombieSpawnCounter++  >= zombieSpawnRate && zombieCount < zombieMaxCount) {
                    zombieSpawnCounter = 0;
                    zombieCount += 1;

                    Character zombieInstance = new Character();
                    zombieInstance.setHealth(zombieHealth);
                    zombieInstance.setSpeed(zombieSpeed);
                    zombieInstance.setX(Math.random() * (anchorPane.getPrefWidth() - playerWidth));
                    zombieInstance.setY(Math.random() * (anchorPane.getPrefHeight() - playerHeight));

                    Platform.runLater(() -> {
                        Rectangle zombieRectangle = new Rectangle(zombieInstance.getX(), zombieInstance.getY(), playerWidth, playerHeight);
                        zombieRectangle.setFill(Color.GREEN);
                        anchorPane.getChildren().add(zombieRectangle);

                        zombieList.add(zombieInstance);
                        zombieRectangleList.add(zombieRectangle);
                    });
                }
                
                // Get center of player
                playerCenter = new Point(player.getX() + (playerWidth / 2), player.getY() + (playerHeight / 2));
                
                Platform.runLater(() -> {
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
                    
                    // Get bounds of player
                    Bounds playerBounds = playerRect.getBoundsInParent();

                    // Get y coordinates of corners
                    Double rightY = Math.sqrt(Math.pow(playerWidth / 2, 2) + Math.pow(playerHeight / 2, 2) - Math.pow(playerCenter.getX() - playerBounds.getMinX(), 2));
                    Double leftY = playerCenter.getY() - rightY;
                    rightY += playerCenter.getY();
                    if(rightY.isNaN()) {
                        leftY = playerCenter.getY();
                        rightY = playerCenter.getY();
                    }

                    // Get x coordinates of corners
                    Double upX = Math.sqrt(Math.pow(playerWidth / 2, 2) + Math.pow(playerHeight / 2, 2) - Math.pow(playerCenter.getY() - playerBounds.getMinY(), 2));
                    Double downX = playerCenter.getX() - upX;
                    upX += playerCenter.getX();
                    if(upX.isNaN()) {
                        upX = playerCenter.getX();
                        downX = playerCenter.getX();
                    }
                    
                    // Swap values based on rotation
                    double rotation = Math.abs(getDegrees(player.getTheta()));
                    if(player.getTheta() < 0) {
                        if((rotation > 45 && rotation < 90) || (rotation > 135 && rotation < 180)) {
                            // Swap left and right
                            double swap = leftY;
                            leftY = rightY;
                            rightY = swap;

                            // Swap up and down
                            swap = upX;
                            upX = downX;
                            downX = swap;
                        }
                    } else if(player.getTheta() > 0) {
                        if((rotation > 0 && rotation < 45) || (rotation > 90 && rotation < 135)) {
                            // Swap left and right
                            double swap = leftY;
                            leftY = rightY;
                            rightY = swap;

                            // Swap up and down
                            swap = upX;
                            upX = downX;
                            downX = swap;
                        }
                    }
                    
                    // Create points of each corner of rectangle
                    Point left = new Point(playerBounds.getMinX(), leftY);
                    Point right = new Point(playerBounds.getMaxX(), rightY);
                    Point up = new Point(upX, playerBounds.getMinY());
                    Point down = new Point(downX, playerBounds.getMaxY());
                    
                    // Create list of points to track for leftward movement
                    ArrayList<Point> leftTrackPoints = new ArrayList<>();
                    leftTrackPoints.add(left);
                    
                    // Create list of points to track for upward movement
                    ArrayList<Point> upTrackPoints = new ArrayList<>();
                    upTrackPoints.add(up);
                    
                    // Create list of points to track for downward movement
                    ArrayList<Point> downTrackPoints = new ArrayList<>();
                    downTrackPoints.add(down);
                    
                    // Create list of points to track for rightward movement
                    ArrayList<Point> rightTrackPoints = new ArrayList<>();
                    rightTrackPoints.add(right);
                    
                    // Calculate points for leftward movement
                    double slope = (up.getY() - left.getY()) / (up.getX() - left.getX());
                    for(int leftIndex = getGridX(left.getX()) + 1; leftIndex <= getGridX(up.getX()); leftIndex++) {
                        double x = leftIndex * cellWidth;
                        double y = slope * (x - left.getX()) + left.getY();
                        leftTrackPoints.add(new Point(x, y));
                    }
                    slope = (down.getY() - left.getY()) / (down.getX() - left.getX());
                    for(int leftIndex = getGridX(left.getX()) + 1; leftIndex <= getGridX(down.getX()); leftIndex++) {
                        double x = leftIndex * cellWidth;
                        double y = slope * (x - left.getX()) + left.getY();
                        leftTrackPoints.add(new Point(x, y));
                    }
                    
                    // Calculate points for rightward movement
                    slope = (up.getY() - right.getY()) / (up.getX() - right.getX());
                    for(int rightIndex = getGridX(right.getX()) - 1; rightIndex >= getGridX(up.getX()); rightIndex--) {
                        double x = (rightIndex + 1) * cellWidth;
                        double y = slope * (x - right.getX()) + right.getY();
                        rightTrackPoints.add(new Point(x, y));
                    }
                    slope = (down.getY() - right.getY()) / (down.getX() - right.getX());
                    for(int rightIndex = getGridX(right.getX()) - 1; rightIndex >= getGridX(down.getX()); rightIndex--) {
                        double x = (rightIndex + 1) * cellWidth;
                        double y = slope * (x - right.getX()) + right.getY();
                        rightTrackPoints.add(new Point(x, y));
                    }                   
                    
                    // Calculate points for upward movement
                    slope = (left.getY() - up.getY()) / (left.getX() - up.getX());
                    for(int upIndex = getGridY(left.getY()); upIndex > getGridY(up.getY()); upIndex--) {
                        double y = upIndex * cellHeight;
                        double x = (y - up.getY()) / slope + up.getX();
                        upTrackPoints.add(new Point(x, y));
                    }
                    slope = (right.getY() - up.getY()) / (right.getX() - up.getX());
                    for(int upIndex = getGridY(right.getY()); upIndex > getGridY(up.getY()); upIndex--) {
                        double y = upIndex * cellHeight;
                        double x = (y - up.getY()) / slope + up.getX();
                        upTrackPoints.add(new Point(x, y));
                    }
                    
                    // Calculate points for downward movement
                    slope = (left.getY() - down.getY()) / (left.getX() - down.getX());
                    for(int downIndex = getGridY(left.getY()) + 1; downIndex < getGridY(down.getY()); downIndex++) {
                        double y = downIndex * cellHeight;
                        double x = (y - down.getY()) / slope + down.getX();
                        downTrackPoints.add(new Point(x, y));
                    }
                    slope = (right.getY() - down.getY()) / (right.getX() - down.getX());
                    for(int downIndex = getGridY(right.getY()) + 1; downIndex < getGridY(down.getY()); downIndex++) {
                        double y = downIndex * cellHeight;
                        double x = (y - down.getY()) / slope + down.getX();
                        downTrackPoints.add(new Point(x, y));
                    }
                    
                    // Add tracking points to one list
                    ArrayList<Point> edgePoints = new ArrayList<>();
                    edgePoints.addAll(leftTrackPoints);
                    edgePoints.addAll(rightTrackPoints);
                    edgePoints.addAll(upTrackPoints);
                    edgePoints.addAll(downTrackPoints);
                    
                    // Keep player inside bounds of anchorPane
                    if(playerBounds.getMinX() + xMovement < 0) {
                        player.translate(0 - (playerBounds.getMinX() + xMovement), 0);
                    } else if(playerBounds.getMaxX() + xMovement > anchorPane.getWidth()) {
                        player.translate(anchorPane.getWidth() - (playerBounds.getMaxX() + xMovement), 0);
                    }
                    if(playerBounds.getMinY() + yMovement < 0) {
                        player.translate(0, 0 - (playerBounds.getMinY() + yMovement));
                    } else if(playerBounds.getMaxY() + yMovement > anchorPane.getHeight()) {
                        player.translate(0, anchorPane.getHeight() - (playerBounds.getMaxY() + yMovement));
                    }
                    
                    // Check player for wall collisions
                    for(Point point: edgePoints) {
                        Point translatedPoint = new Point(point.getX() + xMovement, point.getY() + yMovement);
                        Vertex vertex = graph.getVertex(getGridIdentity(translatedPoint));
                        if(vertex.getNeighborCount() == 0) {
                            Point vertexCenter = new Point(((vertex.getIdentity() % gridWidth) * cellWidth) + (cellWidth / 2), ((vertex.getIdentity() / gridWidth) * cellHeight) + (cellHeight / 2));
                            double edge;
                            double translation;
                            if(Math.abs(vertexCenter.getX() - playerCenter.getX()) > Math.abs(vertexCenter.getY() - playerCenter.getY())) {
                                if(playerCenter.getX() < vertexCenter.getX()) {
                                    edge = (vertex.getIdentity() % gridWidth) * cellWidth;
                                } else {
                                    edge = ((vertex.getIdentity() % gridWidth) + 1) * cellWidth;
                                }
                                translation = edge - translatedPoint.getX();
                                xMovement += translation;
                                player.translate(translation, 0);
                            } else {
                                if(playerCenter.getY() < vertexCenter.getY()) {
                                    edge = (vertex.getIdentity() / gridWidth) * cellHeight;
                                } else {
                                    edge = ((vertex.getIdentity() / gridWidth) + 1) * cellHeight;
                                }
                                translation = edge - translatedPoint.getY();
                                yMovement += translation;
                                player.translate(0, translation);
                            }
                        }
                    }

                    // Rotate rectangle 
                    playerRect.setRotate(getDegrees(player.getTheta()));

                    // Apply translations
                    playerRect.setX(player.getX());
                    playerRect.setY(player.getY());
                    
                    // Used for zombies to track player
                    playerVertex = graph.getVertex(getGridIdentity(playerCenter.getX() + xMovement, playerCenter.getY() + yMovement));
                    
                    // Move bullets
                    for(Weapon weapon : player.getWeapons()) {
                        for(int i = weapon.getBullets().size() - 1; i >= 0; i--) {
                            Bullet bullet = weapon.getBullets().get(i);
                            bullet.move();
                            
                            try {
                               Rectangle bulletRect = bullets.get(bullet);
                                bulletRect.setX(bullet.getX());
                                bulletRect.setY(bullet.getY());

                                Bounds bulletBounds = bulletRect.getBoundsInParent();
                                if(bulletBounds.getMaxX() < 0) {
                                    anchorPane.getChildren().remove(bulletRect);
                                    bullets.remove(bullet.destroy());
                                    continue;
                                } else if(bulletBounds.getMinX() > anchorPane.getWidth()) {
                                    anchorPane.getChildren().remove(bulletRect);
                                    bullets.remove(bullet.destroy());
                                    continue;
                                }
                                if(bulletBounds.getMaxY() < 0) {
                                    anchorPane.getChildren().remove(bulletRect);
                                    bullets.remove(bullet.destroy());
                                } else if(bulletBounds.getMinY() > anchorPane.getHeight()) {
                                    anchorPane.getChildren().remove(bulletRect);
                                    bullets.remove(bullet.destroy());
                                }
                            } catch(NullPointerException ex) {
                                // Do nothing
                            }
                        }
                    }  
                });
                
//                Platform.runLater(() -> {
//                    // Clear dijkstra path
//                    for(Rectangle rect : dijkstraPath) {
//                        gridPane.getChildren().remove(rect);
//                    }
//                    dijkstraPath.clear();
//                });
                
                for(int i = 0; i < zombieList.size(); i++) {
                    Character zombie = zombieList.get(i);
                    
                    ArrayList<Vertex> zombiePath = dijkstraPaths.get(zombie);
                    if(zombiePath == null) {
                        continue;
                    }

                    if(zombiePath.size() > 1){
                        double yDifference = (((int)(zombiePath.get(1).getIdentity() / gridWidth) * cellHeight) + cellHeight / 2) - (zombie.getY() + (playerHeight / 2));
                        double xDifference = ((zombiePath.get(1).getIdentity() % gridWidth * cellWidth) + cellWidth / 2) - (zombie.getX() + (playerWidth / 2)) ;

                        zombie.setTheta(Math.atan2( yDifference , xDifference));
                        zombie.translate(zombie.getSpeed() * Math.cos(zombie.getTheta()), zombie.getSpeed() * Math.sin(zombie.getTheta()));

//                        // Draw dijkstra path
//                        for(Vertex vertex : zombiePath) {
//                            int index = vertex.getIdentity();
//
//                            Platform.runLater(() -> {
//                                Rectangle rect = new Rectangle(5, 5, Color.BLUE);
//                                dijkstraPath.add(rect);
//                                GridPane.setColumnIndex(rect, index % gridWidth);
//                                GridPane.setRowIndex(rect, index / gridWidth);
//                                gridPane.getChildren().add(rect);
//                            });
//                        }

                        Platform.runLater(() -> {
                            Rectangle zombieRect = zombieRectangleList.get(zombieList.indexOf(zombie));
                            zombieRect.setX(zombie.getX());
                            zombieRect.setY(zombie.getY());
                            zombieRect.setRotate(getDegrees(zombie.getTheta())); 
                        });
                    }
                }
            }
        };
        movementTimer.scheduleAtFixedRate(movementTask, 0, 15);
        
        anchorPane.getChildren().add(playerRect);
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
    
    // Cancel timers - gets called on exit in main method
    public void dispose() {
        movementTimer.cancel();
        dijkstraTimer.cancel();
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
