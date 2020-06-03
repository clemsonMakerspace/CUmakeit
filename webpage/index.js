
function nextPage(){

var inputList = document.getElementsByName('input');
var input;
var id = getUrlVars()["id"];

//get radio button input
for (var i = 0, length = inputList.length; i < length; i++) {
  if (inputList[i].checked) {
     input = inputList[i].value;
     break;
  }
}

//change screens based on the button pressed
if(input === "yes"){
   window.location.replace("enterUsername.html?id=" + id);
  } else {
   window.location.replace("registration.html?id=" + id);

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
