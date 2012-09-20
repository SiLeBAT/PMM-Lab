package org.hsh.bfr.db.gui.dbtable.sorter;
/**
 * 
 */


import java.util.Comparator;

/**
 * @author Armin
 *
 */
public class MyIntegerSorter implements Comparator<Integer> {

  @Override
  public int compare(Integer o1, Integer o2) {
  	//System.out.println("MyIntegerSorter\t" + o1 + "\t" + o2);
  	if (o1 == null && o2 == null) return 0;
  	else if (o1 == null) return 1;
  	else if (o2 == null) return -1;
  	else return o1.compareTo(o2);
  }
}