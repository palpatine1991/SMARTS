/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SMARTS.servlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;

/**
 *
 * @author Palpatine
 */
public class SMARTSGraph {
    Map<String,SMARTSGraphAtom> atoms = new HashMap<String,SMARTSGraphAtom>();
    List<SMARTSGraphBond> bonds = new ArrayList<SMARTSGraphBond>();
    int counter = 0;
    
    public Set<String> getIndexFileNames(){
        Set<String> result = new HashSet<String>();
        Set<String> usedAtoms = new HashSet<String>();
        
        for(SMARTSGraphBond bond : bonds){
            addBondIfSMILES(result, usedAtoms, bond);
        }
        
        for(SMARTSGraphAtom atom : atoms.values()){
            addAtomifSMILES(result, usedAtoms, atom);
        }
        
        return result;
    }
    
    private void addAtomifSMILES(Set<String> result, Set<String> usedAtoms, SMARTSGraphAtom atom){
        int atomicNumber = 0;
        Set<Integer> tmp = new HashSet<Integer>();
        tmp.addAll(atom.possibleAliphaticNumbers);
        tmp.addAll(atom.possibleAromaticNumbers);
        if(tmp.size() == 1){
            for(int i : tmp){
                atomicNumber = i;
            }
        }

        if(atomicNumber != 0 && !usedAtoms.contains(Integer.toString(atomicNumber))){
            result.add(Integer.toString(atomicNumber));
        }
    }
    
    private void addBondIfSMILES(Set<String> result, Set<String> usedAtoms, SMARTSGraphBond bond){
        if(bond.typeCount > 1){
            return;
        }
        int atomicNumber1 = 0;
        int atomicNumber2 = 0;
        //find if first atom got only 1 atomicNumber
        Set<Integer> tmp = new HashSet<Integer>();
        tmp.addAll(bond.firstAtom.possibleAliphaticNumbers);
        tmp.addAll(bond.firstAtom.possibleAromaticNumbers);
        if(tmp.size() == 1){
            for(int i : tmp){
                atomicNumber1 = i;
            }
        }     
        //find if second atom got only 1 atomicNumber
        tmp.clear();
        tmp.addAll(bond.secondAtom.possibleAliphaticNumbers);
        tmp.addAll(bond.secondAtom.possibleAromaticNumbers);
        if(tmp.size() == 1){
            for(int i : tmp){
                atomicNumber2 = i;
            }
        }

        if(atomicNumber1 > atomicNumber2){
            int tmp1 = atomicNumber1;
            atomicNumber1 = atomicNumber2;
            atomicNumber2 = tmp1;
        }

        if(atomicNumber1 != 0 && atomicNumber2 != 0){
            usedAtoms.add(Integer.toString(atomicNumber1));
            usedAtoms.add(Integer.toString(atomicNumber2));
            if(bond.aromaticBond){
                result.add(atomicNumber1 + "~" + atomicNumber2);
            }
            if(bond.singleBond){
                result.add(atomicNumber1 + "-" + atomicNumber2);
            }
            if(bond.doubleBond){
                result.add(atomicNumber1 + "=" + atomicNumber2);
            }
            if(bond.tripleBond){
                result.add(atomicNumber1 + "#" + atomicNumber2);
            }
        }
    }
}
