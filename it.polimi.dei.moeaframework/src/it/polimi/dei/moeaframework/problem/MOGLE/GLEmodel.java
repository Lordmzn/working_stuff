package it.polimi.dei.moeaframework.problem.MOGLE;


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

public class GLEmodel extends ExternalProblem {
	private static final String EXECUTABLE = "./GLEmodel4moea";
	private static final String PARAMFILE_NAME = "paramfile.yaml";
	public static String LOGFILE_ID = "";
	protected static final String NAME = "GLEmodel"; // public to be read from GLEmodelProblemProvider

	// yaml file keys
	private static final String OBJECTIVE_KEY = "objective";
	private static final String OBJECTIVE_TYPES_KEY = "types";
	private static final String CONTROLS_KEY = "controls";
	private static final String BOUNDS_KEY = "bounds";
	private static final String STATE_KEY = "state";
	private static final String INITIAL_STATE_KEY = "initial";
	private static final String SOURCETYPE_KEY = "sourceType";
	private static final String SOURCE_PYRAMID_KEY = "PYRAMID";
	private static final String SOURCE_WEDGE_KEY = "WEDGE";
	private static final String SOURCE_DATAFILE_KEY = "DATAFILE";
	private static final String DIMENSION_KEY = "dimensions";
	private static final String BASINLENGTH_KEY = "basinLength";
	private static final String SHAPEFACTOR_KEY = "shapeFactor";
	private static final String INTERP_KEY = "interpolator";
	private static final String INTERP_TYPE_KEY = "type";
	private static final String INTERP_NONE_KEY = "none";
	private static final String INTERP_RBF_ML = "RBF-ML";
	private static final String INTERP_IWD_KEY = "IDW";
	private static final String INTERP_IDW_CONTROLS_KEY = "IDW-CONTROLS";
	private static final String INTERP_PARAMETER_KEY = "parameters";

	// parameter reading
	private int nObj;
	private int nr;
	private int nc;
	private boolean isPyramid;
	private boolean isWedge;
	private int lBasin;
	private int wBasin;
	private double shapeFactor;
	// control parameter
	private int lbound;
	private int ubound;
	private int numberOfControl;
	static private boolean isUsingRBFML;
	static private boolean isUsingIWD;
	static private boolean isUsingIDWCONTROLS;
	private int sample_frequence;
//	private int elStep;

	@SuppressWarnings("unchecked")
	public GLEmodel() throws IOException, FileNotFoundException {
		super(EXECUTABLE, PARAMFILE_NAME, LOGFILE_ID);
		isPyramid = false;
		isWedge = false;
		isUsingRBFML = false;
		isUsingIWD = false;
		isUsingIDWCONTROLS = false;
//		elStep = 4;
		
		// loading the same paramfile used by the model
		InputStream paramfile = new FileInputStream(new File(PARAMFILE_NAME));
		Yaml yaml = new Yaml();
		Map<String, Object> params = (Map<String, Object>) yaml.load(paramfile);
		// number of objectives
		Map<String, Object> objectives = (Map<String, Object>) params
				.get(OBJECTIVE_KEY);
		nObj = ((ArrayList<String>) objectives.get(OBJECTIVE_TYPES_KEY)).size();
		
		
		// initial state
		Map<String, Object> initialState = (Map<String, Object>) (((Map<String, Object>) params
				.get(STATE_KEY)).get(INITIAL_STATE_KEY));
		if (initialState.get(SOURCETYPE_KEY).toString()
				.equals(SOURCE_WEDGE_KEY)) {
			isWedge = true;
			lBasin = (Integer) initialState.get(BASINLENGTH_KEY);
			shapeFactor = (Double) initialState.get(SHAPEFACTOR_KEY);
			wBasin = (int) (lBasin * shapeFactor);
			nr = (int) (lBasin * 1.2) + 2;
			nc = (int) (wBasin * 1.2) + 2;
		} else if (initialState.get(SOURCETYPE_KEY).toString()
				.equals(SOURCE_PYRAMID_KEY)) {
			isPyramid = true;
			lBasin = 0;
			wBasin = 0;
			shapeFactor = 0.0;
			ArrayList<Integer> dim = (ArrayList<Integer>) ((Map<String, Object>) (params
					.get(STATE_KEY))).get(DIMENSION_KEY);
			nr = dim.get(0);
			nc = dim.get(1);
		} else if (initialState.get(SOURCETYPE_KEY).toString()
				.equals(SOURCE_DATAFILE_KEY)) {
			System.out
					.println("Loading a demFile as starting dem isn't supported.");
			throw new FileNotFoundException();
		}
		
		// control bounds
		ArrayList<Integer> bounds = (ArrayList<Integer>) (((Map<String, Object>) params
				.get(CONTROLS_KEY)).get(BOUNDS_KEY));
		lbound = bounds.get(1) + 1;
		ubound = bounds.get(0) - 1;
		// number of controls
		numberOfControl = (nr - 2) * (nc - 2);
		Map<String, Object> interpolator = (Map<String, Object>) params
				.get(INTERP_KEY);
		if (interpolator != null) {
			if (interpolator.get(INTERP_TYPE_KEY).toString()
					.equals(INTERP_NONE_KEY)) {
				// do nothing since number of control is already right
			} else if (interpolator.get(INTERP_TYPE_KEY).toString()
					.equals(INTERP_RBF_ML)) {
				isUsingRBFML = true;
				ArrayList<Number> interpParam = (ArrayList<Number>) interpolator
						.get(INTERP_PARAMETER_KEY);
				if (interpParam != null && interpParam.size() >= 2) {
					sample_frequence = (Integer) interpParam.get(0);
					numberOfControl = ((nr - 2) / sample_frequence) * ((nc - 2) / sample_frequence);
				}
			} else if (interpolator.get(INTERP_TYPE_KEY).toString()
					.equals(INTERP_IWD_KEY) || interpolator.get(INTERP_TYPE_KEY).toString()
					.equals(INTERP_IDW_CONTROLS_KEY)) {
				ArrayList<Number> interpParam = (ArrayList<Number>) interpolator
						.get(INTERP_PARAMETER_KEY);
				if (interpParam != null && interpParam.size() >= 2) {
					sample_frequence = (Integer) interpParam.get(0);
					numberOfControl = ((nr - 2) / sample_frequence) * ((nc - 2) / sample_frequence);
				}
			}
		}
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getNumberOfVariables() {
		return numberOfControl;
	}

	@Override
	public int getNumberOfObjectives() {
		return nObj;
	}

	@Override
	public int getNumberOfConstraints() {
		return 1;
	}
	
	static public boolean isUsingRBFML() {
		return isUsingRBFML;
	}
	
	static public boolean isUsingIDW() {
		return isUsingIWD;
	}
	
	static public boolean isUsingIDWCONTROLS() {
		return isUsingIDWCONTROLS;
	}

	@Override
	public Solution newSolution() {
		Solution solution = new Solution(getNumberOfVariables(),
				getNumberOfObjectives(), getNumberOfConstraints());
		if (isUsingRBFML || isUsingIWD) {
			for (int i = 0; i < numberOfControl; i++) {
				solution.setVariable(i, new RealVariable(1.0, (double)ubound));
			}
		} else if (!isUsingIWD && !isUsingRBFML && (isPyramid || isWedge)) {
			for (int i = 0; i < numberOfControl; i++) {
				solution.setVariable(i, new RealVariable((double) lbound,
						(double) ubound));
			}
		} else {
			System.out.println("Bad dem source initialization");
			return null;
		}
	return solution;
}
}
			
/*
			if (isPyramid) {
				int lb, nc_, nr_, el;
				int nc_c = (nc - 2 + 1) / 2;
				int nr_c = (nr - 2 + 1) / 2;
				for (int i = 0; i < getNumberOfVariables(); i++) {
					nc_ = (i % (nc - 2)) + 1;
					nr_ = (i / (nc - 2)) + 1;
					nc = (nc_ > nc_c) ? nc - nc_ - 1 : nc_;
					nr = (nr_ > nr_c) ? nr - nr_ - 1 : nr_;
					el = nr > nc ? nc * elStep : nr * elStep;
					if (lbound + el - 1 < 0) {
						lb = -el + 1;
					} else {
						lb = lbound;
					}
					solution.setVariable(i, new RealVariable((double)lb, (double)ubound));
				}
			} else if (isWedge) {
				int[][] elevationData;
				elevationData = new int[nr][nc];
				// create the wedge larger surface
				for (int i = 1; i < lBasin + 1; i++) {
					for (int j = 1; j < nc - 1; j++) {
						elevationData[i][j] = i * elStep;
					}
				}
				// creates the back surface
				int delta = nc - wBasin;
				int elStepBehind = (Integer) (elevationData[lBasin][delta] / (nr
						- 2 - lBasin + 1));
				for (int i = nr - 2; i > lBasin; i--) {
					for (int j = 1; j < nc - 1; j++) {
						elevationData[i][j] = elStepBehind * (nr - 1 - i);
					}
				}
				// adjust the sides
				for (int j = 1; j < delta + 1; j++) {
					for (int i = 1; i < nr - 1 - j; i++) {
						if (elevationData[i][j] > elevationData[nr - 1 - j][j]) {
							elevationData[i][j] = elevationData[nr - 1 - j][j];
							elevationData[i][nc - 1 - j] = elevationData[nr - 1 - j][j];
						}
					}
				}
				// applying the fixed normal distributed noise
				// since the noise can add a maximum of -(elStep/2) to the dem
				// we use that as bound, then we initialize the solution variables
				int minVariation = (Integer) (-(elStep / 2));
				int countVars = 0;
				int lb;
				for (int i = 1; i < nr - 1; i++) {
					for (int j = 1; j < nc - 1; j++) {
						elevationData[i][j] = elevationData[i][j] - minVariation;
						lb = elevationData[i][j] - (-lbound) - 1 < 0 ? - elevationData[i][j] + 1
								: lbound;
						solution.setVariable(countVars,
								new RealVariable(lb, ubound));
						countVars++;
						if (countVars > getNumberOfVariables()) {
							System.out.println("Error while initializing " +
									"the wedge: wrong dimension");
							break;
						}
					}
				}
			} else {
				System.out.println("Bad dem source initialization");
				return null;
			}
		}
		return solution;
	}
}*/