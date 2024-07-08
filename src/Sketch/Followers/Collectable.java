package Sketch.Followers;

import processing.core.PApplet;
import processing.core.PVector;

public class Collectable {
    PVector location;
    PApplet parent;
    float health;
    float maxHealth;

    public Collectable(PApplet p, float x, float y) {
        parent = p;
        location = new PVector(x, y);
        maxHealth = 100; // Set initial health to 100
        health = maxHealth;
    }

    PVector getPosition() {
        return location;
    }

    float getHealth() {
        return health;
    }

    void reduceHealth(float amount) {
        health -= amount;
        if (health <= 0) {
            health = 0;
        }
    }

    void show() {
        parent.fill(80, 30, 180);
        parent.noStroke();
        parent.circle(location.x, location.y, 15);
    
        if (health != maxHealth) {
            parent.stroke(255, 0, 0);
            parent.strokeWeight(2);
            parent.noFill();
            float arcAngle = PApplet.map(health, 0, maxHealth, 0, PApplet.TWO_PI);
            parent.arc(location.x, location.y, 20, 20, 0, arcAngle);
        }
    }    
}