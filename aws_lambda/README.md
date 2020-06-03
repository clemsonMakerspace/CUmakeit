1. Download the code into Intellij, keeping the same file structure 
2. Press Ctrl, Ctrl to bring up the "Run As" menu 
3. Type in "maven package shade:shade" to create jar files
4. Upload "lambda-jdbc-sample-1.0-SNAPSHOT-shaded" to lambda
5. Change execution role to "lambda-vpc-execution-role"
6. Change handler to "*packagename*.*classname*::handleRequest" 
7. Save and create a new test
