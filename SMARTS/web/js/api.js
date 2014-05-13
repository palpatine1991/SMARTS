var stage;
var bondContainer = new Container();
var atomContainer = new Container();
var update = false;
var action = "move"; //..,createAtom, createBond, delete
var activeAromaticType = [];
var activeAliphaticType = [];
var activeBondType;
var interimBond;
var shownAtom;
var json;

 $(function() {
     $(".AtomButton").button();
     $("#singleBondButton").button("option", "icons", { primary: "ui-icon-singleBond" });
     $("#doubleBondButton").button("option", "icons", { primary: "ui-icon-doubleBond" });
     $("#tripleBondButton").button("option", "icons", { primary: "ui-icon-tripleBond" });
     $("#aromaticBondButton").button("option", "icons", { primary: "ui-icon-aromaticBond" });
     $("#benzen").button("option", "icons", { primary: "ui-icon-benzen" });
 });

function init(){
	$(window).keydown(function(e) {
  		if(e.which == 46){
		  	if(shownAtom){
		  		var sa = shownAtom;
		  		Atom.deactivateAtom();
		  		Atom.deleteAtom(sa);
		  	}
		  	else if(Bond.shownBond){
		  		var sb = Bond.shownBond;
		  		Bond.deactivateBond();
		  		Bond.deleteBond(sb);
		  		
		  	}
		}
		if(e.which == 27){
			interimBond.removeAllChildren();
			changeAction("move");
			if(lastActiveAtom){
				Atom.deactivateAtom();
			}
			
			if(Bond.shownBond){
				Bond.deactivateBond();
			}
			update = true;
		}
	});
	
	stage = new Stage(document.getElementById("canvas"));	
	stage.onMouseDown = stageOnMouseDownHandler;
	stage.onMouseMove = stageOnMouseMoveHandler;
	
	stage.addChild(bondContainer);
	stage.addChild(atomContainer);
	
	Ticker.addListener(window);
	
	interimBond = new Bond("singleBond", null, null, 0,0,0,0);
	changeAction("move");
}

function changeAction(actualAction){
	if(actualAction == "move"){
		deactivateButton();
		action = "move";
	}
	else if(actualAction == "benzen"){
		deactivateButton();
		action = "benzen";
		activeAromaticType = ["C"];
		activeAliphaticType = [];
		document.getElementById("benzen").style.backgroundColor = "#6A85FF";
	}
}

function stageOnMouseMoveHandler(e){
	interimBond.removeAllChildren();
	if(lastActiveAtom && lastActiveAtom.newBond){
		interimBond.type = [activeBondType];
		interimBond.startX = lastActiveAtom.x + 10;
		interimBond.startY = lastActiveAtom.y + 10;
		interimBond.endX = e.stageX;
		interimBond.endY = e.stageY;
		interimBond.redraw();
	}
	update = true;
}

function stageOnMouseDownHandler(e){	
	//interimBond.removeAllChildren();
	
	//deaktivace atomu
	if(lastActiveAtom){
		Atom.deactivateAtom();
	}
	
	if(Bond.shownBond){
		Bond.deactivateBond();
	}
	
	if(action == "createAtom"){
		createAtom(e);
	}
	else if(action == "benzen"){
		createBenzen(e);
	}
}

function createBenzen(e){
	if(isThereIntersect(e)){
		return;
	}
	if(e.stageX < 50 || e.stageX > 750 || e.stageY < 50 || e.stageY > 550){
		return;	
	}
	var atom1 = new Atom(activeAromaticType, activeAliphaticType, e.stageX - 10, e.stageY - 50);
	var atom2 = new Atom(activeAromaticType, activeAliphaticType, e.stageX + 20, e.stageY - 30);
	var atom3 = new Atom(activeAromaticType, activeAliphaticType, e.stageX + 20, e.stageY + 10);
	var atom4 = new Atom(activeAromaticType, activeAliphaticType, e.stageX - 10, e.stageY + 30);
	var atom5 = new Atom(activeAromaticType, activeAliphaticType, e.stageX - 40, e.stageY + 10);
	var atom6 = new Atom(activeAromaticType, activeAliphaticType, e.stageX - 40, e.stageY - 30);
	
	var bond = new Bond("aromaticBond", atom1, atom2, atom1.x + 10, atom1.y + 10, atom2.x + 10, atom2.y + 10);
	atom1.bonds.push(bond);
	atom2.bonds.push(bond);
	bond = new Bond("aromaticBond", atom2, atom3, atom2.x + 10, atom2.y + 10, atom3.x + 10, atom3.y + 10);
	atom2.bonds.push(bond);
	atom3.bonds.push(bond);
	bond = new Bond("aromaticBond", atom3, atom4, atom3.x + 10, atom3.y + 10, atom4.x + 10, atom4.y + 10);
	atom3.bonds.push(bond);
	atom4.bonds.push(bond);
	bond = new Bond("aromaticBond", atom4, atom5, atom4.x + 10, atom4.y + 10, atom5.x + 10, atom5.y + 10);
	atom4.bonds.push(bond);
	atom5.bonds.push(bond);
	bond = new Bond("aromaticBond", atom5, atom6, atom5.x + 10, atom5.y + 10, atom6.x + 10, atom6.y + 10);
	atom5.bonds.push(bond);
	atom6.bonds.push(bond);
	bond = new Bond("aromaticBond", atom6, atom1, atom6.x + 10, atom6.y + 10, atom1.x + 10, atom1.y + 10);
	atom6.bonds.push(bond);
	atom1.bonds.push(bond);
}

function createAtom(e){
	if(isThereIntersect(e)){
		return;
	}
	var atom = new Atom(activeAromaticType, activeAliphaticType, e.stageX - 10, e.stageY - 10);
	update = true;
}

function isThereIntersect(e){
	for(var i=0; i < stage.children.length; i++){
		var container = stage.children[i];
		if(container.hitTest(e.stageX - 10 - container.x, e.stageY - container.y)){
			return true;
		}
		else if(container.hitTest(e.stageX + 10 - container.x, e.stageY - container.y)){
			return true;
		}
		else if(container.hitTest(e.stageX - container.x, e.stageY - 10 - container.y)){
			return true;
		}
		else if(container.hitTest(e.stageX - container.x, e.stageY + 10 - container.y)){
			return true;
		}
		else if(container.hitTest(e.stageX + 10 - container.x, e.stageY + 10 - container.y)){
			return true;
		}
		else if(container.hitTest(e.stageX + 10 - container.x, e.stageY - 10 - container.y)){
			return true;
		}
		else if(container.hitTest(e.stageX - 10 - container.x, e.stageY - 10 - container.y)){
			return true;
		}
		else if(container.hitTest(e.stageX - 10 - container.x, e.stageY + 10 - container.y)){
			return true;
		}
	}
	return false;
}

function tick(){
	if (update) {
		update = false;
		stage.update();
	}	
}

function changeAtomType(type){
	deactivateButton();
	action = "createAtom";
	
	activeAliphaticType = [type];
	activeAromaticType = [];
    var button = document.getElementById(type);
	button.style.backgroundColor = "#6A85FF";
	update = true;
}

function deactivateButton(){
	/*if(activeAliphaticType.length == 1 && activeAromaticType.length == 0){
		var button = document.getElementById(activeAliphaticType[0]);	
		if(button != null){
			button.style.backgroundColor = "#EFEFEF";
		}
	}
	var button = document.getElementById(activeBondType + "Button");	
	if(button != null){
		button.style.backgroundColor = "#EFEFEF";
		activeBondType = null;
	}
	if(lastActiveAtom && lastActiveAtom.newBond){
		lastActiveAtom.newBond = false;
	}
	var button = document.getElementById(action);
	if(button != null){
		button.style.backgroundColor = "#EFEFEF";
	}*/
        $(".AtomButton").each(function(){
           $(this).prop('checked', false); 
           $(this).button( "refresh" );
        });
        //activeBondType = null;
        //lastActiveAtom.newBond = false;
}

function changeBondType(type){
	deactivateButton();
	action = "createBond";
	activeBondType = type;
	
	var button = document.getElementById(type + "Button");
	button.style.backgroundColor = "#6A85FF";
	update = true;
}

function openAtomChoice(atom){
	var popup = window.open("atomChoice.html",'atomChoice','height=380,width=620');
	popup.openAtom = atom;
}

function addCharge(){
	if(!shownAtom){return;}
	var number = document.getElementById("chargeAdder").valueAsNumber;
	if((number || number == 0) && shownAtom.possibleCharges.indexOf(number) == -1){
		shownAtom.possibleCharges.push(number);
		document.getElementById("possibleCharges").innerHTML = shownAtom.possibleCharges;
	}
}

function deleteCharge(){
	if(!shownAtom){return;}
	var number = document.getElementById("chargeAdder").valueAsNumber;
	if((number || number == 0) && shownAtom.possibleCharges.indexOf(number) != -1){
		var index = shownAtom.possibleCharges.indexOf(number);
		shownAtom.possibleCharges.splice(index,1);
		document.getElementById("possibleCharges").innerHTML = shownAtom.possibleCharges;
	}
}

function addValence(){
	if(!shownAtom){return;}
	var number = document.getElementById("valenceAdder").valueAsNumber;
	if((number > 0) && shownAtom.possibleValences.indexOf(number) == -1){
		shownAtom.possibleValences.push(number);
		document.getElementById("possibleValences").innerHTML = shownAtom.possibleValences;
	}
}

function deleteValence(){
	if(!shownAtom){return;}
	var number = document.getElementById("valenceAdder").valueAsNumber;
	if((number > 0) && shownAtom.possibleValences.indexOf(number) != -1){
		var index = shownAtom.possibleValences.indexOf(number);
		shownAtom.possibleValences.splice(index,1);
		document.getElementById("possibleValences").innerHTML = shownAtom.possibleValences;
	}
}

function query(){
    if(!Serializer.serialize()){
        return;
    }
    
    var d = new Date();
    d.setTime(d.getTime()+(60*60*1000));
    var expires = "expires="+d.toGMTString();
    

    var obj = {smarts : $("#serialization").text(), json: json};
    document.cookie = "queryObject=" + encodeURIComponent(JSON.stringify(obj)) + "; " + expires + "; path=./index.jsp";
    window.location.href = "./index.html";
}
