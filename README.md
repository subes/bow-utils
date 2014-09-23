bow-utils
=========

Some utility classes that are used in the count-db project and other bow-* projects

## Most relevant classes:

- be.bagofwords.application.ApplicationManager: class to wire and run your application with spring without needing an xml
- be.bagofwords.memory.MemoryManager: class that notifies listeners (i.e. MemoryGobblers) when the JVM is running low on memory
- be.bagofwords.cache.CachesManager: efficient cache that uses primitive maps (from the fastutil project) whenever possible. Works with the memory Manager to flush values when the memory becomes full.
- be.bagofwords.counts.BinComputer: utlity class to create a histogram 

## Usage

git clone https://github.com/koendeschacht/bow-utils.git 
cd bow-utils
mvn clean install
