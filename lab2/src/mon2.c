/*--------------------------------------------------------------------------
File: mon2.c

Description: This program creates a process to run the program identified
             on the commande line.  It will then start procmon in another
	     process to monitor the change in the state of the first process.
	     After 20 seconds, signals are sent to the processes to terminate
	     them.

	     Also a third process is created to run the program filter.  
	     A pipe is created between the procmon process and the filter
	     process so that the standard output from procmon is sent to
	     the standard input of the filter process.
--------------------------------------------------------------------------*/
#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <signal.h>
#include <unistd.h>
#include <limits.h>


/* It's all done in main */
int main(int argc, char **argv)
{
    char    *program;

    if (argc != 2) {
        printf("Usage: mon2 fileName\n where fileName is an executable file.\n");
        exit(-1);
    } else
        program = argv[1];  /* This is the program to run and monitor */


    char path[PATH_MAX];
    ssize_t len = readlink("/proc/self/exe", path, sizeof(path) - 1);
    if(len == -1)
    {
        perror("readlink");
        exit(EXIT_FAILURE);
    }
    path[len] = '\0';

    
    char* last_slash = strrchr(path, '/');
    if(last_slash != NULL)
    {
        *(last_slash+1) = '\0';
    }


    char program_path[PATH_MAX];
    char procmon_path[PATH_MAX];
    char filter_path[PATH_MAX];

    snprintf(program_path, sizeof(program_path), "%s%s", path, program);
    snprintf(procmon_path, sizeof(procmon_path), "%s%s", path, "procmon");
    snprintf(filter_path, sizeof(filter_path), "%s%s", path, "filter");


    /* First Step: Create the first process to run the program from the command line */

    pid_t program_pid = fork();

    // If process fails to fork
    if (program_pid < 0)
    {
        perror("Failed to fork process - program.");
        return 1;
    }
    // Child Branch
    if (program_pid == 0)
    {
        execlp(program_path, program, NULL);
        perror("Failed to execute process - program.");
        return 1;
    }

    /* Second step: Create the pipe to be used for connecting procmon to filter */

    int pipefd[2];

    if(pipe(pipefd) == -1)
    {
        perror("Failed to create pipe.");
        return 1;
    }

    /* Third step: Lets create the filter process - don't forget to connect to the pipe */

    pid_t filter_pid;
    filter_pid = fork();

    if(filter_pid < 0)
    {
        perror("Failed to fork process - filter");
        return 1;
    }
    if(filter_pid == 0)
    {
        // Child: filter
        close(pipefd[1]);                 // Close unused write end
        dup2(pipefd[0], STDIN_FILENO);    // Redirect stdin to read from pipe
        close(pipefd[0]);                 // Close original read end
        
        execlp(filter_path, "filter", NULL);
        perror("Failed to execute filter program");
        return 1;
    }

    /* Fourth step: Lets create the procmon process - don't forget to connect to the pipe */

    pid_t procmon_pid;
    procmon_pid = fork();

    if(procmon_pid < 0)
    {
        perror("Failed to fork process - procmon");
        return 1;
    }

    char pid_arg[20];
    sprintf(pid_arg, "%d", program_pid);

    if(procmon_pid == 0)
    {
         // Child: procmon
        close(pipefd[0]);                 // Close unused read end
        dup2(pipefd[1], STDOUT_FILENO);   // Redirect stdout to write to pipe
        close(pipefd[1]);                 // Close original write end
        
        execlp(procmon_path, "procmon", pid_arg, NULL);
        perror("Failed to execute process - procmon");
    }


    /* Fifth step: Let things run for 20 seconds */
    sleep(20);

    /* Last step: 
       1. Kill the process running the program
       2. Sleep for 2 seconds 
       3. Kill the procmon and filter processes
    */
    kill(program_pid, SIGKILL);
    waitpid(program_pid, NULL, 0);

    sleep(2);

    kill(procmon_pid, SIGKILL);
    waitpid(procmon_pid, NULL, 0);
    
    kill(filter_pid, SIGKILL);
    waitpid(filter_pid, NULL, 0);


    return 0;  /* All done */
}



