package checkbalance;

import checkbalance.model.UserId;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpStatus;
import org.junit.Test;
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
        assertEquals(response.getStatusCode().intValue(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void emptyRequestBodyTest() {
        App app = new App();
        APIGatewayProxyResponseEvent response = app.handleRequest(new APIGatewayProxyRequestEvent(), null);
        assertEquals(response.getStatusCode().intValue(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void notFoundTest() {
        String id = "my_id";
        UserId userId = new UserId(id);
        dynamoDB = mock(DynamoDB.class);
        table = mock(Table.class);
        when(dynamoDB.getTable(any())).thenReturn(table);
        when(table.getItem("userId", id)).thenReturn(null);

        Gson gson = new GsonBuilder().create();
        App app = new App(dynamoDB);
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent().withBody(gson.toJson(userId));
        APIGatewayProxyResponseEvent response = app.handleRequest(request, null);
        assertTrue(response.getBody().contains("not found"));
        assertEquals(response.getStatusCode().intValue(), HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void successfulTest() {
        String id = "my_id";
        UserId userId = new UserId(id);
        Item item = new Item().with("balance", 12.5);
        dynamoDB = mock(DynamoDB.class);
        table = mock(Table.class);
        when(dynamoDB.getTable(any())).thenReturn(table);
        when(table.getItem("userId", id)).thenReturn(item);

        Gson gson = new GsonBuilder().create();
        App app = new App(dynamoDB);
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent().withBody(gson.toJson(userId));
        APIGatewayProxyResponseEvent response = app.handleRequest(request, null);
        assertTrue(response.getBody().contains("balance"));
        assertTrue(response.getBody().contains("12.5"));
        assertEquals(response.getStatusCode().intValue(), HttpStatus.SC_OK);
    }
}

