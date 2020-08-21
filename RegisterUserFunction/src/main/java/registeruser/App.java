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
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import registeruser.model.UserCredentials;
import registeruser.model.UserId;

import java.util.UUID;

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
            String userId = UUID.randomUUID().toString();
            PutItemOutcome outcome = table.putItem(new Item()
                    .withPrimaryKey("email", userCredentials.getEmail())
                    .with("password", userCredentials.getPassword())
                    .with("userId", userId)
                    .with("balance", 0.0)
            );
            logger.info("successfully added user, outcome = " + outcome);
            return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatus.SC_OK)
                    .withBody(gson.toJson(new UserId(userId)));
        } catch (JsonSyntaxException exception) {
            logger.warn("failed parsing user credentials");
            return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatus.SC_BAD_REQUEST)
                    .withBody("failed parsing user credentials");
        } catch (Exception exception) {
            logger.warn("failed adding user credentials, error = ", exception);
            return new APIGatewayProxyResponseEvent().withStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .withBody("failed adding user");
        }
    }
}