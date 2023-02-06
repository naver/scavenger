# Scavenger

As the system ages, more and more unused code, aka "Dead Code", will pile up due to requirement and spec changes.

Scavenger was started from [codekvast](https://github.com/crispab/codekvast).
Codekvast is the first tool which enables runtime dead code analysis in Java.
Scavenger provides more sophisticated and clear UI and elaborate the instrumentation logic by re-writing most of codes.

### Components

* Scavenger agent
    * Collect the code base and regularly send the invocations of the host application to collectors.
* Scavenger Collector
    * Store the data received from the agent in the database.
    * Clean up garbages.
* Scavenger API
    * Provide APs for exploring invocations.
* Scavenger Frontend
    * Provides UI.

# Features

* Analyze runtime dead code with no code changes.
* Support JVM based languages.
    * Java 7 must use [old agent]().
* Provide a web-based interface for project management, invocation snapshot, and project dashboard.
* Supports MySQL, Vitess, and H2 as databases.

# Download

You can download the latest Scavenger in the following link.

- TBD

# Documentation

You can find the installation guide at the following link.

- TBD

You can find the user guide at the following location link.

- TBD

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