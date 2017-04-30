/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

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
    
    public ArrayList<Vertex> dijkstras(int start, int stop) {
        return dijkstras(getVertex(start), getVertex(stop));
    }
    
    // This is Dijkstra's algorithm modified to return a path of vertices from the start node to the stop node
    public ArrayList<Vertex> dijkstras(Vertex start, Vertex stop) {
        if(!vertices.contains(start) || !vertices.contains(stop)) {
            return null;
        }
        
        boolean settled[] = new boolean[getVertexCount()];
        double distance[] = new double[getVertexCount()];
        Arrays.fill(distance, Double.POSITIVE_INFINITY);
        Hashtable<Vertex, Vertex> previous = new Hashtable<>();
        ArrayList<Vertex> unsettled = new ArrayList<>();
        
        distance[start.getIdentity()] = 0;
        unsettled.add(start);
        
        while(!unsettled.isEmpty()) {
            Vertex vertex = getMinDistance(unsettled, distance);
            settled[vertex.getIdentity()] = true;
            if(vertex.equals(stop)) {
                break;
            }
            unsettled.remove(vertex);
            
            for(Vertex neighbor : vertex.getNeighbors()) {
                double alt = distance[vertex.getIdentity()] + neighbor.getDistance(vertex);
                if(settled[neighbor.getIdentity()] == false && alt < distance[neighbor.getIdentity()]) {
                    distance[neighbor.getIdentity()] = alt;
                    previous.put(neighbor, vertex);
                    unsettled.add(neighbor);
                }
            }
        }
        
        ArrayList<Vertex> path = new ArrayList<>();
        Vertex prev = stop;
        while(prev != null) {
            path.add(0, prev);
            prev = previous.get(prev);
        }
        return path;
    }
    
    private Vertex getMinDistance(ArrayList<Vertex> vertices, double[] distance) {
        Vertex min = vertices.get(0);
        for(Vertex vertex : vertices) {
            if(distance[vertex.getIdentity()] < distance[min.getIdentity()]) {
                min = vertex;
            }
        }
        return min;
    }
}
