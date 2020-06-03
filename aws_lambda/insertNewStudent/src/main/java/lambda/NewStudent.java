package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
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
    String college = "";

    //prep logger
    LambdaLogger logger = context.getLogger();

    ObjectMapper oMapper = new ObjectMapper();

    //parse all values in the json values
    logger.log("First test\n");
    Map <String, Object> params = new HashMap<String, Object>();
    for (Map.Entry<String, Object> entry: event.entrySet()){
      String key = entry.getKey();
      logger.log(key  + "\n");
      if (key.equals("body")) {
        Object value = entry.getValue();
        params = oMapper.readValue((String) entry.getValue(), new TypeReference<Map<String, Object>>(){});
      }
    }
    String fName = (String)params.get("FirstName");
    logger.log("FirstName:" + fName);
    String lName = (String)params.get("LastName");
    logger.log("LastName:" + lName);
    String user = (String)params.get("Username");
    logger.log("Username:" + user);
    String year = (String)params.get("Year");
    logger.log("Year:" + year);
    String id = (String)params.get("ID");
    logger.log("ID:" + id);
    String major = (String)params.get("Major");

        /* Inputs:
           1. First Name
           2. Last Name
           3. Clemson Username
           4. Major
           5. Year
           6. Id
         */

    try {
      // using Ini4j to parse our configuration file to remove sensitive information
      Ini config = new Ini(new File("../../../../../../config.ini"));
      String url = config.get("aws_endpoints", "DBConnection");
      String username = config.get("database", "dbUsername");
      String password = config.get("database", "dbPassword");

      Connection conn = DriverManager.getConnection(url, username, password);
      Statement stmt = conn.createStatement();

      //check to see if email already exists in database
      ResultSet resultSet = stmt.executeQuery("SELECT * From Makerspace_User WHERE Email = '" + user + "@clemson.edu'");
      int cid = 0;
      if (resultSet.next() == true) {
        cid = resultSet.getInt(0);
      }
        //if no, then insert all info into Makerspace_User, Student_ID, and Clemson_Student
        if (!resultSet.first()) {

          stmt.executeUpdate("INSERT INTO Makerspace_User(First_Name, Email, AccessLevel, Last_Name)\n" +
                  "Values('" + fName + "','" + user + "@clemson.edu', 1, '" + lName + "');");

          //get the lid of the newly inserted student to be used in the other inserstions
          ResultSet resultSet2 = stmt.executeQuery("SELECT LID From Makerspace_User WHERE Email = '" + user + "@clemson.edu'");
          int lid = 0;
          if (resultSet2.next()) {
            lid = resultSet2.getInt(1);
          }

          //inserts into Clemson_Student
          resultSet = stmt.executeQuery("SELECT * FROM Majors WHERE Major = '" + major + "'");
          if (resultSet.next()) {
            college = resultSet.getString("College");
          }
          stmt.executeUpdate("INSERT INTO Clemson_Student(LID, Major, Year, First_Name, Last_Name, College)\n" +
                  "Values (" + lid + ",'" + major + "', '" + year + "', '" + fName + "', '" + lName + "', ' " + college + "');");

          //inserts into Student_ID
          stmt.executeUpdate("INSERT INTO Student_ID(LID, SID)\n" +
                  "Values (" + lid + "," + id + ");");

        }


      //if yes, then only insert id into Student_ID
      else {
        stmt.executeUpdate("INSERT INTO Student_ID(LID, SID) Values(\n" +
                cid + ", " + id + ")");
      }
      logger.log(" Successfully executed query. ");

    } catch (Exception e) {
      e.printStackTrace();
      logger.log("Caught exception: " + e.getMessage());
      return formResponse(501, e.getMessage());
    }

    return formResponse(200, "Success");
  }
}
