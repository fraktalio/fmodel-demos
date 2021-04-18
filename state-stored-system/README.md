## State stored system

State stored aggregate is using a `Decider` to handle commands and produce new state. In order to handle the command,
aggregate needs to fetch the current state via `fetchState` suspending function first. All you have to do is to
implement this simple method.

New state should be stored via `storeState` **suspending function**. All you have to do, is to implement this simple
method.

These two functions are producing side effects (persistence), and they are deliberately separated from the decider (
pure `domain` logic). These two functions belongs to the `Aggregate` component within the `application` package.

![aggregate image](../.assets/aggregate.jpg)

### State-stored system

Obviously, this is specialized case, in which we use shared/common domain (`decider`) by focusing only on storing and reading
the current state. We are not storing events/facts. Nevertheless, events are used for evolving/constructing the current
state, and to make sure we are **focusing on behaviour rather than on the structure**.

> In the next example, we will demonstrate how to construct an event-sourcing system by using the same `decider` : `(C, Iterable<E>) -> Iterable<E>`

- [System1 - combines all Deciders under one Aggregate root by using inheritance structure of messages (messages extend common class)](state-stored-system1)
- [System2 - no Decider combining, we create and distribute Aggregate for each Decider](state-stored-system2)
- [System3 - combines all Deciders under one Aggregate root by using Either (messages do not extend common class)](state-stored-system3)
