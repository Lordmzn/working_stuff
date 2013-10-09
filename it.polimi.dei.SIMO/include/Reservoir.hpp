/*
 * Reservoir.hpp
 *
 *  Created on: 09/ott/2013
 *      Author: emanuele
 */

#ifndef RESERVOIR_HPP_
#define RESERVOIR_HPP_

namespace DPSmodel {

class Reservoir {
public:
  Reservoir();

  bool set_instantaneous_releases(Function& min, Function& max);

  bool update_storage(real inflow, real decision);
  real get_current_level();
  real get_current_surface();
  real get_current_storage();

private:
  real storage_;
  real release_;

  Function min_instantaneous_release_;
  Function max_instantaneous_release_;
  Function storage_surface_;
  Function storage_level_;

  void integrate_min_max_release();
  void evaluate_current_release(real decision);
};

} /* namespace DPSmodel */

#endif /* RESERVOIR_HPP_ */
