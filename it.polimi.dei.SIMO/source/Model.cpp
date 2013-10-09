#include "Model.hpp"

namespace DPSmodel {
  
Model::Model() {
}
  
bool Model::initialize() {
  return true;
}
  
bool Model::set_policy_parameters() {
  return true;
}

bool Model::simulation_step() {
  return true;
}
  
} /* namespace DPSmodel */
