.text
.globl encrypt_data

#   a0 - char* in  (input string pointer)
#   a1 - int key   (encryption key)
#   a2 - char* out (output string pointer)
# Returns:
#   a0 - 1 on success, 0 on failure

encrypt_data:
    # Validations for the inputs
    li t0, 0
    blt a1, t0, error
    li t0, 26
    bgt a1, t0, error

    beqz a0, error
    beqz a2, error

    # Initialize the pointers and the key
    mv t1, a0                   # t1 = input pointer
    mv t2, a2                   # t2 = output pointer
    mv t3, a1                   # t3 = key

encrypt_loop:
    lbu t4, 0(t1)
    beqz t4, end_success

    # Character's validation: must be 'A' to 'Z' (65 to 90 [ASCII values])
    li t5, 65
    blt t4, t5, error_clear
    li t5, 90
    bgt t4, t5, error_clear

    # Encryption
    li t5, 65
    sub t4, t4, t5
    add t4, t4, t3

    li t5, 26
    remu t4, t4, t5

    li t5, 65
    add t4, t4, t5

    # Store the encrypted character
    sb t4, 0(t2)

    # Moves to the next character
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
