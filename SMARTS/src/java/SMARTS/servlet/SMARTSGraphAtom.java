/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SMARTS.servlet;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.isomorphism.matchers.IQueryAtom;
import org.openscience.cdk.isomorphism.matchers.smarts.AliphaticAtom;
import org.openscience.cdk.isomorphism.matchers.smarts.AromaticAtom;
import org.openscience.cdk.isomorphism.matchers.smarts.AtomicNumberAtom;
import org.openscience.cdk.isomorphism.matchers.smarts.LogicalOperatorAtom;
import org.openscience.cdk.isomorphism.matchers.smarts.SMARTSAtom;
import org.openscience.cdk.smiles.SmilesGenerator;

/**
 *
 * @author Palpatine
 */
public class SMARTSGraphAtom {
    List<Integer> posibleAromaticNumbers = new ArrayList<Integer>();
    List<Integer> posibleAliphaticNumbers = new ArrayList<Integer>();
    List<SMARTSGraphBond> bonds = new ArrayList<SMARTSGraphBond>();
    
    public SMARTSGraphAtom(IAtom atom) throws CDKException{
        //recursiveAtomParse(atom);
    }
    
    private void recursiveAtomParse(IAtom atom) throws CDKException{
        SmilesGenerator sg = SmilesGenerator.generic().aromatic();
        
        IAtomContainer cont = new AtomContainer();
        
        SMARTSAtom atom2 = (SMARTSAtom) atom;
        LogicalOperatorAtom atom3 = (LogicalOperatorAtom)atom2;
        cont.addAtom(atom);
        
        String smiles = sg.create(cont);
        System.out.println(smiles);
        
        //TODO: parsing SMARTSAtom
        
        System.out.println(atom.getClass());
        if(atom.getClass().toString().equals("class org.openscience.cdk.isomorphism.matchers.smarts.LogicalOperatorAtom$Conjunction")){
            System.out.println();
            /*IQueryAtom left = ((LogicalOperatorAtom)atom).getLeft();
            IQueryAtom right = ((LogicalOperatorAtom)atom).getRight();
            if(left instanceof AtomicNumberAtom && right instanceof AliphaticAtom){
                posibleAliphaticNumbers.add(((AtomicNumberAtom)left).getAtomicNumber());
            }
            else if(left instanceof AtomicNumberAtom && right instanceof AromaticAtom){
                posibleAromaticNumbers.add(((AtomicNumberAtom)left).getAtomicNumber());
            }
            else{
                recursiveAtomParse(left);
                recursiveAtomParse(right);
            }*/
            
        }else{
            System.out.println("Something wrong with atom structure");
        }
    }
}
