# Scavenger Agent Python (BETA)

*(This Agent is a beta version and is recommended for use only in development environment, not for production environments)* This Agent is a Python
version of Scavenger Agent, and [Server Component](../doc/installation.md) must be prepared in advance.

## Development Guide

Scavenger Agent Python uses [Poetry](https://python-poetry.org/) as a package manager.

- Install dependencies

```sh
$ poetry install
```

- Test

```sh
$ poetry run python -m unittest
```

- Build

```sh
$ poetry build
```

For more information, please refer to [Poetry Docs](https://python-poetry.org/docs/).

## Installation Guide

### Prerequisite

`Python >= 3.7`

### Installation

```
$ pip install scavenger-agent-python --save
```

### Configuration

Configuration supports two methods: using the configuration file `scavenger.conf` or directly creating a `Config` instance.

* Example of `scavenger.conf`

```py
# scavenger.conf
apiKey=eb99ff0f-aaaa-bbbb-cccc-5d1ec81f6183
serverUrl=http://10.106.93.41:8081
environment=dev
appName=apiserver
packages=views
codebase=.
---
# source
config = Config.load_config()
agent = Agent(config)
```

* Example of `Config()`

```py
config = Config(
    api_key="eb99ff0f-aaaa-bbbb-cccc-5d1ec81f6183",
    server_url="http://10.106.93.41:8081",
    environment="dev",
    app_name="apiserver",
    packages=["views"],
    codebase=["."],
)
agent = Agent(config)
```

### Agent Start

Unlike Scavenger Agent Java, Scavenger Agent Python works by directly inserting the agent start code. *Note that the agent start code must be inserted
at the start point of the program*, because the agent must run before the module of the function to be collected is imported.

```py
from scavenger import Agent, Config

config = Config.load_config()
agent = Agent(config)
agent.start()

### your source code
from ...
```

### Limitations

* The implementation of Graceful Shutdown is not automatically supported due to potential conflicts with existing frameworks. Instead,
  the `agent.shutdown()` function as shown below is provided, so you must register it into your framework shutdown routine by yourself.

```py
def shutdown_gracefully(*args):
    # Shutdown your application/server first.
    agent.shutdown()
    sys.exit(0)

signal.signal(signal.SIGTERM, shutdown_gracefully)
agent.start()
```

* Scavenger Python Agent works by replacing the reference of the current module/class, so it does not work by calling a function without using a
  reference. Therefore, if only the function reference is saved in the framework using Decorator as shown below, `def hello_world()` will not work.
  This will be changed and supported in the future by inserting code within the function.

```py
from flask import Flask
app = Flask(__name__)

@app.route('/')
def hello_world():
    return 'Hello World!'
```

* The direct Instrumentation for pyc files are currently not supported because Python code must be read by the scavenger agent.
