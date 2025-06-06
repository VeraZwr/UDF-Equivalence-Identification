# UDF Equivalence Identification

This project was developed as part of the BDSPRO Course in the Sumer Semester 2024 at DIMA from TU Berlin.
The participating students were Jakob Gerhardt, Jennifer Fomin, Sohaila Efelky and Weiran Zhao.
The Project Supervisor was Ankit Chaudhary.


# Documentation
The Report is uploaded here: [Report.pdf](./Report.pdf)
The Java Classes are documented with Javadoc.

# Usage

This project contains multiple maven based java projects. 
As these depend on one another, one must install the dependencies first:
```shell
cd ./udfIE; 
mvn clean;
cd ../testcases; 
mvn install;
cd ../udfIE; 
mvn compile;


```


## Codemap

- `./Timetable` - Planning Document
- `./testcases` - A collection of UDFs that *should* be equivalent. Each subfolder is numbered and contains a few files with UDFs that all should be evaluated as equivalent.
- `./Previous_Work` - work from a previous group on this (in gitignore, so not commited.)
