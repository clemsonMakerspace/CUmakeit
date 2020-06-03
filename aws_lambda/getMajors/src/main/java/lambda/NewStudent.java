package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;


public class NewStudent implements RequestHandler<Map<String,Object>, APIGatewayProxyResponseEvent> {


  public APIGatewayProxyResponseEvent formResponse(int statusCode, String body){
    APIGatewayProxyResponseEvent apiResponse = new APIGatewayProxyResponseEvent();
    apiResponse.setStatusCode(statusCode);
    apiResponse.setHeaders(Collections.singletonMap("Access-Control-Allow-Origin", "*"));
    apiResponse.setBody(body);
    return apiResponse;
  }

  public APIGatewayProxyResponseEvent handleRequest(Map<String,Object> event, Context context){

    APIGatewayProxyResponseEvent apiResponse = null;
    try {
      apiResponse = insertNewStudent(context, event);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return apiResponse;
  }

  public APIGatewayProxyResponseEvent insertNewStudent(Context context, Map<String,Object> event) throws IOException {

    //prep logger
    LambdaLogger logger = context.getLogger();

    String retVal = "";

    try {
      // using Ini4j to parse our configuration file to remove sensitive information
      Ini config = new Ini(new File("../../../../../../config.ini"));
      String url = config.get("aws_endpoints", "DBConnection");
      String username = config.get("database", "dbUsername");
      String password = config.get("database", "dbPassword");

      Connection conn = DriverManager.getConnection(url, username, password);
      Statement stmt = conn.createStatement();

      //retrieve list of majors and degree type
      ResultSet resultSet = stmt.executeQuery("SELECT Major, DegreeType From Majors ORDER BY Major");

      //build string containing all majors and degree types
      while (resultSet.next()) {
        retVal += resultSet.getString("Major") + "\n";
      }
      logger.log(retVal);
      logger.log(" Successfully executed query. ");

    } catch (Exception e) {
      e.printStackTrace();
      logger.log("Caught exception: " + e.getMessage());
      return formResponse(501, e.getMessage());
    }
    return formResponse(200, retVal);
  }
}
