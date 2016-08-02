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
// Generated from Table.g by ANTLR 4.4
package oracle.kv.impl.api.table.query;
import java.util.List;

import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.RuntimeMetaData;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class TableParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.4", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__3=1, T__2=2, T__1=3, T__0=4, CREATE=5, TABLE=6, TABLES=7, INDEX=8, 
		INDEXES=9, FULLTEXT=10, ADD=11, DROP=12, ALTER=13, MODIFY=14, USER=15, 
		USERS=16, ROLE=17, ROLES=18, GRANT=19, REVOKE=20, ARRAY_T=21, BINARY_T=22, 
		BOOLEAN_T=23, DOUBLE_T=24, ENUM_T=25, FLOAT_T=26, INTEGER_T=27, LONG_T=28, 
		MAP_T=29, RECORD_T=30, STRING_T=31, IF_NOT_EXISTS=32, IF_EXISTS=33, COMMENT=34, 
		DEFAULT=35, DESC=36, DESCRIBE=37, KEY_TAG=38, NOT_NULL=39, PRIMARY_KEY=40, 
		MIN=41, MAX=42, KEYOF=43, ON=44, SHARD=45, INCL=46, EXCL=47, CHECK=48, 
		AND=49, SHOW=50, ELEMENT_TAG=51, ELEMENTOF=52, IDENTIFIED=53, EXTERNALLY=54, 
		BY=55, ADMIN=56, PASSWORD=57, LIFETIME=58, PASSWORD_EXPIRE=59, PASSWORD_LIFETIME=60, 
		IDENTIFIED_EXTERNALLY=61, TIME_UNIT=62, RETAIN_CURRENT_PASSWORD=63, CLEAR_RETAINED_PASSWORD=64, 
		REPLACE=65, ACCOUNT=66, LOCK=67, UNLOCK=68, TO=69, FROM=70, ALL=71, PRIVILEGES=72, 
		ALL_PRIVILEGES=73, END=74, COMMA=75, COLON=76, LP=77, RP=78, LBRACK=79, 
		RBRACK=80, LBRACE=81, RBRACE=82, BOOLEAN_VALUE=83, OP=84, ID=85, NAME_PATH=86, 
		WS=87, INT=88, FLOAT=89, STRING=90, C_COMMENT=91, LINE_COMMENT=92, LINE_COMMENT1=93, 
		GTE=94, LTE=95, BAD_ID=96, BAD_NAME_PATH=97, AS_JSON=98, UnrecognizedToken=99, 
		JSON_NUMBER=100;
	public static final String[] tokenNames = {
		"<INVALID>", "'null'", "'true'", "'false'", "'.'", "CREATE", "TABLE", 
		"TABLES", "INDEX", "INDEXES", "FULLTEXT", "ADD", "DROP", "ALTER", "MODIFY", 
		"USER", "USERS", "ROLE", "ROLES", "GRANT", "REVOKE", "ARRAY_T", "BINARY_T", 
		"BOOLEAN_T", "DOUBLE_T", "ENUM_T", "FLOAT_T", "INTEGER_T", "LONG_T", "MAP_T", 
		"RECORD_T", "STRING_T", "IF_NOT_EXISTS", "IF_EXISTS", "COMMENT", "DEFAULT", 
		"DESC", "DESCRIBE", "KEY_TAG", "NOT_NULL", "PRIMARY_KEY", "MIN", "MAX", 
		"KEYOF", "ON", "SHARD", "INCL", "EXCL", "CHECK", "AND", "SHOW", "ELEMENT_TAG", 
		"ELEMENTOF", "IDENTIFIED", "EXTERNALLY", "BY", "ADMIN", "PASSWORD", "LIFETIME", 
		"PASSWORD_EXPIRE", "PASSWORD_LIFETIME", "IDENTIFIED_EXTERNALLY", "TIME_UNIT", 
		"RETAIN_CURRENT_PASSWORD", "CLEAR_RETAINED_PASSWORD", "REPLACE", "ACCOUNT", 
		"LOCK", "UNLOCK", "TO", "FROM", "ALL", "PRIVILEGES", "ALL_PRIVILEGES", 
		"';'", "','", "':'", "'('", "')'", "'['", "']'", "'{'", "'}'", "BOOLEAN_VALUE", 
		"OP", "ID", "NAME_PATH", "WS", "INT", "FLOAT", "STRING", "C_COMMENT", 
		"LINE_COMMENT", "LINE_COMMENT1", "GTE", "LTE", "BAD_ID", "BAD_NAME_PATH", 
		"AS_JSON", "UnrecognizedToken", "JSON_NUMBER"
	};
	public static final int
		RULE_parse = 0, RULE_create_table_statement = 1, RULE_drop_table_statement = 2, 
		RULE_create_index_statement = 3, RULE_drop_index_statement = 4, RULE_create_text_index_statement = 5, 
		RULE_alter_table_statement = 6, RULE_describe_statement = 7, RULE_show_statement = 8, 
		RULE_alter_field_statement = 9, RULE_add_field_statement = 10, RULE_drop_field_statement = 11, 
		RULE_modify_field_statement = 12, RULE_table_def = 13, RULE_key_def = 14, 
		RULE_shard_key_def = 15, RULE_id_list_with_paren = 16, RULE_id_list = 17, 
		RULE_complex_field_list = 18, RULE_path_list = 19, RULE_fts_field_list = 20, 
		RULE_fts_path_list = 21, RULE_fts_path = 22, RULE_string_list = 23, RULE_simple_field_list = 24, 
		RULE_field_def = 25, RULE_type_def = 26, RULE_integer_def = 27, RULE_float_def = 28, 
		RULE_string_def = 29, RULE_boolean_def = 30, RULE_enum_def = 31, RULE_binary_def = 32, 
		RULE_record_def = 33, RULE_map_def = 34, RULE_array_def = 35, RULE_comment = 36, 
		RULE_boolean_constraint = 37, RULE_integer_constraint = 38, RULE_integer_default = 39, 
		RULE_float_constraint = 40, RULE_float_default = 41, RULE_string_constraint = 42, 
		RULE_string_default = 43, RULE_enum_constraint = 44, RULE_check_expression = 45, 
		RULE_expr = 46, RULE_not_null = 47, RULE_name_path = 48, RULE_complex_name_path = 49, 
		RULE_keyof_expr = 50, RULE_elementof_expr = 51, RULE_index_name = 52, 
		RULE_create_user_statement = 53, RULE_create_role_statement = 54, RULE_alter_user_statement = 55, 
		RULE_drop_user_statement = 56, RULE_drop_role_statement = 57, RULE_grant_statement = 58, 
		RULE_revoke_statement = 59, RULE_extended_id = 60, RULE_identifier = 61, 
		RULE_identifier_or_string = 62, RULE_identified_clause = 63, RULE_create_user_identified_clause = 64, 
		RULE_by_password = 65, RULE_password_lifetime = 66, RULE_duration = 67, 
		RULE_reset_password_clause = 68, RULE_account_lock = 69, RULE_grant_roles = 70, 
		RULE_grant_system_privileges = 71, RULE_grant_object_privileges = 72, 
		RULE_revoke_roles = 73, RULE_revoke_system_privileges = 74, RULE_revoke_object_privileges = 75, 
		RULE_principal = 76, RULE_sys_priv_list = 77, RULE_priv_item = 78, RULE_obj_priv_list = 79, 
		RULE_object = 80, RULE_json = 81, RULE_jsobject = 82, RULE_jsarray = 83, 
		RULE_jspair = 84, RULE_jsvalue = 85;
	public static final String[] ruleNames = {
		"parse", "create_table_statement", "drop_table_statement", "create_index_statement", 
		"drop_index_statement", "create_text_index_statement", "alter_table_statement", 
		"describe_statement", "show_statement", "alter_field_statement", "add_field_statement", 
		"drop_field_statement", "modify_field_statement", "table_def", "key_def", 
		"shard_key_def", "id_list_with_paren", "id_list", "complex_field_list", 
		"path_list", "fts_field_list", "fts_path_list", "fts_path", "string_list", 
		"simple_field_list", "field_def", "type_def", "integer_def", "float_def", 
		"string_def", "boolean_def", "enum_def", "binary_def", "record_def", "map_def", 
		"array_def", "comment", "boolean_constraint", "integer_constraint", "integer_default", 
		"float_constraint", "float_default", "string_constraint", "string_default", 
		"enum_constraint", "check_expression", "expr", "not_null", "name_path", 
		"complex_name_path", "keyof_expr", "elementof_expr", "index_name", "create_user_statement", 
		"create_role_statement", "alter_user_statement", "drop_user_statement", 
		"drop_role_statement", "grant_statement", "revoke_statement", "extended_id", 
		"identifier", "identifier_or_string", "identified_clause", "create_user_identified_clause", 
		"by_password", "password_lifetime", "duration", "reset_password_clause", 
		"account_lock", "grant_roles", "grant_system_privileges", "grant_object_privileges", 
		"revoke_roles", "revoke_system_privileges", "revoke_object_privileges", 
		"principal", "sys_priv_list", "priv_item", "obj_priv_list", "object", 
		"json", "jsobject", "jsarray", "jspair", "jsvalue"
	};

	@Override
	public String getGrammarFileName() { return "Table.g"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public TableParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class ParseContext extends ParserRuleContext {
		public Create_text_index_statementContext create_text_index_statement() {
			return getRuleContext(Create_text_index_statementContext.class,0);
		}
		public Grant_statementContext grant_statement() {
			return getRuleContext(Grant_statementContext.class,0);
		}
		public Revoke_statementContext revoke_statement() {
			return getRuleContext(Revoke_statementContext.class,0);
		}
		public Drop_index_statementContext drop_index_statement() {
			return getRuleContext(Drop_index_statementContext.class,0);
		}
		public Alter_table_statementContext alter_table_statement() {
			return getRuleContext(Alter_table_statementContext.class,0);
		}
		public Create_table_statementContext create_table_statement() {
			return getRuleContext(Create_table_statementContext.class,0);
		}
		public Drop_role_statementContext drop_role_statement() {
			return getRuleContext(Drop_role_statementContext.class,0);
		}
		public Drop_table_statementContext drop_table_statement() {
			return getRuleContext(Drop_table_statementContext.class,0);
		}
		public Alter_user_statementContext alter_user_statement() {
			return getRuleContext(Alter_user_statementContext.class,0);
		}
		public Create_role_statementContext create_role_statement() {
			return getRuleContext(Create_role_statementContext.class,0);
		}
		public TerminalNode EOF() { return getToken(TableParser.EOF, 0); }
		public Describe_statementContext describe_statement() {
			return getRuleContext(Describe_statementContext.class,0);
		}
		public Create_index_statementContext create_index_statement() {
			return getRuleContext(Create_index_statementContext.class,0);
		}
		public Drop_user_statementContext drop_user_statement() {
			return getRuleContext(Drop_user_statementContext.class,0);
		}
		public Create_user_statementContext create_user_statement() {
			return getRuleContext(Create_user_statementContext.class,0);
		}
		public Show_statementContext show_statement() {
			return getRuleContext(Show_statementContext.class,0);
		}
		public ParseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parse; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterParse(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitParse(this);
		}
	}

	public final ParseContext parse() throws RecognitionException {
		ParseContext _localctx = new ParseContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_parse);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(187);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				{
				setState(172); create_table_statement();
				}
				break;
			case 2:
				{
				setState(173); create_index_statement();
				}
				break;
			case 3:
				{
				setState(174); create_user_statement();
				}
				break;
			case 4:
				{
				setState(175); create_role_statement();
				}
				break;
			case 5:
				{
				setState(176); drop_index_statement();
				}
				break;
			case 6:
				{
				setState(177); create_text_index_statement();
				}
				break;
			case 7:
				{
				setState(178); drop_role_statement();
				}
				break;
			case 8:
				{
				setState(179); drop_user_statement();
				}
				break;
			case 9:
				{
				setState(180); alter_table_statement();
				}
				break;
			case 10:
				{
				setState(181); alter_user_statement();
				}
				break;
			case 11:
				{
				setState(182); drop_table_statement();
				}
				break;
			case 12:
				{
				setState(183); grant_statement();
				}
				break;
			case 13:
				{
				setState(184); revoke_statement();
				}
				break;
			case 14:
				{
				setState(185); describe_statement();
				}
				break;
			case 15:
				{
				setState(186); show_statement();
				}
				break;
			}
			setState(189); match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Create_table_statementContext extends ParserRuleContext {
		public TerminalNode RP() { return getToken(TableParser.RP, 0); }
		public TerminalNode IF_NOT_EXISTS() { return getToken(TableParser.IF_NOT_EXISTS, 0); }
		public Name_pathContext name_path() {
			return getRuleContext(Name_pathContext.class,0);
		}
		public TerminalNode LP() { return getToken(TableParser.LP, 0); }
		public Table_defContext table_def() {
			return getRuleContext(Table_defContext.class,0);
		}
		public TerminalNode CREATE() { return getToken(TableParser.CREATE, 0); }
		public TerminalNode TABLE() { return getToken(TableParser.TABLE, 0); }
		public Create_table_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_table_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterCreate_table_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitCreate_table_statement(this);
		}
	}

	public final Create_table_statementContext create_table_statement() throws RecognitionException {
		Create_table_statementContext _localctx = new Create_table_statementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_create_table_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(191); match(CREATE);
			setState(192); match(TABLE);
			setState(194);
			_la = _input.LA(1);
			if (_la==IF_NOT_EXISTS) {
				{
				setState(193); match(IF_NOT_EXISTS);
				}
			}

			setState(196); name_path();
			setState(197); match(LP);
			setState(198); table_def();
			setState(199); match(RP);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Drop_table_statementContext extends ParserRuleContext {
		public Name_pathContext name_path() {
			return getRuleContext(Name_pathContext.class,0);
		}
		public TerminalNode DROP() { return getToken(TableParser.DROP, 0); }
		public TerminalNode TABLE() { return getToken(TableParser.TABLE, 0); }
		public TerminalNode IF_EXISTS() { return getToken(TableParser.IF_EXISTS, 0); }
		public Drop_table_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_table_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterDrop_table_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitDrop_table_statement(this);
		}
	}

	public final Drop_table_statementContext drop_table_statement() throws RecognitionException {
		Drop_table_statementContext _localctx = new Drop_table_statementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_drop_table_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(201); match(DROP);
			setState(202); match(TABLE);
			setState(204);
			_la = _input.LA(1);
			if (_la==IF_EXISTS) {
				{
				setState(203); match(IF_EXISTS);
				}
			}

			setState(206); name_path();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Create_index_statementContext extends ParserRuleContext {
		public TerminalNode INDEX() { return getToken(TableParser.INDEX, 0); }
		public TerminalNode ON() { return getToken(TableParser.ON, 0); }
		public CommentContext comment() {
			return getRuleContext(CommentContext.class,0);
		}
		public TerminalNode IF_NOT_EXISTS() { return getToken(TableParser.IF_NOT_EXISTS, 0); }
		public Name_pathContext name_path() {
			return getRuleContext(Name_pathContext.class,0);
		}
		public Index_nameContext index_name() {
			return getRuleContext(Index_nameContext.class,0);
		}
		public Complex_field_listContext complex_field_list() {
			return getRuleContext(Complex_field_listContext.class,0);
		}
		public TerminalNode CREATE() { return getToken(TableParser.CREATE, 0); }
		public Create_index_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_index_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterCreate_index_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitCreate_index_statement(this);
		}
	}

	public final Create_index_statementContext create_index_statement() throws RecognitionException {
		Create_index_statementContext _localctx = new Create_index_statementContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_create_index_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(208); match(CREATE);
			setState(209); match(INDEX);
			setState(211);
			_la = _input.LA(1);
			if (_la==IF_NOT_EXISTS) {
				{
				setState(210); match(IF_NOT_EXISTS);
				}
			}

			setState(213); index_name();
			setState(214); match(ON);
			setState(215); name_path();
			setState(216); complex_field_list();
			setState(218);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(217); comment();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Drop_index_statementContext extends ParserRuleContext {
		public TerminalNode INDEX() { return getToken(TableParser.INDEX, 0); }
		public TerminalNode ON() { return getToken(TableParser.ON, 0); }
		public Name_pathContext name_path() {
			return getRuleContext(Name_pathContext.class,0);
		}
		public Index_nameContext index_name() {
			return getRuleContext(Index_nameContext.class,0);
		}
		public TerminalNode DROP() { return getToken(TableParser.DROP, 0); }
		public TerminalNode IF_EXISTS() { return getToken(TableParser.IF_EXISTS, 0); }
		public Drop_index_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_index_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterDrop_index_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitDrop_index_statement(this);
		}
	}

	public final Drop_index_statementContext drop_index_statement() throws RecognitionException {
		Drop_index_statementContext _localctx = new Drop_index_statementContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_drop_index_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(220); match(DROP);
			setState(221); match(INDEX);
			setState(223);
			_la = _input.LA(1);
			if (_la==IF_EXISTS) {
				{
				setState(222); match(IF_EXISTS);
				}
			}

			setState(225); index_name();
			setState(226); match(ON);
			setState(227); name_path();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Create_text_index_statementContext extends ParserRuleContext {
		public TerminalNode INDEX() { return getToken(TableParser.INDEX, 0); }
		public TerminalNode ON() { return getToken(TableParser.ON, 0); }
		public CommentContext comment() {
			return getRuleContext(CommentContext.class,0);
		}
		public TerminalNode IF_NOT_EXISTS() { return getToken(TableParser.IF_NOT_EXISTS, 0); }
		public Name_pathContext name_path() {
			return getRuleContext(Name_pathContext.class,0);
		}
		public Index_nameContext index_name() {
			return getRuleContext(Index_nameContext.class,0);
		}
		public Fts_field_listContext fts_field_list() {
			return getRuleContext(Fts_field_listContext.class,0);
		}
		public TerminalNode CREATE() { return getToken(TableParser.CREATE, 0); }
		public TerminalNode FULLTEXT() { return getToken(TableParser.FULLTEXT, 0); }
		public Create_text_index_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_text_index_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterCreate_text_index_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitCreate_text_index_statement(this);
		}
	}

	public final Create_text_index_statementContext create_text_index_statement() throws RecognitionException {
		Create_text_index_statementContext _localctx = new Create_text_index_statementContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_create_text_index_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(229); match(CREATE);
			setState(230); match(FULLTEXT);
			setState(231); match(INDEX);
			setState(233);
			_la = _input.LA(1);
			if (_la==IF_NOT_EXISTS) {
				{
				setState(232); match(IF_NOT_EXISTS);
				}
			}

			setState(235); index_name();
			setState(236); match(ON);
			setState(237); name_path();
			setState(238); fts_field_list();
			setState(240);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(239); comment();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Alter_table_statementContext extends ParserRuleContext {
		public Alter_field_statementContext alter_field_statement() {
			return getRuleContext(Alter_field_statementContext.class,0);
		}
		public Name_pathContext name_path() {
			return getRuleContext(Name_pathContext.class,0);
		}
		public TerminalNode ALTER() { return getToken(TableParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(TableParser.TABLE, 0); }
		public Alter_table_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alter_table_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterAlter_table_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitAlter_table_statement(this);
		}
	}

	public final Alter_table_statementContext alter_table_statement() throws RecognitionException {
		Alter_table_statementContext _localctx = new Alter_table_statementContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_alter_table_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(242); match(ALTER);
			setState(243); match(TABLE);
			setState(244); name_path();
			setState(245); alter_field_statement();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Describe_statementContext extends ParserRuleContext {
		public TerminalNode INDEX() { return getToken(TableParser.INDEX, 0); }
		public TerminalNode AS_JSON() { return getToken(TableParser.AS_JSON, 0); }
		public TerminalNode ON() { return getToken(TableParser.ON, 0); }
		public Name_pathContext name_path() {
			return getRuleContext(Name_pathContext.class,0);
		}
		public Index_nameContext index_name() {
			return getRuleContext(Index_nameContext.class,0);
		}
		public TerminalNode DESCRIBE() { return getToken(TableParser.DESCRIBE, 0); }
		public Complex_field_listContext complex_field_list() {
			return getRuleContext(Complex_field_listContext.class,0);
		}
		public TerminalNode DESC() { return getToken(TableParser.DESC, 0); }
		public TerminalNode TABLE() { return getToken(TableParser.TABLE, 0); }
		public Describe_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_describe_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterDescribe_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitDescribe_statement(this);
		}
	}

	public final Describe_statementContext describe_statement() throws RecognitionException {
		Describe_statementContext _localctx = new Describe_statementContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_describe_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(247);
			_la = _input.LA(1);
			if ( !(_la==DESC || _la==DESCRIBE) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			setState(249);
			_la = _input.LA(1);
			if (_la==AS_JSON) {
				{
				setState(248); match(AS_JSON);
				}
			}

			setState(261);
			switch (_input.LA(1)) {
			case TABLE:
				{
				setState(251); match(TABLE);
				setState(252); name_path();
				setState(254);
				_la = _input.LA(1);
				if (_la==LP) {
					{
					setState(253); complex_field_list();
					}
				}

				}
				break;
			case INDEX:
				{
				setState(256); match(INDEX);
				setState(257); index_name();
				setState(258); match(ON);
				setState(259); name_path();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Show_statementContext extends ParserRuleContext {
		public TerminalNode SHOW() { return getToken(TableParser.SHOW, 0); }
		public TerminalNode ON() { return getToken(TableParser.ON, 0); }
		public Name_pathContext name_path() {
			return getRuleContext(Name_pathContext.class,0);
		}
		public TerminalNode TABLES() { return getToken(TableParser.TABLES, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode USER() { return getToken(TableParser.USER, 0); }
		public TerminalNode TABLE() { return getToken(TableParser.TABLE, 0); }
		public TerminalNode INDEXES() { return getToken(TableParser.INDEXES, 0); }
		public TerminalNode ROLES() { return getToken(TableParser.ROLES, 0); }
		public TerminalNode AS_JSON() { return getToken(TableParser.AS_JSON, 0); }
		public Identifier_or_stringContext identifier_or_string() {
			return getRuleContext(Identifier_or_stringContext.class,0);
		}
		public TerminalNode USERS() { return getToken(TableParser.USERS, 0); }
		public TerminalNode ROLE() { return getToken(TableParser.ROLE, 0); }
		public Show_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_show_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterShow_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitShow_statement(this);
		}
	}

	public final Show_statementContext show_statement() throws RecognitionException {
		Show_statementContext _localctx = new Show_statementContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_show_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(263); match(SHOW);
			setState(265);
			_la = _input.LA(1);
			if (_la==AS_JSON) {
				{
				setState(264); match(AS_JSON);
				}
			}

			setState(279);
			switch (_input.LA(1)) {
			case TABLES:
				{
				setState(267); match(TABLES);
				}
				break;
			case USERS:
				{
				setState(268); match(USERS);
				}
				break;
			case ROLES:
				{
				setState(269); match(ROLES);
				}
				break;
			case USER:
				{
				setState(270); match(USER);
				setState(271); identifier_or_string();
				}
				break;
			case ROLE:
				{
				setState(272); match(ROLE);
				setState(273); identifier();
				}
				break;
			case INDEXES:
				{
				setState(274); match(INDEXES);
				setState(275); match(ON);
				setState(276); name_path();
				}
				break;
			case TABLE:
				{
				setState(277); match(TABLE);
				setState(278); name_path();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Alter_field_statementContext extends ParserRuleContext {
		public TerminalNode RP() { return getToken(TableParser.RP, 0); }
		public List<Modify_field_statementContext> modify_field_statement() {
			return getRuleContexts(Modify_field_statementContext.class);
		}
		public Add_field_statementContext add_field_statement(int i) {
			return getRuleContext(Add_field_statementContext.class,i);
		}
		public TerminalNode LP() { return getToken(TableParser.LP, 0); }
		public Drop_field_statementContext drop_field_statement(int i) {
			return getRuleContext(Drop_field_statementContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(TableParser.COMMA); }
		public Modify_field_statementContext modify_field_statement(int i) {
			return getRuleContext(Modify_field_statementContext.class,i);
		}
		public List<Add_field_statementContext> add_field_statement() {
			return getRuleContexts(Add_field_statementContext.class);
		}
		public List<Drop_field_statementContext> drop_field_statement() {
			return getRuleContexts(Drop_field_statementContext.class);
		}
		public TerminalNode COMMA(int i) {
			return getToken(TableParser.COMMA, i);
		}
		public Alter_field_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alter_field_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterAlter_field_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitAlter_field_statement(this);
		}
	}

	public final Alter_field_statementContext alter_field_statement() throws RecognitionException {
		Alter_field_statementContext _localctx = new Alter_field_statementContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_alter_field_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(281); match(LP);
			setState(285);
			switch (_input.LA(1)) {
			case ADD:
				{
				setState(282); add_field_statement();
				}
				break;
			case DROP:
				{
				setState(283); drop_field_statement();
				}
				break;
			case MODIFY:
				{
				setState(284); modify_field_statement();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(295);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(287); match(COMMA);
				setState(291);
				switch (_input.LA(1)) {
				case ADD:
					{
					setState(288); add_field_statement();
					}
					break;
				case DROP:
					{
					setState(289); drop_field_statement();
					}
					break;
				case MODIFY:
					{
					setState(290); modify_field_statement();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				}
				setState(297);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(298); match(RP);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Add_field_statementContext extends ParserRuleContext {
		public Field_defContext field_def() {
			return getRuleContext(Field_defContext.class,0);
		}
		public TerminalNode ADD() { return getToken(TableParser.ADD, 0); }
		public Add_field_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_add_field_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterAdd_field_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitAdd_field_statement(this);
		}
	}

	public final Add_field_statementContext add_field_statement() throws RecognitionException {
		Add_field_statementContext _localctx = new Add_field_statementContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_add_field_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(300); match(ADD);
			setState(301); field_def();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Drop_field_statementContext extends ParserRuleContext {
		public Name_pathContext name_path() {
			return getRuleContext(Name_pathContext.class,0);
		}
		public TerminalNode DROP() { return getToken(TableParser.DROP, 0); }
		public Drop_field_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_field_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterDrop_field_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitDrop_field_statement(this);
		}
	}

	public final Drop_field_statementContext drop_field_statement() throws RecognitionException {
		Drop_field_statementContext _localctx = new Drop_field_statementContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_drop_field_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(303); match(DROP);
			setState(304); name_path();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Modify_field_statementContext extends ParserRuleContext {
		public Field_defContext field_def() {
			return getRuleContext(Field_defContext.class,0);
		}
		public TerminalNode MODIFY() { return getToken(TableParser.MODIFY, 0); }
		public Modify_field_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_modify_field_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterModify_field_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitModify_field_statement(this);
		}
	}

	public final Modify_field_statementContext modify_field_statement() throws RecognitionException {
		Modify_field_statementContext _localctx = new Modify_field_statementContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_modify_field_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(306); match(MODIFY);
			setState(307); field_def();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Table_defContext extends ParserRuleContext {
		public List<Field_defContext> field_def() {
			return getRuleContexts(Field_defContext.class);
		}
		public List<CommentContext> comment() {
			return getRuleContexts(CommentContext.class);
		}
		public CommentContext comment(int i) {
			return getRuleContext(CommentContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(TableParser.COMMA); }
		public Key_defContext key_def(int i) {
			return getRuleContext(Key_defContext.class,i);
		}
		public List<Key_defContext> key_def() {
			return getRuleContexts(Key_defContext.class);
		}
		public Field_defContext field_def(int i) {
			return getRuleContext(Field_defContext.class,i);
		}
		public TerminalNode COMMA(int i) {
			return getToken(TableParser.COMMA, i);
		}
		public Table_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterTable_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitTable_def(this);
		}
	}

	public final Table_defContext table_def() throws RecognitionException {
		Table_defContext _localctx = new Table_defContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_table_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(312);
			switch (_input.LA(1)) {
			case USERS:
			case ROLE:
			case ROLES:
			case ADMIN:
			case PASSWORD:
			case LIFETIME:
			case TIME_UNIT:
			case ACCOUNT:
			case ID:
			case NAME_PATH:
			case BAD_ID:
			case BAD_NAME_PATH:
				{
				setState(309); field_def();
				}
				break;
			case PRIMARY_KEY:
				{
				setState(310); key_def();
				}
				break;
			case COMMENT:
				{
				setState(311); comment();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(322);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(314); match(COMMA);
				setState(318);
				switch (_input.LA(1)) {
				case USERS:
				case ROLE:
				case ROLES:
				case ADMIN:
				case PASSWORD:
				case LIFETIME:
				case TIME_UNIT:
				case ACCOUNT:
				case ID:
				case NAME_PATH:
				case BAD_ID:
				case BAD_NAME_PATH:
					{
					setState(315); field_def();
					}
					break;
				case PRIMARY_KEY:
					{
					setState(316); key_def();
					}
					break;
				case COMMENT:
					{
					setState(317); comment();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				}
				setState(324);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Key_defContext extends ParserRuleContext {
		public TerminalNode PRIMARY_KEY() { return getToken(TableParser.PRIMARY_KEY, 0); }
		public TerminalNode RP() { return getToken(TableParser.RP, 0); }
		public Shard_key_defContext shard_key_def() {
			return getRuleContext(Shard_key_defContext.class,0);
		}
		public Id_listContext id_list() {
			return getRuleContext(Id_listContext.class,0);
		}
		public TerminalNode LP() { return getToken(TableParser.LP, 0); }
		public TerminalNode COMMA() { return getToken(TableParser.COMMA, 0); }
		public Key_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_key_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterKey_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitKey_def(this);
		}
	}

	public final Key_defContext key_def() throws RecognitionException {
		Key_defContext _localctx = new Key_defContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_key_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(325); match(PRIMARY_KEY);
			setState(326); match(LP);
			setState(331);
			_la = _input.LA(1);
			if (_la==SHARD) {
				{
				setState(327); shard_key_def();
				setState(329);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(328); match(COMMA);
					}
				}

				}
			}

			setState(334);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << USERS) | (1L << ROLE) | (1L << ROLES) | (1L << ADMIN) | (1L << PASSWORD) | (1L << LIFETIME) | (1L << TIME_UNIT))) != 0) || _la==ACCOUNT || _la==ID) {
				{
				setState(333); id_list();
				}
			}

			setState(336); match(RP);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Shard_key_defContext extends ParserRuleContext {
		public TerminalNode SHARD() { return getToken(TableParser.SHARD, 0); }
		public Simple_field_listContext simple_field_list() {
			return getRuleContext(Simple_field_listContext.class,0);
		}
		public Shard_key_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_shard_key_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterShard_key_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitShard_key_def(this);
		}
	}

	public final Shard_key_defContext shard_key_def() throws RecognitionException {
		Shard_key_defContext _localctx = new Shard_key_defContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_shard_key_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(338); match(SHARD);
			setState(339); simple_field_list();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Id_list_with_parenContext extends ParserRuleContext {
		public TerminalNode RP() { return getToken(TableParser.RP, 0); }
		public List<TerminalNode> ID() { return getTokens(TableParser.ID); }
		public Id_listContext id_list() {
			return getRuleContext(Id_listContext.class,0);
		}
		public List<TerminalNode> BAD_ID() { return getTokens(TableParser.BAD_ID); }
		public TerminalNode LP() { return getToken(TableParser.LP, 0); }
		public List<TerminalNode> COMMA() { return getTokens(TableParser.COMMA); }
		public TerminalNode BAD_ID(int i) {
			return getToken(TableParser.BAD_ID, i);
		}
		public TerminalNode ID(int i) {
			return getToken(TableParser.ID, i);
		}
		public TerminalNode COMMA(int i) {
			return getToken(TableParser.COMMA, i);
		}
		public Id_list_with_parenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_id_list_with_paren; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterId_list_with_paren(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitId_list_with_paren(this);
		}
	}

	public final Id_list_with_parenContext id_list_with_paren() throws RecognitionException {
		Id_list_with_parenContext _localctx = new Id_list_with_parenContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_id_list_with_paren);
		int _la;
		try {
			int _alt;
			setState(359);
			switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(341); match(LP);
				setState(342); id_list();
				setState(343); match(RP);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(345); match(LP);
				setState(346); id_list();
				notifyErrorListeners("Missing closing ')'");
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(349); match(LP);
				setState(350);
				_la = _input.LA(1);
				if ( !(_la==ID || _la==BAD_ID) ) {
				_errHandler.recoverInline(this);
				}
				consume();
				setState(355);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(351); match(COMMA);
						setState(352);
						_la = _input.LA(1);
						if ( !(_la==ID || _la==BAD_ID) ) {
						_errHandler.recoverInline(this);
						}
						consume();
						}
						} 
					}
					setState(357);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
				}
				notifyErrorListeners("Identifiers must start with a letter: " +
				                _input.getText(_localctx.start, _input.LT(-1)));
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Id_listContext extends ParserRuleContext {
		public Extended_idContext extended_id(int i) {
			return getRuleContext(Extended_idContext.class,i);
		}
		public List<Extended_idContext> extended_id() {
			return getRuleContexts(Extended_idContext.class);
		}
		public List<TerminalNode> COMMA() { return getTokens(TableParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(TableParser.COMMA, i);
		}
		public Id_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_id_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterId_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitId_list(this);
		}
	}

	public final Id_listContext id_list() throws RecognitionException {
		Id_listContext _localctx = new Id_listContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_id_list);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(361); extended_id();
			setState(366);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,24,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(362); match(COMMA);
					setState(363); extended_id();
					}
					} 
				}
				setState(368);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,24,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Complex_field_listContext extends ParserRuleContext {
		public TerminalNode RP() { return getToken(TableParser.RP, 0); }
		public Path_listContext path_list() {
			return getRuleContext(Path_listContext.class,0);
		}
		public TerminalNode LP() { return getToken(TableParser.LP, 0); }
		public Complex_field_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_complex_field_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterComplex_field_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitComplex_field_list(this);
		}
	}

	public final Complex_field_listContext complex_field_list() throws RecognitionException {
		Complex_field_listContext _localctx = new Complex_field_listContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_complex_field_list);
		try {
			setState(377);
			switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(369); match(LP);
				setState(370); path_list();
				setState(371); match(RP);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(373); match(LP);
				setState(374); path_list();
				notifyErrorListeners("Missing closing ')'");
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Path_listContext extends ParserRuleContext {
		public Complex_name_pathContext complex_name_path(int i) {
			return getRuleContext(Complex_name_pathContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(TableParser.COMMA); }
		public List<Complex_name_pathContext> complex_name_path() {
			return getRuleContexts(Complex_name_pathContext.class);
		}
		public TerminalNode COMMA(int i) {
			return getToken(TableParser.COMMA, i);
		}
		public Path_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_path_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterPath_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitPath_list(this);
		}
	}

	public final Path_listContext path_list() throws RecognitionException {
		Path_listContext _localctx = new Path_listContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_path_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(379); complex_name_path();
			setState(384);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(380); match(COMMA);
				setState(381); complex_name_path();
				}
				}
				setState(386);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Fts_field_listContext extends ParserRuleContext {
		public TerminalNode RP() { return getToken(TableParser.RP, 0); }
		public TerminalNode LP() { return getToken(TableParser.LP, 0); }
		public Fts_path_listContext fts_path_list() {
			return getRuleContext(Fts_path_listContext.class,0);
		}
		public Fts_field_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fts_field_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterFts_field_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitFts_field_list(this);
		}
	}

	public final Fts_field_listContext fts_field_list() throws RecognitionException {
		Fts_field_listContext _localctx = new Fts_field_listContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_fts_field_list);
		try {
			setState(395);
			switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(387); match(LP);
				setState(388); fts_path_list();
				setState(389); match(RP);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(391); match(LP);
				setState(392); fts_path_list();
				notifyErrorListeners("Missing closing ')'");
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Fts_path_listContext extends ParserRuleContext {
		public Fts_pathContext fts_path(int i) {
			return getRuleContext(Fts_pathContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(TableParser.COMMA); }
		public List<Fts_pathContext> fts_path() {
			return getRuleContexts(Fts_pathContext.class);
		}
		public TerminalNode COMMA(int i) {
			return getToken(TableParser.COMMA, i);
		}
		public Fts_path_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fts_path_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterFts_path_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitFts_path_list(this);
		}
	}

	public final Fts_path_listContext fts_path_list() throws RecognitionException {
		Fts_path_listContext _localctx = new Fts_path_listContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_fts_path_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(397); fts_path();
			setState(402);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(398); match(COMMA);
				setState(399); fts_path();
				}
				}
				setState(404);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Fts_pathContext extends ParserRuleContext {
		public JsonContext json() {
			return getRuleContext(JsonContext.class,0);
		}
		public Complex_name_pathContext complex_name_path() {
			return getRuleContext(Complex_name_pathContext.class,0);
		}
		public Fts_pathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fts_path; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterFts_path(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitFts_path(this);
		}
	}

	public final Fts_pathContext fts_path() throws RecognitionException {
		Fts_pathContext _localctx = new Fts_pathContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_fts_path);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(405); complex_name_path();
			setState(407);
			_la = _input.LA(1);
			if (_la==LBRACK || _la==LBRACE) {
				{
				setState(406); json();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class String_listContext extends ParserRuleContext {
		public TerminalNode RP() { return getToken(TableParser.RP, 0); }
		public TerminalNode STRING(int i) {
			return getToken(TableParser.STRING, i);
		}
		public TerminalNode LP() { return getToken(TableParser.LP, 0); }
		public List<TerminalNode> COMMA() { return getTokens(TableParser.COMMA); }
		public List<TerminalNode> STRING() { return getTokens(TableParser.STRING); }
		public TerminalNode COMMA(int i) {
			return getToken(TableParser.COMMA, i);
		}
		public String_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_string_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterString_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitString_list(this);
		}
	}

	public final String_listContext string_list() throws RecognitionException {
		String_listContext _localctx = new String_listContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_string_list);
		int _la;
		try {
			setState(429);
			switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(409); match(LP);
				setState(410); match(STRING);
				setState(415);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(411); match(COMMA);
					setState(412); match(STRING);
					}
					}
					setState(417);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(418); match(RP);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(419); match(LP);
				setState(420); match(STRING);
				setState(425);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(421); match(COMMA);
					setState(422); match(STRING);
					}
					}
					setState(427);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				notifyErrorListeners("Missing closing ')'");
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Simple_field_listContext extends ParserRuleContext {
		public Id_list_with_parenContext id_list_with_paren() {
			return getRuleContext(Id_list_with_parenContext.class,0);
		}
		public Simple_field_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simple_field_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterSimple_field_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitSimple_field_list(this);
		}
	}

	public final Simple_field_listContext simple_field_list() throws RecognitionException {
		Simple_field_listContext _localctx = new Simple_field_listContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_simple_field_list);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(431); id_list_with_paren();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Field_defContext extends ParserRuleContext {
		public Name_pathContext name_path() {
			return getRuleContext(Name_pathContext.class,0);
		}
		public Type_defContext type_def() {
			return getRuleContext(Type_defContext.class,0);
		}
		public Field_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_field_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterField_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitField_def(this);
		}
	}

	public final Field_defContext field_def() throws RecognitionException {
		Field_defContext _localctx = new Field_defContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_field_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(433); name_path();
			setState(434); type_def();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Type_defContext extends ParserRuleContext {
		public Type_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type_def; }
	 
		public Type_defContext() { }
		public void copyFrom(Type_defContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ArrayContext extends Type_defContext {
		public Array_defContext array_def() {
			return getRuleContext(Array_defContext.class,0);
		}
		public ArrayContext(Type_defContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterArray(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitArray(this);
		}
	}
	public static class EnumContext extends Type_defContext {
		public Enum_defContext enum_def() {
			return getRuleContext(Enum_defContext.class,0);
		}
		public EnumContext(Type_defContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterEnum(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitEnum(this);
		}
	}
	public static class FloatContext extends Type_defContext {
		public Float_defContext float_def() {
			return getRuleContext(Float_defContext.class,0);
		}
		public FloatContext(Type_defContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterFloat(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitFloat(this);
		}
	}
	public static class RecordContext extends Type_defContext {
		public Record_defContext record_def() {
			return getRuleContext(Record_defContext.class,0);
		}
		public RecordContext(Type_defContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterRecord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitRecord(this);
		}
	}
	public static class BinaryContext extends Type_defContext {
		public Binary_defContext binary_def() {
			return getRuleContext(Binary_defContext.class,0);
		}
		public BinaryContext(Type_defContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterBinary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitBinary(this);
		}
	}
	public static class StringContext extends Type_defContext {
		public String_defContext string_def() {
			return getRuleContext(String_defContext.class,0);
		}
		public StringContext(Type_defContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitString(this);
		}
	}
	public static class BooleanContext extends Type_defContext {
		public Boolean_defContext boolean_def() {
			return getRuleContext(Boolean_defContext.class,0);
		}
		public BooleanContext(Type_defContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterBoolean(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitBoolean(this);
		}
	}
	public static class MapContext extends Type_defContext {
		public Map_defContext map_def() {
			return getRuleContext(Map_defContext.class,0);
		}
		public MapContext(Type_defContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterMap(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitMap(this);
		}
	}
	public static class IntContext extends Type_defContext {
		public Integer_defContext integer_def() {
			return getRuleContext(Integer_defContext.class,0);
		}
		public IntContext(Type_defContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterInt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitInt(this);
		}
	}

	public final Type_defContext type_def() throws RecognitionException {
		Type_defContext _localctx = new Type_defContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_type_def);
		try {
			setState(445);
			switch (_input.LA(1)) {
			case BINARY_T:
				_localctx = new BinaryContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(436); binary_def();
				}
				break;
			case ARRAY_T:
				_localctx = new ArrayContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(437); array_def();
				}
				break;
			case BOOLEAN_T:
				_localctx = new BooleanContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(438); boolean_def();
				}
				break;
			case ENUM_T:
				_localctx = new EnumContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(439); enum_def();
				}
				break;
			case DOUBLE_T:
			case FLOAT_T:
				_localctx = new FloatContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(440); float_def();
				}
				break;
			case INTEGER_T:
			case LONG_T:
				_localctx = new IntContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(441); integer_def();
				}
				break;
			case MAP_T:
				_localctx = new MapContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(442); map_def();
				}
				break;
			case RECORD_T:
				_localctx = new RecordContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(443); record_def();
				}
				break;
			case STRING_T:
				_localctx = new StringContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(444); string_def();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Integer_defContext extends ParserRuleContext {
		public List<Integer_constraintContext> integer_constraint() {
			return getRuleContexts(Integer_constraintContext.class);
		}
		public CommentContext comment() {
			return getRuleContext(CommentContext.class,0);
		}
		public TerminalNode LONG_T() { return getToken(TableParser.LONG_T, 0); }
		public Integer_constraintContext integer_constraint(int i) {
			return getRuleContext(Integer_constraintContext.class,i);
		}
		public TerminalNode INTEGER_T() { return getToken(TableParser.INTEGER_T, 0); }
		public Integer_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integer_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterInteger_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitInteger_def(this);
		}
	}

	public final Integer_defContext integer_def() throws RecognitionException {
		Integer_defContext _localctx = new Integer_defContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_integer_def);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(447);
			_la = _input.LA(1);
			if ( !(_la==INTEGER_T || _la==LONG_T) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			setState(451);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,34,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(448); integer_constraint();
					}
					} 
				}
				setState(453);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,34,_ctx);
			}
			setState(455);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(454); comment();
				}
			}

			setState(460);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DEFAULT) | (1L << NOT_NULL) | (1L << CHECK))) != 0)) {
				{
				{
				setState(457); integer_constraint();
				}
				}
				setState(462);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Float_defContext extends ParserRuleContext {
		public TerminalNode DOUBLE_T() { return getToken(TableParser.DOUBLE_T, 0); }
		public CommentContext comment() {
			return getRuleContext(CommentContext.class,0);
		}
		public Float_constraintContext float_constraint(int i) {
			return getRuleContext(Float_constraintContext.class,i);
		}
		public TerminalNode FLOAT_T() { return getToken(TableParser.FLOAT_T, 0); }
		public List<Float_constraintContext> float_constraint() {
			return getRuleContexts(Float_constraintContext.class);
		}
		public Float_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_float_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterFloat_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitFloat_def(this);
		}
	}

	public final Float_defContext float_def() throws RecognitionException {
		Float_defContext _localctx = new Float_defContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_float_def);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(463);
			_la = _input.LA(1);
			if ( !(_la==DOUBLE_T || _la==FLOAT_T) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			setState(467);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,37,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(464); float_constraint();
					}
					} 
				}
				setState(469);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,37,_ctx);
			}
			setState(471);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(470); comment();
				}
			}

			setState(476);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DEFAULT) | (1L << NOT_NULL) | (1L << CHECK))) != 0)) {
				{
				{
				setState(473); float_constraint();
				}
				}
				setState(478);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class String_defContext extends ParserRuleContext {
		public String_constraintContext string_constraint(int i) {
			return getRuleContext(String_constraintContext.class,i);
		}
		public CommentContext comment() {
			return getRuleContext(CommentContext.class,0);
		}
		public TerminalNode STRING_T() { return getToken(TableParser.STRING_T, 0); }
		public List<String_constraintContext> string_constraint() {
			return getRuleContexts(String_constraintContext.class);
		}
		public String_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_string_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterString_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitString_def(this);
		}
	}

	public final String_defContext string_def() throws RecognitionException {
		String_defContext _localctx = new String_defContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_string_def);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(479); match(STRING_T);
			setState(483);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,40,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(480); string_constraint();
					}
					} 
				}
				setState(485);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,40,_ctx);
			}
			setState(487);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(486); comment();
				}
			}

			setState(492);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DEFAULT) | (1L << NOT_NULL) | (1L << CHECK))) != 0)) {
				{
				{
				setState(489); string_constraint();
				}
				}
				setState(494);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Boolean_defContext extends ParserRuleContext {
		public CommentContext comment() {
			return getRuleContext(CommentContext.class,0);
		}
		public List<Boolean_constraintContext> boolean_constraint() {
			return getRuleContexts(Boolean_constraintContext.class);
		}
		public Boolean_constraintContext boolean_constraint(int i) {
			return getRuleContext(Boolean_constraintContext.class,i);
		}
		public TerminalNode BOOLEAN_T() { return getToken(TableParser.BOOLEAN_T, 0); }
		public Boolean_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_boolean_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterBoolean_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitBoolean_def(this);
		}
	}

	public final Boolean_defContext boolean_def() throws RecognitionException {
		Boolean_defContext _localctx = new Boolean_defContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_boolean_def);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(495); match(BOOLEAN_T);
			setState(499);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,43,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(496); boolean_constraint();
					}
					} 
				}
				setState(501);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,43,_ctx);
			}
			setState(503);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(502); comment();
				}
			}

			setState(508);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DEFAULT || _la==NOT_NULL) {
				{
				{
				setState(505); boolean_constraint();
				}
				}
				setState(510);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Enum_defContext extends ParserRuleContext {
		public CommentContext comment() {
			return getRuleContext(CommentContext.class,0);
		}
		public List<Enum_constraintContext> enum_constraint() {
			return getRuleContexts(Enum_constraintContext.class);
		}
		public Enum_constraintContext enum_constraint(int i) {
			return getRuleContext(Enum_constraintContext.class,i);
		}
		public Id_list_with_parenContext id_list_with_paren() {
			return getRuleContext(Id_list_with_parenContext.class,0);
		}
		public TerminalNode ENUM_T() { return getToken(TableParser.ENUM_T, 0); }
		public Enum_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enum_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterEnum_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitEnum_def(this);
		}
	}

	public final Enum_defContext enum_def() throws RecognitionException {
		Enum_defContext _localctx = new Enum_defContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_enum_def);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(511); match(ENUM_T);
			setState(512); id_list_with_paren();
			setState(516);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,46,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(513); enum_constraint();
					}
					} 
				}
				setState(518);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,46,_ctx);
			}
			setState(520);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(519); comment();
				}
			}

			setState(525);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DEFAULT || _la==NOT_NULL) {
				{
				{
				setState(522); enum_constraint();
				}
				}
				setState(527);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Binary_defContext extends ParserRuleContext {
		public TerminalNode RP() { return getToken(TableParser.RP, 0); }
		public CommentContext comment() {
			return getRuleContext(CommentContext.class,0);
		}
		public TerminalNode WS(int i) {
			return getToken(TableParser.WS, i);
		}
		public TerminalNode BINARY_T() { return getToken(TableParser.BINARY_T, 0); }
		public TerminalNode LP() { return getToken(TableParser.LP, 0); }
		public List<TerminalNode> WS() { return getTokens(TableParser.WS); }
		public TerminalNode INT() { return getToken(TableParser.INT, 0); }
		public Binary_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_binary_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterBinary_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitBinary_def(this);
		}
	}

	public final Binary_defContext binary_def() throws RecognitionException {
		Binary_defContext _localctx = new Binary_defContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_binary_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(528); match(BINARY_T);
			setState(544);
			_la = _input.LA(1);
			if (_la==LP) {
				{
				setState(529); match(LP);
				setState(533);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WS) {
					{
					{
					setState(530); match(WS);
					}
					}
					setState(535);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(536); match(INT);
				setState(540);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WS) {
					{
					{
					setState(537); match(WS);
					}
					}
					setState(542);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(543); match(RP);
				}
			}

			setState(547);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(546); comment();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Record_defContext extends ParserRuleContext {
		public List<Field_defContext> field_def() {
			return getRuleContexts(Field_defContext.class);
		}
		public TerminalNode RP() { return getToken(TableParser.RP, 0); }
		public CommentContext comment() {
			return getRuleContext(CommentContext.class,0);
		}
		public TerminalNode LP() { return getToken(TableParser.LP, 0); }
		public List<TerminalNode> COMMA() { return getTokens(TableParser.COMMA); }
		public TerminalNode RECORD_T() { return getToken(TableParser.RECORD_T, 0); }
		public Field_defContext field_def(int i) {
			return getRuleContext(Field_defContext.class,i);
		}
		public TerminalNode COMMA(int i) {
			return getToken(TableParser.COMMA, i);
		}
		public Record_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_record_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterRecord_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitRecord_def(this);
		}
	}

	public final Record_defContext record_def() throws RecognitionException {
		Record_defContext _localctx = new Record_defContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_record_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(549); match(RECORD_T);
			setState(550); match(LP);
			setState(551); field_def();
			setState(556);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(552); match(COMMA);
				setState(553); field_def();
				}
				}
				setState(558);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(559); match(RP);
			setState(561);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(560); comment();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Map_defContext extends ParserRuleContext {
		public TerminalNode RP() { return getToken(TableParser.RP, 0); }
		public CommentContext comment() {
			return getRuleContext(CommentContext.class,0);
		}
		public TerminalNode LP() { return getToken(TableParser.LP, 0); }
		public TerminalNode MAP_T() { return getToken(TableParser.MAP_T, 0); }
		public Type_defContext type_def() {
			return getRuleContext(Type_defContext.class,0);
		}
		public Map_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_map_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterMap_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitMap_def(this);
		}
	}

	public final Map_defContext map_def() throws RecognitionException {
		Map_defContext _localctx = new Map_defContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_map_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(563); match(MAP_T);
			setState(564); match(LP);
			setState(565); type_def();
			setState(566); match(RP);
			setState(568);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(567); comment();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Array_defContext extends ParserRuleContext {
		public TerminalNode RP() { return getToken(TableParser.RP, 0); }
		public CommentContext comment() {
			return getRuleContext(CommentContext.class,0);
		}
		public TerminalNode LP() { return getToken(TableParser.LP, 0); }
		public TerminalNode ARRAY_T() { return getToken(TableParser.ARRAY_T, 0); }
		public Type_defContext type_def() {
			return getRuleContext(Type_defContext.class,0);
		}
		public Array_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_array_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterArray_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitArray_def(this);
		}
	}

	public final Array_defContext array_def() throws RecognitionException {
		Array_defContext _localctx = new Array_defContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_array_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(570); match(ARRAY_T);
			setState(571); match(LP);
			setState(572); type_def();
			setState(573); match(RP);
			setState(575);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(574); comment();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CommentContext extends ParserRuleContext {
		public TerminalNode COMMENT() { return getToken(TableParser.COMMENT, 0); }
		public TerminalNode STRING() { return getToken(TableParser.STRING, 0); }
		public CommentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterComment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitComment(this);
		}
	}

	public final CommentContext comment() throws RecognitionException {
		CommentContext _localctx = new CommentContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_comment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(577); match(COMMENT);
			setState(578); match(STRING);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Boolean_constraintContext extends ParserRuleContext {
		public Not_nullContext not_null() {
			return getRuleContext(Not_nullContext.class,0);
		}
		public TerminalNode BOOLEAN_VALUE() { return getToken(TableParser.BOOLEAN_VALUE, 0); }
		public TerminalNode DEFAULT() { return getToken(TableParser.DEFAULT, 0); }
		public Boolean_constraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_boolean_constraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterBoolean_constraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitBoolean_constraint(this);
		}
	}

	public final Boolean_constraintContext boolean_constraint() throws RecognitionException {
		Boolean_constraintContext _localctx = new Boolean_constraintContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_boolean_constraint);
		try {
			setState(583);
			switch (_input.LA(1)) {
			case DEFAULT:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(580); match(DEFAULT);
				setState(581); match(BOOLEAN_VALUE);
				}
				}
				break;
			case NOT_NULL:
				enterOuterAlt(_localctx, 2);
				{
				setState(582); not_null();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Integer_constraintContext extends ParserRuleContext {
		public Not_nullContext not_null() {
			return getRuleContext(Not_nullContext.class,0);
		}
		public Check_expressionContext check_expression() {
			return getRuleContext(Check_expressionContext.class,0);
		}
		public Integer_defaultContext integer_default() {
			return getRuleContext(Integer_defaultContext.class,0);
		}
		public Integer_constraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integer_constraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterInteger_constraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitInteger_constraint(this);
		}
	}

	public final Integer_constraintContext integer_constraint() throws RecognitionException {
		Integer_constraintContext _localctx = new Integer_constraintContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_integer_constraint);
		try {
			setState(588);
			switch (_input.LA(1)) {
			case NOT_NULL:
				enterOuterAlt(_localctx, 1);
				{
				setState(585); not_null();
				}
				break;
			case DEFAULT:
				enterOuterAlt(_localctx, 2);
				{
				setState(586); integer_default();
				}
				break;
			case CHECK:
				enterOuterAlt(_localctx, 3);
				{
				setState(587); check_expression();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Integer_defaultContext extends ParserRuleContext {
		public TerminalNode DEFAULT() { return getToken(TableParser.DEFAULT, 0); }
		public TerminalNode INT() { return getToken(TableParser.INT, 0); }
		public Integer_defaultContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integer_default; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterInteger_default(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitInteger_default(this);
		}
	}

	public final Integer_defaultContext integer_default() throws RecognitionException {
		Integer_defaultContext _localctx = new Integer_defaultContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_integer_default);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(590); match(DEFAULT);
			setState(591); match(INT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Float_constraintContext extends ParserRuleContext {
		public Not_nullContext not_null() {
			return getRuleContext(Not_nullContext.class,0);
		}
		public Check_expressionContext check_expression() {
			return getRuleContext(Check_expressionContext.class,0);
		}
		public Float_defaultContext float_default() {
			return getRuleContext(Float_defaultContext.class,0);
		}
		public Float_constraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_float_constraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterFloat_constraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitFloat_constraint(this);
		}
	}

	public final Float_constraintContext float_constraint() throws RecognitionException {
		Float_constraintContext _localctx = new Float_constraintContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_float_constraint);
		try {
			setState(596);
			switch (_input.LA(1)) {
			case NOT_NULL:
				enterOuterAlt(_localctx, 1);
				{
				setState(593); not_null();
				}
				break;
			case DEFAULT:
				enterOuterAlt(_localctx, 2);
				{
				setState(594); float_default();
				}
				break;
			case CHECK:
				enterOuterAlt(_localctx, 3);
				{
				setState(595); check_expression();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Float_defaultContext extends ParserRuleContext {
		public TerminalNode DEFAULT() { return getToken(TableParser.DEFAULT, 0); }
		public TerminalNode INT() { return getToken(TableParser.INT, 0); }
		public TerminalNode FLOAT() { return getToken(TableParser.FLOAT, 0); }
		public Float_defaultContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_float_default; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterFloat_default(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitFloat_default(this);
		}
	}

	public final Float_defaultContext float_default() throws RecognitionException {
		Float_defaultContext _localctx = new Float_defaultContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_float_default);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(598); match(DEFAULT);
			setState(599);
			_la = _input.LA(1);
			if ( !(_la==INT || _la==FLOAT) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class String_constraintContext extends ParserRuleContext {
		public Not_nullContext not_null() {
			return getRuleContext(Not_nullContext.class,0);
		}
		public Check_expressionContext check_expression() {
			return getRuleContext(Check_expressionContext.class,0);
		}
		public String_defaultContext string_default() {
			return getRuleContext(String_defaultContext.class,0);
		}
		public String_constraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_string_constraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterString_constraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitString_constraint(this);
		}
	}

	public final String_constraintContext string_constraint() throws RecognitionException {
		String_constraintContext _localctx = new String_constraintContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_string_constraint);
		try {
			setState(604);
			switch (_input.LA(1)) {
			case NOT_NULL:
				enterOuterAlt(_localctx, 1);
				{
				setState(601); not_null();
				}
				break;
			case DEFAULT:
				enterOuterAlt(_localctx, 2);
				{
				setState(602); string_default();
				}
				break;
			case CHECK:
				enterOuterAlt(_localctx, 3);
				{
				setState(603); check_expression();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class String_defaultContext extends ParserRuleContext {
		public Extended_idContext extended_id;
		public Extended_idContext extended_id() {
			return getRuleContext(Extended_idContext.class,0);
		}
		public TerminalNode STRING() { return getToken(TableParser.STRING, 0); }
		public TerminalNode DEFAULT() { return getToken(TableParser.DEFAULT, 0); }
		public String_defaultContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_string_default; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterString_default(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitString_default(this);
		}
	}

	public final String_defaultContext string_default() throws RecognitionException {
		String_defaultContext _localctx = new String_defaultContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_string_default);
		try {
			setState(612);
			switch ( getInterpreter().adaptivePredict(_input,61,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(606); match(DEFAULT);
				setState(607); match(STRING);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(608); match(DEFAULT);
				setState(609); ((String_defaultContext)_localctx).extended_id = extended_id();
				notifyErrorListeners
				            ("String default value must be quoted: " + (((String_defaultContext)_localctx).extended_id!=null?_input.getText(((String_defaultContext)_localctx).extended_id.start,((String_defaultContext)_localctx).extended_id.stop):null));
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Enum_constraintContext extends ParserRuleContext {
		public Token STRING;
		public Not_nullContext not_null() {
			return getRuleContext(Not_nullContext.class,0);
		}
		public Extended_idContext extended_id() {
			return getRuleContext(Extended_idContext.class,0);
		}
		public TerminalNode STRING() { return getToken(TableParser.STRING, 0); }
		public TerminalNode DEFAULT() { return getToken(TableParser.DEFAULT, 0); }
		public Enum_constraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enum_constraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterEnum_constraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitEnum_constraint(this);
		}
	}

	public final Enum_constraintContext enum_constraint() throws RecognitionException {
		Enum_constraintContext _localctx = new Enum_constraintContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_enum_constraint);
		try {
			setState(620);
			switch ( getInterpreter().adaptivePredict(_input,62,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(614); not_null();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(615); match(DEFAULT);
				setState(616); extended_id();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(617); match(DEFAULT);
				setState(618); ((Enum_constraintContext)_localctx).STRING = match(STRING);
				notifyErrorListeners
				            ("Enum default value should not be quoted: " + (((Enum_constraintContext)_localctx).STRING!=null?((Enum_constraintContext)_localctx).STRING.getText():null));
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Check_expressionContext extends ParserRuleContext {
		public TerminalNode CHECK() { return getToken(TableParser.CHECK, 0); }
		public TerminalNode RP() { return getToken(TableParser.RP, 0); }
		public TerminalNode AND(int i) {
			return getToken(TableParser.AND, i);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public TerminalNode LP() { return getToken(TableParser.LP, 0); }
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public List<TerminalNode> AND() { return getTokens(TableParser.AND); }
		public Check_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_check_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterCheck_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitCheck_expression(this);
		}
	}

	public final Check_expressionContext check_expression() throws RecognitionException {
		Check_expressionContext _localctx = new Check_expressionContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_check_expression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(622); match(CHECK);
			setState(623); match(LP);
			setState(624); expr();
			setState(629);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AND) {
				{
				{
				setState(625); match(AND);
				setState(626); expr();
				}
				}
				setState(631);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(632); match(RP);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExprContext extends ParserRuleContext {
		public Extended_idContext extended_id() {
			return getRuleContext(Extended_idContext.class,0);
		}
		public TerminalNode OP() { return getToken(TableParser.OP, 0); }
		public TerminalNode STRING() { return getToken(TableParser.STRING, 0); }
		public Elementof_exprContext elementof_expr() {
			return getRuleContext(Elementof_exprContext.class,0);
		}
		public TerminalNode INT() { return getToken(TableParser.INT, 0); }
		public TerminalNode FLOAT() { return getToken(TableParser.FLOAT, 0); }
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitExpr(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		ExprContext _localctx = new ExprContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_expr);
		int _la;
		try {
			setState(642);
			switch (_input.LA(1)) {
			case USERS:
			case ROLE:
			case ROLES:
			case ADMIN:
			case PASSWORD:
			case LIFETIME:
			case TIME_UNIT:
			case ACCOUNT:
			case ID:
				enterOuterAlt(_localctx, 1);
				{
				setState(634); extended_id();
				setState(635); match(OP);
				setState(636);
				_la = _input.LA(1);
				if ( !(((((_la - 88)) & ~0x3f) == 0 && ((1L << (_la - 88)) & ((1L << (INT - 88)) | (1L << (FLOAT - 88)) | (1L << (STRING - 88)))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				consume();
				}
				break;
			case ELEMENTOF:
				enterOuterAlt(_localctx, 2);
				{
				setState(638); elementof_expr();
				setState(639); match(OP);
				setState(640);
				_la = _input.LA(1);
				if ( !(((((_la - 88)) & ~0x3f) == 0 && ((1L << (_la - 88)) & ((1L << (INT - 88)) | (1L << (FLOAT - 88)) | (1L << (STRING - 88)))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				consume();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Not_nullContext extends ParserRuleContext {
		public TerminalNode NOT_NULL() { return getToken(TableParser.NOT_NULL, 0); }
		public Not_nullContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_not_null; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterNot_null(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitNot_null(this);
		}
	}

	public final Not_nullContext not_null() throws RecognitionException {
		Not_nullContext _localctx = new Not_nullContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_not_null);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(644); match(NOT_NULL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Name_pathContext extends ParserRuleContext {
		public Extended_idContext extended_id() {
			return getRuleContext(Extended_idContext.class,0);
		}
		public TerminalNode BAD_ID() { return getToken(TableParser.BAD_ID, 0); }
		public TerminalNode NAME_PATH() { return getToken(TableParser.NAME_PATH, 0); }
		public TerminalNode BAD_NAME_PATH() { return getToken(TableParser.BAD_NAME_PATH, 0); }
		public Name_pathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_name_path; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterName_path(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitName_path(this);
		}
	}

	public final Name_pathContext name_path() throws RecognitionException {
		Name_pathContext _localctx = new Name_pathContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_name_path);
		try {
			setState(654);
			switch (_input.LA(1)) {
			case USERS:
			case ROLE:
			case ROLES:
			case ADMIN:
			case PASSWORD:
			case LIFETIME:
			case TIME_UNIT:
			case ACCOUNT:
			case ID:
			case NAME_PATH:
				enterOuterAlt(_localctx, 1);
				{
				setState(648);
				switch (_input.LA(1)) {
				case USERS:
				case ROLE:
				case ROLES:
				case ADMIN:
				case PASSWORD:
				case LIFETIME:
				case TIME_UNIT:
				case ACCOUNT:
				case ID:
					{
					setState(646); extended_id();
					}
					break;
				case NAME_PATH:
					{
					setState(647); match(NAME_PATH);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case BAD_ID:
				enterOuterAlt(_localctx, 2);
				{
				setState(650); match(BAD_ID);
				notifyErrorListeners
				            ("Field and table names must start with a letter: " + _input.getText(_localctx.start, _input.LT(-1)));
				}
				break;
			case BAD_NAME_PATH:
				enterOuterAlt(_localctx, 3);
				{
				setState(652); match(BAD_NAME_PATH);
				notifyErrorListeners
				            ("Field and table names must start with a letter: " + _input.getText(_localctx.start, _input.LT(-1)));
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Complex_name_pathContext extends ParserRuleContext {
		public Keyof_exprContext keyof_expr() {
			return getRuleContext(Keyof_exprContext.class,0);
		}
		public Name_pathContext name_path() {
			return getRuleContext(Name_pathContext.class,0);
		}
		public Elementof_exprContext elementof_expr() {
			return getRuleContext(Elementof_exprContext.class,0);
		}
		public Complex_name_pathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_complex_name_path; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterComplex_name_path(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitComplex_name_path(this);
		}
	}

	public final Complex_name_pathContext complex_name_path() throws RecognitionException {
		Complex_name_pathContext _localctx = new Complex_name_pathContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_complex_name_path);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(659);
			switch (_input.LA(1)) {
			case USERS:
			case ROLE:
			case ROLES:
			case ADMIN:
			case PASSWORD:
			case LIFETIME:
			case TIME_UNIT:
			case ACCOUNT:
			case ID:
			case NAME_PATH:
			case BAD_ID:
			case BAD_NAME_PATH:
				{
				setState(656); name_path();
				}
				break;
			case KEYOF:
				{
				setState(657); keyof_expr();
				}
				break;
			case ELEMENTOF:
				{
				setState(658); elementof_expr();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Keyof_exprContext extends ParserRuleContext {
		public TerminalNode RP() { return getToken(TableParser.RP, 0); }
		public Name_pathContext name_path() {
			return getRuleContext(Name_pathContext.class,0);
		}
		public TerminalNode KEYOF() { return getToken(TableParser.KEYOF, 0); }
		public TerminalNode LP() { return getToken(TableParser.LP, 0); }
		public Keyof_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyof_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterKeyof_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitKeyof_expr(this);
		}
	}

	public final Keyof_exprContext keyof_expr() throws RecognitionException {
		Keyof_exprContext _localctx = new Keyof_exprContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_keyof_expr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(661); match(KEYOF);
			setState(662); match(LP);
			setState(663); name_path();
			setState(664); match(RP);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Elementof_exprContext extends ParserRuleContext {
		public TerminalNode ELEMENTOF() { return getToken(TableParser.ELEMENTOF, 0); }
		public TerminalNode RP() { return getToken(TableParser.RP, 0); }
		public List<Name_pathContext> name_path() {
			return getRuleContexts(Name_pathContext.class);
		}
		public TerminalNode LP() { return getToken(TableParser.LP, 0); }
		public Name_pathContext name_path(int i) {
			return getRuleContext(Name_pathContext.class,i);
		}
		public Elementof_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elementof_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterElementof_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitElementof_expr(this);
		}
	}

	public final Elementof_exprContext elementof_expr() throws RecognitionException {
		Elementof_exprContext _localctx = new Elementof_exprContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_elementof_expr);
		int _la;
		try {
			setState(677);
			switch ( getInterpreter().adaptivePredict(_input,69,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(666); match(ELEMENTOF);
				setState(667); match(LP);
				setState(668); name_path();
				setState(669); match(RP);
				setState(672);
				_la = _input.LA(1);
				if (_la==T__0) {
					{
					setState(670); match(T__0);
					setState(671); name_path();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(674); match(ELEMENTOF);
				setState(675); match(LP);
				setState(676); match(RP);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Index_nameContext extends ParserRuleContext {
		public Extended_idContext extended_id() {
			return getRuleContext(Extended_idContext.class,0);
		}
		public TerminalNode BAD_ID() { return getToken(TableParser.BAD_ID, 0); }
		public Index_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_index_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterIndex_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitIndex_name(this);
		}
	}

	public final Index_nameContext index_name() throws RecognitionException {
		Index_nameContext _localctx = new Index_nameContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_index_name);
		try {
			setState(682);
			switch (_input.LA(1)) {
			case USERS:
			case ROLE:
			case ROLES:
			case ADMIN:
			case PASSWORD:
			case LIFETIME:
			case TIME_UNIT:
			case ACCOUNT:
			case ID:
				enterOuterAlt(_localctx, 1);
				{
				setState(679); extended_id();
				}
				break;
			case BAD_ID:
				enterOuterAlt(_localctx, 2);
				{
				setState(680); match(BAD_ID);
				notifyErrorListeners("Index names must start with a letter: " +
				                _input.getText(_localctx.start, _input.LT(-1)));
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Create_user_statementContext extends ParserRuleContext {
		public TerminalNode ADMIN() { return getToken(TableParser.ADMIN, 0); }
		public Account_lockContext account_lock() {
			return getRuleContext(Account_lockContext.class,0);
		}
		public TerminalNode CREATE() { return getToken(TableParser.CREATE, 0); }
		public TerminalNode USER() { return getToken(TableParser.USER, 0); }
		public Create_user_identified_clauseContext create_user_identified_clause() {
			return getRuleContext(Create_user_identified_clauseContext.class,0);
		}
		public Create_user_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_user_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterCreate_user_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitCreate_user_statement(this);
		}
	}

	public final Create_user_statementContext create_user_statement() throws RecognitionException {
		Create_user_statementContext _localctx = new Create_user_statementContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_create_user_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(684); match(CREATE);
			setState(685); match(USER);
			setState(686); create_user_identified_clause();
			setState(688);
			_la = _input.LA(1);
			if (_la==ACCOUNT) {
				{
				setState(687); account_lock();
				}
			}

			setState(691);
			_la = _input.LA(1);
			if (_la==ADMIN) {
				{
				setState(690); match(ADMIN);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Create_role_statementContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(TableParser.CREATE, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode ROLE() { return getToken(TableParser.ROLE, 0); }
		public Create_role_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_role_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterCreate_role_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitCreate_role_statement(this);
		}
	}

	public final Create_role_statementContext create_role_statement() throws RecognitionException {
		Create_role_statementContext _localctx = new Create_role_statementContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_create_role_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(693); match(CREATE);
			setState(694); match(ROLE);
			setState(695); identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Alter_user_statementContext extends ParserRuleContext {
		public Password_lifetimeContext password_lifetime() {
			return getRuleContext(Password_lifetimeContext.class,0);
		}
		public Identifier_or_stringContext identifier_or_string() {
			return getRuleContext(Identifier_or_stringContext.class,0);
		}
		public Account_lockContext account_lock() {
			return getRuleContext(Account_lockContext.class,0);
		}
		public Reset_password_clauseContext reset_password_clause() {
			return getRuleContext(Reset_password_clauseContext.class,0);
		}
		public TerminalNode CLEAR_RETAINED_PASSWORD() { return getToken(TableParser.CLEAR_RETAINED_PASSWORD, 0); }
		public TerminalNode ALTER() { return getToken(TableParser.ALTER, 0); }
		public TerminalNode PASSWORD_EXPIRE() { return getToken(TableParser.PASSWORD_EXPIRE, 0); }
		public TerminalNode USER() { return getToken(TableParser.USER, 0); }
		public Alter_user_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alter_user_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterAlter_user_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitAlter_user_statement(this);
		}
	}

	public final Alter_user_statementContext alter_user_statement() throws RecognitionException {
		Alter_user_statementContext _localctx = new Alter_user_statementContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_alter_user_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(697); match(ALTER);
			setState(698); match(USER);
			setState(699); identifier_or_string();
			setState(701);
			_la = _input.LA(1);
			if (_la==IDENTIFIED) {
				{
				setState(700); reset_password_clause();
				}
			}

			setState(704);
			_la = _input.LA(1);
			if (_la==CLEAR_RETAINED_PASSWORD) {
				{
				setState(703); match(CLEAR_RETAINED_PASSWORD);
				}
			}

			setState(707);
			_la = _input.LA(1);
			if (_la==PASSWORD_EXPIRE) {
				{
				setState(706); match(PASSWORD_EXPIRE);
				}
			}

			setState(710);
			_la = _input.LA(1);
			if (_la==PASSWORD_LIFETIME) {
				{
				setState(709); password_lifetime();
				}
			}

			setState(713);
			_la = _input.LA(1);
			if (_la==ACCOUNT) {
				{
				setState(712); account_lock();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Drop_user_statementContext extends ParserRuleContext {
		public Identifier_or_stringContext identifier_or_string() {
			return getRuleContext(Identifier_or_stringContext.class,0);
		}
		public TerminalNode DROP() { return getToken(TableParser.DROP, 0); }
		public TerminalNode USER() { return getToken(TableParser.USER, 0); }
		public Drop_user_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_user_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterDrop_user_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitDrop_user_statement(this);
		}
	}

	public final Drop_user_statementContext drop_user_statement() throws RecognitionException {
		Drop_user_statementContext _localctx = new Drop_user_statementContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_drop_user_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(715); match(DROP);
			setState(716); match(USER);
			setState(717); identifier_or_string();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Drop_role_statementContext extends ParserRuleContext {
		public TerminalNode DROP() { return getToken(TableParser.DROP, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode ROLE() { return getToken(TableParser.ROLE, 0); }
		public Drop_role_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_role_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterDrop_role_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitDrop_role_statement(this);
		}
	}

	public final Drop_role_statementContext drop_role_statement() throws RecognitionException {
		Drop_role_statementContext _localctx = new Drop_role_statementContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_drop_role_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(719); match(DROP);
			setState(720); match(ROLE);
			setState(721); identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Grant_statementContext extends ParserRuleContext {
		public Grant_object_privilegesContext grant_object_privileges() {
			return getRuleContext(Grant_object_privilegesContext.class,0);
		}
		public Grant_system_privilegesContext grant_system_privileges() {
			return getRuleContext(Grant_system_privilegesContext.class,0);
		}
		public Grant_rolesContext grant_roles() {
			return getRuleContext(Grant_rolesContext.class,0);
		}
		public TerminalNode GRANT() { return getToken(TableParser.GRANT, 0); }
		public Grant_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_grant_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterGrant_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitGrant_statement(this);
		}
	}

	public final Grant_statementContext grant_statement() throws RecognitionException {
		Grant_statementContext _localctx = new Grant_statementContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_grant_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(723); match(GRANT);
			setState(727);
			switch ( getInterpreter().adaptivePredict(_input,78,_ctx) ) {
			case 1:
				{
				setState(724); grant_roles();
				}
				break;
			case 2:
				{
				setState(725); grant_system_privileges();
				}
				break;
			case 3:
				{
				setState(726); grant_object_privileges();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Revoke_statementContext extends ParserRuleContext {
		public Revoke_object_privilegesContext revoke_object_privileges() {
			return getRuleContext(Revoke_object_privilegesContext.class,0);
		}
		public TerminalNode REVOKE() { return getToken(TableParser.REVOKE, 0); }
		public Revoke_system_privilegesContext revoke_system_privileges() {
			return getRuleContext(Revoke_system_privilegesContext.class,0);
		}
		public Revoke_rolesContext revoke_roles() {
			return getRuleContext(Revoke_rolesContext.class,0);
		}
		public Revoke_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_revoke_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterRevoke_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitRevoke_statement(this);
		}
	}

	public final Revoke_statementContext revoke_statement() throws RecognitionException {
		Revoke_statementContext _localctx = new Revoke_statementContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_revoke_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(729); match(REVOKE);
			setState(733);
			switch ( getInterpreter().adaptivePredict(_input,79,_ctx) ) {
			case 1:
				{
				setState(730); revoke_roles();
				}
				break;
			case 2:
				{
				setState(731); revoke_system_privileges();
				}
				break;
			case 3:
				{
				setState(732); revoke_object_privileges();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Extended_idContext extends ParserRuleContext {
		public TerminalNode ROLES() { return getToken(TableParser.ROLES, 0); }
		public TerminalNode ID() { return getToken(TableParser.ID, 0); }
		public TerminalNode ACCOUNT() { return getToken(TableParser.ACCOUNT, 0); }
		public TerminalNode ADMIN() { return getToken(TableParser.ADMIN, 0); }
		public TerminalNode USERS() { return getToken(TableParser.USERS, 0); }
		public TerminalNode LIFETIME() { return getToken(TableParser.LIFETIME, 0); }
		public TerminalNode TIME_UNIT() { return getToken(TableParser.TIME_UNIT, 0); }
		public TerminalNode ROLE() { return getToken(TableParser.ROLE, 0); }
		public TerminalNode PASSWORD() { return getToken(TableParser.PASSWORD, 0); }
		public Extended_idContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extended_id; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterExtended_id(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitExtended_id(this);
		}
	}

	public final Extended_idContext extended_id() throws RecognitionException {
		Extended_idContext _localctx = new Extended_idContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_extended_id);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(735);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << USERS) | (1L << ROLE) | (1L << ROLES) | (1L << ADMIN) | (1L << PASSWORD) | (1L << LIFETIME) | (1L << TIME_UNIT))) != 0) || _la==ACCOUNT || _la==ID) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IdentifierContext extends ParserRuleContext {
		public Extended_idContext extended_id() {
			return getRuleContext(Extended_idContext.class,0);
		}
		public TerminalNode BAD_ID() { return getToken(TableParser.BAD_ID, 0); }
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitIdentifier(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_identifier);
		try {
			setState(740);
			switch (_input.LA(1)) {
			case USERS:
			case ROLE:
			case ROLES:
			case ADMIN:
			case PASSWORD:
			case LIFETIME:
			case TIME_UNIT:
			case ACCOUNT:
			case ID:
				enterOuterAlt(_localctx, 1);
				{
				setState(737); extended_id();
				}
				break;
			case BAD_ID:
				enterOuterAlt(_localctx, 2);
				{
				setState(738); match(BAD_ID);
				notifyErrorListeners
				         ("Identifier name must start with a letter: " + _input.getText(_localctx.start, _input.LT(-1)));
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Identifier_or_stringContext extends ParserRuleContext {
		public TerminalNode STRING() { return getToken(TableParser.STRING, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public Identifier_or_stringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier_or_string; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterIdentifier_or_string(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitIdentifier_or_string(this);
		}
	}

	public final Identifier_or_stringContext identifier_or_string() throws RecognitionException {
		Identifier_or_stringContext _localctx = new Identifier_or_stringContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_identifier_or_string);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(744);
			switch (_input.LA(1)) {
			case USERS:
			case ROLE:
			case ROLES:
			case ADMIN:
			case PASSWORD:
			case LIFETIME:
			case TIME_UNIT:
			case ACCOUNT:
			case ID:
			case BAD_ID:
				{
				setState(742); identifier();
				}
				break;
			case STRING:
				{
				setState(743); match(STRING);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Identified_clauseContext extends ParserRuleContext {
		public TerminalNode IDENTIFIED() { return getToken(TableParser.IDENTIFIED, 0); }
		public By_passwordContext by_password() {
			return getRuleContext(By_passwordContext.class,0);
		}
		public Identified_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identified_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterIdentified_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitIdentified_clause(this);
		}
	}

	public final Identified_clauseContext identified_clause() throws RecognitionException {
		Identified_clauseContext _localctx = new Identified_clauseContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_identified_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(746); match(IDENTIFIED);
			setState(747); by_password();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Create_user_identified_clauseContext extends ParserRuleContext {
		public Password_lifetimeContext password_lifetime() {
			return getRuleContext(Password_lifetimeContext.class,0);
		}
		public TerminalNode IDENTIFIED_EXTERNALLY() { return getToken(TableParser.IDENTIFIED_EXTERNALLY, 0); }
		public TerminalNode STRING() { return getToken(TableParser.STRING, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode PASSWORD_EXPIRE() { return getToken(TableParser.PASSWORD_EXPIRE, 0); }
		public Identified_clauseContext identified_clause() {
			return getRuleContext(Identified_clauseContext.class,0);
		}
		public Create_user_identified_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_user_identified_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterCreate_user_identified_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitCreate_user_identified_clause(this);
		}
	}

	public final Create_user_identified_clauseContext create_user_identified_clause() throws RecognitionException {
		Create_user_identified_clauseContext _localctx = new Create_user_identified_clauseContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_create_user_identified_clause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(759);
			switch (_input.LA(1)) {
			case USERS:
			case ROLE:
			case ROLES:
			case ADMIN:
			case PASSWORD:
			case LIFETIME:
			case TIME_UNIT:
			case ACCOUNT:
			case ID:
			case BAD_ID:
				{
				setState(749); identifier();
				setState(750); identified_clause();
				setState(752);
				_la = _input.LA(1);
				if (_la==PASSWORD_EXPIRE) {
					{
					setState(751); match(PASSWORD_EXPIRE);
					}
				}

				setState(755);
				_la = _input.LA(1);
				if (_la==PASSWORD_LIFETIME) {
					{
					setState(754); password_lifetime();
					}
				}

				}
				break;
			case STRING:
				{
				setState(757); match(STRING);
				setState(758); match(IDENTIFIED_EXTERNALLY);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class By_passwordContext extends ParserRuleContext {
		public TerminalNode STRING() { return getToken(TableParser.STRING, 0); }
		public TerminalNode BY() { return getToken(TableParser.BY, 0); }
		public By_passwordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_by_password; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterBy_password(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitBy_password(this);
		}
	}

	public final By_passwordContext by_password() throws RecognitionException {
		By_passwordContext _localctx = new By_passwordContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_by_password);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(761); match(BY);
			setState(762); match(STRING);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Password_lifetimeContext extends ParserRuleContext {
		public TerminalNode PASSWORD_LIFETIME() { return getToken(TableParser.PASSWORD_LIFETIME, 0); }
		public DurationContext duration() {
			return getRuleContext(DurationContext.class,0);
		}
		public Password_lifetimeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_password_lifetime; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterPassword_lifetime(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitPassword_lifetime(this);
		}
	}

	public final Password_lifetimeContext password_lifetime() throws RecognitionException {
		Password_lifetimeContext _localctx = new Password_lifetimeContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_password_lifetime);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(764); match(PASSWORD_LIFETIME);
			setState(765); duration();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DurationContext extends ParserRuleContext {
		public TerminalNode WS(int i) {
			return getToken(TableParser.WS, i);
		}
		public List<TerminalNode> WS() { return getTokens(TableParser.WS); }
		public TerminalNode INT() { return getToken(TableParser.INT, 0); }
		public TerminalNode TIME_UNIT() { return getToken(TableParser.TIME_UNIT, 0); }
		public DurationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_duration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterDuration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitDuration(this);
		}
	}

	public final DurationContext duration() throws RecognitionException {
		DurationContext _localctx = new DurationContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_duration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(767); match(INT);
			setState(771);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WS) {
				{
				{
				setState(768); match(WS);
				}
				}
				setState(773);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(774); match(TIME_UNIT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Reset_password_clauseContext extends ParserRuleContext {
		public TerminalNode RETAIN_CURRENT_PASSWORD() { return getToken(TableParser.RETAIN_CURRENT_PASSWORD, 0); }
		public Identified_clauseContext identified_clause() {
			return getRuleContext(Identified_clauseContext.class,0);
		}
		public Reset_password_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_reset_password_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterReset_password_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitReset_password_clause(this);
		}
	}

	public final Reset_password_clauseContext reset_password_clause() throws RecognitionException {
		Reset_password_clauseContext _localctx = new Reset_password_clauseContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_reset_password_clause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(776); identified_clause();
			setState(778);
			_la = _input.LA(1);
			if (_la==RETAIN_CURRENT_PASSWORD) {
				{
				setState(777); match(RETAIN_CURRENT_PASSWORD);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Account_lockContext extends ParserRuleContext {
		public TerminalNode ACCOUNT() { return getToken(TableParser.ACCOUNT, 0); }
		public TerminalNode LOCK() { return getToken(TableParser.LOCK, 0); }
		public TerminalNode UNLOCK() { return getToken(TableParser.UNLOCK, 0); }
		public Account_lockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_account_lock; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterAccount_lock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitAccount_lock(this);
		}
	}

	public final Account_lockContext account_lock() throws RecognitionException {
		Account_lockContext _localctx = new Account_lockContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_account_lock);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(780); match(ACCOUNT);
			setState(781);
			_la = _input.LA(1);
			if ( !(_la==LOCK || _la==UNLOCK) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Grant_rolesContext extends ParserRuleContext {
		public Id_listContext id_list() {
			return getRuleContext(Id_listContext.class,0);
		}
		public PrincipalContext principal() {
			return getRuleContext(PrincipalContext.class,0);
		}
		public TerminalNode TO() { return getToken(TableParser.TO, 0); }
		public Grant_rolesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_grant_roles; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterGrant_roles(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitGrant_roles(this);
		}
	}

	public final Grant_rolesContext grant_roles() throws RecognitionException {
		Grant_rolesContext _localctx = new Grant_rolesContext(_ctx, getState());
		enterRule(_localctx, 140, RULE_grant_roles);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(783); id_list();
			setState(784); match(TO);
			setState(785); principal();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Grant_system_privilegesContext extends ParserRuleContext {
		public TerminalNode TO() { return getToken(TableParser.TO, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public Sys_priv_listContext sys_priv_list() {
			return getRuleContext(Sys_priv_listContext.class,0);
		}
		public Grant_system_privilegesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_grant_system_privileges; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterGrant_system_privileges(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitGrant_system_privileges(this);
		}
	}

	public final Grant_system_privilegesContext grant_system_privileges() throws RecognitionException {
		Grant_system_privilegesContext _localctx = new Grant_system_privilegesContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_grant_system_privileges);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(787); sys_priv_list();
			setState(788); match(TO);
			setState(789); identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Grant_object_privilegesContext extends ParserRuleContext {
		public TerminalNode ON() { return getToken(TableParser.ON, 0); }
		public TerminalNode TO() { return getToken(TableParser.TO, 0); }
		public Obj_priv_listContext obj_priv_list() {
			return getRuleContext(Obj_priv_listContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ObjectContext object() {
			return getRuleContext(ObjectContext.class,0);
		}
		public Grant_object_privilegesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_grant_object_privileges; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterGrant_object_privileges(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitGrant_object_privileges(this);
		}
	}

	public final Grant_object_privilegesContext grant_object_privileges() throws RecognitionException {
		Grant_object_privilegesContext _localctx = new Grant_object_privilegesContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_grant_object_privileges);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(791); obj_priv_list();
			setState(792); match(ON);
			setState(793); object();
			setState(794); match(TO);
			setState(795); identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Revoke_rolesContext extends ParserRuleContext {
		public Id_listContext id_list() {
			return getRuleContext(Id_listContext.class,0);
		}
		public PrincipalContext principal() {
			return getRuleContext(PrincipalContext.class,0);
		}
		public TerminalNode FROM() { return getToken(TableParser.FROM, 0); }
		public Revoke_rolesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_revoke_roles; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterRevoke_roles(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitRevoke_roles(this);
		}
	}

	public final Revoke_rolesContext revoke_roles() throws RecognitionException {
		Revoke_rolesContext _localctx = new Revoke_rolesContext(_ctx, getState());
		enterRule(_localctx, 146, RULE_revoke_roles);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(797); id_list();
			setState(798); match(FROM);
			setState(799); principal();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Revoke_system_privilegesContext extends ParserRuleContext {
		public TerminalNode FROM() { return getToken(TableParser.FROM, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public Sys_priv_listContext sys_priv_list() {
			return getRuleContext(Sys_priv_listContext.class,0);
		}
		public Revoke_system_privilegesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_revoke_system_privileges; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterRevoke_system_privileges(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitRevoke_system_privileges(this);
		}
	}

	public final Revoke_system_privilegesContext revoke_system_privileges() throws RecognitionException {
		Revoke_system_privilegesContext _localctx = new Revoke_system_privilegesContext(_ctx, getState());
		enterRule(_localctx, 148, RULE_revoke_system_privileges);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(801); sys_priv_list();
			setState(802); match(FROM);
			setState(803); identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Revoke_object_privilegesContext extends ParserRuleContext {
		public TerminalNode ON() { return getToken(TableParser.ON, 0); }
		public TerminalNode FROM() { return getToken(TableParser.FROM, 0); }
		public Obj_priv_listContext obj_priv_list() {
			return getRuleContext(Obj_priv_listContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ObjectContext object() {
			return getRuleContext(ObjectContext.class,0);
		}
		public Revoke_object_privilegesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_revoke_object_privileges; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterRevoke_object_privileges(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitRevoke_object_privileges(this);
		}
	}

	public final Revoke_object_privilegesContext revoke_object_privileges() throws RecognitionException {
		Revoke_object_privilegesContext _localctx = new Revoke_object_privilegesContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_revoke_object_privileges);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(805); obj_priv_list();
			setState(806); match(ON);
			setState(807); object();
			setState(808); match(FROM);
			setState(809); identifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PrincipalContext extends ParserRuleContext {
		public Identifier_or_stringContext identifier_or_string() {
			return getRuleContext(Identifier_or_stringContext.class,0);
		}
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode USER() { return getToken(TableParser.USER, 0); }
		public TerminalNode ROLE() { return getToken(TableParser.ROLE, 0); }
		public PrincipalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_principal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterPrincipal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitPrincipal(this);
		}
	}

	public final PrincipalContext principal() throws RecognitionException {
		PrincipalContext _localctx = new PrincipalContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_principal);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(815);
			switch (_input.LA(1)) {
			case USER:
				{
				setState(811); match(USER);
				setState(812); identifier_or_string();
				}
				break;
			case ROLE:
				{
				setState(813); match(ROLE);
				setState(814); identifier();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Sys_priv_listContext extends ParserRuleContext {
		public Priv_itemContext priv_item(int i) {
			return getRuleContext(Priv_itemContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(TableParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(TableParser.COMMA, i);
		}
		public List<Priv_itemContext> priv_item() {
			return getRuleContexts(Priv_itemContext.class);
		}
		public Sys_priv_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sys_priv_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterSys_priv_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitSys_priv_list(this);
		}
	}

	public final Sys_priv_listContext sys_priv_list() throws RecognitionException {
		Sys_priv_listContext _localctx = new Sys_priv_listContext(_ctx, getState());
		enterRule(_localctx, 154, RULE_sys_priv_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(817); priv_item();
			setState(822);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(818); match(COMMA);
				setState(819); priv_item();
				}
				}
				setState(824);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Priv_itemContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode ALL_PRIVILEGES() { return getToken(TableParser.ALL_PRIVILEGES, 0); }
		public Priv_itemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_priv_item; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterPriv_item(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitPriv_item(this);
		}
	}

	public final Priv_itemContext priv_item() throws RecognitionException {
		Priv_itemContext _localctx = new Priv_itemContext(_ctx, getState());
		enterRule(_localctx, 156, RULE_priv_item);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(827);
			switch (_input.LA(1)) {
			case USERS:
			case ROLE:
			case ROLES:
			case ADMIN:
			case PASSWORD:
			case LIFETIME:
			case TIME_UNIT:
			case ACCOUNT:
			case ID:
			case BAD_ID:
				{
				setState(825); identifier();
				}
				break;
			case ALL_PRIVILEGES:
				{
				setState(826); match(ALL_PRIVILEGES);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Obj_priv_listContext extends ParserRuleContext {
		public Priv_itemContext priv_item(int i) {
			return getRuleContext(Priv_itemContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(TableParser.COMMA); }
		public TerminalNode ALL(int i) {
			return getToken(TableParser.ALL, i);
		}
		public TerminalNode COMMA(int i) {
			return getToken(TableParser.COMMA, i);
		}
		public List<Priv_itemContext> priv_item() {
			return getRuleContexts(Priv_itemContext.class);
		}
		public List<TerminalNode> ALL() { return getTokens(TableParser.ALL); }
		public Obj_priv_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_obj_priv_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterObj_priv_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitObj_priv_list(this);
		}
	}

	public final Obj_priv_listContext obj_priv_list() throws RecognitionException {
		Obj_priv_listContext _localctx = new Obj_priv_listContext(_ctx, getState());
		enterRule(_localctx, 158, RULE_obj_priv_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(831);
			switch (_input.LA(1)) {
			case USERS:
			case ROLE:
			case ROLES:
			case ADMIN:
			case PASSWORD:
			case LIFETIME:
			case TIME_UNIT:
			case ACCOUNT:
			case ALL_PRIVILEGES:
			case ID:
			case BAD_ID:
				{
				setState(829); priv_item();
				}
				break;
			case ALL:
				{
				setState(830); match(ALL);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(840);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(833); match(COMMA);
				setState(836);
				switch (_input.LA(1)) {
				case USERS:
				case ROLE:
				case ROLES:
				case ADMIN:
				case PASSWORD:
				case LIFETIME:
				case TIME_UNIT:
				case ACCOUNT:
				case ALL_PRIVILEGES:
				case ID:
				case BAD_ID:
					{
					setState(834); priv_item();
					}
					break;
				case ALL:
					{
					setState(835); match(ALL);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				}
				setState(842);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ObjectContext extends ParserRuleContext {
		public Name_pathContext name_path() {
			return getRuleContext(Name_pathContext.class,0);
		}
		public ObjectContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_object; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterObject(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitObject(this);
		}
	}

	public final ObjectContext object() throws RecognitionException {
		ObjectContext _localctx = new ObjectContext(_ctx, getState());
		enterRule(_localctx, 160, RULE_object);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(843); name_path();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class JsonContext extends ParserRuleContext {
		public JsobjectContext jsobject() {
			return getRuleContext(JsobjectContext.class,0);
		}
		public JsarrayContext jsarray() {
			return getRuleContext(JsarrayContext.class,0);
		}
		public JsonContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_json; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterJson(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitJson(this);
		}
	}

	public final JsonContext json() throws RecognitionException {
		JsonContext _localctx = new JsonContext(_ctx, getState());
		enterRule(_localctx, 162, RULE_json);
		try {
			setState(847);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(845); jsobject();
				}
				break;
			case LBRACK:
				enterOuterAlt(_localctx, 2);
				{
				setState(846); jsarray();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class JsobjectContext extends ParserRuleContext {
		public JsobjectContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jsobject; }
	 
		public JsobjectContext() { }
		public void copyFrom(JsobjectContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class JsonObjectContext extends JsobjectContext {
		public TerminalNode RBRACE() { return getToken(TableParser.RBRACE, 0); }
		public TerminalNode LBRACE() { return getToken(TableParser.LBRACE, 0); }
		public List<JspairContext> jspair() {
			return getRuleContexts(JspairContext.class);
		}
		public JspairContext jspair(int i) {
			return getRuleContext(JspairContext.class,i);
		}
		public JsonObjectContext(JsobjectContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterJsonObject(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitJsonObject(this);
		}
	}
	public static class EmptyJsonObjectContext extends JsobjectContext {
		public TerminalNode RBRACE() { return getToken(TableParser.RBRACE, 0); }
		public TerminalNode LBRACE() { return getToken(TableParser.LBRACE, 0); }
		public EmptyJsonObjectContext(JsobjectContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterEmptyJsonObject(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitEmptyJsonObject(this);
		}
	}

	public final JsobjectContext jsobject() throws RecognitionException {
		JsobjectContext _localctx = new JsobjectContext(_ctx, getState());
		enterRule(_localctx, 164, RULE_jsobject);
		int _la;
		try {
			setState(862);
			switch ( getInterpreter().adaptivePredict(_input,95,_ctx) ) {
			case 1:
				_localctx = new JsonObjectContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(849); match(LBRACE);
				setState(850); jspair();
				setState(855);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(851); match(COMMA);
					setState(852); jspair();
					}
					}
					setState(857);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(858); match(RBRACE);
				}
				break;
			case 2:
				_localctx = new EmptyJsonObjectContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(860); match(LBRACE);
				setState(861); match(RBRACE);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class JsarrayContext extends ParserRuleContext {
		public JsarrayContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jsarray; }
	 
		public JsarrayContext() { }
		public void copyFrom(JsarrayContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class EmptyJsonArrayContext extends JsarrayContext {
		public TerminalNode RBRACK() { return getToken(TableParser.RBRACK, 0); }
		public TerminalNode LBRACK() { return getToken(TableParser.LBRACK, 0); }
		public EmptyJsonArrayContext(JsarrayContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterEmptyJsonArray(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitEmptyJsonArray(this);
		}
	}
	public static class ArrayOfJsonValuesContext extends JsarrayContext {
		public JsvalueContext jsvalue(int i) {
			return getRuleContext(JsvalueContext.class,i);
		}
		public TerminalNode RBRACK() { return getToken(TableParser.RBRACK, 0); }
		public TerminalNode LBRACK() { return getToken(TableParser.LBRACK, 0); }
		public List<JsvalueContext> jsvalue() {
			return getRuleContexts(JsvalueContext.class);
		}
		public ArrayOfJsonValuesContext(JsarrayContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterArrayOfJsonValues(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitArrayOfJsonValues(this);
		}
	}

	public final JsarrayContext jsarray() throws RecognitionException {
		JsarrayContext _localctx = new JsarrayContext(_ctx, getState());
		enterRule(_localctx, 166, RULE_jsarray);
		int _la;
		try {
			setState(877);
			switch ( getInterpreter().adaptivePredict(_input,97,_ctx) ) {
			case 1:
				_localctx = new ArrayOfJsonValuesContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(864); match(LBRACK);
				setState(865); jsvalue();
				setState(870);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(866); match(COMMA);
					setState(867); jsvalue();
					}
					}
					setState(872);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(873); match(RBRACK);
				}
				break;
			case 2:
				_localctx = new EmptyJsonArrayContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(875); match(LBRACK);
				setState(876); match(RBRACK);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class JspairContext extends ParserRuleContext {
		public JspairContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jspair; }
	 
		public JspairContext() { }
		public void copyFrom(JspairContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class JsonPairContext extends JspairContext {
		public TerminalNode STRING() { return getToken(TableParser.STRING, 0); }
		public JsvalueContext jsvalue() {
			return getRuleContext(JsvalueContext.class,0);
		}
		public JsonPairContext(JspairContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterJsonPair(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitJsonPair(this);
		}
	}

	public final JspairContext jspair() throws RecognitionException {
		JspairContext _localctx = new JspairContext(_ctx, getState());
		enterRule(_localctx, 168, RULE_jspair);
		try {
			_localctx = new JsonPairContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(879); match(STRING);
			setState(880); match(COLON);
			setState(881); jsvalue();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class JsvalueContext extends ParserRuleContext {
		public JsvalueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jsvalue; }
	 
		public JsvalueContext() { }
		public void copyFrom(JsvalueContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class JsonAtomContext extends JsvalueContext {
		public TerminalNode STRING() { return getToken(TableParser.STRING, 0); }
		public TerminalNode JSON_NUMBER() { return getToken(TableParser.JSON_NUMBER, 0); }
		public JsonAtomContext(JsvalueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterJsonAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitJsonAtom(this);
		}
	}
	public static class JsonArrayValueContext extends JsvalueContext {
		public JsarrayContext jsarray() {
			return getRuleContext(JsarrayContext.class,0);
		}
		public JsonArrayValueContext(JsvalueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterJsonArrayValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitJsonArrayValue(this);
		}
	}
	public static class JsonObjectValueContext extends JsvalueContext {
		public JsobjectContext jsobject() {
			return getRuleContext(JsobjectContext.class,0);
		}
		public JsonObjectValueContext(JsvalueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).enterJsonObjectValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof TableListener ) ((TableListener)listener).exitJsonObjectValue(this);
		}
	}

	public final JsvalueContext jsvalue() throws RecognitionException {
		JsvalueContext _localctx = new JsvalueContext(_ctx, getState());
		enterRule(_localctx, 170, RULE_jsvalue);
		try {
			setState(890);
			switch (_input.LA(1)) {
			case LBRACE:
				_localctx = new JsonObjectValueContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(883); jsobject();
				}
				break;
			case LBRACK:
				_localctx = new JsonArrayValueContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(884); jsarray();
				}
				break;
			case STRING:
				_localctx = new JsonAtomContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(885); match(STRING);
				}
				break;
			case JSON_NUMBER:
				_localctx = new JsonAtomContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(886); match(JSON_NUMBER);
				}
				break;
			case T__2:
				_localctx = new JsonAtomContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(887); match(T__2);
				}
				break;
			case T__1:
				_localctx = new JsonAtomContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(888); match(T__1);
				}
				break;
			case T__3:
				_localctx = new JsonAtomContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(889); match(T__3);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3f\u037f\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I"+
		"\tI\4J\tJ\4K\tK\4L\tL\4M\tM\4N\tN\4O\tO\4P\tP\4Q\tQ\4R\tR\4S\tS\4T\tT"+
		"\4U\tU\4V\tV\4W\tW\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2"+
		"\3\2\3\2\5\2\u00be\n\2\3\2\3\2\3\3\3\3\3\3\5\3\u00c5\n\3\3\3\3\3\3\3\3"+
		"\3\3\3\3\4\3\4\3\4\5\4\u00cf\n\4\3\4\3\4\3\5\3\5\3\5\5\5\u00d6\n\5\3\5"+
		"\3\5\3\5\3\5\3\5\5\5\u00dd\n\5\3\6\3\6\3\6\5\6\u00e2\n\6\3\6\3\6\3\6\3"+
		"\6\3\7\3\7\3\7\3\7\5\7\u00ec\n\7\3\7\3\7\3\7\3\7\3\7\5\7\u00f3\n\7\3\b"+
		"\3\b\3\b\3\b\3\b\3\t\3\t\5\t\u00fc\n\t\3\t\3\t\3\t\5\t\u0101\n\t\3\t\3"+
		"\t\3\t\3\t\3\t\5\t\u0108\n\t\3\n\3\n\5\n\u010c\n\n\3\n\3\n\3\n\3\n\3\n"+
		"\3\n\3\n\3\n\3\n\3\n\3\n\3\n\5\n\u011a\n\n\3\13\3\13\3\13\3\13\5\13\u0120"+
		"\n\13\3\13\3\13\3\13\3\13\5\13\u0126\n\13\7\13\u0128\n\13\f\13\16\13\u012b"+
		"\13\13\3\13\3\13\3\f\3\f\3\f\3\r\3\r\3\r\3\16\3\16\3\16\3\17\3\17\3\17"+
		"\5\17\u013b\n\17\3\17\3\17\3\17\3\17\5\17\u0141\n\17\7\17\u0143\n\17\f"+
		"\17\16\17\u0146\13\17\3\20\3\20\3\20\3\20\5\20\u014c\n\20\5\20\u014e\n"+
		"\20\3\20\5\20\u0151\n\20\3\20\3\20\3\21\3\21\3\21\3\22\3\22\3\22\3\22"+
		"\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\7\22\u0164\n\22\f\22\16\22\u0167"+
		"\13\22\3\22\5\22\u016a\n\22\3\23\3\23\3\23\7\23\u016f\n\23\f\23\16\23"+
		"\u0172\13\23\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\5\24\u017c\n\24\3"+
		"\25\3\25\3\25\7\25\u0181\n\25\f\25\16\25\u0184\13\25\3\26\3\26\3\26\3"+
		"\26\3\26\3\26\3\26\3\26\5\26\u018e\n\26\3\27\3\27\3\27\7\27\u0193\n\27"+
		"\f\27\16\27\u0196\13\27\3\30\3\30\5\30\u019a\n\30\3\31\3\31\3\31\3\31"+
		"\7\31\u01a0\n\31\f\31\16\31\u01a3\13\31\3\31\3\31\3\31\3\31\3\31\7\31"+
		"\u01aa\n\31\f\31\16\31\u01ad\13\31\3\31\5\31\u01b0\n\31\3\32\3\32\3\33"+
		"\3\33\3\33\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\5\34\u01c0\n\34"+
		"\3\35\3\35\7\35\u01c4\n\35\f\35\16\35\u01c7\13\35\3\35\5\35\u01ca\n\35"+
		"\3\35\7\35\u01cd\n\35\f\35\16\35\u01d0\13\35\3\36\3\36\7\36\u01d4\n\36"+
		"\f\36\16\36\u01d7\13\36\3\36\5\36\u01da\n\36\3\36\7\36\u01dd\n\36\f\36"+
		"\16\36\u01e0\13\36\3\37\3\37\7\37\u01e4\n\37\f\37\16\37\u01e7\13\37\3"+
		"\37\5\37\u01ea\n\37\3\37\7\37\u01ed\n\37\f\37\16\37\u01f0\13\37\3 \3 "+
		"\7 \u01f4\n \f \16 \u01f7\13 \3 \5 \u01fa\n \3 \7 \u01fd\n \f \16 \u0200"+
		"\13 \3!\3!\3!\7!\u0205\n!\f!\16!\u0208\13!\3!\5!\u020b\n!\3!\7!\u020e"+
		"\n!\f!\16!\u0211\13!\3\"\3\"\3\"\7\"\u0216\n\"\f\"\16\"\u0219\13\"\3\""+
		"\3\"\7\"\u021d\n\"\f\"\16\"\u0220\13\"\3\"\5\"\u0223\n\"\3\"\5\"\u0226"+
		"\n\"\3#\3#\3#\3#\3#\7#\u022d\n#\f#\16#\u0230\13#\3#\3#\5#\u0234\n#\3$"+
		"\3$\3$\3$\3$\5$\u023b\n$\3%\3%\3%\3%\3%\5%\u0242\n%\3&\3&\3&\3\'\3\'\3"+
		"\'\5\'\u024a\n\'\3(\3(\3(\5(\u024f\n(\3)\3)\3)\3*\3*\3*\5*\u0257\n*\3"+
		"+\3+\3+\3,\3,\3,\5,\u025f\n,\3-\3-\3-\3-\3-\3-\5-\u0267\n-\3.\3.\3.\3"+
		".\3.\3.\5.\u026f\n.\3/\3/\3/\3/\3/\7/\u0276\n/\f/\16/\u0279\13/\3/\3/"+
		"\3\60\3\60\3\60\3\60\3\60\3\60\3\60\3\60\5\60\u0285\n\60\3\61\3\61\3\62"+
		"\3\62\5\62\u028b\n\62\3\62\3\62\3\62\3\62\5\62\u0291\n\62\3\63\3\63\3"+
		"\63\5\63\u0296\n\63\3\64\3\64\3\64\3\64\3\64\3\65\3\65\3\65\3\65\3\65"+
		"\3\65\5\65\u02a3\n\65\3\65\3\65\3\65\5\65\u02a8\n\65\3\66\3\66\3\66\5"+
		"\66\u02ad\n\66\3\67\3\67\3\67\3\67\5\67\u02b3\n\67\3\67\5\67\u02b6\n\67"+
		"\38\38\38\38\39\39\39\39\59\u02c0\n9\39\59\u02c3\n9\39\59\u02c6\n9\39"+
		"\59\u02c9\n9\39\59\u02cc\n9\3:\3:\3:\3:\3;\3;\3;\3;\3<\3<\3<\3<\5<\u02da"+
		"\n<\3=\3=\3=\3=\5=\u02e0\n=\3>\3>\3?\3?\3?\5?\u02e7\n?\3@\3@\5@\u02eb"+
		"\n@\3A\3A\3A\3B\3B\3B\5B\u02f3\nB\3B\5B\u02f6\nB\3B\3B\5B\u02fa\nB\3C"+
		"\3C\3C\3D\3D\3D\3E\3E\7E\u0304\nE\fE\16E\u0307\13E\3E\3E\3F\3F\5F\u030d"+
		"\nF\3G\3G\3G\3H\3H\3H\3H\3I\3I\3I\3I\3J\3J\3J\3J\3J\3J\3K\3K\3K\3K\3L"+
		"\3L\3L\3L\3M\3M\3M\3M\3M\3M\3N\3N\3N\3N\5N\u0332\nN\3O\3O\3O\7O\u0337"+
		"\nO\fO\16O\u033a\13O\3P\3P\5P\u033e\nP\3Q\3Q\5Q\u0342\nQ\3Q\3Q\3Q\5Q\u0347"+
		"\nQ\7Q\u0349\nQ\fQ\16Q\u034c\13Q\3R\3R\3S\3S\5S\u0352\nS\3T\3T\3T\3T\7"+
		"T\u0358\nT\fT\16T\u035b\13T\3T\3T\3T\3T\5T\u0361\nT\3U\3U\3U\3U\7U\u0367"+
		"\nU\fU\16U\u036a\13U\3U\3U\3U\3U\5U\u0370\nU\3V\3V\3V\3V\3W\3W\3W\3W\3"+
		"W\3W\3W\5W\u037d\nW\3W\2\2X\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \""+
		"$&(*,.\60\62\64\668:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082\u0084"+
		"\u0086\u0088\u008a\u008c\u008e\u0090\u0092\u0094\u0096\u0098\u009a\u009c"+
		"\u009e\u00a0\u00a2\u00a4\u00a6\u00a8\u00aa\u00ac\2\n\3\2&\'\4\2WWbb\3"+
		"\2\35\36\4\2\32\32\34\34\3\2Z[\3\2Z\\\7\2\22\24:<@@DDWW\3\2EF\u03b6\2"+
		"\u00bd\3\2\2\2\4\u00c1\3\2\2\2\6\u00cb\3\2\2\2\b\u00d2\3\2\2\2\n\u00de"+
		"\3\2\2\2\f\u00e7\3\2\2\2\16\u00f4\3\2\2\2\20\u00f9\3\2\2\2\22\u0109\3"+
		"\2\2\2\24\u011b\3\2\2\2\26\u012e\3\2\2\2\30\u0131\3\2\2\2\32\u0134\3\2"+
		"\2\2\34\u013a\3\2\2\2\36\u0147\3\2\2\2 \u0154\3\2\2\2\"\u0169\3\2\2\2"+
		"$\u016b\3\2\2\2&\u017b\3\2\2\2(\u017d\3\2\2\2*\u018d\3\2\2\2,\u018f\3"+
		"\2\2\2.\u0197\3\2\2\2\60\u01af\3\2\2\2\62\u01b1\3\2\2\2\64\u01b3\3\2\2"+
		"\2\66\u01bf\3\2\2\28\u01c1\3\2\2\2:\u01d1\3\2\2\2<\u01e1\3\2\2\2>\u01f1"+
		"\3\2\2\2@\u0201\3\2\2\2B\u0212\3\2\2\2D\u0227\3\2\2\2F\u0235\3\2\2\2H"+
		"\u023c\3\2\2\2J\u0243\3\2\2\2L\u0249\3\2\2\2N\u024e\3\2\2\2P\u0250\3\2"+
		"\2\2R\u0256\3\2\2\2T\u0258\3\2\2\2V\u025e\3\2\2\2X\u0266\3\2\2\2Z\u026e"+
		"\3\2\2\2\\\u0270\3\2\2\2^\u0284\3\2\2\2`\u0286\3\2\2\2b\u0290\3\2\2\2"+
		"d\u0295\3\2\2\2f\u0297\3\2\2\2h\u02a7\3\2\2\2j\u02ac\3\2\2\2l\u02ae\3"+
		"\2\2\2n\u02b7\3\2\2\2p\u02bb\3\2\2\2r\u02cd\3\2\2\2t\u02d1\3\2\2\2v\u02d5"+
		"\3\2\2\2x\u02db\3\2\2\2z\u02e1\3\2\2\2|\u02e6\3\2\2\2~\u02ea\3\2\2\2\u0080"+
		"\u02ec\3\2\2\2\u0082\u02f9\3\2\2\2\u0084\u02fb\3\2\2\2\u0086\u02fe\3\2"+
		"\2\2\u0088\u0301\3\2\2\2\u008a\u030a\3\2\2\2\u008c\u030e\3\2\2\2\u008e"+
		"\u0311\3\2\2\2\u0090\u0315\3\2\2\2\u0092\u0319\3\2\2\2\u0094\u031f\3\2"+
		"\2\2\u0096\u0323\3\2\2\2\u0098\u0327\3\2\2\2\u009a\u0331\3\2\2\2\u009c"+
		"\u0333\3\2\2\2\u009e\u033d\3\2\2\2\u00a0\u0341\3\2\2\2\u00a2\u034d\3\2"+
		"\2\2\u00a4\u0351\3\2\2\2\u00a6\u0360\3\2\2\2\u00a8\u036f\3\2\2\2\u00aa"+
		"\u0371\3\2\2\2\u00ac\u037c\3\2\2\2\u00ae\u00be\5\4\3\2\u00af\u00be\5\b"+
		"\5\2\u00b0\u00be\5l\67\2\u00b1\u00be\5n8\2\u00b2\u00be\5\n\6\2\u00b3\u00be"+
		"\5\f\7\2\u00b4\u00be\5t;\2\u00b5\u00be\5r:\2\u00b6\u00be\5\16\b\2\u00b7"+
		"\u00be\5p9\2\u00b8\u00be\5\6\4\2\u00b9\u00be\5v<\2\u00ba\u00be\5x=\2\u00bb"+
		"\u00be\5\20\t\2\u00bc\u00be\5\22\n\2\u00bd\u00ae\3\2\2\2\u00bd\u00af\3"+
		"\2\2\2\u00bd\u00b0\3\2\2\2\u00bd\u00b1\3\2\2\2\u00bd\u00b2\3\2\2\2\u00bd"+
		"\u00b3\3\2\2\2\u00bd\u00b4\3\2\2\2\u00bd\u00b5\3\2\2\2\u00bd\u00b6\3\2"+
		"\2\2\u00bd\u00b7\3\2\2\2\u00bd\u00b8\3\2\2\2\u00bd\u00b9\3\2\2\2\u00bd"+
		"\u00ba\3\2\2\2\u00bd\u00bb\3\2\2\2\u00bd\u00bc\3\2\2\2\u00be\u00bf\3\2"+
		"\2\2\u00bf\u00c0\7\2\2\3\u00c0\3\3\2\2\2\u00c1\u00c2\7\7\2\2\u00c2\u00c4"+
		"\7\b\2\2\u00c3\u00c5\7\"\2\2\u00c4\u00c3\3\2\2\2\u00c4\u00c5\3\2\2\2\u00c5"+
		"\u00c6\3\2\2\2\u00c6\u00c7\5b\62\2\u00c7\u00c8\7O\2\2\u00c8\u00c9\5\34"+
		"\17\2\u00c9\u00ca\7P\2\2\u00ca\5\3\2\2\2\u00cb\u00cc\7\16\2\2\u00cc\u00ce"+
		"\7\b\2\2\u00cd\u00cf\7#\2\2\u00ce\u00cd\3\2\2\2\u00ce\u00cf\3\2\2\2\u00cf"+
		"\u00d0\3\2\2\2\u00d0\u00d1\5b\62\2\u00d1\7\3\2\2\2\u00d2\u00d3\7\7\2\2"+
		"\u00d3\u00d5\7\n\2\2\u00d4\u00d6\7\"\2\2\u00d5\u00d4\3\2\2\2\u00d5\u00d6"+
		"\3\2\2\2\u00d6\u00d7\3\2\2\2\u00d7\u00d8\5j\66\2\u00d8\u00d9\7.\2\2\u00d9"+
		"\u00da\5b\62\2\u00da\u00dc\5&\24\2\u00db\u00dd\5J&\2\u00dc\u00db\3\2\2"+
		"\2\u00dc\u00dd\3\2\2\2\u00dd\t\3\2\2\2\u00de\u00df\7\16\2\2\u00df\u00e1"+
		"\7\n\2\2\u00e0\u00e2\7#\2\2\u00e1\u00e0\3\2\2\2\u00e1\u00e2\3\2\2\2\u00e2"+
		"\u00e3\3\2\2\2\u00e3\u00e4\5j\66\2\u00e4\u00e5\7.\2\2\u00e5\u00e6\5b\62"+
		"\2\u00e6\13\3\2\2\2\u00e7\u00e8\7\7\2\2\u00e8\u00e9\7\f\2\2\u00e9\u00eb"+
		"\7\n\2\2\u00ea\u00ec\7\"\2\2\u00eb\u00ea\3\2\2\2\u00eb\u00ec\3\2\2\2\u00ec"+
		"\u00ed\3\2\2\2\u00ed\u00ee\5j\66\2\u00ee\u00ef\7.\2\2\u00ef\u00f0\5b\62"+
		"\2\u00f0\u00f2\5*\26\2\u00f1\u00f3\5J&\2\u00f2\u00f1\3\2\2\2\u00f2\u00f3"+
		"\3\2\2\2\u00f3\r\3\2\2\2\u00f4\u00f5\7\17\2\2\u00f5\u00f6\7\b\2\2\u00f6"+
		"\u00f7\5b\62\2\u00f7\u00f8\5\24\13\2\u00f8\17\3\2\2\2\u00f9\u00fb\t\2"+
		"\2\2\u00fa\u00fc\7d\2\2\u00fb\u00fa\3\2\2\2\u00fb\u00fc\3\2\2\2\u00fc"+
		"\u0107\3\2\2\2\u00fd\u00fe\7\b\2\2\u00fe\u0100\5b\62\2\u00ff\u0101\5&"+
		"\24\2\u0100\u00ff\3\2\2\2\u0100\u0101\3\2\2\2\u0101\u0108\3\2\2\2\u0102"+
		"\u0103\7\n\2\2\u0103\u0104\5j\66\2\u0104\u0105\7.\2\2\u0105\u0106\5b\62"+
		"\2\u0106\u0108\3\2\2\2\u0107\u00fd\3\2\2\2\u0107\u0102\3\2\2\2\u0108\21"+
		"\3\2\2\2\u0109\u010b\7\64\2\2\u010a\u010c\7d\2\2\u010b\u010a\3\2\2\2\u010b"+
		"\u010c\3\2\2\2\u010c\u0119\3\2\2\2\u010d\u011a\7\t\2\2\u010e\u011a\7\22"+
		"\2\2\u010f\u011a\7\24\2\2\u0110\u0111\7\21\2\2\u0111\u011a\5~@\2\u0112"+
		"\u0113\7\23\2\2\u0113\u011a\5|?\2\u0114\u0115\7\13\2\2\u0115\u0116\7."+
		"\2\2\u0116\u011a\5b\62\2\u0117\u0118\7\b\2\2\u0118\u011a\5b\62\2\u0119"+
		"\u010d\3\2\2\2\u0119\u010e\3\2\2\2\u0119\u010f\3\2\2\2\u0119\u0110\3\2"+
		"\2\2\u0119\u0112\3\2\2\2\u0119\u0114\3\2\2\2\u0119\u0117\3\2\2\2\u011a"+
		"\23\3\2\2\2\u011b\u011f\7O\2\2\u011c\u0120\5\26\f\2\u011d\u0120\5\30\r"+
		"\2\u011e\u0120\5\32\16\2\u011f\u011c\3\2\2\2\u011f\u011d\3\2\2\2\u011f"+
		"\u011e\3\2\2\2\u0120\u0129\3\2\2\2\u0121\u0125\7M\2\2\u0122\u0126\5\26"+
		"\f\2\u0123\u0126\5\30\r\2\u0124\u0126\5\32\16\2\u0125\u0122\3\2\2\2\u0125"+
		"\u0123\3\2\2\2\u0125\u0124\3\2\2\2\u0126\u0128\3\2\2\2\u0127\u0121\3\2"+
		"\2\2\u0128\u012b\3\2\2\2\u0129\u0127\3\2\2\2\u0129\u012a\3\2\2\2\u012a"+
		"\u012c\3\2\2\2\u012b\u0129\3\2\2\2\u012c\u012d\7P\2\2\u012d\25\3\2\2\2"+
		"\u012e\u012f\7\r\2\2\u012f\u0130\5\64\33\2\u0130\27\3\2\2\2\u0131\u0132"+
		"\7\16\2\2\u0132\u0133\5b\62\2\u0133\31\3\2\2\2\u0134\u0135\7\20\2\2\u0135"+
		"\u0136\5\64\33\2\u0136\33\3\2\2\2\u0137\u013b\5\64\33\2\u0138\u013b\5"+
		"\36\20\2\u0139\u013b\5J&\2\u013a\u0137\3\2\2\2\u013a\u0138\3\2\2\2\u013a"+
		"\u0139\3\2\2\2\u013b\u0144\3\2\2\2\u013c\u0140\7M\2\2\u013d\u0141\5\64"+
		"\33\2\u013e\u0141\5\36\20\2\u013f\u0141\5J&\2\u0140\u013d\3\2\2\2\u0140"+
		"\u013e\3\2\2\2\u0140\u013f\3\2\2\2\u0141\u0143\3\2\2\2\u0142\u013c\3\2"+
		"\2\2\u0143\u0146\3\2\2\2\u0144\u0142\3\2\2\2\u0144\u0145\3\2\2\2\u0145"+
		"\35\3\2\2\2\u0146\u0144\3\2\2\2\u0147\u0148\7*\2\2\u0148\u014d\7O\2\2"+
		"\u0149\u014b\5 \21\2\u014a\u014c\7M\2\2\u014b\u014a\3\2\2\2\u014b\u014c"+
		"\3\2\2\2\u014c\u014e\3\2\2\2\u014d\u0149\3\2\2\2\u014d\u014e\3\2\2\2\u014e"+
		"\u0150\3\2\2\2\u014f\u0151\5$\23\2\u0150\u014f\3\2\2\2\u0150\u0151\3\2"+
		"\2\2\u0151\u0152\3\2\2\2\u0152\u0153\7P\2\2\u0153\37\3\2\2\2\u0154\u0155"+
		"\7/\2\2\u0155\u0156\5\62\32\2\u0156!\3\2\2\2\u0157\u0158\7O\2\2\u0158"+
		"\u0159\5$\23\2\u0159\u015a\7P\2\2\u015a\u016a\3\2\2\2\u015b\u015c\7O\2"+
		"\2\u015c\u015d\5$\23\2\u015d\u015e\b\22\1\2\u015e\u016a\3\2\2\2\u015f"+
		"\u0160\7O\2\2\u0160\u0165\t\3\2\2\u0161\u0162\7M\2\2\u0162\u0164\t\3\2"+
		"\2\u0163\u0161\3\2\2\2\u0164\u0167\3\2\2\2\u0165\u0163\3\2\2\2\u0165\u0166"+
		"\3\2\2\2\u0166\u0168\3\2\2\2\u0167\u0165\3\2\2\2\u0168\u016a\b\22\1\2"+
		"\u0169\u0157\3\2\2\2\u0169\u015b\3\2\2\2\u0169\u015f\3\2\2\2\u016a#\3"+
		"\2\2\2\u016b\u0170\5z>\2\u016c\u016d\7M\2\2\u016d\u016f\5z>\2\u016e\u016c"+
		"\3\2\2\2\u016f\u0172\3\2\2\2\u0170\u016e\3\2\2\2\u0170\u0171\3\2\2\2\u0171"+
		"%\3\2\2\2\u0172\u0170\3\2\2\2\u0173\u0174\7O\2\2\u0174\u0175\5(\25\2\u0175"+
		"\u0176\7P\2\2\u0176\u017c\3\2\2\2\u0177\u0178\7O\2\2\u0178\u0179\5(\25"+
		"\2\u0179\u017a\b\24\1\2\u017a\u017c\3\2\2\2\u017b\u0173\3\2\2\2\u017b"+
		"\u0177\3\2\2\2\u017c\'\3\2\2\2\u017d\u0182\5d\63\2\u017e\u017f\7M\2\2"+
		"\u017f\u0181\5d\63\2\u0180\u017e\3\2\2\2\u0181\u0184\3\2\2\2\u0182\u0180"+
		"\3\2\2\2\u0182\u0183\3\2\2\2\u0183)\3\2\2\2\u0184\u0182\3\2\2\2\u0185"+
		"\u0186\7O\2\2\u0186\u0187\5,\27\2\u0187\u0188\7P\2\2\u0188\u018e\3\2\2"+
		"\2\u0189\u018a\7O\2\2\u018a\u018b\5,\27\2\u018b\u018c\b\26\1\2\u018c\u018e"+
		"\3\2\2\2\u018d\u0185\3\2\2\2\u018d\u0189\3\2\2\2\u018e+\3\2\2\2\u018f"+
		"\u0194\5.\30\2\u0190\u0191\7M\2\2\u0191\u0193\5.\30\2\u0192\u0190\3\2"+
		"\2\2\u0193\u0196\3\2\2\2\u0194\u0192\3\2\2\2\u0194\u0195\3\2\2\2\u0195"+
		"-\3\2\2\2\u0196\u0194\3\2\2\2\u0197\u0199\5d\63\2\u0198\u019a\5\u00a4"+
		"S\2\u0199\u0198\3\2\2\2\u0199\u019a\3\2\2\2\u019a/\3\2\2\2\u019b\u019c"+
		"\7O\2\2\u019c\u01a1\7\\\2\2\u019d\u019e\7M\2\2\u019e\u01a0\7\\\2\2\u019f"+
		"\u019d\3\2\2\2\u01a0\u01a3\3\2\2\2\u01a1\u019f\3\2\2\2\u01a1\u01a2\3\2"+
		"\2\2\u01a2\u01a4\3\2\2\2\u01a3\u01a1\3\2\2\2\u01a4\u01b0\7P\2\2\u01a5"+
		"\u01a6\7O\2\2\u01a6\u01ab\7\\\2\2\u01a7\u01a8\7M\2\2\u01a8\u01aa\7\\\2"+
		"\2\u01a9\u01a7\3\2\2\2\u01aa\u01ad\3\2\2\2\u01ab\u01a9\3\2\2\2\u01ab\u01ac"+
		"\3\2\2\2\u01ac\u01ae\3\2\2\2\u01ad\u01ab\3\2\2\2\u01ae\u01b0\b\31\1\2"+
		"\u01af\u019b\3\2\2\2\u01af\u01a5\3\2\2\2\u01b0\61\3\2\2\2\u01b1\u01b2"+
		"\5\"\22\2\u01b2\63\3\2\2\2\u01b3\u01b4\5b\62\2\u01b4\u01b5\5\66\34\2\u01b5"+
		"\65\3\2\2\2\u01b6\u01c0\5B\"\2\u01b7\u01c0\5H%\2\u01b8\u01c0\5> \2\u01b9"+
		"\u01c0\5@!\2\u01ba\u01c0\5:\36\2\u01bb\u01c0\58\35\2\u01bc\u01c0\5F$\2"+
		"\u01bd\u01c0\5D#\2\u01be\u01c0\5<\37\2\u01bf\u01b6\3\2\2\2\u01bf\u01b7"+
		"\3\2\2\2\u01bf\u01b8\3\2\2\2\u01bf\u01b9\3\2\2\2\u01bf\u01ba\3\2\2\2\u01bf"+
		"\u01bb\3\2\2\2\u01bf\u01bc\3\2\2\2\u01bf\u01bd\3\2\2\2\u01bf\u01be\3\2"+
		"\2\2\u01c0\67\3\2\2\2\u01c1\u01c5\t\4\2\2\u01c2\u01c4\5N(\2\u01c3\u01c2"+
		"\3\2\2\2\u01c4\u01c7\3\2\2\2\u01c5\u01c3\3\2\2\2\u01c5\u01c6\3\2\2\2\u01c6"+
		"\u01c9\3\2\2\2\u01c7\u01c5\3\2\2\2\u01c8\u01ca\5J&\2\u01c9\u01c8\3\2\2"+
		"\2\u01c9\u01ca\3\2\2\2\u01ca\u01ce\3\2\2\2\u01cb\u01cd\5N(\2\u01cc\u01cb"+
		"\3\2\2\2\u01cd\u01d0\3\2\2\2\u01ce\u01cc\3\2\2\2\u01ce\u01cf\3\2\2\2\u01cf"+
		"9\3\2\2\2\u01d0\u01ce\3\2\2\2\u01d1\u01d5\t\5\2\2\u01d2\u01d4\5R*\2\u01d3"+
		"\u01d2\3\2\2\2\u01d4\u01d7\3\2\2\2\u01d5\u01d3\3\2\2\2\u01d5\u01d6\3\2"+
		"\2\2\u01d6\u01d9\3\2\2\2\u01d7\u01d5\3\2\2\2\u01d8\u01da\5J&\2\u01d9\u01d8"+
		"\3\2\2\2\u01d9\u01da\3\2\2\2\u01da\u01de\3\2\2\2\u01db\u01dd\5R*\2\u01dc"+
		"\u01db\3\2\2\2\u01dd\u01e0\3\2\2\2\u01de\u01dc\3\2\2\2\u01de\u01df\3\2"+
		"\2\2\u01df;\3\2\2\2\u01e0\u01de\3\2\2\2\u01e1\u01e5\7!\2\2\u01e2\u01e4"+
		"\5V,\2\u01e3\u01e2\3\2\2\2\u01e4\u01e7\3\2\2\2\u01e5\u01e3\3\2\2\2\u01e5"+
		"\u01e6\3\2\2\2\u01e6\u01e9\3\2\2\2\u01e7\u01e5\3\2\2\2\u01e8\u01ea\5J"+
		"&\2\u01e9\u01e8\3\2\2\2\u01e9\u01ea\3\2\2\2\u01ea\u01ee\3\2\2\2\u01eb"+
		"\u01ed\5V,\2\u01ec\u01eb\3\2\2\2\u01ed\u01f0\3\2\2\2\u01ee\u01ec\3\2\2"+
		"\2\u01ee\u01ef\3\2\2\2\u01ef=\3\2\2\2\u01f0\u01ee\3\2\2\2\u01f1\u01f5"+
		"\7\31\2\2\u01f2\u01f4\5L\'\2\u01f3\u01f2\3\2\2\2\u01f4\u01f7\3\2\2\2\u01f5"+
		"\u01f3\3\2\2\2\u01f5\u01f6\3\2\2\2\u01f6\u01f9\3\2\2\2\u01f7\u01f5\3\2"+
		"\2\2\u01f8\u01fa\5J&\2\u01f9\u01f8\3\2\2\2\u01f9\u01fa\3\2\2\2\u01fa\u01fe"+
		"\3\2\2\2\u01fb\u01fd\5L\'\2\u01fc\u01fb\3\2\2\2\u01fd\u0200\3\2\2\2\u01fe"+
		"\u01fc\3\2\2\2\u01fe\u01ff\3\2\2\2\u01ff?\3\2\2\2\u0200\u01fe\3\2\2\2"+
		"\u0201\u0202\7\33\2\2\u0202\u0206\5\"\22\2\u0203\u0205\5Z.\2\u0204\u0203"+
		"\3\2\2\2\u0205\u0208\3\2\2\2\u0206\u0204\3\2\2\2\u0206\u0207\3\2\2\2\u0207"+
		"\u020a\3\2\2\2\u0208\u0206\3\2\2\2\u0209\u020b\5J&\2\u020a\u0209\3\2\2"+
		"\2\u020a\u020b\3\2\2\2\u020b\u020f\3\2\2\2\u020c\u020e\5Z.\2\u020d\u020c"+
		"\3\2\2\2\u020e\u0211\3\2\2\2\u020f\u020d\3\2\2\2\u020f\u0210\3\2\2\2\u0210"+
		"A\3\2\2\2\u0211\u020f\3\2\2\2\u0212\u0222\7\30\2\2\u0213\u0217\7O\2\2"+
		"\u0214\u0216\7Y\2\2\u0215\u0214\3\2\2\2\u0216\u0219\3\2\2\2\u0217\u0215"+
		"\3\2\2\2\u0217\u0218\3\2\2\2\u0218\u021a\3\2\2\2\u0219\u0217\3\2\2\2\u021a"+
		"\u021e\7Z\2\2\u021b\u021d\7Y\2\2\u021c\u021b\3\2\2\2\u021d\u0220\3\2\2"+
		"\2\u021e\u021c\3\2\2\2\u021e\u021f\3\2\2\2\u021f\u0221\3\2\2\2\u0220\u021e"+
		"\3\2\2\2\u0221\u0223\7P\2\2\u0222\u0213\3\2\2\2\u0222\u0223\3\2\2\2\u0223"+
		"\u0225\3\2\2\2\u0224\u0226\5J&\2\u0225\u0224\3\2\2\2\u0225\u0226\3\2\2"+
		"\2\u0226C\3\2\2\2\u0227\u0228\7 \2\2\u0228\u0229\7O\2\2\u0229\u022e\5"+
		"\64\33\2\u022a\u022b\7M\2\2\u022b\u022d\5\64\33\2\u022c\u022a\3\2\2\2"+
		"\u022d\u0230\3\2\2\2\u022e\u022c\3\2\2\2\u022e\u022f\3\2\2\2\u022f\u0231"+
		"\3\2\2\2\u0230\u022e\3\2\2\2\u0231\u0233\7P\2\2\u0232\u0234\5J&\2\u0233"+
		"\u0232\3\2\2\2\u0233\u0234\3\2\2\2\u0234E\3\2\2\2\u0235\u0236\7\37\2\2"+
		"\u0236\u0237\7O\2\2\u0237\u0238\5\66\34\2\u0238\u023a\7P\2\2\u0239\u023b"+
		"\5J&\2\u023a\u0239\3\2\2\2\u023a\u023b\3\2\2\2\u023bG\3\2\2\2\u023c\u023d"+
		"\7\27\2\2\u023d\u023e\7O\2\2\u023e\u023f\5\66\34\2\u023f\u0241\7P\2\2"+
		"\u0240\u0242\5J&\2\u0241\u0240\3\2\2\2\u0241\u0242\3\2\2\2\u0242I\3\2"+
		"\2\2\u0243\u0244\7$\2\2\u0244\u0245\7\\\2\2\u0245K\3\2\2\2\u0246\u0247"+
		"\7%\2\2\u0247\u024a\7U\2\2\u0248\u024a\5`\61\2\u0249\u0246\3\2\2\2\u0249"+
		"\u0248\3\2\2\2\u024aM\3\2\2\2\u024b\u024f\5`\61\2\u024c\u024f\5P)\2\u024d"+
		"\u024f\5\\/\2\u024e\u024b\3\2\2\2\u024e\u024c\3\2\2\2\u024e\u024d\3\2"+
		"\2\2\u024fO\3\2\2\2\u0250\u0251\7%\2\2\u0251\u0252\7Z\2\2\u0252Q\3\2\2"+
		"\2\u0253\u0257\5`\61\2\u0254\u0257\5T+\2\u0255\u0257\5\\/\2\u0256\u0253"+
		"\3\2\2\2\u0256\u0254\3\2\2\2\u0256\u0255\3\2\2\2\u0257S\3\2\2\2\u0258"+
		"\u0259\7%\2\2\u0259\u025a\t\6\2\2\u025aU\3\2\2\2\u025b\u025f\5`\61\2\u025c"+
		"\u025f\5X-\2\u025d\u025f\5\\/\2\u025e\u025b\3\2\2\2\u025e\u025c\3\2\2"+
		"\2\u025e\u025d\3\2\2\2\u025fW\3\2\2\2\u0260\u0261\7%\2\2\u0261\u0267\7"+
		"\\\2\2\u0262\u0263\7%\2\2\u0263\u0264\5z>\2\u0264\u0265\b-\1\2\u0265\u0267"+
		"\3\2\2\2\u0266\u0260\3\2\2\2\u0266\u0262\3\2\2\2\u0267Y\3\2\2\2\u0268"+
		"\u026f\5`\61\2\u0269\u026a\7%\2\2\u026a\u026f\5z>\2\u026b\u026c\7%\2\2"+
		"\u026c\u026d\7\\\2\2\u026d\u026f\b.\1\2\u026e\u0268\3\2\2\2\u026e\u0269"+
		"\3\2\2\2\u026e\u026b\3\2\2\2\u026f[\3\2\2\2\u0270\u0271\7\62\2\2\u0271"+
		"\u0272\7O\2\2\u0272\u0277\5^\60\2\u0273\u0274\7\63\2\2\u0274\u0276\5^"+
		"\60\2\u0275\u0273\3\2\2\2\u0276\u0279\3\2\2\2\u0277\u0275\3\2\2\2\u0277"+
		"\u0278\3\2\2\2\u0278\u027a\3\2\2\2\u0279\u0277\3\2\2\2\u027a\u027b\7P"+
		"\2\2\u027b]\3\2\2\2\u027c\u027d\5z>\2\u027d\u027e\7V\2\2\u027e\u027f\t"+
		"\7\2\2\u027f\u0285\3\2\2\2\u0280\u0281\5h\65\2\u0281\u0282\7V\2\2\u0282"+
		"\u0283\t\7\2\2\u0283\u0285\3\2\2\2\u0284\u027c\3\2\2\2\u0284\u0280\3\2"+
		"\2\2\u0285_\3\2\2\2\u0286\u0287\7)\2\2\u0287a\3\2\2\2\u0288\u028b\5z>"+
		"\2\u0289\u028b\7X\2\2\u028a\u0288\3\2\2\2\u028a\u0289\3\2\2\2\u028b\u0291"+
		"\3\2\2\2\u028c\u028d\7b\2\2\u028d\u0291\b\62\1\2\u028e\u028f\7c\2\2\u028f"+
		"\u0291\b\62\1\2\u0290\u028a\3\2\2\2\u0290\u028c\3\2\2\2\u0290\u028e\3"+
		"\2\2\2\u0291c\3\2\2\2\u0292\u0296\5b\62\2\u0293\u0296\5f\64\2\u0294\u0296"+
		"\5h\65\2\u0295\u0292\3\2\2\2\u0295\u0293\3\2\2\2\u0295\u0294\3\2\2\2\u0296"+
		"e\3\2\2\2\u0297\u0298\7-\2\2\u0298\u0299\7O\2\2\u0299\u029a\5b\62\2\u029a"+
		"\u029b\7P\2\2\u029bg\3\2\2\2\u029c\u029d\7\66\2\2\u029d\u029e\7O\2\2\u029e"+
		"\u029f\5b\62\2\u029f\u02a2\7P\2\2\u02a0\u02a1\7\6\2\2\u02a1\u02a3\5b\62"+
		"\2\u02a2\u02a0\3\2\2\2\u02a2\u02a3\3\2\2\2\u02a3\u02a8\3\2\2\2\u02a4\u02a5"+
		"\7\66\2\2\u02a5\u02a6\7O\2\2\u02a6\u02a8\7P\2\2\u02a7\u029c\3\2\2\2\u02a7"+
		"\u02a4\3\2\2\2\u02a8i\3\2\2\2\u02a9\u02ad\5z>\2\u02aa\u02ab\7b\2\2\u02ab"+
		"\u02ad\b\66\1\2\u02ac\u02a9\3\2\2\2\u02ac\u02aa\3\2\2\2\u02adk\3\2\2\2"+
		"\u02ae\u02af\7\7\2\2\u02af\u02b0\7\21\2\2\u02b0\u02b2\5\u0082B\2\u02b1"+
		"\u02b3\5\u008cG\2\u02b2\u02b1\3\2\2\2\u02b2\u02b3\3\2\2\2\u02b3\u02b5"+
		"\3\2\2\2\u02b4\u02b6\7:\2\2\u02b5\u02b4\3\2\2\2\u02b5\u02b6\3\2\2\2\u02b6"+
		"m\3\2\2\2\u02b7\u02b8\7\7\2\2\u02b8\u02b9\7\23\2\2\u02b9\u02ba\5|?\2\u02ba"+
		"o\3\2\2\2\u02bb\u02bc\7\17\2\2\u02bc\u02bd\7\21\2\2\u02bd\u02bf\5~@\2"+
		"\u02be\u02c0\5\u008aF\2\u02bf\u02be\3\2\2\2\u02bf\u02c0\3\2\2\2\u02c0"+
		"\u02c2\3\2\2\2\u02c1\u02c3\7B\2\2\u02c2\u02c1\3\2\2\2\u02c2\u02c3\3\2"+
		"\2\2\u02c3\u02c5\3\2\2\2\u02c4\u02c6\7=\2\2\u02c5\u02c4\3\2\2\2\u02c5"+
		"\u02c6\3\2\2\2\u02c6\u02c8\3\2\2\2\u02c7\u02c9\5\u0086D\2\u02c8\u02c7"+
		"\3\2\2\2\u02c8\u02c9\3\2\2\2\u02c9\u02cb\3\2\2\2\u02ca\u02cc\5\u008cG"+
		"\2\u02cb\u02ca\3\2\2\2\u02cb\u02cc\3\2\2\2\u02ccq\3\2\2\2\u02cd\u02ce"+
		"\7\16\2\2\u02ce\u02cf\7\21\2\2\u02cf\u02d0\5~@\2\u02d0s\3\2\2\2\u02d1"+
		"\u02d2\7\16\2\2\u02d2\u02d3\7\23\2\2\u02d3\u02d4\5|?\2\u02d4u\3\2\2\2"+
		"\u02d5\u02d9\7\25\2\2\u02d6\u02da\5\u008eH\2\u02d7\u02da\5\u0090I\2\u02d8"+
		"\u02da\5\u0092J\2\u02d9\u02d6\3\2\2\2\u02d9\u02d7\3\2\2\2\u02d9\u02d8"+
		"\3\2\2\2\u02daw\3\2\2\2\u02db\u02df\7\26\2\2\u02dc\u02e0\5\u0094K\2\u02dd"+
		"\u02e0\5\u0096L\2\u02de\u02e0\5\u0098M\2\u02df\u02dc\3\2\2\2\u02df\u02dd"+
		"\3\2\2\2\u02df\u02de\3\2\2\2\u02e0y\3\2\2\2\u02e1\u02e2\t\b\2\2\u02e2"+
		"{\3\2\2\2\u02e3\u02e7\5z>\2\u02e4\u02e5\7b\2\2\u02e5\u02e7\b?\1\2\u02e6"+
		"\u02e3\3\2\2\2\u02e6\u02e4\3\2\2\2\u02e7}\3\2\2\2\u02e8\u02eb\5|?\2\u02e9"+
		"\u02eb\7\\\2\2\u02ea\u02e8\3\2\2\2\u02ea\u02e9\3\2\2\2\u02eb\177\3\2\2"+
		"\2\u02ec\u02ed\7\67\2\2\u02ed\u02ee\5\u0084C\2\u02ee\u0081\3\2\2\2\u02ef"+
		"\u02f0\5|?\2\u02f0\u02f2\5\u0080A\2\u02f1\u02f3\7=\2\2\u02f2\u02f1\3\2"+
		"\2\2\u02f2\u02f3\3\2\2\2\u02f3\u02f5\3\2\2\2\u02f4\u02f6\5\u0086D\2\u02f5"+
		"\u02f4\3\2\2\2\u02f5\u02f6\3\2\2\2\u02f6\u02fa\3\2\2\2\u02f7\u02f8\7\\"+
		"\2\2\u02f8\u02fa\7?\2\2\u02f9\u02ef\3\2\2\2\u02f9\u02f7\3\2\2\2\u02fa"+
		"\u0083\3\2\2\2\u02fb\u02fc\79\2\2\u02fc\u02fd\7\\\2\2\u02fd\u0085\3\2"+
		"\2\2\u02fe\u02ff\7>\2\2\u02ff\u0300\5\u0088E\2\u0300\u0087\3\2\2\2\u0301"+
		"\u0305\7Z\2\2\u0302\u0304\7Y\2\2\u0303\u0302\3\2\2\2\u0304\u0307\3\2\2"+
		"\2\u0305\u0303\3\2\2\2\u0305\u0306\3\2\2\2\u0306\u0308\3\2\2\2\u0307\u0305"+
		"\3\2\2\2\u0308\u0309\7@\2\2\u0309\u0089\3\2\2\2\u030a\u030c\5\u0080A\2"+
		"\u030b\u030d\7A\2\2\u030c\u030b\3\2\2\2\u030c\u030d\3\2\2\2\u030d\u008b"+
		"\3\2\2\2\u030e\u030f\7D\2\2\u030f\u0310\t\t\2\2\u0310\u008d\3\2\2\2\u0311"+
		"\u0312\5$\23\2\u0312\u0313\7G\2\2\u0313\u0314\5\u009aN\2\u0314\u008f\3"+
		"\2\2\2\u0315\u0316\5\u009cO\2\u0316\u0317\7G\2\2\u0317\u0318\5|?\2\u0318"+
		"\u0091\3\2\2\2\u0319\u031a\5\u00a0Q\2\u031a\u031b\7.\2\2\u031b\u031c\5"+
		"\u00a2R\2\u031c\u031d\7G\2\2\u031d\u031e\5|?\2\u031e\u0093\3\2\2\2\u031f"+
		"\u0320\5$\23\2\u0320\u0321\7H\2\2\u0321\u0322\5\u009aN\2\u0322\u0095\3"+
		"\2\2\2\u0323\u0324\5\u009cO\2\u0324\u0325\7H\2\2\u0325\u0326\5|?\2\u0326"+
		"\u0097\3\2\2\2\u0327\u0328\5\u00a0Q\2\u0328\u0329\7.\2\2\u0329\u032a\5"+
		"\u00a2R\2\u032a\u032b\7H\2\2\u032b\u032c\5|?\2\u032c\u0099\3\2\2\2\u032d"+
		"\u032e\7\21\2\2\u032e\u0332\5~@\2\u032f\u0330\7\23\2\2\u0330\u0332\5|"+
		"?\2\u0331\u032d\3\2\2\2\u0331\u032f\3\2\2\2\u0332\u009b\3\2\2\2\u0333"+
		"\u0338\5\u009eP\2\u0334\u0335\7M\2\2\u0335\u0337\5\u009eP\2\u0336\u0334"+
		"\3\2\2\2\u0337\u033a\3\2\2\2\u0338\u0336\3\2\2\2\u0338\u0339\3\2\2\2\u0339"+
		"\u009d\3\2\2\2\u033a\u0338\3\2\2\2\u033b\u033e\5|?\2\u033c\u033e\7K\2"+
		"\2\u033d\u033b\3\2\2\2\u033d\u033c\3\2\2\2\u033e\u009f\3\2\2\2\u033f\u0342"+
		"\5\u009eP\2\u0340\u0342\7I\2\2\u0341\u033f\3\2\2\2\u0341\u0340\3\2\2\2"+
		"\u0342\u034a\3\2\2\2\u0343\u0346\7M\2\2\u0344\u0347\5\u009eP\2\u0345\u0347"+
		"\7I\2\2\u0346\u0344\3\2\2\2\u0346\u0345\3\2\2\2\u0347\u0349\3\2\2\2\u0348"+
		"\u0343\3\2\2\2\u0349\u034c\3\2\2\2\u034a\u0348\3\2\2\2\u034a\u034b\3\2"+
		"\2\2\u034b\u00a1\3\2\2\2\u034c\u034a\3\2\2\2\u034d\u034e\5b\62\2\u034e"+
		"\u00a3\3\2\2\2\u034f\u0352\5\u00a6T\2\u0350\u0352\5\u00a8U\2\u0351\u034f"+
		"\3\2\2\2\u0351\u0350\3\2\2\2\u0352\u00a5\3\2\2\2\u0353\u0354\7S\2\2\u0354"+
		"\u0359\5\u00aaV\2\u0355\u0356\7M\2\2\u0356\u0358\5\u00aaV\2\u0357\u0355"+
		"\3\2\2\2\u0358\u035b\3\2\2\2\u0359\u0357\3\2\2\2\u0359\u035a\3\2\2\2\u035a"+
		"\u035c\3\2\2\2\u035b\u0359\3\2\2\2\u035c\u035d\7T\2\2\u035d\u0361\3\2"+
		"\2\2\u035e\u035f\7S\2\2\u035f\u0361\7T\2\2\u0360\u0353\3\2\2\2\u0360\u035e"+
		"\3\2\2\2\u0361\u00a7\3\2\2\2\u0362\u0363\7Q\2\2\u0363\u0368\5\u00acW\2"+
		"\u0364\u0365\7M\2\2\u0365\u0367\5\u00acW\2\u0366\u0364\3\2\2\2\u0367\u036a"+
		"\3\2\2\2\u0368\u0366\3\2\2\2\u0368\u0369\3\2\2\2\u0369\u036b\3\2\2\2\u036a"+
		"\u0368\3\2\2\2\u036b\u036c\7R\2\2\u036c\u0370\3\2\2\2\u036d\u036e\7Q\2"+
		"\2\u036e\u0370\7R\2\2\u036f\u0362\3\2\2\2\u036f\u036d\3\2\2\2\u0370\u00a9"+
		"\3\2\2\2\u0371\u0372\7\\\2\2\u0372\u0373\7N\2\2\u0373\u0374\5\u00acW\2"+
		"\u0374\u00ab\3\2\2\2\u0375\u037d\5\u00a6T\2\u0376\u037d\5\u00a8U\2\u0377"+
		"\u037d\7\\\2\2\u0378\u037d\7f\2\2\u0379\u037d\7\4\2\2\u037a\u037d\7\5"+
		"\2\2\u037b\u037d\7\3\2\2\u037c\u0375\3\2\2\2\u037c\u0376\3\2\2\2\u037c"+
		"\u0377\3\2\2\2\u037c\u0378\3\2\2\2\u037c\u0379\3\2\2\2\u037c\u037a\3\2"+
		"\2\2\u037c\u037b\3\2\2\2\u037d\u00ad\3\2\2\2e\u00bd\u00c4\u00ce\u00d5"+
		"\u00dc\u00e1\u00eb\u00f2\u00fb\u0100\u0107\u010b\u0119\u011f\u0125\u0129"+
		"\u013a\u0140\u0144\u014b\u014d\u0150\u0165\u0169\u0170\u017b\u0182\u018d"+
		"\u0194\u0199\u01a1\u01ab\u01af\u01bf\u01c5\u01c9\u01ce\u01d5\u01d9\u01de"+
		"\u01e5\u01e9\u01ee\u01f5\u01f9\u01fe\u0206\u020a\u020f\u0217\u021e\u0222"+
		"\u0225\u022e\u0233\u023a\u0241\u0249\u024e\u0256\u025e\u0266\u026e\u0277"+
		"\u0284\u028a\u0290\u0295\u02a2\u02a7\u02ac\u02b2\u02b5\u02bf\u02c2\u02c5"+
		"\u02c8\u02cb\u02d9\u02df\u02e6\u02ea\u02f2\u02f5\u02f9\u0305\u030c\u0331"+
		"\u0338\u033d\u0341\u0346\u034a\u0351\u0359\u0360\u0368\u036f\u037c";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}