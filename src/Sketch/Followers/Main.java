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
        float speed = 6;

        Boid(float x, float y) {
            position = new PVector(x, y);
            path = new LinkedList<>();
        }

        void followPath() {
            if (!path.isEmpty()) {
                PVector target = path.getFirst();
                PVector direction = PVector.sub(target, position);
                float distance = direction.mag();

                // Smooth movement using interpolation
                direction.normalize().mult(speed * 0.2f);
                if (distance < speed * 0.5f) {
                    position.set(target);
                    path.removeFirst();
                } else {
                    position.add(direction);
                }
            }
        }

        void setPath(LinkedList<PVector> newPath) {
            path = newPath;
        }

        void display() {
            fill(255, 100, 0);
            noStroke();
            ellipse(position.x, position.y, 16, 16);
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
            fill(100, 200, 255);
            noStroke();
            rect(x, y, w, h);
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
            int startX = floor(rect.x / cellSize);
            int startY = floor(rect.y / cellSize);
            int endX = ceil((rect.x + rect.w) / cellSize);
            int endY = ceil((rect.y + rect.h) / cellSize);

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

                visited[current.x][current.y] = true;

                for (int[] offset : new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}, {1, 1}, {-1, -1}, {1, -1}, {-1, 1}}) {
                    int nx = current.x + offset[0];
                    int ny = current.y + offset[1];

                    if (nx >= 0 && nx < cols && ny >= 0 && ny < rows && walkable[nx][ny] && !visited[nx][ny]) {
                        float cost = (offset[0] == 0 || offset[1] == 0) ? cellSize : cellSize * 1.4f;
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
            return path;
        }

        float heuristic(int x, int y, int goalX, int goalY) {
            return dist(x, y, goalX, goalY);
        }

        void display() {
            stroke(200, 50);
            for (int x = 0; x < cols; x++) {
                for (int y = 0; y < rows; y++) {
                    if (!walkable[x][y]) {
                        fill(255, 100, 100, 150);
                        rect(x * cellSize, y * cellSize, cellSize, cellSize);
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
    Boid boid;
    Rectangle draggingRect;

    public static void main(String[] args) {
        PApplet.main("Sketch.Followers.Main");
    }

    @Override
    public void settings() {
        size(800, 800);
    }

    @Override
    public void setup() {
        rectangles = new ArrayList<>();
        navMesh = new NavMesh(20); // Higher resolution
        boid = new Boid(width / 2, height / 2);
    }

    @Override
    public void draw() {
        background(51);

        for (Rectangle rect : rectangles) {
            rect.display();
        }

        navMesh.display();

        if (draggingRect != null) {
            fill(100, 200, 255, 150);
            rect(draggingRect.x, draggingRect.y, mouseX - draggingRect.x, mouseY - draggingRect.y);
        }

        boid.followPath();
        boid.display();
    }

    @Override
    public void mousePressed() {
        if (mouseButton == LEFT) {
            draggingRect = new Rectangle(mouseX, mouseY, 0, 0);
        }
    }

    @Override
    public void mouseReleased() {
        if (mouseButton == LEFT && draggingRect != null) {
            draggingRect.w = mouseX - draggingRect.x;
            draggingRect.h = mouseY - draggingRect.y;
            rectangles.add(draggingRect);
            navMesh.reset();
            for (Rectangle rect : rectangles) {
                navMesh.addObstacle(rect);
            }
            draggingRect = null;
        } else if (mouseButton == RIGHT) {
            PVector target = new PVector(mouseX, mouseY);
            LinkedList<PVector> path = navMesh.findPath(boid.position, target);
            boid.setPath(path);
        }
    }
}
