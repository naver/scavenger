# Scavenger Agent Python (BETA)

*(본 Agent는 베타 버전으로 현재 개발 환경에서만의 사용을 권장하며 상용 환경에서의 사용을 권장하지 않습니다.)* Scavenger Agent의 Python 버전으로, Agent가 아닌 [Server Component](../doc/installation.md)는 사전에 준비되어야 합니다.

## 개발 가이드

Scavenger Agent Python은 패키지 관리자로 [Poetry](https://python-poetry.org/)를 이용합니다.

- 의존성 설치
```sh
$ poetry install
```
- 테스트
```sh
$ poetry run python -m unittest
```
- 빌드
```sh
$ poetry build
```

자세한 내용은 [Poetry Docs](https://python-poetry.org/docs/)를 참고해주세요.

## 에이전트 설치 가이드

### 전제조건

`Python >= 3.7`

### 설치

```
$ pip install scavenger-agent-python --save
```
### 설정

설정은 설정 파일 `scavenger.conf`을 이용한 방식과 Config Instance를 직접 생성하는 두 가지 방식을 지원합니다.

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

### 에이전트 시작
Scavenger Agent Python은 Scavenger Agent Java와 달리, Agent 시작 코드를 직접 삽입하는 방식으로 동작합니다. 수집을 시작할 함수의 모듈이 import되기 전에 Agent가 동작해야 하므로 *반드시 프로그램의 시작점에 Agent 시작 코드가 삽입되어야 함을 유의하십시오.*
```py
from scavenger import Agent, Config

config = Config.load_config()
agent = Agent(config)
agent.start()

### your source code
from ...
```

### 한계
* Graceful Shutdown에 대한 구현은 기존 프레임워크와의 충돌 가능성으로 인해 자동으로 지원하지 않습니다. 대신 아래와 같은 `agent.shutdown()` 함수를 지원하므로 직접 등록하여서 사용하여야 합니다.
```py
def shutdown_gracefully(*args):
    # Shutdown your application/server first.
    agent.shutdown()
    sys.exit(0)

signal.signal(signal.SIGTERM, shutdown_gracefully)
agent.start()
```
* Scavenger Python Agent는 현재 모듈/클래스의 레퍼런스를 대체하는 방식으로 동작하므로 레퍼런스를 사용하지 않고 함수를 호출하는 경우에는 동작하지 않습니다. 따라서 아래와 같이 Decorator를 이용해 함수의 레퍼런스만 따로 프레임워크에 저장해두는 경우, `def hello_world()`에는 동작하지 않습니다. 이는 추후 함수내 코드를 삽입하는 방식으로 변경되어 지원될 예정입니다.
```py
from flask import Flask
app = Flask(__name__)

@app.route('/')
def hello_world():
    return 'Hello World!'
```
* 코드를 직접 읽어서 메서드를 탐색해야 하므로 pyc 파일은 현재 지원되지 않습니다.
