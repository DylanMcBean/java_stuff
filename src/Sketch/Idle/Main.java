package Sketch.Idle;

import Sketch.ProjectConfig;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main extends PApplet {

    PImage targetImage;
    ArrayList<Genome> population = new ArrayList<Genome>();
    int populationSize = 64;
    int currentGeneration = 0;
    ArrayList<Float> fitnessHistory = new ArrayList<Float>();
    ExecutorService executorService;
    float lastSavedFitness = Float.MAX_VALUE;
    int savedImageCount = 0;  // Counter for saved images

    public static void main(String[] args) {
        PApplet.main("Sketch.Idle.Main");
    }

    @Override
    public void settings() {
        size(572, 434);
        targetImage = loadImage(ProjectConfig.getDataPath("cow.png"));
        targetImage.resize(256, 256);
    }

    @Override
    public void setup() {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (int i = 0; i < populationSize; i++) {
            population.add(new Genome(this, 64, 256, 256));
        }
    }

    @Override
    public void draw() {
        background(26, 28, 44);
        sortGenomesByFitness();
        displayImages();
        displayFitnessScores();
        currentGeneration++;
        evolvePopulation();
        saveBestGenomeImageIfImproved();
    }

    private void evolvePopulation() {
        int eliteCount = 5;
        ArrayList<Genome> newPopulation = new ArrayList<Genome>(population.subList(0, eliteCount));
        
        while (newPopulation.size() < populationSize) {
            Genome parent1 = population.get((int) random(eliteCount));
            Genome parent2 = population.get((int) random(eliteCount));
            Genome child = parent1.crossover(parent2, this);
            applyMutations(child, this);
            newPopulation.add(child);
        }
        
        population = newPopulation;
    }

    private void displayImages() {
        image(targetImage, 20, 20);
        if (!population.isEmpty()) {
            Genome bestGenome = population.get(0);
            image(bestGenome.getPGraphics(), 296, 20);
        }
    }

    private void sortGenomesByFitness() {
        try {
            ArrayList<Future<?>> futures = new ArrayList<>();
            for (Genome genome : population) {
                futures.add(executorService.submit(() -> genome.calculateFitness(targetImage)));
            }
            for (Future<?> future : futures) {
                future.get(); // Wait for all fitness calculations to complete
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Collections.sort(population);
        if (!population.isEmpty()) {
            fitnessHistory.add(population.get(0).calculateFitness(targetImage));
            if (fitnessHistory.size() > 10) {
                fitnessHistory.remove(0);
            }
        }
    }

    private void displayFitnessScores() {
        if (!population.isEmpty()) {
            Genome bestGenome = population.get(0);
            fill(255);
            textSize(32);
            textAlign(LEFT, TOP);

            float currentFitness = bestGenome.calculateFitness(targetImage);
            float previousFitness = fitnessHistory.size() > 0 ? fitnessHistory.get(0) : currentFitness;
            float fitnessIncrease = currentFitness - previousFitness;

            text("Current Generation: " + currentGeneration, 20, 286);
            text("Best Genome Fitness: " + currentFitness, 20, 318);
            text("Fitness Change (last 10): " + fitnessIncrease, 20, 350);
            text("Images Saved: " + savedImageCount, 20, 382);
        }
    }

    private void applyMutations(Genome genome, PApplet applet) {
        if (random(1) < 0.05) {
            genome.mutateMoveTriangle(applet);
        }
        if (random(1) < 0.05) {
            genome.mutateChangeColor(applet);
        }
        if (random(1) < 0.05) {
            genome.mutateCreateNewTriangle(applet);
        }
        if (random(1) < 0.05) {
            genome.mutateMoveWholeTriangle(applet);
        }
        if (random(1) < 0.05) {
            genome.mutateRotateTriangle(applet);
        }
    }

    private void saveBestGenomeImageIfImproved() {
        if (!population.isEmpty()) {
            Genome bestGenome = population.get(0);
            float currentFitness = bestGenome.calculateFitness(targetImage);

            if (lastSavedFitness - currentFitness >= 500) {
                PGraphics bestImage = bestGenome.getPGraphics();
                savedImageCount++;  // Increment the image count
                String formattedCount = String.format("%05d", savedImageCount);  // Format the count with leading zeros
                bestImage.save(ProjectConfig.getDataPath("Output/Image_" + formattedCount + ".png"));
                lastSavedFitness = currentFitness;
            }
        }
    }
}