package Sketch.Idle;

import Sketch.ProjectConfig;
import processing.core.PApplet;
import processing.core.PImage;
import processing.event.MouseEvent;

public class Main extends PApplet {
    Button textButton;
    Button imageButton;
    Button textImageButton;
    Button simpleButton;
    PImage buttonImage;

    public static void main(String[] args) {
        PApplet.main("Sketch.Idle.Main");
    }

    @Override
    public void settings() {
        size(800, 600);
    }

    @Override
    public void setup() {
        buttonImage = loadImage(ProjectConfig.getDataPath("./UI/Buttons/button_square_depth_flat.png")); // Specify the path to your image file

        // Initialize buttons with different configurations
        textButton = new Button(this, 50, 100, 64, 64, "Text Button", () -> println("Text Button Clicked!"));
        imageButton = new Button(this, 50, 200, 64, 64, buttonImage, () -> println("Image Button Clicked!"));
        textImageButton = new Button(this, 50, 300, 64, 64, "Text+Image", buttonImage, () -> println("Text+Image Button Clicked!"));
        simpleButton = new Button(this, 50, 400, 64, 64, () -> println("Simple Button Clicked!"));
    }

    @Override
    public void draw() {
        background(26, 28, 44);
        textButton.display();
        imageButton.display();
        textImageButton.display();
        simpleButton.display();
    }

    @Override
    public void mousePressed(MouseEvent event) {
        textButton.handleMousePressed(event);
        imageButton.handleMousePressed(event);
        textImageButton.handleMousePressed(event);
        simpleButton.handleMousePressed(event);
    }
}
