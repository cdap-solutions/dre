rulebook "Send-To-Error" {
  version 1

  meta {
    description "Test"
    source "my-rules.xslx"
    user "joltie"
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