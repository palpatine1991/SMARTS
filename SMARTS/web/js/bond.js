Bond.prototype = new Container();
Bond.shownBond;

function Bond(type, startAtom, endAtom, startX, startY, endX, endY){
	Container.apply(this);
	var bond = new Shape();
	bondContainer.addChild(this);
	
	this.visited = false;
	this.startAtom = startAtom;
	this.endAtom = endAtom;
	this.startX = startX;
	this.startY = startY;
	this.endX = endX;
	this.endY = endY;
	this.type = [type];
	
	this.drawActiveLine = function(){
		this.activeLine = new Container();
		this.addChild(this.activeLine);
	}
	
	this.drawCombinedBond = function(){
		var transparentLine = new Shape();
		transparentLine.graphics.beginStroke(Graphics.getRGB(0,0,0,0.01)).setStrokeStyle(15);
		transparentLine.graphics.moveTo(this.startX,this.startY).lineTo(this.endX,this.endY);
		this.addChild(transparentLine);
		
		var line = new Shape();
		line.graphics.beginStroke(Graphics.getRGB(255,153,0)).setStrokeStyle(2);
		line.graphics.moveTo(this.startX,this.startY).lineTo(this.endX,this.endY);
		this.addChild(line);
		
		update = true;
	}
	
	this.drawAromaticBond = function(){
		var transparentLine = new Shape();
		transparentLine.graphics.beginStroke(Graphics.getRGB(0,0,0,0.01)).setStrokeStyle(15);
		transparentLine.graphics.moveTo(this.startX,this.startY).lineTo(this.endX,this.endY);
		this.addChild(transparentLine);
		
		var line = new Shape();
		line.graphics.beginStroke(Graphics.getRGB(0,191,191)).setStrokeStyle(2);
		line.graphics.moveTo(this.startX,this.startY).lineTo(this.endX,this.endY);
		this.addChild(line);
		
		update = true;
	}

	this.drawSingleBond = function(){
		var transparentLine = new Shape();
		transparentLine.graphics.beginStroke(Graphics.getRGB(0,0,0,0.01)).setStrokeStyle(15);
		transparentLine.graphics.moveTo(this.startX,this.startY).lineTo(this.endX,this.endY);
		this.addChild(transparentLine);
		
		var line = new Shape();
		line.graphics.beginStroke(Graphics.getRGB(0,0,0)).setStrokeStyle(2);
		line.graphics.moveTo(this.startX,this.startY).lineTo(this.endX,this.endY);
		this.addChild(line);
		
		update = true;
	}
	
	this.drawDoubleBond = function(){		
		var transparentLine = new Shape();
		transparentLine.graphics.beginStroke(Graphics.getRGB(0,0,0,0.01)).setStrokeStyle(15);
		transparentLine.graphics.moveTo(this.startX,this.startY).lineTo(this.endX,this.endY);
		this.addChild(transparentLine);
		
		var line = new Shape();
		line.graphics.beginStroke(Graphics.getRGB(0,0,0)).setStrokeStyle(4);
		line.graphics.moveTo(this.startX,this.startY).lineTo(this.endX,this.endY);
		this.addChild(line);
		
		line = new Shape();
		line.graphics.beginStroke(Graphics.getRGB(255,255,255)).setStrokeStyle(2);
		line.graphics.moveTo(this.startX,this.startY).lineTo(this.endX,this.endY);
		this.addChild(line);
		
		update = true;
	}
	
	this.drawTripleBond = function(){
		var transparentLine = new Shape();
		transparentLine.graphics.beginStroke(Graphics.getRGB(0,0,0,0.01)).setStrokeStyle(15);
		transparentLine.graphics.moveTo(this.startX,this.startY).lineTo(this.endX,this.endY);
		this.addChild(transparentLine);
		
		var line = new Shape();
		line.graphics.beginStroke(Graphics.getRGB(0,0,0)).setStrokeStyle(7);
		line.graphics.moveTo(this.startX,this.startY).lineTo(this.endX,this.endY);
		this.addChild(line);
		
		line = new Shape();
		line.graphics.beginStroke(Graphics.getRGB(255,255,255)).setStrokeStyle(5);
		line.graphics.moveTo(this.startX,this.startY).lineTo(this.endX,this.endY);
		this.addChild(line);
		
		line = new Shape();
		line.graphics.beginStroke(Graphics.getRGB(0,0,0)).setStrokeStyle(1);
		line.graphics.moveTo(this.startX,this.startY).lineTo(this.endX,this.endY);
		this.addChild(line);
		
		update = true;
	}
	
	this.bondPressHandler = function(e){
		if(action != "delete"){
			Bond.activateBond(e.target);	
		}
		else{
			Bond.deleteBond(e.target);
		}
	}
	
	
	this.onPress = this.bondPressHandler;
	
	this.redraw = function(){
		this.removeAllChildren();
		this.drawActiveLine();
		if(this.type.length == 1 && this.type[0] == "singleBond"){
			this.drawSingleBond();
		}
		else if(this.type.length == 1 && this.type[0] == "doubleBond"){
			this.drawDoubleBond();
		}
		else if(this.type.length == 1 && this.type[0] == "tripleBond"){
			this.drawTripleBond();
		}
		else if(this.type.length == 1 && this.type[0] == "aromaticBond"){
			this.drawAromaticBond();
		}
		else{
			this.drawCombinedBond();
		}
		if(Bond.shownBond){
			Bond.activateBond(Bond.shownBond);
		}
	}
	this.redraw();
	
	this.moveBond = function(e, differenceX, differenceY, edge){
		if(edge == "start"){
			this.startX = e.stageX - differenceX + 10;
			this.startY = e.stageY - differenceY + 10;
		}
		else{
			this.endX = e.stageX - differenceX + 10;
			this.endY = e.stageY - differenceY + 10;
		}
		this.redraw();
	}
}

Bond.deleteBond = function(bond){
	var index = bond.startAtom.bonds.indexOf(bond);
	bond.startAtom.bonds.splice(index,1);
	index = bond.endAtom.bonds.indexOf(bond);
	bond.endAtom.bonds.splice(index,1);
	bondContainer.removeChild(bond);
	update = true;
}

Bond.activateBond = function(bond){
	if(bond == interimBond){
		return;
	}
	$("#bondInfoDiv").css("display","block");
	Bond.shownBond = bond;
	
	var actline = new Shape();
	actline.graphics.beginStroke(Graphics.getRGB(0,255,0)).setStrokeStyle(11);
	actline.graphics.moveTo(bond.startX,bond.startY).lineTo(bond.endX,bond.endY);
	bond.activeLine.addChild(actline);

	actline = new Shape();
	actline.graphics.beginStroke(Graphics.getRGB(255,255,255)).setStrokeStyle(8);
	actline.graphics.moveTo(bond.startX,bond.startY).lineTo(bond.endX,bond.endY);
	bond.activeLine.addChild(actline);	
	
	for(var i = 0; i < bond.type.length; i++){
		$("#" + bond.type[i])[0].checked = true;
	}
	
	update = true;
}

Bond.deactivateBond = function(){
	$("#bondInfoDiv").css("display","none");
	Bond.shownBond.activeLine.removeAllChildren();
	Bond.shownBond = null;
	
	$("#singleBond")[0].checked = false;
	$("#doubleBond")[0].checked = false;
	$("#tripleBond")[0].checked = false;
	$("#aromaticBond")[0].checked = false;
}

Bond.checkboxHandler = function(cb){
	if(cb.checked){
		Bond.shownBond.type.push(cb.value);
	}
	else{
		if(Bond.shownBond.type.length == 1){
			cb.checked = true;
			return;
		}
		var index = Bond.shownBond.type.indexOf(cb.value);
		Bond.shownBond.type.splice(index,1);
	}
	Bond.shownBond.redraw();
}
