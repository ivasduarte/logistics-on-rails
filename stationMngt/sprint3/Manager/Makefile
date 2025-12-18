# Makefile for Station Manager (RISC-V 32-bit)

# Compiler and Flags
CC = riscv32-linux-gcc
CFLAGS = -Wall -Wextra -fanalyzer -g -I. -I../Sprint2/USAC01
ARCHFLAGS = -march=rv32imd -mabi=ilp32d
LIBRV32 = $(HOME)/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

GDB = riscv32-linux-gdb

# Directories
SPRINT2_DIR = ../Sprint2
SPRINT3_DIR = ../Sprint3

# Source Files
SRC = manager.c ui.c
ASM_SRC_LOCAL =
ASM_SRC_SPRINT2 = \
	$(SPRINT2_DIR)/USAC01/encrypt_data.s \
	$(SPRINT2_DIR)/USAC02/decrypt_data.s \
	$(SPRINT2_DIR)/USAC03/extract_data.s \
	$(SPRINT2_DIR)/USAC04/usac04_format_command.s \
	$(SPRINT2_DIR)/USAC05/usac05_enqueue_value.s \
	$(SPRINT2_DIR)/USAC06/usac06_dequeue_value.s \
	$(SPRINT2_DIR)/USAC07/move_n_to_array.s \
	$(SPRINT2_DIR)/USAC08/sort_array.s \
	$(SPRINT2_DIR)/USAC09/median.s

ASM_SRC_SPRINT3 = \
	$(SPRINT3_DIR)/USAC11/asm_fill_user_struct.s

# Object Files
OBJ = $(SRC:.c=.o) $(ASM_SRC_LOCAL:.s=.o) $(ASM_SRC_SPRINT2:.s=.o) $(ASM_SRC_SPRINT3:.s=.o)

# Output
EXEC = station_manager

# Rules
all: $(EXEC)

$(EXEC): $(OBJ)
	$(CC) $(CFLAGS) $(ARCHFLAGS) $(OBJ) -o $@

%.o: %.c
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $< -o $@

%.o: %.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $< -o $@

clean:
	rm -f $(OBJ) $(EXEC)

run: $(EXEC)
	qemu-riscv32 -L $(LIBRV32) ./$(EXEC)

debug: $(EXEC)
	@echo "A iniciar o QEMU em modo de espera (porta 1234)..."
	@echo "Num outro terminal, correr: gdb-connect"
	@echo "riscv-linux-gdb -tui ./$(EXEC)"
	qemu-riscv32 -L $(LIBRV32) -g 1234 ./$(EXEC) -S

gdb-connect:
	$(GDB) -tui -ex "set sysroot $(LIBRV32)"
			-ex "target remote :1234"
			./$(EXEC)
