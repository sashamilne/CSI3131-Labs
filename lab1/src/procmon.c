#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#ifdef __APPLE__
#include <libproc.h>
#include <sys/resource.h>
#endif
#define BUFFSIZE 50

typedef struct dStruct {
    char        *state;
    long int    sysTime;
    long int    userTime;
} DataStruct;

/* This function reads the process state, its used system time and used user time
    from the specified statFile and fills the specified data structure. If the file
    cannot be read (i.e. the monitored process no longer exists, reurns NULL pointer,
    otherwise returns pointer to the procided data structure. */
DataStruct *getData(char *statFile, DataStruct *data) {
    FILE *fp;
    char state;

    fp = fopen(statFile, "r");
    if (!fp) {
        printf("procmon: Cannot open %s, the monitored process is not running any more.\n", statFile);
        return NULL;
    }
    fscanf(fp, "%*s %*s %c %*s %*s %*s %*s %*s %*s %*s %*s %*s %*s %ld %ld",
        &state, &(data->userTime), &(data->sysTime));

    switch(state) {
        case 'R': data->state = "Running          "; break;
        case 'S': data->state = "Sleeping(memory) "; break;
        case 'D': data->state = "Sleeping(disk)   "; break;
        case 'Z': data->state = "Zombie           "; break;
        case 'T': data->state = "Traced/Stopped   "; break;
        case 'W': data->state = "Paging           "; break;
    }
    fclose(fp);
    return data;
}

/*--------------------------------------------------------------------------
Function: main
Description: The main function processes command line arguments and invokes
             appropriate functions to read in data from kernel and write
             to the standard output the information collected. This should
             be a relatively simple function.
--------------------------------------------------------------------------*/
int main(int argc, char **argv)
{
    DataStruct  data;
    char        statFile[BUFFSIZE];
    int         t;

    if (argc != 2) {
        printf("Usage: procmon PID\n where PID is a process ID of a running process.\n");
        exit(-1);
    }

#ifdef __APPLE__
    for (int t = 0; ; t++) {
        int pid = atoi(argv[1]);

        struct rusage_info_v2 ri;

        if (proc_pid_rusage(pid, RUSAGE_INFO_V2, (rusage_info_t *)&ri) == 0) {
            data.userTime = ri.ri_user_time;
            data.sysTime = ri.ri_system_time;
        } else {
            perror("proc_pid_rusage failed");
            exit(-1);
        }

        struct proc_taskallinfo tai;

        const int ret = proc_pidinfo(pid, PROC_PIDTASKALLINFO, 0, &tai, sizeof(tai));
        if (ret <= 0) {
            perror("proc_pidinfo failed");
            return 1;
        }

        struct proc_bsdinfo *bsd = &tai.pbsd;

        int pbi_status = bsd->pbi_status;

        switch(pbi_status) {
            case 2: data.state = "Running          "; break;
            case 1: data.state = "Sleeping         "; break;
            case 4: data.state = "Zombie           "; break;
            case 3: data.state = "Traced/Stopped   "; break;
        }
        printf("%3d     %s %6ld %8ld\n", t, data.state, data.sysTime, data.userTime);
        sleep(1);
    }

#else
    sprintf(statFile, "/proc/%s/stat", argv[1]);

    printf("\n\n        Monitoring %s:\n\n", statFile);
    printf("Time        State           SysTm    UsrTm\n");
    for(t=0; ;t++) {
        if (getData(statFile, &data) == NULL)
            exit(0);
        else
            printf("%3d     %s %6ld %8ld\n", t, data.state, data.sysTime, data.userTime);
        sleep(1);
    }
#endif
}
