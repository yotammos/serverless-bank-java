package checkbalance;

import checkbalance.model.CheckBalanceResponse;
import checkbalance.model.UserId;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private final DynamoDB dynamoDB;

    public App() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        dynamoDB = new DynamoDB(client);
    }

    public App(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        logger.info("Check balance endpoint");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            String requestBody = input.getBody();
            UserId userId = gson.fromJson(requestBody, UserId.class);
            Table table = dynamoDB.getTable("users");

            logger.info("Trying to get user balance");
            Item item = table.getItem("userId", userId.getUserId());
            if (item == null) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(HttpStatus.SC_NOT_FOUND)
                        .withBody("Item not found");
            }
            double balance = item.getDouble("balance");
            logger.info("successfully got balance, value = " + balance);
            return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatus.SC_OK)
                    .withBody(gson.toJson(new CheckBalanceResponse(balance)));
        } catch (JsonSyntaxException exception) {
            logger.warn("failed parsing user id");
            return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatus.SC_BAD_REQUEST)
                    .withBody("failed parsing user id");
        } catch (Exception exception) {
            logger.warn("failed fetching user balance, error = ", exception);
            return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR).
                    withBody("failed fetching user balance");
        }
    }
}