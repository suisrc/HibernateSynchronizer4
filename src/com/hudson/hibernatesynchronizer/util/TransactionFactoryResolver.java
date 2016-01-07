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
public class TransactionFactoryResolver {

    private static Map values = new HashMap(20);

    private static TransactionFactoryResolver instance = new TransactionFactoryResolver();

    static {
        values.put("JBoss",
                "org.hibernate.transaction.JBossTransactionManagerLookup");
        values
                .put("Weblogic",
                        "org.hibernate.transaction.WeblogicTransactionManagerLookup");
        values
                .put("WebSphere",
                        "org.hibernate.transaction.WebSphereTransactionManagerLookup");
        values.put("Orion",
                "org.hibernate.transaction.OrionTransactionManagerLookup");
        values.put("Resin",
                "org.hibernate.transaction.ResinTransactionManagerLookup");
        values.put("JOTM",
                "org.hibernate.transaction.JOTMTransactionManagerLookup");
        values.put("JOnAS",
                "org.hibernate.transaction.JOnASTransactionManagerLookup");
        values.put("JRun4",
                "org.hibernate.transaction.JRun4TransactionManagerLookup");
    }

    public static TransactionFactoryResolver getInstance() {
        return instance;
    }

    public static List getApplicationServers() {
        List databaseNames = new ArrayList(values.size());
        for (Iterator i = values.keySet().iterator(); i.hasNext();) {
            databaseNames.add(i.next());
        }
        Collections.sort(databaseNames);
        return databaseNames;
    }

    public String resolve(String appServer) {
        if (null == appServer)
            return null;
        else if (appServer.equals("N/A"))
            return "org.hibernate.transaction.JDBCTransactionFactory";
        return (String) values.get(appServer);
    }
}