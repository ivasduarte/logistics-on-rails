#include <stdio.h>
#include <string.h>
#include "usac04_format_command.h"

int main() {
	int value = 5;
	char str[] = "  rB  ";
	char cmd[20];
	cmd[0] = '\0';
	int res = format_command(str, value, cmd);
	printf("%d: %s\n", res, cmd); //1: RB,05

	strcpy(str, "  Ye  ");
	value = 25;
	res = format_command(str, value, cmd);
	printf("%d: %s\n", res, cmd); //1: YE,25

	strcpy(str, "  Ye  ");
	value = 125;
	res = format_command(str, value, cmd);
	printf("%d: %s\n", res, cmd); //0:

	strcpy(str, " aaa ");
	value = 25;
	res = format_command(str, value, cmd);
	printf("%d: %s\n", res, cmd); //0:

	strcpy(str, " gTh  ");
	res = format_command(str, value, cmd);
	printf("%d: %s\n", res, cmd); //1: GTH
}
