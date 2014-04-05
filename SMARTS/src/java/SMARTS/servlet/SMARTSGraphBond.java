/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SMARTS.servlet;

import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.isomorphism.matchers.IQueryBond;
import org.openscience.cdk.isomorphism.matchers.smarts.AromaticQueryBond;
import org.openscience.cdk.isomorphism.matchers.smarts.LogicalOperatorBond;
import org.openscience.cdk.isomorphism.matchers.smarts.OrderQueryBond;


/**
 *
 * @author Palpatine
 */
public class SMARTSGraphBond {
    public String firstAtomId;
    public String secondAtomId;
    public SMARTSGraphAtom firstAtom;
    public SMARTSGraphAtom secondAtom;
    boolean singleBond = false;
    boolean doubleBond = false;
    boolean tripleBond = false;
    boolean aromaticBond = false;
    int typeCount = 0;
    
    public SMARTSGraphBond(SMARTSGraphAtom a1, SMARTSGraphAtom a2, IBond bond){
        /*firstAtom = a1;
        secondAtom = a2;
        recursiveBondParse(bond);*/
    }
    
    private void recursiveBondParse(IBond bond){
        if(bond instanceof LogicalOperatorBond){
            IQueryBond left = ((LogicalOperatorBond)bond).getLeft();
            IQueryBond right = ((LogicalOperatorBond)bond).getRight(); 
            recursiveBondParse((IBond)left);
            recursiveBondParse((IBond)right);
        }
        else if(bond instanceof AromaticQueryBond){
            aromaticBond = true;
            typeCount++;
        }
        else if(bond instanceof OrderQueryBond){
            IBond.Order order = bond.getOrder();
            if(order == IBond.Order.SINGLE){
                singleBond = true;
                typeCount++;
            }
            if(order == IBond.Order.DOUBLE){
                doubleBond = true;
                typeCount++;
            }
            if(order == IBond.Order.TRIPLE){
                tripleBond = true;
                typeCount++;
            }
        }
    }
}
