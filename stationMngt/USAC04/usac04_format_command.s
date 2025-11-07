# format_command(char *op, int n, char *cmd)
# a0 : op (input string)
# a1 : n (input int)
# a2: cmd (output string)
# returns a0 = 1 on success, 0 on failure

.section .data

re_str:
	.string "RE"
ye_str:
	.string "YE"
ge_str:
	.string "GE"
rb_str:
	.string "RB"
gth_str:
	.string "GTH"

.section .text
.global format_command

# int format_command(char *op, int n, char *cmd)
format_command:
	addi sp, sp, -16 # allocates 16 bytes on the stack
	sw ra, 12(sp) # saves the return address at 12 offset
	sw s0, 8(sp) # save s0
	sw s1, 4(sp) # save s1
	sw s2, 0(sp) # save s2

	# Save arguments to saved registers
	mv s0, a0 # s0 = op (input string)
	mv s1, a1 # s1 = n (number)
	mv s2, a2 # s2 = cmd (output buffer)

	# Validate n is in range [0, 99]
	blt s1, zero, failure # if n < 0
	li t0, 99
	bgt s1, t0, failure # if n > 99

	# Trim the input string
	mv a0, s0 # put string in a0 for trim function
	jal trim # call trim
	mv s0, a0 # save trimmed string pointer back to s0
	
# ===== TRIM FUNCTION =====
# trim leading and trailing whitespaces from string
# Input: a0 = pointer to string
# Output: a0 = pointer to trimmed string

trim:
	mv t0, a0 # t0 = original pointer
	mv t1, a0 # t1 = scan pointer

skip_leading:
	lb t2, 0(t1) # load string character
	beqz t2, empty_string # if null, string is empty

	li t3, 33 # char <= 32 = whitespace
	bge t2, t3, found_start # if char >= 33, found first non whitespace

	addi t1, t1, 1 # move to next character
	j skip_leading

found_start:
	mv a0, t1 # a0 = start of trimmed string
	mv t4, t1 # t4 = pointer to scan for end

find_end:
	lb t2, 0(t4) # loads byte from memory into t2
	beqz t2, trim_trailing # found null terminator
	addi t4, t4, 1 # move to next character
	j find_end

trim_trailing:
	addi t4, t4, -1 # moves back one character
	blt t4, t1, place_null # if before start, done

	lb t2, 0(t4) # load character (fixed: removed comma)
	li t3, 33
	bge t2, t3, place_null # if non whitespace, done

	j trim_trailing

place_null:
	addi t4, t4, 1 # moves to position after last non whitespace
	sb zero, 0(t4) # place null terminator
	ret            # return from trim

empty_string:
	sb zero, 0(a0) # makes it empty string
	ret

# ===== END OF TRIM FUNCTION =====

success:
	li a0, 1
	lw s2, 0(sp) # restore s2
	lw s1, 4(sp) # restore s1
	lw s0, 8(sp) # restore s0
	lw ra, 12(sp) # restores the return address
	addi sp, sp, 16 # deallocates the stack space
	ret

failure:
	sb zero, 0(s2)
	li a0, 0
	lw s2, 0(sp)
	lw s1, 4(sp)
	lw s0, 8(sp)
	lw ra, 12(sp)
	addi sp, sp, 16
	ret
