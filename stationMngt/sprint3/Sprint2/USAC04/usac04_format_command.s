.section .data
re_str: .string "RE"
ye_str: .string "YE"
ge_str: .string "GE"
rb_str: .string "RB"
gth_str: .string "GTH"

.section .text
.global format_command

format_command:
	addi sp, sp, -16
	sw ra, 12(sp)
	sw s0, 8(sp)
	sw s1, 4(sp)
	sw s2, 0(sp)

	mv s0, a0
	mv s1, a1
	mv s2, a2

	blt s1, zero, failure
	li t0, 99
	bgt s1, t0, failure

	mv a0, s0
	jal trim
	mv s0, a0

	mv a0, s0
	jal to_uppercase
	
    	mv a0, s0
    	la a1, re_str
    	jal strcmp
    	beqz a0, handle_re
    
    	mv a0, s0
    	la a1, ye_str
    	jal strcmp
    	beqz a0, handle_ye
    
    	mv a0, s0
    	la a1, ge_str
    	jal strcmp
    	beqz a0, handle_ge
    
    	mv a0, s0
    	la a1, rb_str
    	jal strcmp
    	beqz a0, handle_rb
    
    	mv a0, s0
    	la a1, gth_str
    	jal strcmp
    	beqz a0, handle_gth
    
    	j failure
	
trim:
	mv t0, a0
	mv t1, a0

skip_leading:
	lb t2, 0(t1)
	beqz t2, empty_string
	li t3, 33
	bge t2, t3, found_start
	addi t1, t1, 1
	j skip_leading

found_start:
	mv a0, t1
	mv t4, t1

find_end:
	lb t2, 0(t4)
	beqz t2, start_trim_trailing
	addi t4, t4, 1
	j find_end

start_trim_trailing:
	addi t4, t4, -1

trim_trailing_loop:
	blt t4, a0, place_null
	lb t2, 0(t4)
	li t3, 33
	bge t2, t3, place_null
	addi t4, t4, -1
	j trim_trailing_loop

place_null:
	sb zero, 1(t4)
	ret

empty_string:
	sb zero, 0(a0)
	ret

to_uppercase:
	mv t0, a0

upper_loop:
	lb t1, 0(t0)
	beqz t1, upper_done
	li t2, 'a'
	blt t1, t2, upper_next
	li t2, 'z'
	bgt t1, t2, upper_next
	addi t1, t1, -32
	sb t1, 0(t0)

upper_next:
	addi t0, t0, 1
	j upper_loop

upper_done:
	ret

strcmp:
	mv t0, a0
	mv t1, a1

strcmp_loop:
	lb t2, 0(t0)
	lb t3, 0(t1)
	bne t2, t3, strcmp_not_equal
	beqz t2, strcmp_equal
	addi t0, t0, 1
	addi t1, t1, 1
	j strcmp_loop

strcmp_equal:
	li a0, 0
	ret

strcmp_not_equal:
	sub a0, t2, t3
	ret

handle_re:
	la a0, re_str
	mv a1, s2
	jal strcpy
	li t0, ','
	sb t0, 2(s2)
	mv a0, s1
	addi a1, s2, 3
	jal append_two_digits
	j success

handle_ye:
	la a0, ye_str
	mv a1, s2
	jal strcpy
	li t0, ','
	sb t0, 2(s2)
	mv a0, s1
	addi a1, s2, 3
	jal append_two_digits
	j success

handle_ge:
	la a0, ge_str
	mv a1, s2
	jal strcpy
	li t0, ','
	sb t0, 2(s2)
	mv a0, s1
	addi a1, s2, 3
	jal append_two_digits
	j success

handle_rb:
	la a0, rb_str
	mv a1, s2
	jal strcpy
	li t0, ','
	sb t0, 2(s2)
	mv a0, s1
	addi a1, s2, 3
	jal append_two_digits
	j success

handle_gth:
	la a0, gth_str
	mv a1, s2
	jal strcpy
	j success

strcpy:
	mv t0, a0
	mv t1, a1

strcpy_loop:
	lb t2, 0(t0)
	sb t2, 0(t1)
	beqz t2, strcpy_done
	addi t0, t0, 1
	addi t1, t1, 1
	j strcpy_loop

strcpy_done:
	ret

append_two_digits:
	li t0, 10
	div t1, a0, t0
	rem t2, a0, t0
	addi t1, t1, '0'
	addi t2, t2, '0'
	sb t1, 0(a1)
	sb t2, 1(a1)
	ret

success:
	li a0, 1
	lw s2, 0(sp)
	lw s1, 4(sp)
	lw s0, 8(sp)
	lw ra, 12(sp)
	addi sp, sp, 16
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
