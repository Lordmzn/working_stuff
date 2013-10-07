package it.polimi.dei.moeaframework.problem.HoaBinh;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.PopulationIO;
import org.moeaframework.core.Problem;
import org.moeaframework.core.spi.ProblemProvider;
import org.moeaframework.util.io.CommentedLineReader;

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
    if (name.equalsIgnoreCase(HoaBinh.NAME)) {
      try {
        String resource = "pf/HoaBinh.pf";
        File file = new File(resource);
        if (file.exists()) {
          return new NondominatedPopulation(PopulationIO.readObjectives(
              file));
        } else {
          InputStream input = getClass().getResourceAsStream("/" + resource);

          if (input == null) {
            throw new FileNotFoundException(resource);
          } else {
            try {
              return new NondominatedPopulation(
                  PopulationIO.readObjectives(new CommentedLineReader(
                      new InputStreamReader(input))));
            } finally {
              input.close();
            }
          }
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

}
