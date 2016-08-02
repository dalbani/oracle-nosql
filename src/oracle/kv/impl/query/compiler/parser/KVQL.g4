/*-
 *
 *  This file is part of Oracle NoSQL Database
 *  Copyright (C) 2011, 2016 Oracle and/or its affiliates.  All rights reserved.
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

grammar KVQL;

/*
 * This is the starting rule for the parse.  It accepts one of the number of
 * top-level statements.  The EOF indicates that only a single statement can
 * be accepted and any extraneous input after the statement will generate an
 * error.  Allow a semicolon to terminate statements.  In the future a semicolon
 * may act as a statement separator.
 */
parse : statement EOF;

statement :
    (
    query
  | create_table_statement
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
  | show_statement) ;

/******************************************************************************
 *
 * Query expressions
 *
 ******************************************************************************/

query : prolog? sfw_expr ;

prolog : DECLARE var_decl SEMI (var_decl SEMI)*;

var_decl : var_name type_def;

var_name : DOLLAR id ;

/*
 * For now, this is not used anywhere; it's just a place-holder for the future.
 */
expr :
    sfw_expr
  | or_expr
  ;

sfw_expr :
    select_clause
    from_clause
    where_clause?
    orderby_clause? ;

from_clause : FROM name_path (AS? tab_alias)?;

tab_alias : DOLLAR? id ;

where_clause : WHERE or_expr ;

select_clause :
    SELECT hints? ( STAR | (or_expr col_alias (COMMA or_expr col_alias)*) ) ;

hints : '/*+' hint* '*/' ;

hint : ( (PREFER_INDEXES LP name_path index_name* RP) |
         (FORCE_INDEX    LP name_path index_name  RP) |
         (PREFER_PRIMARY_INDEX LP name_path RP)       |
         (FORCE_PRIMARY_INDEX  LP name_path RP)        ) STRING?;

col_alias : (AS id)? ;

orderby_clause : ORDER BY or_expr sort_spec (COMMA or_expr sort_spec)* ;

sort_spec : (ASC | DESC)? (NULLS (FIRST | LAST))? ;

or_expr : and_expr | or_expr OR and_expr ;

and_expr : comp_expr | and_expr AND comp_expr ;

comp_expr : add_expr ((comp_op | any_op) add_expr)? ;

comp_op : EQ | NEQ | GT | GTE | LT | LTE ;

any_op : (EQ_ANY) | (NEQ_ANY) | (GT_ANY) | (GTE_ANY) | (LT_ANY) | (LTE_ANY);

add_expr : multiply_expr ((PLUS | MINUS) multiply_expr)* ;

multiply_expr : unary_expr ((STAR | DIV) unary_expr)* ;

unary_expr : path_expr | (PLUS | MINUS) unary_expr ;

path_expr : primary_expr (field_step | slice_step | filter_step)* ;

field_step : DOT ( id | string | var_ref | parenthesized_expr | func_call );

slice_step : LBRACK or_expr? COLON or_expr? RBRACK ;

filter_step : LBRACK or_expr? RBRACK ;

primary_expr :
    const_expr |
    column_ref |
    var_ref |
    array_constructor |
    func_call |
    parenthesized_expr ;

/*
 * If there are 2 ids, the first one refers to a table name/alias and the
 * second to a column in that table. A single id refers to a column in some
 * of the table in the FROM clause. If more than one table has a column of
 * that name, an error is thrown. In this case, the user has to rewrite the
 * query to use table aliases to resolve the ambiguity.
 */
column_ref : id (DOT id)? ;

/*
 * INT/FLOAT literals are translated to Long/Double values.
 */
const_expr : INT | FLOAT | string | TRUE | FALSE;

var_ref : DOLLAR id? ;

/*
 * TODO: for now we cannot have a [] constructor because we would not know
*  what the element type would be.
 */
array_constructor : LBRACK or_expr (COMMA or_expr)* RBRACK ;

func_call : id LP (or_expr (COMMA or_expr)*)? RP ;

parenthesized_expr : LP or_expr RP;


/******************************************************************************
 *
 * Types
 *
 ******************************************************************************/

/*
 * All supported type definitions. The # labels on each line cause Antlr to
 * generate events specific to that type, which allows the parser code to more
 * simply discriminate among types.
 */
type_def :
    binary_def         # Binary
  | array_def          # Array
  | boolean_def        # Boolean
  | enum_def           # Enum
  | float_def          # Float
  | integer_def        # Int
  | map_def            # Map
  | record_def         # Record
  | string_def         # StringT
  ;

/*
 * A record contains one or more field definitions.
 */
record_def : RECORD_T LP field_def (COMMA field_def)* RP ;

/*
 * A definition of a named, typed field within a record.
 */
field_def : id type_def default_def? comment? ;

/*
 * The translator checks that the value conforms to the associated type.
 * Binary fields have no default value and as a result they are always nullable.
 * This is enforced in code.
 */
default_def : (default_value not_null?) | (not_null default_value?) ;

/*
 * The id alternative is used for enum defaults. The translator
 * checks that the type of the defualt value conforms with the type of
 * the field.
 */
default_value : DEFAULT (number | string | TRUE | FALSE | id) ;

not_null : NOT NULL ;

map_def : MAP_T LP type_def RP ;

array_def : ARRAY_T LP type_def RP ;

integer_def : (INTEGER_T | LONG_T) ;

float_def : (FLOAT_T | DOUBLE_T) ;

string_def : STRING_T ;

/*
 * Enumeration is defined by a list of ID values.
 *   enum (val1, val2, ...)
 */
enum_def : (ENUM_T LP id_list RP) |
           (ENUM_T LP id_list { notifyErrorListeners("Missing closing ')'"); }) ;

boolean_def : BOOLEAN_T ;

binary_def : BINARY_T (LP INT RP)? ;


/******************************************************************************
 *
 * DDL statements
 *
 ******************************************************************************/

/*
 * name_path is used for both table names and field paths. Both of these may
 * have multiple components. Table names may reference child tables using dot
 * notation and similarly, field paths may reference nested fields using
 * dot notation as well.
 */
name_path : id (DOT id)* ;

/*
 * CREATE TABLE.
 */
create_table_statement :
    CREATE TABLE (IF NOT EXISTS)? table_name comment? LP table_def RP ttl_def? ;

table_name : name_path ;
 
table_def : (field_def | key_def) (COMMA (field_def | key_def))* ;

key_def : PRIMARY KEY LP (shard_key_def COMMA?)? id_list_with_size? RP ;

shard_key_def : 
    (SHARD LP id_list_with_size RP) |
    (LP id_list_with_size { notifyErrorListeners("Missing closing ')'"); }) ;

id_list_with_size : id_with_size (COMMA id_with_size)* ;

id_with_size : id storage_size? ;

storage_size : LP INT RP ;

ttl_def : USING TTL duration ;

/*
 * ALTER TABLE 
 */
alter_table_statement : ALTER TABLE table_name alter_def ;

alter_def : alter_field_statement | ttl_def;

/*
 * Table modification -- add, drop, modify fields in an existing table.
 * This definition allows multiple changes to be contained in a single
 * alter table statement.
 */
alter_field_statement :
    LP
    (add_field_statement | drop_field_statement | modify_field_statement)
    (COMMA (add_field_statement | drop_field_statement | modify_field_statement))*
    RP ;

add_field_statement : ADD name_path type_def default_def? comment? ;

drop_field_statement : DROP name_path ;

/* not actually implemented */
modify_field_statement : MODIFY name_path type_def default_def? comment? ;

/*
 * DROP TABLE
 */
drop_table_statement : DROP TABLE (IF EXISTS)? name_path ;

/*
 * CREATE INDEX
 */
create_index_statement :
    CREATE INDEX (IF NOT EXISTS)? index_name ON table_name
    ((LP path_list RP) |
     (LP path_list { notifyErrorListeners("Missing closing ')'"); }) )
    comment?;

index_name : id ;

/*
 * A comma-separated list of field paths that may or may not reference nested
 * fields. This is used to reference fields in an index or a describe statement.
 */
path_list : complex_name_path (COMMA complex_name_path)* ;

/*
 * complex_name_path handles a basic name_path but adds KEYOF() and ELEMENTOF()
 * expressions to handle addressing in maps and arrays.
 * NOTE: if the syntax of KEYOF or VALUEOF changes the source should be checked
 * for code that reproduces these constants.
 */
complex_name_path : (name_path | keyof_expr | elementof_expr) ;

keyof_expr : KEYOF LP name_path RP ;

elementof_expr : ELEMENTOF LP name_path RP ('.' name_path)? ;

/*
 * CREATE FULLTEXT INDEX [if not exists] name ON name_path (field [ mapping ], ...)
 */
create_text_index_statement :
    CREATE FULLTEXT INDEX (IF NOT EXISTS)?
    index_name ON table_name fts_field_list es_properties? comment?;

/*
 * A list of field names, as above, which may or may not include
 * a text-search mapping specification per field.
 */
fts_field_list :
    LP fts_path_list RP |
    LP fts_path_list {notifyErrorListeners("Missing closing ')'");}
    ;

/*
 * A comma-separated list of paths to field names with optional mapping specs.
 */
fts_path_list : fts_path (COMMA fts_path)* ;

/*
 * A field name with optional mapping spec.
 */
fts_path : complex_name_path json? ;

es_properties: es_property_assignment es_property_assignment* ;

es_property_assignment: ES_SHARDS EQ INT | ES_REPLICAS EQ INT ;

/*
 * DROP INDEX [if exists] index_name ON name_path
 */
drop_index_statement : DROP INDEX (IF EXISTS)? index_name ON name_path ;

/*
 * DESC[RIBE] TABLE name_path [field_path[,field_path]]
 * DESC[RIBE] INDEX index_name ON name_path
 */
describe_statement :
    (DESCRIBE | DESC) (AS JSON)?
    (TABLE name_path (
             (LP path_list RP) |
             (LP path_list { notifyErrorListeners("Missing closing ')'"); })
           )? |
     INDEX index_name ON name_path) ;

/*
 * SHOW TABLES
 * SHOW INDEXES ON name_path
 * SHOW TABLE name_path -- lists hierarchy of the table
 */
show_statement: SHOW (AS JSON)?
        (TABLES
        | USERS
        | ROLES
        | USER identifier_or_string
        | ROLE id
        | INDEXES ON name_path
        | TABLE name_path)
  ;


/******************************************************************************
 *
 * Parse rules of security commands.
 *
 ******************************************************************************/

/*
 * CREATE USER user (IDENTIFIED BY password [PASSWORD EXPIRE]
 * [PASSWORD LIFETIME duration] | IDENTIFIED EXTERNALLY)
 * [ACCOUNT LOCK|UNLOCK] [ADMIN]
 */
create_user_statement : 
    CREATE USER create_user_identified_clause account_lock? ADMIN? ;

/*
 * CREATE ROLE role
 */
create_role_statement : CREATE ROLE id ;

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
drop_role_statement : DROP ROLE id ;

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
 * An identifier or a string
 */
identifier_or_string : (id | string);

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
create_user_identified_clause : 
    id identified_clause (PASSWORD_EXPIRE)? password_lifetime? |
    string IDENTIFIED_EXTERNALLY ;

/*
 * Rule of authentication by password.
 */
by_password : BY string;

/*
 * Rule of password lifetime definition.
 */
password_lifetime : PASSWORD LIFETIME duration;

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
grant_system_privileges : sys_priv_list TO id ;

/*
 * Subrule of granting object privileges to a role.
 */
grant_object_privileges : obj_priv_list ON object TO id ;

/*
 * Subrule of revoking roles from a user or a role.
 */
revoke_roles : id_list FROM principal ;

/*
 * Subrule of revoking system privileges from a role.
 */
revoke_system_privileges : sys_priv_list FROM id ;

/*
 * Subrule of revoking object privileges from a role.
 */
revoke_object_privileges : obj_priv_list ON object FROM id  ;

/*
 * Parsing a principal of user or role.
 */
principal : (USER identifier_or_string | ROLE id) ;

sys_priv_list : priv_item (COMMA priv_item)* ;

priv_item : (id | ALL_PRIVILEGES) ;

obj_priv_list : (priv_item | ALL) (COMMA (priv_item | ALL))* ;

/*
 * Subrule of parsing the operated object. For now, only table object is
 * available.
 */
object : name_path ;


/******************************************************************************
 *
 * Literals and identifiers
 *
 ******************************************************************************/

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

jspair :   DSTRING ':' jsvalue                 # JsonPair ;

jsvalue
    :   jsobject  	# JsonObjectValue
    |   jsarray  	# JsonArrayValue
    |   DSTRING		# JsonAtom
    |   number      # JsonAtom
    |   TRUE		# JsonAtom
    |   FALSE		# JsonAtom
    |   NULL		# JsonAtom ;


comment : COMMENT string ;

duration : INT TIME_UNIT ;

number : '-'? (FLOAT | INT) ;

string : STRING | DSTRING ;

/*
 * Identifiers
 */

id_list : id (COMMA id)* ;

id :
    (ACCOUNT | ADD | ADMIN | ALL | ALTER | AND | AS | ASC | BY | 
     COMMENT | CREATE | DECLARE | DEFAULT | DESC | DESCRIBE | DROP |
     ELEMENTOF | ES_SHARDS | ES_REPLICAS | EXISTS | FIRST | FROM | FULLTEXT |
     GRANT | IDENTIFIED | IF | INDEX | INDEXES | JSON | KEY | KEYOF |
     LIFETIME | LAST | LOCK | MODIFY | NOT | NULLS | ON | OR | ORDER |
     PASSWORD | PRIMARY | ROLE | ROLES | REVOKE |
     SELECT | SHARD | SHOW | TABLE | TABLES | TIME_UNIT | TO | TTL |
     UNLOCK | USER | USERS | USING | WHERE |
     ARRAY_T |  BINARY_T | BOOLEAN_T | DOUBLE_T | ENUM_T | FLOAT_T |
     LONG_T | INTEGER_T | MAP_T | RECORD_T | STRING_T |
     ID) |
     BAD_ID
     {
        notifyErrorListeners("Identifiers must start with a letter: " + $text);
     }
  ;


/******************************************************************************
 * Lexical rules (start with uppercase)
 *
 * Keywords need to be case-insensitive, which makes their lexical rules a bit
 * more complicated than simple strings.
 ******************************************************************************/

/* 
 * Keywords
 */

ACCOUNT : [Aa][Cc][Cc][Oo][Uu][Nn][Tt] ;

ADD : [Aa][Dd][Dd] ;

ADMIN : [Aa][Dd][Mm][Ii][Nn] ;

ALL : [Aa][Ll][Ll] ;

ALTER : [Aa][Ll][Tt][Ee][Rr] ;

AND : [Aa][Nn][Dd] ;

AS : [Aa][Ss] ;

ASC : [Aa][Ss][Cc];

BY : [Bb][Yy] ;

COMMENT : [Cc][Oo][Mm][Mm][Ee][Nn][Tt] ;

CREATE : [Cc][Rr][Ee][Aa][Tt][Ee] ;

DECLARE : [Dd][Ee][Cc][Ll][Aa][Rr][Ee] ;

DEFAULT : [Dd][Ee][Ff][Aa][Uu][Ll][Tt] ;

DESC : [Dd][Ee][Ss][Cc] ;

DESCRIBE : [Dd][Ee][Ss][Cc][Rr][Ii][Bb][Ee] ;

DROP : [Dd][Rr][Oo][Pp] ;

ELEMENTOF : [Ee][Ll][Ee][Mm][Ee][Nn][Tt][Oo][Ff] ;

ES_SHARDS : [Ee][Ss] UNDER [Ss][Hh][Aa][Rr][Dd][Ss] ;

ES_REPLICAS : [Ee][Ss] UNDER [Rr][Ee][Pp][Ll][Ii][Cc][Aa][Ss] ;

EXISTS : [Ee][Xx][Ii][Ss][Tt][Ss] ;

FIRST : [Ff][Ii][Rr][Ss][Tt] ;

FORCE_INDEX : FORCE UNDER INDEX;

FORCE_PRIMARY_INDEX : FORCE UNDER PRIMARY UNDER INDEX;

FROM : [Ff][Rr][Oo][Mm] ;

FULLTEXT : [Ff][Uu][Ll][Ll][Tt][Ee][Xx][Tt] ;

GRANT : [Gg][Rr][Aa][Nn][Tt] ;

IDENTIFIED : [Ii][Dd][Ee][Nn][Tt][Ii][Ff][Ii][Ee][Dd] ;

IF : [Ii][Ff] ;

INDEX : [Ii][Nn][Dd][Ee][Xx] ;

INDEXES : [Ii][Nn][Dd][Ee][Xx][Ee][Ss] ;

JSON : [Jj][Ss][Oo][Nn] ;

KEY : [Kk][Ee][Yy] ;

KEYOF : [Kk][Ee][Yy][Oo][Ff] ;

LAST : [Ll][Aa][Ss][Tt] ;

LIFETIME : [Ll][Ii][Ff][Ee][Tt][Ii][Mm][Ee] ;

LOCK : [Ll][Oo][Cc][Kk] ;

MODIFY : [Mm][Oo][Dd][Ii][Ff][Yy] ;

NOT : [Nn][Oo][Tt] ;

NULLS : [Nn][Uu][Ll][Ll][Ss] ;

ON : [Oo][Nn] ;

OR : [Oo][Rr] ;

ORDER : [Oo][Rr][Dd][Ee][Rr];

PASSWORD : [Pp][Aa][Ss][Ss][Ww][Oo][Rr][Dd] ;

PREFER_INDEXES: PREFER UNDER INDEXES;

PREFER_PRIMARY_INDEX : PREFER UNDER PRIMARY UNDER INDEX;

PRIMARY : [Pp][Rr][Ii][Mm][Aa][Rr][Yy] ;

REVOKE : [Rr][Ee][Vv][Oo][Kk][Ee] ;

ROLE : [Rr][Oo][Ll][Ee] ;

ROLES : [Rr][Oo][Ll][Ee][Ss] ;

SELECT : [Ss][Ee][Ll][Ee][Cc][Tt] ;

SHARD : [Ss][Hh][Aa][Rr][Dd] ;

SHOW : [Ss][Hh][Oo][Ww] ;

TABLE : [Tt][Aa][Bb][Ll][Ee] ;

TABLES : [Tt][Aa][Bb][Ll][Ee][Ss] ;

TIME_UNIT : (SECONDS | MINUTES | HOURS | DAYS) ;

TO : [Tt][Oo] ;

TTL : [Tt][Tt][Ll];

UNLOCK : [Uu][Nn][Ll][Oo][Cc][Kk] ;

USER : [Uu][Ss][Ee][Rr] ;

USERS : [Uu][Ss][Ee][Rr][Ss] ;

USING: [Uu][Ss][Ii][Nn][Gg];

WHERE : [Ww][Hh][Ee][Rr][Ee] ;

/* multi-word tokens */

ALL_PRIVILEGES : ALL WS+ PRIVILEGES ;

IDENTIFIED_EXTERNALLY : IDENTIFIED WS+ EXTERNALLY ;

PASSWORD_EXPIRE : PASSWORD WS+ EXPIRE ;

RETAIN_CURRENT_PASSWORD : RETAIN WS+ CURRENT WS+ PASSWORD ;

CLEAR_RETAINED_PASSWORD : CLEAR WS+ RETAINED WS+ PASSWORD;

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


/* 
 * Punctuation marks
 */
SEMI : ';';
COMMA : ',';
COLON : ':';
LP : '(';
RP : ')';
LBRACK : '[';
RBRACK : ']';
LBRACE : '{';
RBRACE : '}';
STAR : '*';
DOT : '.';
DOLLAR : '$';

/*
 * Operators
 */
LT : '<' ;
LTE : '<=' ;
GT : '>' ;
GTE : '>=' ;
EQ : '=';
NEQ : '!=';

LT_ANY : '<any';
LTE_ANY : '<=any';
GT_ANY : '>any';
GTE_ANY : '>=any';
EQ_ANY : '=any';
NEQ_ANY : '!=any';

PLUS : '+';
MINUS : '-';
//MULT : '*'; STAR already defined
DIV : '/';

/*
 * LITERALS
 */

NULL : [Nn][Uu][Ll][Ll];

FALSE : [Ff][Aa][Ll][Ss][Ee] ;

TRUE : [Tt][Rr][Uu][Ee] ;

INT : DIGIT+ ; // translated to Integer or Long item

FLOAT : ( DIGIT* '.' DIGIT+ ([Ee] [+-]? DIGIT+)? ) |
        ( DIGIT+ [Ee] [+-]? DIGIT+ ) ;

DSTRING : '"' (DSTR_ESC | .)*? '"' ;

STRING : '\'' (ESC | .)*? '\'' ;

/*
 * Identifiers (MUST come after all the keywords and literals defined above)
 */

ID : ALPHA (ALPHA | DIGIT | UNDER)* ;

/* A special token to catch badly-formed identifiers. */
BAD_ID : (DIGIT | UNDER) (ALPHA | DIGIT | UNDER)* ;


/*
 * Skip whitespace, don't pass to parser.
 */
WS : (' ' | '\t' | '\r' | '\n')+ -> skip ;

/* 
 * Comments.  3 styles.
 */
C_COMMENT : '/*' ~[+] .*? '*/' -> skip ;

LINE_COMMENT : '//' ~[\r\n]* -> skip ;

LINE_COMMENT1 : '#' ~[\r\n]* -> skip ;

/*
 * Add a token that will match anything.  The resulting error will be
 * more usable this way.
 */
UnrecognizedToken : . ;

/*
 * fragments can only be used in other lexical rules and are not tokens
 */

fragment ALPHA : 'a'..'z'|'A'..'Z' ;

fragment DIGIT : '0'..'9' ;

fragment DSTR_ESC : '\\' (["\\/bfnrt] | UNICODE) ; /* " */

fragment ESC : '\\' ([\'\\/bfnrt] | UNICODE) ;

fragment HEX : [0-9a-fA-F] ;

fragment UNDER : '_';

fragment UNICODE : 'u' HEX HEX HEX HEX ;

fragment SECONDS : ([Ss] | [Ss][Ee][Cc][Oo][Nn][Dd][Ss]) ;

fragment MINUTES : ([Mm] | [Mm][Ii][Nn][Uu][Tt][Ee][Ss]) ;

fragment HOURS : ([Hh] | [Hh][Oo][Uu][Rr][Ss]) ;

fragment DAYS : ([Dd] | [Dd][Aa][Yy][Ss]) ;

fragment CLEAR : [Cc][Ll][Ee][Aa][Rr] ;

fragment CURRENT : [Cc][Uu][Rr][Rr][Ee][Nn][Tt] ;

fragment EXPIRE : [Ee][Xx][Pp][Ii][Rr][Ee] ;

fragment EXTERNALLY : [Ee][Xx][Tt][Ee][Rr][Nn][Aa][Ll][Ll][Yy] ;

fragment FORCE : [Ff][Oo][Rr][Cc][Ee] ;

fragment PREFER : [Pp][Rr][Ee][Ff][Ee][Rr] ;

fragment PRIVILEGES : [Pp][Rr][Ii][Vv][Ii][Ll][Ee][Gg][Ee][Ss] ;

fragment RETAIN : [Rr][Ee][Tt][Aa][Ii][Nn] ;

fragment RETAINED : [Rr][Ee][Tt][Aa][Ii][Nn][Ee][Dd] ;

