//#Unsafe
/*
 * Date: 2016-02-22
 * Author: heizmann@informtik.uni-freiburg.de
 * 
 */

#include <stdlib.h>
#include <string.h>
#include <stdio.h>

int main() {
	char *src = malloc(3*sizeof(char));
	char *dst = malloc(3*sizeof(char));
	void *res = memcpy(dst+2,src,2);
	free(src);
	free(dst);
	return 0;
}
