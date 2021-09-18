User guide
---
## *Using*
You can run program with command <br>
```#batch 
diff [OPTIONS] FILES
```
For example ```diff -two-columns -color src/files/text1.txt src/files/text2.txt``` <br>
All options should start with '-' and exactly two names of file otherwise diff fall with error<br>

---

## Description

diff - compares ```FILES``` line by line

### Options:
+ ```-brief``` report identical or different files are
+ ```-ignore-case``` ignore case differences in file contents
+ ```-two-columns``` output in two columns
+ ```-unified[=NUM]``` output NUM (default 3) lines of unified context
+ ```-color``` output diff result with color (may be )
+ ```-common-lines``` output common lines of files
---
## Output

The standard output for diff command:

```
[segment of old file][option][segment of new file]
> added line
--- 
< deleted line
```
where ```option``` may be "a", "d" or "c" for adding, deleting and changing operation respectively


The standard output for unified diff command:
```
@@ -l,s +r,s @@
 common line
 common line
 common line
-deleted line
+added line
 common line
 common line
```

where each range is of the format ```l,s``` where l is the starting line number and s is the number of lines the change hunk applies to for each respective file
