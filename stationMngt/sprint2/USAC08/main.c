#define ASC 1
#define DSC 0

#include <stdio.h>
#include <string.h>

extern int sort_array(int* ptr, int num, char order);

void print_array(int* arr, int length) {
    printf("[");
    for(int i = 0; i < length; i++) {
        printf("%d", arr[i]);
        if (i < length - 1) printf(", ");
    }
    printf("]");
}

int arrays_equal(int* arr1, int* arr2, int length) {
    for(int i = 0; i < length; i++) {
        if (arr1[i] != arr2[i]) return 0;
    }
    return 1;
}

void test_sort(const char *label, int* input_arr, int length, char order,
               int* expected_arr, int expected_result)
{
    int test_arr[100];
    int result;

    // Copy input array to test array
    memcpy(test_arr, input_arr, length * sizeof(int));

    result = sort_array(test_arr, length, order);

    int passed = (result == expected_result) && (result == 0 || arrays_equal(test_arr, expected_arr, length));

    printf("--- Test: %s (Order: %s) ---\n", label, order == ASC ? "Ascending" : "Descending");
    printf("Input:      ");
    print_array(input_arr, length);
    printf("\n");
    printf("Expected:   ");
    print_array(expected_arr, length);
    printf(" (Result: %d)\n", expected_result);
    printf("Actual:     ");
    if (result == 1) {
        print_array(test_arr, length);
    } else {
        printf("(Failure)");
    }
    printf(" (Result: %d)\n", result);
    printf("Status:     %s\n\n", passed ? "**PASS**" : "**FAIL**");
}

void test_NullVector() {
    int input[] = {0};
    int expected[] = {0};
    test_sort("test_NullVector (Empty Array)", input, 0, ASC, expected, 0);
}

void test_One() {
    int input[] = {1000};
    int expected[] = {1000};
    test_sort("test_One (Single Element)", input, 1, ASC, expected, 1);
}

void test_Zero() {
    int input[] = {10, 0, 1};
    int expected[] = {0, 1, 10};
    test_sort("test_Zero (With Zero)", input, 3, ASC, expected, 1);
}

void test_Three() {
    int input[] = {-1, -3, -2};
    int expected[] = {-3, -2, -1};
    test_sort("test_Three (Negative Numbers)", input, 3, ASC, expected, 1);
}

void test_Five() {
    int input[] = {2, 1, 1, 1, 1};
    int expected[] = {1, 1, 1, 1, 2};
    test_sort("test_Five (Duplicates)", input, 5, ASC, expected, 1);
}

void test_NullVectorD() {
    int input[] = {0};
    int expected[] = {0};
    test_sort("test_NullVectorD (Empty Array)", input, 0, DSC, expected, 0);
}

void test_OneD() {
    int input[] = {1000};
    int expected[] = {1000};
    test_sort("test_OneD (Single Element)", input, 1, DSC, expected, 1);
}

void test_ZeroD() {
    int input[] = {10, 0, 1};
    int expected[] = {10, 1, 0};
    test_sort("test_ZeroD (With Zero)", input, 3, DSC, expected, 1);
}

void test_ThreeD() {
    int input[] = {-1, -3, -2};
    int expected[] = {-1, -2, -3};
    test_sort("test_ThreeD (Negative Numbers)", input, 3, DSC, expected, 1);
}

void test_FiveD() {
    int input[] = {1, 1, 1, 1, 2};
    int expected[] = {2, 1, 1, 1, 1};
    test_sort("test_FiveD (Duplicates)", input, 5, DSC, expected, 1);
}

int main() {
    printf("=== Sort Array Function Demonstrator ===\n\n");
    test_NullVector();
    test_One();
    test_Zero();
    test_Three();
    test_Five();
    test_NullVectorD();
    test_OneD();
    test_ZeroD();
    test_ThreeD();
    test_FiveD();
    return 0;
}