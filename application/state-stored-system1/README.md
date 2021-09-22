## State stored system

State stored aggregate is using a `Decider` from [domain module](../../domain) to handle commands and produce new state.
In order to handle the command, aggregate needs to fetch the current state via `fetchState` suspending function first.
All you have to do is to implement this simple method.

New state will be stored via `save()` **suspending function**. All you have to do, is to implement this simple method.

These two functions are producing side effects (persistence), and they are deliberately separated from the decider (
pure `domain` logic).

![aggregate_ss image](../../.assets/ss-aggregate.png)
