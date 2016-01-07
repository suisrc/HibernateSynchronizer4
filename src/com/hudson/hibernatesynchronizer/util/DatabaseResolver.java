/**
 * This software is licensed under the general public license.  See http://www.gnu.org/copyleft/gpl.html
 * for more information.
 */
package com.hudson.hibernatesynchronizer.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto: joe@binamics.com">Joe Hudson </a>
 */
public class DatabaseResolver {

    private static Map values = new HashMap(20);

    private static DatabaseResolver instance = new DatabaseResolver();

    static {
        values.put("DB2", "org.hibernate.dialect.DB2Dialect");
        values.put("MySQL", "org.hibernate.dialect.MySQLDialect");
        values.put("SAP DB", "org.hibernate.dialect.SAPDBDialect");
        values.put("Oracle (any version)",
                "org.hibernate.dialect.OracleDialect");
        values.put("Oracle 9", "org.hibernate.dialect.Oracle9Dialect");
        values.put("Sybase", "org.hibernate.dialect.SybaseDialect");
        values.put("Sybase Anywhere",
                "org.hibernate.dialect.SybaseAnywhereDialect");
        values.put("Progress", "org.hibernate.dialect.ProgressDialect");
        values.put("Mckoi SQL", "org.hibernate.dialect.MckoiDialect");
        values.put("Interbase", "org.hibernate.dialect.InterbaseDialect");
        values.put("Pointbase", "org.hibernate.dialect.PointbaseDialect");
        values.put("PostgreSQL", "org.hibernate.dialect.PostgreSQLDialect");
        values.put("HypersonicSQL", "org.hibernate.dialect.HSQLDialect");
        values.put("Microsoft SQL Server",
                "org.hibernate.dialect.SQLServerDialect");
        values.put("Ingres", "org.hibernate.dialect.IngresDialect");
        values.put("Informix", "org.hibernate.dialect.InformixDialect");
        values.put("FrontBase", "org.hibernate.dialect.FrontbaseDialect");
    }

    public static DatabaseResolver getInstance() {
        return instance;
    }

    public static List getDatabaseNames() {
        List databaseNames = new ArrayList(values.size());
        for (Iterator i = values.keySet().iterator(); i.hasNext();) {
            databaseNames.add(i.next());
        }
        Collections.sort(databaseNames);
        return databaseNames;
    }

    public String resolve(String databaseName) {
        return (String) values.get(databaseName);
    }
}