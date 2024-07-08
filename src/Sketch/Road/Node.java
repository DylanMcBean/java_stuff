package Sketch.Road;

import processing.core.PApplet;
import processing.core.PVector;
import java.util.ArrayList;

public class Node {
    PVector location;
    ArrayList<Road> connections = new ArrayList<>();
    PApplet parent;

    Node(PApplet p, PVector loc) {
        parent = p;
        location = loc;
    }

    void addConnection(Road road) {
        connections.add(road);
    }

    void show(boolean highlight) {
        parent.noStroke();
        if (highlight) {
            parent.fill(59, 93, 201);
            parent.ellipse(location.x, location.y, 16, 16);
        } else {
            parent.fill(41, 54, 111);
            parent.ellipse(location.x, location.y, 16, 16);
        }
    }
}