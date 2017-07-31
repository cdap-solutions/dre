# YARE

YARE is big data native Rules Engine built ground up. It includes a easy-to-understand **Business Readable DSL**.
YARE provides an alternative computational model instead of the usual imperative model,
which consists of commands in sequence with conditionals and loops. This rules engine is based
on a Production Rule System. Production rules, each of which has a `when` clause defining the condition for firing the rule and a `action` that is execute when the condition is true.

YARE has been built ground-up to address the needs of business data transformation for big
data systems. It supports application of production rules for both Batch and Realtime system.

## YARE Provides

 * A Inference Engine that implements Forward Chaining
   * CDAP Plugin -- Transform
   * CDAP Plugin -- Action
 * Rule Management Service,
 * Rule Store &
 * **FUTURE** Rules Management Web Interface.

## YARE Rule Construct

Following is the construct of a rule within YARE.

```
rule "rule-id" {
  description "Every rule requires a description"
  when ( condition )
  then {
    wrangler directive;
    wrangler directive;
  }
}
```

Each rule is defined by a `rule-id`. Each rule also consists of mandatory description followed by a `condition` that will be evaluated by the inference engine. If the `condition` evaluates to `true`, the rule is fired and directives within the `then` section are executed.

## YARE Rulebook Construct

`Rulebook` is a collection of `Rules` that can be composed dynamically. A `Rulebook` can consists of one or more `Rule`s. Following is a simple example of a rulebook that can be defined within YARE.

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

## More information

Please read the [wiki pages](https://github.com/cask-solutions/yare/wiki) for more information.
