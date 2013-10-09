/*
 * DPS_types.h
 *
 * Created on: 01/aug/2013
 *     Author: lordmzn
 */

#ifndef DPSMODEL_TYPES_H
#define DPSMODEL_TYPES_H

#include <string>

namespace DPSmodel {
  typedef int                 integer;
  // typedef unsigned long int   time_t;
  typedef double              real;  
  /*
   * This struct contains the datas to identify the HoaBinh model.
   */
  struct ModelInstance {
    real* xreal;
    real* objectives_values;
  };
  // ---- enumerations
  
  namespace PolicyType {
    enum vals {ANN=0, RBF};
    static const std::string vals_string[] = {"ann", "rbf"};
    static const unsigned int n_vals = 2;
  }
  inline const std::string to_string(PolicyType::vals enumobj) {
    return PolicyType::vals_string[enumobj];
  };
}
#endif /* DPSMODEL_TYPES_H */
