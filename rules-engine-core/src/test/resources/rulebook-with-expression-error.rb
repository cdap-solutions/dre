rulebook test1 {
  version 1

  meta {
    description "Test"
    created-date 1500621677
    updated-date 1500622899
    source "mdm"
    user "nmotgi"
  }

  rule validate.age.less {
    description "If age is less than 0 it's set to zero"
    when ( age %^^$^$ 0 ) then {
      age = 0;
    }
  }
}