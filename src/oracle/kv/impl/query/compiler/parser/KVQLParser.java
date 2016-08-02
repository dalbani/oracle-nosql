// Generated from /media/sdd/dev/nsdb/kv/kvstore/src/oracle/kv/impl/query/compiler/parser/KVQL.g4 by ANTLR 4.4
package oracle.kv.impl.query.compiler.parser;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class KVQLParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.4", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__1=1, T__0=2, ACCOUNT=3, ADD=4, ADMIN=5, ALL=6, ALTER=7, AND=8, AS=9, 
		ASC=10, BY=11, COMMENT=12, CREATE=13, DECLARE=14, DEFAULT=15, DESC=16, 
		DESCRIBE=17, DROP=18, ELEMENTOF=19, ES_SHARDS=20, ES_REPLICAS=21, EXISTS=22, 
		FIRST=23, FORCE_INDEX=24, FORCE_PRIMARY_INDEX=25, FROM=26, FULLTEXT=27, 
		GRANT=28, IDENTIFIED=29, IF=30, INDEX=31, INDEXES=32, JSON=33, KEY=34, 
		KEYOF=35, LAST=36, LIFETIME=37, LOCK=38, MODIFY=39, NOT=40, NULLS=41, 
		ON=42, OR=43, ORDER=44, PASSWORD=45, PREFER_INDEXES=46, PREFER_PRIMARY_INDEX=47, 
		PRIMARY=48, REVOKE=49, ROLE=50, ROLES=51, SELECT=52, SHARD=53, SHOW=54, 
		TABLE=55, TABLES=56, TIME_UNIT=57, TO=58, TTL=59, UNLOCK=60, USER=61, 
		USERS=62, USING=63, WHERE=64, ALL_PRIVILEGES=65, IDENTIFIED_EXTERNALLY=66, 
		PASSWORD_EXPIRE=67, RETAIN_CURRENT_PASSWORD=68, CLEAR_RETAINED_PASSWORD=69, 
		ARRAY_T=70, BINARY_T=71, BOOLEAN_T=72, DOUBLE_T=73, ENUM_T=74, FLOAT_T=75, 
		INTEGER_T=76, LONG_T=77, MAP_T=78, RECORD_T=79, STRING_T=80, SEMI=81, 
		COMMA=82, COLON=83, LP=84, RP=85, LBRACK=86, RBRACK=87, LBRACE=88, RBRACE=89, 
		STAR=90, DOT=91, DOLLAR=92, LT=93, LTE=94, GT=95, GTE=96, EQ=97, NEQ=98, 
		LT_ANY=99, LTE_ANY=100, GT_ANY=101, GTE_ANY=102, EQ_ANY=103, NEQ_ANY=104, 
		PLUS=105, MINUS=106, DIV=107, NULL=108, FALSE=109, TRUE=110, INT=111, 
		FLOAT=112, DSTRING=113, STRING=114, ID=115, BAD_ID=116, WS=117, C_COMMENT=118, 
		LINE_COMMENT=119, LINE_COMMENT1=120, UnrecognizedToken=121;
	public static final String[] tokenNames = {
		"<INVALID>", "'*/'", "'/*+'", "ACCOUNT", "ADD", "ADMIN", "ALL", "ALTER", 
		"AND", "AS", "ASC", "BY", "COMMENT", "CREATE", "DECLARE", "DEFAULT", "DESC", 
		"DESCRIBE", "DROP", "ELEMENTOF", "ES_SHARDS", "ES_REPLICAS", "EXISTS", 
		"FIRST", "FORCE_INDEX", "FORCE_PRIMARY_INDEX", "FROM", "FULLTEXT", "GRANT", 
		"IDENTIFIED", "IF", "INDEX", "INDEXES", "JSON", "KEY", "KEYOF", "LAST", 
		"LIFETIME", "LOCK", "MODIFY", "NOT", "NULLS", "ON", "OR", "ORDER", "PASSWORD", 
		"PREFER_INDEXES", "PREFER_PRIMARY_INDEX", "PRIMARY", "REVOKE", "ROLE", 
		"ROLES", "SELECT", "SHARD", "SHOW", "TABLE", "TABLES", "TIME_UNIT", "TO", 
		"TTL", "UNLOCK", "USER", "USERS", "USING", "WHERE", "ALL_PRIVILEGES", 
		"IDENTIFIED_EXTERNALLY", "PASSWORD_EXPIRE", "RETAIN_CURRENT_PASSWORD", 
		"CLEAR_RETAINED_PASSWORD", "ARRAY_T", "BINARY_T", "BOOLEAN_T", "DOUBLE_T", 
		"ENUM_T", "FLOAT_T", "INTEGER_T", "LONG_T", "MAP_T", "RECORD_T", "STRING_T", 
		"';'", "','", "':'", "'('", "')'", "'['", "']'", "'{'", "'}'", "'*'", 
		"'.'", "'$'", "'<'", "'<='", "'>'", "'>='", "'='", "'!='", "'<any'", "'<=any'", 
		"'>any'", "'>=any'", "'=any'", "'!=any'", "'+'", "'-'", "'/'", "NULL", 
		"FALSE", "TRUE", "INT", "FLOAT", "DSTRING", "STRING", "ID", "BAD_ID", 
		"WS", "C_COMMENT", "LINE_COMMENT", "LINE_COMMENT1", "UnrecognizedToken"
	};
	public static final int
		RULE_parse = 0, RULE_statement = 1, RULE_query = 2, RULE_prolog = 3, RULE_var_decl = 4, 
		RULE_var_name = 5, RULE_expr = 6, RULE_sfw_expr = 7, RULE_from_clause = 8, 
		RULE_tab_alias = 9, RULE_where_clause = 10, RULE_select_clause = 11, RULE_hints = 12, 
		RULE_hint = 13, RULE_col_alias = 14, RULE_orderby_clause = 15, RULE_sort_spec = 16, 
		RULE_or_expr = 17, RULE_and_expr = 18, RULE_comp_expr = 19, RULE_comp_op = 20, 
		RULE_any_op = 21, RULE_add_expr = 22, RULE_multiply_expr = 23, RULE_unary_expr = 24, 
		RULE_path_expr = 25, RULE_field_step = 26, RULE_slice_step = 27, RULE_filter_step = 28, 
		RULE_primary_expr = 29, RULE_column_ref = 30, RULE_const_expr = 31, RULE_var_ref = 32, 
		RULE_array_constructor = 33, RULE_func_call = 34, RULE_parenthesized_expr = 35, 
		RULE_type_def = 36, RULE_record_def = 37, RULE_field_def = 38, RULE_default_def = 39, 
		RULE_default_value = 40, RULE_not_null = 41, RULE_map_def = 42, RULE_array_def = 43, 
		RULE_integer_def = 44, RULE_float_def = 45, RULE_string_def = 46, RULE_enum_def = 47, 
		RULE_boolean_def = 48, RULE_binary_def = 49, RULE_name_path = 50, RULE_create_table_statement = 51, 
		RULE_table_name = 52, RULE_table_def = 53, RULE_key_def = 54, RULE_shard_key_def = 55, 
		RULE_id_list_with_size = 56, RULE_id_with_size = 57, RULE_storage_size = 58, 
		RULE_ttl_def = 59, RULE_alter_table_statement = 60, RULE_alter_def = 61, 
		RULE_alter_field_statement = 62, RULE_add_field_statement = 63, RULE_drop_field_statement = 64, 
		RULE_modify_field_statement = 65, RULE_drop_table_statement = 66, RULE_create_index_statement = 67, 
		RULE_index_name = 68, RULE_path_list = 69, RULE_complex_name_path = 70, 
		RULE_keyof_expr = 71, RULE_elementof_expr = 72, RULE_create_text_index_statement = 73, 
		RULE_fts_field_list = 74, RULE_fts_path_list = 75, RULE_fts_path = 76, 
		RULE_es_properties = 77, RULE_es_property_assignment = 78, RULE_drop_index_statement = 79, 
		RULE_describe_statement = 80, RULE_show_statement = 81, RULE_create_user_statement = 82, 
		RULE_create_role_statement = 83, RULE_alter_user_statement = 84, RULE_drop_user_statement = 85, 
		RULE_drop_role_statement = 86, RULE_grant_statement = 87, RULE_revoke_statement = 88, 
		RULE_identifier_or_string = 89, RULE_identified_clause = 90, RULE_create_user_identified_clause = 91, 
		RULE_by_password = 92, RULE_password_lifetime = 93, RULE_reset_password_clause = 94, 
		RULE_account_lock = 95, RULE_grant_roles = 96, RULE_grant_system_privileges = 97, 
		RULE_grant_object_privileges = 98, RULE_revoke_roles = 99, RULE_revoke_system_privileges = 100, 
		RULE_revoke_object_privileges = 101, RULE_principal = 102, RULE_sys_priv_list = 103, 
		RULE_priv_item = 104, RULE_obj_priv_list = 105, RULE_object = 106, RULE_json = 107, 
		RULE_jsobject = 108, RULE_jsarray = 109, RULE_jspair = 110, RULE_jsvalue = 111, 
		RULE_comment = 112, RULE_duration = 113, RULE_number = 114, RULE_string = 115, 
		RULE_id_list = 116, RULE_id = 117;
	public static final String[] ruleNames = {
		"parse", "statement", "query", "prolog", "var_decl", "var_name", "expr", 
		"sfw_expr", "from_clause", "tab_alias", "where_clause", "select_clause", 
		"hints", "hint", "col_alias", "orderby_clause", "sort_spec", "or_expr", 
		"and_expr", "comp_expr", "comp_op", "any_op", "add_expr", "multiply_expr", 
		"unary_expr", "path_expr", "field_step", "slice_step", "filter_step", 
		"primary_expr", "column_ref", "const_expr", "var_ref", "array_constructor", 
		"func_call", "parenthesized_expr", "type_def", "record_def", "field_def", 
		"default_def", "default_value", "not_null", "map_def", "array_def", "integer_def", 
		"float_def", "string_def", "enum_def", "boolean_def", "binary_def", "name_path", 
		"create_table_statement", "table_name", "table_def", "key_def", "shard_key_def", 
		"id_list_with_size", "id_with_size", "storage_size", "ttl_def", "alter_table_statement", 
		"alter_def", "alter_field_statement", "add_field_statement", "drop_field_statement", 
		"modify_field_statement", "drop_table_statement", "create_index_statement", 
		"index_name", "path_list", "complex_name_path", "keyof_expr", "elementof_expr", 
		"create_text_index_statement", "fts_field_list", "fts_path_list", "fts_path", 
		"es_properties", "es_property_assignment", "drop_index_statement", "describe_statement", 
		"show_statement", "create_user_statement", "create_role_statement", "alter_user_statement", 
		"drop_user_statement", "drop_role_statement", "grant_statement", "revoke_statement", 
		"identifier_or_string", "identified_clause", "create_user_identified_clause", 
		"by_password", "password_lifetime", "reset_password_clause", "account_lock", 
		"grant_roles", "grant_system_privileges", "grant_object_privileges", "revoke_roles", 
		"revoke_system_privileges", "revoke_object_privileges", "principal", "sys_priv_list", 
		"priv_item", "obj_priv_list", "object", "json", "jsobject", "jsarray", 
		"jspair", "jsvalue", "comment", "duration", "number", "string", "id_list", 
		"id"
	};

	@Override
	public String getGrammarFileName() { return "KVQL.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public KVQLParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class ParseContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(KVQLParser.EOF, 0); }
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public ParseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parse; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterParse(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitParse(this);
		}
	}

	public final ParseContext parse() throws RecognitionException {
		ParseContext _localctx = new ParseContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_parse);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(236); statement();
			setState(237); match(EOF);
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

	public static class StatementContext extends ParserRuleContext {
		public Create_text_index_statementContext create_text_index_statement() {
			return getRuleContext(Create_text_index_statementContext.class,0);
		}
		public Grant_statementContext grant_statement() {
			return getRuleContext(Grant_statementContext.class,0);
		}
		public Revoke_statementContext revoke_statement() {
			return getRuleContext(Revoke_statementContext.class,0);
		}
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
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
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitStatement(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(255);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				{
				setState(239); query();
				}
				break;
			case 2:
				{
				setState(240); create_table_statement();
				}
				break;
			case 3:
				{
				setState(241); create_index_statement();
				}
				break;
			case 4:
				{
				setState(242); create_user_statement();
				}
				break;
			case 5:
				{
				setState(243); create_role_statement();
				}
				break;
			case 6:
				{
				setState(244); drop_index_statement();
				}
				break;
			case 7:
				{
				setState(245); create_text_index_statement();
				}
				break;
			case 8:
				{
				setState(246); drop_role_statement();
				}
				break;
			case 9:
				{
				setState(247); drop_user_statement();
				}
				break;
			case 10:
				{
				setState(248); alter_table_statement();
				}
				break;
			case 11:
				{
				setState(249); alter_user_statement();
				}
				break;
			case 12:
				{
				setState(250); drop_table_statement();
				}
				break;
			case 13:
				{
				setState(251); grant_statement();
				}
				break;
			case 14:
				{
				setState(252); revoke_statement();
				}
				break;
			case 15:
				{
				setState(253); describe_statement();
				}
				break;
			case 16:
				{
				setState(254); show_statement();
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

	public static class QueryContext extends ParserRuleContext {
		public PrologContext prolog() {
			return getRuleContext(PrologContext.class,0);
		}
		public Sfw_exprContext sfw_expr() {
			return getRuleContext(Sfw_exprContext.class,0);
		}
		public QueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_query; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitQuery(this);
		}
	}

	public final QueryContext query() throws RecognitionException {
		QueryContext _localctx = new QueryContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_query);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(258);
			_la = _input.LA(1);
			if (_la==DECLARE) {
				{
				setState(257); prolog();
				}
			}

			setState(260); sfw_expr();
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

	public static class PrologContext extends ParserRuleContext {
		public TerminalNode DECLARE() { return getToken(KVQLParser.DECLARE, 0); }
		public List<TerminalNode> SEMI() { return getTokens(KVQLParser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(KVQLParser.SEMI, i);
		}
		public Var_declContext var_decl(int i) {
			return getRuleContext(Var_declContext.class,i);
		}
		public List<Var_declContext> var_decl() {
			return getRuleContexts(Var_declContext.class);
		}
		public PrologContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_prolog; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterProlog(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitProlog(this);
		}
	}

	public final PrologContext prolog() throws RecognitionException {
		PrologContext _localctx = new PrologContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_prolog);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(262); match(DECLARE);
			setState(263); var_decl();
			setState(264); match(SEMI);
			setState(270);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOLLAR) {
				{
				{
				setState(265); var_decl();
				setState(266); match(SEMI);
				}
				}
				setState(272);
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

	public static class Var_declContext extends ParserRuleContext {
		public Var_nameContext var_name() {
			return getRuleContext(Var_nameContext.class,0);
		}
		public Type_defContext type_def() {
			return getRuleContext(Type_defContext.class,0);
		}
		public Var_declContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var_decl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterVar_decl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitVar_decl(this);
		}
	}

	public final Var_declContext var_decl() throws RecognitionException {
		Var_declContext _localctx = new Var_declContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_var_decl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(273); var_name();
			setState(274); type_def();
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

	public static class Var_nameContext extends ParserRuleContext {
		public TerminalNode DOLLAR() { return getToken(KVQLParser.DOLLAR, 0); }
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public Var_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterVar_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitVar_name(this);
		}
	}

	public final Var_nameContext var_name() throws RecognitionException {
		Var_nameContext _localctx = new Var_nameContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_var_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(276); match(DOLLAR);
			setState(277); id();
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
		public Sfw_exprContext sfw_expr() {
			return getRuleContext(Sfw_exprContext.class,0);
		}
		public Or_exprContext or_expr() {
			return getRuleContext(Or_exprContext.class,0);
		}
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitExpr(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		ExprContext _localctx = new ExprContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_expr);
		try {
			setState(281);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(279); sfw_expr();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(280); or_expr(0);
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

	public static class Sfw_exprContext extends ParserRuleContext {
		public From_clauseContext from_clause() {
			return getRuleContext(From_clauseContext.class,0);
		}
		public Where_clauseContext where_clause() {
			return getRuleContext(Where_clauseContext.class,0);
		}
		public Orderby_clauseContext orderby_clause() {
			return getRuleContext(Orderby_clauseContext.class,0);
		}
		public Select_clauseContext select_clause() {
			return getRuleContext(Select_clauseContext.class,0);
		}
		public Sfw_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sfw_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterSfw_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitSfw_expr(this);
		}
	}

	public final Sfw_exprContext sfw_expr() throws RecognitionException {
		Sfw_exprContext _localctx = new Sfw_exprContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_sfw_expr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(283); select_clause();
			setState(284); from_clause();
			setState(286);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(285); where_clause();
				}
			}

			setState(289);
			_la = _input.LA(1);
			if (_la==ORDER) {
				{
				setState(288); orderby_clause();
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

	public static class From_clauseContext extends ParserRuleContext {
		public Name_pathContext name_path() {
			return getRuleContext(Name_pathContext.class,0);
		}
		public Tab_aliasContext tab_alias() {
			return getRuleContext(Tab_aliasContext.class,0);
		}
		public TerminalNode FROM() { return getToken(KVQLParser.FROM, 0); }
		public TerminalNode AS() { return getToken(KVQLParser.AS, 0); }
		public From_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_from_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterFrom_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitFrom_clause(this);
		}
	}

	public final From_clauseContext from_clause() throws RecognitionException {
		From_clauseContext _localctx = new From_clauseContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_from_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(291); match(FROM);
			setState(292); name_path();
			setState(297);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				{
				setState(294);
				switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
				case 1:
					{
					setState(293); match(AS);
					}
					break;
				}
				setState(296); tab_alias();
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

	public static class Tab_aliasContext extends ParserRuleContext {
		public TerminalNode DOLLAR() { return getToken(KVQLParser.DOLLAR, 0); }
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public Tab_aliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tab_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterTab_alias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitTab_alias(this);
		}
	}

	public final Tab_aliasContext tab_alias() throws RecognitionException {
		Tab_aliasContext _localctx = new Tab_aliasContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_tab_alias);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(300);
			_la = _input.LA(1);
			if (_la==DOLLAR) {
				{
				setState(299); match(DOLLAR);
				}
			}

			setState(302); id();
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

	public static class Where_clauseContext extends ParserRuleContext {
		public TerminalNode WHERE() { return getToken(KVQLParser.WHERE, 0); }
		public Or_exprContext or_expr() {
			return getRuleContext(Or_exprContext.class,0);
		}
		public Where_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_where_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterWhere_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitWhere_clause(this);
		}
	}

	public final Where_clauseContext where_clause() throws RecognitionException {
		Where_clauseContext _localctx = new Where_clauseContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_where_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(304); match(WHERE);
			setState(305); or_expr(0);
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

	public static class Select_clauseContext extends ParserRuleContext {
		public List<TerminalNode> COMMA() { return getTokens(KVQLParser.COMMA); }
		public Or_exprContext or_expr(int i) {
			return getRuleContext(Or_exprContext.class,i);
		}
		public TerminalNode SELECT() { return getToken(KVQLParser.SELECT, 0); }
		public TerminalNode STAR() { return getToken(KVQLParser.STAR, 0); }
		public List<Or_exprContext> or_expr() {
			return getRuleContexts(Or_exprContext.class);
		}
		public HintsContext hints() {
			return getRuleContext(HintsContext.class,0);
		}
		public List<Col_aliasContext> col_alias() {
			return getRuleContexts(Col_aliasContext.class);
		}
		public TerminalNode COMMA(int i) {
			return getToken(KVQLParser.COMMA, i);
		}
		public Col_aliasContext col_alias(int i) {
			return getRuleContext(Col_aliasContext.class,i);
		}
		public Select_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_select_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterSelect_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitSelect_clause(this);
		}
	}

	public final Select_clauseContext select_clause() throws RecognitionException {
		Select_clauseContext _localctx = new Select_clauseContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_select_clause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(307); match(SELECT);
			setState(309);
			_la = _input.LA(1);
			if (_la==T__0) {
				{
				setState(308); hints();
				}
			}

			setState(323);
			switch (_input.LA(1)) {
			case STAR:
				{
				setState(311); match(STAR);
				}
				break;
			case ACCOUNT:
			case ADD:
			case ADMIN:
			case ALL:
			case ALTER:
			case AND:
			case AS:
			case ASC:
			case BY:
			case COMMENT:
			case CREATE:
			case DECLARE:
			case DEFAULT:
			case DESC:
			case DESCRIBE:
			case DROP:
			case ELEMENTOF:
			case ES_SHARDS:
			case ES_REPLICAS:
			case EXISTS:
			case FIRST:
			case FROM:
			case FULLTEXT:
			case GRANT:
			case IDENTIFIED:
			case IF:
			case INDEX:
			case INDEXES:
			case JSON:
			case KEY:
			case KEYOF:
			case LAST:
			case LIFETIME:
			case LOCK:
			case MODIFY:
			case NOT:
			case NULLS:
			case ON:
			case OR:
			case ORDER:
			case PASSWORD:
			case PRIMARY:
			case REVOKE:
			case ROLE:
			case ROLES:
			case SELECT:
			case SHARD:
			case SHOW:
			case TABLE:
			case TABLES:
			case TIME_UNIT:
			case TO:
			case TTL:
			case UNLOCK:
			case USER:
			case USERS:
			case USING:
			case WHERE:
			case ARRAY_T:
			case BINARY_T:
			case BOOLEAN_T:
			case DOUBLE_T:
			case ENUM_T:
			case FLOAT_T:
			case INTEGER_T:
			case LONG_T:
			case MAP_T:
			case RECORD_T:
			case STRING_T:
			case LP:
			case LBRACK:
			case DOLLAR:
			case PLUS:
			case MINUS:
			case FALSE:
			case TRUE:
			case INT:
			case FLOAT:
			case DSTRING:
			case STRING:
			case ID:
			case BAD_ID:
				{
				{
				setState(312); or_expr(0);
				setState(313); col_alias();
				setState(320);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(314); match(COMMA);
					setState(315); or_expr(0);
					setState(316); col_alias();
					}
					}
					setState(322);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
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

	public static class HintsContext extends ParserRuleContext {
		public HintContext hint(int i) {
			return getRuleContext(HintContext.class,i);
		}
		public List<HintContext> hint() {
			return getRuleContexts(HintContext.class);
		}
		public HintsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hints; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterHints(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitHints(this);
		}
	}

	public final HintsContext hints() throws RecognitionException {
		HintsContext _localctx = new HintsContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_hints);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(325); match(T__0);
			setState(329);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << FORCE_INDEX) | (1L << FORCE_PRIMARY_INDEX) | (1L << PREFER_INDEXES) | (1L << PREFER_PRIMARY_INDEX))) != 0)) {
				{
				{
				setState(326); hint();
				}
				}
				setState(331);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(332); match(T__1);
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

	public static class HintContext extends ParserRuleContext {
		public TerminalNode RP() { return getToken(KVQLParser.RP, 0); }
		public Name_pathContext name_path() {
			return getRuleContext(Name_pathContext.class,0);
		}
		public TerminalNode LP() { return getToken(KVQLParser.LP, 0); }
		public TerminalNode FORCE_INDEX() { return getToken(KVQLParser.FORCE_INDEX, 0); }
		public List<Index_nameContext> index_name() {
			return getRuleContexts(Index_nameContext.class);
		}
		public TerminalNode FORCE_PRIMARY_INDEX() { return getToken(KVQLParser.FORCE_PRIMARY_INDEX, 0); }
		public TerminalNode STRING() { return getToken(KVQLParser.STRING, 0); }
		public Index_nameContext index_name(int i) {
			return getRuleContext(Index_nameContext.class,i);
		}
		public TerminalNode PREFER_INDEXES() { return getToken(KVQLParser.PREFER_INDEXES, 0); }
		public TerminalNode PREFER_PRIMARY_INDEX() { return getToken(KVQLParser.PREFER_PRIMARY_INDEX, 0); }
		public HintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterHint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitHint(this);
		}
	}

	public final HintContext hint() throws RecognitionException {
		HintContext _localctx = new HintContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_hint);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(361);
			switch (_input.LA(1)) {
			case PREFER_INDEXES:
				{
				{
				setState(334); match(PREFER_INDEXES);
				setState(335); match(LP);
				setState(336); name_path();
				setState(340);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACCOUNT) | (1L << ADD) | (1L << ADMIN) | (1L << ALL) | (1L << ALTER) | (1L << AND) | (1L << AS) | (1L << ASC) | (1L << BY) | (1L << COMMENT) | (1L << CREATE) | (1L << DECLARE) | (1L << DEFAULT) | (1L << DESC) | (1L << DESCRIBE) | (1L << DROP) | (1L << ELEMENTOF) | (1L << ES_SHARDS) | (1L << ES_REPLICAS) | (1L << EXISTS) | (1L << FIRST) | (1L << FROM) | (1L << FULLTEXT) | (1L << GRANT) | (1L << IDENTIFIED) | (1L << IF) | (1L << INDEX) | (1L << INDEXES) | (1L << JSON) | (1L << KEY) | (1L << KEYOF) | (1L << LAST) | (1L << LIFETIME) | (1L << LOCK) | (1L << MODIFY) | (1L << NOT) | (1L << NULLS) | (1L << ON) | (1L << OR) | (1L << ORDER) | (1L << PASSWORD) | (1L << PRIMARY) | (1L << REVOKE) | (1L << ROLE) | (1L << ROLES) | (1L << SELECT) | (1L << SHARD) | (1L << SHOW) | (1L << TABLE) | (1L << TABLES) | (1L << TIME_UNIT) | (1L << TO) | (1L << TTL) | (1L << UNLOCK) | (1L << USER) | (1L << USERS) | (1L << USING))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (WHERE - 64)) | (1L << (ARRAY_T - 64)) | (1L << (BINARY_T - 64)) | (1L << (BOOLEAN_T - 64)) | (1L << (DOUBLE_T - 64)) | (1L << (ENUM_T - 64)) | (1L << (FLOAT_T - 64)) | (1L << (INTEGER_T - 64)) | (1L << (LONG_T - 64)) | (1L << (MAP_T - 64)) | (1L << (RECORD_T - 64)) | (1L << (STRING_T - 64)) | (1L << (ID - 64)) | (1L << (BAD_ID - 64)))) != 0)) {
					{
					{
					setState(337); index_name();
					}
					}
					setState(342);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(343); match(RP);
				}
				}
				break;
			case FORCE_INDEX:
				{
				{
				setState(345); match(FORCE_INDEX);
				setState(346); match(LP);
				setState(347); name_path();
				setState(348); index_name();
				setState(349); match(RP);
				}
				}
				break;
			case PREFER_PRIMARY_INDEX:
				{
				{
				setState(351); match(PREFER_PRIMARY_INDEX);
				setState(352); match(LP);
				setState(353); name_path();
				setState(354); match(RP);
				}
				}
				break;
			case FORCE_PRIMARY_INDEX:
				{
				{
				setState(356); match(FORCE_PRIMARY_INDEX);
				setState(357); match(LP);
				setState(358); name_path();
				setState(359); match(RP);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(364);
			_la = _input.LA(1);
			if (_la==STRING) {
				{
				setState(363); match(STRING);
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

	public static class Col_aliasContext extends ParserRuleContext {
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public TerminalNode AS() { return getToken(KVQLParser.AS, 0); }
		public Col_aliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_col_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterCol_alias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitCol_alias(this);
		}
	}

	public final Col_aliasContext col_alias() throws RecognitionException {
		Col_aliasContext _localctx = new Col_aliasContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_col_alias);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(368);
			_la = _input.LA(1);
			if (_la==AS) {
				{
				setState(366); match(AS);
				setState(367); id();
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

	public static class Orderby_clauseContext extends ParserRuleContext {
		public TerminalNode ORDER() { return getToken(KVQLParser.ORDER, 0); }
		public List<TerminalNode> COMMA() { return getTokens(KVQLParser.COMMA); }
		public List<Sort_specContext> sort_spec() {
			return getRuleContexts(Sort_specContext.class);
		}
		public Or_exprContext or_expr(int i) {
			return getRuleContext(Or_exprContext.class,i);
		}
		public Sort_specContext sort_spec(int i) {
			return getRuleContext(Sort_specContext.class,i);
		}
		public List<Or_exprContext> or_expr() {
			return getRuleContexts(Or_exprContext.class);
		}
		public TerminalNode BY() { return getToken(KVQLParser.BY, 0); }
		public TerminalNode COMMA(int i) {
			return getToken(KVQLParser.COMMA, i);
		}
		public Orderby_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderby_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterOrderby_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitOrderby_clause(this);
		}
	}

	public final Orderby_clauseContext orderby_clause() throws RecognitionException {
		Orderby_clauseContext _localctx = new Orderby_clauseContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_orderby_clause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(370); match(ORDER);
			setState(371); match(BY);
			setState(372); or_expr(0);
			setState(373); sort_spec();
			setState(380);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(374); match(COMMA);
				setState(375); or_expr(0);
				setState(376); sort_spec();
				}
				}
				setState(382);
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

	public static class Sort_specContext extends ParserRuleContext {
		public TerminalNode ASC() { return getToken(KVQLParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(KVQLParser.DESC, 0); }
		public TerminalNode NULLS() { return getToken(KVQLParser.NULLS, 0); }
		public TerminalNode FIRST() { return getToken(KVQLParser.FIRST, 0); }
		public TerminalNode LAST() { return getToken(KVQLParser.LAST, 0); }
		public Sort_specContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sort_spec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterSort_spec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitSort_spec(this);
		}
	}

	public final Sort_specContext sort_spec() throws RecognitionException {
		Sort_specContext _localctx = new Sort_specContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_sort_spec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(384);
			_la = _input.LA(1);
			if (_la==ASC || _la==DESC) {
				{
				setState(383);
				_la = _input.LA(1);
				if ( !(_la==ASC || _la==DESC) ) {
				_errHandler.recoverInline(this);
				}
				consume();
				}
			}

			setState(388);
			_la = _input.LA(1);
			if (_la==NULLS) {
				{
				setState(386); match(NULLS);
				setState(387);
				_la = _input.LA(1);
				if ( !(_la==FIRST || _la==LAST) ) {
				_errHandler.recoverInline(this);
				}
				consume();
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

	public static class Or_exprContext extends ParserRuleContext {
		public And_exprContext and_expr() {
			return getRuleContext(And_exprContext.class,0);
		}
		public TerminalNode OR() { return getToken(KVQLParser.OR, 0); }
		public Or_exprContext or_expr() {
			return getRuleContext(Or_exprContext.class,0);
		}
		public Or_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_or_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterOr_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitOr_expr(this);
		}
	}

	public final Or_exprContext or_expr() throws RecognitionException {
		return or_expr(0);
	}

	private Or_exprContext or_expr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		Or_exprContext _localctx = new Or_exprContext(_ctx, _parentState);
		Or_exprContext _prevctx = _localctx;
		int _startState = 34;
		enterRecursionRule(_localctx, 34, RULE_or_expr, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(391); and_expr(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(398);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Or_exprContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_or_expr);
					setState(393);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(394); match(OR);
					setState(395); and_expr(0);
					}
					} 
				}
				setState(400);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class And_exprContext extends ParserRuleContext {
		public And_exprContext and_expr() {
			return getRuleContext(And_exprContext.class,0);
		}
		public Comp_exprContext comp_expr() {
			return getRuleContext(Comp_exprContext.class,0);
		}
		public TerminalNode AND() { return getToken(KVQLParser.AND, 0); }
		public And_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_and_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterAnd_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitAnd_expr(this);
		}
	}

	public final And_exprContext and_expr() throws RecognitionException {
		return and_expr(0);
	}

	private And_exprContext and_expr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		And_exprContext _localctx = new And_exprContext(_ctx, _parentState);
		And_exprContext _prevctx = _localctx;
		int _startState = 36;
		enterRecursionRule(_localctx, 36, RULE_and_expr, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(402); comp_expr();
			}
			_ctx.stop = _input.LT(-1);
			setState(409);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new And_exprContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_and_expr);
					setState(404);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(405); match(AND);
					setState(406); comp_expr();
					}
					} 
				}
				setState(411);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class Comp_exprContext extends ParserRuleContext {
		public List<Add_exprContext> add_expr() {
			return getRuleContexts(Add_exprContext.class);
		}
		public Comp_opContext comp_op() {
			return getRuleContext(Comp_opContext.class,0);
		}
		public Add_exprContext add_expr(int i) {
			return getRuleContext(Add_exprContext.class,i);
		}
		public Any_opContext any_op() {
			return getRuleContext(Any_opContext.class,0);
		}
		public Comp_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comp_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterComp_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitComp_expr(this);
		}
	}

	public final Comp_exprContext comp_expr() throws RecognitionException {
		Comp_exprContext _localctx = new Comp_exprContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_comp_expr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(412); add_expr();
			setState(419);
			switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
			case 1:
				{
				setState(415);
				switch (_input.LA(1)) {
				case LT:
				case LTE:
				case GT:
				case GTE:
				case EQ:
				case NEQ:
					{
					setState(413); comp_op();
					}
					break;
				case LT_ANY:
				case LTE_ANY:
				case GT_ANY:
				case GTE_ANY:
				case EQ_ANY:
				case NEQ_ANY:
					{
					setState(414); any_op();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(417); add_expr();
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

	public static class Comp_opContext extends ParserRuleContext {
		public TerminalNode NEQ() { return getToken(KVQLParser.NEQ, 0); }
		public TerminalNode GTE() { return getToken(KVQLParser.GTE, 0); }
		public TerminalNode LT() { return getToken(KVQLParser.LT, 0); }
		public TerminalNode GT() { return getToken(KVQLParser.GT, 0); }
		public TerminalNode EQ() { return getToken(KVQLParser.EQ, 0); }
		public TerminalNode LTE() { return getToken(KVQLParser.LTE, 0); }
		public Comp_opContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comp_op; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterComp_op(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitComp_op(this);
		}
	}

	public final Comp_opContext comp_op() throws RecognitionException {
		Comp_opContext _localctx = new Comp_opContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_comp_op);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(421);
			_la = _input.LA(1);
			if ( !(((((_la - 93)) & ~0x3f) == 0 && ((1L << (_la - 93)) & ((1L << (LT - 93)) | (1L << (LTE - 93)) | (1L << (GT - 93)) | (1L << (GTE - 93)) | (1L << (EQ - 93)) | (1L << (NEQ - 93)))) != 0)) ) {
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

	public static class Any_opContext extends ParserRuleContext {
		public TerminalNode GTE_ANY() { return getToken(KVQLParser.GTE_ANY, 0); }
		public TerminalNode GT_ANY() { return getToken(KVQLParser.GT_ANY, 0); }
		public TerminalNode LTE_ANY() { return getToken(KVQLParser.LTE_ANY, 0); }
		public TerminalNode EQ_ANY() { return getToken(KVQLParser.EQ_ANY, 0); }
		public TerminalNode LT_ANY() { return getToken(KVQLParser.LT_ANY, 0); }
		public TerminalNode NEQ_ANY() { return getToken(KVQLParser.NEQ_ANY, 0); }
		public Any_opContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_any_op; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterAny_op(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitAny_op(this);
		}
	}

	public final Any_opContext any_op() throws RecognitionException {
		Any_opContext _localctx = new Any_opContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_any_op);
		try {
			setState(429);
			switch (_input.LA(1)) {
			case EQ_ANY:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(423); match(EQ_ANY);
				}
				}
				break;
			case NEQ_ANY:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(424); match(NEQ_ANY);
				}
				}
				break;
			case GT_ANY:
				enterOuterAlt(_localctx, 3);
				{
				{
				setState(425); match(GT_ANY);
				}
				}
				break;
			case GTE_ANY:
				enterOuterAlt(_localctx, 4);
				{
				{
				setState(426); match(GTE_ANY);
				}
				}
				break;
			case LT_ANY:
				enterOuterAlt(_localctx, 5);
				{
				{
				setState(427); match(LT_ANY);
				}
				}
				break;
			case LTE_ANY:
				enterOuterAlt(_localctx, 6);
				{
				{
				setState(428); match(LTE_ANY);
				}
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

	public static class Add_exprContext extends ParserRuleContext {
		public List<Multiply_exprContext> multiply_expr() {
			return getRuleContexts(Multiply_exprContext.class);
		}
		public TerminalNode MINUS(int i) {
			return getToken(KVQLParser.MINUS, i);
		}
		public Multiply_exprContext multiply_expr(int i) {
			return getRuleContext(Multiply_exprContext.class,i);
		}
		public List<TerminalNode> PLUS() { return getTokens(KVQLParser.PLUS); }
		public List<TerminalNode> MINUS() { return getTokens(KVQLParser.MINUS); }
		public TerminalNode PLUS(int i) {
			return getToken(KVQLParser.PLUS, i);
		}
		public Add_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_add_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterAdd_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitAdd_expr(this);
		}
	}

	public final Add_exprContext add_expr() throws RecognitionException {
		Add_exprContext _localctx = new Add_exprContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_add_expr);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(431); multiply_expr();
			setState(436);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(432);
					_la = _input.LA(1);
					if ( !(_la==PLUS || _la==MINUS) ) {
					_errHandler.recoverInline(this);
					}
					consume();
					setState(433); multiply_expr();
					}
					} 
				}
				setState(438);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
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

	public static class Multiply_exprContext extends ParserRuleContext {
		public TerminalNode STAR(int i) {
			return getToken(KVQLParser.STAR, i);
		}
		public List<Unary_exprContext> unary_expr() {
			return getRuleContexts(Unary_exprContext.class);
		}
		public Unary_exprContext unary_expr(int i) {
			return getRuleContext(Unary_exprContext.class,i);
		}
		public List<TerminalNode> STAR() { return getTokens(KVQLParser.STAR); }
		public List<TerminalNode> DIV() { return getTokens(KVQLParser.DIV); }
		public TerminalNode DIV(int i) {
			return getToken(KVQLParser.DIV, i);
		}
		public Multiply_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multiply_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterMultiply_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitMultiply_expr(this);
		}
	}

	public final Multiply_exprContext multiply_expr() throws RecognitionException {
		Multiply_exprContext _localctx = new Multiply_exprContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_multiply_expr);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(439); unary_expr();
			setState(444);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,26,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(440);
					_la = _input.LA(1);
					if ( !(_la==STAR || _la==DIV) ) {
					_errHandler.recoverInline(this);
					}
					consume();
					setState(441); unary_expr();
					}
					} 
				}
				setState(446);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,26,_ctx);
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

	public static class Unary_exprContext extends ParserRuleContext {
		public Unary_exprContext unary_expr() {
			return getRuleContext(Unary_exprContext.class,0);
		}
		public Path_exprContext path_expr() {
			return getRuleContext(Path_exprContext.class,0);
		}
		public TerminalNode PLUS() { return getToken(KVQLParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(KVQLParser.MINUS, 0); }
		public Unary_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unary_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterUnary_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitUnary_expr(this);
		}
	}

	public final Unary_exprContext unary_expr() throws RecognitionException {
		Unary_exprContext _localctx = new Unary_exprContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_unary_expr);
		int _la;
		try {
			setState(450);
			switch (_input.LA(1)) {
			case ACCOUNT:
			case ADD:
			case ADMIN:
			case ALL:
			case ALTER:
			case AND:
			case AS:
			case ASC:
			case BY:
			case COMMENT:
			case CREATE:
			case DECLARE:
			case DEFAULT:
			case DESC:
			case DESCRIBE:
			case DROP:
			case ELEMENTOF:
			case ES_SHARDS:
			case ES_REPLICAS:
			case EXISTS:
			case FIRST:
			case FROM:
			case FULLTEXT:
			case GRANT:
			case IDENTIFIED:
			case IF:
			case INDEX:
			case INDEXES:
			case JSON:
			case KEY:
			case KEYOF:
			case LAST:
			case LIFETIME:
			case LOCK:
			case MODIFY:
			case NOT:
			case NULLS:
			case ON:
			case OR:
			case ORDER:
			case PASSWORD:
			case PRIMARY:
			case REVOKE:
			case ROLE:
			case ROLES:
			case SELECT:
			case SHARD:
			case SHOW:
			case TABLE:
			case TABLES:
			case TIME_UNIT:
			case TO:
			case TTL:
			case UNLOCK:
			case USER:
			case USERS:
			case USING:
			case WHERE:
			case ARRAY_T:
			case BINARY_T:
			case BOOLEAN_T:
			case DOUBLE_T:
			case ENUM_T:
			case FLOAT_T:
			case INTEGER_T:
			case LONG_T:
			case MAP_T:
			case RECORD_T:
			case STRING_T:
			case LP:
			case LBRACK:
			case DOLLAR:
			case FALSE:
			case TRUE:
			case INT:
			case FLOAT:
			case DSTRING:
			case STRING:
			case ID:
			case BAD_ID:
				enterOuterAlt(_localctx, 1);
				{
				setState(447); path_expr();
				}
				break;
			case PLUS:
			case MINUS:
				enterOuterAlt(_localctx, 2);
				{
				setState(448);
				_la = _input.LA(1);
				if ( !(_la==PLUS || _la==MINUS) ) {
				_errHandler.recoverInline(this);
				}
				consume();
				setState(449); unary_expr();
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

	public static class Path_exprContext extends ParserRuleContext {
		public List<Field_stepContext> field_step() {
			return getRuleContexts(Field_stepContext.class);
		}
		public Primary_exprContext primary_expr() {
			return getRuleContext(Primary_exprContext.class,0);
		}
		public List<Filter_stepContext> filter_step() {
			return getRuleContexts(Filter_stepContext.class);
		}
		public Field_stepContext field_step(int i) {
			return getRuleContext(Field_stepContext.class,i);
		}
		public Filter_stepContext filter_step(int i) {
			return getRuleContext(Filter_stepContext.class,i);
		}
		public List<Slice_stepContext> slice_step() {
			return getRuleContexts(Slice_stepContext.class);
		}
		public Slice_stepContext slice_step(int i) {
			return getRuleContext(Slice_stepContext.class,i);
		}
		public Path_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_path_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterPath_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitPath_expr(this);
		}
	}

	public final Path_exprContext path_expr() throws RecognitionException {
		Path_exprContext _localctx = new Path_exprContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_path_expr);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(452); primary_expr();
			setState(458);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					setState(456);
					switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
					case 1:
						{
						setState(453); field_step();
						}
						break;
					case 2:
						{
						setState(454); slice_step();
						}
						break;
					case 3:
						{
						setState(455); filter_step();
						}
						break;
					}
					} 
				}
				setState(460);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
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

	public static class Field_stepContext extends ParserRuleContext {
		public TerminalNode DOT() { return getToken(KVQLParser.DOT, 0); }
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public Var_refContext var_ref() {
			return getRuleContext(Var_refContext.class,0);
		}
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public Parenthesized_exprContext parenthesized_expr() {
			return getRuleContext(Parenthesized_exprContext.class,0);
		}
		public Func_callContext func_call() {
			return getRuleContext(Func_callContext.class,0);
		}
		public Field_stepContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_field_step; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterField_step(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitField_step(this);
		}
	}

	public final Field_stepContext field_step() throws RecognitionException {
		Field_stepContext _localctx = new Field_stepContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_field_step);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(461); match(DOT);
			setState(467);
			switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
			case 1:
				{
				setState(462); id();
				}
				break;
			case 2:
				{
				setState(463); string();
				}
				break;
			case 3:
				{
				setState(464); var_ref();
				}
				break;
			case 4:
				{
				setState(465); parenthesized_expr();
				}
				break;
			case 5:
				{
				setState(466); func_call();
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

	public static class Slice_stepContext extends ParserRuleContext {
		public TerminalNode COLON() { return getToken(KVQLParser.COLON, 0); }
		public Or_exprContext or_expr(int i) {
			return getRuleContext(Or_exprContext.class,i);
		}
		public TerminalNode RBRACK() { return getToken(KVQLParser.RBRACK, 0); }
		public List<Or_exprContext> or_expr() {
			return getRuleContexts(Or_exprContext.class);
		}
		public TerminalNode LBRACK() { return getToken(KVQLParser.LBRACK, 0); }
		public Slice_stepContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_slice_step; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterSlice_step(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitSlice_step(this);
		}
	}

	public final Slice_stepContext slice_step() throws RecognitionException {
		Slice_stepContext _localctx = new Slice_stepContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_slice_step);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(469); match(LBRACK);
			setState(471);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACCOUNT) | (1L << ADD) | (1L << ADMIN) | (1L << ALL) | (1L << ALTER) | (1L << AND) | (1L << AS) | (1L << ASC) | (1L << BY) | (1L << COMMENT) | (1L << CREATE) | (1L << DECLARE) | (1L << DEFAULT) | (1L << DESC) | (1L << DESCRIBE) | (1L << DROP) | (1L << ELEMENTOF) | (1L << ES_SHARDS) | (1L << ES_REPLICAS) | (1L << EXISTS) | (1L << FIRST) | (1L << FROM) | (1L << FULLTEXT) | (1L << GRANT) | (1L << IDENTIFIED) | (1L << IF) | (1L << INDEX) | (1L << INDEXES) | (1L << JSON) | (1L << KEY) | (1L << KEYOF) | (1L << LAST) | (1L << LIFETIME) | (1L << LOCK) | (1L << MODIFY) | (1L << NOT) | (1L << NULLS) | (1L << ON) | (1L << OR) | (1L << ORDER) | (1L << PASSWORD) | (1L << PRIMARY) | (1L << REVOKE) | (1L << ROLE) | (1L << ROLES) | (1L << SELECT) | (1L << SHARD) | (1L << SHOW) | (1L << TABLE) | (1L << TABLES) | (1L << TIME_UNIT) | (1L << TO) | (1L << TTL) | (1L << UNLOCK) | (1L << USER) | (1L << USERS) | (1L << USING))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (WHERE - 64)) | (1L << (ARRAY_T - 64)) | (1L << (BINARY_T - 64)) | (1L << (BOOLEAN_T - 64)) | (1L << (DOUBLE_T - 64)) | (1L << (ENUM_T - 64)) | (1L << (FLOAT_T - 64)) | (1L << (INTEGER_T - 64)) | (1L << (LONG_T - 64)) | (1L << (MAP_T - 64)) | (1L << (RECORD_T - 64)) | (1L << (STRING_T - 64)) | (1L << (LP - 64)) | (1L << (LBRACK - 64)) | (1L << (DOLLAR - 64)) | (1L << (PLUS - 64)) | (1L << (MINUS - 64)) | (1L << (FALSE - 64)) | (1L << (TRUE - 64)) | (1L << (INT - 64)) | (1L << (FLOAT - 64)) | (1L << (DSTRING - 64)) | (1L << (STRING - 64)) | (1L << (ID - 64)) | (1L << (BAD_ID - 64)))) != 0)) {
				{
				setState(470); or_expr(0);
				}
			}

			setState(473); match(COLON);
			setState(475);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACCOUNT) | (1L << ADD) | (1L << ADMIN) | (1L << ALL) | (1L << ALTER) | (1L << AND) | (1L << AS) | (1L << ASC) | (1L << BY) | (1L << COMMENT) | (1L << CREATE) | (1L << DECLARE) | (1L << DEFAULT) | (1L << DESC) | (1L << DESCRIBE) | (1L << DROP) | (1L << ELEMENTOF) | (1L << ES_SHARDS) | (1L << ES_REPLICAS) | (1L << EXISTS) | (1L << FIRST) | (1L << FROM) | (1L << FULLTEXT) | (1L << GRANT) | (1L << IDENTIFIED) | (1L << IF) | (1L << INDEX) | (1L << INDEXES) | (1L << JSON) | (1L << KEY) | (1L << KEYOF) | (1L << LAST) | (1L << LIFETIME) | (1L << LOCK) | (1L << MODIFY) | (1L << NOT) | (1L << NULLS) | (1L << ON) | (1L << OR) | (1L << ORDER) | (1L << PASSWORD) | (1L << PRIMARY) | (1L << REVOKE) | (1L << ROLE) | (1L << ROLES) | (1L << SELECT) | (1L << SHARD) | (1L << SHOW) | (1L << TABLE) | (1L << TABLES) | (1L << TIME_UNIT) | (1L << TO) | (1L << TTL) | (1L << UNLOCK) | (1L << USER) | (1L << USERS) | (1L << USING))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (WHERE - 64)) | (1L << (ARRAY_T - 64)) | (1L << (BINARY_T - 64)) | (1L << (BOOLEAN_T - 64)) | (1L << (DOUBLE_T - 64)) | (1L << (ENUM_T - 64)) | (1L << (FLOAT_T - 64)) | (1L << (INTEGER_T - 64)) | (1L << (LONG_T - 64)) | (1L << (MAP_T - 64)) | (1L << (RECORD_T - 64)) | (1L << (STRING_T - 64)) | (1L << (LP - 64)) | (1L << (LBRACK - 64)) | (1L << (DOLLAR - 64)) | (1L << (PLUS - 64)) | (1L << (MINUS - 64)) | (1L << (FALSE - 64)) | (1L << (TRUE - 64)) | (1L << (INT - 64)) | (1L << (FLOAT - 64)) | (1L << (DSTRING - 64)) | (1L << (STRING - 64)) | (1L << (ID - 64)) | (1L << (BAD_ID - 64)))) != 0)) {
				{
				setState(474); or_expr(0);
				}
			}

			setState(477); match(RBRACK);
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

	public static class Filter_stepContext extends ParserRuleContext {
		public TerminalNode RBRACK() { return getToken(KVQLParser.RBRACK, 0); }
		public Or_exprContext or_expr() {
			return getRuleContext(Or_exprContext.class,0);
		}
		public TerminalNode LBRACK() { return getToken(KVQLParser.LBRACK, 0); }
		public Filter_stepContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filter_step; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterFilter_step(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitFilter_step(this);
		}
	}

	public final Filter_stepContext filter_step() throws RecognitionException {
		Filter_stepContext _localctx = new Filter_stepContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_filter_step);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(479); match(LBRACK);
			setState(481);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACCOUNT) | (1L << ADD) | (1L << ADMIN) | (1L << ALL) | (1L << ALTER) | (1L << AND) | (1L << AS) | (1L << ASC) | (1L << BY) | (1L << COMMENT) | (1L << CREATE) | (1L << DECLARE) | (1L << DEFAULT) | (1L << DESC) | (1L << DESCRIBE) | (1L << DROP) | (1L << ELEMENTOF) | (1L << ES_SHARDS) | (1L << ES_REPLICAS) | (1L << EXISTS) | (1L << FIRST) | (1L << FROM) | (1L << FULLTEXT) | (1L << GRANT) | (1L << IDENTIFIED) | (1L << IF) | (1L << INDEX) | (1L << INDEXES) | (1L << JSON) | (1L << KEY) | (1L << KEYOF) | (1L << LAST) | (1L << LIFETIME) | (1L << LOCK) | (1L << MODIFY) | (1L << NOT) | (1L << NULLS) | (1L << ON) | (1L << OR) | (1L << ORDER) | (1L << PASSWORD) | (1L << PRIMARY) | (1L << REVOKE) | (1L << ROLE) | (1L << ROLES) | (1L << SELECT) | (1L << SHARD) | (1L << SHOW) | (1L << TABLE) | (1L << TABLES) | (1L << TIME_UNIT) | (1L << TO) | (1L << TTL) | (1L << UNLOCK) | (1L << USER) | (1L << USERS) | (1L << USING))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (WHERE - 64)) | (1L << (ARRAY_T - 64)) | (1L << (BINARY_T - 64)) | (1L << (BOOLEAN_T - 64)) | (1L << (DOUBLE_T - 64)) | (1L << (ENUM_T - 64)) | (1L << (FLOAT_T - 64)) | (1L << (INTEGER_T - 64)) | (1L << (LONG_T - 64)) | (1L << (MAP_T - 64)) | (1L << (RECORD_T - 64)) | (1L << (STRING_T - 64)) | (1L << (LP - 64)) | (1L << (LBRACK - 64)) | (1L << (DOLLAR - 64)) | (1L << (PLUS - 64)) | (1L << (MINUS - 64)) | (1L << (FALSE - 64)) | (1L << (TRUE - 64)) | (1L << (INT - 64)) | (1L << (FLOAT - 64)) | (1L << (DSTRING - 64)) | (1L << (STRING - 64)) | (1L << (ID - 64)) | (1L << (BAD_ID - 64)))) != 0)) {
				{
				setState(480); or_expr(0);
				}
			}

			setState(483); match(RBRACK);
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

	public static class Primary_exprContext extends ParserRuleContext {
		public Var_refContext var_ref() {
			return getRuleContext(Var_refContext.class,0);
		}
		public Const_exprContext const_expr() {
			return getRuleContext(Const_exprContext.class,0);
		}
		public Array_constructorContext array_constructor() {
			return getRuleContext(Array_constructorContext.class,0);
		}
		public Parenthesized_exprContext parenthesized_expr() {
			return getRuleContext(Parenthesized_exprContext.class,0);
		}
		public Column_refContext column_ref() {
			return getRuleContext(Column_refContext.class,0);
		}
		public Func_callContext func_call() {
			return getRuleContext(Func_callContext.class,0);
		}
		public Primary_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primary_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterPrimary_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitPrimary_expr(this);
		}
	}

	public final Primary_exprContext primary_expr() throws RecognitionException {
		Primary_exprContext _localctx = new Primary_exprContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_primary_expr);
		try {
			setState(491);
			switch ( getInterpreter().adaptivePredict(_input,34,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(485); const_expr();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(486); column_ref();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(487); var_ref();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(488); array_constructor();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(489); func_call();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(490); parenthesized_expr();
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

	public static class Column_refContext extends ParserRuleContext {
		public TerminalNode DOT() { return getToken(KVQLParser.DOT, 0); }
		public IdContext id(int i) {
			return getRuleContext(IdContext.class,i);
		}
		public List<IdContext> id() {
			return getRuleContexts(IdContext.class);
		}
		public Column_refContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_column_ref; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterColumn_ref(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitColumn_ref(this);
		}
	}

	public final Column_refContext column_ref() throws RecognitionException {
		Column_refContext _localctx = new Column_refContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_column_ref);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(493); id();
			setState(496);
			switch ( getInterpreter().adaptivePredict(_input,35,_ctx) ) {
			case 1:
				{
				setState(494); match(DOT);
				setState(495); id();
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

	public static class Const_exprContext extends ParserRuleContext {
		public TerminalNode FALSE() { return getToken(KVQLParser.FALSE, 0); }
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public TerminalNode TRUE() { return getToken(KVQLParser.TRUE, 0); }
		public TerminalNode INT() { return getToken(KVQLParser.INT, 0); }
		public TerminalNode FLOAT() { return getToken(KVQLParser.FLOAT, 0); }
		public Const_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_const_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterConst_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitConst_expr(this);
		}
	}

	public final Const_exprContext const_expr() throws RecognitionException {
		Const_exprContext _localctx = new Const_exprContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_const_expr);
		try {
			setState(503);
			switch (_input.LA(1)) {
			case INT:
				enterOuterAlt(_localctx, 1);
				{
				setState(498); match(INT);
				}
				break;
			case FLOAT:
				enterOuterAlt(_localctx, 2);
				{
				setState(499); match(FLOAT);
				}
				break;
			case DSTRING:
			case STRING:
				enterOuterAlt(_localctx, 3);
				{
				setState(500); string();
				}
				break;
			case TRUE:
				enterOuterAlt(_localctx, 4);
				{
				setState(501); match(TRUE);
				}
				break;
			case FALSE:
				enterOuterAlt(_localctx, 5);
				{
				setState(502); match(FALSE);
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

	public static class Var_refContext extends ParserRuleContext {
		public TerminalNode DOLLAR() { return getToken(KVQLParser.DOLLAR, 0); }
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public Var_refContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var_ref; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterVar_ref(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitVar_ref(this);
		}
	}

	public final Var_refContext var_ref() throws RecognitionException {
		Var_refContext _localctx = new Var_refContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_var_ref);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(505); match(DOLLAR);
			setState(507);
			switch ( getInterpreter().adaptivePredict(_input,37,_ctx) ) {
			case 1:
				{
				setState(506); id();
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

	public static class Array_constructorContext extends ParserRuleContext {
		public List<TerminalNode> COMMA() { return getTokens(KVQLParser.COMMA); }
		public Or_exprContext or_expr(int i) {
			return getRuleContext(Or_exprContext.class,i);
		}
		public TerminalNode RBRACK() { return getToken(KVQLParser.RBRACK, 0); }
		public List<Or_exprContext> or_expr() {
			return getRuleContexts(Or_exprContext.class);
		}
		public TerminalNode LBRACK() { return getToken(KVQLParser.LBRACK, 0); }
		public TerminalNode COMMA(int i) {
			return getToken(KVQLParser.COMMA, i);
		}
		public Array_constructorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_array_constructor; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterArray_constructor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitArray_constructor(this);
		}
	}

	public final Array_constructorContext array_constructor() throws RecognitionException {
		Array_constructorContext _localctx = new Array_constructorContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_array_constructor);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(509); match(LBRACK);
			setState(510); or_expr(0);
			setState(515);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(511); match(COMMA);
				setState(512); or_expr(0);
				}
				}
				setState(517);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(518); match(RBRACK);
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

	public static class Func_callContext extends ParserRuleContext {
		public TerminalNode RP() { return getToken(KVQLParser.RP, 0); }
		public TerminalNode LP() { return getToken(KVQLParser.LP, 0); }
		public List<TerminalNode> COMMA() { return getTokens(KVQLParser.COMMA); }
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public Or_exprContext or_expr(int i) {
			return getRuleContext(Or_exprContext.class,i);
		}
		public List<Or_exprContext> or_expr() {
			return getRuleContexts(Or_exprContext.class);
		}
		public TerminalNode COMMA(int i) {
			return getToken(KVQLParser.COMMA, i);
		}
		public Func_callContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_func_call; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterFunc_call(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitFunc_call(this);
		}
	}

	public final Func_callContext func_call() throws RecognitionException {
		Func_callContext _localctx = new Func_callContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_func_call);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(520); id();
			setState(521); match(LP);
			setState(530);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACCOUNT) | (1L << ADD) | (1L << ADMIN) | (1L << ALL) | (1L << ALTER) | (1L << AND) | (1L << AS) | (1L << ASC) | (1L << BY) | (1L << COMMENT) | (1L << CREATE) | (1L << DECLARE) | (1L << DEFAULT) | (1L << DESC) | (1L << DESCRIBE) | (1L << DROP) | (1L << ELEMENTOF) | (1L << ES_SHARDS) | (1L << ES_REPLICAS) | (1L << EXISTS) | (1L << FIRST) | (1L << FROM) | (1L << FULLTEXT) | (1L << GRANT) | (1L << IDENTIFIED) | (1L << IF) | (1L << INDEX) | (1L << INDEXES) | (1L << JSON) | (1L << KEY) | (1L << KEYOF) | (1L << LAST) | (1L << LIFETIME) | (1L << LOCK) | (1L << MODIFY) | (1L << NOT) | (1L << NULLS) | (1L << ON) | (1L << OR) | (1L << ORDER) | (1L << PASSWORD) | (1L << PRIMARY) | (1L << REVOKE) | (1L << ROLE) | (1L << ROLES) | (1L << SELECT) | (1L << SHARD) | (1L << SHOW) | (1L << TABLE) | (1L << TABLES) | (1L << TIME_UNIT) | (1L << TO) | (1L << TTL) | (1L << UNLOCK) | (1L << USER) | (1L << USERS) | (1L << USING))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (WHERE - 64)) | (1L << (ARRAY_T - 64)) | (1L << (BINARY_T - 64)) | (1L << (BOOLEAN_T - 64)) | (1L << (DOUBLE_T - 64)) | (1L << (ENUM_T - 64)) | (1L << (FLOAT_T - 64)) | (1L << (INTEGER_T - 64)) | (1L << (LONG_T - 64)) | (1L << (MAP_T - 64)) | (1L << (RECORD_T - 64)) | (1L << (STRING_T - 64)) | (1L << (LP - 64)) | (1L << (LBRACK - 64)) | (1L << (DOLLAR - 64)) | (1L << (PLUS - 64)) | (1L << (MINUS - 64)) | (1L << (FALSE - 64)) | (1L << (TRUE - 64)) | (1L << (INT - 64)) | (1L << (FLOAT - 64)) | (1L << (DSTRING - 64)) | (1L << (STRING - 64)) | (1L << (ID - 64)) | (1L << (BAD_ID - 64)))) != 0)) {
				{
				setState(522); or_expr(0);
				setState(527);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(523); match(COMMA);
					setState(524); or_expr(0);
					}
					}
					setState(529);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(532); match(RP);
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

	public static class Parenthesized_exprContext extends ParserRuleContext {
		public TerminalNode RP() { return getToken(KVQLParser.RP, 0); }
		public TerminalNode LP() { return getToken(KVQLParser.LP, 0); }
		public Or_exprContext or_expr() {
			return getRuleContext(Or_exprContext.class,0);
		}
		public Parenthesized_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parenthesized_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterParenthesized_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitParenthesized_expr(this);
		}
	}

	public final Parenthesized_exprContext parenthesized_expr() throws RecognitionException {
		Parenthesized_exprContext _localctx = new Parenthesized_exprContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_parenthesized_expr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(534); match(LP);
			setState(535); or_expr(0);
			setState(536); match(RP);
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
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterArray(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitArray(this);
		}
	}
	public static class EnumContext extends Type_defContext {
		public Enum_defContext enum_def() {
			return getRuleContext(Enum_defContext.class,0);
		}
		public EnumContext(Type_defContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterEnum(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitEnum(this);
		}
	}
	public static class FloatContext extends Type_defContext {
		public Float_defContext float_def() {
			return getRuleContext(Float_defContext.class,0);
		}
		public FloatContext(Type_defContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterFloat(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitFloat(this);
		}
	}
	public static class RecordContext extends Type_defContext {
		public Record_defContext record_def() {
			return getRuleContext(Record_defContext.class,0);
		}
		public RecordContext(Type_defContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterRecord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitRecord(this);
		}
	}
	public static class BinaryContext extends Type_defContext {
		public Binary_defContext binary_def() {
			return getRuleContext(Binary_defContext.class,0);
		}
		public BinaryContext(Type_defContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterBinary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitBinary(this);
		}
	}
	public static class BooleanContext extends Type_defContext {
		public Boolean_defContext boolean_def() {
			return getRuleContext(Boolean_defContext.class,0);
		}
		public BooleanContext(Type_defContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterBoolean(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitBoolean(this);
		}
	}
	public static class StringTContext extends Type_defContext {
		public String_defContext string_def() {
			return getRuleContext(String_defContext.class,0);
		}
		public StringTContext(Type_defContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterStringT(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitStringT(this);
		}
	}
	public static class MapContext extends Type_defContext {
		public Map_defContext map_def() {
			return getRuleContext(Map_defContext.class,0);
		}
		public MapContext(Type_defContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterMap(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitMap(this);
		}
	}
	public static class IntContext extends Type_defContext {
		public Integer_defContext integer_def() {
			return getRuleContext(Integer_defContext.class,0);
		}
		public IntContext(Type_defContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterInt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitInt(this);
		}
	}

	public final Type_defContext type_def() throws RecognitionException {
		Type_defContext _localctx = new Type_defContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_type_def);
		try {
			setState(547);
			switch (_input.LA(1)) {
			case BINARY_T:
				_localctx = new BinaryContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(538); binary_def();
				}
				break;
			case ARRAY_T:
				_localctx = new ArrayContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(539); array_def();
				}
				break;
			case BOOLEAN_T:
				_localctx = new BooleanContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(540); boolean_def();
				}
				break;
			case ENUM_T:
				_localctx = new EnumContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(541); enum_def();
				}
				break;
			case DOUBLE_T:
			case FLOAT_T:
				_localctx = new FloatContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(542); float_def();
				}
				break;
			case INTEGER_T:
			case LONG_T:
				_localctx = new IntContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(543); integer_def();
				}
				break;
			case MAP_T:
				_localctx = new MapContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(544); map_def();
				}
				break;
			case RECORD_T:
				_localctx = new RecordContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(545); record_def();
				}
				break;
			case STRING_T:
				_localctx = new StringTContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(546); string_def();
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

	public static class Record_defContext extends ParserRuleContext {
		public List<Field_defContext> field_def() {
			return getRuleContexts(Field_defContext.class);
		}
		public TerminalNode RP() { return getToken(KVQLParser.RP, 0); }
		public TerminalNode LP() { return getToken(KVQLParser.LP, 0); }
		public List<TerminalNode> COMMA() { return getTokens(KVQLParser.COMMA); }
		public TerminalNode RECORD_T() { return getToken(KVQLParser.RECORD_T, 0); }
		public Field_defContext field_def(int i) {
			return getRuleContext(Field_defContext.class,i);
		}
		public TerminalNode COMMA(int i) {
			return getToken(KVQLParser.COMMA, i);
		}
		public Record_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_record_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterRecord_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitRecord_def(this);
		}
	}

	public final Record_defContext record_def() throws RecognitionException {
		Record_defContext _localctx = new Record_defContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_record_def);
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
		public CommentContext comment() {
			return getRuleContext(CommentContext.class,0);
		}
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public Default_defContext default_def() {
			return getRuleContext(Default_defContext.class,0);
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
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterField_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitField_def(this);
		}
	}

	public final Field_defContext field_def() throws RecognitionException {
		Field_defContext _localctx = new Field_defContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_field_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(561); id();
			setState(562); type_def();
			setState(564);
			_la = _input.LA(1);
			if (_la==DEFAULT || _la==NOT) {
				{
				setState(563); default_def();
				}
			}

			setState(567);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(566); comment();
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

	public static class Default_defContext extends ParserRuleContext {
		public Default_valueContext default_value() {
			return getRuleContext(Default_valueContext.class,0);
		}
		public Not_nullContext not_null() {
			return getRuleContext(Not_nullContext.class,0);
		}
		public Default_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_default_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterDefault_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitDefault_def(this);
		}
	}

	public final Default_defContext default_def() throws RecognitionException {
		Default_defContext _localctx = new Default_defContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_default_def);
		int _la;
		try {
			setState(577);
			switch (_input.LA(1)) {
			case DEFAULT:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(569); default_value();
				setState(571);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(570); not_null();
					}
				}

				}
				}
				break;
			case NOT:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(573); not_null();
				setState(575);
				_la = _input.LA(1);
				if (_la==DEFAULT) {
					{
					setState(574); default_value();
					}
				}

				}
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

	public static class Default_valueContext extends ParserRuleContext {
		public NumberContext number() {
			return getRuleContext(NumberContext.class,0);
		}
		public TerminalNode FALSE() { return getToken(KVQLParser.FALSE, 0); }
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public TerminalNode TRUE() { return getToken(KVQLParser.TRUE, 0); }
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public TerminalNode DEFAULT() { return getToken(KVQLParser.DEFAULT, 0); }
		public Default_valueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_default_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterDefault_value(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitDefault_value(this);
		}
	}

	public final Default_valueContext default_value() throws RecognitionException {
		Default_valueContext _localctx = new Default_valueContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_default_value);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(579); match(DEFAULT);
			setState(585);
			switch (_input.LA(1)) {
			case MINUS:
			case INT:
			case FLOAT:
				{
				setState(580); number();
				}
				break;
			case DSTRING:
			case STRING:
				{
				setState(581); string();
				}
				break;
			case TRUE:
				{
				setState(582); match(TRUE);
				}
				break;
			case FALSE:
				{
				setState(583); match(FALSE);
				}
				break;
			case ACCOUNT:
			case ADD:
			case ADMIN:
			case ALL:
			case ALTER:
			case AND:
			case AS:
			case ASC:
			case BY:
			case COMMENT:
			case CREATE:
			case DECLARE:
			case DEFAULT:
			case DESC:
			case DESCRIBE:
			case DROP:
			case ELEMENTOF:
			case ES_SHARDS:
			case ES_REPLICAS:
			case EXISTS:
			case FIRST:
			case FROM:
			case FULLTEXT:
			case GRANT:
			case IDENTIFIED:
			case IF:
			case INDEX:
			case INDEXES:
			case JSON:
			case KEY:
			case KEYOF:
			case LAST:
			case LIFETIME:
			case LOCK:
			case MODIFY:
			case NOT:
			case NULLS:
			case ON:
			case OR:
			case ORDER:
			case PASSWORD:
			case PRIMARY:
			case REVOKE:
			case ROLE:
			case ROLES:
			case SELECT:
			case SHARD:
			case SHOW:
			case TABLE:
			case TABLES:
			case TIME_UNIT:
			case TO:
			case TTL:
			case UNLOCK:
			case USER:
			case USERS:
			case USING:
			case WHERE:
			case ARRAY_T:
			case BINARY_T:
			case BOOLEAN_T:
			case DOUBLE_T:
			case ENUM_T:
			case FLOAT_T:
			case INTEGER_T:
			case LONG_T:
			case MAP_T:
			case RECORD_T:
			case STRING_T:
			case ID:
			case BAD_ID:
				{
				setState(584); id();
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

	public static class Not_nullContext extends ParserRuleContext {
		public TerminalNode NOT() { return getToken(KVQLParser.NOT, 0); }
		public TerminalNode NULL() { return getToken(KVQLParser.NULL, 0); }
		public Not_nullContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_not_null; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterNot_null(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitNot_null(this);
		}
	}

	public final Not_nullContext not_null() throws RecognitionException {
		Not_nullContext _localctx = new Not_nullContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_not_null);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(587); match(NOT);
			setState(588); match(NULL);
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
		public TerminalNode RP() { return getToken(KVQLParser.RP, 0); }
		public TerminalNode LP() { return getToken(KVQLParser.LP, 0); }
		public TerminalNode MAP_T() { return getToken(KVQLParser.MAP_T, 0); }
		public Type_defContext type_def() {
			return getRuleContext(Type_defContext.class,0);
		}
		public Map_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_map_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterMap_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitMap_def(this);
		}
	}

	public final Map_defContext map_def() throws RecognitionException {
		Map_defContext _localctx = new Map_defContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_map_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(590); match(MAP_T);
			setState(591); match(LP);
			setState(592); type_def();
			setState(593); match(RP);
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
		public TerminalNode RP() { return getToken(KVQLParser.RP, 0); }
		public TerminalNode LP() { return getToken(KVQLParser.LP, 0); }
		public TerminalNode ARRAY_T() { return getToken(KVQLParser.ARRAY_T, 0); }
		public Type_defContext type_def() {
			return getRuleContext(Type_defContext.class,0);
		}
		public Array_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_array_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterArray_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitArray_def(this);
		}
	}

	public final Array_defContext array_def() throws RecognitionException {
		Array_defContext _localctx = new Array_defContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_array_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(595); match(ARRAY_T);
			setState(596); match(LP);
			setState(597); type_def();
			setState(598); match(RP);
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
		public TerminalNode LONG_T() { return getToken(KVQLParser.LONG_T, 0); }
		public TerminalNode INTEGER_T() { return getToken(KVQLParser.INTEGER_T, 0); }
		public Integer_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integer_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterInteger_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitInteger_def(this);
		}
	}

	public final Integer_defContext integer_def() throws RecognitionException {
		Integer_defContext _localctx = new Integer_defContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_integer_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(600);
			_la = _input.LA(1);
			if ( !(_la==INTEGER_T || _la==LONG_T) ) {
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

	public static class Float_defContext extends ParserRuleContext {
		public TerminalNode DOUBLE_T() { return getToken(KVQLParser.DOUBLE_T, 0); }
		public TerminalNode FLOAT_T() { return getToken(KVQLParser.FLOAT_T, 0); }
		public Float_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_float_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterFloat_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitFloat_def(this);
		}
	}

	public final Float_defContext float_def() throws RecognitionException {
		Float_defContext _localctx = new Float_defContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_float_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(602);
			_la = _input.LA(1);
			if ( !(_la==DOUBLE_T || _la==FLOAT_T) ) {
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

	public static class String_defContext extends ParserRuleContext {
		public TerminalNode STRING_T() { return getToken(KVQLParser.STRING_T, 0); }
		public String_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_string_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterString_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitString_def(this);
		}
	}

	public final String_defContext string_def() throws RecognitionException {
		String_defContext _localctx = new String_defContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_string_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(604); match(STRING_T);
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
		public TerminalNode RP() { return getToken(KVQLParser.RP, 0); }
		public Id_listContext id_list() {
			return getRuleContext(Id_listContext.class,0);
		}
		public TerminalNode LP() { return getToken(KVQLParser.LP, 0); }
		public TerminalNode ENUM_T() { return getToken(KVQLParser.ENUM_T, 0); }
		public Enum_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enum_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterEnum_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitEnum_def(this);
		}
	}

	public final Enum_defContext enum_def() throws RecognitionException {
		Enum_defContext _localctx = new Enum_defContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_enum_def);
		try {
			setState(616);
			switch ( getInterpreter().adaptivePredict(_input,49,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(606); match(ENUM_T);
				setState(607); match(LP);
				setState(608); id_list();
				setState(609); match(RP);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(611); match(ENUM_T);
				setState(612); match(LP);
				setState(613); id_list();
				 notifyErrorListeners("Missing closing ')'"); 
				}
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

	public static class Boolean_defContext extends ParserRuleContext {
		public TerminalNode BOOLEAN_T() { return getToken(KVQLParser.BOOLEAN_T, 0); }
		public Boolean_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_boolean_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterBoolean_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitBoolean_def(this);
		}
	}

	public final Boolean_defContext boolean_def() throws RecognitionException {
		Boolean_defContext _localctx = new Boolean_defContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_boolean_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(618); match(BOOLEAN_T);
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
		public TerminalNode RP() { return getToken(KVQLParser.RP, 0); }
		public TerminalNode BINARY_T() { return getToken(KVQLParser.BINARY_T, 0); }
		public TerminalNode LP() { return getToken(KVQLParser.LP, 0); }
		public TerminalNode INT() { return getToken(KVQLParser.INT, 0); }
		public Binary_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_binary_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterBinary_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitBinary_def(this);
		}
	}

	public final Binary_defContext binary_def() throws RecognitionException {
		Binary_defContext _localctx = new Binary_defContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_binary_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(620); match(BINARY_T);
			setState(624);
			_la = _input.LA(1);
			if (_la==LP) {
				{
				setState(621); match(LP);
				setState(622); match(INT);
				setState(623); match(RP);
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

	public static class Name_pathContext extends ParserRuleContext {
		public List<TerminalNode> DOT() { return getTokens(KVQLParser.DOT); }
		public IdContext id(int i) {
			return getRuleContext(IdContext.class,i);
		}
		public List<IdContext> id() {
			return getRuleContexts(IdContext.class);
		}
		public TerminalNode DOT(int i) {
			return getToken(KVQLParser.DOT, i);
		}
		public Name_pathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_name_path; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterName_path(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitName_path(this);
		}
	}

	public final Name_pathContext name_path() throws RecognitionException {
		Name_pathContext _localctx = new Name_pathContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_name_path);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(626); id();
			setState(631);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(627); match(DOT);
				setState(628); id();
				}
				}
				setState(633);
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

	public static class Create_table_statementContext extends ParserRuleContext {
		public TerminalNode NOT() { return getToken(KVQLParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(KVQLParser.EXISTS, 0); }
		public TerminalNode RP() { return getToken(KVQLParser.RP, 0); }
		public TerminalNode IF() { return getToken(KVQLParser.IF, 0); }
		public CommentContext comment() {
			return getRuleContext(CommentContext.class,0);
		}
		public Ttl_defContext ttl_def() {
			return getRuleContext(Ttl_defContext.class,0);
		}
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public TerminalNode LP() { return getToken(KVQLParser.LP, 0); }
		public Table_defContext table_def() {
			return getRuleContext(Table_defContext.class,0);
		}
		public TerminalNode CREATE() { return getToken(KVQLParser.CREATE, 0); }
		public TerminalNode TABLE() { return getToken(KVQLParser.TABLE, 0); }
		public Create_table_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_table_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterCreate_table_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitCreate_table_statement(this);
		}
	}

	public final Create_table_statementContext create_table_statement() throws RecognitionException {
		Create_table_statementContext _localctx = new Create_table_statementContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_create_table_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(634); match(CREATE);
			setState(635); match(TABLE);
			setState(639);
			switch ( getInterpreter().adaptivePredict(_input,52,_ctx) ) {
			case 1:
				{
				setState(636); match(IF);
				setState(637); match(NOT);
				setState(638); match(EXISTS);
				}
				break;
			}
			setState(641); table_name();
			setState(643);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(642); comment();
				}
			}

			setState(645); match(LP);
			setState(646); table_def();
			setState(647); match(RP);
			setState(649);
			_la = _input.LA(1);
			if (_la==USING) {
				{
				setState(648); ttl_def();
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

	public static class Table_nameContext extends ParserRuleContext {
		public Name_pathContext name_path() {
			return getRuleContext(Name_pathContext.class,0);
		}
		public Table_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterTable_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitTable_name(this);
		}
	}

	public final Table_nameContext table_name() throws RecognitionException {
		Table_nameContext _localctx = new Table_nameContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_table_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(651); name_path();
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
		public List<TerminalNode> COMMA() { return getTokens(KVQLParser.COMMA); }
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
			return getToken(KVQLParser.COMMA, i);
		}
		public Table_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_table_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterTable_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitTable_def(this);
		}
	}

	public final Table_defContext table_def() throws RecognitionException {
		Table_defContext _localctx = new Table_defContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_table_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(655);
			switch ( getInterpreter().adaptivePredict(_input,55,_ctx) ) {
			case 1:
				{
				setState(653); field_def();
				}
				break;
			case 2:
				{
				setState(654); key_def();
				}
				break;
			}
			setState(664);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(657); match(COMMA);
				setState(660);
				switch ( getInterpreter().adaptivePredict(_input,56,_ctx) ) {
				case 1:
					{
					setState(658); field_def();
					}
					break;
				case 2:
					{
					setState(659); key_def();
					}
					break;
				}
				}
				}
				setState(666);
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
		public TerminalNode PRIMARY() { return getToken(KVQLParser.PRIMARY, 0); }
		public TerminalNode KEY() { return getToken(KVQLParser.KEY, 0); }
		public TerminalNode RP() { return getToken(KVQLParser.RP, 0); }
		public Shard_key_defContext shard_key_def() {
			return getRuleContext(Shard_key_defContext.class,0);
		}
		public Id_list_with_sizeContext id_list_with_size() {
			return getRuleContext(Id_list_with_sizeContext.class,0);
		}
		public TerminalNode LP() { return getToken(KVQLParser.LP, 0); }
		public TerminalNode COMMA() { return getToken(KVQLParser.COMMA, 0); }
		public Key_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_key_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterKey_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitKey_def(this);
		}
	}

	public final Key_defContext key_def() throws RecognitionException {
		Key_defContext _localctx = new Key_defContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_key_def);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(667); match(PRIMARY);
			setState(668); match(KEY);
			setState(669); match(LP);
			setState(674);
			switch ( getInterpreter().adaptivePredict(_input,59,_ctx) ) {
			case 1:
				{
				setState(670); shard_key_def();
				setState(672);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(671); match(COMMA);
					}
				}

				}
				break;
			}
			setState(677);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACCOUNT) | (1L << ADD) | (1L << ADMIN) | (1L << ALL) | (1L << ALTER) | (1L << AND) | (1L << AS) | (1L << ASC) | (1L << BY) | (1L << COMMENT) | (1L << CREATE) | (1L << DECLARE) | (1L << DEFAULT) | (1L << DESC) | (1L << DESCRIBE) | (1L << DROP) | (1L << ELEMENTOF) | (1L << ES_SHARDS) | (1L << ES_REPLICAS) | (1L << EXISTS) | (1L << FIRST) | (1L << FROM) | (1L << FULLTEXT) | (1L << GRANT) | (1L << IDENTIFIED) | (1L << IF) | (1L << INDEX) | (1L << INDEXES) | (1L << JSON) | (1L << KEY) | (1L << KEYOF) | (1L << LAST) | (1L << LIFETIME) | (1L << LOCK) | (1L << MODIFY) | (1L << NOT) | (1L << NULLS) | (1L << ON) | (1L << OR) | (1L << ORDER) | (1L << PASSWORD) | (1L << PRIMARY) | (1L << REVOKE) | (1L << ROLE) | (1L << ROLES) | (1L << SELECT) | (1L << SHARD) | (1L << SHOW) | (1L << TABLE) | (1L << TABLES) | (1L << TIME_UNIT) | (1L << TO) | (1L << TTL) | (1L << UNLOCK) | (1L << USER) | (1L << USERS) | (1L << USING))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (WHERE - 64)) | (1L << (ARRAY_T - 64)) | (1L << (BINARY_T - 64)) | (1L << (BOOLEAN_T - 64)) | (1L << (DOUBLE_T - 64)) | (1L << (ENUM_T - 64)) | (1L << (FLOAT_T - 64)) | (1L << (INTEGER_T - 64)) | (1L << (LONG_T - 64)) | (1L << (MAP_T - 64)) | (1L << (RECORD_T - 64)) | (1L << (STRING_T - 64)) | (1L << (ID - 64)) | (1L << (BAD_ID - 64)))) != 0)) {
				{
				setState(676); id_list_with_size();
				}
			}

			setState(679); match(RP);
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
		public TerminalNode RP() { return getToken(KVQLParser.RP, 0); }
		public Id_list_with_sizeContext id_list_with_size() {
			return getRuleContext(Id_list_with_sizeContext.class,0);
		}
		public TerminalNode LP() { return getToken(KVQLParser.LP, 0); }
		public TerminalNode SHARD() { return getToken(KVQLParser.SHARD, 0); }
		public Shard_key_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_shard_key_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterShard_key_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitShard_key_def(this);
		}
	}

	public final Shard_key_defContext shard_key_def() throws RecognitionException {
		Shard_key_defContext _localctx = new Shard_key_defContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_shard_key_def);
		try {
			setState(690);
			switch (_input.LA(1)) {
			case SHARD:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(681); match(SHARD);
				setState(682); match(LP);
				setState(683); id_list_with_size();
				setState(684); match(RP);
				}
				}
				break;
			case LP:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(686); match(LP);
				setState(687); id_list_with_size();
				 notifyErrorListeners("Missing closing ')'"); 
				}
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

	public static class Id_list_with_sizeContext extends ParserRuleContext {
		public Id_with_sizeContext id_with_size(int i) {
			return getRuleContext(Id_with_sizeContext.class,i);
		}
		public List<Id_with_sizeContext> id_with_size() {
			return getRuleContexts(Id_with_sizeContext.class);
		}
		public List<TerminalNode> COMMA() { return getTokens(KVQLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(KVQLParser.COMMA, i);
		}
		public Id_list_with_sizeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_id_list_with_size; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterId_list_with_size(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitId_list_with_size(this);
		}
	}

	public final Id_list_with_sizeContext id_list_with_size() throws RecognitionException {
		Id_list_with_sizeContext _localctx = new Id_list_with_sizeContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_id_list_with_size);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(692); id_with_size();
			setState(697);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,62,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(693); match(COMMA);
					setState(694); id_with_size();
					}
					} 
				}
				setState(699);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,62,_ctx);
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

	public static class Id_with_sizeContext extends ParserRuleContext {
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public Storage_sizeContext storage_size() {
			return getRuleContext(Storage_sizeContext.class,0);
		}
		public Id_with_sizeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_id_with_size; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterId_with_size(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitId_with_size(this);
		}
	}

	public final Id_with_sizeContext id_with_size() throws RecognitionException {
		Id_with_sizeContext _localctx = new Id_with_sizeContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_id_with_size);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(700); id();
			setState(702);
			_la = _input.LA(1);
			if (_la==LP) {
				{
				setState(701); storage_size();
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

	public static class Storage_sizeContext extends ParserRuleContext {
		public TerminalNode RP() { return getToken(KVQLParser.RP, 0); }
		public TerminalNode LP() { return getToken(KVQLParser.LP, 0); }
		public TerminalNode INT() { return getToken(KVQLParser.INT, 0); }
		public Storage_sizeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_storage_size; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterStorage_size(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitStorage_size(this);
		}
	}

	public final Storage_sizeContext storage_size() throws RecognitionException {
		Storage_sizeContext _localctx = new Storage_sizeContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_storage_size);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(704); match(LP);
			setState(705); match(INT);
			setState(706); match(RP);
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

	public static class Ttl_defContext extends ParserRuleContext {
		public TerminalNode TTL() { return getToken(KVQLParser.TTL, 0); }
		public DurationContext duration() {
			return getRuleContext(DurationContext.class,0);
		}
		public TerminalNode USING() { return getToken(KVQLParser.USING, 0); }
		public Ttl_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ttl_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterTtl_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitTtl_def(this);
		}
	}

	public final Ttl_defContext ttl_def() throws RecognitionException {
		Ttl_defContext _localctx = new Ttl_defContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_ttl_def);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(708); match(USING);
			setState(709); match(TTL);
			setState(710); duration();
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
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public Alter_defContext alter_def() {
			return getRuleContext(Alter_defContext.class,0);
		}
		public TerminalNode ALTER() { return getToken(KVQLParser.ALTER, 0); }
		public TerminalNode TABLE() { return getToken(KVQLParser.TABLE, 0); }
		public Alter_table_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alter_table_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterAlter_table_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitAlter_table_statement(this);
		}
	}

	public final Alter_table_statementContext alter_table_statement() throws RecognitionException {
		Alter_table_statementContext _localctx = new Alter_table_statementContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_alter_table_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(712); match(ALTER);
			setState(713); match(TABLE);
			setState(714); table_name();
			setState(715); alter_def();
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

	public static class Alter_defContext extends ParserRuleContext {
		public Alter_field_statementContext alter_field_statement() {
			return getRuleContext(Alter_field_statementContext.class,0);
		}
		public Ttl_defContext ttl_def() {
			return getRuleContext(Ttl_defContext.class,0);
		}
		public Alter_defContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alter_def; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterAlter_def(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitAlter_def(this);
		}
	}

	public final Alter_defContext alter_def() throws RecognitionException {
		Alter_defContext _localctx = new Alter_defContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_alter_def);
		try {
			setState(719);
			switch (_input.LA(1)) {
			case LP:
				enterOuterAlt(_localctx, 1);
				{
				setState(717); alter_field_statement();
				}
				break;
			case USING:
				enterOuterAlt(_localctx, 2);
				{
				setState(718); ttl_def();
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

	public static class Alter_field_statementContext extends ParserRuleContext {
		public TerminalNode RP() { return getToken(KVQLParser.RP, 0); }
		public List<Modify_field_statementContext> modify_field_statement() {
			return getRuleContexts(Modify_field_statementContext.class);
		}
		public Add_field_statementContext add_field_statement(int i) {
			return getRuleContext(Add_field_statementContext.class,i);
		}
		public TerminalNode LP() { return getToken(KVQLParser.LP, 0); }
		public Drop_field_statementContext drop_field_statement(int i) {
			return getRuleContext(Drop_field_statementContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(KVQLParser.COMMA); }
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
			return getToken(KVQLParser.COMMA, i);
		}
		public Alter_field_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alter_field_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterAlter_field_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitAlter_field_statement(this);
		}
	}

	public final Alter_field_statementContext alter_field_statement() throws RecognitionException {
		Alter_field_statementContext _localctx = new Alter_field_statementContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_alter_field_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(721); match(LP);
			setState(725);
			switch (_input.LA(1)) {
			case ADD:
				{
				setState(722); add_field_statement();
				}
				break;
			case DROP:
				{
				setState(723); drop_field_statement();
				}
				break;
			case MODIFY:
				{
				setState(724); modify_field_statement();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(735);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(727); match(COMMA);
				setState(731);
				switch (_input.LA(1)) {
				case ADD:
					{
					setState(728); add_field_statement();
					}
					break;
				case DROP:
					{
					setState(729); drop_field_statement();
					}
					break;
				case MODIFY:
					{
					setState(730); modify_field_statement();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				}
				setState(737);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(738); match(RP);
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
		public CommentContext comment() {
			return getRuleContext(CommentContext.class,0);
		}
		public Name_pathContext name_path() {
			return getRuleContext(Name_pathContext.class,0);
		}
		public TerminalNode ADD() { return getToken(KVQLParser.ADD, 0); }
		public Default_defContext default_def() {
			return getRuleContext(Default_defContext.class,0);
		}
		public Type_defContext type_def() {
			return getRuleContext(Type_defContext.class,0);
		}
		public Add_field_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_add_field_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterAdd_field_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitAdd_field_statement(this);
		}
	}

	public final Add_field_statementContext add_field_statement() throws RecognitionException {
		Add_field_statementContext _localctx = new Add_field_statementContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_add_field_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(740); match(ADD);
			setState(741); name_path();
			setState(742); type_def();
			setState(744);
			_la = _input.LA(1);
			if (_la==DEFAULT || _la==NOT) {
				{
				setState(743); default_def();
				}
			}

			setState(747);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(746); comment();
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

	public static class Drop_field_statementContext extends ParserRuleContext {
		public Name_pathContext name_path() {
			return getRuleContext(Name_pathContext.class,0);
		}
		public TerminalNode DROP() { return getToken(KVQLParser.DROP, 0); }
		public Drop_field_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_field_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterDrop_field_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitDrop_field_statement(this);
		}
	}

	public final Drop_field_statementContext drop_field_statement() throws RecognitionException {
		Drop_field_statementContext _localctx = new Drop_field_statementContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_drop_field_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(749); match(DROP);
			setState(750); name_path();
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
		public CommentContext comment() {
			return getRuleContext(CommentContext.class,0);
		}
		public Name_pathContext name_path() {
			return getRuleContext(Name_pathContext.class,0);
		}
		public TerminalNode MODIFY() { return getToken(KVQLParser.MODIFY, 0); }
		public Default_defContext default_def() {
			return getRuleContext(Default_defContext.class,0);
		}
		public Type_defContext type_def() {
			return getRuleContext(Type_defContext.class,0);
		}
		public Modify_field_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_modify_field_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterModify_field_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitModify_field_statement(this);
		}
	}

	public final Modify_field_statementContext modify_field_statement() throws RecognitionException {
		Modify_field_statementContext _localctx = new Modify_field_statementContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_modify_field_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(752); match(MODIFY);
			setState(753); name_path();
			setState(754); type_def();
			setState(756);
			_la = _input.LA(1);
			if (_la==DEFAULT || _la==NOT) {
				{
				setState(755); default_def();
				}
			}

			setState(759);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(758); comment();
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

	public static class Drop_table_statementContext extends ParserRuleContext {
		public TerminalNode EXISTS() { return getToken(KVQLParser.EXISTS, 0); }
		public TerminalNode IF() { return getToken(KVQLParser.IF, 0); }
		public Name_pathContext name_path() {
			return getRuleContext(Name_pathContext.class,0);
		}
		public TerminalNode DROP() { return getToken(KVQLParser.DROP, 0); }
		public TerminalNode TABLE() { return getToken(KVQLParser.TABLE, 0); }
		public Drop_table_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_table_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterDrop_table_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitDrop_table_statement(this);
		}
	}

	public final Drop_table_statementContext drop_table_statement() throws RecognitionException {
		Drop_table_statementContext _localctx = new Drop_table_statementContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_drop_table_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(761); match(DROP);
			setState(762); match(TABLE);
			setState(765);
			switch ( getInterpreter().adaptivePredict(_input,72,_ctx) ) {
			case 1:
				{
				setState(763); match(IF);
				setState(764); match(EXISTS);
				}
				break;
			}
			setState(767); name_path();
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
		public TerminalNode INDEX() { return getToken(KVQLParser.INDEX, 0); }
		public TerminalNode NOT() { return getToken(KVQLParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(KVQLParser.EXISTS, 0); }
		public TerminalNode RP() { return getToken(KVQLParser.RP, 0); }
		public TerminalNode ON() { return getToken(KVQLParser.ON, 0); }
		public TerminalNode IF() { return getToken(KVQLParser.IF, 0); }
		public CommentContext comment() {
			return getRuleContext(CommentContext.class,0);
		}
		public Path_listContext path_list() {
			return getRuleContext(Path_listContext.class,0);
		}
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public TerminalNode LP() { return getToken(KVQLParser.LP, 0); }
		public Index_nameContext index_name() {
			return getRuleContext(Index_nameContext.class,0);
		}
		public TerminalNode CREATE() { return getToken(KVQLParser.CREATE, 0); }
		public Create_index_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_index_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterCreate_index_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitCreate_index_statement(this);
		}
	}

	public final Create_index_statementContext create_index_statement() throws RecognitionException {
		Create_index_statementContext _localctx = new Create_index_statementContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_create_index_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(769); match(CREATE);
			setState(770); match(INDEX);
			setState(774);
			switch ( getInterpreter().adaptivePredict(_input,73,_ctx) ) {
			case 1:
				{
				setState(771); match(IF);
				setState(772); match(NOT);
				setState(773); match(EXISTS);
				}
				break;
			}
			setState(776); index_name();
			setState(777); match(ON);
			setState(778); table_name();
			setState(787);
			switch ( getInterpreter().adaptivePredict(_input,74,_ctx) ) {
			case 1:
				{
				{
				setState(779); match(LP);
				setState(780); path_list();
				setState(781); match(RP);
				}
				}
				break;
			case 2:
				{
				{
				setState(783); match(LP);
				setState(784); path_list();
				 notifyErrorListeners("Missing closing ')'"); 
				}
				}
				break;
			}
			setState(790);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(789); comment();
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

	public static class Index_nameContext extends ParserRuleContext {
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public Index_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_index_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterIndex_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitIndex_name(this);
		}
	}

	public final Index_nameContext index_name() throws RecognitionException {
		Index_nameContext _localctx = new Index_nameContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_index_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(792); id();
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
		public List<TerminalNode> COMMA() { return getTokens(KVQLParser.COMMA); }
		public List<Complex_name_pathContext> complex_name_path() {
			return getRuleContexts(Complex_name_pathContext.class);
		}
		public TerminalNode COMMA(int i) {
			return getToken(KVQLParser.COMMA, i);
		}
		public Path_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_path_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterPath_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitPath_list(this);
		}
	}

	public final Path_listContext path_list() throws RecognitionException {
		Path_listContext _localctx = new Path_listContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_path_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(794); complex_name_path();
			setState(799);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(795); match(COMMA);
				setState(796); complex_name_path();
				}
				}
				setState(801);
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
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterComplex_name_path(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitComplex_name_path(this);
		}
	}

	public final Complex_name_pathContext complex_name_path() throws RecognitionException {
		Complex_name_pathContext _localctx = new Complex_name_pathContext(_ctx, getState());
		enterRule(_localctx, 140, RULE_complex_name_path);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(805);
			switch ( getInterpreter().adaptivePredict(_input,77,_ctx) ) {
			case 1:
				{
				setState(802); name_path();
				}
				break;
			case 2:
				{
				setState(803); keyof_expr();
				}
				break;
			case 3:
				{
				setState(804); elementof_expr();
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

	public static class Keyof_exprContext extends ParserRuleContext {
		public TerminalNode RP() { return getToken(KVQLParser.RP, 0); }
		public Name_pathContext name_path() {
			return getRuleContext(Name_pathContext.class,0);
		}
		public TerminalNode KEYOF() { return getToken(KVQLParser.KEYOF, 0); }
		public TerminalNode LP() { return getToken(KVQLParser.LP, 0); }
		public Keyof_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyof_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterKeyof_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitKeyof_expr(this);
		}
	}

	public final Keyof_exprContext keyof_expr() throws RecognitionException {
		Keyof_exprContext _localctx = new Keyof_exprContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_keyof_expr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(807); match(KEYOF);
			setState(808); match(LP);
			setState(809); name_path();
			setState(810); match(RP);
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
		public TerminalNode ELEMENTOF() { return getToken(KVQLParser.ELEMENTOF, 0); }
		public TerminalNode RP() { return getToken(KVQLParser.RP, 0); }
		public List<Name_pathContext> name_path() {
			return getRuleContexts(Name_pathContext.class);
		}
		public TerminalNode LP() { return getToken(KVQLParser.LP, 0); }
		public Name_pathContext name_path(int i) {
			return getRuleContext(Name_pathContext.class,i);
		}
		public Elementof_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elementof_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterElementof_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitElementof_expr(this);
		}
	}

	public final Elementof_exprContext elementof_expr() throws RecognitionException {
		Elementof_exprContext _localctx = new Elementof_exprContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_elementof_expr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(812); match(ELEMENTOF);
			setState(813); match(LP);
			setState(814); name_path();
			setState(815); match(RP);
			setState(818);
			_la = _input.LA(1);
			if (_la==DOT) {
				{
				setState(816); match(DOT);
				setState(817); name_path();
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

	public static class Create_text_index_statementContext extends ParserRuleContext {
		public TerminalNode INDEX() { return getToken(KVQLParser.INDEX, 0); }
		public TerminalNode NOT() { return getToken(KVQLParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(KVQLParser.EXISTS, 0); }
		public TerminalNode ON() { return getToken(KVQLParser.ON, 0); }
		public TerminalNode IF() { return getToken(KVQLParser.IF, 0); }
		public CommentContext comment() {
			return getRuleContext(CommentContext.class,0);
		}
		public Table_nameContext table_name() {
			return getRuleContext(Table_nameContext.class,0);
		}
		public Index_nameContext index_name() {
			return getRuleContext(Index_nameContext.class,0);
		}
		public Fts_field_listContext fts_field_list() {
			return getRuleContext(Fts_field_listContext.class,0);
		}
		public Es_propertiesContext es_properties() {
			return getRuleContext(Es_propertiesContext.class,0);
		}
		public TerminalNode CREATE() { return getToken(KVQLParser.CREATE, 0); }
		public TerminalNode FULLTEXT() { return getToken(KVQLParser.FULLTEXT, 0); }
		public Create_text_index_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_text_index_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterCreate_text_index_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitCreate_text_index_statement(this);
		}
	}

	public final Create_text_index_statementContext create_text_index_statement() throws RecognitionException {
		Create_text_index_statementContext _localctx = new Create_text_index_statementContext(_ctx, getState());
		enterRule(_localctx, 146, RULE_create_text_index_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(820); match(CREATE);
			setState(821); match(FULLTEXT);
			setState(822); match(INDEX);
			setState(826);
			switch ( getInterpreter().adaptivePredict(_input,79,_ctx) ) {
			case 1:
				{
				setState(823); match(IF);
				setState(824); match(NOT);
				setState(825); match(EXISTS);
				}
				break;
			}
			setState(828); index_name();
			setState(829); match(ON);
			setState(830); table_name();
			setState(831); fts_field_list();
			setState(833);
			_la = _input.LA(1);
			if (_la==ES_SHARDS || _la==ES_REPLICAS) {
				{
				setState(832); es_properties();
				}
			}

			setState(836);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(835); comment();
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

	public static class Fts_field_listContext extends ParserRuleContext {
		public TerminalNode RP() { return getToken(KVQLParser.RP, 0); }
		public TerminalNode LP() { return getToken(KVQLParser.LP, 0); }
		public Fts_path_listContext fts_path_list() {
			return getRuleContext(Fts_path_listContext.class,0);
		}
		public Fts_field_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fts_field_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterFts_field_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitFts_field_list(this);
		}
	}

	public final Fts_field_listContext fts_field_list() throws RecognitionException {
		Fts_field_listContext _localctx = new Fts_field_listContext(_ctx, getState());
		enterRule(_localctx, 148, RULE_fts_field_list);
		try {
			setState(846);
			switch ( getInterpreter().adaptivePredict(_input,82,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(838); match(LP);
				setState(839); fts_path_list();
				setState(840); match(RP);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(842); match(LP);
				setState(843); fts_path_list();
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
		public List<TerminalNode> COMMA() { return getTokens(KVQLParser.COMMA); }
		public List<Fts_pathContext> fts_path() {
			return getRuleContexts(Fts_pathContext.class);
		}
		public TerminalNode COMMA(int i) {
			return getToken(KVQLParser.COMMA, i);
		}
		public Fts_path_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fts_path_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterFts_path_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitFts_path_list(this);
		}
	}

	public final Fts_path_listContext fts_path_list() throws RecognitionException {
		Fts_path_listContext _localctx = new Fts_path_listContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_fts_path_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(848); fts_path();
			setState(853);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(849); match(COMMA);
				setState(850); fts_path();
				}
				}
				setState(855);
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
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterFts_path(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitFts_path(this);
		}
	}

	public final Fts_pathContext fts_path() throws RecognitionException {
		Fts_pathContext _localctx = new Fts_pathContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_fts_path);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(856); complex_name_path();
			setState(858);
			_la = _input.LA(1);
			if (_la==LBRACK || _la==LBRACE) {
				{
				setState(857); json();
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

	public static class Es_propertiesContext extends ParserRuleContext {
		public Es_property_assignmentContext es_property_assignment(int i) {
			return getRuleContext(Es_property_assignmentContext.class,i);
		}
		public List<Es_property_assignmentContext> es_property_assignment() {
			return getRuleContexts(Es_property_assignmentContext.class);
		}
		public Es_propertiesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_es_properties; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterEs_properties(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitEs_properties(this);
		}
	}

	public final Es_propertiesContext es_properties() throws RecognitionException {
		Es_propertiesContext _localctx = new Es_propertiesContext(_ctx, getState());
		enterRule(_localctx, 154, RULE_es_properties);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(860); es_property_assignment();
			setState(864);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ES_SHARDS || _la==ES_REPLICAS) {
				{
				{
				setState(861); es_property_assignment();
				}
				}
				setState(866);
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

	public static class Es_property_assignmentContext extends ParserRuleContext {
		public TerminalNode ES_SHARDS() { return getToken(KVQLParser.ES_SHARDS, 0); }
		public TerminalNode ES_REPLICAS() { return getToken(KVQLParser.ES_REPLICAS, 0); }
		public TerminalNode EQ() { return getToken(KVQLParser.EQ, 0); }
		public TerminalNode INT() { return getToken(KVQLParser.INT, 0); }
		public Es_property_assignmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_es_property_assignment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterEs_property_assignment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitEs_property_assignment(this);
		}
	}

	public final Es_property_assignmentContext es_property_assignment() throws RecognitionException {
		Es_property_assignmentContext _localctx = new Es_property_assignmentContext(_ctx, getState());
		enterRule(_localctx, 156, RULE_es_property_assignment);
		try {
			setState(873);
			switch (_input.LA(1)) {
			case ES_SHARDS:
				enterOuterAlt(_localctx, 1);
				{
				setState(867); match(ES_SHARDS);
				setState(868); match(EQ);
				setState(869); match(INT);
				}
				break;
			case ES_REPLICAS:
				enterOuterAlt(_localctx, 2);
				{
				setState(870); match(ES_REPLICAS);
				setState(871); match(EQ);
				setState(872); match(INT);
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

	public static class Drop_index_statementContext extends ParserRuleContext {
		public TerminalNode INDEX() { return getToken(KVQLParser.INDEX, 0); }
		public TerminalNode EXISTS() { return getToken(KVQLParser.EXISTS, 0); }
		public TerminalNode ON() { return getToken(KVQLParser.ON, 0); }
		public TerminalNode IF() { return getToken(KVQLParser.IF, 0); }
		public Name_pathContext name_path() {
			return getRuleContext(Name_pathContext.class,0);
		}
		public Index_nameContext index_name() {
			return getRuleContext(Index_nameContext.class,0);
		}
		public TerminalNode DROP() { return getToken(KVQLParser.DROP, 0); }
		public Drop_index_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_index_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterDrop_index_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitDrop_index_statement(this);
		}
	}

	public final Drop_index_statementContext drop_index_statement() throws RecognitionException {
		Drop_index_statementContext _localctx = new Drop_index_statementContext(_ctx, getState());
		enterRule(_localctx, 158, RULE_drop_index_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(875); match(DROP);
			setState(876); match(INDEX);
			setState(879);
			switch ( getInterpreter().adaptivePredict(_input,87,_ctx) ) {
			case 1:
				{
				setState(877); match(IF);
				setState(878); match(EXISTS);
				}
				break;
			}
			setState(881); index_name();
			setState(882); match(ON);
			setState(883); name_path();
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
		public TerminalNode INDEX() { return getToken(KVQLParser.INDEX, 0); }
		public TerminalNode RP() { return getToken(KVQLParser.RP, 0); }
		public TerminalNode ON() { return getToken(KVQLParser.ON, 0); }
		public Path_listContext path_list() {
			return getRuleContext(Path_listContext.class,0);
		}
		public TerminalNode JSON() { return getToken(KVQLParser.JSON, 0); }
		public Name_pathContext name_path() {
			return getRuleContext(Name_pathContext.class,0);
		}
		public TerminalNode LP() { return getToken(KVQLParser.LP, 0); }
		public Index_nameContext index_name() {
			return getRuleContext(Index_nameContext.class,0);
		}
		public TerminalNode DESCRIBE() { return getToken(KVQLParser.DESCRIBE, 0); }
		public TerminalNode DESC() { return getToken(KVQLParser.DESC, 0); }
		public TerminalNode TABLE() { return getToken(KVQLParser.TABLE, 0); }
		public TerminalNode AS() { return getToken(KVQLParser.AS, 0); }
		public Describe_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_describe_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterDescribe_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitDescribe_statement(this);
		}
	}

	public final Describe_statementContext describe_statement() throws RecognitionException {
		Describe_statementContext _localctx = new Describe_statementContext(_ctx, getState());
		enterRule(_localctx, 160, RULE_describe_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(885);
			_la = _input.LA(1);
			if ( !(_la==DESC || _la==DESCRIBE) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			setState(888);
			_la = _input.LA(1);
			if (_la==AS) {
				{
				setState(886); match(AS);
				setState(887); match(JSON);
				}
			}

			setState(907);
			switch (_input.LA(1)) {
			case TABLE:
				{
				setState(890); match(TABLE);
				setState(891); name_path();
				setState(900);
				switch ( getInterpreter().adaptivePredict(_input,89,_ctx) ) {
				case 1:
					{
					{
					setState(892); match(LP);
					setState(893); path_list();
					setState(894); match(RP);
					}
					}
					break;
				case 2:
					{
					{
					setState(896); match(LP);
					setState(897); path_list();
					 notifyErrorListeners("Missing closing ')'"); 
					}
					}
					break;
				}
				}
				break;
			case INDEX:
				{
				setState(902); match(INDEX);
				setState(903); index_name();
				setState(904); match(ON);
				setState(905); name_path();
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
		public TerminalNode SHOW() { return getToken(KVQLParser.SHOW, 0); }
		public TerminalNode ON() { return getToken(KVQLParser.ON, 0); }
		public TerminalNode JSON() { return getToken(KVQLParser.JSON, 0); }
		public Name_pathContext name_path() {
			return getRuleContext(Name_pathContext.class,0);
		}
		public TerminalNode TABLES() { return getToken(KVQLParser.TABLES, 0); }
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public TerminalNode USER() { return getToken(KVQLParser.USER, 0); }
		public TerminalNode TABLE() { return getToken(KVQLParser.TABLE, 0); }
		public TerminalNode AS() { return getToken(KVQLParser.AS, 0); }
		public TerminalNode INDEXES() { return getToken(KVQLParser.INDEXES, 0); }
		public TerminalNode ROLES() { return getToken(KVQLParser.ROLES, 0); }
		public Identifier_or_stringContext identifier_or_string() {
			return getRuleContext(Identifier_or_stringContext.class,0);
		}
		public TerminalNode USERS() { return getToken(KVQLParser.USERS, 0); }
		public TerminalNode ROLE() { return getToken(KVQLParser.ROLE, 0); }
		public Show_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_show_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterShow_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitShow_statement(this);
		}
	}

	public final Show_statementContext show_statement() throws RecognitionException {
		Show_statementContext _localctx = new Show_statementContext(_ctx, getState());
		enterRule(_localctx, 162, RULE_show_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(909); match(SHOW);
			setState(912);
			_la = _input.LA(1);
			if (_la==AS) {
				{
				setState(910); match(AS);
				setState(911); match(JSON);
				}
			}

			setState(926);
			switch (_input.LA(1)) {
			case TABLES:
				{
				setState(914); match(TABLES);
				}
				break;
			case USERS:
				{
				setState(915); match(USERS);
				}
				break;
			case ROLES:
				{
				setState(916); match(ROLES);
				}
				break;
			case USER:
				{
				setState(917); match(USER);
				setState(918); identifier_or_string();
				}
				break;
			case ROLE:
				{
				setState(919); match(ROLE);
				setState(920); id();
				}
				break;
			case INDEXES:
				{
				setState(921); match(INDEXES);
				setState(922); match(ON);
				setState(923); name_path();
				}
				break;
			case TABLE:
				{
				setState(924); match(TABLE);
				setState(925); name_path();
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

	public static class Create_user_statementContext extends ParserRuleContext {
		public TerminalNode ADMIN() { return getToken(KVQLParser.ADMIN, 0); }
		public Account_lockContext account_lock() {
			return getRuleContext(Account_lockContext.class,0);
		}
		public TerminalNode CREATE() { return getToken(KVQLParser.CREATE, 0); }
		public TerminalNode USER() { return getToken(KVQLParser.USER, 0); }
		public Create_user_identified_clauseContext create_user_identified_clause() {
			return getRuleContext(Create_user_identified_clauseContext.class,0);
		}
		public Create_user_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_user_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterCreate_user_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitCreate_user_statement(this);
		}
	}

	public final Create_user_statementContext create_user_statement() throws RecognitionException {
		Create_user_statementContext _localctx = new Create_user_statementContext(_ctx, getState());
		enterRule(_localctx, 164, RULE_create_user_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(928); match(CREATE);
			setState(929); match(USER);
			setState(930); create_user_identified_clause();
			setState(932);
			_la = _input.LA(1);
			if (_la==ACCOUNT) {
				{
				setState(931); account_lock();
				}
			}

			setState(935);
			_la = _input.LA(1);
			if (_la==ADMIN) {
				{
				setState(934); match(ADMIN);
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
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public TerminalNode CREATE() { return getToken(KVQLParser.CREATE, 0); }
		public TerminalNode ROLE() { return getToken(KVQLParser.ROLE, 0); }
		public Create_role_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_role_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterCreate_role_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitCreate_role_statement(this);
		}
	}

	public final Create_role_statementContext create_role_statement() throws RecognitionException {
		Create_role_statementContext _localctx = new Create_role_statementContext(_ctx, getState());
		enterRule(_localctx, 166, RULE_create_role_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(937); match(CREATE);
			setState(938); match(ROLE);
			setState(939); id();
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
		public TerminalNode CLEAR_RETAINED_PASSWORD() { return getToken(KVQLParser.CLEAR_RETAINED_PASSWORD, 0); }
		public TerminalNode ALTER() { return getToken(KVQLParser.ALTER, 0); }
		public TerminalNode PASSWORD_EXPIRE() { return getToken(KVQLParser.PASSWORD_EXPIRE, 0); }
		public TerminalNode USER() { return getToken(KVQLParser.USER, 0); }
		public Alter_user_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alter_user_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterAlter_user_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitAlter_user_statement(this);
		}
	}

	public final Alter_user_statementContext alter_user_statement() throws RecognitionException {
		Alter_user_statementContext _localctx = new Alter_user_statementContext(_ctx, getState());
		enterRule(_localctx, 168, RULE_alter_user_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(941); match(ALTER);
			setState(942); match(USER);
			setState(943); identifier_or_string();
			setState(945);
			_la = _input.LA(1);
			if (_la==IDENTIFIED) {
				{
				setState(944); reset_password_clause();
				}
			}

			setState(948);
			_la = _input.LA(1);
			if (_la==CLEAR_RETAINED_PASSWORD) {
				{
				setState(947); match(CLEAR_RETAINED_PASSWORD);
				}
			}

			setState(951);
			_la = _input.LA(1);
			if (_la==PASSWORD_EXPIRE) {
				{
				setState(950); match(PASSWORD_EXPIRE);
				}
			}

			setState(954);
			_la = _input.LA(1);
			if (_la==PASSWORD) {
				{
				setState(953); password_lifetime();
				}
			}

			setState(957);
			_la = _input.LA(1);
			if (_la==ACCOUNT) {
				{
				setState(956); account_lock();
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
		public TerminalNode DROP() { return getToken(KVQLParser.DROP, 0); }
		public TerminalNode USER() { return getToken(KVQLParser.USER, 0); }
		public Drop_user_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_user_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterDrop_user_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitDrop_user_statement(this);
		}
	}

	public final Drop_user_statementContext drop_user_statement() throws RecognitionException {
		Drop_user_statementContext _localctx = new Drop_user_statementContext(_ctx, getState());
		enterRule(_localctx, 170, RULE_drop_user_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(959); match(DROP);
			setState(960); match(USER);
			setState(961); identifier_or_string();
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
		public TerminalNode DROP() { return getToken(KVQLParser.DROP, 0); }
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public TerminalNode ROLE() { return getToken(KVQLParser.ROLE, 0); }
		public Drop_role_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drop_role_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterDrop_role_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitDrop_role_statement(this);
		}
	}

	public final Drop_role_statementContext drop_role_statement() throws RecognitionException {
		Drop_role_statementContext _localctx = new Drop_role_statementContext(_ctx, getState());
		enterRule(_localctx, 172, RULE_drop_role_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(963); match(DROP);
			setState(964); match(ROLE);
			setState(965); id();
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
		public TerminalNode GRANT() { return getToken(KVQLParser.GRANT, 0); }
		public Grant_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_grant_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterGrant_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitGrant_statement(this);
		}
	}

	public final Grant_statementContext grant_statement() throws RecognitionException {
		Grant_statementContext _localctx = new Grant_statementContext(_ctx, getState());
		enterRule(_localctx, 174, RULE_grant_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(967); match(GRANT);
			setState(971);
			switch ( getInterpreter().adaptivePredict(_input,100,_ctx) ) {
			case 1:
				{
				setState(968); grant_roles();
				}
				break;
			case 2:
				{
				setState(969); grant_system_privileges();
				}
				break;
			case 3:
				{
				setState(970); grant_object_privileges();
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
		public TerminalNode REVOKE() { return getToken(KVQLParser.REVOKE, 0); }
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
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterRevoke_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitRevoke_statement(this);
		}
	}

	public final Revoke_statementContext revoke_statement() throws RecognitionException {
		Revoke_statementContext _localctx = new Revoke_statementContext(_ctx, getState());
		enterRule(_localctx, 176, RULE_revoke_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(973); match(REVOKE);
			setState(977);
			switch ( getInterpreter().adaptivePredict(_input,101,_ctx) ) {
			case 1:
				{
				setState(974); revoke_roles();
				}
				break;
			case 2:
				{
				setState(975); revoke_system_privileges();
				}
				break;
			case 3:
				{
				setState(976); revoke_object_privileges();
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

	public static class Identifier_or_stringContext extends ParserRuleContext {
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public Identifier_or_stringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier_or_string; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterIdentifier_or_string(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitIdentifier_or_string(this);
		}
	}

	public final Identifier_or_stringContext identifier_or_string() throws RecognitionException {
		Identifier_or_stringContext _localctx = new Identifier_or_stringContext(_ctx, getState());
		enterRule(_localctx, 178, RULE_identifier_or_string);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(981);
			switch (_input.LA(1)) {
			case ACCOUNT:
			case ADD:
			case ADMIN:
			case ALL:
			case ALTER:
			case AND:
			case AS:
			case ASC:
			case BY:
			case COMMENT:
			case CREATE:
			case DECLARE:
			case DEFAULT:
			case DESC:
			case DESCRIBE:
			case DROP:
			case ELEMENTOF:
			case ES_SHARDS:
			case ES_REPLICAS:
			case EXISTS:
			case FIRST:
			case FROM:
			case FULLTEXT:
			case GRANT:
			case IDENTIFIED:
			case IF:
			case INDEX:
			case INDEXES:
			case JSON:
			case KEY:
			case KEYOF:
			case LAST:
			case LIFETIME:
			case LOCK:
			case MODIFY:
			case NOT:
			case NULLS:
			case ON:
			case OR:
			case ORDER:
			case PASSWORD:
			case PRIMARY:
			case REVOKE:
			case ROLE:
			case ROLES:
			case SELECT:
			case SHARD:
			case SHOW:
			case TABLE:
			case TABLES:
			case TIME_UNIT:
			case TO:
			case TTL:
			case UNLOCK:
			case USER:
			case USERS:
			case USING:
			case WHERE:
			case ARRAY_T:
			case BINARY_T:
			case BOOLEAN_T:
			case DOUBLE_T:
			case ENUM_T:
			case FLOAT_T:
			case INTEGER_T:
			case LONG_T:
			case MAP_T:
			case RECORD_T:
			case STRING_T:
			case ID:
			case BAD_ID:
				{
				setState(979); id();
				}
				break;
			case DSTRING:
			case STRING:
				{
				setState(980); string();
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
		public TerminalNode IDENTIFIED() { return getToken(KVQLParser.IDENTIFIED, 0); }
		public By_passwordContext by_password() {
			return getRuleContext(By_passwordContext.class,0);
		}
		public Identified_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identified_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterIdentified_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitIdentified_clause(this);
		}
	}

	public final Identified_clauseContext identified_clause() throws RecognitionException {
		Identified_clauseContext _localctx = new Identified_clauseContext(_ctx, getState());
		enterRule(_localctx, 180, RULE_identified_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(983); match(IDENTIFIED);
			setState(984); by_password();
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
		public TerminalNode IDENTIFIED_EXTERNALLY() { return getToken(KVQLParser.IDENTIFIED_EXTERNALLY, 0); }
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public TerminalNode PASSWORD_EXPIRE() { return getToken(KVQLParser.PASSWORD_EXPIRE, 0); }
		public Identified_clauseContext identified_clause() {
			return getRuleContext(Identified_clauseContext.class,0);
		}
		public Create_user_identified_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_create_user_identified_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterCreate_user_identified_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitCreate_user_identified_clause(this);
		}
	}

	public final Create_user_identified_clauseContext create_user_identified_clause() throws RecognitionException {
		Create_user_identified_clauseContext _localctx = new Create_user_identified_clauseContext(_ctx, getState());
		enterRule(_localctx, 182, RULE_create_user_identified_clause);
		int _la;
		try {
			setState(997);
			switch (_input.LA(1)) {
			case ACCOUNT:
			case ADD:
			case ADMIN:
			case ALL:
			case ALTER:
			case AND:
			case AS:
			case ASC:
			case BY:
			case COMMENT:
			case CREATE:
			case DECLARE:
			case DEFAULT:
			case DESC:
			case DESCRIBE:
			case DROP:
			case ELEMENTOF:
			case ES_SHARDS:
			case ES_REPLICAS:
			case EXISTS:
			case FIRST:
			case FROM:
			case FULLTEXT:
			case GRANT:
			case IDENTIFIED:
			case IF:
			case INDEX:
			case INDEXES:
			case JSON:
			case KEY:
			case KEYOF:
			case LAST:
			case LIFETIME:
			case LOCK:
			case MODIFY:
			case NOT:
			case NULLS:
			case ON:
			case OR:
			case ORDER:
			case PASSWORD:
			case PRIMARY:
			case REVOKE:
			case ROLE:
			case ROLES:
			case SELECT:
			case SHARD:
			case SHOW:
			case TABLE:
			case TABLES:
			case TIME_UNIT:
			case TO:
			case TTL:
			case UNLOCK:
			case USER:
			case USERS:
			case USING:
			case WHERE:
			case ARRAY_T:
			case BINARY_T:
			case BOOLEAN_T:
			case DOUBLE_T:
			case ENUM_T:
			case FLOAT_T:
			case INTEGER_T:
			case LONG_T:
			case MAP_T:
			case RECORD_T:
			case STRING_T:
			case ID:
			case BAD_ID:
				enterOuterAlt(_localctx, 1);
				{
				setState(986); id();
				setState(987); identified_clause();
				setState(989);
				_la = _input.LA(1);
				if (_la==PASSWORD_EXPIRE) {
					{
					setState(988); match(PASSWORD_EXPIRE);
					}
				}

				setState(992);
				_la = _input.LA(1);
				if (_la==PASSWORD) {
					{
					setState(991); password_lifetime();
					}
				}

				}
				break;
			case DSTRING:
			case STRING:
				enterOuterAlt(_localctx, 2);
				{
				setState(994); string();
				setState(995); match(IDENTIFIED_EXTERNALLY);
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

	public static class By_passwordContext extends ParserRuleContext {
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public TerminalNode BY() { return getToken(KVQLParser.BY, 0); }
		public By_passwordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_by_password; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterBy_password(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitBy_password(this);
		}
	}

	public final By_passwordContext by_password() throws RecognitionException {
		By_passwordContext _localctx = new By_passwordContext(_ctx, getState());
		enterRule(_localctx, 184, RULE_by_password);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(999); match(BY);
			setState(1000); string();
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
		public DurationContext duration() {
			return getRuleContext(DurationContext.class,0);
		}
		public TerminalNode LIFETIME() { return getToken(KVQLParser.LIFETIME, 0); }
		public TerminalNode PASSWORD() { return getToken(KVQLParser.PASSWORD, 0); }
		public Password_lifetimeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_password_lifetime; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterPassword_lifetime(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitPassword_lifetime(this);
		}
	}

	public final Password_lifetimeContext password_lifetime() throws RecognitionException {
		Password_lifetimeContext _localctx = new Password_lifetimeContext(_ctx, getState());
		enterRule(_localctx, 186, RULE_password_lifetime);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1002); match(PASSWORD);
			setState(1003); match(LIFETIME);
			setState(1004); duration();
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
		public TerminalNode RETAIN_CURRENT_PASSWORD() { return getToken(KVQLParser.RETAIN_CURRENT_PASSWORD, 0); }
		public Identified_clauseContext identified_clause() {
			return getRuleContext(Identified_clauseContext.class,0);
		}
		public Reset_password_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_reset_password_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterReset_password_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitReset_password_clause(this);
		}
	}

	public final Reset_password_clauseContext reset_password_clause() throws RecognitionException {
		Reset_password_clauseContext _localctx = new Reset_password_clauseContext(_ctx, getState());
		enterRule(_localctx, 188, RULE_reset_password_clause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1006); identified_clause();
			setState(1008);
			_la = _input.LA(1);
			if (_la==RETAIN_CURRENT_PASSWORD) {
				{
				setState(1007); match(RETAIN_CURRENT_PASSWORD);
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
		public TerminalNode ACCOUNT() { return getToken(KVQLParser.ACCOUNT, 0); }
		public TerminalNode LOCK() { return getToken(KVQLParser.LOCK, 0); }
		public TerminalNode UNLOCK() { return getToken(KVQLParser.UNLOCK, 0); }
		public Account_lockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_account_lock; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterAccount_lock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitAccount_lock(this);
		}
	}

	public final Account_lockContext account_lock() throws RecognitionException {
		Account_lockContext _localctx = new Account_lockContext(_ctx, getState());
		enterRule(_localctx, 190, RULE_account_lock);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1010); match(ACCOUNT);
			setState(1011);
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
		public TerminalNode TO() { return getToken(KVQLParser.TO, 0); }
		public Grant_rolesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_grant_roles; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterGrant_roles(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitGrant_roles(this);
		}
	}

	public final Grant_rolesContext grant_roles() throws RecognitionException {
		Grant_rolesContext _localctx = new Grant_rolesContext(_ctx, getState());
		enterRule(_localctx, 192, RULE_grant_roles);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1013); id_list();
			setState(1014); match(TO);
			setState(1015); principal();
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
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public TerminalNode TO() { return getToken(KVQLParser.TO, 0); }
		public Sys_priv_listContext sys_priv_list() {
			return getRuleContext(Sys_priv_listContext.class,0);
		}
		public Grant_system_privilegesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_grant_system_privileges; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterGrant_system_privileges(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitGrant_system_privileges(this);
		}
	}

	public final Grant_system_privilegesContext grant_system_privileges() throws RecognitionException {
		Grant_system_privilegesContext _localctx = new Grant_system_privilegesContext(_ctx, getState());
		enterRule(_localctx, 194, RULE_grant_system_privileges);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1017); sys_priv_list();
			setState(1018); match(TO);
			setState(1019); id();
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
		public TerminalNode ON() { return getToken(KVQLParser.ON, 0); }
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public TerminalNode TO() { return getToken(KVQLParser.TO, 0); }
		public Obj_priv_listContext obj_priv_list() {
			return getRuleContext(Obj_priv_listContext.class,0);
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
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterGrant_object_privileges(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitGrant_object_privileges(this);
		}
	}

	public final Grant_object_privilegesContext grant_object_privileges() throws RecognitionException {
		Grant_object_privilegesContext _localctx = new Grant_object_privilegesContext(_ctx, getState());
		enterRule(_localctx, 196, RULE_grant_object_privileges);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1021); obj_priv_list();
			setState(1022); match(ON);
			setState(1023); object();
			setState(1024); match(TO);
			setState(1025); id();
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
		public TerminalNode FROM() { return getToken(KVQLParser.FROM, 0); }
		public Revoke_rolesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_revoke_roles; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterRevoke_roles(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitRevoke_roles(this);
		}
	}

	public final Revoke_rolesContext revoke_roles() throws RecognitionException {
		Revoke_rolesContext _localctx = new Revoke_rolesContext(_ctx, getState());
		enterRule(_localctx, 198, RULE_revoke_roles);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1027); id_list();
			setState(1028); match(FROM);
			setState(1029); principal();
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
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public TerminalNode FROM() { return getToken(KVQLParser.FROM, 0); }
		public Sys_priv_listContext sys_priv_list() {
			return getRuleContext(Sys_priv_listContext.class,0);
		}
		public Revoke_system_privilegesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_revoke_system_privileges; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterRevoke_system_privileges(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitRevoke_system_privileges(this);
		}
	}

	public final Revoke_system_privilegesContext revoke_system_privileges() throws RecognitionException {
		Revoke_system_privilegesContext _localctx = new Revoke_system_privilegesContext(_ctx, getState());
		enterRule(_localctx, 200, RULE_revoke_system_privileges);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1031); sys_priv_list();
			setState(1032); match(FROM);
			setState(1033); id();
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
		public TerminalNode ON() { return getToken(KVQLParser.ON, 0); }
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public TerminalNode FROM() { return getToken(KVQLParser.FROM, 0); }
		public Obj_priv_listContext obj_priv_list() {
			return getRuleContext(Obj_priv_listContext.class,0);
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
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterRevoke_object_privileges(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitRevoke_object_privileges(this);
		}
	}

	public final Revoke_object_privilegesContext revoke_object_privileges() throws RecognitionException {
		Revoke_object_privilegesContext _localctx = new Revoke_object_privilegesContext(_ctx, getState());
		enterRule(_localctx, 202, RULE_revoke_object_privileges);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1035); obj_priv_list();
			setState(1036); match(ON);
			setState(1037); object();
			setState(1038); match(FROM);
			setState(1039); id();
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
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public TerminalNode USER() { return getToken(KVQLParser.USER, 0); }
		public TerminalNode ROLE() { return getToken(KVQLParser.ROLE, 0); }
		public PrincipalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_principal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterPrincipal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitPrincipal(this);
		}
	}

	public final PrincipalContext principal() throws RecognitionException {
		PrincipalContext _localctx = new PrincipalContext(_ctx, getState());
		enterRule(_localctx, 204, RULE_principal);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1045);
			switch (_input.LA(1)) {
			case USER:
				{
				setState(1041); match(USER);
				setState(1042); identifier_or_string();
				}
				break;
			case ROLE:
				{
				setState(1043); match(ROLE);
				setState(1044); id();
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
		public List<TerminalNode> COMMA() { return getTokens(KVQLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(KVQLParser.COMMA, i);
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
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterSys_priv_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitSys_priv_list(this);
		}
	}

	public final Sys_priv_listContext sys_priv_list() throws RecognitionException {
		Sys_priv_listContext _localctx = new Sys_priv_listContext(_ctx, getState());
		enterRule(_localctx, 206, RULE_sys_priv_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1047); priv_item();
			setState(1052);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1048); match(COMMA);
				setState(1049); priv_item();
				}
				}
				setState(1054);
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
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public TerminalNode ALL_PRIVILEGES() { return getToken(KVQLParser.ALL_PRIVILEGES, 0); }
		public Priv_itemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_priv_item; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterPriv_item(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitPriv_item(this);
		}
	}

	public final Priv_itemContext priv_item() throws RecognitionException {
		Priv_itemContext _localctx = new Priv_itemContext(_ctx, getState());
		enterRule(_localctx, 208, RULE_priv_item);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1057);
			switch (_input.LA(1)) {
			case ACCOUNT:
			case ADD:
			case ADMIN:
			case ALL:
			case ALTER:
			case AND:
			case AS:
			case ASC:
			case BY:
			case COMMENT:
			case CREATE:
			case DECLARE:
			case DEFAULT:
			case DESC:
			case DESCRIBE:
			case DROP:
			case ELEMENTOF:
			case ES_SHARDS:
			case ES_REPLICAS:
			case EXISTS:
			case FIRST:
			case FROM:
			case FULLTEXT:
			case GRANT:
			case IDENTIFIED:
			case IF:
			case INDEX:
			case INDEXES:
			case JSON:
			case KEY:
			case KEYOF:
			case LAST:
			case LIFETIME:
			case LOCK:
			case MODIFY:
			case NOT:
			case NULLS:
			case ON:
			case OR:
			case ORDER:
			case PASSWORD:
			case PRIMARY:
			case REVOKE:
			case ROLE:
			case ROLES:
			case SELECT:
			case SHARD:
			case SHOW:
			case TABLE:
			case TABLES:
			case TIME_UNIT:
			case TO:
			case TTL:
			case UNLOCK:
			case USER:
			case USERS:
			case USING:
			case WHERE:
			case ARRAY_T:
			case BINARY_T:
			case BOOLEAN_T:
			case DOUBLE_T:
			case ENUM_T:
			case FLOAT_T:
			case INTEGER_T:
			case LONG_T:
			case MAP_T:
			case RECORD_T:
			case STRING_T:
			case ID:
			case BAD_ID:
				{
				setState(1055); id();
				}
				break;
			case ALL_PRIVILEGES:
				{
				setState(1056); match(ALL_PRIVILEGES);
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
		public List<TerminalNode> COMMA() { return getTokens(KVQLParser.COMMA); }
		public TerminalNode ALL(int i) {
			return getToken(KVQLParser.ALL, i);
		}
		public TerminalNode COMMA(int i) {
			return getToken(KVQLParser.COMMA, i);
		}
		public List<Priv_itemContext> priv_item() {
			return getRuleContexts(Priv_itemContext.class);
		}
		public List<TerminalNode> ALL() { return getTokens(KVQLParser.ALL); }
		public Obj_priv_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_obj_priv_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterObj_priv_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitObj_priv_list(this);
		}
	}

	public final Obj_priv_listContext obj_priv_list() throws RecognitionException {
		Obj_priv_listContext _localctx = new Obj_priv_listContext(_ctx, getState());
		enterRule(_localctx, 210, RULE_obj_priv_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1061);
			switch ( getInterpreter().adaptivePredict(_input,110,_ctx) ) {
			case 1:
				{
				setState(1059); priv_item();
				}
				break;
			case 2:
				{
				setState(1060); match(ALL);
				}
				break;
			}
			setState(1070);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1063); match(COMMA);
				setState(1066);
				switch ( getInterpreter().adaptivePredict(_input,111,_ctx) ) {
				case 1:
					{
					setState(1064); priv_item();
					}
					break;
				case 2:
					{
					setState(1065); match(ALL);
					}
					break;
				}
				}
				}
				setState(1072);
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
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterObject(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitObject(this);
		}
	}

	public final ObjectContext object() throws RecognitionException {
		ObjectContext _localctx = new ObjectContext(_ctx, getState());
		enterRule(_localctx, 212, RULE_object);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1073); name_path();
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
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterJson(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitJson(this);
		}
	}

	public final JsonContext json() throws RecognitionException {
		JsonContext _localctx = new JsonContext(_ctx, getState());
		enterRule(_localctx, 214, RULE_json);
		try {
			setState(1077);
			switch (_input.LA(1)) {
			case LBRACE:
				enterOuterAlt(_localctx, 1);
				{
				setState(1075); jsobject();
				}
				break;
			case LBRACK:
				enterOuterAlt(_localctx, 2);
				{
				setState(1076); jsarray();
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
		public TerminalNode RBRACE() { return getToken(KVQLParser.RBRACE, 0); }
		public TerminalNode LBRACE() { return getToken(KVQLParser.LBRACE, 0); }
		public List<JspairContext> jspair() {
			return getRuleContexts(JspairContext.class);
		}
		public JspairContext jspair(int i) {
			return getRuleContext(JspairContext.class,i);
		}
		public JsonObjectContext(JsobjectContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterJsonObject(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitJsonObject(this);
		}
	}
	public static class EmptyJsonObjectContext extends JsobjectContext {
		public TerminalNode RBRACE() { return getToken(KVQLParser.RBRACE, 0); }
		public TerminalNode LBRACE() { return getToken(KVQLParser.LBRACE, 0); }
		public EmptyJsonObjectContext(JsobjectContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterEmptyJsonObject(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitEmptyJsonObject(this);
		}
	}

	public final JsobjectContext jsobject() throws RecognitionException {
		JsobjectContext _localctx = new JsobjectContext(_ctx, getState());
		enterRule(_localctx, 216, RULE_jsobject);
		int _la;
		try {
			setState(1092);
			switch ( getInterpreter().adaptivePredict(_input,115,_ctx) ) {
			case 1:
				_localctx = new JsonObjectContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1079); match(LBRACE);
				setState(1080); jspair();
				setState(1085);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(1081); match(COMMA);
					setState(1082); jspair();
					}
					}
					setState(1087);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1088); match(RBRACE);
				}
				break;
			case 2:
				_localctx = new EmptyJsonObjectContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1090); match(LBRACE);
				setState(1091); match(RBRACE);
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
		public TerminalNode RBRACK() { return getToken(KVQLParser.RBRACK, 0); }
		public TerminalNode LBRACK() { return getToken(KVQLParser.LBRACK, 0); }
		public EmptyJsonArrayContext(JsarrayContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterEmptyJsonArray(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitEmptyJsonArray(this);
		}
	}
	public static class ArrayOfJsonValuesContext extends JsarrayContext {
		public JsvalueContext jsvalue(int i) {
			return getRuleContext(JsvalueContext.class,i);
		}
		public TerminalNode RBRACK() { return getToken(KVQLParser.RBRACK, 0); }
		public TerminalNode LBRACK() { return getToken(KVQLParser.LBRACK, 0); }
		public List<JsvalueContext> jsvalue() {
			return getRuleContexts(JsvalueContext.class);
		}
		public ArrayOfJsonValuesContext(JsarrayContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterArrayOfJsonValues(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitArrayOfJsonValues(this);
		}
	}

	public final JsarrayContext jsarray() throws RecognitionException {
		JsarrayContext _localctx = new JsarrayContext(_ctx, getState());
		enterRule(_localctx, 218, RULE_jsarray);
		int _la;
		try {
			setState(1107);
			switch ( getInterpreter().adaptivePredict(_input,117,_ctx) ) {
			case 1:
				_localctx = new ArrayOfJsonValuesContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1094); match(LBRACK);
				setState(1095); jsvalue();
				setState(1100);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(1096); match(COMMA);
					setState(1097); jsvalue();
					}
					}
					setState(1102);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1103); match(RBRACK);
				}
				break;
			case 2:
				_localctx = new EmptyJsonArrayContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1105); match(LBRACK);
				setState(1106); match(RBRACK);
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
		public TerminalNode DSTRING() { return getToken(KVQLParser.DSTRING, 0); }
		public JsvalueContext jsvalue() {
			return getRuleContext(JsvalueContext.class,0);
		}
		public JsonPairContext(JspairContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterJsonPair(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitJsonPair(this);
		}
	}

	public final JspairContext jspair() throws RecognitionException {
		JspairContext _localctx = new JspairContext(_ctx, getState());
		enterRule(_localctx, 220, RULE_jspair);
		try {
			_localctx = new JsonPairContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(1109); match(DSTRING);
			setState(1110); match(COLON);
			setState(1111); jsvalue();
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
		public NumberContext number() {
			return getRuleContext(NumberContext.class,0);
		}
		public TerminalNode NULL() { return getToken(KVQLParser.NULL, 0); }
		public TerminalNode FALSE() { return getToken(KVQLParser.FALSE, 0); }
		public TerminalNode TRUE() { return getToken(KVQLParser.TRUE, 0); }
		public TerminalNode DSTRING() { return getToken(KVQLParser.DSTRING, 0); }
		public JsonAtomContext(JsvalueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterJsonAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitJsonAtom(this);
		}
	}
	public static class JsonArrayValueContext extends JsvalueContext {
		public JsarrayContext jsarray() {
			return getRuleContext(JsarrayContext.class,0);
		}
		public JsonArrayValueContext(JsvalueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterJsonArrayValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitJsonArrayValue(this);
		}
	}
	public static class JsonObjectValueContext extends JsvalueContext {
		public JsobjectContext jsobject() {
			return getRuleContext(JsobjectContext.class,0);
		}
		public JsonObjectValueContext(JsvalueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterJsonObjectValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitJsonObjectValue(this);
		}
	}

	public final JsvalueContext jsvalue() throws RecognitionException {
		JsvalueContext _localctx = new JsvalueContext(_ctx, getState());
		enterRule(_localctx, 222, RULE_jsvalue);
		try {
			setState(1120);
			switch (_input.LA(1)) {
			case LBRACE:
				_localctx = new JsonObjectValueContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1113); jsobject();
				}
				break;
			case LBRACK:
				_localctx = new JsonArrayValueContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1114); jsarray();
				}
				break;
			case DSTRING:
				_localctx = new JsonAtomContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(1115); match(DSTRING);
				}
				break;
			case MINUS:
			case INT:
			case FLOAT:
				_localctx = new JsonAtomContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(1116); number();
				}
				break;
			case TRUE:
				_localctx = new JsonAtomContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(1117); match(TRUE);
				}
				break;
			case FALSE:
				_localctx = new JsonAtomContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(1118); match(FALSE);
				}
				break;
			case NULL:
				_localctx = new JsonAtomContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(1119); match(NULL);
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

	public static class CommentContext extends ParserRuleContext {
		public TerminalNode COMMENT() { return getToken(KVQLParser.COMMENT, 0); }
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public CommentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterComment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitComment(this);
		}
	}

	public final CommentContext comment() throws RecognitionException {
		CommentContext _localctx = new CommentContext(_ctx, getState());
		enterRule(_localctx, 224, RULE_comment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1122); match(COMMENT);
			setState(1123); string();
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
		public TerminalNode INT() { return getToken(KVQLParser.INT, 0); }
		public TerminalNode TIME_UNIT() { return getToken(KVQLParser.TIME_UNIT, 0); }
		public DurationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_duration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterDuration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitDuration(this);
		}
	}

	public final DurationContext duration() throws RecognitionException {
		DurationContext _localctx = new DurationContext(_ctx, getState());
		enterRule(_localctx, 226, RULE_duration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1125); match(INT);
			setState(1126); match(TIME_UNIT);
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

	public static class NumberContext extends ParserRuleContext {
		public TerminalNode INT() { return getToken(KVQLParser.INT, 0); }
		public TerminalNode FLOAT() { return getToken(KVQLParser.FLOAT, 0); }
		public NumberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_number; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterNumber(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitNumber(this);
		}
	}

	public final NumberContext number() throws RecognitionException {
		NumberContext _localctx = new NumberContext(_ctx, getState());
		enterRule(_localctx, 228, RULE_number);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1129);
			_la = _input.LA(1);
			if (_la==MINUS) {
				{
				setState(1128); match(MINUS);
				}
			}

			setState(1131);
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

	public static class StringContext extends ParserRuleContext {
		public TerminalNode DSTRING() { return getToken(KVQLParser.DSTRING, 0); }
		public TerminalNode STRING() { return getToken(KVQLParser.STRING, 0); }
		public StringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_string; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterString(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitString(this);
		}
	}

	public final StringContext string() throws RecognitionException {
		StringContext _localctx = new StringContext(_ctx, getState());
		enterRule(_localctx, 230, RULE_string);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1133);
			_la = _input.LA(1);
			if ( !(_la==DSTRING || _la==STRING) ) {
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

	public static class Id_listContext extends ParserRuleContext {
		public IdContext id(int i) {
			return getRuleContext(IdContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(KVQLParser.COMMA); }
		public List<IdContext> id() {
			return getRuleContexts(IdContext.class);
		}
		public TerminalNode COMMA(int i) {
			return getToken(KVQLParser.COMMA, i);
		}
		public Id_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_id_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterId_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitId_list(this);
		}
	}

	public final Id_listContext id_list() throws RecognitionException {
		Id_listContext _localctx = new Id_listContext(_ctx, getState());
		enterRule(_localctx, 232, RULE_id_list);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1135); id();
			setState(1140);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,120,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1136); match(COMMA);
					setState(1137); id();
					}
					} 
				}
				setState(1142);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,120,_ctx);
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

	public static class IdContext extends ParserRuleContext {
		public TerminalNode COMMENT() { return getToken(KVQLParser.COMMENT, 0); }
		public TerminalNode ASC() { return getToken(KVQLParser.ASC, 0); }
		public TerminalNode ES_REPLICAS() { return getToken(KVQLParser.ES_REPLICAS, 0); }
		public TerminalNode IF() { return getToken(KVQLParser.IF, 0); }
		public TerminalNode BAD_ID() { return getToken(KVQLParser.BAD_ID, 0); }
		public TerminalNode DESCRIBE() { return getToken(KVQLParser.DESCRIBE, 0); }
		public TerminalNode TABLES() { return getToken(KVQLParser.TABLES, 0); }
		public TerminalNode FIRST() { return getToken(KVQLParser.FIRST, 0); }
		public TerminalNode REVOKE() { return getToken(KVQLParser.REVOKE, 0); }
		public TerminalNode STRING_T() { return getToken(KVQLParser.STRING_T, 0); }
		public TerminalNode INDEXES() { return getToken(KVQLParser.INDEXES, 0); }
		public TerminalNode INDEX() { return getToken(KVQLParser.INDEX, 0); }
		public TerminalNode ID() { return getToken(KVQLParser.ID, 0); }
		public TerminalNode ACCOUNT() { return getToken(KVQLParser.ACCOUNT, 0); }
		public TerminalNode ADMIN() { return getToken(KVQLParser.ADMIN, 0); }
		public TerminalNode USERS() { return getToken(KVQLParser.USERS, 0); }
		public TerminalNode LOCK() { return getToken(KVQLParser.LOCK, 0); }
		public TerminalNode LIFETIME() { return getToken(KVQLParser.LIFETIME, 0); }
		public TerminalNode FLOAT_T() { return getToken(KVQLParser.FLOAT_T, 0); }
		public TerminalNode ENUM_T() { return getToken(KVQLParser.ENUM_T, 0); }
		public TerminalNode DOUBLE_T() { return getToken(KVQLParser.DOUBLE_T, 0); }
		public TerminalNode JSON() { return getToken(KVQLParser.JSON, 0); }
		public TerminalNode MODIFY() { return getToken(KVQLParser.MODIFY, 0); }
		public TerminalNode MAP_T() { return getToken(KVQLParser.MAP_T, 0); }
		public TerminalNode OR() { return getToken(KVQLParser.OR, 0); }
		public TerminalNode USING() { return getToken(KVQLParser.USING, 0); }
		public TerminalNode TABLE() { return getToken(KVQLParser.TABLE, 0); }
		public TerminalNode KEY() { return getToken(KVQLParser.KEY, 0); }
		public TerminalNode ROLES() { return getToken(KVQLParser.ROLES, 0); }
		public TerminalNode ORDER() { return getToken(KVQLParser.ORDER, 0); }
		public TerminalNode INTEGER_T() { return getToken(KVQLParser.INTEGER_T, 0); }
		public TerminalNode ALTER() { return getToken(KVQLParser.ALTER, 0); }
		public TerminalNode ROLE() { return getToken(KVQLParser.ROLE, 0); }
		public TerminalNode BOOLEAN_T() { return getToken(KVQLParser.BOOLEAN_T, 0); }
		public TerminalNode PRIMARY() { return getToken(KVQLParser.PRIMARY, 0); }
		public TerminalNode ELEMENTOF() { return getToken(KVQLParser.ELEMENTOF, 0); }
		public TerminalNode BINARY_T() { return getToken(KVQLParser.BINARY_T, 0); }
		public TerminalNode LONG_T() { return getToken(KVQLParser.LONG_T, 0); }
		public TerminalNode TO() { return getToken(KVQLParser.TO, 0); }
		public TerminalNode FULLTEXT() { return getToken(KVQLParser.FULLTEXT, 0); }
		public TerminalNode TIME_UNIT() { return getToken(KVQLParser.TIME_UNIT, 0); }
		public TerminalNode AS() { return getToken(KVQLParser.AS, 0); }
		public TerminalNode PASSWORD() { return getToken(KVQLParser.PASSWORD, 0); }
		public TerminalNode SHARD() { return getToken(KVQLParser.SHARD, 0); }
		public TerminalNode ALL() { return getToken(KVQLParser.ALL, 0); }
		public TerminalNode NOT() { return getToken(KVQLParser.NOT, 0); }
		public TerminalNode ADD() { return getToken(KVQLParser.ADD, 0); }
		public TerminalNode AND() { return getToken(KVQLParser.AND, 0); }
		public TerminalNode CREATE() { return getToken(KVQLParser.CREATE, 0); }
		public TerminalNode DEFAULT() { return getToken(KVQLParser.DEFAULT, 0); }
		public TerminalNode BY() { return getToken(KVQLParser.BY, 0); }
		public TerminalNode ES_SHARDS() { return getToken(KVQLParser.ES_SHARDS, 0); }
		public TerminalNode SHOW() { return getToken(KVQLParser.SHOW, 0); }
		public TerminalNode ON() { return getToken(KVQLParser.ON, 0); }
		public TerminalNode IDENTIFIED() { return getToken(KVQLParser.IDENTIFIED, 0); }
		public TerminalNode TTL() { return getToken(KVQLParser.TTL, 0); }
		public TerminalNode DECLARE() { return getToken(KVQLParser.DECLARE, 0); }
		public TerminalNode KEYOF() { return getToken(KVQLParser.KEYOF, 0); }
		public TerminalNode DROP() { return getToken(KVQLParser.DROP, 0); }
		public TerminalNode FROM() { return getToken(KVQLParser.FROM, 0); }
		public TerminalNode DESC() { return getToken(KVQLParser.DESC, 0); }
		public TerminalNode ARRAY_T() { return getToken(KVQLParser.ARRAY_T, 0); }
		public TerminalNode USER() { return getToken(KVQLParser.USER, 0); }
		public TerminalNode RECORD_T() { return getToken(KVQLParser.RECORD_T, 0); }
		public TerminalNode LAST() { return getToken(KVQLParser.LAST, 0); }
		public TerminalNode GRANT() { return getToken(KVQLParser.GRANT, 0); }
		public TerminalNode EXISTS() { return getToken(KVQLParser.EXISTS, 0); }
		public TerminalNode WHERE() { return getToken(KVQLParser.WHERE, 0); }
		public TerminalNode SELECT() { return getToken(KVQLParser.SELECT, 0); }
		public TerminalNode NULLS() { return getToken(KVQLParser.NULLS, 0); }
		public TerminalNode UNLOCK() { return getToken(KVQLParser.UNLOCK, 0); }
		public IdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_id; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).enterId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof KVQLListener ) ((KVQLListener)listener).exitId(this);
		}
	}

	public final IdContext id() throws RecognitionException {
		IdContext _localctx = new IdContext(_ctx, getState());
		enterRule(_localctx, 234, RULE_id);
		int _la;
		try {
			setState(1146);
			switch (_input.LA(1)) {
			case ACCOUNT:
			case ADD:
			case ADMIN:
			case ALL:
			case ALTER:
			case AND:
			case AS:
			case ASC:
			case BY:
			case COMMENT:
			case CREATE:
			case DECLARE:
			case DEFAULT:
			case DESC:
			case DESCRIBE:
			case DROP:
			case ELEMENTOF:
			case ES_SHARDS:
			case ES_REPLICAS:
			case EXISTS:
			case FIRST:
			case FROM:
			case FULLTEXT:
			case GRANT:
			case IDENTIFIED:
			case IF:
			case INDEX:
			case INDEXES:
			case JSON:
			case KEY:
			case KEYOF:
			case LAST:
			case LIFETIME:
			case LOCK:
			case MODIFY:
			case NOT:
			case NULLS:
			case ON:
			case OR:
			case ORDER:
			case PASSWORD:
			case PRIMARY:
			case REVOKE:
			case ROLE:
			case ROLES:
			case SELECT:
			case SHARD:
			case SHOW:
			case TABLE:
			case TABLES:
			case TIME_UNIT:
			case TO:
			case TTL:
			case UNLOCK:
			case USER:
			case USERS:
			case USING:
			case WHERE:
			case ARRAY_T:
			case BINARY_T:
			case BOOLEAN_T:
			case DOUBLE_T:
			case ENUM_T:
			case FLOAT_T:
			case INTEGER_T:
			case LONG_T:
			case MAP_T:
			case RECORD_T:
			case STRING_T:
			case ID:
				enterOuterAlt(_localctx, 1);
				{
				setState(1143);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACCOUNT) | (1L << ADD) | (1L << ADMIN) | (1L << ALL) | (1L << ALTER) | (1L << AND) | (1L << AS) | (1L << ASC) | (1L << BY) | (1L << COMMENT) | (1L << CREATE) | (1L << DECLARE) | (1L << DEFAULT) | (1L << DESC) | (1L << DESCRIBE) | (1L << DROP) | (1L << ELEMENTOF) | (1L << ES_SHARDS) | (1L << ES_REPLICAS) | (1L << EXISTS) | (1L << FIRST) | (1L << FROM) | (1L << FULLTEXT) | (1L << GRANT) | (1L << IDENTIFIED) | (1L << IF) | (1L << INDEX) | (1L << INDEXES) | (1L << JSON) | (1L << KEY) | (1L << KEYOF) | (1L << LAST) | (1L << LIFETIME) | (1L << LOCK) | (1L << MODIFY) | (1L << NOT) | (1L << NULLS) | (1L << ON) | (1L << OR) | (1L << ORDER) | (1L << PASSWORD) | (1L << PRIMARY) | (1L << REVOKE) | (1L << ROLE) | (1L << ROLES) | (1L << SELECT) | (1L << SHARD) | (1L << SHOW) | (1L << TABLE) | (1L << TABLES) | (1L << TIME_UNIT) | (1L << TO) | (1L << TTL) | (1L << UNLOCK) | (1L << USER) | (1L << USERS) | (1L << USING))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (WHERE - 64)) | (1L << (ARRAY_T - 64)) | (1L << (BINARY_T - 64)) | (1L << (BOOLEAN_T - 64)) | (1L << (DOUBLE_T - 64)) | (1L << (ENUM_T - 64)) | (1L << (FLOAT_T - 64)) | (1L << (INTEGER_T - 64)) | (1L << (LONG_T - 64)) | (1L << (MAP_T - 64)) | (1L << (RECORD_T - 64)) | (1L << (STRING_T - 64)) | (1L << (ID - 64)))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				consume();
				}
				break;
			case BAD_ID:
				enterOuterAlt(_localctx, 2);
				{
				setState(1144); match(BAD_ID);

				        notifyErrorListeners("Identifiers must start with a letter: " + _input.getText(_localctx.start, _input.LT(-1)));
				     
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 17: return or_expr_sempred((Or_exprContext)_localctx, predIndex);
		case 18: return and_expr_sempred((And_exprContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean and_expr_sempred(And_exprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1: return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean or_expr_sempred(Or_exprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0: return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3{\u047f\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I"+
		"\tI\4J\tJ\4K\tK\4L\tL\4M\tM\4N\tN\4O\tO\4P\tP\4Q\tQ\4R\tR\4S\tS\4T\tT"+
		"\4U\tU\4V\tV\4W\tW\4X\tX\4Y\tY\4Z\tZ\4[\t[\4\\\t\\\4]\t]\4^\t^\4_\t_\4"+
		"`\t`\4a\ta\4b\tb\4c\tc\4d\td\4e\te\4f\tf\4g\tg\4h\th\4i\ti\4j\tj\4k\t"+
		"k\4l\tl\4m\tm\4n\tn\4o\to\4p\tp\4q\tq\4r\tr\4s\ts\4t\tt\4u\tu\4v\tv\4"+
		"w\tw\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3"+
		"\3\3\3\3\3\5\3\u0102\n\3\3\4\5\4\u0105\n\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5"+
		"\3\5\7\5\u010f\n\5\f\5\16\5\u0112\13\5\3\6\3\6\3\6\3\7\3\7\3\7\3\b\3\b"+
		"\5\b\u011c\n\b\3\t\3\t\3\t\5\t\u0121\n\t\3\t\5\t\u0124\n\t\3\n\3\n\3\n"+
		"\5\n\u0129\n\n\3\n\5\n\u012c\n\n\3\13\5\13\u012f\n\13\3\13\3\13\3\f\3"+
		"\f\3\f\3\r\3\r\5\r\u0138\n\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\7\r\u0141\n\r"+
		"\f\r\16\r\u0144\13\r\5\r\u0146\n\r\3\16\3\16\7\16\u014a\n\16\f\16\16\16"+
		"\u014d\13\16\3\16\3\16\3\17\3\17\3\17\3\17\7\17\u0155\n\17\f\17\16\17"+
		"\u0158\13\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3"+
		"\17\3\17\3\17\3\17\3\17\3\17\3\17\5\17\u016c\n\17\3\17\5\17\u016f\n\17"+
		"\3\20\3\20\5\20\u0173\n\20\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\7\21"+
		"\u017d\n\21\f\21\16\21\u0180\13\21\3\22\5\22\u0183\n\22\3\22\3\22\5\22"+
		"\u0187\n\22\3\23\3\23\3\23\3\23\3\23\3\23\7\23\u018f\n\23\f\23\16\23\u0192"+
		"\13\23\3\24\3\24\3\24\3\24\3\24\3\24\7\24\u019a\n\24\f\24\16\24\u019d"+
		"\13\24\3\25\3\25\3\25\5\25\u01a2\n\25\3\25\3\25\5\25\u01a6\n\25\3\26\3"+
		"\26\3\27\3\27\3\27\3\27\3\27\3\27\5\27\u01b0\n\27\3\30\3\30\3\30\7\30"+
		"\u01b5\n\30\f\30\16\30\u01b8\13\30\3\31\3\31\3\31\7\31\u01bd\n\31\f\31"+
		"\16\31\u01c0\13\31\3\32\3\32\3\32\5\32\u01c5\n\32\3\33\3\33\3\33\3\33"+
		"\7\33\u01cb\n\33\f\33\16\33\u01ce\13\33\3\34\3\34\3\34\3\34\3\34\3\34"+
		"\5\34\u01d6\n\34\3\35\3\35\5\35\u01da\n\35\3\35\3\35\5\35\u01de\n\35\3"+
		"\35\3\35\3\36\3\36\5\36\u01e4\n\36\3\36\3\36\3\37\3\37\3\37\3\37\3\37"+
		"\3\37\5\37\u01ee\n\37\3 \3 \3 \5 \u01f3\n \3!\3!\3!\3!\3!\5!\u01fa\n!"+
		"\3\"\3\"\5\"\u01fe\n\"\3#\3#\3#\3#\7#\u0204\n#\f#\16#\u0207\13#\3#\3#"+
		"\3$\3$\3$\3$\3$\7$\u0210\n$\f$\16$\u0213\13$\5$\u0215\n$\3$\3$\3%\3%\3"+
		"%\3%\3&\3&\3&\3&\3&\3&\3&\3&\3&\5&\u0226\n&\3\'\3\'\3\'\3\'\3\'\7\'\u022d"+
		"\n\'\f\'\16\'\u0230\13\'\3\'\3\'\3(\3(\3(\5(\u0237\n(\3(\5(\u023a\n(\3"+
		")\3)\5)\u023e\n)\3)\3)\5)\u0242\n)\5)\u0244\n)\3*\3*\3*\3*\3*\3*\5*\u024c"+
		"\n*\3+\3+\3+\3,\3,\3,\3,\3,\3-\3-\3-\3-\3-\3.\3.\3/\3/\3\60\3\60\3\61"+
		"\3\61\3\61\3\61\3\61\3\61\3\61\3\61\3\61\3\61\5\61\u026b\n\61\3\62\3\62"+
		"\3\63\3\63\3\63\3\63\5\63\u0273\n\63\3\64\3\64\3\64\7\64\u0278\n\64\f"+
		"\64\16\64\u027b\13\64\3\65\3\65\3\65\3\65\3\65\5\65\u0282\n\65\3\65\3"+
		"\65\5\65\u0286\n\65\3\65\3\65\3\65\3\65\5\65\u028c\n\65\3\66\3\66\3\67"+
		"\3\67\5\67\u0292\n\67\3\67\3\67\3\67\5\67\u0297\n\67\7\67\u0299\n\67\f"+
		"\67\16\67\u029c\13\67\38\38\38\38\38\58\u02a3\n8\58\u02a5\n8\38\58\u02a8"+
		"\n8\38\38\39\39\39\39\39\39\39\39\39\59\u02b5\n9\3:\3:\3:\7:\u02ba\n:"+
		"\f:\16:\u02bd\13:\3;\3;\5;\u02c1\n;\3<\3<\3<\3<\3=\3=\3=\3=\3>\3>\3>\3"+
		">\3>\3?\3?\5?\u02d2\n?\3@\3@\3@\3@\5@\u02d8\n@\3@\3@\3@\3@\5@\u02de\n"+
		"@\7@\u02e0\n@\f@\16@\u02e3\13@\3@\3@\3A\3A\3A\3A\5A\u02eb\nA\3A\5A\u02ee"+
		"\nA\3B\3B\3B\3C\3C\3C\3C\5C\u02f7\nC\3C\5C\u02fa\nC\3D\3D\3D\3D\5D\u0300"+
		"\nD\3D\3D\3E\3E\3E\3E\3E\5E\u0309\nE\3E\3E\3E\3E\3E\3E\3E\3E\3E\3E\3E"+
		"\5E\u0316\nE\3E\5E\u0319\nE\3F\3F\3G\3G\3G\7G\u0320\nG\fG\16G\u0323\13"+
		"G\3H\3H\3H\5H\u0328\nH\3I\3I\3I\3I\3I\3J\3J\3J\3J\3J\3J\5J\u0335\nJ\3"+
		"K\3K\3K\3K\3K\3K\5K\u033d\nK\3K\3K\3K\3K\3K\5K\u0344\nK\3K\5K\u0347\n"+
		"K\3L\3L\3L\3L\3L\3L\3L\3L\5L\u0351\nL\3M\3M\3M\7M\u0356\nM\fM\16M\u0359"+
		"\13M\3N\3N\5N\u035d\nN\3O\3O\7O\u0361\nO\fO\16O\u0364\13O\3P\3P\3P\3P"+
		"\3P\3P\5P\u036c\nP\3Q\3Q\3Q\3Q\5Q\u0372\nQ\3Q\3Q\3Q\3Q\3R\3R\3R\5R\u037b"+
		"\nR\3R\3R\3R\3R\3R\3R\3R\3R\3R\3R\5R\u0387\nR\3R\3R\3R\3R\3R\5R\u038e"+
		"\nR\3S\3S\3S\5S\u0393\nS\3S\3S\3S\3S\3S\3S\3S\3S\3S\3S\3S\3S\5S\u03a1"+
		"\nS\3T\3T\3T\3T\5T\u03a7\nT\3T\5T\u03aa\nT\3U\3U\3U\3U\3V\3V\3V\3V\5V"+
		"\u03b4\nV\3V\5V\u03b7\nV\3V\5V\u03ba\nV\3V\5V\u03bd\nV\3V\5V\u03c0\nV"+
		"\3W\3W\3W\3W\3X\3X\3X\3X\3Y\3Y\3Y\3Y\5Y\u03ce\nY\3Z\3Z\3Z\3Z\5Z\u03d4"+
		"\nZ\3[\3[\5[\u03d8\n[\3\\\3\\\3\\\3]\3]\3]\5]\u03e0\n]\3]\5]\u03e3\n]"+
		"\3]\3]\3]\5]\u03e8\n]\3^\3^\3^\3_\3_\3_\3_\3`\3`\5`\u03f3\n`\3a\3a\3a"+
		"\3b\3b\3b\3b\3c\3c\3c\3c\3d\3d\3d\3d\3d\3d\3e\3e\3e\3e\3f\3f\3f\3f\3g"+
		"\3g\3g\3g\3g\3g\3h\3h\3h\3h\5h\u0418\nh\3i\3i\3i\7i\u041d\ni\fi\16i\u0420"+
		"\13i\3j\3j\5j\u0424\nj\3k\3k\5k\u0428\nk\3k\3k\3k\5k\u042d\nk\7k\u042f"+
		"\nk\fk\16k\u0432\13k\3l\3l\3m\3m\5m\u0438\nm\3n\3n\3n\3n\7n\u043e\nn\f"+
		"n\16n\u0441\13n\3n\3n\3n\3n\5n\u0447\nn\3o\3o\3o\3o\7o\u044d\no\fo\16"+
		"o\u0450\13o\3o\3o\3o\3o\5o\u0456\no\3p\3p\3p\3p\3q\3q\3q\3q\3q\3q\3q\5"+
		"q\u0463\nq\3r\3r\3r\3s\3s\3s\3t\5t\u046c\nt\3t\3t\3u\3u\3v\3v\3v\7v\u0475"+
		"\nv\fv\16v\u0478\13v\3w\3w\3w\5w\u047d\nw\3w\2\4$&x\2\4\6\b\n\f\16\20"+
		"\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@BDFHJLNPRTVXZ\\^`bdfhj"+
		"lnprtvxz|~\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e\u0090\u0092"+
		"\u0094\u0096\u0098\u009a\u009c\u009e\u00a0\u00a2\u00a4\u00a6\u00a8\u00aa"+
		"\u00ac\u00ae\u00b0\u00b2\u00b4\u00b6\u00b8\u00ba\u00bc\u00be\u00c0\u00c2"+
		"\u00c4\u00c6\u00c8\u00ca\u00cc\u00ce\u00d0\u00d2\u00d4\u00d6\u00d8\u00da"+
		"\u00dc\u00de\u00e0\u00e2\u00e4\u00e6\u00e8\u00ea\u00ec\2\16\4\2\f\f\22"+
		"\22\4\2\31\31&&\3\2_d\3\2kl\4\2\\\\mm\3\2NO\4\2KKMM\3\2\22\23\4\2((>>"+
		"\3\2qr\3\2st\7\2\5\31\34/\62BHRuu\u04bb\2\u00ee\3\2\2\2\4\u0101\3\2\2"+
		"\2\6\u0104\3\2\2\2\b\u0108\3\2\2\2\n\u0113\3\2\2\2\f\u0116\3\2\2\2\16"+
		"\u011b\3\2\2\2\20\u011d\3\2\2\2\22\u0125\3\2\2\2\24\u012e\3\2\2\2\26\u0132"+
		"\3\2\2\2\30\u0135\3\2\2\2\32\u0147\3\2\2\2\34\u016b\3\2\2\2\36\u0172\3"+
		"\2\2\2 \u0174\3\2\2\2\"\u0182\3\2\2\2$\u0188\3\2\2\2&\u0193\3\2\2\2(\u019e"+
		"\3\2\2\2*\u01a7\3\2\2\2,\u01af\3\2\2\2.\u01b1\3\2\2\2\60\u01b9\3\2\2\2"+
		"\62\u01c4\3\2\2\2\64\u01c6\3\2\2\2\66\u01cf\3\2\2\28\u01d7\3\2\2\2:\u01e1"+
		"\3\2\2\2<\u01ed\3\2\2\2>\u01ef\3\2\2\2@\u01f9\3\2\2\2B\u01fb\3\2\2\2D"+
		"\u01ff\3\2\2\2F\u020a\3\2\2\2H\u0218\3\2\2\2J\u0225\3\2\2\2L\u0227\3\2"+
		"\2\2N\u0233\3\2\2\2P\u0243\3\2\2\2R\u0245\3\2\2\2T\u024d\3\2\2\2V\u0250"+
		"\3\2\2\2X\u0255\3\2\2\2Z\u025a\3\2\2\2\\\u025c\3\2\2\2^\u025e\3\2\2\2"+
		"`\u026a\3\2\2\2b\u026c\3\2\2\2d\u026e\3\2\2\2f\u0274\3\2\2\2h\u027c\3"+
		"\2\2\2j\u028d\3\2\2\2l\u0291\3\2\2\2n\u029d\3\2\2\2p\u02b4\3\2\2\2r\u02b6"+
		"\3\2\2\2t\u02be\3\2\2\2v\u02c2\3\2\2\2x\u02c6\3\2\2\2z\u02ca\3\2\2\2|"+
		"\u02d1\3\2\2\2~\u02d3\3\2\2\2\u0080\u02e6\3\2\2\2\u0082\u02ef\3\2\2\2"+
		"\u0084\u02f2\3\2\2\2\u0086\u02fb\3\2\2\2\u0088\u0303\3\2\2\2\u008a\u031a"+
		"\3\2\2\2\u008c\u031c\3\2\2\2\u008e\u0327\3\2\2\2\u0090\u0329\3\2\2\2\u0092"+
		"\u032e\3\2\2\2\u0094\u0336\3\2\2\2\u0096\u0350\3\2\2\2\u0098\u0352\3\2"+
		"\2\2\u009a\u035a\3\2\2\2\u009c\u035e\3\2\2\2\u009e\u036b\3\2\2\2\u00a0"+
		"\u036d\3\2\2\2\u00a2\u0377\3\2\2\2\u00a4\u038f\3\2\2\2\u00a6\u03a2\3\2"+
		"\2\2\u00a8\u03ab\3\2\2\2\u00aa\u03af\3\2\2\2\u00ac\u03c1\3\2\2\2\u00ae"+
		"\u03c5\3\2\2\2\u00b0\u03c9\3\2\2\2\u00b2\u03cf\3\2\2\2\u00b4\u03d7\3\2"+
		"\2\2\u00b6\u03d9\3\2\2\2\u00b8\u03e7\3\2\2\2\u00ba\u03e9\3\2\2\2\u00bc"+
		"\u03ec\3\2\2\2\u00be\u03f0\3\2\2\2\u00c0\u03f4\3\2\2\2\u00c2\u03f7\3\2"+
		"\2\2\u00c4\u03fb\3\2\2\2\u00c6\u03ff\3\2\2\2\u00c8\u0405\3\2\2\2\u00ca"+
		"\u0409\3\2\2\2\u00cc\u040d\3\2\2\2\u00ce\u0417\3\2\2\2\u00d0\u0419\3\2"+
		"\2\2\u00d2\u0423\3\2\2\2\u00d4\u0427\3\2\2\2\u00d6\u0433\3\2\2\2\u00d8"+
		"\u0437\3\2\2\2\u00da\u0446\3\2\2\2\u00dc\u0455\3\2\2\2\u00de\u0457\3\2"+
		"\2\2\u00e0\u0462\3\2\2\2\u00e2\u0464\3\2\2\2\u00e4\u0467\3\2\2\2\u00e6"+
		"\u046b\3\2\2\2\u00e8\u046f\3\2\2\2\u00ea\u0471\3\2\2\2\u00ec\u047c\3\2"+
		"\2\2\u00ee\u00ef\5\4\3\2\u00ef\u00f0\7\2\2\3\u00f0\3\3\2\2\2\u00f1\u0102"+
		"\5\6\4\2\u00f2\u0102\5h\65\2\u00f3\u0102\5\u0088E\2\u00f4\u0102\5\u00a6"+
		"T\2\u00f5\u0102\5\u00a8U\2\u00f6\u0102\5\u00a0Q\2\u00f7\u0102\5\u0094"+
		"K\2\u00f8\u0102\5\u00aeX\2\u00f9\u0102\5\u00acW\2\u00fa\u0102\5z>\2\u00fb"+
		"\u0102\5\u00aaV\2\u00fc\u0102\5\u0086D\2\u00fd\u0102\5\u00b0Y\2\u00fe"+
		"\u0102\5\u00b2Z\2\u00ff\u0102\5\u00a2R\2\u0100\u0102\5\u00a4S\2\u0101"+
		"\u00f1\3\2\2\2\u0101\u00f2\3\2\2\2\u0101\u00f3\3\2\2\2\u0101\u00f4\3\2"+
		"\2\2\u0101\u00f5\3\2\2\2\u0101\u00f6\3\2\2\2\u0101\u00f7\3\2\2\2\u0101"+
		"\u00f8\3\2\2\2\u0101\u00f9\3\2\2\2\u0101\u00fa\3\2\2\2\u0101\u00fb\3\2"+
		"\2\2\u0101\u00fc\3\2\2\2\u0101\u00fd\3\2\2\2\u0101\u00fe\3\2\2\2\u0101"+
		"\u00ff\3\2\2\2\u0101\u0100\3\2\2\2\u0102\5\3\2\2\2\u0103\u0105\5\b\5\2"+
		"\u0104\u0103\3\2\2\2\u0104\u0105\3\2\2\2\u0105\u0106\3\2\2\2\u0106\u0107"+
		"\5\20\t\2\u0107\7\3\2\2\2\u0108\u0109\7\20\2\2\u0109\u010a\5\n\6\2\u010a"+
		"\u0110\7S\2\2\u010b\u010c\5\n\6\2\u010c\u010d\7S\2\2\u010d\u010f\3\2\2"+
		"\2\u010e\u010b\3\2\2\2\u010f\u0112\3\2\2\2\u0110\u010e\3\2\2\2\u0110\u0111"+
		"\3\2\2\2\u0111\t\3\2\2\2\u0112\u0110\3\2\2\2\u0113\u0114\5\f\7\2\u0114"+
		"\u0115\5J&\2\u0115\13\3\2\2\2\u0116\u0117\7^\2\2\u0117\u0118\5\u00ecw"+
		"\2\u0118\r\3\2\2\2\u0119\u011c\5\20\t\2\u011a\u011c\5$\23\2\u011b\u0119"+
		"\3\2\2\2\u011b\u011a\3\2\2\2\u011c\17\3\2\2\2\u011d\u011e\5\30\r\2\u011e"+
		"\u0120\5\22\n\2\u011f\u0121\5\26\f\2\u0120\u011f\3\2\2\2\u0120\u0121\3"+
		"\2\2\2\u0121\u0123\3\2\2\2\u0122\u0124\5 \21\2\u0123\u0122\3\2\2\2\u0123"+
		"\u0124\3\2\2\2\u0124\21\3\2\2\2\u0125\u0126\7\34\2\2\u0126\u012b\5f\64"+
		"\2\u0127\u0129\7\13\2\2\u0128\u0127\3\2\2\2\u0128\u0129\3\2\2\2\u0129"+
		"\u012a\3\2\2\2\u012a\u012c\5\24\13\2\u012b\u0128\3\2\2\2\u012b\u012c\3"+
		"\2\2\2\u012c\23\3\2\2\2\u012d\u012f\7^\2\2\u012e\u012d\3\2\2\2\u012e\u012f"+
		"\3\2\2\2\u012f\u0130\3\2\2\2\u0130\u0131\5\u00ecw\2\u0131\25\3\2\2\2\u0132"+
		"\u0133\7B\2\2\u0133\u0134\5$\23\2\u0134\27\3\2\2\2\u0135\u0137\7\66\2"+
		"\2\u0136\u0138\5\32\16\2\u0137\u0136\3\2\2\2\u0137\u0138\3\2\2\2\u0138"+
		"\u0145\3\2\2\2\u0139\u0146\7\\\2\2\u013a\u013b\5$\23\2\u013b\u0142\5\36"+
		"\20\2\u013c\u013d\7T\2\2\u013d\u013e\5$\23\2\u013e\u013f\5\36\20\2\u013f"+
		"\u0141\3\2\2\2\u0140\u013c\3\2\2\2\u0141\u0144\3\2\2\2\u0142\u0140\3\2"+
		"\2\2\u0142\u0143\3\2\2\2\u0143\u0146\3\2\2\2\u0144\u0142\3\2\2\2\u0145"+
		"\u0139\3\2\2\2\u0145\u013a\3\2\2\2\u0146\31\3\2\2\2\u0147\u014b\7\4\2"+
		"\2\u0148\u014a\5\34\17\2\u0149\u0148\3\2\2\2\u014a\u014d\3\2\2\2\u014b"+
		"\u0149\3\2\2\2\u014b\u014c\3\2\2\2\u014c\u014e\3\2\2\2\u014d\u014b\3\2"+
		"\2\2\u014e\u014f\7\3\2\2\u014f\33\3\2\2\2\u0150\u0151\7\60\2\2\u0151\u0152"+
		"\7V\2\2\u0152\u0156\5f\64\2\u0153\u0155\5\u008aF\2\u0154\u0153\3\2\2\2"+
		"\u0155\u0158\3\2\2\2\u0156\u0154\3\2\2\2\u0156\u0157\3\2\2\2\u0157\u0159"+
		"\3\2\2\2\u0158\u0156\3\2\2\2\u0159\u015a\7W\2\2\u015a\u016c\3\2\2\2\u015b"+
		"\u015c\7\32\2\2\u015c\u015d\7V\2\2\u015d\u015e\5f\64\2\u015e\u015f\5\u008a"+
		"F\2\u015f\u0160\7W\2\2\u0160\u016c\3\2\2\2\u0161\u0162\7\61\2\2\u0162"+
		"\u0163\7V\2\2\u0163\u0164\5f\64\2\u0164\u0165\7W\2\2\u0165\u016c\3\2\2"+
		"\2\u0166\u0167\7\33\2\2\u0167\u0168\7V\2\2\u0168\u0169\5f\64\2\u0169\u016a"+
		"\7W\2\2\u016a\u016c\3\2\2\2\u016b\u0150\3\2\2\2\u016b\u015b\3\2\2\2\u016b"+
		"\u0161\3\2\2\2\u016b\u0166\3\2\2\2\u016c\u016e\3\2\2\2\u016d\u016f\7t"+
		"\2\2\u016e\u016d\3\2\2\2\u016e\u016f\3\2\2\2\u016f\35\3\2\2\2\u0170\u0171"+
		"\7\13\2\2\u0171\u0173\5\u00ecw\2\u0172\u0170\3\2\2\2\u0172\u0173\3\2\2"+
		"\2\u0173\37\3\2\2\2\u0174\u0175\7.\2\2\u0175\u0176\7\r\2\2\u0176\u0177"+
		"\5$\23\2\u0177\u017e\5\"\22\2\u0178\u0179\7T\2\2\u0179\u017a\5$\23\2\u017a"+
		"\u017b\5\"\22\2\u017b\u017d\3\2\2\2\u017c\u0178\3\2\2\2\u017d\u0180\3"+
		"\2\2\2\u017e\u017c\3\2\2\2\u017e\u017f\3\2\2\2\u017f!\3\2\2\2\u0180\u017e"+
		"\3\2\2\2\u0181\u0183\t\2\2\2\u0182\u0181\3\2\2\2\u0182\u0183\3\2\2\2\u0183"+
		"\u0186\3\2\2\2\u0184\u0185\7+\2\2\u0185\u0187\t\3\2\2\u0186\u0184\3\2"+
		"\2\2\u0186\u0187\3\2\2\2\u0187#\3\2\2\2\u0188\u0189\b\23\1\2\u0189\u018a"+
		"\5&\24\2\u018a\u0190\3\2\2\2\u018b\u018c\f\3\2\2\u018c\u018d\7-\2\2\u018d"+
		"\u018f\5&\24\2\u018e\u018b\3\2\2\2\u018f\u0192\3\2\2\2\u0190\u018e\3\2"+
		"\2\2\u0190\u0191\3\2\2\2\u0191%\3\2\2\2\u0192\u0190\3\2\2\2\u0193\u0194"+
		"\b\24\1\2\u0194\u0195\5(\25\2\u0195\u019b\3\2\2\2\u0196\u0197\f\3\2\2"+
		"\u0197\u0198\7\n\2\2\u0198\u019a\5(\25\2\u0199\u0196\3\2\2\2\u019a\u019d"+
		"\3\2\2\2\u019b\u0199\3\2\2\2\u019b\u019c\3\2\2\2\u019c\'\3\2\2\2\u019d"+
		"\u019b\3\2\2\2\u019e\u01a5\5.\30\2\u019f\u01a2\5*\26\2\u01a0\u01a2\5,"+
		"\27\2\u01a1\u019f\3\2\2\2\u01a1\u01a0\3\2\2\2\u01a2\u01a3\3\2\2\2\u01a3"+
		"\u01a4\5.\30\2\u01a4\u01a6\3\2\2\2\u01a5\u01a1\3\2\2\2\u01a5\u01a6\3\2"+
		"\2\2\u01a6)\3\2\2\2\u01a7\u01a8\t\4\2\2\u01a8+\3\2\2\2\u01a9\u01b0\7i"+
		"\2\2\u01aa\u01b0\7j\2\2\u01ab\u01b0\7g\2\2\u01ac\u01b0\7h\2\2\u01ad\u01b0"+
		"\7e\2\2\u01ae\u01b0\7f\2\2\u01af\u01a9\3\2\2\2\u01af\u01aa\3\2\2\2\u01af"+
		"\u01ab\3\2\2\2\u01af\u01ac\3\2\2\2\u01af\u01ad\3\2\2\2\u01af\u01ae\3\2"+
		"\2\2\u01b0-\3\2\2\2\u01b1\u01b6\5\60\31\2\u01b2\u01b3\t\5\2\2\u01b3\u01b5"+
		"\5\60\31\2\u01b4\u01b2\3\2\2\2\u01b5\u01b8\3\2\2\2\u01b6\u01b4\3\2\2\2"+
		"\u01b6\u01b7\3\2\2\2\u01b7/\3\2\2\2\u01b8\u01b6\3\2\2\2\u01b9\u01be\5"+
		"\62\32\2\u01ba\u01bb\t\6\2\2\u01bb\u01bd\5\62\32\2\u01bc\u01ba\3\2\2\2"+
		"\u01bd\u01c0\3\2\2\2\u01be\u01bc\3\2\2\2\u01be\u01bf\3\2\2\2\u01bf\61"+
		"\3\2\2\2\u01c0\u01be\3\2\2\2\u01c1\u01c5\5\64\33\2\u01c2\u01c3\t\5\2\2"+
		"\u01c3\u01c5\5\62\32\2\u01c4\u01c1\3\2\2\2\u01c4\u01c2\3\2\2\2\u01c5\63"+
		"\3\2\2\2\u01c6\u01cc\5<\37\2\u01c7\u01cb\5\66\34\2\u01c8\u01cb\58\35\2"+
		"\u01c9\u01cb\5:\36\2\u01ca\u01c7\3\2\2\2\u01ca\u01c8\3\2\2\2\u01ca\u01c9"+
		"\3\2\2\2\u01cb\u01ce\3\2\2\2\u01cc\u01ca\3\2\2\2\u01cc\u01cd\3\2\2\2\u01cd"+
		"\65\3\2\2\2\u01ce\u01cc\3\2\2\2\u01cf\u01d5\7]\2\2\u01d0\u01d6\5\u00ec"+
		"w\2\u01d1\u01d6\5\u00e8u\2\u01d2\u01d6\5B\"\2\u01d3\u01d6\5H%\2\u01d4"+
		"\u01d6\5F$\2\u01d5\u01d0\3\2\2\2\u01d5\u01d1\3\2\2\2\u01d5\u01d2\3\2\2"+
		"\2\u01d5\u01d3\3\2\2\2\u01d5\u01d4\3\2\2\2\u01d6\67\3\2\2\2\u01d7\u01d9"+
		"\7X\2\2\u01d8\u01da\5$\23\2\u01d9\u01d8\3\2\2\2\u01d9\u01da\3\2\2\2\u01da"+
		"\u01db\3\2\2\2\u01db\u01dd\7U\2\2\u01dc\u01de\5$\23\2\u01dd\u01dc\3\2"+
		"\2\2\u01dd\u01de\3\2\2\2\u01de\u01df\3\2\2\2\u01df\u01e0\7Y\2\2\u01e0"+
		"9\3\2\2\2\u01e1\u01e3\7X\2\2\u01e2\u01e4\5$\23\2\u01e3\u01e2\3\2\2\2\u01e3"+
		"\u01e4\3\2\2\2\u01e4\u01e5\3\2\2\2\u01e5\u01e6\7Y\2\2\u01e6;\3\2\2\2\u01e7"+
		"\u01ee\5@!\2\u01e8\u01ee\5> \2\u01e9\u01ee\5B\"\2\u01ea\u01ee\5D#\2\u01eb"+
		"\u01ee\5F$\2\u01ec\u01ee\5H%\2\u01ed\u01e7\3\2\2\2\u01ed\u01e8\3\2\2\2"+
		"\u01ed\u01e9\3\2\2\2\u01ed\u01ea\3\2\2\2\u01ed\u01eb\3\2\2\2\u01ed\u01ec"+
		"\3\2\2\2\u01ee=\3\2\2\2\u01ef\u01f2\5\u00ecw\2\u01f0\u01f1\7]\2\2\u01f1"+
		"\u01f3\5\u00ecw\2\u01f2\u01f0\3\2\2\2\u01f2\u01f3\3\2\2\2\u01f3?\3\2\2"+
		"\2\u01f4\u01fa\7q\2\2\u01f5\u01fa\7r\2\2\u01f6\u01fa\5\u00e8u\2\u01f7"+
		"\u01fa\7p\2\2\u01f8\u01fa\7o\2\2\u01f9\u01f4\3\2\2\2\u01f9\u01f5\3\2\2"+
		"\2\u01f9\u01f6\3\2\2\2\u01f9\u01f7\3\2\2\2\u01f9\u01f8\3\2\2\2\u01faA"+
		"\3\2\2\2\u01fb\u01fd\7^\2\2\u01fc\u01fe\5\u00ecw\2\u01fd\u01fc\3\2\2\2"+
		"\u01fd\u01fe\3\2\2\2\u01feC\3\2\2\2\u01ff\u0200\7X\2\2\u0200\u0205\5$"+
		"\23\2\u0201\u0202\7T\2\2\u0202\u0204\5$\23\2\u0203\u0201\3\2\2\2\u0204"+
		"\u0207\3\2\2\2\u0205\u0203\3\2\2\2\u0205\u0206\3\2\2\2\u0206\u0208\3\2"+
		"\2\2\u0207\u0205\3\2\2\2\u0208\u0209\7Y\2\2\u0209E\3\2\2\2\u020a\u020b"+
		"\5\u00ecw\2\u020b\u0214\7V\2\2\u020c\u0211\5$\23\2\u020d\u020e\7T\2\2"+
		"\u020e\u0210\5$\23\2\u020f\u020d\3\2\2\2\u0210\u0213\3\2\2\2\u0211\u020f"+
		"\3\2\2\2\u0211\u0212\3\2\2\2\u0212\u0215\3\2\2\2\u0213\u0211\3\2\2\2\u0214"+
		"\u020c\3\2\2\2\u0214\u0215\3\2\2\2\u0215\u0216\3\2\2\2\u0216\u0217\7W"+
		"\2\2\u0217G\3\2\2\2\u0218\u0219\7V\2\2\u0219\u021a\5$\23\2\u021a\u021b"+
		"\7W\2\2\u021bI\3\2\2\2\u021c\u0226\5d\63\2\u021d\u0226\5X-\2\u021e\u0226"+
		"\5b\62\2\u021f\u0226\5`\61\2\u0220\u0226\5\\/\2\u0221\u0226\5Z.\2\u0222"+
		"\u0226\5V,\2\u0223\u0226\5L\'\2\u0224\u0226\5^\60\2\u0225\u021c\3\2\2"+
		"\2\u0225\u021d\3\2\2\2\u0225\u021e\3\2\2\2\u0225\u021f\3\2\2\2\u0225\u0220"+
		"\3\2\2\2\u0225\u0221\3\2\2\2\u0225\u0222\3\2\2\2\u0225\u0223\3\2\2\2\u0225"+
		"\u0224\3\2\2\2\u0226K\3\2\2\2\u0227\u0228\7Q\2\2\u0228\u0229\7V\2\2\u0229"+
		"\u022e\5N(\2\u022a\u022b\7T\2\2\u022b\u022d\5N(\2\u022c\u022a\3\2\2\2"+
		"\u022d\u0230\3\2\2\2\u022e\u022c\3\2\2\2\u022e\u022f\3\2\2\2\u022f\u0231"+
		"\3\2\2\2\u0230\u022e\3\2\2\2\u0231\u0232\7W\2\2\u0232M\3\2\2\2\u0233\u0234"+
		"\5\u00ecw\2\u0234\u0236\5J&\2\u0235\u0237\5P)\2\u0236\u0235\3\2\2\2\u0236"+
		"\u0237\3\2\2\2\u0237\u0239\3\2\2\2\u0238\u023a\5\u00e2r\2\u0239\u0238"+
		"\3\2\2\2\u0239\u023a\3\2\2\2\u023aO\3\2\2\2\u023b\u023d\5R*\2\u023c\u023e"+
		"\5T+\2\u023d\u023c\3\2\2\2\u023d\u023e\3\2\2\2\u023e\u0244\3\2\2\2\u023f"+
		"\u0241\5T+\2\u0240\u0242\5R*\2\u0241\u0240\3\2\2\2\u0241\u0242\3\2\2\2"+
		"\u0242\u0244\3\2\2\2\u0243\u023b\3\2\2\2\u0243\u023f\3\2\2\2\u0244Q\3"+
		"\2\2\2\u0245\u024b\7\21\2\2\u0246\u024c\5\u00e6t\2\u0247\u024c\5\u00e8"+
		"u\2\u0248\u024c\7p\2\2\u0249\u024c\7o\2\2\u024a\u024c\5\u00ecw\2\u024b"+
		"\u0246\3\2\2\2\u024b\u0247\3\2\2\2\u024b\u0248\3\2\2\2\u024b\u0249\3\2"+
		"\2\2\u024b\u024a\3\2\2\2\u024cS\3\2\2\2\u024d\u024e\7*\2\2\u024e\u024f"+
		"\7n\2\2\u024fU\3\2\2\2\u0250\u0251\7P\2\2\u0251\u0252\7V\2\2\u0252\u0253"+
		"\5J&\2\u0253\u0254\7W\2\2\u0254W\3\2\2\2\u0255\u0256\7H\2\2\u0256\u0257"+
		"\7V\2\2\u0257\u0258\5J&\2\u0258\u0259\7W\2\2\u0259Y\3\2\2\2\u025a\u025b"+
		"\t\7\2\2\u025b[\3\2\2\2\u025c\u025d\t\b\2\2\u025d]\3\2\2\2\u025e\u025f"+
		"\7R\2\2\u025f_\3\2\2\2\u0260\u0261\7L\2\2\u0261\u0262\7V\2\2\u0262\u0263"+
		"\5\u00eav\2\u0263\u0264\7W\2\2\u0264\u026b\3\2\2\2\u0265\u0266\7L\2\2"+
		"\u0266\u0267\7V\2\2\u0267\u0268\5\u00eav\2\u0268\u0269\b\61\1\2\u0269"+
		"\u026b\3\2\2\2\u026a\u0260\3\2\2\2\u026a\u0265\3\2\2\2\u026ba\3\2\2\2"+
		"\u026c\u026d\7J\2\2\u026dc\3\2\2\2\u026e\u0272\7I\2\2\u026f\u0270\7V\2"+
		"\2\u0270\u0271\7q\2\2\u0271\u0273\7W\2\2\u0272\u026f\3\2\2\2\u0272\u0273"+
		"\3\2\2\2\u0273e\3\2\2\2\u0274\u0279\5\u00ecw\2\u0275\u0276\7]\2\2\u0276"+
		"\u0278\5\u00ecw\2\u0277\u0275\3\2\2\2\u0278\u027b\3\2\2\2\u0279\u0277"+
		"\3\2\2\2\u0279\u027a\3\2\2\2\u027ag\3\2\2\2\u027b\u0279\3\2\2\2\u027c"+
		"\u027d\7\17\2\2\u027d\u0281\79\2\2\u027e\u027f\7 \2\2\u027f\u0280\7*\2"+
		"\2\u0280\u0282\7\30\2\2\u0281\u027e\3\2\2\2\u0281\u0282\3\2\2\2\u0282"+
		"\u0283\3\2\2\2\u0283\u0285\5j\66\2\u0284\u0286\5\u00e2r\2\u0285\u0284"+
		"\3\2\2\2\u0285\u0286\3\2\2\2\u0286\u0287\3\2\2\2\u0287\u0288\7V\2\2\u0288"+
		"\u0289\5l\67\2\u0289\u028b\7W\2\2\u028a\u028c\5x=\2\u028b\u028a\3\2\2"+
		"\2\u028b\u028c\3\2\2\2\u028ci\3\2\2\2\u028d\u028e\5f\64\2\u028ek\3\2\2"+
		"\2\u028f\u0292\5N(\2\u0290\u0292\5n8\2\u0291\u028f\3\2\2\2\u0291\u0290"+
		"\3\2\2\2\u0292\u029a\3\2\2\2\u0293\u0296\7T\2\2\u0294\u0297\5N(\2\u0295"+
		"\u0297\5n8\2\u0296\u0294\3\2\2\2\u0296\u0295\3\2\2\2\u0297\u0299\3\2\2"+
		"\2\u0298\u0293\3\2\2\2\u0299\u029c\3\2\2\2\u029a\u0298\3\2\2\2\u029a\u029b"+
		"\3\2\2\2\u029bm\3\2\2\2\u029c\u029a\3\2\2\2\u029d\u029e\7\62\2\2\u029e"+
		"\u029f\7$\2\2\u029f\u02a4\7V\2\2\u02a0\u02a2\5p9\2\u02a1\u02a3\7T\2\2"+
		"\u02a2\u02a1\3\2\2\2\u02a2\u02a3\3\2\2\2\u02a3\u02a5\3\2\2\2\u02a4\u02a0"+
		"\3\2\2\2\u02a4\u02a5\3\2\2\2\u02a5\u02a7\3\2\2\2\u02a6\u02a8\5r:\2\u02a7"+
		"\u02a6\3\2\2\2\u02a7\u02a8\3\2\2\2\u02a8\u02a9\3\2\2\2\u02a9\u02aa\7W"+
		"\2\2\u02aao\3\2\2\2\u02ab\u02ac\7\67\2\2\u02ac\u02ad\7V\2\2\u02ad\u02ae"+
		"\5r:\2\u02ae\u02af\7W\2\2\u02af\u02b5\3\2\2\2\u02b0\u02b1\7V\2\2\u02b1"+
		"\u02b2\5r:\2\u02b2\u02b3\b9\1\2\u02b3\u02b5\3\2\2\2\u02b4\u02ab\3\2\2"+
		"\2\u02b4\u02b0\3\2\2\2\u02b5q\3\2\2\2\u02b6\u02bb\5t;\2\u02b7\u02b8\7"+
		"T\2\2\u02b8\u02ba\5t;\2\u02b9\u02b7\3\2\2\2\u02ba\u02bd\3\2\2\2\u02bb"+
		"\u02b9\3\2\2\2\u02bb\u02bc\3\2\2\2\u02bcs\3\2\2\2\u02bd\u02bb\3\2\2\2"+
		"\u02be\u02c0\5\u00ecw\2\u02bf\u02c1\5v<\2\u02c0\u02bf\3\2\2\2\u02c0\u02c1"+
		"\3\2\2\2\u02c1u\3\2\2\2\u02c2\u02c3\7V\2\2\u02c3\u02c4\7q\2\2\u02c4\u02c5"+
		"\7W\2\2\u02c5w\3\2\2\2\u02c6\u02c7\7A\2\2\u02c7\u02c8\7=\2\2\u02c8\u02c9"+
		"\5\u00e4s\2\u02c9y\3\2\2\2\u02ca\u02cb\7\t\2\2\u02cb\u02cc\79\2\2\u02cc"+
		"\u02cd\5j\66\2\u02cd\u02ce\5|?\2\u02ce{\3\2\2\2\u02cf\u02d2\5~@\2\u02d0"+
		"\u02d2\5x=\2\u02d1\u02cf\3\2\2\2\u02d1\u02d0\3\2\2\2\u02d2}\3\2\2\2\u02d3"+
		"\u02d7\7V\2\2\u02d4\u02d8\5\u0080A\2\u02d5\u02d8\5\u0082B\2\u02d6\u02d8"+
		"\5\u0084C\2\u02d7\u02d4\3\2\2\2\u02d7\u02d5\3\2\2\2\u02d7\u02d6\3\2\2"+
		"\2\u02d8\u02e1\3\2\2\2\u02d9\u02dd\7T\2\2\u02da\u02de\5\u0080A\2\u02db"+
		"\u02de\5\u0082B\2\u02dc\u02de\5\u0084C\2\u02dd\u02da\3\2\2\2\u02dd\u02db"+
		"\3\2\2\2\u02dd\u02dc\3\2\2\2\u02de\u02e0\3\2\2\2\u02df\u02d9\3\2\2\2\u02e0"+
		"\u02e3\3\2\2\2\u02e1\u02df\3\2\2\2\u02e1\u02e2\3\2\2\2\u02e2\u02e4\3\2"+
		"\2\2\u02e3\u02e1\3\2\2\2\u02e4\u02e5\7W\2\2\u02e5\177\3\2\2\2\u02e6\u02e7"+
		"\7\6\2\2\u02e7\u02e8\5f\64\2\u02e8\u02ea\5J&\2\u02e9\u02eb\5P)\2\u02ea"+
		"\u02e9\3\2\2\2\u02ea\u02eb\3\2\2\2\u02eb\u02ed\3\2\2\2\u02ec\u02ee\5\u00e2"+
		"r\2\u02ed\u02ec\3\2\2\2\u02ed\u02ee\3\2\2\2\u02ee\u0081\3\2\2\2\u02ef"+
		"\u02f0\7\24\2\2\u02f0\u02f1\5f\64\2\u02f1\u0083\3\2\2\2\u02f2\u02f3\7"+
		")\2\2\u02f3\u02f4\5f\64\2\u02f4\u02f6\5J&\2\u02f5\u02f7\5P)\2\u02f6\u02f5"+
		"\3\2\2\2\u02f6\u02f7\3\2\2\2\u02f7\u02f9\3\2\2\2\u02f8\u02fa\5\u00e2r"+
		"\2\u02f9\u02f8\3\2\2\2\u02f9\u02fa\3\2\2\2\u02fa\u0085\3\2\2\2\u02fb\u02fc"+
		"\7\24\2\2\u02fc\u02ff\79\2\2\u02fd\u02fe\7 \2\2\u02fe\u0300\7\30\2\2\u02ff"+
		"\u02fd\3\2\2\2\u02ff\u0300\3\2\2\2\u0300\u0301\3\2\2\2\u0301\u0302\5f"+
		"\64\2\u0302\u0087\3\2\2\2\u0303\u0304\7\17\2\2\u0304\u0308\7!\2\2\u0305"+
		"\u0306\7 \2\2\u0306\u0307\7*\2\2\u0307\u0309\7\30\2\2\u0308\u0305\3\2"+
		"\2\2\u0308\u0309\3\2\2\2\u0309\u030a\3\2\2\2\u030a\u030b\5\u008aF\2\u030b"+
		"\u030c\7,\2\2\u030c\u0315\5j\66\2\u030d\u030e\7V\2\2\u030e\u030f\5\u008c"+
		"G\2\u030f\u0310\7W\2\2\u0310\u0316\3\2\2\2\u0311\u0312\7V\2\2\u0312\u0313"+
		"\5\u008cG\2\u0313\u0314\bE\1\2\u0314\u0316\3\2\2\2\u0315\u030d\3\2\2\2"+
		"\u0315\u0311\3\2\2\2\u0316\u0318\3\2\2\2\u0317\u0319\5\u00e2r\2\u0318"+
		"\u0317\3\2\2\2\u0318\u0319\3\2\2\2\u0319\u0089\3\2\2\2\u031a\u031b\5\u00ec"+
		"w\2\u031b\u008b\3\2\2\2\u031c\u0321\5\u008eH\2\u031d\u031e\7T\2\2\u031e"+
		"\u0320\5\u008eH\2\u031f\u031d\3\2\2\2\u0320\u0323\3\2\2\2\u0321\u031f"+
		"\3\2\2\2\u0321\u0322\3\2\2\2\u0322\u008d\3\2\2\2\u0323\u0321\3\2\2\2\u0324"+
		"\u0328\5f\64\2\u0325\u0328\5\u0090I\2\u0326\u0328\5\u0092J\2\u0327\u0324"+
		"\3\2\2\2\u0327\u0325\3\2\2\2\u0327\u0326\3\2\2\2\u0328\u008f\3\2\2\2\u0329"+
		"\u032a\7%\2\2\u032a\u032b\7V\2\2\u032b\u032c\5f\64\2\u032c\u032d\7W\2"+
		"\2\u032d\u0091\3\2\2\2\u032e\u032f\7\25\2\2\u032f\u0330\7V\2\2\u0330\u0331"+
		"\5f\64\2\u0331\u0334\7W\2\2\u0332\u0333\7]\2\2\u0333\u0335\5f\64\2\u0334"+
		"\u0332\3\2\2\2\u0334\u0335\3\2\2\2\u0335\u0093\3\2\2\2\u0336\u0337\7\17"+
		"\2\2\u0337\u0338\7\35\2\2\u0338\u033c\7!\2\2\u0339\u033a\7 \2\2\u033a"+
		"\u033b\7*\2\2\u033b\u033d\7\30\2\2\u033c\u0339\3\2\2\2\u033c\u033d\3\2"+
		"\2\2\u033d\u033e\3\2\2\2\u033e\u033f\5\u008aF\2\u033f\u0340\7,\2\2\u0340"+
		"\u0341\5j\66\2\u0341\u0343\5\u0096L\2\u0342\u0344\5\u009cO\2\u0343\u0342"+
		"\3\2\2\2\u0343\u0344\3\2\2\2\u0344\u0346\3\2\2\2\u0345\u0347\5\u00e2r"+
		"\2\u0346\u0345\3\2\2\2\u0346\u0347\3\2\2\2\u0347\u0095\3\2\2\2\u0348\u0349"+
		"\7V\2\2\u0349\u034a\5\u0098M\2\u034a\u034b\7W\2\2\u034b\u0351\3\2\2\2"+
		"\u034c\u034d\7V\2\2\u034d\u034e\5\u0098M\2\u034e\u034f\bL\1\2\u034f\u0351"+
		"\3\2\2\2\u0350\u0348\3\2\2\2\u0350\u034c\3\2\2\2\u0351\u0097\3\2\2\2\u0352"+
		"\u0357\5\u009aN\2\u0353\u0354\7T\2\2\u0354\u0356\5\u009aN\2\u0355\u0353"+
		"\3\2\2\2\u0356\u0359\3\2\2\2\u0357\u0355\3\2\2\2\u0357\u0358\3\2\2\2\u0358"+
		"\u0099\3\2\2\2\u0359\u0357\3\2\2\2\u035a\u035c\5\u008eH\2\u035b\u035d"+
		"\5\u00d8m\2\u035c\u035b\3\2\2\2\u035c\u035d\3\2\2\2\u035d\u009b\3\2\2"+
		"\2\u035e\u0362\5\u009eP\2\u035f\u0361\5\u009eP\2\u0360\u035f\3\2\2\2\u0361"+
		"\u0364\3\2\2\2\u0362\u0360\3\2\2\2\u0362\u0363\3\2\2\2\u0363\u009d\3\2"+
		"\2\2\u0364\u0362\3\2\2\2\u0365\u0366\7\26\2\2\u0366\u0367\7c\2\2\u0367"+
		"\u036c\7q\2\2\u0368\u0369\7\27\2\2\u0369\u036a\7c\2\2\u036a\u036c\7q\2"+
		"\2\u036b\u0365\3\2\2\2\u036b\u0368\3\2\2\2\u036c\u009f\3\2\2\2\u036d\u036e"+
		"\7\24\2\2\u036e\u0371\7!\2\2\u036f\u0370\7 \2\2\u0370\u0372\7\30\2\2\u0371"+
		"\u036f\3\2\2\2\u0371\u0372\3\2\2\2\u0372\u0373\3\2\2\2\u0373\u0374\5\u008a"+
		"F\2\u0374\u0375\7,\2\2\u0375\u0376\5f\64\2\u0376\u00a1\3\2\2\2\u0377\u037a"+
		"\t\t\2\2\u0378\u0379\7\13\2\2\u0379\u037b\7#\2\2\u037a\u0378\3\2\2\2\u037a"+
		"\u037b\3\2\2\2\u037b\u038d\3\2\2\2\u037c\u037d\79\2\2\u037d\u0386\5f\64"+
		"\2\u037e\u037f\7V\2\2\u037f\u0380\5\u008cG\2\u0380\u0381\7W\2\2\u0381"+
		"\u0387\3\2\2\2\u0382\u0383\7V\2\2\u0383\u0384\5\u008cG\2\u0384\u0385\b"+
		"R\1\2\u0385\u0387\3\2\2\2\u0386\u037e\3\2\2\2\u0386\u0382\3\2\2\2\u0386"+
		"\u0387\3\2\2\2\u0387\u038e\3\2\2\2\u0388\u0389\7!\2\2\u0389\u038a\5\u008a"+
		"F\2\u038a\u038b\7,\2\2\u038b\u038c\5f\64\2\u038c\u038e\3\2\2\2\u038d\u037c"+
		"\3\2\2\2\u038d\u0388\3\2\2\2\u038e\u00a3\3\2\2\2\u038f\u0392\78\2\2\u0390"+
		"\u0391\7\13\2\2\u0391\u0393\7#\2\2\u0392\u0390\3\2\2\2\u0392\u0393\3\2"+
		"\2\2\u0393\u03a0\3\2\2\2\u0394\u03a1\7:\2\2\u0395\u03a1\7@\2\2\u0396\u03a1"+
		"\7\65\2\2\u0397\u0398\7?\2\2\u0398\u03a1\5\u00b4[\2\u0399\u039a\7\64\2"+
		"\2\u039a\u03a1\5\u00ecw\2\u039b\u039c\7\"\2\2\u039c\u039d\7,\2\2\u039d"+
		"\u03a1\5f\64\2\u039e\u039f\79\2\2\u039f\u03a1\5f\64\2\u03a0\u0394\3\2"+
		"\2\2\u03a0\u0395\3\2\2\2\u03a0\u0396\3\2\2\2\u03a0\u0397\3\2\2\2\u03a0"+
		"\u0399\3\2\2\2\u03a0\u039b\3\2\2\2\u03a0\u039e\3\2\2\2\u03a1\u00a5\3\2"+
		"\2\2\u03a2\u03a3\7\17\2\2\u03a3\u03a4\7?\2\2\u03a4\u03a6\5\u00b8]\2\u03a5"+
		"\u03a7\5\u00c0a\2\u03a6\u03a5\3\2\2\2\u03a6\u03a7\3\2\2\2\u03a7\u03a9"+
		"\3\2\2\2\u03a8\u03aa\7\7\2\2\u03a9\u03a8\3\2\2\2\u03a9\u03aa\3\2\2\2\u03aa"+
		"\u00a7\3\2\2\2\u03ab\u03ac\7\17\2\2\u03ac\u03ad\7\64\2\2\u03ad\u03ae\5"+
		"\u00ecw\2\u03ae\u00a9\3\2\2\2\u03af\u03b0\7\t\2\2\u03b0\u03b1\7?\2\2\u03b1"+
		"\u03b3\5\u00b4[\2\u03b2\u03b4\5\u00be`\2\u03b3\u03b2\3\2\2\2\u03b3\u03b4"+
		"\3\2\2\2\u03b4\u03b6\3\2\2\2\u03b5\u03b7\7G\2\2\u03b6\u03b5\3\2\2\2\u03b6"+
		"\u03b7\3\2\2\2\u03b7\u03b9\3\2\2\2\u03b8\u03ba\7E\2\2\u03b9\u03b8\3\2"+
		"\2\2\u03b9\u03ba\3\2\2\2\u03ba\u03bc\3\2\2\2\u03bb\u03bd\5\u00bc_\2\u03bc"+
		"\u03bb\3\2\2\2\u03bc\u03bd\3\2\2\2\u03bd\u03bf\3\2\2\2\u03be\u03c0\5\u00c0"+
		"a\2\u03bf\u03be\3\2\2\2\u03bf\u03c0\3\2\2\2\u03c0\u00ab\3\2\2\2\u03c1"+
		"\u03c2\7\24\2\2\u03c2\u03c3\7?\2\2\u03c3\u03c4\5\u00b4[\2\u03c4\u00ad"+
		"\3\2\2\2\u03c5\u03c6\7\24\2\2\u03c6\u03c7\7\64\2\2\u03c7\u03c8\5\u00ec"+
		"w\2\u03c8\u00af\3\2\2\2\u03c9\u03cd\7\36\2\2\u03ca\u03ce\5\u00c2b\2\u03cb"+
		"\u03ce\5\u00c4c\2\u03cc\u03ce\5\u00c6d\2\u03cd\u03ca\3\2\2\2\u03cd\u03cb"+
		"\3\2\2\2\u03cd\u03cc\3\2\2\2\u03ce\u00b1\3\2\2\2\u03cf\u03d3\7\63\2\2"+
		"\u03d0\u03d4\5\u00c8e\2\u03d1\u03d4\5\u00caf\2\u03d2\u03d4\5\u00ccg\2"+
		"\u03d3\u03d0\3\2\2\2\u03d3\u03d1\3\2\2\2\u03d3\u03d2\3\2\2\2\u03d4\u00b3"+
		"\3\2\2\2\u03d5\u03d8\5\u00ecw\2\u03d6\u03d8\5\u00e8u\2\u03d7\u03d5\3\2"+
		"\2\2\u03d7\u03d6\3\2\2\2\u03d8\u00b5\3\2\2\2\u03d9\u03da\7\37\2\2\u03da"+
		"\u03db\5\u00ba^\2\u03db\u00b7\3\2\2\2\u03dc\u03dd\5\u00ecw\2\u03dd\u03df"+
		"\5\u00b6\\\2\u03de\u03e0\7E\2\2\u03df\u03de\3\2\2\2\u03df\u03e0\3\2\2"+
		"\2\u03e0\u03e2\3\2\2\2\u03e1\u03e3\5\u00bc_\2\u03e2\u03e1\3\2\2\2\u03e2"+
		"\u03e3\3\2\2\2\u03e3\u03e8\3\2\2\2\u03e4\u03e5\5\u00e8u\2\u03e5\u03e6"+
		"\7D\2\2\u03e6\u03e8\3\2\2\2\u03e7\u03dc\3\2\2\2\u03e7\u03e4\3\2\2\2\u03e8"+
		"\u00b9\3\2\2\2\u03e9\u03ea\7\r\2\2\u03ea\u03eb\5\u00e8u\2\u03eb\u00bb"+
		"\3\2\2\2\u03ec\u03ed\7/\2\2\u03ed\u03ee\7\'\2\2\u03ee\u03ef\5\u00e4s\2"+
		"\u03ef\u00bd\3\2\2\2\u03f0\u03f2\5\u00b6\\\2\u03f1\u03f3\7F\2\2\u03f2"+
		"\u03f1\3\2\2\2\u03f2\u03f3\3\2\2\2\u03f3\u00bf\3\2\2\2\u03f4\u03f5\7\5"+
		"\2\2\u03f5\u03f6\t\n\2\2\u03f6\u00c1\3\2\2\2\u03f7\u03f8\5\u00eav\2\u03f8"+
		"\u03f9\7<\2\2\u03f9\u03fa\5\u00ceh\2\u03fa\u00c3\3\2\2\2\u03fb\u03fc\5"+
		"\u00d0i\2\u03fc\u03fd\7<\2\2\u03fd\u03fe\5\u00ecw\2\u03fe\u00c5\3\2\2"+
		"\2\u03ff\u0400\5\u00d4k\2\u0400\u0401\7,\2\2\u0401\u0402\5\u00d6l\2\u0402"+
		"\u0403\7<\2\2\u0403\u0404\5\u00ecw\2\u0404\u00c7\3\2\2\2\u0405\u0406\5"+
		"\u00eav\2\u0406\u0407\7\34\2\2\u0407\u0408\5\u00ceh\2\u0408\u00c9\3\2"+
		"\2\2\u0409\u040a\5\u00d0i\2\u040a\u040b\7\34\2\2\u040b\u040c\5\u00ecw"+
		"\2\u040c\u00cb\3\2\2\2\u040d\u040e\5\u00d4k\2\u040e\u040f\7,\2\2\u040f"+
		"\u0410\5\u00d6l\2\u0410\u0411\7\34\2\2\u0411\u0412\5\u00ecw\2\u0412\u00cd"+
		"\3\2\2\2\u0413\u0414\7?\2\2\u0414\u0418\5\u00b4[\2\u0415\u0416\7\64\2"+
		"\2\u0416\u0418\5\u00ecw\2\u0417\u0413\3\2\2\2\u0417\u0415\3\2\2\2\u0418"+
		"\u00cf\3\2\2\2\u0419\u041e\5\u00d2j\2\u041a\u041b\7T\2\2\u041b\u041d\5"+
		"\u00d2j\2\u041c\u041a\3\2\2\2\u041d\u0420\3\2\2\2\u041e\u041c\3\2\2\2"+
		"\u041e\u041f\3\2\2\2\u041f\u00d1\3\2\2\2\u0420\u041e\3\2\2\2\u0421\u0424"+
		"\5\u00ecw\2\u0422\u0424\7C\2\2\u0423\u0421\3\2\2\2\u0423\u0422\3\2\2\2"+
		"\u0424\u00d3\3\2\2\2\u0425\u0428\5\u00d2j\2\u0426\u0428\7\b\2\2\u0427"+
		"\u0425\3\2\2\2\u0427\u0426\3\2\2\2\u0428\u0430\3\2\2\2\u0429\u042c\7T"+
		"\2\2\u042a\u042d\5\u00d2j\2\u042b\u042d\7\b\2\2\u042c\u042a\3\2\2\2\u042c"+
		"\u042b\3\2\2\2\u042d\u042f\3\2\2\2\u042e\u0429\3\2\2\2\u042f\u0432\3\2"+
		"\2\2\u0430\u042e\3\2\2\2\u0430\u0431\3\2\2\2\u0431\u00d5\3\2\2\2\u0432"+
		"\u0430\3\2\2\2\u0433\u0434\5f\64\2\u0434\u00d7\3\2\2\2\u0435\u0438\5\u00da"+
		"n\2\u0436\u0438\5\u00dco\2\u0437\u0435\3\2\2\2\u0437\u0436\3\2\2\2\u0438"+
		"\u00d9\3\2\2\2\u0439\u043a\7Z\2\2\u043a\u043f\5\u00dep\2\u043b\u043c\7"+
		"T\2\2\u043c\u043e\5\u00dep\2\u043d\u043b\3\2\2\2\u043e\u0441\3\2\2\2\u043f"+
		"\u043d\3\2\2\2\u043f\u0440\3\2\2\2\u0440\u0442\3\2\2\2\u0441\u043f\3\2"+
		"\2\2\u0442\u0443\7[\2\2\u0443\u0447\3\2\2\2\u0444\u0445\7Z\2\2\u0445\u0447"+
		"\7[\2\2\u0446\u0439\3\2\2\2\u0446\u0444\3\2\2\2\u0447\u00db\3\2\2\2\u0448"+
		"\u0449\7X\2\2\u0449\u044e\5\u00e0q\2\u044a\u044b\7T\2\2\u044b\u044d\5"+
		"\u00e0q\2\u044c\u044a\3\2\2\2\u044d\u0450\3\2\2\2\u044e\u044c\3\2\2\2"+
		"\u044e\u044f\3\2\2\2\u044f\u0451\3\2\2\2\u0450\u044e\3\2\2\2\u0451\u0452"+
		"\7Y\2\2\u0452\u0456\3\2\2\2\u0453\u0454\7X\2\2\u0454\u0456\7Y\2\2\u0455"+
		"\u0448\3\2\2\2\u0455\u0453\3\2\2\2\u0456\u00dd\3\2\2\2\u0457\u0458\7s"+
		"\2\2\u0458\u0459\7U\2\2\u0459\u045a\5\u00e0q\2\u045a\u00df\3\2\2\2\u045b"+
		"\u0463\5\u00dan\2\u045c\u0463\5\u00dco\2\u045d\u0463\7s\2\2\u045e\u0463"+
		"\5\u00e6t\2\u045f\u0463\7p\2\2\u0460\u0463\7o\2\2\u0461\u0463\7n\2\2\u0462"+
		"\u045b\3\2\2\2\u0462\u045c\3\2\2\2\u0462\u045d\3\2\2\2\u0462\u045e\3\2"+
		"\2\2\u0462\u045f\3\2\2\2\u0462\u0460\3\2\2\2\u0462\u0461\3\2\2\2\u0463"+
		"\u00e1\3\2\2\2\u0464\u0465\7\16\2\2\u0465\u0466\5\u00e8u\2\u0466\u00e3"+
		"\3\2\2\2\u0467\u0468\7q\2\2\u0468\u0469\7;\2\2\u0469\u00e5\3\2\2\2\u046a"+
		"\u046c\7l\2\2\u046b\u046a\3\2\2\2\u046b\u046c\3\2\2\2\u046c\u046d\3\2"+
		"\2\2\u046d\u046e\t\13\2\2\u046e\u00e7\3\2\2\2\u046f\u0470\t\f\2\2\u0470"+
		"\u00e9\3\2\2\2\u0471\u0476\5\u00ecw\2\u0472\u0473\7T\2\2\u0473\u0475\5"+
		"\u00ecw\2\u0474\u0472\3\2\2\2\u0475\u0478\3\2\2\2\u0476\u0474\3\2\2\2"+
		"\u0476\u0477\3\2\2\2\u0477\u00eb\3\2\2\2\u0478\u0476\3\2\2\2\u0479\u047d"+
		"\t\r\2\2\u047a\u047b\7v\2\2\u047b\u047d\bw\1\2\u047c\u0479\3\2\2\2\u047c"+
		"\u047a\3\2\2\2\u047d\u00ed\3\2\2\2|\u0101\u0104\u0110\u011b\u0120\u0123"+
		"\u0128\u012b\u012e\u0137\u0142\u0145\u014b\u0156\u016b\u016e\u0172\u017e"+
		"\u0182\u0186\u0190\u019b\u01a1\u01a5\u01af\u01b6\u01be\u01c4\u01ca\u01cc"+
		"\u01d5\u01d9\u01dd\u01e3\u01ed\u01f2\u01f9\u01fd\u0205\u0211\u0214\u0225"+
		"\u022e\u0236\u0239\u023d\u0241\u0243\u024b\u026a\u0272\u0279\u0281\u0285"+
		"\u028b\u0291\u0296\u029a\u02a2\u02a4\u02a7\u02b4\u02bb\u02c0\u02d1\u02d7"+
		"\u02dd\u02e1\u02ea\u02ed\u02f6\u02f9\u02ff\u0308\u0315\u0318\u0321\u0327"+
		"\u0334\u033c\u0343\u0346\u0350\u0357\u035c\u0362\u036b\u0371\u037a\u0386"+
		"\u038d\u0392\u03a0\u03a6\u03a9\u03b3\u03b6\u03b9\u03bc\u03bf\u03cd\u03d3"+
		"\u03d7\u03df\u03e2\u03e7\u03f2\u0417\u041e\u0423\u0427\u042c\u0430\u0437"+
		"\u043f\u0446\u044e\u0455\u0462\u046b\u0476\u047c";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}