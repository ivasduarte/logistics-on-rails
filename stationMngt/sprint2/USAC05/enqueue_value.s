.globl enqueue_value

# int enqueue_value(int *buffer, int length, int *nelem, int *tail, int *head, int value)
# a0 = buffer
# a1 = length
# a2 = nelem
# a3 = tail (read position)
# a4 = head (write position)
# a5 = value

enqueue_value:
    # Load pointer values
    lw t0, 0(a2)        # t0 = *nelem (number of elements)
    lw t1, 0(a3)        # t1 = *tail (read position)
    lw t2, 0(a4)        # t2 = *head (write position)

    # Check if buffer is full (*nelem == length)
    bne t0, a1, buffer_not_full

buffer_full:
    # Buffer is full - overwrite at head position
    # Calculate offset: head * 4 (each int has 4 bytes)
    slli t3, t2, 2      # t3 = head * 4
    add t3, a0, t3      # t3 = buffer + (head * 4)
    sw a5, 0(t3)        # buffer[head] = value

    # Update head: head = (head + 1) % length
    addi t2, t2, 1      # head++
    rem t2, t2, a1      # head = head % length
    sw t2, 0(a4)        # *head = new value

    # Update tail: tail = (tail + 1) % length (oldest element moves)
    addi t1, t1, 1      # tail++
    rem t1, t1, a1      # tail = tail % length
    sw t1, 0(a3)        # *tail = new value

    # Return 1 (buffer is full)
    li a0, 1
    ret

buffer_not_full:
    # Buffer is not full - insert at head position
    # Calculate offset: head * 4
    slli t3, t2, 2      # t3 = head * 4
    add t3, a0, t3      # t3 = buffer + (head * 4)
    sw a5, 0(t3)        # buffer[head] = value

    # Update head: head = (head + 1) % length
    addi t2, t2, 1      # head++
    rem t2, t2, a1      # head = head % length
    sw t2, 0(a4)        # *head = new value

    # Increment nelem
    addi t0, t0, 1      # nelem++
    sw t0, 0(a2)        # *nelem = new value

    # Check if buffer is now full after insertion
    beq t0, a1, return_one

    # Return 0 (buffer is not full)
    li a0, 0
    ret

return_one:
    li a0, 1            # Return 1 (buffer is full)
    ret
