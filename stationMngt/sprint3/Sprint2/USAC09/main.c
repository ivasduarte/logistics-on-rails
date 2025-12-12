#include <stdio.h>
#include <ctype.h>
#include "median.h"
#include "sort_array.h"

void print_array(int* arr, int length) {
	printf("[");
	for (int i = 0; i < length; i++) {
		printf("%d", arr[i]);
		if (i < length - 1) printf(", ");
	}
	printf("]\n");
}

int main() {
	int median_value;
	int result;

	int arr1[] = {5, 2, 8, 1, 9};
	int len1 = 5;

	printf("Test 1 -> Odd Length\n\n");
	printf("Input: ");
	print_array(arr1, len1);

	result = median(arr1, len1, &median_value);
	printf("\nRESULT\n");
	if (result == 1) {
		if (len1 % 2 == 0) {
			printf("Median: %.1f\n", median_value / 2.0);
		} else {
			printf("Median: %d\n", median_value);
		}
		printf("Success!\n");
	} else {
		printf("Failed!\n");
	}
	
	int arr2[] = {5, 2, 8, 1};
	int len2 = 4;
	
	printf("\nTest 2 -> Even Length\n\n");
	printf("Input: ");
	print_array(arr2, len2);
	
	result = median(arr2, len2, &median_value);
	
	printf("\nRESULT\n");
	if (result == 1) {
		if (len2 % 2 == 0) {
			printf("Median: %.1f\n", median_value / 2.0);
		} else {
			printf("Median: %d\n", median_value);
		}		
		printf("Success!\n");
	} else {
		printf("Failed!\n");
	}
	
	return 0;			
}
