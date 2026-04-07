// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.

// Runs an infinite loop that listens to the keyboard input. 
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel. When no key is pressed, 
// the screen should be cleared.

(KBD_LISTENER)
    @SCREEN
    D=A

    @address
    M=D

    @8192
    D=A

    @count
    M=D

 	@KBD
 	D=M

 	@color
 	M=0

 	@BLACK
 	D;JNE

 	@LOOP
 	0;JMP

 (BLACK)
 	@color
 	M=-1

(LOOP)
	@color
	D=M

	@address
	// D=M
	A=M
	M=D

	@address
	M=M+1

	@count
	M=M-1
	D=M

	@LOOP
	D;JGT

	@KBD_LISTENER
	D;JEQ
