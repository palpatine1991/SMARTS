activeAliphaticType = [];
activeAromaticType = [];
activeType = "aliphatic"; //..,aromatic,both

function init(){
	document.getElementById("aliphatic").style.backgroundColor = "#fd3c43";
	if(openAtom){
		initPeriodicTable();
	}
}

function initPeriodicTable(){
	//init aliphatic
	for(var i = 0; i < openAtom.possibleAliphatic.length; i++){
		turnAliphatic(document.getElementById(openAtom.possibleAliphatic[i]));
	}
	//init aromatic
	for(var i = 0; i < openAtom.possibleAromatic.length; i++){
		var button = document.getElementById(openAtom.possibleAromatic[i]);
		if(button.style.borderColor == "rgb(253, 60, 67)"){
			turnBoth(button);
		}
		else{
			turnAromatic(button);
		}
	}	
}

function turnAliphatic(dom){
	var index = activeAromaticType.indexOf(dom.innerHTML);
	if(index != -1){
		activeAromaticType.splice(index,1);
	}
	index = activeAliphaticType.indexOf(dom.innerHTML);
	if(index == -1){
		activeAliphaticType.push(dom.innerHTML);
	}
	dom.style.borderColor = "#fd3c43";
	dom.style.borderWidth = "4px";
}

function turnAromatic(dom){
	var index = activeAliphaticType.indexOf(dom.innerHTML);
	if(index != -1){
		activeAliphaticType.splice(index,1);
	}
	index = activeAromaticType.indexOf(dom.innerHTML);
	if(index == -1){
		activeAromaticType.push(dom.innerHTML);
	}
	dom.style.borderColor = "#0CC";
	dom.style.borderWidth = "4px";
}

function turnBoth(dom){
	var index = activeAliphaticType.indexOf(dom.innerHTML);
	if(index == -1){
		activeAliphaticType.push(dom.innerHTML);
	}
	index = activeAromaticType.indexOf(dom.innerHTML);
	if(index == -1){
		activeAromaticType.push(dom.innerHTML);
	}
	dom.style.borderColor = "#C0F";
	dom.style.borderWidth = "4px";
}

function turnUnmarked(dom){
	var index = activeAliphaticType.indexOf(dom.innerHTML);
	if(index != -1){
		activeAliphaticType.splice(index,1);
	}
	index = activeAromaticType.indexOf(dom.innerHTML);
	if(index != -1){
		activeAromaticType.splice(index,1);
	}
	dom.style.borderColor = "#000";
	dom.style.borderWidth = "1px";
}

function selectAtomType(dom){
	if(activeType == "aliphatic"){
		if(dom.style.borderColor == "rgb(255, 0, 0)"){  //if button is selected
			turnUnmarked(dom);
		}
		else{ //button is not selected
			turnAliphatic(dom);
		}
	}
	else if(activeType == "aromatic"){
		if(dom.style.borderColor == "rgb(0, 255, 255)"){  //if button is selected
			turnUnmarked(dom);
		}
		else{ //button is not selected
			turnAromatic(dom);
		}
	}
	else{
		if(dom.style.borderColor == "rgb(204, 0, 255)"){  //if button is selected
			turnUnmarked(dom);
		}
		else{ //button is not selected
			turnBoth(dom);
		}
	}
}

function changeType(type){
	document.getElementById("aromatic").style.backgroundColor = "#D4D4D4";
	document.getElementById("aliphatic").style.backgroundColor = "#D4D4D4";
	document.getElementById("both").style.backgroundColor = "#D4D4D4";
	
	activeType = type;
	if(type == "aliphatic"){
		document.getElementById("aliphatic").style.backgroundColor = "#fd3c43";
	}
	else if(type == "aromatic"){
		document.getElementById("aromatic").style.backgroundColor = "#0CC";
	}
	else{
		document.getElementById("both").style.backgroundColor = "#C0F";
	}
}

function ok(){
	if(activeAliphaticType.length == 0 && activeAromaticType.length == 0){
		alert("You can not set empty atom");
		return;
	}
	if(openAtom){
		for(var i = 0; i < window.opener.stage.children[1].children.length; i++){ //finding atom in atomContainer
			if(window.opener.stage.children[1].children[i].id == openAtom.id){
				window.opener.stage.children[1].children[i].possibleAliphatic = activeAliphaticType;
				window.opener.stage.children[1].children[i].possibleAromatic = activeAromaticType;
				window.opener.stage.children[1].children[i].changeColorAndText();
				window.opener.Atom.updateShownProperties();
				break;
			}
		}
	}
	else{
		window.opener.deactivateButton();
		if(activeAromaticType.length + activeAliphaticType.length == 0){
			window.opener.changeAction("move");
		}
		else{
			window.opener.action = "createAtom";
		}
		window.opener.activeAromaticType = activeAromaticType;
		window.opener.activeAliphaticType = activeAliphaticType;
	}
	window.close();
}

function cancel(){
	window.close();
}