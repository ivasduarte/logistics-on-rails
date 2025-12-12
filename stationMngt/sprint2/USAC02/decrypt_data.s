.text
.globl decrypt_data

#   a0 - char* in  (input string pointer)
#   a1 - int key   (decryption key)
#   a2 - char* out (output string pointer)
# Returns:
#   a0 - 1 on success, 0 on failure

decrypt_data:
    li t0, 1
    blt a1, t0, decrypt_error   # Key must be non-negative
    li t0, 26
    bgt a1, t0, decrypt_error   # Key must be <= 26

    beqz a0, decrypt_error      # Input pointer check
    beqz a2, decrypt_error      # Output pointer check

    # Initialize the pointers and the key
    mv t1, a0                   # t1 = input pointer
    mv t2, a2                   # t2 = output pointer
    mv t3, a1                   # t3 = key

decrypt_loop:
    lbu t4, 0(t1)               # Load character
    beqz t4, decrypt_end_success # End of string

    #Must be 'A' to 'Z' (65 to 90 [ASCII values])
    li t5, 65
    blt t4, t5, decrypt_error_clear
    li t5, 90
    bgt t4, t5, decrypt_error_clear

    #Normalize to 0-25 range
    li t5, 65
    sub t4, t4, t5              # t4 = char - 'A' (0-25)

    #Subtract the key
    sub t4, t4, t3              # t4 = (char - 'A') - key (Can be negative)

    #Handle wrap-around (if negative, add 26 to make it positive)
    bltz t4, wrap_around        # If t4 < 0, jump to fix the negative number

    j mod_operation             # If non-negative, skip fix

wrap_around:
    li t5, 26
    add t4, t4, t5              # t4 = t4 + 26 (e.g., -3 + 26 = 23)

mod_operation:
    #Use 'rem' after ensuring the value is non-negative
    li t5, 26
    rem t4, t4, t5              # t4 = t4 % 26

    #Denormalize back to ASCII ('A' to 'Z')
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
    # Null-terminate the output buffer immediately to ensure output is ""
    sb zero, 0(a2)
    # Fall through to decrypt_error
decrypt_error:
    sb zero, 0(a2)
    li a0, 0
    ret