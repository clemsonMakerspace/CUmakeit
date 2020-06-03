package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.transform.Result;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Totals implements RequestStreamHandler {

  public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
    outputStream.write(getTotals(context, inputStream).getBytes());
  }

  public String getTotals(Context context, InputStream inputStream) throws IOException {
    //get json value in a usable format
    final ObjectMapper objectMapper = new ObjectMapper();
    JsonNode json = objectMapper.readTree(inputStream);
    String input = json.toString().substring(8, json.toString().length() - 1);
    int target = input.indexOf('\"');
    input = input.substring(0, target);

    LambdaLogger logger = context.getLogger();
    logger.log("Current input value: " + input);
    logger.log("Current target value: " + target);
    logger.log("Invoked JDBCSample.getCurrentTime ");
    String query_result = "";
    // Get time from DB server
    try {
      // using Ini4j to parse our configuration file to remove sensitive information
      Ini config = new Ini(new File("../../../../../../config.ini"));
      String url = config.get("aws_endpoints", "DBConnection");
      String username = config.get("database", "dbUsername");
      String password = config.get("database", "dbPassword");

      Connection conn = DriverManager.getConnection(url, username, password);
      Statement stmt = conn.createStatement();
      ResultSet resultSet; //TODO: Remove this line?? = stmt.executeQuery("SELECT * FROM Clemson_Student where year = 'Owen'");
      if (input.equals("major")){
        resultSet = stmt.executeQuery("SELECT COUNT(major) major_count, major\n" +
                "\tFROM Clemson_Student \t\n" +
                "\tGROUP BY major\n" +
                "\tORDER BY major_count DESC;");
      }
      else if (input.equals("year")){
        resultSet = stmt.executeQuery("SELECT COUNT(year) year_count, year\n" +
                "\tFROM Clemson_Student \t\n" +
                "\tGROUP BY year\n" +
                "\tORDER BY year_count DESC;");
      }
      else if (input.equals("college")){
        resultSet = stmt.executeQuery("SELECT COUNT(college) college_count, college\n" +
                "\tFROM Clemson_Student \t\n" +
                "\tGROUP BY college\n" +
                "\tORDER BY college_count DESC;");
      }


      while (resultSet.next()){
        query_result += resultSet.getInt(1) + " ";
        query_result += resultSet.getString(2) + " ";
      }

      logger.log(" Successfully executed query.  Result: " + query_result);

    } catch (Exception e) {
      e.printStackTrace();
      logger.log("Caught exception: " + e.getMessage());
    }

    return query_result;

  }

}


