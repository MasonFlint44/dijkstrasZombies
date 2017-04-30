/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 *
 * @author mason
 */
public class Vertex {
    // Edges represented by adjacency list
    private final ArrayList<Vertex> neighbors = new ArrayList<>();
    // Distances to each neighboring vertex stored in hashtable
    private final Hashtable<Vertex, Double> distances = new Hashtable<>();
    // Each vertex in a graph will have a unique identifier
    private final Integer identifier;
    protected DijkstraInfo dijkstraInfo = new DijkstraInfo();
    
    public Vertex(int identifier) {
        this.identifier = identifier;
    }
    
    // Add edge to both vertices to build undirected graph
    public void addNeighbor(Vertex vertex, double distance) {
        if(!neighbors.contains(vertex)) {
            neighbors.add(vertex);
            distances.put(vertex, distance);
        }
    }
    
    public void addNeighbor(int identifier) {
        addNeighbor(new Vertex(identifier));
    }
    
    public void addNeighbor(int identifier, double distance) {
        addNeighbor(new Vertex(identifier), distance);
    }
    
    public void addNeighbor(Vertex vertex) {
        addNeighbor(vertex, 1);
    }
    
    // Remove edge from both vertices for undirected graph
    public void removeNeighbor(Vertex vertex) {
        if(neighbors.contains(vertex)) {
            distances.remove(vertex);
            neighbors.remove(vertex);
        }
    }
    
    public void removeNeighbor(int identifier) {
        removeNeighbor(new Vertex(identifier));
    }
    
    public Vertex getNeighbor(int identifier) {
        int index = neighbors.indexOf(new Vertex(identifier));
        if(index >= 0) {
            return neighbors.get(index);
        }
        return null;
    }
    
    public int getNeighborCount() {
        return neighbors.size();
    }
    
    public boolean containsNeighbor(Vertex vertex) {
        return neighbors.contains(vertex);
    }
    
    public boolean containsNeighbor(int identity) {
        return containsNeighbor(new Vertex(identity));
    }
    
    public int getIdentity() {
        return identifier;
    }
    
    // Get distance to neighboring vertex
    public double getDistance(Vertex vertex) {
        // Returns null if vertex is not a neighbor
        return distances.get(vertex);
    }
    
    public double getDistance(int identifier) {
        return getDistance(new Vertex(identifier));
    }
    
    public ArrayList<Vertex> getNeighbors() {
        return neighbors;
    }
    
    public Hashtable<Vertex, Double> getDistances() {
        return distances;
    }
    
    @Override
    public String toString() {
        return "Vertex: " + identifier;
    }
    
    @Override
    public boolean equals(Object object) {
        if(object instanceof Vertex && ((Vertex)object).getIdentity() == this.identifier) {
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return identifier.hashCode();
    }
    
    public class DijkstraInfo {
        public boolean settled;
        public double distance = 0;
        public Vertex previous;
    }  
}
