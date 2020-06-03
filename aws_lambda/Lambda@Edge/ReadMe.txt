The stack from 
https://aws.amazon.com/blogs/networking-and-content-delivery/authorizationedge-how-to-use-lambdaedge-and-json-web-tokens-to-enhance-web-application-security/
failed to work, so I worked on editing the failiures within it

1. In the lambdaedge file, there was and error with an outdated nodejs reference.
   Line 178

2. NOTE: to use these in cloudformation, they need to be hosted publically
         in a S3 bucket, and the primary file needs to have the corresponding
         URL references changed. Currently, only the lambda at edge file
         was broken, so the other references remain intact.

3. Tests to come with sprint 6 for user pool to test working implementation, next
   connecting to RDS via cognito lambda functions. 
