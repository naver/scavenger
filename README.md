# Scavenger

As the system ages, more and more unused code, aka "Dead Code", will pile up due to requirement and spec changes.

Scavenger was started from [codekvast](https://github.com/crispab/codekvast).
Codekvast is the first tool which enables runtime dead code analysis in Java.
Scavenger provides more sophisticated and clear UI and elaborate the instrumentation logic by re-writing most of codes.

### Components

* Scavenger Agent
    * Collect the code base and regularly send the invocations of the host application to collectors.
* Scavenger Collector
    * Store the data received from the agent in the database.
    * Clean up garbages.
* Scavenger API
    * Provide APs for exploring invocations.
* Scavenger Frontend
    * Provides UI.
* [Scavenger Python Agent (BETA)](https://github.com/naver/scavenger/blob/develop/scavenger-agent-python)
    * Python agent of Scavenger Agent described above.

# Features

* Analyze runtime dead code with no code changes.
* Support JVM based languages.
    * Agent for Java 1.7 is officially not supported any more.
        * However, if you cannot avoid, please use the old agent.
* Provide a web-based interface for project management, invocation snapshot, and project dashboard.
* Supports MySQL, Vitess, and H2 as databases.

# Download

You can download the latest Scavenger in the following link.

* Collector, API
    * https://github.com/naver/scavenger/releases
* Agent for Java
    * Download the latest version from
      https://repo1.maven.org/maven2/com/navercorp/scavenger/scavenger-agent-java/{VERSION}/scavenger-agent-java-{VERSION}.jar
        * You can find out what latest VERSION is
          in [maven central](https://search.maven.org/search?q=g:com.navercorp.scavenger%20AND%20a:scavenger-agent-java)
* Old agent for Java (support java 1.7 but not maintained any more)
    * Download the latest version from https://repo1.maven.org/maven2/com/navercorp/scavenger/scavenger-old-agent-java/{VERSION}/scavenger-old-agent-java-{VERSION}.jar
      *  You can find out what latest VERSION is in [maven central](https://search.maven.org/search?q=g:com.navercorp.scavenger%20AND%20a:scavenger-old-agent-java)

# Documentation

You can find the installation guide at the following link.

- https://github.com/naver/scavenger/blob/develop/doc/installation.md

You can find the user guide at the following location link.

- https://github.com/naver/scavenger/blob/develop/doc/user-guide.md

# Contribution?

Scavenger welcomes any contributions from users.<br/>
Pull requests are the best way to propose changes to the codebase. We actively welcome your pull requests:

1. Fork the repo and create your branch from master.
2. If you've added code that should be tested, add tests.
3. Ensure the test suite passes.
4. Issue that pull request!

# Versioning

For transparency and insight into our release cycle, and to strive to maintain backward compatibility, Scavenger will be
maintained under the Semantic Versioning guidelines to the greatest extent possible.

Releases will be numbered in the following format:

      `<major>.<minor>.<patch>`

Release will be constructed based on the following guidelines:

* Breaking backward compatibility bumps the major (and resets the minor and patch)
* New additions without breaking backward compatibility bump the minor (and reset the patch)
* Bug fixes and small enhancement. changes bump the patch

# Q/A and Bug tracker

Found the apparent bug? Got a brilliant idea for an enhancement? Please create an issue here on GitHub so you can notify
us!

* https://github.com/naver/scavenger/issues

# License

```
Copyright 2023-present NAVER Corp.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
