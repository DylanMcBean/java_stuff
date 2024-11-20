package Sketch.Followers;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class Main extends PApplet {

    class Boid {
        PVector position;
        LinkedList<PVector> path;
        LinkedList<PVector> waypoints;
        float speed;
        NavMesh navMesh;
        boolean userControlled = false;

        Boid(float x, float y, NavMesh navMesh, boolean _userControlled) {
            position = new PVector(x, y);
            path = new LinkedList<>();
            waypoints = new LinkedList<>();
            this.navMesh = navMesh;
            userControlled = _userControlled;
            if (userControlled) {
                speed = 6;
            } else {
                speed = 2;
            }
        }

        void followPath() {
            if (path == null || path.isEmpty()) {
                if (!waypoints.isEmpty()) {
                    updatePath();
                } else {
                    return;
                }
            }

            if (!path.isEmpty()) {
                PVector target = path.getFirst();
                PVector direction = PVector.sub(target, position);
                float distance = direction.mag();

                float step = min(speed * 0.2f, distance);
                direction.normalize().mult(step);
                position.add(direction);

                if (distance < 1) {
                    position.set(target);
                    path.removeFirst();
                    if (path.isEmpty() && !waypoints.isEmpty()) {
                        // Reached waypoint
                        waypoints.removeFirst();
                        updatePath();
                    }
                }
            }
        }

        void addWaypoint(PVector waypoint) {
            waypoints.add(waypoint);
            if (path.isEmpty()) {
                updatePath();
            }
        }

        void updatePath() {
            while (!waypoints.isEmpty()) {
                PVector nextWaypoint = waypoints.getFirst();
                LinkedList<PVector> newPath = navMesh.findPath(position, nextWaypoint);
                if (!newPath.isEmpty()) {
                    path = newPath;
                    break;
                } else {
                    // Cannot reach this waypoint, remove it and try next
                    waypoints.removeFirst();
                }
            }
            if (waypoints.isEmpty()) {
                path = new LinkedList<>();
            }
        }

        void display() {
            if (userControlled) {
                fill(255, 100, 0);
                noStroke();
                ellipse(position.x, position.y, 10, 10);
            } else {
                fill(100, 200, 255);
                noStroke();
                ellipse(position.x, position.y, 8, 8);
            }
        }

        void displayPath() {
            if (path != null && !path.isEmpty()) {
                stroke(0, 255, 0);
                strokeWeight(1);
                noFill();
                beginShape();
                vertex(position.x, position.y);
                for (PVector p : path) {
                    vertex(p.x, p.y);
                }
                endShape();
            }
        }

        void displayWaypoints() {
            fill(0, 255, 200);
            noStroke();
            for (PVector wp : waypoints) {
                ellipse(wp.x, wp.y, 2, 2);
            }
        }
    }

    class Rectangle {
        float x, y, w, h;

        Rectangle(float x, float y, float w, float h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        void display() {
            float x1 = x;
            float y1 = y;
            float w1 = w;
            float h1 = h;

            if (w1 < 0) {
                x1 += w1;
                w1 = -w1;
            }
            if (h1 < 0) {
                y1 += h1;
                h1 = -h1;
            }

            fill(100, 200, 255);
            noStroke();
            rect(x1, y1, w1, h1);
        }
    }

    class NavMesh {
        int cols, rows;
        float cellSize;
        boolean[][] walkable;

        NavMesh(float cellSize) {
            this.cellSize = cellSize;
            cols = ceil(width / cellSize);
            rows = ceil(height / cellSize);
            walkable = new boolean[cols][rows];
            reset();
        }

        void reset() {
            for (int x = 0; x < cols; x++) {
                for (int y = 0; y < rows; y++) {
                    walkable[x][y] = true;
                }
            }
        }

        void addObstacle(Rectangle rect) {
            float x1 = rect.x;
            float y1 = rect.y;
            float x2 = rect.x + rect.w;
            float y2 = rect.y + rect.h;

            if (x1 > x2) {
                float temp = x1;
                x1 = x2;
                x2 = temp;
            }

            if (y1 > y2) {
                float temp = y1;
                y1 = y2;
                y2 = temp;
            }

            int startX = floor(x1 / cellSize);
            int startY = floor(y1 / cellSize);
            int endX = ceil(x2 / cellSize);
            int endY = ceil(y2 / cellSize);

            for (int x = startX; x < endX; x++) {
                for (int y = startY; y < endY; y++) {
                    if (x >= 0 && x < cols && y >= 0 && y < rows) {
                        walkable[x][y] = false;
                    }
                }
            }
        }

        LinkedList<PVector> findPath(PVector start, PVector goal) {
            int startX = floor(start.x / cellSize);
            int startY = floor(start.y / cellSize);
            int goalX = floor(goal.x / cellSize);
            int goalY = floor(goal.y / cellSize);

            PriorityQueue<Node> openSet = new PriorityQueue<>();
            boolean[][] visited = new boolean[cols][rows];
            Node[][] nodes = new Node[cols][rows];

            for (int x = 0; x < cols; x++) {
                for (int y = 0; y < rows; y++) {
                    nodes[x][y] = new Node(x, y);
                }
            }

            Node startNode = nodes[startX][startY];
            startNode.gCost = 0;
            startNode.hCost = heuristic(startX, startY, goalX, goalY);
            openSet.add(startNode);

            while (!openSet.isEmpty()) {
                Node current = openSet.poll();
                if (current.x == goalX && current.y == goalY) {
                    return reconstructPath(nodes, goalX, goalY);
                }

                if (visited[current.x][current.y]) continue;
                visited[current.x][current.y] = true;

                for (int[] offset : new int[][]{
                        {1, 0}, {0, 1}, {-1, 0}, {0, -1},
                        {1, 1}, {-1, 1}, {-1, -1}, {1, -1}
                }) {
                    int nx = current.x + offset[0];
                    int ny = current.y + offset[1];

                    if (nx >= 0 && nx < cols && ny >= 0 && ny < rows && walkable[nx][ny]) {
                        if (visited[nx][ny]) continue;

                        // Check for illegal diagonal movement
                        if (offset[0] != 0 && offset[1] != 0) {
                            int x1 = current.x + offset[0];
                            int y1 = current.y;
                            int x2 = current.x;
                            int y2 = current.y + offset[1];
                            if (!walkable[x1][y1] || !walkable[x2][y2]) {
                                continue;
                            }
                        }

                        float cost;
                        if (offset[0] != 0 && offset[1] != 0) {
                            // Diagonal movement cost
                            cost = cellSize * sqrt(2);
                        } else {
                            // Straight movement cost
                            cost = cellSize;
                        }

                        float newGCost = current.gCost + cost;
                        Node neighbor = nodes[nx][ny];

                        if (newGCost < neighbor.gCost) {
                            neighbor.gCost = newGCost;
                            neighbor.hCost = heuristic(nx, ny, goalX, goalY);
                            neighbor.parent = current;
                            openSet.add(neighbor);
                        }
                    }
                }
            }

            return new LinkedList<>();
        }

        LinkedList<PVector> reconstructPath(Node[][] nodes, int goalX, int goalY) {
            LinkedList<PVector> path = new LinkedList<>();
            Node current = nodes[goalX][goalY];
            while (current != null) {
                path.addFirst(new PVector(current.x * cellSize + cellSize / 2, current.y * cellSize + cellSize / 2));
                current = current.parent;
            }
            path = smoothPath(path);
            return path;
        }

        LinkedList<PVector> smoothPath(LinkedList<PVector> path) {
            if (path.size() < 3) return path;

            LinkedList<PVector> newPath = new LinkedList<>();
            newPath.add(path.getFirst());

            int index = 0;
            while (index < path.size() - 1) {
                int nextIndex = path.size() - 1;
                for (int i = path.size() - 1; i > index; i--) {
                    if (lineOfSight(path.get(index), path.get(i))) {
                        nextIndex = i;
                        break;
                    }
                }
                newPath.add(path.get(nextIndex));
                index = nextIndex;
            }

            return newPath;
        }

        // Bresenham's Line Algorithm for line of sight check
        boolean lineOfSight(PVector start, PVector end) {
            int x0 = floor(start.x / cellSize);
            int y0 = floor(start.y / cellSize);
            int x1 = floor(end.x / cellSize);
            int y1 = floor(end.y / cellSize);

            int dx = abs(x1 - x0);
            int dy = abs(y1 - y0);

            int sx = x0 < x1 ? 1 : -1;
            int sy = y0 < y1 ? 1 : -1;

            int err = dx - dy;

            while (true) {
                if (!walkable[x0][y0]) {
                    return false;
                }
                if (x0 == x1 && y0 == y1) {
                    break;
                }
                int e2 = 2 * err;
                if (e2 > -dy) {
                    err -= dy;
                    x0 += sx;
                }
                if (e2 < dx) {
                    err += dx;
                    y0 += sy;
                }
            }
            return true;
        }

        float heuristic(int x, int y, int goalX, int goalY) {
            return dist(x, y, goalX, goalY) * cellSize;
        }

        void display() {
            strokeWeight(1);
            noStroke();
            fill(173, 216, 230, 50);
            for (int x = 0; x < cols; x++) {
                for (int y = 0; y < rows; y++) {
                    if (walkable[x][y]) {
                        rect(x * cellSize, y * cellSize, cellSize, cellSize);
                    }
                }
            }

            fill(255, 100, 100, 20);
            for (int x = 0; x < cols; x++) {
                for (int y = 0; y < rows; y++) {
                    if (!walkable[x][y]) {
                        rect(x * cellSize, y * cellSize, cellSize, cellSize);
                    }
                }
            }

            stroke(255);
            for (int x = 0; x < cols; x++) {
                for (int y = 0; y < rows; y++) {
                    if (walkable[x][y]) {
                        if (x > 0 && !walkable[x - 1][y]) {
                            // Left edge
                            line(x * cellSize, y * cellSize, x * cellSize, y * cellSize + cellSize);
                        }
                        if (x < cols - 1 && !walkable[x + 1][y]) {
                            // Right edge
                            line((x + 1) * cellSize, y * cellSize, (x + 1) * cellSize, y * cellSize + cellSize);
                        }
                        if (y > 0 && !walkable[x][y - 1]) {
                            // Top edge
                            line(x * cellSize, y * cellSize, x * cellSize + cellSize, y * cellSize);
                        }
                        if (y < rows - 1 && !walkable[x][y + 1]) {
                            // Bottom edge
                            line(x * cellSize, (y + 1) * cellSize, x * cellSize + cellSize, (y + 1) * cellSize);
                        }
                    }
                }
            }
        }
    }

    class Node implements Comparable<Node> {
        int x, y;
        float gCost, hCost;
        Node parent;

        Node(int x, int y) {
            this.x = x;
            this.y = y;
            this.gCost = Float.MAX_VALUE;
            this.hCost = Float.MAX_VALUE;
            this.parent = null;
        }

        float fCost() {
            return gCost + hCost;
        }

        @Override
        public int compareTo(Node other) {
            return Float.compare(this.fCost(), other.fCost());
        }
    }

    ArrayList<Rectangle> rectangles;
    NavMesh navMesh;
    ArrayList<Boid> boids;
    Rectangle draggingRect;

    public static void main(String[] args) {
        PApplet.main("Sketch.Followers.Main");
    }

    @Override
    public void settings() {
        size(1200, 1200);
    }

    @Override
    public void setup() {
        rectangles = new ArrayList<>();
        navMesh = new NavMesh(20);
        boids = new ArrayList<>(); 
        int numBoids = 1;
        for (int i = 0; i < numBoids; i++) {
            Boid b = new Boid(width / 2, height / 2, navMesh, i == 0);
            boids.add(b);
        }
    }

    @Override
    public void draw() {
        background(51);

        navMesh.display();

        for (Rectangle rect : rectangles) {
            rect.display();
        }

        if (draggingRect != null) {
            float x = draggingRect.x;
            float y = draggingRect.y;
            float w = mouseX - draggingRect.x;
            float h = mouseY - draggingRect.y;

            if (w < 0) {
                x += w;
                w = -w;
            }
            if (h < 0) {
                y += h;
                h = -h;
            }

            fill(100, 200, 255, 150);
            noStroke();
            rect(x, y, w, h);
        }

        for (int i = 0; i < boids.size(); i++) {
            Boid boid = boids.get(i);
            if (i != 0) {
                if ((boid.path == null || boid.path.isEmpty()) && boid.waypoints.isEmpty()) {
                    PVector waypoint = new PVector(random(width), random(height));
                    boid.addWaypoint(waypoint);
                }
            }
            boid.followPath();
            boid.displayPath();
            boid.displayWaypoints();
            boid.display();
        }

        fill(255);
        text("FPS: " + (int) frameRate, 10, 20);
        text("Boids: " + boids.size(), 10, 40);
    }

    @Override
    public void mousePressed() {
        if (mouseButton == LEFT) {
            draggingRect = new Rectangle(mouseX, mouseY, 0, 0);
        }
    }

    @Override
    public void mouseDragged() {
        if (mouseButton == LEFT && draggingRect != null) {
            draggingRect.w = mouseX - draggingRect.x;
            draggingRect.h = mouseY - draggingRect.y;
        }
    }

    @Override
    public void mouseReleased() {
        if (mouseButton == LEFT && draggingRect != null) {
            draggingRect.w = mouseX - draggingRect.x;
            draggingRect.h = mouseY - draggingRect.y;

            if (draggingRect.w < 0) {
                draggingRect.x += draggingRect.w;
                draggingRect.w = -draggingRect.w;
            }
            if (draggingRect.h < 0) {
                draggingRect.y += draggingRect.h;
                draggingRect.h = -draggingRect.h;
            }

            rectangles.add(draggingRect);
            navMesh.reset();
            for (Rectangle rect : rectangles) {
                navMesh.addObstacle(rect);
            }
            draggingRect = null;

            for (Boid b : boids) {
                b.updatePath();
            }
        } else if (mouseButton == RIGHT) {
            PVector waypoint = new PVector(mouseX, mouseY);
            if (!boids.isEmpty()) {
                boids.get(0).addWaypoint(waypoint);
            }
        }
    }

    @Override
    public void keyPressed() {
        if (key == '+') {
            boolean isUserControlled = boids.isEmpty();
            Boid b = new Boid(width / 2, height / 2, navMesh, isUserControlled);
            boids.add(b);
        } else if (key == '-') {
            if (!boids.isEmpty()) {
                boids.remove(boids.size() - 1);
            }
        }
    }
}
