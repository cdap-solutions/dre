/**
 * This is rule book for normalizing the processing of titanic file.
 * The rules are applied using an inference engine with forward chaining
 * Rule firing is defines the ordering of how the rules are applied to
 * the input record.
 *
 * A service exists to transform this data.
 */
rulebook "Titanic-Normalization" {
  version 1

  meta {
    description "Test"
    source "my-rules.xslx"
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
      set columns PassengerId,Survived,Pclass,Name,Sex,Age,SibSp,Parch,Ticket,Fare,Cabin,Embarked;
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