/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sdftosmiles;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import org.openscience.cdk.*;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.smiles.*;
import org.openscience.cdk.io.random.RandomAccessSDFReader;

/**
 *
 * @author Palpatine
 */
public class SDFtoSMILES {
    static String input = "../SMARTS/db/chembl_full1.sdf";
    static String output = "../SMARTS/db/chembl_full1.sml";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, CDKException {
        BufferedOutputStream writer =  new BufferedOutputStream(new FileOutputStream(output));
       
        int j = 0;
        
        File f;
        
        SmilesGenerator sg;
        f = new File(input);

        System.out.println(input);
        IteratingSDFReader reader = new IteratingSDFReader(new FileInputStream(f), DefaultChemObjectBuilder.getInstance());

        sg = SmilesGenerator.generic().aromatic();
        sg.setUseAromaticityFlag(true);

        IAtomContainer mol;

        while(reader.hasNext()){
            mol = (IAtomContainer)reader.next();
            j++;
            String smiles = sg.create(mol);
            if(!smiles.equals("")){
                writer.write(sg.create(mol).getBytes());
                writer.write(";".getBytes());
                writer.write(((String)mol.getProperties().get("chembl_id")).getBytes());
                writer.write("\n".getBytes());
            }
            else{
                System.out.println("HUUUUGE");
            }     
            System.out.println("done" + j);
        }
        
        System.out.println("done" + j);
        writer.close();
    }
}
