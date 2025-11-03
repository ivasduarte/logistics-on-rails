CC = riscv32-linux-gcc
CFLAGS = -Wall -Wextra -fanalyzer -g
ARCHFLAGS = -march=rv32imd -mabi=ilp32d
LIBRV32 = ${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

program.elf: main.o decrypt_string.o
	$(CC) $(CFLAGS) $(ARCHFLAGS) main.o decrypt_string.o -o program.elf

main.o: main.c
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c main.c

decrypt_string.o: decrypt_string.s decrypt_string.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c decrypt_string.s

clean:
	rm -f *.o *.elf

run : program.elf
	qemu-riscv32 -L $(LIBRV32) ./program.elf
