package registeruser;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import registeruser.model.UserCredentials;

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
        logger.info("Register user endpoint");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            String requestBody = input.getBody();
            UserCredentials userCredentials = gson.fromJson(requestBody, UserCredentials.class);
            Table table = dynamoDB.getTable("users");

            logger.info("Trying to add new user");
            PutItemOutcome outcome = table.putItem(new Item()
                    .withPrimaryKey("email", userCredentials.getEmail(),
                            "password", userCredentials.getPassword()));
            logger.info("successfully added user, outcome = " + outcome);
            return new APIGatewayProxyResponseEvent().withStatusCode(204);
        } catch (JsonSyntaxException exception) {
            logger.warn("failed parsing user credentials");
            return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("failed parsing user credentials");
        } catch (Exception exception) {
            logger.warn("failed adding user credentials, error = ", exception);
            return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("failed adding user");
        }
    }
}