#ifndef DPSMODEL_UNIFORMIZER_HPP
#define DPSMODEL_UNIFORMIZER_HPP

#include <vector>

#include "DPS_types.h"

namespace DPSmodel {

class Uniformizer {
public:
  Uniformizer() : is_initialized_(false) {}
  
  bool set_uniformization_bounds(const std::vector<real>& mins,
                                 const std::vector<real>& maxs) {
    if (mins.size() != maxs.size()) {
      return false;
    }
    // check if any of the mins is greater that the corresponding max
    std::vector<real>::const_iterator it_maxs = maxs.begin();
    for (std::vector<real>::const_iterator it_mins = mins.begin();
          it_mins != mins.end(); ++it_mins) {
      if (*it_mins > *it_maxs) {
        return false;
      }
      ++it_maxs;
    }
    vars_min_max_.min_value = mins;
    vars_min_max_.max_value = maxs;
    is_initialized_ = true;
    return true;
  }

  real uniformize(const real& x, const unsigned int& idxIn) const {
    if (!is_initialized_ || idxIn >= vars_min_max_.min_value.size()) {
      return x;
    }
    return (x - vars_min_max_.min_value[idxIn]) / 
      (vars_min_max_.max_value[idxIn] - vars_min_max_.min_value[idxIn]);
  }
  bool uniformize(std::vector<real>& vars) const {
    if (vars.size() > vars_min_max_.min_value.size() || 
        !is_initialized_) {
      return false;
    }
    for (unsigned int i = 0; i < vars.size(); i++) {
      vars[i] = uniformize(vars[i], i);
    }
    return true;
  }
  
  real deuniformize(const real& x, const unsigned int idxIn) const {
    if (!is_initialized_ || idxIn >= vars_min_max_.min_value.size()) {
      return x;
    }
    return x * (vars_min_max_.max_value[idxIn] - vars_min_max_.min_value[idxIn]) + 
      vars_min_max_.min_value[idxIn];
  }
  bool deuniformize(std::vector<real>& vars) const {
    if (vars.size() > vars_min_max_.min_value.size() || 
        !is_initialized_) {
      return false;
    }
    for (unsigned int i = 0; i < vars.size(); i++) {
      vars[i] = deuniformize(vars[i], i);
    }
    return true;
  }
  
private:
  struct Bounds {
    // for uniformization: [inputs_min output_min]
    std::vector<real> min_value;
    std::vector<real> max_value;
  };
  Bounds vars_min_max_;
  bool is_initialized_;
};

} /* namespace DPSmodel */ 
#endif //DPSMODEL_UNIFORMIZER_HPP