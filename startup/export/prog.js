var singleNext=navigator.userAgent.indexOf("MSIE")>-1 || navigator.userAgent.indexOf("Mozilla/4")>-1 || navigator.userAgent.indexOf("Opera")>-1	;

function nextElem(elem) {  
	if (elem==null)
		return null;
	var nextElem = null;
	if (singleNext) 
		nextElem = elem.nextSibling; 
	else  
		nextElem = elem.nextSibling.nextSibling;
	return nextElem;
}  

function firstElem() {
	return document.getElementById("f1");
}

function isElemSelected(elem) {
	return elem.style.fontWeight == "bold";
}

function elemId(elem) {
	return elem.id.substring(1);
}

function init() {  
	var elem = firstElem(); 
	while (elem != null) {
		elem.onclick = handleKlik;
		var child = elem.firstChild;
		while (child) {
			child.onmouseover = handleMouseOver;
			child.onmouseout = handleMouseOut;
			child = child.nextSibling;
		}
		elem = nextElem(elem); 
	}
}  

function handleMouseOver() {
	this.parentNode.style.backgroundColor = "#d7e18a";
}

function handleMouseOut() {
	var parent = this.parentNode;
	if (isElemSelected(parent)) {
		parent.style.backgroundColor = "lightblue";
	}
	else { 
		var currId = parent.id.substring(1);
		if (currId%2==0)  
			parent.style.backgroundColor = "#E5EFC4";
		else  
			parent.style.backgroundColor = "#FFFFFF";
	}
}

function handleKlik() {
	if (!isElemSelected(this)) { 
		this.style.backgroundColor = "lightblue";
		this.style.fontWeight="bold"; 
	}  
	else { 
		var currId = this.id.substring(1);
		if (currId%2==0)  
			this.style.backgroundColor = "#E5EFC4";
		else  
			this.style.backgroundColor = "#FFFFFF";
		this.style.fontWeight="normal"; 
	}  
	refreshChoices();
}  

function refreshChoices() {  
	var text = ""; 
	var elem = firstElem(); 
	while (elem != null) {
		if (isElemSelected(elem)) {  
			var sel = elem.firstChild.innerHTML + " - [" + elem.firstChild.nextSibling.innerHTML + "]"; 
			text = text + sel + '\n';
		} 
		elem = nextElem(elem); 
	}  
	var selectionWrapper = document.getElementById("selectionWrapper"); 
	document.getElementById("selectionTarget").value = text;
	if (text.length==0 && selectionWrapper.style.display.indexOf("block")>-1)
		selectionWrapper.style.display = "none"; 
	else if (text.length >0 && selectionWrapper.style.display.indexOf("none")>-1)
		selectionWrapper.style.display = "block";
}  

function resetujListu() {  
	var elem = firstElem();
	while (elem != null) {
		if (elemId(elem)%2==0)  
			elem.style.backgroundColor = "#E5EFC4";
		else  
			elem.style.backgroundColor = "#FFFFFF";
		elem.style.fontWeight="normal";
		elem = nextElem(elem); 
	}  
	refreshChoices();
}