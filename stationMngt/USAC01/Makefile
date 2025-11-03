CC = riscv32-linux-gcc
CFLAGS = -Wall -Wextra -fanalyzer -g
ARCHFLAGS = -march=rv32imd -mabi=ilp32d
LIBRV32 = ${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

program.elf: main.o encrypt_string.o
	$(CC) $(CFLAGS) $(ARCHFLAGS) main.o encrypt_string.o -o program.elf

main.o: main.c
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c main.c

encrypt_string.o: encrypt_string.s encrypt_string.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c encrypt_string.s

clean:
	rm -f *.o *.elf

run : program.elf
	qemu-riscv32 -L $(LIBRV32) ./program.elf
