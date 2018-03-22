package cz.vsb.cs.neurace.server;

import cz.vsb.cs.neurace.gui.Text;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * Přihlašování přes LDAP
 */
public class Login {

    /**
     * Přihlásí uživatele přes ldap.vsb.cz. Komunikace je je šifrovaná SSL.
     * @param login login
     * @param password heslo
     * @param messages pole pro odpověď LDAP serveru
     * @return true, pokud bylo přihlášení úspěšné
     */
    public static boolean login(String login, String password, ArrayList<String> messages) {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldaps://ldap.vsb.cz/");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");

        InitialDirContext ctx = null;
        InitialDirContext ctx2 = null;
        try {
            ctx = new InitialDirContext(env);
            SearchControls ctls = new SearchControls();
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String filter = "cn=" + login;
            NamingEnumeration<SearchResult> answer = ctx.search("ou=USERS,o=VSB", filter, ctls);
            if(answer.hasMore()){
                SearchResult sr = answer.next();
                String name = sr.getName();
                env.put(Context.SECURITY_CREDENTIALS, password);
                env.put(Context.SECURITY_PRINCIPAL, name + ",ou=USERS,o=VSB");
                ctx2 = new InitialDirContext(env);
                return true;
            } else {
                messages.add(Text.getString("user") +" "+ login +" "+ Text.getString("not_found"));
            }

        } catch (AuthenticationException e) {
            messages.add(Text.getString("incorrect"));
        } catch (NamingException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            messages.add(Text.getString("ldap_error") + sw.toString().replace("\n", "</ br>"));
            System.err.println(e.getCause());
            //e.printStackTrace();
        } finally {
            if(ctx != null){
                try {
                   ctx.close();
                } catch (NamingException e) {
                    System.err.println(e.getCause());
                    //e.printStackTrace();
                }
            }
            if(ctx2 != null){
                try {
                   ctx2.close();
                } catch (NamingException e) {
                    System.err.println(e.getCause());
                    //e.printStackTrace();
                }
            }
        }
        return false;
    }

    
    /*public static void main(String args[]) {
        try {

            ArrayList<String> messages = new ArrayList<String>();
            boolean result = login("login", "heslo", messages);
            if(result) {
                System.out.println("OK");
            }
            else {
                for(String msg: messages) {
                    System.out.println(msg);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }*/

}
