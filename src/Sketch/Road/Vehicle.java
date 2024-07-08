package Sketch.Road;

import processing.core.PApplet;
import processing.core.PVector;
import java.util.ArrayList;
import java.util.Random;

public class Vehicle {
    PApplet parent;
    PVector position;
    PVector velocity;
    Node currentTarget;
    Road currentRoad;
    Random random;

    Vehicle(PApplet p, Node startNode) {
        parent = p;
        position = new PVector(startNode.location.x, startNode.location.y);
        velocity = new PVector(0, 0);
        currentTarget = startNode;
        random = new Random();
        pickNewRoad();
    }

    void pickNewRoad() {
        ArrayList<Road> validRoads = new ArrayList<>();
        for (Road road : currentTarget.connections) {
            if (road.twoWay || road.start == currentTarget) {
                validRoads.add(road);
            }
        }

        if (validRoads.size() == 0) return;

        Road previousRoad = currentRoad;
        ArrayList<Integer> weights = new ArrayList<>();
        int totalWeight = 0;

        for (Road road : validRoads) {
            int weight = road == previousRoad ? 1 : 100;
            weights.add(weight);
            totalWeight += weight;
        }

        int randomValue = random.nextInt(totalWeight);
        int cumulativeWeight = 0;
        Road selectedRoad = null;

        for (int i = 0; i < validRoads.size(); i++) {
            cumulativeWeight += weights.get(i);
            if (randomValue < cumulativeWeight) {
                selectedRoad = validRoads.get(i);
                break;
            }
        }

        currentRoad = selectedRoad;
        currentTarget = currentRoad.start == currentTarget ? currentRoad.end : currentRoad.start;
    }

    void update() {
        if (currentRoad == null) return;

        PVector targetPosition = currentTarget.location;
        PVector direction = PVector.sub(targetPosition, position);
        float distance = direction.mag();

        direction.normalize();
        velocity.set(direction);
        velocity.mult(currentRoad.speed * 0.1f);
        position.add(velocity);

        if (distance < 5) {
            position.set(currentTarget.location.x, currentTarget.location.y);
            pickNewRoad();
        }
    }

    void show() {
        if (currentRoad != null) {
            PVector roadDirection = PVector.sub(currentRoad.end.location, currentRoad.start.location);
            roadDirection.normalize();
            PVector perpendicular = new PVector(-roadDirection.y, roadDirection.x).mult(5);

            PVector drawPosition = PVector.add(position, perpendicular);

            parent.fill(255, 0, 0);
            parent.ellipse(drawPosition.x, drawPosition.y, 10, 10);
        } else {
            parent.fill(255, 0, 0);
            parent.ellipse(position.x, position.y, 10, 10);
        }
    }
}