#include <stdio.h>
#include <string.h>
#include <ctype.h>

extern int encrypt_data(char* in, int key, char* out);

void test_encrypt(const char *label, const char *input_str, int key,
                  const char *expected_output, int expected_result)
{
    char input[100];
    char output[100];
    int result;

    strncpy(input, input_str, sizeof(input) - 1);
    input[sizeof(input) - 1] = '\0';

    memset(output, 0, sizeof(output));

    result = encrypt_data(input, key, output);

    int passed = (result == expected_result) &&
                 (result == 0 || strcmp(output, expected_output) == 0);

    printf("--- Test: %s (Key: %d) ---\n", label, key);
    printf("Input:      '%s'\n", input_str);
    printf("Expected:   '%s' (Result: %d)\n", expected_output, expected_result);
    printf("Actual:     '%s' (Result: %d)\n", (result == 1 ? output : "(Failure)"), result);
    printf("Status:     %s\n\n", passed ? "**PASS**" : "**FAIL**");
}

void test_None() {
    test_encrypt("test_None (No Op)", "", 0, "", 1);
}

void test_Error() {
    test_encrypt("test_Error (Invalid Input)", "heu", 2, "", 0);
}

void test_S1() {
    test_encrypt("test_S1 (Basic Encrypt)", "ABCDEFGH", 4, "EFGHIJKL", 1);
}

void test_S2() {
    test_encrypt("test_S2 (Wrap Encrypt)", "UVWXYZAAAAA", 8, "CDEFGHIIIII", 1);
}

void test_S3() {
    test_encrypt("test_S3 (Large Shift)", "CDEFGHIIIII", 20, "WXYZABCCCCC", 1);
}

int main() {
    printf("=== Caesar Cipher Encryption Demonstrator ===\n\n");
    test_None();
    test_Error();
    test_S1();
    test_S2();
    test_S3();
    return 0;
}