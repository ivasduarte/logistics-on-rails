.text
.globl decrypt_data

decrypt_data:
    li t0, 1
    blt a1, t0, decrypt_error
    li t0, 26
    bgt a1, t0, decrypt_error

    beqz a0, decrypt_error
    beqz a2, decrypt_error

    mv t1, a0
    mv t2, a2
    mv t3, a1

decrypt_loop:
    lbu t4, 0(t1)
    beqz t4, decrypt_end_success

    li t5, 65
    blt t4, t5, decrypt_error_clear
    li t5, 90
    bgt t4, t5, decrypt_error_clear

    li t5, 65
    sub t4, t4, t5
    sub t4, t4, t3
    
    bltz t4, wrap_around
    j mod_operation

wrap_around:
    li t5, 26
    add t4, t4, t5

mod_operation:
    li t5, 26
    rem t4, t4, t5
    li t5, 65
    add t4, t4, t5

    sb t4, 0(t2)
    addi t1, t1, 1
    addi t2, t2, 1
    j decrypt_loop

decrypt_end_success:
    sb zero, 0(t2)
    li a0, 1
    ret

decrypt_error_clear:
    sb zero, 0(a2)
decrypt_error:
    li a0, 0
    ret
