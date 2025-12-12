#include <stdio.h>
#include "usac05_enqueue_value.h"

#define BUFFER_SIZE 5

int main() {
    int buffer[BUFFER_SIZE] = {0};
    int nelem = 0;
    int tail = 0;
    int head = 0;

    printf("Circular Queue Test\n\n");

    // Insert 5 elements
    printf("Inserting 5 elements (10, 20, 30, 40, 50):\n");
    for (int i = 1; i <= 5; i++) {
        int result = enqueue_value(buffer, BUFFER_SIZE, &nelem, &tail, &head, i * 10);
        printf("  Value %d inserted. Buffer full? %s\n", i * 10, result ? "Yes" : "No");
    }

    printf("\nBuffer: ");
    for (int i = 0; i < BUFFER_SIZE; i++) {
        printf("%d ", buffer[i]);
    }
    printf("\nnelem=%d, tail=%d, head=%d\n\n", nelem, tail, head);

    // Insert 2 more (will overwrite)
    printf("Inserting 2 more elements (60, 70):\n");
    enqueue_value(buffer, BUFFER_SIZE, &nelem, &tail, &head, 60);
    printf("  60 inserted\n");
    enqueue_value(buffer, BUFFER_SIZE, &nelem, &tail, &head, 70);
    printf("  70 inserted\n");

    printf("\nFinal buffer: ");
    for (int i = 0; i < BUFFER_SIZE; i++) {
        printf("%d ", buffer[i]);
    }
    printf("\nnelem=%d, tail=%d, head=%d\n", nelem, tail, head);

    return 0;
}