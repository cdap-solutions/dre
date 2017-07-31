rulebook Titanic-Normalization {
  version 1

  meta {
    description "Test"
    source "my-rules.xslx"
    user "joltie"
  }

  rule remove-id {
    description "Removes the id"
    when(present(id)) then {
      drop id;
    }
  }

  rule composite-b-compute {
    description "Creates new field 'b' derived from a."
    when(present(a)) then {
      set-column b a*10;
    }
  }

  rule composite-c-compute {
    description "Creates new field 'c' after b is created"
    when(present(b)) then {
      set-column b 1;
    }
  }

  rule mask-ssn {
    description "Mask SSN if exists in the record"
    when(present(ssn)) then {
      mask-number ssn xxx-xx-####;
    }
  }

  rule shuffle-address {
    description "Address field will be shuffled."
    when(present(address)) then {
      mask-shuffle address
    }
  }

  rule single-character-gender {
    description "Rule reduces the gender to single character"
    when(present(gender) && gender.length() > 1) then {
      cut-character gender 1;
      uppercase gender;
    }
  }

  rule missing-age {
    description "If age is missing, set it to zero"
    when (present(age)) then {
      set-column age 0;
    }
  }

  rule error-missing-fare {
    description "Send to error if fare is missing"
    when(!present(fare)) then {
      send-to-error true;
    }
  }

  rule fare-less-than-10 {
    description "Send records to error when the fare is less than 10"
    when(fare < 10.0) then {
      send-to-error true;
    }
  }
}