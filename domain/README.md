# Domain modeling `#well-typed` `#pure` `#immutable` `#composable`

* [Algebraic Data Types - Commands, Events and State](#algebraic-data-types---commands-events-and-state)
    * [Commands](#commands)
        * [Sum/OR](#sumor)
        * [Product/AND](#productand)
    * [Events](#events)
        * [Sum/OR](#sumor-1)
        * [Product/AND](#productand-1)
    * [State](#state)
        * [Product/AND](#productand-2)
* [Behaviour](#behaviour)
    * [Decider](#decider)
    * [View](#view)
    * [Saga](#saga)
* [References and further reading](#references-and-further-reading)

A pure declaration of the program logic - pure computation

## Algebraic Data Types - Commands, Events and State

> In computer programming, especially functional programming and type theory, an algebraic data type is a kind of
> composite, i.e., a type formed by combining other types. Two common classes of algebraic types are `product` types (
> i.e., tuples and data classes) and `sum` types.

> `Sum` types and `product` types provide the necessary abstraction for structuring the various data of our domain
> model. Whereas `sum` types let you model the variations within a particular data type, `product` types help cluster
> related data into a larger abstraction.

> `Sum` type models an `OR` relationship and `Product` type models an `AND` relationship.

### Commands

Commands represent the intent to change the state of the system.

#### Sum/OR

The `sealed` class in Kotlin represents data composition which is also known as a `Sum` type (models the `OR`
relationship).

The key benefit of using `sealed` classes comes into play when you use them in a `when` expression. If it's possible to
verify that the statement covers all cases, **you don't need to add an `else` clause to the statement**.

We model our commands as a `Sum` type (`OR` relationship) by using `sealed` class. In this example, we have two possible
sub-classes of `RestaurantOrderCommand` which are known at compile time: `CreateRestaurantOrderCommand`
and `MarkRestaurantOrderAsPreparedCommand`.

```kotlin
sealed class Command

sealed class RestaurantOrderCommand : Command() {
    abstract val identifier: RestaurantOrderId
}

data class CreateRestaurantOrderCommand(
    override val identifier: RestaurantOrderId,
    val restaurantIdentifier: RestaurantId = RestaurantId(),
    val lineItems: ImmutableList<RestaurantOrderLineItem>
) : RestaurantOrderCommand()

data class MarkRestaurantOrderAsPreparedCommand(
    override val identifier: RestaurantOrderId
) : RestaurantOrderCommand()
```

Other programming languages are using different mechanisms to model `OR`/`Sum` relationship. For example, `TypeScript`
has `Unions`:

```typescript
type RestaurantOrderCommand = CreateRestaurantOrderCommand | MarkRestaurantOrderAsPreparedCommand;
```

#### Product/AND

If you zoom in into the concrete command types, for example `CreateRestaurantOrderCommand`, you will notice that it is
formed by combining other types: `RestaurantOrderId`, `RestaurantId`, `RestaurantOrderLineItem`.
Essentially, `CreateRestaurantOrderCommand` data class is a `Product` type which models `AND` relationship:

```
CreateRestaurantOrderCommand = RestaurantOrderId & RestaurantId & list of [RestaurantOrderLineItem]
```

### Events

Events represent the state change itself, a fact. These events represent decisions that already happened (past tense).

#### Sum/OR

We model our events as a `Sum` type (`OR` relationship) by using `sealed` class. In this example, we have four possible
sub-classes of `RestaurantOrderEvent` which are known at compile time: `RestaurantOrderCreatedEvent`
, `RestaurantOrderPreparedEvent`, `RestaurantOrderNotPreparedEvent` and `RestaurantOrderRejectedEvent`.

```kotlin
sealed class Event

sealed class RestaurantOrderEvent : Event() {
    abstract val identifier: RestaurantOrderId
}

sealed class RestaurantOrderErrorEvent : RestaurantOrderEvent() {
    abstract val reason: String
}

data class RestaurantOrderCreatedEvent(
    override val identifier: RestaurantOrderId,
    val lineItems: ImmutableList<RestaurantOrderLineItem>,
    val restaurantId: RestaurantId
) : RestaurantOrderEvent()

data class RestaurantOrderPreparedEvent(
    override val identifier: RestaurantOrderId,
) : RestaurantOrderEvent()

data class RestaurantOrderRejectedEvent(
    override val identifier: RestaurantOrderId,
    override val reason: String
) : RestaurantOrderErrorEvent()

data class RestaurantOrderNotPreparedEvent(
    override val identifier: RestaurantOrderId,
    override val reason: String
) : RestaurantOrderErrorEvent()
```

#### Product/AND

If you zoom in into the concrete event types, for example `RestaurantOrderCreatedEvent`, you will notice that it is
formed by combining other types: `RestaurantOrderId`, `RestaurantId`, `RestaurantOrderLineItem`.
Essentially, `RestaurantOrderCreatedEvent` data class is a `Product` type which models `AND` relationship:

```
RestaurantOrderCreatedEvent = RestaurantOrderId & RestaurantId & list of [RestaurantOrderLineItem]
```

### State

The current state of the information system is always evolved out of past events/facts.

```kotlin
data class RestaurantOrder(
    val id: RestaurantOrderId,
    val restaurantId: RestaurantId,
    val status: Status,
    val lineItems: ImmutableList<RestaurantOrderLineItem>
)
```

#### Product/AND

If you zoom in into the concrete state types, for example `RestaurantOrder`, you will notice that it is formed by
combining other types: `RestaurantOrderId`, `RestaurantId`, `RestaurantOrderLineItem`, `Status`.
Essentially, `RestaurantOrder` data class is a `Product` type which models `AND` relationship:

```
RestaurantOrder = RestaurantOrderId & RestaurantId & Status & list of [RestaurantOrderLineItem]
```

## Behaviour

- algebraic data types form the `structure` of our entities (commands, state and events)
- functions/lambda offer the algebra of manipulating the entities in a compositional manner, effectively modeling
  the `behaviour`.

This leads to modularity in design and a clear separation of the `structure` and functions/`behaviour` of the entity.

`Fmodel` library offers generic and abstract components which you can specialise for your specific case/expected
behaviour.

### Decider

Decider is a pure domain component. It represents the main decision-making algorithm.

- `initialState` - A starting point / An initial state of type `RestaurantOrder?`
- `decide` (Exhaustive / pattern matching command handler) - A function/lambda that takes command of
  type `RestaurantOrderCommand?` and input state of type `RestaurantOrder?` as parameters, and returns/emits the flow of
  output events `Flow`<`RestaurantOrderEvent?`>
- `evolve` (Exhaustive / pattern matching event-sourcing handler) - A function/lambda that takes input state of
  type `RestaurantOrder?` and input event of type `RestaurantOrderEvent` as parameters, and returns the output/new
  state `RestaurantOrder`

![decider image](../.assets/decider-kotlin.png)

```kotlin
fun restaurantOrderDecider() = Decider<RestaurantOrderCommand?, RestaurantOrder?, RestaurantOrderEvent?>(
    // Initial state of the Restaurant Order is `null`. It does not exist.
    initialState = null,
    // Exhaustive command handler(s): for each type of [RestaurantCommand] you are going to publish specific events/facts, as required by the current state/s of the [RestaurantOrder].
    decide = { c, s ->
        when (c) {
            is CreateRestaurantOrderCommand ->
                // ** positive flow **
                if (s == null) flowOf(RestaurantOrderCreatedEvent(c.identifier, c.lineItems, c.restaurantIdentifier))
                // ** negative flow **
                else flowOf(RestaurantOrderRejectedEvent(c.identifier, "Restaurant order already exists"))
            is MarkRestaurantOrderAsPreparedCommand ->
                // ** positive flow **
                if ((s != null && CREATED == s.status)) flowOf(RestaurantOrderPreparedEvent(c.identifier))
                // ** negative flow **
                else flowOf(RestaurantOrderNotPreparedEvent(c.identifier, "Restaurant order does not exist or not in CREATED state"))
            null -> emptyFlow() // We ignore the `null` command by emitting the empty flow. Only the Decider that can handle `null` command can be combined (Monoid) with other Deciders.
        }
    },
    // Exhaustive event-sourcing handler(s): for each event of type [RestaurantEvent] you are going to evolve from the current state/s of the [RestaurantOrder] to a new state of the [RestaurantOrder]
    evolve = { s, e ->
        when (e) {
            is RestaurantOrderCreatedEvent -> RestaurantOrder(e.identifier, e.restaurantId, CREATED, e.lineItems)
            is RestaurantOrderPreparedEvent -> s?.copy(status = PREPARED)
            is RestaurantOrderErrorEvent -> s // Error events are not changing the state / We return current state instead.
            null -> s // Null events are not changing the state / We return current state instead. Only the Decider that can handle `null` event can be combined (Monoid) with other Deciders.
        }
    }
)
```

### View

View is a datatype that represents the event handling algorithm, responsible for translating the events into
denormalized state, which is more adequate for querying.

- `initialState` - A starting point / An initial state of type `RestaurantOrderView?`
- `evolve` (Exhaustive / pattern matching event handler) - A function/lambda that takes input state of
  type `RestaurantOrderView?` and input event of type `RestaurantOrderEvent` as parameters, and returns the output/new
  state `RestaurantOrderView`

![view image](../.assets/view-kotlin.png)

```kotlin
fun restaurantOrderView() = View<RestaurantOrderView?, RestaurantOrderEvent?>(
    // Initial state of the [RestaurantOrderViewState] is `null`. It does not exist.
    initialState = null,
    // Exhaustive event handler(s): for each event of type [RestaurantOrderEvent] you are going to evolve from the current state/s of the [RestaurantOrderViewState] to a new state of the [RestaurantOrderViewState].
    evolve = { s, e ->
        when (e) {
            is RestaurantOrderCreatedEvent -> RestaurantOrderView(e.identifier, e.restaurantId, CREATED, e.lineItems)
            is RestaurantOrderPreparedEvent -> s?.copy(status = PREPARED)
            is RestaurantOrderErrorEvent -> s // We ignore the `error` event by returning current State/s.
            null -> s // We ignore the `null` event by returning current State/s. Only the View that can handle `null` event can be combined (Monoid) with other Views.

        }
    }
)
```

### Saga

Saga is a datatype that represents the central point of control deciding what to execute next. It is responsible for
mapping different events from deciders into action result that the Saga then can use to calculate the next actions to be
mapped to command of other deciders.

Saga does not maintain the state.

- `react` - A function/lambda that takes input action-result/event of type `RestaurantEvent?`, and returns the flow of
  actions/commands `Flow`<`RestaurantOrderCommand`> that should be published.

![saga image](../.assets/saga-kotlin.png)

```kotlin
fun restaurantOrderSaga() = Saga<RestaurantEvent?, RestaurantOrderCommand?>(
    react = { e ->
        when (e) {
            is RestaurantOrderPlacedAtRestaurantEvent -> flowOf(
                CreateRestaurantOrderCommand(
                    e.restaurantOrderId,
                    e.identifier,
                    e.lineItems
                )
            )
            is RestaurantCreatedEvent -> emptyFlow()
            is RestaurantMenuActivatedEvent -> emptyFlow()
            is RestaurantMenuChangedEvent -> emptyFlow()
            is RestaurantMenuPassivatedEvent -> emptyFlow()
            is RestaurantErrorEvent -> emptyFlow()
            null -> emptyFlow() // We ignore the `null` event by returning the empty flow of commands. Only the Saga that can handle `null` event/action-result can be combined (Monoid) with other Sagas.
        }
    }
)
```

> The essence of functional programming lies in the power of pure functions. Add static types to the mix, and you have
> algebraic abstractions—functions operating on types and honoring certain laws. Make the functions generic on types, and
> you have parametricity. The function becomes polymorphic, which implies more reusability, and if you’re disciplined
> enough not to leak any implementation details by sneaking in specialized types (or unmanaged hazards such as exceptions)
> , you get free theorems.

## References and further reading

- [https://fraktalio.com/fmodel/](https://fraktalio.com/fmodel/)
- [https://www.manning.com/books/functional-and-reactive-domain-modeling](https://www.manning.com/books/functional-and-reactive-domain-modeling)

---
Created with :heart: by [Fraktalio](https://fraktalio.com/)
