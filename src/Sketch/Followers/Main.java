package Sketch.Followers;

import processing.core.PApplet;
import java.util.ArrayList;

public class Main extends PApplet {

  ArrayList<Boid> boids = new ArrayList<Boid>();
  ArrayList<Collectable> collectables = new ArrayList<Collectable>();

  public static void main(String[] args) {
    PApplet.main("Sketch.Followers.Main");
  }

  @Override
  public void settings() {
    size(1600, 1600);
  }

  @Override
  public void setup() {
    for (int i = 0; i < 1; i++) {
      collectables.add(new Collectable(this, random(width), random(height)));
    }

    for (int i = 0; i < 1; i++) {
      boids.add(new Boid(this, random(width), random(height)));
    }
  }

  @Override
  public void draw() {
    background(51);

    for (int i = boids.size() - 1; i >= 0; i--) {
      Boid b = boids.get(i);

      b.behaviors(collectables);
      b.update();
      b.show();
    }

    for (int i = collectables.size() - 1; i >= 0; i--) {
      Collectable c = collectables.get(i);
      if (c.getHealth() <= 0) {
        collectables.remove(i);
      } else {
        c.show();
      }
    }
  }
}