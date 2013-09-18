package it.polimi.dei.moeaframework.problem.MOGLE;


import java.io.FileNotFoundException;
import java.io.IOException;

import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Problem;
import org.moeaframework.core.spi.ProblemProvider;

public class GLEmodelProblemProvider extends ProblemProvider {

	@Override
	public Problem getProblem(String name) {
		if (name.equalsIgnoreCase(GLEmodel.NAME)) {
			try {
				return new GLEmodel();
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
