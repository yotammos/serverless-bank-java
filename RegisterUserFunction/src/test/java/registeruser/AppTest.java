package registeruser;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;
import registeruser.model.UserCredentials;
import org.mockito.Mock;

public class AppTest {

    @Mock
    private DynamoDB dynamoDB;

    @Mock
    private Table table;

    @Test
    public void nullRequestTest() {
        App app = new App();
        APIGatewayProxyResponseEvent response = app.handleRequest(null, null);
        assertEquals(response.getStatusCode().intValue(), 500);
    }

    @Test
    public void emptyRequestBodyTest() {
        App app = new App();
        APIGatewayProxyResponseEvent response = app.handleRequest(new APIGatewayProxyRequestEvent(), null);
        assertEquals(response.getStatusCode().intValue(), 500);
    }

    @Test
    public void successfulTest() {
        UserCredentials userCredentials = new UserCredentials("a@b.com", "password");
        dynamoDB = mock(DynamoDB.class);
        table = mock(Table.class);
        when(dynamoDB.getTable(any())).thenReturn(table);

        Gson gson = new GsonBuilder().create();
        App app = new App(dynamoDB);
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent().withBody(gson.toJson(userCredentials));
        APIGatewayProxyResponseEvent response = app.handleRequest(request, null);
        assertTrue(response.getBody().contains("userId"));
        assertEquals(response.getStatusCode().intValue(), 200);
    }
}
