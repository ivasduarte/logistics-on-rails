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
 	addi sp, sp, -80    # Allocate: 16 (saved regs) + 64 (local buffer)
 	sw ra, 12(sp)
 	sw s0, 8(sp)
 	sw s1, 4(sp)
 	sw s2, 0(sp)

 	# Save arguments
 	mv s0, a0           # s0 = original op pointer
 	mv s1, a1           # s1 = n
 	mv s2, a2           # s2 = cmd output

 	# Validate n
 	blt s1, zero, failure
 	li t0, 99
 	bgt s1, t0, failure

 	# Create local buffer for mutable string (64 bytes)
 	addi t5, sp, 16     # t5 = local buffer start
    
 	# Copy input string to local buffer
 	mv a0, s0           # source = original op
 	mv a1, t5           # dest = local buffer
 	jal my_strcpy_local
    
 	mv s0, t5           # s0 now points to local mutable copy

 	# Trim the local copy
 	mv a0, s0
	jal trim
	mv s0, a0

	# Convert to uppercase
	mv a0, s0
	jal to_uppercase
	
	# Compare it to "RE", "YE", "GE", "RB", "GTH"
	# Check if it's "RE"
    	mv a0, s0 # a0 = trimmed/uppercase string
    	la a1, re_str # a1 = "RE"
    	jal strcmp # Compare them
    	beqz a0, handle_re # If a0 == 0, strings match!
    
    	# Check if it's "YE"
    	mv a0, s0
    	la a1, ye_str
    	jal strcmp
    	beqz a0, handle_ye
    
    	# Check if it's "GE"
    	mv a0, s0
    	la a1, ge_str
    	jal strcmp
    	beqz a0, handle_ge
    
    	# Check if it's "RB"
    	mv a0, s0
    	la a1, rb_str
    	jal strcmp
    	beqz a0, handle_rb
    
    	# Check if it's "GTH"
    	mv a0, s0
    	la a1, gth_str
    	jal strcmp
    	beqz a0, handle_gth
    
    	# If none matched, it's an unknown command
    	j failure
	
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
	ret

# ===== END OF TRIM FUNCTION =====

# ==== TO UPPERCASE FUNCTION =====
# converts string to uppercase
# Input: a0 = pointer to string
# Output: string is modified in place

to_uppercase:
	mv t0, a0 # t0 = current position in the string

upper_loop:
	lb t1, 0(t0) # load current character
	beqz t1, upper_done # if null terminator, done

	li t2, 'a'
	blt t1, t2, upper_next # if char < 'a', not lowercase

	li t2, 'z'
	bgt t1, t2, upper_next # if char > 'z', not lowercase

	addi t1, t1, -32 # converts lowercase character to uppercase
	sb t1, 0(t0)

upper_next:
	addi t0, t0, 1 # move to next character
	j upper_loop

upper_done:
	ret

# ==== END OF TO UPPERCASE FUNCTION ====

# ==== COMPARE STRINGS FUNCTION ====
# compares two strings
# Input: a0 = string1, a1 = string2
# Returns a0 = 0 if equal, non zero if different

strcmp:
	mv t0, a0 # t0 = pointer to string1
	mv t1, a1 # t1 = pointer to string2

strcmp_loop:
	lb t2, 0(t0) # load char from string1
	lb t3, 0(t1) # load char from string2

	bne t2, t3, strcmp_not_equal # if chars are different, not equal
	beqz t2, strcmp_equal # if null terminator, equal

	addi t0, t0, 1 # move to next char in string1
	addi t1, t1, 1 # move to next char in string2
	j strcmp_loop

strcmp_equal:
	li a0, 0
	ret

strcmp_not_equal:
	sub a0, t2, t3
	ret

# ==== END OF COMPARE STRINGS FUNCTION ====

# ==== FORMAT STRINGS WITH TWO DIGIT NUMBER FUNCTIONS ====

handle_re:
	la a0, re_str
	mv a1, s2
	jal my_strcpy_local # Copy "RE" to output
	li t0, ','
	sb t0, 2(s2) # writes ',' in position 2
	mv a0, s1 # Get the number
	addi a1, s2, 3 # Point to position after "RE,"
	jal append_two_digits # Append number
	j success

handle_ye:
	la a0, ye_str
	mv a1, s2
	jal my_strcpy_local
	li t0, ','
	sb t0, 2(s2) # writes ',' in position 2
	mv a0, s1
	addi a1, s2, 3
	jal append_two_digits
	j success

handle_ge:
	la a0, ge_str
	mv a1, s2
	jal my_strcpy_local
	li t0, ','
	sb t0, 2(s2) # writes ',' in position 2
	mv a0, s1
	addi a1, s2, 3
	jal append_two_digits
	j success

handle_rb:
	la a0, rb_str
	mv a1, s2
	jal my_strcpy_local
	li t0, ','
	sb t0, 2(s2) # writes ',' in position 2
	mv a0, s1
	addi a1, s2, 3
	jal append_two_digits
	j success

handle_gth:
	la a0, gth_str
	mv a1, s2
	jal my_strcpy_local # Just copy "GTH", no number
	j success

# ==== END OF FORMAT STRINGS WITH TWO DIGIT NUMBER FUNCTIONS ====

# ==== STRING COPY FUNCTION ====
# copies source string to destination
# Input: a0 = source, a1 = destination

my_strcpy_local:
	mv t0, a0
	mv t1, a1

my_strcpy_local_loop:
	lb t2, 0(t0)
	sb t2, 0(t1)
	beqz t2, my_strcpy_local_done
	addi t0, t0, 1
	addi t1, t1, 1
	j my_strcpy_local_loop

my_strcpy_local_done:
	ret

# ==== END OF STRING COPY FUNCTION ====

# ==== APPEND TWO DIGITS FUNCTION ====
# appends two-digit number to buffer
# Input: a0 = number (0-99), a1 = buffer pointer

append_two_digits:
	li t0, 10
	div t1, a0, t0 # t1 = tens digit
	rem t2, a0, t0 # t2 = ones digit
	
	addi t1, t1, '0' # Convert to ASCII
	addi t2, t2, '0'
	
	sb t1, 0(a1) # Store tens
	sb t2, 1(a1) # Store ones
	sb zero, 2(a1) # Null terminator
	ret

# ==== END OF APPEND TWO DIGITS FUNCTION ====

failure:
	sb zero, 0(s2)      # Clear output buffer on failure
	li a0, 0
	lw s2, 0(sp)
	lw s1, 4(sp)
	lw s0, 8(sp)
	lw ra, 12(sp)
	addi sp, sp, 80
	ret

success:
	li a0, 1
	lw s2, 0(sp)
	lw s1, 4(sp)
	lw s0, 8(sp)
	lw ra, 12(sp)
	addi sp, sp, 80
	ret

