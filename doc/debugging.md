## How to debug the agent?

### Prepare Agent debugging

Create `Run/Debug Configurations` > `Remote JVM Debug`</br>
For the Configurations, just use the default values.

> - Host: `localhost`
> - port: `5005`
> - JVM option: `-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005`

### Agent debugging

1. Run `run-agent.sh`.
2. Run the 'Remote JVM Debug' that created.
3. Start debugging.
