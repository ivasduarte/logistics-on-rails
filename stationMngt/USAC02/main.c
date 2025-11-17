#include <stdio.h>
#include <string.h>
#include <ctype.h>

extern int decrypt_data(char* in, int key, char* out);

void test_decrypt(const char *label, const char *input_str, int key,
                  const char *expected_output, int expected_result)
{
    char input[100];
    char output[100];
    int result;

    strncpy(input, input_str, sizeof(input) - 1);
    input[sizeof(input) - 1] = '\0';

    memset(output, 0, sizeof(output));

    result = decrypt_data(input, key, output);

    int passed = (result == expected_result) &&
                 (result == 0 || strcmp(output, expected_output) == 0);

    printf("--- Test: %s (Key: %d) ---\n", label, key);
    printf("Input:      '%s'\n", input_str);
    printf("Expected:   '%s' (Result: %d)\n", expected_output, expected_result);
    printf("Actual:     '%s' (Result: %d)\n", (result == 1 ? output : "(Failure)"), result);
    printf("Status:     %s\n\n", passed ? "**PASS**" : "**FAIL**");
}

int main() {

    printf("=== Caesar Cipher Decryption Demonstrator ===\n\n");

    test_decrypt("test_None (No Op)", "", 0, "", 1);

    test_decrypt("test_Error (Invalid Input)", "heu", 2, "", 0);

    test_decrypt("test_S1 (Basic Decrypt)", "EFGHIJKL", 4, "ABCDEFGH", 1);

    test_decrypt("test_S2 (Wrap Decrypt)", "CDEFGHIIIII", 8, "UVWXYZAAAAA", 1);

    test_decrypt("test_S3 (Large Shift)", "WXYZABCCCCC", 20, "CDEFGHIIIII", 1);

    return 0;
}