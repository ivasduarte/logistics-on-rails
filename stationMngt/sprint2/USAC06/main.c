// main.c
#include <stdio.h>
#include "dequeue_value.h"

int main(){
	int buffer[100];
	int length = 100, nelem, tail = 0, head, value;

	printf("\n\nInitial state:");
	printf("  nelem: %d, tail: %d, head: %d\n\n", nelem, tail, head);
	
	if(dequeue_value(buffer, length, &nelem, &tail, &head, &value)){
		printf("Removed: %d\n", value);
	}
	
	return 0;
}
