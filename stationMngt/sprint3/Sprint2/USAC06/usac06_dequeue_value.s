.section .text
.global dequeue_value

dequeue_value:
	lw  t0, 0(a2)
	beqz t0, buffer_empty

	lw t1, 0(a3)
	slli t2, t1, 2
	add t2, a0, t2
	lw t3, 0(t2)
	sw t3, 0(a5)

	addi t1, t1, 1
	blt t1, a1, no_wrap
	li t1, 0

no_wrap:
	sw t1, 0(a3)
	addi t0, t0, -1
	sw t0, 0(a2)
	li a0, 1
	ret

buffer_empty:
	li a0, 0
	ret
