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
import indexlibrary.IndexLibrary;

/**
 *
 * @author Palpatine
 */
public class IndexBuilder {
    BufferedReader br;
    
    public static final String path = "../SMARTS/db/";
    public static final String file = "chembl_test_small1.sml";
    public static final String index = "tiny_index";
    
    PrintWriter info;   
    
        
    public IndexBuilder() throws FileNotFoundException, CDKException, IOException{
        boolean success = true;
        if(!new File(path + index).exists()){
            success = new File(path + index).mkdirs();
        }
        
        if(!success){
            System.out.println("Can not create folder " + path + index);
            return;
        }
        
        info = new PrintWriter(path + index + "/info.index", "UTF-8");
        
        makeSimpleIndex();
        makePairIndex(); 
        makeChargeIndex();
        makeValenceIndex();
        
        info.close();
    }
    
    private void makeValenceIndex() throws FileNotFoundException, IOException, InvalidSmilesException, CDKException {
        BufferedOutputStream[][] outs = new BufferedOutputStream[109][11];
        int[][] results = new int[109][11];
        int[][] matches = new int[109][11];
        boolean[][] matchStructure = new boolean[109][11];
        for(int i = 0; i < 109; i++){
            for(int j = 0; j < 11; j++){
                outs[i][j] =  new BufferedOutputStream(new FileOutputStream(path + index + "/" + (i + 1) + ";v" + j + ".index"));
                results[i][j] = 0;
                matches[i][j] = 0;
            }
        }
        br = new BufferedReader(new FileReader(path + file));
 
        
        int bit = 0;
        bit = IndexLibrary.createValenceIndex(bit, br, matchStructure, matches, results, outs);
        IndexLibrary.closeByte(bit, results, outs, 11);
        for(int i = 1; i <= 109; i++){
            for(int j = 0; j < 11; j++){ 
               info.println(i + ";v" + j + ";;" + matches[i - 1][j]);
               outs[i - 1][j].close();
            }
        }
        for(int i = 1; i <= 109; i++){
            for(int j = 0; j < 11; j++){ 
               if(matches[i - 1][j] == 0){
                   String fileName = i + ";v" + j + ".index";
                   File f = new File(path + index + "/" + fileName);
                   f.delete();
               }
            }
        }
        br.close();
    }
    
    
    
    private void makeChargeIndex() throws FileNotFoundException, IOException, InvalidSmilesException, CDKException {
        BufferedOutputStream[][] outs = new BufferedOutputStream[109][21]; //charge = index -10
        int[][] results = new int[109][21];
        int[][] matches = new int[109][21];
        boolean[][] matchStructure = new boolean[109][21];
        for(int i = 0; i < 109; i++){
            for(int j = 0; j < 21; j++){
                if(j < 10){
                    outs[i][j] =  new BufferedOutputStream(new FileOutputStream(path + index + "/" + (i + 1) + ";" + (j - 10) + ".index"));
                }
                else{
                    outs[i][j] =  new BufferedOutputStream(new FileOutputStream(path + index + "/" + (i + 1) + ";+" + (j - 10) + ".index"));
                }
                results[i][j] = 0;
                matches[i][j] = 0;
            }
        }
        br = new BufferedReader(new FileReader(path + file));
        int bit = 0;
        
        bit = IndexLibrary.createChargeIndex(bit, br, matchStructure, matches, results, outs);
        IndexLibrary.closeByte(bit, results, outs, 21);

        for(int i = 1; i <= 109; i++){
            for(int j = 0; j < 21; j++){ 
               String fileName = i + ";";
               if(j >= 10){
                   fileName += "+";
               }
               fileName += (j - 10);
               info.println(fileName + ";;" + matches[i - 1][j]);
               outs[i - 1][j].close();
            }
        }
        for(int i = 1; i <= 109; i++){
            for(int j = 0; j < 21; j++){ 
               if(matches[i - 1][j] == 0){
                   String fileName = i + ";";
                   if(j >= 10){
                       fileName += "+";
                   }
                   fileName += (j - 10) + ".index";
                   File f = new File(path + index + "/" + fileName);
                   f.delete();
               }
            }
        }
        br.close();
    }
    
    private void makePairIndex() throws FileNotFoundException, IOException, InvalidSmilesException{    
        Map<String, Boolean> matchStructure = new HashMap<String, Boolean>();
        Map<String, Integer> numOfMatches = new HashMap<String, Integer>();
        Map<String, Integer> results = new HashMap<String, Integer>();
        Map<String, BufferedOutputStream> outs = new HashMap<String, BufferedOutputStream>();
        
        generateAllPairsBool(matchStructure);
        generateAllPairsInt(numOfMatches);
        generateAllPairsStream(outs);
        generateAllPairsInt(results);
        
        br = new BufferedReader(new FileReader(path + file));

        int bit = 0;
        bit = IndexLibrary.createPairIndex(bit, br, matchStructure, numOfMatches, results, outs);
        IndexLibrary.closeByte(bit, results, outs);
        for(String key : outs.keySet()){
           outs.get(key).close();
        }
        for(String key : numOfMatches.keySet()){
            if(numOfMatches.get(key) == 0){
                File f = new File(path + index + "/" + key + ".index");
                f.delete();
            }
            info.println(key + ";;" + numOfMatches.get(key));
        }
        br.close();
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
                map.put(i + "-" + j, new BufferedOutputStream(new FileOutputStream(path + index + "/" + i + "-" + j + ".index")));
                map.put(i + "=" + j, new BufferedOutputStream(new FileOutputStream(path + index + "/" + i + "=" + j + ".index")));
                map.put(i + "#" + j, new BufferedOutputStream(new FileOutputStream(path + index + "/" + i + "#" + j + ".index")));
                map.put(i + "~" + j, new BufferedOutputStream(new FileOutputStream(path + index + "/" + i + "~" + j + ".index")));
            }
        }
    }
    
    private void makeSimpleIndex() throws FileNotFoundException, CDKException, IOException{
       
        BufferedOutputStream[] outs = new BufferedOutputStream[109];
        int[] results = new int[109];
        int[] matches = new int[109];
        boolean[] matchStructure = new boolean[109];
        int[] numberOfRecords = new int[1];
        numberOfRecords[0] = 0;
        for(int i = 0; i < 109; i++){
            outs[i] =  new BufferedOutputStream(new FileOutputStream(path + index + "/" + (i + 1) + ".index"));
            results[i] = 0;
            matches[i] = 0;
        }
        br = new BufferedReader(new FileReader(path + file));
 
        int bit = 0;
        bit = IndexLibrary.createSimpleIndex(bit, br, numberOfRecords, matchStructure, matches, results, outs);
       
        IndexLibrary.closeByte(bit, results, outs);

        info.println("numberOfRecords;;" + numberOfRecords[0]);
        for(int i = 1; i <= 109; i++){
            info.println(i + ";;" + matches[i - 1]);
            outs[i - 1].close();
        }
        for(int i = 1; i <= 109; i++){
            if(matches[i - 1] == 0){
                File f = new File(path + index + "/" + i + ".index");
                f.delete();
            }
        }
        br.close();
         
    }
}
