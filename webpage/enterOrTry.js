
function radioInput(){

var inputList = document.getElementsByName('input');
var input;
var id = getUrlVars()["id"];

  //check if ID is a number or not
  if(isNaN(id)){
     alert("Invalid ID, please scan CUID again");
     window.location.replace("index.html");
     return false;	
   }


for (var i = 0, length = inputList.length; i < length; i++) {
  if (inputList[i].checked) {
     input = inputList[i].value;
     break;
  }
}


if(input === "reenter"){
   window.location.replace("enterUsername.html?id=" + id);
  } else {
   window.location.replace("registration.html?id=" + id);

  }

return false;

}


function getUrlVars() {
    var vars = {};
    var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value;
    });
    return vars;
}
