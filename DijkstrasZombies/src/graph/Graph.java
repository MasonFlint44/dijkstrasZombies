/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author mason
 */
public class Graph {
    private ArrayList<Vertex> vertices = new ArrayList<>();
    
    public Graph(int count) {
        for(int i = 0; i < count; i++) {
            vertices.add(new Vertex(i));
        }
    }
    
    public Graph() {
        // Do nothing
    }
    
    public Vertex addVertex(int identifier) {
        Vertex vertex = new Vertex(identifier);
        addVertex(vertex);
        return vertex;
    }
    
    public void addVertex(Vertex vertex) {
        if(vertex != null && !vertices.contains(vertex)) {
            vertices.add(vertex);
        }
    }
    
    public boolean containsVertex(int identifier) {
        return containsVertex(new Vertex(identifier));
    }
    
    public boolean containsVertex(Vertex vertex) {
        return vertices.contains(vertex);
    }
    
    public void removeVertex(int identifier) {
        removeVertex(new Vertex(identifier));
    }
    
    public void removeVertex(Vertex vertex) {
        disconnect(vertex.getIdentity());
        vertices.remove(vertex);
    }
    
    // Removes all neighbors from a vertex without removing vertex from graph
    public void disconnect(int identifier) {
        Vertex vertex = getVertex(identifier);
        if(vertex != null) {
            for(Vertex neighbor : vertex.getNeighbors()) {
                neighbor.removeNeighbor(vertex);
            }
            vertex.getNeighbors().clear();
            vertex.getDistances().clear();
        }
    }
    
    public void disconnect(Vertex vertex) {
        disconnect(vertex.getIdentity());
    }
    
    public int getVertexCount() {
        return vertices.size();
    }
    
    public void addEdge(Vertex x, Vertex y, double distance) {
        if(!vertices.contains(x)) {
            addVertex(x);
        }
        if(!vertices.contains(y)) {
            addVertex(y);
        }
        x.addNeighbor(y, distance);
        y.addNeighbor(x, distance);
    }
    
    public void addEdge(Vertex x, Vertex y) {
        addEdge(x, y, 1);
    }
    
    public void addEdge(int x, int y, double distance) {
        Vertex xVertex = getVertex(x);
        Vertex yVertex = getVertex(y);
        if(xVertex == null) {
            xVertex = addVertex(x);
        }
        if(yVertex == null) {
            yVertex = addVertex(y);
        }
        addEdge(xVertex, yVertex, distance);
    }
    
    public void addEdge(int x, int y) {
        addEdge(x, y, 1);
    }
    
    public boolean containsEdge(Vertex x, Vertex y) {
        if(x.containsNeighbor(y) && y.containsNeighbor(x)) {
            return true;
        }
        return false;
    }
    
    public boolean containsEdge(int x, int y) {
        Vertex xVertex = getVertex(x);
        Vertex yVertex = getVertex(y);
        if(xVertex == null || yVertex == null) {
            return false;
        }
        return containsEdge(xVertex, yVertex);
    }
    
    public void removeEdge(Vertex x, Vertex y) {
        if(x != null) {
            x.removeNeighbor(y);
        }
        if(y != null) {
            y.removeNeighbor(x);
        }
    }
    
    public void removeEdge(int x, int y) {
        Vertex xVertex = getVertex(x);
        Vertex yVertex = getVertex(y);
        removeEdge(xVertex, yVertex);
    }
    
    public Vertex getVertex(int identifier) {
        int index = vertices.indexOf(new Vertex(identifier));
        if(index >= 0) {
            return vertices.get(index);
        }
        return null;
    }
    
    public ArrayList<Vertex> getVertices() {
        return vertices;
    }
    
//    public ArrayList<Vertex> dijkstras(int start, int stop) {
//        return dijkstras(getVertex(start), getVertex(stop));
//    }
    
    public HashMap<Vertex, Double> dijkstras(int start) {
        return dijkstras(getVertex(start));
    }
    
    
    public HashMap<Vertex, Double> dijkstras(Vertex start) {
        
        if(!vertices.contains(start)) {
            return null;
        }
        
        HashMap<Vertex, Double> distances = new HashMap<>();
        boolean settled[] = new boolean[getVertexCount()];
        ArrayList<Vertex> unsettled = new ArrayList<>();
        
        distances.put(start, 0.0);
        unsettled.add(start);
        
        while(!unsettled.isEmpty()) {
            Vertex vertex = getMinDistance(unsettled, distances);
            settled[vertex.getIdentity()] = true;

            unsettled.remove(vertex);
            
            for(Vertex neighbor : vertex.getNeighbors()) {
                double alt = distances.getOrDefault(vertex, Double.POSITIVE_INFINITY) + neighbor.getDistance(vertex);

                if(settled[neighbor.getIdentity()] == false && alt < distances.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
                    distances.put(neighbor, alt);
                    unsettled.add(neighbor);
                }
            }
        }

        return distances;
    }
    
    // Return closest neighboring vertex
    public Vertex getMinDistance(ArrayList<Vertex> neighbors, HashMap<Vertex, Double> distances) {
        Vertex min = neighbors.get(0);
        for(Vertex vertex : neighbors) {
            if(distances.getOrDefault(vertex, Double.POSITIVE_INFINITY) < distances.getOrDefault(min, Double.POSITIVE_INFINITY)) {
                min = vertex;
            }
        }
        return min;
    }
}
