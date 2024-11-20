package Sketch.Idle;

import processing.core.PApplet;
import processing.core.PImage;
import processing.event.MouseEvent;

/**
 * Represents a customizable button in a Processing sketch.
 */
public class Button {
    private final PApplet p;
    private final float x, y, width, height;
    private final String text;
    private final PImage image;
    private final Runnable callback;
    private boolean enabled;
    private int backgroundColor;
    private int textColor;
    private int disabledColor;

    public Button(PApplet p, float x, float y, float width, float height, String text, Runnable callback) {
        this(p, x, y, width, height, text, null, callback);
    }

    public Button(PApplet p, float x, float y, float width, float height, PImage image, Runnable callback) {
        this(p, x, y, width, height, null, image, callback);
    }

    public Button(PApplet p, float x, float y, float width, float height, String text, PImage image, Runnable callback) {
        this.p = p;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.image = image;
        this.callback = callback;
        this.enabled = true;
        this.backgroundColor = p.color(100);
        this.textColor = p.color(255);
        this.disabledColor = p.color(150);
    }

    public Button(PApplet p, float x, float y, float width, float height, Runnable callback) {
        this(p, x, y, width, height, null, null, callback);
    }

    public void display() {
        p.stroke(255);
        if (image == null) {
            p.fill(enabled ? backgroundColor : disabledColor);
            p.rect(x, y, width, height);
        } else {
            p.image(image, x, y, width, height);
        }

        if (text != null && !text.isEmpty()) {
            p.fill(textColor);
            p.textAlign(PApplet.CENTER, PApplet.CENTER);
            p.text(text, x + width / 2, y + height / 2);
        }
    }

    public void handleMousePressed(MouseEvent event) {
        if (enabled && isMouseOver(event.getX(), event.getY())) {
            if (callback != null) {
                callback.run();
            }
        }
    }

    private boolean isMouseOver(float mouseX, float mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setDisabledColor(int disabledColor) {
        this.disabledColor = disabledColor;
    }
}