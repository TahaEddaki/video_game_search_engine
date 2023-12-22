package fr.lernejo.search.api;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.action.index.IndexRequest;
import org.springframework.amqp.core.Message;

@Component
public class GameInfoListener {

    private final RestHighLevelClient restHighLevelClient;

    public GameInfoListener(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    @RabbitListener(queues = AmqpConfiguration.GAME_INFO_QUEUE)
    public void onMessage(Message message) {
        try {
            String gameId = message.getMessageProperties().getHeader("game_id").toString();
            String content = new String(message.getBody());
            IndexRequest indexRequest = new IndexRequest("games")
                .id(gameId)
                .source(content, XContentType.JSON);
            restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            System.out.println("Game info indexed");
        } catch (Exception e) {
            System.out.println("Error while indexing game info");
        }
    }
}
