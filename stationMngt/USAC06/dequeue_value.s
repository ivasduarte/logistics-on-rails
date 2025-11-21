.section .text
	.global dequeue_value

# int dequeue_value(int* buffer, int length, int *nelem, int* tail, int* head, int* value);

# Parameters
#	a0 = buffer         # Pointer to the array
#	a1 = length         # Array size
#	a2 = nelem          # Pointer to number of elements in array
#	a3 = tail           # Pointer to end of array
#	a4 = head           # Pointer to start of array
#	a5 = value          # Value to return

# Note: head (a4) is not used in dequeue operation
# It's kept in the interface for consistency with enqueue_value

dequeue_value:
	# Check if number of elements is zero
	lw  t0, 0(a2)	 	            # t0 = *nelem
	beqz t0, buffer_empty	        # If nelem == 0, buffer is empty

	# If buffer != 0, proceed with dequeue

	# Read value in the tail position
	lw t1, 0(a3)		            # t1 = *tail
	add t2, t1, t1		            # t2 = t1 * 2
	add t2, t2, t2		            # t2 = t2 * 2
	add t2, a0, t2		            # t2 = &buffer[tail]
	lw t3, 0(t2)		            # t3 = buffer[tail]
	sw t3, 0(a5)		            # *value = buffer[tail]

	# Increment the tail
	addi t1, t1, 1		            # tail++
	blt t1, a1, no_wrap	            # If tail < length, wrap around isn't needed
	li t1, 0		                # tail = 0, goes back to the beginning of the buffer

no_wrap:
	sw t1, 0(a3)		            # *tail = new tail

	# Decrement nelem
	addi t0, t0, -1		            # nelem--
	sw t0, 0(a2)		            # *nelem = new nelem

	# Return success case
	li a0, 1		                # a0 = 1 (return)
	ret

buffer_empty:
	# Return failure
	li a0, 0		                # a0 = 0 (return)
	ret