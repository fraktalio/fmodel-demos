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

Axon Framework is constrained on the application layer only, enabling location transparency and message routing
capabilities out of the box.
