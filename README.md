# FMcalc-as-a-java-subprocess

It provides standard input for fmcalc and reads it's standard output.


## Prerequisites

a change must be made to ktools as follows: 

In ktools/src/fmcalc/main.cpp the lines 

```
setvbuf(stdout, NULL, _IONBF, 0);
setvbuf(stdin, NULL, _IONBF, 0);
```
must be added after line 193. *(in bewteen initstreams("","") and doit(new_max))*

Ktools must then be installed as usual.

## Usage

