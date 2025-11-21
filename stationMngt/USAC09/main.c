#include <stdio.h>
#include <string.h>

extern int median(int* vec, int length, int* me);

void print_array(int* arr, int length) {
    printf("[");
    for(int i = 0; i < length; i++) {
        printf("%d", arr[i]);
        if (i < length - 1) printf(", ");
    }
    printf("]");
}

void test_median(const char *label, int* input_arr, int length,
                 int expected_result, int expected_median)
{
    int test_arr[100];
    int median_value = -1;
    int result;

    // Copy input array to test array
    memcpy(test_arr, input_arr, length * sizeof(int));

    result = median(test_arr, length, &median_value);

    int passed = (result == expected_result);
    if (result == 1) {
        passed = passed && (median_value == expected_median);
    }

    printf("--- Test: %s ---\n", label);
    printf("Input:      ");
    print_array(input_arr, length);
    printf(" (Length: %d)\n", length);

    if (expected_result == 1) {
        printf("Expected:   Median = ");
        if (length % 2 == 0) {
            printf("%.1f", expected_median / 2.0);
        } else {
            printf("%d", expected_median);
        }
        printf(" (Result: %d)\n", expected_result);
    } else {
        printf("Expected:   (Failure) (Result: %d)\n", expected_result);
    }

    if (result == 1) {
        printf("Actual:     Median = ");
        if (length % 2 == 0) {
            printf("%.1f", median_value / 2.0);
        } else {
            printf("%d", median_value);
        }
        printf(" (Result: %d)\n", result);
    } else {
        printf("Actual:     (Failure) (Result: %d)\n", result);
    }

    printf("Status:     %s\n\n", passed ? "**PASS**" : "**FAIL**");
}

void test_NullVector() {
    int input[] = {0};
    test_median("test_NullVector (Empty Array)", input, 0, 0, 0);
}

void test_One() {
    int input[] = {1000};
    test_median("test_One (Single Element)", input, 1, 1, 1000);
}

void test_Zero() {
    int input[] = {10, 0, 1};
    test_median("test_Zero (With Zero)", input, 3, 1, 1);
}

void test_Three() {
    int input[] = {-1, -3, -2};
    test_median("test_Three (Negative Numbers - Odd)", input, 3, 1, -2);
}

void test_Four() {
    int input[] = {-1, -3, -4, -2};
    test_median("test_Four (Negative Numbers - Even)", input, 4, 1, -5);
}

void test_Five() {
    int input[] = {1, 1, 1, 1, 2};
    test_median("test_Five (Duplicates)", input, 5, 1, 1);
}

void test_Six() {
    int input[] = {0, 3000, 10, 20, 0, 300};
    test_median("test_Six (Mixed Values - Even)", input, 6, 1, 30);
}

int main() {
    printf("=== Median Function Demonstrator ===\n\n");
    test_NullVector();
    test_One();
    test_Zero();
    test_Three();
    test_Four();
    test_Five();
    test_Six();
    return 0;
}