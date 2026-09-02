package dev.gbove73.tinylanguagelab.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * Modello linguistico n-grammi a caratteri, mantenuto volutamente piccolo e ispezionabile.
 *
 * <p>L'ordine indica quanti token precedenti costituiscono il contesto. Con ordine 2,
 * per esempio, dal testo "casa" impariamo che dopo "ca" arriva "s".</p>
 */
public final class NGramLanguageModel {

    private static final double LAPLACE_SMOOTHING = 1.0;

    private final int order;
    private final List<String> vocabulary;
    private final Map<String, Map<String, Long>> transitionCounts;
    private final long observedTransitions;

    private NGramLanguageModel(int order, List<String> vocabulary,
                               Map<String, Map<String, Long>> transitionCounts,
                               long observedTransitions) {
        this.order = order;
        this.vocabulary = List.copyOf(vocabulary);
        this.transitionCounts = Map.copyOf(transitionCounts);
        this.observedTransitions = observedTransitions;
    }

    /** Addestra un nuovo modello contando ogni coppia contesto-token presente nel corpus. */
    public static NGramLanguageModel train(String corpus, int order) {
        Objects.requireNonNull(corpus, "Il corpus non può essere null");
        if (order < 1 || order > 8) {
            throw new IllegalArgumentException("L'ordine deve essere compreso tra 1 e 8");
        }

        CharacterTokenizer tokenizer = new CharacterTokenizer();
        List<String> tokens = tokenizer.tokenize(corpus);
        if (tokens.size() <= order) {
            throw new IllegalArgumentException("Il corpus deve contenere più token dell'ordine scelto");
        }

        // LinkedHashSet conserva l'ordine di prima apparizione: utile per risultati riproducibili.
        List<String> vocabulary = new ArrayList<>(new LinkedHashSet<>(tokens));
        Map<String, Map<String, Long>> counts = new HashMap<>();

        for (int tokenIndex = order; tokenIndex < tokens.size(); tokenIndex++) {
            String context = join(tokens.subList(tokenIndex - order, tokenIndex));
            String nextToken = tokens.get(tokenIndex);
            counts.computeIfAbsent(context, ignored -> new HashMap<>())
                    .merge(nextToken, 1L, Long::sum);
        }

        return new NGramLanguageModel(order, vocabulary, counts, tokens.size() - order);
    }

    /** Restituisce la distribuzione completa, ordinata dalla continuazione più probabile. */
    public List<TokenProbability> predict(String prompt) {
        Objects.requireNonNull(prompt, "Il prompt non può essere null");
        String context = extractContext(prompt);
        Map<String, Long> counts = transitionCounts.getOrDefault(context, Map.of());
        long totalCount = counts.values().stream().mapToLong(Long::longValue).sum();
        double denominator = totalCount + LAPLACE_SMOOTHING * vocabulary.size();

        return vocabulary.stream()
                .map(token -> {
                    long observedCount = counts.getOrDefault(token, 0L);
                    double probability = (observedCount + LAPLACE_SMOOTHING) / denominator;
                    return new TokenProbability(token, probability, observedCount);
                })
                .sorted(Comparator.comparingDouble(TokenProbability::probability).reversed()
                        .thenComparing(TokenProbability::token))
                .toList();
    }

    /**
     * Genera nuovi token uno alla volta. La temperatura controlla quanto la scelta è prudente:
     * valori bassi premiano i token probabili, valori alti rendono l'uscita più varia.
     */
    public GenerationResult generate(String prompt, int maxNewTokens, double temperature,
                                     RandomGenerator randomGenerator) {
        if (maxNewTokens < 1 || maxNewTokens > 500) {
            throw new IllegalArgumentException("I nuovi token devono essere compresi tra 1 e 500");
        }
        if (!Double.isFinite(temperature) || temperature < 0.1 || temperature > 2.0) {
            throw new IllegalArgumentException("La temperatura deve essere compresa tra 0.1 e 2.0");
        }

        StringBuilder generatedText = new StringBuilder(prompt);
        List<GenerationResult.GenerationStep> steps = new ArrayList<>();

        for (int position = 0; position < maxNewTokens; position++) {
            String context = extractContext(generatedText.toString());
            List<TokenProbability> adjustedDistribution = applyTemperature(predict(generatedText.toString()), temperature);
            TokenProbability selected = sample(adjustedDistribution, randomGenerator.nextDouble());
            generatedText.append(selected.token());
            steps.add(new GenerationResult.GenerationStep(position + 1, context, selected.token(), selected.probability()));
        }

        return new GenerationResult(generatedText.toString(), List.copyOf(steps));
    }

    public ModelSnapshot snapshot() {
        return new ModelSnapshot(order, vocabulary.size(), transitionCounts.size(), observedTransitions, vocabulary);
    }

    public String extractContext(String text) {
        List<String> tokens = new CharacterTokenizer().tokenize(text);
        int startIndex = Math.max(0, tokens.size() - order);
        return join(tokens.subList(startIndex, tokens.size()));
    }

    private List<TokenProbability> applyTemperature(List<TokenProbability> distribution, double temperature) {
        List<Double> weights = distribution.stream()
                .map(item -> Math.pow(item.probability(), 1.0 / temperature))
                .toList();
        double totalWeight = weights.stream().mapToDouble(Double::doubleValue).sum();

        List<TokenProbability> adjusted = new ArrayList<>();
        for (int index = 0; index < distribution.size(); index++) {
            TokenProbability original = distribution.get(index);
            adjusted.add(new TokenProbability(original.token(), weights.get(index) / totalWeight,
                    original.observedCount()));
        }
        return adjusted;
    }

    private TokenProbability sample(List<TokenProbability> distribution, double randomValue) {
        double cumulativeProbability = 0.0;
        for (TokenProbability candidate : distribution) {
            cumulativeProbability += candidate.probability();
            if (randomValue < cumulativeProbability) {
                return candidate;
            }
        }
        // Gli arrotondamenti floating point possono lasciare una minuscola distanza da 1.
        return distribution.getLast();
    }

    private static String join(List<String> tokens) {
        return String.join("", tokens);
    }
}
