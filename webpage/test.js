alert("hello");

  const url = "https://o2hypaqt1k.execute-api.us-east-1.amazonaws.com/default/insertNewStudent";
  var jsonobj = {"FirstName": "tyler21","LastName": "davis21","Username": "tdavis21","Major": "Financial Management","Year": "Freshman","ID": "00900990"}

  fetch(url, {
    method: "POST",
    body: jsonobj
  }).then(res => {
    console.log("Request complete! response:", res);
  }).catch(res => {
    console.log("Request failed. response:", res);
  });