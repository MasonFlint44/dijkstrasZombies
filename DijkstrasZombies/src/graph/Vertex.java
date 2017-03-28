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
    // Since graph is undirected, edges will be represented by a list of neighbors
    private final ArrayList<Vertex> neighbors = new ArrayList<>();
    // Distances to each neighboring vertex stored in hashtable
    private final Hashtable<Vertex, Double> distances = new Hashtable<>();
    // Each vertex in a graph will have a unique identifier
    private final Integer identifier;
    protected DijkstraInfo dijkstraInfo = new DijkstraInfo();
    
    public Vertex(int identifier) {
        this.identifier = identifier;
    }
    
    // Add edge to both verticies to build undirected graph
    public void addNeighbor(Vertex vertex, double distance) {
        if(!neighbors.contains(vertex)) {
            neighbors.add(vertex);
            distances.put(vertex, distance);
        }
    }
    
    public void addNeighbor(Vertex vertex) {
        addNeighbor(vertex, 1);
    }
    
    // Remove edge from both verticies for undirected graph
    public void removeNeighbor(Vertex vertex) {
        if(neighbors.contains(vertex)) {
            distances.remove(vertex);
            neighbors.remove(vertex);
        }
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
    
    public int getIdentity() {
        return identifier;
    }
    
    // Get distance to neighboring vertex
    public double getDistance(Vertex vertex) {
        // Returns null if vertex is not a neighbor
        return distances.get(vertex);
    }
    
    public ArrayList<Vertex> getNeighbors() {
        return neighbors;
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
        public boolean visited;
        public double distance = 0;
        public Vertex previous;
    }  
}
