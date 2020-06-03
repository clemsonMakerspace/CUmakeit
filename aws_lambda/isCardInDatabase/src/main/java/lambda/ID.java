package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.lambda.runtime.RequestHandler;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;

public class ID implements RequestHandler<Map<String,Object>, String> {

  public String handleRequest(Map<String,Object> event, Context context){
      try {
          return insertID(context, event);
      } catch (IOException e) {
          return e.getMessage();
      }
  }

  public String insertID(Context context, Map<String,Object> event) throws IOException {

    //prep logger
    LambdaLogger logger = context.getLogger();
    String id = (String)event.get("ID");

    try {
      // using Ini4j to parse our configuration file to remove sensitive information
      Ini config = new Ini(new File("../../../../../../config.ini"));
      String url = config.get("aws_endpoints", "DBConnection");
      String username = config.get("database", "dbUsername");
      String password = config.get("database", "dbPassword");

      Connection conn = DriverManager.getConnection(url, username, password);
      Statement stmt = conn.createStatement();

      //check to see if id already exists in database
      ResultSet resultSet = stmt.executeQuery("SELECT * From Student_ID WHERE SID = " + id);
      //if no, then return false
      if (!resultSet.first()){
          logger.log(" Successfully executed query. ");
          return "False";
    }
      //if yes, then return true
     else {
          logger.log(" Successfully executed query. ");
          return "True";
      }

    } catch (Exception e) {
        e.printStackTrace();
        logger.log("Caught exception: " + e.getMessage());
        return e.getMessage();
    }
  }

}


