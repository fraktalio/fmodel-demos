## Application(s)

Orchestrates the execution of the logic declared in [domain](../domain)

Different application/adapter implementations are using the common/unique [domain model](../domain).
![aggregate image](../.assets/decider.png)

### State stored systems

![aggregate_ss image](../.assets/ss-aggregate.png)

- [State stored System 1](state-stored-system1) - combines all Deciders from [domain module](../domain) under one
  Aggregate root. `#SpringBoot`

### Event sourced/stored systems

![aggregate_es image](../.assets/es-aggregate.png)

- [Event-sourced system 1](event-sourced-system1) - combines all Deciders from [domain module](../domain) under one
  Aggregate root. FModel `Domain` and `Application` library are used. Axon is pushed to adapter (infra) layer. `#SpringBoot`, `#AxonFramework`, `#AxonServer`
- [Event-sourced system 2](event-sourced-system2) - Deciders from [domain module](../domain) are distributed via unique
  Aggregates. Saga is used to communicate Aggregates. FModel `Domain` and `Application` library are used. Axon is pushed to adapter (infra) layer. `#SpringBoot`, `#AxonFramework`, `#AxonServer`
- [Event-sourced system 3](event-sourced-system3) - Deciders from [domain module](../domain) are distributed via unique
  Aggregates. Saga is used to communicate Aggregates. Only FModel `Domain` library is used. Application layer aggregates and other components are fully supported by Axon in this case.  `#SpringBoot`, `#AxonFramework`, `#AxonServer`

