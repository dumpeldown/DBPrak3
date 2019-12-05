import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class SQLManager{

    public static boolean isValid(String tab, String bez, int nr){
        Statement stmt;
        ResultSet rs = null;
        String SQL = "Select * from "+tab+" where "+bez+"="+nr;
        try{
            stmt = ConnectionManager.con.createStatement();
            rs = stmt.executeQuery(SQL);
        }catch(SQLException e){
            e.printStackTrace();
        }
        //überprüfen, ob das Resultset Zeilen enthält. wenn ja, ist es eine valide Knr.
        try{
            if(rs.isBeforeFirst()){
                return true;
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Die funktionen schreibt die einzelnen kunde als neue kunden ins System, falls diese dort noch nicht existieren.
     *
     * @param kunden Liste mit String-Arrays, in denen jweils die details eines kunden stehen
     */
    public static void writeKunden(ArrayList<String[]> kunden){
        String SQL;
        int updated = 0, notFully = 0;
        PreparedStatement pstmt = null;
        Statement stmt = null;
        String kname = null, strasse= null, ort= null;
        int plz = 0;
        SQL = "Insert into kunde(kname, plz, ort, strasse) values(?,?,?,?);";
        try{
            pstmt = ConnectionManager.con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
            stmt = ConnectionManager.con.createStatement();
        }catch(SQLException e){
            e.printStackTrace();
        }
        for(String[] a : kunden){
            if(a.length != 4){
                notFully++;
                System.out.println("Bei einem Kunden sind nicht alle erforderlichen Daten angegeben.\n");
                continue;
            }
            try{
                kname = a[0];
                plz = Integer.parseInt(a[1]);
                ort = a[2];
                strasse = a[3];
            }catch(Exception e){
                System.out.println("\n\n[!!]Die KUNDE.CSV Datei scheint korrupt zu sein. Bitte beheben.\n\n");
                System.exit(0);
            }

            try{
                //fragezeichen aus pstmt ersetzen.
                pstmt.setString(1 ,kname);
                pstmt.setInt(2, plz);
                pstmt.setString(3, ort);
                pstmt.setString(4, strasse);

                //überprüfen, ob schon ein Kunde mit dem selben name, plz und strasse im system existiert.
                //isBeforeFirst gibt true zurück, falls das resultset mehr als 0 zeilen hat und der cursor an
                // derersten stelle steht.
                if((stmt.executeQuery("select * from kunde where kname = '"+kname+"' and plz = "+plz+" and strasse = '"+strasse+"';").isBeforeFirst())){
                    System.out.println("Der Kunde \""+a[0]+"\" existiert schon im System.\n");
                    continue;
                }

                //wenn nicht, wird der "insert into" ausgeführt.
                int temp = pstmt.executeUpdate();
                if(temp > 0) updated++;
            }catch(SQLException e){
                System.out.println("[!!] Fehler beim Schreiben: "+e.getMessage());
            }
        }
        try{
            pstmt.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        System.out.println("[!]Erfolgreich "+ updated+" Kundendatensaetze in die Datenbank geschrieben!\n");
    }

    /**
     * einfaches select auf artikel, zeigt alle artikel auf der console an.
     */
    public static void showAllArtikel(){
        Statement stmt;
        ResultSet rs;
        String SQL = "Select * from artikel order by artnr";
        try{
            stmt = ConnectionManager.con.createStatement();
            rs = stmt.executeQuery(SQL);
            while(rs.next()){
                int artnr = rs.getInt("artnr");
                String artbez = rs.getString("artbez");
                String menge = rs.getString("mge");
                double preis = rs.getDouble("preis");
                int steuer = rs.getInt("steu");

                System.out.println("Artikelnummer: " + artnr);
                System.out.println("Artikelnamme: " + artbez);
                System.out.println("Mengenangabe in: " + menge);
                System.out.println("Preis: " + preis);
                System.out.println("Steuer: " + steuer);
                System.out.println("-----------------------");
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * einfaches select und zeigt alle kunden in der DB an.
     */
    public static void showAllKunden(){
        Statement stmt;
        ResultSet rs;
        String SQL = "Select * from Kunde order by knr";
        try{
            stmt = ConnectionManager.con.createStatement();
            rs = stmt.executeQuery(SQL);
            while(rs.next()){
                int knr = rs.getInt("knr");
                String kname = rs.getString("kname");
                int plz = rs.getInt("plz");
                String stadt = rs.getString("ort");
                String str = rs.getString("strasse");

                System.out.println("Kundennummer: "+knr);
                System.out.println("Kundenname: "+ kname);
                System.out.println("PLZ: "+ plz);
                System.out.println("Ort: "+stadt);
                System.out.println("Strasse: "+str);
                System.out.println("-----------------------");
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * einfaches select und zeigt alles lager in DB an.
     */
    public static void showAllLager(){
        Statement stmt;
        ResultSet rs;
        String SQL = "Select * from lager order by lnr";
        try{
            stmt = ConnectionManager.con.createStatement();
            rs = stmt.executeQuery(SQL);
            while(rs.next()){
                int lnr = rs.getInt("lnr");
                String lort = rs.getString("lort");
                int lplz = rs.getInt("lplz");

                System.out.println("Lagernummer: "+lnr);
                System.out.println("Lagerort: "+ lort);
                System.out.println("PLZ: "+ lplz);
                System.out.println("-----------------------");
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * funktion fragt nach user input = lagernummer
     * und gibt alle lagerbestände für diese lagernummer auf der console aus.
     */
    public static void showLagerbestandForArtnr(){
        Statement stmt;
        ResultSet rs;
        Scanner sc = new Scanner(System.in);
        int lnr;
        System.out.print("Gib eine Lagernummmer ein:");
        try{
            lnr = Integer.parseInt(sc.nextLine());
        }catch(NumberFormatException e){
            e.printStackTrace();
            return;
        }
        String SQL = "Select * from lagerbestand where lnr = "+lnr;
        try{
            stmt = ConnectionManager.con.createStatement();
            rs = stmt.executeQuery(SQL);
            if (!rs.isBeforeFirst() ) {
                System.out.println("Keine Daten fuer die Lagernummer gefunden.");
            }
            while(rs.next()){
                int artnr = rs.getInt("artnr");
                String stuecke = rs.getString("stuecke");
                int bstnr = rs.getInt("bstnr");

                System.out.println("Artikelnr.:"+artnr);
                System.out.println("Anzahl der Bestaende: "+stuecke+" mit Bestandsnummer "+ bstnr+".");
                System.out.println("-----------------------");
            }
            System.out.println("\n");
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * funktion erwartet input einer artikelnummer
     * auf die zeile mit dieser artnr in tabelle lagerbestand wird ein update auf wert ausgeführt
     * es wird eingesetzt "wert = artikelpreis * artikelbestand"
     */
    public static void updateWert(){
        Statement stmt;
        int updateArtnr;
        Scanner sc = new Scanner(System.in);
        System.out.print("Gib eine Artikelnummer ein:");
        try{
            updateArtnr = Integer.parseInt(sc.nextLine());
        }catch(NumberFormatException e){
            e.printStackTrace();
            return;
        }
        String SQL = "update lagerbestand set wert = artikel.preis * lagerbestand.stuecke from artikel where artikel" +
                ".artnr ="+updateArtnr+" and artikel.artnr = lagerbestand.artnr";
        try{
            stmt = ConnectionManager.con.createStatement();
            int updatedLines = stmt.executeUpdate(SQL);
            if(updatedLines < 1){
                System.out.println("[!] Update failed.");
            }else{
                System.out.println("Update auf WERT erfolgreich.\n");
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * gibt für eine eingegebene artnr den bestand in allen lagern an.
     */
    public static void showArtikelAndBestand(){
        Statement stmt;
        ResultSet rs;
        int toGet;
        Scanner sc = new Scanner(System.in);
        System.out.print("Gib eine Artikelnummer ein:");
        try{
            toGet = Integer.parseInt(sc.nextLine());
        }catch(NumberFormatException e){
            e.printStackTrace();
            return;
        }
        if(!isValid("artikel", "artnr", toGet)){
            System.out.println("\n[!!]Die Artikelnummer ist nicht gueltig.\n");
            return;
        }
        String SQL = "select artikel.*, lagerbestand.* from artikel, lagerbestand where artikel.artnr = lagerbestand" +
                ".artnr and artikel.artnr ="+toGet;
        try{
            stmt = ConnectionManager.con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY );
            rs = stmt.executeQuery(SQL);
            rs.next();
            int artnr = rs.getInt("artnr");
            String artbez = rs.getString("artbez");
            String menge = rs.getString("mge");
            double preis = rs.getDouble("preis");
            int steuer = rs.getInt("steu");

            System.out.println("Artikelnummer: " + artnr);
            System.out.println("Artikelbezeichnung: " + artbez);
            System.out.println("Mengenangabe in: " + menge);
            System.out.println("Preis: " + preis);
            System.out.println("Steuer: " + steuer);
            System.out.println("-----------------------");
            rs.beforeFirst();
            while(rs.next()){
                int lnr = rs.getInt("lnr");
                int stuecke = rs.getInt("stuecke");
                double wert = rs.getDouble("wert");

                System.out.println("Lagernummer: " + lnr);
                System.out.println("Stuecke: " + stuecke);
                if(wert != 0){
                    System.out.println("Wert: " + wert);
                }else{
                    System.out.println("Wert: \"Noch nicht angegeben, kann durch 6) aktualisiert werden.");
                }
                System.out.println("-----------------------");
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static void writeBestellug(int knr, Datum dat){
        String SQL;
        PreparedStatement pstmt = null;
        Statement stmt = null;
        ResultSet rs;
        SQL = "Insert into bestellung(knr, bestdat, status) values(?,?,1);";
        try{
            pstmt = ConnectionManager.con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        }catch(SQLException e){
            e.printStackTrace();
        }
        try{
            //fragezeichen aus pstmt ersetzen.
            pstmt.setInt(1, knr);
            pstmt.setDate(2, Datum.toSQLDate(dat));

            int temp = pstmt.executeUpdate();
            if(temp <= 0) throw new SQLException("Keine Zeile geupdated.");
        }catch(SQLException e){
            System.out.println("[!!] Fehler beim Schreiben: "+e.getMessage());
        }
    }

    public static int checkAnzahlStuecke(int artnr, int eStuecke){
        Statement stmt;
        ResultSet rs;
        String SQL = "Select * from lagerbestand";
        try{
            stmt = ConnectionManager.con.createStatement();
            rs = stmt.executeQuery(SQL);
            while(rs.next()){
                //beim überprüfen muss die artnr übreinstimmen, der bestand muss genug stücke enthalten und der
                // bestand dard nicht schon einer anderen bestellung zugeordnet sein.
                if(rs.getInt("artnr") == artnr){
                    if(rs.getInt("stuecke")-eStuecke >=0){
                        if(rs.getInt("bestnr") == 0){
                            //bestandsnummer als "key" zurückgeben.
                            System.out.println("Im lagerbstand " + rs.getInt("bstnr") + " sind genug artikel " +
                                    "vorhanden.");
                            return rs.getInt("bstnr");
                        }
                    }
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return -1;
    }

    public static void writeNewLagerbestand(int bstnr, int eStuecke){
        String SQL;
        PreparedStatement pstmt;
        Statement stmt;
        ResultSet rs;
        int artnr = 0, lnr = 0, bestnr = 0, stuecke = 0;
        double wert = 0;
        SQL = "Select * from lagerbestand where bstnr="+bstnr;
        try{
            stmt = ConnectionManager.con.createStatement();
            rs = stmt.executeQuery(SQL);
            while(rs.next()){
                artnr = rs.getInt("artnr");
                lnr = rs.getInt("lnr");
                stuecke = rs.getInt("stuecke");
                wert = rs.getDouble("wert");
            }

        }catch(SQLException e){
            e.printStackTrace();
        }
        //System.out.println("zeile zum kopieren geholt, bstnr: "+bstnr);

        SQL = "Insert into lagerbestand(artnr, lnr, stuecke, wert, bestnr) values(?,?,?, ?, ?);";
        try{
            pstmt = ConnectionManager.con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);

            //auf bstnr ist autoinc
            pstmt.setInt(1, artnr);
            pstmt.setInt(2, lnr);
            pstmt.setInt(3, (stuecke-eStuecke));
            pstmt.setDouble(4, wert);
            pstmt.setNull(5, Types.INTEGER);
            pstmt.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
        //System.out.println("zeile eingefügt mit neuer bstnummer und allen selben werten.");
    }

    public static void writeUpdateOnLagerbestand(int bestnr, int eStuecke, int bstnr){
        Statement stmt;
        int succ = 0;
        String SQL = "update lagerbestand set bestnr = "+bestnr+", stuecke = "+eStuecke+" where bstnr="+bstnr+";";
        try{
            stmt = ConnectionManager.con.createStatement();
            succ = stmt.executeUpdate(SQL);
        }catch(SQLException e){
            e.printStackTrace();
        }
       if(succ < 0){
            System.out.println("Fehler beim Schreiben der Bestellung.");
	   }

    }
}