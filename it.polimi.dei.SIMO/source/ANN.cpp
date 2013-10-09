/*
 * ANN.cpp
 *
 * Created on: 09/aug/2013
 *     Author: Emanuele Mason
 */
#include <cmath> // exp

#include "ANN.hpp"

namespace DPSmodel {
  
ANN::ANN() :
  number_of_neurons_(0), number_of_inputs_(0), is_initialized_(false) {

}
  
bool ANN::initialize(unsigned int n_neuron,
                     unsigned int n_input,
                     unsigned int n_output,
                     std::vector<real>& mins, 
                     std::vector<real>& maxs) {
  if (n_neuron == 0 || n_input == 0 || n_output != 1) {
    return false;
  }
  if (mins.size() < n_input + n_output) {
    return false;
  }
  if (!uniformizer_.set_uniformization_bounds(mins, maxs)) {
    return false;
  }
  number_of_inputs_ = n_input;
  number_of_neurons_ = n_neuron;
  number_of_outputs_ = n_output;
  parameters_.b.resize(number_of_neurons_, 0.0);
  parameters_.c.resize(number_of_neurons_);
  for (std::vector< std::vector<real> >::iterator it = parameters_.c.begin();
      it != parameters_.c.end(); ++it) {
    it->resize(number_of_inputs_, 0.0);
  }
  parameters_.d.resize(number_of_neurons_, 0.0);
  is_initialized_ = true;
  return true;
}

bool ANN::set_parameters(const std::vector<real>& params) {
  if (!is_initialized_) {
    return false;
  }
  if (params.size() != (1 + number_of_neurons_ * (2 + number_of_inputs_))) {
    return false;
  }
  std::vector<real>::const_iterator it_new_param = params.begin();
  std::vector<real>::iterator it; 
  for (it = parameters_.d.begin(); it != parameters_.d.end(); ++it) {
    *it = *it_new_param;
    ++it_new_param;
  }
  parameters_.a = *it_new_param;
  ++it_new_param;
  for (it = parameters_.b.begin(); it != parameters_.b.end(); ++it) {
    *it = *it_new_param;
    ++it_new_param;
  }
  for (std::vector< std::vector<real> >::iterator it_out = parameters_.c.begin(); 
      it_out != parameters_.c.end(); ++it_out) {
    for (it = it_out->begin(); it != it_out->end(); ++it) {
      *it = *it_new_param;
      ++it_new_param;
    }
  }
  return true;
}

std::vector<real> ANN::operator()(std::vector<real>& inputs) const {
  if (!is_initialized_ || inputs.size() < number_of_inputs_) {
    return std::vector<real>();
  }
  // evaluate the inputs
  if (!uniformizer_.uniformize(inputs)) {
    return std::vector<real>();
  }
  // Compute value = a + sum b * sig(c*input + d);
  // (c*input + d)
  real* neurons_value = new real[number_of_neurons_];
  unsigned int n;
  for (n = 0; n < number_of_neurons_; n++) {
    neurons_value[n] = parameters_.d[n];
    for (unsigned int m = 0; m < get_number_input(); m++) {
      neurons_value[n] += inputs[m] * parameters_.c[n][m];
    }
    // sig(c*input + d)
    neurons_value[n] = ( 2 / (1 + exp(-2 * neurons_value[n])) ) - 1;
    // overflow
    if (neurons_value[n] == HUGE_VAL ||
        neurons_value[n] == -HUGE_VAL) {
      return std::vector<real>();
    }
    // invalid values: dont happen (exp(-2*x) is never = -1)
    //if (isnan(neurons_value[n])) {
    //  return 0.0;
    //}
  }
  real result = parameters_.a;
  // a + sum b * sig(c*input + d)
  for (n = 0; n < number_of_neurons_; n++){
      result += neurons_value[n] * parameters_.b[n];
  }
  delete[] neurons_value;
  // number_input = idx 1st output
  return std::vector<real>(1, uniformizer_.deuniformize(result, inputs.size()) ); 
}

} /* namespace DPSmodel */
