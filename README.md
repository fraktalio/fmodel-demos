# Functional domain modeling - Examples

The word `domain` here means the area of interest in the business. When you are developing an `information system` to
automate activities of that business, you’re modeling the business. The abstractions that you design, the behaviors that
you implement, and the UI interactions that you build all reflect the business — *together they constitute the `model`
of the domain*.

## f(model)

These examples are demonstrating the usage of the [fmodel](https://fraktalio.com/fmodel)
library.

Library is written in [Kotlin](https://kotlinlang.org/) programing language by practicing functional style. It is a
small library constructed out of generic data types that can be used to effectively model any stateful information
system.

It is truly decoupling the pure declaration of our program logic from the runtime. And therefore, the runtime has the
chance to see the big picture of our program and decide how to run and optimize it.

## Examples

To demonstrate that `pure declaration of our program logic` is separated from the runtime target and infrastructure, we
are going to
practice [onion architecture / ports and adapters](https://blog.ploeh.dk/2013/12/03/layers-onions-ports-adapters-its-all-the-same/)
in all examples.

![onion architecture image](.assets/onion.png)

The arrows in the image are showing the direction of the dependency. Notice that all dependencies point inwards, and
that Domain is not depending on anybody or anything.

### Restaurant management system

Restaurant management system is:

- managing restaurant menus and other information including location and opening hours
- managing the preparation of orders at a restaurant kitchen

![restaurant management - event model](.assets/event-model.jpg)

We are going to run our **unique** core domain logic in different ways, by implementing
different [`application`](application) and/or `persistence` layers:

| Type | Description | Technology |
| --- | --- | --- |
| [State stored information system](application/state-stored-system1) | Restaurant management | Kotlin, Arrow, Reactive Spring Boot, R2DBC |
| [Event-sourced information system](application/event-sourced-system1) | Restaurant management| Kotlin, Arrow, Reactive Spring Boot, [Axon Framework](https://axoniq.io/product-overview/axon-framework) and [Axon Server](https://axoniq.io/product-overview/axon-server) |
| [Event-sourced information system - distributed](application/event-sourced-system2) | Restaurant management| Kotlin, Arrow, Reactive Spring Boot, [Axon Framework](https://axoniq.io/product-overview/axon-framework) and [Axon Server](https://axoniq.io/product-overview/axon-server) |

## References and further reading

- [https://fraktalio.com/fmodel/](https://fraktalio.com/fmodel/)

---
Created with :heart: by [Fraktalio](https://fraktalio.com/)
