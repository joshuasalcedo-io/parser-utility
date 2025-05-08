package io.joshuasalcedo.ai.config;

import io.joshuasalcedo.ai.domain.CodeReview;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class ChatController {

    private final OllamaChatModel chatModel;

    @Autowired
    public ChatController(OllamaChatModel chatModel) {
        this.chatModel = chatModel;

    }

    @GetMapping("/ai/generate")
    public Map<String,String> generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return Map.of("generation", this.chatModel.call(message));
    }

    @GetMapping("/ai/generateStream")
	public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return this.chatModel.stream(prompt);
    }

    @GetMapping("/ai/code-review")
    public Flux<ChatResponse> codeReview(@RequestParam(value = "message", defaultValue = """
              @GetMapping("/ai/generateStream")
            	public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
                    Prompt prompt = new Prompt(new UserMessage(message));
                    return this.chatModel.stream(prompt);
                }
            """) String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return this.chatModel.stream(prompt);
    }

    private Flux<CodeReview> generateACodeReviewStreaming(String code) {
        // Create a converter for the CodeReview type
        var converter = new BeanOutputConverter<>(new ParameterizedTypeReference<CodeReview>() {});

        // Create a message with the format template
        UserMessage userMessage = new UserMessage(
                "Please provide a code review for the following code:\n\n" +
                        code +
                        "\n\n{format}",
                null,
                Map.of("format", converter.getFormat())
        );

        // Stream the response
        Flux<String> flux = chatModel.stream(
                new Prompt(
                        userMessage,
                        OllamaOptions.builder()
                                .model(OllamaModel.CODELLAMA)
                                .temperature(0.2)
                                .build()
                )).map(response -> String.valueOf(response.getResult().getOutput()));

        // Collect the streamed content and convert to CodeReview
        return flux.collectList()
                .map(chunks -> String.join("", chunks))
                .map(converter::convert)
                .flatMapMany(Flux::just); // Properly convert Mono<CodeReview> to Flux<CodeReview>
    }


}