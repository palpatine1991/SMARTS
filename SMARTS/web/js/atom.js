Atom.prototype = new Container();
var atomDiameter = 20;
var lastActiveAtom;

function Atom(aromatic, aliphatic, x, y){
	Container.apply(this);
	this.x = x;
	this.y = y;
	atomContainer.addChild(this);
	
	var atom = new Shape();
	
	this.visited = false;
	this.possibleAliphatic = aliphatic;
	this.possibleAromatic = aromatic;
	this.atom = atom;
	this.bonds = [];
	this.possibleCharges = [];
	this.possibleValences = [];
	
	this.changeColorAndText = function(){
		atom.graphics.beginStroke(Graphics.getRGB(0, 0, 0, 1));
		if(this == shownAtom){
			atom.graphics.beginStroke(Graphics.getRGB(0, 255, 0, 1));
		}
		atom.graphics.setStrokeStyle(2);	
		if(this.possibleAromatic.length > 0 && this.possibleAliphatic.length > 0){
			atom.graphics.beginFill("#C0F");
		}
		else if(this.possibleAromatic.length > 0){
			atom.graphics.beginFill("#0CC");
		}
		else{
			atom.graphics.beginFill("#fd3c43");
		}
		atom.graphics.drawEllipse(0,0,atomDiameter,atomDiameter);
		this.addChild(atom);
		
		if(this.possibleAromatic.length  + this.possibleAliphatic.length > 1){
			var text = new Text("*", "11px Arial", "#000000");
		}
		else if(this.possibleAromatic.length == 1){
			var text = new Text(this.possibleAromatic[0], "11px Arial", "#000000");
		}
		else if(this.possibleAliphatic.length == 1){
			var text = new Text(this.possibleAliphatic[0], "11px Arial", "#000000");
		}
		
		text.y = 10;
		text.x = 10;
		text.textAlign = "center";
		text.textBaseline = "middle";
		this.addChild(text);
		update = true;
	}
	
	this.atomPressHandler = function(e){
		if(action == "createBond" && lastActiveAtom && !lastActiveAtom.newBondCheck){
			e.target.newBond = true;
			e.target.newBondCheck = true;
		}
		else if(action == "createBond" && lastActiveAtom && lastActiveAtom.newBondCheck){
			if(lastActiveAtom == e.target) {
				e.target.newBond = true;
				e.target.newBondCheck = true;
			}
			else if(Atom.checkExistingBonds(e.target, lastActiveAtom)){
				var bond = new Bond(activeBondType, lastActiveAtom, e.target, lastActiveAtom.x + 10, lastActiveAtom.y + 10, e.target.x + 10, e.target.y + 10);
				lastActiveAtom.bonds.push(bond);
				e.target.bonds.push(bond);
				changeAction("move");
				update = true;
			}
		}
		else if(action == "delete"){
			Atom.deleteAtom(e.target);
		}
		
		if(action != "delete"){
			Atom.activateAtom(e.target);
			
			//pohyb
			var differenceX = e.stageX - e.target.x;
			var differenceY = e.stageY - e.target.y;
			e.onMouseMove = function(ev) {
				Atom.moveBonds(ev, e.target, differenceX, differenceY);
				e.target.x = ev.stageX - differenceX;
				e.target.y = ev.stageY - differenceY;
				update = true;
			}	
		}
		
		update = true;
	}
	
	
	
	this.changeColorAndText();
	
	//2 promenne kvuli tomu, ze prvni event dostane stage, ktera atom deaktivuje a teprve potom atom
	//proto musim udrzovat druhou promennou - smulace 3 stavove promenne - V poradku, stale jeste v poradku, uz ne
	this.newBond = false;
	this.newBondCheck = false;
	
	this.onPress = this.atomPressHandler;
	
	changeAction("move");
	
	
}

Atom.moveBonds = function(e, atom, differenceX, differenceY){
	for(var i = 0; i < atom.bonds.length; i++){
		if(atom.x == atom.bonds[i].startX - 10 && atom.y == atom.bonds[i].startY - 10){
			atom.bonds[i].moveBond(e, differenceX, differenceY, "start");
		}
		else{
			atom.bonds[i].moveBond(e, differenceX, differenceY, "end");
		}
	}
}

Atom.deleteAtom = function(_atom){
	for(var i = 0; i < _atom.bonds.length; i++){
		//odstraneni vazby i z druheho koncoveho atomu
		var end = null;
		if(_atom.bonds[i].startAtom == _atom){
			end = _atom.bonds[i].endAtom;
		}
		else{
			end = _atom.bonds[i].startAtom;
		}
		var index = end.bonds.indexOf(_atom.bonds[i]);
		end.bonds.splice(index,1);
				
		//odstraneni vazby z canvasu
		bondContainer.removeChild(_atom.bonds[i]);
	}
	atomContainer.removeChild(_atom);
	update = true;
}

Atom.updateShownProperties = function(){
	document.getElementById("possibleAliphatic").innerHTML = shownAtom.possibleAliphatic;
	document.getElementById("possibleAromatic").innerHTML = shownAtom.possibleAromatic;
	document.getElementById("possibleCharges").innerHTML = shownAtom.possibleCharges;
	document.getElementById("possibleValences").innerHTML = shownAtom.possibleValences;
}

Atom.activateAtom = function(atom){
	//aktivace atomu
	$("#atomInfoDiv").css("visibility","visible");
	shownAtom = atom;
	lastActiveAtom = atom;
	lastActiveAtom.atom.graphics.beginStroke(Graphics.getRGB(0, 255, 0, 1));
	lastActiveAtom.atom.graphics.drawEllipse(0,0,atomDiameter,atomDiameter);
	
	//zverejneni properties
	Atom.updateShownProperties();
}

Atom.deactivateAtom = function(){
	$("#atomInfoDiv").css("visibility","hidden");
	lastActiveAtom.atom.graphics.beginStroke(Graphics.getRGB(0, 0, 0, 1));
	lastActiveAtom.atom.graphics.drawEllipse(0,0,atomDiameter,atomDiameter);
	document.getElementById("possibleAliphatic").innerHTML = [];
	document.getElementById("possibleAromatic").innerHTML = [];
	document.getElementById("possibleCharges").innerHTML = [];
	document.getElementById("possibleValences").innerHTML = [];
	if(lastActiveAtom.newBond){
		lastActiveAtom.newBond = false;
	}
	else if(lastActiveAtom.newBondCheck){
		lastActiveAtom.newBondCheck = false;
	}
	shownAtom = null;
	update = true;
}

Atom.checkExistingBonds = function(start, end){ //vrati true, pokud mezi start a end zatim neexistuje vazba
	var answer = true;
	for(var i=0; i < start.bonds.length; i++){
		var bond = start.bonds[i];
		if(bond.startAtom == start && bond.endAtom == end){
			answer = false;
		}
		else if(bond.startAtom == end && bond.endAtom == start){
			answer = false;
		}
	}
	return answer;
}
