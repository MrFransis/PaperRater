# PaperRater
Final project for the course in 
_Large Scale and Multi-Structured Databases_ @University of Pisa.

## Objective 
The goal of the project is to design and implement a complete application which manages a “big dataset”
stored in a distributed data store, built considering at least two NoSQL architectures. Checkout the [assignment](assignment.pdf) 
for more information about the project goal.

## Overview
PaperRater is a Java application which allows users to search, rate and comment
scientific papers retrieved from different sources. The application has two main
purposes:
- Merge in a unique database, papers published in various open access archives
providing fast and efficient ways to search papers.
- Realizing a social network, allowing users to interact and express their opinions
about papers. Users can also create Reading Lists in which they can save
papers.

The application is composed by two main programs:
- PaperRaterApp, the application that is thought to be publicly distributed to real
users.
- DB_Updater, a command line application that has the task of initializing and
keeping updated the database of PaperRaterApp.

Refer to the [documentation](documentation.pdf) for the full project description.

## Authors
- Francesco Hudema [@MrFransis](https://github.com/mrfransis)
- Tommaso Baldi [@balditommaso](https://github.com/balditommaso)
- Edoardo Ruffoli [@edoardoruffoli](https://github.com/edoardoruffoli)
