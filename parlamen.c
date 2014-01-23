/*
 *  3. Parlament (palamen.in, parlamen.out)
 *  --fiction (16.4.2001)
 */

#define DEBUG

#include <stdio.h>
#include <stdlib.h>

#ifdef DEBUG
#include <conio.h>
#endif

int N = 0;
int *Elements = NULL;

FILE *StreamOut, *StreamIn;

void Assert(int b, char *msg) {
	if (!b) {
		fprintf(StreamOut, "---ASSERT FAILED---\n");
		fprintf(StreamOut, "Error: %s\n\n", msg);
		exit(1);
	}
}

int Knapsack(int w, int try) {
	if (w == 0) { return 1; }
	if ((w < 0) || (try >= N)) { return 0; }

	if (Knapsack(w-Elements[try], try+1)) {
		fprintf(StreamOut, "%d ", try+1);
		/* try+1 because of elements are 1..N and not 0..N-1 */
		return 1;
	} else { return Knapsack(w, try+1); }
}

int main() {
	int i;
	int P = 0;

#ifdef DEBUG
	StreamIn = stdin;
	StreamOut = stdout;
	clrscr();
#else
	StreamIn = fopen("parlamen.in", "rt");
	StreamOut = fopen("parlamen.out", "wt");
#endif

	fscanf(StreamIn, "%d", &N);
	Assert((N >= 1) && (N <= 20), "Too many elements.");
	Elements = malloc(N * sizeof(int));
	Assert(Elements!=NULL, "Couldn't malloc().");

	for (i=0; i<N; ++i) {
		fscanf(StreamIn, "%d", Elements+i);
		P+=Elements[i];
	}

	if (N == 1) { fprintf(StreamOut, "1 "); } else {
		for (i=(P/2)+1; (i<P) && (!Knapsack(i, 0)); ++i);
	}
}
