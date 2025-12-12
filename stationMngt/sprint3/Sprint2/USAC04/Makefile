CC = riscv32-linux-gcc
CFLAGS = -Wall -Wextra -fanalyzer -g
ARCHFLAGS = -march=rv32imd -mabi=ilp32d
LIBRV32 = ${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

.PHONY: clean run debug

usac04.elf: usac04_main.o usac04_format_command.o
	$(CC) $(CFLAGS) $(ARCHFLAGS) $^ -o $@

usac04_main.o: usac04_main.c usac04_format_command.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $<

usac04_format_command.o: usac04_format_command.s usac04_format_command.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $<

clean:
	rm -f *.o *.elf

run: usac04.elf
	qemu-riscv32 -L $(LIBRV32) ./usac04.elf

debug: usac04.elf
	@echo "On other terminal window do:"
	@echo "riscv32-linux-gdb -tui ./usac04.elf"
	qemu-riscv32 -L $(LIBRV32) -g 1234 usac04.elf
