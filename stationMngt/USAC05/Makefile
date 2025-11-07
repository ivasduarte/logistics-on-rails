CC=riscv32-linux-gcc
CFLAGS=-Wall -Wextra -fanalyzer -g
ARCHFLAGS=-march=rv32imd -mabi=ilp32d
LIBRV32=${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

usac05.elf: usac05_main.o usac05_enqueue_value.o
	$(CC) $(CFLAGS) $(ARCHFLAGS) usac05_main.o usac05_enqueue_value.o -o usac05.elf

usac05_main.o: usac05_main.c
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c usac05_main.c

usac05_enqueue_value.o: usac05_enqueue_value.s usac05_enqueue_value.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c usac05_enqueue_value.s

clean:
	rm -f *.o *.elf

run: usac05.elf
	qemu-riscv32 -L $(LIBRV32) ./usac05.elf

debug: usac05.elf
	@echo "On other terminal window do:"
	@echo "riscv32-linux-gdb -tui ./usac05.elf"
	qemu-riscv32 -L $(LIBRV32) -g 1234 usac05.elf -S
