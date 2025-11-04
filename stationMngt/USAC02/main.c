#define KEY 5
#include <stdio.h>
#include <ctype.h>
#include "decrypt_data.h"

int main() {
    char input[256] = "MJQQT";
    char output[256];
    int result;

    for (int i = 0; input[i]; i++) {
        input[i] = toupper((unsigned char)input[i]);
    }

    printf("=== Caesar Cipher Decryption ===\n\n");
    printf("Input:     '%s'\n", input);
    printf("Key:       %d\n", KEY);

    result = decrypt_data(input, KEY, output);

    printf("\n--- Result ---\n");
    if (result == 1) {
        printf("Output:   '%s'\n", output);
    } else {
        printf("Failed (Invalid input or key)\n");
    }

    return 0;
}
