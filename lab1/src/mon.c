#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <limits.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <sys/wait.h>

/* the program execution starts here */
int main(const int argc, char** argv)
{
    if (argc != 2) {
        printf("Usage: mon fileName\n where fileName is an executable file.\n");
        exit(-1);
    }
    const char* program = argv[1];

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

    snprintf(program_path, sizeof(program_path), "%s%s", path, program);
    snprintf(procmon_path, sizeof(procmon_path), "%s%s", path, "procmon");

    // 1. launch the program specified by the variable 'fileName' and get its pid
    pid_t child_pid = fork();

    if (child_pid == 0) {
        execlp(program_path, program, NULL);
        perror("Failed to exec program");
        exit(-1);
    }

    if (child_pid < 0) {
        perror("Failed to fork");
        exit(-1);
    }

    // 2. launch 'procmon pid' where pid is the pid of the program launched in step 1
    char pid_str[20];
    sprintf(pid_str, "%d", child_pid);

    pid_t procmon_pid = fork();
    if (procmon_pid == 0) {
        execlp(procmon_path, "procmon", pid_str, NULL);
        perror("Failed to exec program");
        exit(-1);
    }
    if (procmon_pid < 0) {
        perror("Failed to fork");
        exit(-1);
    }

    // 3. wait for 20 seconds
    sleep(20);

    // 4. kill the first program
    kill(child_pid, SIGKILL);
    waitpid(child_pid, NULL, 0);

    // 5. wait for 2 seconds
    sleep(2);

    // 6. kill the procmon
    kill(procmon_pid, SIGKILL);
    waitpid(procmon_pid, NULL, 0);

    printf("Process Terminated");
    return 0;
}