#include <gtest/gtest.h>
#include <vector>

#include "../ANN.hpp"

namespace DPSmodelTest {

class ANNTest : public ::testing::Test {
public:
  ANNTest();
  
  ANN ann;
};

TEST_F(ANNTest, InitTest) {
  std::vector<real> mins(3);
  mins[0] = 0.0; mins[1] = 100.0; mins[2] = -0.001;
  std::vector<real> maxs(3);
  maxs[0] = 1.0; maxs[1] = 200.0; maxs[2] = -0.0001;
	ASSERT_TRUE(ann.initialize(3, 2, 1, mins, maxs));
	ASSERT_EQ(3, get_number_neuron());
 	ASSERT_EQ(2, get_number_input());
	ASSERT_EQ(1, get_number_output());
  ASSERT_FALSE(ann.initialize(3, 2, 2, mins, maxs));
  maxs[2] = -0.01;
  ASSERT_FALSE(ann.initialize(3, 2, 1, mins, maxs));
  maxs[2] = -0.0001; maxs.push_back(12); mins.push_back(11);
  ASSERT_TRUE(ann.initialize(4, 3, 1, mins, maxs));
  ASSERT_EQ(3, ann.get_number_input());
}

TEST_F(ANNTest, EvaluateTest) {
  set_parameters
    ()
}
} /* namespace DPSmodelTest */