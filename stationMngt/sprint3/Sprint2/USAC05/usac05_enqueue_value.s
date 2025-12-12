.section .text
.globl enqueue_value

# int enqueue_value(int *buffer, int length, int *nelem, int *tail, int *head, int value)

enqueue_value:
    lw t0, 0(a2)        # t0 = *nelem
    lw t1, 0(a3)        # t1 = *tail

    bne t0, a1, buffer_not_full

buffer_full:
    slli t3, t1, 2
    add t3, a0, t3
    sw a5, 0(t3)

    addi t1, t1, 1
    rem t1, t1, a1
    sw t1, 0(a3)

    lw t2, 0(a4)
    addi t2, t2, 1
    rem t2, t2, a1
    sw t2, 0(a4)

    li a0, 1
    ret

buffer_not_full:
    slli t3, t1, 2
    add t3, a0, t3
    sw a5, 0(t3)

    addi t1, t1, 1
    rem t1, t1, a1
    sw t1, 0(a3)

    addi t0, t0, 1
    sw t0, 0(a2)

    bne t0, a1, return_zero
    li a0, 1
    ret

return_zero:
    li a0, 0
    ret
