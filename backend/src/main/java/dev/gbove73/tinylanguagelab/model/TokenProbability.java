package dev.gbove73.tinylanguagelab.model;

/** Una possibile continuazione e la probabilità assegnata dal modello. */
public record TokenProbability(String token, double probability, long observedCount) {
}
