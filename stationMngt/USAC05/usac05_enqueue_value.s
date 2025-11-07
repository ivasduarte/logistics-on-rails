.globl enqueue_value

# int enqueue_value(int *buffer, int length, int *nelem, int *tail, int *head, int value)
# a0 = buffer
# a1 = length
# a2 = nelem
# a3 = tail
# a4 = head
# a5 = value

enqueue_value:
    # load pointer values
    lw t0, 0(a2)        # t0 = *nelem (number of elements)
    lw t1, 0(a3)        # t1 = *tail (position of element to be removed)
    lw t2, 0(a4)        # t2 = *head (position of element to be added)

    # verify if buffer is full (*nelem == length)
    bne t0, a1, buffer_not_full

buffer_full:
    # buffer is full - delete oldest element (position 0)
    # determine offset: tail * 4 (each int has 4 bytes)
    slli t3, t1, 2      # t3 = tail * 4
    add t3, a0, t3      # t3 = buffer + (tail * 4)
    sw a5, 0(t3)        # buffer[tail] = value

    # update tail: tail = (tail + 1) % length
    addi t1, t1, 1      # tail++
    rem t1, t1, a1      # tail = tail % length
    sw t1, 0(a3)        # *tail = new value

    # update head: head = (head + 1) % length
    addi t2, t2, 1      # head++
    rem t2, t2, a1      # head = head % length
    sw t2, 0(a4)        # *head = new value

    # call return_one (buffer is full)
    j return_one

buffer_not_full:
    # buffer is not full - insert new element normally
    # determine offset: tail * 4
    slli t3, t1, 2      # t3 = tail * 4
    add t3, a0, t3      # t3 = buffer + (tail * 4)
    sw a5, 0(t3)        # buffer[tail] = value

    # update tail: tail = (tail + 1) % length
    addi t1, t1, 1      # tail++
    rem t1, t1, a1      # tail = tail % length
    sw t1, 0(a3)        # *tail = new value

    # increment nelem
    addi t0, t0, 1      # nelem++
    sw t0, 0(a2)        # *nelem = new value

    # verify if buffer is full after insertion
    bne t0, a1, return_zero

return_one:
    li a0, 1            # return 1 (buffer is full)
    ret

return_zero:
    li a0, 0            # return 0 (buffer is not full)
    ret