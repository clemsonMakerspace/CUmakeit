function checkForUsername(){

  var username = document.getElementsByName("username")[0].value;
  var isUsernameInDB = false;
  var id = getUrlVars()["id"];

  //check if ID is a number or not
  if(isNaN(id)){
     alert("Invalid ID, please scan CUID again");
     window.location.replace("index.html");
     return false;	
   }


  /*THIS NEEDS TO BE FIXED TO CALL THE API BELOW AND GET THE TRUE/FALSE RESPONSE IT'S NOT WORKING FOR SOME REASON*/

  /******Format JSON******/

  let jsonobj =
    '{"Username": "' + username + '"}';

  alert(jsonobj);



  const postUrl = "https://6b78hpx6rd.execute-api.us-east-2.amazonaws.com/default/isUsernameInDatabase";



  fetch(postUrl, {
    method: "POST",
    body: jsonobj
  }).then(res => {
    alert("Request complete! response:", res);
  }).catch(res => {
    alert("Request failed. response:", res);
  });


  /*SET THE isUsernameInDB VARIABLE TO THE API RESPONSE*/
  if(isUsernameInDB === true){
     window.location.replace("welcome.html?id=" + id);
    } else {
     window.location.replace("usernameUnknown.html?id=" + id);
    }


return false;

}


//parse url parameters to forward ID
function getUrlVars() {
    var vars = {};
    var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value;
    });
    return vars;
}

