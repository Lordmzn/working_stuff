package it.polimi.dei.moeaframework;

import java.io.File;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.analysis.sensitivity.ResultEntry;
import org.moeaframework.analysis.sensitivity.ResultFileWriter;
import org.moeaframework.core.FrameworkException;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.spi.ProblemFactory;
import org.moeaframework.util.CommandLineUtility;
import org.moeaframework.util.Localization;

public class ExecutorUtility extends CommandLineUtility {
  
  static protected String nl = System.getProperty("line.separator");
  
  private String problem;
  private String algorithm;
  private int nfe;
  private long seed;
  private double[] epsilons;
  private File out;
  private Instrumenter instrumenter;

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
    options.addOption(OptionBuilder
        .withLongOpt("seed")
        .hasArg()
        .withArgName("value")
        .create('q'));
    options.addOption(OptionBuilder
        .withLongOpt("runtime")
        .hasArg()
        .withArgName("frequency")
        .create('f'));
    // options letters: a b c d e f g h i j l m n o p q r s t x

    // fill in option descriptions
    for (Object obj : options.getOptions()) {
      Option option = (Option) obj;

      option.setDescription(Localization.getString(ExecutorUtility.class,
          "option." + option.getLongOpt()));
    }

    return options;
  }

  // simplest experiment run
  @Override
  public void run(CommandLine commandLine) throws IOException {
    // running experiment
    NondominatedPopulation result = runExperiment(commandLine);
    // outputting results
    if (result != null) {
      ResultFileWriter writer = null;
      try {
        writer = new ResultFileWriter(ProblemFactory.getInstance().getProblem(
            commandLine.getOptionValue("problemName")), out);
        writer.append(new ResultEntry(result));
      } finally {
        writer.close();
      }
    }
    if (commandLine.hasOption("runtime")) {
      // runtime metrics output
      Accumulator accumulator = instrumenter.getLastAccumulator();
      System.out.println("# Runtime metrics");
      System.out.println("# Problem: " + problem);
      System.out.println("# Algorithm: " + algorithm);
      System.out.println("# Total NFE: " + nfe);
      System.out.println("# Seed: " + seed);
      System.out
          .println("# NFE\tElapsed Time\tPopulation Size\tGenerational Distance\tAdditiveEpsilonIndicator");
      for (int j = 0; j < accumulator.size("NFE"); j++) {
        System.out.println(accumulator.get("NFE", j) + "\t"
            + accumulator.get("Elapsed Time", j) + "\t"
            + accumulator.get("Population Size", j) + "\t"
            + accumulator.get("GenerationalDistance", j) + "\t"
            + accumulator.get("AdditiveEpsilonIndicator", j));
      }
      System.out.println("#");
    }
  }

  public NondominatedPopulation runExperiment(CommandLine commandLine)
      throws IOException {
    // setup the optimizer
    try {
      problem = (String) commandLine.getOptionValue("problemName");
      algorithm = (String) commandLine.getOptionValue("algorithm");
      if (!(algorithm.equalsIgnoreCase("eNSGAII")
          || algorithm.equalsIgnoreCase("MOEAD")
          || algorithm.equalsIgnoreCase("GDE3")
          || algorithm.equalsIgnoreCase("random")
       // || algorithm.equalsIgnoreCase("AMALGAM")
          || algorithm.equalsIgnoreCase("OMOPSO"))) {
        throw new FrameworkException(
            "Algorithm not yet coded in the Executor Utility.");
      }
      nfe = Integer.parseInt(commandLine.getOptionValue("maxEvaluation"));
      if (commandLine.hasOption("output")) {
        out = new File(commandLine.getOptionValue("output"));
      } else {
        out = new File(problem + "_" + algorithm + ".output");
      }
      try {
        seed = Long.parseLong(commandLine.getOptionValue("seed"));
        PRNG.setSeed(seed);
      } catch (NumberFormatException e) {
        System.err.println("Argument must be a number");
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
    
    NondominatedPopulation result;
    Executor executor;
    // configure the executor generic options
    executor = new Executor()
        .withProblem(problem)
        .withAlgorithm(algorithm)
        .withMaxEvaluations(nfe);
    if (commandLine.hasOption("checkpoint")) {
      int chkpFreq;
      try {
        chkpFreq = Integer.parseInt(commandLine
            .getOptionValues("checkpoint")[1]);
        chkpFreq = chkpFreq < 1 ? 10 : chkpFreq;
        executor.withCheckpointFile(
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
      executor.withProperty("populationSize", pop);
    }
    // configure algorithm specific settings
    if (commandLine.hasOption("epsilon")) {
      String[] eps_s = commandLine.getOptionValues("epsilon");
      epsilons = new double[eps_s.length];
      int idx = 0;
      for (String s : eps_s) {
        epsilons[idx] = Double.parseDouble(s);
        idx++;
      }
      if (algorithm.equalsIgnoreCase("eNSGAII")
          || algorithm.equalsIgnoreCase("random")) {
        executor.withEpsilon(epsilons);
      }
    }
    if (algorithm.equalsIgnoreCase("eNSGAII")
        || algorithm.equalsIgnoreCase("MOEAD")
        || algorithm.equalsIgnoreCase("AMALGAM")) {
      if (commandLine.hasOption("pm")) {
        String[] pm = commandLine.getOptionValues("pm");
        executor.withProperty("pm.rate", Double.parseDouble(pm[0]));
        executor.withProperty("pm.distributionIndex",
            Double.parseDouble(pm[1]));
      }
    }
    if (algorithm.equalsIgnoreCase("eNSGAII")) {
      if (commandLine.hasOption("injectionRate")) {
        double inj = Double.parseDouble(commandLine
            .getOptionValue("injectionRate"));
        executor.withProperty("injectionRate", inj);
      }
    }
    if (algorithm.equalsIgnoreCase("eNSGAII")
        || algorithm.equalsIgnoreCase("AMALGAM")) {
      if (commandLine.hasOption("sbx")) {
        String[] sbx = commandLine.getOptionValues("sbx");
        executor.withProperty("sbx.rate", Double.parseDouble(sbx[0]));
        executor.withProperty("sbx.distributionIndex",
            Double.parseDouble(sbx[1]));
      }
    }
    if (algorithm.equalsIgnoreCase("GDE3")
        || algorithm.equalsIgnoreCase("MOEAD")) {
      if (commandLine.hasOption("de")) {
        String[] de = commandLine.getOptionValues("de");
        executor.withProperty("de.crossoverRate", Double.parseDouble(de[0]));
        executor.withProperty("de.stepSize", Double.parseDouble(de[1]));
      }
    }
    if (algorithm.equalsIgnoreCase("AMALGAM")) {
      if (commandLine.hasOption("de")) {
        String[] de = commandLine.getOptionValues("de");
        executor.withProperty("de.F", Double.parseDouble(de[0]));
        executor.withProperty("de.K", Double.parseDouble(de[1]));
      }
    }
    if (algorithm.equalsIgnoreCase("MOEAD")) {
      if (commandLine.hasOption("neighborhoodSize")) {
        double neigh = Double.parseDouble(commandLine
            .getOptionValue("neighborhoodSize"));
        executor.withProperty("neighborhoodSize", neigh);
      }
      if (commandLine.hasOption("delta")) {
        double delta = Double.parseDouble(commandLine
            .getOptionValue("delta"));
        executor.withProperty("delta", delta);
      }
      if (commandLine.hasOption("eta")) {
        double eta = Double.parseDouble(commandLine
            .getOptionValue("eta"));
        executor.withProperty("eta", eta);
      }
    }
    if (algorithm.equalsIgnoreCase("OMOPSO")) {
      if (commandLine.hasOption("perturbationIndex")) {
        double pert = Double.parseDouble(commandLine
            .getOptionValue("perturbationIndex"));
        executor.withProperty("perturbationIndex", pert);
      }
      if (commandLine.hasOption("archiveSize")) {
        double arch = Double.parseDouble(commandLine
            .getOptionValue("archiveSize"));
        executor.withProperty("archiveSize", arch);
      }
    }
    if (algorithm.equalsIgnoreCase("AMALGAM")) {
      if (commandLine.hasOption("metro")) {
        double metro = Double.parseDouble(commandLine
            .getOptionValue("metro"));
        executor.withProperty("metro.jumpRate", metro);
      }
      if (commandLine.hasOption("pso")) {
        String[] pso = commandLine.getOptionValues("pso");
        executor.withProperty("pso.c1", Double.parseDouble(pso[0]));
        executor.withProperty("pso.c2", Double.parseDouble(pso[1]));
        executor.withProperty("pso.chi", Double.parseDouble(pso[2]));
      }
    }
    
    // runtime metrics
    if (commandLine.hasOption("runtime")) {
      //Create and configure Instrumenter object        
      instrumenter = new Instrumenter()
              .withProblem(problem)
              .withFrequency(Integer.parseInt(commandLine.getOptionValue("runtime")))
              .attachPopulationSizeCollector()
              .attachElapsedTimeCollector()
              .attachGenerationalDistanceCollector()
              .attachHypervolumeCollector()
              .attachAdditiveEpsilonIndicatorCollector();
      if (commandLine.hasOption("epsilon")) {
        instrumenter.withEpsilon(epsilons);
      }
      executor.withInstrumenter(instrumenter);
    }
    
    // run the model
    // try {
      // result = ex.distributeOnAllCores().run();
    result = executor.run();
    //} catch (IllegalArgumentException e) {
    //  System.err.println(e.getMessage());
    //  return null;
    //} 
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