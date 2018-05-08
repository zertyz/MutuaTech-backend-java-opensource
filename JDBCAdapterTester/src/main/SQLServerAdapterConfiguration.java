package main;

import java.sql.SQLException;

import adapters.JDBCAdapter;
import adapters.SQLServerAdapter;

/** <pre>
 * SQLServerAdapterConfiguration.java  --  $Id: SQLServerHelperConfiguration.java,v 1.1 2010/07/01 22:03:06 luiz Exp $
 * ==================================
 * (created by luiz, Jun 29, 2010)
 *
 * Provides the needed 'SQLServerAdapter' configuration to access and operate on the database
 */

public class SQLServerAdapterConfiguration /*extends SQLServerAdapter */ { /*
	
	
	// SQLServerAdapter section
	///////////////////////////
	
	static {
		JDBCAdapter.SHOULD_DEBUG_QUERIES = true;
	}

	private SQLServerAdapterConfiguration() throws SQLException {
		super(Configuration.log);
	}

	@Override
	protected String[] getCredentials() {
		String hostname     = "201.20.9.173";
		String port         = "1433";
		String databaseName = "";
		String user         = "wmobile";
		String password     = "w@g3m0b1l3";
		return new String[] {hostname, port, databaseName, user, password};
	}

	@Override
	protected String[][] getTableDefinitions() {
		return null;
	}
	
	
	// public access methods
	////////////////////////
	
	public static SQLServerAdapter getDBAdapter() throws SQLException {
		return new SQLServerAdapterConfiguration(new String[][] {
				{"GetVeiculos",        "SELECT [renaro].[dbo].[tbl_ren_linha].[nome] as categoria," +
                                       "[renaro].[dbo].[tbl_ren_modelo].[nome] as modelo," +
                                       "[renaro].[dbo].[tbl_ren_submodelo].nome as submodelo " +
                                       "FROM [renaro].[dbo].[tbl_ren_linha] INNER JOIN [renaro].[dbo].[tbl_ren_modelo] " +
                                       "ON [renaro].[dbo].[tbl_ren_modelo].linha_fk = [renaro].[dbo].[tbl_ren_linha].linha_pk " +
                                       "INNER JOIN [renaro].[dbo].[tbl_ren_submodelo] ON [renaro].[dbo].[tbl_ren_modelo].modelo_pk = [renaro].[dbo].[tbl_ren_submodelo].modelo_fk"},
                {"GetCaracteristicas", "SELECT [renaro].[dbo].[tbl_ren_modelo].[nome] as modelo," +
                                       "[renaro].[dbo].[tbl_ren_submodelo].nome as submodelo," +
                                       "[renaro].[dbo].[tbl_ren_feature].area as tipo_caracteristica," +
                                       "[renaro].[dbo].[tbl_ren_feature].nome_resumido as caracteristica " +
                                       "FROM [renaro].[dbo].[tbl_ren_modelo] " +
                                       "INNER JOIN [renaro].[dbo].[tbl_ren_submodelo] ON [renaro].[dbo].[tbl_ren_modelo].modelo_pk = [renaro].[dbo].[tbl_ren_submodelo].modelo_fk " +
                                       "INNER JOIN [renaro].[dbo].[tbl_ren_submodelo_feature] ON [renaro].[dbo].[tbl_ren_submodelo].submodelo_pk = [renaro].[dbo].[tbl_ren_submodelo_feature].submodelo_fk " +
                                       "INNER JOIN [renaro].[dbo].[tbl_ren_feature] ON [renaro].[dbo].[tbl_ren_submodelo_feature].feature_fk = [renaro].[dbo].[tbl_ren_feature].feature_pk"},
                {"GetFichas",   "SELECT [renaro].[dbo].[tbl_ren_modelo].[nome] as modelo," +
                                "[renaro].[dbo].[tbl_ren_submodelo].nome as submodelo," +
                                "[renaro].[dbo].[tbl_ren_ficha_tecnica].area as tipo_medida," +
                                "[renaro].[dbo].[tbl_ren_ficha_tecnica].nome as nome_medida," +
                                "[renaro].[dbo].[tbl_ren_ficha_tecnica].dado as valor_medida " +
                                "FROM [renaro].[dbo].[tbl_ren_modelo] " +
                                "INNER JOIN [renaro].[dbo].[tbl_ren_submodelo] ON [renaro].[dbo].[tbl_ren_modelo].modelo_pk = [renaro].[dbo].[tbl_ren_submodelo].modelo_fk " +
                                "INNER JOIN [renaro].[dbo].[tbl_ren_submodelo_ficha] ON [renaro].[dbo].[tbl_ren_submodelo].submodelo_pk = [renaro].[dbo].[tbl_ren_submodelo_ficha].submodelo_fk " +
                                "INNER JOIN [renaro].[dbo].[tbl_ren_ficha_tecnica] ON [renaro].[dbo].[tbl_ren_submodelo_ficha].ficha_fk = [renaro].[dbo].[tbl_ren_ficha_tecnica].ficha_pk"},
                {"GetLojas",  "SELECT [FRIENDLY_PK], [NOME], [ENDERECO], [BAIRRO], [TEL1], [FAX], " +
                              "[CIDADE], [UF], [CEP] FROM [renaro2009].[dbo].[tbl_loj_empresa]"},
		});
	}

*/}