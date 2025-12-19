.section .text
.globl extract_data

# int extract_data(char* str, char* token, char* unit, int* value)
# a0 = str (input string)
# a1 = token (TOKEN to search for: "TEMP" or "HUM")
# a2 = unit (output string for unit)
# a3 = value (output pointer for value)
#
# Format: TEMP&unit:celsius&value:35#HUM&unit:percentage&value:65
# Returns: 1 on success, 0 on failure

extract_data:
    addi sp, sp, -32
    sw ra, 28(sp)
    sw s0, 24(sp)
    sw s1, 20(sp)
    sw s2, 16(sp)
    sw s3, 12(sp)
    sw s4, 8(sp)
    sw s5, 4(sp)
    
    # Save parameters
    mv s0, a0        # s0 = str
    mv s1, a1        # s1 = token
    mv s2, a2        # s2 = unit (output)
    mv s3, a3        # s3 = value (output pointer)
    
    # Find token in string
    mv a0, s0        # str
    mv a1, s1        # token
    call find_token
    
    beqz a0, extract_fail   # Token not found
    mv s4, a0        # s4 = position after token
    
    # Now s4 points to '&unit:...'
    # Skip '&unit:'
    li t0, '&'
    lb t1, 0(s4)
    bne t1, t0, extract_fail
    addi s4, s4, 1       # Skip '&'
    
    # Check for 'unit:'
    li t0, 'u'
    lb t1, 0(s4)
    bne t1, t0, extract_fail
    addi s4, s4, 1
    
    li t0, 'n'
    lb t1, 0(s4)
    bne t1, t0, extract_fail
    addi s4, s4, 1
    
    li t0, 'i'
    lb t1, 0(s4)
    bne t1, t0, extract_fail
    addi s4, s4, 1
    
    li t0, 't'
    lb t1, 0(s4)
    bne t1, t0, extract_fail
    addi s4, s4, 1
    
    li t0, ':'
    lb t1, 0(s4)
    bne t1, t0, extract_fail
    addi s4, s4, 1       # Skip ':'
    
    # Now s4 points to the unit value (e.g., "celsius&value:...")
    # Extract unit until '&'
    mv s5, s2            # s5 = unit output pointer
    
extract_unit_loop:
    lb t0, 0(s4)
    beqz t0, extract_fail    # End of string
    li t1, '&'
    beq t0, t1, extract_unit_done
    sb t0, 0(s5)
    addi s4, s4, 1
    addi s5, s5, 1
    j extract_unit_loop
    
extract_unit_done:
    # Null-terminate unit string
    sb zero, 0(s5)
    
    # Now s4 points to '&value:XX'
    # Skip '&value:'
    li t0, '&'
    lb t1, 0(s4)
    bne t1, t0, extract_fail
    addi s4, s4, 1       # Skip '&'
    
    # Check for 'value:'
    li t0, 'v'
    lb t1, 0(s4)
    bne t1, t0, extract_fail
    addi s4, s4, 1
    
    li t0, 'a'
    lb t1, 0(s4)
    bne t1, t0, extract_fail
    addi s4, s4, 1
    
    li t0, 'l'
    lb t1, 0(s4)
    bne t1, t0, extract_fail
    addi s4, s4, 1
    
    li t0, 'u'
    lb t1, 0(s4)
    bne t1, t0, extract_fail
    addi s4, s4, 1
    
    li t0, 'e'
    lb t1, 0(s4)
    bne t1, t0, extract_fail
    addi s4, s4, 1
    
    li t0, ':'
    lb t1, 0(s4)
    bne t1, t0, extract_fail
    addi s4, s4, 1       # Skip ':'
    
    # Now s4 points to the numeric value
    # Parse integer until '#' or end of string
    li t2, 0             # t2 = accumulated value
    li t6, 0             # t6 = digit count
    
extract_value_loop:
    lb t0, 0(s4)
    beqz t0, extract_value_done    # End of string
    li t1, '#'
    beq t0, t1, extract_value_done # End marker for next token
    li t1, ' '
    beq t0, t1, extract_value_done # Space (shouldn't happen but safe)
    
    # Check if digit
    li t1, '0'
    blt t0, t1, extract_fail
    li t1, '9'
    bgt t0, t1, extract_fail
    
    # Convert char to digit and accumulate
    addi t0, t0, -48     # Convert ASCII to digit
    li t3, 10
    mul t2, t2, t3       # value *= 10
    add t2, t2, t0       # value += digit
    
    addi s4, s4, 1
    addi t6, t6, 1       # Increment digit count
    j extract_value_loop
    
extract_value_done:
    # Check if we got at least one digit
    beqz t6, extract_fail
    
    # Store value
    sw t2, 0(s3)
    
    # Success
    li a0, 1
    j extract_cleanup
    
extract_fail:
    # Set value to 0 and unit to empty string
    sw zero, 0(s3)
    sb zero, 0(s2)
    li a0, 0
    
extract_cleanup:
    lw ra, 28(sp)
    lw s0, 24(sp)
    lw s1, 20(sp)
    lw s2, 16(sp)
    lw s3, 12(sp)
    lw s4, 8(sp)
    lw s5, 4(sp)
    addi sp, sp, 32
    ret

# Helper function: find_token
# a0 = str
# a1 = token
# Returns: pointer to character after token, or 0 if not found
find_token:
    addi sp, sp, -16
    sw s0, 12(sp)
    sw s1, 8(sp)
    sw s2, 4(sp)
    
    mv s0, a0        # s0 = str
    mv s1, a1        # s1 = token
    
find_token_loop:
    lb t0, 0(s0)
    beqz t0, find_token_fail    # End of string
    
    # Check if current position matches token
    mv s2, s0        # s2 = current position in str
    mv t1, s1        # t1 = token pointer
    
find_token_match:
    lb t2, 0(t1)
    beqz t2, find_token_found   # End of token = match!
    lb t3, 0(s2)
    bne t2, t3, find_token_next # Mismatch
    
    addi t1, t1, 1
    addi s2, s2, 1
    j find_token_match
    
find_token_next:
    addi s0, s0, 1
    j find_token_loop
    
find_token_found:
    # Return pointer to character after token
    mv a0, s2
    j find_token_cleanup
    
find_token_fail:
    li a0, 0
    
find_token_cleanup:
    lw s0, 12(sp)
    lw s1, 8(sp)
    lw s2, 4(sp)
    addi sp, sp, 16
    ret