# YARE

YARE is big data native Rules Engine built ground up. It includes a easy-to-understand **Business Readable DSL**.
YARE provides an alternative computational model instead of the usual imperative model, 
which consists of commands in sequence with conditionals and loops. This rules engine is based 
on a Production Rule System. Production rules, each of which has a `when` clause defining the condition for firing the rule and a `action` that is execute when the condition is true.

YARE has been built ground-up to address the needs of business data transformation for big 
data systems. It supports application of production rules for both Batch and Realtime system. 

## What's available with this Rules Engine

It contains:

 * A Inference Engine that implements Forward Chaining,
 * Rule Management Service &
 * **FUTURE** Rules Management Web Interface. 
 
NOTE: Current implementation of inference engine does not implement
Rete Algorithm for inferring the execution working set,
but relies of pre compilation step to determine the rules to be fired.
This will be optimized in the upcoming releases. 

## A Rule

Following is a simple example of a rule that can be defined within this
implementation of Rule Engine.

```
/**
 * This is rule book for normalizing the processing of titanic file. 
 * The rules are applied using an inference engine with forward chaining
 * Rule firing defines the ordering of how the rules are applied to 
 * the input record.
 */
rulebook "Titanic Feed Normalization" {
  version 1

  meta {
    description "This rule book applies transformation on the titanic feed."
    source "titanic-rules.xslx"
    user "joltie"
  }

  rule "remove-first-line" {
    description "Removes first line when offset is zero"
    when(present(offset) && offset == 0) then {
      filter-row-if-true true;
    }
  }

  rule "parse-as-csv" {
    description "Parses body"
    when(present(body)) then {
      parse-as-csv body ',' false;
      drop body;
      set columns offset,PassengerId,Survived,Pclass,Name,Sex,Age,SibSp,Parch,Ticket,Fare,Cabin,Embarked;
      cleanse-column-names;
    }
  }

  rule "rename-sex-to-gender" {
    description "Rename sex field to gender"
    when(present(sex)) then {
      rename sex gender;
    }
  }

  rule "single-character-gender" {
    description "Converts gender to single character"
    when(present(gender) && gender.length() > 1) then {
      cut-character gender gender 1-1;
      uppercase gender;
    }
  }

  rule "missing-age" {
    description "If age is missing, send it to error"
    when (!present(age)) then {
      send-to-error true;
    }
  }
  
  rule "remove-fare-less-than-8.06" {
    description "Send to error fares that are less that 8.06"
    when (fare < 8.06) then {
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
