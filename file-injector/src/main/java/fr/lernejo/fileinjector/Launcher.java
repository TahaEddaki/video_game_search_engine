package fr.lernejo.fileinjector;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
@SpringBootApplication
public class Launcher {
    public static void main(String[] args) {
        SpringApplication.run(Launcher.class, args);
    }
    @Bean
    public CommandLineRunner run(RabbitTemplate rabbitTemplate) {
        return args -> {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                loadAndSendMessages(objectMapper, rabbitTemplate);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.exit(0);
        };
    }
    private void loadAndSendMessages(ObjectMapper objectMapper, RabbitTemplate rabbitTemplate) throws IOException {
        URL resource = getClass().getClassLoader().getResource("games.json");
        if (resource == null) {
            throw new RuntimeException("Le fichier games.json n'a pas été trouvé dans les ressources.");
        }
        File file = new File(resource.getFile());
        JsonNode messages = objectMapper.readTree(file);
        Iterator<JsonNode> iterator = messages.iterator();
        while (iterator.hasNext()) {
            JsonNode message = iterator.next();
            sendMessage(rabbitTemplate, message);
        }
    }
    private void sendMessage(RabbitTemplate rabbitTemplate, JsonNode message) {
        try {
            Message rabbitMessage = MessageBuilder
                .withBody(message.toString().getBytes())
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setHeader("game_id", message.get("id"))
                .build();
            rabbitTemplate.convertAndSend("game_info", rabbitMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
