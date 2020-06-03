/*
This function is not currently being used. It returns a list of every student currently in the MakerSpace based on the
Login_History table. The output should be formatted differently to fit into a dashboard; the sql statement is accurate.
 */

package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;


public class CurrentStudents implements RequestHandler<Map<String,Object>, APIGatewayProxyResponseEvent> {
  
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
    //set up logger
    LambdaLogger logger = context.getLogger();

    //get input values
    String location = (String)event.get("Location");
    String order = (String)event.get("Order");
    String query_result = "";
    try {
      //using Ini4j to parse our configuration file to remove sensitive information
      Ini config = new Ini(new File("../../../../../../config.ini"));
      String url = config.get("aws_endpoints", "DBConnection");
      String username = config.get("database", "dbUsername");
      String password = config.get("database", "dbPassword");

      Connection conn = DriverManager.getConnection(url, username, password);
      Statement stmt = conn.createStatement();
      ResultSet resultSet = stmt.executeQuery("SELECT Distinct CS.LID, CS.First_Name, CS.Last_Name, CS.Major, CS.Year FROM Clemson_Student as CS WHERE CS.LID in\n" +
              "(SELECT LH.RelatedUser FROM Login_History as LH INNER JOIN Locations as L ON LH.LogLocation WHERE LocationName = '" + location + "' AND LogoutTime is null) ORDER BY " + order +" ASC");

      while (resultSet.next()){
        query_result += resultSet.getInt(1) + " ";
        query_result += resultSet.getString(2) + " ";
        query_result += resultSet.getString(3) + " ";
        query_result += resultSet.getString(4) + "\n";
      }

      logger.log(" Successfully executed query.  Result: " + query_result);

    } catch (Exception e) {
      e.printStackTrace();
      logger.log("Caught exception: " + e.getMessage());
      return formResponse(501, e.getMessage());
    }
    return formResponse(200, query_result);
  }
}

