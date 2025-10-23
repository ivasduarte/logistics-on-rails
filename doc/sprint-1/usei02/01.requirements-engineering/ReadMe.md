# USEI02 - Order Eligibility & Allocation

## 1. Requirements Engineering

### 1.1. User Story Description

As a warehouse planner, when I receive open orders, I want the system to examine current inventory and allocate quantities from boxes in FEFO order,
and produce per-line statuses (ELIGIBLE, PARTIAL, UNDISPATCHABLE) and a list of allocation rows with box and bay information.

### 1.2. Customer Specifications and Clarifications

**From the document specifications:**

> A warehouse planner must fulfill the orders it receives. To achieve 
this, he needs to verify from the inventory which order lines can be dispatched and, 
for those, allocate concrete quantities from boxes using FEFO rules. The outcome 
is a list of per-line statuses and a set of concrete allocation rows (per order,line,box) for dispatch planning.

**From the client clarifications:**

> **Question:** The output of the USEI02 is supposed to be used as input for the following US?
Or is it just an informative description of the process?
> **Answer:** If you want you can use.
> 
> 
> **Question:** Can the user select which open orders to process? Or does it run through all of them?
> **Answer:** It must run all of them.
> 
> 
> **Question:** Good morning. In the USEI02, since we can have more than one warehouse and the orders are not designated
for a specific warehouse, can we make allocations from different warehouses for the same order if the first warehouse
doesn’t have enough quantity of an SKU to complete the order? If yes, in the USEI04, do we calculate the total distance 
for each warehouse and then sum them up?
> **Answer:** Yes, you can have allocations from more than one warehouse if it is necessary. 
The distances traversed in the different warehouses must be added.

### 1.3. Acceptance Criteria

> **1. Order Processing Method**
> Orders must be processed by priority, due date and order id.

> **2. Order Line Processing**
> Within an order, lines must be processed based on its number.

> **3. Organization Mode**
> For each line, allocation must follow FEFO/FIFO organization mode.

> **4. Availability Verification** 
> For each bay, its allocation takes the lowest value between remaining quantity and box available quantity,
> reduce the box availability and continue until request is satisfied or the boxes end.

> **5. Minimal Value**
> Box availability must never go below zero.

> **6. Modes supported** 
> The system must support STRICT and PARTIAL modes.
> STRICT: A line is only eligible for allocation if its entire requested quantity can be allocated.
> PARTIAL: If it isn't possible to fulfill the request, line should be marked as PARTIAL, and the portion allocated must be kept. 

### 1.4. Found out Dependencies
- **USEI01** : This User Story depends on the inventory organization made on USEI01.

### 1.5. System Sequence Diagram (SSD)

![System Sequence Diagram](svg/USEI02-SSD.svg)