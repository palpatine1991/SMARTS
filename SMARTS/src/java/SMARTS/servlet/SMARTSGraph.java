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
    Map<String,byte[]> filters;
    Map<String,List<Integer>> smallFilters;
    Map<String, Integer> filterStats;
    int numberOfRecords;
    int listLimit;
    
    
    public byte[] getFilter(){
        byte[] result = null;
        Set<String> usedAtoms = new HashSet<String>();
        
        boolean firstItem = true;
        
        for(SMARTSGraphBond bond : bonds){
            if(firstItem){
                firstItem = false;
                result = createBondFilter(usedAtoms, bond);
            }
            else{
                result = doAnd(result, createBondFilter(usedAtoms, bond));
            }
            
        }  
        for(SMARTSGraphAtom atom : atoms.values()){
            if(firstItem){
                firstItem = false;
                result = createAtomFilter(usedAtoms, atom);
            }
            else{
                byte[] tmp = createAtomFilter(usedAtoms, atom);
                if(tmp != null){
                    result = doAnd(result, tmp);
                }             
            }
        }
        
        return result;
    }
    
    private byte[] createAtomFilter(Set<String> usedAtoms, SMARTSGraphAtom atom){
        Set<Integer> tmp = new HashSet<Integer>();
        tmp.addAll(atom.possibleAliphaticNumbers);
        tmp.addAll(atom.possibleAromaticNumbers);
        
        byte[] result = null;
        
        boolean firstItem = true;
        
        for(int i : tmp){
            if(!usedAtoms.contains(Integer.toString(i)) && atom.possibleCharges.isEmpty() && atom.possibleValences.isEmpty()){
                byte[] filter = null;
                if(filterStats.get(Integer.toString(i)) >= listLimit){
                    filter = filters.get(Integer.toString(i));
                }
                else{
                    filter = listToByte(smallFilters.get(Integer.toString(i)));
                }
                if(firstItem){
                    firstItem = false;
                    result = filter;
                }
                else{
                    result = doOr(result, filter);
                }
            }
            for(int j : atom.possibleCharges){
                String key = "";
                if(j >= 0){
                    key = i + ";+" + j;
                }
                else{
                    key = i + ";" + j;
                }
                byte[] filter = null;
                if(filterStats.get(key) >= listLimit){
                    filter = filters.get(key);
                }
                else{
                    filter = listToByte(smallFilters.get(key));
                }
                if(firstItem){
                    firstItem = false;
                    result = filter;
                }
                else{
                    result = doOr(result, filter);
                }
            }
            for(int j : atom.possibleValences){
                String key = i + ";v" + j;
                byte[] filter = null;
                if(filterStats.get(key) >= listLimit){
                    filter = filters.get(key);
                }
                else{
                    filter = listToByte(smallFilters.get(key));
                }
                if(firstItem){
                    firstItem = false;
                    result = filter;
                }
                else{
                    result = doOr(result, filter);
                }
            }
        }
        
        return result;
    }
    
    private byte[] createBondFilter(Set<String> usedAtoms, SMARTSGraphBond bond){
        byte[] result = null;
        Set<Integer> atomset1 = new HashSet<Integer>();
        Set<Integer> atomset2 = new HashSet<Integer>();
        atomset1.addAll(bond.firstAtom.possibleAliphaticNumbers);
        atomset1.addAll(bond.firstAtom.possibleAromaticNumbers);
     
        atomset2.addAll(bond.secondAtom.possibleAliphaticNumbers);
        atomset2.addAll(bond.secondAtom.possibleAromaticNumbers);
        
        boolean[] firstItem = new boolean[1]; // trik pro predavani parametru do funkce referenci     
        firstItem[0] = true;

        for(int i : atomset1){
            for(int j : atomset2){
                usedAtoms.add(Integer.toString(i));
                usedAtoms.add(Integer.toString(j));
                int first = i;
                int second = j;
                if(first > second){
                    int tmp = first;
                    first = second;
                    second = tmp;
                }
                if(bond.aromaticBond){
                    result = addBondPropertyToFilter(firstItem, first + "~" + second, result);
                }
                if(bond.singleBond){
                    result = addBondPropertyToFilter(firstItem, first + "-" + second, result);
                }
                if(bond.doubleBond){
                    result = addBondPropertyToFilter(firstItem, first + "=" + second, result);
                }
                if(bond.tripleBond){
                    result = addBondPropertyToFilter(firstItem, first + "#" + second, result);
                }
            }
        }
        
        return result;
    }
    
    private byte[] addBondPropertyToFilter(boolean[] firstItem, String key, byte[] result){  
        byte[] filter = null;
        if(filterStats.get(key) >= listLimit){
            filter = filters.get(key);
        }
        else{
            filter = listToByte(smallFilters.get(key));
        }
        if(firstItem[0]){
            firstItem[0] = false;
            result = filter;
        }
        else{
            result = doOr(result, filter);
        }
        return result;
    }
    
    private byte[] doAnd(byte[] arr1, byte[] arr2){
        if(arr1.length == 1){
            return arr1;
        }
        if(arr2.length == 1){
            return arr2;
        }
        
        for(int i = 0; i < arr1.length; i++){
            arr1[i] = (byte)(arr1[i] & arr2[i]);
        }
        return arr1;
    }
    
    private byte[] doOr(byte[] arr1, byte[] arr2){
        if(arr1.length == 1){
            return arr2;
        }
        if(arr2.length == 1){
            return arr1;
        }
        
        for(int i = 0; i < arr1.length; i++){
            arr1[i] = (byte)(arr1[i] | arr2[i]);
        }
        return arr1;
    }
    
    private byte[] listToByte(List<Integer> list){
        if(list.size() == 0){
            byte[] tmp = new byte[1];
            tmp[0] = 0;
            return tmp;
        }
        int arrayLength = (int) Math.ceil(((double)numberOfRecords) / 8);
        byte[] result = new byte[arrayLength];
        
        int progress = 0;
        int byteProgress = 1;
        byte actualByte = 0;
        
        for(int i : list){
            if(i > (progress * 8) + 8 && byteProgress > 1){
                for(int j = byteProgress; j <= 8; j++){
                    actualByte *= 2;
                }
                result[progress] = actualByte;
                actualByte = 0;
                byteProgress = 1;
                progress++;
            }
            while(i > (progress * 8) + 8){
                result[progress] = 0;
                progress++;
            }
            for(int j = byteProgress; j <= 8; j++){
                byteProgress++;
                actualByte *= 2;
                if((progress * 8) + j == i){
                    actualByte++;
                    break;
                }
            }
        }
        if(byteProgress > 1){
            for(int j = byteProgress; j <= 8; j++){
                actualByte *= 2;
            }
            result[progress] = actualByte;
        }
        
        return result;
    }
}
