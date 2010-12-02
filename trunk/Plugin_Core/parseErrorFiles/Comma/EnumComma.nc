::: no comma at the end of enum construct
module TestTinyVizM {
  provides {
    interface StdControl;
  }
  uses {
    interface Timer;

  }
} implementation {

  enum {
    MAX_NEIGHBORS = 8,
  };

  command int StdControl.init() {
   
    return 5;
  }
}