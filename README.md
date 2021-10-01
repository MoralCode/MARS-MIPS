# MARS MIPS simulator

This is the source code provided with MARS (V4.5,  Aug. 2014) downloaded from https://courses.missouristate.edu/KenVollmar/mars/download.htm

This version has been modified from the original and its code is hosted at https://github.com/MoralCode/MARS-MIPS

## Changelog
Some modifications have been made from the original source. See https://github.com/MoralCode/MARS-MIPS/releases for the full changelog and binary files for each release.

## Custom Pseudo Operations

One of the major changes made to MARS in this repo has been the introduction of a mechanism for configuring a custom path to the "PseudoOps File". This file, which came with MARS as `PseudoOps.txt`, contains definitions for pseudo operations that substitute for other values. This allows you to create your own "shortcut" instructions, such as extending `addi` to support 32 bit immediate values (which is the default behavior). 

### How to create custom files
1. make a copy of `PseudoOps.txt` from this repository and read over the documentation at the top regarding formatting of the file.
2. make any changes, or additions you want. if you are stuck, maybe try creating a test instruction called `dead` by adding the following line to the file
	```
	dead $t1	addi RG1, $0, 0xdead	#Bitwise NOT (bit inversion)
	```
3. Inside MARS, go to **Settings** at the top of the page and  open the **Pseudo Operations...** menu. Click the button to change the file path and select the file that you modified in step 2.
4. Close MARS and reopen it. This causes MARS to load the instructions using your new file instead of the built-in one.
