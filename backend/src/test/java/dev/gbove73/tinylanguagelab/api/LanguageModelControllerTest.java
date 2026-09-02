package dev.gbove73.tinylanguagelab.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LanguageModelControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void trainsAndExposesModelStatistics() throws Exception {
        var request = new LanguageModelController.TrainRequest("uno due uno tre", 2);

        mockMvc.perform(post("/api/model/train")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order").value(2))
                .andExpect(jsonPath("$.vocabularySize").isNumber())
                .andExpect(jsonPath("$.learnedContexts").isNumber());
    }

    @Test
    void rejectsInvalidTrainingOrder() throws Exception {
        var request = new LanguageModelController.TrainRequest("corpus valido", 0);

        mockMvc.perform(post("/api/model/train")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
