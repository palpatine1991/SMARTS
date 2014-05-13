package indexlibrary;


import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Palpatine
 */
public class IndexLibrary {
    public static void closeByte(int bit, int[][] results, BufferedOutputStream[][] outs, int size) throws IOException{
        for(int k = bit + 1; k <= 8; k++){
             if(k == 1){
                 break;
             }
             if(k == 8){
                 for(int i = 1; i <= 109; i++){
                    for(int j = 0; j < size; j++){ 
                        outs[i - 1][j].write(results[i - 1][j]);
                    }
                }
             }
             else{
                for(int i = 1; i <= 109; i++){
                    for(int j = 0; j < size; j++){ 
                        results[i - 1][j] *= 2;
                    }
                }
             }
         }
    }
    
    public static void closeByte(int bit, int[] results, BufferedOutputStream[] outs) throws IOException{
        for(int k = bit + 1; k <= 8; k++){
             if(k == 1){
                 break;
             }
             if(k == 8){
                 for(int i = 1; i <= 109; i++){
                    outs[i - 1].write(results[i - 1]);
                 }
             }
             else{
                for(int i = 1; i <= 109; i++){ 
                    results[i - 1] *= 2;
                }
             }
         }
    }
    
    public static void closeByte(int bit, Map<String, Integer> results, Map<String, BufferedOutputStream> outs) throws IOException{
        for(int j = bit + 1; j <= 8; j++){
             if(j == 1){
                 break;
             }
             if(j == 8){
                 for(String key : outs.keySet()){
                    outs.get(key).write(results.get(key));
                }
             }
             else{
                for(String key : results.keySet()){
                    results.put(key, results.get(key) * 2);
                }
             }
         }
    }
    
    public static int createSimpleIndex(int bit, BufferedReader br, int[] numberOfRecords, boolean[] matchStructure, int[] matches, int[] results, BufferedOutputStream[] outs) throws IOException, InvalidSmilesException{     
        String line = br.readLine();
        String[] splitLine;
        IAtomContainer mol;
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        
        while(line != null){
            numberOfRecords[0]++;
            bit++;
            splitLine = line.split(";");
            mol = sp.parseSmiles(splitLine[0]);
            for(IAtom atom : mol.atoms()){
                matchStructure[atom.getAtomicNumber() - 1] = true;
            }   
            for(int i = 0; i < 109; i++){
                if(matchStructure[i]){
                    matchStructure[i] = false;
                    matches[i]++;
                    results[i]++;
                }
            }
            if(bit == 8){
                for(int i = 1; i <= 109; i++){
                    outs[i - 1].write(results[i - 1]);
                    results[i - 1] =  0;
                }
                bit = 0;
            }
            else{
                for(int i = 1; i <= 109; i++){
                    results[i - 1] *= 2;
                }
            }
            line = br.readLine();
        }
                
        return bit;
    }
    
    public static int createPairIndex(int bit, BufferedReader br, Map<String, Boolean> matchStructure, Map<String, Integer> numOfMatches, Map<String, Integer> results, Map<String, BufferedOutputStream> outs) throws IOException, InvalidSmilesException{
        String line = br.readLine();
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        String[] splitLine;
        IAtomContainer mol;
        
        while(line != null){
            bit++;
            splitLine = line.split(";");
            mol = sp.parseSmiles(splitLine[0]);
            for(IBond bond : mol.bonds()){
                IndexLibrary.processBond(bond, matchStructure); 
            }
            
            for(String key : matchStructure.keySet()){
                if(matchStructure.get(key)){
                    results.put(key, results.get(key) + 1);
                    matchStructure.put(key,false);
                    int oldNumOfMatches = numOfMatches.get(key);
                    numOfMatches.put(key,oldNumOfMatches + 1);
                }
            }
            if(bit == 8){
                for(String key : outs.keySet()){
                    outs.get(key).write(results.get(key));
                    results.put(key, 0);
                }
                bit = 0;
            }
            else{
                for(String key : results.keySet()){
                    results.put(key, results.get(key) * 2);
                }
            }
            line = br.readLine();
        }
        return bit;
    }
    
    public static int createChargeIndex(int bit, BufferedReader br, boolean[][] matchStructure, int[][] matches, int[][] results, BufferedOutputStream[][] outs) throws IOException, InvalidSmilesException, CDKException{
        String line = br.readLine();
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        String[] splitLine;
        IAtomContainer mol;
        SMARTSQueryTool querytool = new SMARTSQueryTool("c", DefaultChemObjectBuilder.getInstance());
        
        while(line != null){
            bit++;
            splitLine = line.split(";");
            mol = sp.parseSmiles(splitLine[0]);
            for(int j = 0; j < 21; j++){
                String smarts;
                if(j < 10){
                    smarts = "[" + (j - 10) + "]";
                }
                else{
                    smarts = "[+" + (j - 10) + "]";
                }
                querytool.setSmarts(smarts);
                if(!querytool.matches(mol)){
                    continue;
                }
                for(List<Integer> list : querytool.getUniqueMatchingAtoms()){
                    for(int i : list){
                        matchStructure[mol.getAtom(i).getAtomicNumber() - 1][j] = true;
                    }
                }
                for(int i = 0; i < 109; i++){
                    if(matchStructure[i][j]){
                        matchStructure[i][j] = false;
                        matches[i][j]++;
                        results[i][j]++;
                    }
                }
            }
            if(bit == 8){
                for(int i = 1; i <= 109; i++){
                    for(int j = 0; j < 21; j++){
                        outs[i - 1][j].write(results[i - 1][j]);
                        results[i - 1][j] =  0;
                    }
                }
                bit = 0;
            }
            else{
                for(int i = 1; i <= 109; i++){
                    for(int j = 0; j < 21; j++){
                        results[i - 1][j] *= 2;
                    }
                }
            }
            line = br.readLine();
         }
        return bit;
    }
    
    public static int createValenceIndex(int bit, BufferedReader br, boolean[][] matchStructure, int[][] matches, int[][] results, BufferedOutputStream[][] outs) throws IOException, CDKException{
        String line = br.readLine();
        String[] splitLine;
        IAtomContainer mol;
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        SMARTSQueryTool querytool = new SMARTSQueryTool("c", DefaultChemObjectBuilder.getInstance());
        
        while(line != null){
            bit++;
            splitLine = line.split(";");
            mol = sp.parseSmiles(splitLine[0]);
            for(int j = 0; j < 11; j++){
                String smarts = "[v" + j + "]";
                querytool.setSmarts(smarts);
                if(!querytool.matches(mol)){
                    continue;
                }
                for(List<Integer> list : querytool.getUniqueMatchingAtoms()){
                    for(int i : list){
                        matchStructure[mol.getAtom(i).getAtomicNumber() - 1][j] = true;
                    }
                }
                for(int i = 0; i < 109; i++){
                    if(matchStructure[i][j]){
                        matchStructure[i][j] = false;
                        matches[i][j]++;
                        results[i][j]++;
                    }
                }
            }
            if(bit == 8){
                for(int i = 1; i <= 109; i++){
                    for(int j = 0; j < 11; j++){
                        outs[i - 1][j].write(results[i - 1][j]);
                        results[i - 1][j] =  0;
                    }
                }
                bit = 0;
            }
            else{
                for(int i = 1; i <= 109; i++){
                    for(int j = 0; j < 11; j++){
                        results[i - 1][j] *= 2;
                    }
                }
            }
            line = br.readLine();
         }
        
        return bit;
    }
    
    public static void processBond(IBond bond, Map<String, Boolean> matchStructure){
        int atomicNumber1 = bond.getAtom(0).getAtomicNumber();
        int atomicNumber2 = bond.getAtom(1).getAtomicNumber();
        if(atomicNumber1 > atomicNumber2){
            int tmp = atomicNumber1;
            atomicNumber1 = atomicNumber2;
            atomicNumber2 = tmp;
        }
        if(atomicNumber1 == 0){ //atom is * //MAYBE BETTER CHECKING OF PSEUDOATOM
            for(int k = 1; k < atomicNumber2; k++ ){
                if(bond.getOrder() == IBond.Order.SINGLE){
                     matchStructure.put(k + "-" + atomicNumber2, true);
                     matchStructure.put(k + "~" + atomicNumber2, true);
                 }
                 else if(bond.getOrder() == IBond.Order.DOUBLE){
                     matchStructure.put(k + "=" + atomicNumber2, true);
                     matchStructure.put(k + "~" + atomicNumber2, true);
                 }
                 else if(bond.getOrder() == IBond.Order.TRIPLE){
                     matchStructure.put(k + "#" + atomicNumber2, true);
                 } 
            }
            for(int k = atomicNumber2; k <= 109 ; k++ ){
                if(bond.getOrder() == IBond.Order.SINGLE){
                     matchStructure.put(atomicNumber2 + "-" + k, true);
                     matchStructure.put(atomicNumber2 + "~" + k, true);
                 }
                 else if(bond.getOrder() == IBond.Order.DOUBLE){
                     matchStructure.put(atomicNumber2 + "=" + k, true);
                     matchStructure.put(atomicNumber2 + "~" + k, true);
                 }
                 else if(bond.getOrder() == IBond.Order.TRIPLE){
                     matchStructure.put(atomicNumber2 + "#" + k, true);
                 } 
            }
        }
        else if(bond.getOrder() == IBond.Order.SINGLE){
             matchStructure.put(atomicNumber1 + "-" + atomicNumber2, true);
             matchStructure.put(atomicNumber1 + "~" + atomicNumber2, true);
         }
         else if(bond.getOrder() == IBond.Order.DOUBLE){
             matchStructure.put(atomicNumber1 + "=" + atomicNumber2, true);
             matchStructure.put(atomicNumber1 + "~" + atomicNumber2, true);
         }
         else if(bond.getOrder() == IBond.Order.TRIPLE){
             matchStructure.put(atomicNumber1 + "#" + atomicNumber2, true);
         } 
    }
}
