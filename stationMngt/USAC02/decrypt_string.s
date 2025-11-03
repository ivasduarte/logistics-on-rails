.text
.globl decrypt_string

#   a0 - char* in  (input string pointer)
#   a1 - int key   (decryption key)
#   a2 - char* out (output string pointer)
# Returns:
#   a0 - 1 on success, 0 on failure

decrypt_string:
    # Validations for the inputs
    li t0, 1
    blt a1, t0, decrypt_error
    li t0, 26
    bgt a1, t0, decrypt_error

    beqz a0, decrypt_error
    beqz a2, decrypt_error

    # Initialize the pointers and the key
    mv t1, a0                   # t1 = input pointer
    mv t2, a2                   # t2 = output pointer
    mv t3, a1                   # t3 = key

decrypt_loop:
    lbu t4, 0(t1)
    beqz t4, decrypt_end_success

    # Character's validation: must be 'A' to 'Z' (65 to 90 [ASCII values])
    li t5, 65
    blt t4, t5, decrypt_error_clear
    li t5, 90
    bgt t4, t5, decrypt_error_clear

    # Decryption
    li t5, 65
    sub t4, t4, t5
    sub t4, t4, t3
    li t5, 26
    add t4, t4, t5

    li t5, 26
    remu t4, t4, t5

    li t5, 65
    add t4, t4, t5

    # Store decrypted character
    sb t4, 0(t2)

    # Moves to next character
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