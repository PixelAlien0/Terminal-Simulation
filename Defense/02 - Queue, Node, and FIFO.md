---
tags: [terminal-simulation, queue, node, fifo]
---

# Queue, Node, and FIFO

## The picture to remember

```text
front                                      rear
  |                                          |
  v                                          v
[P1 | next] -> [P2 | next] -> [P3 | null]
```

Each box is a `PassengerNode`. The queue remembers the first and last node.

## Key terms

**Queue**  
A linear data structure where elements are normally added at the rear and removed from the front.

**FIFO**  
First In, First Out. If P1 enters before P2, P1 is served first unless a separate priority rule applies.

**Node**  
An object that stores data and a reference to another node. In this project, the data is a `Person`, and `next` points to the following passenger node.

**Front**  
The node that will be inspected or removed first.

**Rear**  
The last node, where a new passenger is attached.

## `enqueue(passenger)`

1. Create a node containing the passenger.
2. If the queue is empty, make both `front` and `rear` point to it.
3. Otherwise, make the old rear's `next` point to the new node.
4. Move `rear` to the new node.
5. Increase `size`.

Typical time complexity: **O(1)** because the queue already knows the rear.

## `dequeue()`

1. Save the passenger at `front`.
2. Move `front` to `front.next`.
3. If the queue becomes empty, also set `rear` to `null`.
4. Decrease `size`.
5. Return the saved passenger.

Typical time complexity: **O(1)** because removal happens at the front.

## `remove(passenger)`

This operation searches through nodes until it finds the requested passenger. It must reconnect the previous node to the following node. If the removed node was the front or rear, those references must also be corrected.

Typical time complexity: **O(n)** because it may inspect the whole queue.

## Why two ticket queues?

The simulation keeps regular and priority passengers separate so each line remains FIFO internally while the service rule can prefer the priority line. Priority does not mean the custom queue is no longer FIFO; it means the engine chooses which FIFO queue to serve.

## Queue versus `ArrayList`

The master `ArrayList<Person>` is useful for listing, searching, drawing, and CRUD operations on all active passengers. The custom `PassengerQueue` is specifically responsible for service order. They have different jobs.
