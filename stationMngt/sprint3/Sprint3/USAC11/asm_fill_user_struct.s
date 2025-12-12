.section .text
.global asm_fill_user_struct

# void asm_fill_user_struct(User *user, char *name, char *username, char *password, int key)
# a0: user
# a1: name (char*)
# a2: username (char*)
# a3: password (char*)
# a4: key (int)

asm_fill_user_struct:
    sw a1, 0(a0)
    sw a2, 4(a0)
    sw a3, 8(a0)
    sw a4, 12(a0)
    ret
