package edu.stanford.cs276;

import java.io.IOException;

public class TestEmpiricalCostModel {

	public static void main( String[] args ) throws Exception{
			String editsFile = args[0] ;
			System.out.println( editsFile );
			try {
				EmpiricalCostModel testCostModel = new EmpiricalCostModel( editsFile );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 }
	
	
}

