#include <vector>

#include "Reservoir.hpp"

namespace DPSmodel {
class Model {
public:
  Model();
  // set all the external data required: parameters and timeseries
  bool initialize();
  // set the policy parameters
  bool set_policy_parameters();
  bool simulation_step();

private:
  // this is responsible of logging with a certain degree of detail the
  // variables evaluated during the simulation
  Logger variable_logger_;
  // contains pointers to the reservoirs objs
  std::vector<Reservoir*> reservoirs_;
  // contains the exit transformation functions
  std::vector<Function* > stepcosts_;

  // evaluate every u_t with current state + I_t
  void evaluate_policy();
  // evaluate every g_t as a function of u_t, x_t and others
  void evaluate_stepcosts();
  // evaluates x_t+1
  void update_state_variables();
};
} /* namespace DPSmodel */
