package it.polimi.dei.moeaframework;

import java.io.File;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.moeaframework.Executor;
import org.moeaframework.analysis.sensitivity.Evaluator;
import org.moeaframework.analysis.sensitivity.ResultEntry;
import org.moeaframework.analysis.sensitivity.ResultFileWriter;
import org.moeaframework.core.FrameworkException;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.util.CommandLineUtility;
import org.moeaframework.util.Localization;

public class ExecutorUtility extends CommandLineUtility {
  
  static protected String nl = System.getProperty("line.separator");
  
  private String problem;
  private String algorithm;
  private int nfe;
  private File out;

  public File getOut() {
    return out;
  }

  public void setOut(File out) {
    this.out = out;
  }

  @SuppressWarnings("static-access")
  @Override
  public Options getOptions() {
    Options options = super.getOptions();

    options.addOption(OptionBuilder
        .withLongOpt("algorithm")
        .hasArg()
        .withArgName("name")
        .isRequired()
        .create('a'));
    options.addOption(OptionBuilder
        .withLongOpt("maxEvaluation")
        .hasArg()
        .withArgName("nfe")
        .isRequired()
        .create('n'));
    options.addOption(OptionBuilder
        .withLongOpt("output")
        .hasArg()
        .withArgName("file")
        .create('o'));
    options.addOption(OptionBuilder
        .withLongOpt("epsilon")
        .hasArgs()
        .withValueSeparator(',')
        .withArgName("e1,e2,...")
        .create('e'));
    options.addOption(OptionBuilder
        .withLongOpt("populationSize")
        .hasArg()
        .withArgName("value")
        .create('p'));
    options.addOption(OptionBuilder
        .withLongOpt("injectionRate")
        .hasArg()
        .withArgName("value")
        .create('i'));
    options.addOption(OptionBuilder
        .withLongOpt("sbx")
        .hasArgs(2)
        .withValueSeparator(',')
        .withArgName("rate,distributionIndex")
        .create('x'));
    options.addOption(OptionBuilder
        .withLongOpt("pm")
        .hasArgs(2)
        .withValueSeparator(',')
        .withArgName("rate,distributionIndex")
        .create('m'));
    options.addOption(OptionBuilder
        .withLongOpt("de")
        .hasArgs(2)
        .withValueSeparator(',')
        .withArgName("crossoverRate,stepSize")
        .create('d'));
    options.addOption(OptionBuilder
        .withLongOpt("delta")
        .hasArg()
        .withArgName("value")
        .create('l'));
    options.addOption(OptionBuilder
        .withLongOpt("eta")
        .hasArg()
        .withArgName("value")
        .create('t'));
    options.addOption(OptionBuilder
        .withLongOpt("neighborhood")
        .hasArg()
        .withArgName("size")
        .create('g'));
    options.addOption(OptionBuilder
        .withLongOpt("checkpoint")
        .hasArgs(2)
        .withValueSeparator(',')
        .withArgName("file,frequency")
        .create('c'));
    options.addOption(OptionBuilder
        .withLongOpt("problemName")
        .isRequired()
        .hasArg()
        .withArgName("name")
        .create('b'));
    options.addOption(OptionBuilder
        .withLongOpt("perturbationIndex")
        .hasArg()
        .withArgName("value")
        .create('r'));
    options.addOption(OptionBuilder
        .withLongOpt("archiveSize")
        .hasArg()
        .withArgName("value")
        .create('h'));
    options.addOption(OptionBuilder
        .withLongOpt("metro")
        .hasArg()
        .withArgName("jumpRate")
        .create('j'));
    options.addOption(OptionBuilder
        .withLongOpt("pso")
        .hasArgs(3)
        .withValueSeparator(',')
        .withArgName("c1,c2,chi")
        .create('s'));
    // options letters: a b c d e g h i j l m n o p r s t x

    // fill in option descriptions
    for (Object obj : options.getOptions()) {
      Option option = (Option) obj;

      option.setDescription(Localization.getString(Evaluator.class,
          "option." + option.getLongOpt()));
    }

    return options;
  }
  
  // simplest experiment run
  @Override
  public void run(CommandLine commandLine) throws IOException {
    // running experiment
    NondominatedPopulation result = runExperiment(commandLine);
    // outputting
    ResultFileWriter writer = new ResultFileWriter(null, out);
    writer.append(new ResultEntry(result));
    writer.close();
  }

  public NondominatedPopulation runExperiment(CommandLine commandLine) throws IOException {
    // setup the optimizer
    try {
      problem = (String) commandLine.getOptionValue("problemName");
      algorithm = (String) commandLine.getOptionValue("algorithm");
      nfe = Integer.parseInt(commandLine.getOptionValue("maxEvaluation"));
      if (commandLine.hasOption("output")) {
        out = new File(commandLine.getOptionValue("output"));
      } else {
        out = new File(problem + "_" + algorithm + ".output");
      }
      if (!(algorithm.equalsIgnoreCase("eNSGAII")
          || algorithm.equalsIgnoreCase("MOEAD")
          || algorithm.equalsIgnoreCase("GDE3")
          || algorithm.equalsIgnoreCase("random")
          //|| algorithm.equalsIgnoreCase("AMALGAM")
          || algorithm.equalsIgnoreCase("OMOPSO"))) {
        throw new FrameworkException("Algorithm not yet coded in the Executor Utility.");
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
    
    NondominatedPopulation result;
    Executor ex;
    // configure the executor generic options
    ex = new Executor()
        .withProblem(problem)
        .withAlgorithm(algorithm)
        .withMaxEvaluations(nfe);
    if (commandLine.hasOption("checkpoint")) {
      int chkpFreq;
      try {
        chkpFreq = Integer.parseInt(commandLine
            .getOptionValues("checkpoint")[1]);
        chkpFreq = chkpFreq < 1 ? 10 : chkpFreq;
        ex.withCheckpointFile(
            new File(commandLine.getOptionValues("checkpoint")[0]))
            .withCheckpointFrequency(chkpFreq);
      } catch (NumberFormatException e) {
        System.err.println(e.getMessage());
        return null;
      }
    }
    if (commandLine.hasOption("populationSize")
        && !(algorithm.equalsIgnoreCase("random"))) {
      double pop = Double.parseDouble(commandLine
          .getOptionValue("populationSize"));
      ex.withProperty("populationSize", pop);
    }
    // configure algorithm specific settings
    if (algorithm.equalsIgnoreCase("eNSGAII")
        || algorithm.equalsIgnoreCase("random")) {
      if (commandLine.hasOption("epsilon")) {
        String[] eps_s = commandLine.getOptionValues("epsilon");
        double[] eps = new double[eps_s.length];
        int idx = 0;
        for (String s : eps_s) {
          eps[idx] = Double.parseDouble(s);
          idx++;
        }
        ex.withEpsilon(eps);
      }
    }
    if (algorithm.equalsIgnoreCase("eNSGAII")
        || algorithm.equalsIgnoreCase("MOEAD")
        || algorithm.equalsIgnoreCase("AMALGAM")) {
      if (commandLine.hasOption("pm")) {
        String[] pm = commandLine.getOptionValues("pm");
        ex.withProperty("pm.rate", Double.parseDouble(pm[0]));
        ex.withProperty("pm.distributionIndex",
            Double.parseDouble(pm[1]));
      }
    }
    if (algorithm.equalsIgnoreCase("eNSGAII")) {
      if (commandLine.hasOption("injectionRate")) {
        double inj = Double.parseDouble(commandLine
            .getOptionValue("injectionRate"));
        ex.withProperty("injectionRate", inj);
      }
    }
    if (algorithm.equalsIgnoreCase("eNSGAII")
        || algorithm.equalsIgnoreCase("AMALGAM")) {
      if (commandLine.hasOption("sbx")) {
        String[] sbx = commandLine.getOptionValues("sbx");
        ex.withProperty("sbx.rate", Double.parseDouble(sbx[0]));
        ex.withProperty("sbx.distributionIndex",
            Double.parseDouble(sbx[1]));
      }
    }
    if (algorithm.equalsIgnoreCase("GDE3")
        || algorithm.equalsIgnoreCase("MOEAD")) {
      if (commandLine.hasOption("de")) {
        String[] de = commandLine.getOptionValues("de");
        ex.withProperty("de.crossoverRate", Double.parseDouble(de[0]));
        ex.withProperty("de.stepSize", Double.parseDouble(de[1]));
      }
    }
    if (algorithm.equalsIgnoreCase("AMALGAM")) {
      if (commandLine.hasOption("de")) {
        String[] de = commandLine.getOptionValues("de");
        ex.withProperty("de.F", Double.parseDouble(de[0]));
        ex.withProperty("de.K", Double.parseDouble(de[1]));
      }
    }
    if (algorithm.equalsIgnoreCase("MOEAD")) {
      if (commandLine.hasOption("neighborhoodSize")) {
        double neigh = Double.parseDouble(commandLine
            .getOptionValue("neighborhoodSize"));
        ex.withProperty("neighborhoodSize", neigh);
      }
      if (commandLine.hasOption("delta")) {
        double delta = Double.parseDouble(commandLine
            .getOptionValue("delta"));
        ex.withProperty("delta", delta);
      }
      if (commandLine.hasOption("eta")) {
        double eta = Double.parseDouble(commandLine
            .getOptionValue("eta"));
        ex.withProperty("eta", eta);
      }
    }
    if (algorithm.equalsIgnoreCase("OMOPSO")) {
      if (commandLine.hasOption("perturbationIndex")) {
        double pert = Double.parseDouble(commandLine
            .getOptionValue("perturbationIndex"));
        ex.withProperty("perturbationIndex", pert);
      }
      if (commandLine.hasOption("archiveSize")) {
        double arch = Double.parseDouble(commandLine
            .getOptionValue("archiveSize"));
        ex.withProperty("archiveSize", arch);
      }
    }
    if (algorithm.equalsIgnoreCase("AMALGAM")) {
      if (commandLine.hasOption("metro")) {
        double metro = Double.parseDouble(commandLine
            .getOptionValue("metro"));
        ex.withProperty("metro.jumpRate", metro);
      }
      if (commandLine.hasOption("pso")) {
        String[] pso = commandLine.getOptionValues("pso");
        ex.withProperty("pso.c1", Double.parseDouble(pso[0]));
        ex.withProperty("pso.c2", Double.parseDouble(pso[1]));
        ex.withProperty("pso.chi", Double.parseDouble(pso[2]));
      }
    }
    // run the model
    try {
      // result = ex.distributeOnAllCores().run();
      result = ex.run();
    } catch (IllegalArgumentException e) {
      System.err.println(e.getMessage());
      return null;
    }
    return result;
  }
  
  public static void main(String[] args) {
    try {
      new ExecutorUtility().start(args);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}