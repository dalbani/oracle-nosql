/*-
 *
 *  This file is part of Oracle NoSQL Database
 *  Copyright (C) 2011, 2015 Oracle and/or its affiliates.  All rights reserved.
 *
 *  Oracle NoSQL Database is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU Affero General Public License
 *  as published by the Free Software Foundation, version 3.
 *
 *  Oracle NoSQL Database is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public
 *  License in the LICENSE file along with Oracle NoSQL Database.  If not,
 *  see <http://www.gnu.org/licenses/>.
 *
 *  An active Oracle commercial licensing agreement for this product
 *  supercedes this license.
 *
 *  For more information please contact:
 *
 *  Vice President Legal, Development
 *  Oracle America, Inc.
 *  5OP-10
 *  500 Oracle Parkway
 *  Redwood Shores, CA 94065
 *
 *  or
 *
 *  berkeleydb-info_us@oracle.com
 *
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  EOF
 *
 */

/*
 * IMPORTANT: the code generation by Antlr using this grammar is not
 * automatically part of the build.  It has its own target:
 *   ant generate-ddl
 * If this file is modified that target must be run.  Once all relevant
 * testing is done the resulting files must be modified to avoid warnings and
 * errors in Eclipse because of unused imports.  At that point the new files can
 * be check in.
 *
 * This file describes the syntax of the Oracle NoSQL Table DDL.  In order to
 * make the syntax as familiar as possible, the following hierarchy of existing
 * languages is used as the model for each operation:
 * 1.  SQL standard
 * 2.  Oracle SQL
 * 3.  MySQL
 * 4.  SQLite or other SQL
 * 5.  Any comparable declarative language
 * 6.  New syntax, specific to Oracle NoSQL.
 *
 * The major commands in this grammar include
 * o create/drop table
 * o alter table
 * o create/drop index
 * o describe
 * o show
 *
 * Grammar notes:
 *  Antlr resolves ambiguity in token recognition by using order of
 *  declaration, so order matters when ambiguity is possible.  This is most
 *  typical with different types of identifiers that have some overlap.
 *
 *  This grammar uses some extra syntax and Antlr actions to generate more
 *  useful error messages (see use of "notifyErrorListeners").  This is Java-
 *  specific code.  In the future it may be useful to parse in other languages,
 *  which is supported by Antlr4.  If done, these errors may have to be handled
 *  elsewhere or we'd have to handle multiple versions of the grammar with minor
 *  changes for language-specific constructs.
 */

/*
 * Parser rules (start with lowercase).
 */

grammar Table;

/*
 * This is the starting rule for the parse.  It accepts one of the number of
 * top-level statements.  The EOF indicates that only a single statement can
 * be accepted and any extraneous input after the statement will generate an
 * error.  Allow a semicolon to terminate statements.  In the future a semicolon
 * may act as a statement separator.
 */
parse   : (create_table_statement
        | create_index_statement
        | create_user_statement
        | create_role_statement
        | drop_index_statement
        | create_text_index_statement
        | drop_role_statement
        | drop_user_statement
        | alter_table_statement
        | alter_user_statement
        | drop_table_statement
        | grant_statement
        | revoke_statement
        | describe_statement
        | show_statement)
        EOF
    ;

/*
 * CREATE TABLE.
 * A sequence of field definitions and key definitions.
 * CREATE TABLE (if not exists) foo (fields)
 */
create_table_statement : CREATE TABLE (IF_NOT_EXISTS)?
        name_path LP table_def RP ;

/*
 * DROP TABLE [if exists] name_path
 */
drop_table_statement : DROP TABLE (IF_EXISTS)? name_path ;

/*
 * CREATE INDEX [if not exists] name ON name_path (field, ...)
 */
create_index_statement : CREATE INDEX (IF_NOT_EXISTS)?
        index_name ON name_path complex_field_list comment?;

/*
 * DROP INDEX [if exists] index_name ON name_path
 */
drop_index_statement : DROP INDEX (IF_EXISTS)? index_name ON name_path ;

/*
 * CREATE FULLTEXT INDEX [if not exists] name ON name_path (field [ mapping ], ...)
 */
create_text_index_statement : CREATE FULLTEXT INDEX (IF_NOT_EXISTS)?
        index_name ON name_path fts_field_list comment?;

/*
 * ALTER TABLE name_path (ADD|DROP|MODIFY field_path [field_type])
 * Multiple operations can exist, comma-separated, inside the parentheses.
 */
alter_table_statement : ALTER TABLE name_path alter_field_statement ;

/*
 * DESC[RIBE] TABLE name_path [field_path[,field_path]]
 * DESC[RIBE] INDEX index_name ON name_path
 */
describe_statement : (DESCRIBE | DESC) AS_JSON?
        (TABLE name_path complex_field_list?
        | INDEX index_name ON name_path) ;

/*
 * SHOW TABLES
 * SHOW INDEXES ON name_path
 * SHOW TABLE name_path -- lists hierarchy of the table
 */
show_statement: SHOW AS_JSON?
        (TABLES
        | USERS
        | ROLES
        | USER identifier_or_string
        | ROLE identifier
        | INDEXES ON name_path
        | TABLE name_path)
    ;

/*
 * Table modification -- add, drop, modify fields in an existing table.
 * This definition allows multiple changes to be contained in a single
 * alter table statement.
 */
alter_field_statement : LP (add_field_statement | drop_field_statement |
                            modify_field_statement)
                      (COMMA (add_field_statement | drop_field_statement |
                              modify_field_statement))* RP ;

/* Add a field */
add_field_statement : ADD field_def ;

/* Drop a field */
drop_field_statement : DROP name_path ;

/* Modify a field.  Types of modifications are limited. */
modify_field_statement : MODIFY field_def ;

table_def : (field_def | key_def | comment)
        (COMMA (field_def | key_def | comment))* ;

/*
 * PRIMARY KEY ([SHARD(id_list),] id_list)
 */
key_def :  PRIMARY_KEY LP (shard_key_def COMMA?)? id_list? RP ;

/*
 * SHARD (field (, field)*)
 */
shard_key_def : SHARD simple_field_list ;

/*
 * A comma-separated list of ids contained inside parentheses
 */
id_list_with_paren : LP id_list RP
    | LP id_list  {notifyErrorListeners("Missing closing ')'");}
    | LP (BAD_ID|ID) (COMMA (BAD_ID|ID))*
        {notifyErrorListeners("Identifiers must start with a letter: " +
                $text);}
    ;

id_list : extended_id (COMMA extended_id)* ;

/*
 * A list of field names that may or may not reference nested fields.  This is
 * used to reference fields in an index or a describe statement.
 *  (fieldName[.fieldName] (,fieldName[.fieldName])*)
 *  KEYOF(fieldName)(.fieldName)*
 *  ELEMENTOF(fieldName)(.fieldName)*
 */
complex_field_list : LP path_list RP
    | LP path_list  {notifyErrorListeners("Missing closing ')'");}
    ;

/*
 * A comma-separated list of paths to field names.
 */
path_list : complex_name_path (COMMA complex_name_path)* ;

/*
 * A list of field names, as above, which may or may not include
 * a text-search mapping specification per field.
 */
fts_field_list : LP fts_path_list RP
    | LP fts_path_list {notifyErrorListeners("Missing closing ')'");}
    ;

/*
 * A comma-separated list of paths to field names with optional mapping specs.
 */
fts_path_list : fts_path (COMMA fts_path)* ;

/*
 * A field name with optional mapping spec.
 */
fts_path : complex_name_path json? ;

/*
 * A comma-separated list of strings contained inside parentheses
 */
string_list : LP STRING (COMMA STRING)*  RP
    | LP STRING (COMMA STRING)* {notifyErrorListeners("Missing closing ')'");}
    ;

/*
 * A list of field names that can only have one component:
 *  (fieldName (fieldName)*)
 */
simple_field_list : id_list_with_paren ;

/*
 * A definition of a named, typed field.
 */
field_def : name_path type_def ;

/*
 * All supported type definitions.  Each type has type-specific, optional
 * constraints.  The # labels on each line cause Antlr to generate events
 * specific to that type, which allows the parser code to more simply
 * discriminate among types.
 */
type_def : binary_def    # Binary
    | array_def          # Array
    | boolean_def        # Boolean
    | enum_def           # Enum
    | float_def          # Float
    | integer_def        # Int
    | map_def            # Map
    | record_def         # Record
    | string_def         # String
    ;

/*
 * integer_def includes INTEGER and LONG
 */

integer_def : (INTEGER_T | LONG_T) integer_constraint* comment? integer_constraint* ;

/*
 * float_def includes FLOAT and DOUBLE
 */
float_def : (FLOAT_T | DOUBLE_T) float_constraint* comment? float_constraint*;

string_def : STRING_T string_constraint* comment? string_constraint* ;

boolean_def : BOOLEAN_T boolean_constraint* comment? boolean_constraint* ;

/*
 * Enumeration is defined by a list of ID values.
 *   enum (val1, val2, ...)
 */
enum_def : ENUM_T id_list_with_paren enum_constraint* comment? enum_constraint* ;

binary_def : BINARY_T (LP (WS)* INT (WS)* RP)? comment?;

/*
 * A record contains one or more field definitions.
 * Record (field [,field]+)
 */
record_def : RECORD_T LP field_def (COMMA field_def)* RP comment?;

/*
 * A map has a single type definition for its map element
 * map (type)
 */
map_def : MAP_T LP type_def RP comment?;

/*
 * An array has a single type definition for its map element
 * array (type)
 */
array_def : ARRAY_T LP type_def RP comment?;

/*
 * A comment string of arbitrary length
 */
comment : COMMENT STRING ;

/*
 * Booleans can be defaulted but that is all.
 */
boolean_constraint : (DEFAULT BOOLEAN_VALUE) | not_null ;

/*
 * Integer constraints include range and default, and apply to both
 * integer and long.
 *
 * Repeated constraints are handled in code.
 */
integer_constraint : not_null | integer_default | check_expression ;

integer_default : DEFAULT INT ;

/*
 * Float constraints include range and default, and apply to both
 * float and double.
 *
 * Repeated constraints are handled in code.
 */
float_constraint : not_null | float_default | check_expression ;

float_default : DEFAULT (FLOAT | INT);

/*
 * String constraints include range and default
 */
string_constraint : not_null | string_default | check_expression ;

string_default : DEFAULT STRING
    | DEFAULT extended_id {notifyErrorListeners
            ("String default value must be quoted: " + $extended_id.text);}
    ;

/*
 * Enum constraints are not null and default value.
 */
enum_constraint : not_null
    | DEFAULT extended_id
    | DEFAULT STRING {notifyErrorListeners
            ("Enum default value should not be quoted: " + $STRING.text);}
    ;

/*
 * Expressions are generic.  Type checking is done in the parser handling code.
 */
check_expression : CHECK LP expr (AND expr)* RP ;

expr : extended_id OP (INT | FLOAT | STRING)
    | elementof_expr OP (INT | FLOAT | STRING)
    ;

/*
 * This is a separate rule to allow a single entry point to be shared among
 * the types that can be set to not null.
 */
not_null : NOT_NULL;

/*
 * name_path includes table names and field names.  Both of these may have
 * multiple components.  Table names may reference child tables using dot
 * notation and similarly, field names may reference nested fields using
 * dot notation as well.
 */
name_path : (extended_id | NAME_PATH)
    | BAD_ID {notifyErrorListeners
            ("Field and table names must start with a letter: " + $text);}
    | BAD_NAME_PATH {notifyErrorListeners
            ("Field and table names must start with a letter: " + $text);}
    ;

/*
 * complex_name_path handles a basic name_path but adds KEYOF() and ELEMENTOF()
 * expressions to handle addressing in maps and arrays.
 * NOTE: if the syntax of KEYOF or VALUEOF changes the source should be checked
 * for code that reproduces these constants.
 */
complex_name_path : (name_path | keyof_expr | elementof_expr) ;

keyof_expr : KEYOF LP name_path RP ;

elementof_expr : ELEMENTOF LP name_path RP ('.' name_path)?
    | ELEMENTOF LP RP ;

index_name : extended_id
    | BAD_ID {notifyErrorListeners("Index names must start with a letter: " +
                $text);}
    ;

/*
 * Parse rules of security commands.
 */

/*
 * CREATE USER user (IDENTIFIED BY password [PASSWORD EXPIRE]
 * [PASSWORD LIFETIME duration] | IDENTIFIED EXTERNALLY)
 * [ACCOUNT LOCK|UNLOCK] [ADMIN]
 */
create_user_statement : CREATE USER create_user_identified_clause
    account_lock? ADMIN? ;

/*
 * CREATE ROLE role
 */
create_role_statement : CREATE ROLE identifier ;

/*
 * ALTER USER user [IDENTIFIED BY password [RETAIN CURRENT PASSWORD]]
 *       [CLEAR RETAINED PASSWORD] [PASSWORD EXPIRE]
 *       [PASSWORD LIFETIME duration] [ACCOUNT UNLOCK|LOCK]
 */
alter_user_statement : ALTER USER identifier_or_string
    reset_password_clause? (CLEAR_RETAINED_PASSWORD)? (PASSWORD_EXPIRE)?
        password_lifetime? account_lock? ;

/*
 * DROP USER user
 */
drop_user_statement : DROP USER identifier_or_string ;

/*
 * DROP ROLE role_name
 */
drop_role_statement : DROP ROLE identifier ;

/*
 * GRANT (grant_roles|grant_system_privileges|grant_object_privileges)
 *     grant_roles ::= role [, role]... TO { USER user | ROLE role }
 *     grant_system_privileges ::=
 *         {system_privilege | ALL PRIVILEGES}
 *             [,{system_privilege | ALL PRIVILEGES}]...
 *         TO role
 *     grant_object_privileges ::=
 *         {object_privileges| ALL [PRIVILEGES]}
 *             [,{object_privileges| ALL [PRIVILEGES]}]...
 *         ON table TO role
 */
grant_statement : GRANT
        (grant_roles
        | grant_system_privileges
        | grant_object_privileges)
    ;

/*
 * REVOKE (revoke_roles | revoke_system_privileges | revoke_object_privileges)
 *     revoke_roles ::= role [, role]... FROM { user | role }
 *     revoke_system_privileges ::=
 *         {system_privilege | ALL PRIVILEGES}
 *             [, {system_privilege | ALL PRIVILEGES}]...
 *         FROM role
 *     revoke_object_privileges ::=
 *         {object_privileges| ALL [PRIVILEGES]}
 *             [, { object_privileges | ALL [PRIVILEGES] }]...
 *         ON object FROM role
 */
revoke_statement : REVOKE
        (revoke_roles
        | revoke_system_privileges
        | revoke_object_privileges)
    ;

/*
 * Extended ID definition allowing some tokens to be reused
 */
extended_id : (ACCOUNT | ADMIN | ROLE | ROLES | USERS | TIME_UNIT
    | PASSWORD | LIFETIME | ID) ;

/*
 * Identifier rule parses the user name, role name and privilege name. The
 * names follows the format of ID, and will report error if simple violation is
 * found.
 */
identifier : extended_id
    | BAD_ID {notifyErrorListeners
         ("Identifier name must start with a letter: " + $text);} ;

/*
 * An identifier or a string
 */
identifier_or_string : (identifier | STRING) ;

/*
 * Identified clause, indicates the authentication method of user.
 */
identified_clause : IDENTIFIED by_password ;

/*
 * Identified clause for create user command, indicates the authentication
 * method of user. If the user is an internal user, we use the extended_id
 * for the user name. If the user is an external user, we use STRING for
 * the user name.
 */
create_user_identified_clause : (identifier identified_clause
    (PASSWORD_EXPIRE)? password_lifetime? | STRING IDENTIFIED_EXTERNALLY) ;

/*
 * Rule of authentication by password.
 */
by_password : BY STRING ;

/*
 * Rule of password lifetime definition.
 */
password_lifetime : PASSWORD_LIFETIME duration;

duration : INT (WS)* TIME_UNIT ;

/*
 * Rule of defining the reset password clause in the alter user statement.
 */
reset_password_clause : identified_clause RETAIN_CURRENT_PASSWORD? ;

account_lock : ACCOUNT (LOCK | UNLOCK) ;

/*
 * Subrule of granting roles to a user or a role.
 */
grant_roles : id_list TO principal ;

/*
 * Subrule of granting system privileges to a role.
 */
grant_system_privileges : sys_priv_list TO identifier ;

/*
 * Subrule of granting object privileges to a role.
 */
grant_object_privileges : obj_priv_list ON object TO identifier ;

/*
 * Subrule of revoking roles from a user or a role.
 */
revoke_roles : id_list FROM principal ;

/*
 * Subrule of revoking system privileges from a role.
 */
revoke_system_privileges : sys_priv_list FROM identifier ;

/*
 * Subrule of revoking object privileges from a role.
 */
revoke_object_privileges : obj_priv_list ON object FROM identifier  ;

/*
 * Parsing a principal of user or role.
 */
principal : (USER identifier_or_string | ROLE identifier) ;

sys_priv_list : priv_item (COMMA priv_item)* ;

priv_item : (identifier | ALL_PRIVILEGES) ;

obj_priv_list : (priv_item | ALL) (COMMA (priv_item | ALL))* ;

/*
 * Subrule of parsing the operated object. For now, only table object is
 * available.
 */
object : name_path ;

/*
 * Simple JSON parser, derived from example in Terence Parr's book,
 * _The Definitive Antlr 4 Reference_.
 */
json : jsobject | jsarray ;

jsobject
    :   LBRACE jspair (',' jspair)* RBRACE    # JsonObject
    |   LBRACE RBRACE                         # EmptyJsonObject ;
	
jsarray
    :   LBRACK jsvalue (',' jsvalue)* RBRACK  # ArrayOfJsonValues
    |   LBRACK RBRACK                         # EmptyJsonArray ;

jspair :   STRING ':' jsvalue                 # JsonPair ;

jsvalue
    :   jsobject  	# JsonObjectValue
    |   jsarray  	# JsonArrayValue
    |   STRING		# JsonAtom
    |   JSON_NUMBER	# JsonAtom
    |   'true'		# JsonAtom
    |   'false'		# JsonAtom
    |   'null'		# JsonAtom ;

/*
 * Lexical rules (start with uppercase)
 *
 * Keywords need to be case-insensitive, which makes their lexical rules a bit
 * more complicated than simple strings.
 */

/* commands */
CREATE : [Cc][Rr][Ee][Aa][Tt][Ee] ;
TABLE : [Tt][Aa][Bb][Ll][Ee] ;
TABLES : [Tt][Aa][Bb][Ll][Ee][Ss] ;
INDEX : [Ii][Nn][Dd][Ee][Xx] ;
INDEXES : [Ii][Nn][Dd][Ee][Xx][Ee][Ss] ;
FULLTEXT : [Ff][Uu][Ll][Ll][Tt][Ee][Xx][Tt] ;
ADD : [Aa][Dd][Dd] ;
DROP : [Dd][Rr][Oo][Pp] ;
ALTER : [Aa][Ll][Tt][Ee][Rr] ;
MODIFY : [Mm][Oo][Dd][Ii][Ff][Yy] ;
USER : [Uu][Ss][Ee][Rr] ;
USERS : [Uu][Ss][Ee][Rr][Ss] ;
ROLE : [Rr][Oo][Ll][Ee] ;
ROLES : [Rr][Oo][Ll][Ee][Ss] ;
GRANT : [Gg][Rr][Aa][Nn][Tt] ;
REVOKE : [Rr][Ee][Vv][Oo][Kk][Ee] ;

/* types */
ARRAY_T : [Aa][Rr][Rr][Aa][Yy] ;
BINARY_T : [Bb][Ii][Nn][Aa][Rr][Yy] ;
BOOLEAN_T : [Bb][Oo][Oo][Ll][Ee][Aa][Nn] ;
DOUBLE_T : [Dd][Oo][Uu][Bb][Ll][Ee] ;
ENUM_T : [Ee][Nn][Uu][Mm] ;
FLOAT_T : [Ff][Ll][Oo][Aa][Tt] ;
INTEGER_T : [Ii][Nn][Tt][Ee][Gg][Ee][Rr] ;
LONG_T : [Ll][Oo][Nn][Gg] ;
MAP_T : [Mm][Aa][Pp] ;
RECORD_T : [Rr][Ee][Cc][Oo][Rr][Dd] ;
STRING_T : [Ss][Tt][Rr][Ii][Nn][Gg] ;

/* additional operations and tokens */
IF_NOT_EXISTS : IF (WS)* NOT (WS)* EXISTS ;
IF_EXISTS : IF (WS)* EXISTS;
COMMENT : [Cc][Oo][Mm][Mm][Ee][Nn][Tt] ;
DEFAULT : [Dd][Ee][Ff][Aa][Uu][Ll][Tt] ;
DESC : [Dd][Ee][Ss][Cc] ;
DESCRIBE : [Dd][Ee][Ss][Cc][Rr][Ii][Bb][Ee] ;
KEY_TAG : '_' [Kk][Ee][Yy] ;
NOT_NULL : NOT (WS)* NULL ;
PRIMARY_KEY : PRIMARY (WS)* KEY ;
MIN : [Mm][Ii][Nn] ;
MAX : [Mm][Aa][Xx] ;
KEYOF : [Kk][Ee][Yy][Oo][Ff] ;
ON : [Oo][Nn] ;
SHARD : [Ss][Hh][Aa][Rr][Dd] ;
INCL : [Ii][Nn][Cc][Ll] ;
EXCL : [Ee][Xx][Cc][Ll] ;
CHECK : [Cc][Hh][Ee][Cc][Kk] ;
AND : [Aa][Nn][Dd] ;
SHOW : [Ss][Hh][Oo][Ww] ;
ELEMENT_TAG : '['']' ;
ELEMENTOF : [Ee][Ll][Ee][Mm][Ee][Nn][Tt][Oo][Ff] ;
IDENTIFIED : [Ii][Dd][Ee][Nn][Tt][Ii][Ff][Ii][Ee][Dd] ;
EXTERNALLY : [Ee][Xx][Tt][Ee][Rr][Nn][Aa][Ll][Ll][Yy] ;
BY : [Bb][Yy] ;
ADMIN : [Aa][Dd][Mm][Ii][Nn] ;
PASSWORD : [Pp][Aa][Ss][Ss][Ww][Oo][Rr][Dd] ;
LIFETIME : [Ll][Ii][Ff][Ee][Tt][Ii][Mm][Ee] ;
PASSWORD_EXPIRE : PASSWORD (WS)* EXPIRE ;
PASSWORD_LIFETIME : PASSWORD (WS)* LIFETIME ;
IDENTIFIED_EXTERNALLY : IDENTIFIED (WS)* EXTERNALLY ;
TIME_UNIT : (SECONDS | MINUTES | HOURS | DAYS) ;
RETAIN_CURRENT_PASSWORD : RETAIN (WS)* CURRENT (WS)* PASSWORD ;
CLEAR_RETAINED_PASSWORD : CLEAR (WS)* RETAINED (WS)* PASSWORD;
REPLACE : [Rr][Ee][Pp][Ll][Aa][Cc][Ee] ;
ACCOUNT : [Aa][Cc][Cc][Oo][Uu][Nn][Tt] ;
LOCK : [Ll][Oo][Cc][Kk] ;
UNLOCK : [Uu][Nn][Ll][Oo][Cc][Kk] ;
TO : [Tt][Oo] ;
FROM : [Ff][Rr][Oo][Mm] ;
ALL : [Aa][Ll][Ll] ;
PRIVILEGES : [Pp][Rr][Ii][Vv][Ii][Ll][Ee][Gg][Ee][Ss] ;
ALL_PRIVILEGES : ALL (WS)* PRIVILEGES ;

/* syntax */
END : ';';
COMMA : ',';
COLON : ':';
LP : '(';
RP : ')';
LBRACK : '[';
RBRACK : ']';
LBRACE : '{';
RBRACE : '}';
BOOLEAN_VALUE : TRUE | FALSE ;
OP : GT | GTE | LT | LTE ;

ID : ALPHA (ALPHA|DIGIT|UNDER)* ;

/* This must come after ID in the grammar */
NAME_PATH : ID ('.' SPECIAL_PATH)+ ;

/*
 * Skip whitespace, don't pass to parser.
 */
WS : (' ' | '\t' | '\r' | '\n')+ -> skip ;

/*
 * INT includes long.  It's a sequence of digits.
 */
INT : [-+]? DIGIT+ ;

/*
 * FLOAT includes double. Exponents not yet supported.
 * Float requires a decimal point in order to distinguish it from INT.
 * The grammar rules themselves must decide if they accept one or the other,
 * or both.
 *
 * Possible expression for exponents:
 *   [-+]?[0-9]*.[0-9]+([eE][-+]?[0-9]+)?
 */
FLOAT : [-+]? DIGIT* '.' DIGIT+ ;

STRING : '"' (ESC | .)*? '"' ;

/*
 * Comments.  3 styles.
 */
C_COMMENT : '/*' .*? '*/' -> skip ;

LINE_COMMENT : '//' ~[\r\n]* -> skip ;

LINE_COMMENT1 : '#' ~[\r\n]* -> skip ;

GTE : GT (WS)* EQ ;
LTE : LT (WS)* EQ ;

/*
 * A special token to catch badly-formed identifiers.
 */
BAD_ID : (DIGIT|UNDER) (ALPHA|DIGIT|UNDER)* ;

BAD_NAME_PATH : ID ('.' BAD_ID)+ ;

AS_JSON : AS (WS)* JSON ;

/*
 * Add a token that will match anything.  The resulting error will be
 * more usable this way.
 */
UnrecognizedToken : . ;

/*
 * fragments can only be used in other lexical rules and are not tokens
 */
fragment AS : [Aa][Ss] ;
fragment ALPHA : 'a'..'z'|'A'..'Z' ;
fragment DIGIT : '0'..'9' ;
fragment ESC : '\\' (["\\/bfnrt] | UNICODE) ; /* " */
fragment EXISTS : [Ee][Xx][Ii][Ss][Tt][Ss] ;
fragment FALSE : [Ff][Aa][Ll][Ss][Ee] ;
fragment HEX : [0-9a-fA-F] ;
fragment IF : [Ii][Ff] ;
fragment JDIGIT : ('0' | '1'..'9') ('0'..'9')* ;
fragment JSON : [Jj][Ss][Oo][Nn] ;
fragment KEY : [Kk][Ee][Yy] ;
fragment NOT : [Nn][Oo][Tt] ;
fragment NULL : [Nn][Uu][Ll][Ll] ;
fragment PRIMARY : [Pp][Rr][Ii][Mm][Aa][Rr][Yy] ;
fragment TRUE : [Tt][Rr][Uu][Ee] ;
fragment UNDER : '_';
fragment UNICODE : 'u' HEX HEX HEX HEX ;
fragment LT : '<' ;
fragment GT : '>' ;
fragment EQ : '=';
fragment SPECIAL_PATH : (ID | KEY_TAG | ELEMENT_TAG) ;
fragment EXPIRE : [Ee][Xx][Pp][Ii][Rr][Ee] ;
fragment SECONDS : ([Ss] | [Ss][Ee][Cc][Oo][Nn][Dd][Ss]) ;
fragment MINUTES : ([Mm] | [Mm][Ii][Nn][Uu][Tt][Ee][Ss]) ;
fragment HOURS : ([Hh] | [Hh][Oo][Uu][Rr][Ss]) ;
fragment DAYS : ([Dd] | [Dd][Aa][Yy][Ss]) ;
fragment CLEAR : [Cc][Ll][Ee][Aa][Rr] ;
fragment RETAIN : [Rr][Ee][Tt][Aa][Ii][Nn] ;
fragment RETAINED : [Rr][Ee][Tt][Aa][Ii][Nn][Ee][Dd] ;
fragment CURRENT : [Cc][Uu][Rr][Rr][Ee][Nn][Tt] ;

JSON_NUMBER
    :   '-'? JSON_INT '.' JSON_INT JSON_EXP?
    |   '-'? JSON_INT JSON_EXP
    |   '-'? JSON_INT
    ;
fragment JSON_INT : '0' | '1'..'9' '0'..'9'* ; // no leading zeros
fragment JSON_EXP : [Ee] [+\-]? JSON_INT ;
