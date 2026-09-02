package dev.gbove73.tinylanguagelab.service;

import dev.gbove73.tinylanguagelab.model.GenerationResult;
import dev.gbove73.tinylanguagelab.model.ModelSnapshot;
import dev.gbove73.tinylanguagelab.model.NGramLanguageModel;
import dev.gbove73.tinylanguagelab.model.TokenProbability;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.atomic.AtomicReference;

/** Coordina l'unica istanza del modello e la sostituisce in modo atomico dopo ogni training. */
@Service
public class LanguageModelService {

    private static final String DEFAULT_CORPUS = "la luna illumina il lago. la luce danza lenta sull'acqua. ";
    private final AtomicReference<NGramLanguageModel> currentModel =
            new AtomicReference<>(NGramLanguageModel.train(DEFAULT_CORPUS, 2));

    public ModelSnapshot train(String corpus, int order) {
        NGramLanguageModel trainedModel = NGramLanguageModel.train(corpus, order);
        currentModel.set(trainedModel);
        return trainedModel.snapshot();
    }

    public PredictionView predict(String prompt, int limit) {
        NGramLanguageModel model = currentModel.get();
        List<TokenProbability> predictions = model.predict(prompt).stream().limit(limit).toList();
        return new PredictionView(model.extractContext(prompt), predictions);
    }

    public GenerationResult generate(String prompt, int maxNewTokens, double temperature, long seed) {
        return currentModel.get().generate(prompt, maxNewTokens, temperature, new SplittableRandom(seed));
    }

    public ModelSnapshot snapshot() {
        return currentModel.get().snapshot();
    }

    public record PredictionView(String context, List<TokenProbability> predictions) {
    }
}
