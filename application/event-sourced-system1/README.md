## Event sourced system

Event sourcing aggregate is using a `Decider` from [domain module](../../domain) to handle commands and produce new
events. In order to handle the command, aggregate needs to fetch the current state via `EventRepository.fetchEvents`
suspending function first. All you have to do is to implement this simple method.

New events will be stored via `EventRepository.save` **suspending function**. All you have to do, is to implement this
simple method.

These two functions are producing side effects (persistence), and they are deliberately separated from the decider (
pure `domain` logic).

![aggregate_es image](../../.assets/es-aggregate.png)

In this example we are using [Axon Framework](https://axoniq.io/product-overview/axon-framework) as a programming model
and [Axon Server](https://axoniq.io/product-overview/axon-server) as a message broker and event store database.

Axon Framework is constrained on the adapter/infra layer only, enabling location transparency and message routing
capabilities out of the box.

## Running the application locally

**Requirements**

> You can [download](https://download.axoniq.io/axonserver/AxonServer.zip) a ZIP file with AxonServer as a standalone
> JAR. This will also give you the AxonServer CLI and information on how to run and configure the server.
>
> Alternatively, you can run the following command to start AxonServer in a Docker container:
>
> ```
> docker run -d --name axonserver -p 8024:8024 -p 8124:8124 axoniq/axonserver
> ```

```shell script
mvn clean install
cd application/event-sourced-system1/
mvn spring-boot:run

```

## References and further reading

- [https://fraktalio.com/fmodel/](https://fraktalio.com/fmodel/)

---
Created with :heart: by [Fraktalio](https://fraktalio.com/)

