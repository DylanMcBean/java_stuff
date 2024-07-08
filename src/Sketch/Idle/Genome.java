package Sketch.Idle;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import java.awt.Color;

public class Genome implements Comparable<Genome> {

    private float[] x1, y1, x2, y2, x3, y3;
    private int[] colors;
    private int numTriangles;
    private PGraphics pg;
    private float fitness;
    private boolean fitnessCalculated;

    public Genome(PApplet applet, int numTriangles, int width, int height) {
        this.numTriangles = numTriangles;
        x1 = new float[numTriangles];
        y1 = new float[numTriangles];
        x2 = new float[numTriangles];
        y2 = new float[numTriangles];
        x3 = new float[numTriangles];
        y3 = new float[numTriangles];
        colors = new int[numTriangles];
        pg = applet.createGraphics(width, height);
        this.fitness = -1;
        this.fitnessCalculated = false;

        initializeTriangles(applet);
    }

    private void initializeTriangles(PApplet applet) {
        for (int i = 0; i < numTriangles; i++) {
            x1[i] = applet.random(pg.width);
            y1[i] = applet.random(pg.height);
            x2[i] = applet.random(pg.width);
            y2[i] = applet.random(pg.height);
            x3[i] = applet.random(pg.width);
            y3[i] = applet.random(pg.height);
            colors[i] = applet.color(applet.random(255), applet.random(255), applet.random(255), applet.random(255));
        }
    }

    public void drawTriangles() {
        pg.beginDraw();
        pg.background(255);
        for (int i = 0; i < numTriangles; i++) {
            pg.fill(colors[i]);
            pg.noStroke();
            pg.triangle(x1[i], y1[i], x2[i], y2[i], x3[i], y3[i]);
        }
        pg.endDraw();
        fitnessCalculated = false;
    }

    public PGraphics getPGraphics() {
        return pg;
    }

    public float calculateFitness(PImage targetImage) {
        if (!fitnessCalculated) {
            drawTriangles();
            fitness = 0;
            pg.loadPixels();
            targetImage.loadPixels();

            for (int i = 0; i < pg.pixels.length; i++) {
                int currentPixel = pg.pixels[i];
                int targetPixel = targetImage.pixels[i];

                int r1 = (currentPixel >> 16) & 0xFF;
                int g1 = (currentPixel >> 8) & 0xFF;
                int b1 = currentPixel & 0xFF;

                int r2 = (targetPixel >> 16) & 0xFF;
                int g2 = (targetPixel >> 8) & 0xFF;
                int b2 = targetPixel & 0xFF;

                float[] lab1 = rgbToLab(r1, g1, b1);
                float[] lab2 = rgbToLab(r2, g2, b2);

                float deltaE = calculateCIEDE2000(lab1, lab2);
                fitness += deltaE;
            }
            fitnessCalculated = true;
        }
        return fitness;
    }

    private float[] rgbToLab(int r, int g, int b) {
        float rNorm = r / 255.0f;
        float gNorm = g / 255.0f;
        float bNorm = b / 255.0f;

        float x = (rNorm > 0.04045f) ? (float) Math.pow((rNorm + 0.055f) / 1.055f, 2.4f) : rNorm / 12.92f;
        float y = (gNorm > 0.04045f) ? (float) Math.pow((gNorm + 0.055f) / 1.055f, 2.4f) : gNorm / 12.92f;
        float z = (bNorm > 0.04045f) ? (float) Math.pow((bNorm + 0.055f) / 1.055f, 2.4f) : bNorm / 12.92f;

        float[] xyz = {
            x * 0.4124564f + y * 0.3575761f + z * 0.1804375f,
            x * 0.2126729f + y * 0.7151522f + z * 0.0721750f,
            x * 0.0193339f + y * 0.1191920f + z * 0.9503041f
        };

        return xyzToLab(xyz[0], xyz[1], xyz[2]);
    }

    private float[] xyzToLab(float x, float y, float z) {
        x = x / 0.95047f;
        y = y / 1.00000f;
        z = z / 1.08883f;

        x = (x > 0.008856f) ? (float) Math.pow(x, 1.0 / 3.0) : (7.787f * x) + (16.0f / 116.0f);
        y = (y > 0.008856f) ? (float) Math.pow(y, 1.0 / 3.0) : (7.787f * y) + (16.0f / 116.0f);
        z = (z > 0.008856f) ? (float) Math.pow(z, 1.0 / 3.0) : (7.787f * z) + (16.0f / 116.0f);

        return new float[]{(116.0f * y) - 16.0f, 500.0f * (x - y), 200.0f * (y - z)};
    }

    private float calculateCIEDE2000(float[] lab1, float[] lab2) {
        double deltaL = lab2[0] - lab1[0];
        double lBar = (lab1[0] + lab2[0]) / 2.0;
        double c1 = Math.sqrt(lab1[1] * lab1[1] + lab1[2] * lab1[2]);
        double c2 = Math.sqrt(lab2[1] * lab2[1] + lab2[2] * lab2[2]);
        double cBar = (c1 + c2) / 2.0;
        double a1Prime = lab1[1] + lab1[1] / 2.0 * (1 - Math.sqrt(Math.pow(cBar, 7.0) / (Math.pow(cBar, 7.0) + Math.pow(25.0, 7.0))));
        double a2Prime = lab2[1] + lab2[1] / 2.0 * (1 - Math.sqrt(Math.pow(cBar, 7.0) / (Math.pow(cBar, 7.0) + Math.pow(25.0, 7.0))));
        double c1Prime = Math.sqrt(a1Prime * a1Prime + lab1[2] * lab1[2]);
        double c2Prime = Math.sqrt(a2Prime * a2Prime + lab2[2] * lab2[2]);
        double cBarPrime = (c1Prime + c2Prime) / 2.0;
        double h1Prime = Math.atan2(lab1[2], a1Prime) * 180 / Math.PI;
        double h2Prime = Math.atan2(lab2[2], a2Prime) * 180 / Math.PI;
        if (h1Prime < 0) h1Prime += 360;
        if (h2Prime < 0) h2Prime += 360;
        double hBarPrime = (Math.abs(h1Prime - h2Prime) > 180) ? (h1Prime + h2Prime + 360) / 2.0 : (h1Prime + h2Prime) / 2.0;
        double t = 1 - 0.17 * Math.cos(Math.toRadians(hBarPrime - 30)) + 0.24 * Math.cos(Math.toRadians(2 * hBarPrime)) + 0.32 * Math.cos(Math.toRadians(3 * hBarPrime + 6)) - 0.20 * Math.cos(Math.toRadians(4 * hBarPrime - 63));
        double deltaHPrime = 2 * Math.sqrt(c1Prime * c2Prime) * Math.sin(Math.toRadians((h2Prime - h1Prime) / 2.0));
        double sl = 1 + ((0.015 * (lBar - 50) * (lBar - 50)) / Math.sqrt(20 + (lBar - 50) * (lBar - 50)));
        double sc = 1 + 0.045 * cBarPrime;
        double sh = 1 + 0.015 * cBarPrime * t;
        double deltaTheta = 30 * Math.exp(-((hBarPrime - 275) / 25) * ((hBarPrime - 275) / 25));
        double rc = 2 * Math.sqrt(Math.pow(cBarPrime, 7.0) / (Math.pow(cBarPrime, 7.0) + Math.pow(25.0, 7.0)));
        double rt = -rc * Math.sin(2 * Math.toRadians(deltaTheta));
        return (float) Math.sqrt(Math.pow(deltaL / sl, 2) + Math.pow((c2Prime - c1Prime) / sc, 2) + Math.pow(deltaHPrime / sh, 2) + rt * (c2Prime - c1Prime) * (h2Prime - h1Prime) / (sc * sh));
    }

    public Genome crossover(Genome other, PApplet applet) {
        Genome offspring = new Genome(applet, numTriangles, pg.width, pg.height);
    
        // Ensure fitness values are calculated
        float thisFitness = this.calculateFitness(null); // Assuming targetImage is available in your context
        float otherFitness = other.calculateFitness(null); // Assuming targetImage is available in your context
    
        // Calculate the total fitness and probabilities
        float totalFitness = thisFitness + otherFitness;
        float thisProbability = thisFitness / totalFitness;
    
        for (int i = 0; i < numTriangles; i++) {
            if (applet.random(1) < thisProbability) {
                offspring.x1[i] = this.x1[i];
                offspring.y1[i] = this.y1[i];
                offspring.x2[i] = this.x2[i];
                offspring.y2[i] = this.y2[i];
                offspring.x3[i] = this.x3[i];
                offspring.y3[i] = this.y3[i];
                offspring.colors[i] = this.colors[i];
            } else {
                offspring.x1[i] = other.x1[i];
                offspring.y1[i] = other.y1[i];
                offspring.x2[i] = other.x2[i];
                offspring.y2[i] = other.y2[i];
                offspring.x3[i] = other.x3[i];
                offspring.y3[i] = other.y3[i];
                offspring.colors[i] = other.colors[i];
            }
        }
    
        return offspring;
    }    

    public void mutateMoveTriangle(PApplet applet) {
        int triangleIndex = (int) applet.random(numTriangles);
        if (applet.random(1) > 0.5) {
            x1[triangleIndex] = applet.random(pg.width);
            y1[triangleIndex] = applet.random(pg.height);
            x2[triangleIndex] = applet.random(pg.width);
            y2[triangleIndex] = applet.random(pg.height);
            x3[triangleIndex] = applet.random(pg.width);
            y3[triangleIndex] = applet.random(pg.height);
        } else {
            int pointToMove = (int) applet.random(3);
            switch (pointToMove) {
                case 0:
                    x1[triangleIndex] = applet.random(pg.width);
                    y1[triangleIndex] = applet.random(pg.height);
                    break;
                case 1:
                    x2[triangleIndex] = applet.random(pg.width);
                    y2[triangleIndex] = applet.random(pg.height);
                    break;
                case 2:
                    x3[triangleIndex] = applet.random(pg.width);
                    y3[triangleIndex] = applet.random(pg.height);
                    break;
            }
        }
        fitnessCalculated = false;
    }

    public void mutateMoveWholeTriangle(PApplet applet) {
        int triangleIndex = (int) applet.random(numTriangles);
        float dx = applet.random(-pg.width / 4, pg.width / 4);
        float dy = applet.random(-pg.height / 4, pg.height / 4);

        x1[triangleIndex] = constrain(x1[triangleIndex] + dx, 0, pg.width);
        y1[triangleIndex] = constrain(y1[triangleIndex] + dy, 0, pg.height);
        x2[triangleIndex] = constrain(x2[triangleIndex] + dx, 0, pg.width);
        y2[triangleIndex] = constrain(y2[triangleIndex] + dy, 0, pg.height);
        x3[triangleIndex] = constrain(x3[triangleIndex] + dx, 0, pg.width);
        y3[triangleIndex] = constrain(y3[triangleIndex] + dy, 0, pg.height);

        fitnessCalculated = false;
    }

    private float constrain(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public void mutateRotateTriangle(PApplet applet) {
        int triangleIndex = (int) applet.random(numTriangles);

        float centerX = (x1[triangleIndex] + x2[triangleIndex] + x3[triangleIndex]) / 3;
        float centerY = (y1[triangleIndex] + y2[triangleIndex] + y3[triangleIndex]) / 3;

        float angle = applet.random(PApplet.TWO_PI);

        x1[triangleIndex] = rotatePointX(x1[triangleIndex], y1[triangleIndex], centerX, centerY, angle);
        y1[triangleIndex] = rotatePointY(x1[triangleIndex], y1[triangleIndex], centerX, centerY, angle);

        x2[triangleIndex] = rotatePointX(x2[triangleIndex], y2[triangleIndex], centerX, centerY, angle);
        y2[triangleIndex] = rotatePointY(x2[triangleIndex], y2[triangleIndex], centerX, centerY, angle);

        x3[triangleIndex] = rotatePointX(x3[triangleIndex], y3[triangleIndex], centerX, centerY, angle);
        y3[triangleIndex] = rotatePointY(x3[triangleIndex], y3[triangleIndex], centerX, centerY, angle);

        fitnessCalculated = false;
    }

    private float rotatePointX(float x, float y, float centerX, float centerY, float angle) {
        float dx = x - centerX;
        float dy = y - centerY;
        return centerX + (dx * PApplet.cos(angle) - dy * PApplet.sin(angle));
    }

    private float rotatePointY(float x, float y, float centerX, float centerY, float angle) {
        float dx = x - centerX;
        float dy = y - centerY;
        return centerY + (dx * PApplet.sin(angle) + dy * PApplet.cos(angle));
    }

    public void mutateChangeColor(PApplet applet) {
        int triangleIndex = (int) applet.random(numTriangles);
        colors[triangleIndex] = applet.color(applet.random(255), applet.random(255), applet.random(255), applet.random(255));
        fitnessCalculated = false;
    }

    public void mutateCreateNewTriangle(PApplet applet) {
        int triangleIndex = (int) applet.random(numTriangles);
        x1[triangleIndex] = applet.random(pg.width);
        y1[triangleIndex] = applet.random(pg.height);
        x2[triangleIndex] = applet.random(pg.width);
        y2[triangleIndex] = applet.random(pg.height);
        x3[triangleIndex] = applet.random(pg.width);
        y3[triangleIndex] = applet.random(pg.height);
        colors[triangleIndex] = applet.color(applet.random(255), applet.random(255), applet.random(255), applet.random(255));
        fitnessCalculated = false;
    }

    @Override
    public int compareTo(Genome other) {
        return Float.compare(this.fitness, other.fitness);
    }
}