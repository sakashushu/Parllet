onload = init;
var result;

function init() {
	result = document.getElementById("result");
	calc();
}

function calc() {
	var a = 1;
	var b = 2;
	var c = a + b;
	result.innerHTML = "a + b = " + c;
}

