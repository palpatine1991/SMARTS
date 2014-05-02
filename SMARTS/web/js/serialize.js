function Serializer(){
	
}

Serializer.serialize = function(){
        
        var obj = Serializer.getJson();
        
        json = JSON.stringify(obj);
    
	Serializer.bracket = 1;
	Serializer.resultArray = [];
	Serializer.resultArrayIndex = 0;
	$("#serialization").text("");
	Serializer.dfs(atomContainer.getChildAt(0), null);
	if(!Serializer.clearFlags()){
		alert("Graph has to be connected");
		return false;
	}
	for(var i=0; i < Serializer.resultArray.length; i++){
		$("#serialization").append(Serializer.resultArray[i]);
	}
        return true;
}

Serializer.getJson = function(){
    var obj = {atoms : {}, bonds : []};
    
    $.each(atomContainer.children, function(index, value){
        obj.atoms[index.toString()] = {
            possibleAromaticNumbers : [],
            possibleAliphaticNumbers : [],
            possibleCharges : value.possibleCharges,
            possibleValences : value.possibleValences
        };
        $.each(value.possibleAromatic, function(i,v){
            obj.atoms[index.toString()].possibleAromaticNumbers.push(parseInt(Serializer.convertToNumber(v).substring(1)));
        });
        $.each(value.possibleAliphatic, function(i,v){
            obj.atoms[index.toString()].possibleAliphaticNumbers.push(parseInt(Serializer.convertToNumber(v).substring(1)));
        });
    });
    
    $.each(bondContainer.children, function(index, value){
        if((!value.startAtom) || (!value.endAtom)){
            return true;
        }
        var tmp = {singleBond : false, doubleBond: false, tripleBond : false, aromaticBond: false};
        tmp.firstAtomId = atomContainer.children.lastIndexOf(value.startAtom).toString();
        tmp.secondAtomId = atomContainer.children.lastIndexOf(value.endAtom).toString();
        
        $.each(value.type, function(i,v){
           if(v == "singleBond"){
               tmp.singleBond = true;
           }
           else if(v == "doubleBond"){
               tmp.doubleBond = true;
           }
           else if(v == "tripleBond"){
               tmp.tripleBond = true;
           }
           else if(v == "aromaticBond"){
               tmp.aromaticBond = true;
           }
        });
        
        obj.bonds.push(tmp);
    });
    
    return obj;
}

Serializer.clearFlags = function(){ //vrati false, pokud nektery atom nemel nahozeny flag visited
	var value = true;
	for(var i=0; i < atomContainer.children.length;i++){
		if(!atomContainer.children[i].visited){
			value = false;
		}
		atomContainer.children[i].visited = false;
	}
	for(var i=0; i < bondContainer.children.length;i++){
		bondContainer.children[i].visited = false;
	}
	return value;
}

Serializer.dfs = function(atom,bond){
	atom.visited = true;
	
	Serializer.addString("(");
	//pridani vazby
	if(bond){
		var bondString = Serializer.serializeBond(bond);
		Serializer.addString(bondString);
	}
	
	var atomString = "[";
	//pridani alifatickych moznosti
	for(var i=0; i < atom.possibleAliphatic.length; i++){
		atomString += Serializer.convertToNumber(atom.possibleAliphatic[i]);
		atomString += "&A";
		
		if(i < atom.possibleAliphatic.length - 1){
			atomString += ",";
		}
	}
	
	if(atom.possibleAliphatic.length > 0 && atom.possibleAromatic.length > 0){
		atomString += ",";
	}
	//pridani aromatickych moznosti
	for(var i=0; i < atom.possibleAromatic.length; i++){
		atomString += Serializer.convertToNumber(atom.possibleAromatic[i]);
		atomString += "&a";
		
		if(i < atom.possibleAromatic.length - 1){
			atomString += ",";
		}
	}
	//pridani moznych naboju
	if(atom.possibleCharges.length > 0){
		atomString += ";";
	}
	
	for(var i=0; i < atom.possibleCharges.length; i++){
		if(atom.possibleCharges[i] >= 0){
			atomString += "+";
		}
		atomString += atom.possibleCharges[i];
		
		if(i < atom.possibleCharges.length - 1){
			atomString += ",";
		}
	}
	//pridani moznych stupnu
	if(atom.possibleValences.length > 0){
		atomString += ";";
	}
	
	for(var i=0; i < atom.possibleValences.length; i++){
		atomString+= "v";
		atomString += atom.possibleValences[i];
		
		if(i < atom.possibleValences.length - 1){
			atomString += ",";
		}
	}
	
	atomString += "]";
	atom.stringIndex = Serializer.resultArrayIndex;
	Serializer.addString(atomString);
	
	//zpracovani prislusnych vazeb a rekurzivni zavolani na spojene atomy
	for(var i=0; i < atom.bonds.length; i++){
		var otherAtom;
		if(atom.bonds[i].startAtom == atom){
			otherAtom = atom.bonds[i].endAtom;
		}
		else{
			otherAtom = atom.bonds[i].startAtom;
		}
		
		if(otherAtom.visited){
			if(atom.bonds[i].visited){
				continue;
			}
			else{
				atom.bonds[i].visited = true;
				var bondString = Serializer.serializeBond(atom.bonds[i]);
				if(Serializer.bracket >= 10){
					bondString += "%";	
				}
				bondString += Serializer.bracket;
				Serializer.bracket++;
				Serializer.resultArray[atom.stringIndex] += bondString;
				Serializer.resultArray[otherAtom.stringIndex] += bondString;
			}
		}
		else{
			atom.bonds[i].visited = true;
			Serializer.dfs(otherAtom, atom.bonds[i]);
		}
	}
	
	Serializer.addString(")");
	
}

Serializer.serializeBond = function(bond){
	var bondString = "";
	for (var i=0; i < bond.type.length; i++) {
		if(bond.type[i] == "singleBond"){
			bondString += "-";
		}
		else if(bond.type[i] == "doubleBond"){
			bondString += "=";
		}
		else if(bond.type[i] == "tripleBond"){
			bondString += "#";
		}
		else if(bond.type[i] == "aromaticBond"){
			bondString += ":";
		}
		if(i < bond.type.length - 1){
			bondString += ",";
		}
	}
	return bondString;
}

Serializer.addString = function(string){
	Serializer.resultArray.push(string);
	Serializer.resultArrayIndex++;
}

Serializer.convertToNumber = function(string){
	switch (string)
	{
	case "H":
		return "#1";
	case "He":
		return "#2";
	case "Li":
		return "#3";
	case "Be":
		return "#4";
	case "B":
		return "#5";
	case "C":
		return "#6";
	case "N":
		return "#7";
	case "O":
		return "#8";
	case "F":
		return "#9";
	case "Ne":
		return "#10";
	case "Na":
		return "#11";
	case "Mg":
		return "#12";
	case "Al":
		return "#13";
	case "Si":
		return "#14";
	case "P":
		return "#15";
	case "S":
		return "#16";
	case "Cl":
		return "#17";
	case "Ar":
		return "#18";
	case "K":
		return "#19";
	case "Ca":
		return "#20";
	case "Sc":
		return "#21";
	case "Ti":
		return "#22";
	case "V":
		return "#23";
	case "Cr":
		return "#24";
	case "Mn":
		return "#25";
	case "Fe":
		return "#26";
	case "Co":
		return "#27";
	case "Ni":
		return "#28";
	case "Cu":
		return "#29";
	case "Zn":
		return "#30";
	case "Ga":
		return "#31";
	case "Ge":
		return "#32";
	case "As":
		return "#33";
	case "Se":
		return "#34";
	case "Br":
		return "#35";
	case "Kr":
		return "#36";
	case "Rb":
		return "#37";
	case "Sr":
		return "#38";
	case "Y":
		return "#39";
	case "Zr":
		return "#40";
	case "Nb":
		return "#41";
	case "Mo":
		return "#42";
	case "Tc":
		return "#43";
	case "Ru":
		return "#44";
	case "Rh":
		return "#45";
	case "Pd":
		return "#46";
	case "Ag":
		return "#47";
	case "Cd":
		return "#48";
	case "In":
		return "#49";
	case "Sn":
		return "#50";
	case "Sb":
		return "#51";
	case "Te":
		return "#52";
	case "I":
		return "#53";
	case "Xe":
		return "#54";
	case "Cs":
		return "#55";
	case "Ba":
		return "#56";
	case "La":
		return "#57";
	case "Ce":
		return "#58";
	case "Pr":
		return "#59";
	case "Nd":
		return "#60";
	case "Pm":
		return "#61";
	case "Sm":
		return "#62";
	case "Eu":
		return "#63";
	case "Gd":
		return "#64";
	case "Tb":
		return "#65";
	case "Dy":
		return "#66";
	case "Ho":
		return "#67";
	case "Er":
		return "#68";
	case "Tm":
		return "#69";
	case "Yb":
		return "#70";
	case "Lu":
		return "#71";
	case "Hf":
		return "#72";
	case "Ta":
		return "#73";
	case "W":
		return "#74";
	case "Re":
		return "#75";
	case "Os":
		return "#76";
	case "Ir":
		return "#77";
	case "Pt":
		return "#78";
	case "Au":
		return "#79";
	case "Hg":
		return "#80";
	case "Tl":
		return "#81";
	case "Pb":
		return "#82";
	case "Bi":
		return "#83";
	case "Po":
		return "#84";
	case "At":
		return "#85";
	case "Rn":
		return "#86";
	case "Fr":
		return "#87";
	case "Ra":
		return "#88";
	case "Ac":
		return "#89";
	case "Th":
		return "#90";
	case "Pa":
		return "#91";
	case "U":
		return "#92";
	case "Np":
		return "#93";
	case "Pu":
		return "#94";
	case "Am":
		return "#95";
	case "Cm":
		return "#96";
	case "Bk":
		return "#97";
	case "Cf":
		return "#98";
	case "Es":
		return "#99";
	case "Fm":
		return "#100";
	case "Md":
		return "#101";
	case "No":
		return "#102";
	case "Lr":
		return "#103";
	case "Rf":
		return "#104";
	case "Db":
		return "#105";
	case "Sg":
		return "#106";
	case "Bh":
		return "#107";
	case "Hs":
		return "#108";
	case "Mt":
		return "#109";
	}
}
