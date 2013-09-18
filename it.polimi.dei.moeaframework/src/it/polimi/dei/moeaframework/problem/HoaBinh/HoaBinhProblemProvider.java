package it.polimi.dei.moeaframework.problem.HoaBinh;


import java.io.FileNotFoundException;
import java.io.IOException;

import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.spi.ProblemProvider;

public class HoaBinhProblemProvider extends ProblemProvider {

	@Override
	public Problem getProblem(String name) {
		if (name.equalsIgnoreCase(HoaBinh.NAME)) {
			try {
				return new HoaBinh();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public NondominatedPopulation getReferenceSet(String name) {
		// TODO Auto-generated method stub
		return null;
	}

}
