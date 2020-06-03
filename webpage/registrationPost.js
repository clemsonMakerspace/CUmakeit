function validateInput() {

  /******Get User Input******/

  var firstName = document.getElementsByName("firstname")[0].value;
  var lastName = document.getElementsByName("lastname")[0].value;
  var username = document.getElementsByName("username")[0].value;
  var major = document.getElementsByName("major")[0].value;
  var grade = document.getElementsByName("grade")[0].value;
  var id = getUrlVars()["id"];

  //check if ID is a number or not
  if(isNaN(id)){
     alert("Invalid ID, please scan CUID again");
     window.location.replace("index.html");
     return false;	
   }

  /******Verify User Input Not Blank******/

  if (document.getElementsByName("firstname")[0].value === "") {
    alert("Please enter your first name");
    return false;
  }

  if (document.getElementsByName("lastname")[0].value === "") {
    alert("Please enter your last name");
    return false;
  }
  if (document.getElementsByName("username")[0].value === "") {
    alert("Please enter your Clemson username");
    return false;
  }
  if (document.getElementsByName("major")[0].value === "") {
    alert("Please enter your major from the list");
    return false;
  }
  if (document.getElementsByName("grade")[0].value === "") {
    alert("Please enter your grade from the list");
    return false;
  }


  /******Format JSON******/

  let jsonobj =
    '{"FirstName": "' + firstName + '",' +
      '"LastName": "' + lastName + '",' +
      '"Username": "' + username + '",' +
      '"Major": "' + major + '",' +
      '"Year": "' + grade + '",' +
      '"ID": "' + id + '"}';

  //alert(jsonobj);


  /******HTTP Post Request******/

  const postUrl = "https://qv9ld1gdk0.execute-api.us-east-1.amazonaws.com/default/insertNewStudent";


  fetch(postUrl, {
    method: "POST",
    body: jsonobj
  }).then(res => {
    //alert("Request complete! response:", res);
  }).catch(res => {
    //alert("Request failed. response:", res);
  });



  /******Check if user is in DB******/

  var inDB = true;

  if(inDB === true){
     window.location.replace("welcome.html");
  } else {

  }
  return false;

}






/******Gets Parameters from URL******/
function getUrlVars() {
    var vars = {};
    var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value;
    });
    return vars;
}


