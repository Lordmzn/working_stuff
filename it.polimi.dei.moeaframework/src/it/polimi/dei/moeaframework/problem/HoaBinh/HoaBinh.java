package it.polimi.dei.moeaframework.problem.HoaBinh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.ExternalProblem;
import org.yaml.snakeyaml.Yaml;

public class HoaBinh extends ExternalProblem {
  private static final String OBJECTIVE_KEY = "objective";
  private static final String OBJECTIVE_TYPES_KEY = "types";
  private static final String CONTROL_KEY = "control";
  private static final String CONTROL_NUMBER_KEY = "numberOfVariables";
  private static final String BOUNDS_KEY = "bounds";
  
  private static final String EXECUTABLE = "./HoaBinhDPS";
  private static final String SETTINGS_FILENAME = "settings.yaml";
  private static final String CALIBRATION_DATASET = "ts_HB_flow_prec_cal_1962_1969.txt";
  private static final String INITIAL_STORAGE = "9668050000";
  private static final String POLICY_TYPE = "ann";
  private static final String NUMBER_OF_NEURONS = "4";
  private static final String NUMBER_OF_INPUTS = "3";
  protected static final String NAME = "HoaBinh";
  
  
  // parameter reading
  private int numberOfObjectives;
  // control parameter
  private int numberOfControlVariables;
  private double lowerBound;
  private double upperBound;

  @SuppressWarnings("unchecked")
  public HoaBinh() throws IOException, FileNotFoundException {
    super(EXECUTABLE, CALIBRATION_DATASET, INITIAL_STORAGE, POLICY_TYPE, NUMBER_OF_NEURONS, NUMBER_OF_INPUTS);
    // loading the same paramfile used by the model
    InputStream paramfile = new FileInputStream(new File(SETTINGS_FILENAME));
    Yaml yaml = new Yaml();
    Map<String, Object> params = (Map<String, Object>) yaml.load(paramfile);
    // number of controls
    Map<String, Object> controls = (Map<String, Object>) params.get(CONTROL_KEY);
    numberOfControlVariables = (Integer) controls.get(CONTROL_NUMBER_KEY);
    ArrayList<Integer> bounds = (ArrayList<Integer>) (controls.get(BOUNDS_KEY));
    lowerBound = bounds.get(0);
    upperBound = bounds.get(1);
    // number of objectives
    Map<String, Object> objectives = (Map<String, Object>) params
        .get(OBJECTIVE_KEY);
    numberOfObjectives = ((ArrayList<String>) objectives.get(OBJECTIVE_TYPES_KEY)).size();
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public int getNumberOfVariables() {
    return numberOfControlVariables;
  }

  @Override
  public int getNumberOfObjectives() {
    return numberOfObjectives;
  }

  @Override
  public int getNumberOfConstraints() {
    return 0;
  }

  @Override
  public Solution newSolution() {
    Solution solution = new Solution(getNumberOfVariables(),
        getNumberOfObjectives(), getNumberOfConstraints());
    for (int i = 0; i < numberOfControlVariables; i++) {
      solution.setVariable(i, new RealVariable(lowerBound, upperBound));
    }
    return solution;
  }
}