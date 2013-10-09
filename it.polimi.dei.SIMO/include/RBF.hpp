/**
 * rbf.h
 * Create a radial basis function.
 * 
 * 1st version: Matteo Giuliani
 * 2nd version: Emanuele Mason
 *   - removed namespace std:: (to not pollute the std namespace)
 *   - added const specifier when possible
 *   - divided the dimension specification from the instantation
 *     with the values of the parameters
 * 
 * Polimi, 23 sep 2013
 **/ 
#ifndef RBF_H
#define RBF_H

#include <vector>
#include <string>
#include <iostream>

#include "DPS_types.h"
#include "Uniformizer.hpp"

namespace DPSmodel {
  
class RBF {
public:  
  RBF();
  /**
   * initializer with parameters:
   *     pn = number of RBF
   *     pm = number of input
   *     pK = number of output
   *     mins = lower bound of vars' set of definition
   *     maxs = upper bound of vars' set of definition
   **/
  bool initialize(unsigned int pn, unsigned int pm, unsigned int pK,
                  std::vector<real>& mins, std::vector<real>& maxs);
  /**
   * constructor with parameters:
   * theta = parameters (c, b, w)
   **/
  bool set_parameters(const std::vector<real>& theta);
  /**
   * RBF control law. Requires a number of arguments = number_of_inputs_.
   **/
  std::vector<real> operator()(std::vector<real>& inputs) const;
  // more evaluations at once
  //std::vector<std::vector<real>> operator()(const std::vector<std::vector<real>> vectorial_inputs) const;  
  /**
   * getters for the dimensions
   **/
  unsigned int get_number_RBF() const {
      return number_of_functions_;
  }
  unsigned int get_number_input() const {
      return number_of_inputs_;
  }
  unsigned int get_number_output() const {
      return number_of_outputs_;
  }

private:
  bool is_initialized_;
  struct single_RBF_params {
      std::vector<real> centers; // center (M-state variables)
      std::vector<real> radiuses; // radius (M-state variables)
      std::vector<real> weights; // weights (K-output)
  };
  std::vector<single_RBF_params> parameters_;
  
  unsigned int number_of_functions_; // number of RBF
  unsigned int number_of_inputs_; // number of input
  unsigned int number_of_outputs_; // number of output
  
  Uniformizer uniformizer_;
  
  static const real kEpsilon = 0.000000001; // 10^-9
  
  bool setNumberRBF(unsigned int x) {
    number_of_functions_ = x;
    return true;
  }
  bool setNumberInput(unsigned int x) {
    number_of_inputs_ = x;
    return true;
  }
  bool setNumberOutput(unsigned int x) {
    number_of_outputs_ = x;
    return true;
  }
  
};

} /* namespace DPSmodel */
#endif // RBF_H
