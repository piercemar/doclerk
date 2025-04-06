# Doclerk document generator

Doclerk was created with the idea that most delivery-time documentation in a software project contains a lot of manually gathered information,
collected in a very error-prone way, that is readily available in the projects' source code.

The aim of this tool is to initiate such documentation/deliverables using the code and SCM managed data as it's main source of truth.

## Example output

Quickly achievable outputs may be:

* Test plans
* Delivery check-list or manifest
* Deployment plans / guides

but ultimately as data are gathered and better structured, Doclerk could produce:

* Deployment *recipes* (targeted at CI/CD pipelines or other deployment tools like Ansible/Chef/Puppet/XLDeploy)
* Interactive test plans
* Automated test plans (e.g. Selenium)
* Technical / User documentation

## How to run it

The current status allows running from command-line only.

### Command Line

Run this command *all on one line* (line breaks are there for clarity)

```
java -classpath 
  <path_to>\doclerk-cli-0.1.0.jar;
  <path_to>\mod-in-testplan-0.1.0.jar
 io.github.doclerk.cli.Doclerk
 --input-file "<path_to_exec_plan>"
 --work-dir "<path_to_work_dir>"
```

### Maven

The best you can get at the moment is by using *exec-maven-plugin*.

See doclerk-samples -> **cli-with-testplan** for an example on how to setup.
