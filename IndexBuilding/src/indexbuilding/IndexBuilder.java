/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package indexbuilding;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.smiles.SmilesParser;

/**
 *
 * @author Palpatine
 */
public class IndexBuilder {
    SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
    BufferedReader br;
        
    public static final String path = "../SMARTS/db/";
    public static final String file = "chembl_full1.sml";
        
    public IndexBuilder() throws FileNotFoundException, CDKException, IOException{
        makeSimpleIndex();
        makePairIndex(); 
    }
    
    private void makePairIndex() throws FileNotFoundException, IOException, InvalidSmilesException{
        long startTime = System.currentTimeMillis();
        
        new File(path + "small_index").mkdirs();
        Map<String, Boolean> matchStructure = new HashMap<String, Boolean>();
        Map<String, Integer> results = new HashMap<String, Integer>();
        Map<String, BufferedOutputStream> outs = new HashMap<String, BufferedOutputStream>();
        
        generateAllPairsBool(matchStructure);
        generateAllPairsStream(outs);
        generateAllPairsInt(results);
        
        System.out.println("GENERATING PAIRS: " + (System.currentTimeMillis() - startTime));
        
        br = new BufferedReader(new FileReader(path + file));
 
        String line = br.readLine();
        String[] splitLine;
        IAtomContainer mol;
        int bit = 0;
        long checking = 0;
        long parsing = 0;
        long writing = 0;
        int i = 0;
        while(line != null){
            bit++;
            i++;
            splitLine = line.split(";");
            startTime = System.currentTimeMillis();
            mol = sp.parseSmiles(splitLine[0]);
            parsing += System.currentTimeMillis() - startTime;
            startTime = System.currentTimeMillis();
            for(IBond bond : mol.bonds()){
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
            checking += System.currentTimeMillis() - startTime;
            
            startTime = System.currentTimeMillis();
            for(String key : matchStructure.keySet()){
                if(matchStructure.get(key)){
                    results.put(key, results.get(key) + 1);
                    matchStructure.put(key,false);
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
            writing += System.currentTimeMillis() - startTime;
            line = br.readLine();
            System.out.println("pair: " + i);
         }
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
         for(String key : outs.keySet()){
            outs.get(key).close();
         }
         br.close();
         System.out.println("Parsing: " + parsing + " Checking: " + checking + " Writing: " + writing);
    }
    
    private void generateAllPairsBool(Map<String, Boolean> map){
        for(int i = 1; i <= 109; i++){
            for(int j = i; j <= 109; j++){
                map.put(i + "-" + j, false);
                map.put(i + "=" + j, false);
                map.put(i + "#" + j, false);
                map.put(i + "~" + j, false);
            }
        }
    }
    
    private void generateAllPairsInt(Map<String, Integer> map){
        for(int i = 1; i <= 109; i++){
            for(int j = i; j <= 109; j++){
                map.put(i + "-" + j, 0);
                map.put(i + "=" + j, 0);
                map.put(i + "#" + j, 0);
                map.put(i + "~" + j, 0);
            }
        }
    }
    
    private void generateAllPairsStream(Map<String, BufferedOutputStream> map) throws FileNotFoundException{
        for(int i = 1; i <= 109; i++){
            for(int j = i; j <= 109; j++){
                map.put(i + "-" + j, new BufferedOutputStream(new FileOutputStream(path + "small_index/" + i + "-" + j + ".index")));
                map.put(i + "=" + j, new BufferedOutputStream(new FileOutputStream(path + "small_index/" + i + "=" + j + ".index")));
                map.put(i + "#" + j, new BufferedOutputStream(new FileOutputStream(path + "small_index/" + i + "#" + j + ".index")));
                map.put(i + "~" + j, new BufferedOutputStream(new FileOutputStream(path + "small_index/" + i + "~" + j + ".index")));
            }
        }
    }
    
    private void makeSimpleIndex() throws FileNotFoundException, CDKException, IOException{
        
        new File(path + "index").mkdirs();
        FileOutputStream[] outs = new FileOutputStream[109];
        int[] results = new int[109];
        int[] matches = new int[109];
        int numberOfRecords = 0;
        for(int i = 0; i < 109; i++){
            outs[i] =  new FileOutputStream(path + "small_index/" + (i + 1) + ".index");
            results[i] = 0;
            matches[i] = 0;
        }
        br = new BufferedReader(new FileReader(path + file));
 
        String line = br.readLine();
        String[] splitLine;
        IAtomContainer mol;
        int bit = 0;
        long checking = 0;
        long parsing = 0;
        int number = 0;
        while(line != null){
            numberOfRecords++;
            number++;
            bit++;
            splitLine = line.split(";");
            long startTime = System.currentTimeMillis();
            mol = sp.parseSmiles(splitLine[0]);
            parsing += System.currentTimeMillis() - startTime;
            startTime = System.currentTimeMillis();
            for(int i = 1; i <= 109; i++){
               boolean match = false;
               for(IAtom atom : mol.atoms()){
                   if(atom.getAtomicNumber() == i){
                       match = true;
                       matches[i - 1]++;
                       break;
                   }
               }   
               if(match){
                   results[i - 1]++;
               }
            }
            checking += System.currentTimeMillis() - startTime;
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
            System.out.println("simple: " + number);
            line = br.readLine();
         }
         for(int j = bit + 1; j <= 8; j++){
             if(j == 1){
                 break;
             }
             if(j == 8){
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
         PrintWriter info = new PrintWriter(path + "small_index/info.index", "UTF-8");
         info.println("numberOfRecords;" + numberOfRecords);
         for(int i = 1; i <= 109; i++){
             info.println(i + ";" + matches[i - 1]);
             outs[i - 1].close();
         }
         info.close();
         br.close();
         System.out.println("Parsing: " + parsing + " Checking: " + checking);
         
    }
}
