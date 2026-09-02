package dev.gbove73.tinylanguagelab.model;

import java.util.List;

/** Testo generato e traccia leggibile delle decisioni prese. */
public record GenerationResult(String text, List<GenerationStep> steps) {

    public record GenerationStep(int position, String context, String selectedToken, double probability) {
    }
}
