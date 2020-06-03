package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.ini4j.Ini;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class CurrentStudents_First implements RequestStreamHandler {

  public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
    outputStream.write(getCurrentStudents(context, inputStream).getBytes());
  }

  public String getCurrentStudents(Context context, InputStream inputStream) throws IOException {
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

    ArrayList<String> queryArray = new ArrayList<String>();
    String startArray = "[";
    String endArray = "]";
    queryArray.add(startArray);
    // Get time from DB server
    try {
      Ini config = new Ini(new File("../../../../../../config.ini"));
      String url = config.get("aws_endpoints", "DBConnection");
      String username = config.get("database", "dbUsername");
      String password = config.get("database", "dbPassword");

      Connection conn = DriverManager.getConnection(url, username, password);
      Statement stmt = conn.createStatement();
      ResultSet resultSet = stmt.executeQuery("SELECT Distinct CS.LID, CS.First_Name, CS.Last_Name, CS.Major, CS.Year FROM Clemson_Student as CS WHERE CS.LID in\n" +
              "(SELECT LH.RelatedUser FROM Login_History as LH INNER JOIN Locations as L ON LH.LogLocation WHERE LocationName = '" + input + "' AND LogoutTime is null) ORDER BY First_Name ASC");

      /*
      while (resultSet.next()){

        query_result += resultSet.getInt(1) + " ";
        query_result += resultSet.getString(2) + " ";
        query_result += resultSet.getString(3) + " ";
        query_result += resultSet.getString(4) + " ";
      }
       */

      //start to prepare the return as a JSON array of objects. EXAMPLE:
      /*
      [
      {},
      {}
      ]
       */
      int k = 0;

      while(resultSet.next()){
        String ret = "{ ";
        //ret += "\"UserID\"" + resultSet.getInt(1) + " ";
        ret += "\"FirstName\"" + ":" + "\"" + resultSet.getString(2) + "\", ";
        ret += "\"LastName\"" + ":" + "\"" + resultSet.getString(3) + "\", ";
        ret += "\"ID\"" + ":" + "\"" + resultSet.getInt(1) + "\" ";
        ret += "},";
        queryArray.add(ret);
      }
      //needed in case of empty string (IE index -1)
      try
      {
        //Remove the last element's tailing ","
        String eTemp = queryArray.get(queryArray.size() - 1);
        String eNew = eTemp.substring(0, eTemp.length()-1);

        // Calculate index of last element in array list
        int index = queryArray.size() - 1;
        // Delete last element by passing index
        queryArray.remove(index);
        //Add new last element, now missing ","
        queryArray.add(eNew);
        //add ending to JSON array
        queryArray.add(endArray);

      } catch (Exception e) {
        e.printStackTrace();
        logger.log("Caught exception: " + e.getMessage());
      }

      //make the array into a single return string

    } catch (Exception e) {
      e.printStackTrace();
      logger.log("Caught exception: " + e.getMessage());
    }
    /*
    logger.log(" Successfully executed query.  Result: " + query_result);
    query_result += input;
    //query_result += "  " + json.toString();
    return query_result;
    */

    //return the string made from above - IE a element formatted as
    // [ {}, {}, {}, ....... {} ]
    //which is a JSON array of objects
  }

}


