---
title: Introduction 
tags: [getting-started]
keywords: start, introduction, begin, install, build, hello world,
last_updated: August 12, 2015
summary: "Since 1.2.7 release, wro4j allows defining the groups using wildcards. This can drastically simplify the wro.xml configuration. "
---

The following characters are considered wildcards: ```*``` and ```?```. Deep recursion (searching in subdirectories) can be achieved using ```**``` character.
  You can use wildcards in almost any type of resources: classpath, file, ftp, servlet context. When no resources are found using the supplied uri with contained wildcard - you'll be warned about that in logs.

Below are several examples:

* ```/static/*```  - all files with any extension inside static folder only (doesn't look in subfolders)
* ```/static/*.js``` - all files with js extension inside static folder only 
* ```/static/**.js``` - all files with js extension inside static folder and all subfolders (recursive)
* ```/static/*.cs?``` - all files with extensions like (css, csv, csx, etc) inside static folder only (not recursive)
* ```/static/test.??``` - all files with two letters extensions (ex: js,as,...) from static folder only (not recursive)
* ```classpath:com/resources/*.css```  - all files with css extension inside com.resources package of the classpath
* ```file:c:/temp/**```  - all files with any extension inside the temp folder on drive c (windows OS)

