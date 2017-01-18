bow-utils
=========

Some utility classes that are used in the count-db project and other bow-* projects

## Important classes:

- be.bagofwords.application.ApplicationManager: class to wire and run your application with spring without needing an xml
- be.bagofwords.memory.MemoryManager: class that notifies listeners (i.e. MemoryGobblers) when the JVM is running low on memory
- be.bagofwords.cache.CachesManager: efficient cache that uses primitive maps (from the fastutil project) whenever possible. Works with the memory Manager to flush values when the memory becomes full.
- be.bagofwords.counts.BinComputer: utlity class to create a histogram 

## Usage

```
git clone https://github.com/koendeschacht/bow-utils.git 
cd bow-utils
mvn clean install
```

## License

```
The MIT License (MIT)
Copyright (c) 2014 Koen Deschacht
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
