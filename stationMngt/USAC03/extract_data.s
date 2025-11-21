.globl extract_data
.type extract_data, @function

# int extract_data(char* str, char* token, char* unit, int* value)
# a0 = str, a1 = token, a2 = unit, a3 = value
extract_data:
    addi sp, sp, -32
    sw ra, 28(sp)
    sw s0, 24(sp)
    sw s1, 20(sp)
    sw s2, 16(sp)
    sw s3, 12(sp)
    sw s4, 8(sp)
    
    mv s0, a0          # s0 = str
    mv s1, a1          # s1 = token
    mv s2, a2          # s2 = unit
    mv s3, a3          # s3 = value
    
    # Start searching from the beginning of str
    mv t0, s0          # t0 = current position in str
    
search_token:
    lb t1, 0(t0)       # Load current char
    beqz t1, fail      # If null terminator, token not found
    
    # Initialize comparison pointers
    mv t2, t0          # t2 = iterator for str
    mv t3, s1          # t3 = iterator for token
    
compare_loop:
    lb t4, 0(t3)       # Load char from token
    beqz t4, token_found  # If end of token, match found
    lb t5, 0(t2)       # Load char from str
    bne t4, t5, next_pos  # Mismatch, try next position
    addi t2, t2, 1
    addi t3, t3, 1
    j compare_loop
    
next_pos:
    addi t0, t0, 1     # Advance to next char in str
    j search_token
    
token_found:
    # Ensure token is at start of string or preceded by '#'
    beq t0, s0, check_suffix    # Check if at start
    lb t5, -1(t0)               # Load previous char
    li t6, '#'
    bne t5, t6, next_pos        # If not '#', it's a partial match (fail)

check_suffix:
    # Check if token is followed by '&'
    lb t1, 0(t2)
    li t4, '&'
    bne t1, t4, next_pos
    
    # Search for "unit:" label
    mv t0, t2          # t0 = position after token
find_unit_label:
    lb t1, 0(t0)
    beqz t1, fail
    li t2, '&'
    bne t1, t2, skip_char
    
    # Check specifically for "unit:"
    lb t1, 1(t0)
    li t2, 'u'
    bne t1, t2, skip_char
    lb t1, 2(t0)
    li t2, 'n'
    bne t1, t2, skip_char
    lb t1, 3(t0)
    li t2, 'i'
    bne t1, t2, skip_char
    lb t1, 4(t0)
    li t2, 't'
    bne t1, t2, skip_char
    lb t1, 5(t0)
    li t2, ':'
    bne t1, t2, skip_char
    
    # "unit:" found, skip it
    addi t0, t0, 6
    j extract_unit
    
skip_char:
    # If '#' is found before unit, this token is invalid
    li t2, '#'
    beq t1, t2, fail
    
    addi t0, t0, 1
    j find_unit_label
    
extract_unit:
    # Copy characters to unit buffer
    mv t1, s2          # t1 = destination
copy_unit:
    lb t2, 0(t0)
    beqz t2, fail      # Unexpected end of string
    li t3, '&'
    beq t2, t3, end_unit
    
    # Stop if delimiter '#' is found (invalid format)
    li t3, '#'
    beq t2, t3, fail
    
    sb t2, 0(t1)
    addi t1, t1, 1
    addi t0, t0, 1
    j copy_unit
    
end_unit:
    sb zero, 0(t1)     # Null-terminate unit string
    
    # Search for "value:" label
find_value_label:
    lb t1, 0(t0)
    beqz t1, fail
    
    # Check specifically for "value:"
    lb t1, 0(t0)
    li t2, 'v'
    bne t1, t2, skip_value_char
    lb t1, 1(t0)
    li t2, 'a'
    bne t1, t2, skip_value_char
    lb t1, 2(t0)
    li t2, 'l'
    bne t1, t2, skip_value_char
    lb t1, 3(t0)
    li t2, 'u'
    bne t1, t2, skip_value_char
    lb t1, 4(t0)
    li t2, 'e'
    bne t1, t2, skip_value_char
    lb t1, 5(t0)
    li t2, ':'
    bne t1, t2, skip_value_char
    
    # "value:" found, skip it
    addi t0, t0, 6
    j parse_value
    
skip_value_char:
    # If '#' is found before value, this token is invalid
    li t2, '#'
    beq t1, t2, fail
    
    addi t0, t0, 1
    j find_value_label
    
parse_value:
    # Convert string to integer
    mv t1, zero        # t1 = result
    mv t2, zero        # t2 = sign flag
    
    # Check for negative sign
    lb t3, 0(t0)
    li t4, '-'
    bne t3, t4, parse_digits
    li t2, 1
    addi t0, t0, 1
    
parse_digits:
    lb t3, 0(t0)
    
    # Stop if delimiter '#' is found
    li t4, '#'
    beq t3, t4, end_parse
    
    # Check if char is a digit
    li t4, '0'
    blt t3, t4, end_parse
    li t4, '9'
    bgt t3, t4, end_parse
    
    # Accumulate digit: result = result * 10 + (char - '0')
    li t5, 10
    mul t1, t1, t5
    addi t3, t3, -'0'
    add t1, t1, t3
    
    addi t0, t0, 1
    j parse_digits
    
end_parse:
    # Apply sign if negative
    beqz t2, store_value
    neg t1, t1
    
store_value:
    sw t1, 0(s3)       # Store result in value pointer
    li a0, 1           # Return success
    j cleanup
    
fail:
    # Handle failure cases
    sw zero, 0(s3)     # Set value to 0
    sb zero, 0(s2)     # Set unit to empty string
    li a0, 0           # Return failure
    
cleanup:
    # Restore registers and stack
    lw ra, 28(sp)
    lw s0, 24(sp)
    lw s1, 20(sp)
    lw s2, 16(sp)
    lw s3, 12(sp)
    lw s4, 8(sp)
    addi sp, sp, 32
    ret

.size extract_data, .-extract_data