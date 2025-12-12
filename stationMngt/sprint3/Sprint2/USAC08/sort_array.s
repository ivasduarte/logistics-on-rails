.text
.globl sort_array

sort_array:
	blez a1, sort_fail

	addi sp, sp, -20
	sw ra, 16(sp)
	sw s0, 12(sp)
	sw s1, 8(sp)
	sw s2, 4(sp)
	sw s3, 0(sp)

	mv s0, a0
	mv s1, a1
	mv s2, a2

	li t0, 0

outer_loop:
	addi t1, s1, -1
	bge t0, t1, sort_success

	li t2, 0
	sub t3, s1, t0
	addi t3, t3, -1

inner_loop:
	bge t2, t3, inner_done

	slli t4, t2, 2
	add t5, s0, t4
	lw a3, 0(t5)
	lw a4, 4(t5)

	beqz s2, descending_compare

ascending_compare:
	ble a3, a4, no_swap
	j do_swap

descending_compare:
	bge a3, a4, no_swap

do_swap:
	sw a4, 0(t5)
	sw a3, 4(t5)

no_swap:
	addi t2, t2, 1
	j inner_loop

inner_done:
	addi t0, t0, 1
	j outer_loop

sort_success:
	lw ra, 16(sp)
	lw s0, 12(sp)
	lw s1, 8(sp)
	lw s2, 4(sp)
	sw s3, 0(sp)
	addi sp, sp, 20

	li a0, 1
	ret

sort_fail:
	li a0, 0
	ret
