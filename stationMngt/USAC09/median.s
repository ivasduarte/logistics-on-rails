.text
.globl median

# int median(int* vec, int length, int *me)
# a0 = vec (pointer to array)
# a1 = length
# a2 = me (pointer to stored median value)
# Returns: a0 = 1 if success, 0 if fail

median:
	blez a1, median_fail

	addi sp, sp, -20
	sw ra, 16(sp)
	sw s0, 12(sp)
	sw s1, 8(sp)
	sw s2, 4(sp)
	sw s3, 0(sp)

	# s0 = vec
	# s1 = length
	# s2 = me
	mv s0, a0
	mv s1, a1
	mv s2, a2

	mv a0, s0	# a0 = vec
	mv a1, s1	# a1 = length
	li a2, 1	# a2 = 1 (sort in ascending order)
	call sort_array

	# Check if sort_array succeded
	beqz a0, median_restore

	andi t0, s1, 1
	bnez t0, odd_length

even_length:
	srli t1, s1, 1
	addi t2, t1, -1

	slli t3, t2, 2
	add t4, s0, t3
	lw t5, 0(t4)

	slli t3, t1, 2
	add t4, s0, t3
	lw t6, 0(t4)

	# Store the sum to preserve fractional values
	add s3, t5, t6

	j store_median

odd_length:
	srli t1, s1, 1

	slli t3, t1, 2
	add t4, s0, t3
	lw s3, 0(t4)

store_median:
	# Store median value to *me
	sw s3, 0(s2)

	lw ra, 16(sp)
	lw s0, 12(sp)
	lw s1, 8(sp)
	lw s2, 4(sp)
	lw s3, 0(sp)
	addi sp, sp, 20

	li a0, 1
	ret

median_restore:
	lw ra, 16(sp)
	lw s0, 12(sp)
	lw s1, 8(sp)
	lw s2, 4(sp)
	lw s3, 0(sp)
	addi sp, sp, 20

median_fail:
	li a0, 0
	ret
