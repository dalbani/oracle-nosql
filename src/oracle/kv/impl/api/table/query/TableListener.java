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
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TableParser}.
 */
public interface TableListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by the {@code Enum}
	 * labeled alternative in {@link TableParser#type_def}.
	 * @param ctx the parse tree
	 */
	void enterEnum(@NotNull TableParser.EnumContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Enum}
	 * labeled alternative in {@link TableParser#type_def}.
	 * @param ctx the parse tree
	 */
	void exitEnum(@NotNull TableParser.EnumContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#path_list}.
	 * @param ctx the parse tree
	 */
	void enterPath_list(@NotNull TableParser.Path_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#path_list}.
	 * @param ctx the parse tree
	 */
	void exitPath_list(@NotNull TableParser.Path_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#sys_priv_list}.
	 * @param ctx the parse tree
	 */
	void enterSys_priv_list(@NotNull TableParser.Sys_priv_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#sys_priv_list}.
	 * @param ctx the parse tree
	 */
	void exitSys_priv_list(@NotNull TableParser.Sys_priv_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#boolean_constraint}.
	 * @param ctx the parse tree
	 */
	void enterBoolean_constraint(@NotNull TableParser.Boolean_constraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#boolean_constraint}.
	 * @param ctx the parse tree
	 */
	void exitBoolean_constraint(@NotNull TableParser.Boolean_constraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#fts_path}.
	 * @param ctx the parse tree
	 */
	void enterFts_path(@NotNull TableParser.Fts_pathContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#fts_path}.
	 * @param ctx the parse tree
	 */
	void exitFts_path(@NotNull TableParser.Fts_pathContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#password_lifetime}.
	 * @param ctx the parse tree
	 */
	void enterPassword_lifetime(@NotNull TableParser.Password_lifetimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#password_lifetime}.
	 * @param ctx the parse tree
	 */
	void exitPassword_lifetime(@NotNull TableParser.Password_lifetimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#array_def}.
	 * @param ctx the parse tree
	 */
	void enterArray_def(@NotNull TableParser.Array_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#array_def}.
	 * @param ctx the parse tree
	 */
	void exitArray_def(@NotNull TableParser.Array_defContext ctx);
	/**
	 * Enter a parse tree produced by the {@code String}
	 * labeled alternative in {@link TableParser#type_def}.
	 * @param ctx the parse tree
	 */
	void enterString(@NotNull TableParser.StringContext ctx);
	/**
	 * Exit a parse tree produced by the {@code String}
	 * labeled alternative in {@link TableParser#type_def}.
	 * @param ctx the parse tree
	 */
	void exitString(@NotNull TableParser.StringContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#elementof_expr}.
	 * @param ctx the parse tree
	 */
	void enterElementof_expr(@NotNull TableParser.Elementof_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#elementof_expr}.
	 * @param ctx the parse tree
	 */
	void exitElementof_expr(@NotNull TableParser.Elementof_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#revoke_object_privileges}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_object_privileges(@NotNull TableParser.Revoke_object_privilegesContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#revoke_object_privileges}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_object_privileges(@NotNull TableParser.Revoke_object_privilegesContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#drop_role_statement}.
	 * @param ctx the parse tree
	 */
	void enterDrop_role_statement(@NotNull TableParser.Drop_role_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#drop_role_statement}.
	 * @param ctx the parse tree
	 */
	void exitDrop_role_statement(@NotNull TableParser.Drop_role_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#create_text_index_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_text_index_statement(@NotNull TableParser.Create_text_index_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#create_text_index_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_text_index_statement(@NotNull TableParser.Create_text_index_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#fts_field_list}.
	 * @param ctx the parse tree
	 */
	void enterFts_field_list(@NotNull TableParser.Fts_field_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#fts_field_list}.
	 * @param ctx the parse tree
	 */
	void exitFts_field_list(@NotNull TableParser.Fts_field_listContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Boolean}
	 * labeled alternative in {@link TableParser#type_def}.
	 * @param ctx the parse tree
	 */
	void enterBoolean(@NotNull TableParser.BooleanContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Boolean}
	 * labeled alternative in {@link TableParser#type_def}.
	 * @param ctx the parse tree
	 */
	void exitBoolean(@NotNull TableParser.BooleanContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(@NotNull TableParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(@NotNull TableParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#id_list_with_paren}.
	 * @param ctx the parse tree
	 */
	void enterId_list_with_paren(@NotNull TableParser.Id_list_with_parenContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#id_list_with_paren}.
	 * @param ctx the parse tree
	 */
	void exitId_list_with_paren(@NotNull TableParser.Id_list_with_parenContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#float_constraint}.
	 * @param ctx the parse tree
	 */
	void enterFloat_constraint(@NotNull TableParser.Float_constraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#float_constraint}.
	 * @param ctx the parse tree
	 */
	void exitFloat_constraint(@NotNull TableParser.Float_constraintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code JsonPair}
	 * labeled alternative in {@link TableParser#jspair}.
	 * @param ctx the parse tree
	 */
	void enterJsonPair(@NotNull TableParser.JsonPairContext ctx);
	/**
	 * Exit a parse tree produced by the {@code JsonPair}
	 * labeled alternative in {@link TableParser#jspair}.
	 * @param ctx the parse tree
	 */
	void exitJsonPair(@NotNull TableParser.JsonPairContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#integer_def}.
	 * @param ctx the parse tree
	 */
	void enterInteger_def(@NotNull TableParser.Integer_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#integer_def}.
	 * @param ctx the parse tree
	 */
	void exitInteger_def(@NotNull TableParser.Integer_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#field_def}.
	 * @param ctx the parse tree
	 */
	void enterField_def(@NotNull TableParser.Field_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#field_def}.
	 * @param ctx the parse tree
	 */
	void exitField_def(@NotNull TableParser.Field_defContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Float}
	 * labeled alternative in {@link TableParser#type_def}.
	 * @param ctx the parse tree
	 */
	void enterFloat(@NotNull TableParser.FloatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Float}
	 * labeled alternative in {@link TableParser#type_def}.
	 * @param ctx the parse tree
	 */
	void exitFloat(@NotNull TableParser.FloatContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#extended_id}.
	 * @param ctx the parse tree
	 */
	void enterExtended_id(@NotNull TableParser.Extended_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#extended_id}.
	 * @param ctx the parse tree
	 */
	void exitExtended_id(@NotNull TableParser.Extended_idContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#enum_constraint}.
	 * @param ctx the parse tree
	 */
	void enterEnum_constraint(@NotNull TableParser.Enum_constraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#enum_constraint}.
	 * @param ctx the parse tree
	 */
	void exitEnum_constraint(@NotNull TableParser.Enum_constraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#fts_path_list}.
	 * @param ctx the parse tree
	 */
	void enterFts_path_list(@NotNull TableParser.Fts_path_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#fts_path_list}.
	 * @param ctx the parse tree
	 */
	void exitFts_path_list(@NotNull TableParser.Fts_path_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#complex_name_path}.
	 * @param ctx the parse tree
	 */
	void enterComplex_name_path(@NotNull TableParser.Complex_name_pathContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#complex_name_path}.
	 * @param ctx the parse tree
	 */
	void exitComplex_name_path(@NotNull TableParser.Complex_name_pathContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#drop_field_statement}.
	 * @param ctx the parse tree
	 */
	void enterDrop_field_statement(@NotNull TableParser.Drop_field_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#drop_field_statement}.
	 * @param ctx the parse tree
	 */
	void exitDrop_field_statement(@NotNull TableParser.Drop_field_statementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Map}
	 * labeled alternative in {@link TableParser#type_def}.
	 * @param ctx the parse tree
	 */
	void enterMap(@NotNull TableParser.MapContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Map}
	 * labeled alternative in {@link TableParser#type_def}.
	 * @param ctx the parse tree
	 */
	void exitMap(@NotNull TableParser.MapContext ctx);
	/**
	 * Enter a parse tree produced by the {@code EmptyJsonObject}
	 * labeled alternative in {@link TableParser#jsobject}.
	 * @param ctx the parse tree
	 */
	void enterEmptyJsonObject(@NotNull TableParser.EmptyJsonObjectContext ctx);
	/**
	 * Exit a parse tree produced by the {@code EmptyJsonObject}
	 * labeled alternative in {@link TableParser#jsobject}.
	 * @param ctx the parse tree
	 */
	void exitEmptyJsonObject(@NotNull TableParser.EmptyJsonObjectContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#object}.
	 * @param ctx the parse tree
	 */
	void enterObject(@NotNull TableParser.ObjectContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#object}.
	 * @param ctx the parse tree
	 */
	void exitObject(@NotNull TableParser.ObjectContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#simple_field_list}.
	 * @param ctx the parse tree
	 */
	void enterSimple_field_list(@NotNull TableParser.Simple_field_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#simple_field_list}.
	 * @param ctx the parse tree
	 */
	void exitSimple_field_list(@NotNull TableParser.Simple_field_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#complex_field_list}.
	 * @param ctx the parse tree
	 */
	void enterComplex_field_list(@NotNull TableParser.Complex_field_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#complex_field_list}.
	 * @param ctx the parse tree
	 */
	void exitComplex_field_list(@NotNull TableParser.Complex_field_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#map_def}.
	 * @param ctx the parse tree
	 */
	void enterMap_def(@NotNull TableParser.Map_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#map_def}.
	 * @param ctx the parse tree
	 */
	void exitMap_def(@NotNull TableParser.Map_defContext ctx);
	/**
	 * Enter a parse tree produced by the {@code EmptyJsonArray}
	 * labeled alternative in {@link TableParser#jsarray}.
	 * @param ctx the parse tree
	 */
	void enterEmptyJsonArray(@NotNull TableParser.EmptyJsonArrayContext ctx);
	/**
	 * Exit a parse tree produced by the {@code EmptyJsonArray}
	 * labeled alternative in {@link TableParser#jsarray}.
	 * @param ctx the parse tree
	 */
	void exitEmptyJsonArray(@NotNull TableParser.EmptyJsonArrayContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#reset_password_clause}.
	 * @param ctx the parse tree
	 */
	void enterReset_password_clause(@NotNull TableParser.Reset_password_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#reset_password_clause}.
	 * @param ctx the parse tree
	 */
	void exitReset_password_clause(@NotNull TableParser.Reset_password_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#binary_def}.
	 * @param ctx the parse tree
	 */
	void enterBinary_def(@NotNull TableParser.Binary_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#binary_def}.
	 * @param ctx the parse tree
	 */
	void exitBinary_def(@NotNull TableParser.Binary_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#key_def}.
	 * @param ctx the parse tree
	 */
	void enterKey_def(@NotNull TableParser.Key_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#key_def}.
	 * @param ctx the parse tree
	 */
	void exitKey_def(@NotNull TableParser.Key_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#enum_def}.
	 * @param ctx the parse tree
	 */
	void enterEnum_def(@NotNull TableParser.Enum_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#enum_def}.
	 * @param ctx the parse tree
	 */
	void exitEnum_def(@NotNull TableParser.Enum_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#create_user_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_user_statement(@NotNull TableParser.Create_user_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#create_user_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_user_statement(@NotNull TableParser.Create_user_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#alter_field_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_field_statement(@NotNull TableParser.Alter_field_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#alter_field_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_field_statement(@NotNull TableParser.Alter_field_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#revoke_system_privileges}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_system_privileges(@NotNull TableParser.Revoke_system_privilegesContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#revoke_system_privileges}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_system_privileges(@NotNull TableParser.Revoke_system_privilegesContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#index_name}.
	 * @param ctx the parse tree
	 */
	void enterIndex_name(@NotNull TableParser.Index_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#index_name}.
	 * @param ctx the parse tree
	 */
	void exitIndex_name(@NotNull TableParser.Index_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#drop_table_statement}.
	 * @param ctx the parse tree
	 */
	void enterDrop_table_statement(@NotNull TableParser.Drop_table_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#drop_table_statement}.
	 * @param ctx the parse tree
	 */
	void exitDrop_table_statement(@NotNull TableParser.Drop_table_statementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code JsonAtom}
	 * labeled alternative in {@link TableParser#jsvalue}.
	 * @param ctx the parse tree
	 */
	void enterJsonAtom(@NotNull TableParser.JsonAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code JsonAtom}
	 * labeled alternative in {@link TableParser#jsvalue}.
	 * @param ctx the parse tree
	 */
	void exitJsonAtom(@NotNull TableParser.JsonAtomContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#table_def}.
	 * @param ctx the parse tree
	 */
	void enterTable_def(@NotNull TableParser.Table_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#table_def}.
	 * @param ctx the parse tree
	 */
	void exitTable_def(@NotNull TableParser.Table_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#keyof_expr}.
	 * @param ctx the parse tree
	 */
	void enterKeyof_expr(@NotNull TableParser.Keyof_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#keyof_expr}.
	 * @param ctx the parse tree
	 */
	void exitKeyof_expr(@NotNull TableParser.Keyof_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#drop_index_statement}.
	 * @param ctx the parse tree
	 */
	void enterDrop_index_statement(@NotNull TableParser.Drop_index_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#drop_index_statement}.
	 * @param ctx the parse tree
	 */
	void exitDrop_index_statement(@NotNull TableParser.Drop_index_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#integer_constraint}.
	 * @param ctx the parse tree
	 */
	void enterInteger_constraint(@NotNull TableParser.Integer_constraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#integer_constraint}.
	 * @param ctx the parse tree
	 */
	void exitInteger_constraint(@NotNull TableParser.Integer_constraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#not_null}.
	 * @param ctx the parse tree
	 */
	void enterNot_null(@NotNull TableParser.Not_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#not_null}.
	 * @param ctx the parse tree
	 */
	void exitNot_null(@NotNull TableParser.Not_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#show_statement}.
	 * @param ctx the parse tree
	 */
	void enterShow_statement(@NotNull TableParser.Show_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#show_statement}.
	 * @param ctx the parse tree
	 */
	void exitShow_statement(@NotNull TableParser.Show_statementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code JsonObject}
	 * labeled alternative in {@link TableParser#jsobject}.
	 * @param ctx the parse tree
	 */
	void enterJsonObject(@NotNull TableParser.JsonObjectContext ctx);
	/**
	 * Exit a parse tree produced by the {@code JsonObject}
	 * labeled alternative in {@link TableParser#jsobject}.
	 * @param ctx the parse tree
	 */
	void exitJsonObject(@NotNull TableParser.JsonObjectContext ctx);
	/**
	 * Enter a parse tree produced by the {@code JsonArrayValue}
	 * labeled alternative in {@link TableParser#jsvalue}.
	 * @param ctx the parse tree
	 */
	void enterJsonArrayValue(@NotNull TableParser.JsonArrayValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code JsonArrayValue}
	 * labeled alternative in {@link TableParser#jsvalue}.
	 * @param ctx the parse tree
	 */
	void exitJsonArrayValue(@NotNull TableParser.JsonArrayValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#grant_roles}.
	 * @param ctx the parse tree
	 */
	void enterGrant_roles(@NotNull TableParser.Grant_rolesContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#grant_roles}.
	 * @param ctx the parse tree
	 */
	void exitGrant_roles(@NotNull TableParser.Grant_rolesContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#float_def}.
	 * @param ctx the parse tree
	 */
	void enterFloat_def(@NotNull TableParser.Float_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#float_def}.
	 * @param ctx the parse tree
	 */
	void exitFloat_def(@NotNull TableParser.Float_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#add_field_statement}.
	 * @param ctx the parse tree
	 */
	void enterAdd_field_statement(@NotNull TableParser.Add_field_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#add_field_statement}.
	 * @param ctx the parse tree
	 */
	void exitAdd_field_statement(@NotNull TableParser.Add_field_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#alter_table_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_table_statement(@NotNull TableParser.Alter_table_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#alter_table_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_table_statement(@NotNull TableParser.Alter_table_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#grant_statement}.
	 * @param ctx the parse tree
	 */
	void enterGrant_statement(@NotNull TableParser.Grant_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#grant_statement}.
	 * @param ctx the parse tree
	 */
	void exitGrant_statement(@NotNull TableParser.Grant_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#string_def}.
	 * @param ctx the parse tree
	 */
	void enterString_def(@NotNull TableParser.String_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#string_def}.
	 * @param ctx the parse tree
	 */
	void exitString_def(@NotNull TableParser.String_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#alter_user_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_user_statement(@NotNull TableParser.Alter_user_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#alter_user_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_user_statement(@NotNull TableParser.Alter_user_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#float_default}.
	 * @param ctx the parse tree
	 */
	void enterFloat_default(@NotNull TableParser.Float_defaultContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#float_default}.
	 * @param ctx the parse tree
	 */
	void exitFloat_default(@NotNull TableParser.Float_defaultContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#create_user_identified_clause}.
	 * @param ctx the parse tree
	 */
	void enterCreate_user_identified_clause(@NotNull TableParser.Create_user_identified_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#create_user_identified_clause}.
	 * @param ctx the parse tree
	 */
	void exitCreate_user_identified_clause(@NotNull TableParser.Create_user_identified_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#integer_default}.
	 * @param ctx the parse tree
	 */
	void enterInteger_default(@NotNull TableParser.Integer_defaultContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#integer_default}.
	 * @param ctx the parse tree
	 */
	void exitInteger_default(@NotNull TableParser.Integer_defaultContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#revoke_roles}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_roles(@NotNull TableParser.Revoke_rolesContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#revoke_roles}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_roles(@NotNull TableParser.Revoke_rolesContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#string_constraint}.
	 * @param ctx the parse tree
	 */
	void enterString_constraint(@NotNull TableParser.String_constraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#string_constraint}.
	 * @param ctx the parse tree
	 */
	void exitString_constraint(@NotNull TableParser.String_constraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#grant_system_privileges}.
	 * @param ctx the parse tree
	 */
	void enterGrant_system_privileges(@NotNull TableParser.Grant_system_privilegesContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#grant_system_privileges}.
	 * @param ctx the parse tree
	 */
	void exitGrant_system_privileges(@NotNull TableParser.Grant_system_privilegesContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#by_password}.
	 * @param ctx the parse tree
	 */
	void enterBy_password(@NotNull TableParser.By_passwordContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#by_password}.
	 * @param ctx the parse tree
	 */
	void exitBy_password(@NotNull TableParser.By_passwordContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#string_default}.
	 * @param ctx the parse tree
	 */
	void enterString_default(@NotNull TableParser.String_defaultContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#string_default}.
	 * @param ctx the parse tree
	 */
	void exitString_default(@NotNull TableParser.String_defaultContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Binary}
	 * labeled alternative in {@link TableParser#type_def}.
	 * @param ctx the parse tree
	 */
	void enterBinary(@NotNull TableParser.BinaryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Binary}
	 * labeled alternative in {@link TableParser#type_def}.
	 * @param ctx the parse tree
	 */
	void exitBinary(@NotNull TableParser.BinaryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ArrayOfJsonValues}
	 * labeled alternative in {@link TableParser#jsarray}.
	 * @param ctx the parse tree
	 */
	void enterArrayOfJsonValues(@NotNull TableParser.ArrayOfJsonValuesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ArrayOfJsonValues}
	 * labeled alternative in {@link TableParser#jsarray}.
	 * @param ctx the parse tree
	 */
	void exitArrayOfJsonValues(@NotNull TableParser.ArrayOfJsonValuesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code JsonObjectValue}
	 * labeled alternative in {@link TableParser#jsvalue}.
	 * @param ctx the parse tree
	 */
	void enterJsonObjectValue(@NotNull TableParser.JsonObjectValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code JsonObjectValue}
	 * labeled alternative in {@link TableParser#jsvalue}.
	 * @param ctx the parse tree
	 */
	void exitJsonObjectValue(@NotNull TableParser.JsonObjectValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#id_list}.
	 * @param ctx the parse tree
	 */
	void enterId_list(@NotNull TableParser.Id_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#id_list}.
	 * @param ctx the parse tree
	 */
	void exitId_list(@NotNull TableParser.Id_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#drop_user_statement}.
	 * @param ctx the parse tree
	 */
	void enterDrop_user_statement(@NotNull TableParser.Drop_user_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#drop_user_statement}.
	 * @param ctx the parse tree
	 */
	void exitDrop_user_statement(@NotNull TableParser.Drop_user_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#identifier_or_string}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier_or_string(@NotNull TableParser.Identifier_or_stringContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#identifier_or_string}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier_or_string(@NotNull TableParser.Identifier_or_stringContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#create_table_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_table_statement(@NotNull TableParser.Create_table_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#create_table_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_table_statement(@NotNull TableParser.Create_table_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#record_def}.
	 * @param ctx the parse tree
	 */
	void enterRecord_def(@NotNull TableParser.Record_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#record_def}.
	 * @param ctx the parse tree
	 */
	void exitRecord_def(@NotNull TableParser.Record_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#create_index_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_index_statement(@NotNull TableParser.Create_index_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#create_index_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_index_statement(@NotNull TableParser.Create_index_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#boolean_def}.
	 * @param ctx the parse tree
	 */
	void enterBoolean_def(@NotNull TableParser.Boolean_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#boolean_def}.
	 * @param ctx the parse tree
	 */
	void exitBoolean_def(@NotNull TableParser.Boolean_defContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Int}
	 * labeled alternative in {@link TableParser#type_def}.
	 * @param ctx the parse tree
	 */
	void enterInt(@NotNull TableParser.IntContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Int}
	 * labeled alternative in {@link TableParser#type_def}.
	 * @param ctx the parse tree
	 */
	void exitInt(@NotNull TableParser.IntContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#priv_item}.
	 * @param ctx the parse tree
	 */
	void enterPriv_item(@NotNull TableParser.Priv_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#priv_item}.
	 * @param ctx the parse tree
	 */
	void exitPriv_item(@NotNull TableParser.Priv_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#name_path}.
	 * @param ctx the parse tree
	 */
	void enterName_path(@NotNull TableParser.Name_pathContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#name_path}.
	 * @param ctx the parse tree
	 */
	void exitName_path(@NotNull TableParser.Name_pathContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#duration}.
	 * @param ctx the parse tree
	 */
	void enterDuration(@NotNull TableParser.DurationContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#duration}.
	 * @param ctx the parse tree
	 */
	void exitDuration(@NotNull TableParser.DurationContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#principal}.
	 * @param ctx the parse tree
	 */
	void enterPrincipal(@NotNull TableParser.PrincipalContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#principal}.
	 * @param ctx the parse tree
	 */
	void exitPrincipal(@NotNull TableParser.PrincipalContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#obj_priv_list}.
	 * @param ctx the parse tree
	 */
	void enterObj_priv_list(@NotNull TableParser.Obj_priv_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#obj_priv_list}.
	 * @param ctx the parse tree
	 */
	void exitObj_priv_list(@NotNull TableParser.Obj_priv_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#modify_field_statement}.
	 * @param ctx the parse tree
	 */
	void enterModify_field_statement(@NotNull TableParser.Modify_field_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#modify_field_statement}.
	 * @param ctx the parse tree
	 */
	void exitModify_field_statement(@NotNull TableParser.Modify_field_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#json}.
	 * @param ctx the parse tree
	 */
	void enterJson(@NotNull TableParser.JsonContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#json}.
	 * @param ctx the parse tree
	 */
	void exitJson(@NotNull TableParser.JsonContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Record}
	 * labeled alternative in {@link TableParser#type_def}.
	 * @param ctx the parse tree
	 */
	void enterRecord(@NotNull TableParser.RecordContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Record}
	 * labeled alternative in {@link TableParser#type_def}.
	 * @param ctx the parse tree
	 */
	void exitRecord(@NotNull TableParser.RecordContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(@NotNull TableParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(@NotNull TableParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#shard_key_def}.
	 * @param ctx the parse tree
	 */
	void enterShard_key_def(@NotNull TableParser.Shard_key_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#shard_key_def}.
	 * @param ctx the parse tree
	 */
	void exitShard_key_def(@NotNull TableParser.Shard_key_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#identified_clause}.
	 * @param ctx the parse tree
	 */
	void enterIdentified_clause(@NotNull TableParser.Identified_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#identified_clause}.
	 * @param ctx the parse tree
	 */
	void exitIdentified_clause(@NotNull TableParser.Identified_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#grant_object_privileges}.
	 * @param ctx the parse tree
	 */
	void enterGrant_object_privileges(@NotNull TableParser.Grant_object_privilegesContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#grant_object_privileges}.
	 * @param ctx the parse tree
	 */
	void exitGrant_object_privileges(@NotNull TableParser.Grant_object_privilegesContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#create_role_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_role_statement(@NotNull TableParser.Create_role_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#create_role_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_role_statement(@NotNull TableParser.Create_role_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#parse}.
	 * @param ctx the parse tree
	 */
	void enterParse(@NotNull TableParser.ParseContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#parse}.
	 * @param ctx the parse tree
	 */
	void exitParse(@NotNull TableParser.ParseContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#describe_statement}.
	 * @param ctx the parse tree
	 */
	void enterDescribe_statement(@NotNull TableParser.Describe_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#describe_statement}.
	 * @param ctx the parse tree
	 */
	void exitDescribe_statement(@NotNull TableParser.Describe_statementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Array}
	 * labeled alternative in {@link TableParser#type_def}.
	 * @param ctx the parse tree
	 */
	void enterArray(@NotNull TableParser.ArrayContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Array}
	 * labeled alternative in {@link TableParser#type_def}.
	 * @param ctx the parse tree
	 */
	void exitArray(@NotNull TableParser.ArrayContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#revoke_statement}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_statement(@NotNull TableParser.Revoke_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#revoke_statement}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_statement(@NotNull TableParser.Revoke_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#account_lock}.
	 * @param ctx the parse tree
	 */
	void enterAccount_lock(@NotNull TableParser.Account_lockContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#account_lock}.
	 * @param ctx the parse tree
	 */
	void exitAccount_lock(@NotNull TableParser.Account_lockContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#string_list}.
	 * @param ctx the parse tree
	 */
	void enterString_list(@NotNull TableParser.String_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#string_list}.
	 * @param ctx the parse tree
	 */
	void exitString_list(@NotNull TableParser.String_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#comment}.
	 * @param ctx the parse tree
	 */
	void enterComment(@NotNull TableParser.CommentContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#comment}.
	 * @param ctx the parse tree
	 */
	void exitComment(@NotNull TableParser.CommentContext ctx);
	/**
	 * Enter a parse tree produced by {@link TableParser#check_expression}.
	 * @param ctx the parse tree
	 */
	void enterCheck_expression(@NotNull TableParser.Check_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link TableParser#check_expression}.
	 * @param ctx the parse tree
	 */
	void exitCheck_expression(@NotNull TableParser.Check_expressionContext ctx);
}