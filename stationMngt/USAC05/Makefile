CC=riscv32-linux-gcc
CFLAGS=-Wall -Wextra -fanalyzer -g
ARCHFLAGS=-march=rv32imd -mabi=ilp32d
LIBRV32=${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

usac05.elf: main.o enqueue_value.o
	$(CC) $(CFLAGS) $(ARCHFLAGS) main.o enqueue_value.o -o usac05.elf

main.o: main.c
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c main.c

enqueue_value.o: enqueue_value.s enqueue_value.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c enqueue_value.s

clean:
	rm -f *.o *.elf

run: usac05.elf
	qemu-riscv32 -L $(LIBRV32) ./usac05.elf

debug: usac05.elf
	@echo "On other terminal window do:"
	@echo "riscv32-linux-gdb -tui ./usac05.elf"
	qemu-riscv32 -L $(LIBRV32) -g 1234 usac05.elf -S
