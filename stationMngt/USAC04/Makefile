CC=riscv32-linux-gcc
CFLAGS=-Wall -Wextra -fanalyzer -g
ARCHFLAGS=-march=rv32imd -mabi=ilp32d
LIBRV32=${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

usac04.elf: main.o format_command.o
	$(CC) $(CFLAGS) $(ARCHFLAGS) main.o format_command.o -o usac04.elf

main.o: main.c
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c main.c

format_command.o: format_command.s format_command.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c format_command.s

clean:
	rm -f *.o *.elf

run: usac04.elf
	qemu-riscv32 -L $(LIBRV32) ./usac04.elf

debug: usac04.elf
	@echo "On other terminal window do:"
	@echo "riscv32-linux-gdb -tui ./usac04.elf"
	qemu-riscv32 -L $(LIBRV32) -g 1234 usac04.elf -S
