#include <stdio.h>

extern int extract_data(char* str, char* token, char* unit, int* value);

int main(void) {
    char str[] = "TEMP&unit:celsius&value:20#HUM&unit:percentage&value:80";
    
    char token[] = "TEMP";
    char unit[20];
    int value;
    int res = extract_data(str, token, unit, &value);
    printf("%d:%s,%d\n", res, unit, value);
    
    char token2[] = "AAA";
    res = extract_data(str, token2, unit, &value);
    printf("%d:%s,%d\n", res, unit, value);
    
    return 0;
}