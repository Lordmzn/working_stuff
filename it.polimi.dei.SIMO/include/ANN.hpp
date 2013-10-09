/*
 * ANN.hpp
 *
 * Created on: 05/aug/2013
 *     Author: Emanuele Mason
 */

#ifndef DPSMODEL_ANN_HPP
#define DPSMODEL_ANN_HPP

#include <vector>

#include "DPS_types.h"
#include "Uniformizer.hpp"

namespace DPSmodel {
  /* Generic MISO ANN function with his parameters.
   * a + sum b * tansig(c*input + d);
   * Right now works only with 1 output, should be improved
   */
  class ANN {
  public:
    ANN();
    
    /* order of mins and maxs: [ input [1..#input..optional], output [1..#outputs] ]
     */
    bool initialize(unsigned int n_neuron,
                    unsigned int n_input,
                    unsigned int n_output,
                    std::vector<real>& mins,
                    std::vector<real>& maxs);
    /* order: d[1..#neurons], a, b[1..#neurons], c(1st neuron)[1..#input], 
     *         c(2nd neuron)[1..#input], ... , ] 
     */
    bool set_parameters(const std::vector<real>& parameters);
    /**
     * Evaluates the ANN. Requires a number of arguments = number_of_inputs_.
     **/
    std::vector<real> operator()(std::vector<real>& inputs) const;

    unsigned int get_number_input() const {
        return number_of_inputs_;
    }
    unsigned int get_number_output() const {
        return number_of_outputs_;
    }
    unsigned int get_number_neuron() const {
      return number_of_neurons_;
    }

  private:
    struct ANN_params {
      real a;  // scalar
      std::vector<real> b;  // vector[number_neurons]
      //matrix[number_neurons, number_inputs]
      std::vector< std::vector<real> > c; 
      std::vector<real> d;  // vector[number_neurons]
    };
    ANN_params parameters_;
    unsigned int number_of_neurons_;
    unsigned int number_of_inputs_;
    unsigned int number_of_outputs_;
    bool is_initialized_;
    Uniformizer uniformizer_;
  };

} /* namespace DPSmodel */
#endif /* DPSMODEL_ANN_HPP */
