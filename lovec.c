/* lovec.c */

#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>
#include <stdlib.h>

int cur_energy = 42;
char cur_char = '.';

void output()
{
  printf("%c", cur_char);
  fflush(stdout);
  cur_energy--;

  if (cur_energy <= 0)
  {
    printf("Out of energy. Aggghhhhhrrrr.\n");
    fflush(stdout);
    exit(0);
  } 
  alarm(1);
}

void switch_char()
{
  /* Could be done a bit more elegantly */
  if (cur_char == '.')
  {
    cur_char = '*';
  }
  else if (cur_char == '*')
  {
    cur_char = '.';
  }
}

void inc_energy()
{
  cur_energy += 10;
  printf("Yahoo! Bonus energy (%d).\n", cur_energy);
  fflush(stdout);
}

void replicate()
{
  int time, status;
  pid_t pid;

  pid = fork();  
 
  if (pid == 0)
  {
    time = (cur_energy % 7) + 1;
    status = (cur_energy * 42) % 234;
    sleep(time);
    printf("Child exit with status: %d.\n", status);
    fflush(stdout);
    exit(status);
  }

  else if (pid > 0)
  {
    printf("Forked child %d.\n", pid);
    fflush(stdout);
  }
  else
  {
    printf("Fork failed.\n");
    fflush(stdout);
  } 
}

void zombie()
{
  int child_status;
  pid_t pid = wait(&child_status);
  if (WIFEXITED(child_status))
  {
    printf("Zombie caught with status: %d\n", WEXITSTATUS(child_status));
    fflush(stdout);
  }
  else
  {
    printf("Zombie %d terminated abnormally\n", pid);
    fflush(stdout);
  }
}

int main(int argc, char *argv[])
{
  printf("My PID: %d.\n", getpid());
  printf("Starting with energy: %d.\n", cur_energy);
  fflush(stdout);

  signal(SIGALRM, output);
  signal(SIGUSR1, switch_char);
  signal(SIGUSR2, replicate);
  signal(SIGTERM, inc_energy);
  signal(SIGCHLD, zombie);

  alarm(1);
  
  /* Wait until we are leet :) */
  while (1337)
  {
    sleep(1337);
  }

  /* Not reached */
  return 0;
}
