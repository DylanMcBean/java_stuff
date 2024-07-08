package Sketch.Road;

import processing.core.PApplet;
import processing.core.PVector;

public class Road {
    Node start;
    Node end;
    int speed;
    boolean twoWay;
    PApplet parent;

    Road(PApplet p, Node s, Node e, boolean twoWay) {
        parent = p;
        start = s;
        end = e;
        speed = 30;
        this.twoWay = twoWay;
    }

    void update(Node deletingNode) {

        if (deletingNode != start) {
            start.connections.remove(this);
            System.err.println("Start Updated");
        }

        if (deletingNode != end) {
            end.connections.remove(this);
            System.err.println("End Updated");
        }
    }

    void show() {
        parent.strokeWeight(5);
        parent.stroke(56, 183, 100);

        if (twoWay) {
            PVector direction = PVector.sub(end.location, start.location);
            direction.normalize();
            PVector perp = direction.copy().rotate(PApplet.HALF_PI).mult(5);

            parent.line(start.location.x + perp.x, start.location.y + perp.y, end.location.x + perp.x, end.location.y + perp.y);
            parent.line(start.location.x - perp.x, start.location.y - perp.y, end.location.x - perp.x, end.location.y - perp.y);
        } else {
            parent.line(start.location.x, start.location.y, end.location.x, end.location.y);
        }
    }
}