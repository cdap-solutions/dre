# Rules Engine

This Rule Engine is a sophisticated if-then-else statement interpreter that runs
natively on big data system. It's built ground-up to address the needs of business
data transformation for big data systems. This implementation of Rule Engine supports
transformation for both Batch and Realtime system.

Rule Engine contains:

 * A Simple Inference Engine that implements Forward Chaining,
 * Rule Management Service &
 * Rules DB
 * **FUTURE** Rules Management Web Interface

NOTE: Current implementation of inference engine does not implement
Rete Algorithm for inferring the execution working set,
but relies of pre compilation step to determine the rules to be fired.
This will be optimized in the upcoming releases.

## A Rule

Following is a simple example of a rule that can be defined within this
implementation of Rule Engine.

```
rulebook "Titanic-Normalization" {
  version 1

  meta {
    description "Test"
    source "my-rules.xslx"
    user "joltie"
  }

  rule "remove-id" {
    description "Removes the id"
    when(present(id)) then {
      drop id;
    }
  }

  rule "composite-b-compute" {
    description "Creates new field 'b' derived from a."
    when(present(a)) then {
      set-column b a*10;
    }
  }

  rule "composite-c-compute" {
    description "Creates new field 'c' after b is created"
    when(present(b)) then {
      set-column b 1;
    }
  }

  rule "mask-ssn" {
    description "Mask SSN if exists in the record"
    when(present(ssn)) then {
      mask-number ssn xxx-xx-####;
    }
  }

  rule "shuffle-address" {
    description "Address field will be shuffled."
    when(present(address)) then {
      mask-shuffle address
    }
  }

  rule "single-character-gender" {
    description "Rule reduces the gender to single character"
    when(present(gender) && gender.length() > 1) then {
      cut-character gender 1;
      uppercase gender;
    }
  }

  rule "missing-age" {
    description "If age is missing, set it to zero"
    when (present(age)) then {
      set-column age 0;
    }
  }

  rule "error-missing-fare" {
    description "Send to error if fare is missing"
    when(!present(fare)) then {
      send-to-error true;
    }
  }

  rule "fare-less-than-10" {
    description "Send records to error when the fare is less than 10"
    when(fare < 10.0) then {
      send-to-error true;
    }
  }
}
```


## Background

Rules Engine provide a simple way to separate out the data and logic manipulating the data.
Rule engine represents the decision that a business does by taking in one or more
rule books as input set along with data
elements on which the rule books are applied to generate the final
data elements meeting the business goals.

This implementation of Rule Engine is meant for applying rules on
a large scale data processing system like Apache Spark and MapReduce engines.

The Rule Engine implements the forward-chaining where the actions of
one rule can cause the condition of other rules to be executed.
Execution has cascading effect in terms of how the rules are executed.

This Rule Engine is built for performing business data transformation
for large and complex datasets mainly observed within big data systems.
It's built with the goal of providing reusability, greater flexibility,
reduced complexity and ease of understanding.

## Finding the Perfect Rules Engine for Big Data

First, we looked at Drools, but we quickly decided that Drools was much
more than was required and also not scalable for high volume data processing
environment. Besides that, Drool has it's own repository and management
that's not integrated with other system in Big Data ecosystem like
Data Governance. Aside from that, many developers are generally not familiar
with Drools.

Next, we looked at Easy Rules. Easy Rules seemed a lot easier and much
easier interface than Drools. Ordering and support for Forward or Backward
chain responsibilities were difficult to implement.
