.text
.globl encrypt_data

encrypt_data:
    li t0, 1
    blt a1, t0, error
    li t0, 26
    bgt a1, t0, error

    beqz a0, error
    beqz a2, error

    mv t1, a0
    mv t2, a2
    mv t3, a1

encrypt_loop:
    lbu t4, 0(t1)
    beqz t4, end_success

    li t5, 65
    blt t4, t5, error_clear
    li t5, 90
    bgt t4, t5, error_clear

    li t5, 65
    sub t4, t4, t5
    add t4, t4, t3
    li t5, 26
    remu t4, t4, t5
    li t5, 65
    add t4, t4, t5

    sb t4, 0(t2)
    addi t1, t1, 1
    addi t2, t2, 1
    j encrypt_loop

end_success:
    sb zero, 0(t2)
    li a0, 1
    ret

error_clear:
    sb zero, 0(a2)
error:
    li a0, 0
    ret
