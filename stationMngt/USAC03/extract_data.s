.globl extract_data
.type extract_data, @function

# int extract_data(char* str, char* token, char* unit, int* value)
# a0 = str, a1 = token, a2 = unit, a3 = value
extract_data:
    addi sp, sp, -16
    sw ra, 12(sp)
    sw s0, 8(sp)
    sw s1, 4(sp)
    sw s2, 0(sp)
    
    mv s0, a2          # s0 = unit
    mv s1, a3          # s1 = value
    mv s2, a0          # s2 = str
    
    # Procurar token em str
    mv a0, s2          # a0 = str
    mv a1, a1          # a1 = token (já está)
    call strstr
    beqz a0, fail      # token não encontrado
    
    # a0 aponta para início do token em str
    # Avançar para depois de "token&unit:"
    mv a0, a0          # posição do token
    mv a1, s2          # str original
    
find_unit_start:
    lb t0, 0(a0)
    beqz t0, fail      # fim da string
    li t1, ':'
    beq t0, t1, found_colon
    addi a0, a0, 1
    j find_unit_start
    
found_colon:
    addi a0, a0, 1     # avançar depois do ':'
    
    # Copiar unit até '&'
    mv t2, s0          # destino unit
copy_unit:
    lb t0, 0(a0)
    beqz t0, fail
    li t1, '&'
    beq t0, t1, end_unit
    sb t0, 0(t2)
    addi t2, t2, 1
    addi a0, a0, 1
    j copy_unit
    
end_unit:
    sb zero, 0(t2)     # null terminator
    
    # Avançar para depois de "value:"
find_value:
    lb t0, 0(a0)
    beqz t0, fail
    li t1, ':'
    beq t0, t1, found_value_colon
    addi a0, a0, 1
    j find_value
    
found_value_colon:
    addi a0, a0, 1     # avançar depois do ':'
    
    # Converter string para int
    mv t2, zero        # acumulador value
    mv t3, zero        # flag negativo
    
    # Verificar sinal
    lb t0, 0(a0)
    li t1, '-'
    bne t0, t1, parse_digits
    li t3, 1           # negativo
    addi a0, a0, 1
    
parse_digits:
    lb t0, 0(a0)
    li t1, '0'
    blt t0, t1, end_parse
    li t1, '9'
    bgt t0, t1, end_parse
    
    # t2 = t2 * 10 + (t0 - '0')
    li t4, 10
    mul t2, t2, t4
    addi t0, t0, -'0'
    add t2, t2, t0
    
    addi a0, a0, 1
    j parse_digits
    
end_parse:
    # Aplicar sinal se negativo
    beqz t3, store_value
    neg t2, t2
    
store_value:
    sw t2, 0(s1)       # *value = t2
    
    li a0, 1           # sucesso
    j cleanup
    
fail:
    sw zero, 0(s1)     # *value = 0
    sb zero, 0(s0)     # *unit = ""
    li a0, 0           # falha
    
cleanup:
    lw ra, 12(sp)
    lw s0, 8(sp)
    lw s1, 4(sp)
    lw s2, 0(sp)
    addi sp, sp, 16
    ret

.size extract_data, .-extract_data
