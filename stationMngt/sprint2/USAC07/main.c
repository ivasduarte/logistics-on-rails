#include <stdio.h>
#include "move_n_to_array.h"

int main(void) {
    int buffer[5] = {10, 20, 30, 40, 50};
    int length = 5;
    int nelem = 3; 
    int tail = 1;  
    int head = 4;
    int array[3];
    
    int ret = move_n_to_array(buffer, length, &nelem, &tail, &head, 2, array);
    
    printf("Resultado: %d\n", ret);
    printf("Array: [%d, %d]\n", array[0], array[1]);
    printf("nelem=%d, tail=%d\n", nelem, tail);
    
    return 0;
}