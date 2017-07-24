/*
 * Copyright © 2017 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

grammar Rulebook;

options {
  language = Java;
}

@lexer::header {
/*
 * Copyright © 2017 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
}

rulebook
  : 'rulebook' Identifier '{' rbVersion rbMeta rbRules '}' EOF
  ;

rbVersion
  : 'version' Number
  ;

rbRules
  : rbRule+
  ;

rbMeta
  : 'meta' '{' ( rbMetaDescription | rbMetaCreatedDate| rbMetaUpdatedDate| rbMetaUserName | rbMetaSourceName )*? '}'
  ;

rbRule
  : 'rule' Identifier '{' descriptionClause givenClause whenClause thenClause '}'
  | 'rule' Identifier '{' givenClause whenClause thenClause '}'
  | 'rule' Identifier '{' whenClause thenClause '}'
  | 'rule' Identifier '{' descriptionClause whenClause thenClause '}'
  ;

rbMetaDescription
  : 'description' String
  ;

rbMetaCreatedDate
  : 'created-date' Number
  ;

rbMetaUpdatedDate
  : 'updated-date' Number
  ;

rbMetaUserName
  : 'user' String
  ;

rbMetaSourceName
  : 'source' String
  ;

descriptionClause
  : 'description' String
  ;

givenClause
  : 'given' '{' assignments '}'
  ;

assignments
  : assignment+
  ;

assignment
  : Identifier '=' (Identifier | Number | String)
  ;

whenClause
  : 'when' condition
  ;

thenClause
  : 'then' '{' actions ';' '}'
  ;

actions
  : action (';' action)*?
  ;

action
  : Identifier
  | .*?
  ;

condition
 : OParen (.+?) CParen
 ;


OBrace   : '{';
CBrace   : '}';
SColon   : ';';
Or       : '||';
And      : '&&';
Equals   : '==';
NEquals  : '!=';
GTEquals : '>=';
LTEquals : '<=';
Match    : '=~';
NotMatch : '!~';
StartsWith : '=^';
NotStartsWith : '!^';
EndsWith : '=$';
NotEndsWith : '!$';
PlusEqual : '+=';
SubEqual : '-=';
MulEqual : '*=';
DivEqual : '/=';
PerEqual : '%=';
AndEqual : '&=';
OrEqual  : '|=';
XOREqual : '^=';
Pow      : '^';
External : '!';
GT       : '>';
LT       : '<';
Add      : '+';
Subtract : '-';
Multiply : '*';
Divide   : '/';
Modulus  : '%';
OBracket : '[';
CBracket : ']';
OParen   : '(';
CParen   : ')';
Assign   : '=';
Comma    : ',';
QMark    : '?';
Colon    : ':';
Dot      : '.';
At       : '@';
Pipe     : '|';
BackSlash: '\\';
Dollar   : '$';
Tilde    : '~';
Hash     : '#';

Bool
 : 'true'
 | 'false'
 ;

Number
 : Int ('.' Digit*)?
 ;

Identifier
 : [a-zA-Z] [a-zA-Z_0-9\-\\.]*
 ;

String
 : '\'' ( EscapeSequence | ~('\'') )* '\''
 | '"'  ( EscapeSequence | ~('"') )* '"'
 ;

EscapeSequence
   :   '\\' ('b'|'t'|'n'|'f'|'r'|'"'|'\''|'\\')
   |   UnicodeEscape
   |   OctalEscape
   ;

fragment
OctalEscape
   :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
   |   '\\' ('0'..'7') ('0'..'7')
   |   '\\' ('0'..'7')
   ;

fragment
UnicodeEscape
   :   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
   ;

fragment
   HexDigit : ('0'..'9'|'a'..'f'|'A'..'F') ;

Comment
 : ('//' ~[\r\n]* | '/*' .*? '*/' | '--' ~[\r\n]* ) -> skip
 ;

Space
 : [ \t\r\n\u000C]+ -> skip
 ;

fragment Int
 : [1-9] Digit* [L]*
 | '0'
 ;

fragment Digit
 : [0-9]
 ;