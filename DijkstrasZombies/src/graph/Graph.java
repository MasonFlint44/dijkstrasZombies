/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.util.ArrayList;

/**
 *
 * @author mason
 */
public class Graph {
    private ArrayList<Vertex> verticies = new ArrayList<>();
    
    public Graph(int count) {
        for(int i = 0; i < count; i++) {
            verticies.add(new Vertex(i));
        }
    }
    
    public Graph() {
        // Do nothing
    }
    
    public void addVertex(int identifier) {
        Vertex vertex = new Vertex(identifier);
        if(!verticies.contains(vertex)) {
            verticies.add(vertex);
        }
    }
    
    public boolean containsVertex(int identifier) {
        return verticies.contains(new Vertex(identifier));
    }
    
    public void removeVertex(int identifier) {
        Vertex vertex = new Vertex(identifier);
        if(verticies.contains(vertex)) {
            for(Vertex neighbor : vertex.getNeighbors()) {
                neighbor.removeNeighbor(vertex);
            }
            verticies.remove(vertex);
        }
    }
    
    public int getVertexCount() {
        return verticies.size();
    }
    
    public void addEdge(Vertex x, Vertex y, double distance) {
        if(verticies.contains(x) && verticies.contains(y)) {
            x.addNeighbor(y, distance);
            y.addNeighbor(x, distance);
        }
    }
    
    public void addEdge(Vertex x, Vertex y) {
        addEdge(x, y, 1);
    }
    
    public boolean containsEdge(Vertex x, Vertex y) {
        if(x.containsNeighbor(y) && y.containsNeighbor(x)) {
            return true;
        }
        return false;
    }
    
    public void removeEdge(Vertex x, Vertex y) {
        if(x.containsNeighbor(y) && y.containsNeighbor(x)) {
            x.removeNeighbor(y);
            y.removeNeighbor(x);
        }
    }
    
    public Vertex getVertex(int identifier) {
        int index = verticies.indexOf(new Vertex(identifier));
        if(index >= 0) {
            return verticies.get(index);
        }
        return null;
    }
    
    public ArrayList<Vertex> getVerticies() {
        return verticies;
    }
    
    // Helper method for Dijkstra's
    private void reset() {
        for(Vertex vertex : verticies) {
            vertex.dijkstraInfo.distance = Double.POSITIVE_INFINITY;
            vertex.dijkstraInfo.settled = false;
        }
    }
    
    // This is Dijkstra's algorithm modified to return a path of verticies from the start node to the stop node
    public ArrayList<Vertex> dijkstras(Vertex start, Vertex stop) {
        if(!verticies.contains(start) || !verticies.contains(stop)) {
            return null;
        }
        
        ArrayList<Vertex> unsettled = new ArrayList<>();
        reset();
        
        start.dijkstraInfo.distance = 0;
        start.dijkstraInfo.previous = null;
        unsettled.add(start);
        
        while(!unsettled.isEmpty()) {
            Vertex vertex = getMin(unsettled);
            vertex.dijkstraInfo.settled = true;
            if(vertex.equals(stop)) {
                break;
            }
            unsettled.remove(vertex);
            
            for(Vertex neighbor : vertex.getNeighbors()) {
                double alt = vertex.dijkstraInfo.distance + neighbor.getDistance(vertex);
                if(neighbor.dijkstraInfo.settled == false && alt < neighbor.dijkstraInfo.distance) {
                    neighbor.dijkstraInfo.distance = alt;
                    neighbor.dijkstraInfo.previous = vertex;
                    unsettled.add(neighbor);
                }
            }
        }
        
        ArrayList<Vertex> path = new ArrayList<>();
        Vertex prev = stop;
        while(prev != null) {
            path.add(0, prev);
            prev = prev.dijkstraInfo.previous;
        }
        return path;
    }
    
    public Vertex getMin(ArrayList<Vertex> verticies) {
        Vertex min = verticies.get(0);
        for(Vertex vertex : verticies) {
            if(vertex.dijkstraInfo.distance < min.dijkstraInfo.distance) {
                min = vertex;
            }
        }
        return min;
    }
}
