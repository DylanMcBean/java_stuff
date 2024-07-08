package Sketch.Road;

import processing.core.PApplet;
import processing.core.PVector;
import java.util.ArrayList;

public class Graph {
  PApplet parent;
  ArrayList<Node> nodes = new ArrayList<>();
  ArrayList<Road> roads = new ArrayList<>();
  ArrayList<Vehicle> vehicles = new ArrayList<>();

  Graph(PApplet p) {
    parent = p;
  }

  Node addNode(PVector loc) {
    Node newNode = new Node(parent, loc);
    nodes.add(newNode);
    return newNode;
  }

  void addVehicle(Node node) {
    Vehicle vehicle = new Vehicle(this.parent, node);
    vehicles.add(vehicle);
  }

  void deleteNode(Node node) {
    roads.removeIf(road -> road.start == node || road.end == node);
    nodes.remove(node);
  }

  void addRoad(Node start, Node end, boolean twoWay) {
    Road newRoad = new Road(parent, start, end, twoWay);
    if (start == end) {
        return;
    }
    start.addConnection(newRoad);
    if (twoWay) {
        end.addConnection(newRoad);
    }
    roads.add(newRoad);
}

  void updateRoads(Node node) {
    System.err.println("Before Delete");

    if (node.connections.size() == 0) {
        System.err.println("Nothing to delete");
    } else {
        for (Road road : node.connections) {
            road.update(node);
        }
    }
  }

  Node getNearestNode(float x, float y, float snapDistance) {
    Node nearestNode = null;
    float minDist = snapDistance;
    for (Node node : nodes) {
      float d = PApplet.dist(node.location.x, node.location.y, x, y);
      if (d < minDist) {
        minDist = d;
        nearestNode = node;
      }
    }
    return nearestNode;
  }

  void show(Node hoverNode) {
    for (Road road : roads) {
      road.show();
    }
    for (Node node : nodes) {
      if (node == hoverNode) {
        node.show(true);
      } else {
        node.show(false);
      }
    }
    for (Vehicle vehicle : vehicles) {
        vehicle.update();
        vehicle.show();
    }
  }
}