//#Termination
/*
 * Date: November 2013
 * Author: heizmann@informatik.uni-freiburg.de
 *
 * 
 */

__VERIFIER_nondet_int() { int val; return val; }

int main() {
  int x,y,z,m,n;
  if (__VERIFIER_nondet_int()) {
    x = 1;
  } else {
    x = -1;
  }
  while (y<100 && z<100) {
    y = y+x;
    z = z-x;
  }
  return 0;
}
