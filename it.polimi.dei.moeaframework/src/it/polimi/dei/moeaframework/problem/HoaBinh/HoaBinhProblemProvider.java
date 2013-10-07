package it.polimi.dei.moeaframework.problem.HoaBinh;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.NoSuchElementException;
import org.moeaframework.analysis.sensitivity.ResultEntry;
import org.moeaframework.analysis.sensitivity.ResultFileReader;
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
    if (name.equalsIgnoreCase(HoaBinh.NAME)) {
      try {
        File file = new File("pf/HoaBinh.pf");
        if (!file.exists()) {
          return null;
        }
        ResultFileReader reader = new ResultFileReader(new HoaBinh(), file);
        ResultEntry entry = reader.next();
        reader.close();
        return entry.getPopulation();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (NoSuchElementException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

}
