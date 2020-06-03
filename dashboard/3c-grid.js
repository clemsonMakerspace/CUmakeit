// (A) RAW JSON DATA
/* ORIGINAL jsondata
var jsondata = '[{"Name":"John Doe","Email":"john@doe.com","Gender":"male"},'
+ '{"Name":"Jane Doe","Email":"jane@doe.com","Gender":"female"},'
+ '{"Name":"Joseph Doe","Email":"joseph@doe.com","Gender":"male"},'
+ '{"Name":"Joanne Doe","Email":"joanne@doe.com","Gender":"female"}]';
*/
//Test , "ID":"112232"

//HERE IS WHERE YOU CALL THE API GATEWAY TO GET THIS DATA
var jsondata = '[{"FirstName": "Samuel","LastName": "Brooks","Username": "scbrook"},'
+ '{"FirstName": "Robert","LastName": "Lantham","Username": "rlanth"},'
+ '{"FirstName": "KMUNEY","LastName": "Maker","Username": "mmman"},'
+ '{"FirstName": "Fearless","LastName": "Leader","Username": "ffleaed"}]';

// (B) PARSE JSON INTO OBJECT
var parsed = JSON.parse(jsondata);
console.table(parsed);

// (C) TABLE HEADER
var theWrap = document.getElementById("tableWrap");
var theCell = null;
for (let key in parsed[0]) {
  theCell = document.createElement("div");
  theCell.innerHTML = key;
  theCell.classList.add("cell");
  theCell.classList.add("head");
  theWrap.appendChild(theCell);
}

// (D) TABLE CELLS
var thePerson = null;
var altRow = false;
for (let key in parsed) {
  thePerson = parsed[key];
  for (let i in thePerson) {
    theCell = document.createElement("div");
    theCell.innerHTML = thePerson[i];
    theCell.classList.add("cell");
    if (altRow) {
      theCell.classList.add("alt");
    }
    theWrap.appendChild(theCell);
  }
  altRow = !altRow;
}
