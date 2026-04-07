// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)
// The algorithm is based on repetitive addition.
	@R2
	M=0
	@R1
	D=M
	@END
	D;JEQ
	@rest
	M=D

(LOOP)
	@R0
	D=M
	@R2
	M=D+M
	@rest
	M=M-1
	@rest
	D=M
	@LOOP
	D;JGT

(END)
	@END
	0;JMP
