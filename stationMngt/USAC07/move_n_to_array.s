.section .text
.global move_n_to_array

move_n_to_array:
    lw t0, 0(a2)           
    lw t1, 0(a3)            
    mv t2, a1               
    mv t3, a5               
    mv t4, a0               
    mv t5, a6               
    
    bltz t3, us7_fail       
    blt  t0, t3, us7_fail   
    
us7_loop:
    beqz t3, us7_done
    slli a4, t1, 2          
    add  a4, t4, a4         
    lw   a7, 0(a4)         
    sw   a7, 0(t5)         
    addi t5, t5, 4
    addi t1, t1, 1
    bne  t1, t2, skip_wrap
    mv   t1, zero
skip_wrap:
    addi t0, t0, -1
    addi t3, t3, -1
    j    us7_loop
    
us7_done:
    sw t1, 0(a3)           
    sw t0, 0(a2)            
    li a0, 1
    ret
    
us7_fail:
    mv a0, zero
    ret

.size move_n_to_array, .-move_n_to_array
