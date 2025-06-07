# CSI3131-Labs
## Lab 1
Make sure you have essential build tools installed  
Run **make** to create the binaries

## Lab 2

### Recompiling binaries
Run **make** from inside the lab2 directory

### Executing programs
To execute mon2.c with calcloop being monitored, run
```bash
bin/mon2.c calcloop
```
Same thing with cploop

```bash
bin/mon2.c cploop
```

### Compiling MandelBrot
To compile the MandelBrot java program, run
```bash
javac MandelBrot.java
```
from the MandelBrot directory.

To clean the .class files (for recompiling), run
```bash
rm *.class
```

If you make changes to the sourcefiles (i.e.) MBCanvas.java, you will need to recompile the specific .class file.
You can also clean the .class files and recompile the whole thing from source.

### Running MandelBrot

To run the program (default arguments), run
```bash
java MandelBrot -2 2 4 600 600 
```
