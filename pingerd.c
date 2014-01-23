/* pinger.c */

#include <stdio.h>
#include <string.h>
#include <stdarg.h>
#include <sys/file.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/stat.h> 
#include <unistd.h>
#include <stdlib.h>
#include <time.h>
#include <signal.h>

#define PING "/sbin/ping"

int num_children, sleep_time;
char* name;
char pipe_name[512];
FILE *pipe_fd;
int lockfp;

char* get_line(char *pattern);
void put_line(char *pattern, char *newline);
void do_term();
void configure();
void do_log(pid_t pid, time_t timestamp, char *fmt, ...);

void zombie()
{
  wait(NULL); 
  num_children--;
}

void wait_term()
{
  int n = 0; 
  const int MAX = 500;
  while (num_children > 0 && n++ < MAX)
  {
    do_log(getpid(), time(NULL), "Still waiting for %d children", num_children);
    usleep(500);
  }

  return;
}

void do_log(pid_t pid, time_t timestamp, char *fmt, ...) 
{
  FILE *fp;
  va_list ap;
  char timebuf[32];
  char buf[512], buf2[512];

  va_start(ap, fmt);
  ctime_r(&timestamp, timebuf);
  timebuf[24] = '\0';
  
  sprintf(buf, "%.256s.log", name);
  fp = fopen(buf, "a");
  if (!fp)
  {
    perror("fopen");
  } 

  vsprintf(buf, fmt, ap);
  sprintf(buf2, "[%s] [%d] %s\n", timebuf, pid, buf);

  while (1)
  {
    if (flock(fileno(fp), LOCK_EX) == -1)
    {
      usleep(200);
      continue;
    }    
   
    fputs(buf2, fp); 
    break;
  }
 
  fclose(fp);
}

int pinger(char *host)
{
  pid_t childpid;
  char buf[512];
  int num, fd[2];

  if (pipe(fd))
  {
    perror("pipe");
    return 0;
  }

  if ((childpid = fork()) == -1) 
  {
    perror("fork");
    return 0;
  }

  if (childpid == 0)
  {
    if (dup2(fd[1], fileno(stdout)) == -1)
    {
      perror("dup2");
    }
    if (dup2(fd[1], fileno(stderr)) == -1)
    {
      perror("dup2");
    }

    if (execlp(PING, "ping", "-c", "1", host, NULL))
    {
      perror("execlp");
    } 
  }
  else
  {
    wait(NULL);
    num = read(fd[0], buf, sizeof(buf));
    if (num > 0 && strstr(buf, "1 packets received"))
    {
      return 1;
    }
    else 
    {
      return 0;
    }
  }
   
  return 0; 
}

void terminate()
{
  do_log(getpid(), time(NULL), "Terminating because of SIGTERM");
  do_term();
}

void do_term()
{
  char buf[512];
  signal(SIGTERM, SIG_IGN);
  kill(0, SIGTERM);
  wait_term();
  close(lockfp);

  sprintf(buf, "%.256s.pid", name);
  unlink(buf);
  
  sprintf(buf, "%.256s.lock", name);
  unlink(buf);

  exit(0);
}

void reload()
{
  signal(SIGTERM, SIG_IGN);
  do_log(getpid(), time(NULL), "Reloading because of SIGHUP");
  kill(0, SIGTERM);
  wait_term();
  configure();
  signal(SIGTERM, terminate);
}

void term_pinger()
{
  do_log(getppid(), time(NULL), "Pinger stopped for %s (time %d)", pipe_name, sleep_time);
  exit(0);
}

void invoke_pinger(char *host, int num) 
{
  pid_t childpid;
  int ret, succ, fail;
  char *s;
  char buf[512], buf2[512];

  if ((childpid = fork()) == -1)
  {
    perror("fork");
    return;
  }

  if (childpid == 0)
  {
    do_log(getppid(), time(NULL), "Pinger started for %s (time %d)", host, num);

    strcpy(pipe_name, host);
    sleep_time = num;

    num_children++;

    signal(SIGTERM, term_pinger);
    signal(SIGHUP, SIG_DFL);

    while (1)
    {
      s = get_line(host);
      ret = pinger(host);
      if (s != NULL && sscanf(s, "%s %d %d", buf, &succ, &fail) == 3)
      {
        /* read from file */
      }
      else 
      {
        strcpy(buf, host);
        succ = 0;
        fail = 0;
      }

      if (ret)
      {
        do_log(getppid(), time(NULL), "Pinging %s successful", host);
        succ++;
      }
      else
      {
        do_log(getppid(), time(NULL), "Pinging %s unsuccessful", host);
        fail++;
      }

      sprintf(buf2, "%s %d %d\n", buf, succ, fail);

      put_line(host, buf2);     

      sleep(num);
    }
  }
}

void configure()
{
  char buf[512];
  FILE *fp;
  static int mytime;
  static char mybuf[512];

  do_log(getpid(), time(NULL), "Configure started");
  sprintf(buf, "%.256s.conf", name);
  fp = fopen(buf, "r");

  if (!fp)
  {
    perror("fopen");
    exit(1);
  }

  while (fgets(buf, sizeof(buf), fp) != NULL) 
  {
    if (sscanf(buf, "param Sleep=%d", &mytime) == 1)
    {
      do_log(getpid(), time(NULL), "Sleep set to %d", mytime);
      sleep_time = mytime;  
    }
    else if (sscanf(buf, "param Pipe=%s", mybuf) == 1)
    {
      do_log(getpid(), time(NULL), "Pipe set to %s", mybuf);

      if (strlen(pipe_name) <= 0 || strcmp(pipe_name, mybuf))
      {
        fclose(pipe_fd);
        unlink(pipe_name);
        mkfifo(mybuf, S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH);
        pipe_fd = fopen(pipe_name, "r");
        strcpy(pipe_name, mybuf);
      }  
    }
    else if (sscanf(buf, "pinger %s %d", mybuf, &mytime) == 2)
    {
      do_log(getpid(), time(NULL), "Pinger configured for %s %d", mybuf, mytime);
      invoke_pinger(mybuf, mytime);
    }
    else
    {
      if (strlen(buf) > 0)
      {
        buf[strlen(buf) - 1] = '\0';
        do_log(getpid(), time(NULL), "Ignored config line %s", buf);
      }
    }
  }
  
  do_log(getpid(), time(NULL), "Reading config file completed");
}

void reply(char *pipe, char *msg)
{
  FILE *fp;

  fp = fopen(pipe, "w");
  if (!fp)
  { 
    perror("fopen");
    return;
  }

  fputs(msg, fp);
  fclose(fp);
}

char *get_line(char *pattern)
{
  FILE *fp, *lockfp;
  char buf[512];
  static char buf2[512];
  
  sprintf(buf, "%.256s.lock", name);
  lockfp = fopen(buf, "w+");
  if (!lockfp)
  {
    return NULL;
  }

  buf2[0] = '\0';

  while (1)
  {
    if (flock(fileno(lockfp), LOCK_SH) == -1)
    {
      usleep(200);
      continue;
    }
  
    sprintf(buf, "%.256s.stat", name);
    fp = fopen(buf, "r"); 
    
    if (fp == NULL)
    {
      break;
    }

    while (fgets(buf, sizeof(buf), fp) != NULL)
    {
      if (strstr(buf, pattern))
      {
        strcpy(buf2, buf);
        break;
      }
    }
 
    fclose(fp);

    break;
  } 

  fclose(lockfp); 

  if (strlen(buf2) > 0)
  {
    return buf2;
  }
  else
  {
    return NULL;
  }
}

void put_line(char *pattern, char *newline)
{
  FILE *oldfp, *lockfp;
  int newfp;
  int found;
  char buf[512];
  static char buf2[512];
  char template[] = "/tmp/fileXXXXXXXXXXXXXXXXX";
  
  sprintf(buf, "%.256s.lock", name);
  lockfp = fopen(buf, "w+");
  if (!lockfp)
  {
    perror("fopen");
    exit(1);
  }
 
  while (1)
  {
    if (flock(fileno(lockfp), LOCK_EX) == -1)
    {
      usleep(200);
      continue;
    }
  
    /*** EXCLUSIVE LOCK ***/

    sprintf(buf, "%.256s.stat", name);
    oldfp = fopen(buf, "r"); 
    
    newfp = mkstemp(template);
 
    if (oldfp == NULL)
    {
      write(newfp, newline, strlen(newline));
    }
    else 
    {    
      found = 0;
      while (fgets(buf2, sizeof(buf2), oldfp) != NULL)
      {
        if (strstr(buf2, pattern))
        {
          write(newfp, newline, strlen(newline));
          found = 1;
        }
        else
        {
          write(newfp, buf2, strlen(buf2)); 
        }
      }

      if (!found)
      {
          write(newfp, newline, strlen(newline));
      }

      fclose(oldfp);
    }

    rename(template, buf);
    close(newfp);

    break;
  } 

  fclose(lockfp); 

  /*** EXCLUSIVE LOCK ***/
}


void daemonize()
{
  char buf[512], buf2[512], buf3[512];
  char *line;

  sprintf(buf, "%.256s.pid", name);

  /* flock is evil in respect to NFS */
  lockfp = open(buf, O_CREAT | O_EXCL | O_WRONLY | S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH);
  if (lockfp == -1)
  {
    fprintf(stderr, "Another instance is already running\n");
    exit(1); 
  } 

  sprintf(buf, "%d\n", getpid()); 
  if (!write(lockfp, buf, strlen(buf)))
  {
    perror("write");
    exit(1);
  }

  fprintf(stdout, "Daemonized as PID %d\n", getpid());
  fflush(stdout);
  do_log(getpid(), time(NULL), "Initializing...");

  signal(SIGCHLD, zombie);
  signal(SIGTERM, terminate);
  signal(SIGHUP, reload);
  signal(SIGBUS, do_term);
  signal(SIGSEGV, do_term);

  configure();

  while (1)
  {
    if (pipe_fd == NULL)
    {
      sleep(sleep_time);
      continue;
    }

    if (fgets(buf, sizeof(buf), pipe_fd) != NULL)
    {
      if (sscanf(buf, "Hi %s", buf2) == 1)
      {
        do_log(getpid(), time(NULL), "Replying to Hi on %s", buf2);
        reply(buf2, "Hi\n");
      }
      else if (sscanf(buf, "Term %s", buf2) == 1)
      {
        reply(buf2, "Terminating\n");
        do_log(getpid(), time(NULL), "Terminating due to request and replying on %s", buf2);
        do_term();
      }
      else if (sscanf(buf, "Get %s %s", buf2, buf3) == 2)
      {
        do_log(getpid(), time(NULL), "Replying to Get on %s", buf2);
        line = get_line(buf3);
        if (line)
        {
          reply(buf2, line);
        }
        else
        {
          sprintf(buf, "%s 0 0", buf3);
          reply(buf2, buf);
        }
      }
    } 
    sleep(sleep_time);
  }

  do_term();
}

int main(int argc, char *argv[])
{
  pid_t childpid;

  name = argv[0];
  pipe_name[0] = '\0';
  sleep_time = 10;
   
  if ((childpid = fork()) == -1)
  {
    perror("fork");
    return 0;
  }

  if (childpid == 0)
  {
    daemonize();
  }
  else
  {
    sleep(1);
  }
  
  return 0;
}
