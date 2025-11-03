#define KEY 5
#include <stdio.h>
#include <ctype.h>
#include "encrypt_string.h"

int main() {
    char input[256] = "HELLO";
    char output[256];
    int result;

    for (int i = 0; input[i]; i++) {
        input[i] = toupper((unsigned char)input[i]);
    }

    printf("=== Caesar Cipher Encryption ===\n\n");
    printf("Input:     '%s'\n", input);
    printf("Key:       %d\n", KEY);

    result = encrypt_string(input, KEY, output);

    printf("\n--- Result ---\n");
    if (result == 1) {
        printf("Output:   '%s'\n", output);
    } else {
        printf("Failed (Invalid input or key)\n");
    }

    return 0;
}
