package dev.gbove73.tinylanguagelab.model;

import java.util.List;

/** Fotografia immutabile delle dimensioni del modello dopo l'addestramento. */
public record ModelSnapshot(int order, int vocabularySize, int learnedContexts, long observedTransitions,
                            List<String> vocabulary) {
}
