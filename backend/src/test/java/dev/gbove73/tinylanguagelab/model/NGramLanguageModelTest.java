package dev.gbove73.tinylanguagelab.model;

import org.junit.jupiter.api.Test;

import java.util.random.RandomGeneratorFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NGramLanguageModelTest {

    @Test
    void learnsObservedTransitionsAndNormalizesProbabilities() {
        NGramLanguageModel model = NGramLanguageModel.train("ababac", 1);

        var predictions = model.predict("a");

        assertThat(predictions.getFirst().token()).isEqualTo("b");
        assertThat(predictions.getFirst().observedCount()).isEqualTo(2);
        double probabilitySum = predictions.stream().mapToDouble(TokenProbability::probability).sum();
        assertThat(probabilitySum).isCloseTo(1.0, within(1e-12));
    }

    @Test
    void generationIsReproducibleWithTheSameSeed() {
        NGramLanguageModel model = NGramLanguageModel.train("banana bandana", 2);
        var firstRandom = RandomGeneratorFactory.of("L64X128MixRandom").create(42);
        var secondRandom = RandomGeneratorFactory.of("L64X128MixRandom").create(42);

        String first = model.generate("ba", 12, 0.7, firstRandom).text();
        String second = model.generate("ba", 12, 0.7, secondRandom).text();

        assertThat(first).isEqualTo(second).hasSize(14);
    }

    @Test
    void rejectsCorpusThatCannotContainATransition() {
        assertThatThrownBy(() -> NGramLanguageModel.train("ab", 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("più token");
    }

    private static org.assertj.core.data.Offset<Double> within(double value) {
        return org.assertj.core.data.Offset.offset(value);
    }
}
