#define ASC 1
#define DSC 0

#include <stdio.h>
#include <ctype.h>
#include "sort_array.h"

void print_array(int* arr, int length) {
	printf("[");
	for(int i = 0; i < length; i++) {
		printf("%d", arr[i]);
		if (i < length - 1) printf(", ");
	}
	printf("]\n");
}

int main() {
	int array[] = {5, 2, 8, 1, 9, 3};
	int length = 6;
	int result;

	printf("Initial Array: \n");
	print_array(array, length);
	
	printf("\nAscending Order Sorting test\n");
	result = sort_array(array, length, ASC);

	if (result == 1) {
		print_array(array, length);
		printf("\nSuccess!\n");
	} else {
		printf("Failed\n");
	}
	
	printf("\n");
	int array2[] = {5, 2, 8, 1, 9, 3};
	
	printf("Descending Order Sorting test\n");
	result = sort_array(array2, length, DSC);
	
	if (result == 1) {
		print_array(array2, length);
		printf("\nSuccess!\n");
	} else {
		printf("Failed!\n");
	}
	
	return 0;			
}
