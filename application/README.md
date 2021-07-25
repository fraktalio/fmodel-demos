## Application(s)

Orchestrates the execution of the logic declared in [domain](../domain)


Different application/adapter implementations are using the common/unique [domain model](../domain).
![aggregate image](../.assets/aggregate.jpg)


### State stored systems

![aggregate_ss image](../.assets/state-stored_aggregate.jpg)

- [State stored System 1](state-stored-system1) - combines all Deciders from [domain module](../domain) under one Aggregate root by using inheritance structure of messages (messages extend common class)

### Event sourced/stored systems

![aggregate_es image](../.assets/es_aggregate.jpg)
