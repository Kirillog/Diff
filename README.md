User guide
---
## *Using*
For Windows:
---
You can run program with command <br>
```#batch 
diff [OPTIONS] FILES
```
For example ```diff -two-columns -color src/files/wikiFile1.txt src/files/wikiFile2.txt``` prints diff output with color in two columns. <br>

For Linux/MacOS:
---
```#bash 
./diff [OPTIONS] FILES
```
For example ```./diff -unified -ignore-case src/files/wikiFile1.txt src/files/wikiFile2.txt``` prints unified diff output ignoring case of symbols.<br>
All options should start with '-' and exactly two names of file otherwise diff fall with error<br>

---

## Description

diff - compares ```FILES``` line by line

### Options:
+ ```-brief``` report identical or different files are
+ ```-ignore-case``` ignore case differences in file contents
+ ```-two-columns``` output in two columns
+ ```-unified[=NUM]``` output NUM (default 3) lines of unified context
+ ```-color``` output diff result with color
+ ```-common-lines``` output common lines of files
---
## Output

The standard output for diff command:

```
[segment of old file][option][segment of new file]
< deleted line
--- 
> added line
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

Testing
---
You can find module tests in ```src/test/kotlin``` for all functions.

And there are pairs of files in ```src/files``` to check the whole program. For example,
you can try ```diff src/files/testFile1.txt src/files/testFile2.txt``` to check the whole program