#include <math.h>

#include "RBF.hpp"

namespace DPSmodel {

RBF::RBF() {
  is_initialized_ = false;
}

bool RBF::initialize(unsigned int pn, unsigned int pm, unsigned int pK,
                     std::vector<real>& mins, std::vector<real>& maxs) {
  if (setNumberRBF(pn) && setNumberInput(pm) && setNumberOutput(pK) && 
        mins.size() == number_of_outputs_ + number_of_inputs_ && 
        uniformizer_.set_uniformization_bounds(mins, maxs)) {
    is_initialized_ = true;
  }
  return is_initialized_;
}

bool RBF::set_parameters(const std::vector<real>& theta) {
  if (!is_initialized_) {
    return false;
  }
  // check if centers are < 1 and > -1, and that radiuses
  // and weights are between 0 and 1
  unsigned int idx = 0;
  for (unsigned int i = 0; i < number_of_functions_; i++) {
    for (unsigned int j = 0; j < number_of_inputs_; j++) {
      // center
      if (theta[idx] < -1 || theta[idx] > 1) {
        return false;
      }
      idx++;
      //radius
      if (theta[idx] < 0 || theta[idx] > 1) {
        return false;
      }
      idx++;
    }
    //weights
    for (unsigned int k = 0; k < number_of_outputs_; k++) {
      if (theta[idx] < 0 || theta[idx] > 1) {
        return false;
      }
      idx++;
    }
  }
  std::vector<real> weights (number_of_outputs_, 0.0);
  single_RBF_params c_param;  
  // this collects the sum of the weights to produce each output w_(i,k)
  // it is needed cause the weights are given as a random number in [0, 1]
  // but their sum must also be 1, i.e. sum_{i=1}^#functions w_(i,k) = 1
  idx = 2 * number_of_inputs_;
  // each function (i-th) has its own weights
  for (unsigned int i = 0; i < number_of_functions_; i++) {
    // each output (k-th) has its own weight
    for (unsigned int k = 0; k < number_of_outputs_; k++) {
      // this collects the sum of the weights for each output
      weights[k] = weights[k] + theta[idx];
      // weights of different outputs but same function are next to each other
      idx = idx + 1;
    }
    // here we skip the (center, radius) couples of the next function
    idx = idx + 2 * number_of_inputs_;
  }  
  unsigned int count = 0;
  for (unsigned int i = 0; i < number_of_functions_; i++) {
    // collect the center and radius of the i-th function
    for (unsigned int j = 0; j < number_of_inputs_; j++) {
      c_param.centers.push_back( theta[count] ); // c_(i,j)
      c_param.radiuses.push_back( theta[count+1] ); // b_(i,j)
      count = count + 2;
    }
    // collect the weights associated to the i-th function
    // and uniformize them in [0,1]
    for (unsigned int k = 0; k < number_of_outputs_; k++) {
      if (weights[k] < kEpsilon) {
        c_param.weights.push_back( theta[count] );
      } else {
        c_param.weights.push_back( theta[count] / weights[k] );
      }
      count = count + 1;
    }
    parameters_.push_back(c_param);
    c_param.centers.clear();
    c_param.radiuses.clear();
    c_param.weights.clear();
  }
  return true;
}

std::vector<real> RBF::operator()(std::vector<real>& inputs) const {
  if (!is_initialized_ || inputs.size() < number_of_inputs_) {
    return std::vector<real>();
  }
  // evaluate the inputs
  if (!uniformizer_.uniformize(inputs)) {
    return std::vector<real>();
  }
  real phi[number_of_functions_];
  real base_fun, num, den;
  for (unsigned int j = 0; j < number_of_functions_; j++) {
    base_fun = 0.0;
    for (unsigned int i = 0; i < number_of_inputs_; i++) {
      num = (inputs[i] - parameters_[j].centers[i]) * 
        (inputs[i] - parameters_[j].centers[i]);
      den = (parameters_[j].radiuses[i] * parameters_[j].radiuses[i]);
      if (den < kEpsilon) {
        den = kEpsilon;
      }
      base_fun += num / den;
    }
    phi[j] = exp(-base_fun);
  }
  // compute outputs
  std::vector<real> out(number_of_outputs_, 0.0);
  for (unsigned int k = 0; k < number_of_outputs_; k++) {
    for (unsigned int i = 0; i < number_of_functions_; i++) {
      out[k] = out[k] + parameters_[i].weights[k] * phi[i];
    }
    out[k] = uniformizer_.deuniformize(out[k], inputs.size() + k);
  }
  return out;
}

} /* namespace DPSmodel */