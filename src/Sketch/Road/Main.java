package Sketch.Road;

import processing.core.PApplet;
import processing.core.PVector;

public class Main extends PApplet {
  public static void main(String[] args) {
    PApplet.main("Sketch.Road.Main");
  }

  Graph graph;
  Node selectedNode = null;
  Node hoverNode = null;
  final float SNAP_DISTANCE = 20;
  boolean dragging = false;

  @Override
  public void settings() {
    size(1600, 1600);
  }

  @Override
  public void setup() {
    graph = new Graph(this);
  }

  @Override
  public void draw() {
    background(26, 28, 44);
    hoverNode = graph.getNearestNode(mouseX, mouseY, SNAP_DISTANCE);

    graph.show(hoverNode);

    if (selectedNode != null && mousePressed && !dragging) {
        strokeWeight(2);
        stroke(37, 113, 121);
        line(selectedNode.location.x, selectedNode.location.y, mouseX, mouseY);
    }
}

  @Override
  public void mousePressed() {
    if (mouseButton == LEFT) {
        hoverNode = graph.getNearestNode(mouseX, mouseY, SNAP_DISTANCE);

        if (hoverNode != null) {
            if (keyPressed && key == CODED && keyCode == SHIFT) {
                // Add a vehicle if SHIFT key is pressed
                graph.addVehicle(hoverNode);
            } else {
                selectedNode = hoverNode;
            }
        } else {
            selectedNode = graph.addNode(new PVector(mouseX, mouseY));
        }
    } else if (mouseButton == RIGHT) {
        hoverNode = graph.getNearestNode(mouseX, mouseY, SNAP_DISTANCE);

        if (hoverNode != null) {
            selectedNode = hoverNode;
            dragging = true;
        }
    } else if (mouseButton == CENTER) {
        hoverNode = graph.getNearestNode(mouseX, mouseY, SNAP_DISTANCE);

        if (hoverNode != null) {
            graph.updateRoads(hoverNode);
            graph.deleteNode(hoverNode);
        }
    }
}


  @Override
  public void mouseDragged() {
    if (dragging && selectedNode != null) {
      selectedNode.location.set(mouseX, mouseY);
    }
  }

  @Override
  public void mouseReleased() {
    if (mouseButton == LEFT && selectedNode != null) {
      hoverNode = graph.getNearestNode(mouseX, mouseY, SNAP_DISTANCE);

      if (hoverNode != null && hoverNode != selectedNode) {
        graph.addRoad(selectedNode, hoverNode, true);
      }
    }
    selectedNode = null;
    dragging = false;
  }
}