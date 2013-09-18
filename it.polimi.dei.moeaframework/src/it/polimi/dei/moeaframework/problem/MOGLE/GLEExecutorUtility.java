package it.polimi.dei.moeaframework.problem.MOGLE;

import it.polimi.dei.moeaframework.ExecutorUtility;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.RealVariable;

public class GLEExecutorUtility extends ExecutorUtility {
	
	@SuppressWarnings("static-access")
	@Override
	public Options getOptions() {
		Options options = super.getOptions();
		
		options.addOption(OptionBuilder
				.withLongOpt("seed")
				.hasArg()
				.withArgName("value")
				.create('q'));
		
		return options;
	}
	
	@Override
	public void run(CommandLine commandLine) throws IOException {
		if (!commandLine.getOptionValue("problemName").equalsIgnoreCase("glemodel")) {
			System.err.println("You're using the wrong utility, dude! Problem name should be: GLEmodel");
			return;
		}
		GLEmodel.LOGFILE_ID = commandLine.getOptionValue("algorithm");
		if (commandLine.hasOption("seed")) {
			GLEmodel.LOGFILE_ID += (".seed" + commandLine.getOptionValue("seed"));
		}
		// run the experiment using the method of the parent class
		NondominatedPopulation result = runExperiment(commandLine);
		// print results
		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getOut())));
		if (GLEmodel.isUsingRBFML() || GLEmodel.isUsingIDW()) {
			out.write(" with " + result.get(0).getNumberOfVariables()
					+ " controls varying between "
					+ Double.toString(((RealVariable) result.get(0).getVariable(5)).getLowerBound())
					+ " and "
					+ Double.toString(((RealVariable) result.get(0).getVariable(5)).getUpperBound()) 
					+ " for cell (6,6)." + nl);
			for (Solution solution : result) {
				// writing controls
				for (int i = 0; i < solution.getNumberOfVariables(); i++) {
					double val = ((RealVariable) solution.getVariable(i))
							.getValue();
					out.write(Integer.toString((int) (val > 0.0 ? Math
							.floor(val + 0.5) : Math.ceil(val - 0.5))));
					out.write(' ');
				}
				// writing objs
				for (int i = 0; i < solution.getNumberOfObjectives(); i++) {
					out.write(Double.toString(solution.getObjective(i)));
					out.write(' ');
				}
				// writing costraints
				for (int i = 0; i < solution.getNumberOfConstraints(); i++) {
					out.write(Double.toString(solution.getConstraint(i)));
					out.write(' ');
				}
				out.write(nl);
			}
		} else {
			out.write(" with " + result.get(0).getNumberOfVariables()
					+ " controls varying between "
					+ Double.toString(((RealVariable) result.get(0).getVariable(5)).getLowerBound())
					+ " and "
					+ Double.toString(((RealVariable) result.get(0).getVariable(5)).getUpperBound()) 
					+ " for cell (6,6)." + nl);
			for (Solution solution : result) {
				// writing controls
				for (int i = 0; i < solution.getNumberOfVariables(); i++) {
					double val = ((RealVariable) solution.getVariable(i))
							.getValue();
					out.write(Integer.toString((int) (val > 0.0 ? Math
							.floor(val + 0.5) : Math.ceil(val - 0.5))));
					out.write(' ');
				}
				// writing objs
				for (int i = 0; i < solution.getNumberOfObjectives(); i++) {
					out.write(Double.toString(solution.getObjective(i)));
					out.write(' ');
				}
				// writing costraints
				for (int i = 0; i < solution.getNumberOfConstraints(); i++) {
					out.write(Double.toString(solution.getConstraint(i)));
					out.write(' ');
				}
				out.write(nl);
			}
		}
		out.write("#" + nl);
		out.flush();
		out.close();
	}
	
	/**
	 * Requires these parameters:
	 * 
	 * @param args
	 *            should contain: - -h to display the help OR - -a ALGORITHM the
	 *            algorithm used - -n NFE the number of function evaluation AND
	 *            other optional algorithm related parameters
	 */
	public static void main(String[] args) {
		try {
      new GLEExecutorUtility().start(args);
    } catch (Exception e) {
      e.printStackTrace();
    }
	}
}