package Sketch.Followers;

import processing.core.PApplet;
import processing.core.PVector;
import java.util.ArrayList;

public class Boid {
    PVector location;
    PVector velocity;
    PVector acceleration;
    float maxForce;
    float maxSpeed;
    PApplet parent;
    float attackSpeed = 0.5f; // Attack speed per frame

    Collectable target;

    public Boid(PApplet p, float x, float y) {
        parent = p;
        location = new PVector(x, y);
        velocity = new PVector(parent.random(-1, 1), parent.random(-1, 1));
        acceleration = new PVector(0, 0);
        maxForce = p.random(0.05f,0.2f);
        maxSpeed = p.random(1.5f,3.0f);
    }

    void show() {
        float angle = velocity.heading() + PApplet.PI / 2;
        parent.fill(127);
        parent.stroke(200);
        parent.pushMatrix();
        parent.translate(location.x, location.y);
        parent.rotate(angle);
        parent.beginShape();
        parent.vertex(0, -10);
        parent.vertex(-5, 10);
        parent.vertex(5, 10);
        parent.endShape(PApplet.CLOSE);
        parent.popMatrix();
    }

    void update() {
        velocity.add(acceleration);
        velocity.limit(maxSpeed);
        location.add(velocity);
        acceleration.mult(0);
    }

    void applyForce(PVector force) {
        acceleration.add(force);
    }

    PVector seek(PVector target) {
        PVector desired = PVector.sub(target, location);
        desired.setMag(maxSpeed);
        PVector steer = PVector.sub(desired, velocity);
        steer.limit(maxForce);
        return steer;
    }

    void arrive(PVector target, int distance) {
        PVector desired = PVector.sub(target, location);
        float d = desired.mag();

        if (d < 100) {
            float m = map(d, distance, 100 + distance, 0, maxSpeed, true);
            desired.setMag(m);
        } else {
            desired.setMag(maxSpeed);
        }

        PVector steer = PVector.sub(desired, velocity);
        steer.limit(maxForce);
        applyForce(steer);
    }

    void behaviors(ArrayList<Collectable> collectables) {
        if (target == null || target.getHealth() == 0) {
            target = null;
            if (collectables.size() > 0) {
                target = collectables.get((int) parent.random(collectables.size()));
            }
        }

        if (target != null) {
            float distance = PVector.dist(location, target.getPosition());
            if (distance < 30) {
                target.reduceHealth(attackSpeed);
                if (target.getHealth() == 0) {
                    target = null;
                }
            } else {
                arrive(target.getPosition(), 25);
            }
        } else {
            velocity.setMag(0);
            acceleration.setMag(0);
        }
    }

    float map(float value, float start1, float stop1, float start2, float stop2, boolean constrain) {
        float newValue = start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
        if (constrain) {
            if (start2 < stop2) {
                newValue = PApplet.constrain(newValue, start2, stop2);
            } else {
                newValue = PApplet.constrain(newValue, stop2, start2);
            }
        }
        return newValue;
    }
}