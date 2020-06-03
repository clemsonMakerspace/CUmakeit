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

    //extract body parameters
    String location = (String)event.get("Location");
    String id = (String)event.get("ID");
    String type = (String)event.get("Event");

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
      int studentid = 0;
      if(resultSet.next()){
        studentid = resultSet.getInt(1);
      }
      //if no, then return false
      if (!resultSet.first()){
        return "False";
      }

      //if yes, then insert into login_history based on type
      else {
         //inserts sign in time
          if (type.equals("SignIn")) {
              ResultSet resultSet2 = stmt.executeQuery("SELECT LOID From Locations WHERE LocationName = '" + location + "'");
              int locationid = 0;
              if (resultSet2.next()) {
                  locationid = resultSet2.getInt(1);
              }
              stmt.executeUpdate("INSERT INTO Login_History(LoginTime, RelatedUser, LogLocation)\n" +
                      "Values (CURRENT_TIMESTAMP , " + studentid + "," + locationid + ");");
          }
          //inserts sign out time
          else {
              stmt.executeUpdate("UPDATE Login_History SET LogoutTime = CURRENT_TIMESTAMP WHERE RelatedUser = " + studentid);
          }
      }
    } catch (Exception e) {
        e.printStackTrace();
        logger.log("Caught exception: " + e.getMessage());
        return e.getMessage();
    }
      logger.log(" Successfully executed query. ");
    return "Success";

  }

}


