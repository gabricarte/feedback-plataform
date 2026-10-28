package techchallenge.feedback.core.repository;

import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import techchallenge.feedback.core.config.Environment;
import techchallenge.feedback.core.domain.Feedback;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DynamoDbFeedbackRepository implements FeedbackRepository {

    private static final String TABLE_NAME_VARIABLE = "FEEDBACK_TABLE_NAME";

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoDbFeedbackRepository(DynamoDbClient dynamoDbClient, String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    public static DynamoDbFeedbackRepository fromEnvironment() {
        DynamoDbClient client = DynamoDbClient.builder()
                .httpClient(UrlConnectionHttpClient.create())
                .build();
        return new DynamoDbFeedbackRepository(client, Environment.require(TABLE_NAME_VARIABLE));
    }

    @Override
    public void save(Feedback feedback) {
        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(FeedbackItemMapper.toItem(feedback))
                .conditionExpression("attribute_not_exists(#id)")
                .expressionAttributeNames(Map.of("#id", FeedbackItemMapper.ID))
                .build();

        dynamoDbClient.putItem(request);
    }

    @Override
    public List<Feedback> findSentSince(Instant since) {
        ScanRequest request = ScanRequest.builder()
                .tableName(tableName)
                .filterExpression("#sentAt >= :since")
                .expressionAttributeNames(Map.of("#sentAt", FeedbackItemMapper.SENT_AT))
                .expressionAttributeValues(Map.of(":since", AttributeValue.fromS(since.toString())))
                .build();

        return dynamoDbClient.scanPaginator(request).items().stream()
                .map(FeedbackItemMapper::fromItem)
                .sorted(Comparator.comparing(Feedback::sentAt))
                .toList();
    }
}
