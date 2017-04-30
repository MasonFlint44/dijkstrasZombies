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
    private double mouseX;
    private double mouseY;
   
    private boolean left = false;
    private boolean right = false;
    private boolean up = false;
    private boolean down = false;
    
    private boolean click = false;
    private int clickCount = 0;
    
    private final double playerWidth = 10;
    private final double playerHeight = 10;
    private final Color playerColor = Color.RED;
    
    private final double bulletWidth = 5;
    private final double bulletHeight = 5;
    private final Color bulletColor = Color.BLACK;
    private final double offset = (playerWidth / 2) + (bulletWidth / 2);
    private final double centerWidth = (playerWidth / 2) - (bulletWidth / 2);
    private final double centerHeight = (playerHeight / 2) - (bulletHeight / 2);
    
    private final Timer movementTimer = new Timer("MovementTimer");
    private final Timer shootTimer = new Timer("ShootTimer");
    
    private Rectangle playerRect;
    private final Character player = new Character();
    private final Hashtable<Bullet, Rectangle> bullets = new Hashtable<>();
    private Point playerCenter;
    private Vertex playerVertex;
    
    private final int gridWidth = 100;
    private final int gridHeight = 100;
    private double cellWidth;
    private double cellHeight;
    private final Graph graph;
    
    private int zombieSpawnCounter = 0;
    private int zombieSpawnRate = 60;
    private int zombieCount = 0;
    private int zombieMaxCount = 2;
    
    private final ArrayList<Character> zombieList = new ArrayList<>();
    private final ArrayList<Rectangle> zombieRectangleList = new ArrayList<>();
    
    private final ArrayList<Rectangle> dPath = new ArrayList<>();
    
    
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private GridPane gridPane;
    
    public ZombiesFXMLController() {
        player.setHealth(100);
        player.setSpeed(1.5);
        player.wield(new Weapon(1, 5));
        
        graph = new Graph(gridWidth * gridHeight);
        gridify(graph, gridWidth, gridHeight);
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
//        // Example visualization of dijkstra's algorithm
//        for(Vertex vertex : graph.dijkstras(graph.getVertex(568), graph.getVertex(7))) {
//            int index = vertex.getIdentity();
//            Rectangle rect = new Rectangle(5, 5, Color.RED);
//            GridPane.setColumnIndex(rect, index % gridWidth);
//            GridPane.setRowIndex(rect, index / gridWidth);
//            gridPane.getChildren().add(rect);
//        }

        cellWidth = gridPane.getPrefWidth() / gridWidth;
        cellHeight = gridPane.getPrefHeight() / gridHeight;
        
        player.setX((anchorPane.getPrefWidth() / 2) - (playerWidth / 2));
        player.setY((anchorPane.getPrefHeight() / 2) - (playerWidth / 2));
        
        playerRect = new Rectangle(player.getX(), player.getY(), playerWidth, playerHeight);
        playerRect.setFill(playerColor);
        
        // Forward mouse events from gridPane to anchorPane
//        gridPane.addEventHandler(MouseEvent.ANY, (mouseEvent) -> {
//            anchorPane.fireEvent(mouseEvent.copyFor(anchorPane, anchorPane));
//            mouseEvent.consume();
//        });
        
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
                        
                        anchorPane.getChildren().add(bulletRect);
                    });
                }
            }
        };
        shootTimer.scheduleAtFixedRate(shootTask, 0, 300);
        
        // Move player
        TimerTask movementTask = new TimerTask() {
            @Override
            public void run() {
                // Calculate degrees of rotation
                player.setTheta(Math.atan2(mouseY - (player.getY() + (playerHeight / 2)), mouseX - (player.getX() + (playerWidth / 2))));
                
                Platform.runLater(() -> {
                    double xMovement = 0;
                    double yMovement = 0;
                    

                    // Get center of player
                    playerCenter = new Point(player.getX() + (playerWidth / 2), player.getY() + (playerHeight / 2));
                    // Used for zombies to track player
                    playerVertex = graph.getVertex(getGridIdentity(playerCenter.getX(), playerCenter.getY()));
                   
                    if (zombieSpawnCounter++  == zombieSpawnRate && zombieCount != zombieMaxCount) {
                        zombieSpawnCounter = 0;
                        Character zombieInstance = new Character();
                        zombieInstance.setHealth(1000);
                        zombieInstance.setSpeed(1.0);
                        zombieInstance.setX( Math.random() * (anchorPane.getPrefWidth() - playerWidth)  );
                        zombieInstance.setY( Math.random() * (anchorPane.getPrefHeight() - playerHeight)  );
                     //   zombieInstance.setTheta( (Math.random() *  Math.PI ));
                        Rectangle zombieRectangle = new Rectangle(zombieInstance.getX(), zombieInstance.getY(), playerWidth, playerHeight);
                        zombieRectangle.setFill(Color.GREEN);
                        zombieCount += 1;
                       // zombieRectangle.setRotate(getDegrees(zombieInstance.getTheta()));
                        zombieList.add(zombieInstance);
                        zombieRectangleList.add(zombieRectangle);
                        anchorPane.getChildren().add(zombieRectangle);

                    }
                

                    
                    
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
                    
                    // If out of bounds, move back in bounds
                    Bounds rectBounds = playerRect.getBoundsInParent();
                    
//                    if(rectBounds.getMinX() + xMovement < 0) {
//                        player.translate(0 - (rectBounds.getMinX() + xMovement), 0);
//                    } else if(rectBounds.getMaxX() + xMovement > anchorPane.getWidth()) {
//                        player.translate(anchorPane.getWidth() - (rectBounds.getMaxX() + xMovement), 0);
//                    }
//                    if(rectBounds.getMinY() + yMovement < 0) {
//                        player.translate(0, 0 - (rectBounds.getMinY() + yMovement));
//                    } else if(rectBounds.getMaxY() + yMovement > anchorPane.getHeight()) {
//                        player.translate(0, anchorPane.getHeight() - (rectBounds.getMaxY() + yMovement));
//                    }
                    
                    // Get y coordinates of corners
                    Double rightY = Math.sqrt(Math.pow(playerWidth / 2, 2) + Math.pow(playerHeight / 2, 2) - Math.pow(playerCenter.getX() - rectBounds.getMinX(), 2));
                    Double leftY = playerCenter.getY() - rightY;
                    rightY += playerCenter.getY();
                    if(rightY.isNaN()) {
                        leftY = playerCenter.getY();
                        rightY = playerCenter.getY();
                    }
                    
                    // Get x coordinates of corners
                    Double upX = Math.sqrt(Math.pow(playerWidth / 2, 2) + Math.pow(playerHeight / 2, 2) - Math.pow(playerCenter.getY() - rectBounds.getMinY(), 2));
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
                    Point left = new Point(rectBounds.getMinX(), leftY);
                    Point right = new Point(rectBounds.getMaxX(), rightY);
                    Point up = new Point(upX, rectBounds.getMinY());
                    Point down = new Point(downX, rectBounds.getMaxY());
                    
                    // Create list of points to track for leftward movement
                    ArrayList<Point> leftTrackPoints = new ArrayList<>();
                    leftTrackPoints.add(left);
                    
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
                    for(Point point: leftTrackPoints) {
                        Vertex vertex = graph.getVertex(getGridIdentity(point));
                        if(point == left && !vertex.containsNeighbor(vertex.getIdentity() + 1)) {
                            vertex = graph.getVertex(vertex.getIdentity() + 1);
                        }
                        if(!vertex.containsNeighbor(vertex.getIdentity() - 1)) {
                            double leftEdge = (vertex.getIdentity() % gridWidth) * cellWidth;
                            if(point.getX() + xMovement < leftEdge) {
                                double translation = leftEdge - (point.getX() + xMovement);
                                player.translate(translation, 0);
                                xMovement += translation;
                            }
                        }
                    }
                    
                    // Create list of points to track for rightward movement
                    ArrayList<Point> rightTrackPoints = new ArrayList<>();
                    rightTrackPoints.add(right);
                    
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
                    for(Point point: rightTrackPoints) {
                        Vertex vertex = graph.getVertex(getGridIdentity(point));
                        if(point == right && !vertex.containsNeighbor(vertex.getIdentity() - 1)) {
                            vertex = graph.getVertex(vertex.getIdentity() - 1);
                        }
                        if(!vertex.containsNeighbor(vertex.getIdentity() + 1)) {
                            double rightEdge = ((vertex.getIdentity() % gridWidth) + 1) * cellWidth;
                            if(point.getX() + xMovement > rightEdge) {
                                double translation = rightEdge - (point.getX() + xMovement);
                                player.translate(translation, 0);
                                xMovement += translation;
                            }
                        }
                    }
                    
                    // Create list of points to track for upward movement
                    ArrayList<Point> upTrackPoints = new ArrayList<>();
                    upTrackPoints.add(up);
                    
                    slope = (left.getY() - up.getY()) / (left.getX() - up.getX());
                    for(int upIndex = getGridY(left.getY()); upIndex > getGridY(up.getY()); upIndex--) {
                        double y = upIndex * cellHeight;
                        double x = slope * (y - up.getY()) + up.getX();
                        upTrackPoints.add(new Point(x, y));
                    }
                    slope = (right.getY() - up.getY()) / (right.getX() - up.getX());
                    for(int upIndex = getGridY(right.getY()); upIndex > getGridY(up.getY()); upIndex--) {
                        double y = upIndex * cellHeight;
                        double x = slope * (y - up.getY()) + up.getX();
                        upTrackPoints.add(new Point(x, y));
                    }
                    for(Point point: upTrackPoints) {
                        Vertex vertex = graph.getVertex(getGridIdentity(point));
                        if(point == up && !vertex.containsNeighbor(vertex.getIdentity() + gridWidth)) {
                            vertex = graph.getVertex(vertex.getIdentity() + gridWidth);
                        }
                        if(!vertex.containsNeighbor(vertex.getIdentity() - gridWidth)) {
                            double upEdge = Math.floor((vertex.getIdentity() / gridWidth)) * cellHeight;
                            if(point.getY() + yMovement < upEdge) {
                                double translation = upEdge - (point.getY() + yMovement);
                                player.translate(0, translation);
                                yMovement += translation;
                            }
                        }
                    }
                    
                    // Create list of points to track for downward movement
                    ArrayList<Point> downTrackPoints = new ArrayList<>();
                    downTrackPoints.add(down);
                    
                    slope = (left.getY() - down.getY()) / (left.getX() - down.getX());
                    for(int downIndex = getGridY(left.getY()) + 1; downIndex < getGridY(down.getY()); downIndex++) {
                        double y = downIndex * cellHeight;
                        double x = slope * (y - down.getY()) + down.getX();
                        downTrackPoints.add(new Point(x, y));
                    }
                    slope = (right.getY() - down.getY()) / (right.getX() - down.getX());
                    for(int downIndex = getGridY(right.getY()) + 1; downIndex < getGridY(down.getY()); downIndex++) {
                        double y = downIndex * cellHeight;
                        double x = slope * (y - down.getY()) + down.getX();
                        downTrackPoints.add(new Point(x, y));
                    }
                    for(Point point: downTrackPoints) {
                        Vertex vertex = graph.getVertex(getGridIdentity(point));
                        if(point == down && !vertex.containsNeighbor(vertex.getIdentity() - gridWidth)) {
                            vertex = graph.getVertex(vertex.getIdentity() - gridWidth);
                        }
                        if(!vertex.containsNeighbor(vertex.getIdentity() + gridWidth)) {
                            double downEdge = (Math.floor(vertex.getIdentity() / gridWidth) + 1) * cellHeight;
                            if(point.getY() + yMovement > downEdge) {
                                double translation = downEdge - (point.getY() + yMovement);
                                player.translate(0, translation);
                                yMovement += translation;
                            }
                        }
                    }

                    // Rotate rectangle 
                    playerRect.setRotate(getDegrees(player.getTheta()));

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
                        }
                        
                        for(Rectangle rect : dPath) {
                            gridPane.getChildren().remove(rect);
                        }
                        dPath.clear();
                     
                        for (Character eachZombie : zombieList) {
                    
                            
                                ArrayList<Vertex> zombiePath = graph.dijkstras(getGridIdentity(eachZombie.getX() + playerWidth / 2, eachZombie.getY() + playerHeight / 2), playerVertex.getIdentity());
                                
                                if(zombiePath.size() > 1){
                                
                                    double yDifference = (int)(zombiePath.get(1).getIdentity() / gridWidth) * cellHeight  -  eachZombie.getY();
                                    double xDifference =  zombiePath.get(1).getIdentity() % gridWidth * cellWidth - eachZombie.getX();
                                    
                                    
                                    if ( Double.isInfinite(xDifference) || Double.isNaN(xDifference))
                                            {
                                                System.out.println("this is x " + xDifference);
                                            
                                            }
                                    
                                    if(Double.isInfinite(yDifference) || Double.isNaN(yDifference))
                                    {
                                                            System.out.println("this is y " + yDifference);   
                                    }
                                    
                                //     eachZombie.translate(zombiePath.get(1).getIdentity() % gridWidth * cellWidth - eachZombie.getX(), (int)(zombiePath.get(1).getIdentity() / gridWidth) * cellHeight - eachZombie.getY());
                                 
                                eachZombie.setTheta(Math.atan2( yDifference ,  xDifference));
                            //    System.out.println("theta " + eachZombie.getTheta());
                                
                                double yTranslate = eachZombie.getSpeed() * Math.sin(eachZombie.getTheta());
                                double xTranslate = eachZombie.getSpeed() * Math.cos(eachZombie.getTheta());
                                        
                                eachZombie.translate( xTranslate,yTranslate);
                                
                                             if ( Double.isInfinite(yTranslate) || Double.isNaN(yTranslate) || yTranslate == 0.0)
                                            {
                                                System.out.println("this is x " + yTranslate);
                                            
                                            }
                                    
                                    if(Double.isInfinite(xTranslate) || Double.isNaN(xTranslate) || xTranslate == 0.0)
                                    {
                                                            System.out.println("this is y " + xTranslate);   
                                    }
                                
                                
                                
                                for(Vertex vertex : zombiePath) {
                                    int index = vertex.getIdentity();
                                    
                                    Rectangle rect = new Rectangle(5, 5, Color.BLUE);
                                    dPath.add(rect);
                                    GridPane.setColumnIndex(rect, index % gridWidth);
                                    GridPane.setRowIndex(rect, index / gridWidth);
                                    gridPane.getChildren().add(rect);
                                }
                                  
                                  Platform.runLater(() -> {
                                   Rectangle zombieMove = zombieRectangleList.get(zombieList.indexOf(eachZombie));
                                   zombieMove.setX(eachZombie.getX());
                                   zombieMove.setY(eachZombie.getY());
                                   zombieMove.setRotate(getDegrees(eachZombie.getTheta())); 
                                  });

                   
                        }
                        
                        }
                        
                        
                    }
                });
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
        shootTimer.cancel();
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

