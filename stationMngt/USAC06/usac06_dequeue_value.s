.section .text 			# Defines that this code will be executable
	.global dequeue_value 	# Makes the func accessable to other files

# int dequeue_value(int* buffer, int length, int *nelem, int* tail, int* head, int* value);

# Parameters
#	a0 = buffer 	# Pointer for the array
#	a1 = length 	# Size of the buffer
#	a2 = nelem 	# Pointer for the number of elements
#	a3 = tail 	# Pointer for tail
#	a4 = head 	# Pointer for head
#	a5 = value 	# Pointer for return value


dequeue_value:
	# Verify if the buffer if empty (nelem == 0)
	lw  t0, 0(a2)	 	# t0 = *nelem
	beqz t0, buffer_empty	# If nelem == 0, buffer is empty

	# If buffer != 0, proceed with dequeue
	# Read value in the tail position
	lw t1, 0(a3)		# t1 = *tail
	slli t2, t1, 2		# t2 = t1 * 4 (size of int)
	add t2, a0, t2		# t2 = &buffer[tail]
	lw t3, 0(t2)		# t3 = buffer[tail]
	sw t3, 0(a5)		# *value = buffer[tail]

	# Increment the tail
	addi t1, t1, 1		# tail++
	blt t1, a1, no_wrap	# If tail < length, wrap isn't needed
	li t1, 0		# tail = 0 (wrap around)

no_wrap:
	sw t1, 0(a3)		# *tail = new tail

	# Decrement nelem
	addi t0, t0, -1		# nelem--
	sw t0, 0(a2)		# *nelem = new nelem

	# Return success case
	li a0, 1		# a0 = 1 (return)
	ret

buffer_empty:
	# Return failure
	li a0, 0		# a0 = 0 (return)
	ret
