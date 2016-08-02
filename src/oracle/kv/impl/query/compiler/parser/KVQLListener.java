// Generated from /media/sdd/dev/nsdb/kv/kvstore/src/oracle/kv/impl/query/compiler/parser/KVQL.g4 by ANTLR 4.4
package oracle.kv.impl.query.compiler.parser;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link KVQLParser}.
 */
public interface KVQLListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by the {@code Enum}
	 * labeled alternative in {@link KVQLParser#type_def}.
	 * @param ctx the parse tree
	 */
	void enterEnum(@NotNull KVQLParser.EnumContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Enum}
	 * labeled alternative in {@link KVQLParser#type_def}.
	 * @param ctx the parse tree
	 */
	void exitEnum(@NotNull KVQLParser.EnumContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#path_list}.
	 * @param ctx the parse tree
	 */
	void enterPath_list(@NotNull KVQLParser.Path_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#path_list}.
	 * @param ctx the parse tree
	 */
	void exitPath_list(@NotNull KVQLParser.Path_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#sys_priv_list}.
	 * @param ctx the parse tree
	 */
	void enterSys_priv_list(@NotNull KVQLParser.Sys_priv_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#sys_priv_list}.
	 * @param ctx the parse tree
	 */
	void exitSys_priv_list(@NotNull KVQLParser.Sys_priv_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#fts_path}.
	 * @param ctx the parse tree
	 */
	void enterFts_path(@NotNull KVQLParser.Fts_pathContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#fts_path}.
	 * @param ctx the parse tree
	 */
	void exitFts_path(@NotNull KVQLParser.Fts_pathContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#password_lifetime}.
	 * @param ctx the parse tree
	 */
	void enterPassword_lifetime(@NotNull KVQLParser.Password_lifetimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#password_lifetime}.
	 * @param ctx the parse tree
	 */
	void exitPassword_lifetime(@NotNull KVQLParser.Password_lifetimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#array_def}.
	 * @param ctx the parse tree
	 */
	void enterArray_def(@NotNull KVQLParser.Array_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#array_def}.
	 * @param ctx the parse tree
	 */
	void exitArray_def(@NotNull KVQLParser.Array_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#table_name}.
	 * @param ctx the parse tree
	 */
	void enterTable_name(@NotNull KVQLParser.Table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#table_name}.
	 * @param ctx the parse tree
	 */
	void exitTable_name(@NotNull KVQLParser.Table_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#number}.
	 * @param ctx the parse tree
	 */
	void enterNumber(@NotNull KVQLParser.NumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#number}.
	 * @param ctx the parse tree
	 */
	void exitNumber(@NotNull KVQLParser.NumberContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#elementof_expr}.
	 * @param ctx the parse tree
	 */
	void enterElementof_expr(@NotNull KVQLParser.Elementof_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#elementof_expr}.
	 * @param ctx the parse tree
	 */
	void exitElementof_expr(@NotNull KVQLParser.Elementof_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#and_expr}.
	 * @param ctx the parse tree
	 */
	void enterAnd_expr(@NotNull KVQLParser.And_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#and_expr}.
	 * @param ctx the parse tree
	 */
	void exitAnd_expr(@NotNull KVQLParser.And_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#revoke_object_privileges}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_object_privileges(@NotNull KVQLParser.Revoke_object_privilegesContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#revoke_object_privileges}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_object_privileges(@NotNull KVQLParser.Revoke_object_privilegesContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#or_expr}.
	 * @param ctx the parse tree
	 */
	void enterOr_expr(@NotNull KVQLParser.Or_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#or_expr}.
	 * @param ctx the parse tree
	 */
	void exitOr_expr(@NotNull KVQLParser.Or_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#drop_role_statement}.
	 * @param ctx the parse tree
	 */
	void enterDrop_role_statement(@NotNull KVQLParser.Drop_role_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#drop_role_statement}.
	 * @param ctx the parse tree
	 */
	void exitDrop_role_statement(@NotNull KVQLParser.Drop_role_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#create_text_index_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_text_index_statement(@NotNull KVQLParser.Create_text_index_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#create_text_index_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_text_index_statement(@NotNull KVQLParser.Create_text_index_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#fts_field_list}.
	 * @param ctx the parse tree
	 */
	void enterFts_field_list(@NotNull KVQLParser.Fts_field_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#fts_field_list}.
	 * @param ctx the parse tree
	 */
	void exitFts_field_list(@NotNull KVQLParser.Fts_field_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#id}.
	 * @param ctx the parse tree
	 */
	void enterId(@NotNull KVQLParser.IdContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#id}.
	 * @param ctx the parse tree
	 */
	void exitId(@NotNull KVQLParser.IdContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Boolean}
	 * labeled alternative in {@link KVQLParser#type_def}.
	 * @param ctx the parse tree
	 */
	void enterBoolean(@NotNull KVQLParser.BooleanContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Boolean}
	 * labeled alternative in {@link KVQLParser#type_def}.
	 * @param ctx the parse tree
	 */
	void exitBoolean(@NotNull KVQLParser.BooleanContext ctx);
	/**
	 * Enter a parse tree produced by the {@code JsonPair}
	 * labeled alternative in {@link KVQLParser#jspair}.
	 * @param ctx the parse tree
	 */
	void enterJsonPair(@NotNull KVQLParser.JsonPairContext ctx);
	/**
	 * Exit a parse tree produced by the {@code JsonPair}
	 * labeled alternative in {@link KVQLParser#jspair}.
	 * @param ctx the parse tree
	 */
	void exitJsonPair(@NotNull KVQLParser.JsonPairContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#hints}.
	 * @param ctx the parse tree
	 */
	void enterHints(@NotNull KVQLParser.HintsContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#hints}.
	 * @param ctx the parse tree
	 */
	void exitHints(@NotNull KVQLParser.HintsContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#integer_def}.
	 * @param ctx the parse tree
	 */
	void enterInteger_def(@NotNull KVQLParser.Integer_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#integer_def}.
	 * @param ctx the parse tree
	 */
	void exitInteger_def(@NotNull KVQLParser.Integer_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#array_constructor}.
	 * @param ctx the parse tree
	 */
	void enterArray_constructor(@NotNull KVQLParser.Array_constructorContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#array_constructor}.
	 * @param ctx the parse tree
	 */
	void exitArray_constructor(@NotNull KVQLParser.Array_constructorContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#field_def}.
	 * @param ctx the parse tree
	 */
	void enterField_def(@NotNull KVQLParser.Field_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#field_def}.
	 * @param ctx the parse tree
	 */
	void exitField_def(@NotNull KVQLParser.Field_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#col_alias}.
	 * @param ctx the parse tree
	 */
	void enterCol_alias(@NotNull KVQLParser.Col_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#col_alias}.
	 * @param ctx the parse tree
	 */
	void exitCol_alias(@NotNull KVQLParser.Col_aliasContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Float}
	 * labeled alternative in {@link KVQLParser#type_def}.
	 * @param ctx the parse tree
	 */
	void enterFloat(@NotNull KVQLParser.FloatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Float}
	 * labeled alternative in {@link KVQLParser#type_def}.
	 * @param ctx the parse tree
	 */
	void exitFloat(@NotNull KVQLParser.FloatContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#complex_name_path}.
	 * @param ctx the parse tree
	 */
	void enterComplex_name_path(@NotNull KVQLParser.Complex_name_pathContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#complex_name_path}.
	 * @param ctx the parse tree
	 */
	void exitComplex_name_path(@NotNull KVQLParser.Complex_name_pathContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#fts_path_list}.
	 * @param ctx the parse tree
	 */
	void enterFts_path_list(@NotNull KVQLParser.Fts_path_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#fts_path_list}.
	 * @param ctx the parse tree
	 */
	void exitFts_path_list(@NotNull KVQLParser.Fts_path_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#storage_size}.
	 * @param ctx the parse tree
	 */
	void enterStorage_size(@NotNull KVQLParser.Storage_sizeContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#storage_size}.
	 * @param ctx the parse tree
	 */
	void exitStorage_size(@NotNull KVQLParser.Storage_sizeContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#tab_alias}.
	 * @param ctx the parse tree
	 */
	void enterTab_alias(@NotNull KVQLParser.Tab_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#tab_alias}.
	 * @param ctx the parse tree
	 */
	void exitTab_alias(@NotNull KVQLParser.Tab_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#drop_field_statement}.
	 * @param ctx the parse tree
	 */
	void enterDrop_field_statement(@NotNull KVQLParser.Drop_field_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#drop_field_statement}.
	 * @param ctx the parse tree
	 */
	void exitDrop_field_statement(@NotNull KVQLParser.Drop_field_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#var_decl}.
	 * @param ctx the parse tree
	 */
	void enterVar_decl(@NotNull KVQLParser.Var_declContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#var_decl}.
	 * @param ctx the parse tree
	 */
	void exitVar_decl(@NotNull KVQLParser.Var_declContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Map}
	 * labeled alternative in {@link KVQLParser#type_def}.
	 * @param ctx the parse tree
	 */
	void enterMap(@NotNull KVQLParser.MapContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Map}
	 * labeled alternative in {@link KVQLParser#type_def}.
	 * @param ctx the parse tree
	 */
	void exitMap(@NotNull KVQLParser.MapContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#orderby_clause}.
	 * @param ctx the parse tree
	 */
	void enterOrderby_clause(@NotNull KVQLParser.Orderby_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#orderby_clause}.
	 * @param ctx the parse tree
	 */
	void exitOrderby_clause(@NotNull KVQLParser.Orderby_clauseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code EmptyJsonObject}
	 * labeled alternative in {@link KVQLParser#jsobject}.
	 * @param ctx the parse tree
	 */
	void enterEmptyJsonObject(@NotNull KVQLParser.EmptyJsonObjectContext ctx);
	/**
	 * Exit a parse tree produced by the {@code EmptyJsonObject}
	 * labeled alternative in {@link KVQLParser#jsobject}.
	 * @param ctx the parse tree
	 */
	void exitEmptyJsonObject(@NotNull KVQLParser.EmptyJsonObjectContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#object}.
	 * @param ctx the parse tree
	 */
	void enterObject(@NotNull KVQLParser.ObjectContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#object}.
	 * @param ctx the parse tree
	 */
	void exitObject(@NotNull KVQLParser.ObjectContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#map_def}.
	 * @param ctx the parse tree
	 */
	void enterMap_def(@NotNull KVQLParser.Map_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#map_def}.
	 * @param ctx the parse tree
	 */
	void exitMap_def(@NotNull KVQLParser.Map_defContext ctx);
	/**
	 * Enter a parse tree produced by the {@code EmptyJsonArray}
	 * labeled alternative in {@link KVQLParser#jsarray}.
	 * @param ctx the parse tree
	 */
	void enterEmptyJsonArray(@NotNull KVQLParser.EmptyJsonArrayContext ctx);
	/**
	 * Exit a parse tree produced by the {@code EmptyJsonArray}
	 * labeled alternative in {@link KVQLParser#jsarray}.
	 * @param ctx the parse tree
	 */
	void exitEmptyJsonArray(@NotNull KVQLParser.EmptyJsonArrayContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StringT}
	 * labeled alternative in {@link KVQLParser#type_def}.
	 * @param ctx the parse tree
	 */
	void enterStringT(@NotNull KVQLParser.StringTContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StringT}
	 * labeled alternative in {@link KVQLParser#type_def}.
	 * @param ctx the parse tree
	 */
	void exitStringT(@NotNull KVQLParser.StringTContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#reset_password_clause}.
	 * @param ctx the parse tree
	 */
	void enterReset_password_clause(@NotNull KVQLParser.Reset_password_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#reset_password_clause}.
	 * @param ctx the parse tree
	 */
	void exitReset_password_clause(@NotNull KVQLParser.Reset_password_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#binary_def}.
	 * @param ctx the parse tree
	 */
	void enterBinary_def(@NotNull KVQLParser.Binary_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#binary_def}.
	 * @param ctx the parse tree
	 */
	void exitBinary_def(@NotNull KVQLParser.Binary_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#es_properties}.
	 * @param ctx the parse tree
	 */
	void enterEs_properties(@NotNull KVQLParser.Es_propertiesContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#es_properties}.
	 * @param ctx the parse tree
	 */
	void exitEs_properties(@NotNull KVQLParser.Es_propertiesContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#key_def}.
	 * @param ctx the parse tree
	 */
	void enterKey_def(@NotNull KVQLParser.Key_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#key_def}.
	 * @param ctx the parse tree
	 */
	void exitKey_def(@NotNull KVQLParser.Key_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#enum_def}.
	 * @param ctx the parse tree
	 */
	void enterEnum_def(@NotNull KVQLParser.Enum_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#enum_def}.
	 * @param ctx the parse tree
	 */
	void exitEnum_def(@NotNull KVQLParser.Enum_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#alter_def}.
	 * @param ctx the parse tree
	 */
	void enterAlter_def(@NotNull KVQLParser.Alter_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#alter_def}.
	 * @param ctx the parse tree
	 */
	void exitAlter_def(@NotNull KVQLParser.Alter_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#ttl_def}.
	 * @param ctx the parse tree
	 */
	void enterTtl_def(@NotNull KVQLParser.Ttl_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#ttl_def}.
	 * @param ctx the parse tree
	 */
	void exitTtl_def(@NotNull KVQLParser.Ttl_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#slice_step}.
	 * @param ctx the parse tree
	 */
	void enterSlice_step(@NotNull KVQLParser.Slice_stepContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#slice_step}.
	 * @param ctx the parse tree
	 */
	void exitSlice_step(@NotNull KVQLParser.Slice_stepContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#es_property_assignment}.
	 * @param ctx the parse tree
	 */
	void enterEs_property_assignment(@NotNull KVQLParser.Es_property_assignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#es_property_assignment}.
	 * @param ctx the parse tree
	 */
	void exitEs_property_assignment(@NotNull KVQLParser.Es_property_assignmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#create_user_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_user_statement(@NotNull KVQLParser.Create_user_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#create_user_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_user_statement(@NotNull KVQLParser.Create_user_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#alter_field_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_field_statement(@NotNull KVQLParser.Alter_field_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#alter_field_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_field_statement(@NotNull KVQLParser.Alter_field_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#revoke_system_privileges}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_system_privileges(@NotNull KVQLParser.Revoke_system_privilegesContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#revoke_system_privileges}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_system_privileges(@NotNull KVQLParser.Revoke_system_privilegesContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#index_name}.
	 * @param ctx the parse tree
	 */
	void enterIndex_name(@NotNull KVQLParser.Index_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#index_name}.
	 * @param ctx the parse tree
	 */
	void exitIndex_name(@NotNull KVQLParser.Index_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#var_name}.
	 * @param ctx the parse tree
	 */
	void enterVar_name(@NotNull KVQLParser.Var_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#var_name}.
	 * @param ctx the parse tree
	 */
	void exitVar_name(@NotNull KVQLParser.Var_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#var_ref}.
	 * @param ctx the parse tree
	 */
	void enterVar_ref(@NotNull KVQLParser.Var_refContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#var_ref}.
	 * @param ctx the parse tree
	 */
	void exitVar_ref(@NotNull KVQLParser.Var_refContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#drop_table_statement}.
	 * @param ctx the parse tree
	 */
	void enterDrop_table_statement(@NotNull KVQLParser.Drop_table_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#drop_table_statement}.
	 * @param ctx the parse tree
	 */
	void exitDrop_table_statement(@NotNull KVQLParser.Drop_table_statementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code JsonAtom}
	 * labeled alternative in {@link KVQLParser#jsvalue}.
	 * @param ctx the parse tree
	 */
	void enterJsonAtom(@NotNull KVQLParser.JsonAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code JsonAtom}
	 * labeled alternative in {@link KVQLParser#jsvalue}.
	 * @param ctx the parse tree
	 */
	void exitJsonAtom(@NotNull KVQLParser.JsonAtomContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#multiply_expr}.
	 * @param ctx the parse tree
	 */
	void enterMultiply_expr(@NotNull KVQLParser.Multiply_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#multiply_expr}.
	 * @param ctx the parse tree
	 */
	void exitMultiply_expr(@NotNull KVQLParser.Multiply_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#default_value}.
	 * @param ctx the parse tree
	 */
	void enterDefault_value(@NotNull KVQLParser.Default_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#default_value}.
	 * @param ctx the parse tree
	 */
	void exitDefault_value(@NotNull KVQLParser.Default_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#table_def}.
	 * @param ctx the parse tree
	 */
	void enterTable_def(@NotNull KVQLParser.Table_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#table_def}.
	 * @param ctx the parse tree
	 */
	void exitTable_def(@NotNull KVQLParser.Table_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#id_list_with_size}.
	 * @param ctx the parse tree
	 */
	void enterId_list_with_size(@NotNull KVQLParser.Id_list_with_sizeContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#id_list_with_size}.
	 * @param ctx the parse tree
	 */
	void exitId_list_with_size(@NotNull KVQLParser.Id_list_with_sizeContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#id_with_size}.
	 * @param ctx the parse tree
	 */
	void enterId_with_size(@NotNull KVQLParser.Id_with_sizeContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#id_with_size}.
	 * @param ctx the parse tree
	 */
	void exitId_with_size(@NotNull KVQLParser.Id_with_sizeContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#keyof_expr}.
	 * @param ctx the parse tree
	 */
	void enterKeyof_expr(@NotNull KVQLParser.Keyof_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#keyof_expr}.
	 * @param ctx the parse tree
	 */
	void exitKeyof_expr(@NotNull KVQLParser.Keyof_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#select_clause}.
	 * @param ctx the parse tree
	 */
	void enterSelect_clause(@NotNull KVQLParser.Select_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#select_clause}.
	 * @param ctx the parse tree
	 */
	void exitSelect_clause(@NotNull KVQLParser.Select_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#drop_index_statement}.
	 * @param ctx the parse tree
	 */
	void enterDrop_index_statement(@NotNull KVQLParser.Drop_index_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#drop_index_statement}.
	 * @param ctx the parse tree
	 */
	void exitDrop_index_statement(@NotNull KVQLParser.Drop_index_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#from_clause}.
	 * @param ctx the parse tree
	 */
	void enterFrom_clause(@NotNull KVQLParser.From_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#from_clause}.
	 * @param ctx the parse tree
	 */
	void exitFrom_clause(@NotNull KVQLParser.From_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void enterWhere_clause(@NotNull KVQLParser.Where_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#where_clause}.
	 * @param ctx the parse tree
	 */
	void exitWhere_clause(@NotNull KVQLParser.Where_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#add_expr}.
	 * @param ctx the parse tree
	 */
	void enterAdd_expr(@NotNull KVQLParser.Add_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#add_expr}.
	 * @param ctx the parse tree
	 */
	void exitAdd_expr(@NotNull KVQLParser.Add_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#not_null}.
	 * @param ctx the parse tree
	 */
	void enterNot_null(@NotNull KVQLParser.Not_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#not_null}.
	 * @param ctx the parse tree
	 */
	void exitNot_null(@NotNull KVQLParser.Not_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#show_statement}.
	 * @param ctx the parse tree
	 */
	void enterShow_statement(@NotNull KVQLParser.Show_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#show_statement}.
	 * @param ctx the parse tree
	 */
	void exitShow_statement(@NotNull KVQLParser.Show_statementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code JsonObject}
	 * labeled alternative in {@link KVQLParser#jsobject}.
	 * @param ctx the parse tree
	 */
	void enterJsonObject(@NotNull KVQLParser.JsonObjectContext ctx);
	/**
	 * Exit a parse tree produced by the {@code JsonObject}
	 * labeled alternative in {@link KVQLParser#jsobject}.
	 * @param ctx the parse tree
	 */
	void exitJsonObject(@NotNull KVQLParser.JsonObjectContext ctx);
	/**
	 * Enter a parse tree produced by the {@code JsonArrayValue}
	 * labeled alternative in {@link KVQLParser#jsvalue}.
	 * @param ctx the parse tree
	 */
	void enterJsonArrayValue(@NotNull KVQLParser.JsonArrayValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code JsonArrayValue}
	 * labeled alternative in {@link KVQLParser#jsvalue}.
	 * @param ctx the parse tree
	 */
	void exitJsonArrayValue(@NotNull KVQLParser.JsonArrayValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#grant_roles}.
	 * @param ctx the parse tree
	 */
	void enterGrant_roles(@NotNull KVQLParser.Grant_rolesContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#grant_roles}.
	 * @param ctx the parse tree
	 */
	void exitGrant_roles(@NotNull KVQLParser.Grant_rolesContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#comp_expr}.
	 * @param ctx the parse tree
	 */
	void enterComp_expr(@NotNull KVQLParser.Comp_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#comp_expr}.
	 * @param ctx the parse tree
	 */
	void exitComp_expr(@NotNull KVQLParser.Comp_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#sfw_expr}.
	 * @param ctx the parse tree
	 */
	void enterSfw_expr(@NotNull KVQLParser.Sfw_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#sfw_expr}.
	 * @param ctx the parse tree
	 */
	void exitSfw_expr(@NotNull KVQLParser.Sfw_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#func_call}.
	 * @param ctx the parse tree
	 */
	void enterFunc_call(@NotNull KVQLParser.Func_callContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#func_call}.
	 * @param ctx the parse tree
	 */
	void exitFunc_call(@NotNull KVQLParser.Func_callContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#float_def}.
	 * @param ctx the parse tree
	 */
	void enterFloat_def(@NotNull KVQLParser.Float_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#float_def}.
	 * @param ctx the parse tree
	 */
	void exitFloat_def(@NotNull KVQLParser.Float_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#field_step}.
	 * @param ctx the parse tree
	 */
	void enterField_step(@NotNull KVQLParser.Field_stepContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#field_step}.
	 * @param ctx the parse tree
	 */
	void exitField_step(@NotNull KVQLParser.Field_stepContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#add_field_statement}.
	 * @param ctx the parse tree
	 */
	void enterAdd_field_statement(@NotNull KVQLParser.Add_field_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#add_field_statement}.
	 * @param ctx the parse tree
	 */
	void exitAdd_field_statement(@NotNull KVQLParser.Add_field_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#parenthesized_expr}.
	 * @param ctx the parse tree
	 */
	void enterParenthesized_expr(@NotNull KVQLParser.Parenthesized_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#parenthesized_expr}.
	 * @param ctx the parse tree
	 */
	void exitParenthesized_expr(@NotNull KVQLParser.Parenthesized_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#alter_table_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_table_statement(@NotNull KVQLParser.Alter_table_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#alter_table_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_table_statement(@NotNull KVQLParser.Alter_table_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#grant_statement}.
	 * @param ctx the parse tree
	 */
	void enterGrant_statement(@NotNull KVQLParser.Grant_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#grant_statement}.
	 * @param ctx the parse tree
	 */
	void exitGrant_statement(@NotNull KVQLParser.Grant_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#string_def}.
	 * @param ctx the parse tree
	 */
	void enterString_def(@NotNull KVQLParser.String_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#string_def}.
	 * @param ctx the parse tree
	 */
	void exitString_def(@NotNull KVQLParser.String_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#alter_user_statement}.
	 * @param ctx the parse tree
	 */
	void enterAlter_user_statement(@NotNull KVQLParser.Alter_user_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#alter_user_statement}.
	 * @param ctx the parse tree
	 */
	void exitAlter_user_statement(@NotNull KVQLParser.Alter_user_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#create_user_identified_clause}.
	 * @param ctx the parse tree
	 */
	void enterCreate_user_identified_clause(@NotNull KVQLParser.Create_user_identified_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#create_user_identified_clause}.
	 * @param ctx the parse tree
	 */
	void exitCreate_user_identified_clause(@NotNull KVQLParser.Create_user_identified_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#primary_expr}.
	 * @param ctx the parse tree
	 */
	void enterPrimary_expr(@NotNull KVQLParser.Primary_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#primary_expr}.
	 * @param ctx the parse tree
	 */
	void exitPrimary_expr(@NotNull KVQLParser.Primary_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#revoke_roles}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_roles(@NotNull KVQLParser.Revoke_rolesContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#revoke_roles}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_roles(@NotNull KVQLParser.Revoke_rolesContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#query}.
	 * @param ctx the parse tree
	 */
	void enterQuery(@NotNull KVQLParser.QueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#query}.
	 * @param ctx the parse tree
	 */
	void exitQuery(@NotNull KVQLParser.QueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#column_ref}.
	 * @param ctx the parse tree
	 */
	void enterColumn_ref(@NotNull KVQLParser.Column_refContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#column_ref}.
	 * @param ctx the parse tree
	 */
	void exitColumn_ref(@NotNull KVQLParser.Column_refContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#unary_expr}.
	 * @param ctx the parse tree
	 */
	void enterUnary_expr(@NotNull KVQLParser.Unary_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#unary_expr}.
	 * @param ctx the parse tree
	 */
	void exitUnary_expr(@NotNull KVQLParser.Unary_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#grant_system_privileges}.
	 * @param ctx the parse tree
	 */
	void enterGrant_system_privileges(@NotNull KVQLParser.Grant_system_privilegesContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#grant_system_privileges}.
	 * @param ctx the parse tree
	 */
	void exitGrant_system_privileges(@NotNull KVQLParser.Grant_system_privilegesContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#by_password}.
	 * @param ctx the parse tree
	 */
	void enterBy_password(@NotNull KVQLParser.By_passwordContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#by_password}.
	 * @param ctx the parse tree
	 */
	void exitBy_password(@NotNull KVQLParser.By_passwordContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#comp_op}.
	 * @param ctx the parse tree
	 */
	void enterComp_op(@NotNull KVQLParser.Comp_opContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#comp_op}.
	 * @param ctx the parse tree
	 */
	void exitComp_op(@NotNull KVQLParser.Comp_opContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#hint}.
	 * @param ctx the parse tree
	 */
	void enterHint(@NotNull KVQLParser.HintContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#hint}.
	 * @param ctx the parse tree
	 */
	void exitHint(@NotNull KVQLParser.HintContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#path_expr}.
	 * @param ctx the parse tree
	 */
	void enterPath_expr(@NotNull KVQLParser.Path_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#path_expr}.
	 * @param ctx the parse tree
	 */
	void exitPath_expr(@NotNull KVQLParser.Path_exprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Binary}
	 * labeled alternative in {@link KVQLParser#type_def}.
	 * @param ctx the parse tree
	 */
	void enterBinary(@NotNull KVQLParser.BinaryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Binary}
	 * labeled alternative in {@link KVQLParser#type_def}.
	 * @param ctx the parse tree
	 */
	void exitBinary(@NotNull KVQLParser.BinaryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ArrayOfJsonValues}
	 * labeled alternative in {@link KVQLParser#jsarray}.
	 * @param ctx the parse tree
	 */
	void enterArrayOfJsonValues(@NotNull KVQLParser.ArrayOfJsonValuesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ArrayOfJsonValues}
	 * labeled alternative in {@link KVQLParser#jsarray}.
	 * @param ctx the parse tree
	 */
	void exitArrayOfJsonValues(@NotNull KVQLParser.ArrayOfJsonValuesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code JsonObjectValue}
	 * labeled alternative in {@link KVQLParser#jsvalue}.
	 * @param ctx the parse tree
	 */
	void enterJsonObjectValue(@NotNull KVQLParser.JsonObjectValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code JsonObjectValue}
	 * labeled alternative in {@link KVQLParser#jsvalue}.
	 * @param ctx the parse tree
	 */
	void exitJsonObjectValue(@NotNull KVQLParser.JsonObjectValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#id_list}.
	 * @param ctx the parse tree
	 */
	void enterId_list(@NotNull KVQLParser.Id_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#id_list}.
	 * @param ctx the parse tree
	 */
	void exitId_list(@NotNull KVQLParser.Id_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#drop_user_statement}.
	 * @param ctx the parse tree
	 */
	void enterDrop_user_statement(@NotNull KVQLParser.Drop_user_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#drop_user_statement}.
	 * @param ctx the parse tree
	 */
	void exitDrop_user_statement(@NotNull KVQLParser.Drop_user_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#identifier_or_string}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier_or_string(@NotNull KVQLParser.Identifier_or_stringContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#identifier_or_string}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier_or_string(@NotNull KVQLParser.Identifier_or_stringContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#create_table_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_table_statement(@NotNull KVQLParser.Create_table_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#create_table_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_table_statement(@NotNull KVQLParser.Create_table_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#string}.
	 * @param ctx the parse tree
	 */
	void enterString(@NotNull KVQLParser.StringContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#string}.
	 * @param ctx the parse tree
	 */
	void exitString(@NotNull KVQLParser.StringContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#record_def}.
	 * @param ctx the parse tree
	 */
	void enterRecord_def(@NotNull KVQLParser.Record_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#record_def}.
	 * @param ctx the parse tree
	 */
	void exitRecord_def(@NotNull KVQLParser.Record_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#create_index_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_index_statement(@NotNull KVQLParser.Create_index_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#create_index_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_index_statement(@NotNull KVQLParser.Create_index_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#filter_step}.
	 * @param ctx the parse tree
	 */
	void enterFilter_step(@NotNull KVQLParser.Filter_stepContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#filter_step}.
	 * @param ctx the parse tree
	 */
	void exitFilter_step(@NotNull KVQLParser.Filter_stepContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#boolean_def}.
	 * @param ctx the parse tree
	 */
	void enterBoolean_def(@NotNull KVQLParser.Boolean_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#boolean_def}.
	 * @param ctx the parse tree
	 */
	void exitBoolean_def(@NotNull KVQLParser.Boolean_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#const_expr}.
	 * @param ctx the parse tree
	 */
	void enterConst_expr(@NotNull KVQLParser.Const_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#const_expr}.
	 * @param ctx the parse tree
	 */
	void exitConst_expr(@NotNull KVQLParser.Const_exprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Int}
	 * labeled alternative in {@link KVQLParser#type_def}.
	 * @param ctx the parse tree
	 */
	void enterInt(@NotNull KVQLParser.IntContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Int}
	 * labeled alternative in {@link KVQLParser#type_def}.
	 * @param ctx the parse tree
	 */
	void exitInt(@NotNull KVQLParser.IntContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#priv_item}.
	 * @param ctx the parse tree
	 */
	void enterPriv_item(@NotNull KVQLParser.Priv_itemContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#priv_item}.
	 * @param ctx the parse tree
	 */
	void exitPriv_item(@NotNull KVQLParser.Priv_itemContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#name_path}.
	 * @param ctx the parse tree
	 */
	void enterName_path(@NotNull KVQLParser.Name_pathContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#name_path}.
	 * @param ctx the parse tree
	 */
	void exitName_path(@NotNull KVQLParser.Name_pathContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#principal}.
	 * @param ctx the parse tree
	 */
	void enterPrincipal(@NotNull KVQLParser.PrincipalContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#principal}.
	 * @param ctx the parse tree
	 */
	void exitPrincipal(@NotNull KVQLParser.PrincipalContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#duration}.
	 * @param ctx the parse tree
	 */
	void enterDuration(@NotNull KVQLParser.DurationContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#duration}.
	 * @param ctx the parse tree
	 */
	void exitDuration(@NotNull KVQLParser.DurationContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#obj_priv_list}.
	 * @param ctx the parse tree
	 */
	void enterObj_priv_list(@NotNull KVQLParser.Obj_priv_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#obj_priv_list}.
	 * @param ctx the parse tree
	 */
	void exitObj_priv_list(@NotNull KVQLParser.Obj_priv_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(@NotNull KVQLParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(@NotNull KVQLParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#any_op}.
	 * @param ctx the parse tree
	 */
	void enterAny_op(@NotNull KVQLParser.Any_opContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#any_op}.
	 * @param ctx the parse tree
	 */
	void exitAny_op(@NotNull KVQLParser.Any_opContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#modify_field_statement}.
	 * @param ctx the parse tree
	 */
	void enterModify_field_statement(@NotNull KVQLParser.Modify_field_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#modify_field_statement}.
	 * @param ctx the parse tree
	 */
	void exitModify_field_statement(@NotNull KVQLParser.Modify_field_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#json}.
	 * @param ctx the parse tree
	 */
	void enterJson(@NotNull KVQLParser.JsonContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#json}.
	 * @param ctx the parse tree
	 */
	void exitJson(@NotNull KVQLParser.JsonContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(@NotNull KVQLParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(@NotNull KVQLParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Record}
	 * labeled alternative in {@link KVQLParser#type_def}.
	 * @param ctx the parse tree
	 */
	void enterRecord(@NotNull KVQLParser.RecordContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Record}
	 * labeled alternative in {@link KVQLParser#type_def}.
	 * @param ctx the parse tree
	 */
	void exitRecord(@NotNull KVQLParser.RecordContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#shard_key_def}.
	 * @param ctx the parse tree
	 */
	void enterShard_key_def(@NotNull KVQLParser.Shard_key_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#shard_key_def}.
	 * @param ctx the parse tree
	 */
	void exitShard_key_def(@NotNull KVQLParser.Shard_key_defContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#identified_clause}.
	 * @param ctx the parse tree
	 */
	void enterIdentified_clause(@NotNull KVQLParser.Identified_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#identified_clause}.
	 * @param ctx the parse tree
	 */
	void exitIdentified_clause(@NotNull KVQLParser.Identified_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#grant_object_privileges}.
	 * @param ctx the parse tree
	 */
	void enterGrant_object_privileges(@NotNull KVQLParser.Grant_object_privilegesContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#grant_object_privileges}.
	 * @param ctx the parse tree
	 */
	void exitGrant_object_privileges(@NotNull KVQLParser.Grant_object_privilegesContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#create_role_statement}.
	 * @param ctx the parse tree
	 */
	void enterCreate_role_statement(@NotNull KVQLParser.Create_role_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#create_role_statement}.
	 * @param ctx the parse tree
	 */
	void exitCreate_role_statement(@NotNull KVQLParser.Create_role_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#parse}.
	 * @param ctx the parse tree
	 */
	void enterParse(@NotNull KVQLParser.ParseContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#parse}.
	 * @param ctx the parse tree
	 */
	void exitParse(@NotNull KVQLParser.ParseContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#describe_statement}.
	 * @param ctx the parse tree
	 */
	void enterDescribe_statement(@NotNull KVQLParser.Describe_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#describe_statement}.
	 * @param ctx the parse tree
	 */
	void exitDescribe_statement(@NotNull KVQLParser.Describe_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#prolog}.
	 * @param ctx the parse tree
	 */
	void enterProlog(@NotNull KVQLParser.PrologContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#prolog}.
	 * @param ctx the parse tree
	 */
	void exitProlog(@NotNull KVQLParser.PrologContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Array}
	 * labeled alternative in {@link KVQLParser#type_def}.
	 * @param ctx the parse tree
	 */
	void enterArray(@NotNull KVQLParser.ArrayContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Array}
	 * labeled alternative in {@link KVQLParser#type_def}.
	 * @param ctx the parse tree
	 */
	void exitArray(@NotNull KVQLParser.ArrayContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#sort_spec}.
	 * @param ctx the parse tree
	 */
	void enterSort_spec(@NotNull KVQLParser.Sort_specContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#sort_spec}.
	 * @param ctx the parse tree
	 */
	void exitSort_spec(@NotNull KVQLParser.Sort_specContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#revoke_statement}.
	 * @param ctx the parse tree
	 */
	void enterRevoke_statement(@NotNull KVQLParser.Revoke_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#revoke_statement}.
	 * @param ctx the parse tree
	 */
	void exitRevoke_statement(@NotNull KVQLParser.Revoke_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#account_lock}.
	 * @param ctx the parse tree
	 */
	void enterAccount_lock(@NotNull KVQLParser.Account_lockContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#account_lock}.
	 * @param ctx the parse tree
	 */
	void exitAccount_lock(@NotNull KVQLParser.Account_lockContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#comment}.
	 * @param ctx the parse tree
	 */
	void enterComment(@NotNull KVQLParser.CommentContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#comment}.
	 * @param ctx the parse tree
	 */
	void exitComment(@NotNull KVQLParser.CommentContext ctx);
	/**
	 * Enter a parse tree produced by {@link KVQLParser#default_def}.
	 * @param ctx the parse tree
	 */
	void enterDefault_def(@NotNull KVQLParser.Default_defContext ctx);
	/**
	 * Exit a parse tree produced by {@link KVQLParser#default_def}.
	 * @param ctx the parse tree
	 */
	void exitDefault_def(@NotNull KVQLParser.Default_defContext ctx);
}