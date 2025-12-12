.globl extract_data
.type extract_data, @function

extract_data:
    addi sp, sp, -32
    sw ra, 28(sp)
    sw s0, 24(sp)
    sw s1, 20(sp)
    sw s2, 16(sp)
    sw s3, 12(sp)
    sw s4, 8(sp)
    
    mv s0, a0
    mv s1, a1
    mv s2, a2
    mv s3, a3
    
    mv t0, s0
    
search_token:
    lb t1, 0(t0)
    beqz t1, fail
    
    mv t2, t0
    mv t3, s1
    
compare_loop:
    lb t4, 0(t3)
    beqz t4, token_found
    lb t5, 0(t2)
    bne t4, t5, next_pos
    addi t2, t2, 1
    addi t3, t3, 1
    j compare_loop
    
next_pos:
    addi t0, t0, 1
    j search_token
    
token_found:
    beq t0, s0, check_suffix
    lb t5, -1(t0)
    li t6, '#'
    bne t5, t6, next_pos

check_suffix:
    lb t1, 0(t2)
    li t4, '&'
    bne t1, t4, next_pos
    
    mv t0, t2
find_unit_label:
    lb t1, 0(t0)
    beqz t1, fail
    li t2, '&'
    bne t1, t2, skip_char
    
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
    
    addi t0, t0, 6
    j extract_unit
    
skip_char:
    li t2, '#'
    beq t1, t2, fail
    addi t0, t0, 1
    j find_unit_label
    
extract_unit:
    mv t1, s2
copy_unit:
    lb t2, 0(t0)
    beqz t2, fail
    li t3, '&'
    beq t2, t3, end_unit
    li t3, '#'
    beq t2, t3, fail
    sb t2, 0(t1)
    addi t1, t1, 1
    addi t0, t0, 1
    j copy_unit
    
end_unit:
    sb zero, 0(t1)
    
find_value_label:
    lb t1, 0(t0)
    beqz t1, fail
    
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
    
    addi t0, t0, 6
    j parse_value
    
skip_value_char:
    li t2, '#'
    beq t1, t2, fail
    addi t0, t0, 1
    j find_value_label
    
parse_value:
    mv t1, zero
    mv t2, zero
    lb t3, 0(t0)
    li t4, '-'
    bne t3, t4, parse_digits
    li t2, 1
    addi t0, t0, 1
    
parse_digits:
    lb t3, 0(t0)
    li t4, '#'
    beq t3, t4, end_parse
    li t4, '0'
    blt t3, t4, end_parse
    li t4, '9'
    bgt t3, t4, end_parse
    
    li t5, 10
    mul t1, t1, t5
    addi t3, t3, -'0'
    add t1, t1, t3
    addi t0, t0, 1
    j parse_digits
    
end_parse:
    beqz t2, store_value
    neg t1, t1
    
store_value:
    sw t1, 0(s3)
    li a0, 1
    j cleanup
    
fail:
    sw zero, 0(s3)
    sb zero, 0(s2)
    li a0, 0
    
cleanup:
    lw ra, 28(sp)
    lw s0, 24(sp)
    lw s1, 20(sp)
    lw s2, 16(sp)
    lw s3, 12(sp)
    lw s4, 8(sp)
    addi sp, sp, 32
    ret
