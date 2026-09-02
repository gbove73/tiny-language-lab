package dev.gbove73.tinylanguagelab.api;

import dev.gbove73.tinylanguagelab.model.GenerationResult;
import dev.gbove73.tinylanguagelab.model.ModelSnapshot;
import dev.gbove73.tinylanguagelab.service.LanguageModelService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** API HTTP del laboratorio. I record rendono esplicito il contratto di ogni operazione. */
@RestController
@RequestMapping("/api/model")
public class LanguageModelController {

    private final LanguageModelService modelService;

    public LanguageModelController(LanguageModelService modelService) {
        this.modelService = modelService;
    }

    @GetMapping
    public ModelSnapshot getModel() {
        return modelService.snapshot();
    }

    @PostMapping("/train")
    public ModelSnapshot train(@Valid @RequestBody TrainRequest request) {
        return modelService.train(request.corpus(), request.order());
    }

    @PostMapping("/predict")
    public LanguageModelService.PredictionView predict(@Valid @RequestBody PredictRequest request) {
        return modelService.predict(request.prompt(), request.limit());
    }

    @PostMapping("/generate")
    public GenerationResult generate(@Valid @RequestBody GenerateRequest request) {
        return modelService.generate(request.prompt(), request.maxNewTokens(), request.temperature(), request.seed());
    }

    public record TrainRequest(@NotBlank @Size(max = 100_000) String corpus, @Min(1) @Max(8) int order) {
    }

    public record PredictRequest(@Size(max = 10_000) String prompt, @Min(1) @Max(100) int limit) {
    }

    public record GenerateRequest(@Size(max = 10_000) String prompt,
                                  @Min(1) @Max(500) int maxNewTokens,
                                  @DecimalMin("0.1") @DecimalMax("2.0") double temperature,
                                  long seed) {
    }
}
