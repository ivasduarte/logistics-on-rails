// main.c
#include <stdio.h>
#include "usac06_dequeue_value.h"

int main(){
	int buffer[10] = {100, 200, 300};
	int length = 10, nelem = 3, tail = 0, head = 3, value;

	printf("\n\nInitial state:");
	printf("  nelem: %d, tail: %d, head: %d\n\n", nelem, tail, head);
	
	if(dequeue_value(buffer, length, &nelem, &tail, &head, &value)){
		printf("Removed: %d\n", value); // Prints 100
	}
	
	return 0;
}
